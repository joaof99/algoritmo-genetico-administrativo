package com.genetico.service;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.model.Distancia;
import com.genetico.model.Endereco;
import com.genetico.repository.DistanciaRepository;
import com.genetico.repository.EnderecoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistanciaServiceTest {

    @Mock
    IntegracaoLocationIQAPI integracaoLocationIQAPI;

    @Mock
    EnderecoRepository enderecoRepository;

    @Mock
    DistanciaRepository distanciaRepository;

    @InjectMocks
    DistanciaService distanciaService;

    @Test
    @DisplayName("Não deve buscar na API caso distância já esteja no banco")
    void naoDeveBuscarNaAPICasoDistanciaJaEstejaNoBanco() {
        var origem = new Endereco();
        var destino = new Endereco();

        origem.setId(1);
        destino.setId(2);

        when(distanciaRepository.findById(any()))
                .thenReturn(Optional.of(new Distancia(origem, destino, 3000)));

        var distancia = distanciaService.buscarDistancia(1, 2);
        assertEquals(3000, distancia);
        verify(integracaoLocationIQAPI, never()).buscarDistanciaEntreCoordenadas(any(), any());
    }

    @Test
    @DisplayName("Deve retornar distâncias corretamente mas sem buscar na API")
    void deveRetornarDistanciasCorretamenteSemBuscarNaAPI() {
        var endereco = new Endereco();
        endereco.setId(1);

        var endereco2 = new Endereco();
        endereco2.setId(2);

        var endereco3 = new Endereco();
        endereco3.setId(3);

        var distancias = List.of(
                new Distancia(endereco, endereco2, 50),
                new Distancia(endereco, endereco3, 60),
                new Distancia(endereco2, endereco, 70),
                new Distancia(endereco2, endereco3, 80),
                new Distancia(endereco3, endereco, 90),
                new Distancia(endereco3, endereco2, 100)
        );

        when(distanciaRepository.findAllById(any()))
                .thenReturn(distancias);

        var distanciaResponses = distanciaService.buscarDistancias(List.of(1, 2, 3));

        verify(integracaoLocationIQAPI, never()).buscarDistanciaEnderecos(any());
        assertEquals(6, distanciaResponses.size());

        assertEquals(1, distanciaResponses.getFirst().idOrigem());
        assertEquals(2, distanciaResponses.getFirst().idDestino());
        assertEquals(50, distanciaResponses.getFirst().distancia());

        assertEquals(1, distanciaResponses.get(1).idOrigem());
        assertEquals(3, distanciaResponses.get(1).idDestino());
        assertEquals(60, distanciaResponses.get(1).distancia());

        assertEquals(2, distanciaResponses.get(2).idOrigem());
        assertEquals(1, distanciaResponses.get(2).idDestino());
        assertEquals(70, distanciaResponses.get(2).distancia());

        assertEquals(2, distanciaResponses.get(3).idOrigem());
        assertEquals(3, distanciaResponses.get(3).idDestino());
        assertEquals(80, distanciaResponses.get(3).distancia());

        assertEquals(3, distanciaResponses.get(4).idOrigem());
        assertEquals(1, distanciaResponses.get(4).idDestino());
        assertEquals(90, distanciaResponses.get(4).distancia());

        assertEquals(3, distanciaResponses.get(5).idOrigem());
        assertEquals(2, distanciaResponses.get(5).idDestino());
        assertEquals(100, distanciaResponses.get(5).distancia());
    }
}