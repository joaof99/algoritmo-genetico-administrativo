package com.genetico.service;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.model.Distancia;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.DistanciaRepository;
import com.genetico.repository.EnderecoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

        when(enderecoRepository.findAllById(any()))
                .thenReturn(List.of(endereco, endereco2, endereco3));

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

    @Test
    @DisplayName("Deve executar corretamente a rotina de busca e armazenagem de distâncias faltantes na API")
    void deveExecutarCorretamenteARotinaDeBuscaEArmazenagemDeDistanciasFaltantesNaAPI() {
        var endereco1 = new Endereco();
        endereco1.setId(1);

        var endereco2 = new Endereco();
        endereco2.setId(2);

        var endereco3 = new Endereco();
        endereco3.setId(3);

        var endereco4 = new Endereco();
        endereco4.setId(4);

        var distancias = List.of(
                new Distancia(endereco1, endereco2, 50),
                new Distancia(endereco1, endereco3, 60),
                new Distancia(endereco1, endereco4, 60),
                new Distancia(endereco2, endereco1, 70),
                new Distancia(endereco2, endereco3, 80),
                new Distancia(endereco2, endereco4, 80),
                new Distancia(endereco3, endereco1, 80),
                new Distancia(endereco3, endereco2, 80),
                new Distancia(endereco3, endereco4, 80),
                new Distancia(endereco4, endereco1, 80),
                new Distancia(endereco4, endereco2, 80)
        );

        when(distanciaRepository.findAllById(any()))
                .thenReturn(distancias);

        when(enderecoRepository.findAllById(any()))
                .thenReturn(List.of(endereco1, endereco2, endereco3, endereco4));

        when(integracaoLocationIQAPI.buscarDistanciaEnderecos(any()))
                .thenReturn(List.of(new Distancia(endereco4, endereco3, 100),
                        new Distancia(endereco4, endereco4, 0),
                        new Distancia(endereco3, endereco3, 0)
                ));

        distanciaService.buscarDistancias(List.of(1, 2, 3, 4));

        ArgumentCaptor<List<Endereco>> enderecosCaptor = ArgumentCaptor.forClass(List.class);
        verify(integracaoLocationIQAPI, times(1)).buscarDistanciaEnderecos(enderecosCaptor.capture());

        var enderecosParaBuscaAPI = enderecosCaptor.getValue();
        assertEquals(2, enderecosParaBuscaAPI.size());
        assertEquals(3, enderecosParaBuscaAPI.getFirst().getId());
        assertEquals(4, enderecosParaBuscaAPI.get(1).getId());

        ArgumentCaptor<List<Distancia>> distanciasParaSalvarBanco = ArgumentCaptor.forClass(List.class);
        verify(distanciaRepository, times(1)).saveAll(distanciasParaSalvarBanco.capture());

        var distanciasSalvasBanco = distanciasParaSalvarBanco.getValue();
        assertEquals(1, distanciasSalvasBanco.size());
        assertEquals(4, distanciasSalvasBanco.getFirst().getOrigem().getId());
        assertEquals(3, distanciasSalvasBanco.getFirst().getDestino().getId());
    }
}