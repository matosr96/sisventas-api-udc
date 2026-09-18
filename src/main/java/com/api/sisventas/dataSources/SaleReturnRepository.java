package com.api.sisventas.dataSources;

import com.api.sisventas.models.SaleReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {

    List<SaleReturn> findBySaleIdOrderByCreatedAtDesc(Long saleId);

    /** Una venta con devoluciones no se anula: sus unidades ya volvieron en parte. */
    boolean existsBySaleId(Long saleId);
}
