package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.Malha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MalhaRepository extends JpaRepository<Malha, Long> {

    Optional<Malha> findByCodigoIgnoreCase(String codigo);
}
