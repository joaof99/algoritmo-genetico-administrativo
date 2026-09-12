package com.genetico.api;

import com.genetico.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    @DisplayName("Deve retornar corretamente as distâncias")
    void deveRetornarCorretamenteAsDistancias() throws IOException, InterruptedException {
        var response = Mockito.mock(HttpResponse.class);

        when(response.statusCode())
                .thenReturn(200);

        when(response.body()).thenReturn("""
                {
                    "distances": [
                        [0,     12500, 8300,  15700],
                        [12500, 0,     6200,  9400],
                        [8300,  6200,  0,     11200],
                        [15700, 9400,  11200, 0]
                    ]
                }
                """);

        when(httpClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(response);

        var enderecoA = new Endereco("Endereço A", "", "", "", "", -9.5, -8.3);
        var enderecoB = new Endereco("Endereço B", "", "", "", "", -10.5, -9.3);
        var enderecoC = new Endereco("Endereço C", "", "", "", "", -11.5, -10.3);
        var enderecoD = new Endereco("Endereço D", "", "", "", "", -12.5, -11.3);

        var distancias = integracaoLocationIQAPI.buscarDistanciaEnderecos(List.of(enderecoA, enderecoB, enderecoC, enderecoD));
        assertEquals(16, distancias.size());

        assertEquals(0.0, distancias.get(0).getDistancia(), 0.0001);
        assertEquals(12.5, distancias.get(1).getDistancia(), 0.0001);
        assertEquals(8.3, distancias.get(2).getDistancia(), 0.0001);
        assertEquals(15.7, distancias.get(3).getDistancia(), 0.0001);

        assertEquals(12.5, distancias.get(4).getDistancia(), 0.0001);
        assertEquals(0.0, distancias.get(5).getDistancia(), 0.0001);
        assertEquals(6.2, distancias.get(6).getDistancia(), 0.0001);
        assertEquals(9.4, distancias.get(7).getDistancia(), 0.0001);

        assertEquals(8.3, distancias.get(8).getDistancia(), 0.0001);
        assertEquals(6.2, distancias.get(9).getDistancia(), 0.0001);
        assertEquals(0.0, distancias.get(10).getDistancia(), 0.0001);
        assertEquals(11.2, distancias.get(11).getDistancia(), 0.0001);

        assertEquals(15.7, distancias.get(12).getDistancia(), 0.0001);
        assertEquals(9.4, distancias.get(13).getDistancia(), 0.0001);
        assertEquals(11.2, distancias.get(14).getDistancia(), 0.0001);
        assertEquals(0.0, distancias.get(15).getDistancia(), 0.0001);
    }

    @ParameterizedTest
    @MethodSource("cenariosInvalidos")
    @DisplayName("Deve ocorrer erro para quantidade inválida de endereços")
    void deveGerarErroParaQuantidadeInvalidaDeEnderecos(List<Endereco> enderecos, String mensagemEsperada) {
        var exception = assertThrows(
                IntegracaoLocationIQAPIException.class,
                () -> integracaoLocationIQAPI.buscarDistanciaEnderecos(enderecos)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
    }

    private static Stream<Arguments> cenariosInvalidos() {
        var enderecosAcimaDoLimite = IntStream.range(0, 26)
                .mapToObj(i -> new Endereco("", "", "", "", "", -9.5, -8.3))
                .toList();

        return Stream.of(
                Arguments.of(List.of(), "É necessário informar pelo menos um endereço."),
                Arguments.of(enderecosAcimaDoLimite, "A API suporta no máximo 25 endereços simultâneos. Encontrado 26.")
        );
    }
}