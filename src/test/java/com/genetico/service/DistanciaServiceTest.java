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
import static org.mockito.ArgumentMatchers.anyInt;
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
    @DisplayName("Não deve buscar na API caso todas as distâncias já possuam suas combinações")
    void naoDeveBuscarNaAPICasoTodasAsDistanciasJaPossuamSuasCombinacoes() {
        var endereco = new Endereco();
        endereco.setId(1);

        var endereco2 = new Endereco();
        endereco2.setId(2);

        var endereco3 = new Endereco();
        endereco3.setId(3);

        var distancias = List.of(
                new Distancia(endereco, endereco2, 3),
                new Distancia(endereco, endereco3, 3),
                new Distancia(endereco2, endereco, 3),
                new Distancia(endereco2, endereco3, 3),
                new Distancia(endereco3, endereco, 3),
                new Distancia(endereco3, endereco2, 3)
        );

        when(distanciaRepository.findAllById(any()))
                .thenReturn(distancias);

        distanciaService.buscarDistancias(List.of(1, 2, 3));
        verify(integracaoLocationIQAPI, never()).buscarDistanciaEnderecos(any());
    }
}