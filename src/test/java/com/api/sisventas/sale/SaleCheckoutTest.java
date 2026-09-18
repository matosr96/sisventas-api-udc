package com.api.sisventas.sale;

import com.api.sisventas.businessLogic.product.CreateProduct;
import com.api.sisventas.businessLogic.product.GetProduct;
import com.api.sisventas.businessLogic.report.GetCashClosing;
import com.api.sisventas.businessLogic.report.GetSummary;
import com.api.sisventas.businessLogic.sale.CreateSale;
import com.api.sisventas.businessLogic.sale.CreateSaleReturn;
import com.api.sisventas.businessLogic.sale.DeleteSale;
import com.api.sisventas.businessLogic.sale.ListSales;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.RoleRepository;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.PaymentMethod;
import com.api.sisventas.models.Role;
import com.api.sisventas.models.RoleName;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.product.CreateProductRequest;
import com.api.sisventas.models.dtos.product.ProductResponse;
import com.api.sisventas.models.dtos.report.CashClosingResponse;
import com.api.sisventas.models.dtos.report.SummaryResponse;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.CreateSaleReturnRequest;
import com.api.sisventas.models.dtos.sale.SaleItemRequest;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import com.api.sisventas.models.dtos.sale.SaleReturnItemRequest;
import com.api.sisventas.models.dtos.sale.SaleReturnResponse;
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
 * El cobro y las devoluciones: impuesto y descuento calculados por el servidor, cambio solo
 * en efectivo, nunca se devuelve más de lo vendido, una venta con devoluciones no se anula,
 * y los reportes agregan en la base lo que estas operaciones escribieron.
 */
@SpringBootTest
class SaleCheckoutTest {

    private static final String TESTER = "checkout-tester";
    private static final BigDecimal PRICE = new BigDecimal("1000.00");
    private static final int STOCK = 10;

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CreateProduct createProduct;
    @Autowired private GetProduct getProduct;
    @Autowired private CreateSale createSale;
    @Autowired private CreateSaleReturn createSaleReturn;
    @Autowired private DeleteSale deleteSale;
    @Autowired private ListSales listSales;
    @Autowired private GetSummary getSummary;
    @Autowired private GetCashClosing getCashClosing;

    private Long productId;

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername(TESTER).isEmpty()) {
            Role role = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ADMIN)));
            User user = new User();
            user.setFirstName("Checkout");
            user.setLastName("Tester");
            user.setUsername(TESTER);
            user.setPassword("not-used");
            user.setRoles(Set.of(role));
            userRepository.save(user);
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TESTER, null, List.of()));
        ProductResponse product = createProduct.execute(new CreateProductRequest(
                "CHK-" + System.nanoTime(), "Producto de caja", null, PRICE, STOCK, null, null, null, null));
        productId = product.id();
    }

    private CreateSaleRequest sale(int quantity, BigDecimal discount, PaymentMethod method, BigDecimal paid) {
        return new CreateSaleRequest(null, List.of(new SaleItemRequest(productId, quantity)), discount, method, paid, null);
    }

    @Test
    void serverComputesDiscountTaxTotalAndChange() {
        // Tasa de prueba: 19 %. 3 × 1000 = 3000; -500 = 2500; +19 % = 2975; pagados 5000 → cambio 2025.
        SaleResponse sale = createSale.execute(
                sale(3, new BigDecimal("500"), PaymentMethod.CASH, new BigDecimal("5000")));
        assertEquals(new BigDecimal("3000.00"), sale.subtotal());
        assertEquals(new BigDecimal("500.00"), sale.discount());
        assertEquals(new BigDecimal("475.00"), sale.tax());
        assertEquals(new BigDecimal("2975.00"), sale.total());
        assertEquals(new BigDecimal("2025.00"), sale.changeAmount());

        SaleResponse card = createSale.execute(sale(1, null, PaymentMethod.CARD, new BigDecimal("9999")));
        assertEquals(PaymentMethod.CARD, card.paymentMethod());
        assertEquals(BigDecimal.ZERO.setScale(2), card.changeAmount(), "con tarjeta no hay cambio");
        assertEquals(null, card.amountPaid());
    }

    @Test
    void rejectsShortCashAndDiscountAboveSubtotal() {
        DomainError shortCash = assertThrows(DomainError.class,
                () -> createSale.execute(sale(1, null, PaymentMethod.CASH, new BigDecimal("100"))));
        assertEquals(ErrorCodes.INSUFFICIENT_PAYMENT, shortCash.code());
        DomainError discount = assertThrows(DomainError.class,
                () -> createSale.execute(sale(1, new BigDecimal("5000"), null, null)));
        assertEquals(ErrorCodes.INVALID_DISCOUNT, discount.code());
        assertEquals(STOCK, getProduct.execute(productId).currentStock(), "una venta rechazada no mueve stock");
    }

    @Test
    void partialReturnRestocksCapsAtSoldAndBlocksVoiding() {
        SaleResponse sale = createSale.execute(sale(4, null, null, null));
        Long saleItemId = sale.items().get(0).id();
        SaleReturnResponse first = createSaleReturn.execute(sale.id(), new CreateSaleReturnRequest(
                "Cliente se arrepintió", List.of(new SaleReturnItemRequest(saleItemId, 3))));
        assertTrue(first.returnNumber().startsWith("R-"));
        assertEquals(new BigDecimal("3000.00"), first.total());
        assertEquals(STOCK - 1, getProduct.execute(productId).currentStock(), "tres unidades vuelven al stock");

        DomainError tooMany = assertThrows(DomainError.class, () -> createSaleReturn.execute(sale.id(),
                new CreateSaleReturnRequest("otra", List.of(new SaleReturnItemRequest(saleItemId, 2)))));
        assertEquals(ErrorCodes.RETURN_EXCEEDS_SOLD, tooMany.code());

        DomainError voiding = assertThrows(DomainError.class, () -> deleteSale.execute(sale.id()));
        assertEquals(ErrorCodes.SALE_HAS_RETURNS, voiding.code());
    }

    @Test
    void listsFilterByPaymentMethodAndReportsAggregateInTheDatabase() {
        createSale.execute(sale(1, null, PaymentMethod.TRANSFER, null));
        createSale.execute(sale(2, null, PaymentMethod.CASH, null));
        assertTrue(listSales.execute(null, null, null, PaymentMethod.TRANSFER, null, null, null, 1, 100)
                .items().stream().allMatch(item -> item.paymentMethod() == PaymentMethod.TRANSFER));

        SummaryResponse summary = getSummary.execute();
        assertTrue(summary.todaySaleCount() >= 2);
        assertTrue(summary.todayTotal().compareTo(new BigDecimal("3570.00")) >= 0, "1190 + 2380 con 19 %");

        CashClosingResponse closing = getCashClosing.execute(null, null);
        assertEquals(closing.total().subtract(closing.returned()), closing.net());
        assertTrue(closing.byPaymentMethod().stream().anyMatch(b -> b.paymentMethod() == PaymentMethod.TRANSFER));
    }
}
