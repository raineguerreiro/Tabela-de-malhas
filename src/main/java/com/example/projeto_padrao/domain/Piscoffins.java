package com.example.projeto_padrao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 * Valor unico (singleton) usado depois em calculos diversos - ex.: 0,9175. So existe uma linha
 * nesta tabela; a tela em /malhas cria/atualiza sempre a mesma.
 */
@Entity
public class Piscoffins {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sem limite pratico de casas decimais - o valor precisa ficar exatamente como o usuario digitar, sem arredondar. */
    @Column(precision = 19, scale = 4)
    private BigDecimal valor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
