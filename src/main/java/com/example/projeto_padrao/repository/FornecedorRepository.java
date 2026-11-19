package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Optional<Fornecedor> findByNomeIgnoreCase(String nome);
}
