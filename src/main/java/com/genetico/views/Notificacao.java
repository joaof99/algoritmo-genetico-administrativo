package com.genetico.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

final class Notificacao {
    private static final int DURACAO_EM_MILISSEGUNDOS = 4500;

    private Notificacao() {
    }

    static void sucesso(String mensagem) {
        exibir(mensagem, NotificationVariant.LUMO_SUCCESS);
    }

    static void erro(String mensagem) {
        exibir(mensagem, NotificationVariant.LUMO_ERROR);
    }

    private static void exibir(String mensagem, NotificationVariant variante) {
        var notificacao = new Notification(mensagem, DURACAO_EM_MILISSEGUNDOS, Notification.Position.BOTTOM_END);
        notificacao.addThemeVariants(variante);
        notificacao.open();
    }
}
