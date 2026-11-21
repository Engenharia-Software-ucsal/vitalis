CREATE TABLE documento_clinico_rag (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo          VARCHAR(240) NOT NULL,
    fonte_oficial   VARCHAR(240) NOT NULL,
    medicamento_ref VARCHAR(160) NOT NULL,
    secao           VARCHAR(80)  NOT NULL,
    conteudo        TEXT         NOT NULL,
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_doc_rag_medicamento ON documento_clinico_rag (lower(medicamento_ref));

CREATE TABLE interacao_medicamentosa_local (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    farmaco_a       VARCHAR(160) NOT NULL,
    farmaco_b       VARCHAR(160) NOT NULL,
    categoria       VARCHAR(10)  NOT NULL CHECK (categoria IN ('X', 'D', 'C')),
    descricao       TEXT         NOT NULL,
    referencia      VARCHAR(240) NOT NULL
);

CREATE TABLE log_inferencia_ia (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    atendimento_id          BIGINT       NOT NULL,
    usuario_id              BIGINT       NOT NULL,
    modulo_ia               VARCHAR(40)  NOT NULL
        CHECK (modulo_ia IN ('COPILOTO_PRESCRICAO', 'SUMARIO_ALTA', 'SEPSE_ANALISE')),
    provedor_ia             VARCHAR(60)  NOT NULL,
    modelo_ia_versao        VARCHAR(120) NOT NULL,
    prompt_sanitizado_hash  CHAR(64)     NOT NULL,
    tokens_prompt           INTEGER      NOT NULL DEFAULT 0,
    tokens_resposta         INTEGER      NOT NULL DEFAULT 0,
    latencia_ms             INTEGER      NOT NULL DEFAULT 0,
    score_confianca         NUMERIC(4,3) CHECK (score_confianca IS NULL OR (score_confianca >= 0.0 AND score_confianca <= 1.0)),
    modo_contingencia_ativo BOOLEAN      NOT NULL DEFAULT FALSE,
    resposta_ia             JSONB        NOT NULL,
    criado_em               TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_log_ia_atendimento ON log_inferencia_ia (atendimento_id);
CREATE INDEX idx_log_ia_modulo_criacao ON log_inferencia_ia (modulo_ia, criado_em DESC);

CREATE TABLE decisao_humana_ia (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    log_inferencia_id   BIGINT      NOT NULL UNIQUE REFERENCES log_inferencia_ia(id),
    usuario_id          BIGINT      NOT NULL,
    decisao_medica      VARCHAR(20) NOT NULL
        CHECK (decisao_medica IN ('ACEITA', 'MODIFICADA', 'REJEITADA')),
    justificativa_humana TEXT,
    decidido_em         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (
        (decisao_medica IN ('MODIFICADA', 'REJEITADA')
            AND justificativa_humana IS NOT NULL
            AND length(trim(justificativa_humana)) >= 10)
        OR (decisao_medica = 'ACEITA')
    )
);

CREATE FUNCTION impedir_mutacao_worm_ia()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'Operacao negada: registros de auditoria de IA sao estritamente imutaveis (WORM).';
END;
$$;

CREATE TRIGGER trg_log_inferencia_ia_worm
    BEFORE UPDATE OR DELETE ON log_inferencia_ia
    FOR EACH ROW EXECUTE FUNCTION impedir_mutacao_worm_ia();

CREATE TRIGGER trg_log_inferencia_ia_sem_truncate
    BEFORE TRUNCATE ON log_inferencia_ia
    FOR EACH STATEMENT EXECUTE FUNCTION impedir_mutacao_worm_ia();

CREATE TRIGGER trg_decisao_humana_ia_worm
    BEFORE UPDATE OR DELETE ON decisao_humana_ia
    FOR EACH ROW EXECUTE FUNCTION impedir_mutacao_worm_ia();

CREATE TRIGGER trg_decisao_humana_ia_sem_truncate
    BEFORE TRUNCATE ON decisao_humana_ia
    FOR EACH STATEMENT EXECUTE FUNCTION impedir_mutacao_worm_ia();
