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

    public RotaView(EnderecoService enderecoService, RotaRepository rotaRepository) {
        var botaoCadastroRota = inicializarBotaoCadastroRota();
        var comboBoxEnderecos = inicializarComboBoxEnderecos(enderecoService.buscarTodos());

        botaoCadastroRota.addClickListener(evento -> {
            try {
                var enderecosSelecionadosComboBox = comboBoxEnderecos.getSelectedItems().stream().toList();

                var rota = new Rota(enderecosSelecionadosComboBox);
                rotaRepository.save(rota);
                comboBoxEnderecos.clear();

                Notification.show("Rota salva com sucesso", 3000, Notification.Position.MIDDLE);
            } catch (Exception exception) {
                Notification.show("Erro inesperado ao salvar rota, tente novamente", 4000, Notification.Position.MIDDLE);
            }
        });

        add(comboBoxEnderecos, botaoCadastroRota);
    }

    private Button inicializarBotaoCadastroRota() {
        var botaoCadastroRota = new Button("Cadastrar Rota");
        botaoCadastroRota.setId("btn-cadastro-rota");
        botaoCadastroRota.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return botaoCadastroRota;
    }

    private MultiSelectComboBox<Endereco> inicializarComboBoxEnderecos(List<Endereco> itensComboBox) {
        var cbxEnderecos = new MultiSelectComboBox<Endereco>("Selecione os endereços da rota");

        cbxEnderecos.setId("cbx-listagem-enderecos");
        cbxEnderecos.setItems(itensComboBox);
        cbxEnderecos.setItemLabelGenerator(Endereco::getLogradouro);
        cbxEnderecos.setWidth("100%");

        return cbxEnderecos;
    }
}

