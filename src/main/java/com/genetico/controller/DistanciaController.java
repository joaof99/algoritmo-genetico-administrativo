package com.genetico.controller;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.dto.DistanciaResponse;
import com.genetico.model.*;
import com.genetico.repository.DistanciaRepository;
import com.genetico.repository.EnderecoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/distancias")
public class DistanciaController {
    private final Logger log = LoggerFactory.getLogger(DistanciaController.class);
    private final IntegracaoLocationIQAPI integracaoLocationIQAPI;
    private final EnderecoRepository enderecoRepository;
    private final DistanciaRepository distanciaRepository;

    public DistanciaController(IntegracaoLocationIQAPI integracaoLocationIQAPI, EnderecoRepository enderecoRepository, DistanciaRepository distanciaRepository) {
        this.integracaoLocationIQAPI = integracaoLocationIQAPI;
        this.enderecoRepository = enderecoRepository;
        this.distanciaRepository = distanciaRepository;
    }

    @GetMapping("/{origemId}/{destinoId}")
    public double buscarDistancia(@PathVariable int origemId, @PathVariable int destinoId) {
        log.info("Buscando distância na API entre os pontos os endereços de ID {} e ID {} ", origemId, destinoId);

        return distanciaRepository.findById(new DistanciaId(origemId, destinoId))
                .map(Distancia::getDistancia)
                .orElseGet(() -> buscarDistanciaEmApi(origemId, destinoId));
    }

    private double buscarDistanciaEmApi(int origemId, int destinoId) {
        var origem = enderecoRepository.findById(origemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Origem de ID %d não encontrado", origemId)));

        var destino = enderecoRepository.findById(destinoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Destino de ID %d não encontrado", destinoId)));

        var coordenadaOrigem = new CoordenadaGeografica(origem.getLatitude(), origem.getLongitude());
        var coordenadaDestino = new CoordenadaGeografica(destino.getLatitude(), destino.getLongitude());

        var distanciaAPI = integracaoLocationIQAPI.buscarDistanciaEntreCoordenadas(coordenadaOrigem, coordenadaDestino);

        salvarDistanciaNoBanco(origemId, destinoId, origem, destino, distanciaAPI);

        return distanciaAPI;
    }

    private void salvarDistanciaNoBanco(int origemId, int destinoId, Endereco origem, Endereco destino, double distanciaAPI) {
        distanciaRepository.save(new Distancia(origem, destino, distanciaAPI));
        log.info("Distância entre endereço de ID {} e ID {} armazenada com sucesso na base de dados", origemId, destinoId);
    }

    @GetMapping("/matrix")
    public List<DistanciaResponse> buscarDistanciaEnderecos(@RequestParam List<Integer> idsEnderecos) {
        var distanciasIdsRequisicao = new ArrayList<DistanciaId>();

        for (Integer idOrigem : idsEnderecos) {
            for (Integer idDestino : idsEnderecos) {
                if (idOrigem.equals(idDestino)) {
                    continue;
                }

                distanciasIdsRequisicao.add(new DistanciaId(idOrigem, idDestino));
            }
        }

        var distanciasBanco = distanciaRepository.findAllById(distanciasIdsRequisicao);

        var paresExistentesBanco = distanciasBanco.stream()
                .map(d -> new DistanciaId(
                        d.getOrigem().getId(),
                        d.getDestino().getId()
                ))
                .collect(Collectors.toSet());

        var paresFaltantesBanco = distanciasIdsRequisicao.stream()
                .filter(par -> !paresExistentesBanco.contains(par))
                .toList();

        var idsFaltantes = paresFaltantesBanco.stream()
                .flatMap(d -> Stream.of(
                        d.getOrigem(),
                        d.getDestino()
                ))
                .collect(Collectors.toSet());

        var enderecos = enderecoRepository.findAllById(idsEnderecos);

        var enderecosParaBusca = enderecos.stream()
                .filter(e -> idsFaltantes.contains(e.getId()))
                .toList();

        if (enderecosParaBusca.isEmpty()) {
            log.info("Todos os endereços já possuem suas combinações. Não é necessário busca na API");

            return distanciasBanco.stream()
                    .map(d -> new DistanciaResponse(
                            d.getOrigem().getId(),
                            d.getDestino().getId(),
                            d.getDistancia()
                    ))
                    .toList();
        }

        var distanciasAPI = integracaoLocationIQAPI.buscarDistanciaEnderecos(enderecosParaBusca);

        var distanciasAPISemMesmaOrigemDestino = distanciasAPI.stream()
                .filter(d -> !d.getOrigem().getId().equals(d.getDestino().getId()))
                .toList();

        var distanciasParaSalvar = distanciasAPISemMesmaOrigemDestino
                .stream()
                .filter(d -> !distanciasBanco.contains(d))
                .toList();

        distanciaRepository.saveAll(distanciasParaSalvar);

        return distanciasParaSalvar.stream()
                .map(d -> new DistanciaResponse(
                        d.getOrigem().getId(),
                        d.getDestino().getId(),
                        d.getDistancia()
                ))
                .toList();
    }
}
