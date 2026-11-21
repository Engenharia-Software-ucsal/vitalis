-- Dados iniciais para demonstracao do MVP Vitalis
-- Senha padrao de todos os usuarios demo: Vitalis@2026

INSERT INTO usuario (nome, email, senha_hash, status) VALUES
    ('Administrador Vitalis', 'admin@vitalis.local', '$2b$10$q2gZksCOWCOLWF3W4qH04.N/cqQMAjuD9HxCJEyV8WbQlzrmz7ws2', 'ATIVO'),
    ('Carla Recepcionista', 'recepcao@vitalis.local', '$2b$10$q2gZksCOWCOLWF3W4qH04.N/cqQMAjuD9HxCJEyV8WbQlzrmz7ws2', 'ATIVO'),
    ('Enf. Bruno Triagem', 'enfermeiro@vitalis.local', '$2b$10$q2gZksCOWCOLWF3W4qH04.N/cqQMAjuD9HxCJEyV8WbQlzrmz7ws2', 'ATIVO'),
    ('Dra. Helena Medeiros', 'medico@vitalis.local', '$2b$10$q2gZksCOWCOLWF3W4qH04.N/cqQMAjuD9HxCJEyV8WbQlzrmz7ws2', 'ATIVO'),
    ('Marcos Gestor de Leitos', 'leitos@vitalis.local', '$2b$10$q2gZksCOWCOLWF3W4qH04.N/cqQMAjuD9HxCJEyV8WbQlzrmz7ws2', 'ATIVO');

INSERT INTO usuario_papel (usuario_id, papel_id)
SELECT u.id, p.id FROM usuario u JOIN papel p ON p.codigo = 'ADMINISTRADOR' WHERE u.email = 'admin@vitalis.local';

INSERT INTO usuario_papel (usuario_id, papel_id)
SELECT u.id, p.id FROM usuario u JOIN papel p ON p.codigo = 'RECEPCIONISTA' WHERE u.email = 'recepcao@vitalis.local';

INSERT INTO usuario_papel (usuario_id, papel_id)
SELECT u.id, p.id FROM usuario u JOIN papel p ON p.codigo = 'ENFERMEIRO' WHERE u.email = 'enfermeiro@vitalis.local';

INSERT INTO usuario_papel (usuario_id, papel_id)
SELECT u.id, p.id FROM usuario u JOIN papel p ON p.codigo = 'MEDICO' WHERE u.email = 'medico@vitalis.local';

INSERT INTO usuario_papel (usuario_id, papel_id)
SELECT u.id, p.id FROM usuario u JOIN papel p ON p.codigo = 'GESTOR_LEITOS' WHERE u.email = 'leitos@vitalis.local';

INSERT INTO medicamento (nome, principio_ativo, unidade, ativo) VALUES
    ('Dipirona Sodica 500mg', 'Dipirona Sodica', 'comprimido', TRUE),
    ('Amoxicilina 500mg', 'Amoxicilina', 'capsula', TRUE),
    ('Meropenem 1g', 'Meropenem Tri-hidratado', 'frasco-ampola', TRUE),
    ('Paracetamol 750mg', 'Paracetamol', 'comprimido', TRUE),
    ('Omeprazol 20mg', 'Omeprazol', 'capsula', TRUE),
    ('Ceftriaxona 1g', 'Ceftriaxona Sodica', 'frasco-ampola', TRUE);

INSERT INTO paciente (nome, cpf, cns, data_nascimento) VALUES
    ('Joao Carlos da Silva', '52998224725', '700000000000001', DATE '1958-04-12'),
    ('Maria Eduarda Oliveira', '11144477735', '700000000000002', DATE '1989-09-23');

INSERT INTO prontuario (paciente_id)
SELECT id FROM paciente WHERE cpf IN ('52998224725', '11144477735');

INSERT INTO contato_emergencia (paciente_id, nome, parentesco, telefone, principal)
SELECT id, 'Ana Paula da Silva', 'Filha', '(71) 99999-1010', TRUE
FROM paciente WHERE cpf = '52998224725';

INSERT INTO alergia (paciente_id, substancia, reacao, gravidade, ativa, registrada_por)
SELECT p.id, 'Penicilinas / Amoxicilina', 'Anafilaxia e urticaria grave', 'GRAVE', TRUE, u.id
FROM paciente p, usuario u
WHERE p.cpf = '52998224725' AND u.email = 'medico@vitalis.local';
