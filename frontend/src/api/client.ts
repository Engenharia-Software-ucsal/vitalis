import type {
  Alergia,
  AuthResponse,
  ContatoEmergencia,
  FilaAtendimentoItem,
  GravidadeAlergia,
  ItemPrescricaoInput,
  Leito,
  LeitoHistorico,
  Medicamento,
  Paciente,
  ProntuarioResponse,
  StatusLeito,
  TipoAtendimento,
  TriagemInput,
  Usuario,
} from '../types/api';

const ACCESS_TOKEN_KEY = 'vitalis_access_token';
const REFRESH_TOKEN_KEY = 'vitalis_refresh_token';
const USER_KEY = 'vitalis_user';

export class ApiError extends Error {
  status: number;
  data: unknown;

  constructor(status: number, message: string, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export function getStoredAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getStoredRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function getStoredUser(): Usuario | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Usuario;
  } catch {
    return null;
  }
}

export function saveAuthSession(auth: AuthResponse): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(auth.usuario));
}

export function clearAuthSession(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

let onSessionExpiredCallback: (() => void) | null = null;
export function setOnSessionExpired(cb: (() => void) | null): void {
  onSessionExpiredCallback = cb;
}

let refreshPromise: Promise<string | null> | null = null;

async function attemptTokenRefresh(): Promise<string | null> {
  if (refreshPromise) return refreshPromise;

  const refreshToken = getStoredRefreshToken();
  if (!refreshToken) return null;

  refreshPromise = (async () => {
    try {
      const res = await fetch('/api/clinico/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      if (!res.ok) {
        clearAuthSession();
        onSessionExpiredCallback?.();
        return null;
      }

      const data = (await res.json()) as AuthResponse;
      saveAuthSession(data);
      return data.accessToken;
    } catch {
      return null;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

function extractErrorMessage(status: number, data: unknown): string {
  if (data && typeof data === 'object') {
    const obj = data as Record<string, unknown>;
    if (typeof obj.message === 'string' && obj.message.trim()) return obj.message;
    if (typeof obj.detail === 'string' && obj.detail.trim()) return obj.detail;
    if (typeof obj.error === 'string' && obj.error.trim()) return obj.error;
  }

  switch (status) {
    case 401:
      return 'Sessão expirada ou credenciais inválidas.';
    case 403:
      return 'Você não tem permissão para realizar esta ação.';
    case 404:
      return 'Recurso não encontrado.';
    case 409:
      return 'Conflito de estado: verifique se o registro já existe ou foi alterado.';
    case 422:
      return 'Regra de negócio impediu o envio. Verifique os dados informados.';
    default:
      return `Erro ao comunicar com o servidor (HTTP ${status}).`;
  }
}

interface RequestOptions extends RequestInit {
  skipAuth?: boolean;
  _retried?: boolean;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { skipAuth = false, _retried = false, headers: customHeaders, ...rest } = options;

  const headers = new Headers(customHeaders);
  if (!headers.has('Content-Type') && rest.body !== undefined) {
    headers.set('Content-Type', 'application/json');
  }

  if (!skipAuth) {
    const token = getStoredAccessToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  const response = await fetch(path, {
    ...rest,
    headers,
  });

  if (response.status === 401 && !skipAuth && !_retried) {
    const newToken = await attemptTokenRefresh();
    if (newToken) {
      return apiRequest<T>(path, { ...options, _retried: true });
    }
  }

  const text = await response.text();
  let parsed: unknown = undefined;
  if (text) {
    try {
      parsed = JSON.parse(text);
    } catch {
      parsed = text;
    }
  }

  if (!response.ok) {
    throw new ApiError(response.status, extractErrorMessage(response.status, parsed), parsed);
  }

  return parsed as T;
}

export const api = {
  // Auth
  login: (email: string, senha: string) =>
    apiRequest<AuthResponse>('/api/clinico/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
      skipAuth: true,
    }),

  me: () => apiRequest<Usuario>('/api/clinico/auth/me'),

  // Pacientes
  listarPacientes: (q = '') =>
    apiRequest<Paciente[]>(`/api/clinico/pacientes?q=${encodeURIComponent(q)}`),

  criarPaciente: (payload: {
    nome: string;
    cpf: string;
    cns?: string;
    dataNascimento: string;
  }) =>
    apiRequest<Paciente>('/api/clinico/pacientes', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listarContatosEmergencia: (pacienteId: string) =>
    apiRequest<ContatoEmergencia[]>(
      `/api/clinico/pacientes/${pacienteId}/contatos-emergencia`
    ),

  criarContatoEmergencia: (
    pacienteId: string,
    payload: { nome: string; parentesco?: string; telefone: string; principal?: boolean }
  ) =>
    apiRequest<ContatoEmergencia>(
      `/api/clinico/pacientes/${pacienteId}/contatos-emergencia`,
      {
        method: 'POST',
        body: JSON.stringify(payload),
      }
    ),

  removerContatoEmergencia: (pacienteId: string, contatoId: string) =>
    apiRequest<void>(
      `/api/clinico/pacientes/${pacienteId}/contatos-emergencia/${contatoId}`,
      { method: 'DELETE' }
    ),

  // Atendimentos & Triagem
  abrirAtendimento: (payload: { pacienteId: string; tipo: TipoAtendimento }) =>
    apiRequest<{ id: string }>('/api/clinico/atendimentos', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listarFilaAtendimentos: () =>
    apiRequest<FilaAtendimentoItem[]>('/api/clinico/atendimentos/fila'),

  assumirAtendimento: (atendimentoId: string) =>
    apiRequest<void>(`/api/clinico/atendimentos/${atendimentoId}/assumir`, {
      method: 'PATCH',
    }),

  darAltaAtendimento: (atendimentoId: string, motivoEncerramento: string) =>
    apiRequest<void>(`/api/clinico/atendimentos/${atendimentoId}/alta`, {
      method: 'POST',
      body: JSON.stringify({ motivoEncerramento }),
    }),

  registrarTriagem: (atendimentoId: string, payload: TriagemInput) =>
    apiRequest<unknown>(`/api/clinico/atendimentos/${atendimentoId}/triagens`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  // PEP, Alergias, Medicamentos & Prescrição
  obterProntuario: (pacienteId: string) =>
    apiRequest<ProntuarioResponse>(`/api/clinico/pacientes/${pacienteId}/prontuario`),

  criarEvolucao: (pacienteId: string, payload: { atendimentoId: string; texto: string }) =>
    apiRequest<unknown>(`/api/clinico/pacientes/${pacienteId}/prontuario/evolucoes`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  criarAlergia: (
    pacienteId: string,
    payload: { substancia: string; reacao?: string; gravidade: GravidadeAlergia }
  ) =>
    apiRequest<Alergia>(`/api/clinico/pacientes/${pacienteId}/alergias`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  inativarAlergia: (pacienteId: string, alergiaId: string) =>
    apiRequest<void>(`/api/clinico/pacientes/${pacienteId}/alergias/${alergiaId}/inativar`, {
      method: 'PATCH',
    }),

  listarMedicamentos: (q = '') =>
    apiRequest<Medicamento[]>(`/api/clinico/medicamentos?q=${encodeURIComponent(q)}`),

  criarPrescricao: (
    atendimentoId: string,
    payload: { alergiasReconhecidas: boolean; itens: ItemPrescricaoInput[] }
  ) =>
    apiRequest<unknown>(`/api/clinico/atendimentos/${atendimentoId}/prescricoes`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  // Leitos & Alocações
  listarLeitos: (params?: { status?: string; ala?: string }) => {
    const qs = new URLSearchParams();
    if (params?.status) qs.set('status', params.status);
    if (params?.ala) qs.set('ala', params.ala);
    const query = qs.toString();
    return apiRequest<Leito[]>(`/api/leitos/leitos${query ? `?${query}` : ''}`);
  },

  criarLeito: (payload: { codigo: string; ala: string }) =>
    apiRequest<Leito>('/api/leitos/leitos', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  alterarStatusLeito: (leitoId: string, status: StatusLeito) =>
    apiRequest<Leito>(`/api/leitos/leitos/${leitoId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    }),

  higienizarLeito: (leitoId: string) =>
    apiRequest<Leito>(`/api/leitos/leitos/${leitoId}/higienizar`, {
      method: 'PATCH',
    }),

  historicoLeito: (leitoId: string) =>
    apiRequest<LeitoHistorico[]>(`/api/leitos/leitos/${leitoId}/historico`),

  alocarLeito: (payload: {
    leitoId: string;
    atendimentoId: string;
    status?: 'OCUPADO' | 'RESERVADO';
  }) =>
    apiRequest<unknown>('/api/leitos/alocacoes', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  transferirLeito: (alocacaoId: string, leitoDestinoId: string) =>
    apiRequest<unknown>(`/api/leitos/alocacoes/${alocacaoId}/transferir`, {
      method: 'POST',
      body: JSON.stringify({ leitoDestinoId }),
    }),

  // Servico de IA Clinica (agnostico a provedor/modelo)
  analisarPrescricaoIa: (payload: {
    atendimentoId: number;
    nomePaciente?: string;
    idadeAnos?: number;
    sexo?: string;
    pesoKg?: number;
    creatininaSericaMgDl?: number;
    alergiasAtivas: string[];
    prescricoesVigentes: string[];
    itensSolicitados: { medicamento: string; dose: string; via: string; frequencia: string }[];
  }) =>
    apiRequest<{
      inferenciaId: number;
      nivelAlerta: string;
      clearanceCreatininaMlMin?: number | null;
      exigeAjusteRenal: boolean;
      alertasDeterministicos: { tipo: string; gravidade: string; mensagem: string; referencia: string }[];
      parecerConsultivo: string;
      referenciasRag: { documento: string; fonteOficial: string; secao: string; trecho: string }[];
      modoContingenciaAtivo: boolean;
      mensagemContingencia?: string | null;
      provedor: string;
      modelo: string;
      latenciaMs: number;
      circuitBreakerEstado: string;
    }>('/api/ia/copiloto/prescricao', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  gerarSumarioAltaIa: (payload: {
    atendimentoId: number;
    nomePaciente?: string;
    idadeAnos?: number;
    queixaPrincipal?: string;
    evolucoes: string[];
    prescricoes: string[];
  }) =>
    apiRequest<{
      inferenciaId: number;
      minuta: string;
      modoContingenciaAtivo: boolean;
      provedor: string;
      modelo: string;
      latenciaMs: number;
    }>('/api/ia/sumario-alta', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  registrarDecisaoIa: (
    inferenciaId: number,
    payload: { decisaoMedica: 'ACEITA' | 'MODIFICADA' | 'REJEITADA'; justificativaHumana?: string }
  ) =>
    apiRequest<unknown>(`/api/ia/inferencias/${inferenciaId}/decisao`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
};
