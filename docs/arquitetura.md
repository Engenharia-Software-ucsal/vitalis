# Arquitetura do Vitalis — Modelo C4 Completo (Níveis 1 a 4)

O **Vitalis** é um sistema integrado de gestão hospitalar estruturado em **arquitetura de microsserviços** atrás de um **API Gateway**, com isolamento de dados por serviço (*database-per-service*) e módulo de **Inteligência Artificial Clínica** desacoplado por *Ports & Adapters* (agnóstico a modelo/provedor, com *Circuit Breaker*, sanitização de PII e contingência determinística local).

---

## 1. Stack Tecnológica

| Camada | Tecnologia | Papel na Arquitetura |
|---|---|---|
| **Linguagem (Back-end)** | Java 21 | Exigência da disciplina; records, pattern matching e tipagem forte |
| **Framework Back-end** | Spring Boot 3.5.7 | Microsserviços REST (`servico-clinico`, `servico-leitos`, `servico-ia`) |
| **API Gateway** | Spring Cloud Gateway (WebFlux) | Ponto único de entrada (`/api/**`), validação JWT e política de CORS |
| **Segurança** | Spring Security + OAuth2 Resource Server (JWT HS256) | Autenticação stateless (TTL 15 min + refresh) e RBAC por papel (`@PreAuthorize`) |
| **Resiliência (IA)** | Resilience4j Circuit Breaker | Proteção de chamadas de IA com timeout estrito e fallback determinístico |
| **Persistência & Migrações** | PostgreSQL 17 + Spring Data JPA + Flyway | 3 bancos isolados (`vitalis_clinico`, `vitalis_leitos`, `vitalis_ia`) em 3NF com triggers WORM (`append-only`) |
| **Front-end** | React 19 + TypeScript + Vite + Tailwind CSS + TanStack Query | SPA responsiva (desktop/tablet) servida via Nginx com polling reativo a cada 5s |
| **Orquestração** | Docker & Docker Compose | Subida unificada dos 6 contêineres com healthchecks encadeados |

---

## 2. C4 — Nível 1: Diagrama de Contexto

Mostra o sistema **Vitalis** como uma caixa-preta no centro, os 5 perfis de usuários hospitalares que interagem com ele e a integração externa/local com provedores de modelos de linguagem (LLMs).

```mermaid
C4Context
    title Vitalis — C4 Nível 1: Diagrama de Contexto

    Person(recepcao, "Recepcionista", "Cadastra pacientes, contatos de emergência e abre atendimentos")
    Person(enfermagem, "Enfermeiro(a)", "Realiza triagem Manchester + qSOFA/NEWS2 e monitora fila prioritária")
    Person(medico, "Médico(a)", "Acessa PEP, registra evoluções, alergias, prescrições com Copiloto IA e concede alta")
    Person(gestorLeitos, "Gestor de Leitos / Hotelaria", "Aloca pacientes triados, transfere entre alas e confirma higienização")
    Person(admin, "Administrador", "Gerencia operação global e consulta métricas de governança de IA")

    System(vitalis, "Sistema de Gestão Hospitalar Vitalis", "Centraliza acolhimento, triagem Manchester, PEP, prescrição assistida por IA, gestão de leitos e auditoria imutável")

    System_Ext(llmProvider, "Provedor de IA (Ollama / OpenRouter / OpenAI)", "API compatível com /v1/chat/completions que recebe apenas cargas úteis higienizadas sem PII")

    Rel(recepcao, vitalis, "Cadastra pacientes e abre atendimentos", "HTTPS")
    Rel(enfermagem, vitalis, "Classifica risco e monitora sinais vitais", "HTTPS")
    Rel(medico, vitalis, "Evolui PEP, prescreve com Copiloto IA e dá alta", "HTTPS")
    Rel(gestorLeitos, vitalis, "Regula ocupação, transferências e higienização", "HTTPS")
    Rel(admin, vitalis, "Audita indicadores e governança de IA", "HTTPS")
    Rel(vitalis, llmProvider, "Envia prompts clínicos sanitizados (timeout <= 2s, fallback local)", "REST/JSON")
```

---

## 3. C4 — Nível 2: Diagrama de Contêineres

Detalha as unidades de implantação (*containers* Docker), os três bancos de dados isolados no PostgreSQL 17 e as rotas de comunicação síncrona REST.

```mermaid
C4Container
    title Vitalis — C4 Nível 2: Diagrama de Contêineres

    Person(usuario, "Profissional de Saúde", "Recepcionista, Enfermeiro, Médico, Gestor de Leitos ou Admin")

    System_Boundary(vitalis, "Ecossistema Vitalis (Docker Compose)") {
        Container(front, "Front-end Web (SPA)", "React 19, TypeScript, Tailwind, Nginx (:3000)", "Interface responsiva de recepção, triagem, PEP, Copiloto IA e mapa de leitos")
        Container(gateway, "API Gateway", "Spring Cloud Gateway WebFlux (:8080)", "Valida JWT, aplica CORS e roteia /api/clinico/**, /api/leitos/** e /api/ia/**")

        Container(clinico, "Serviço Clínico", "Java 21 / Spring Boot 3.5 (:8081)", "Auth/JWT, pacientes, fila prioritária, triagem Manchester/qSOFA/NEWS2, PEP, alergias, prescrições, alta e auditoria")
        Container(leitos, "Serviço de Leitos", "Java 21 / Spring Boot 3.5 (:8082)", "Mapa de leitos, alocações, transferência interna (RF-019), higienização (RN-010) e histórico")
        Container(ia, "Serviço de IA Clínica", "Java 21 / Spring Boot 3.5 (:8083)", "Sanitizador PII (LGPD), RAG clínico, Circuit Breaker, motor determinístico (Cockcroft-Gault/interações) e auditoria WORM")

        ContainerDb(dbClinico, "vitalis_clinico", "PostgreSQL 17", "Usuários, papéis, pacientes, atendimentos, triagens, prontuário, evoluções, alergias, medicamentos, prescrições e auditoria_acesso")
        ContainerDb(dbLeitos, "vitalis_leitos", "PostgreSQL 17", "Leitos, alocações ativas/encerradas e histórico cronológico de status")
        ContainerDb(dbIa, "vitalis_ia", "PostgreSQL 17", "Base documental RAG (ANVISA), interações Categoria X/D, log_inferencia_ia (WORM) e decisao_humana_ia (WORM)")
    }

    System_Ext(llm, "Provedor LLM Agnóstico", "Ollama Local (:11434) ou OpenRouter", "Inferência textual consultiva via protocolo OpenAI-compatible")

    Rel(usuario, front, "Acessa via navegador / tablet", "HTTP/HTTPS :3000")
    Rel(front, gateway, "Consome APIs REST com Bearer JWT", "JSON/HTTP :8080")
    Rel(gateway, clinico, "Roteia /api/clinico/**", "HTTP :8081")
    Rel(gateway, leitos, "Roteia /api/leitos/**", "HTTP :8082")
    Rel(gateway, ia, "Roteia /api/ia/**", "HTTP :8083")

    Rel(leitos, clinico, "Verifica elegibilidade e triagem prévia (RN-004)", "RestClient + JWT")
    Rel(clinico, leitos, "Libera leito automaticamente na alta médica (RF-012)", "RestClient + JWT")
    Rel(ia, llm, "Solicita completude com payload sem PII", "RestClient + CircuitBreaker")

    Rel(clinico, dbClinico, "Lê/grava (Flyway V1..V4)", "JDBC")
    Rel(leitos, dbLeitos, "Lê/grava (Flyway V1..V3)", "JDBC")
    Rel(ia, dbIa, "Lê/grava (Flyway V1..V2)", "JDBC")
```

---

## 4. C4 — Nível 3: Diagrama de Componentes

### 4.1 Componentes do `servico-clinico`

```mermaid
C4Component
    title Vitalis — C4 Nível 3: Componentes do Serviço Clínico (servico-clinico)

    Container_Boundary(clinico, "servico-clinico (Spring Boot 3.5)") {
        Component(sec, "SecurityConfig & UsuarioAutenticado", "Spring Security / JWT", "Valida e emite tokens JWT HS256 (15 min) e extrai papéis RBAC")
        Component(authCtrl, "AuthController & AuthService", "REST Controller / Service", "Login (/auth/login), refresh (/auth/refresh) e perfil (/auth/me)")
        Component(pacCtrl, "PacienteController & ValidadorCpf", "REST Controller", "Cadastro de pacientes (CPF/CNS), criação 1:1 de prontuário e contatos de emergência")
        Component(atendCtrl, "AtendimentoController & AtendimentoService", "REST Controller / Service", "Abertura de atendimento, fila ordenada por Manchester (RN-001), elegibilidade (RN-004) e alta (RF-012)")
        Component(triCtrl, "TriagemController & TriagemService", "REST Controller / Service", "Registra sinais vitais e aciona calculadoras determinísticas")
        Component(calcManch, "CalculadoraManchester & CalculadoraEscoresClinicos", "Domain Service", "Classificação determinística das 5 cores Manchester, qSOFA (0-3), NEWS2 (0-20) e alerta de sepse")
        Component(pepCtrl, "ProntuarioController & ProntuarioService", "REST Controller / Service", "Linha do tempo consolidada do PEP e registro de evoluções até 8.000 chars com bloqueio pós-alta (RN-12)")
        Component(prescCtrl, "PrescricaoController, PrescricaoService & AlergiaController", "REST Controller / Service", "Gestão de alergias, catálogo de medicamentos e bloqueio de prescrição sem reconhecimento explícito de alergia (RN-008)")
        Component(auditSvc, "AuditoriaService", "Domain Service", "Gravação automática de eventos LEITURA/CRIACAO/ALTERACAO em auditoria_acesso (append-only)")
    }

    ContainerDb(dbClinico, "vitalis_clinico", "PostgreSQL 17")
    Container(leitos, "servico-leitos", "Spring Boot (:8082)")

    Rel(authCtrl, sec, "Emite/valida JWT")
    Rel(atendCtrl, leitos, "POST /alocacoes/atendimento/{id}/liberar na alta", "RestClient")
    Rel(triCtrl, calcManch, "Calcula cor Manchester, qSOFA e NEWS2")
    Rel(pepCtrl, auditSvc, "Registra leitura e escrita do PEP")
    Rel(prescCtrl, auditSvc, "Registra emissão de prescrição")
    Rel(atendCtrl, auditSvc, "Registra abertura e alta")
    Rel(auditSvc, dbClinico, "INSERT imutável")
```

### 4.2 Componentes do `servico-leitos`

```mermaid
C4Component
    title Vitalis — C4 Nível 3: Componentes do Serviço de Leitos (servico-leitos)

    Container_Boundary(leitos, "servico-leitos (Spring Boot 3.5)") {
        Component(secL, "SecurityConfig & UsuarioAutenticado", "Spring Security / JWT", "Valida JWT HS256 e propaga Bearer token para chamadas inter-serviços")
        Component(leitoCtrl, "LeitoController", "REST Controller", "Mapa de leitos por ala/status, cadastro, mudança de status, higienização (RN-010) e histórico")
        Component(alocCtrl, "AlocacaoController & AlocacaoService", "REST Controller / Service", "Alocação (RN-004, RN-010), transferência interna transacional (RF-019) e liberação na alta (RF-012)")
        Component(clinClient, "ClinicoClient", "Spring RestClient", "Consulta GET /atendimentos/{id}/elegibilidade-internacao no servico-clinico")
        Component(leitoDomain, "Leito, AlocacaoLeito & HistoricoStatusLeito", "JPA Entities", "Máquina de estados do leito e trilha cronológica de transições")
    }

    ContainerDb(dbLeitos, "vitalis_leitos", "PostgreSQL 17")
    Container(clinico, "servico-clinico", "Spring Boot (:8081)")

    Rel(alocCtrl, clinClient, "Valida se paciente possui triagem ativa (RN-004)")
    Rel(clinClient, clinico, "GET /atendimentos/{id}/elegibilidade-internacao", "HTTP/JSON")
    Rel(leitoCtrl, leitoDomain, "Altera status e grava histórico")
    Rel(alocCtrl, leitoDomain, "Aloca, transfere e libera leito")
    Rel(leitoDomain, dbLeitos, "JPA / JDBC")
```

### 4.3 Componentes do `servico-ia` (Agnóstico a Modelo/Provedor)

```mermaid
C4Component
    title Vitalis — C4 Nível 3: Componentes do Serviço de IA Clínica (servico-ia)

    Container_Boundary(ia, "servico-ia (Spring Boot 3.5)") {
        Component(iaCtrl, "IaController", "REST Controller", "Endpoints /copiloto/prescricao, /sumario-alta, /inferencias/{id}/decisao e /governanca/metricas")
        Component(copSvc, "CopilotoClinicoService", "Application Service", "Orquestra sanitização PII, busca RAG, regras determinísticas, Circuit Breaker e auditoria WORM")
        Component(piiSvc, "SanitizadorPiiService", "Privacy Gateway (RF-018)", "Remove CPF, CNS, nome, telefone, e-mail, CEP e gera token HMAC-SHA256 + hash SHA-256")
        Component(detSvc, "CalculadoraCockcroftGault & InteracaoMedicamentosaRepository", "Motor Determinístico (RN-4)", "Calcula ClCr renal, detecta interações Categoria X/D e conflitos de alergia localmente")
        Component(ragPort, "KnowledgeRetriever -> PostgresKnowledgeRetriever", "Port & Adapter (RAG)", "Recupera monografias ANVISA e protocolos clínicos para grounding estrito")
        Component(llmPort, "LlmProvider -> OpenAiCompatibleLlmProvider / MockLlmProvider", "Port & Adapter (LLM)", "Comunica via /v1/chat/completions com Ollama, OpenRouter, OpenAI, Groq ou Mock")
        Component(wormRepo, "LogInferenciaIaRepository & DecisaoHumanaIaRepository", "WORM Repositories (RF-019 / RN-2)", "Persiste inferências e deliberações Human-in-the-Loop em tabelas append-only")
    }

    ContainerDb(dbIa, "vitalis_ia", "PostgreSQL 17")
    System_Ext(llmExt, "Ollama / OpenRouter", "API OpenAI-Compatible")

    Rel(iaCtrl, copSvc, "Delega análise, sumário e decisão HITL")
    Rel(copSvc, piiSvc, "1. Higieniza payload e calcula hash SHA-256")
    Rel(copSvc, detSvc, "2. Executa Cockcroft-Gault e checa interações X/D")
    Rel(copSvc, ragPort, "3. Busca trechos clínicos oficiais")
    Rel(copSvc, llmPort, "4. Invoca LLM protegido por Resilience4j CircuitBreaker")
    Rel(llmPort, llmExt, "POST /v1/chat/completions", "HTTP/JSON")
    Rel(copSvc, wormRepo, "5. Grava trilha imutável WORM")
    Rel(ragPort, dbIa, "SELECT documento_clinico_rag")
    Rel(detSvc, dbIa, "SELECT interacao_medicamentosa_local")
    Rel(wormRepo, dbIa, "INSERT log_inferencia_ia / decisao_humana_ia")
```

---

## 5. C4 — Nível 4: Diagramas de Código (Classes Implementadas)

### 5.1 Domínio Clínico (`br.ucsal.vitalis.clinico`)

```mermaid
classDiagram
    class Usuario {
        +Long id
        +String nome
        +String email
        +String senhaHash
        +String status
        +Set~Papel~ papeis
    }

    class Papel {
        +Long id
        +String codigo
        +String descricao
    }

    class Paciente {
        +Long id
        +String nome
        +String cpf
        +String cns
        +LocalDate dataNascimento
        +OffsetDateTime criadoEm
    }

    class ContatoEmergencia {
        +Long id
        +Long pacienteId
        +String nome
        +String parentesco
        +String telefone
        +boolean principal
    }

    class Atendimento {
        +Long id
        +Long pacienteId
        +Long responsavelId
        +TipoAtendimento tipo
        +StatusAtendimento status
        +OffsetDateTime iniciadoEm
        +OffsetDateTime encerradoEm
        +String motivoEncerramento
        +assumir(Long profissionalId) void
        +concederAlta(String motivo) void
    }

    class Triagem {
        +Long id
        +Long atendimentoId
        +Long enfermeiroId
        +PrioridadeManchester prioridade
        +String queixaPrincipal
        +BigDecimal temperaturaC
        +Integer frequenciaCardiaca
        +Integer frequenciaRespiratoria
        +Integer pressaoSistolica
        +Integer pressaoDiastolica
        +BigDecimal saturacaoO2
        +Integer escalaDor
        +Short escalaGlasgow
        +boolean oxigenioSuplementar
        +short escoreQsofa
        +short escoreNews2
        +boolean alertaSepse
    }

    class CalculadoraManchester {
        +calcular(spo2, pas, fr, fc, glasgow, tempC, dor, qsofa, news2)$ PrioridadeManchester
    }

    class CalculadoraEscoresClinicos {
        +calcularQsofa(Integer fr, Integer pas, Integer glasgow)$ int
        +calcularNews2(fr, spo2, o2Sup, pas, fc, glasgow, tempC)$ ResultadoNews2
        +isAlertaSepse(int qsofa, int news2, boolean criticoIndividual)$ boolean
    }

    class Prontuario {
        +Long id
        +Long pacienteId
        +OffsetDateTime criadoEm
    }

    class EvolucaoClinica {
        +Long id
        +Long prontuarioId
        +Long atendimentoId
        +Long profissionalId
        +String texto
        +OffsetDateTime criadaEm
    }

    class Alergia {
        +Long id
        +Long pacienteId
        +String substancia
        +String reacao
        +GravidadeAlergia gravidade
        +boolean ativa
        +inativar() void
    }

    class Prescricao {
        +Long id
        +Long atendimentoId
        +Long prescritorId
        +StatusPrescricao status
        +boolean alergiasReconhecidas
        +OffsetDateTime criadaEm
    }

    class ItemPrescricao {
        +Long id
        +Long prescricaoId
        +Long medicamentoId
        +String dose
        +String via
        +String frequencia
        +String duracao
        +BigDecimal quantidade
    }

    class Medicamento {
        +Long id
        +String nome
        +String principioAtivo
        +String unidade
        +boolean ativo
    }

    class AuditoriaAcesso {
        <<Immutable>>
        +Long id
        +Long atorId
        +AcaoAuditoria acao
        +String recurso
        +String recursoId
        +Map~String,Object~ metadados
        +OffsetDateTime ocorridoEm
    }

    Usuario "*" --> "*" Papel : usuario_papel
    Paciente "1" --> "*" ContatoEmergencia : possui
    Paciente "1" --> "1" Prontuario : titular
    Paciente "1" --> "*" Alergia : registra
    Paciente "1" --> "*" Atendimento : passa por
    Atendimento "1" --> "*" Triagem : avaliado em
    Triagem ..> CalculadoraManchester : calcula cor
    Triagem ..> CalculadoraEscoresClinicos : calcula qSOFA/NEWS2
    Prontuario "1" --> "*" EvolucaoClinica : contem
    Atendimento "1" --> "*" Prescricao : origina
    Prescricao "1" --> "1..*" ItemPrescricao : contem
    ItemPrescricao "*" --> "1" Medicamento : referencia
```

### 5.2 Domínio de Leitos (`br.ucsal.vitalis.leitos`) e Serviço de IA (`br.ucsal.vitalis.ia`)

```mermaid
classDiagram
    class Leito {
        +Long id
        +String codigo
        +String ala
        +StatusLeito status
        +alterarStatus(StatusLeito novo) void
    }

    class AlocacaoLeito {
        +Long id
        +Long leitoId
        +Long atendimentoId
        +StatusAlocacao status
        +OffsetDateTime alocadoEm
        +OffsetDateTime liberadoEm
        +Long responsavelId
        +encerrar(OffsetDateTime instante) void
    }

    class HistoricoStatusLeito {
        +Long id
        +Long leitoId
        +Long alocacaoId
        +StatusLeito statusAnterior
        +StatusLeito statusNovo
        +Long atendimentoId
        +Long responsavelId
        +OffsetDateTime alteradoEm
    }

    class AlocacaoService {
        +alocar(NovaAlocacaoRequest req) AlocacaoLeito
        +transferir(Long alocacaoId, Long leitoDestinoId) AlocacaoLeito
        +liberarPorAtendimento(Long atendimentoId) LiberacaoResponse
    }

    class ClinicoClient {
        +verificarElegibilidadeInternacao(Long atendimentoId, String token) ElegibilidadeInternacaoResponse
    }

    class LlmProvider {
        <<interface>>
        +providerName() String
        +modelName() String
        +completar(LlmRequest request) LlmResponse
    }

    class OpenAiCompatibleLlmProvider {
        +completar(LlmRequest request) LlmResponse
    }

    class MockLlmProvider {
        +completar(LlmRequest request) LlmResponse
    }

    class KnowledgeRetriever {
        <<interface>>
        +buscar(List~String~ termos, int topK) List~TrechoClinico~
    }

    class PostgresKnowledgeRetriever {
        +buscar(List~String~ termos, int topK) List~TrechoClinico~
    }

    class SanitizadorPiiService {
        +sanitizarTexto(String texto, String nomePaciente) String
        +gerarTokenSessao(Long atendimentoId) String
        +hashSha256(String payload) String
    }

    class CalculadoraCockcroftGault {
        +calcular(Integer idade, Double peso, Double creatinina, String sexo)$ BigDecimal
        +exigeAjusteRenal(BigDecimal clcr)$ boolean
    }

    class CopilotoClinicoService {
        +analisarPrescricao(AnalisePrescricaoRequest req) Map
        +gerarSumarioAlta(SumarioAltaRequest req) Map
        +registrarDecisaoHitl(Long inferenciaId, DecisaoHitlRequest req) DecisaoHumanaIa
        +metricasGovernanca() Map
    }

    class LogInferenciaIa {
        <<Immutable>>
        +Long id
        +Long atendimentoId
        +Long usuarioId
        +ModuloIa moduloIa
        +String provedorIa
        +String modeloIaVersao
        +String promptSanitizadoHash
        +boolean modoContingenciaAtivo
        +Map~String,Object~ respostaIa
    }

    class DecisaoHumanaIa {
        <<Immutable>>
        +Long id
        +Long logInferenciaId
        +Long usuarioId
        +DecisaoMedica decisaoMedica
        +String justificativaHumana
        +OffsetDateTime decididoEm
    }

    Leito "1" --> "*" AlocacaoLeito : recebe
    Leito "1" --> "*" HistoricoStatusLeito : registra
    AlocacaoService --> Leito : gerencia
    AlocacaoService --> AlocacaoLeito : cria/encerra
    AlocacaoService --> ClinicoClient : valida RN-004
    OpenAiCompatibleLlmProvider ..|> LlmProvider
    MockLlmProvider ..|> LlmProvider
    PostgresKnowledgeRetriever ..|> KnowledgeRetriever
    CopilotoClinicoService --> LlmProvider : usa via CircuitBreaker
    CopilotoClinicoService --> KnowledgeRetriever : consulta RAG
    CopilotoClinicoService --> SanitizadorPiiService : remove PII
    CopilotoClinicoService ..> CalculadoraCockcroftGault : calcula ClCr
    CopilotoClinicoService --> LogInferenciaIa : persiste WORM
    CopilotoClinicoService --> DecisaoHumanaIa : registra HITL
    DecisaoHumanaIa "0..1" --> "1" LogInferenciaIa : delibera sobre
```

---

## 6. Catálogo Completo de Endpoints (via API Gateway)

| Serviço | Método | Rota no Gateway | Papéis Permitidos | Requisito / Regra |
|---|---|---|---|---|
| **Clínico** | `POST` | `/api/clinico/auth/login` | Público | RF-001 (JWT 15m + Refresh) |
| **Clínico** | `POST` | `/api/clinico/auth/refresh` | Público | RNF-011 (Renovação de sessão) |
| **Clínico** | `GET` | `/api/clinico/auth/me` | Autenticado | RF-001 (Sessão atual) |
| **Clínico** | `GET` | `/api/clinico/pacientes?q=` | Autenticado | RF-002 (Busca por nome/CPF) |
| **Clínico** | `POST` | `/api/clinico/pacientes` | `RECEPCIONISTA`, `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | RF-002 (Valida CPF/CNS + cria PEP) |
| **Clínico** | `GET/POST/DELETE` | `/api/clinico/pacientes/{id}/contatos-emergencia` | `RECEPCIONISTA`, `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | RF-002 (Contatos de emergência) |
| **Clínico** | `POST` | `/api/clinico/atendimentos` | `RECEPCIONISTA`, `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | Abertura de episódio clínico |
| **Clínico** | `GET` | `/api/clinico/atendimentos/fila` | Autenticado | RN-001 (Fila prioritária: Vermelho no topo) |
| **Clínico** | `PATCH` | `/api/clinico/atendimentos/{id}/assumir` | `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | Assumir atendimento ativo |
| **Clínico** | `GET` | `/api/clinico/atendimentos/{id}/elegibilidade-internacao` | Autenticado (inter-serviço) | RN-004 (Verifica triagem prévia) |
| **Clínico** | `POST` | `/api/clinico/atendimentos/{id}/alta` | `MEDICO`, `ADMINISTRADOR` | RF-012 / RN-12 (Alta + libera leito) |
| **Clínico** | `GET/POST` | `/api/clinico/atendimentos/{id}/triagens` | `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | RF-003 / RF-004(OV) (Manchester + qSOFA + NEWS2) |
| **Clínico** | `GET` | `/api/clinico/pacientes/{id}/prontuario` | `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | RF-005 / RNF-004 (PEP + Auditoria `LEITURA`) |
| **Clínico** | `POST` | `/api/clinico/pacientes/{id}/prontuario/evolucoes` | `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | RF-010 / RN-12 (Evolução até 8000 chars) |
| **Clínico** | `GET/POST/PATCH` | `/api/clinico/pacientes/{id}/alergias` | `ENFERMEIRO`, `MEDICO`, `ADMINISTRADOR` | RF-018 (Cadastro e inativação de alergias) |
| **Clínico** | `GET/POST` | `/api/clinico/medicamentos` | Autenticado / `MEDICO`, `FARMACEUTICO`, `ADMINISTRADOR` | Catálogo farmacológico |
| **Clínico** | `GET/POST` | `/api/clinico/atendimentos/{id}/prescricoes` | `MEDICO`, `ADMINISTRADOR` | RF-006 / RN-008 (Reconhecimento de alergia) |
| **Leitos** | `GET` | `/api/leitos/leitos?status=&ala=` | Autenticado | RF-004 (Mapa visual com alocação ativa) |
| **Leitos** | `POST` | `/api/leitos/leitos` | `GESTOR_LEITOS`, `ADMINISTRADOR` | Cadastro de leito |
| **Leitos** | `PATCH` | `/api/leitos/leitos/{id}/status` | `GESTOR_LEITOS`, `ENFERMEIRO`, `ADMINISTRADOR` | RN-010 (Bloqueia ocupar `EM_HIGIENIZACAO`) |
| **Leitos** | `PATCH` | `/api/leitos/leitos/{id}/higienizar` | `GESTOR_LEITOS`, `ADMINISTRADOR` | RN-010 (`EM_HIGIENIZACAO` → `LIVRE`) |
| **Leitos** | `GET` | `/api/leitos/leitos/{id}/historico` | Autenticado | Histórico cronológico de status |
| **Leitos** | `POST` | `/api/leitos/alocacoes` | `GESTOR_LEITOS`, `MEDICO`, `ENFERMEIRO`, `ADMINISTRADOR` | RN-004 / RN-010 (Alocar paciente triado) |
| **Leitos** | `POST` | `/api/leitos/alocacoes/{id}/transferir` | `GESTOR_LEITOS`, `MEDICO`, `ENFERMEIRO`, `ADMINISTRADOR` | RF-019 (Transferência interna transacional) |
| **Leitos** | `POST` | `/api/leitos/alocacoes/atendimento/{id}/liberar` | `MEDICO`, `GESTOR_LEITOS`, `ADMINISTRADOR` | RF-012 (Liberação automática na alta) |
| **IA** | `POST` | `/api/ia/copiloto/prescricao` | `MEDICO`, `ADMINISTRADOR` | RF-008 / RF-018 / RF-020 (RAG + PII + Circuit Breaker) |
| **IA** | `POST` | `/api/ia/sumario-alta` | `MEDICO`, `ADMINISTRADOR` | RF-011 (Minuta estruturada de alta) |
| **IA** | `POST` | `/api/ia/inferencias/{id}/decisao` | `MEDICO`, `ENFERMEIRO`, `ADMINISTRADOR` | RF-019 / RN-2 (Deliberação HITL em WORM) |
| **IA** | `GET` | `/api/ia/governanca/metricas` | `ADMINISTRADOR`, `MEDICO` | RF-019 (Indicadores de governança e aceite) |
