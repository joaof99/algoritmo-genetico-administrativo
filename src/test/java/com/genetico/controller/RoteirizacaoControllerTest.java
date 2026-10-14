package com.genetico.controller;

import com.genetico.dto.RoteirizacaoRequest;
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
    @DisplayName("Deve retornar status 201 ao receber o resultado da roteirização")
    void deveRetornarStatus201AoReceberResultadoDaRoteirizacao() throws Exception {
        mockMvc.perform(post("/roteirizacao/armazenar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id_rota": 7,
                                    "melhor_fitness_populacao_inicial": 1500.2,
                                    "melhor_fitness_populacao_final": 1234.5,
                                    "qtde_geracoes": 50,
                                    "tamanho_populacao": 100,
                                    "prob_crossover": 75,
                                    "prob_mutacao": 10
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve repassar ao serviço os dados recebidos do algoritmo")
    void deveRepassarAoServicoDadosRecebidosDoAlgoritmo() throws Exception {
        mockMvc.perform(post("/roteirizacao/armazenar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "id_rota": 7,
                            "melhor_fitness_populacao_inicial": 1500.2,
                            "melhor_fitness_populacao_final": 1234.5,
                            "qtde_geracoes": 50,
                            "tamanho_populacao": 100,
                            "prob_crossover": 75,
                            "prob_mutacao": 10
                        }
                        """));

        verify(roteirizacaoService).armazenar(new RoteirizacaoRequest(7, 1500.2, 1234.5, 50, 100, 75, 10));
    }

    @Test
    @DisplayName("Deve retornar status 400 quando faltar algum campo obrigatório")
    void deveRetornarStatus400QuandoFaltarCampoObrigatorio() throws Exception {
        mockMvc.perform(post("/roteirizacao/armazenar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id_rota": 7,
                                    "melhor_fitness_populacao_final": 1234.5,
                                    "qtde_geracoes": 50,
                                    "tamanho_populacao": 100,
                                    "prob_crossover": 75,
                                    "prob_mutacao": 10
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roteirizacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 400 quando o ID da rota não for informado")
    void deveRetornarStatus400QuandoIdDaRotaNaoForInformado() throws Exception {
        mockMvc.perform(post("/roteirizacao/armazenar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "melhor_fitness_populacao_inicial": 1500.2,
                                    "melhor_fitness_populacao_final": 1234.5,
                                    "qtde_geracoes": 50,
                                    "tamanho_populacao": 100,
                                    "prob_crossover": 75,
                                    "prob_mutacao": 10
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(roteirizacaoService);
    }
}
