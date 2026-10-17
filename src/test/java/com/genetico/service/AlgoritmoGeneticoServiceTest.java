package com.genetico.service;

import com.genetico.dto.AlgoritmoGeneticoRequest;
import com.genetico.dto.EnderecoRequest;
import com.genetico.dto.DistanciaResponse;
import com.genetico.enums.StatusRoteirizacao;
import com.genetico.exception.SolicitacaoRoteirizacaoException;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RotaRepository;
import com.genetico.repository.RoteirizacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlgoritmoGeneticoServiceTest {
    private static final Instant AGORA = Instant.parse("2026-10-13T20:00:00Z");

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @Mock
    private RotaRepository rotaRepository;

    @Mock
    private RoteirizacaoRepository roteirizacaoRepository;

    @Mock
    private DistanciaService distanciaService;

    private AlgoritmoGeneticoService algoritmoGeneticoService;

    @BeforeEach
    void setUp() {
        algoritmoGeneticoService = new AlgoritmoGeneticoService(restClient,
                rotaRepository,
                roteirizacaoRepository,
                distanciaService,
                Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Deve criar a roteirização em andamento com a rota e os parâmetros informados")
    void deveCriarRoteirizacaoEmAndamentoComRotaEParametrosInformados() {
        var rota = new Rota(List.of());

        when(rotaRepository.buscarPorIdComEnderecos(7))
                .thenReturn(Optional.of(rota));

        when(roteirizacaoRepository.save(any(Roteirizacao.class)))
                .thenAnswer(invocacao -> {
                    var roteirizacao = invocacao.getArgument(0);
                    ReflectionTestUtils.setField(roteirizacao, "id", 99);
                    return roteirizacao;
                });

        algoritmoGeneticoService.solicitarRoteirizacao(7, 100, 50, 10, 75);

        var roteirizacaoCaptor = ArgumentCaptor.forClass(Roteirizacao.class);
        verify(roteirizacaoRepository).save(roteirizacaoCaptor.capture());

        var roteirizacao = roteirizacaoCaptor.getValue();
        assertSame(rota, roteirizacao.getRota());
        assertEquals(StatusRoteirizacao.EM_ANDAMENTO, roteirizacao.getStatus());
        assertEquals(AGORA, roteirizacao.getDataInicio());
        assertEquals(100, roteirizacao.getTamanhoPopulacao());
        assertEquals(50, roteirizacao.getQuantidadeGeracoes());
        assertEquals(10, roteirizacao.getChanceOcorrenciaMutacao());
        assertEquals(75, roteirizacao.getChanceOcorrenciaCrossover());
    }

    @Test
    @DisplayName("Deve enviar ao algoritmo o ID da roteirização criada, os dados da rota e os parâmetros informados")
    void deveEnviarAoAlgoritmoIdDaRoteirizacaoDadosDaRotaEParametros() {
        var rota = new Rota(Stream.of(10, 20, 30)
                .map(id -> {
                    var endereco = new Endereco();
                    endereco.setId(id);
                    endereco.setLatitude(id + 0.1);
                    endereco.setLongitude(id + 0.2);
                    return endereco;
                })
                .toList());

        when(rotaRepository.buscarPorIdComEnderecos(7))
                .thenReturn(Optional.of(rota));

        var distancias = List.of(new DistanciaResponse(10, 20, 125.5), new DistanciaResponse(20, 10, 130.0));

        when(distanciaService.obterDistancias(List.of(10, 20, 30)))
                .thenReturn(distancias);

        when(roteirizacaoRepository.save(any(Roteirizacao.class)))
                .thenAnswer(invocacao -> {
                    var roteirizacao = invocacao.getArgument(0);
                    ReflectionTestUtils.setField(roteirizacao, "id", 99);
                    return roteirizacao;
                });

        algoritmoGeneticoService.solicitarRoteirizacao(7, 100, 50, 10, 75);

        var requisicaoCaptor = ArgumentCaptor.forClass(AlgoritmoGeneticoRequest.class);
        verify(restClient.post().uri("/ag/iniciar")).body(requisicaoCaptor.capture());

        var requisicao = requisicaoCaptor.getValue();
        assertEquals(99, requisicao.idRoteirizacao());
        assertEquals(distancias, requisicao.distancias());
        assertEquals(List.of(new EnderecoRequest(10, 10.1, 10.2), new EnderecoRequest(20, 20.1, 20.2), new EnderecoRequest(30, 30.1, 30.2)), requisicao.enderecos());
        assertEquals(100, requisicao.tamanhoPopulacao());
        assertEquals(50, requisicao.quantidadeGeracoes());
        assertEquals(10, requisicao.chanceOcorrenciaMutacao());
        assertEquals(75, requisicao.chanceOcorrenciaCrossover());
    }

    @Test
    @DisplayName("Deve lançar exceção de solicitação quando o algoritmo não puder ser acionado")
    void deveLancarExcecaoDeSolicitacaoQuandoAlgoritmoNaoPuderSerAcionado() {
        when(rotaRepository.buscarPorIdComEnderecos(7))
                .thenReturn(Optional.of(new Rota(List.of())));

        when(roteirizacaoRepository.save(any(Roteirizacao.class)))
                .thenAnswer(invocacao -> {
                    var roteirizacao = invocacao.getArgument(0);
                    ReflectionTestUtils.setField(roteirizacao, "id", 99);
                    return roteirizacao;
                });

        when(restClient
                .post()
                .uri("/ag/iniciar")
                .body(any(AlgoritmoGeneticoRequest.class))
                .retrieve()
                .toBodilessEntity())
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThrows(SolicitacaoRoteirizacaoException.class, () -> algoritmoGeneticoService.solicitarRoteirizacao(7, 100, 50, 10, 75));
    }

    @Test
    @DisplayName("Deve marcar a roteirização como falha quando o algoritmo não puder ser acionado")
    void deveMarcarRoteirizacaoComoFalhaQuandoAlgoritmoNaoPuderSerAcionado() {
        when(rotaRepository.buscarPorIdComEnderecos(7))
                .thenReturn(Optional.of(new Rota(List.of())));

        when(roteirizacaoRepository.save(any(Roteirizacao.class)))
                .thenAnswer(invocacao -> {
                    var roteirizacao = invocacao.getArgument(0);
                    ReflectionTestUtils.setField(roteirizacao, "id", 99);
                    return roteirizacao;
                });

        when(restClient
                .post()
                .uri("/ag/iniciar")
                .body(any(AlgoritmoGeneticoRequest.class))
                .retrieve()
                .toBodilessEntity())
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThrows(SolicitacaoRoteirizacaoException.class,
                () -> algoritmoGeneticoService.solicitarRoteirizacao(7, 100, 50, 10, 75));

        var roteirizacaoCaptor = ArgumentCaptor.forClass(Roteirizacao.class);
        verify(roteirizacaoRepository, atLeastOnce()).save(roteirizacaoCaptor.capture());

        var roteirizacao = roteirizacaoCaptor.getValue();
        assertEquals(StatusRoteirizacao.FALHA, roteirizacao.getStatus());
        assertEquals(AGORA, roteirizacao.getDataFim());
        assertEquals("Não foi possível solicitar a roteirização ao algoritmo genético: Connection refused", roteirizacao.getMensagemErro());
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando a rota não for encontrada")
    void deveRetornarErro404QuandoRotaNaoForEncontrada() {
        when(rotaRepository.buscarPorIdComEnderecos(404))
                .thenReturn(Optional.empty());

        var excecao = assertThrows(ResponseStatusException.class,
                () -> algoritmoGeneticoService.solicitarRoteirizacao(404, 100, 50, 10, 75));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Rota de ID 404 não encontrada", excecao.getReason());
        verifyNoInteractions(distanciaService, roteirizacaoRepository, restClient);
    }
}
