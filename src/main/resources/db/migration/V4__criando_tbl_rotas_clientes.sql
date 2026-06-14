CREATE TABLE rotas_clientes (
    rota_id SERIAL NOT NULL,
    cliente_id SERIAL NOT NULL,
    PRIMARY KEY (rota_id, cliente_id),
    CONSTRAINT fk_rota FOREIGN KEY (rota_id) REFERENCES rotas(id) ON DELETE CASCADE,
    CONSTRAINT fk_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);
