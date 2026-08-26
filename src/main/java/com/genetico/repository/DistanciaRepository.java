package com.genetico.repository;

import com.genetico.model.Distancia;
import com.genetico.model.DistanciaId;
import com.genetico.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistanciaRepository extends JpaRepository<Distancia, DistanciaId> {

}
