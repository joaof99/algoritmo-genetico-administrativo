package com.genetico.views;

import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import com.genetico.service.AlgoritmoGeneticoService;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

        when(rotaRepository.buscarTodosComEnderecos())
                .thenReturn(List.of(new Rota(enderecos)));

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
    }


}