package com.genetico.service;

import com.genetico.dto.AlgoritmoGeneticoRequest;
import com.genetico.model.Endereco;
import com.genetico.repository.RotaRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlgoritmoGeneticoService {
    private final RotaRepository rotaRepository;
    private final DistanciaService distanciaService;

    private final RestClient restClient;

    public AlgoritmoGeneticoService(RestClient restClient, RotaRepository rotaRepository, DistanciaService distanciaService) {
        this.restClient = restClient;
        this.rotaRepository = rotaRepository;
        this.distanciaService = distanciaService;
    }

    @Transactional
    public void iniciarAlgoritmoGenetico(int idRota, int tamanhoPopulacao, int quantidadeGeracoes, int chanceOcorrenciaMutacao, int chanceOcorrenciaCrossover) {
        var rota = rotaRepository.findById(idRota)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Rota de ID %d não encontrada", idRota)
                ));

        var enderecos = rota.getEnderecos();

        var idsEnderecos = enderecos
                .stream()
                .map(Endereco::getId)
                .toList();

        var distanciaResponse = distanciaService.obterDistancias(idsEnderecos);
        var response = new AlgoritmoGeneticoRequest(distanciaResponse, enderecos, tamanhoPopulacao, quantidadeGeracoes, chanceOcorrenciaMutacao, chanceOcorrenciaCrossover);

        restClient.post()
                .uri("/ag/iniciar")
                .body(response)
                .retrieve()
                .toBodilessEntity();
    }
}

