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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;

@Route("")
@PageTitle("Algoritmo Genético Administrativo")
public class MainView extends VerticalLayout {
    private final BeanValidationBinder<Endereco> enderecoBinder;

    private final TextField logradouro = new TextField("Digite o logradouro");
    private final TextField latitude = new TextField("Digite a latitude");
    private final TextField longitude = new TextField("Digite a longitude");

    private Grid<Endereco> grid;
    private ListDataProvider<Endereco> enderecoProvider;

    private final IntegracaoLocationIQAPI integracaoLocationIQAPI;
    private final EnderecoService enderecoService;
    private final Button botaoBuscaAPI;
    private final Button botaoCastroEndereco;

    public MainView(EnderecoService enderecoService, IntegracaoLocationIQAPI integracaoLocationIQAPI) {
        this.enderecoService = enderecoService;
        this.integracaoLocationIQAPI = integracaoLocationIQAPI;
        enderecoBinder = inicializarEnderecoBinder();

        botaoCastroEndereco = inicializarBotaoCadastroEndereco();
        var tituloCadastroEndereco = new H3("Cadastro de endereço");
        var formularioCadastroEndereco = new FormLayout();
        formularioCadastroEndereco.add(logradouro, latitude, longitude);
        add(tituloCadastroEndereco, formularioCadastroEndereco, botaoCastroEndereco);

        botaoBuscaAPI = inicializarBotaoBuscaApi();
        add(botaoBuscaAPI);

        grid = inicializarGrid();
        var tituloListagemClientes = new H3("Listagem de endereços existentes");
        add(tituloListagemClientes, grid);
    }

    private Grid<Endereco> inicializarGrid() {
        grid = new Grid<>(Endereco.class, true);
        grid.setId("grid-enderecos");

        var enderecos = enderecoService.buscarTodos();

        enderecoProvider = new ListDataProvider<>(enderecos);
        grid.setDataProvider(enderecoProvider);

        return grid;
    }

    private BeanValidationBinder<Endereco> inicializarEnderecoBinder() {
        var enderecoBinder = new BeanValidationBinder<>(Endereco.class);
        enderecoBinder.bindInstanceFields(this);
        enderecoBinder.setBean(new Endereco());

        return enderecoBinder;
    }

    private Button inicializarBotaoCadastroEndereco() {
        var botaoCadastroEndereco = new Button("Cadastrar endereco");
        botaoCadastroEndereco.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoCadastroEndereco.addClickListener(clickBotao -> {
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

        return botaoCadastroEndereco;
    }

    private Button inicializarBotaoBuscaApi() {
        var botaoBuscaApi = new Button("Buscar lat/long na API");
        botaoBuscaApi.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoBuscaApi.addClickListener(clickBotao -> {
            if (logradouro.isEmpty()) {
                exibirMensagemErro("Erro. Digite o logradouro antes de pesquisar na API");
            } else {
                try {
                    System.out.println("Buscando logradouro: " + logradouro.getValue());

                    var coordenadaGeografica = integracaoLocationIQAPI.buscarCoordenadaGeografica(logradouro.getValue());
                    latitude.setValue(String.valueOf(coordenadaGeografica.latitude()));
                    longitude.setValue(String.valueOf(coordenadaGeografica.longitude()));
                } catch (IOException e) {
                    exibirMensagemErro("Houve um erro de I/O ao consultar na API LocationIQ");
                } catch (InterruptedException e) {
                    exibirMensagemErro("Conexão interrompida ao consultar API LocationIQ");
                }
            }
        });

        return botaoBuscaApi;
    }

    private void exibirMensagemErro(String mensagem) {
        var notification = new Notification(mensagem, 4500, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
    }

    private void adicionarEnderecoGrid(Endereco endereco) {
        enderecoProvider.getItems().add(endereco);
        enderecoProvider.refreshAll();
    }

    private void limparFormularioEndereco() {
        enderecoBinder.setBean(new Endereco());
    }
}
