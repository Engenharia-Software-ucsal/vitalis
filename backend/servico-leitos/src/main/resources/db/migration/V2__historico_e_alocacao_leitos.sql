CREATE TABLE alocacao_leito (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    leito_id            BIGINT NOT NULL REFERENCES leito(id),
    atendimento_id      BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL
        CHECK (status IN ('RESERVADO', 'OCUPADO', 'ENCERRADO', 'CANCELADO')),
    alocado_em          TIMESTAMPTZ NOT NULL DEFAULT now(),
    liberado_em         TIMESTAMPTZ,
    responsavel_id      BIGINT,
    CHECK (
        (status IN ('RESERVADO', 'OCUPADO') AND liberado_em IS NULL)
        OR (status IN ('ENCERRADO', 'CANCELADO') AND liberado_em IS NOT NULL)
    ),
    CHECK (liberado_em IS NULL OR liberado_em >= alocado_em)
);

CREATE UNIQUE INDEX uq_alocacao_leito_aberta
    ON alocacao_leito (leito_id)
    WHERE liberado_em IS NULL;

CREATE UNIQUE INDEX uq_atendimento_leito_aberto
    ON alocacao_leito (atendimento_id)
    WHERE liberado_em IS NULL;

CREATE INDEX idx_alocacao_leito_atendimento
    ON alocacao_leito (atendimento_id, alocado_em DESC);

CREATE TABLE historico_status_leito (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    leito_id            BIGINT NOT NULL REFERENCES leito(id),
    alocacao_id         BIGINT REFERENCES alocacao_leito(id),
    status_anterior     VARCHAR(20)
        CHECK (status_anterior IS NULL OR status_anterior IN
            ('LIVRE', 'RESERVADO', 'OCUPADO', 'EM_HIGIENIZACAO', 'MANUTENCAO', 'ISOLAMENTO')),
    status_novo         VARCHAR(20) NOT NULL
        CHECK (status_novo IN
            ('LIVRE', 'RESERVADO', 'OCUPADO', 'EM_HIGIENIZACAO', 'MANUTENCAO', 'ISOLAMENTO')),
    atendimento_id      BIGINT,
    responsavel_id      BIGINT,
    alterado_em         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_historico_status_leito_data
    ON historico_status_leito (leito_id, alterado_em DESC);

INSERT INTO historico_status_leito (leito_id, status_novo, alterado_em)
SELECT id, status, now()
FROM leito;
