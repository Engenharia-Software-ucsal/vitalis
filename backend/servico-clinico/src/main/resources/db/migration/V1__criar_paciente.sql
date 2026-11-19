CREATE TABLE paciente (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    cpf             VARCHAR(11)  NOT NULL UNIQUE,
    cns             VARCHAR(15),
    data_nascimento DATE         NOT NULL,
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
