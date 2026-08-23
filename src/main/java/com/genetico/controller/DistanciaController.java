package com.genetico.controller;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.model.CoordenadaGeografica;
import com.genetico.repository.EnderecoRepository;
import com.genetico.views.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/distancias")
public class DistanciaController {
    private final Logger log = LoggerFactory.getLogger(DistanciaController.class);
    private final IntegracaoLocationIQAPI integracaoLocationIQAPI;
    private final EnderecoRepository enderecoRepository;

    public DistanciaController(IntegracaoLocationIQAPI integracaoLocationIQAPI, EnderecoRepository enderecoRepository) {
        this.integracaoLocationIQAPI = integracaoLocationIQAPI;
        this.enderecoRepository = enderecoRepository;
    }

    @GetMapping("/{origemId}/{destinoId}")
    public double buscarDistancia(@PathVariable int origemId, @PathVariable int destinoId) {
        log.info("Buscando distância na API entre os pontos os endereços de ID {} e ID {} ", origemId, destinoId);

        var origem = enderecoRepository.findById(origemId).orElseThrow();
        var destino = enderecoRepository.findById(destinoId).orElseThrow();

        var coordenadaOrigem = new CoordenadaGeografica(origem.getLatitude(), origem.getLongitude());
        var coordenadaDestino = new CoordenadaGeografica(destino.getLatitude(), destino.getLongitude());

        return integracaoLocationIQAPI.buscarDistanciaEntreCoordenadas(coordenadaOrigem, coordenadaDestino);
    }
}
