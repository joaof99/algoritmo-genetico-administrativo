package com.genetico.service;

import com.genetico.dto.AlgoritmoGeneticoRequest;
import com.genetico.dto.EnderecoRequest;
import com.genetico.exception.SolicitacaoRoteirizacaoException;
import com.genetico.model.Endereco;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RotaRepository;
import com.genetico.repository.RoteirizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;

@Service
public class AlgoritmoGeneticoService {
    private final Logger log = LoggerFactory.getLogger(AlgoritmoGeneticoService.class);
    private final RotaRepository rotaRepository;
    private final RoteirizacaoRepository roteirizacaoRepository;
    private final DistanciaService distanciaService;
    private final Clock clock;

    private final RestClient restClient;

    public AlgoritmoGeneticoService(RestClient restClient, RotaRepository rotaRepository, RoteirizacaoRepository roteirizacaoRepository, DistanciaService distanciaService, Clock clock) {
        this.restClient = restClient;
        this.rotaRepository = rotaRepository;
        this.roteirizacaoRepository = roteirizacaoRepository;
        this.distanciaService = distanciaService;
        this.clock = clock;
    }

    public void solicitarRoteirizacao(int idRota, int tamanhoPopulacao, int quantidadeGeracoes, int chanceOcorrenciaMutacao, int chanceOcorrenciaCrossover) {
        var rota = rotaRepository.buscarPorIdComEnderecos(idRota)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Rota de ID %d não encontrada", idRota)
                ));

        var enderecos = rota.getEnderecos();

        var idsEnderecos = enderecos
                .stream()
                .map(Endereco::getId)
                .toList();

        var distancias = distanciaService.obterDistancias(idsEnderecos);

        var roteirizacao = roteirizacaoRepository.save(new Roteirizacao(rota,
                tamanhoPopulacao,
                quantidadeGeracoes,
                chanceOcorrenciaMutacao,
                chanceOcorrenciaCrossover,
                Instant.now(clock)));

        var request = new AlgoritmoGeneticoRequest(roteirizacao.getId(),
                distancias,
                enderecos.stream()
                        .map(endereco -> new EnderecoRequest(endereco.getId(), endereco.getLatitude(), endereco.getLongitude()))
                        .toList(),
                tamanhoPopulacao,
                quantidadeGeracoes,
                chanceOcorrenciaMutacao,
                chanceOcorrenciaCrossover);

        try {
            restClient.post()
                    .uri("/ag/iniciar")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Não foi possível solicitar a roteirização de ID {} ao algoritmo genético", roteirizacao.getId(), e);

            roteirizacao.falhar(String.format("Não foi possível solicitar a roteirização ao algoritmo genético: %s", e.getMessage()), Instant.now(clock));
            roteirizacaoRepository.save(roteirizacao);

            throw new SolicitacaoRoteirizacaoException("Não foi possível solicitar a roteirização ao algoritmo genético", e);
        }
    }
}
