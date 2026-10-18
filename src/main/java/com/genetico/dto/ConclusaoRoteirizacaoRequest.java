package com.genetico.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConclusaoRoteirizacaoRequest(@NotNull @JsonProperty("melhor_fitness_populacao_inicial") Double melhorFitnessPopulacaoInicial,
                                           @NotNull @JsonProperty("melhor_fitness_populacao_final") Double melhorFitnessPopulacaoFinal,
                                           @NotEmpty @JsonProperty("melhor_rota") List<Integer> melhorRota,
                                           @NotEmpty @JsonProperty("melhores_fitness_por_geracao") List<@NotNull Double> melhoresFitnessPorGeracao) {
}
