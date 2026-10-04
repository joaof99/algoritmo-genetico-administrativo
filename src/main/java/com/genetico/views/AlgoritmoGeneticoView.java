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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.stream.Collectors;

@Route("solicitacao-roteirizacao")
@PageTitle("Solicitação de roteirização")
public class AlgoritmoGeneticoView extends VerticalLayout {
    static class ParametrosAlgoritmo {
        private Rota rota;
        private Integer tamanhoPopulacao;
        private Integer quantidadeGeracoes;
        private Integer chanceMutacao;
        private Integer chanceCrossover;

        public void setRota(Rota rota) {
            this.rota = rota;
        }

        public Rota getRota() {
            return rota;
        }

        public Integer getTamanhoPopulacao() {
            return tamanhoPopulacao;
        }

        public void setTamanhoPopulacao(Integer valor) {
            tamanhoPopulacao = valor;
        }

        public Integer getQuantidadeGeracoes() {
            return quantidadeGeracoes;
        }

        public void setQuantidadeGeracoes(Integer valor) {
            quantidadeGeracoes = valor;
        }

        public Integer getChanceMutacao() {
            return chanceMutacao;
        }

        public void setChanceMutacao(Integer valor) {
            chanceMutacao = valor;
        }

        public Integer getChanceCrossover() {
            return chanceCrossover;
        }

        public void setChanceCrossover(Integer valor) {
            chanceCrossover = valor;
        }
    }

    private final RotaRepository rotaRepository;
    private final AlgoritmoGeneticoService algoritmoGeneticoService;
    private IntegerField txtTamanhoPopulacao;
    private IntegerField txtQuantidadeGeracoes;
    private IntegerField txtChanceOcorrenciaMutacao;
    private IntegerField txtChanceOcorrenciaCrossover;
    private ComboBox<Rota> cbxRotas;
    private final Binder<ParametrosAlgoritmo> binder = new Binder<>(ParametrosAlgoritmo.class);

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

        txtTamanhoPopulacao = new IntegerField("Digite o tamanho da população");
        txtTamanhoPopulacao.setId("txt-tamanho-populacao");
        txtTamanhoPopulacao.setTooltipText("Mínimo de 20 e no máximo 500 populações");

        txtQuantidadeGeracoes = new IntegerField("Digite a quantidade de gerações");
        txtQuantidadeGeracoes.setId("txt-quantidade-geracoes");
        txtQuantidadeGeracoes.setTooltipText("Mínimo 10 e no máximo 1000 gerações");

        txtChanceOcorrenciaMutacao = new IntegerField("Digite a probabilidade de chance de ocorrência de mutação");
        txtChanceOcorrenciaMutacao.setId("txt-chance-ocorrencia-mutacao");
        txtChanceOcorrenciaMutacao.setTooltipText("Probabilidade entre 1 e 100%");

        txtChanceOcorrenciaCrossover = new IntegerField("Digite a probabilidade de chance de ocorrência de crossover");
        txtChanceOcorrenciaCrossover.setId("txt-chance-ocorrencia-crossover");
        txtChanceOcorrenciaCrossover.setTooltipText("Probabilidade entre 1 e 100%");

        formulario.add(txtTamanhoPopulacao, txtQuantidadeGeracoes, txtChanceOcorrenciaMutacao, txtChanceOcorrenciaCrossover);

        configurarBinder();

        return formulario;
    }

    private void configurarBinder() {
        binder.forField(cbxRotas)
                .asRequired("Selecione uma rota")
                .bind("rota");

        binder.forField(txtTamanhoPopulacao)
                .asRequired("Informe o tamanho da população")
                .withValidator(valor -> valor != null && valor >= 20 && valor <= 500, "O tamanho deve estar entre 20 e 500")
                .bind("tamanhoPopulacao");

        binder.forField(txtQuantidadeGeracoes)
                .asRequired("Informe a quantidade de gerações")
                .withValidator(valor -> valor != null && valor >= 10 && valor <= 1000, "A quantidade deve estar entre 10 e 1000")
                .bind("quantidadeGeracoes");

        binder.forField(txtChanceOcorrenciaMutacao)
                .asRequired("Informe a chance de mutação")
                .withValidator(valor -> valor != null && valor >= 1 && valor <= 100, "A chance deve estar entre 1 e 100%")
                .bind("chanceMutacao");

        binder.forField(txtChanceOcorrenciaCrossover)
                .asRequired("Informe a chance de crossover")
                .withValidator(valor -> valor != null && valor >= 1 && valor <= 100, "A chance deve estar entre 1 e 100%")
                .bind("chanceCrossover");
    }

    private Button criarBotaoRoteirizacao() {
        var botaoRoteirizacao = new Button("Solicitar roteirização");
        botaoRoteirizacao.setId("btn-roteirizacao");
        botaoRoteirizacao.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        botaoRoteirizacao.addClickListener(clickBotao -> {
            var parametros = new ParametrosAlgoritmo();
            if (!binder.writeBeanIfValid(parametros)) {
                return;
            }

            solicitarRoteirizacao(parametros);
        });

        return botaoRoteirizacao;
    }

    private void solicitarRoteirizacao(ParametrosAlgoritmo parametros) {
        var idRota = cbxRotas.getValue().getId();

        algoritmoGeneticoService.solicitarRoteirizacao(idRota,
                parametros.getTamanhoPopulacao(),
                parametros.getQuantidadeGeracoes(),
                parametros.getChanceMutacao(),
                parametros.getChanceCrossover());

        Notification.show("Roteirização solicitada!", 4500, Notification.Position.MIDDLE);
    }
}

