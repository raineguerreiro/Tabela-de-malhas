package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.PrecoBaseTecido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrecoBaseTecidoRepository extends JpaRepository<PrecoBaseTecido, Long> {

    Optional<PrecoBaseTecido> findByReferenciaAndTamanhoAndTecidoBase(String referencia, String tamanho,
            String tecidoBase);
}
