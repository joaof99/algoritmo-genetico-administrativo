package com.genetico.views;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.api.IntegracaoLocationIQAPIException;
import com.genetico.model.Endereco;
import com.genetico.service.EnderecoService;
import com.vaadin.flow.component.Component;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("")
@PageTitle("Algoritmo Genético Administrativo")
public class MainView extends VerticalLayout {
    private final Logger log = LoggerFactory.getLogger(MainView.class);
    private final BeanValidationBinder<Endereco> enderecoBinder;

    private TextField logradouro;
    private TextField numero;
    private TextField complemento;
    private TextField bairro;
    private TextField cep;
    private TextField latitude;
    private TextField longitude;

    private Grid<Endereco> grid;
    private ListDataProvider<Endereco> enderecoProvider;

    private final IntegracaoLocationIQAPI integracaoLocationIQAPI;
    private final EnderecoService enderecoService;
    private Button botaoBuscaAPI;
    private Button botaoCadastroEndereco;

    public MainView(EnderecoService enderecoService, IntegracaoLocationIQAPI integracaoLocationIQAPI) {
        this.enderecoService = enderecoService;
        this.integracaoLocationIQAPI = integracaoLocationIQAPI;
        add(criarSecaoCadastroEnderecos());
        enderecoBinder = criarEnderecoBinder();
        add(criarSecaoListagemEnderecos());
    }

    private BeanValidationBinder<Endereco> criarEnderecoBinder() {
        var enderecoBinder = new BeanValidationBinder<>(Endereco.class);
        enderecoBinder.bindInstanceFields(this);
        enderecoBinder.setBean(new Endereco());

        return enderecoBinder;
    }

    private Component criarSecaoCadastroEnderecos() {
        var tituloCadastroEndereco = new H3("Cadastro de endereço");
        var formularioCadastroEndereco = criarFormularioCadastroEndereco();
        botaoCadastroEndereco = criarBotaoCadastroEndereco();
        botaoBuscaAPI = criarBotaoBuscaApi();

        return new VerticalLayout(tituloCadastroEndereco, formularioCadastroEndereco, botaoCadastroEndereco, botaoBuscaAPI);
    }

    private Button criarBotaoCadastroEndereco() {
        var botaoCadastroEndereco = new Button("Cadastrar endereco");
        botaoCadastroEndereco.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoCadastroEndereco.addClickListener(clickBotao -> {
            if (!enderecoBinder.validate().hasErrors()) {
                try {
                    var novoEndereco = enderecoService.salvar(enderecoBinder.getBean());
                    adicionarEnderecoGrid(novoEndereco);
                    limparFormularioEndereco();
                    exibirMensagemSucesso("Endereço salvo com sucesso");
                } catch (Exception exception) {
                    log.error(exception.getMessage());
                    Notification.show("Erro inesperado ao salvar endereço, tente novamente", 4000, Notification.Position.MIDDLE);
                }
            }
        });

        return botaoCadastroEndereco;
    }

    private FormLayout criarFormularioCadastroEndereco() {
        var formularioCadastroEndereco = new FormLayout();
        formularioCadastroEndereco.setId("form-cadastro-endereco");

        logradouro = new TextField("Digite o logradouro");
        logradouro.setId("txt-logradouro");

        numero = new TextField("Digite o número");
        numero.setId("txt-numero");

        bairro = new TextField("Digite o bairro");
        bairro.setId("txt-bairro");

        complemento = new TextField("Digite o complemento");
        complemento.setId("txt-complemento");

        cep = new TextField("Digite o cep");
        cep.setId("txt-cep");

        latitude = new TextField("Digite a latitude");
        latitude.setId("txt-latitude");

        longitude = new TextField("Digite a longitude");
        longitude.setId("txt-longitude");

        formularioCadastroEndereco.add(logradouro, numero, bairro, complemento, cep, latitude, longitude);

        return formularioCadastroEndereco;
    }

    private void adicionarEnderecoGrid(Endereco endereco) {
        enderecoProvider.getItems().add(endereco);
        enderecoProvider.refreshAll();
    }

    private Button criarBotaoBuscaApi() {
        var botaoBuscaApi = new Button("Buscar lat/long na API");
        botaoBuscaApi.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        botaoBuscaApi.setId("botao-busca-api");

        botaoBuscaApi.addClickListener(clickBotao -> {
            if (logradouro.isEmpty()) {
                exibirMensagemErro("Erro. Digite o logradouro antes de pesquisar na API");
            } else {
                try {
                    botaoBuscaApi.setEnabled(false);
                    log.info("Buscando logradouro {}", logradouro.getValue());

                    var coordenadaGeografica = integracaoLocationIQAPI.buscarCoordenadaGeografica(logradouro.getValue());
                    latitude.setValue(String.valueOf(coordenadaGeografica.latitude()));
                    longitude.setValue(String.valueOf(coordenadaGeografica.longitude()));
                    exibirMensagemSucesso("Latitude e longitude encontradas com sucesso. Foram definidas nos campos de texto");
                } catch (IntegracaoLocationIQAPIException e) {
                    log.error(e.getMessage(), e);
                    log.info("Houve um erro ao efetuar requisição a API, tente novamente");
                } finally {
                    botaoBuscaAPI.setEnabled(true);
                }
            }
        });

        return botaoBuscaApi;
    }

    private Component criarSecaoListagemEnderecos() {
        var tituloListagemClientes = new H3("Listagem de endereços existentes");
        grid = criarGrid();

        return new VerticalLayout(tituloListagemClientes, grid);
    }

    private Grid<Endereco> criarGrid() {
        grid = new Grid<>(Endereco.class, true);
        grid.setId("grid-enderecos");

        var enderecos = enderecoService.buscarTodos();

        enderecoProvider = new ListDataProvider<>(enderecos);
        grid.setDataProvider(enderecoProvider);

        return grid;
    }

    private void exibirMensagemErro(String mensagem) {
        var notification = new Notification(mensagem, 4500, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.open();
    }

    private void exibirMensagemSucesso(String mensagem) {
        var notification = new Notification(mensagem, 4500, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.open();
    }

    private void limparFormularioEndereco() {
        enderecoBinder.setBean(new Endereco());
    }
}
