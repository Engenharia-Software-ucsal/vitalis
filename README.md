# Sistema de Gestão Hospitalar Vitalis
## Projeto Semestral — Tópicos Avançados de Programação (UCSAL 2026)

**Instituição:** Universidade Católica do Salvador (UCSAL)  
**Curso:** Bacharelado em Engenharia de Software  
**Disciplina:** Tópicos Avançados de Programação  
**Professor:** Elton Figueiredo da Silva  

### Equipe de Acadêmicos
- Alisson de Oliveira dos Santos
- Davi dos Santos Ribeiro
- Larissa da Silva Santana
- Maria Fernanda Pereira Barçante Costa
- Rafael Farias Castro

---

### Visão Geral do Projeto
O **Vitalis** é um ecossistema integrado de gestão hospitalar projetado para mitigar a fragmentação assistencial em ambientes de saúde de alta complexidade. A documentação formal engloba a especificação completa de requisitos, modelagem de processos de negócio de ponta a ponta (BPMN 2.0), arquitetura estrutural orientada a objetos (Diagrama de Classes UML), diagrama e especificação textual de casos de uso, estimativa paramétrica de esforço e metrificação de software (EAP e MFI da UCSAL) e especificação técnica detalhada do módulo clínico assistido por Inteligência Artificial (Copiloto Farmacoterapêutico com RAG e Predição Precoce de Sepse com qSOFA/NEWS2).

---

### Principais Entregáveis do Repositório

Todos os artefatos formais e fontes do projeto encontram-se estruturados no diretório [`projeto-semestral/`](./projeto-semestral/):

1. **Documento Técnico Consolidado (PDF):**  
   [`projeto-semestral/Vitalis_Documentacao_Completa_UCSAL.pdf`](./projeto-semestral/Vitalis_Documentacao_Completa_UCSAL.pdf)  
   Documento acadêmico de 98 páginas diagramado com capa institucional, sumário, lista de figuras, lista de tabelas e as 10 seções do projeto.

2. **Pacote para Overleaf (ZIP):**  
   [`projeto-semestral/vitalis_overleaf_projeto.zip`](./projeto-semestral/vitalis_overleaf_projeto.zip)  
   Pacote compactado contendo todos os fontes LaTeX (`main.tex`, `secoes/`, `config/`, `anexos/`), pronto para importação direta no [Overleaf](https://www.overleaf.com) sem dependências externas.

3. **Planilha de Metrificação Oficial da UCSAL (MFI):**  
   [`projeto-semestral/Documento de metrificação - Vitalis.xlsx`](./projeto-semestral/Documento%20de%20metrifica%C3%A7%C3%A3o%20-%20Vitalis.xlsx)  
   Planilha parametrizada na metodologia de Métrica de Função de Implementação (320 PI, 1.081,31 horas brutas e 851 horas líquidas).

4. **Fontes Modulares em LaTeX (TikZ Nativo):**  
   [`projeto-semestral/vitalis-overleaf/`](./projeto-semestral/vitalis-overleaf/)  
   Diretório com todo o código-fonte LaTeX, incluindo diagramas BPMN e UML compilados diretamente via código vetorial.

---

### Sistema (MVP + Serviço de IA Clínica)

Back-end em Java 21 + Spring Boot 3.5 (microsserviços atrás do Spring Cloud Gateway com JWT + RBAC) e front-end em React + TypeScript + Vite + Tailwind CSS.
- **Arquitetura e Modelo C4 Completo (Níveis 1 a 4):** [`docs/arquitetura.md`](./docs/arquitetura.md)
- **Catálogo de Telas e Fluxos (Screenshots Playwright):** [`docs/telas/telas-e-fluxos.md`](./docs/telas/telas-e-fluxos.md)
- **Guia Passo a Passo de Navegação por Perfil:** [`docs/guia-de-uso.md`](./docs/guia-de-uso.md)

```
backend/
├── gateway/           # Spring Cloud Gateway + validação JWT/CORS (porta 8080)
├── servico-clinico/   # Auth/JWT, pacientes, triagem Manchester/qSOFA/NEWS2, PEP, prescrição, alta
├── servico-leitos/    # Leitos, alocações, transferência interna (RF-019), histórico e higienização
└── servico-ia/        # Copiloto RAG, Sumário de Alta, Sanitizador PII, Circuit Breaker 2s e auditoria WORM
frontend/              # React + TypeScript + Vite + Tailwind CSS (porta 3000 via Nginx / 5173 dev)
docker-compose.yml     # Postgres 17 + 4 serviços Java + Front-end Nginx
```

---

### Passo a Passo para Rodar o Projeto

#### Pré-requisitos
- **Docker** e **Docker Compose** (única exigência para rodar toda a stack e os testes do back-end).
- *(Opcional)* **Node.js 20+** caso queira rodar o front-end fora do Docker em modo hot-reload.

#### 1. Configurar variáveis de ambiente
Na raiz do repositório, copie o arquivo de exemplo:

```bash
cp .env.example .env
```

> **Dica:** O `.env` padrão já vem pronto para subir localmente. Caso a porta `8080` ou `3000` esteja ocupada na sua máquina, ajuste `GATEWAY_PORT` ou `FRONTEND_PORT` no `.env` (ex.: `GATEWAY_PORT=8088`).

#### 2. Configurar o provedor de IA (`servico-ia`, opcional)
O `servico-ia` é agnóstico a modelo/ferramenta e usa o protocolo padrão OpenAI-compatible (`/v1/chat/completions`). Edite no `.env` conforme o provedor desejado:

- **OpenRouter (padrão — Gemini, Claude, GPT, Llama, etc.):**
  ```env
  AI_PROVIDER=openai-compatible
  AI_BASE_URL=https://openrouter.ai/api/v1
  AI_API_KEY=sk-or-v1-sua-chave-aqui
  AI_MODEL=google/gemini-2.5-flash
  AI_TIMEOUT_MS=8000
  ```
- **Ollama local:**
  ```env
  AI_PROVIDER=openai-compatible
  AI_BASE_URL=http://host.docker.internal:11434/v1
  AI_API_KEY=ollama
  AI_MODEL=llama3.1:8b
  ```
- **Modo Mock / Offline:**
  ```env
  AI_PROVIDER=mock
  ```

> Se o provedor configurado estiver indisponível ou demorar mais que `AI_TIMEOUT_MS` (padrão `8000ms` para nuvem; ajustável para `2000ms` em testes estritos de RNF-002), o **Circuit Breaker** comuta automaticamente para o **motor determinístico local** (Cockcroft-Gault, interações Categoria X/D e base RAG), sem travar o sistema.

#### 3. Subir toda a aplicação via Docker (recomendado)

```bash
docker compose up -d --build
```

Aguarde os healthchecks finalizarem e confirme com:

```bash
docker compose ps
```

Todos os 6 serviços (`postgres`, `servico-clinico`, `servico-leitos`, `servico-ia`, `gateway` e `frontend`) devem aparecer como `healthy` / `running`.

- **Aplicação Web (Front-end):** `http://localhost:3000`
- **API Gateway:** `http://localhost:8080` (ou a porta definida em `GATEWAY_PORT`)
- **PostgreSQL 17:** `localhost:5433` (usuário/senha `vitalis`, bancos `vitalis_clinico`, `vitalis_leitos` e `vitalis_ia`)

#### 4. *(Opcional)* Rodar o Front-end em modo desenvolvimento local (hot-reload)

```bash
docker compose up -d --build postgres servico-clinico servico-leitos servico-ia gateway
cd frontend
npm install
npm run dev
```

Acesse `http://localhost:5173` (o Vite faz proxy automático de `/api` para `http://localhost:8080`).

#### 5. Usuários de Demonstração (senha padrão: `Vitalis@2026`)

As migrations criam automaticamente 5 usuários (1 por perfil), medicamentos, leitos e pacientes de exemplo:

| Perfil | E-mail | Principais Telas / Ações |
|---|---|---|
| Administrador | `admin@vitalis.local` | Acesso total + métricas de governança de IA |
| Recepcionista | `recepcao@vitalis.local` | Cadastro de pacientes, contatos de emergência e abertura de atendimentos |
| Enfermeiro | `enfermeiro@vitalis.local` | Fila prioritária (RN-001) e triagem Manchester + qSOFA/NEWS2 |
| Médico | `medico@vitalis.local` | PEP, evoluções, alergias, prescrição (RN-008), Copiloto IA (HITL) e Alta Médica (RF-012) |
| Gestor de Leitos | `leitos@vitalis.local` | Mapa de leitos, alocação (RN-004), transferência interna (RF-019) e higienização (RN-010) |

#### 6. Rodar os testes automatizados

- **Back-end (via Docker, sem precisar instalar Java 21/Maven localmente):**
  ```bash
  docker run --rm -v "$PWD/backend:/src" -v vitalis-m2:/root/.m2 -w /src maven:3.9-eclipse-temurin-21 mvn test
  ```
- **Front-end (verificação de tipos TypeScript + build de produção):**
  ```bash
  cd frontend && npm run build
  ```

#### 7. Parar e limpar o ambiente

```bash
# Parar os containers mantendo os dados do banco
docker compose down

# Parar os containers e resetar os bancos de dados (reaplica migrations e seeds do zero)
docker compose down -v
```
