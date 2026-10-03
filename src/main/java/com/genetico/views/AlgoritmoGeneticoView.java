package com.genetico.views;

import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import com.genetico.service.AlgoritmoGeneticoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.stream.Collectors;

@Route("solicitacao-roteirizacao")
@PageTitle("Solicitação de roteirização")
public class AlgoritmoGeneticoView extends VerticalLayout {
    private final RotaRepository rotaRepository;
    private final AlgoritmoGeneticoService algoritmoGeneticoService;
    private IntegerField txtTamanhoPopulacao;
    private IntegerField txtQuantidadeGeracoes;
    private IntegerField txtChanceOcorrenciaMutacao;
    private IntegerField txtChanceOcorrenciaCrossover;
    private ComboBox<Rota> cbxRotas;

    public AlgoritmoGeneticoView(RotaRepository rotaRepository, AlgoritmoGeneticoService algoritmoGeneticoService) {
        this.rotaRepository = rotaRepository;
        this.algoritmoGeneticoService = algoritmoGeneticoService;
        add(criarSecaoCadastro());
    }

    private Component criarSecaoCadastro() {
        cbxRotas = criarCbx();

        return new VerticalLayout(
                new H3("Solicitar roteirização"),
                cbxRotas,
                criarFormulario(),
                criarBotaoRoteirizacao()
        );
    }

    private ComboBox<Rota> criarCbx() {
        var cbx = new ComboBox<Rota>("Rota");
        cbx.setItems(rotaRepository.buscarTodosComEnderecos());
        cbx.setWidthFull();

        cbx.setItemLabelGenerator(rota ->
                "Rota: " + rota.getId() + " - " +
                        rota.getEnderecos()
                                .stream()
                                .limit(5)
                                .map(Endereco::getLogradouro)
                                .collect(Collectors.joining(", "))
        );

        return cbx;
    }


    private FormLayout criarFormulario() {
        var formulario = new FormLayout();
        formulario.setId("form-solicitacao-roteirizacao");

        txtTamanhoPopulacao = new IntegerField("Digite o tamanho da poulação");
        txtTamanhoPopulacao.setId("txt-tamanho-populacao");
        txtTamanhoPopulacao.setRequired(true);
        txtTamanhoPopulacao.setMin(20);
        txtTamanhoPopulacao.setMax(500);
        txtTamanhoPopulacao.setTooltipText("Mínimo de 20 e no máximo 500 populações");

        txtQuantidadeGeracoes = new IntegerField("Digite a quantidade de gerações");
        txtQuantidadeGeracoes.setId("txt-quantidade-geracoes");
        txtQuantidadeGeracoes.setRequired(true);
        txtQuantidadeGeracoes.setMin(10);
        txtQuantidadeGeracoes.setMax(1000);
        txtQuantidadeGeracoes.setTooltipText("Mínimo 10 e no máximo 1000 gerações");

        txtChanceOcorrenciaMutacao = new IntegerField("Digite a probabilidade de chance de ocorrência de mutação");
        txtChanceOcorrenciaMutacao.setId("txt-chance-ocorrencia-mutacao");
        txtChanceOcorrenciaMutacao.setRequired(true);
        txtChanceOcorrenciaMutacao.setMin(1);
        txtChanceOcorrenciaMutacao.setMax(100);
        txtChanceOcorrenciaMutacao.setTooltipText("Probabilidade entre 1 e 100%");

        txtChanceOcorrenciaCrossover = new IntegerField("Digite a probabilidade de chance de ocorrência de crossover");
        txtChanceOcorrenciaCrossover.setId("txt-chance-ocorrencia-crossover");
        txtChanceOcorrenciaCrossover.setRequired(true);
        txtChanceOcorrenciaCrossover.setMin(1);
        txtChanceOcorrenciaCrossover.setMax(100);
        txtChanceOcorrenciaCrossover.setTooltipText("Probabilidade entre 1 e 100%");

        formulario.add(txtTamanhoPopulacao, txtQuantidadeGeracoes, txtChanceOcorrenciaMutacao, txtChanceOcorrenciaCrossover);

        return formulario;
    }

    private Button criarBotaoRoteirizacao() {
        var botaoRoteirizacao = new Button("Solicitar roteirização");
        botaoRoteirizacao.setId("btn-roteirizacao");
        botaoRoteirizacao.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoRoteirizacao.addClickListener(clickBotao -> {
            if (txtTamanhoPopulacao.getValue() == null ||
                    txtChanceOcorrenciaCrossover.getValue() == null ||
                    txtChanceOcorrenciaMutacao.getValue() == null ||
                    txtQuantidadeGeracoes.getValue() == null) {
                Notification.show("Todos os campos são obrigatórios para solicitar uma roteirização.", 4500, Notification.Position.MIDDLE);
                return;
            }

            if (cbxRotas.getValue() == null) {
                Notification.show("É obrigatório selecionar a rota.", 4500, Notification.Position.MIDDLE);
                return;
            }

            solicitarRoteirizacao();
        });

        return botaoRoteirizacao;
    }

    private void solicitarRoteirizacao() {
        var idRota = cbxRotas.getValue().getId();
        var tamanhoPopulacao = txtTamanhoPopulacao.getValue();
        var quantidadeGeracoes = txtQuantidadeGeracoes.getValue();
        var chanceOcorrenciaMutacao = txtChanceOcorrenciaMutacao.getValue();
        var chanceOcorrenciaCrossover = txtChanceOcorrenciaCrossover.getValue();

        algoritmoGeneticoService.iniciarAlgoritmoGenetico(idRota,
                tamanhoPopulacao,
                quantidadeGeracoes,
                chanceOcorrenciaMutacao,
                chanceOcorrenciaCrossover);
    }
}

