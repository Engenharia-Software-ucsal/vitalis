# ATIVIDADE PRÁTICA I: Especificação de Requisitos de Software (ERS)
**Universidade Católica de Salvador (UCSAL)**
Disciplina: Engenharia de Requisitos
Professor: Elton Figueiredo da Silva
Acadêmico: Davi Ribeiro

## 1. Introdução e Escopo

### 1.1 Visão Geral do Sistema

O Sistema de Gestão Hospitalar Vitalis surge da necessidade de centralizar e otimizar processos críticos em ambientes de saúde de alta complexidade. O principal propósito da aplicação é resolver a fragmentação de informações entre os setores de triagem, gestão de leitos e prontuário eletrônico. Através de uma interface integrada, o sistema visa reduzir o tempo de espera no atendimento, garantir a precisão dos registros clínicos e fornecer métricas em tempo real para a administração hospitalar sobre a ocupação de recursos.

### 1.2 Escopo do Projeto

**O que o sistema fará:**

- Gerenciamento completo do ciclo de vida do paciente, desde a recepção e triagem (Protocolo de Manchester) até a alta médica.
- Controle em tempo real da ocupação, reserva e higienização de leitos hospitalares.
- Manutenção de Prontuários Eletrônicos de Saúde (PEP) com histórico de evoluções, prescrições e resultados de exames.
- Módulo de parametrização de custos de assistência e faturamento por serviços prestados.
- Dashboard administrativo com indicadores de desempenho e ranking de produtividade.

**O que o sistema não fará:**

- Processamento de folha de pagamento de funcionários (integrado via API externa, mas não parte do core).
- Gestão de compras e suprimentos de almoxarifado (fora do domínio de triagem/leitos).
- Realização de diagnósticos automatizados sem supervisão médica.

### 1.3 Perfis de Usuários (Stakeholders)

| Ator | Descrição das Responsabilidades |
|---|---|
| Administrador | Gestão de usuários, configuração de roles (papéis), parametrização de regras globais e acesso a dashboards estratégicos. |
| Enfermeiro(a) de Triagem | Realização da classificação de risco dos pacientes, preenchimento de sinais vitais e encaminhamento para especialidades. |
| Médico(a) | Acesso ao PEP, realização de evoluções clínicas, prescrição de medicamentos e procedimentos, e solicitação de exames. |
| Gestor de Leitos | Monitoramento do status de ocupação, atribuição de quartos para novos pacientes e liberação para higienização. |
| Recepcionista | Cadastro inicial de pacientes, verificação de convênios e agendamento de consultas básicas. |
| Farmacêutico(a) | Validação e dispensação de medicamentos prescritos, controle de estoque de fármacos vinculado à prescrição digital. |
| Paciente / Responsável Legal | Acesso a um portal restrito para visualizar exames, agendamentos e documentos de alta, mediante autenticação própria. |

## 2. Requisitos Funcionais (RF)

Abaixo estão detalhados os requisitos funcionais que compõem o comportamento esperado do sistema Vitalis:

- **RF-001** - Autenticação e Autorização baseada em Roles: O sistema deve permitir o login de usuários com diferentes níveis de acesso (Admin, Médico, Colaborador), garantindo que cada perfil visualize apenas as funcionalidades pertinentes ao seu cargo.
- **RF-002** - Cadastro Unificado de Pacientes: O sistema deve permitir o registro de dados pessoais, contatos de emergência e histórico de convênios, integrando campos de parametrização de assistência.
- **RF-003** - Módulo de Triagem (Manchester): O sistema deve fornecer um formulário estruturado para triagem, onde o enfermeiro registra sintomas e sinais vitais, e o sistema sugere a prioridade (cor) conforme o protocolo.
- **RF-004** - Gestão de Ocupação de Leitos: O sistema deve apresentar um mapa visual dos leitos por ala, permitindo alterar o status para: Livre, Ocupado, Reservado ou Em Limpeza.
- **RF-005** - Prontuário Eletrônico do Paciente (PEP): O sistema deve manter um registro cronológico de todas as interações do paciente no hospital, incluindo notas de evolução e histórico de procedimentos.
- **RF-006** - Prescrição Médica Digital: O sistema deve permitir que médicos criem prescrições digitais vinculadas ao prontuário, com verificação de obrigatoriedade de campos de data e hora.
- **RF-007** - Parametrização de Pacotes de Assistência: O sistema deve permitir configurar regras de faturamento (carteira/mês ou item tratado) para os serviços de saúde prestados aos pacientes vinculados a convênios.
- **RF-008** - Dashboard de Indicadores em Tempo Real: O sistema deve exibir gráficos de tempo médio de espera, taxa de ocupação de leitos e ranking de atendimentos por especialidade.
- **RF-009** - Gestão de Alertas de Vigência: O sistema deve notificar quando a vigência de uma parametrização de assistência ou plano de saúde de um paciente estiver próxima do fim.
- **RF-010** - Registro de Evolução Clínica: O sistema deve permitir a inserção de textos detalhados de evolução, com suporte a até 8000 caracteres por registro.
- **RF-011** - Solicitação de Exames Complementares: O médico deve poder solicitar exames laboratoriais e de imagem diretamente pelo prontuário, gerando uma guia eletrônica.
- **RF-012** - Fluxo de Alta Hospitalar: O sistema deve exigir a validação médica e a confirmação de higienização do leito para que o processo de alta seja concluído e o registro faturado.
- **RF-013** - Dispensação e Controle de Medicamentos: O sistema deve permitir que o farmacêutico confirme a dispensação de itens prescritos, dando baixa automática no estoque vinculado à prescrição.
- **RF-014** - Agendamento de Consultas e Procedimentos: O sistema deve permitir à recepção agendar consultas ambulatoriais, verificando conflito de horário com a agenda do profissional selecionado.
- **RF-015** - Recebimento de Resultados de Exames: O sistema deve permitir o upload e a anexação de laudos e imagens de exames ao prontuário do paciente, associando-os à solicitação original (RF-011).
- **RF-016** - Portal do Paciente: O sistema deve disponibilizar um ambiente restrito para o paciente ou responsável legal consultar exames liberados, histórico de agendamentos e documentos de alta.
- **RF-017** - Exportação de Relatórios Gerenciais: O sistema deve permitir a exportação dos indicadores do dashboard (RF-008) em formato PDF e CSV para períodos selecionáveis.
- **RF-018** - Registro de Alergias e Contraindicações: O sistema deve permitir o cadastro de alergias e contraindicações do paciente, exibindo alerta visual destacado ao médico durante a prescrição.
- **RF-019** - Transferência Interna de Paciente: O sistema deve permitir a transferência de um paciente entre leitos ou alas, atualizando automaticamente o status de origem para "Em Limpeza" e o de destino para "Ocupado".
- **RF-020** - Assinatura Digital de Documentos Clínicos: O sistema deve permitir que médicos assinem digitalmente prescrições, laudos e evoluções, vinculando a assinatura a um certificado ou credencial pessoal.

## 3. Regras de Negócio (RN)

As regras de negócio abaixo definem as restrições operacionais e lógicas do domínio de saúde:

| Código | Nome | Descrição |
|---|---|---|
| RN-001 | Prioridade na Triagem | Pacientes classificados com a cor "Vermelha" (Emergência) devem ser movidos automaticamente para o topo da lista de espera médica, ignorando a ordem de chegada. |
| RN-002 | Vigência de Assistência | Ao criar um pacote de serviço ou assistência, a data de início da vigência não pode ser inferior à data atual do sistema. |
| RN-003 | Sigilo de Prontuário | Um médico só pode acessar o prontuário de um paciente que não está sob seus cuidados se houver uma autorização explícita do administrador ou em caso de emergência auditada. |
| RN-004 | Obrigatoriedade de Triagem | Nenhum paciente pode ser internado em um leito sem ter passado previamente pelo módulo de triagem e classificação de risco. |
| RN-005 | Faturamento por Item | Quando o tipo de faturamento for "Item tratado", o sistema deve obrigatoriamente exigir o preenchimento do custo do serviço e o percentual de taxa de serviço (fee). |
| RN-006 | Limite de Acionamento | Se o pacote de assistência for limitado por quantidade de acionamentos ao ano, o sistema deve bloquear novos atendimentos via convênio após atingir o limite parametrizado. |
| RN-007 | Confirmação de Exclusão | Qualquer tentativa de remover uma assistência ou serviço selecionado em um plano deve exigir a confirmação do usuário via modal de segurança. |
| RN-008 | Checagem de Alergia na Prescrição | O sistema não deve permitir a confirmação de uma prescrição sem que o médico reconheça explicitamente um alerta de alergia registrado para o paciente (RF-018). |
| RN-009 | Dispensação Vinculada à Prescrição | Nenhuma dispensação de medicamento (RF-013) pode ocorrer sem uma prescrição digital ativa (RF-006) correspondente. |
| RN-010 | Bloqueio de Leito Não Higienizado | Um leito com status "Em Limpeza" não pode ser reservado ou ocupado até que o Gestor de Leitos confirme a conclusão da higienização. |

## 4. Requisitos Não Funcionais (RNF)

Os requisitos não funcionais descrevem as qualidades do sistema e restrições técnicas:

### 4.1 Desempenho

- **RNF-001** - Tempo de Resposta: O sistema deve processar consultas ao prontuário eletrônico e carregar o mapa de leitos em menos de 2 segundos para até 500 usuários simultâneos.
- **RNF-002** - Latência de API: As chamadas de integração com o backend para gravação de pacotes de assistência devem ter latência média inferior a 500ms.
- **RNF-009** - Escalabilidade Horizontal: A arquitetura deve suportar o aumento de carga por meio da adição de novas instâncias de aplicação, sem necessidade de reescrita de componentes existentes.

### 4.2 Segurança

- **RNF-003** - Criptografia de Dados: Todos os dados sensíveis dos pacientes (conforme LGPD) devem ser criptografados em repouso utilizando o padrão AES-256.
- **RNF-004** - Auditoria de Acesso: O sistema deve manter logs de auditoria (logs de auditoria imutáveis) de cada visualização ou alteração feita em prontuários médicos.
- **RNF-010** - Autenticação Multifator: O acesso de perfis com permissão de prescrição ou alteração de prontuário deve exigir autenticação multifator (MFA).
- **RNF-011** - Expiração de Sessão: Sessões inativas por mais de 15 minutos devem ser encerradas automaticamente, exigindo nova autenticação.

### 4.3 Usabilidade e Acessibilidade

- **RNF-005** - Interface Responsiva: A interface deve ser adaptável para tablets, permitindo que enfermeiros realizem a triagem e médicos façam evoluções à beira do leito.
- **RNF-006** - Conformidade WCAG: O sistema deve atingir o nível AA de conformidade com as diretrizes WCAG 2.1, garantindo acessibilidade para profissionais com deficiência visual ou motora.
- **RNF-012** - Suporte Multilíngue: A interface do Portal do Paciente (RF-016) deve suportar, no mínimo, português e inglês, com possibilidade de expansão para outros idiomas.

### 4.4 Disponibilidade e Confiabilidade

- **RNF-007** - SLA de Funcionamento: O sistema deve garantir uma disponibilidade mínima de 99,9% (High Availability), considerando a natureza crítica da operação hospitalar.
- **RNF-008** - Rotina de Backup: Devem ser realizados backups incrementais a cada 4 horas e backups completos diariamente, com armazenamento em ambiente geograficamente distribuído.
- **RNF-013** - Recuperação de Desastres (RTO/RPO): O sistema deve garantir um tempo máximo de recuperação (RTO) de 4 horas e uma perda máxima de dados (RPO) de 1 hora em caso de falha catastrófica.

### 4.5 Manutenibilidade e Interoperabilidade

- **RNF-014** - Padrão de Interoperabilidade em Saúde: As integrações de troca de dados clínicos com sistemas externos devem seguir o padrão HL7 FHIR.
- **RNF-015** - Rastreabilidade de Versão: Toda alteração em regras de negócio parametrizáveis (ex.: pacotes de assistência) deve manter histórico de versões anteriores, permitindo consulta retroativa.

## 5. Detalhes de Interface e Experiência (UX)

O sistema seguirá o padrão de design institucional, garantindo sobriedade e clareza para reduzir a fadiga cognitiva dos profissionais de saúde. Os formulários de cadastro devem respeitar a ordem lógica de preenchimento e fornecer feedback imediato de sucesso ou erro na gravação de pacotes de assistência.

- **Paleta de Cores:** Uso de tons azuis e cinzas para transmitir profissionalismo e calma.
- **Componentes:** Cards expansíveis para detalhamento de serviços e modais de confirmação para ações destrutivas.
- **Feedback Visual:** Notificações flutuantes (toasts) para confirmação de salvamento de registros.

## 6. Glossário de Termos

- **PEP (Prontuário Eletrônico do Paciente):** Registro digital completo das informações de saúde de um paciente.
- **Protocolo de Manchester:** Sistema de triagem que classifica pacientes por cores conforme a gravidade.
- **SLA (Service Level Agreement):** Acordo de nível de serviço que define métricas de disponibilidade.
- **Role:** Papel de usuário que define permissões de acesso ao sistema.
- **LGPD:** Lei Geral de Proteção de Dados, normativa para tratamento de dados pessoais.
- **HL7 FHIR:** Padrão internacional para troca eletrônica de informações de saúde entre sistemas.
- **RTO (Recovery Time Objective):** Tempo máximo tolerável para restauração de um sistema após uma falha.
- **RPO (Recovery Point Objective):** Intervalo máximo tolerável de perda de dados em caso de falha, medido em tempo.
- **MFA (Multi-Factor Authentication):** Mecanismo de autenticação que exige mais de um fator de verificação de identidade.
