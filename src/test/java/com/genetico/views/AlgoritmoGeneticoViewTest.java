package com.genetico.views;

import com.genetico.exception.SolicitacaoRoteirizacaoException;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.service.AlgoritmoGeneticoService;
import com.genetico.service.RotaService;
import com.genetico.service.RoteirizacaoService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.GridKt._getFormattedRow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @BeforeEach
    void setUp() {
        MockVaadin.setup();

        var enderecos = List.of(new Endereco("Endereço A", "", "", "", "", -9.5, -8.3),
                new Endereco("Endereço B", "", "", "", "", -10.5, -24.3),
                new Endereco("Endereço C", "", "", "", "", -11.5, -25.3),
                new Endereco("Endereço D", "", "", "", "", -25.5, -90.3));

        when(rotaService.buscarTodosComEnderecos()).thenReturn(List.of(new Rota(enderecos)));

        UI.getCurrent().add(new AlgoritmoGeneticoView(rotaService, algoritmoGeneticoService, roteirizacaoService));

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
        roteirizacao.concluir(15.0, 12.4, List.of(), List.of(15.0, 12.4), dataInicio);

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
}
