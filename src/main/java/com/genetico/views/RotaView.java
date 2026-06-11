package com.genetico.views;

import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import com.genetico.service.EnderecoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("/cadastrar-rotas")
@PageTitle("Cadastro de Rotas")
public class RotaView extends VerticalLayout {

    private final Button botaoCadastroRota;
    private final MultiSelectComboBox<Endereco> comboBoxEnderecos;
    private final RotaRepository rotaRepository;
    private final EnderecoService enderecoService;

    public RotaView(EnderecoService enderecoService, RotaRepository rotaRepository) {
        this.enderecoService = enderecoService;
        this.rotaRepository = rotaRepository;
        this.comboBoxEnderecos = inicializarComboBoxEnderecos();
        this.botaoCadastroRota = inicializarBotaoCadastroRota();

        add(comboBoxEnderecos, botaoCadastroRota);
    }

    private Button inicializarBotaoCadastroRota() {
        var botaoCadastroRota = new Button("Cadastrar Rota");
        botaoCadastroRota.setId("btn-cadastro-rota");
        botaoCadastroRota.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoCadastroRota.addClickListener(evento -> {
            salvarRota();
            Notification.show("Rota salva com sucesso", 4500, Notification.Position.MIDDLE);
            comboBoxEnderecos.clear();
        });

        return botaoCadastroRota;
    }

    private MultiSelectComboBox<Endereco> inicializarComboBoxEnderecos() {
        var comboBoxEnderecos = new MultiSelectComboBox<Endereco>("Selecione os endereços da rota");

        comboBoxEnderecos.setId("cbx-listagem-enderecos");
        comboBoxEnderecos.setItems(enderecoService.buscarTodos());
        comboBoxEnderecos.setItemLabelGenerator(Endereco::getLogradouro);
        comboBoxEnderecos.setWidth("100%");

        return comboBoxEnderecos;
    }

    private void salvarRota() {
        try {
            var enderecosSelecionadosComboBox = List.copyOf(comboBoxEnderecos.getSelectedItems());

            var rota = new Rota(enderecosSelecionadosComboBox);
            rotaRepository.save(rota);
        } catch (Exception exception) {
            exception.printStackTrace();
            Notification.show("Erro inesperado ao salvar rota, tente novamente", 4000, Notification.Position.MIDDLE);
        }
    }
}

