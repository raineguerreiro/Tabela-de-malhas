package com.example.projeto_padrao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Configuracao de uma consulta ao banco do ERP: o SQL a executar e como mapear as colunas do
 * resultado para os campos de {@link CustoServico}. Editavel via API/tela, sem precisar de deploy,
 * para permitir trocar a tabela/consulta de origem quando necessario.
 */
@Entity
public class ErpConsultaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Lob
    @Column(length = 4000)
    private String sqlConsulta;

    // Nome das colunas retornadas pelo sqlConsulta que correspondem a cada campo do custo.
    @NotBlank
    private String colunaMalhaCodigo;

    @NotBlank
    private String colunaServicoNome;

    @NotBlank
    private String colunaFornecedorNome;

    @NotBlank
    private String colunaValorUnitario;

    private String colunaQuantidade;

    private String colunaUnidadeMedida;

    private String colunaDataReferencia;

    private boolean ativo = true;

    private LocalDateTime ultimaExecucao;

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

    public String getSqlConsulta() {
        return sqlConsulta;
    }

    public void setSqlConsulta(String sqlConsulta) {
        this.sqlConsulta = sqlConsulta;
    }

    public String getColunaMalhaCodigo() {
        return colunaMalhaCodigo;
    }

    public void setColunaMalhaCodigo(String colunaMalhaCodigo) {
        this.colunaMalhaCodigo = colunaMalhaCodigo;
    }

    public String getColunaServicoNome() {
        return colunaServicoNome;
    }

    public void setColunaServicoNome(String colunaServicoNome) {
        this.colunaServicoNome = colunaServicoNome;
    }

    public String getColunaFornecedorNome() {
        return colunaFornecedorNome;
    }

    public void setColunaFornecedorNome(String colunaFornecedorNome) {
        this.colunaFornecedorNome = colunaFornecedorNome;
    }

    public String getColunaValorUnitario() {
        return colunaValorUnitario;
    }

    public void setColunaValorUnitario(String colunaValorUnitario) {
        this.colunaValorUnitario = colunaValorUnitario;
    }

    public String getColunaQuantidade() {
        return colunaQuantidade;
    }

    public void setColunaQuantidade(String colunaQuantidade) {
        this.colunaQuantidade = colunaQuantidade;
    }

    public String getColunaUnidadeMedida() {
        return colunaUnidadeMedida;
    }

    public void setColunaUnidadeMedida(String colunaUnidadeMedida) {
        this.colunaUnidadeMedida = colunaUnidadeMedida;
    }

    public String getColunaDataReferencia() {
        return colunaDataReferencia;
    }

    public void setColunaDataReferencia(String colunaDataReferencia) {
        this.colunaDataReferencia = colunaDataReferencia;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getUltimaExecucao() {
        return ultimaExecucao;
    }

    public void setUltimaExecucao(LocalDateTime ultimaExecucao) {
        this.ultimaExecucao = ultimaExecucao;
    }
}
