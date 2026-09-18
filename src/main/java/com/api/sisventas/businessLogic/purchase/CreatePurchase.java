package com.api.sisventas.businessLogic.purchase;

import com.api.sisventas.businessLogic.auth.GetAuthenticatedUser;
import com.api.sisventas.businessLogic.document.NextDocumentNumber;
import com.api.sisventas.businessLogic.inventory.RecordStockMovement;
import com.api.sisventas.common.DomainError;
import com.api.sisventas.common.ErrorCodes;
import com.api.sisventas.dataSources.PurchaseRepository;
import com.api.sisventas.dataSources.SupplierRepository;
import com.api.sisventas.models.DocumentKind;
import com.api.sisventas.models.Product;
import com.api.sisventas.models.ProductStatus;
import com.api.sisventas.models.Purchase;
import com.api.sisventas.models.PurchaseItem;
import com.api.sisventas.models.StockMovement;
import com.api.sisventas.models.StockMovementType;
import com.api.sisventas.models.Supplier;
import com.api.sisventas.models.SupplierStatus;
import com.api.sisventas.models.User;
import com.api.sisventas.models.dtos.purchase.CreatePurchaseRequest;
import com.api.sisventas.models.dtos.purchase.PurchaseItemRequest;
import com.api.sisventas.models.dtos.purchase.PurchaseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registra una compra a proveedor: congela el costo de cada línea, suma el stock con su
 * asiento y calcula el total. Todo en la misma transacción.
 */
@Service
public class CreatePurchase {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final GetAuthenticatedUser getAuthenticatedUser;
    private final NextDocumentNumber nextDocumentNumber;
    private final RecordStockMovement recordStockMovement;

    public CreatePurchase(PurchaseRepository purchaseRepository,
                          SupplierRepository supplierRepository,
                          GetAuthenticatedUser getAuthenticatedUser,
                          NextDocumentNumber nextDocumentNumber,
                          RecordStockMovement recordStockMovement) {
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.getAuthenticatedUser = getAuthenticatedUser;
        this.nextDocumentNumber = nextDocumentNumber;
        this.recordStockMovement = recordStockMovement;
    }

    @Transactional
    public PurchaseResponse execute(CreatePurchaseRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new DomainError(ErrorCodes.SUPPLIER_NOT_FOUND));
        if (supplier.getStatus() != SupplierStatus.ACTIVE) {
            throw new DomainError(ErrorCodes.SUPPLIER_INACTIVE);
        }
        User buyer = getAuthenticatedUser.execute();
        Purchase purchase = new Purchase();
        purchase.setPurchaseDate(request.purchaseDate() == null ? Instant.now() : request.purchaseDate());
        purchase.setPurchaseNumber(nextDocumentNumber.execute(DocumentKind.PURCHASE, purchase.getPurchaseDate()));
        purchase.setSupplier(supplier);
        purchase.setUser(buyer);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseItemRequest line : request.items()) {
            PurchaseItem item = buildItem(line, purchase.getPurchaseNumber(), buyer);
            purchase.addItem(item);
            total = total.add(item.getSubtotal());
        }
        purchase.setTotal(total);
        return PurchaseResponse.from(purchaseRepository.save(purchase));
    }

    /** El libro carga el producto con bloqueo: por eso no se consulta antes por aquí. */
    private PurchaseItem buildItem(PurchaseItemRequest line, String purchaseNumber, User buyer) {
        StockMovement movement = recordStockMovement.execute(
                line.productId(), StockMovementType.PURCHASE, line.quantity(), purchaseNumber, null, buyer);
        Product product = movement.getProduct();
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new DomainError(ErrorCodes.PRODUCT_INACTIVE);
        }
        // purchasePrice es el último costo pagado: cada compra lo actualiza.
        product.setPurchasePrice(line.unitCost());

        PurchaseItem item = new PurchaseItem();
        item.setProduct(product);
        item.setQuantity(line.quantity());
        item.setUnitCost(line.unitCost());
        item.setSubtotal(line.unitCost().multiply(BigDecimal.valueOf(line.quantity())));
        return item;
    }
}
