package com.genetico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoteirizacaoFinalizadaException extends RuntimeException {
    public RoteirizacaoFinalizadaException(Integer idRoteirizacao) {
        super(String.format("Roteirização de ID %d já foi finalizada", idRoteirizacao));
    }
}
