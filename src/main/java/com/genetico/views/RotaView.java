package com.genetico.views;

import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.service.EnderecoService;
import com.genetico.service.RotaService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "cadastro-rotas", layout = MainLayout.class)
@PageTitle("Rotas")
public class RotaView extends VerticalLayout {

    private final Button botaoCadastroRota;
    private final MultiSelectComboBox<Endereco> comboBoxEnderecos;
    private final RotaService rotaService;
    private final List<Endereco> enderecos;
    private final BeanValidationBinder<Rota> rotaBinder;
    private final Grid<Rota> gridRotas;

    public RotaView(EnderecoService enderecoService, RotaService rotaService) {
        enderecos = List.copyOf(enderecoService.buscarTodos());
        this.rotaService = rotaService;
        rotaBinder = new BeanValidationBinder<>(Rota.class);
        comboBoxEnderecos = inicializarComboBoxEnderecos();
        botaoCadastroRota = inicializarBotaoCadastroRota();
        gridRotas = criarGridRotas();

        add(criarSecaoCadastroRota(), criarSecaoListagemRotas());
    }

    private Component criarSecaoCadastroRota() {
        var secao = new VerticalLayout(new H3("Cadastro de rota"), comboBoxEnderecos, botaoCadastroRota);
        secao.addClassName("cartao");
        secao.setHorizontalComponentAlignment(Alignment.END, botaoCadastroRota);

        return secao;
    }

    private Component criarSecaoListagemRotas() {
        var secao = new VerticalLayout(new H3("Rotas cadastradas"), gridRotas);
        secao.addClassName("cartao");

        return secao;
    }

    private MultiSelectComboBox<Endereco> inicializarComboBoxEnderecos() {
        var comboBoxEnderecos = new MultiSelectComboBox<Endereco>("Selecione os endereços da rota");

        comboBoxEnderecos.setId("cbx-listagem-enderecos");
        comboBoxEnderecos.setItems(enderecos);
        comboBoxEnderecos.setItemLabelGenerator(Endereco::getLogradouro);
        comboBoxEnderecos.setWidth("100%");

        configurarBinderComboBox(comboBoxEnderecos);

        return comboBoxEnderecos;
    }

    private void configurarBinderComboBox(MultiSelectComboBox<Endereco> comboBoxEnderecos) {
        rotaBinder.forField(comboBoxEnderecos).withConverter(ArrayList::new, HashSet::new).bind("enderecos");
    }

    private Button inicializarBotaoCadastroRota() {
        var botaoCadastroRota = new Button("Cadastrar rota");
        botaoCadastroRota.setId("btn-cadastro-rota");
        botaoCadastroRota.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoCadastroRota.addClickListener(evento -> {
            if (salvarRota()) {
                comboBoxEnderecos.clear();
                gridRotas.getDataProvider().refreshAll();
            }
        });

        return botaoCadastroRota;
    }

    private boolean salvarRota() {
        var rota = new Rota();
        if (rotaBinder.writeBeanIfValid(rota)) {
            rotaService.salvar(rota);
            Notificacao.sucesso("Rota salva com sucesso");
            return true;
        } else {
            Notificacao.erro("Erro ao salvar rota. Verifique erros apresentados na tela");
            return false;
        }
    }

    private Grid<Rota> criarGridRotas() {
        var grid = new Grid<Rota>();
        grid.setId("grid-rotas");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(rota -> String.format("Rota %d", rota.getId()))
                .setHeader("Rota")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(rota -> rota.getEnderecos()
                        .stream()
                        .map(Endereco::getLogradouro)
                        .collect(Collectors.joining(", ")))
                .setHeader("Endereços");

        grid.addColumn(rota -> rota.getEnderecos().size())
                .setHeader("Paradas")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setItems(consulta -> rotaService
                .buscarPaginaComEnderecos(PageRequest.of(consulta.getPage(), consulta.getPageSize()))
                .stream());

        return grid;
    }
}
