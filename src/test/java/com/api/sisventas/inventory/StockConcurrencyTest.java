package com.api.sisventas.inventory;

import com.api.sisventas.businessLogic.inventory.ListStockMovements;
import com.api.sisventas.businessLogic.product.CreateProduct;
import com.api.sisventas.businessLogic.product.GetProduct;
import com.api.sisventas.businessLogic.sale.CreateSale;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;
import com.api.sisventas.models.dtos.product.CreateProductRequest;
import com.api.sisventas.models.dtos.product.ProductResponse;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.SaleItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ventas simultáneas sobre el mismo producto no pueden vender más de lo que hay ni dejar
 * el libro contradictorio. Sin bloqueo de fila, varias transacciones leen el mismo saldo
 * y cada una escribe "saldo - 1": se vende de más y el libro repite saldos.
 */
@SpringBootTest
class StockConcurrencyTest {

    private static final String TESTER = "race-tester";
    private static final int INITIAL_STOCK = 10;
    private static final int CONCURRENT_SALES = 20;

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CreateProduct createProduct;
    @Autowired private GetProduct getProduct;
    @Autowired private CreateSale createSale;
    @Autowired private ListStockMovements listStockMovements;

    @BeforeEach
    void signInAsTester() {
        if (userRepository.findByUsername(TESTER).isEmpty()) {
            Role role = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
            User user = new User();
            user.setFirstName("Race");
            user.setLastName("Tester");
            user.setUsername(TESTER);
            user.setPassword("not-used");
            user.setRoles(Set.of(role));
            userRepository.save(user);
        }
        signIn();
    }

    private static void signIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TESTER, null, List.of()));
    }

    @Test
    void concurrentSalesNeverOversellNorCorruptTheLedger() throws Exception {
        ProductResponse product = createProduct.execute(new CreateProductRequest(
                "RACE-" + System.nanoTime(), "Producto disputado", null, new BigDecimal("1.00"),
                INITIAL_STOCK, null, null, null, null));

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_SALES);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> outcomes = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_SALES; i++) {
            outcomes.add(pool.submit(sellOneUnit(product.id(), start)));
        }
        start.countDown();
        int accepted = 0;
        for (Future<Boolean> outcome : outcomes) {
            if (outcome.get(30, TimeUnit.SECONDS)) {
                accepted++;
            }
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        signIn();
        int remaining = getProduct.execute(product.id()).currentStock();
        assertEquals(INITIAL_STOCK, accepted, "solo caben tantas ventas como unidades había");
        assertEquals(0, remaining, "el stock termina exactamente en cero");

        List<Integer> balances = listStockMovements.execute(product.id(), 1, 100).items().stream()
                .map(StockMovementResponse::stockAfter)
                .toList();
        assertEquals(INITIAL_STOCK + 1, balances.size(), "un asiento INITIAL más uno por venta aceptada");
        assertEquals(balances.size(), balances.stream().distinct().count(),
                "cada asiento deja un saldo distinto: dos iguales significan una lectura repetida");
    }

    /** Cada hilo firma como el mismo usuario: el contexto de seguridad es por hilo. */
    private Callable<Boolean> sellOneUnit(Long productId, CountDownLatch start) {
        return () -> {
            signIn();
            start.await();
            try {
                createSale.execute(new CreateSaleRequest(null, List.of(new SaleItemRequest(productId, 1))));
                return true;
            } catch (DomainError error) {
                assertEquals(ErrorCodes.INSUFFICIENT_STOCK, error.code());
                return false;
            }
        };
    }
}
