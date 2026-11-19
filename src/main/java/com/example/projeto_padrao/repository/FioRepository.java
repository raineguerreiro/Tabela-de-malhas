package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.Fio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FioRepository extends JpaRepository<Fio, Long> {

    List<Fio> findAllByOrderByCodigoAsc();

    Optional<Fio> findByNomeAbreviado(String nomeAbreviado);
}
