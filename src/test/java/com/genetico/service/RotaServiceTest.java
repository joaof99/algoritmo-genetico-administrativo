package com.genetico.service;

import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotaServiceTest {
    @Mock
    private RotaRepository rotaRepository;

    @Test
    @DisplayName("Deve buscar a rota pelo id com seus endereços")
    void deveBuscarRotaPeloIdComSeusEnderecos() {
        var rota = new Rota();
        var rotaService = new RotaService(rotaRepository);

        when(rotaRepository.buscarPorIdComEnderecos(8))
                .thenReturn(Optional.of(rota));

        var rotaEncontrada = rotaService.buscarPorIdComEnderecos(8);

        assertEquals(Optional.of(rota), rotaEncontrada);
    }

    @Test
    @DisplayName("Deve buscar as rotas da página com seus endereços")
    void deveBuscarRotasDaPaginaComSeusEnderecos() {
        var pagina = PageRequest.of(0, 50);
        var rotas = List.of(new Rota(), new Rota());
        var rotaService = new RotaService(rotaRepository);

        when(rotaRepository.buscarIdsMaisRecentes(pagina))
                .thenReturn(List.of(9, 8));

        when(rotaRepository.buscarPorIdsComEnderecos(List.of(9, 8)))
                .thenReturn(rotas);

        var rotasDaPagina = rotaService.buscarPaginaComEnderecos(pagina);

        assertEquals(rotas, rotasDaPagina);
    }

    @Test
    @DisplayName("Não deve consultar os endereços quando a página não tiver rotas")
    void naoDeveConsultarEnderecosQuandoPaginaNaoTiverRotas() {
        var pagina = PageRequest.of(3, 50);
        var rotaService = new RotaService(rotaRepository);

        when(rotaRepository.buscarIdsMaisRecentes(pagina))
                .thenReturn(List.of());

        var rotasDaPagina = rotaService.buscarPaginaComEnderecos(pagina);

        assertTrue(rotasDaPagina.isEmpty());
        verify(rotaRepository, never()).buscarPorIdsComEnderecos(anyList());
    }
}
