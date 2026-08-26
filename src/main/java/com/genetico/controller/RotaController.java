package com.genetico.controller;

import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
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
public class RotaController {
    private final RotaRepository rotaRepository;

    public RotaController(RotaRepository rotaRepository) {
        this.rotaRepository = rotaRepository;
    }

    @GetMapping("/todas")
    public List<Rota> buscarTodas() {
        return rotaRepository.findAll();
    }

    @GetMapping("/{id}/enderecos")
    public ResponseEntity<List<Endereco>> buscarEnderecosPorRotaId(@PathVariable Integer id) {
        var rota = rotaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Rota de ID %d não encontrada", id)
                ));

        return ResponseEntity.ok(rota.getEnderecos());
    }
}
