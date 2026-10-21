# Projeto Semestral: Sistema de Gestão Hospitalar Vitalis
## Disciplina: Tópicos Avançados de Programação (UCSAL 2026)
**Professor:** Elton Figueiredo da Silva  
**Curso:** Engenharia de Software  
**Acadêmicos:**  
- Alisson de Oliveira dos Santos  
- Davi dos Santos Ribeiro  
- Larissa da Silva Santana  
- Maria Fernanda Pereira Barçante Costa  
- Rafael Farias Castro  

---

### Estrutura de Arquivos do Projeto Semestral

Esta pasta consolida todos os artefatos, documentações, modelos e entregáveis desenvolvidos para o **Projeto Semestral do Sistema Vitalis**:

```text
projeto-semestral/
│
├── Vitalis_Documentacao_Completa_UCSAL.pdf    # PDF final oficial consolidado (98 páginas diagramadas)
├── vitalis_overleaf_projeto.zip               # Pacote ZIP completo pronto para upload no Overleaf
│
├── Documento de metrificação - Vitalis.xlsx   # Planilha oficial MFI (UCSAL) parametrizada para o Vitalis
├── Documento de metrificação.xlsx             # Template original da planilha MFI disponibilizado pela cátedra
│
├── vitalis-overleaf/                          # Diretório com os fontes completos em LaTeX modular
│   ├── main.tex                               # Documento mestre unificado (capa, sumário, listas)
│   ├── config/                                # Configuração de pacotes e comandos visuais TikZ
│   ├── secoes/                                # As 10 seções técnicas completas (01 a 10)
│   ├── anexos/                                # Cópias das planilhas de metrificação
│   ├── referencias.bib                        # Base bibliográfica acadêmica
│   ├── plain-pt.bst                           # Estilo BibTeX traduzido para o padrão em português
│   └── README.md                              # Guia de compilação e importação no Overleaf
│
├── ERS-Vitalis.md                             # Especificação de Requisitos de Software inicial em Markdown
├── Especificação de Requisitos de Software... # ERS original em formato PDF
├── extracted_ucsal_vitalis.tex                # Extração do documento base UCSAL
└── docs/                                      # Planos de arquitetura e registros de execução
```

### Principais Entregáveis do Projeto

1. **PDF Oficial:** [`Vitalis_Documentacao_Completa_UCSAL.pdf`](./Vitalis_Documentacao_Completa_UCSAL.pdf) contendo as 10 seções exigidas (Descritivo com mais de 100 linhas, Problema, Objetivos SMART, Cronograma com Gantt vetorial, 22 RFs / 12 RNs / 7 RNFs, Metrificação EAP e MFI, 3 Fluxos BPMN em TikZ, Diagrama de Classes UML com 15 entidades, Diagrama de Casos de Uso com especificação de UC-01 a UC-08 e Módulo Clínico de IA com RAG e Circuit Breaker).
2. **Pacote Overleaf:** [`vitalis_overleaf_projeto.zip`](./vitalis_overleaf_projeto.zip), bastando selecionar "New Project" -> "Upload Project" no Overleaf para compilar sem necessidade de pacotes externos.
3. **Planilha de Metrificação:** [`Documento de metrificação - Vitalis.xlsx`](./Documento%20de%20metrifica%C3%A7%C3%A3o%20-%20Vitalis.xlsx), estruturada no modelo paramétrico da MFI (320 PI, 1.081,31h brutas / 851h líquidas).
