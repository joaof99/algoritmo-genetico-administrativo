package com.genetico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "enderecos")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Logradouro não pode ser vazio")
    private String logradouro;

    @NotNull(message = "Número é obrigatório")
    private Integer numero;

    @NotNull(message = "Bairro é obrigatório")
    @NotEmpty(message = "Bairro não pode ser vazio")
    private String bairro;

    private String complemento;

    @NotNull(message = "Cep é obrigatório")
    private String cep;

    @NotNull(message = "Latitude é obrigatória")
    @DecimalMin(value = "-90.0", message = "Latitude mínima deve ser -90.0")
    @DecimalMax(value = "90.0", message = "Latitude máxima deve ser 90.0")
    private Double latitude;

    @NotNull(message = "Longitude é obrigatória")
    @DecimalMin(value = "-180.0", message = "Longitude mínima deve ser -180.0")
    @DecimalMax(value = "180.0", message = "Longitude máxima deve ser 180.0")
    private Double longitude;

    @ManyToMany(mappedBy = "enderecos")
    private List<Rota> rotas;

    public Endereco() {

    }

    public Endereco(String logradouro, Double latitude, Double longitude) {
        this.logradouro = logradouro;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Endereco(String logradouro, int numero, String complemento, String bairro, String cep, Double latitude, Double longitude) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cep = cep;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
