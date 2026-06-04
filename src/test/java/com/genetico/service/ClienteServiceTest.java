package com.genetico.service;

import com.genetico.model.Cliente;
import com.genetico.repository.ClienteRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ClienteServiceTest {

    @Test
    public void deveChamarSalvarComSucesso() {
        var cliente = new Cliente();
        cliente.setDescricao("Descrição teste");
        cliente.setLongitude(Double.valueOf("-90.0"));
        cliente.setLatitude(Double.valueOf("-450.0"));

        var clienteRepository = mock(ClienteRepository.class);
        var clienteService = new ClienteService(clienteRepository);

        clienteService.salvar(cliente);
        verify(clienteRepository, times(1)).save(cliente);
    }
}