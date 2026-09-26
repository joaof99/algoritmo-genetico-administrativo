package com.genetico.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("solicitacao-roteirizacao")
@PageTitle("Solicitação de roteirização")
public class AlgoritmoGeneticoView extends VerticalLayout {
    private TextField txtTamanhoPopulacao;
    private TextField txtQuantidadeGeracoes;
    private TextField txtChanceOcorrenciaMutacao;
    private TextField txtChanceOcorrenciaCrossover;

    public AlgoritmoGeneticoView() {
        add(criarSecaoCadastro());
    }

    private Component criarSecaoCadastro() {
        return new VerticalLayout(
                new H3("Solicitar roteirização"),
                criarFormulario(),
                criarBotaoRoteirizacao()
        );
    }

    private FormLayout criarFormulario() {
        var formulario = new FormLayout();
        formulario.setId("form-solicitacao-roteirizacao");

        txtTamanhoPopulacao = new TextField("Digite o tamanho da poulação");
        txtTamanhoPopulacao.setId("txt-tamanho-populacao");
        txtTamanhoPopulacao.setMaxLength(3);
        txtTamanhoPopulacao.setRequired(true);

        txtQuantidadeGeracoes = new TextField("Digite a quantidade de gerações");
        txtQuantidadeGeracoes.setId("txt-quantidade-geracoes");
        txtQuantidadeGeracoes.setMaxLength(3);
        txtQuantidadeGeracoes.setRequired(true);

        txtChanceOcorrenciaMutacao = new TextField("Digite a probabilidade de chance de ocorrência de mutação");
        txtChanceOcorrenciaMutacao.setId("txt-chance-ocorrencia-mutacao");
        txtChanceOcorrenciaMutacao.setMaxLength(3);
        txtChanceOcorrenciaMutacao.setRequired(true);

        txtChanceOcorrenciaCrossover = new TextField("Digite a probabilidade de chance de ocorrência de crossover");
        txtChanceOcorrenciaCrossover.setId("txt-chance-ocorrencia-crossover");
        txtChanceOcorrenciaCrossover.setMaxLength(3);
        txtChanceOcorrenciaCrossover.setRequired(true);

        formulario.add(txtTamanhoPopulacao, txtQuantidadeGeracoes, txtChanceOcorrenciaMutacao, txtChanceOcorrenciaCrossover);

        return formulario;
    }

    private Button criarBotaoRoteirizacao() {
        var botaoRoteirizacao = new Button("Solicitar roteirização");
        botaoRoteirizacao.setId("btn-roteirizacao");
        botaoRoteirizacao.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoRoteirizacao.addClickListener(clickBotao -> {
            if (txtTamanhoPopulacao.getValue().isBlank() ||
                    txtChanceOcorrenciaCrossover.getValue().isBlank() ||
                    txtChanceOcorrenciaMutacao.getValue().isBlank() ||
                    txtQuantidadeGeracoes.getValue().isBlank()) {
                Notification.show("Todos os campos são obrigatórios para solicitar uma roteirização.", 4500, Notification.Position.MIDDLE);
            }
        });

        return botaoRoteirizacao;
    }
}

