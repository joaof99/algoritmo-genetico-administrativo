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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Endereços")
public class EnderecoView extends VerticalLayout {
    private final Logger log = LoggerFactory.getLogger(EnderecoView.class);
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

    public EnderecoView(EnderecoService enderecoService, IntegracaoLocationIQAPI integracaoLocationIQAPI) {
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
        botaoBuscaAPI = criarBotaoBuscaApi();

        var cabecalho = new HorizontalLayout(new H3("Cadastro de endereço"), botaoBuscaAPI);
        cabecalho.setWidthFull();
        cabecalho.setAlignItems(FlexComponent.Alignment.CENTER);
        cabecalho.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        botaoCadastroEndereco = criarBotaoCadastroEndereco();

        var secao = new VerticalLayout(cabecalho, criarFormularioCadastroEndereco(), botaoCadastroEndereco);
        secao.addClassName("cartao");
        secao.setHorizontalComponentAlignment(Alignment.END, botaoCadastroEndereco);

        return secao;
    }

    private Button criarBotaoCadastroEndereco() {
        var botaoCadastroEndereco = new Button("Cadastrar endereço");
        botaoCadastroEndereco.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoCadastroEndereco.addClickListener(clickBotao -> {
            if (!enderecoBinder.validate().hasErrors()) {
                try {
                    var novoEndereco = enderecoService.salvar(enderecoBinder.getBean());
                    adicionarEnderecoGrid(novoEndereco);
                    limparFormularioEndereco();
                    Notificacao.sucesso("Endereço salvo com sucesso");
                } catch (Exception exception) {
                    log.error(exception.getMessage(), exception);
                    Notificacao.erro("Erro inesperado ao salvar endereço, tente novamente");
                }
            }
        });

        return botaoCadastroEndereco;
    }

    private FormLayout criarFormularioCadastroEndereco() {
        var formularioCadastroEndereco = new FormLayout();
        formularioCadastroEndereco.setId("form-cadastro-endereco");
        formularioCadastroEndereco.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 4)
        );

        logradouro = new TextField("Logradouro");
        logradouro.setId("txt-form-cadastro-logradouro");
        logradouro.setMaxLength(255);

        numero = new TextField("Número");
        numero.setId("txt-form-cadastro-numero");
        numero.setMaxLength(10);

        bairro = new TextField("Bairro");
        bairro.setMaxLength(255);
        bairro.setId("txt-form-cadastro-bairro");

        complemento = new TextField("Complemento");
        complemento.setId("txt-form-cadastro-complemento");
        complemento.setMaxLength(255);

        cep = criarCep();

        cidade = new TextField("Cidade");
        cidade.setId("txt-form-cadastro-cidade");
        cidade.setMaxLength(255);
        cidade.setAllowedCharPattern("[a-zA-ZÀ-ÿ\\s]");

        uf = criarComboBoxUF();

        latitude = new NumberField("Latitude");
        latitude.setId("txt-form-cadastro-latitude");
        latitude.setAllowedCharPattern("[0-9.\\-]");

        longitude = new NumberField("Longitude");
        longitude.setId("txt-form-cadastro-longitude");
        longitude.setAllowedCharPattern("[0-9.\\-]");

        formularioCadastroEndereco.add(logradouro, 2);
        formularioCadastroEndereco.add(numero, complemento, bairro, cep, cidade, uf);
        formularioCadastroEndereco.add(latitude, 2);
        formularioCadastroEndereco.add(longitude, 2);

        return formularioCadastroEndereco;
    }

    private TextField criarCep() {
        var cep = new TextField("CEP");
        cep.setId("txt-form-cadastro-cep");
        cep.setMaxLength(9);
        cep.setAllowedCharPattern("[0-9\\-]");

        return cep;
    }

    private ComboBox<String> criarComboBoxUF() {
        var comboBoxUF = new ComboBox<String>("UF");
        comboBoxUF.setId("cbx-form-cadastro-uf");
        comboBoxUF.setWidth("100%");

        comboBoxUF.setItems("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
                "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

        return comboBoxUF;
    }

    private void adicionarEnderecoGrid(Endereco endereco) {
        enderecoProvider.getItems().add(endereco);
        enderecoProvider.refreshAll();
    }

    private Button criarBotaoBuscaApi() {
        var botaoBuscaApi = new Button("Buscar latitude/longitude", VaadinIcon.SEARCH.create());
        botaoBuscaApi.setId("botao-busca-api");
        botaoBuscaApi.setTooltipText("Todos os campos de endereço marcados com * são obrigatórios para pesquisar na API");

        botaoBuscaApi.addClickListener(clickBotao -> {
            var algumCampoFaltando = logradouro.isEmpty() || bairro.isEmpty() || uf.isEmpty() || cidade.isEmpty() || cep.isEmpty();

            if (algumCampoFaltando) {
                Notificacao.erro("Erro. Todos os campos de endereço marcados com * são obrigatórios para pesquisar na API");
            } else {
                try {
                    botaoBuscaApi.setEnabled(false);
                    log.info("Buscando logradouro {}", logradouro.getValue());

                    var numeroEndereco = numero.getValue().isBlank() ? null : numero.getValue();

                    var coordenadaGeografica = integracaoLocationIQAPI.buscarCoordenadaGeografica(logradouro.getValue(), numeroEndereco,
                            bairro.getValue(), uf.getValue(),
                            cidade.getValue(), cep.getValue());

                    latitude.setValue(coordenadaGeografica.latitude());
                    longitude.setValue(coordenadaGeografica.longitude());
                    Notificacao.sucesso("Latitude e longitude encontradas com sucesso. Foram definidas nos campos de texto");
                } catch (IntegracaoLocationIQAPIException exception) {
                    log.error(exception.getMessage(), exception);
                    Notificacao.erro("Erro ao consultar a API. Tente novamente");
                } finally {
                    botaoBuscaAPI.setEnabled(true);
                }
            }
        });

        return botaoBuscaApi;
    }

    private Component criarSecaoListagemEnderecos() {
        grid = criarGrid();

        var secao = new VerticalLayout(new H3("Endereços cadastrados"), grid);
        secao.addClassName("cartao");

        return secao;
    }

    private Grid<Endereco> criarGrid() {
        var grid = new Grid<>(Endereco.class, false);

        grid.addColumn(Endereco::getLogradouro).setHeader("Logradouro");
        grid.addColumn(Endereco::getNumero).setHeader("Número");
        grid.addColumn(Endereco::getBairro).setHeader("Bairro");
        grid.addColumn(Endereco::getCep).setHeader("CEP");
        grid.addColumn(Endereco::getCidade).setHeader("Cidade");
        grid.addColumn(Endereco::getUf).setHeader("UF");
        grid.addColumn(Endereco::getLatitude).setHeader("Latitude");
        grid.addColumn(Endereco::getLongitude).setHeader("Longitude");

        grid.setId("grid-enderecos");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        var enderecos = enderecoService.buscarTodos();
        enderecoProvider = new ListDataProvider<>(enderecos);

        grid.setDataProvider(enderecoProvider);

        return grid;
    }

    private void limparFormularioEndereco() {
        enderecoBinder.setBean(new Endereco());
    }
}
