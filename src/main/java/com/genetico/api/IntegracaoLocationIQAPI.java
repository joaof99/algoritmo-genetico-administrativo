package com.genetico.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genetico.model.CoordenadaGeografica;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class IntegracaoLocationIQAPI {
    public CoordenadaGeografica buscarCoordenadaGeografica(String logradouro) throws IOException, InterruptedException {
        var apiKey = System.getenv("IQ_API");

        var url = "https://us1.locationiq.com/v1/search"
                + "?key=" + apiKey
                + "&q=" + URLEncoder.encode(logradouro, StandardCharsets.UTF_8)
                + "&format=json"
                + "&limit=1";

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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
