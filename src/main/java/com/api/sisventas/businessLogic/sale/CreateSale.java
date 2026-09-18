package com.api.sisventas.businessLogic.sale;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.businessLogic.document.NextDocumentNumber;
import com.api.sisventas.businessLogic.inventory.RecordStockMovement;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.SaleRepository;
import com.api.sisventas.models.DocumentKind;
import com.api.sisventas.models.PaymentMethod;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.ProductStatus;
import com.api.sisventas.models.Sale;
import com.api.sisventas.models.SaleItem;
import com.api.sisventas.models.StockMovement;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.sale.CreateSaleRequest;
import com.api.sisventas.models.dtos.sale.SaleItemRequest;
import com.api.sisventas.models.dtos.sale.SaleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Registra una venta: congela el precio de cada línea, descuenta el stock y calcula
 * subtotal, descuento, impuesto (con la tasa configurada) y total. En efectivo comprueba
 * que lo entregado cubre el total y guarda el cambio. Todo ocurre en la misma
 * transacción, así que una línea sin stock deshace la venta entera.
 */
@Service
public class CreateSale {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int MONEY_SCALE = 2;

    private final SaleRepository saleRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;
    private final NextDocumentNumber nextDocumentNumber;
    private final RecordStockMovement recordStockMovement;
    private final BigDecimal taxRate;

    public CreateSale(SaleRepository saleRepository,
                      GetAuthenticatedUser getAuthenticatedUser,
                      NextDocumentNumber nextDocumentNumber,
                      RecordStockMovement recordStockMovement,
                      @Value("${app.sales.tax-rate}") BigDecimal taxRate) {
        this.saleRepository = saleRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
        this.nextDocumentNumber = nextDocumentNumber;
        this.recordStockMovement = recordStockMovement;
        this.taxRate = taxRate;
    }

    @Transactional
    public SaleResponse execute(CreateSaleRequest request) {
        User seller = getAuthenticatedUser.execute();
        Sale sale = new Sale();
        sale.setSaleDate(request.saleDate() == null ? Instant.now() : request.saleDate());
        sale.setSaleNumber(nextDocumentNumber.execute(DocumentKind.SALE, sale.getSaleDate()));
        sale.setUser(seller);
        sale.setCustomerName(blankToNull(request.customerName()));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleItemRequest line : request.items()) {
            SaleItem item = buildItem(line, sale.getSaleNumber(), seller);
            sale.addItem(item);
            subtotal = subtotal.add(item.getSubtotal());
        }
        applyAmounts(sale, subtotal, request.discount() == null ? BigDecimal.ZERO : request.discount());
        applyPayment(sale, request.paymentMethod(), request.amountPaid());
        return SaleResponse.from(saleRepository.save(sale));
    }

    /** El libro carga el producto con bloqueo: por eso no se consulta antes por aquí. */
    private SaleItem buildItem(SaleItemRequest line, String saleNumber, User seller) {
        StockMovement movement = recordStockMovement.execute(
                line.productId(), StockMovementType.SALE, -line.quantity(), saleNumber, null, seller);
        Product product = movement.getProduct();
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new DomainError(ErrorCodes.PRODUCT_INACTIVE);
        }
        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(line.quantity());
        item.setUnitPrice(product.getSalePrice());
        item.setSubtotal(product.getSalePrice().multiply(BigDecimal.valueOf(line.quantity())));
        return item;
    }

    private void applyAmounts(Sale sale, BigDecimal subtotal, BigDecimal discount) {
        if (discount.compareTo(subtotal) > 0) {
            throw new DomainError(ErrorCodes.INVALID_DISCOUNT);
        }
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(taxRate).divide(HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
        sale.setSubtotal(subtotal);
        sale.setDiscount(discount.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        sale.setTaxRate(taxRate);
        sale.setTax(tax);
        sale.setTotal(taxable.add(tax));
    }

    /** Solo el efectivo lleva importe entregado y cambio; con tarjeta o transferencia se cobra exacto. */
    private void applyPayment(Sale sale, PaymentMethod method, BigDecimal amountPaid) {
        PaymentMethod paymentMethod = method == null ? PaymentMethod.CASH : method;
        sale.setPaymentMethod(paymentMethod);
        if (paymentMethod != PaymentMethod.CASH || amountPaid == null) {
            sale.setAmountPaid(null);
            sale.setChangeAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY));
            return;
        }
        if (amountPaid.compareTo(sale.getTotal()) < 0) {
            throw new DomainError(ErrorCodes.INSUFFICIENT_PAYMENT);
        }
        sale.setAmountPaid(amountPaid.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        sale.setChangeAmount(sale.getAmountPaid().subtract(sale.getTotal()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
