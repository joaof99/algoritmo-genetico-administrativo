package com.genetico.views;

import java.util.Locale;

final class FormatoDistancia {
    private FormatoDistancia() {
    }

    static String formatar(Double distanciaEmKm) {
        return distanciaEmKm == null
                ? "—"
                : String.format(Locale.of("pt", "BR"), "%,.1f km", distanciaEmKm);
    }
}
