package com.example.projeto_padrao.service;

import java.math.BigDecimal;

/**
 * Resultado do calculo do "Preco da Malha" de uma linha de Rotativo pela regra da sua tecnica
 * (ver {@link RegraPrecoRotativo}): o valor (null quando nao deu pra calcular), a situacao e a
 * memoria de calculo com os numeros da linha, exibida na tela ao passar o mouse.
 */
public record ResultadoPrecoRotativo(BigDecimal valor, Situacao situacao, String memoria) {

    public enum Situacao {
        /** Regra aplicada, {@code valor} preenchido. */
        CALCULADO,
        /** Tecnica com regra definida, mas sem preco base cadastrado no Banco de dados para a combinacao. */
        SEM_PRECO_BASE,
        /** Regra precisa do Preco da Malha da aba Tingimentos, mas nenhuma linha de la bate com o rotativo. */
        SEM_TINGIMENTO,
        /** Linha nao se encaixa em nenhum cenario da regra da sua tecnica. */
        FORA_DA_REGRA,
        /** Tecnica escolhida ainda nao tem regra de preco definida. */
        REGRA_A_DEFINIR,
        /** Linha sem tecnica escolhida. */
        SEM_TECNICA
    }
}
