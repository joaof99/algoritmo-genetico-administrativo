CREATE TABLE roteirizacoes (
    id SERIAL PRIMARY KEY,
    rota_id INTEGER NOT NULL REFERENCES rotas (id),
    status VARCHAR(20) NOT NULL,
    data_inicio TIMESTAMPTZ NOT NULL,
    data_fim TIMESTAMPTZ,
    quantidade_geracoes INTEGER NOT NULL,
    tamanho_populacao INTEGER NOT NULL,
    chance_ocorrencia_crossover INTEGER NOT NULL,
    chance_ocorrencia_mutacao INTEGER NOT NULL,
    melhor_fitness_populacao_inicial DOUBLE PRECISION,
    melhor_fitness_populacao_final DOUBLE PRECISION,
    melhor_rota INTEGER[],
    mensagem_erro VARCHAR(500)
);
