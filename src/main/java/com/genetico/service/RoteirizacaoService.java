package com.genetico.service;

import com.genetico.dto.ConclusaoRoteirizacaoRequest;
import com.genetico.dto.FalhaRoteirizacaoRequest;
import com.genetico.enums.StatusRoteirizacao;
import com.genetico.exception.RoteirizacaoFinalizadaException;
import com.genetico.model.Endereco;
import com.genetico.model.Rota;
import com.genetico.model.Roteirizacao;
import com.genetico.repository.RoteirizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoteirizacaoService {
    private final Logger log = LoggerFactory.getLogger(RoteirizacaoService.class);
    private final RoteirizacaoRepository roteirizacaoRepository;
    private final Clock clock;
    private final Duration tempoLimiteExecucao;

    public RoteirizacaoService(RoteirizacaoRepository roteirizacaoRepository, Clock clock, @Value("${roteirizacao.tempo-limite-execucao}") Duration tempoLimiteExecucao) {
        this.roteirizacaoRepository = roteirizacaoRepository;
        this.clock = clock;
        this.tempoLimiteExecucao = tempoLimiteExecucao;
    }

    public void concluir(int idRoteirizacao, ConclusaoRoteirizacaoRequest request) {
        log.info("Resultado da roteirização de ID {} recebido do algoritmo genético", idRoteirizacao);

        var roteirizacao = buscarRoteirizacao(idRoteirizacao);

        if (!melhorRotaContemCadaEnderecoDaRota(request.melhorRota(), roteirizacao.getRota())) {
            throw recusarConclusao(roteirizacao, String.format("A melhor rota devolvida pelo algoritmo genético não corresponde aos endereços da rota: %s", request.melhorRota()));
        }

        var quantidadePopulacoes = roteirizacao.getQuantidadeGeracoes() + 1;

        if (request.melhoresFitnessPorGeracao().size() != quantidadePopulacoes) {
            throw recusarConclusao(roteirizacao, String.format("O algoritmo genético devolveu %d valores de melhor fitness por geração, mas eram esperados %d (população inicial e %d gerações)",
                    request.melhoresFitnessPorGeracao().size(),
                    quantidadePopulacoes,
                    roteirizacao.getQuantidadeGeracoes()));
        }

        roteirizacao.concluir(
                request.melhorFitnessPopulacaoInicial(),
                request.melhorFitnessPopulacaoFinal(),
                request.melhorRota(),
                request.melhoresFitnessPorGeracao(),
                Instant.now(clock));

        salvarFinalizacao(roteirizacao);

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
        salvarFinalizacao(roteirizacao);
    }

    @Scheduled(fixedDelayString = "${roteirizacao.intervalo-verificacao-expiracao}")
    public void expirarRoteirizacoesSemResposta() {
        var agora = Instant.now(clock);
        var roteirizacoesSemResposta = roteirizacaoRepository.buscarPorStatusIniciadasAntesDe(StatusRoteirizacao.EM_ANDAMENTO, agora.minus(tempoLimiteExecucao));

        for (var roteirizacao : roteirizacoesSemResposta) {
            roteirizacao.falhar("Tempo limite de execução excedido sem resposta do algoritmo genético", agora);

            try {
                salvarFinalizacao(roteirizacao);
                log.warn("Roteirização de ID {} marcada como falha por exceder o tempo limite sem resposta do algoritmo genético", roteirizacao.getId());
            } catch (RoteirizacaoFinalizadaException e) {
                log.info("Roteirização de ID {} foi finalizada por outra requisição durante a expiração", roteirizacao.getId());
            }
        }
    }

    public List<Roteirizacao> buscarHistorico(Pageable pagina) {
        return roteirizacaoRepository.buscarMaisRecentes(pagina);
    }

    private ResponseStatusException recusarConclusao(Roteirizacao roteirizacao, String mensagemErro) {
        roteirizacao.falhar(mensagemErro, Instant.now(clock));
        salvarFinalizacao(roteirizacao);

        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemErro);
    }

    private void salvarFinalizacao(Roteirizacao roteirizacao) {
        try {
            roteirizacaoRepository.save(roteirizacao);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RoteirizacaoFinalizadaException(roteirizacao.getId());
        }
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
