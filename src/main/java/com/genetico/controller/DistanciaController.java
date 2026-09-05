package com.genetico.controller;

import com.genetico.dto.DistanciaResponse;
import com.genetico.service.DistanciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/distancias")
public class DistanciaController {
    private final DistanciaService distanciaService;

    public DistanciaController(DistanciaService distanciaService) {
        this.distanciaService = distanciaService;
    }

    @GetMapping("/{origemId}/{destinoId}")
    public double buscarDistancia(@PathVariable int origemId, @PathVariable int destinoId) {
        return distanciaService.buscarDistancia(origemId, destinoId);
    }

    @GetMapping("/matrix")
    public List<DistanciaResponse> buscarDistanciaEnderecos(@RequestParam List<Integer> idsEnderecos) {
        return distanciaService.buscarDistancias(idsEnderecos);
    }
}
