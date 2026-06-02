package com.genetico.views;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class PaginaPrincipalView extends VerticalLayout {

    public PaginaPrincipalView() {
        var titulo = new H3("Bem-vindo ao painel administrativo do AG de Roteirização");
        add(titulo);
    }

}
