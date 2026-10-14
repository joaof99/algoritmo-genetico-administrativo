package com.genetico.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RoteirizacaoRequest(@NotNull @JsonProperty("id_rota") Integer idRota,
                                  @NotNull @JsonProperty("melhor_fitness_populacao_inicial") Double melhorFitnessPopulacaoInicial,
                                  @NotNull @JsonProperty("melhor_fitness_populacao_final") Double melhorFitnessPopulacaoFinal,
                                  @NotNull @JsonProperty("qtde_geracoes") Integer quantidadeGeracoes,
                                  @NotNull @JsonProperty("tamanho_populacao") Integer tamanhoPopulacao,
                                  @NotNull @JsonProperty("prob_crossover") Integer chanceOcorrenciaCrossover,
                                  @NotNull @JsonProperty("prob_mutacao") Integer chanceOcorrenciaMutacao) {
}
