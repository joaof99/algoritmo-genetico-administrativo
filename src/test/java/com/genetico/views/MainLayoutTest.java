package com.genetico.views;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.theme.lumo.Lumo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainLayoutTest {
    @BeforeEach
    void setUp() {
        MockVaadin.setup();

        var rotas = RouteConfiguration.forApplicationScope();
        rotas.setAnnotatedRoute(EnderecoView.class);
        rotas.setAnnotatedRoute(RotaView.class);
        rotas.setAnnotatedRoute(AlgoritmoGeneticoView.class);
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    @DisplayName("Deve iniciar com o tema escuro")
    void deveIniciarComTemaEscuro() {
        UI.getCurrent().add(new MainLayout());

        assertTrue(UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK));
    }

    @Test
    @DisplayName("Deve trocar para o tema claro ao clicar no botão de tema")
    void deveTrocarParaTemaClaroAoClicarNoBotaoDeTema() {
        UI.getCurrent().add(new MainLayout());

        _click(_get(Button.class, spec -> spec.withId("btn-alternar-tema")));

        assertFalse(UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK));
    }

    @Test
    @DisplayName("Deve manter o tema claro escolhido ao abrir o sistema novamente na mesma sessão")
    void deveManterTemaClaroEscolhidoAoAbrirSistemaNovamenteNaMesmaSessao() {
        UI.getCurrent().add(new MainLayout());
        _click(_get(Button.class, spec -> spec.withId("btn-alternar-tema")));

        UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);
        UI.getCurrent().removeAll();
        UI.getCurrent().add(new MainLayout());

        assertFalse(UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK));
    }
}
