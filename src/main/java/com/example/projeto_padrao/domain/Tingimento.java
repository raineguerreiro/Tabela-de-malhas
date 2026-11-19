package com.example.projeto_padrao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Custo de tingimento de uma combinacao referencia (malha) + tamanho + cor, dentro de uma tabela
 * de precos. Layout flat, uma linha por combinacao - o mesmo formato que a futura sincronizacao
 * direta com o ERP vai usar. Vem hoje de upload manual de planilha Excel (ver
 * TingimentoImportService), colunas: REFERENCIA, TAMANHO, COR, DESCRICAO, NOMECOR, PRECO, TABELA.
 */
@Entity
public class Tingimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codigo da malha (ex.: "COT06"), o mesmo exibido na aba "Malhas" - sem FK, correlacionado so por texto. */
    @NotBlank
    private String referencia;

    /** Pode ser vazio ("") quando a linha nao especifica tamanho - nunca null, para o upsert funcionar entre reenvios. */
    private String tamanho;

    @NotBlank
    private String corCodigo;

    /** Descricao da referencia (malha), nao da cor. */
    private String descricao;

    private String corNome;

    private BigDecimal preco;

    /** Nome da tabela de precos a que esta linha pertence - vem de dentro do arquivo, nao de um campo do upload. */
    @NotBlank
    private String tabela;

    /** Sequencia da linha no arquivo importado, para a tela exibir na mesma ordem do Excel. */
    private Integer ordem;

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

    public String getCorCodigo() {
        return corCodigo;
    }

    public void setCorCodigo(String corCodigo) {
        this.corCodigo = corCodigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCorNome() {
        return corNome;
    }

    public void setCorNome(String corNome) {
        this.corNome = corNome;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getTabela() {
        return tabela;
    }

    public void setTabela(String tabela) {
        this.tabela = tabela;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
