import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  ClipboardPlus,
  FileText,
  Phone,
  Plus,
  Search,
  Star,
  Trash2,
  UserPlus,
  Users,
  X,
} from 'lucide-react';
import { api, ApiError } from '../api/client';
import { useToast } from '../context/ToastContext';
import type { Paciente, TipoAtendimento } from '../types/api';
import { formatCpf, formatDate } from '../utils/clinical';

export function PacientesPage() {
  const toast = useToast();
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const [busca, setBusca] = useState('');
  const [showNovoPaciente, setShowNovoPaciente] = useState(false);
  const [pacienteContatos, setPacienteContatos] = useState<Paciente | null>(null);
  const [pacienteAtendimento, setPacienteAtendimento] = useState<Paciente | null>(null);

  // Novo paciente form
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [cns, setCns] = useState('');
  const [dataNascimento, setDataNascimento] = useState('');

  // Novo contato form
  const [contatoNome, setContatoNome] = useState('');
  const [contatoParentesco, setContatoParentesco] = useState('');
  const [contatoTelefone, setContatoTelefone] = useState('');
  const [contatoPrincipal, setContatoPrincipal] = useState(true);

  // Abrir atendimento form
  const [tipoAtendimento, setTipoAtendimento] = useState<TipoAtendimento>('EMERGENCIA');

  const pacientesQuery = useQuery({
    queryKey: ['pacientes', busca],
    queryFn: () => api.listarPacientes(busca.trim()),
  });

  const contatosQuery = useQuery({
    queryKey: ['contatos-emergencia', pacienteContatos?.id],
    queryFn: () => api.listarContatosEmergencia(pacienteContatos!.id),
    enabled: Boolean(pacienteContatos),
  });

  const criarPacienteMutation = useMutation({
    mutationFn: api.criarPaciente,
    onSuccess: (novo) => {
      toast.success('Paciente cadastrado', `${novo.nome} foi registrado com sucesso.`);
      setShowNovoPaciente(false);
      setNome('');
      setCpf('');
      setCns('');
      setDataNascimento('');
      void queryClient.invalidateQueries({ queryKey: ['pacientes'] });
    },
    onError: (err) => {
      toast.error(
        'Erro ao cadastrar paciente',
        err instanceof ApiError ? err.message : 'Verifique os campos informados.'
      );
    },
  });

  const criarContatoMutation = useMutation({
    mutationFn: (vars: {
      pacienteId: string;
      nome: string;
      parentesco?: string;
      telefone: string;
      principal: boolean;
    }) =>
      api.criarContatoEmergencia(vars.pacienteId, {
        nome: vars.nome,
        parentesco: vars.parentesco || undefined,
        telefone: vars.telefone,
        principal: vars.principal,
      }),
    onSuccess: () => {
      toast.success('Contato de emergência salvo');
      setContatoNome('');
      setContatoParentesco('');
      setContatoTelefone('');
      setContatoPrincipal(false);
      void queryClient.invalidateQueries({
        queryKey: ['contatos-emergencia', pacienteContatos?.id],
      });
    },
    onError: (err) => {
      toast.error(
        'Erro ao salvar contato',
        err instanceof ApiError ? err.message : 'Tente novamente.'
      );
    },
  });

  const removerContatoMutation = useMutation({
    mutationFn: (vars: { pacienteId: string; contatoId: string }) =>
      api.removerContatoEmergencia(vars.pacienteId, vars.contatoId),
    onSuccess: () => {
      toast.info('Contato removido');
      void queryClient.invalidateQueries({
        queryKey: ['contatos-emergencia', pacienteContatos?.id],
      });
    },
    onError: (err) => {
      toast.error(
        'Erro ao remover contato',
        err instanceof ApiError ? err.message : 'Tente novamente.'
      );
    },
  });

  const abrirAtendimentoMutation = useMutation({
    mutationFn: api.abrirAtendimento,
    onSuccess: () => {
      toast.success(
        'Atendimento aberto',
        `Paciente encaminhado para a fila (${tipoAtendimento}).`
      );
      setPacienteAtendimento(null);
      void queryClient.invalidateQueries({ queryKey: ['fila-atendimentos'] });
      navigate('/triagem');
    },
    onError: (err) => {
      toast.error(
        'Não foi possível abrir atendimento',
        err instanceof ApiError ? err.message : 'Tente novamente.'
      );
    },
  });

  const handleCriarPaciente = (e: FormEvent) => {
    e.preventDefault();
    const cpfLimpo = cpf.replace(/\D/g, '');
    criarPacienteMutation.mutate({
      nome: nome.trim(),
      cpf: cpfLimpo,
      cns: cns.trim() || undefined,
      dataNascimento,
    });
  };

  const handleCriarContato = (e: FormEvent) => {
    e.preventDefault();
    if (!pacienteContatos) return;
    criarContatoMutation.mutate({
      pacienteId: pacienteContatos.id,
      nome: contatoNome.trim(),
      parentesco: contatoParentesco.trim() || undefined,
      telefone: contatoTelefone.trim(),
      principal: contatoPrincipal,
    });
  };

  const pacientes = pacientesQuery.data ?? [];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900">
            Cadastro de Pacientes
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Busque pacientes, gerencie contatos de emergência, abra atendimentos e acesse o PEP.
          </p>
        </div>
        <button
          type="button"
          onClick={() => setShowNovoPaciente(true)}
          className="inline-flex min-h-12 items-center justify-center gap-2 rounded-xl bg-blue-700 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-800"
        >
          <UserPlus className="h-5 w-5" />
          Novo Paciente
        </button>
      </div>

      {/* Search Bar */}
      <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-2xs">
        <label htmlFor="busca-paciente" className="sr-only">
          Buscar paciente por nome, CPF ou CNS
        </label>
        <div className="relative">
          <Search className="pointer-events-none absolute top-1/2 left-3.5 h-5 w-5 -translate-y-1/2 text-slate-400" />
          <input
            id="busca-paciente"
            type="search"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por nome completo, CPF ou CNS..."
            className="min-h-12 w-full rounded-xl border border-slate-300 bg-slate-50/50 pr-4 pl-11 text-sm text-slate-900 placeholder:text-slate-400 focus:border-blue-600 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-600/20"
          />
        </div>
      </div>

      {/* Patients List */}
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xs">
        {pacientesQuery.isLoading ? (
          <div className="flex items-center justify-center p-12 text-sm text-slate-500">
            Carregando pacientes...
          </div>
        ) : pacientesQuery.isError ? (
          <div className="p-8 text-center text-sm text-red-700">
            Erro ao carregar pacientes. Verifique sua conexão ou permissões.
          </div>
        ) : pacientes.length === 0 ? (
          <div className="flex flex-col items-center justify-center gap-2 p-12 text-center">
            <Users className="h-10 w-10 text-slate-300" />
            <p className="text-sm font-semibold text-slate-700">Nenhum paciente encontrado</p>
            <p className="text-xs text-slate-500">
              Ajuste o termo de busca ou cadastre um novo paciente.
            </p>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {pacientes.map((paciente) => (
              <div
                key={paciente.id}
                className="flex flex-col gap-4 p-4 transition hover:bg-slate-50/80 sm:p-5 lg:flex-row lg:items-center lg:justify-between"
              >
                <div className="min-w-0 space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="text-base font-semibold text-slate-900">
                      {paciente.nome}
                    </h2>
                    <span className="rounded-md bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                      CPF: {formatCpf(paciente.cpf)}
                    </span>
                    {paciente.cns && (
                      <span className="rounded-md bg-sky-50 px-2 py-0.5 text-xs font-medium text-sky-700">
                        CNS: {paciente.cns}
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-500">
                    Nascimento: {formatDate(paciente.dataNascimento)} · Cadastrado em{' '}
                    {formatDate(paciente.criadoEm)}
                  </p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setPacienteContatos(paciente)}
                    className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-slate-300 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
                  >
                    <Phone className="h-4 w-4 text-slate-500" />
                    Contatos
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setTipoAtendimento('EMERGENCIA');
                      setPacienteAtendimento(paciente);
                    }}
                    className="inline-flex min-h-11 items-center gap-2 rounded-xl border border-blue-200 bg-blue-50 px-3.5 py-2 text-xs font-semibold text-blue-800 transition hover:bg-blue-100"
                  >
                    <ClipboardPlus className="h-4 w-4" />
                    Abrir Atendimento
                  </button>
                  <Link
                    to={`/pacientes/${paciente.id}/prontuario`}
                    className="inline-flex min-h-11 items-center gap-2 rounded-xl bg-slate-800 px-3.5 py-2 text-xs font-semibold text-white transition hover:bg-slate-900"
                  >
                    <FileText className="h-4 w-4" />
                    Prontuário
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal: Novo Paciente */}
      {showNovoPaciente && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-novo-paciente-title"
        >
          <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
              <h2 id="modal-novo-paciente-title" className="text-lg font-semibold text-slate-900">
                Cadastrar Novo Paciente
              </h2>
              <button
                type="button"
                onClick={() => setShowNovoPaciente(false)}
                aria-label="Fechar"
                className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleCriarPaciente} className="space-y-4 p-6">
              <div>
                <label htmlFor="pac-nome" className="block text-sm font-medium text-slate-700">
                  Nome completo *
                </label>
                <input
                  id="pac-nome"
                  type="text"
                  required
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                />
              </div>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label htmlFor="pac-cpf" className="block text-sm font-medium text-slate-700">
                    CPF (11 dígitos) *
                  </label>
                  <input
                    id="pac-cpf"
                    type="text"
                    required
                    value={cpf}
                    onChange={(e) => setCpf(e.target.value)}
                    placeholder="000.000.000-00"
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                  />
                </div>
                <div>
                  <label htmlFor="pac-nasc" className="block text-sm font-medium text-slate-700">
                    Data de nascimento *
                  </label>
                  <input
                    id="pac-nasc"
                    type="date"
                    required
                    value={dataNascimento}
                    onChange={(e) => setDataNascimento(e.target.value)}
                    className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="pac-cns" className="block text-sm font-medium text-slate-700">
                  Cartão Nacional de Saúde (CNS) — opcional
                </label>
                <input
                  id="pac-cns"
                  type="text"
                  value={cns}
                  onChange={(e) => setCns(e.target.value)}
                  placeholder="15 dígitos"
                  className="mt-1 min-h-11 w-full rounded-xl border border-slate-300 px-3.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                />
              </div>

              <div className="flex justify-end gap-2.5 pt-2">
                <button
                  type="button"
                  onClick={() => setShowNovoPaciente(false)}
                  className="min-h-11 rounded-xl border border-slate-300 bg-white px-4 text-sm font-medium text-slate-700 hover:bg-slate-50"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={criarPacienteMutation.isPending}
                  className="min-h-11 rounded-xl bg-blue-700 px-5 text-sm font-semibold text-white hover:bg-blue-800 disabled:opacity-60"
                >
                  {criarPacienteMutation.isPending ? 'Salvando...' : 'Salvar Paciente'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Contatos de Emergência */}
      {pacienteContatos && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-contatos-title"
        >
          <div className="flex max-h-[90vh] w-full max-w-xl flex-col rounded-2xl border border-slate-200 bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
              <div>
                <h2 id="modal-contatos-title" className="text-lg font-semibold text-slate-900">
                  Contatos de Emergência
                </h2>
                <p className="text-xs text-slate-500">{pacienteContatos.nome}</p>
              </div>
              <button
                type="button"
                onClick={() => setPacienteContatos(null)}
                aria-label="Fechar"
                className="rounded-lg p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="flex-1 space-y-5 overflow-y-auto p-6">
              {/* Lista */}
              <div className="space-y-2.5">
                {contatosQuery.isLoading ? (
                  <p className="text-sm text-slate-500">Carregando contatos...</p>
                ) : (contatosQuery.data ?? []).length === 0 ? (
                  <p className="rounded-xl border border-dashed border-slate-200 p-4 text-center text-sm text-slate-500">
                    Nenhum contato de emergência cadastrado.
                  </p>
                ) : (
                  (contatosQuery.data ?? []).map((c) => (
                    <div
                      key={c.id}
                      className="flex items-center justify-between gap-3 rounded-xl border border-slate-200 bg-slate-50/70 p-3.5"
                    >
                      <div className="min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="text-sm font-semibold text-slate-900">{c.nome}</p>
                          {c.principal && (
                            <span className="inline-flex items-center gap-1 rounded-md bg-amber-100 px-2 py-0.5 text-[11px] font-semibold text-amber-800">
                              <Star className="h-3 w-3" />
                              Principal
                            </span>
                          )}
                        </div>
                        <p className="text-xs text-slate-600">
                          {c.telefone}
                          {c.parentesco ? ` · ${c.parentesco}` : ''}
                        </p>
                      </div>
                      <button
                        type="button"
                        disabled={removerContatoMutation.isPending}
                        onClick={() =>
                          removerContatoMutation.mutate({
                            pacienteId: pacienteContatos.id,
                            contatoId: c.id,
                          })
                        }
                        aria-label={`Remover contato ${c.nome}`}
                        className="flex h-10 w-10 items-center justify-center rounded-lg border border-red-200 bg-white text-red-600 transition hover:bg-red-50"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  ))
                )}
              </div>

              {/* Form novo contato */}
              <form
                onSubmit={handleCriarContato}
                className="space-y-3 rounded-xl border border-slate-200 bg-slate-50/50 p-4"
              >
                <p className="text-sm font-semibold text-slate-800">Adicionar novo contato</p>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div>
                    <label htmlFor="ct-nome" className="block text-xs font-medium text-slate-700">
                      Nome *
                    </label>
                    <input
                      id="ct-nome"
                      type="text"
                      required
                      value={contatoNome}
                      onChange={(e) => setContatoNome(e.target.value)}
                      className="mt-1 min-h-10 w-full rounded-lg border border-slate-300 bg-white px-3 text-sm focus:border-blue-600 focus:outline-none"
                    />
                  </div>
                  <div>
                    <label htmlFor="ct-tel" className="block text-xs font-medium text-slate-700">
                      Telefone *
                    </label>
                    <input
                      id="ct-tel"
                      type="tel"
                      required
                      value={contatoTelefone}
                      onChange={(e) => setContatoTelefone(e.target.value)}
                      placeholder="(11) 99999-9999"
                      className="mt-1 min-h-10 w-full rounded-lg border border-slate-300 bg-white px-3 text-sm focus:border-blue-600 focus:outline-none"
                    />
                  </div>
                </div>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:items-end">
                  <div>
                    <label htmlFor="ct-par" className="block text-xs font-medium text-slate-700">
                      Parentesco
                    </label>
                    <input
                      id="ct-par"
                      type="text"
                      value={contatoParentesco}
                      onChange={(e) => setContatoParentesco(e.target.value)}
                      placeholder="Ex: Cônjuge, Mãe, Filho"
                      className="mt-1 min-h-10 w-full rounded-lg border border-slate-300 bg-white px-3 text-sm focus:border-blue-600 focus:outline-none"
                    />
                  </div>
                  <label className="flex min-h-10 items-center gap-2 text-sm text-slate-700">
                    <input
                      type="checkbox"
                      checked={contatoPrincipal}
                      onChange={(e) => setContatoPrincipal(e.target.checked)}
                      className="h-4 w-4 rounded border-slate-300 text-blue-700"
                    />
                    Contato principal
                  </label>
                </div>
                <button
                  type="submit"
                  disabled={criarContatoMutation.isPending}
                  className="inline-flex min-h-11 items-center gap-2 rounded-xl bg-blue-700 px-4 text-sm font-semibold text-white hover:bg-blue-800 disabled:opacity-60"
                >
                  <Plus className="h-4 w-4" />
                  {criarContatoMutation.isPending ? 'Adicionando...' : 'Adicionar Contato'}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Abrir Atendimento */}
      {pacienteAtendimento && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs"
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-atendimento-title"
        >
          <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
              <div>
                <h2 id="modal-atendimento-title" className="text-lg font-semibold text-slate-900">
                  Abrir Atendimento
                </h2>
                <p className="text-xs text-slate-500">{pacienteAtendimento.nome}</p>
              </div>
              <button
                type="button"
                onClick={() => setPacienteAtendimento(null)}
                aria-label="Fechar"
                className="rounded-lg p-2 text-slate-400 hover:bg-slate-100"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-4 p-6">
              <fieldset className="space-y-2">
                <legend className="text-sm font-medium text-slate-700">
                  Tipo de atendimento
                </legend>
                {(
                  [
                    {
                      value: 'EMERGENCIA',
                      label: 'Emergência',
                      desc: 'Encaminhamento prioritário para Triagem Manchester',
                    },
                    {
                      value: 'HOSPITALAR',
                      label: 'Hospitalar / Internação',
                      desc: 'Atendimento com previsão de alocação em leito',
                    },
                    {
                      value: 'AMBULATORIAL',
                      label: 'Ambulatorial',
                      desc: 'Consulta eletiva ou pronto atendimento de baixa complexidade',
                    },
                  ] as const
                ).map((opt) => (
                  <label
                    key={opt.value}
                    className={`flex cursor-pointer items-start gap-3 rounded-xl border p-3.5 transition ${
                      tipoAtendimento === opt.value
                        ? 'border-blue-600 bg-blue-50/60'
                        : 'border-slate-200 hover:bg-slate-50'
                    }`}
                  >
                    <input
                      type="radio"
                      name="tipo-atendimento"
                      value={opt.value}
                      checked={tipoAtendimento === opt.value}
                      onChange={() => setTipoAtendimento(opt.value)}
                      className="mt-1 h-4 w-4 text-blue-700"
                    />
                    <div>
                      <p className="text-sm font-semibold text-slate-900">{opt.label}</p>
                      <p className="text-xs text-slate-500">{opt.desc}</p>
                    </div>
                  </label>
                ))}
              </fieldset>

              <div className="flex justify-end gap-2.5 pt-2">
                <button
                  type="button"
                  onClick={() => setPacienteAtendimento(null)}
                  className="min-h-11 rounded-xl border border-slate-300 bg-white px-4 text-sm font-medium text-slate-700"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  disabled={abrirAtendimentoMutation.isPending}
                  onClick={() =>
                    abrirAtendimentoMutation.mutate({
                      pacienteId: pacienteAtendimento.id,
                      tipo: tipoAtendimento,
                    })
                  }
                  className="min-h-11 rounded-xl bg-blue-700 px-5 text-sm font-semibold text-white hover:bg-blue-800 disabled:opacity-60"
                >
                  {abrirAtendimentoMutation.isPending ? 'Abrindo...' : 'Confirmar Abertura'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
