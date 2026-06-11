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

@Route("/cadastrar-rotas")
@PageTitle("Cadastro de Rotas")
public class RotaView extends VerticalLayout {

    public RotaView(EnderecoService enderecoService, RotaRepository rotaRepository) {
        var cbxEnderecos = new MultiSelectComboBox<Endereco>("Selecione os endereços da rota");
        cbxEnderecos.setItems(enderecoService.buscarTodos());
        cbxEnderecos.setItemLabelGenerator(Endereco::getLogradouro);
        cbxEnderecos.setWidth("100%");

        var cadastrarRota = new Button("Cadastrar Rota");
        cadastrarRota.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cadastrarRota.addClickListener(evento -> {
            var rota = new Rota();
            var enderecosSelecionadosComboBox = cbxEnderecos.getSelectedItems().stream().toList();

            rota.setEnderecos(enderecosSelecionadosComboBox);
            rotaRepository.save(rota);

            Notification.show("Rota salva com sucesso", 3000, Notification.Position.MIDDLE);
        });

        add(cbxEnderecos, cadastrarRota);
    }

}
