export interface Usuario {
  id: string;
  nome: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  usuario: Usuario;
}

export interface Paciente {
  id: string;
  nome: string;
  cpf: string;
  cns?: string | null;
  dataNascimento: string;
  criadoEm: string;
}

export interface ContatoEmergencia {
  id: string;
  nome: string;
  parentesco?: string | null;
  telefone: string;
  principal: boolean;
}

export type TipoAtendimento = 'AMBULATORIAL' | 'HOSPITALAR' | 'EMERGENCIA';

export type StatusAtendimento =
  | 'AGUARDANDO_TRIAGEM'
  | 'AGUARDANDO_ATENDIMENTO'
  | 'EM_ATENDIMENTO'
  | 'ALTA'
  | 'CANCELADO'
  | string;

export type PrioridadeManchester =
  | 'VERMELHA'
  | 'LARANJA'
  | 'AMARELA'
  | 'VERDE'
  | 'AZUL';

export interface FilaAtendimentoItem {
  atendimentoId: string;
  pacienteId: string;
  pacienteNome: string;
  pacienteCpf: string;
  tipo: TipoAtendimento;
  status: StatusAtendimento;
  iniciadoEm: string;
  prioridade?: PrioridadeManchester | null;
  escoreQsofa?: number | null;
  escoreNews2?: number | null;
  alertaSepse?: boolean | null;
  possuiTriagem: boolean;
  queixaPrincipal?: string | null;
}

export interface TriagemInput {
  queixaPrincipal: string;
  temperaturaC?: number | null;
  frequenciaCardiaca?: number | null;
  frequenciaRespiratoria?: number | null;
  pressaoSistolica?: number | null;
  pressaoDiastolica?: number | null;
  saturacaoO2?: number | null;
  escalaDor?: number | null;
  escalaGlasgow?: number | null;
  oxigenioSuplementar?: boolean;
  observacoes?: string | null;
}

export type GravidadeAlergia = 'LEVE' | 'MODERADA' | 'GRAVE' | 'NAO_INFORMADA';

export interface Alergia {
  id: string;
  substancia: string;
  reacao?: string | null;
  gravidade: GravidadeAlergia;
  ativa: boolean;
  registradaEm?: string;
  criadoEm?: string;
}

export interface AtendimentoResumo {
  id: string;
  tipo: TipoAtendimento;
  status: StatusAtendimento;
  iniciadoEm: string;
  encerradoEm?: string | null;
  motivoEncerramento?: string | null;
  profissionalResponsavelId?: string | null;
  prioridade?: PrioridadeManchester | null;
}

export interface TriagemRegistro {
  id: string;
  atendimentoId: string;
  queixaPrincipal: string;
  temperaturaC?: number | null;
  frequenciaCardiaca?: number | null;
  frequenciaRespiratoria?: number | null;
  pressaoSistolica?: number | null;
  pressaoDiastolica?: number | null;
  saturacaoO2?: number | null;
  escalaDor?: number | null;
  escalaGlasgow?: number | null;
  oxigenioSuplementar?: boolean;
  prioridade?: PrioridadeManchester | null;
  escoreQsofa?: number | null;
  escoreNews2?: number | null;
  alertaSepse?: boolean | null;
  observacoes?: string | null;
  realizadaEm?: string;
  criadoEm?: string;
}

export interface EvolucaoRegistro {
  id: string;
  atendimentoId: string;
  texto: string;
  autorId?: string;
  autorNome?: string;
  criadoEm?: string;
  registradaEm?: string;
}

export interface ItemPrescricaoInput {
  medicamentoId: string;
  dose: string;
  via: string;
  frequencia: string;
  duracao?: string;
  quantidade: number;
  instrucoes?: string;
}

export interface ItemPrescricaoRegistro {
  id?: string;
  medicamentoId: string;
  medicamentoNome?: string;
  dose: string;
  via: string;
  frequencia: string;
  duracao?: string | null;
  quantidade: number;
  instrucoes?: string | null;
}

export interface PrescricaoRegistro {
  id: string;
  atendimentoId: string;
  alergiasReconhecidas: boolean;
  criadoEm?: string;
  prescritaEm?: string;
  profissionalId?: string;
  itens: ItemPrescricaoRegistro[];
}

export interface ProntuarioResponse {
  prontuarioId: string;
  paciente: Paciente;
  alergias: Alergia[];
  atendimentos: AtendimentoResumo[];
  triagens: TriagemRegistro[];
  evolucoes: EvolucaoRegistro[];
  prescricoes: PrescricaoRegistro[];
}

export interface Medicamento {
  id: string;
  nome: string;
  principioAtivo: string;
  unidade: string;
  ativo: boolean;
}

export type StatusLeito =
  | 'LIVRE'
  | 'OCUPADO'
  | 'RESERVADO'
  | 'EM_HIGIENIZACAO'
  | 'MANUTENCAO'
  | 'ISOLAMENTO'
  | string;

export interface AlocacaoAtual {
  id: string;
  atendimentoId: string;
  status: 'OCUPADO' | 'RESERVADO' | string;
  alocadoEm: string;
  responsavelId?: string | null;
}

export interface Leito {
  id: string;
  codigo: string;
  ala: string;
  status: StatusLeito;
  alocacaoAtual: AlocacaoAtual | null;
}

export interface LeitoHistorico {
  id: string;
  leitoId: string;
  alocacaoId?: string | null;
  statusAnterior?: string | null;
  statusNovo: string;
  atendimentoId?: string | null;
  responsavelId?: string | null;
  alteradoEm: string;
}
