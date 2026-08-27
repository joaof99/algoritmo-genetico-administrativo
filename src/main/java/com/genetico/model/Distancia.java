package com.genetico.model;

import jakarta.persistence.*;

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
}
