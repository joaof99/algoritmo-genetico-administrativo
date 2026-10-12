package com.genetico.service;

import com.genetico.dto.RoteirizacaoRequest;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RoteirizacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoteirizacaoServiceTest {
    @Mock
    private RoteirizacaoRepository roteirizacaoRepository;

    @InjectMocks
    private RoteirizacaoService roteirizacaoService;

    @Test
    @DisplayName("Deve armazenar a roteirização com os dados recebidos do algoritmo")
    void deveArmazenarRoteirizacaoComDadosRecebidosDoAlgoritmo() {
        when(roteirizacaoRepository.save(any(Roteirizacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        roteirizacaoService.armazenar(new RoteirizacaoRequest(1500.2, 1234.5, 50, 100, 75, 10));

        var roteirizacaoCaptor = ArgumentCaptor.forClass(Roteirizacao.class);
        verify(roteirizacaoRepository).save(roteirizacaoCaptor.capture());
        assertEquals(1500.2, roteirizacaoCaptor.getValue().getMelhorFitnessPopulacaoInicial());
        assertEquals(1234.5, roteirizacaoCaptor.getValue().getMelhorFitnessPopulacaoFinal());
        assertEquals(50, roteirizacaoCaptor.getValue().getQuantidadeGeracoes());
        assertEquals(100, roteirizacaoCaptor.getValue().getTamanhoPopulacao());
        assertEquals(75, roteirizacaoCaptor.getValue().getChanceOcorrenciaCrossover());
        assertEquals(10, roteirizacaoCaptor.getValue().getChanceOcorrenciaMutacao());
    }
}
