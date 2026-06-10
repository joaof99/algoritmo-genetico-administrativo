package com.genetico.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "rotas")
public class Rota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToMany
    @JoinTable(
            name = "rotas_clientes",
            joinColumns = @JoinColumn(name = "rota_id"),
            inverseJoinColumns = @JoinColumn(name = "cliente_id")
    )
    private List<Cliente> clientes;

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }
}
