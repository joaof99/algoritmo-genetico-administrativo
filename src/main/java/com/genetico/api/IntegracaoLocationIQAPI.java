package com.genetico.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genetico.model.CoordenadaGeografica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class IntegracaoLocationIQAPI {
    private final Logger log = LoggerFactory.getLogger(IntegracaoLocationIQAPI.class);
    private final HttpClient httpClient;

    @Value("${locationiq.api.key}")
    private String locationIQKey;

    public IntegracaoLocationIQAPI() {
        httpClient = HttpClient.newHttpClient();
    }

    public CoordenadaGeografica buscarCoordenadaGeografica(String endereco) {
        var uri = UriComponentsBuilder.fromUriString("https://us1.locationiq.com/v1/search")
                .queryParam("key", locationIQKey)
                .queryParam("q", endereco)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .encode()
                .build()
                .toUri();

        var response = executarRequisicaoAPI(uri);

        var json = parsearResposta(response);

        var primeiroItem = json.get(0);

        var latitude = primeiroItem.get("lat").asDouble();
        var longitude = primeiroItem.get("lon").asDouble();

        return new CoordenadaGeografica(latitude, longitude);
    }

    public CoordenadaGeografica buscarCoordenadaGeografica(String logradouro, Integer numero, String bairro, String uf, String cidade, String cep) {
        var uri = UriComponentsBuilder
                .fromUriString("https://us1.locationiq.com/v1/search")
                .queryParam("key", locationIQKey)
                .queryParam("street", logradouro + ", " + numero)
                .queryParam("neighbourhood", bairro)
                .queryParam("city", cidade)
                .queryParam("state", uf)
                .queryParam("country", "Brazil")
                .queryParam("postalcode", cep)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .encode()
                .build()
                .toUri();

        var response = executarRequisicaoAPI(uri);

        var json = parsearResposta(response);

        var primeiroItem = json.get(0);

        var latitude = primeiroItem.get("lat").asDouble();
        var longitude = primeiroItem.get("lon").asDouble();

        return new CoordenadaGeografica(latitude, longitude);
    }


    private HttpResponse<String> executarRequisicaoAPI(URI uri) {
        try {
            var request = HttpRequest.newBuilder().uri(uri).GET().build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                var mensagemErro = "Erro na API de geocodificação (LocationIQ). HTTP status: " + response.statusCode();
                log.error("{} - body: {}", mensagemErro, response.body());
                throw new IntegracaoLocationIQAPIException(mensagemErro);
            }

            return response;
        } catch (IOException e) {
            var mensagemErro = "Erro de comunicação ao consultar a API de geocodificação (LocationIQ)";
            log.error(mensagemErro);
            throw new IntegracaoLocationIQAPIException(mensagemErro, e);
        } catch (InterruptedException e) {
            var mensagemErro = "Requisição à API de geocodificação (LocationIQ) foi interrompida.";
            log.error(mensagemErro);
            Thread.currentThread().interrupt();
            throw new IntegracaoLocationIQAPIException(mensagemErro, e);
        }
    }

    private JsonNode parsearResposta(HttpResponse<String> response) {
        try {
            var json = new ObjectMapper().readTree(response.body());

            if (!json.isArray() || json.isEmpty()) {
                var mensagemErro = "API de geocodificação retornou resposta vazia para";
                log.error(mensagemErro);
                throw new IntegracaoLocationIQAPIException(mensagemErro);
            }

            return json;
        } catch (JsonProcessingException e) {
            var mensagemErro = "Resposta inválida da API de geocodificação (formato inesperado).";
            log.error(mensagemErro);
            throw new IntegracaoLocationIQAPIException("Resposta inválida da API de geocodificação (formato inesperado).", e);
        }
    }
}
