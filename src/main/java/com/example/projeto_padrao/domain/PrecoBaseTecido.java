package com.example.projeto_padrao.domain;

import com.example.projeto_padrao.service.MemoriaCalculo;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Preco base de um tecido, por combinacao referencia + tamanho + tecido base - o "Banco de dados"
 * da aba Rotativos. As combinacoes sao montadas a partir dos {@link Rotativo} com Tecido Base
 * preenchido; aqui fica guardado so' o Preco digitado na tela para cada uma. Sem FK para Rotativo,
 * correlacionado so' por texto - o preco sobrevive a reimportacoes da planilha de rotativos.
 */
@Entity
public class PrecoBaseTecido {

    private static final BigDecimal FATOR_CONTRATIPO = new BigDecimal("0.9175");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String referencia;

    /** Pode ser vazio ("") quando o rotativo nao especifica tamanho - nunca null, igual a {@link Rotativo}. */
    private String tamanho;

    @NotBlank
    private String tecidoBase;

    private BigDecimal preco;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public String getTecidoBase() {
        return tecidoBase;
    }

    public void setTecidoBase(String tecidoBase) {
        this.tecidoBase = tecidoBase;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    /** Sempre calculado a partir do preco (Preco x 0,9175) - nunca digitado. */
    public BigDecimal getPrecoContratipo() {
        return preco == null ? null : preco.multiply(FATOR_CONTRATIPO);
    }

    /** Memoria de calculo do Preco Contratipo (exibida ao passar o mouse). */
    public String getMemoriaPrecoContratipo() {
        if (preco == null) {
            return null;
        }
        return "Preço R$ " + MemoriaCalculo.reais(preco) + " × 0,9175 = R$ " + MemoriaCalculo.reais(getPrecoContratipo());
    }
}
