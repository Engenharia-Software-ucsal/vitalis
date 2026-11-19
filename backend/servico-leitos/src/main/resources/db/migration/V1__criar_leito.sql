CREATE TABLE leito (
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(20)  NOT NULL UNIQUE,
    ala    VARCHAR(100) NOT NULL,
    status VARCHAR(20)  NOT NULL DEFAULT 'LIVRE'
        CHECK (status IN ('LIVRE', 'RESERVADO', 'OCUPADO', 'EM_HIGIENIZACAO', 'MANUTENCAO', 'ISOLAMENTO'))
);

INSERT INTO leito (codigo, ala) VALUES
    ('UTI-01', 'UTI Adulto'),
    ('UTI-02', 'UTI Adulto'),
    ('ENF-101', 'Clinica Medica'),
    ('ENF-102', 'Clinica Medica'),
    ('ENF-201', 'Cirurgica');
