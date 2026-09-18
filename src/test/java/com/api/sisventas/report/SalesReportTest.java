package com.api.sisventas.report;

import com.api.sisventas.businessLogic.product.CreateProduct;
import com.api.sisventas.businessLogic.report.GetSalesReport;
import com.api.sisventas.businessLogic.sale.CreateSale;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.product.CreateProductRequest;
import com.api.sisventas.models.dtos.product.ProductResponse;
import com.api.sisventas.models.dtos.report.SalesReportResponse;
import com.api.sisventas.models.dtos.report.TopProduct;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.SaleItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El informe agrega en la base: periodo por defecto, serie diaria, margen con el último costo y top acotado. */
@SpringBootTest
class SalesReportTest {

    private static final String TESTER = "report-tester";

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CreateProduct createProduct;
    @Autowired private CreateSale createSale;
    @Autowired private GetSalesReport getSalesReport;

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername(TESTER).isEmpty()) {
            Role role = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
            User user = new User();
            user.setFirstName("Report");
            user.setLastName("Tester");
            user.setUsername(TESTER);
            user.setPassword("not-used");
            user.setRoles(Set.of(role));
            userRepository.save(user);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TESTER, null, List.of()));
    }

    @Test
    void defaultPeriodIsThirtyDaysAndTheReportReflectsTodaysSalesWithMargin() {
        ProductResponse product = createProduct.execute(new CreateProductRequest(
                "RPT-" + System.nanoTime(), "Reportado", new BigDecimal("6.00"), new BigDecimal("10.00"), 20, null, null, null, null));
        createSale.execute(new CreateSaleRequest(null, List.of(new SaleItemRequest(product.id(), 2)), null, null, null, null));

        SalesReportResponse report = getSalesReport.execute(null, null);
        assertEquals(Duration.ofDays(30), Duration.between(report.from(), report.to()));
        assertTrue(report.count() >= 1);
        assertTrue(report.byDay().stream().anyMatch(day -> day.count() >= 1), "hoy aparece en la serie");
        TopProduct top = report.topProducts().stream().filter(item -> item.productId().equals(product.id())).findFirst().orElseThrow();
        assertEquals(2, top.quantity());
        assertEquals(new BigDecimal("20.00"), top.total());
        assertEquals(new BigDecimal("8.00"), top.margin(), "(10 - 6) × 2 con el último costo conocido");
        assertTrue(report.topProducts().size() <= 10);
        assertTrue(report.byUser().stream().anyMatch(user -> user.userName().equals("Report Tester")));
    }

    @Test
    void anEmptyPeriodReportsZeros() {
        SalesReportResponse report = getSalesReport.execute(LocalDate.of(2001, 1, 1), LocalDate.of(2001, 1, 2));
        assertEquals(0, report.count());
        assertEquals(0, BigDecimal.ZERO.compareTo(report.total()));
        assertTrue(report.byDay().isEmpty());
        assertTrue(report.topProducts().isEmpty());
    }
}
