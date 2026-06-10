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

import java.util.List;

@Route("")
@PageTitle("Algoritmo Genético Administrativo")
public class MainView extends VerticalLayout {
    private BeanValidationBinder<Cliente> clienteBinder;

    private final TextField logradouro = new TextField("Digite o logradouro");
    private final TextField latitude = new TextField("Digite a latitude");
    private final TextField longitude = new TextField("Digite a longitude");

    private Grid<Cliente> grid;
    private ListDataProvider<Cliente> clientesProvider;

    public MainView(ClienteService clienteService) {
        inicializarGrid();
        inicializarClienteBinder();
        criarCadastroClientes(clienteService);
        criarListagemClientes(clienteService);
    }

    private void inicializarGrid() {
        grid = new Grid<>(Cliente.class, true);
        grid.setId("grid-clientes");
    }

    private void inicializarClienteBinder() {
        clienteBinder = new BeanValidationBinder<>(Cliente.class);
        clienteBinder.bindInstanceFields(this);
        clienteBinder.setBean(new Cliente());
    }

    private void criarCadastroClientes(ClienteService clienteService) {
        var titulo = new H3("Cadastro de cliente");

        var formularioCadastro = new FormLayout();
        formularioCadastro.add(logradouro, latitude, longitude);

        var cadastrarCliente = new Button("Cadastrar cliente");
        cadastrarCliente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cadastrarCliente.addClickListener(clickBotao -> {
            var formularioValido = !clienteBinder.validate().hasErrors();
            if (formularioValido) {
                try {
                    var novoCliente = clienteService.salvar(clienteBinder.getBean());
                    adicionarClienteGrid(novoCliente);
                    limparFormularioCliente();
                    Notification.show("Cliente salvo com sucesso", 3000, Notification.Position.MIDDLE);
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                    Notification.show("Erro inesperado ao salvar cliente, tente novamente", 4000, Notification.Position.MIDDLE);
                }
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

    private void criarListagemClientes(ClienteService clienteService) {
        var titulo = new H3("Listagem de clientes existentes");

        var clientes = clienteService.buscarTodos();
        incluirClientesGrid(clientes);

        add(titulo, grid);
    }

    private void incluirClientesGrid(List<Cliente> itensGrid) {
        clientesProvider = new ListDataProvider<>(itensGrid);
        grid.setDataProvider(clientesProvider);
    }
}
