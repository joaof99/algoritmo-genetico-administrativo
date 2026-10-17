package com.genetico.service;

import com.genetico.dto.ConclusaoRoteirizacaoRequest;
import com.genetico.dto.FalhaRoteirizacaoRequest;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RoteirizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoteirizacaoService {
    private final Logger log = LoggerFactory.getLogger(RoteirizacaoService.class);
    private final RoteirizacaoRepository roteirizacaoRepository;
    private final Clock clock;

    public RoteirizacaoService(RoteirizacaoRepository roteirizacaoRepository, Clock clock) {
        this.roteirizacaoRepository = roteirizacaoRepository;
        this.clock = clock;
    }

    public void concluir(int idRoteirizacao, ConclusaoRoteirizacaoRequest request) {
        log.info("Resultado da roteirização de ID {} recebido do algoritmo genético", idRoteirizacao);

        var roteirizacao = buscarRoteirizacao(idRoteirizacao);

        if (!melhorRotaContemCadaEnderecoDaRota(request.melhorRota(), roteirizacao.getRota())) {
            var mensagemErro = String.format("A melhor rota devolvida pelo algoritmo genético não corresponde aos endereços da rota: %s", request.melhorRota());

            roteirizacao.falhar(mensagemErro, Instant.now(clock));
            roteirizacaoRepository.save(roteirizacao);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemErro);
        }

        roteirizacao.concluir(
                request.melhorFitnessPopulacaoInicial(),
                request.melhorFitnessPopulacaoFinal(),
                request.melhorRota(),
                Instant.now(clock));

        roteirizacaoRepository.save(roteirizacao);

        log.info("Roteirização de ID {} da rota de ID {} concluída. Melhor fitness da população inicial: {}, melhor fitness da população final: {}",
                roteirizacao.getId(),
                roteirizacao.getRota().getId(),
                roteirizacao.getMelhorFitnessPopulacaoInicial(),
                roteirizacao.getMelhorFitnessPopulacaoFinal());
    }

    public void falhar(int idRoteirizacao, FalhaRoteirizacaoRequest request) {
        log.warn("Falha na roteirização de ID {} informada pelo algoritmo genético: {}", idRoteirizacao, request.mensagem());

        var roteirizacao = buscarRoteirizacao(idRoteirizacao);

        roteirizacao.falhar(request.mensagem(), Instant.now(clock));
        roteirizacaoRepository.save(roteirizacao);
    }

    private Roteirizacao buscarRoteirizacao(int idRoteirizacao) {
        return roteirizacaoRepository.buscarPorIdComEnderecosDaRota(idRoteirizacao)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Roteirização de ID %d não encontrada", idRoteirizacao)
                ));
    }

    private boolean melhorRotaContemCadaEnderecoDaRota(List<Integer> melhorRota, Rota rota) {
        var idsEnderecosRota = rota.getEnderecos()
                .stream()
                .map(Endereco::getId)
                .collect(Collectors.toSet());

        return melhorRota.size() == idsEnderecosRota.size() && idsEnderecosRota.equals(new HashSet<>(melhorRota));
    }
}
