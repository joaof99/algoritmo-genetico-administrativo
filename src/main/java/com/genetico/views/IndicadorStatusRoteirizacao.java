package com.genetico.views;

import com.genetico.model.Roteirizacao;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.Tooltip;

final class IndicadorStatusRoteirizacao extends Span {

    IndicadorStatusRoteirizacao(Roteirizacao roteirizacao) {
        super(switch (roteirizacao.getStatus()) {
            case EM_ANDAMENTO -> "Em andamento";
            case CONCLUIDA -> "Concluída";
            case FALHA -> "Falha";
        });

        addClassNames("status", String.format("status-%s", roteirizacao.getStatus().name().toLowerCase()));

        if (roteirizacao.getMensagemErro() != null) {
            Tooltip.forComponent(this).setText(roteirizacao.getMensagemErro());
        }
    }
}
