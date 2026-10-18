package com.genetico.repository;

import com.genetico.enums.StatusRoteirizacao;
import com.genetico.model.Roteirizacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RoteirizacaoRepository extends JpaRepository<Roteirizacao, Integer> {
    @Query("""
    SELECT r
    FROM Roteirizacao r
    JOIN FETCH r.rota rota
    JOIN FETCH rota.enderecos
    WHERE r.id = :id
""")
    Optional<Roteirizacao> buscarPorIdComEnderecosDaRota(int id);

    @Query("""
    SELECT r
    FROM Roteirizacao r
    WHERE r.status = :status
    AND r.dataInicio < :dataLimite
""")
    List<Roteirizacao> buscarPorStatusIniciadasAntesDe(StatusRoteirizacao status, Instant dataLimite);

    @Query("""
    SELECT r
    FROM Roteirizacao r
    JOIN FETCH r.rota
    ORDER BY r.dataInicio DESC
""")
    List<Roteirizacao> buscarMaisRecentes(Pageable pagina);
}
