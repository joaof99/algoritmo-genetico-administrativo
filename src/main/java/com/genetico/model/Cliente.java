package com.genetico.model;

import jakarta.validation.constraints.NotNull;

public class Cliente {
    @NotNull(message = "Descrição é obrigatório")
    private final String descricao;

    @NotNull(message = "Latitude é obrigatório")
    private final String latitude;

    @NotNull(message = "Longitude é obrigatório")
    private final String longitude;

    public Cliente(String descricao, String latitude, String longitude) {
        this.descricao = descricao;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDescricao() {
        return descricao;
    }
}
