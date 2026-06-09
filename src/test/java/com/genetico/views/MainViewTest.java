package com.genetico.views;

import com.genetico.model.Cliente;
import com.genetico.service.ClienteService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainViewTest {

    @Mock
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        MockVaadin.setup();
        when(clienteService.buscarTodos()).thenReturn(new ArrayList<>());
        UI.getCurrent().add(new MainView(clienteService));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    @DisplayName("Deve adicionar cliente ao grid após salvar com sucesso")
    void deveAdicionarClienteAoGridAposSalvar() {
        when(clienteService.salvar(any())).thenReturn(new Cliente());

        _setValue(_get(TextField.class, spec -> spec.withLabel("Digite a descrição")), "Cliente Teste");
        _setValue(_get(TextField.class, spec -> spec.withLabel("Digite a latitude")), "-23.5");
        _setValue(_get(TextField.class, spec -> spec.withLabel("Digite a longitude")), "-46.6");
        _click(_get(Button.class, spec -> spec.withText("Cadastrar cliente")));

        verify(clienteService, times(1)).salvar(any());
        var quantidadeItensGrid = _size(_get(Grid.class, spec -> spec.withId("grid-clientes")));
        assertEquals(1, quantidadeItensGrid);
    }

    @Test
    @DisplayName("Não deve atualizar o grid se ocorrer exceção ao salvar o cliente")
    void naoDeveAtualizarGridSeHouverExcecaoAoSalvarCliente() {
        when(clienteService.salvar(any())).thenThrow(new RuntimeException("Erro no banco"));

        _setValue(_get(TextField.class, spec -> spec.withLabel("Digite a descrição")), "Cliente Teste");
        _setValue(_get(TextField.class, spec -> spec.withLabel("Digite a latitude")), "-23.5");
        _setValue(_get(TextField.class, spec -> spec.withLabel("Digite a longitude")), "-46.6");

        _click(_get(Button.class, spec -> spec.withText("Cadastrar cliente")));

        assertEquals(0, _size(_get(Grid.class)));
    }
}
