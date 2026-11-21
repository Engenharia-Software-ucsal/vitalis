import { useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  AlertTriangle,
  ArrowLeft,
  CheckCircle2,
  ClipboardPlus,
  FileText,
  LogOut,
  Pill,
  Plus,
  ShieldAlert,
  Trash2,
} from 'lucide-react';
import { api, ApiError } from '../api/client';
import { ConfirmModal } from '../components/ConfirmModal';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import type { GravidadeAlergia, ItemPrescricaoInput } from '../types/api';
import {
  formatCpf,
  formatDate,
  formatDateTime,
  formatStatusAtendimento,
  hasAnyRole,
  MANCHESTER_META,
} from '../utils/clinical';

type Tab = 'timeline' | 'evolucao' | 'alergias' | 'prescricao';

export function ProntuarioPage() {
  const { id: pacienteId = '' } = useParams();
  const { user } = useAuth();
  const { notify } = useToast();
  const showToast = (msg: string, variant: 'success' | 'error' | 'warning' | 'info' = 'info') =>
    notify(variant, msg);
  const queryClient = useQueryClient();

  const [tab, setTab] = useState<Tab>('timeline');
  const [selectedAtendimentoId, setSelectedAtendimentoId] = useState<string>('');

  // Evolução
  const [evolucaoTexto, setEvolucaoTexto] = useState('');

  // Alergia
  const [substancia, setSubstancia] = useState('');
  const [reacao, setReacao] = useState('');
  const [gravidade, setGravidade] = useState<GravidadeAlergia>('GRAVE');

  // Prescrição
  const [itens, setItens] = useState<ItemPrescricaoInput[]>([
    { medicamentoId: '', dose: '500mg', via: 'VO', frequencia: '8/8h', duracao: '5 dias', quantidade: 1, instrucoes: '' },
  ]);
  const [showAlergiaModal, setShowAlergiaModal] = useState(false);

  // Copiloto IA (RF-008 / RN-2 / RN-4)
  const [analiseIa, setAnaliseIa] = useState<Awaited<ReturnType<typeof api.analisarPrescricaoIa>> | null>(null);
  const [decisaoRegistrada, setDecisaoRegistrada] = useState<string | null>(null);
  const [justificativaIa, setJustificativaIa] = useState('');

  // Alta
  const [showAltaModal, setShowAltaModal] = useState(false);
  const [motivoAlta, setMotivoAlta] = useState('Melhora clinica e condicoes de alta hospitalar');

  const canPrescribe = hasAnyRole(user?.roles, ['MEDICO']);
  const canDischarge = hasAnyRole(user?.roles, ['MEDICO']);
  const canEditClinical = hasAnyRole(user?.roles, ['MEDICO', 'ENFERMEIRO']);

  const { data: prontuario, isLoading, error } = useQuery({
    queryKey: ['prontuario', pacienteId],
    queryFn: () => api.obterProntuario(pacienteId),
    enabled: Boolean(pacienteId),
  });

  const { data: medicamentos = [] } = useQuery({
    queryKey: ['medicamentos'],
    queryFn: () => api.listarMedicamentos(),
    enabled: canPrescribe,
  });

  const atendimentosAtivos = useMemo(
    () => (prontuario?.atendimentos ?? []).filter((a) => a.status !== 'ALTA' && a.status !== 'CANCELADO'),
    [prontuario]
  );

  const atendimentoAtual = useMemo(() => {
    const all = prontuario?.atendimentos ?? [];
    if (selectedAtendimentoId) {
      return all.find((a) => String(a.id) === String(selectedAtendimentoId)) ?? all[0];
    }
    return atendimentosAtivos[0] ?? all[0];
  }, [prontuario, selectedAtendimentoId, atendimentosAtivos]);

  const atendimentoEncerrado =
    !atendimentoAtual || atendimentoAtual.status === 'ALTA' || atendimentoAtual.status === 'CANCELADO';

  const alergiasAtivas = useMemo(
    () => (prontuario?.alergias ?? []).filter((a) => a.ativa),
    [prontuario]
  );

  const refreshProntuario = () => {
    queryClient.invalidateQueries({ queryKey: ['prontuario', pacienteId] });
    queryClient.invalidateQueries({ queryKey: ['fila-atendimentos'] });
    queryClient.invalidateQueries({ queryKey: ['leitos'] });
  };

  const evolucaoMutation = useMutation({
    mutationFn: () =>
      api.criarEvolucao(pacienteId, {
        atendimentoId: String(atendimentoAtual!.id),
        texto: evolucaoTexto.trim(),
      }),
    onSuccess: () => {
      setEvolucaoTexto('');
      showToast('Evolução clínica registrada com sucesso.', 'success');
      refreshProntuario();
      setTab('timeline');
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao registrar evolução.', 'error');
    },
  });

  const alergiaMutation = useMutation({
    mutationFn: () =>
      api.criarAlergia(pacienteId, {
        substancia: substancia.trim(),
        reacao: reacao.trim() || undefined,
        gravidade,
      }),
    onSuccess: () => {
      setSubstancia('');
      setReacao('');
      showToast('Alergia registrada no prontuário.', 'success');
      refreshProntuario();
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao cadastrar alergia.', 'error');
    },
  });

  const inativarAlergiaMutation = useMutation({
    mutationFn: (alergiaId: string) => api.inativarAlergia(pacienteId, alergiaId),
    onSuccess: () => {
      showToast('Alergia inativada.', 'info');
      refreshProntuario();
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao inativar alergia.', 'error');
    },
  });

  const prescricaoMutation = useMutation({
    mutationFn: (alergiasReconhecidas: boolean) =>
      api.criarPrescricao(String(atendimentoAtual!.id), {
        alergiasReconhecidas,
        itens: itens.map((i) => ({
          ...i,
          quantidade: Number(i.quantidade) || 1,
        })),
      }),
    onSuccess: () => {
      setShowAlergiaModal(false);
      setItens([
        { medicamentoId: '', dose: '500mg', via: 'VO', frequencia: '8/8h', duracao: '5 dias', quantidade: 1, instrucoes: '' },
      ]);
      showToast('Prescrição médica confirmada e registrada.', 'success');
      refreshProntuario();
      setTab('timeline');
    },
    onError: (err) => {
      if (err instanceof ApiError && err.status === 422 && alergiasAtivas.length > 0) {
        setShowAlergiaModal(true);
        return;
      }
      showToast(err instanceof ApiError ? err.message : 'Erro ao criar prescrição.', 'error');
    },
  });

  const abrirAtendimentoMutation = useMutation({
    mutationFn: () => api.abrirAtendimento({ pacienteId, tipo: 'EMERGENCIA' }),
    onSuccess: () => {
      showToast('Atendimento aberto com sucesso.', 'success');
      refreshProntuario();
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao abrir atendimento.', 'error');
    },
  });

  const altaMutation = useMutation({
    mutationFn: () => api.darAltaAtendimento(String(atendimentoAtual!.id), motivoAlta.trim()),
    onSuccess: () => {
      setShowAltaModal(false);
      showToast('Alta médica concluída. Leito (se alocado) enviado para higienização.', 'success');
      refreshProntuario();
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao conceder alta médica.', 'error');
    },
  });

  const copilotoIaMutation = useMutation({
    mutationFn: () => {
      const medsMap = new Map(medicamentos.map((m) => [String(m.id), m.nome]));
      const idadeAnos = prontuario?.paciente.dataNascimento
        ? Math.max(0, new Date().getFullYear() - new Date(prontuario.paciente.dataNascimento).getFullYear())
        : undefined;
      return api.analisarPrescricaoIa({
        atendimentoId: Number(atendimentoAtual!.id),
        nomePaciente: prontuario?.paciente.nome,
        idadeAnos,
        alergiasAtivas: alergiasAtivas.map((a) => a.substancia),
        prescricoesVigentes: (prontuario?.prescricoes ?? []).flatMap((p) =>
          p.itens.map((i) => i.medicamentoNome ?? `Med #${i.medicamentoId}`)
        ),
        itensSolicitados: itens
          .filter((i) => i.medicamentoId)
          .map((i) => ({
            medicamento: medsMap.get(String(i.medicamentoId)) ?? `Med #${i.medicamentoId}`,
            dose: i.dose,
            via: i.via,
            frequencia: i.frequencia,
          })),
      });
    },
    onSuccess: (data) => {
      setAnaliseIa(data);
      setDecisaoRegistrada(null);
      setJustificativaIa('');
      showToast(
        data.modoContingenciaAtivo
          ? 'Análise concluída via Contingência Determinística (RN-4).'
          : `Análise consultiva concluída (${data.provedor} / ${data.modelo}).`,
        data.modoContingenciaAtivo ? 'warning' : 'info'
      );
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao consultar Copiloto IA.', 'error');
    },
  });

  const decisaoIaMutation = useMutation({
    mutationFn: (decisaoMedica: 'ACEITA' | 'MODIFICADA' | 'REJEITADA') =>
      api.registrarDecisaoIa(analiseIa!.inferenciaId, {
        decisaoMedica,
        justificativaHumana: justificativaIa.trim() || undefined,
      }),
    onSuccess: (_data, decisao) => {
      setDecisaoRegistrada(decisao);
      showToast(`Decisão HITL (${decisao}) registrada em trilha imutável (RN-2).`, 'success');
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao registrar decisão HITL.', 'error');
    },
  });

  const sumarioAltaIaMutation = useMutation({
    mutationFn: () => {
      const idadeAnos = prontuario?.paciente.dataNascimento
        ? Math.max(0, new Date().getFullYear() - new Date(prontuario.paciente.dataNascimento).getFullYear())
        : undefined;
      return api.gerarSumarioAltaIa({
        atendimentoId: Number(atendimentoAtual!.id),
        nomePaciente: prontuario?.paciente.nome,
        idadeAnos,
        queixaPrincipal: prontuario?.triagens[0]?.queixaPrincipal,
        evolucoes: (prontuario?.evolucoes ?? []).map((e) => e.texto),
        prescricoes: (prontuario?.prescricoes ?? []).flatMap((p) =>
          p.itens.map((i) => `${i.medicamentoNome ?? i.medicamentoId} ${i.dose} ${i.via} ${i.frequencia}`)
        ),
      });
    },
    onSuccess: (data) => {
      setMotivoAlta(data.minuta);
      showToast(
        data.modoContingenciaAtivo
          ? 'Minuta gerada em modo determinístico. Revise antes de confirmar a alta.'
          : `Minuta de alta gerada (${data.provedor}). Revisão médica obrigatória.`,
        'info'
      );
    },
    onError: (err) => {
      showToast(err instanceof ApiError ? err.message : 'Erro ao gerar minuta de alta.', 'error');
    },
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-200 border-t-blue-700" />
      </div>
    );
  }

  if (error || !prontuario) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-sm text-red-800">
        Não foi possível carregar o prontuário eletrônico deste paciente.
      </div>
    );
  }

  const handleSubmitPrescricao = (e: React.FormEvent) => {
    e.preventDefault();
    if (itens.some((i) => !i.medicamentoId || !i.dose.trim() || !i.via.trim() || !i.frequencia.trim())) {
      showToast('Preencha medicamento, dose, via e frequência em todos os itens.', 'warning');
      return;
    }
    if (alergiasAtivas.length > 0) {
      setShowAlergiaModal(true);
      return;
    }
    prescricaoMutation.mutate(false);
  };

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <Link
            to="/pacientes"
            className="mb-2 inline-flex items-center gap-1.5 text-sm font-medium text-blue-700 hover:text-blue-900"
          >
            <ArrowLeft className="h-4 w-4" /> Voltar para Pacientes
          </Link>
          <h1 className="text-2xl font-bold text-slate-900">{prontuario.paciente.nome}</h1>
          <p className="mt-1 text-sm text-slate-600">
            CPF: <span className="font-mono">{formatCpf(prontuario.paciente.cpf)}</span>
            {prontuario.paciente.cns ? ` • CNS: ${prontuario.paciente.cns}` : ''} • Nasc.:{' '}
            {formatDate(prontuario.paciente.dataNascimento)} • Prontuário #{prontuario.prontuarioId}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {prontuario.atendimentos.length > 0 && (
            <select
              aria-label="Selecionar atendimento"
              value={String(atendimentoAtual?.id ?? '')}
              onChange={(e) => setSelectedAtendimentoId(e.target.value)}
              className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700"
            >
              {prontuario.atendimentos.map((at) => (
                <option key={at.id} value={String(at.id)}>
                  Atendimento #{at.id} — {at.tipo} ({formatStatusAtendimento(at.status)})
                </option>
              ))}
            </select>
          )}

          {canDischarge && atendimentoAtual && !atendimentoEncerrado && (
            <button
              type="button"
              onClick={() => setShowAltaModal(true)}
              className="inline-flex items-center gap-2 rounded-lg bg-slate-800 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-900"
            >
              <LogOut className="h-4 w-4" />
              Dar Alta Médica
            </button>
          )}
        </div>
      </div>

      {/* Faixa destacada de alergias ativas (RF-018) */}
      {alergiasAtivas.length > 0 && (
        <div className="rounded-xl border-2 border-red-300 bg-red-50 p-4">
          <div className="flex items-start gap-3">
            <ShieldAlert className="mt-0.5 h-6 w-6 shrink-0 text-red-600" />
            <div>
              <h2 className="text-sm font-bold uppercase tracking-wide text-red-800">
                Alerta de Alergias Ativas ({alergiasAtivas.length})
              </h2>
              <div className="mt-2 flex flex-wrap gap-2">
                {alergiasAtivas.map((al) => (
                  <span
                    key={al.id}
                    className="inline-flex items-center gap-1.5 rounded-full border border-red-300 bg-white px-3 py-1 text-xs font-semibold text-red-800"
                  >
                    <AlertTriangle className="h-3.5 w-3.5 text-red-600" />
                    {al.substancia} ({al.gravidade})
                    {al.reacao ? ` — ${al.reacao}` : ''}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Aviso de prontuário sem atendimento ativo / encerrado (RN-12) */}
      {!atendimentoAtual ? (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-900">
          <span>
            <strong>Nenhum atendimento aberto para este paciente.</strong> Abra um atendimento para registrar evoluções,
            prescrições e consultar o Copiloto IA.
          </span>
          <button
            type="button"
            disabled={abrirAtendimentoMutation.isPending}
            onClick={() => abrirAtendimentoMutation.mutate()}
            className="rounded-lg bg-blue-700 px-3.5 py-1.5 text-xs font-semibold text-white hover:bg-blue-800 disabled:opacity-50"
          >
            {abrirAtendimentoMutation.isPending ? 'Abrindo...' : 'Abrir Atendimento Agora'}
          </button>
        </div>
      ) : atendimentoEncerrado ? (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-slate-300 bg-slate-100 px-4 py-3 text-sm text-slate-700">
          <span>
            <strong>Atendimento #{atendimentoAtual.id} encerrado ({formatStatusAtendimento(atendimentoAtual.status)}):</strong>{' '}
            o prontuário deste episódio está bloqueado para novas evoluções e prescrições (RN-12).
            {atendimentoAtual.motivoEncerramento ? ` Motivo: ${atendimentoAtual.motivoEncerramento}` : ''}
          </span>
          <button
            type="button"
            disabled={abrirAtendimentoMutation.isPending}
            onClick={() => abrirAtendimentoMutation.mutate()}
            className="rounded-lg bg-blue-700 px-3.5 py-1.5 text-xs font-semibold text-white hover:bg-blue-800 disabled:opacity-50"
          >
            Novo Atendimento
          </button>
        </div>
      ) : null}

      {/* Abas */}
      <div className="flex flex-wrap gap-2 border-b border-slate-200 pb-2">
        {[
          { id: 'timeline' as Tab, label: 'Linha do Tempo', icon: FileText },
          { id: 'evolucao' as Tab, label: 'Nova Evolução', icon: ClipboardPlus },
          { id: 'alergias' as Tab, label: `Alergias (${alergiasAtivas.length})`, icon: ShieldAlert },
          { id: 'prescricao' as Tab, label: 'Nova Prescrição', icon: Pill },
        ].map((item) => {
          const Icon = item.icon;
          const active = tab === item.id;
          return (
            <button
              key={item.id}
              type="button"
              onClick={() => setTab(item.id)}
              className={`inline-flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-semibold transition ${
                active
                  ? 'bg-blue-700 text-white'
                  : 'bg-white text-slate-700 hover:bg-slate-100 border border-slate-200'
              }`}
            >
              <Icon className="h-4 w-4" />
              {item.label}
            </button>
          );
        })}
      </div>

      {/* Aba: Linha do Tempo */}
      {tab === 'timeline' && (
        <div className="grid gap-6 lg:grid-cols-2">
          <section className="space-y-4">
            <h3 className="text-base font-bold text-slate-900">Evoluções Clínicas ({prontuario.evolucoes.length})</h3>
            {prontuario.evolucoes.length === 0 ? (
              <p className="rounded-xl border border-dashed border-slate-300 bg-white p-6 text-sm text-slate-500">
                Nenhuma evolução clínica registrada ainda.
              </p>
            ) : (
              prontuario.evolucoes.map((ev) => (
                <article key={ev.id} className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
                  <div className="flex items-center justify-between text-xs text-slate-500">
                    <span className="font-semibold text-slate-700">
                      {ev.autorNome ?? `Profissional #${ev.autorId}`} • Atendimento #{ev.atendimentoId}
                    </span>
                    <span>{formatDateTime(ev.criadoEm ?? (ev as unknown as { criadaEm?: string }).criadaEm)}</span>
                  </div>
                  <p className="mt-2 whitespace-pre-wrap text-sm text-slate-800">{ev.texto}</p>
                </article>
              ))
            )}
          </section>

          <section className="space-y-6">
            <div>
              <h3 className="mb-3 text-base font-bold text-slate-900">
                Triagens ({prontuario.triagens.length})
              </h3>
              <div className="space-y-3">
                {prontuario.triagens.length === 0 ? (
                  <p className="rounded-xl border border-dashed border-slate-300 bg-white p-5 text-sm text-slate-500">
                    Sem triagens registradas.
                  </p>
                ) : (
                  prontuario.triagens.map((t) => {
                    const meta = t.prioridade ? MANCHESTER_META[t.prioridade] : null;
                    return (
                      <div key={t.id} className="rounded-xl border border-slate-200 bg-white p-4">
                        <div className="flex flex-wrap items-center justify-between gap-2">
                          {meta && (
                            <span className={`rounded-full border px-2.5 py-0.5 text-xs font-bold ${meta.badgeClass}`}>
                              {meta.label}
                            </span>
                          )}
                          <span className="text-xs text-slate-500">{formatDateTime(t.realizadaEm)}</span>
                        </div>
                        <p className="mt-2 text-sm font-medium text-slate-800">{t.queixaPrincipal}</p>
                        <div className="mt-2 flex flex-wrap gap-2 text-xs text-slate-600">
                          <span className="rounded bg-slate-100 px-2 py-0.5 font-semibold">qSOFA: {t.escoreQsofa ?? 0}</span>
                          <span className="rounded bg-slate-100 px-2 py-0.5 font-semibold">NEWS2: {t.escoreNews2 ?? 0}</span>
                          {t.alertaSepse && (
                            <span className="rounded bg-red-100 px-2 py-0.5 font-bold text-red-700">
                              ALERTA SEPSE
                            </span>
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div>
              <h3 className="mb-3 text-base font-bold text-slate-900">
                Prescrições Médicas ({prontuario.prescricoes.length})
              </h3>
              <div className="space-y-3">
                {prontuario.prescricoes.length === 0 ? (
                  <p className="rounded-xl border border-dashed border-slate-300 bg-white p-5 text-sm text-slate-500">
                    Sem prescrições registradas.
                  </p>
                ) : (
                  prontuario.prescricoes.map((pr) => (
                    <div key={pr.id} className="rounded-xl border border-slate-200 bg-white p-4">
                      <div className="flex items-center justify-between text-xs text-slate-500">
                        <span className="font-semibold text-slate-700">
                          Prescrição #{pr.id} • Atendimento #{pr.atendimentoId}
                        </span>
                        <span>{formatDateTime(pr.criadoEm ?? (pr as unknown as { criadaEm?: string }).criadaEm)}</span>
                      </div>
                      {pr.alergiasReconhecidas && (
                        <p className="mt-1 inline-flex items-center gap-1 text-xs font-medium text-emerald-700">
                          <CheckCircle2 className="h-3.5 w-3.5" /> Alergias explicitamente reconhecidas (RN-008)
                        </p>
                      )}
                      <ul className="mt-2 space-y-1.5 text-sm text-slate-800">
                        {pr.itens.map((item, idx) => (
                          <li key={item.id ?? idx} className="rounded-lg bg-slate-50 px-3 py-2">
                            <strong>{item.medicamentoNome ?? `Med #${item.medicamentoId}`}</strong> — {item.dose},{' '}
                            {item.via}, {item.frequencia}
                            {item.duracao ? ` (${item.duracao})` : ''} • Qtd: {item.quantidade}
                            {item.instrucoes ? <p className="text-xs text-slate-500">{item.instrucoes}</p> : null}
                          </li>
                        ))}
                      </ul>
                    </div>
                  ))
                )}
              </div>
            </div>
          </section>
        </div>
      )}

      {/* Aba: Nova Evolução */}
      {tab === 'evolucao' && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            evolucaoMutation.mutate();
          }}
          className="rounded-xl border border-slate-200 bg-white p-6 space-y-4"
        >
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-slate-900">Registrar Evolução Clínica (RF-010)</h3>
            <span className="text-xs font-medium text-slate-500">{evolucaoTexto.length} / 8000 caracteres</span>
          </div>
          <textarea
            rows={8}
            maxLength={8000}
            required
            disabled={atendimentoEncerrado || !canEditClinical}
            value={evolucaoTexto}
            onChange={(e) => setEvolucaoTexto(e.target.value)}
            placeholder="Descreva a evolução clínica, exame físico, condutas e resposta terapêutica..."
            className="w-full rounded-lg border border-slate-300 p-3 text-sm focus:border-blue-600 focus:outline-none disabled:bg-slate-100"
          />
          <button
            type="submit"
            disabled={atendimentoEncerrado || !canEditClinical || !evolucaoTexto.trim() || evolucaoMutation.isPending}
            className="rounded-lg bg-blue-700 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-800 disabled:opacity-50"
          >
            {evolucaoMutation.isPending ? 'Salvando...' : 'Salvar Evolução'}
          </button>
        </form>
      )}

      {/* Aba: Alergias */}
      {tab === 'alergias' && (
        <div className="grid gap-6 lg:grid-cols-2">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              alergiaMutation.mutate();
            }}
            className="rounded-xl border border-slate-200 bg-white p-6 space-y-4"
          >
            <h3 className="text-base font-bold text-slate-900">Cadastrar Alergia / Contraindicação</h3>
            <div>
              <label className="block text-sm font-medium text-slate-700">Substância *</label>
              <input
                type="text"
                required
                value={substancia}
                onChange={(e) => setSubstancia(e.target.value)}
                placeholder="Ex.: Penicilina, Dipirona, Látex"
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700">Gravidade</label>
              <select
                value={gravidade}
                onChange={(e) => setGravidade(e.target.value as GravidadeAlergia)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              >
                <option value="GRAVE">Grave</option>
                <option value="MODERADA">Moderada</option>
                <option value="LEVE">Leve</option>
                <option value="NAO_INFORMADA">Não informada</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700">Reação observada</label>
              <input
                type="text"
                value={reacao}
                onChange={(e) => setReacao(e.target.value)}
                placeholder="Ex.: Edema de glote, urticária"
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              />
            </div>
            <button
              type="submit"
              disabled={!canEditClinical || alergiaMutation.isPending}
              className="rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:opacity-50"
            >
              Registrar Alergia
            </button>
          </form>

          <div className="space-y-3">
            <h3 className="text-base font-bold text-slate-900">Histórico de Alergias</h3>
            {prontuario.alergias.map((al) => (
              <div
                key={al.id}
                className={`flex items-center justify-between rounded-xl border p-4 ${
                  al.ativa ? 'border-red-200 bg-red-50/50' : 'border-slate-200 bg-slate-50 opacity-70'
                }`}
              >
                <div>
                  <p className="font-semibold text-slate-900">
                    {al.substancia}{' '}
                    <span className="text-xs font-bold uppercase text-red-700">({al.gravidade})</span>
                  </p>
                  {al.reacao && <p className="text-xs text-slate-600">{al.reacao}</p>}
                  <p className="text-xs text-slate-500">{al.ativa ? 'Ativa' : 'Inativada'}</p>
                </div>
                {al.ativa && canPrescribe && (
                  <button
                    type="button"
                    onClick={() => inativarAlergiaMutation.mutate(String(al.id))}
                    className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-100"
                  >
                    Inativar
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Aba: Nova Prescrição */}
      {tab === 'prescricao' && (
        <form onSubmit={handleSubmitPrescricao} className="rounded-xl border border-slate-200 bg-white p-6 space-y-5">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-slate-900">Prescrição Médica Digital (RF-006 / RN-008)</h3>
            <button
              type="button"
              onClick={() =>
                setItens((prev) => [
                  ...prev,
                  { medicamentoId: '', dose: '', via: 'VO', frequencia: '8/8h', duracao: '', quantidade: 1, instrucoes: '' },
                ])
              }
              className="inline-flex items-center gap-1.5 rounded-lg border border-blue-200 bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-700 hover:bg-blue-100"
            >
              <Plus className="h-3.5 w-3.5" /> Adicionar Item
            </button>
          </div>

          {!canPrescribe && (
            <p className="rounded-lg bg-amber-50 p-3 text-sm text-amber-800">
              Apenas profissionais com perfil <strong>MÉDICO</strong> podem emitir prescrições.
            </p>
          )}

          <div className="space-y-4">
            {itens.map((item, idx) => (
              <div key={idx} className="grid gap-3 rounded-lg border border-slate-200 bg-slate-50 p-4 md:grid-cols-6">
                <div className="md:col-span-2">
                  <label className="block text-xs font-semibold text-slate-600">Medicamento *</label>
                  <select
                    required
                    value={item.medicamentoId}
                    onChange={(e) =>
                      setItens((prev) =>
                        prev.map((it, i) => (i === idx ? { ...it, medicamentoId: e.target.value } : it))
                      )
                    }
                    className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-2.5 py-2 text-sm"
                  >
                    <option value="">Selecione...</option>
                    {medicamentos.map((m) => (
                      <option key={m.id} value={String(m.id)}>
                        {m.nome} ({m.unidade})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600">Dose *</label>
                  <input
                    type="text"
                    required
                    value={item.dose}
                    onChange={(e) =>
                      setItens((prev) => prev.map((it, i) => (i === idx ? { ...it, dose: e.target.value } : it)))
                    }
                    className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-2.5 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600">Via *</label>
                  <input
                    type="text"
                    required
                    value={item.via}
                    onChange={(e) =>
                      setItens((prev) => prev.map((it, i) => (i === idx ? { ...it, via: e.target.value } : it)))
                    }
                    className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-2.5 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-600">Frequência *</label>
                  <input
                    type="text"
                    required
                    value={item.frequencia}
                    onChange={(e) =>
                      setItens((prev) =>
                        prev.map((it, i) => (i === idx ? { ...it, frequencia: e.target.value } : it))
                      )
                    }
                    className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-2.5 py-2 text-sm"
                  />
                </div>
                <div className="flex items-end gap-2">
                  <div className="flex-1">
                    <label className="block text-xs font-semibold text-slate-600">Qtd *</label>
                    <input
                      type="number"
                      min={1}
                      step="1"
                      required
                      value={item.quantidade}
                      onChange={(e) =>
                        setItens((prev) =>
                          prev.map((it, i) => (i === idx ? { ...it, quantidade: Number(e.target.value) } : it))
                        )
                      }
                      className="mt-1 w-full rounded-lg border border-slate-300 bg-white px-2.5 py-2 text-sm"
                    />
                  </div>
                  {itens.length > 1 && (
                    <button
                      type="button"
                      aria-label="Remover item"
                      onClick={() => setItens((prev) => prev.filter((_, i) => i !== idx))}
                      className="rounded-lg p-2 text-red-600 hover:bg-red-50"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              disabled={!canPrescribe || atendimentoEncerrado || itens.every((i) => !i.medicamentoId) || copilotoIaMutation.isPending}
              onClick={() => copilotoIaMutation.mutate()}
              className="rounded-lg border border-blue-300 bg-blue-50 px-4 py-2.5 text-sm font-semibold text-blue-800 hover:bg-blue-100 disabled:opacity-50"
            >
              {copilotoIaMutation.isPending ? 'Consultando Copiloto IA...' : 'Consultar Copiloto IA (RAG)'}
            </button>
            <button
              type="submit"
              disabled={!canPrescribe || atendimentoEncerrado || prescricaoMutation.isPending}
              className="rounded-lg bg-blue-700 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-800 disabled:opacity-50"
            >
              {prescricaoMutation.isPending ? 'Registrando...' : 'Confirmar Prescrição'}
            </button>
          </div>

          {analiseIa && (
            <div className="rounded-xl border border-blue-200 bg-slate-50 p-4 space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-slate-900">Parecer Consultivo (Copiloto IA)</span>
                  <span className="rounded-full bg-slate-200 px-2.5 py-0.5 text-xs font-semibold text-slate-700">
                    Alerta: {analiseIa.nivelAlerta}
                  </span>
                </div>
                <span className="text-xs text-slate-500">
                  {analiseIa.provedor} • {analiseIa.modelo} • {analiseIa.latenciaMs}ms • CB: {analiseIa.circuitBreakerEstado}
                </span>
              </div>

              {analiseIa.modoContingenciaAtivo && (
                <div className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-xs font-semibold text-amber-900">
                  {analiseIa.mensagemContingencia}
                </div>
              )}

              {analiseIa.alertasDeterministicos.length > 0 && (
                <ul className="space-y-1.5">
                  {analiseIa.alertasDeterministicos.map((al, idx) => (
                    <li key={idx} className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-900">
                      <strong>[{al.gravidade}]</strong> {al.mensagem}{' '}
                      <span className="text-red-700">({al.referencia})</span>
                    </li>
                  ))}
                </ul>
              )}

              <p className="whitespace-pre-wrap text-sm text-slate-800">{analiseIa.parecerConsultivo}</p>

              {analiseIa.referenciasRag.length > 0 && (
                <div className="text-xs text-slate-600">
                  <strong>Fontes oficiais (RAG):</strong>{' '}
                  {analiseIa.referenciasRag.map((r) => `${r.documento} — ${r.secao}`).join(' | ')}
                </div>
              )}

              <div className="border-t border-slate-200 pt-3 space-y-2">
                <p className="text-xs font-semibold text-slate-700">
                  Deliberação Human-in-the-Loop (RN-2 — Auditoria Imutável #{analiseIa.inferenciaId}):
                </p>
                {decisaoRegistrada ? (
                  <p className="text-xs font-semibold text-emerald-700">
                    Decisão registrada: {decisaoRegistrada}
                  </p>
                ) : (
                  <>
                    <input
                      type="text"
                      value={justificativaIa}
                      onChange={(e) => setJustificativaIa(e.target.value)}
                      placeholder="Justificativa clínica (obrigatória com ≥10 caracteres se Modificar ou Rejeitar)"
                      className="w-full rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs"
                    />
                    <div className="flex flex-wrap gap-2">
                      <button
                        type="button"
                        onClick={() => decisaoIaMutation.mutate('ACEITA')}
                        className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700"
                      >
                        Aceitar Recomendação
                      </button>
                      <button
                        type="button"
                        onClick={() => decisaoIaMutation.mutate('MODIFICADA')}
                        className="rounded-lg bg-amber-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-amber-700"
                      >
                        Modificar Conduta
                      </button>
                      <button
                        type="button"
                        onClick={() => decisaoIaMutation.mutate('REJEITADA')}
                        className="rounded-lg bg-red-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-700"
                      >
                        Rejeitar Alerta
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          )}
        </form>
      )}

      {/* Modal obrigatório RN-008: Reconhecimento explícito de alergias */}
      <ConfirmModal
        open={showAlergiaModal}
        title="RN-008: Confirmar Reconhecimento de Alergias"
        description={`Este paciente possui alergias ativas (${alergiasAtivas
          .map((a) => `${a.substancia} [${a.gravidade}]`)
          .join(', ')}). Confirma que revisou os itens prescritos e assume a responsabilidade clínica?`}
        confirmLabel="Reconhecer Alergias e Prescrever"
        variant="danger"
        loading={prescricaoMutation.isPending}
        onCancel={() => setShowAlergiaModal(false)}
        onConfirm={() => prescricaoMutation.mutate(true)}
      />

      {/* Modal RF-012: Alta médica */}
      <ConfirmModal
        open={showAltaModal}
        title="Confirmar Alta Hospitalar (RF-012)"
        description="A alta médica encerrará o atendimento, bloqueará novas alterações no prontuário (RN-12) e colocará automaticamente o leito ocupado em status EM_HIGIENIZACAO."
        confirmLabel="Confirmar Alta Médica"
        variant="danger"
        loading={altaMutation.isPending}
        onCancel={() => setShowAltaModal(false)}
        onConfirm={() => altaMutation.mutate()}
      >
        <div className="mt-3 space-y-2">
          <div className="flex items-center justify-between">
            <label className="block text-xs font-semibold text-slate-700">Motivo / Sumário de Alta</label>
            <button
              type="button"
              disabled={sumarioAltaIaMutation.isPending}
              onClick={() => sumarioAltaIaMutation.mutate()}
              className="rounded-md border border-blue-300 bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-800 hover:bg-blue-100 disabled:opacity-50"
            >
              {sumarioAltaIaMutation.isPending ? 'Gerando...' : 'Gerar Minuta com IA (RF-011)'}
            </button>
          </div>
          <textarea
            rows={4}
            value={motivoAlta}
            onChange={(e) => setMotivoAlta(e.target.value)}
            className="w-full rounded-lg border border-slate-300 p-2 text-sm"
          />
        </div>
      </ConfirmModal>
    </div>
  );
}
