package com.genetico.model;

import java.io.Serializable;
import java.util.Objects;

public class DistanciaId implements Serializable {
    private Integer origem;
    private Integer destino;

    public DistanciaId() {}

    public DistanciaId(Integer origem, Integer destino) {
        this.origem = origem;
        this.destino = destino;
    }

    public Integer getOrigem() { return origem; }
    public Integer getDestino() { return destino; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DistanciaId)) return false;
        DistanciaId that = (DistanciaId) o;
        return Objects.equals(origem, that.origem)
                && Objects.equals(destino, that.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origem, destino);
    }
}
