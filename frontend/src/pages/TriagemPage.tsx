import { useEffect, useMemo, useRef, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Activity,
  AlertOctagon,
  AlertTriangle,
  Clock,
  FileText,
  RefreshCw,
  Siren,
  Stethoscope,
  Volume2,
  VolumeX,
  X,
} from 'lucide-react';
import { api, ApiError } from '../api/client';
import { useToast } from '../context/ToastContext';
import type { FilaAtendimentoItem, TriagemInput } from '../types/api';
import {
  calculateClinicalPreview,
  formatCpf,
  formatDateTime,
  formatStatusAtendimento,
  MANCHESTER_META,
} from '../utils/clinical';
import { playEmergencyAlertSound } from '../utils/sound';

const INITIAL_TRIAGEM_FORM: TriagemInput = {
  queixaPrincipal: '',
  temperaturaC: undefined,
  frequenciaCardiaca: undefined,
  frequenciaRespiratoria: undefined,
  pressaoSistolica: undefined,
  pressaoDiastolica: undefined,
  saturacaoO2: undefined,
  escalaDor: 0,
  escalaGlasgow: 15,
  oxigenioSuplementar: false,
  observacoes: '',
};

type FiltroFila = 'TODOS' | 'SEM_TRIAGEM' | 'CRITICOS' | 'EM_ATENDIMENTO';

export function TriagemPage() {
  const toast = useToast();
  const queryClient = useQueryClient();

  const [filtro, setFiltro] = useState<FiltroFila>('TODOS');
  const [soundEnabled, setSoundEnabled] = useState(false);
  const [atendimentoTriagem, setAtendimentoTriagem] =
    useState<FilaAtendimentoItem | null>(null);
  const [form, setForm] = useState<TriagemInput>(INITIAL_TRIAGEM_FORM);

  const seenRedIdsRef = useRef<Set<string>>(new Set());

  const filaQuery = useQuery({
    queryKey: ['fila-atendimentos'],
    queryFn: api.listarFilaAtendimentos,
    refetchInterval: 5000,
  });

  const fila = filaQuery.data ?? [];

  const pacientesVermelhos = useMemo(
    () => fila.filter((item) => item.prioridade === 'VERMELHA'),
    [fila]
  );

  useEffect(() => {
    const currentRedIds = new Set(pacientesVermelhos.map((p) => p.atendimentoId));
    let hasNewRed = false;
    for (const id of currentRedIds) {
      if (!seenRedIdsRef.current.has(id)) {
        hasNewRed = true;
      }
    }
    seenRedIdsRef.current = currentRedIds;

    if (hasNewRed && soundEnabled) {
      playEmergencyAlertSound();
    }
  }, [pacientesVermelhos, soundEnabled]);

  const filaFiltrada = useMemo(() => {
    return fila.filter((item) => {
      if (filtro === 'SEM_TRIAGEM') return !item.possuiTriagem;
      if (filtro === 'CRITICOS')
        return (
          item.prioridade === 'VERMELHA' ||
          item.prioridade === 'LARANJA' ||
          Boolean(item.alertaSepse)
        );
      if (filtro === 'EM_ATENDIMENTO') return item.status === 'EM_ATENDIMENTO';
      return true;
    });
  }, [fila, filtro]);

  const triagemMutation = useMutation({
    mutationFn: (vars: { atendimentoId: string; payload: TriagemInput }) =>
      api.registrarTriagem(vars.atendimentoId, vars.payload),
    onSuccess: () => {
      toast.success(
        'Triagem registrada',
        'Classificação de risco e escores clínicos atualizados na fila.'
      );
      setAtendimentoTriagem(null);
      setForm(INITIAL_TRIAGEM_FORM);
      void queryClient.invalidateQueries({ queryKey: ['fila-atendimentos'] });
    },
    onError: (err) => {
      toast.error(
        'Erro ao salvar triagem',
        err instanceof ApiError ? err.message : 'Verifique os dados clínicos.'
      );
    },
  });

  const assumirMutation = useMutation({
    mutationFn: api.assumirAtendimento,
    onSuccess: () => {
      toast.success('Atendimento assumido', 'O paciente agora está em atendimento.');
      void queryClient.invalidateQueries({ queryKey: ['fila-atendimentos'] });
    },
    onError: (err) => {
      toast.error(
        'Não foi possível assumir',
        err instanceof ApiError ? err.message : 'Verifique suas permissões.'
      );
    },
  });

  const preview = useMemo(() => calculateClinicalPreview(form), [form]);
  const previewMeta = MANCHESTER_META[preview.prioridade];

  const handleNumberField = (field: keyof TriagemInput, raw: string) => {
    const num = raw === '' ? undefined : Number(raw);
    setForm((prev) => ({
      ...prev,
      [field]: num !== undefined && Number.isNaN(num) ? undefined : num,
    }));
  };

  const handleSubmitTriagem = (e: FormEvent) => {
    e.preventDefault();
    if (!atendimentoTriagem) return;
    if (!form.queixaPrincipal.trim()) {
      toast.warning('Informe a queixa principal');
      return;
    }
    triagemMutation.mutate({
      atendimentoId: atendimentoTriagem.atendimentoId,
      payload: {
        ...form,
        queixaPrincipal: form.queixaPrincipal.trim(),
        observacoes: form.observacoes?.trim() || undefined,
      },
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Fila de Triagem & Emergência
          </h1>
          <p className="mt-1 flex items-center gap-2 text-sm text-slate-500">
            <span className="inline-block h-2 w-2 rounded-full bg-emerald-500" />
            Atualização automática a cada 5 segundos · Protocolo de Manchester + qSOFA/NEWS2
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={() => {
              const next = !soundEnabled;
              setSoundEnabled(next);
              if (next) {
                playEmergencyAlertSound();
                toast.info(
                  'Alerta sonoro ativado',
                  'Um aviso tocará automaticamente para emergências (Vermelha).'
                );
              }
            }}
            className={`inline-flex min-h-11 items-center gap-2 rounded-xl border px-4 text-xs font-semibold transition ${
              soundEnabled
                ? 'border-red-300 bg-red-50 text-red-800 hover:bg-red-100'
                : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'
            }`}
          >
            {soundEnabled ? (
              <>
                <Volume2 className="h-4 w-4 text-red-600" />
                Som de Emergência: ON
              </>
            ) : (
              <>
                <VolumeX className="h-4 w-4 text-slate-400" />
                Ativar Som de Emergência
              </>
            )}
          </button>

          <button
            type="button"
            onClick={() => void filaQuery.refetch()}
            disabled={filaQuery.isFetching}
            className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
          >
            <RefreshCw
              className={`h-4 w-4 ${filaQuery.isFetching ? 'animate-spin text-blue-600' : ''}`}
            />
            Atualizar
          </button>
        </div>
      </div>

      {/* RN-001 / RF-021 Banner for VERMELHA priority */}
      {pacientesVermelhos.length > 0 && (
        <div
          role="alert"
          className="animate-emergency-ring flex flex-col gap-3 rounded-2xl border-2 border-red-600 bg-red-600 p-4 text-white shadow-lg sm:flex-row sm:items-center sm:justify-between"
        >
          <div className="flex items-start gap-3">
            <div className="rounded-xl bg-white/15 p-2.5">
              <Siren className="h-6 w-6 animate-pulse text-white" />
            </div>
            <div>
              <p className="text-base font-bold uppercase tracking-wide">
                Alerta de Emergência Imediata ({pacientesVermelhos.length})
              </p>
              <p className="mt-0.5 text-sm text-red-100">
                Paciente(s) classificado(s) em prioridade VERMELHA:{' '}
                <strong className="text-white">
                  {pacientesVermelhos.map((p) => p.pacienteNome).join(', ')}
                </strong>
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => playEmergencyAlertSound()}
            className="min-h-10 shrink-0 rounded-xl bg-white/15 px-4 text-xs font-semibold text-white transition hover:bg-white/25"
          >
            Reproduzir aviso sonoro
          </button>
        </div>
      )}

      {/* Quick Filters */}
      <div className="flex flex-wrap gap-2">
        {(
          [
            { id: 'TODOS', label: `Todos (${fila.length})` },
            {
              id: 'SEM_TRIAGEM',
              label: `Aguardando Triagem (${fila.filter((i) => !i.possuiTriagem).length})`,
            },
            {
              id: 'CRITICOS',
              label: `Críticos / Sepse (${
                fila.filter(
                  (i) =>
                    i.prioridade === 'VERMELHA' ||
                    i.prioridade === 'LARANJA' ||
                    Boolean(i.alertaSepse)
                ).length
              })`,
            },
            {
              id: 'EM_ATENDIMENTO',
              label: `Em Atendimento (${
                fila.filter((i) => i.status === 'EM_ATENDIMENTO').length
              })`,
            },
          ] as const
        ).map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setFiltro(tab.id)}
            className={`min-h-10 rounded-xl px-4 text-xs font-semibold transition ${
              filtro === tab.id
                ? 'bg-blue-700 text-white shadow-xs'
                : 'border border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Queue Cards */}
      {filaQuery.isLoading ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center text-sm text-slate-500">
          Carregando fila de atendimento...
        </div>
      ) : filaQuery.isError ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 p-8 text-center text-sm text-red-700">
          Não foi possível carregar a fila de triagem.
        </div>
      ) : filaFiltrada.length === 0 ? (
        <div className="flex flex-col items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-white p-12 text-center">
          <Activity className="h-10 w-10 text-slate-300" />
          <p className="text-sm font-semibold text-slate-700">
            Nenhum atendimento neste filtro
          </p>
          <p className="text-xs text-slate-500">
            Novos atendimentos abertos na recepção aparecerão automaticamente aqui.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {filaFiltrada.map((item) => {
            const meta = item.prioridade ? MANCHESTER_META[item.prioridade] : null;
            return (
              <div
                key={item.atendimentoId}
                className={`rounded-2xl border border-slate-200 border-l-4 bg-white p-4 shadow-2xs transition hover:shadow-sm sm:p-5 ${
                  meta ? meta.borderClass : 'border-l-slate-300'
                }`}
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                  <div className="min-w-0 space-y-2">
                    <div className="flex flex-wrap items-center gap-2">
                      {meta ? (
                        <span
                          className={`inline-flex items-center rounded-lg border px-2.5 py-1 text-xs font-bold ${meta.badgeClass}`}
                        >
                          {meta.label}
                        </span>
                      ) : (
                        <span className="inline-flex items-center rounded-lg border border-slate-300 bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">
                          Sem triagem
                        </span>
                      )}

                      <span className="rounded-lg bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">
                        {formatStatusAtendimento(item.status)}
                      </span>

                      <span className="rounded-lg bg-sky-50 px-2.5 py-1 text-xs font-medium text-sky-800">
                        {item.tipo}
                      </span>

                      {item.alertaSepse && (
                        <span className="inline-flex items-center gap-1 rounded-lg bg-red-100 px-2.5 py-1 text-xs font-bold text-red-800 border border-red-200">
                          <AlertOctagon className="h-3.5 w-3.5" />
                          ALERTA SEPSE
                        </span>
                      )}
                    </div>

                    <div>
                      <h2 className="text-base font-bold text-slate-900">
                        {item.pacienteNome}{' '}
                        <span className="text-xs font-normal text-slate-500">
                          · CPF {formatCpf(item.pacienteCpf)}
                        </span>
                      </h2>
                      {item.queixaPrincipal && (
                        <p className="mt-1 text-sm text-slate-700">
                          <span className="font-medium text-slate-500">Queixa:</span>{' '}
                          {item.queixaPrincipal}
                        </p>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
                      <span className="inline-flex items-center gap-1">
                        <Clock className="h-3.5 w-3.5" />
                        Entrada: {formatDateTime(item.iniciadoEm)}
                      </span>
                      {item.escoreQsofa != null && (
                        <span className="font-semibold text-slate-700">
                          qSOFA: {item.escoreQsofa}/3
                        </span>
                      )}
                      {item.escoreNews2 != null && (
                        <span className="font-semibold text-slate-700">
                          NEWS2: {item.escoreNews2}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        setForm({
                          ...INITIAL_TRIAGEM_FORM,
                          queixaPrincipal: item.queixaPrincipal ?? '',
                        });
                        setAtendimentoTriagem(item);
                      }}
                      className="inline-flex min-h-11 items-center gap-2 rounded-xl bg-blue-700 px-4 py-2 text-xs font-semibold text-white transition hover:bg-blue-800"
                    >
                      <Activity className="h-4 w-4" />
                      {item.possuiTriagem ? 'Reavaliar Triagem' : 'Realizar Triagem'}
                    </button>

                    {item.status !== 'EM_ATENDIMENTO' && item.status !== 'ALTA' && (
                      <button
                        type="button"
                        disabled={assumirMutation.isPending}
                        onClick={() => assumirMutation.mutate(item.atendimentoId)}
                        className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-emerald-300 bg-emerald-50 px-4 py-2 text-xs font-semibold text-emerald-800 transition hover:bg-emerald-100 disabled:opacity-50"
                      >
                        <Stethoscope className="h-4 w-4" />
                        Assumir
                      </button>
                    )}

                    <Link
                      to={`/pacientes/${item.pacienteId}/prontuario`}
                      className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
                    >
                      <FileText className="h-4 w-4" />
                      Prontuário
                    </Link>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modal: Formulário de Triagem com Preview Instantâneo */}
      {atendimentoTriagem && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-triagem-title"
        >
          <div className="flex max-h-[92vh] w-full max-w-3xl flex-col rounded-2xl border border-slate-200 bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
              <div>
                <h2 id="modal-triagem-title" className="text-lg font-bold text-slate-900">
                  Triagem Clínica — Protocolo de Manchester
                </h2>
                <p className="text-xs text-slate-500">
                  Paciente: <strong>{atendimentoTriagem.pacienteNome}</strong>
                </p>
              </div>
              <button
                type="button"
                onClick={() => setAtendimentoTriagem(null)}
                aria-label="Fechar"
                className="rounded-lg p-2 text-slate-400 hover:bg-slate-100"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <form
              onSubmit={handleSubmitTriagem}
              className="flex-1 space-y-5 overflow-y-auto p-6"
            >
              {/* Instant Preview Card */}
              <div
                className={`rounded-2xl border p-4 transition-colors ${previewMeta.bgSoftClass}`}
              >
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-600">
                      Prévia instantânea da classificação
                    </p>
                    <div className="mt-1 flex flex-wrap items-center gap-2">
                      <span
                        className={`inline-flex rounded-lg border px-3 py-1 text-xs font-bold ${previewMeta.badgeClass}`}
                      >
                        {previewMeta.label}
                      </span>
                      <span className="text-xs font-medium text-slate-700">
                        {preview.tempoAlvo}
                      </span>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <div className="rounded-xl border border-slate-200/80 bg-white px-3 py-1.5 text-center">
                      <p className="text-[10px] font-semibold uppercase text-slate-500">
                        qSOFA
                      </p>
                      <p
                        className={`text-base font-bold ${
                          preview.qsofa >= 2 ? 'text-red-600' : 'text-slate-900'
                        }`}
                      >
                        {preview.qsofa}/3
                      </p>
                    </div>
                    <div className="rounded-xl border border-slate-200/80 bg-white px-3 py-1.5 text-center">
                      <p className="text-[10px] font-semibold uppercase text-slate-500">
                        NEWS2
                      </p>
                      <p
                        className={`text-base font-bold ${
                          preview.news2 >= 5 ? 'text-red-600' : 'text-slate-900'
                        }`}
                      >
                        {preview.news2}
                      </p>
                    </div>
                    {preview.alertaSepse && (
                      <div className="inline-flex items-center gap-1.5 rounded-xl bg-red-600 px-3 py-2 text-xs font-bold text-white">
                        <AlertTriangle className="h-4 w-4" />
                        Suspeita de Sepse
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Queixa principal */}
              <div>
                <label
                  htmlFor="tri-queixa"
                  className="block text-sm font-medium text-slate-700"
                >
                  Queixa principal *
                </label>
                <input
                  id="tri-queixa"
                  type="text"
                  required
                  value={form.queixaPrincipal}
                  onChange={(e) =>
                    setForm((prev) => ({ ...prev, queixaPrincipal: e.target.value }))
                  }
                  placeholder="Ex: Dor torácica irradiada há 30 minutos, dispneia..."
                  className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                />
              </div>

              {/* Sinais Vitais Grid */}
              <div className="grid grid-cols-2 gap-3.5 sm:grid-cols-3">
                <div>
                  <label htmlFor="tri-temp" className="block text-xs font-medium text-slate-700">
                    Temperatura (°C)
                  </label>
                  <input
                    id="tri-temp"
                    type="number"
                    step="0.1"
                    min="30"
                    max="45"
                    value={form.temperaturaC ?? ''}
                    onChange={(e) => handleNumberField('temperaturaC', e.target.value)}
                    placeholder="36.8"
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="tri-fc" className="block text-xs font-medium text-slate-700">
                    Freq. Cardíaca (bpm)
                  </label>
                  <input
                    id="tri-fc"
                    type="number"
                    min="20"
                    max="260"
                    value={form.frequenciaCardiaca ?? ''}
                    onChange={(e) => handleNumberField('frequenciaCardiaca', e.target.value)}
                    placeholder="82"
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="tri-fr" className="block text-xs font-medium text-slate-700">
                    Freq. Respiratória (irpm)
                  </label>
                  <input
                    id="tri-fr"
                    type="number"
                    min="4"
                    max="80"
                    value={form.frequenciaRespiratoria ?? ''}
                    onChange={(e) =>
                      handleNumberField('frequenciaRespiratoria', e.target.value)
                    }
                    placeholder="16"
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="tri-pas" className="block text-xs font-medium text-slate-700">
                    PA Sistólica (mmHg)
                  </label>
                  <input
                    id="tri-pas"
                    type="number"
                    min="40"
                    max="300"
                    value={form.pressaoSistolica ?? ''}
                    onChange={(e) => handleNumberField('pressaoSistolica', e.target.value)}
                    placeholder="120"
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="tri-pad" className="block text-xs font-medium text-slate-700">
                    PA Diastólica (mmHg)
                  </label>
                  <input
                    id="tri-pad"
                    type="number"
                    min="20"
                    max="200"
                    value={form.pressaoDiastolica ?? ''}
                    onChange={(e) => handleNumberField('pressaoDiastolica', e.target.value)}
                    placeholder="80"
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="tri-spo2" className="block text-xs font-medium text-slate-700">
                    Saturação O₂ (%)
                  </label>
                  <input
                    id="tri-spo2"
                    type="number"
                    min="50"
                    max="100"
                    value={form.saturacaoO2 ?? ''}
                    onChange={(e) => handleNumberField('saturacaoO2', e.target.value)}
                    placeholder="98"
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div>
                  <label htmlFor="tri-dor" className="block text-xs font-medium text-slate-700">
                    Escala de Dor (0-10)
                  </label>
                  <input
                    id="tri-dor"
                    type="number"
                    min="0"
                    max="10"
                    value={form.escalaDor ?? ''}
                    onChange={(e) => handleNumberField('escalaDor', e.target.value)}
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div>
                  <label
                    htmlFor="tri-glasgow"
                    className="block text-xs font-medium text-slate-700"
                  >
                    Escala de Glasgow (3-15)
                  </label>
                  <input
                    id="tri-glasgow"
                    type="number"
                    min="3"
                    max="15"
                    value={form.escalaGlasgow ?? ''}
                    onChange={(e) => handleNumberField('escalaGlasgow', e.target.value)}
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3 text-sm focus:border-blue-600 focus:outline-none"
                  />
                </div>

                <div className="flex items-end pb-1">
                  <label className="flex min-h-11 w-full cursor-pointer items-center gap-2.5 rounded-xl border border-slate-200 bg-slate-50 px-3 text-xs font-medium text-slate-700">
                    <input
                      type="checkbox"
                      checked={Boolean(form.oxigenioSuplementar)}
                      onChange={(e) =>
                        setForm((prev) => ({
                          ...prev,
                          oxigenioSuplementar: e.target.checked,
                        }))
                      }
                      className="h-4 w-4 rounded border-slate-300 text-blue-700"
                    />
                    Oxigênio suplementar
                  </label>
                </div>
              </div>

              {/* Observações */}
              <div>
                <label htmlFor="tri-obs" className="block text-sm font-medium text-slate-700">
                  Observações de enfermagem
                </label>
                <textarea
                  id="tri-obs"
                  rows={2}
                  value={form.observacoes ?? ''}
                  onChange={(e) =>
                    setForm((prev) => ({ ...prev, observacoes: e.target.value }))
                  }
                  placeholder="Antecedentes relevantes, estado geral, uso de medicações prévias..."
                  className="mt-1 w-full rounded-xl border border-slate-300 p-3 text-sm focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                />
              </div>

              <div className="flex justify-end gap-2.5 border-t border-slate-100 pt-4">
                <button
                  type="button"
                  onClick={() => setAtendimentoTriagem(null)}
                  className="min-h-11 rounded-xl border border-slate-300 bg-white px-4 text-sm font-medium text-slate-700"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={triagemMutation.isPending}
                  className="min-h-11 rounded-xl bg-blue-700 px-6 text-sm font-semibold text-white hover:bg-blue-800 disabled:opacity-60"
                >
                  {triagemMutation.isPending ? 'Registrando...' : 'Salvar Triagem'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
