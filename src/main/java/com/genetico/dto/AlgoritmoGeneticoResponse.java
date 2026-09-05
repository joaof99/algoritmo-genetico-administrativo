package com.genetico.dto;

import com.genetico.model.Endereco;

import java.util.List;

public record AlgoritmoGeneticoResponse(List<DistanciaResponse> distancias, List<Endereco> enderecos){}
