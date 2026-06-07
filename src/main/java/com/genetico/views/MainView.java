package com.genetico.views;

import com.genetico.model.Cliente;
import com.genetico.service.ClienteService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Algoritmo Genético Administrativo")
public class MainView extends VerticalLayout {
    public MainView(ClienteService clienteService) {
        var titulo = new H3("Lista de clientes existentes");
        add(titulo);

        var clientes = clienteService.buscarTodos();

        var grid = new Grid<>(Cliente.class, true);
        grid.setItems(clientes);

        var botaoCadastrarCliente = new Button("Cadastrar cliente");
        botaoCadastrarCliente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        botaoCadastrarCliente.addClickListener(evento -> UI.getCurrent().navigate(CadastroClienteView.class));

        add(grid, botaoCadastrarCliente);
    }
}
