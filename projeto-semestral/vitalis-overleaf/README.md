# Sistema de Gestão Hospitalar Inteligente Vitalis
## Documentação Técnica de Engenharia de Software para Overleaf

**Instituição:** Universidade Católica do Salvador -- UCSAL  
**Curso:** Engenharia de Software  
**Disciplina:** Tópicos Avançados de Programação  
**Professor:** Elton Figueiredo da Silva  
**Acadêmicos:**  
- Alisson de Oliveira dos Santos  
- Davi dos Santos Ribeiro  
- Larissa da Silva Santana  
- Maria Fernanda Pereira Barçante Costa  
- Rafael Farias Castro  

---

## 1. Visão Geral do Projeto

Este repositório contém a suíte completa de documentação formal de engenharia de software, modelagem de processos e arquitetura clínica inteligente para o **Sistema Vitalis**. Todo o projeto foi arquitetado em LaTeX modular compatível com o **Overleaf** (`pdfLaTeX` e `XeLaTeX`), sem dependência de ferramentas externas para geração de diagramas, que foram codificados nativamente em **TikZ**.

O projeto atende rigorosamente aos 10 itens solicitados:
1. **Descritivo Detalhado:** Mais de 100 linhas de densidade técnica, cobrindo o ecossistema hospitalar, microsserviços NestJS/BullMQ/PostgreSQL, ciclo assistencial e governança clínica.
2. **Identificação e Delimitação do Problema:** Análise profunda da fragmentação assistencial, septicemia hospitalar tardia, sobrecarga médica e glosas TISS/TUSS.
3. **Objetivos Gerais e Metas SMART:** Matriz quantitativa mensurável e tabela comparativa de KPIs hospitalares.
4. **Cronograma do Trabalho:** 6 Fases ao longo de 28 semanas com matriz de entregáveis e gráfico de Gantt vetorial nativo (`pgfgantt`).
5. **Requisitos do Sistema e Diretrizes Operacionais:** Catálogo completo com 22 Requisitos Funcionais (RF-001 a RF-022) com prioridade MoSCoW, 12 Regras de Negócio (RN-1 a RN-12) e 7 Requisitos Não Funcionais (RNF-001 a RNF-007).
6. **Metrificação em Horas e Estimativa de Esforço:** Estrutura Analítica do Projeto (EAP) com 38 subtarefas (2.160h base / 2.484h com contingência) e a incorporação oficial da **Métrica de Função de Implementação (MFI / Modelo UCSAL)** calibrada na planilha institucional (1.081,31h brutas / 851h líquidas).
7. **Modelagem de Processos de Negócio BPMN 2.0:** 3 fluxos ponta a ponta (Triagem Manchester com IA; Prescrição com Copiloto RAG e Farmácia; Alocação de Leitos, Alta e Faturamento TISS) em TikZ nativo com raias dedicadas.
8. **Diagrama de Classes UML:** 15 classes estruturadas em 3 camadas conceituais (`vitalis.infraestrutura`, `vitalis.clinico`, `vitalis.ia.auditoria`), padrões Strategy, Repository e normalização em 3NF.
9. **Diagrama de Casos de Uso UML:** Mapeamento de 6 atores humanos e do Serviço de IA Vitalis, fronteiras de sistema, relacionamentos $\ll\text{include}\gg$ e $\ll\text{extend}\gg$, e especificação detalhada de UC-01 a UC-08.
10. **Funcionalidade Integrada com Inteligência Artificial:** Especificação aprofundada do Copiloto Farmacoterapêutico (RAG com `pgvector` e busca híbrida BM25 + Dense RRF), Motor Preditivo de Sepse (qSOFA/NEWS2 e explicabilidade SHAP), Gateway de Anonimização LGPD (Art. 11), Circuit Breaker e trilha WORM imutável.

---

## 2. Estrutura de Arquivos do Projeto

```text
vitalis-overleaf/
├── main.tex                       # Documento mestre unificado (capa, sumário, listas)
├── config/
│   ├── pacotes.tex                # Configuração tipográfica, TikZ, pgfgantt, tabularx
│   └── comandos.tex               # Estilos vetoriais para BPMN, UML e caixas de destaque
├── secoes/
│   ├── 01_descritivo.tex          # Tópico 1: Descritivo aprofundado (>= 100 linhas)
│   ├── 02_problema.tex            # Tópico 2: Identificação do problema hospitalar
│   ├── 03_objetivo.tex            # Tópico 3: Objetivos SMART e KPIs
│   ├── 04_cronograma.tex          # Tópico 4: Cronograma e gráfico pgfgantt
│   ├── 05_requisitos.tex          # Tópico 5: RFs (22), RNs (12) e RNFs (7)
│   ├── 06_metricas_horas.tex      # Tópico 6: Metrificação EAP e MFI da UCSAL
│   ├── 07_bpmn.tex                # Tópico 7: 3 Fluxos BPMN em TikZ nativo
│   ├── 08_diagrama_classes.tex    # Tópico 8: Diagrama de Classes UML em TikZ
│   ├── 09_diagrama_casos_uso.tex  # Tópico 9: Diagrama de Casos de Uso UML em TikZ
│   └── 10_funcionalidade_ia.tex   # Tópico 10: Especificação técnica da IA clínica
├── anexos/
│   ├── Documento_de_metrificacao_Vitalis.xlsx        # Planilha oficial MFI calculada para o Vitalis
│   └── Template_Original_Metrificacao_UCSAL.xlsx    # Planilha modelo fornecida pela cátedra
├── referencias.bib                # Base bibliográfica (CFM, LGPD, ANS, HL7, Manchester, Topol)
└── Makefile                       # Automação de compilação local (pdflatex/bibtex)
```

---

## 3. Como Importar e Compilar no Overleaf (Passo a Passo)

1. **Baixar o pacote ZIP:** Faça o download do arquivo `vitalis_overleaf_projeto.zip` gerado na raiz deste repositório.
2. **Acessar o Overleaf:** Faça login em [https://www.overleaf.com](https://www.overleaf.com).
3. **Criar Novo Projeto:**
   - Clique no botão verde **"New Project"** (Novo Projeto);
   - Selecione a opção **"Upload Project"** (Carregar Projeto);
   - Arraste ou selecione o arquivo `vitalis_overleaf_projeto.zip`.
4. **Compilar:**
   - O Overleaf detectará automaticamente o arquivo mestre `main.tex`;
   - Certifique-se de que o compilador configurado seja **pdfLaTeX** (configuração padrão do Overleaf) com TeX Live 2023 ou superior;
   - Clique em **"Recompile"** (Recompilar) para gerar o PDF de 98 páginas completo e diagramado.

---

## 4. Planilha de Metrificação Oficial da UCSAL

O projeto integra a planilha **`Documento de metrificação - Vitalis.xlsx`** (localizada em `anexos/`), calculada rigorosamente segundo a **Métrica de Função de Implementação (MFI)** da UCSAL:

- **Volume:** 17 programas distribuídos em 8 Macrofunções de Implementação (MFI-01 a MFI-08).
- **Tamanho Total:** 320 Pontos de Implementação (PI).
- **Esforço de Construção (55%):** 594,72 horas.
- **Esforço de Projeto/Especificação (25%):** 270,33 horas.
- **Esforço de Testes (20%):** 216,26 horas.
- **Total de Esforço Bruto de Software:** **1.081,31 horas**.
- **Esforço Líquido Calibrado por Recursos (Níveis 2 e 3):** **851 horas**.
