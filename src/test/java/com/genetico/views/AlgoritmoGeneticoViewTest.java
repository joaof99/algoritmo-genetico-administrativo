package com.genetico.views;

import com.genetico.exception.SolicitacaoRoteirizacaoException;
import com.genetico.model.DistanciaId;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.service.AlgoritmoGeneticoService;
import com.genetico.service.DistanciaService;
import com.genetico.service.RotaService;
import com.genetico.service.RoteirizacaoService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.mvysny.kaributesting.v10.GridKt._doubleClickItem;
import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.GridKt._getFormattedRow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static com.github.mvysny.kaributesting.v10.LocatorJ._assertNone;
import static com.github.mvysny.kaributesting.v10.LocatorJ._assertOne;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.NotificationsKt.expectNotifications;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlgoritmoGeneticoViewTest {
    @Mock
    private RotaService rotaService;

    @Mock
    private AlgoritmoGeneticoService algoritmoGeneticoService;

    @Mock
    private RoteirizacaoService roteirizacaoService;

    @Mock
    private DistanciaService distanciaService;

    @BeforeEach
    void setUp() {
        MockVaadin.setup();

        var enderecos = List.of(new Endereco("Endereço A", "", "", "", "", -9.5, -8.3),
                new Endereco("Endereço B", "", "", "", "", -10.5, -24.3),
                new Endereco("Endereço C", "", "", "", "", -11.5, -25.3),
                new Endereco("Endereço D", "", "", "", "", -25.5, -90.3));

        when(rotaService.buscarTodosComEnderecos()).thenReturn(List.of(new Rota(enderecos)));

        UI.getCurrent().add(new AlgoritmoGeneticoView(rotaService, algoritmoGeneticoService, roteirizacaoService, distanciaService));

    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    @DisplayName("Deve impedir solicitação de roteirização caso os campos estejam inválidos")
    void deveImpedirSolicitacaoDeRoteirizacaoCasoCamposEstejamInvalidos() {
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));

        verify(algoritmoGeneticoService, never()).solicitarRoteirizacao(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
        assertEquals("Selecione uma rota", _get(ComboBox.class, spec -> spec.withId("cbx-rota")).getErrorMessage());
        assertEquals("Informe o tamanho da população", _get(IntegerField.class, spec -> spec.withId("txt-tamanho-populacao")).getErrorMessage());
        assertEquals("Informe a quantidade de gerações", _get(IntegerField.class, spec -> spec.withId("txt-quantidade-geracoes")).getErrorMessage());
        assertEquals("Informe a chance de mutação", _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-mutacao")).getErrorMessage());
        assertEquals("Informe a chance de crossover", _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-crossover")).getErrorMessage());
    }

    @Test
    @DisplayName("Deve enviar ao serviço a rota selecionada e os quatro parâmetros válidos")
    void deveEnviarRotaSelecionadaEParametrosValidosAoServico() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 42;
            }
        };

        var cbxRotas = _get(ComboBox.class, spec -> spec.withId("cbx-rota"));
        cbxRotas.setItems(rota);
        cbxRotas.setValue(rota);

        _get(IntegerField.class, spec -> spec.withId("txt-tamanho-populacao")).setValue(100);
        _get(IntegerField.class, spec -> spec.withId("txt-quantidade-geracoes")).setValue(50);
        _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-mutacao")).setValue(10);
        _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-crossover")).setValue(75);

        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));

        verify(algoritmoGeneticoService).solicitarRoteirizacao(42, 100, 50, 10, 75);
    }

    @Test
    @DisplayName("Deve exibir mensagem de erro quando não for possível solicitar a roteirização")
    void deveExibirMensagemDeErroQuandoNaoForPossivelSolicitarRoteirizacao() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 42;
            }
        };

        var cbxRotas = _get(ComboBox.class, spec -> spec.withId("cbx-rota"));
        cbxRotas.setItems(rota);
        cbxRotas.setValue(rota);

        _get(IntegerField.class, spec -> spec.withId("txt-tamanho-populacao")).setValue(100);
        _get(IntegerField.class, spec -> spec.withId("txt-quantidade-geracoes")).setValue(50);
        _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-mutacao")).setValue(10);
        _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-crossover")).setValue(75);

        doThrow(new SolicitacaoRoteirizacaoException("Não foi possível obter as distâncias entre os endereços da rota", new RuntimeException()))
                .when(algoritmoGeneticoService)
                .solicitarRoteirizacao(42, 100, 50, 10, 75);

        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));

        expectNotifications("Não foi possível obter as distâncias entre os endereços da rota");
    }

    @Test
    @DisplayName("Deve rejeitar tamanho de população fora dos limites com a mensagem correspondente")
    void deveRejeitarTamanhoPopulacaoForaDosLimites() {
        var tamanhoPopulacao = _get(IntegerField.class, spec -> spec.withId("txt-tamanho-populacao"));
        tamanhoPopulacao.setValue(19);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("O tamanho deve estar entre 20 e 500", tamanhoPopulacao.getErrorMessage());
        tamanhoPopulacao.setValue(501);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("O tamanho deve estar entre 20 e 500", tamanhoPopulacao.getErrorMessage());
        verifyNoInteractions(algoritmoGeneticoService);
    }

    @Test
    @DisplayName("Deve rejeitar quantidade de gerações fora dos limites com a mensagem correspondente")
    void deveRejeitarQuantidadeGeracoesForaDosLimites() {
        var quantidadeGeracoes = _get(IntegerField.class, spec -> spec.withId("txt-quantidade-geracoes"));
        quantidadeGeracoes.setValue(9);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("A quantidade deve estar entre 10 e 1000", quantidadeGeracoes.getErrorMessage());
        quantidadeGeracoes.setValue(1001);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("A quantidade deve estar entre 10 e 1000", quantidadeGeracoes.getErrorMessage());
        verifyNoInteractions(algoritmoGeneticoService);
    }

    @Test
    @DisplayName("Deve rejeitar chance de mutação fora dos limites com a mensagem correspondente")
    void deveRejeitarChanceMutacaoForaDosLimites() {
        var chanceMutacao = _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-mutacao"));
        chanceMutacao.setValue(0);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("A chance deve estar entre 1 e 100%", chanceMutacao.getErrorMessage());
        chanceMutacao.setValue(101);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("A chance deve estar entre 1 e 100%", chanceMutacao.getErrorMessage());
        verifyNoInteractions(algoritmoGeneticoService);
    }

    @Test
    @DisplayName("Deve rejeitar chance de crossover fora dos limites com a mensagem correspondente")
    void deveRejeitarChanceCrossoverForaDosLimites() {
        var chanceCrossover = _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-crossover"));
        chanceCrossover.setValue(0);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("A chance deve estar entre 1 e 100%", chanceCrossover.getErrorMessage());
        chanceCrossover.setValue(101);
        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));
        assertEquals("A chance deve estar entre 1 e 100%", chanceCrossover.getErrorMessage());
        verifyNoInteractions(algoritmoGeneticoService);
    }

    @Test
    @DisplayName("Deve exibir no histórico o resultado da roteirização concluída")
    void deveExibirNoHistoricoResultadoDaRoteirizacaoConcluida() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:42:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 15.0;
        var melhorFitnessPopulacaoFinal = 12.4;
        List<Integer> melhorRota = List.of();
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataInicio);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        var linha = _getFormattedRow(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        var inicioFormatado = DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(dataInicio);

        assertEquals("Rota 8", linha.get(1));
        assertEquals("12,4 km", linha.get(3));
        assertEquals(inicioFormatado, linha.get(4));
    }

    @Test
    @DisplayName("Deve exibir no histórico o status de falha da roteirização")
    void deveExibirNoHistoricoStatusDeFalhaDaRoteirizacao() {
        var roteirizacao = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        roteirizacao.falhar("Algoritmo genético fora do ar", Instant.parse("2026-10-17T21:31:00Z"));

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        var status = (Span) _getCellComponent(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0, "status");

        assertEquals("Falha", status.getText());
    }

    @Test
    @DisplayName("Deve exibir traço no melhor fitness da roteirização em andamento")
    void deveExibirTracoNoMelhorFitnessDaRoteirizacaoEmAndamento() {
        var roteirizacao = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        var linha = _getFormattedRow(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        assertEquals("—", linha.get(3));
    }

    @Test
    @DisplayName("Duplo clique em uma roteirização concluída deve exibir as paradas na ordem da melhor rota")
    void duploCliqueEmUmaRoteirizacaoConcluidaDeveExibirParadasNaOrdemDaMelhorRota() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        var gridOrdemVisita = _get(Grid.class, spec -> spec.withId("grid-ordem-visita"));

        assertEquals("Rua C", ((Span) _getCellComponent(gridOrdemVisita, 0, "logradouro")).getText());
        assertEquals("Rua A", ((Span) _getCellComponent(gridOrdemVisita, 1, "logradouro")).getText());
        assertEquals("Rua B", ((Span) _getCellComponent(gridOrdemVisita, 2, "logradouro")).getText());
    }

    @Test
    @DisplayName("Deve exibir a distância de cada parada até a próxima")
    void deveExibirDistanciaDeCadaParadaAteProxima() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        when(distanciaService.buscarDistanciasEntreParadasConsecutivas(List.of(3, 1, 2)))
                .thenReturn(Map.of(new DistanciaId(3, 1), 12.5, new DistanciaId(1, 2), 8.0));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        var gridOrdemVisita = _get(Grid.class, spec -> spec.withId("grid-ordem-visita"));

        assertEquals("12,5 km", _getFormattedRow(gridOrdemVisita, 0).get(2));
        assertEquals("8,0 km", _getFormattedRow(gridOrdemVisita, 1).get(2));
    }

    @Test
    @DisplayName("Última parada não deve exibir distância até a próxima")
    void ultimaParadaNaoDeveExibirDistanciaAteProxima() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        assertEquals("—", _getFormattedRow(_get(Grid.class, spec -> spec.withId("grid-ordem-visita")), 2).get(2));
    }

    @Test
    @DisplayName("Deve exibir a melhor distância inicial e a melhor distância final da roteirização concluída")
    void deveExibirMelhorDistanciaInicialEMelhorDistanciaFinalDaRoteirizacaoConcluida() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 2214.8;
        var melhorFitnessPopulacaoFinal = 1397.5;
        List<Integer> melhorRota = List.of();
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        assertEquals("2.214,8 km", _get(Span.class, spec -> spec.withId("metrica-melhor-distancia-inicial")).getText());
        assertEquals("1.397,5 km", _get(Span.class, spec -> spec.withId("metrica-melhor-distancia-final")).getText());
    }

    @Test
    @DisplayName("Deve exibir a melhoria percentual da melhor distância final em relação à melhor distância inicial")
    void deveExibirMelhoriaPercentualDaMelhorDistanciaFinalEmRelacaoAMelhorDistanciaInicial() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 2214.8;
        var melhorFitnessPopulacaoFinal = 1397.5;
        List<Integer> melhorRota = List.of();
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        assertEquals("Distância 36,9% menor", _get(Span.class, spec -> spec.withId("resumo-melhoria")).getText());
    }

    @Test
    @DisplayName("Deve exibir a duração da execução da roteirização")
    void deveExibirDuracaoDaExecucaoDaRoteirizacao() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .tamanhoPopulacao(100)
                .quantidadeGeracoes(200)
                .chanceOcorrenciaMutacao(5)
                .chanceOcorrenciaCrossover(80)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 2214.8;
        var melhorFitnessPopulacaoFinal = 1397.5;
        List<Integer> melhorRota = List.of();
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(252);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        var parametro = _get(Div.class, spec -> spec.withId("duracao-execucao"));

        assertEquals("4m12s", _get(parametro, Span.class, spec -> spec.withClasses("parametro-valor")).getText());
    }

    @Test
    @DisplayName("Deve exibir a quantidade de paradas e a economia de distância da melhor rota")
    void deveExibirQuantidadeDeParadasEEconomiaDeDistanciaDaMelhorRota() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 2214.8;
        var melhorFitnessPopulacaoFinal = 1397.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        assertEquals("3 paradas · economia de 817,3 km", _get(Span.class, spec -> spec.withId("resumo-paradas")).getText());
    }

    @Test
    @DisplayName("Primeira parada deve ser marcada como início")
    void primeiraParadaDeveSerMarcadaComoInicio() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        var logradouro = _getCellComponent(_get(Grid.class, spec -> spec.withId("grid-ordem-visita")), 0, "logradouro");

        assertEquals("Início", _get(logradouro, Span.class, spec -> spec.withClasses("marcador-parada")).getText());
    }

    @Test
    @DisplayName("Última parada deve ser marcada como fim")
    void ultimaParadaDeveSerMarcadaComoFim() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        var logradouro = _getCellComponent(_get(Grid.class, spec -> spec.withId("grid-ordem-visita")), 2, "logradouro");

        assertEquals("Fim", _get(logradouro, Span.class, spec -> spec.withClasses("marcador-parada")).getText());
    }

    @Test
    @DisplayName("Parada intermediária não deve ter marcador")
    void paradaIntermediariaNaoDeveTerMarcador() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        var logradouro = _getCellComponent(_get(Grid.class, spec -> spec.withId("grid-ordem-visita")), 1, "logradouro");

        _assertNone(logradouro, Span.class, spec -> spec.withClasses("marcador-parada"));
    }

    @Test
    @DisplayName("Deve exibir no rodapé o total das distâncias entre as paradas")
    void deveExibirNoRodapeTotalDasDistanciasEntreAsParadas() {
        var enderecoA = new Endereco("Rua A", "", "", "", "", -9.5, -8.3);
        enderecoA.setId(1);
        var enderecoB = new Endereco("Rua B", "", "", "", "", -10.5, -24.3);
        enderecoB.setId(2);
        var enderecoC = new Endereco("Rua C", "", "", "", "", -11.5, -25.3);
        enderecoC.setId(3);

        var rota = new Rota(List.of(enderecoA, enderecoB, enderecoC)) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        var melhorRota = List.of(3, 1, 2);
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        when(distanciaService.buscarDistanciasEntreParadasConsecutivas(List.of(3, 1, 2)))
                .thenReturn(Map.of(new DistanciaId(3, 1), 12.5, new DistanciaId(1, 2), 8.0));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        Grid<?> gridOrdemVisita = _get(Grid.class, spec -> spec.withId("grid-ordem-visita"));

        assertEquals("20,5 km", gridOrdemVisita.getFooterRows().getFirst().getCell(gridOrdemVisita.getColumnByKey("distanciaAteProxima")).getText());
    }

    @ParameterizedTest(name = "Roteirização {0}")
    @MethodSource("cenariosDuploCliqueSemAbrirDetalhe")
    @DisplayName("Duplo clique em uma roteirização não concluída não deve abrir o detalhe")
    void duploCliqueEmUmaRoteirizacaoNaoConcluidaNaoDeveAbrirDetalhe(Roteirizacao roteirizacao) {
        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        _assertNone(Grid.class, spec -> spec.withId("grid-ordem-visita"));
        verify(rotaService, never()).buscarPorIdComEnderecos(anyInt());
    }

    private static Stream<Arguments> cenariosDuploCliqueSemAbrirDetalhe() {
        var roteirizacaoEmAndamento = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        var roteirizacaoComFalha = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        roteirizacaoComFalha.falhar("Algoritmo genético fora do ar", Instant.parse("2026-10-17T21:31:00Z"));

        return Stream.of(
                Arguments.of(Named.of("em andamento", roteirizacaoEmAndamento)),
                Arguments.of(Named.of("com falha", roteirizacaoComFalha))
        );
    }

    @ParameterizedTest(name = "Roteirização {0}")
    @MethodSource("cenariosDuploCliqueComAvisoDeDetalheIndisponivel")
    @DisplayName("Duplo clique em uma roteirização não concluída deve informar que os detalhes só são exibidos para roteirizações concluídas")
    void duploCliqueEmUmaRoteirizacaoNaoConcluidaDeveInformarQueDetalhesSoSaoExibidosParaRoteirizacoesConcluidas(Roteirizacao roteirizacao) {
        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        expectNotifications("Os detalhes da rota só são exibidos para roteirizações concluídas");
    }

    private static Stream<Arguments> cenariosDuploCliqueComAvisoDeDetalheIndisponivel() {
        var roteirizacaoEmAndamento = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        var roteirizacaoComFalha = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        roteirizacaoComFalha.falhar("Algoritmo genético fora do ar", Instant.parse("2026-10-17T21:31:00Z"));

        return Stream.of(
                Arguments.of(Named.of("em andamento", roteirizacaoEmAndamento)),
                Arguments.of(Named.of("com falha", roteirizacaoComFalha))
        );
    }

    @Test
    @DisplayName("Clique no botão de detalhe de uma roteirização concluída deve abrir o detalhe")
    void cliqueNoBotaoDeDetalheDeUmaRoteirizacaoConcluidaDeveAbrirDetalhe() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        List<Integer> melhorRota = List.of();
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        _click((Button) _getCellComponent(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0, "detalhe"));

        _assertOne(Grid.class, spec -> spec.withId("grid-ordem-visita"));
    }

    @ParameterizedTest(name = "Roteirização {0}")
    @MethodSource("cenariosBotaoDetalheDesabilitado")
    @DisplayName("Botão de detalhe de uma roteirização não concluída deve ficar desabilitado")
    void botaoDeDetalheDeUmaRoteirizacaoNaoConcluidaDeveFicarDesabilitado(Roteirizacao roteirizacao) {
        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        var botaoDetalhe = (Button) _getCellComponent(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0, "detalhe");

        assertFalse(botaoDetalhe.isEnabled());
    }

    private static Stream<Arguments> cenariosBotaoDetalheDesabilitado() {
        var roteirizacaoEmAndamento = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        var roteirizacaoComFalha = new Roteirizacao.Builder()
                .rota(new Rota(List.of()))
                .dataInicio(Instant.parse("2026-10-17T21:30:00Z"))
                .build();

        roteirizacaoComFalha.falhar("Algoritmo genético fora do ar", Instant.parse("2026-10-17T21:31:00Z"));

        return Stream.of(
                Arguments.of(Named.of("em andamento", roteirizacaoEmAndamento)),
                Arguments.of(Named.of("com falha", roteirizacaoComFalha))
        );
    }

    @Test
    @DisplayName("Deve exibir mensagem de erro quando a rota da roteirização não for encontrada")
    void deveExibirMensagemDeErroQuandoRotaDaRoteirizacaoNaoForEncontrada() {
        var rota = new Rota(List.of()) {
            @Override
            public Integer getId() {
                return 8;
            }
        };

        var dataInicio = Instant.parse("2026-10-17T21:30:00Z");

        var roteirizacao = new Roteirizacao.Builder()
                .rota(rota)
                .quantidadeGeracoes(1)
                .dataInicio(dataInicio)
                .build();

        var melhorFitnessPopulacaoInicial = 30.0;
        var melhorFitnessPopulacaoFinal = 20.5;
        List<Integer> melhorRota = List.of();
        var melhoresFitnessPorGeracao = List.of(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal);
        var dataFim = dataInicio.plusSeconds(10);

        roteirizacao.concluir(melhorFitnessPopulacaoInicial, melhorFitnessPopulacaoFinal, melhorRota, melhoresFitnessPorGeracao, dataFim);

        when(roteirizacaoService.buscarHistorico(any()))
                .thenReturn(List.of(roteirizacao));

        when(rotaService.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.empty());

        _doubleClickItem(_get(Grid.class, spec -> spec.withId("grid-historico-roteirizacoes")), 0);

        expectNotifications("Rota 8 não encontrada");
    }
}
