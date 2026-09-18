package com.api.sisventas.inventory;

import com.api.sisventas.businessLogic.inventory.AdjustStock;
import com.api.sisventas.businessLogic.inventory.ListStockMovements;
import com.api.sisventas.businessLogic.product.CreateProduct;
import com.api.sisventas.businessLogic.product.GetProduct;
import com.api.sisventas.businessLogic.purchase.CreatePurchase;
import com.api.sisventas.businessLogic.purchase.DeletePurchase;
import com.api.sisventas.businessLogic.sale.CreateSale;
import com.api.sisventas.businessLogic.supplier.CreateSupplier;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.inventory.AdjustStockRequest;
import com.api.sisventas.models.dtos.inventory.StockMovementResponse;
import com.api.sisventas.models.dtos.product.CreateProductRequest;
import com.api.sisventas.models.dtos.product.ProductResponse;
import com.api.sisventas.models.dtos.purchase.CreatePurchaseRequest;
import com.api.sisventas.models.dtos.purchase.PurchaseItemRequest;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.SaleItemRequest;
import com.api.sisventas.models.dtos.supplier.CreateSupplierRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Invariantes del libro de stock: toda variación deja un asiento, el saldo nunca queda
 * negativo y las anulaciones se rechazan si la mercancía ya salió.
 */
@SpringBootTest
class StockLedgerTest {

    private static final String TESTER = "ledger-tester";

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CreateProduct createProduct;
    @Autowired private GetProduct getProduct;
    @Autowired private CreateSupplier createSupplier;
    @Autowired private CreatePurchase createPurchase;
    @Autowired private DeletePurchase deletePurchase;
    @Autowired private CreateSale createSale;
    @Autowired private AdjustStock adjustStock;
    @Autowired private ListStockMovements listStockMovements;

    @BeforeEach
    void signInAsTester() {
        if (userRepository.findByUsername(TESTER).isEmpty()) {
            Role role = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
            User user = new User();
            user.setFirstName("Ledger");
            user.setLastName("Tester");
            user.setUsername(TESTER);
            user.setPassword("not-used-in-this-test");
            user.setRoles(Set.of(role));
            userRepository.save(user);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TESTER, null, List.of()));
    }

    @Test
    void everyStockChangeLeavesOneEntryAndBalanceNeverGoesNegative() {
        ProductResponse product = createProduct.execute(new CreateProductRequest(
                "LEDGER-1", "Producto del libro", new BigDecimal("100.00"), new BigDecimal("150.00"),
                10, null, null, null, null));
        assertEquals(10, product.currentStock());

        Long supplierId = createSupplier.execute(
                new CreateSupplierRequest("Proveedor del libro", null, null, null)).id();
        PurchaseResponse purchase = createPurchase.execute(new CreatePurchaseRequest(
                supplierId, null, List.of(new PurchaseItemRequest(product.id(), 5, new BigDecimal("90.00")))));
        assertTrue(purchase.purchaseNumber().startsWith("P-"), "la compra recibe correlativo P-");
        assertEquals(new BigDecimal("450.00"), purchase.total());
        assertEquals(15, getProduct.execute(product.id()).currentStock());

        createSale.execute(new CreateSaleRequest(null, List.of(new SaleItemRequest(product.id(), 12))));
        assertEquals(3, getProduct.execute(product.id()).currentStock());

        adjustStock.execute(product.id(), new AdjustStockRequest(-3, "merma"));
        assertEquals(0, getProduct.execute(product.id()).currentStock());

        DomainError belowZero = assertThrows(DomainError.class,
                () -> adjustStock.execute(product.id(), new AdjustStockRequest(-1, "no hay")));
        assertEquals(ErrorCodes.INSUFFICIENT_STOCK, belowZero.code());

        DomainError voidSoldGoods = assertThrows(DomainError.class,
                () -> deletePurchase.execute(purchase.id()));
        assertEquals(ErrorCodes.INSUFFICIENT_STOCK, voidSoldGoods.code(),
                "no se anula una compra cuya mercancía ya se vendió");

        List<StockMovementResponse> ledger = listStockMovements.execute(product.id(), 1, 10).items();
        assertEquals(4, ledger.size(), "cuatro variaciones, cuatro asientos, ni uno más");
        // El listado va del más reciente al más antiguo.
        assertEquals(List.of(StockMovementType.ADJUSTMENT, StockMovementType.SALE,
                        StockMovementType.PURCHASE, StockMovementType.INITIAL),
                ledger.stream().map(StockMovementResponse::type).toList());
        assertEquals(List.of(0, 3, 15, 10),
                ledger.stream().map(StockMovementResponse::stockAfter).toList());
    }

    @Test
    void adjustmentOfZeroIsRejected() {
        ProductResponse product = createProduct.execute(new CreateProductRequest(
                "LEDGER-2", "Otro producto", null, new BigDecimal("1.00"), 0, null, null, null, null));
        DomainError error = assertThrows(DomainError.class,
                () -> adjustStock.execute(product.id(), new AdjustStockRequest(0, "nada")));
        assertEquals(ErrorCodes.INVALID_REQUEST, error.code());
    }
}
