package com.genetico.service;

import com.genetico.dto.RoteirizacaoRequest;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RoteirizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoteirizacaoService {
    private final Logger log = LoggerFactory.getLogger(RoteirizacaoService.class);
    private final RoteirizacaoRepository roteirizacaoRepository;

    public RoteirizacaoService(RoteirizacaoRepository roteirizacaoRepository) {
        this.roteirizacaoRepository = roteirizacaoRepository;
    }

    public void armazenar(RoteirizacaoRequest request) {
        log.info("Resultado de roteirização recebido do algoritmo genético. Armazenando na base de dados...");

        var roteirizacao = roteirizacaoRepository.save(new Roteirizacao(
                request.melhorFitnessPopulacaoInicial(),
                request.melhorFitnessPopulacaoFinal(),
                request.quantidadeGeracoes(),
                request.tamanhoPopulacao(),
                request.chanceOcorrenciaCrossover(),
                request.chanceOcorrenciaMutacao()
        ));

        log.info("Roteirização de ID {} armazenada com sucesso. Melhor fitness da população inicial: {}, melhor fitness da população final: {}",
                roteirizacao.getId(),
                roteirizacao.getMelhorFitnessPopulacaoInicial(),
                roteirizacao.getMelhorFitnessPopulacaoFinal());
    }
}
