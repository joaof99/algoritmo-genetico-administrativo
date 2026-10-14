package com.genetico.service;

import com.genetico.dto.RoteirizacaoRequest;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RotaRepository;
import com.genetico.repository.RoteirizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoteirizacaoService {
    private final Logger log = LoggerFactory.getLogger(RoteirizacaoService.class);
    private final RoteirizacaoRepository roteirizacaoRepository;
    private final RotaRepository rotaRepository;

    public RoteirizacaoService(RoteirizacaoRepository roteirizacaoRepository, RotaRepository rotaRepository) {
        this.roteirizacaoRepository = roteirizacaoRepository;
        this.rotaRepository = rotaRepository;
    }

    public void armazenar(RoteirizacaoRequest request) {
        log.info("Resultado de roteirização recebido do algoritmo genético. Armazenando na base de dados...");

        var rota = rotaRepository.findById(request.idRota())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Rota de ID %d não encontrada", request.idRota())
                ));

        var roteirizacao = roteirizacaoRepository.save(new Roteirizacao(
                rota,
                request.melhorFitnessPopulacaoInicial(),
                request.melhorFitnessPopulacaoFinal(),
                request.quantidadeGeracoes(),
                request.tamanhoPopulacao(),
                request.chanceOcorrenciaCrossover(),
                request.chanceOcorrenciaMutacao()
        ));

        log.info("Roteirização de ID {} da rota de ID {} armazenada com sucesso. Melhor fitness da população inicial: {}, melhor fitness da população final: {}",
                roteirizacao.getId(),
                rota.getId(),
                roteirizacao.getMelhorFitnessPopulacaoInicial(),
                roteirizacao.getMelhorFitnessPopulacaoFinal());
    }
}
