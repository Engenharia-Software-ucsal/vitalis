INSERT INTO documento_clinico_rag (titulo, fonte_oficial, medicamento_ref, secao, conteudo) VALUES
('Monografia ANVISA - Meropenem Tri-hidratado', 'Bulario Eletronico ANVISA', 'Meropenem', 'Ajuste Renal',
 'Em pacientes com insuficiencia renal, a dose deve ser ajustada conforme depuracao de creatinina (ClCr): ClCr 26-50 mL/min: 1g a cada 12h; ClCr 10-25 mL/min: 500mg a cada 12h; ClCr < 10 mL/min: 500mg a cada 24h.'),
('Monografia ANVISA - Meropenem Tri-hidratado', 'Bulario Eletronico ANVISA', 'Meropenem', 'Interacoes Medicamentosas',
 'A coadministracao de carbapenemicos (incluindo Meropenem) com acido valproico pode reduzir as concentracoes plasmaticas do acido valproico em 60% a 100% em dois dias, com risco grave de convulsoes. Associacao contraindicada (Categoria X).'),
('Monografia ANVISA - Amoxicilina 500mg', 'Bulario Eletronico ANVISA', 'Amoxicilina', 'Contraindicacoes',
 'Contraindicado para pacientes com historico de hipersensibilidade a penicilinas ou cefalosporinas devido ao risco de anafilaxia grave. Em ClCr < 30 mL/min, espacar intervalo para 12/12h ou 24/24h.'),
('Monografia ANVISA - Dipirona Sodica 500mg', 'Bulario Eletronico ANVISA', 'Dipirona', 'Contraindicacoes e Posologia',
 'Contraindicada em pacientes com alergia a pirazolonas ou historico de agranulocitose. Dose usual adulto: 500mg a 1g a cada 6h (maximo 4g/dia). Em insuficiencia renal ou hepatica grave, evitar doses elevadas.'),
('Monografia ANVISA - Ceftriaxona 1g', 'Bulario Eletronico ANVISA', 'Ceftriaxona', 'Interacoes e Precaucoes',
 'Nao administrar simultaneamente com solucoes intravenosas contendo calcio (como Ringer Lactato) na mesma via devido ao risco de precipitacao fatal. Avaliar reatividade cruzada em alergicos graves a beta-lactamicos.'),
('Monografia ANVISA - Paracetamol 750mg', 'Bulario Eletronico ANVISA', 'Paracetamol', 'Posologia e Hepatotoxicidade',
 'Dose maxima diaria de 4g em adultos saudaveis e 2g em hepatopatas ou etilistas cronicos. Nao exige ajuste renal rotineiro, mas aumentar intervalo para 8/8h se ClCr < 10 mL/min.'),
('Monografia ANVISA - Omeprazol 20mg', 'Bulario Eletronico ANVISA', 'Omeprazol', 'Interacoes Medicamentosas',
 'Inibe CYP2C19 e pode reduzir a eficacia antiagregante do clopidogrel (Categoria D). Preferir pantoprazol quando associado a clopidogrel.'),
('Protocolo Surviving Sepsis Campaign - Bundle 1 Hora', 'Diretriz Institucional Vitalis / SSC', 'Sepse', 'Protocolo de Emergencia',
 'Na suspeita de sepse (qSOFA >= 2 ou NEWS2 >= 7): 1) Medir lactato serico; 2) Coletar hemoculturas antes do antimicrobiano; 3) Iniciar antimicrobiano de amplo espectro na primeira hora; 4) Administrar cristaloide 30 mL/kg se hipotensao (PAS < 90 mmHg) ou lactato >= 4 mmol/L.');

INSERT INTO interacao_medicamentosa_local (farmaco_a, farmaco_b, categoria, descricao, referencia) VALUES
('Meropenem', 'Acido Valproico', 'X',
 'Queda abrupta (60-100%) dos niveis sericos de acido valproico com alto risco de crise convulsiva refrataria. Evitar associacao.',
 'Bulario ANVISA - Meropenem / Micromedex Categoria X'),
('Ceftriaxona', 'Calcio', 'X',
 'Risco de precipitacao de ceftriaxona-calcio em pulmoes e rins. Contraindicada administracao simultanea IV.',
 'Bulario ANVISA - Ceftriaxona / Categoria X'),
('Omeprazol', 'Clopidogrel', 'D',
 'Inibicao da CYP2C19 pelo omeprazol reduz formacao do metabolito ativo do clopidogrel, elevando risco trombotico. Considerar pantoprazol.',
 'Diretriz SBC / Bulario ANVISA - Categoria D');
