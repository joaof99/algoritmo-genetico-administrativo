package com.genetico.service;

import com.genetico.model.Cliente;
import com.genetico.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository){
        this.clienteRepository = clienteRepository;
    }

    public void salvar(Cliente cliente) {
        clienteRepository.save(cliente);
    }

    public List<Cliente> buscarTodos(){
        return clienteRepository.findAll();
    }
}
