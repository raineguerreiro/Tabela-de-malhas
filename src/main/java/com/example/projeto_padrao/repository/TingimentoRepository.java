package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.Tingimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TingimentoRepository extends JpaRepository<Tingimento, Long> {

    List<Tingimento> findAllByOrderByOrdemAsc();

    List<Tingimento> findByTabelaOrderByOrdemAsc(String tabela);

    Optional<Tingimento> findTopByOrderByOrdemDesc();

    Optional<Tingimento> findByReferenciaAndTamanhoAndCorCodigoAndTabela(String referencia, String tamanho,
            String corCodigo, String tabela);
}
