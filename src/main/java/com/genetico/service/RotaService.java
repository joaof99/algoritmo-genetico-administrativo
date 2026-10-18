package com.genetico.service;

import com.genetico.model.Rota;
import com.genetico.repository.RotaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RotaService {
    private final RotaRepository rotaRepository;

    public RotaService(RotaRepository rotaRepository) {
        this.rotaRepository = rotaRepository;
    }

    public Rota salvar(Rota rota) {
        return rotaRepository.save(rota);
    }

    public List<Rota> buscarTodosComEnderecos() {
        return rotaRepository.buscarTodosComEnderecos();
    }

    public List<Rota> buscarPaginaComEnderecos(Pageable pagina) {
        var idsRotas = rotaRepository.buscarIdsMaisRecentes(pagina);

        if (idsRotas.isEmpty()) {
            return List.of();
        }

        return rotaRepository.buscarPorIdsComEnderecos(idsRotas);
    }
}
