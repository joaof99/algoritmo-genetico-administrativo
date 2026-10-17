package com.genetico.model;

import com.genetico.exception.RoteirizacaoFinalizadaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoteirizacaoTest {
    @Test
    @DisplayName("Não deve permitir concluir uma roteirização já concluída")
    void naoDevePermitirConcluirRoteirizacaoJaConcluida() {
        var roteirizacao = new Roteirizacao(new Rota(), 100, 50, 10, 75, Instant.parse("2026-10-13T20:00:00Z"));

        roteirizacao.concluir(1500.2, 1234.5, List.of(30, 10, 20), Instant.parse("2026-10-13T20:05:00Z"));

        assertThrows(RoteirizacaoFinalizadaException.class,
                () -> roteirizacao.concluir(1500.2, 1234.5, List.of(30, 10, 20), Instant.parse("2026-10-13T20:06:00Z")));
    }

    @Test
    @DisplayName("Não deve permitir registrar falha em uma roteirização já concluída")
    void naoDevePermitirRegistrarFalhaEmRoteirizacaoJaConcluida() {
        var roteirizacao = new Roteirizacao(new Rota(), 100, 50, 10, 75, Instant.parse("2026-10-13T20:00:00Z"));

        roteirizacao.concluir(1500.2, 1234.5, List.of(30, 10, 20), Instant.parse("2026-10-13T20:05:00Z"));

        assertThrows(RoteirizacaoFinalizadaException.class,
                () -> roteirizacao.falhar("Erro", Instant.parse("2026-10-13T20:06:00Z")));
    }

    @Test
    @DisplayName("Não deve permitir concluir uma roteirização que falhou")
    void naoDevePermitirConcluirRoteirizacaoQueFalhou() {
        var roteirizacao = new Roteirizacao(new Rota(), 100, 50, 10, 75, Instant.parse("2026-10-13T20:00:00Z"));

        roteirizacao.falhar("Erro", Instant.parse("2026-10-13T20:05:00Z"));

        assertThrows(RoteirizacaoFinalizadaException.class,
                () -> roteirizacao.concluir(1500.2, 1234.5, List.of(30, 10, 20), Instant.parse("2026-10-13T20:06:00Z")));
    }

    @Test
    @DisplayName("Deve limitar a mensagem de erro a 500 caracteres")
    void deveLimitarMensagemDeErroA500Caracteres() {
        var roteirizacao = new Roteirizacao(new Rota(), 100, 50, 10, 75, Instant.parse("2026-10-13T20:00:00Z"));

        roteirizacao.falhar("a".repeat(600), Instant.parse("2026-10-13T20:05:00Z"));

        assertEquals(500, roteirizacao.getMensagemErro().length());
    }
}
