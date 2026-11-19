package com.example.projeto_padrao.domain;

import com.example.projeto_padrao.service.MemoriaCalculo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Um lancamento de custo: quanto um fornecedor cobrou por um servico feito em uma malha. */
@Entity
public class CustoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    private Malha malha;

    @NotNull
    @ManyToOne
    private Servico servico;

    @NotNull
    @ManyToOne
    private Fornecedor fornecedor;

    @NotNull
    private BigDecimal valorUnitario;

    /** Ex.: kg, m, un. */
    private String unidadeMedida;

    @NotNull
    private BigDecimal quantidade;

    private LocalDate dataReferencia;

    @Enumerated(EnumType.STRING)
    private OrigemCusto origem = OrigemCusto.MANUAL;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Malha getMalha() {
        return malha;
    }

    public void setMalha(Malha malha) {
        this.malha = malha;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public OrigemCusto getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemCusto origem) {
        this.origem = origem;
    }

    public BigDecimal getValorTotal() {
        if (valorUnitario == null || quantidade == null) {
            return BigDecimal.ZERO;
        }
        return valorUnitario.multiply(quantidade);
    }

    /** Memoria de calculo do Total (exibida ao passar o mouse): valor unitario x quantidade. */
    public String getMemoriaValorTotal() {
        if (valorUnitario == null || quantidade == null) {
            return "Valor unitário ou quantidade não preenchido - total considerado 0.";
        }
        return "R$ " + MemoriaCalculo.reais(valorUnitario) + " × " + MemoriaCalculo.numero(quantidade) + " = R$ "
                + MemoriaCalculo.reais(getValorTotal());
    }
}
