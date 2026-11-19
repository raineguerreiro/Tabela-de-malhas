package com.example.projeto_padrao.repository;

import com.example.projeto_padrao.domain.CustoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustoServicoRepository extends JpaRepository<CustoServico, Long> {

    List<CustoServico> findByMalhaId(Long malhaId);

    Optional<CustoServico> findByMalhaIdAndServicoIdAndFornecedorIdAndDataReferencia(
            Long malhaId, Long servicoId, Long fornecedorId, LocalDate dataReferencia);
}
