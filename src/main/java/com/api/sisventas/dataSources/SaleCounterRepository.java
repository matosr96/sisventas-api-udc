package com.api.sisventas.dataSources;

import com.api.sisventas.models.SaleCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleCounterRepository extends JpaRepository<SaleCounter, Integer> {

    /** Bloqueo pesimista: serializa la toma del correlativo entre ventas simultáneas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SaleCounter> findByYear(Integer year);
}
