package com.genetico.service;

import com.genetico.dto.ConclusaoRoteirizacaoRequest;
import com.genetico.dto.FalhaRoteirizacaoRequest;
import com.genetico.enums.StatusRoteirizacao;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RoteirizacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoteirizacaoServiceTest {
    private static final Instant AGORA = Instant.parse("2026-10-13T20:30:00Z");

    @Mock
    private RoteirizacaoRepository roteirizacaoRepository;

    private RoteirizacaoService roteirizacaoService;

    @BeforeEach
    void setUp() {
        roteirizacaoService = new RoteirizacaoService(roteirizacaoRepository, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Deve concluir a roteirização com o resultado recebido do algoritmo")
    void deveConcluirRoteirizacaoComResultadoRecebidoDoAlgoritmo() {
        var rota = new Rota(Stream.of(10, 20, 30)
                .map(id -> {
                    var endereco = new Endereco();
                    endereco.setId(id);
                    return endereco;
                })
                .toList());
        var roteirizacao = new Roteirizacao(rota, 100, 50, 10, 75, AGORA.minusSeconds(60));

        when(roteirizacaoRepository.buscarPorIdComEnderecosDaRota(7))
                .thenReturn(Optional.of(roteirizacao));

        roteirizacaoService.concluir(7, new ConclusaoRoteirizacaoRequest(1500.2, 1234.5, List.of(30, 10, 20)));

        verify(roteirizacaoRepository).save(roteirizacao);

        assertEquals(StatusRoteirizacao.CONCLUIDA, roteirizacao.getStatus());
        assertEquals(1500.2, roteirizacao.getMelhorFitnessPopulacaoInicial());
        assertEquals(1234.5, roteirizacao.getMelhorFitnessPopulacaoFinal());
        assertEquals(List.of(30, 10, 20), roteirizacao.getMelhorRota());
        assertEquals(AGORA, roteirizacao.getDataFim());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando a melhor rota não corresponder aos endereços da rota")
    void deveRetornarErro400QuandoMelhorRotaNaoCorresponderAosEnderecosDaRota() {
        var rota = new Rota(Stream.of(10, 20, 30)
                .map(id -> {
                    var endereco = new Endereco();
                    endereco.setId(id);
                    return endereco;
                })
                .toList());

        when(roteirizacaoRepository.buscarPorIdComEnderecosDaRota(7))
                .thenReturn(Optional.of(new Roteirizacao(rota, 100, 50, 10, 75, AGORA.minusSeconds(60))));

        var excecao = assertThrows(ResponseStatusException.class,
                () -> roteirizacaoService.concluir(7, new ConclusaoRoteirizacaoRequest(1500.2, 1234.5, List.of(30, 10, 99))));

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando a melhor rota repetir um endereço")
    void deveRetornarErro400QuandoMelhorRotaRepetirEndereco() {
        var rota = new Rota(Stream.of(10, 20, 30)
                .map(id -> {
                    var endereco = new Endereco();
                    endereco.setId(id);
                    return endereco;
                })
                .toList());

        when(roteirizacaoRepository.buscarPorIdComEnderecosDaRota(7))
                .thenReturn(Optional.of(new Roteirizacao(rota, 100, 50, 10, 75, AGORA.minusSeconds(60))));

        var excecao = assertThrows(ResponseStatusException.class,
                () -> roteirizacaoService.concluir(7, new ConclusaoRoteirizacaoRequest(1500.2, 1234.5, List.of(30, 10, 20, 10))));

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
    }

    @Test
    @DisplayName("Deve marcar a roteirização como falha quando a melhor rota não corresponder aos endereços da rota")
    void deveMarcarRoteirizacaoComoFalhaQuandoMelhorRotaNaoCorresponderAosEnderecosDaRota() {
        var rota = new Rota(Stream.of(10, 20, 30)
                .map(id -> {
                    var endereco = new Endereco();
                    endereco.setId(id);
                    return endereco;
                })
                .toList());
        var roteirizacao = new Roteirizacao(rota, 100, 50, 10, 75, AGORA.minusSeconds(60));

        when(roteirizacaoRepository.buscarPorIdComEnderecosDaRota(7))
                .thenReturn(Optional.of(roteirizacao));

        assertThrows(ResponseStatusException.class,
                () -> roteirizacaoService.concluir(7, new ConclusaoRoteirizacaoRequest(1500.2, 1234.5, List.of(30, 10, 99))));

        verify(roteirizacaoRepository).save(roteirizacao);

        assertEquals(StatusRoteirizacao.FALHA, roteirizacao.getStatus());
        assertEquals("A melhor rota devolvida pelo algoritmo genético não corresponde aos endereços da rota: [30, 10, 99]", roteirizacao.getMensagemErro());
    }

    @Test
    @DisplayName("Deve marcar a roteirização como falha com a mensagem recebida do algoritmo")
    void deveMarcarRoteirizacaoComoFalhaComMensagemRecebidaDoAlgoritmo() {
        var roteirizacao = new Roteirizacao(new Rota(List.of()), 100, 50, 10, 75, AGORA.minusSeconds(60));

        when(roteirizacaoRepository.buscarPorIdComEnderecosDaRota(7))
                .thenReturn(Optional.of(roteirizacao));

        roteirizacaoService.falhar(7, new FalhaRoteirizacaoRequest("Index 51 out of bounds for length 51"));

        verify(roteirizacaoRepository).save(roteirizacao);

        assertEquals(StatusRoteirizacao.FALHA, roteirizacao.getStatus());
        assertEquals("Index 51 out of bounds for length 51", roteirizacao.getMensagemErro());
        assertEquals(AGORA, roteirizacao.getDataFim());
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao concluir uma roteirização inexistente")
    void deveRetornarErro404AoConcluirRoteirizacaoInexistente() {
        when(roteirizacaoRepository.buscarPorIdComEnderecosDaRota(404))
                .thenReturn(Optional.empty());

        var excecao = assertThrows(ResponseStatusException.class,
                () -> roteirizacaoService.concluir(404, new ConclusaoRoteirizacaoRequest(1500.2, 1234.5, List.of(30, 10, 20))));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Roteirização de ID 404 não encontrada", excecao.getReason());
        verifyNoMoreInteractions(roteirizacaoRepository);
    }

    @Test
    @DisplayName("Deve retornar erro 404 ao registrar falha de uma roteirização inexistente")
    void deveRetornarErro404AoRegistrarFalhaDeRoteirizacaoInexistente() {
        when(roteirizacaoRepository.buscarPorIdComEnderecosDaRota(404))
                .thenReturn(Optional.empty());

        var excecao = assertThrows(ResponseStatusException.class,
                () -> roteirizacaoService.falhar(404, new FalhaRoteirizacaoRequest("Erro")));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verifyNoMoreInteractions(roteirizacaoRepository);
    }
}
