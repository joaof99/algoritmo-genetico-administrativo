package com.genetico.repository;

import com.genetico.model.Rota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RotaRepository extends JpaRepository<Rota, Integer> {
    @Query("""
    SELECT DISTINCT r
    FROM Rota r
    JOIN FETCH r.enderecos
""")
    List<Rota> buscarTodosComEnderecos();

    @Query("""
    SELECT r
    FROM Rota r
    JOIN FETCH r.enderecos
    WHERE r.id = :id
""")
    Optional<Rota> buscarPorIdComEnderecos(int id);
}
