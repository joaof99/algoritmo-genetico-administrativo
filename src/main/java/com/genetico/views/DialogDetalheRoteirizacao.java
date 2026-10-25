package com.genetico.views;

import com.genetico.model.DistanciaId;
import com.genetico.model.Endereco;
import com.genetico.model.Roteirizacao;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class DialogDetalheRoteirizacao extends Dialog {
    private record Parada(int ordem, Endereco endereco, String distanciaAteProxima) {
    }

    DialogDetalheRoteirizacao(Roteirizacao roteirizacao, List<Endereco> enderecosRota, Map<DistanciaId, Double> distanciasPorTrecho) {
        setHeaderTitle(String.format("Roteirização #%d · Rota %d", roteirizacao.getId(), roteirizacao.getRota().getId()));
        getHeader().add(new IndicadorStatusRoteirizacao(roteirizacao));
        setWidth("1100px");
        setMaxWidth("95vw");

        var conteudo = new VerticalLayout(
                criarResumo(roteirizacao),
                criarComparacaoDistancias(roteirizacao),
                criarParametrosAlgoritmo(roteirizacao),
                new H4("Ordem de visita da melhor rota"),
                criarOrdemVisita(roteirizacao, enderecosRota, distanciasPorTrecho));

        conteudo.setPadding(false);

        add(conteudo);
        getFooter().add(new Button("Fechar", evento -> close()));
    }

    private Div criarResumo(Roteirizacao roteirizacao) {
        var spanMelhoria = new Span(formatarMelhoria(roteirizacao));
        spanMelhoria.setId("resumo-melhoria");

        var spanParadas = new Span(
                String.format("%d paradas · economia de %s",
                        roteirizacao.getMelhorRota().size(),
                        FormatoDistancia.formatar(roteirizacao.getMelhorFitnessPopulacaoInicial() - roteirizacao.getMelhorFitnessPopulacaoFinal()))
        );

        spanParadas.setId("resumo-paradas");

        var resumo = new Div(spanMelhoria, spanParadas);
        resumo.addClassName("resumo-otimizacao");

        return resumo;
    }

    private String formatarMelhoria(Roteirizacao roteirizacao) {
        var melhorFitnessInicial = roteirizacao.getMelhorFitnessPopulacaoInicial();
        var percentualMelhoria = (melhorFitnessInicial - roteirizacao.getMelhorFitnessPopulacaoFinal()) / melhorFitnessInicial * 100;

        return String.format(Locale.of("pt", "BR"), "Distância %.1f%% menor", percentualMelhoria);
    }

    private Div criarComparacaoDistancias(Roteirizacao roteirizacao) {
        var rotuloDistanciaInicial = new Span(new Text("Melhor distância inicial "), VaadinIcon.INFO_CIRCLE_O.create());
        Tooltip.forComponent(rotuloDistanciaInicial).setText("Menor distância entre as rotas sorteadas no início do algoritmo");

        var seta = VaadinIcon.ARROW_RIGHT.create();
        seta.addClassName("seta-comparacao");

        var comparacao = new Div(
                criarMetrica("metrica-melhor-distancia-inicial", rotuloDistanciaInicial, FormatoDistancia.formatar(roteirizacao.getMelhorFitnessPopulacaoInicial())),
                seta,
                criarMetrica("metrica-melhor-distancia-final", new Span("Melhor distância final"), FormatoDistancia.formatar(roteirizacao.getMelhorFitnessPopulacaoFinal())));

        comparacao.addClassName("comparacao-distancias");

        return comparacao;
    }

    private Div criarMetrica(String id, Span rotulo, String valor) {
        rotulo.addClassName("metrica-rotulo");

        var spanValor = new Span(valor);
        spanValor.setId(id);
        spanValor.addClassName("metrica-valor");

        var metrica = new Div(rotulo, spanValor);
        metrica.addClassName("metrica");

        return metrica;
    }

    private Div criarParametrosAlgoritmo(Roteirizacao roteirizacao) {
        var duracao = Duration.between(roteirizacao.getDataInicio(), roteirizacao.getDataFim());

        var titulo = new Span(VaadinIcon.SLIDERS.create(), new Text("Parâmetros do algoritmo"));
        titulo.addClassName("parametros-titulo");

        var parametros = new Div(
                criarParametro("parametro-tamanho-populacao", VaadinIcon.USERS, "População",
                        String.valueOf(roteirizacao.getTamanhoPopulacao()),
                        String.format("%d rotas candidatas avaliadas a cada geração", roteirizacao.getTamanhoPopulacao())),
                criarParametro("parametro-quantidade-geracoes", VaadinIcon.REFRESH, "Gerações",
                        String.valueOf(roteirizacao.getQuantidadeGeracoes()),
                        String.format("As rotas foram evoluídas ao longo de %d gerações", roteirizacao.getQuantidadeGeracoes())),
                criarParametro("parametro-chance-ocorrencia-mutacao", VaadinIcon.RANDOM, "Mutação",
                        String.format("%d%%", roteirizacao.getChanceOcorrenciaMutacao()),
                        String.format("%d%% de chance de ocorrer mutação, alterando a ordem das paradas de uma rota", roteirizacao.getChanceOcorrenciaMutacao())),
                criarParametro("parametro-chance-ocorrencia-crossover", VaadinIcon.EXCHANGE, "Crossover",
                        String.format("%d%%", roteirizacao.getChanceOcorrenciaCrossover()),
                        String.format("%d%% de chance de ocorrer crossover, combinando duas rotas para gerar uma nova", roteirizacao.getChanceOcorrenciaCrossover())),
                criarParametro("duracao-execucao", VaadinIcon.CLOCK, "Duração",
                        String.format("%dm%02ds", duracao.toMinutes(), duracao.toSecondsPart()),
                        "Tempo que o algoritmo levou para concluir a roteirização")
        );

        parametros.addClassName("parametros-lista");

        var secao = new Div(titulo, parametros);
        secao.addClassName("parametros-algoritmo");

        return secao;
    }

    private Div criarParametro(String id, VaadinIcon icone, String rotulo, String valor, String dica) {
        var spanRotulo = new Span(icone.create(), new Text(rotulo));
        spanRotulo.addClassName("parametro-rotulo");

        var spanValor = new Span(valor);
        spanValor.addClassName("parametro-valor");

        var parametro = new Div(spanRotulo, spanValor);
        parametro.setId(id);
        parametro.addClassName("parametro");
        Tooltip.forComponent(parametro).setText(dica);

        return parametro;
    }

    private Grid<Parada> criarOrdemVisita(Roteirizacao roteirizacao, List<Endereco> enderecosRota, Map<DistanciaId, Double> distanciasPorTrecho) {
        var grid = new Grid<Parada>();
        grid.setId("grid-ordem-visita");
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setAllRowsVisible(true);

        grid.addComponentColumn(this::criarNumeroParada)
                .setKey("ordem")
                .setHeader("#")
                .setAutoWidth(true)
                .setFlexGrow(0);

        var quantidadeParadas = roteirizacao.getMelhorRota().size();

        var colunaLogradouro = grid.addComponentColumn(parada -> criarLogradouroParada(parada, quantidadeParadas))
                .setKey("logradouro")
                .setHeader("Logradouro");

        var colunaDistancia = grid.addColumn(Parada::distanciaAteProxima)
                .setKey("distanciaAteProxima")
                .setHeader("Até a próxima")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);

        var rodape = grid.appendFooterRow();
        rodape.getCell(colunaLogradouro).setText("Total");

        var distanciaTotal = distanciasPorTrecho.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        rodape.getCell(colunaDistancia).setText(FormatoDistancia.formatar(distanciaTotal));

        grid.setItems(criarParadas(roteirizacao.getMelhorRota(), enderecosRota, distanciasPorTrecho));

        return grid;
    }

    private Span criarNumeroParada(Parada parada) {
        var numero = new Span(String.valueOf(parada.ordem()));
        numero.addClassName("numero-parada");

        return numero;
    }

    private Span criarLogradouroParada(Parada parada, int quantidadeParadas) {
        var logradouro = new Span(parada.endereco().getLogradouro());

        if (parada.ordem() == 1) {
            logradouro.add(criarMarcadorParada("Início"));
        } else if (parada.ordem() == quantidadeParadas) {
            logradouro.add(criarMarcadorParada("Fim"));
        }

        return logradouro;
    }

    private Span criarMarcadorParada(String texto) {
        var marcador = new Span(texto);
        marcador.addClassName("marcador-parada");

        return marcador;
    }

    private List<Parada> criarParadas(List<Integer> melhorRota, List<Endereco> enderecosRota, Map<DistanciaId, Double> distanciasPorTrecho) {
        var enderecosPorId = enderecosRota.stream().collect(Collectors.toMap(Endereco::getId, Function.identity()));

        return IntStream.range(0, melhorRota.size())
                .mapToObj(indice -> new Parada(
                        indice + 1,
                        enderecosPorId.get(melhorRota.get(indice)),
                        formatarDistanciaAteProxima(melhorRota, indice, distanciasPorTrecho)))
                .toList();
    }

    private String formatarDistanciaAteProxima(List<Integer> melhorRota, int indice, Map<DistanciaId, Double> distanciasPorTrecho) {
        var ultimaParada = indice == melhorRota.size() - 1;

        if (ultimaParada) {
            return "—";
        }

        var idEnderecoParadaAtual = melhorRota.get(indice);
        var idEnderecoProximaParada = melhorRota.get(indice + 1);

        var distanciaIdTrecho = new DistanciaId(idEnderecoParadaAtual, idEnderecoProximaParada);
        var distancia = distanciasPorTrecho.get(distanciaIdTrecho);

        return FormatoDistancia.formatar(distancia);
    }
}
