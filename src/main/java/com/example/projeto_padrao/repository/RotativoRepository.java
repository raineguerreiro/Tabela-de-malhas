package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.Rotativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RotativoRepository extends JpaRepository<Rotativo, Long> {

    List<Rotativo> findAllByOrderByOrdemAsc();

    List<Rotativo> findByTabelaOrderByOrdemAsc(String tabela);

    Optional<Rotativo> findTopByOrderByOrdemDesc();

    Optional<Rotativo> findByReferenciaAndTamanhoAndCorCodigoAndTabela(String referencia, String tamanho,
            String corCodigo, String tabela);
}
