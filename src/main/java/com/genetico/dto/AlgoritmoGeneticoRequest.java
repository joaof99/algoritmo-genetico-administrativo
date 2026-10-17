package com.genetico.dto;

import java.util.List;

public record AlgoritmoGeneticoRequest(int idRoteirizacao,
                                       List<DistanciaResponse> distancias,
                                       List<EnderecoRequest> enderecos,
                                       int tamanhoPopulacao,
                                       int quantidadeGeracoes,
                                       int chanceOcorrenciaMutacao,
                                       int chanceOcorrenciaCrossover) {
}