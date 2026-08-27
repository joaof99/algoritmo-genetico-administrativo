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

import java.util.List;

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
        var enderecos = enderecoRepository.findAllById(idsEnderecos);
        var distanciasEntreEnderecos = integracaoLocationIQAPI.buscarDistanciaEnderecos(enderecos);

        return distanciasEntreEnderecos.stream()
                .map(d -> new DistanciaResponse(
                        d.getOrigem().getId(),
                        d.getDestino().getId(),
                        d.getDistancia()
                ))
                .toList();
    }
}
