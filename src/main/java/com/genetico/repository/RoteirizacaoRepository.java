package com.genetico.repository;

import com.genetico.model.Roteirizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
