package com.api.sisventas.dataSources;

import com.api.sisventas.models.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /** Un proveedor con compras no se borra: se desactiva, para no romper el histórico. */
    boolean existsBySupplierId(Long supplierId);
}
