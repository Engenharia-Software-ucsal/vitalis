INSERT INTO leito (codigo, ala, status) VALUES
    ('UTI-03', 'UTI Adulto', 'EM_HIGIENIZACAO'),
    ('EMERG-01', 'Emergencia', 'LIVRE'),
    ('EMERG-02', 'Emergencia', 'LIVRE'),
    ('ENF-103', 'Clinica Medica', 'MANUTENCAO'),
    ('ISO-01', 'Isolamento', 'ISOLAMENTO');

INSERT INTO historico_status_leito (leito_id, status_novo, alterado_em)
SELECT id, status, now()
FROM leito
WHERE codigo IN ('UTI-03', 'EMERG-01', 'EMERG-02', 'ENF-103', 'ISO-01');
