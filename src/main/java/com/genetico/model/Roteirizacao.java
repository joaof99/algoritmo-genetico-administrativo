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

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<Double> melhoresFitnessPorGeracao;

    private String mensagemErro;

    @Version
    private int versao;

    public Roteirizacao() {

    }

    private Roteirizacao(Builder builder) {
        this.rota = builder.rota;
        this.tamanhoPopulacao = builder.tamanhoPopulacao;
        this.quantidadeGeracoes = builder.quantidadeGeracoes;
        this.chanceOcorrenciaMutacao = builder.chanceOcorrenciaMutacao;
        this.chanceOcorrenciaCrossover = builder.chanceOcorrenciaCrossover;
        this.dataInicio = builder.dataInicio;
        this.status = StatusRoteirizacao.EM_ANDAMENTO;
    }

    public void concluir(double melhorFitnessPopulacaoInicial, double melhorFitnessPopulacaoFinal, List<Integer> melhorRota, List<Double> melhoresFitnessPorGeracao, Instant dataFim) {
        validarEmAndamento();

        this.melhorFitnessPopulacaoInicial = melhorFitnessPopulacaoInicial;
        this.melhorFitnessPopulacaoFinal = melhorFitnessPopulacaoFinal;
        this.melhorRota = melhorRota;
        this.melhoresFitnessPorGeracao = melhoresFitnessPorGeracao;
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

    public List<Double> getMelhoresFitnessPorGeracao() {
        return melhoresFitnessPorGeracao;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public static class Builder {
        private Rota rota;
        private int tamanhoPopulacao;
        private int quantidadeGeracoes;
        private int chanceOcorrenciaMutacao;
        private int chanceOcorrenciaCrossover;
        private Instant dataInicio;

        public Builder rota(Rota rota) {
            this.rota = rota;
            return this;
        }

        public Builder tamanhoPopulacao(int tamanhoPopulacao) {
            this.tamanhoPopulacao = tamanhoPopulacao;
            return this;
        }

        public Builder quantidadeGeracoes(int quantidadeGeracoes) {
            this.quantidadeGeracoes = quantidadeGeracoes;
            return this;
        }

        public Builder chanceOcorrenciaMutacao(int chanceOcorrenciaMutacao) {
            this.chanceOcorrenciaMutacao = chanceOcorrenciaMutacao;
            return this;
        }

        public Builder chanceOcorrenciaCrossover(int chanceOcorrenciaCrossover) {
            this.chanceOcorrenciaCrossover = chanceOcorrenciaCrossover;
            return this;
        }

        public Builder dataInicio(Instant dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public Roteirizacao build() {
            return new Roteirizacao(this);
        }
    }
}
