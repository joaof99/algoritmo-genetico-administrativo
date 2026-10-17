package com.genetico.controller;

import com.genetico.dto.ConclusaoRoteirizacaoRequest;
import com.genetico.dto.FalhaRoteirizacaoRequest;
import com.genetico.service.RoteirizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roteirizacao")
public class RoteirizacaoController {
    private final RoteirizacaoService roteirizacaoService;

    public RoteirizacaoController(RoteirizacaoService roteirizacaoService) {
        this.roteirizacaoService = roteirizacaoService;
    }

    @PostMapping("/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable int id, @Valid @RequestBody ConclusaoRoteirizacaoRequest request) {
        roteirizacaoService.concluir(id, request);
    }

    @PostMapping("/{id}/falhar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void falhar(@PathVariable int id, @Valid @RequestBody FalhaRoteirizacaoRequest request) {
        roteirizacaoService.falhar(id, request);
    }
}
