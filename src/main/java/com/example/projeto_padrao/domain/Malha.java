package com.example.projeto_padrao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

/** Ficha tecnica da malha: especificacoes usadas para identificar e precificar cada tipo produzido. */
@Entity
public class Malha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String codigo;

    @NotBlank
    private String nome;

    /** Ex.: "100% Algodao", "95% Algodao / 5% Elastano". */
    private String composicaoFio;

    /** Gramatura em g/m2. */
    private Double gramatura;

    /** Largura do tecido acabado, em cm. */
    private Double largura;

    /** Ex.: Jersey, Ribana, Moletom, Piquet. */
    private String tipoPonto;

    /** Cor de tingimento ou padrao de estampa aplicado. */
    private String corEstampa;

    private String observacoes;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getComposicaoFio() {
        return composicaoFio;
    }

    public void setComposicaoFio(String composicaoFio) {
        this.composicaoFio = composicaoFio;
    }

    public Double getGramatura() {
        return gramatura;
    }

    public void setGramatura(Double gramatura) {
        this.gramatura = gramatura;
    }

    public Double getLargura() {
        return largura;
    }

    public void setLargura(Double largura) {
        this.largura = largura;
    }

    public String getTipoPonto() {
        return tipoPonto;
    }

    public void setTipoPonto(String tipoPonto) {
        this.tipoPonto = tipoPonto;
    }

    public String getCorEstampa() {
        return corEstampa;
    }

    public void setCorEstampa(String corEstampa) {
        this.corEstampa = corEstampa;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
