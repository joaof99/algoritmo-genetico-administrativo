package com.genetico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity
@Table(name = "enderecos")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Logradouro não pode ser vazio")
    private String logradouro;

    private String numero;

    @NotNull(message = "Bairro é obrigatório")
    @NotEmpty(message = "Bairro não pode ser vazio")
    private String bairro;

    @NotNull(message = "Cidade é obrigatória")
    @NotEmpty(message = "Cidade não pode ser vazia")
    private String cidade;

    @NotNull(message = "Uf é obrigatório")
    @NotEmpty(message = "Uf não pode ser vazio")
    private String uf;

    private String complemento;

    @NotNull(message = "Cep é obrigatório")
    @Pattern(regexp = "^[0-9]{5}-[0-9]{3}$", message = "CEP deve estar no formato: 00000-000")
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

    @Deprecated
    public Endereco(String logradouro, Double latitude, Double longitude) {
        this.logradouro = logradouro;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Endereco(String logradouro, String numero, String complemento, String bairro, String cep, Double latitude, Double longitude) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cep = cep;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getId() {
        return id;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNumero() {
        return numero;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCep() {
        return cep;
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

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getUf() {
        return uf;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCidade() {
        return cidade;
    }
}