package com.genetico.views;

import com.genetico.model.Cliente;
import com.genetico.model.Rota;
import com.genetico.service.ClienteService;
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

    public RotaView(ClienteService clienteService) {
        var cbxClientes = new MultiSelectComboBox<Cliente>("Selecione os clientes da rota");
        cbxClientes.setItems(clienteService.buscarTodos());
        cbxClientes.setItemLabelGenerator(Cliente::getDescricao);
        cbxClientes.setWidth("100%");

        var cadastrarRota = new Button("Cadastrar Rota");
        cadastrarRota.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cadastrarRota.addClickListener(buttonClickEvent -> {
            var rota = new Rota();
            var clientesSelecionados = cbxClientes.getSelectedItems().stream().toList();
            rota.setClientes(clientesSelecionados);

            Notification.show("Rota salva com sucesso", 3000, Notification.Position.MIDDLE);
        });

        add(cbxClientes, cadastrarRota);
    }

}
