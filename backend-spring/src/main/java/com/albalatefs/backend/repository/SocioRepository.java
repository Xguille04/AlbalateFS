package com.albalatefs.backend.repository;

import com.albalatefs.backend.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {
    boolean existsByDni(String dni);
}
