package com.genetico.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoLocationIQAPITest {
    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private IntegracaoLocationIQAPI integracaoLocationIQAPI;

    @Test
    @DisplayName("Deve retornar coordenada geográfica corretamente")
    void deveRetornarCoordenadaGeograficaCorretamente() throws IOException, InterruptedException {
        var response = Mockito.mock(HttpResponse.class);

        when(response.statusCode())
                .thenReturn(200);

        when(response.body()).thenReturn("""
            [{"lat": "-23.5505", "lon": "-46.6333"}]
            """);

        when(httpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(response);

        var coordenadaGeografica = integracaoLocationIQAPI.buscarCoordenadaGeografica(
                        "Av. Paulista",
                        "1000",
                        "Bela Vista",
                        "SP",
                        "São Paulo",
                        "01310-100"
                );

        assertEquals(-23.5505, coordenadaGeografica.latitude());
        assertEquals(-46.6333, coordenadaGeografica.longitude());
    }
}