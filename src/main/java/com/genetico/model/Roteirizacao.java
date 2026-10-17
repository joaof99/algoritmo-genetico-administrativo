package com.genetico.model;

import com.genetico.enums.StatusRoteirizacao;
import com.genetico.exception.RoteirizacaoFinalizadaException;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "roteirizacoes")
public class Roteirizacao {
    private static final int TAMANHO_MAXIMO_MENSAGEM_ERRO = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rota_id")
    private Rota rota;

    @Enumerated(EnumType.STRING)
    private StatusRoteirizacao status;

    private Instant dataInicio;

    private Instant dataFim;

    private int quantidadeGeracoes;

    private int tamanhoPopulacao;

    private int chanceOcorrenciaCrossover;

    private int chanceOcorrenciaMutacao;

    private Double melhorFitnessPopulacaoInicial;

    private Double melhorFitnessPopulacaoFinal;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<Integer> melhorRota;

    private String mensagemErro;

    @Version
    private int versao;

    public Roteirizacao() {

    }

    public Roteirizacao(Rota rota, int tamanhoPopulacao, int quantidadeGeracoes, int chanceOcorrenciaMutacao, int chanceOcorrenciaCrossover, Instant dataInicio) {
        this.rota = rota;
        this.tamanhoPopulacao = tamanhoPopulacao;
        this.quantidadeGeracoes = quantidadeGeracoes;
        this.chanceOcorrenciaMutacao = chanceOcorrenciaMutacao;
        this.chanceOcorrenciaCrossover = chanceOcorrenciaCrossover;
        this.dataInicio = dataInicio;
        this.status = StatusRoteirizacao.EM_ANDAMENTO;
    }

    public void concluir(double melhorFitnessPopulacaoInicial, double melhorFitnessPopulacaoFinal, List<Integer> melhorRota, Instant dataFim) {
        validarEmAndamento();

        this.melhorFitnessPopulacaoInicial = melhorFitnessPopulacaoInicial;
        this.melhorFitnessPopulacaoFinal = melhorFitnessPopulacaoFinal;
        this.melhorRota = melhorRota;
        this.dataFim = dataFim;
        this.status = StatusRoteirizacao.CONCLUIDA;
    }

    public void falhar(String mensagemErro, Instant dataFim) {
        validarEmAndamento();

        this.mensagemErro = mensagemErro.length() > TAMANHO_MAXIMO_MENSAGEM_ERRO
                ? mensagemErro.substring(0, TAMANHO_MAXIMO_MENSAGEM_ERRO)
                : mensagemErro;
        this.dataFim = dataFim;
        this.status = StatusRoteirizacao.FALHA;
    }

    private void validarEmAndamento() {
        if (status != StatusRoteirizacao.EM_ANDAMENTO) {
            throw new RoteirizacaoFinalizadaException(id);
        }
    }

    public Integer getId() {
        return id;
    }

    public Rota getRota() {
        return rota;
    }

    public StatusRoteirizacao getStatus() {
        return status;
    }

    public Instant getDataInicio() {
        return dataInicio;
    }

    public Instant getDataFim() {
        return dataFim;
    }

    public int getQuantidadeGeracoes() {
        return quantidadeGeracoes;
    }

    public int getTamanhoPopulacao() {
        return tamanhoPopulacao;
    }

    public int getChanceOcorrenciaCrossover() {
        return chanceOcorrenciaCrossover;
    }

    public int getChanceOcorrenciaMutacao() {
        return chanceOcorrenciaMutacao;
    }

    public Double getMelhorFitnessPopulacaoInicial() {
        return melhorFitnessPopulacaoInicial;
    }

    public Double getMelhorFitnessPopulacaoFinal() {
        return melhorFitnessPopulacaoFinal;
    }

    public List<Integer> getMelhorRota() {
        return melhorRota;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }
}
