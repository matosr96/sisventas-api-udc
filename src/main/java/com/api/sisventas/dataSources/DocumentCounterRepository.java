package com.api.sisventas.dataSources;

import com.api.sisventas.models.DocumentCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentCounterRepository extends JpaRepository<DocumentCounter, String> {


    /** Bloqueo pesimista: serializa la toma del correlativo entre documentos simultáneos. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DocumentCounter> findOneById(String id);
}
