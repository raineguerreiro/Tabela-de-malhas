package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.Tecelagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TecelagemRepository extends JpaRepository<Tecelagem, Long> {

    List<Tecelagem> findAllByOrderByOrdemAsc();

    Optional<Tecelagem> findByArtigoAndCodigoAndTear(String artigo, String codigo, String tear);
}
