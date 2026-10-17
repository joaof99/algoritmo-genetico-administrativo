package com.genetico.controller;

import com.genetico.dto.ConclusaoRoteirizacaoRequest;
import com.genetico.dto.FalhaRoteirizacaoRequest;
import com.genetico.exception.RoteirizacaoFinalizadaException;
import com.genetico.service.RoteirizacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoteirizacaoControllerTest {
    @Mock
    private RoteirizacaoService roteirizacaoService;

    @InjectMocks
    private RoteirizacaoController roteirizacaoController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(roteirizacaoController).build();
    }

    @Test
    @DisplayName("Deve retornar status 204 ao receber a conclusão da roteirização")
    void deveRetornarStatus204AoReceberConclusaoDaRoteirizacao() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "melhor_fitness_populacao_inicial": 1500.2,
                                    "melhor_fitness_populacao_final": 1234.5,
                                    "melhor_rota": [30, 10, 20]
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve repassar ao serviço a conclusão recebida do algoritmo")
    void deveRepassarAoServicoConclusaoRecebidaDoAlgoritmo() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/concluir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "melhor_fitness_populacao_inicial": 1500.2,
                            "melhor_fitness_populacao_final": 1234.5,
                            "melhor_rota": [30, 10, 20]
                        }
                        """));

        verify(roteirizacaoService).concluir(7, new ConclusaoRoteirizacaoRequest(1500.2, 1234.5, List.of(30, 10, 20)));
    }

    @Test
    @DisplayName("Deve retornar status 400 quando a conclusão não informar a melhor rota")
    void deveRetornarStatus400QuandoConclusaoNaoInformarMelhorRota() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "melhor_fitness_populacao_inicial": 1500.2,
                                    "melhor_fitness_populacao_final": 1234.5
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roteirizacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 400 quando a conclusão não informar o fitness da população inicial")
    void deveRetornarStatus400QuandoConclusaoNaoInformarFitnessDaPopulacaoInicial() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "melhor_fitness_populacao_final": 1234.5,
                                    "melhor_rota": [30, 10, 20]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roteirizacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 400 quando a conclusão não informar o fitness da população final")
    void deveRetornarStatus400QuandoConclusaoNaoInformarFitnessDaPopulacaoFinal() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "melhor_fitness_populacao_inicial": 1500.2,
                                    "melhor_rota": [30, 10, 20]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roteirizacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 409 quando a roteirização já estiver finalizada")
    void deveRetornarStatus409QuandoRoteirizacaoJaEstiverFinalizada() throws Exception {
        doThrow(new RoteirizacaoFinalizadaException(7))
                .when(roteirizacaoService)
                .concluir(eq(7), any(ConclusaoRoteirizacaoRequest.class));

        mockMvc.perform(post("/api/roteirizacao/7/concluir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "melhor_fitness_populacao_inicial": 1500.2,
                                    "melhor_fitness_populacao_final": 1234.5,
                                    "melhor_rota": [30, 10, 20]
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar status 204 ao receber a falha da roteirização")
    void deveRetornarStatus204AoReceberFalhaDaRoteirizacao() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/falhar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "mensagem": "Index 51 out of bounds for length 51" }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve repassar ao serviço a falha recebida do algoritmo")
    void deveRepassarAoServicoFalhaRecebidaDoAlgoritmo() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/falhar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "mensagem": "Index 51 out of bounds for length 51" }
                        """));

        verify(roteirizacaoService).falhar(7, new FalhaRoteirizacaoRequest("Index 51 out of bounds for length 51"));
    }

    @Test
    @DisplayName("Deve retornar status 400 quando a falha vier sem mensagem")
    void deveRetornarStatus400QuandoFalhaVierSemMensagem() throws Exception {
        mockMvc.perform(post("/api/roteirizacao/7/falhar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "mensagem": " " }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roteirizacaoService);
    }
}
