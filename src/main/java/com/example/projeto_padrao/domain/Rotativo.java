package com.example.projeto_padrao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Custo de rotativo de uma combinacao referencia (malha) + tamanho + cor, dentro de uma tabela
 * de precos. Layout flat, uma linha por combinacao - mesmo formato usado em {@link Tingimento}.
 * Vem hoje de upload manual de planilha Excel (ver RotativoImportService), colunas: REFERENCIA,
 * TAMANHO, COR, DESCRICAO, NOMECOR, PRECO, TABELA.
 */
@Entity
public class Rotativo {

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

    /** Tecnica escolhida na tela (ex.: CILINDRO, DIGITAL, SUBLIMACAO, ROT. QUADRO) - nulavel, editada linha a linha. */
    private String tecnica;

    /** Tecido base digitado na tela - manual por enquanto, no futuro vem do ERP. */
    private String tecidoBase;

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

    public String getTecnica() {
        return tecnica;
    }

    public void setTecnica(String tecnica) {
        this.tecnica = tecnica;
    }

    public String getTecidoBase() {
        return tecidoBase;
    }

    public void setTecidoBase(String tecidoBase) {
        this.tecidoBase = tecidoBase;
    }
}
