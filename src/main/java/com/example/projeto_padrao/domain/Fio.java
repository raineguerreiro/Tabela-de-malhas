package com.example.projeto_padrao.domain;

import com.example.projeto_padrao.service.MemoriaCalculo;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Um fio cadastrado pelo codigo do ERP (1 + 5 + 3 + 6 digitos, concatenados). Descricao e
 * Custo Informado sao preenchidos manualmente ate a integracao com o ERP existir para fios;
 * quando existir, esses dois campos passam a ser preenchidos automaticamente a partir do codigo.
 */
@Entity
public class Fio {

    private static final BigDecimal FATOR_CONTRATIPO = new BigDecimal("0.9175");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String codigo;

    /** Abreviacao do fio como aparece na Tabela Geral (ex.: "10 OE", "30 PES") - correlaciona este cadastro com o fio usado nas malhas. */
    private String nomeAbreviado;

    private String descricao;

    private BigDecimal custoInformado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNomeAbreviado() {
        return nomeAbreviado;
    }

    public void setNomeAbreviado(String nomeAbreviado) {
        this.nomeAbreviado = nomeAbreviado;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getCustoInformado() {
        return custoInformado;
    }

    public void setCustoInformado(BigDecimal custoInformado) {
        this.custoInformado = custoInformado;
    }

    /** Sempre calculado a partir do custo informado - nunca digitado. */
    public BigDecimal getCustoContratipo() {
        return custoInformado == null ? null : custoInformado.multiply(FATOR_CONTRATIPO);
    }

    /** Memoria de calculo do Custo Contratipo (exibida ao passar o mouse). */
    public String getMemoriaCustoContratipo() {
        if (custoInformado == null) {
            return "Fio sem Custo Informado cadastrado.";
        }
        return "Custo Informado R$ " + MemoriaCalculo.reais(custoInformado) + " × 0,9175 = R$ "
                + MemoriaCalculo.reais(getCustoContratipo());
    }
}
