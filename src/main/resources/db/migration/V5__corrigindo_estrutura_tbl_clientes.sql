
ALTER TABLE rotas_clientes
    DROP CONSTRAINT fk_cliente;

ALTER TABLE clientes RENAME TO enderecos;

ALTER TABLE rotas_clientes
    RENAME COLUMN cliente_id TO endereco_id;

ALTER TABLE rotas_clientes RENAME TO rotas_enderecos;

ALTER TABLE rotas_enderecos
    ADD CONSTRAINT fk_endereco
    FOREIGN KEY (endereco_id) REFERENCES enderecos(id)
    ON DELETE CASCADE;

ALTER TABLE enderecos RENAME COLUMN descricao TO logradouro;