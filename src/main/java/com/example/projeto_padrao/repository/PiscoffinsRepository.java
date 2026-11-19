package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.Piscoffins;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PiscoffinsRepository extends JpaRepository<Piscoffins, Long> {
}
