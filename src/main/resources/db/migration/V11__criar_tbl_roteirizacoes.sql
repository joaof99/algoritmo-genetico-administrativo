CREATE TABLE roteirizacoes (
    id SERIAL PRIMARY KEY,
    rota_id INTEGER NOT NULL REFERENCES rotas (id),
    melhor_fitness_populacao_inicial DOUBLE PRECISION NOT NULL,
    melhor_fitness_populacao_final DOUBLE PRECISION NOT NULL,
    quantidade_geracoes INTEGER NOT NULL,
    tamanho_populacao INTEGER NOT NULL,
    chance_ocorrencia_crossover INTEGER NOT NULL,
    chance_ocorrencia_mutacao INTEGER NOT NULL
);
