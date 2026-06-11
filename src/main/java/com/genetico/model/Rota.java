package com.genetico.model;

import com.genetico.enums.StatusRota;
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
            name = "rotas_enderecos",
            joinColumns = @JoinColumn(name = "rota_id"),
            inverseJoinColumns = @JoinColumn(name = "endereco_id")
    )
    private final List<Endereco> enderecos;

    @Enumerated(EnumType.STRING)
    private StatusRota status = StatusRota.PENDENTE;

    public Rota(List<Endereco> enderecos) {
        this.enderecos = enderecos;
    }

    public List<Endereco> getEnderecos() {
        return enderecos;
    }
}
