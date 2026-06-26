package com.genetico.views;

import com.genetico.api.IntegracaoLocationIQAPI;
import com.genetico.api.IntegracaoLocationIQAPIException;
import com.genetico.model.Endereco;
import com.genetico.service.EnderecoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    private TextField cidade;
    private ComboBox<String> uf;
    private NumberField latitude;
    private NumberField longitude;

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

        return new VerticalLayout(tituloCadastroEndereco, botaoBuscaAPI, formularioCadastroEndereco, botaoCadastroEndereco);
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
                    log.error(exception.getMessage(), exception);
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
        logradouro.setId("txt-form-cadastro-logradouro");

        numero = new TextField("Digite o número");
        numero.setId("txt-form-cadastro-numero");

        bairro = new TextField("Digite o bairro");
        bairro.setId("txt-form-cadastro-bairro");

        complemento = new TextField("Digite o complemento");
        complemento.setId("txt-form-cadastro-complemento");

        cep = new TextField("Digite o cep");
        cep.setId("txt-form-cadastro-cep");

        cidade = new TextField("Digite a cidade");
        cidade.setId("txt-form-cadastro-cidade");

        uf = criarComboBoxUF();

        latitude = new NumberField("Digite a latitude");
        latitude.setId("txt-form-cadastro-latitude");

        longitude = new NumberField("Digite a longitude");
        longitude.setId("txt-form-cadastro-longitude");

        formularioCadastroEndereco.add(logradouro, numero, bairro, complemento, cep, cidade, uf, latitude, longitude);

        return formularioCadastroEndereco;
    }

    private ComboBox<String> criarComboBoxUF() {
        var comboBoxUF = new ComboBox<String>("UF");
        comboBoxUF.setId("cbx-form-cadastro-uf");
        comboBoxUF.setWidth("100%");

        var ufs = List.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

        comboBoxUF.setItems(ufs);
        comboBoxUF.setValue(ufs.getFirst());

        return comboBoxUF;
    }

    private void adicionarEnderecoGrid(Endereco endereco) {
        enderecoProvider.getItems().add(endereco);
        enderecoProvider.refreshAll();
    }

    private Button criarBotaoBuscaApi() {
        var botaoBuscaApi = new Button("Buscar latitude/longitude na API");
        botaoBuscaApi.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        botaoBuscaApi.setId("botao-busca-api");
        botaoBuscaApi.setTooltipText("Todos os campos de endereço marcados com * são obrigatórios para pesquisar na API");

        botaoBuscaApi.addClickListener(clickBotao -> {
            var algumCampoFaltando = logradouro.isEmpty() || numero.isEmpty() || bairro.isEmpty() || uf.isEmpty() || cidade.isEmpty() || cep.isEmpty();

            if (algumCampoFaltando) {
                exibirMensagemErro("Erro. Todos os campos de endereço marcados com * são obrigatórios para pesquisar na API");
            } else {
                try {
                    botaoBuscaApi.setEnabled(false);
                    log.info("Buscando logradouro {}", logradouro.getValue());

                    var coordenadaGeografica = integracaoLocationIQAPI.buscarCoordenadaGeografica(logradouro.getValue(), numero.getValue(), bairro.getValue(), uf.getValue(), cidade.getValue(), cep.getValue());

                    latitude.setValue(coordenadaGeografica.latitude());
                    longitude.setValue(coordenadaGeografica.longitude());
                    exibirMensagemSucesso("Latitude e longitude encontradas com sucesso. Foram definidas nos campos de texto");
                } catch (IntegracaoLocationIQAPIException exception) {
                    log.error(exception.getMessage(), exception);
                    exibirMensagemErro("Erro ao consultar a API. Tente novamente");
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
