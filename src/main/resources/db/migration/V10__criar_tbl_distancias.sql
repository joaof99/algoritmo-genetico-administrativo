CREATE TABLE IF NOT EXISTS distancias (
    origem_id BIGINT NOT NULL,
    destino_id BIGINT NOT NULL,
    distancia DOUBLE PRECISION NOT NULL,

    CONSTRAINT pk_distancias
        PRIMARY KEY (origem_id, destino_id),

    CONSTRAINT fk_distancias_origem
        FOREIGN KEY (origem_id)
        REFERENCES enderecos (id),

    CONSTRAINT fk_distancias_destino
        FOREIGN KEY (destino_id)
        REFERENCES enderecos (id)
);
