package com.genetico.service;

import com.genetico.dto.AlgoritmoGeneticoRequest;
import com.genetico.dto.DistanciaResponse;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlgoritmoGeneticoServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @Mock
    private RotaRepository rotaRepository;

    @Mock
    private DistanciaService distanciaService;

    @InjectMocks
    private AlgoritmoGeneticoService algoritmoGeneticoService;

    @Test
    @DisplayName("Deve enviar ao algoritmo os dados da rota e os parâmetros informados")
    void deveEnviarDadosDaRotaEParametrosAoAlgoritmo() {
        var enderecoA = new Endereco();
        enderecoA.setId(10);
        var enderecoB = new Endereco();
        enderecoB.setId(20);
        var enderecoC = new Endereco();
        enderecoC.setId(30);
        var enderecos = List.of(enderecoA, enderecoB, enderecoC);
        var distancias = List.of(new DistanciaResponse(10, 20, 125.5), new DistanciaResponse(20, 10, 130.0));

        when(rotaRepository.findById(7))
                .thenReturn(Optional.of(new Rota(enderecos)));

        when(distanciaService.obterDistancias(List.of(10, 20, 30)))
                .thenReturn(distancias);

        algoritmoGeneticoService.solicitarRoteirizacao(7, 100, 50, 10, 75);

        var requisicaoCaptor = ArgumentCaptor.forClass(AlgoritmoGeneticoRequest.class);
        verify(restClient.post().uri("/ag/iniciar")).body(requisicaoCaptor.capture());
        assertEquals(distancias, requisicaoCaptor.getValue().distancias());
        assertEquals(enderecos, requisicaoCaptor.getValue().enderecos());
        assertEquals(100, requisicaoCaptor.getValue().tamanhoPopulacao());
        assertEquals(50, requisicaoCaptor.getValue().quantidadeGeracoes());
        assertEquals(10, requisicaoCaptor.getValue().chanceOcorrenciaMutacao());
        assertEquals(75, requisicaoCaptor.getValue().chanceOcorrenciaCrossover());

        verify(rotaRepository).findById(7);
        verify(distanciaService).obterDistancias(List.of(10, 20, 30));
        verify(restClient.post().uri("/ag/iniciar").body(any(AlgoritmoGeneticoRequest.class)).retrieve())
                .toBodilessEntity();
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando a rota não for encontrada")
    void deveRetornarErro404QuandoRotaNaoForEncontrada() {
        when(rotaRepository.findById(404)).thenReturn(Optional.empty());

        var excecao = assertThrows(ResponseStatusException.class,
                () -> algoritmoGeneticoService.solicitarRoteirizacao(404, 100, 50, 10, 75));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Rota de ID 404 não encontrada", excecao.getReason());
        verifyNoInteractions(distanciaService, restClient);
    }
}
