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

        return Objects.equals(origem.getId(), outra.origem.getId())
                && Objects.equals(destino.getId(), outra.destino.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(origem.getId(), destino.getId());
    }
}
