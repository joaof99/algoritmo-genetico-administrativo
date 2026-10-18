package com.genetico.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "distancias")
@IdClass(DistanciaId.class)
public class Distancia {
    @Id
    @ManyToOne
    @JoinColumn(name = "origem_id")
    private Endereco origem;

    @Id
    @ManyToOne
    @JoinColumn(name = "destino_id")
    private Endereco destino;

    private double distancia;

    public Distancia() {

    }

    public Distancia(Endereco origem, Endereco destino, double distanciaEntreEnderecos) {
        this.origem = origem;
        this.destino = destino;
        this.distancia = distanciaEntreEnderecos;
    }

    public Endereco getOrigem() {
        return origem;
    }

    public Endereco getDestino() {
        return destino;
    }

    public double getDistancia() {
        return distancia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Distancia outra)) {
            return false;
        }

        var origemId = origem != null ? origem.getId() : null;
        var destinoId = destino != null ? destino.getId() : null;
        var outraOrigemId = outra.origem != null ? outra.origem.getId() : null;
        var outroDestinoId = outra.destino != null ? outra.destino.getId() : null;

        return origemId != null
                && destinoId != null
                && Objects.equals(origemId, outraOrigemId)
                && Objects.equals(destinoId, outroDestinoId);
    }

    @Override
    public int hashCode() {
        return Distancia.class.hashCode();
    }
}
