# Plano de Implementação: Documentação Técnica Vitalis v2.0 para Overleaf

> **Para executores agenticos:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` em conjunto com `dispatching-parallel-agents` e `humanizer` para implementar este plano tarefa por tarefa. As etapas utilizam a sintaxe de checklist (`- [ ]`) para rastreamento.

**Goal:** Desenvolver uma suíte completa de documentação de engenharia de software para o Sistema de Gestão Hospitalar Inteligente Vitalis v2.0 em formato Overleaf (LaTeX modular compatível com `abntex2` e `pdflatex`), cobrindo os 10 tópicos solicitados com diagramas vetoriais nativos em TikZ, garantindo escrita natural via skill `humanizer` e revisão enxuta via skill `ponytail-review`.

**Architecture:** O projeto será estruturado de forma modular para o Overleaf, composto por um arquivo mestre `main.tex`, preâmbulo de configuração e pacotes `config/pacotes.tex`, base bibliográfica `referencias.bib`, e 10 módulos independentes em `secoes/` (um para cada entrega). Os diagramas (BPMN, Classes e Casos de Uso) serão codificados em TikZ nativo e compiláveis diretamente sem dependências externas de binários.

**Tech Stack:** LaTeX (`abntex2`, `tikz`, `pgfgantt`, `booktabs`, `tabularx`, `amsmath`, `listings`), Git com Conventional Commits, subagentes autônomos com as skills `humanizer` e `ponytail-review`.

**Spec:** Baseado em [ERS-Vitalis.md](file:///home/daviribeiro/Documentos/faculdade/topicos%20avançados%20de%20programaçao/ERS-Vitalis.md) e na especificação formal UCSAL extraída de [extracted_ucsal_vitalis.tex](file:///home/daviribeiro/Documentos/faculdade/topicos%20avançados%20de%20programaçao/extracted_ucsal_vitalis.tex).

---

## Restrições Globais e Diretrizes de Qualidade

1. **Formato Overleaf**: Todo código LaTeX deve compilar perfeitamente no ambiente Overleaf (motor `pdfLaTeX` ou `XeLaTeX`), sem pacotes obsoletos e com tratamento rigoroso de caracteres especiais.
2. **Extensão Mínima do Descritivo**: O tópico 1 (Descritivo) deve conter **no mínimo 100 linhas** de texto técnico, denso e detalhado no arquivo `.tex`.
3. **Escrita Natural (Skill Humanizer)**: Todos os subagentes redatores devem seguir estritamente as diretrizes da skill `humanizer` (sem jargões inflados de IA, sem "testamento/crucial/pivotal/ecossistema vibrante", ritmo variado, voz ativa, precisão técnica e factual).
4. **Revisão Técnica Enxuta (Skill Ponytail-Review)**: Cada seção gerada passará por um subagente revisor focado em eliminar over-engineering, redundâncias textuais e abstrações desnecessárias.
5. **Diagramas Vetoriais Integrados**: Os diagramas de processos (BPMN), estrutura estática (Classes) e interações (Casos de Uso) devem ser gerados em TikZ, garantindo renderização nítida no PDF gerado.
6. **Padrão de Commits**: Todas as alterações devem seguir **Conventional Commits** (`docs:`, `feat:`, `refactor:`, `chore:`).

---

## Estrutura do Repositório Overleaf

```text
vitalis-overleaf/
├── main.tex                       # Documento mestre (abntex2, dados UCSAL, sumário)
├── config/
│   ├── pacotes.tex                # Imports: tikz, pgfgantt, tabularx, booktabs, xcolor
│   └── comandos.tex               # Definições de estilos para caixas e diagramas TikZ
├── secoes/
│   ├── 01_descritivo.tex          # Tópico 1: Descrição aprofundada (>= 100 linhas)
│   ├── 02_problema.tex            # Tópico 2: Identificação clara do problema
│   ├── 03_objetivo.tex            # Tópico 3: Objetivos específicos e mensuráveis (SMART)
│   ├── 04_cronograma.tex          # Tópico 4: Cronograma e marcos (com pgfgantt)
│   ├── 05_requisitos.tex          # Tópico 5: RFs (22), RNFs (7) e RNs (12) estruturados
│   ├── 06_metricas_horas.tex      # Tópico 6: Metrificação em horas por fase e papel
│   ├── 07_bpmn.tex                # Tópico 7: Fluxos BPMN de ponta a ponta em TikZ
│   ├── 08_diagrama_classes.tex    # Tópico 8: Diagrama de Classes UML em TikZ
│   ├── 09_diagrama_casos_uso.tex  # Tópico 9: Diagrama de Casos de Uso UML em TikZ
│   └── 10_funcionalidade_ia.tex   # Tópico 10: Especificação detalhada do módulo de IA
├── referencias.bib                # Referências ABNT (LGPD, ANS/TISS, HL7/FHIR, Topol, Pressman)
└── Makefile                       # Automação local de compilação (latexmk/pdflatex)
```

---

## Tarefas de Execução

### Fase 1: Fundação do Projeto Overleaf

#### Tarefa 1: Scaffolding e Preâmbulo LaTeX
**Arquivos:**
- Criar: `vitalis-overleaf/main.tex`
- Criar: `vitalis-overleaf/config/pacotes.tex`
- Criar: `vitalis-overleaf/config/comandos.tex`
- Criar: `vitalis-overleaf/referencias.bib`

- [ ] **Etapa 1.1:** Criar a estrutura de diretórios `vitalis-overleaf/config`, `vitalis-overleaf/secoes` e `vitalis-overleaf/imagens`.
- [ ] **Etapa 1.2:** Configurar `pacotes.tex` com suporte a `abntex2`, `babel` [brazil], `fontenc` [T1], `tikz` (com bibliotecas `shapes`, `arrows.meta`, `positioning`, `calc`, `fit`), `pgfgantt`, `booktabs`, `tabularx` e `listings`.
- [ ] **Etapa 1.3:** Configurar `main.tex` com cabeçalho institucional UCSAL (Universidade Católica do Salvador, Professor Elton Figueiredo da Silva, Acadêmico Davi Ribeiro, Tópicos Avançados de Programação).
- [ ] **Etapa 1.4:** Gerar arquivo `referencias.bib` com citações de normas técnicas e literatura científica (LGPD, ANS TISS/TUSS, HL7 FHIR, Protocolo de Manchester, Topol 2019, Sommerville/Pressman).
- [ ] **Etapa 1.5:** Commit: `chore: inicializar estrutura e preambulo latex para overleaf`.

---

### Fase 2: Geração Paralela dos Documentos Conceituais e Metrificação
*(Disparo concorrente via `dispatching-parallel-agents` com skill `humanizer` e revisão via `ponytail-review`)*

#### Tarefa 2: Descritivo do Projeto (>= 100 linhas)
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/01_descritivo.tex`

- [ ] **Etapa 2.1:** Disparar subagente redator com a skill `humanizer` para redigir o Descritivo Completo do Sistema Vitalis v2.0 com no mínimo 100 linhas no LaTeX, abordando:
  - Contextualização do ambiente hospitalar de alta complexidade.
  - Visão arquitetural do sistema (microsserviços NestJS, mensageria BullMQ, PostgreSQL 3NF).
  - Ciclo de vida assistencial completo do paciente (admissão, triagem, internação, prescrição, alta, faturamento).
  - Conformidade com a LGPD e sanitização de dados pré-inferência externa.
  - Governança ética e preceito *Human-in-the-Loop*.
- [ ] **Etapa 2.2:** Disparar subagente revisor (`ponytail-review`) para verificar contagem de linhas (>= 100), cortar adjetivações vazias e validar consistência técnica.
- [ ] **Etapa 2.3:** Commit: `docs: adicionar descritivo detalhado do sistema vitalis`.

#### Tarefa 3: Identificação do Problema
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/02_problema.tex`

- [ ] **Etapa 3.1:** Disparar subagente redator (`humanizer`) para estruturar a identificação do problema:
  - Fragmentação de prontuários e latência na transferência de informações entre setores.
  - Falhas na identificação precoce de deterioração hemodinâmica e sepse (qSOFA/NEWS2).
  - Sobrecarga cognitiva de médicos e enfermeiros, elevando erros de prescrição e interações medicamentosas adversas.
  - Alocação ineficiente de leitos de UTI e enfermarias sem previsão de tempo de permanência (*Length of Stay* - LOS).
  - Inconsistências de faturamento TISS/TUSS e impacto financeiro por glosas hospitalares.
- [ ] **Etapa 3.2:** Disparar subagente revisor (`ponytail-review`) para garantir precisão factual e clareza objetiva.
- [ ] **Etapa 3.3:** Commit: `docs: formalizar secao de problema e dores do ambiente hospitalar`.

#### Tarefa 4: Objetivos Específicos e Mensuráveis (SMART)
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/03_objetivo.tex`

- [ ] **Etapa 4.1:** Disparar subagente redator (`humanizer`) formulando os objetivos gerais e a matriz SMART:
  - **S**pecific: Digitalização do acolhimento com transcrição por voz e alerta preditivo em beira de leito.
  - **M**easurable: Redução do tempo de triagem em 40%, detecção precoce de sepse com acurácia AUC-ROC > 0.88, bloqueio de 100% das contraindicações graves na prescrição, e mitigação de 75% das glosas TISS.
  - **A**chievable: Uso de microsserviços desacoplados e APIs LLM com RAG clínico indexado localmente.
  - **R**elevant: Conformidade integral com normas do CFM, ANS e LGPD.
  - **T**ime-bound: Metas de implementação e validação com prazos fixados por ciclo de desenvolvimento.
- [ ] **Etapa 4.2:** Disparar subagente revisor (`ponytail-review`) para verificar quantificadores e métricas.
- [ ] **Etapa 4.3:** Commit: `docs: adicionar objetivos gerais e especificos mensuraveis smart`.

#### Tarefa 5: Cronograma de Trabalho com Marcos e Gantt
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/04_cronograma.tex`

- [ ] **Etapa 5.1:** Disparar subagente redator estruturando as etapas, sprints e marcos em tabela detalhada e gráfico visual `pgfgantt`:
  - Fase I: Concepção, Elicitação e Levantamento de Requisitos (Semanas 1-4).
  - Fase II: Arquitetura, Modelagem de Dados 3NF e Setup de Microsserviços (Semanas 5-8).
  - Fase III: Desenvolvimento do Core Clínico e Triagem Manchester (Semanas 9-14).
  - Fase IV: Módulo de IA (RAG Clínico, qSOFA/NEWS2 e Sanitizador PII) (Semanas 15-20).
  - Fase V: Gestão de Leitos, Faturamento TISS/TUSS e Dashboard (Semanas 21-24).
  - Fase VI: Testes Integrados, Simulações Clínicas, Segurança e Homologação (Semanas 25-28).
- [ ] **Etapa 5.2:** Disparar subagente revisor (`ponytail-review`) para verificar sintaxe do `pgfgantt` e consistência de prazos.
- [ ] **Etapa 5.3:** Commit: `docs: adicionar cronograma detalhado de etapas e grafico pgfgantt`.

#### Tarefa 6: Requisitos Funcionais, Não Funcionais e Regras de Negócio
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/05_requisitos.tex`

- [ ] **Etapa 6.1:** Disparar subagente redator (`humanizer`) compilando a matriz completa em tabelas formais:
  - 22 Requisitos Funcionais (RF-001 a RF-022) com código, descrição técnica, critério de aceitação mensurável e prioridade MoSCoW.
  - 12 Regras de Negócio (RN-001 a RN-012) incluindo prioridade vermelha, *Human-in-the-Loop*, Circuit Breaker e dupla checagem de medicamentos de alta vigilância.
  - 7 Requisitos Não Funcionais (RNF-001 a RNF-007) cobrindo latência (< 200ms em leitura, < 1.5s na IA), TLS 1.3/AES-256, 99.95% de disponibilidade, interoperabilidade HL7/FHIR e WCAG 2.1 AA.
- [ ] **Etapa 6.2:** Disparar subagente revisor (`ponytail-review`) para validar integridade tabular e ausência de redundâncias.
- [ ] **Etapa 6.3:** Commit: `docs: adicionar catalogo completo de requisitos e regras de negocio`.

#### Tarefa 7: Metrificação em Horas por Fase e Papel
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/06_metricas_horas.tex`

- [ ] **Etapa 7.1:** Disparar subagente redator (`humanizer`) detalhando a decomposição do esforço (EAP / WBS):
  - Estimativa de horas por pacote de trabalho (Engenharia de Requisitos, Backend/APIs, IA/RAG/MLOps, Frontend/Tablet UX, QA/Testes, Segurança/LGPD, Treinamento/Deploy).
  - Distribuição de horas por perfil profissional (Tech Lead, Desenvolvedor Backend Sênior, Desenvolvedor Fullstack, Engenheiro de ML/IA, DBA, Especialista Clínico/Auditor).
  - Total consolidado de horas do projeto com buffers de contingência técnica.
- [ ] **Etapa 7.2:** Disparar subagente revisor (`ponytail-review`) para verificar cálculos e somatórios das tabelas.
- [ ] **Etapa 7.3:** Commit: `docs: adicionar metricas de esforco e estimativa detalhada em horas`.

---

### Fase 3: Geração dos Diagramas Técnicos e Funcionalidade de IA
*(Disparo concorrente de subagentes para modelagem visual TikZ e especificação da IA)*

#### Tarefa 8: Fluxos de Processos de Negócio Ponta a Ponta (BPMN em TikZ)
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/07_bpmn.tex`

- [ ] **Etapa 8.1:** Disparar subagente redator para implementar os diagramas de processo BPMN completos codificados em TikZ nativo:
  - **Fluxo 1 (Triagem e Risco Preditivo)**: Chegada do paciente -> Coleta de Sinais Vitais -> Transcrição por voz -> Inferência de Risco IA (qSOFA/NEWS2) -> Classificação Manchester -> Validação Humana -> Fila Priorizada.
  - **Fluxo 2 (Prescrição Médica Inteligente e Dispensação)**: Abertura do PEP -> Seleção de Fármaco -> Validação RAG com histórico de alergias/função renal -> Alerta de Interação/Ajuste de Dose -> Assinatura ICP-Brasil -> Dispensação com Dupla Checagem Farmacêutica.
  - **Fluxo 3 (Alocação de Leitos, Alta e Faturamento)**: Indicação de Internação -> Predição de LOS via IA -> Reserva de Leito Higienizado -> Evoluções Clínicas -> Sumário de Alta gerado por LLM -> Auditoria Prévia de Guias TISS/TUSS -> Transmissão à Operadora.
- [ ] **Etapa 8.2:** Disparar subagente revisor (`ponytail-review`) para garantir sintaxe TikZ limpa, sem sobreposição de nós e com legendas adequadas.
- [ ] **Etapa 8.3:** Commit: `docs: adicionar diagramas de processos bpmn em tikz`.

#### Tarefa 9: Diagrama de Classes UML (TikZ)
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/08_diagrama_classes.tex`

- [ ] **Etapa 9.1:** Disparar subagente redator para estruturar o Diagrama de Classes UML em TikZ representando a estrutura estática completa:
  - Entidades de Domínio Clínico: `Paciente`, `Atendimento`, `TriagemManchester`, `EscoreRisco`, `ProntuarioEletronico`, `EvolucaoClinica`, `PrescricaoMedica`, `ItemPrescricao`.
  - Entidades de Infraestrutura e Faturamento: `Leito`, `AlaHospitalar`, `ContaFaturamento`, `GuiaTISS`.
  - Entidades do Núcleo de IA e Auditoria: `LogInferenciaIA`, `PredicaoRiscoClinico`, `VetorEmbeddingClinico`, `TrilhaAuditoria`.
  - Relações, multiplicidades (1..1, 1..*, 0..*), atributos com tipagem e métodos core.
- [ ] **Etapa 9.2:** Disparar subagente revisor (`ponytail-review`) para verificar coerência estrutural e clareza gráfica.
- [ ] **Etapa 9.3:** Commit: `docs: adicionar diagrama de classes uml completo em tikz`.

#### Tarefa 10: Diagrama de Casos de Uso UML (TikZ)
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/09_diagrama_casos_uso.tex`

- [ ] **Etapa 10.1:** Disparar subagente redator para estruturar o Diagrama de Casos de Uso UML em TikZ:
  - Atores do Sistema: `Enfermeiro Triador`, `Médico Assistente`, `Gestor de Leitos`, `Farmacêutico`, `Auditor de Faturamento`, `Paciente/Familiar` e o ator secundário `Serviço de IA Vitalis`.
  - Casos de Uso: UC-01 (Triagem Preditiva), UC-02 (Gestão de Leitos LOS), UC-03 (Prescrição com Copiloto RAG), UC-04 (Sumário de Alta via LLM), UC-05 (Auditoria TISS/TUSS), UC-06 (Dispensação Segura), UC-07 (Consulta Portal do Paciente).
  - Relações de extensão `<<extend>>` (ex.: disparo de alerta vermelho) e inclusão `<<include>>` (ex.: autenticação ICP-Brasil, higienização PII).
- [ ] **Etapa 10.2:** Disparar subagente revisor (`ponytail-review`) para verificar alinhamento dos nós e clareza das fronteiras do sistema.
- [ ] **Etapa 10.3:** Commit: `docs: adicionar diagrama de casos de uso uml em tikz`.

#### Tarefa 11: Definição Aprofundada da Funcionalidade com IA
**Arquivos:**
- Criar: `vitalis-overleaf/secoes/10_funcionalidade_ia.tex`

- [ ] **Etapa 11.1:** Disparar subagente redator (`humanizer`) para documentar com rigor técnico a arquitetura das funcionalidades de IA:
  - **Copiloto Farmacoterapêutico com RAG Clínico**: Arquitetura do pipeline (vetorização de diretrizes clínicas e bulários em banco vetorial, busca por similaridade semântica com bi-encoders, re-ranking e geração contextualizada).
  - **Motor Preditivo de Sepse e Instabilidade (qSOFA/NEWS2)**: Algoritmos de classificação tabular, telemetria de sinais vitais, cálculo de probabilidade e explicabilidade baseada em SHAP values.
  - **Pipeline de Anonimização e LGPD**: Gateway de sanitização com remoção e tokenização de 18 identificadores diretos (HIPAA Safe Harbor / LGPD Art. 11) antes do despacho para APIs LLM.
  - **Mecanismos de Confiabilidade e Resiliência**: Fallback determinístico (Circuit Breaker padrão Martin Fowler em caso de latência > 2.0s ou timeout) e logs imutáveis de inferência para auditoria clínica.
  - **Princípio Human-in-the-Loop**: Bloqueio de automações de decisão clínica pura; exigência mandatória de confirmação pelo profissional habilitado.
- [ ] **Etapa 11.2:** Disparar subagente revisor (`ponytail-review`) para assegurar robustez técnica e eliminar afirmações infladas.
- [ ] **Etapa 11.3:** Commit: `docs: detalhar especificacao tecnica do modulo e pipeline de ia`.

---

### Fase 4: Integração, Validação da Compilação e Entrega Final

#### Tarefa 12: Integração no Documento Mestre e Validação
**Arquivos:**
- Modificar: `vitalis-overleaf/main.tex`
- Criar: `vitalis-overleaf/README.md` (Instruções passo a passo de importação no Overleaf)

- [ ] **Etapa 12.1:** Configurar `main.tex` para importar todas as seções de `01_descritivo.tex` a `10_funcionalidade_ia.tex` em ordem formal ABNT com sumário, lista de figuras e tabelas.
- [ ] **Etapa 12.2:** Realizar teste de compilação local via `pdflatex` / `latexmk` para verificar integridade, referências cruzadas e ausência de erros de sintaxe LaTeX/TikZ.
- [ ] **Etapa 12.3:** Redigir `vitalis-overleaf/README.md` com instruções sobre como criar um projeto no Overleaf, fazer upload do arquivo compactado `.zip` e compilar diretamente com 1 clique.
- [ ] **Etapa 12.4:** Criar pacote de exportação `vitalis_overleaf_projeto.zip` contendo todos os fontes e assets prontos para upload imediato no Overleaf.
- [ ] **Etapa 12.5:** Commit final: `chore: consolidar projeto overleaf, gerar documentacao de instrucoes e zip`.
