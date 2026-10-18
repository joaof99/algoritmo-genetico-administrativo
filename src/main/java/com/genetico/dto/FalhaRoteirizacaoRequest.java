package com.genetico.dto;

import jakarta.validation.constraints.NotBlank;

public record FalhaRoteirizacaoRequest(@NotBlank String mensagem) {
}
