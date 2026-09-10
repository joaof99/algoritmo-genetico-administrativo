package com.genetico.service;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.dto.DistanciaResponse;
import com.genetico.model.CoordenadaGeografica;
import com.genetico.model.Distancia;
import com.genetico.model.DistanciaId;
import com.genetico.model.Endereco;
import com.genetico.repository.DistanciaRepository;
import com.genetico.repository.EnderecoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DistanciaService {
    private final Logger log = LoggerFactory.getLogger(DistanciaService.class);
    private final IntegracaoLocationIQAPI integracaoLocationIQAPI;
    private final EnderecoRepository enderecoRepository;
    private final DistanciaRepository distanciaRepository;

    public DistanciaService(IntegracaoLocationIQAPI integracaoLocationIQAPI, EnderecoRepository enderecoRepository, DistanciaRepository distanciaRepository) {
        this.integracaoLocationIQAPI = integracaoLocationIQAPI;
        this.enderecoRepository = enderecoRepository;
        this.distanciaRepository = distanciaRepository;
    }

    public double buscarDistancia(int origemId, int destinoId) {
        log.info("Buscando distância entre os pontos os endereços de ID {} e ID {} no banco de dados...", origemId, destinoId);
        return distanciaRepository.findById(new DistanciaId(origemId, destinoId))
                .map(Distancia::getDistancia)
                .orElseGet(() -> buscarDistanciaEmApi(origemId, destinoId));
    }

    private double buscarDistanciaEmApi(int origemId, int destinoId) {
        log.info("Buscando distância entre endereço de ID {} e ID {} na API...", origemId, destinoId);

        var origem = enderecoRepository.findById(origemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Origem de ID %d não encontrado", origemId)));

        var destino = enderecoRepository.findById(destinoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Destino de ID %d não encontrado", destinoId)));

        var distanciaAPI = integracaoLocationIQAPI.buscarDistanciaEntreCoordenadas(origem, destino);

        salvarDistanciaNoBanco(origemId, destinoId, origem, destino, distanciaAPI);

        return distanciaAPI;
    }

    private void salvarDistanciaNoBanco(int origemId, int destinoId, Endereco origem, Endereco destino, double distanciaAPI) {
        distanciaRepository.save(new Distancia(origem, destino, distanciaAPI));
        log.info("Distância entre endereço de ID {} e ID {} armazenada com sucesso na base de dados", origemId, destinoId);
    }

    public List<DistanciaResponse> obterDistancias(List<Integer> idsEnderecos) {
        var distanciasIds = criarDistanciasIds(idsEnderecos);
        var distanciasExistentesBanco = distanciaRepository.findAllById(distanciasIds);

        var enderecosParaBuscaAPI = buscarEnderecosComDistanciasFaltantes(
                idsEnderecos,
                criarIdsEnderecosSemTodasAsDistancias(distanciasIds, distanciasExistentesBanco)
        );

        if (enderecosParaBuscaAPI.isEmpty()) {
            log.info("Todos os endereços já possuem suas combinações de distâncias. Não é necessário busca na API");

            return converterParaResponse(distanciasExistentesBanco);
        }

        var distanciasParaSalvar = buscarDistanciasNovas(enderecosParaBuscaAPI, distanciasExistentesBanco);
        salvarDistancias(distanciasParaSalvar);

        return converterParaResponse(distanciasParaSalvar);
    }

    private List<DistanciaId> criarDistanciasIds(List<Integer> idsEnderecos) {
        var distanciasIds = new ArrayList<com.genetico.model.DistanciaId>();

        for (Integer idOrigem : idsEnderecos) {
            for (Integer idDestino : idsEnderecos) {
                if (idOrigem.equals(idDestino)) {
                    continue;
                }

                distanciasIds.add(new com.genetico.model.DistanciaId(idOrigem, idDestino));
            }
        }

        return distanciasIds;
    }

    private List<Endereco> buscarEnderecosComDistanciasFaltantes(List<Integer> idsEnderecos, Set<Integer> idsEnderecosComDistanciasFaltantes) {
        return enderecoRepository.findAllById(idsEnderecos).stream()
                .filter(endereco -> idsEnderecosComDistanciasFaltantes.contains(endereco.getId()))
                .toList();
    }

    private Set<Integer> criarIdsEnderecosSemTodasAsDistancias(List<DistanciaId> distanciasIds, List<Distancia> distanciasBanco) {
        var paresJaExistentesBanco = distanciasBanco.stream()
                .map(d -> new DistanciaId(
                        d.getOrigem().getId(),
                        d.getDestino().getId()
                ))
                .collect(Collectors.toSet());

        var paresFaltantesBanco = distanciasIds.stream()
                .filter(par -> !paresJaExistentesBanco.contains(par))
                .toList();

        return paresFaltantesBanco.stream()
                .flatMap(d -> Stream.of(
                        d.getOrigem(),
                        d.getDestino()
                ))
                .collect(Collectors.toSet());
    }

    private List<Distancia> buscarDistanciasNovas(List<Endereco> enderecosParaBuscaAPI, List<Distancia> distanciasExistentesBanco) {
        return integracaoLocationIQAPI.buscarDistanciaEnderecos(enderecosParaBuscaAPI).stream()
                .filter(distancia -> !distancia.getOrigem().getId().equals(distancia.getDestino().getId()))
                .filter(distancia -> !distanciasExistentesBanco.contains(distancia))
                .toList();
    }

    private void salvarDistancias(List<Distancia> distancias){
        distanciaRepository.saveAll(distancias);
    }

    private List<DistanciaResponse> converterParaResponse(List<Distancia> distancias) {
        return distancias.stream()
                .map(d -> new DistanciaResponse(
                        d.getOrigem().getId(),
                        d.getDestino().getId(),
                        d.getDistancia()
                ))
                .toList();
    }
}

