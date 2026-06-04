package com.genetico.views;

import com.genetico.model.Cliente;
import com.genetico.service.ClienteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("/cadastrar-cliente")
@PageTitle("Cadastro de cliente")
public class CadastroClienteView extends VerticalLayout {
    private final BeanValidationBinder<Cliente> clienteBinder = new BeanValidationBinder<>(Cliente.class);
    private final TextField descricao = new TextField("Digite a descrição");
    private final TextField latitude = new TextField("Digite a latitude");
    private final TextField longitude = new TextField("Digite a longitude");
    private final Cliente cliente = new Cliente();

    public CadastroClienteView(ClienteService clienteService) {
        clienteBinder.bindInstanceFields(this);
        clienteBinder.setBean(cliente);

        var titulo = new H3("Cadastro de cliente");

        var formularioCadastro = new FormLayout();
        formularioCadastro.add(descricao, latitude, longitude);

        var botaoCadastrarCliente = new Button("Cadastrar cliente");
        botaoCadastrarCliente.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoCadastrarCliente.addClickListener(clickBotao -> {
            if (!clienteBinder.validate().hasErrors()) {
                clienteService.salvar(cliente);
                Notification.show("Cliente salvo com sucesso");
            }
        });

        add(titulo, formularioCadastro, botaoCadastrarCliente);
    }
}
