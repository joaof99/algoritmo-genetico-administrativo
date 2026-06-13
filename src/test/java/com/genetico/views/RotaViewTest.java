package com.genetico.views;

import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import com.genetico.service.EnderecoService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RotaViewTest {
    @Mock
    private EnderecoService enderecoService;

    @Mock
    private RotaRepository rotaRepository;

    @BeforeEach
    void setUp() {
        MockVaadin.setup();
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    @DisplayName("Somente endereços selecionados em combo box devem ser cadastrados na rota")
    void somenteEnderecosSelecionadosEmComboBoxDevemSerCadastradosNaRota() {
        var endereco1 = new Endereco("Endereço A", -9.5, -8.3);
        var endereco2 = new Endereco("Endereço B", -10.5, -24.3);
        var endereco3 = new Endereco("Endereço C", -11.5, -25.3);
        var endereco4 = new Endereco("Endereço D", -25.5, -90.3);

        when(enderecoService.buscarTodos()).thenReturn(List.of(endereco1, endereco2, endereco3, endereco4));

        UI.getCurrent().add(new RotaView(enderecoService, rotaRepository));

        var cbxEnderecos = _get(MultiSelectComboBox.class, spec -> spec.withId("cbx-listagem-enderecos"));
        cbxEnderecos.setValue(Set.of(endereco1, endereco2, endereco3));

        _click(_get(Button.class, spec -> spec.withId("btn-cadastro-rota")));

        var rotaCaptor = ArgumentCaptor.forClass(Rota.class);
        verify(rotaRepository, times(1)).save(rotaCaptor.capture());

        var rotaCapturada = rotaCaptor.getValue();
        assertEquals(3, rotaCapturada.getEnderecos().size());
    }

    @Test
    @DisplayName("ComboBox de clientes deve ser limpa após um cadastro de rota")
    void comboBoxClientesDeveSerLimpaAposUmCadastroDeRota() {
        var endereco1 = new Endereco("Endereço A", -9.5, -8.3);
        var endereco2 = new Endereco("Endereço B", -10.5, -24.3);
        var endereco3 = new Endereco("Endereço C", -10.5, -24.3);
        var endereco4 = new Endereco("Endereço D", -10.5, -24.3);

        UI.getCurrent().add(new RotaView(enderecoService, rotaRepository));

        var cbxEnderecos = _get(MultiSelectComboBox.class, spec -> spec.withId("cbx-listagem-enderecos"));
        cbxEnderecos.setValue(Set.of(endereco1, endereco2, endereco3, endereco4));

        _click(_get(Button.class, spec -> spec.withId("btn-cadastro-rota")));

        assertEquals(0, cbxEnderecos.getSelectedItems().size());
    }
}