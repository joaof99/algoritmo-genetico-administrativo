package com.genetico.views;

import com.genetico.model.Cliente;
import com.genetico.service.ClienteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Algoritmo Genético Administrativo")
public class MainView extends VerticalLayout {
    private final BeanValidationBinder<Cliente> clienteBinder = new BeanValidationBinder<>(Cliente.class);

    private final TextField descricao = new TextField("Digite a descrição");
    private final TextField latitude = new TextField("Digite a latitude");
    private final TextField longitude = new TextField("Digite a longitude");

    private final Grid<Cliente> grid = new Grid<>(Cliente.class, true);
    private ListDataProvider<Cliente> clientesProvider;

    public MainView(ClienteService clienteService) {
        renderizarListagemClientes(clienteService);
        renderizarCadastroClientes(clienteService);
    }

    private void renderizarListagemClientes(ClienteService clienteService) {
        var titulo = new H3("Listagem de clientes existentes");

        var clientes = clienteService.buscarTodos();
        this.clientesProvider = new ListDataProvider<>(clientes);
        grid.setItems(clientes);
        grid.setDataProvider(this.clientesProvider);

        add(titulo, grid);
    }

    private void renderizarCadastroClientes(ClienteService clienteService) {
        clienteBinder.bindInstanceFields(this);
        clienteBinder.setBean(new Cliente());
        var titulo = new H3("Cadastro de cliente");

        var formularioCadastro = new FormLayout();
        formularioCadastro.add(descricao, latitude, longitude);

        var cadastrarCliente = new Button("Cadastrar cliente");
        cadastrarCliente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cadastrarCliente.addClickListener(clickBotao -> {
            var formularioValido = !clienteBinder.validate().hasErrors();
            if (formularioValido) {
                var clienteAtual = clienteBinder.getBean();
                clienteService.salvar(clienteAtual);
                Notification.show("Cliente salvo com sucesso");

                adicionarClienteGrid(clienteAtual);
                limparFormularioCliente();
            }
        });

        add(titulo, formularioCadastro, cadastrarCliente);
    }

    private void adicionarClienteGrid(Cliente cliente) {
        clientesProvider.getItems().add(cliente);
        clientesProvider.refreshAll();
    }

    private void limparFormularioCliente() {
        clienteBinder.setBean(new Cliente());
    }
}
