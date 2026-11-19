package com.example.projeto_padrao.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Uma linha da planilha "TABELA GERAL .xls" (ficha tecnica de malhas), importada tal como esta
 * no arquivo original. Cada coluna da planilha vira um campo texto aqui - os valores sao mantidos
 * exatamente como exibidos no Excel (sem conversao/arredondamento), para servir de referencia na
 * composicao de precos.
 */
@Entity
public class TabelaGeralItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Posicao original da linha na planilha, para manter a ordem de exibicao. */
    private Integer ordem;

    /** true quando a linha e apenas um titulo de secao na planilha (sem valores nas demais colunas). */
    private boolean tituloSecao;

    /** Texto da linha, usado somente quando tituloSecao = true. */
    private String textoTituloSecao;

    /**
     * Indice da cor de preenchimento da linha no Excel (CellStyle.getFillForegroundColor(), lido
     * da coluna Malha) - usado por TabelaGeralAgrupador para saber onde comeca cada bloco de
     * malha: linhas do mesmo bloco compartilham a mesma cor de fundo na planilha.
     */
    private Short corLinha;

    private String malha;
    private String codigo;
    private String rendimento;
    private String tearFinuraDiametroAgulhas;
    private String composicaoDaMalha;
    private String participacaoDeCadaFio;
    private String fio;
    private String lfa;
    private String tamanhoDoPonto;
    private String fatorCobertura;
    private String fluxo;
    private String larguraFinalAcabado;
    private String larguraParaEncaixe;
    private String gramaturaPadrao;
    private String gramaturaLimites;
    private String encolhimentoSecadoraC;
    private String encolhimentoSecadoraL;
    private String encolhimentoVaralC;
    private String encolhimentoVaralL;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public boolean isTituloSecao() {
        return tituloSecao;
    }

    public void setTituloSecao(boolean tituloSecao) {
        this.tituloSecao = tituloSecao;
    }

    public String getTextoTituloSecao() {
        return textoTituloSecao;
    }

    public void setTextoTituloSecao(String textoTituloSecao) {
        this.textoTituloSecao = textoTituloSecao;
    }

    public Short getCorLinha() {
        return corLinha;
    }

    public void setCorLinha(Short corLinha) {
        this.corLinha = corLinha;
    }

    public String getMalha() {
        return malha;
    }

    public void setMalha(String malha) {
        this.malha = malha;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getRendimento() {
        return rendimento;
    }

    public void setRendimento(String rendimento) {
        this.rendimento = rendimento;
    }

    public String getTearFinuraDiametroAgulhas() {
        return tearFinuraDiametroAgulhas;
    }

    public void setTearFinuraDiametroAgulhas(String tearFinuraDiametroAgulhas) {
        this.tearFinuraDiametroAgulhas = tearFinuraDiametroAgulhas;
    }

    public String getComposicaoDaMalha() {
        return composicaoDaMalha;
    }

    public void setComposicaoDaMalha(String composicaoDaMalha) {
        this.composicaoDaMalha = composicaoDaMalha;
    }

    public String getParticipacaoDeCadaFio() {
        return participacaoDeCadaFio;
    }

    public void setParticipacaoDeCadaFio(String participacaoDeCadaFio) {
        this.participacaoDeCadaFio = participacaoDeCadaFio;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getLfa() {
        return lfa;
    }

    public void setLfa(String lfa) {
        this.lfa = lfa;
    }

    public String getTamanhoDoPonto() {
        return tamanhoDoPonto;
    }

    public void setTamanhoDoPonto(String tamanhoDoPonto) {
        this.tamanhoDoPonto = tamanhoDoPonto;
    }

    public String getFatorCobertura() {
        return fatorCobertura;
    }

    public void setFatorCobertura(String fatorCobertura) {
        this.fatorCobertura = fatorCobertura;
    }

    public String getFluxo() {
        return fluxo;
    }

    public void setFluxo(String fluxo) {
        this.fluxo = fluxo;
    }

    public String getLarguraFinalAcabado() {
        return larguraFinalAcabado;
    }

    public void setLarguraFinalAcabado(String larguraFinalAcabado) {
        this.larguraFinalAcabado = larguraFinalAcabado;
    }

    public String getLarguraParaEncaixe() {
        return larguraParaEncaixe;
    }

    public void setLarguraParaEncaixe(String larguraParaEncaixe) {
        this.larguraParaEncaixe = larguraParaEncaixe;
    }

    public String getGramaturaPadrao() {
        return gramaturaPadrao;
    }

    public void setGramaturaPadrao(String gramaturaPadrao) {
        this.gramaturaPadrao = gramaturaPadrao;
    }

    public String getGramaturaLimites() {
        return gramaturaLimites;
    }

    public void setGramaturaLimites(String gramaturaLimites) {
        this.gramaturaLimites = gramaturaLimites;
    }

    public String getEncolhimentoSecadoraC() {
        return encolhimentoSecadoraC;
    }

    public void setEncolhimentoSecadoraC(String encolhimentoSecadoraC) {
        this.encolhimentoSecadoraC = encolhimentoSecadoraC;
    }

    public String getEncolhimentoSecadoraL() {
        return encolhimentoSecadoraL;
    }

    public void setEncolhimentoSecadoraL(String encolhimentoSecadoraL) {
        this.encolhimentoSecadoraL = encolhimentoSecadoraL;
    }

    public String getEncolhimentoVaralC() {
        return encolhimentoVaralC;
    }

    public void setEncolhimentoVaralC(String encolhimentoVaralC) {
        this.encolhimentoVaralC = encolhimentoVaralC;
    }

    public String getEncolhimentoVaralL() {
        return encolhimentoVaralL;
    }

    public void setEncolhimentoVaralL(String encolhimentoVaralL) {
        this.encolhimentoVaralL = encolhimentoVaralL;
    }
}
