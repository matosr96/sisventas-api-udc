package com.api.sisventas.dataSources;

import com.api.sisventas.models.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long>, JpaSpecificationExecutor<StockMovement> {

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    /** Un producto con asientos tiene histórico: se desactiva, no se borra. */
    boolean existsByProductId(Long productId);
}
