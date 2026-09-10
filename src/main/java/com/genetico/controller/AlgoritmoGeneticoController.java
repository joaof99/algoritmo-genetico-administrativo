package com.genetico.controller;

import com.genetico.dto.AlgoritmoGeneticoResponse;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import com.genetico.service.DistanciaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/rotas")
public class AlgoritmoGeneticoController {
    private final RotaRepository rotaRepository;
    private final DistanciaService distanciaService;

    public AlgoritmoGeneticoController(RotaRepository rotaRepository, DistanciaService distanciaService) {
        this.rotaRepository = rotaRepository;
        this.distanciaService = distanciaService;
    }

    @GetMapping("/todas")
    public List<Rota> buscarTodas() {
        return rotaRepository.findAll();
    }

    @GetMapping("/{id}/enderecos")
    public ResponseEntity<AlgoritmoGeneticoResponse> buscarEnderecosPorRotaId(@PathVariable Integer idRota) {
        var rota = rotaRepository.findById(idRota)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Rota de ID %d não encontrada", idRota)
                ));

        var enderecos = rota.getEnderecos();

        var idsEnderecos = enderecos
                .stream()
                .map(Endereco::getId).toList();

        var distanciasResponse = distanciaService.obterDistancias(idsEnderecos);

        return ResponseEntity.ok(new AlgoritmoGeneticoResponse(distanciasResponse, enderecos));
    }
}
