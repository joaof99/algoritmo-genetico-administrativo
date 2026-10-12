package com.genetico.controller;

import com.genetico.dto.RoteirizacaoRequest;
import com.genetico.service.RoteirizacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roteirizacao")
public class RoteirizacaoController {
    private final RoteirizacaoService roteirizacaoService;

    public RoteirizacaoController(RoteirizacaoService roteirizacaoService) {
        this.roteirizacaoService = roteirizacaoService;
    }

    @PostMapping("/armazenar")
    @ResponseStatus(HttpStatus.CREATED)
    public void armazenar(@Valid @RequestBody RoteirizacaoRequest request) {
        roteirizacaoService.armazenar(request);
    }
}
