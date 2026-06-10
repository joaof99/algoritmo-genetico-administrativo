package com.genetico.service;

import com.genetico.model.Endereco;
import com.genetico.repository.EnderecoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Test
    @DisplayName("Deve chamar salvar com sucesso")
    public void deveChamarSalvarComSucesso() {
        var endereco = new Endereco();
        var enderecoService = new EnderecoService(enderecoRepository);

        enderecoService.salvar(endereco);
        verify(enderecoRepository, times(1)).save(endereco);
    }

    @Test
    @DisplayName("Deve executar com sucesso a rotina de buscar todos os endereços no banco de dados")
    public void deveExecutarComSucessoBuscarTodosOsEnderecos() {
        var enderecos = new ArrayList<Endereco>();
        enderecos.add(new Endereco());
        enderecos.add(new Endereco());
        enderecos.add(new Endereco());

        when(enderecoRepository.findAll()).thenReturn(enderecos);

        var enderecoService = new EnderecoService(enderecoRepository);
        assertEquals(3, enderecoService.buscarTodos().size());
        verify(enderecoRepository, times(1)).findAll();
    }
}