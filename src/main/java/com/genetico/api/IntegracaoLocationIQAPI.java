package com.genetico.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genetico.model.CoordenadaGeografica;
import com.genetico.model.Distancia;
import com.genetico.model.Endereco;
import org.jetbrains.annotations.Nullable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IntegracaoLocationIQAPI {
    private final Logger log = LoggerFactory.getLogger(IntegracaoLocationIQAPI.class);

    private final HttpClient httpClient;

    @Value("${locationiq.api.key}")
    private String locationIQKey;

    public IntegracaoLocationIQAPI(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CoordenadaGeografica buscarCoordenadaGeografica(String logradouro, @Nullable String numero, String bairro, String uf, String cidade, String cep) {
        var logradouroENumero = numero != null && !numero.isBlank()
                ? logradouro + ", " + numero
                : logradouro;

        var uri = UriComponentsBuilder
                .fromUriString("https://us1.locationiq.com/v1/search")
                .queryParam("key", locationIQKey)
                .queryParam("street", logradouroENumero)
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

        if (!json.isArray() || json.isEmpty()) {
            throw new IntegracaoLocationIQAPIException("API de geocodificação retornou resposta vazia para");
        }

        var primeiroItem = json.get(0);

        var latitude = primeiroItem.get("lat").asDouble();
        var longitude = primeiroItem.get("lon").asDouble();

        return new CoordenadaGeografica(latitude, longitude);
    }

    public double buscarDistanciaEntreCoordenadas(Endereco origem, Endereco destino) {
        var coordenadas = origem.getLongitude() + "," + origem.getLatitude()
                + ";" + destino.getLongitude() + "," + destino.getLatitude();

        log.info("Buscando distância entre coordenadas");
        log.info("Coordenadas origem: {}, {}", origem.getLatitude(), origem.getLongitude());
        log.info("Coordenadas destino: {}, {}", destino.getLatitude(), destino.getLongitude());

        var uri = UriComponentsBuilder
                .fromUriString("https://us1.locationiq.com/v1/directions/driving/" + coordenadas)
                .queryParam("key", locationIQKey)
                .queryParam("overview", "false")
                .queryParam("annotations", "false")
                .encode()
                .build()
                .toUri();

        var response = executarRequisicaoAPI(uri);
        var json = parsearResposta(response);

        if (!json.has("routes") || json.get("routes").isEmpty()) {
            throw new IntegracaoLocationIQAPIException("Directions API retornou resposta sem rotas");
        }

        var distanciaEmMetros = json.get("routes").get(0).get("distance").asDouble();
        var distanciaEmKm = distanciaEmMetros / 1000;

        return distanciaEmKm;
    }

    public List<Distancia> buscarDistanciaEnderecos(List<Endereco> enderecos) {
        if (enderecos.isEmpty()) {
            throw new IntegracaoLocationIQAPIException("É necessário informar pelo menos um endereço.");
        }

        var quantidadeEnderecos = enderecos.size();
        if (quantidadeEnderecos > 25) {
            throw new IntegracaoLocationIQAPIException(String.format("A API suporta no máximo 25 endereços simultâneos. Encontrado %d.", quantidadeEnderecos));
        }

        log.info("Mapeamento distâncias entre {} endereços.", quantidadeEnderecos);

        var coordenadas = enderecos.stream()
                .map(e -> new CoordenadaGeografica(e.getLatitude(), e.getLongitude()))
                .toList();

        var coordenadasFormatadas = coordenadas.stream()
                .map(c -> c.longitude() + "," + c.latitude())
                .collect(Collectors.joining(";"));

        var uri = UriComponentsBuilder
                .fromUriString("https://us1.locationiq.com/v1/matrix/driving/" + coordenadasFormatadas)
                .queryParam("key", locationIQKey)
                .queryParam("annotations", "distance")
                .encode()
                .build()
                .toUri();

        var response = executarRequisicaoAPI(uri);
        var json = parsearResposta(response);

        if (!json.has("distances") || json.get("distances").isEmpty()) {
            throw new IntegracaoLocationIQAPIException("Matrix API retornou resposta sem distâncias");
        }

        var distances = json.get("distances");
        var tamanho = enderecos.size();
        var distancias = new ArrayList<Distancia>();

        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                var origem = enderecos.get(i);
                var destino = enderecos.get(j);
                var distanciaEmKm = distances.get(i).get(j).asDouble() / 1000;
                distancias.add(new Distancia(origem, destino, distanciaEmKm));

            }
        }

        return distancias;
    }

    private HttpResponse<String> executarRequisicaoAPI(URI uri) {
        try {
            var request = HttpRequest.newBuilder().uri(uri).GET().build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IntegracaoLocationIQAPIException("Erro na API de geocodificação (LocationIQ). HTTP status: " + response.statusCode());
            }

            return response;
        } catch (IOException e) {
            throw new IntegracaoLocationIQAPIException("Erro de comunicação ao consultar a API de geocodificação (LocationIQ)", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegracaoLocationIQAPIException("Requisição à API de geocodificação (LocationIQ) foi interrompida.", e);
        }
    }

    private JsonNode parsearResposta(HttpResponse<String> response) {
        try {
            return new ObjectMapper().readTree(response.body());
        } catch (JsonProcessingException e) {
            throw new IntegracaoLocationIQAPIException("Resposta inválida da API de geocodificação (formato inesperado).", e);
        }
    }
}
