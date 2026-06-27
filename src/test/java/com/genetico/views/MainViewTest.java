package com.genetico.views;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.model.CoordenadaGeografica;
import com.genetico.model.Endereco;
import com.genetico.service.EnderecoService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static com.github.mvysny.kaributesting.v10.GridKt._size;
import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainViewTest {

    @Mock
    private EnderecoService enderecoService;

    @Mock
    private IntegracaoLocationIQAPI integracaoLocationIQAPI;

    private IntegerField numero;
    private TextField logradouro;
    private TextField bairro;
    private TextField cep;
    private ComboBox<String> uf;
    private TextField cidade;
    private NumberField latitude;
    private NumberField longitude;

    @BeforeEach
    void setUp() {
        MockVaadin.setup();
        when(enderecoService.buscarTodos()).thenReturn(new ArrayList<>());
        UI.getCurrent().add(new MainView(enderecoService, integracaoLocationIQAPI));
        inicializarTextFields();
    }

    private void inicializarTextFields() {
        numero = _get(IntegerField.class, spec -> spec.withId("txt-form-cadastro-numero"));
        numero.setValue(500);

        logradouro = _get(TextField.class, spec -> spec.withId("txt-form-cadastro-logradouro"));
        logradouro.setValue("Endereco Teste");

        bairro = _get(TextField.class, spec -> spec.withId("txt-form-cadastro-bairro"));
        bairro.setValue("Norte");

        cep = _get(TextField.class, spec -> spec.withId("txt-form-cadastro-cep"));
        cep.setValue("18304-303");

        uf = _get(ComboBox.class, spec -> spec.withId("cbx-form-cadastro-uf"));
        uf.setValue("SP");

        cidade = _get(TextField.class, spec -> spec.withId("txt-form-cadastro-cidade"));
        cidade.setValue("São Paulo");

        latitude = _get(NumberField.class, spec -> spec.withId("txt-form-cadastro-latitude"));
        latitude.setValue(-23.5);

        longitude = _get(NumberField.class, spec -> spec.withId("txt-form-cadastro-longitude"));
        longitude.setValue(-46.6);
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    @DisplayName("Deve adicionar endereco ao grid após salvar com sucesso")
    void deveAdicionarEnderecoAoGridAposSalvar() {
        when(enderecoService.salvar(any())).thenReturn(new Endereco());
        _click(_get(Button.class, spec -> spec.withText("Cadastrar endereco")));

        verify(enderecoService, times(1)).salvar(any(Endereco.class));
        var quantidadeItensGrid = _size(_get(Grid.class, spec -> spec.withId("grid-enderecos")));
        assertEquals(1, quantidadeItensGrid);
    }

    @Test
    @DisplayName("Deve limpar o formulário após salvar um endereco")
    void deveLimparFormularioCorretamenteAposSalvarEndereco() {
        when(enderecoService.salvar(any())).thenReturn(new Endereco());
        _click(_get(Button.class, spec -> spec.withText("Cadastrar endereco")));

        var todosInputsEstaoLimpos = _find(TextField.class)
                .stream()
                .allMatch(field -> field.getValue().isEmpty());

        assertTrue(todosInputsEstaoLimpos);
    }

    @Test
    @DisplayName("Não deve atualizar o grid se ocorrer exceção ao salvar o endereco")
    void naoDeveAtualizarGridSeOcorrerExcecaoAoSalvarEndereco() {
        when(enderecoService.salvar(any())).thenThrow(new RuntimeException("Erro no banco"));
        _click(_get(Button.class, spec -> spec.withText("Cadastrar endereco")));

        var quantidadeItensGrid = _size(_get(Grid.class, spec -> spec.withId("grid-enderecos")));
        assertEquals(0, quantidadeItensGrid);
    }

    @Test
    @DisplayName("Não deve salvar se o formulário estiver inválido")
    void naoDeveSalvarComFormularioInvalido() {
        logradouro.setValue("");
        _click(_get(Button.class, spec -> spec.withText("Cadastrar endereco")));
        verify(enderecoService, never()).salvar(any());
    }

    @Test
    @DisplayName("Latitude e longitude devem ser preenchidos corretamente ao buscar na API")
    void latitudeELongitudeDevemSerPreenchidosCorretamenteAobuscarNaAPI() {
        when(integracaoLocationIQAPI.buscarCoordenadaGeografica(anyString(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new CoordenadaGeografica(-90.50, -80.40));

        _click(_get(Button.class, spec -> spec.withId("botao-busca-api")));

        assertEquals(-90.50, latitude.getValue());
        assertEquals(-80.40, longitude.getValue());
    }

    @Test
    @DisplayName("API não deve ser chamada se não for preenchido valores exigidos para busca")
    void apiNaoDeveSerChamadaCasoNaoSejaPreenchidoValoresExigidosParaBusca() {
        logradouro.setValue("");
        _click(_get(Button.class, spec -> spec.withId("botao-busca-api")));
        verify(integracaoLocationIQAPI, never()).buscarCoordenadaGeografica(anyString(), anyInt(), anyString(), anyString(), anyString(), anyString());
    }
}
