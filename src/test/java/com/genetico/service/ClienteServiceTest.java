package com.genetico.service;

import com.genetico.model.Cliente;
import com.genetico.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Test
    @DisplayName("Deve chamar salvar com sucesso")
    public void deveChamarSalvarComSucesso() {
        var cliente = new Cliente();
        var clienteService = new ClienteService(clienteRepository);

        clienteService.salvar(cliente);
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Deve executar com sucesso a rotina de buscar todos os clientes no banco de dados")
    public void deveExecutarComSucessoBuscarTodosOsClientes() {
        List<Cliente> clientes = new ArrayList<>();
        clientes.add(new Cliente());
        clientes.add(new Cliente());
        clientes.add(new Cliente());

        when(clienteRepository.findAll()).thenReturn(clientes);

        var clienteService = new ClienteService(clienteRepository);
        assertEquals(3, clienteService.buscarTodos().size());
        verify(clienteRepository, times(1)).findAll();
    }
}