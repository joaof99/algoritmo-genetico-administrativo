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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Route("cadastro-rotas")
@PageTitle("Cadastro de Rotas")
public class RotaView extends VerticalLayout {

    private final Button botaoCadastroRota;
    private final MultiSelectComboBox<Endereco> comboBoxEnderecos;
    private final RotaRepository rotaRepository;
    private final List<Endereco> enderecos;
    private final BeanValidationBinder<Rota> rotaBinder;

    public RotaView(EnderecoService enderecoService, RotaRepository rotaRepository) {
        this.enderecos = List.copyOf(enderecoService.buscarTodos());
        this.rotaRepository = rotaRepository;
        this.rotaBinder = new BeanValidationBinder<>(Rota.class);
        this.comboBoxEnderecos = inicializarComboBoxEnderecos();
        this.botaoCadastroRota = inicializarBotaoCadastroRota();

        add(comboBoxEnderecos, botaoCadastroRota);
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
        this.rotaBinder.forField(comboBoxEnderecos).withConverter(ArrayList::new, HashSet::new).bind("enderecos");
    }

    private Button inicializarBotaoCadastroRota() {
        var botaoCadastroRota = new Button("Cadastrar Rota");
        botaoCadastroRota.setId("btn-cadastro-rota");
        botaoCadastroRota.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoCadastroRota.addClickListener(evento -> {
            if (salvarRota()) {
                comboBoxEnderecos.clear();
            }
        });

        return botaoCadastroRota;
    }

    private boolean salvarRota() {
        var rota = new Rota();
        if (rotaBinder.writeBeanIfValid(rota)) {
            rotaRepository.save(rota);
            Notification.show("Rota salva com sucesso", 4500, Notification.Position.MIDDLE);
            return true;
        } else {
            Notification.show("Erro ao salvar rota. Verifique erros apresentados na tela", 4500, Notification.Position.MIDDLE);
            return false;
        }
    }
}

