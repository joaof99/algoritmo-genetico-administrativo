package com.genetico.controller;

import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
