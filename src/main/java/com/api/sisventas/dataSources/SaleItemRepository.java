package com.api.sisventas.dataSources;

import com.api.sisventas.models.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    /** Un producto con ventas no se borra: se desactiva, para no romper el histórico. */
    boolean existsByProductId(Long productId);
}
