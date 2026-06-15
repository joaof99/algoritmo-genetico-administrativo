package com.genetico.views;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.model.Endereco;
import com.genetico.service.EnderecoService;
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

import java.io.IOException;
import java.util.List;

@Route("")
@PageTitle("Algoritmo Genético Administrativo")
public class MainView extends VerticalLayout {
    private BeanValidationBinder<Endereco> enderecoBinder;

    private final TextField logradouro = new TextField("Digite o logradouro");
    private final TextField latitude = new TextField("Digite a latitude");
    private final TextField longitude = new TextField("Digite a longitude");

    private Grid<Endereco> grid;
    private ListDataProvider<Endereco> enderecoProvider;

    private final IntegracaoLocationIQAPI integracaoLocationIQAPI;

    public MainView(EnderecoService enderecoService) {
        integracaoLocationIQAPI = new IntegracaoLocationIQAPI();
        inicializarGrid();
        inicializarEnderecoBinder();
        inicializarBotoes(enderecoService);
        inicializarListagemEnderecos(enderecoService);
    }

    private void inicializarGrid() {
        grid = new Grid<>(Endereco.class, true);
        grid.setId("grid-enderecos");
    }

    private void inicializarEnderecoBinder() {
        enderecoBinder = new BeanValidationBinder<>(Endereco.class);
        enderecoBinder.bindInstanceFields(this);
        enderecoBinder.setBean(new Endereco());
    }

    private void inicializarBotoes(EnderecoService enderecoService) {
        var titulo = new H3("Cadastro de endereço");

        var formularioCadastro = new FormLayout();
        formularioCadastro.add(logradouro, latitude, longitude);

        var cadastrarEndereco = new Button("Cadastrar endereco");
        cadastrarEndereco.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cadastrarEndereco.addClickListener(clickBotao -> {
            if (!enderecoBinder.validate().hasErrors()) {
                try {
                    var novoEndereco = enderecoService.salvar(enderecoBinder.getBean());
                    adicionarEnderecoGrid(novoEndereco);
                    limparFormularioEndereco();
                    Notification.show("Endereço salvo com sucesso", 3000, Notification.Position.MIDDLE);
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                    Notification.show("Erro inesperado ao salvar endereço, tente novamente", 4000, Notification.Position.MIDDLE);
                }
            }
        });

        var botaoBuscaApi = inicializarBotaoBuscaApi();

        add(titulo, formularioCadastro, cadastrarEndereco, botaoBuscaApi);
    }

    private Button inicializarBotaoBuscaApi() {
        var botaoBuscaApi = new Button("Buscar lat/long na API");
        botaoBuscaApi.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoBuscaApi.addClickListener(clickBotao -> {
            if (logradouro.isEmpty()) {
                Notification.show("Erro. Digite o logradouro antes de pesquisar na API", 4500, Notification.Position.MIDDLE);
            } else {
                try {
                    System.out.println("Buscando logradouro: " + logradouro.getValue());

                    var coordenadaGeografica = integracaoLocationIQAPI.buscarCoordenadaGeografica(logradouro.getValue());
                    latitude.setValue(String.valueOf(coordenadaGeografica.latitude()));
                    longitude.setValue(String.valueOf(coordenadaGeografica.longitude()));
                } catch (IOException e) {
                    Notification.show("Houve um erro de I/O ao consultar na API LocationIQ", 4500, Notification.Position.MIDDLE);
                } catch (InterruptedException e) {
                    Notification.show("Conexão interrompida ao consultar API LocationIQ", 4500, Notification.Position.MIDDLE);
                }
            }
        });

        return botaoBuscaApi;
    }

    private void adicionarEnderecoGrid(Endereco endereco) {
        enderecoProvider.getItems().add(endereco);
        enderecoProvider.refreshAll();
    }

    private void limparFormularioEndereco() {
        enderecoBinder.setBean(new Endereco());
    }

    private void inicializarListagemEnderecos(EnderecoService enderecoService) {
        var titulo = new H3("Listagem de endereços existentes");

        var enderecos = enderecoService.buscarTodos();
        incluirEnderecosGrid(enderecos);

        add(titulo, grid);
    }

    private void incluirEnderecosGrid(List<Endereco> itensGrid) {
        enderecoProvider = new ListDataProvider<>(itensGrid);
        grid.setDataProvider(enderecoProvider);
    }
}
