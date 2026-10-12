package com.genetico.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roteirizacoes")
public class Roteirizacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private double melhorFitnessPopulacaoInicial;

    private double melhorFitnessPopulacaoFinal;

    private int quantidadeGeracoes;

    private int tamanhoPopulacao;

    private int chanceOcorrenciaCrossover;

    private int chanceOcorrenciaMutacao;

    public Roteirizacao() {

    }

    public Roteirizacao(double melhorFitnessPopulacaoInicial, double melhorFitnessPopulacaoFinal, int quantidadeGeracoes, int tamanhoPopulacao, int chanceOcorrenciaCrossover, int chanceOcorrenciaMutacao) {
        this.melhorFitnessPopulacaoInicial = melhorFitnessPopulacaoInicial;
        this.melhorFitnessPopulacaoFinal = melhorFitnessPopulacaoFinal;
        this.quantidadeGeracoes = quantidadeGeracoes;
        this.tamanhoPopulacao = tamanhoPopulacao;
        this.chanceOcorrenciaCrossover = chanceOcorrenciaCrossover;
        this.chanceOcorrenciaMutacao = chanceOcorrenciaMutacao;
    }

    public Integer getId() {
        return id;
    }

    public double getMelhorFitnessPopulacaoInicial() {
        return melhorFitnessPopulacaoInicial;
    }

    public double getMelhorFitnessPopulacaoFinal() {
        return melhorFitnessPopulacaoFinal;
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
}
