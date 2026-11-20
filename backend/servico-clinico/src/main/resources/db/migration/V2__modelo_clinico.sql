CREATE TABLE usuario (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(160) NOT NULL,
    email       VARCHAR(254) NOT NULL UNIQUE,
    senha_hash  VARCHAR(255) NOT NULL,
    paciente_id BIGINT UNIQUE REFERENCES paciente(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
        CHECK (status IN ('ATIVO', 'INATIVO', 'BLOQUEADO')),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE paciente
    ADD CONSTRAINT chk_paciente_nome_nao_vazio CHECK (length(trim(nome)) > 0),
    ADD CONSTRAINT chk_paciente_cpf_digitos CHECK (cpf ~ '^[0-9]{11}$'),
    ADD CONSTRAINT chk_paciente_cns_digitos
        CHECK (cns IS NULL OR cns ~ '^[0-9]{15}$');

CREATE TABLE papel (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo      VARCHAR(40) NOT NULL UNIQUE,
    descricao   VARCHAR(160) NOT NULL
);

CREATE TABLE usuario_papel (
    usuario_id  BIGINT NOT NULL REFERENCES usuario(id),
    papel_id    BIGINT NOT NULL REFERENCES papel(id),
    PRIMARY KEY (usuario_id, papel_id)
);

INSERT INTO papel (codigo, descricao) VALUES
    ('ADMINISTRADOR', 'Administrador do sistema'),
    ('MEDICO', 'Profissional medico'),
    ('ENFERMEIRO', 'Profissional de enfermagem'),
    ('GESTOR_LEITOS', 'Gestor de leitos'),
    ('RECEPCIONISTA', 'Profissional de recepcao'),
    ('FARMACEUTICO', 'Profissional de farmacia'),
    ('PACIENTE', 'Acesso ao portal do paciente');

CREATE TABLE contato_emergencia (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id     BIGINT NOT NULL REFERENCES paciente(id),
    nome            VARCHAR(160) NOT NULL,
    parentesco      VARCHAR(60),
    telefone        VARCHAR(30) NOT NULL,
    principal       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_contato_emergencia_paciente
    ON contato_emergencia (paciente_id);

CREATE TABLE convenio (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome            VARCHAR(160) NOT NULL UNIQUE,
    registro_ans    VARCHAR(20),
    ativo           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE paciente_convenio (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id         BIGINT NOT NULL REFERENCES paciente(id),
    convenio_id         BIGINT NOT NULL REFERENCES convenio(id),
    numero_carteira     VARCHAR(80) NOT NULL,
    vigencia_inicio     DATE NOT NULL,
    vigencia_fim        DATE,
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (vigencia_fim IS NULL OR vigencia_fim >= vigencia_inicio),
    UNIQUE (paciente_id, convenio_id, numero_carteira)
);

CREATE INDEX idx_paciente_convenio_paciente
    ON paciente_convenio (paciente_id);

CREATE TABLE pacote_assistencia (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    convenio_id             BIGINT REFERENCES convenio(id),
    nome                    VARCHAR(160) NOT NULL,
    ativo                   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE versao_pacote_assistencia (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pacote_assistencia_id   BIGINT NOT NULL REFERENCES pacote_assistencia(id),
    numero_versao           INTEGER NOT NULL CHECK (numero_versao > 0),
    modalidade              VARCHAR(20) NOT NULL
        CHECK (modalidade IN ('CARTEIRA_MES', 'ITEM_TRATADO')),
    vigencia_inicio         DATE NOT NULL,
    vigencia_fim            DATE,
    custo_servico           NUMERIC(12, 2),
    percentual_taxa         NUMERIC(5, 2),
    limite_acionamentos_ano INTEGER,
    criado_por              BIGINT NOT NULL REFERENCES usuario(id),
    criado_em               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (vigencia_fim IS NULL OR vigencia_fim >= vigencia_inicio),
    CHECK (custo_servico IS NULL OR custo_servico >= 0),
    CHECK (percentual_taxa IS NULL OR percentual_taxa BETWEEN 0 AND 100),
    CHECK (limite_acionamentos_ano IS NULL OR limite_acionamentos_ano >= 0),
    CHECK (
        modalidade <> 'ITEM_TRATADO'
        OR (custo_servico IS NOT NULL AND percentual_taxa IS NOT NULL)
    ),
    UNIQUE (pacote_assistencia_id, numero_versao)
);

CREATE TABLE paciente_assistencia (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_convenio_id    BIGINT NOT NULL REFERENCES paciente_convenio(id),
    versao_pacote_id        BIGINT NOT NULL REFERENCES versao_pacote_assistencia(id),
    vigencia_inicio         DATE NOT NULL,
    vigencia_fim            DATE,
    CHECK (vigencia_fim IS NULL OR vigencia_fim >= vigencia_inicio)
);

CREATE INDEX idx_paciente_assistencia_convenio
    ON paciente_assistencia (paciente_convenio_id);

CREATE INDEX idx_paciente_assistencia_versao
    ON paciente_assistencia (versao_pacote_id);

CREATE TABLE agendamento (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id         BIGINT NOT NULL REFERENCES paciente(id),
    profissional_id     BIGINT NOT NULL REFERENCES usuario(id),
    inicio              TIMESTAMPTZ NOT NULL,
    fim                 TIMESTAMPTZ NOT NULL,
    tipo                VARCHAR(30) NOT NULL
        CHECK (tipo IN ('CONSULTA', 'PROCEDIMENTO')),
    status              VARCHAR(20) NOT NULL DEFAULT 'AGENDADO'
        CHECK (status IN ('AGENDADO', 'CONFIRMADO', 'CANCELADO', 'REALIZADO', 'FALTOU')),
    observacao          VARCHAR(1000),
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (fim > inicio)
);

CREATE INDEX idx_agendamento_profissional_inicio
    ON agendamento (profissional_id, inicio);

CREATE INDEX idx_agendamento_paciente_inicio
    ON agendamento (paciente_id, inicio);

CREATE TABLE atendimento (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id         BIGINT NOT NULL REFERENCES paciente(id),
    agendamento_id      BIGINT REFERENCES agendamento(id),
    responsavel_id      BIGINT NOT NULL REFERENCES usuario(id),
    tipo                VARCHAR(20) NOT NULL
        CHECK (tipo IN ('AMBULATORIAL', 'HOSPITALAR', 'EMERGENCIA')),
    status              VARCHAR(20) NOT NULL DEFAULT 'ABERTO'
        CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'ALTA', 'CANCELADO')),
    iniciado_em         TIMESTAMPTZ NOT NULL DEFAULT now(),
    encerrado_em        TIMESTAMPTZ,
    motivo_encerramento VARCHAR(500),
    CHECK (encerrado_em IS NULL OR encerrado_em >= iniciado_em),
    CHECK (
        (status IN ('ALTA', 'CANCELADO') AND encerrado_em IS NOT NULL)
        OR (status IN ('ABERTO', 'EM_ANDAMENTO') AND encerrado_em IS NULL)
    )
);

CREATE INDEX idx_atendimento_paciente_inicio
    ON atendimento (paciente_id, iniciado_em DESC);

CREATE TABLE triagem (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    atendimento_id      BIGINT NOT NULL REFERENCES atendimento(id),
    enfermeiro_id       BIGINT NOT NULL REFERENCES usuario(id),
    protocolo           VARCHAR(20) NOT NULL DEFAULT 'MANCHESTER'
        CHECK (protocolo IN ('MANCHESTER')),
    prioridade          VARCHAR(10) NOT NULL
        CHECK (prioridade IN ('VERMELHA', 'LARANJA', 'AMARELA', 'VERDE', 'AZUL')),
    queixa_principal    VARCHAR(2000) NOT NULL,
    temperatura_c      NUMERIC(4, 1) CHECK (temperatura_c BETWEEN 25 AND 45),
    frequencia_cardiaca INTEGER CHECK (frequencia_cardiaca BETWEEN 0 AND 300),
    frequencia_respiratoria INTEGER CHECK (frequencia_respiratoria BETWEEN 0 AND 100),
    pressao_sistolica  INTEGER CHECK (pressao_sistolica BETWEEN 0 AND 300),
    pressao_diastolica INTEGER CHECK (pressao_diastolica BETWEEN 0 AND 200),
    saturacao_o2       NUMERIC(5, 2) CHECK (saturacao_o2 BETWEEN 0 AND 100),
    escala_dor         INTEGER CHECK (escala_dor BETWEEN 0 AND 10),
    observacoes        VARCHAR(4000),
    realizada_em       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_triagem_atendimento_data
    ON triagem (atendimento_id, realizada_em DESC);

CREATE TABLE prontuario (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id     BIGINT NOT NULL UNIQUE REFERENCES paciente(id),
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE evolucao_clinica (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    prontuario_id       BIGINT NOT NULL REFERENCES prontuario(id),
    atendimento_id      BIGINT REFERENCES atendimento(id),
    profissional_id     BIGINT NOT NULL REFERENCES usuario(id),
    texto               VARCHAR(8000) NOT NULL,
    criada_em           TIMESTAMPTZ NOT NULL DEFAULT now(),
    assinatura_ref      VARCHAR(500),
    assinada_em         TIMESTAMPTZ,
    CHECK (length(trim(texto)) > 0),
    CHECK (assinada_em IS NULL OR assinatura_ref IS NOT NULL)
);

CREATE INDEX idx_evolucao_prontuario_criada
    ON evolucao_clinica (prontuario_id, criada_em DESC);

CREATE TABLE alergia (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id         BIGINT NOT NULL REFERENCES paciente(id),
    substancia          VARCHAR(160) NOT NULL,
    reacao              VARCHAR(1000),
    gravidade           VARCHAR(20) NOT NULL
        CHECK (gravidade IN ('LEVE', 'MODERADA', 'GRAVE', 'NAO_INFORMADA')),
    ativa               BOOLEAN NOT NULL DEFAULT TRUE,
    registrada_por      BIGINT NOT NULL REFERENCES usuario(id),
    registrada_em       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_alergia_paciente_ativa
    ON alergia (paciente_id, ativa);

CREATE TABLE medicamento (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome                VARCHAR(200) NOT NULL,
    principio_ativo     VARCHAR(200),
    unidade             VARCHAR(40) NOT NULL,
    ativo               BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE estoque_medicamento (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    medicamento_id      BIGINT NOT NULL REFERENCES medicamento(id),
    local_estoque       VARCHAR(120) NOT NULL,
    quantidade          NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    atualizado_em       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (medicamento_id, local_estoque)
);

CREATE TABLE movimentacao_estoque (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    estoque_medicamento_id  BIGINT NOT NULL REFERENCES estoque_medicamento(id),
    tipo                    VARCHAR(10) NOT NULL
        CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE')),
    quantidade              NUMERIC(14, 3) NOT NULL CHECK (quantidade > 0),
    referencia              VARCHAR(160),
    responsavel_id          BIGINT NOT NULL REFERENCES usuario(id),
    criada_em               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE prescricao (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    atendimento_id          BIGINT NOT NULL REFERENCES atendimento(id),
    prescritor_id           BIGINT NOT NULL REFERENCES usuario(id),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ATIVA'
        CHECK (status IN ('ATIVA', 'CANCELADA', 'CONCLUIDA')),
    alergias_reconhecidas   BOOLEAN NOT NULL DEFAULT FALSE,
    criada_em               TIMESTAMPTZ NOT NULL DEFAULT now(),
    assinada_em             TIMESTAMPTZ,
    assinatura_ref          VARCHAR(500),
    CHECK (assinada_em IS NULL OR assinatura_ref IS NOT NULL)
);

CREATE TABLE item_prescricao (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    prescricao_id       BIGINT NOT NULL REFERENCES prescricao(id),
    medicamento_id      BIGINT NOT NULL REFERENCES medicamento(id),
    dose                VARCHAR(120) NOT NULL,
    via                 VARCHAR(60) NOT NULL,
    frequencia          VARCHAR(120) NOT NULL,
    duracao             VARCHAR(120),
    quantidade          NUMERIC(12, 3) NOT NULL CHECK (quantidade > 0),
    instrucoes          VARCHAR(1000)
);

CREATE TABLE dispensacao (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_prescricao_id      BIGINT NOT NULL REFERENCES item_prescricao(id),
    estoque_medicamento_id  BIGINT NOT NULL REFERENCES estoque_medicamento(id),
    farmaceutico_id         BIGINT NOT NULL REFERENCES usuario(id),
    quantidade              NUMERIC(12, 3) NOT NULL CHECK (quantidade > 0),
    dispensada_em           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE pedido_exame (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    atendimento_id      BIGINT NOT NULL REFERENCES atendimento(id),
    solicitante_id      BIGINT NOT NULL REFERENCES usuario(id),
    tipo                VARCHAR(20) NOT NULL
        CHECK (tipo IN ('LABORATORIAL', 'IMAGEM')),
    descricao           VARCHAR(500) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'SOLICITADO'
        CHECK (status IN ('SOLICITADO', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO')),
    solicitada_em       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE resultado_exame (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_exame_id     BIGINT NOT NULL REFERENCES pedido_exame(id),
    laudo               VARCHAR(8000),
    arquivo_uri         VARCHAR(1000),
    nome_arquivo        VARCHAR(255),
    tipo_mime           VARCHAR(120),
    enviado_por         BIGINT NOT NULL REFERENCES usuario(id),
    liberado_em         TIMESTAMPTZ,
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (laudo IS NOT NULL OR arquivo_uri IS NOT NULL)
);

CREATE INDEX idx_resultado_exame_pedido
    ON resultado_exame (pedido_exame_id, criado_em DESC);

CREATE TABLE lancamento_faturamento (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    atendimento_id          BIGINT NOT NULL REFERENCES atendimento(id),
    paciente_assistencia_id BIGINT REFERENCES paciente_assistencia(id),
    descricao               VARCHAR(300) NOT NULL,
    tipo                    VARCHAR(20) NOT NULL
        CHECK (tipo IN ('CARTEIRA_MES', 'ITEM_TRATADO')),
    valor                   NUMERIC(12, 2) NOT NULL CHECK (valor >= 0),
    taxa_servico_percentual NUMERIC(5, 2) NOT NULL DEFAULT 0
        CHECK (taxa_servico_percentual BETWEEN 0 AND 100),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
        CHECK (status IN ('PENDENTE', 'FATURADO', 'GLOSADO', 'CANCELADO')),
    criado_em               TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE auditoria_acesso (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ator_id             BIGINT REFERENCES usuario(id),
    acao                VARCHAR(20) NOT NULL
        CHECK (acao IN ('LEITURA', 'CRIACAO', 'ALTERACAO', 'EXCLUSAO', 'AUTORIZACAO')),
    recurso             VARCHAR(120) NOT NULL,
    recurso_id          VARCHAR(120),
    justificativa       VARCHAR(1000),
    metadados           JSONB NOT NULL DEFAULT '{}'::jsonb,
    ocorrido_em         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_acesso_recurso_data
    ON auditoria_acesso (recurso, recurso_id, ocorrido_em DESC);

CREATE FUNCTION impedir_alteracao_append_only()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'Este registro e somente de inclusao';
END;
$$;

CREATE TRIGGER trg_auditoria_acesso_imutavel
    BEFORE UPDATE OR DELETE ON auditoria_acesso
    FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_append_only();

CREATE TRIGGER trg_auditoria_acesso_sem_truncate
    BEFORE TRUNCATE ON auditoria_acesso
    FOR EACH STATEMENT EXECUTE FUNCTION impedir_alteracao_append_only();

CREATE TRIGGER trg_versao_pacote_assistencia_imutavel
    BEFORE UPDATE OR DELETE ON versao_pacote_assistencia
    FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_append_only();

CREATE TRIGGER trg_versao_pacote_assistencia_sem_truncate
    BEFORE TRUNCATE ON versao_pacote_assistencia
    FOR EACH STATEMENT EXECUTE FUNCTION impedir_alteracao_append_only();
