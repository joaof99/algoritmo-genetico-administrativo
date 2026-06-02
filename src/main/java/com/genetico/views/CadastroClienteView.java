package com.genetico.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("/cadastrar-cliente")
public class CadastroClienteView extends VerticalLayout {

    public CadastroClienteView() {
        var titulo = new H3("Cadastro de cliente");
        var formularioCadastro = new FormLayout();
        var descricao = new TextField("Descrição");
        var latitude = new TextField("Latitude");
        var longitude = new TextField("Longitude");

        formularioCadastro.add(descricao, latitude, longitude);

        var botaoCadastrarCliente = new Button("Cadastrar cliente");
        botaoCadastrarCliente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(titulo, formularioCadastro, botaoCadastrarCliente);
    }

}
