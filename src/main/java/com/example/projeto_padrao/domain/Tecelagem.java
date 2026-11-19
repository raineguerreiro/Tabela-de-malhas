package com.example.projeto_padrao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Custo de tecelagem de um artigo/codigo/tear, com duas colunas de preco lado a lado - uma por
 * fornecedor - espelhando o layout real da planilha (que traz o nome e a vigencia de cada
 * fornecedor no cabecalho, nao em cada linha). Vem hoje de upload manual de planilha Excel (ver
 * TecelagemImportService); no futuro pode ser substituido por sincronizacao com o ERP.
 */
@Entity
public class Tecelagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String artigo;

    @NotBlank
    private String codigo;

    private String tear;

    private BigDecimal custoFornecedor1;

    private BigDecimal custoFornecedor2;

    /** Nome/vigencia do fornecedor da 1a coluna de custo (ex.: "Wilbert" / "a partir de julho"), lido do cabecalho do arquivo. */
    private String fornecedor1Nome;

    private String fornecedor1Vigencia;

    private String fornecedor2Nome;

    private String fornecedor2Vigencia;

    /** Sequencia da linha no arquivo importado, para a tela exibir na mesma ordem do Excel. */
    private Integer ordem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArtigo() {
        return artigo;
    }

    public void setArtigo(String artigo) {
        this.artigo = artigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTear() {
        return tear;
    }

    public void setTear(String tear) {
        this.tear = tear;
    }

    public BigDecimal getCustoFornecedor1() {
        return custoFornecedor1;
    }

    public void setCustoFornecedor1(BigDecimal custoFornecedor1) {
        this.custoFornecedor1 = custoFornecedor1;
    }

    public BigDecimal getCustoFornecedor2() {
        return custoFornecedor2;
    }

    public void setCustoFornecedor2(BigDecimal custoFornecedor2) {
        this.custoFornecedor2 = custoFornecedor2;
    }

    public String getFornecedor1Nome() {
        return fornecedor1Nome;
    }

    public void setFornecedor1Nome(String fornecedor1Nome) {
        this.fornecedor1Nome = fornecedor1Nome;
    }

    public String getFornecedor1Vigencia() {
        return fornecedor1Vigencia;
    }

    public void setFornecedor1Vigencia(String fornecedor1Vigencia) {
        this.fornecedor1Vigencia = fornecedor1Vigencia;
    }

    public String getFornecedor2Nome() {
        return fornecedor2Nome;
    }

    public void setFornecedor2Nome(String fornecedor2Nome) {
        this.fornecedor2Nome = fornecedor2Nome;
    }

    public String getFornecedor2Vigencia() {
        return fornecedor2Vigencia;
    }

    public void setFornecedor2Vigencia(String fornecedor2Vigencia) {
        this.fornecedor2Vigencia = fornecedor2Vigencia;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
