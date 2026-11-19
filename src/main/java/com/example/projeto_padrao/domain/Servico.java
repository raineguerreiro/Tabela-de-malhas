package com.example.projeto_padrao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    /** Posicao do servico no fluxo produtivo (ex.: 1 = Compra de Fio, 2 = Tecelagem...). */
    private Integer ordemFluxo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getOrdemFluxo() {
        return ordemFluxo;
    }

    public void setOrdemFluxo(Integer ordemFluxo) {
        this.ordemFluxo = ordemFluxo;
    }
}
