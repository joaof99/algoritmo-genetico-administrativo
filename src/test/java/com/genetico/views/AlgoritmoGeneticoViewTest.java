package com.genetico.views;

import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import com.genetico.service.AlgoritmoGeneticoService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlgoritmoGeneticoViewTest {
    @Mock
    private RotaRepository rotaRepository;

    @Mock
    private AlgoritmoGeneticoService algoritmoGeneticoService;

    @BeforeEach
    void setUp() {
        MockVaadin.setup();

        var enderecos = List.of(new Endereco("Endereço A", -9.5, -8.3),
                new Endereco("Endereço B", -10.5, -24.3),
                new Endereco("Endereço C", -11.5, -25.3),
                new Endereco("Endereço D", -25.5, -90.3));

        when(rotaRepository.buscarTodosComEnderecos()).thenReturn(List.of(new Rota(enderecos)));

        UI.getCurrent().add(new AlgoritmoGeneticoView(rotaRepository, algoritmoGeneticoService));

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
        var rota = new Rota(rotaRepository.buscarTodosComEnderecos().get(0).getEnderecos()) {
            @Override
            public Integer getId() {
                return 42;
            }
        };
        var cbxRotas = _get(ComboBox.class, spec -> spec.withId("cbx-rota"));
        cbxRotas.setItems(rota);
        cbxRotas.setValue(rota);

        var tamanhoPopulacao = _get(IntegerField.class, spec -> spec.withId("txt-tamanho-populacao"));
        var quantidadeGeracoes = _get(IntegerField.class, spec -> spec.withId("txt-quantidade-geracoes"));
        var chanceMutacao = _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-mutacao"));
        var chanceCrossover = _get(IntegerField.class, spec -> spec.withId("txt-chance-ocorrencia-crossover"));

        tamanhoPopulacao.setValue(100);
        quantidadeGeracoes.setValue(50);
        chanceMutacao.setValue(10);
        chanceCrossover.setValue(75);

        _click(_get(Button.class, spec -> spec.withId("btn-roteirizacao")));

        verify(algoritmoGeneticoService).solicitarRoteirizacao(42, 100, 50, 10, 75);
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
}
