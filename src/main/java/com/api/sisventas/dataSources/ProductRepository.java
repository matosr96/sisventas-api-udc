package com.api.sisventas.dataSources;

import com.api.sisventas.models.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsByCategoryId(Long categoryId);

    /**
     * Carga el producto bloqueando su fila (SELECT ... FOR UPDATE). Obligatoria para toda
     * operación que mueva stock: sin ella dos transacciones leen el mismo saldo y cada una
     * escribe el suyo, con lo que se vende de más y el libro repite saldos.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findForUpdateById(Long id);
}
