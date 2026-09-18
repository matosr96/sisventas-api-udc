package com.api.sisventas.dataSources;

import com.api.sisventas.models.Sale;
import com.api.sisventas.models.dtos.report.PaymentBreakdown;
import com.api.sisventas.models.dtos.report.SalesByDay;
import com.api.sisventas.models.dtos.report.SalesByUser;
import com.api.sisventas.models.dtos.report.TopProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Agregados para los reportes. Se calculan en la base, no trayendo todas las ventas a
 * memoria: es lo que hace que los informes sigan siendo instantáneos con años de datos.
 */
@Repository
public interface ReportRepository extends JpaRepository<Sale, Long> {

    @Query("select count(s) from Sale s where s.saleDate >= :from and s.saleDate < :to")
    long countSales(Instant from, Instant to);

    @Query("select coalesce(sum(s.total), 0) from Sale s where s.saleDate >= :from and s.saleDate < :to")
    BigDecimal sumSales(Instant from, Instant to);

    @Query("select count(p) from Purchase p where p.purchaseDate >= :from and p.purchaseDate < :to")
    long countPurchases(Instant from, Instant to);

    @Query("select coalesce(sum(p.total), 0) from Purchase p where p.purchaseDate >= :from and p.purchaseDate < :to")
    BigDecimal sumPurchases(Instant from, Instant to);

    @Query("select count(p) from Product p where p.status = com.api.sisventas.models.ProductStatus.ACTIVE")
    long countActiveProducts();

    @Query("select count(p) from Product p where p.status = com.api.sisventas.models.ProductStatus.ACTIVE"
            + " and p.lowStock is not null and p.currentStock <= p.lowStock")
    long countLowStock();

    /** Valor del inventario al último costo conocido; los productos sin costo no suman. */
    @Query("select coalesce(sum(p.currentStock * p.purchasePrice), 0) from Product p"
            + " where p.status = com.api.sisventas.models.ProductStatus.ACTIVE and p.purchasePrice is not null")
    BigDecimal inventoryValue();

    @Query("select new com.api.sisventas.models.dtos.report.SalesByDay("
            + "cast(s.saleDate as java.time.LocalDate), count(s), coalesce(sum(s.total), 0))"
            + " from Sale s where s.saleDate >= :from and s.saleDate < :to"
            + " group by cast(s.saleDate as java.time.LocalDate) order by 1")
    List<SalesByDay> salesByDay(Instant from, Instant to);

    @Query("select new com.api.sisventas.models.dtos.report.SalesByUser("
            + "u.id, concat(u.firstName, ' ', u.lastName), count(s), coalesce(sum(s.total), 0))"
            + " from Sale s join s.user u where s.saleDate >= :from and s.saleDate < :to"
            + " group by u.id, u.firstName, u.lastName order by 4 desc")
    List<SalesByUser> salesByUser(Instant from, Instant to);

    @Query("select new com.api.sisventas.models.dtos.report.TopProduct("
            + "p.id, p.sku, p.name, sum(i.quantity - i.returnedQuantity),"
            + " coalesce(sum((i.quantity - i.returnedQuantity) * i.unitPrice), 0),"
            + " coalesce(sum((i.quantity - i.returnedQuantity) * (i.unitPrice - coalesce(p.purchasePrice, 0))), 0))"
            + " from SaleItem i join i.product p join i.sale s where s.saleDate >= :from and s.saleDate < :to"
            + " group by p.id, p.sku, p.name order by 5 desc")
    List<TopProduct> topProducts(Instant from, Instant to);

    @Query("select new com.api.sisventas.models.dtos.report.PaymentBreakdown("
            + "s.paymentMethod, count(s), coalesce(sum(s.total), 0))"
            + " from Sale s where s.saleDate >= :from and s.saleDate < :to"
            + " and (:userId is null or s.user.id = :userId)"
            + " group by s.paymentMethod order by 3 desc")
    List<PaymentBreakdown> paymentBreakdown(Instant from, Instant to, Long userId);

    @Query("select coalesce(sum(r.total), 0) from SaleReturn r join r.sale s"
            + " where r.createdAt >= :from and r.createdAt < :to and (:userId is null or s.user.id = :userId)")
    BigDecimal sumReturns(Instant from, Instant to, Long userId);
}
