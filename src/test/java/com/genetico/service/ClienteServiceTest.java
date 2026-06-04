package com.genetico.service;

import com.genetico.model.Cliente;
import com.genetico.repository.ClienteRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ClienteServiceTest {

    @Test
    public void deveChamarSalvarComSucesso() {
        var cliente = new Cliente();
        var clienteRepository = mock(ClienteRepository.class);
        var clienteService = new ClienteService(clienteRepository);

        clienteService.salvar(cliente);
        verify(clienteRepository, times(1)).save(cliente);
    }
}