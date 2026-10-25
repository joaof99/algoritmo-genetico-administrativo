package com.genetico.views;

import com.genetico.enums.StatusRoteirizacao;
import com.genetico.exception.SolicitacaoRoteirizacaoException;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.service.AlgoritmoGeneticoService;
import com.genetico.service.DistanciaService;
import com.genetico.service.RotaService;
import com.genetico.service.RoteirizacaoService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.PageRequest;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Route(value = "solicitacao-roteirizacao", layout = MainLayout.class)
@PageTitle("Roteirizações")
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

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final RotaService rotaService;
    private final AlgoritmoGeneticoService algoritmoGeneticoService;
    private final RoteirizacaoService roteirizacaoService;
    private final DistanciaService distanciaService;
    private IntegerField txtTamanhoPopulacao;
    private IntegerField txtQuantidadeGeracoes;
    private IntegerField txtChanceOcorrenciaMutacao;
    private IntegerField txtChanceOcorrenciaCrossover;
    private ComboBox<Rota> cbxRotas;
    private Grid<Roteirizacao> gridHistorico;
    private final Binder<ParametrosAlgoritmo> binder = new Binder<>();

    public AlgoritmoGeneticoView(RotaService rotaService, AlgoritmoGeneticoService algoritmoGeneticoService, RoteirizacaoService roteirizacaoService,
                                 DistanciaService distanciaService) {
        this.rotaService = rotaService;
        this.algoritmoGeneticoService = algoritmoGeneticoService;
        this.roteirizacaoService = roteirizacaoService;
        this.distanciaService = distanciaService;
        add(criarSecaoCadastro(), criarSecaoHistorico());
    }

    private Component criarSecaoCadastro() {
        cbxRotas = criarCbx();
        var botaoRoteirizacao = criarBotaoRoteirizacao();

        var secao = new VerticalLayout(new H3("Solicitar roteirização"), criarFormulario(), botaoRoteirizacao);

        secao.addClassName("cartao");
        secao.setHorizontalComponentAlignment(Alignment.CENTER, botaoRoteirizacao);

        return secao;
    }

    private ComboBox<Rota> criarCbx() {
        var cbx = new ComboBox<Rota>("Rota");
        cbx.setId("cbx-rota");
        cbx.setItems(rotaService.buscarTodosComEnderecos());
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
        formulario.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2));

        txtTamanhoPopulacao = new IntegerField("Tamanho da população (20 a 500)");
        txtTamanhoPopulacao.setId("txt-tamanho-populacao");
        txtTamanhoPopulacao.setTooltipText("Mínimo de 20 e no máximo 500 populações");

        txtQuantidadeGeracoes = new IntegerField("Quantidade de gerações (10 a 1000)");
        txtQuantidadeGeracoes.setId("txt-quantidade-geracoes");
        txtQuantidadeGeracoes.setTooltipText("Mínimo 10 e no máximo 1000 gerações");

        txtChanceOcorrenciaMutacao = new IntegerField("Chance de mutação em % (1 a 100)");
        txtChanceOcorrenciaMutacao.setId("txt-chance-ocorrencia-mutacao");
        txtChanceOcorrenciaMutacao.setTooltipText("Probabilidade entre 1 e 100%");

        txtChanceOcorrenciaCrossover = new IntegerField("Chance de crossover em % (1 a 100)");
        txtChanceOcorrenciaCrossover.setId("txt-chance-ocorrencia-crossover");
        txtChanceOcorrenciaCrossover.setTooltipText("Probabilidade entre 1 e 100%");

        formulario.add(cbxRotas, 2);
        formulario.add(txtTamanhoPopulacao, txtQuantidadeGeracoes, txtChanceOcorrenciaMutacao, txtChanceOcorrenciaCrossover);

        configurarBinder();

        return formulario;
    }

    private void configurarBinder() {
        binder.forField(cbxRotas)
                .asRequired("Selecione uma rota")
                .bind(ParametrosAlgoritmo::getRota, ParametrosAlgoritmo::setRota);

        binder.forField(txtTamanhoPopulacao)
                .asRequired("Informe o tamanho da população")
                .withValidator(valor -> valor != null && valor >= 20 && valor <= 500, "O tamanho deve estar entre 20 e 500")
                .bind(ParametrosAlgoritmo::getTamanhoPopulacao, ParametrosAlgoritmo::setTamanhoPopulacao);

        binder.forField(txtQuantidadeGeracoes)
                .asRequired("Informe a quantidade de gerações")
                .withValidator(valor -> valor != null && valor >= 10 && valor <= 1000, "A quantidade deve estar entre 10 e 1000")
                .bind(ParametrosAlgoritmo::getQuantidadeGeracoes, ParametrosAlgoritmo::setQuantidadeGeracoes);

        binder.forField(txtChanceOcorrenciaMutacao)
                .asRequired("Informe a chance de mutação")
                .withValidator(valor -> valor != null && valor >= 1 && valor <= 100, "A chance deve estar entre 1 e 100%")
                .bind(ParametrosAlgoritmo::getChanceMutacao, ParametrosAlgoritmo::setChanceMutacao);

        binder.forField(txtChanceOcorrenciaCrossover)
                .asRequired("Informe a chance de crossover")
                .withValidator(valor -> valor != null && valor >= 1 && valor <= 100, "A chance deve estar entre 1 e 100%")
                .bind(ParametrosAlgoritmo::getChanceCrossover, ParametrosAlgoritmo::setChanceCrossover);
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

        try {
            algoritmoGeneticoService.solicitarRoteirizacao(idRota,
                    parametros.getTamanhoPopulacao(),
                    parametros.getQuantidadeGeracoes(),
                    parametros.getChanceMutacao(),
                    parametros.getChanceCrossover());

            Notificacao.sucesso("Roteirização solicitada!");
        } catch (SolicitacaoRoteirizacaoException e) {
            Notificacao.erro(e.getMessage());
        } finally {
            gridHistorico.getDataProvider().refreshAll();
        }
    }

    private Component criarSecaoHistorico() {
        gridHistorico = criarGridHistorico();

        var botaoAtualizar = new Button("Atualizar", VaadinIcon.REFRESH.create());
        botaoAtualizar.setId("btn-atualizar-historico");
        botaoAtualizar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        botaoAtualizar.addClickListener(clique -> gridHistorico.getDataProvider().refreshAll());

        var cabecalho = new HorizontalLayout(new H3("Histórico de roteirizações"), botaoAtualizar);
        cabecalho.setWidthFull();
        cabecalho.setAlignItems(FlexComponent.Alignment.CENTER);
        cabecalho.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        var secao = new VerticalLayout(cabecalho, gridHistorico);
        secao.addClassName("cartao");

        return secao;
    }

    private Grid<Roteirizacao> criarGridHistorico() {
        var grid = new Grid<Roteirizacao>();
        grid.setId("grid-historico-roteirizacoes");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(Roteirizacao::getId)
                .setHeader("#")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(roteirizacao -> String.format("Rota %d", roteirizacao.getRota().getId()))
                .setHeader("Rota");

        grid.addComponentColumn(IndicadorStatusRoteirizacao::new)
                .setKey("status")
                .setHeader("Status");

        grid.addColumn(roteirizacao -> FormatoDistancia.formatar(roteirizacao.getMelhorFitnessPopulacaoFinal()))
                .setHeader("Melhor fitness");

        grid.addColumn(roteirizacao -> FORMATO_DATA_HORA.format(roteirizacao.getDataInicio()))
                .setHeader("Início")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(this::criarBotaoDetalhe)
                .setKey("detalhe")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setFrozenToEnd(true);

        grid.setItems(consulta -> roteirizacaoService
                .buscarHistorico(PageRequest.of(consulta.getPage(), consulta.getPageSize()))
                .stream());

        grid.addItemDoubleClickListener(evento -> {
            if (evento.getItem().getStatus() == StatusRoteirizacao.CONCLUIDA) {
                abrirDialogDetalheRoteirizacao(evento.getItem());
            } else {
                Notificacao.informacao("Os detalhes da rota só são exibidos para roteirizações concluídas");
            }
        });

        return grid;
    }

    private Button criarBotaoDetalhe(Roteirizacao roteirizacao) {
        var botaoDetalhe = new Button(VaadinIcon.EYE.create(), clique -> abrirDialogDetalheRoteirizacao(roteirizacao));
        botaoDetalhe.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        botaoDetalhe.setTooltipText("Ver detalhes");
        botaoDetalhe.setAriaLabel("Ver detalhes");
        botaoDetalhe.setEnabled(roteirizacao.getStatus() == StatusRoteirizacao.CONCLUIDA);

        return botaoDetalhe;
    }

    private void abrirDialogDetalheRoteirizacao(Roteirizacao roteirizacao) {
        var idRota = roteirizacao.getRota().getId();

        rotaService.buscarPorIdComEnderecos(idRota).ifPresentOrElse(
                rota -> criarDialogDetalheRoteirizacao(roteirizacao, rota).open(),
                () -> Notificacao.erro(String.format("Rota %d não encontrada", idRota)));
    }

    private DialogDetalheRoteirizacao criarDialogDetalheRoteirizacao(Roteirizacao roteirizacao, Rota rota) {
        var distanciasPorTrecho = distanciaService.buscarDistanciasEntreParadasConsecutivas(roteirizacao.getMelhorRota());

        return new DialogDetalheRoteirizacao(roteirizacao, rota.getEnderecos(), distanciasPorTrecho);
    }
}
