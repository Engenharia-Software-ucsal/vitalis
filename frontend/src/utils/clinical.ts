import type { PrioridadeManchester, StatusLeito, TriagemInput } from '../types/api';

export interface ClinicalScorePreview {
  qsofa: number;
  news2: number;
  alertaSepse: boolean;
  prioridade: PrioridadeManchester;
  tempoAlvo: string;
}

export function calculateClinicalPreview(input: TriagemInput): ClinicalScorePreview {
  // qSOFA (0-3)
  let qsofa = 0;
  if (input.frequenciaRespiratoria != null && input.frequenciaRespiratoria >= 22) qsofa += 1;
  if (input.pressaoSistolica != null && input.pressaoSistolica <= 100) qsofa += 1;
  if (input.escalaGlasgow != null && input.escalaGlasgow < 15) qsofa += 1;

  // NEWS2
  let news2 = 0;
  let hasSingleThree = false;

  const addNews = (pts: number) => {
    news2 += pts;
    if (pts >= 3) hasSingleThree = true;
  };

  if (input.frequenciaRespiratoria != null) {
    const fr = input.frequenciaRespiratoria;
    if (fr <= 8 || fr >= 25) addNews(3);
    else if (fr >= 21) addNews(2);
    else if (fr <= 11) addNews(1);
  }

  if (input.saturacaoO2 != null) {
    const spo2 = input.saturacaoO2;
    if (spo2 <= 91) addNews(3);
    else if (spo2 <= 93) addNews(2);
    else if (spo2 <= 95) addNews(1);
  }

  if (input.oxigenioSuplementar) {
    addNews(2);
  }

  if (input.pressaoSistolica != null) {
    const pas = input.pressaoSistolica;
    if (pas <= 90 || pas >= 220) addNews(3);
    else if (pas <= 100) addNews(2);
    else if (pas <= 110) addNews(1);
  }

  if (input.frequenciaCardiaca != null) {
    const fc = input.frequenciaCardiaca;
    if (fc <= 40 || fc >= 131) addNews(3);
    else if (fc >= 111) addNews(2);
    else if (fc <= 50 || fc >= 91) addNews(1);
  }

  if (input.escalaGlasgow != null && input.escalaGlasgow < 15) {
    addNews(3);
  }

  if (input.temperaturaC != null) {
    const t = input.temperaturaC;
    if (t <= 35.0) addNews(3);
    else if (t >= 39.1) addNews(2);
    else if (t <= 36.0 || t >= 38.1) addNews(1);
  }

  const alertaSepse = qsofa >= 2;

  // Manchester preview
  let prioridade: PrioridadeManchester = 'AZUL';

  const isEmergencia =
    (input.escalaGlasgow != null && input.escalaGlasgow <= 8) ||
    (input.saturacaoO2 != null && input.saturacaoO2 < 85) ||
    (input.pressaoSistolica != null && input.pressaoSistolica < 80) ||
    (input.frequenciaRespiratoria != null && (input.frequenciaRespiratoria <= 8 || input.frequenciaRespiratoria >= 30)) ||
    news2 >= 7;

  const isMuitoUrgente =
    alertaSepse ||
    news2 >= 5 ||
    hasSingleThree ||
    (input.escalaDor != null && input.escalaDor >= 8) ||
    (input.saturacaoO2 != null && input.saturacaoO2 < 92) ||
    (input.temperaturaC != null && (input.temperaturaC >= 39.5 || input.temperaturaC <= 35.0));

  const isUrgente =
    news2 >= 3 ||
    (input.escalaDor != null && input.escalaDor >= 5) ||
    (input.temperaturaC != null && input.temperaturaC >= 38.0);

  const isPoucoUrgente =
    news2 >= 1 ||
    (input.escalaDor != null && input.escalaDor >= 1) ||
    input.queixaPrincipal.trim().length > 0;

  if (isEmergencia) prioridade = 'VERMELHA';
  else if (isMuitoUrgente) prioridade = 'LARANJA';
  else if (isUrgente) prioridade = 'AMARELA';
  else if (isPoucoUrgente) prioridade = 'VERDE';

  const tempoMap: Record<PrioridadeManchester, string> = {
    VERMELHA: 'Atendimento imediato (0 min)',
    LARANJA: 'Muito urgente (até 10 min)',
    AMARELA: 'Urgente (até 60 min)',
    VERDE: 'Pouco urgente (até 120 min)',
    AZUL: 'Não urgente (até 240 min)',
  };

  return {
    qsofa,
    news2,
    alertaSepse,
    prioridade,
    tempoAlvo: tempoMap[prioridade],
  };
}

export const MANCHESTER_META: Record<
  PrioridadeManchester,
  {
    label: string;
    shortLabel: string;
    badgeClass: string;
    borderClass: string;
    bgSoftClass: string;
    dotClass: string;
    order: number;
  }
> = {
  VERMELHA: {
    label: 'Vermelha — Emergência',
    shortLabel: 'Emergência',
    badgeClass: 'bg-red-600 text-white border-red-700',
    borderClass: 'border-l-red-600',
    bgSoftClass: 'bg-red-50 border-red-200',
    dotClass: 'bg-red-600',
    order: 1,
  },
  LARANJA: {
    label: 'Laranja — Muito Urgente',
    shortLabel: 'Muito Urgente',
    badgeClass: 'bg-orange-500 text-white border-orange-600',
    borderClass: 'border-l-orange-500',
    bgSoftClass: 'bg-orange-50 border-orange-200',
    dotClass: 'bg-orange-500',
    order: 2,
  },
  AMARELA: {
    label: 'Amarela — Urgente',
    shortLabel: 'Urgente',
    badgeClass: 'bg-amber-400 text-slate-900 border-amber-500',
    borderClass: 'border-l-amber-400',
    bgSoftClass: 'bg-amber-50 border-amber-200',
    dotClass: 'bg-amber-400',
    order: 3,
  },
  VERDE: {
    label: 'Verde — Pouco Urgente',
    shortLabel: 'Pouco Urgente',
    badgeClass: 'bg-emerald-600 text-white border-emerald-700',
    borderClass: 'border-l-emerald-600',
    bgSoftClass: 'bg-emerald-50 border-emerald-200',
    dotClass: 'bg-emerald-600',
    order: 4,
  },
  AZUL: {
    label: 'Azul — Não Urgente',
    shortLabel: 'Não Urgente',
    badgeClass: 'bg-blue-600 text-white border-blue-700',
    borderClass: 'border-l-blue-600',
    bgSoftClass: 'bg-blue-50 border-blue-200',
    dotClass: 'bg-blue-600',
    order: 5,
  },
};

export function getLeitoStatusMeta(status: StatusLeito) {
  switch (status) {
    case 'LIVRE':
      return {
        label: 'Livre',
        badge: 'bg-emerald-100 text-emerald-800 border-emerald-200',
        card: 'border-emerald-200 bg-white hover:border-emerald-300',
        bar: 'bg-emerald-500',
      };
    case 'OCUPADO':
      return {
        label: 'Ocupado',
        badge: 'bg-blue-100 text-blue-800 border-blue-200',
        card: 'border-blue-200 bg-blue-50/40 hover:border-blue-300',
        bar: 'bg-blue-600',
      };
    case 'RESERVADO':
      return {
        label: 'Reservado',
        badge: 'bg-sky-100 text-sky-800 border-sky-200',
        card: 'border-sky-200 bg-sky-50/40 hover:border-sky-300',
        bar: 'bg-sky-500',
      };
    case 'EM_HIGIENIZACAO':
    case 'HIGIENIZACAO':
      return {
        label: 'Em Higienização',
        badge: 'bg-amber-100 text-amber-800 border-amber-200',
        card: 'border-amber-200 bg-amber-50/50 hover:border-amber-300',
        bar: 'bg-amber-500',
      };
    case 'ISOLAMENTO':
      return {
        label: 'Isolamento',
        badge: 'bg-purple-100 text-purple-800 border-purple-200',
        card: 'border-purple-200 bg-purple-50/40 hover:border-purple-300',
        bar: 'bg-purple-600',
      };
    case 'MANUTENCAO':
      return {
        label: 'Manutenção',
        badge: 'bg-slate-200 text-slate-700 border-slate-300',
        card: 'border-slate-300 bg-slate-100/70 hover:border-slate-400',
        bar: 'bg-slate-500',
      };
    default:
      return {
        label: status,
        badge: 'bg-slate-100 text-slate-700 border-slate-200',
        card: 'border-slate-200 bg-white',
        bar: 'bg-slate-400',
      };
  }
}

export function formatCpf(cpf?: string | null): string {
  if (!cpf) return '—';
  const digits = cpf.replace(/\D/g, '');
  if (digits.length !== 11) return cpf;
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;
}

export function formatDate(iso?: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso.length === 10 ? `${iso}T00:00:00` : iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleDateString('pt-BR');
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function normalizeRole(role: string): string {
  return role.toUpperCase().replace(/^ROLE_/, '');
}

export function hasAnyRole(userRoles: string[] | undefined, allowed: string[]): boolean {
  if (!userRoles || userRoles.length === 0) return false;
  const normalized = userRoles.map(normalizeRole);
  if (normalized.includes('ADMIN') || normalized.includes('ADMINISTRADOR')) return true;
  return allowed.some((r) => normalized.includes(normalizeRole(r)));
}

export function formatStatusAtendimento(status: string): string {
  const map: Record<string, string> = {
    ABERTO: 'Aberto',
    EM_ANDAMENTO: 'Em Andamento',
    AGUARDANDO_TRIAGEM: 'Aguardando Triagem',
    AGUARDANDO_ATENDIMENTO: 'Aguardando Atendimento',
    EM_ATENDIMENTO: 'Em Atendimento',
    ALTA: 'Alta Médica',
    CANCELADO: 'Cancelado',
  };
  return map[status] ?? status.replace(/_/g, ' ');
}
