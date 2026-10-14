package com.genetico.service;

import com.genetico.dto.RoteirizacaoRequest;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RotaRepository;
import com.genetico.repository.RoteirizacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoteirizacaoServiceTest {
    @Mock
    private RoteirizacaoRepository roteirizacaoRepository;

    @Mock
    private RotaRepository rotaRepository;

    @InjectMocks
    private RoteirizacaoService roteirizacaoService;

    @Test
    @DisplayName("Deve armazenar a roteirização com os dados recebidos do algoritmo")
    void deveArmazenarRoteirizacaoComDadosRecebidosDoAlgoritmo() {
        when(rotaRepository.findById(7))
                .thenReturn(Optional.of(new Rota()));

        when(roteirizacaoRepository.save(any(Roteirizacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        roteirizacaoService.armazenar(new RoteirizacaoRequest(7, 1500.2, 1234.5, 50, 100, 75, 10));

        var roteirizacaoCaptor = ArgumentCaptor.forClass(Roteirizacao.class);
        verify(roteirizacaoRepository).save(roteirizacaoCaptor.capture());
        assertEquals(1500.2, roteirizacaoCaptor.getValue().getMelhorFitnessPopulacaoInicial());
        assertEquals(1234.5, roteirizacaoCaptor.getValue().getMelhorFitnessPopulacaoFinal());
        assertEquals(50, roteirizacaoCaptor.getValue().getQuantidadeGeracoes());
        assertEquals(100, roteirizacaoCaptor.getValue().getTamanhoPopulacao());
        assertEquals(75, roteirizacaoCaptor.getValue().getChanceOcorrenciaCrossover());
        assertEquals(10, roteirizacaoCaptor.getValue().getChanceOcorrenciaMutacao());
    }

    @Test
    @DisplayName("Deve relacionar a roteirização à rota informada pelo algoritmo")
    void deveRelacionarRoteirizacaoARotaInformadaPeloAlgoritmo() {
        var rota = new Rota();

        when(rotaRepository.findById(7))
                .thenReturn(Optional.of(rota));

        when(roteirizacaoRepository.save(any(Roteirizacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        roteirizacaoService.armazenar(new RoteirizacaoRequest(7, 1500.2, 1234.5, 50, 100, 75, 10));

        var roteirizacaoCaptor = ArgumentCaptor.forClass(Roteirizacao.class);
        verify(roteirizacaoRepository).save(roteirizacaoCaptor.capture());
        assertSame(rota, roteirizacaoCaptor.getValue().getRota());
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando a rota da roteirização não for encontrada")
    void deveRetornarErro404QuandoRotaDaRoteirizacaoNaoForEncontrada() {
        when(rotaRepository.findById(404))
                .thenReturn(Optional.empty());

        var excecao = assertThrows(ResponseStatusException.class,
                () -> roteirizacaoService.armazenar(new RoteirizacaoRequest(404, 1500.2, 1234.5, 50, 100, 75, 10)));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Rota de ID 404 não encontrada", excecao.getReason());
        verifyNoInteractions(roteirizacaoRepository);
    }
}
