import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  ArrowRightLeft,
  BedDouble,
  History,
  Plus,
  RefreshCw,
  Sparkles,
  UserPlus,
  X,
} from 'lucide-react';
import { api, ApiError } from '../api/client';
import { ConfirmModal } from '../components/ConfirmModal';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import type { Leito, StatusLeito } from '../types/api';
import { formatDateTime, getLeitoStatusMeta, hasAnyRole } from '../utils/clinical';

export function LeitosPage() {
  const { user } = useAuth();
  const { notify } = useToast();
  const showToast = (msg: string, variant: 'success' | 'error' | 'warning' | 'info' = 'info') =>
    notify(variant, msg);
  const queryClient = useQueryClient();

  const [statusFilter, setStatusFilter] = useState<string>('');
  const [showNovoModal, setShowNovoModal] = useState(false);
  const [novoCodigo, setNovoCodigo] = useState('');
  const [novaAla, setNovaAla] = useState('Clinica Medica');

  const [alocarLeitoTarget, setAlocarLeitoTarget] = useState<Leito | null>(null);
  const [atendimentoSelecionado, setAtendimentoSelecionado] = useState('');

  const [transferirLeitoTarget, setTransferirLeitoTarget] = useState<Leito | null>(null);
  const [leitoDestinoId, setLeitoDestinoId] = useState('');

  const [higienizarTarget, setHigienizarTarget] = useState<Leito | null>(null);
  const [historicoTarget, setHistoricoTarget] = useState<Leito | null>(null);

  const canManageLeitos = hasAnyRole(user?.roles, ['GESTOR_LEITOS']);
  const canAllocate = hasAnyRole(user?.roles, ['GESTOR_LEITOS', 'MEDICO', 'ENFERMEIRO']);

  const { data: leitos = [], isLoading, isFetching } = useQuery({
    queryKey: ['leitos', statusFilter],
    queryFn: () => api.listarLeitos(statusFilter ? { status: statusFilter } : undefined),
    refetchInterval: 5000,
  });

  const { data: fila = [] } = useQuery({
    queryKey: ['fila-atendimentos'],
    queryFn: () => api.listarFilaAtendimentos(),
    refetchInterval: 5000,
  });

  const { data: historico = [], isLoading: loadingHistorico } = useQuery({
    queryKey: ['leito-historico', historicoTarget?.id],
    queryFn: () => api.historicoLeito(String(historicoTarget!.id)),
    enabled: Boolean(historicoTarget),
  });

  const leitosPorAla = useMemo(() => {
    const groups = new Map<string, Leito[]>();
    for (const l of leitos) {
      const list = groups.get(l.ala) ?? [];
      list.push(l);
      groups.set(l.ala, list);
    }
    return Array.from(groups.entries());
  }, [leitos]);

  const leitosLivres = useMemo(() => leitos.filter((l) => l.status === 'LIVRE'), [leitos]);

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['leitos'] });
    queryClient.invalidateQueries({ queryKey: ['leito-historico'] });
  };

  const criarLeitoMutation = useMutation({
    mutationFn: () => api.criarLeito({ codigo: novoCodigo.trim().toUpperCase(), ala: novaAla.trim() }),
    onSuccess: () => {
      setShowNovoModal(false);
      setNovoCodigo('');
      showToast('Leito cadastrado com sucesso.', 'success');
      invalidate();
    },
    onError: (err) => showToast(err instanceof ApiError ? err.message : 'Erro ao cadastrar leito.', 'error'),
  });

  const alocarMutation = useMutation({
    mutationFn: () =>
      api.alocarLeito({
        leitoId: String(alocarLeitoTarget!.id),
        atendimentoId: atendimentoSelecionado,
        status: 'OCUPADO',
      }),
    onSuccess: () => {
      setAlocarLeitoTarget(null);
      setAtendimentoSelecionado('');
      showToast('Paciente alocado ao leito com sucesso.', 'success');
      invalidate();
    },
    onError: (err) => showToast(err instanceof ApiError ? err.message : 'Erro ao alocar leito.', 'error'),
  });

  const transferirMutation = useMutation({
    mutationFn: () =>
      api.transferirLeito(String(transferirLeitoTarget!.alocacaoAtual!.id), leitoDestinoId),
    onSuccess: () => {
      setTransferirLeitoTarget(null);
      setLeitoDestinoId('');
      showToast('Transferência realizada (RF-019). Leito de origem enviado para higienização.', 'success');
      invalidate();
    },
    onError: (err) => showToast(err instanceof ApiError ? err.message : 'Erro na transferência.', 'error'),
  });

  const higienizarMutation = useMutation({
    mutationFn: () => api.higienizarLeito(String(higienizarTarget!.id)),
    onSuccess: () => {
      setHigienizarTarget(null);
      showToast('Higienização confirmada (RN-010). Leito liberado para novas internações.', 'success');
      invalidate();
    },
    onError: (err) => showToast(err instanceof ApiError ? err.message : 'Erro ao liberar leito.', 'error'),
  });

  const statusOptions: { value: string; label: string }[] = [
    { value: '', label: 'Todos os status' },
    { value: 'LIVRE', label: 'Livre' },
    { value: 'OCUPADO', label: 'Ocupado' },
    { value: 'RESERVADO', label: 'Reservado' },
    { value: 'EM_HIGIENIZACAO', label: 'Em Higienização' },
    { value: 'MANUTENCAO', label: 'Manutenção' },
    { value: 'ISOLAMENTO', label: 'Isolamento' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Mapa de Leitos</h1>
          <p className="mt-1 flex items-center gap-2 text-sm text-slate-600">
            Atualização automática a cada 5s
            <RefreshCw className={`h-3.5 w-3.5 ${isFetching ? 'animate-spin text-blue-600' : 'text-slate-400'}`} />
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <select
            aria-label="Filtrar leitos por status"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700"
          >
            {statusOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>

          {canManageLeitos && (
            <button
              type="button"
              onClick={() => setShowNovoModal(true)}
              className="inline-flex items-center gap-2 rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
            >
              <Plus className="h-4 w-4" />
              Novo Leito
            </button>
          )}
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-200 border-t-blue-700" />
        </div>
      ) : (
        <div className="space-y-8">
          {leitosPorAla.map(([ala, items]) => (
            <section key={ala} className="space-y-3">
              <h2 className="text-sm font-bold uppercase tracking-wider text-slate-500">
                Ala: {ala} ({items.length})
              </h2>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {items.map((leito) => {
                  const meta = getLeitoStatusMeta(leito.status as StatusLeito);
                  const isHig = leito.status === 'EM_HIGIENIZACAO';
                  return (
                    <div
                      key={leito.id}
                      className={`overflow-hidden rounded-xl border transition shadow-xs ${meta.card}`}
                    >
                      <div className={`h-1.5 w-full ${meta.bar}`} />
                      <div className="p-4 space-y-3">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex items-center gap-2">
                            <BedDouble className="h-5 w-5 text-slate-600" />
                            <span className="text-lg font-bold text-slate-900">{leito.codigo}</span>
                          </div>
                          <span className={`rounded-full border px-2.5 py-0.5 text-xs font-semibold ${meta.badge}`}>
                            {meta.label}
                          </span>
                        </div>

                        {leito.alocacaoAtual ? (
                          <div className="rounded-lg bg-white/80 p-2.5 text-xs text-slate-700 border border-slate-200">
                            <p className="font-semibold">Atendimento #{leito.alocacaoAtual.atendimentoId}</p>
                            <p className="text-slate-500">Desde {formatDateTime(leito.alocacaoAtual.alocadoEm)}</p>
                          </div>
                        ) : (
                          <p className="text-xs text-slate-500">Sem paciente alocado</p>
                        )}

                        <div className="flex flex-wrap gap-2 pt-1">
                          {leito.status === 'LIVRE' && canAllocate && (
                            <button
                              type="button"
                              onClick={() => setAlocarLeitoTarget(leito)}
                              className="inline-flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-blue-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-blue-800"
                            >
                              <UserPlus className="h-3.5 w-3.5" /> Alocar
                            </button>
                          )}

                          {leito.alocacaoAtual && canAllocate && (
                            <button
                              type="button"
                              onClick={() => setTransferirLeitoTarget(leito)}
                              className="inline-flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-slate-800 px-3 py-1.5 text-xs font-semibold text-white hover:bg-slate-900"
                            >
                              <ArrowRightLeft className="h-3.5 w-3.5" /> Transferir
                            </button>
                          )}

                          {isHig && canManageLeitos && (
                            <button
                              type="button"
                              onClick={() => setHigienizarTarget(leito)}
                              className="inline-flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-amber-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-amber-700"
                            >
                              <Sparkles className="h-3.5 w-3.5" /> Liberar
                            </button>
                          )}

                          <button
                            type="button"
                            onClick={() => setHistoricoTarget(leito)}
                            className="inline-flex items-center justify-center gap-1 rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50"
                            title="Histórico de status"
                          >
                            <History className="h-3.5 w-3.5" />
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </section>
          ))}
        </div>
      )}

      {/* Modal: Novo Leito */}
      {showNovoModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              criarLeitoMutation.mutate();
            }}
            className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-900">Cadastrar Novo Leito</h3>
              <button type="button" onClick={() => setShowNovoModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700">Código *</label>
              <input
                type="text"
                required
                value={novoCodigo}
                onChange={(e) => setNovoCodigo(e.target.value)}
                placeholder="Ex.: UTI-04"
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700">Ala *</label>
              <input
                type="text"
                required
                value={novaAla}
                onChange={(e) => setNovaAla(e.target.value)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowNovoModal(false)}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={criarLeitoMutation.isPending}
                className="rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
              >
                Cadastrar
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Modal: Alocar Paciente (RN-004) */}
      {alocarLeitoTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              alocarMutation.mutate();
            }}
            className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-900">Alocar Leito {alocarLeitoTarget.codigo}</h3>
              <button type="button" onClick={() => setAlocarLeitoTarget(null)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" />
              </button>
            </div>
            <p className="text-xs text-slate-600">
              RN-004: Somente atendimentos que já passaram pela triagem podem ser internados.
            </p>
            <div>
              <label className="block text-sm font-medium text-slate-700">Atendimento ativo *</label>
              <select
                required
                value={atendimentoSelecionado}
                onChange={(e) => setAtendimentoSelecionado(e.target.value)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              >
                <option value="">Selecione um atendimento...</option>
                {fila.map((item) => (
                  <option key={item.atendimentoId} value={String(item.atendimentoId)}>
                    #{item.atendimentoId} — {item.pacienteNome}{' '}
                    {item.possuiTriagem ? `(${item.prioridade})` : '(SEM TRIAGEM)'}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setAlocarLeitoTarget(null)}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={!atendimentoSelecionado || alocarMutation.isPending}
                className="rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800 disabled:opacity-50"
              >
                Confirmar Alocação
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Modal: Transferir Leito (RF-019) */}
      {transferirLeitoTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              transferirMutation.mutate();
            }}
            className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl space-y-4"
          >
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-900">
                Transferir do Leito {transferirLeitoTarget.codigo}
              </h3>
              <button
                type="button"
                onClick={() => setTransferirLeitoTarget(null)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <p className="text-xs text-slate-600">
              RF-019: O leito de origem passará automaticamente para <strong>EM_HIGIENIZACAO</strong> e o destino para{' '}
              <strong>OCUPADO</strong>.
            </p>
            <div>
              <label className="block text-sm font-medium text-slate-700">Leito de Destino (Livre) *</label>
              <select
                required
                value={leitoDestinoId}
                onChange={(e) => setLeitoDestinoId(e.target.value)}
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              >
                <option value="">Selecione um leito livre...</option>
                {leitosLivres.map((l) => (
                  <option key={l.id} value={String(l.id)}>
                    {l.codigo} — {l.ala}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setTransferirLeitoTarget(null)}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={!leitoDestinoId || transferirMutation.isPending}
                className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:opacity-50"
              >
                Confirmar Transferência
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Modal: Confirmar Higienização (RN-010) */}
      <ConfirmModal
        open={Boolean(higienizarTarget)}
        title={`Confirmar Higienização — ${higienizarTarget?.codigo ?? ''}`}
        description="Confirma que a equipe de hotelaria concluiu a higienização deste leito? O status passará para LIVRE."
        confirmLabel="Confirmar e Liberar Leito"
        loading={higienizarMutation.isPending}
        onCancel={() => setHigienizarTarget(null)}
        onConfirm={() => higienizarMutation.mutate()}
      />

      {/* Modal: Histórico do Leito */}
      {historicoTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-lg rounded-xl bg-white p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-900">Histórico — Leito {historicoTarget.codigo}</h3>
              <button type="button" onClick={() => setHistoricoTarget(null)} className="text-slate-400 hover:text-slate-600">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="max-h-80 space-y-2 overflow-y-auto">
              {loadingHistorico ? (
                <p className="text-sm text-slate-500">Carregando histórico...</p>
              ) : historico.length === 0 ? (
                <p className="text-sm text-slate-500">Nenhum evento registrado.</p>
              ) : (
                historico.map((h) => (
                  <div key={h.id} className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs">
                    <div className="flex justify-between font-semibold text-slate-800">
                      <span>
                        {h.statusAnterior ?? 'INICIAL'} → {h.statusNovo}
                      </span>
                      <span className="text-slate-500">{formatDateTime(h.alteradoEm)}</span>
                    </div>
                    {h.atendimentoId && <p className="mt-1 text-slate-600">Atendimento #{h.atendimentoId}</p>}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
