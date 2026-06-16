package com.genetico.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genetico.model.CoordenadaGeografica;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class IntegracaoLocationIQAPI {
    public CoordenadaGeografica buscarCoordenadaGeografica(String logradouro) throws IOException, InterruptedException {
        var apiKey = System.getenv("IQ_API");

        var uri = UriComponentsBuilder
                .fromUriString("https://us1.locationiq.com/v1/search")
                .queryParam("key", apiKey)
                .queryParam("q", logradouro)
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .encode()
                .build()
                .toUri();

        var request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        var response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        var json = new ObjectMapper().readTree(response.body());

        var primeiroItem = json.get(0);

        var latitude = primeiroItem.get("lat").asDouble();
        var longitude = primeiroItem.get("lon").asDouble();

        return new CoordenadaGeografica(latitude, longitude);
    }
}
