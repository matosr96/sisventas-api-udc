package com.api.sisventas.security;

import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.AuditRepository;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lo que la matriz de autorización y los filtros prometen, comprobado por HTTP: quién
 * entra dónde, qué devuelve cada denegación, que una cuenta inactiva pierde su token
 * vigente, que toda escritura queda auditada y que el login y el registro tienen tope.
 *
 * Los tokens se emiten directamente con {@link JwtGenerator} para no consumir el cupo del
 * limitador de login; el limitador se prueba aparte con una IP propia.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HttpSecurityTest {

    private static final String ADMIN = "http-admin";
    private static final String USER = "http-user";
    private static final String PRODUCTS = "/api/v1/products";
    private static final String JSON_PRODUCT = "{\"sku\":\"HTTP-%d\",\"name\":\"x\",\"salePrice\":1,\"initialStock\":1}";

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtGenerator jwtGenerator;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuditRepository auditRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        Role admin = roleRepository.findByName(RoleName.ADMIN).orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
        Role user = roleRepository.findByName(RoleName.USER).orElseGet(() -> roleRepository.save(new Role(null, RoleName.USER)));
        userRepository.findByUsername(ADMIN).orElseGet(() -> save(ADMIN, Set.of(admin)));
        userRepository.findByUsername(USER).orElseGet(() -> save(USER, Set.of(user)));
        adminToken = tokenFor(ADMIN, "ADMIN");
        userToken = tokenFor(USER, "USER");
    }

    private User save(String username, Set<Role> roles) {
        User u = new User();
        u.setFirstName("Http");
        u.setLastName("Test");
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode("password123"));
        u.setRoles(roles);
        return userRepository.save(u);
    }

    private String tokenFor(String username, String authority) {
        return jwtGenerator.generateToken(new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(authority))));
    }

    private static MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token).with(r -> {
            r.setRemoteAddr("10.1.1.1");
            return r;
        });
    }

    @Test
    void anonymousGets401WithTheErrorContract() throws Exception {
        mockMvc.perform(get(PRODUCTS))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("611"));
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void userCanReadButNotWriteTheCatalogAndGets403NotAuthenticationError() throws Exception {
        mockMvc.perform(as(get(PRODUCTS), userToken)).andExpect(status().isOk());
        mockMvc.perform(as(post(PRODUCTS), userToken).contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_PRODUCT.formatted(1)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("613"));
        mockMvc.perform(as(get("/api/v1/users"), userToken)).andExpect(status().isForbidden());
        mockMvc.perform(as(get("/api/v1/users/me"), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USER));
    }

    @Test
    void adminWritesAreAuditedAndFailedOnesAreNot() throws Exception {
        long before = auditRepository.count();
        mockMvc.perform(as(post(PRODUCTS), adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_PRODUCT.formatted(System.nanoTime())))
                .andExpect(status().isCreated());
        mockMvc.perform(as(delete(PRODUCTS + "/999999"), adminToken))
                .andExpect(status().isNotFound());
        assertEquals(before + 1, auditRepository.count(), "una escritura exitosa, un asiento; la fallida no cuenta");
    }

    @Test
    void forgedTokenAndDeactivatedAccountAreBothRejected() throws Exception {
        String forged = adminToken.substring(0, adminToken.length() - 2) + "xx";
        mockMvc.perform(as(get(PRODUCTS), forged)).andExpect(status().isUnauthorized());

        User victim = save("http-victim-" + System.nanoTime(), Set.of(roleRepository.findByName(RoleName.USER).orElseThrow()));
        String victimToken = tokenFor(victim.getUsername(), "USER");
        mockMvc.perform(as(get(PRODUCTS), victimToken)).andExpect(status().isOk());

        mockMvc.perform(as(put("/api/v1/users/" + victim.getId() + "/status"), adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(UserStatus.INACTIVE.name()));
        mockMvc.perform(as(get(PRODUCTS), victimToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedInputIsA400WithTheErrorContractNeverA500() throws Exception {
        mockMvc.perform(as(post(PRODUCTS), adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"X\",\"categoryId\":}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCodes.INVALID_REQUEST));
        mockMvc.perform(as(post(PRODUCTS), adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"X\",\"name\":\"x\",\"salePrice\":\"caro\",\"initialStock\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCodes.INVALID_REQUEST));
        mockMvc.perform(as(get(PRODUCTS + "/not-a-number"), adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCodes.INVALID_REQUEST));
    }

    @Test
    void logoutEverywhereInvalidatesTheTokensIssuedBefore() throws Exception {
        User account = save("http-logout-" + System.nanoTime(), Set.of(roleRepository.findByName(RoleName.USER).orElseThrow()));
        String token = mockMvc.perform(post("/api/v1/auth/signin").with(r -> { r.setRemoteAddr("10.2.2.2"); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + account.getUsername() + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
        mockMvc.perform(as(get(PRODUCTS), token)).andExpect(status().isOk());
        mockMvc.perform(as(post("/api/v1/users/me/logout-all"), token)).andExpect(status().isNoContent());
        mockMvc.perform(as(get(PRODUCTS), token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsAcceptFiltersAndRejectUnknownSortSilently() throws Exception {
        mockMvc.perform(as(get(PRODUCTS + "?search=zzz-nothing&status=ACTIVE&lowStock=true&sort=hacker&dir=asc"), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
        mockMvc.perform(as(get("/api/v1/audits"), userToken)).andExpect(status().isForbidden());
        mockMvc.perform(as(get("/api/v1/audits?method=POST"), adminToken)).andExpect(status().isOk());
        mockMvc.perform(as(get("/api/v1/reports/summary"), userToken)).andExpect(status().isOk());
        mockMvc.perform(as(get("/api/v1/settings"), userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxRate").value(19));
    }

    @Test
    void invoicePdfIsServedAsPdf() throws Exception {
        String product = mockMvc.perform(as(post(PRODUCTS), adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_PRODUCT.formatted(System.nanoTime())))
                .andReturn().getResponse().getContentAsString();
        long productId = Long.parseLong(product.replaceAll(".*\"id\":(\\d+).*", "$1"));
        String sale = mockMvc.perform(as(post("/api/v1/sales"), adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":" + productId + ",\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long saleId = Long.parseLong(sale.replaceAll(".*\"id\":(\\d+).*", "$1"));

        byte[] pdf = mockMvc.perform(as(get("/api/v1/sales/" + saleId + "/pdf"), adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".pdf")))
                .andReturn().getResponse().getContentAsByteArray();
        assertTrue(new String(pdf, 0, 5).startsWith("%PDF-"), "empieza por la firma de un PDF");
        mockMvc.perform(as(get("/api/v1/sales/" + saleId + "/pdf?format=receipt"), adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("-receipt.pdf")));
    }

    @Test
    void signinAndSignupAreRateLimitedPerIp() throws Exception {
        String ip = "10.9.9." + (System.nanoTime() % 200 + 1);
        for (int i = 0; i < LoginRateLimitFilter.SIGNIN_MAX_ATTEMPTS; i++) {
            mockMvc.perform(post("/api/v1/auth/signin").with(r -> { r.setRemoteAddr(ip); return r; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"nobody\",\"password\":\"wrong-password\"}"))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/v1/auth/signin").with(r -> { r.setRemoteAddr(ip); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("640"));

        for (int i = 0; i < LoginRateLimitFilter.SIGNUP_MAX_ATTEMPTS; i++) {
            mockMvc.perform(post("/api/v1/auth/signup").with(r -> { r.setRemoteAddr(ip); return r; })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"firstName\":\"Bot\",\"lastName\":\"Test\",\"username\":\"bot-" + ip.replace('.', '-') + "-" + i
                                    + "\",\"password\":\"password123\"}"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/v1/auth/signup").with(r -> { r.setRemoteAddr(ip); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Bot\",\"lastName\":\"Test\",\"username\":\"bot-one-too-many\",\"password\":\"password123\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
