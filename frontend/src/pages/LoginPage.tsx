import { useState, type FormEvent } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import {
  Activity,
  BedDouble,
  HeartPulse,
  Lock,
  Mail,
  Shield,
  Stethoscope,
  UserCheck,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { ApiError } from '../api/client';

const DEMO_USERS = [
  {
    label: 'Admin',
    roleHint: 'Acesso completo',
    email: 'admin@vitalis.local',
    icon: <Shield className="h-4 w-4 text-blue-700" />,
  },
  {
    label: 'Recepcionista',
    roleHint: 'Pacientes e fila',
    email: 'recepcao@vitalis.local',
    icon: <UserCheck className="h-4 w-4 text-sky-700" />,
  },
  {
    label: 'Enfermeiro',
    roleHint: 'Triagem e PEP',
    email: 'enfermeiro@vitalis.local',
    icon: <Activity className="h-4 w-4 text-emerald-700" />,
  },
  {
    label: 'Médico',
    roleHint: 'PEP, prescrição e alta',
    email: 'medico@vitalis.local',
    icon: <Stethoscope className="h-4 w-4 text-indigo-700" />,
  },
  {
    label: 'Gestor de Leitos',
    roleHint: 'Mapa e alocações',
    email: 'leitos@vitalis.local',
    icon: <BedDouble className="h-4 w-4 text-amber-700" />,
  },
];

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const performLogin = async (targetEmail: string, targetSenha: string) => {
    setErrorMsg(null);
    setSubmitting(true);
    try {
      const usuario = await login(targetEmail.trim(), targetSenha);
      toast.success(`Bem-vindo(a), ${usuario.nome}`);
      navigate('/', { replace: true });
    } catch (err) {
      const msg =
        err instanceof ApiError
          ? err.message
          : 'Não foi possível autenticar. Verifique a conexão com o servidor.';
      setErrorMsg(msg);
      toast.error('Falha no login', msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !senha) {
      setErrorMsg('Informe e-mail e senha para continuar.');
      return;
    }
    void performLogin(email, senha);
  };

  return (
    <div className="flex min-h-screen flex-col justify-center bg-gradient-to-br from-slate-100 via-sky-50/70 to-blue-100/60 px-4 py-10">
      <div className="mx-auto w-full max-w-4xl overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-xl grid grid-cols-1 lg:grid-cols-12">
        {/* Left institutional panel */}
        <div className="flex flex-col justify-between bg-gradient-to-br from-blue-900 via-blue-800 to-sky-800 p-8 text-white lg:col-span-5">
          <div>
            <div className="inline-flex items-center gap-3 rounded-2xl bg-white/10 px-3.5 py-2.5 backdrop-blur-xs">
              <HeartPulse className="h-7 w-7 text-sky-300" />
              <div>
                <h1 className="text-xl font-bold tracking-tight">Vitalis</h1>
                <p className="text-[11px] font-medium text-sky-200">
                  Sistema Clínico & Gestão de Leitos
                </p>
              </div>
            </div>

            <h2 className="mt-8 text-2xl font-bold leading-snug">
              Prontuário Eletrônico, Triagem Inteligente e Mapa de Leitos em Tempo Real.
            </h2>
            <p className="mt-3 text-sm leading-relaxed text-blue-100/90">
              Classificação de risco pelo Protocolo de Manchester com escores qSOFA e NEWS2,
              prescrição segura com alerta de alergias e controle de ocupação hospitalar.
            </p>
          </div>

          <div className="mt-8 rounded-2xl border border-white/15 bg-white/10 p-4 text-xs text-blue-100">
            <p className="font-semibold text-white">Ambiente de Demonstração</p>
            <p className="mt-1 leading-relaxed">
              Use os perfis ao lado para testar o controle de acesso por papel (RBAC) e os
              fluxos assistenciais integrados.
            </p>
          </div>
        </div>

        {/* Right form + quick demo login */}
        <div className="p-6 sm:p-8 lg:col-span-7">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Acesso ao Sistema</h2>
            <p className="mt-1 text-sm text-slate-500">
              Entre com suas credenciais institucionais ou selecione um perfil demo.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="mt-6 space-y-4">
            <div>
              <label
                htmlFor="login-email"
                className="block text-sm font-medium text-slate-700"
              >
                E-mail institucional
              </label>
              <div className="relative mt-1.5">
                <Mail className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  id="login-email"
                  type="email"
                  autoComplete="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="usuario@vitalis.local"
                  className="min-h-12 w-full rounded-xl border border-slate-300 bg-white pr-4 pl-10 text-sm text-slate-900 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="login-senha"
                className="block text-sm font-medium text-slate-700"
              >
                Senha
              </label>
              <div className="relative mt-1.5">
                <Lock className="pointer-events-none absolute top-1/2 left-3.5 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  id="login-senha"
                  type="password"
                  autoComplete="current-password"
                  required
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  placeholder="••••••••"
                  className="min-h-12 w-full rounded-xl border border-slate-300 bg-white pr-4 pl-10 text-sm text-slate-900 placeholder:text-slate-400 focus:border-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-600/20"
                />
              </div>
            </div>

            {errorMsg && (
              <div
                role="alert"
                className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800"
              >
                {errorMsg}
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="min-h-12 w-full rounded-xl bg-blue-700 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:opacity-60"
            >
              {submitting ? 'Autenticando...' : 'Entrar no Vitalis'}
            </button>
          </form>

          <div className="mt-7 border-t border-slate-100 pt-6">
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Entrar rapidamente como...
            </p>
            <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-2">
              {DEMO_USERS.map((demo) => (
                <button
                  key={demo.email}
                  type="button"
                  disabled={submitting}
                  onClick={() => {
                    setEmail(demo.email);
                    setSenha('Vitalis@2026');
                    void performLogin(demo.email, 'Vitalis@2026');
                  }}
                  className="flex min-h-12 items-center gap-3 rounded-xl border border-slate-200 bg-slate-50/70 px-3.5 py-2.5 text-left transition hover:border-blue-300 hover:bg-blue-50/60 disabled:opacity-50"
                >
                  <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white shadow-2xs border border-slate-200/80">
                    {demo.icon}
                  </span>
                  <div className="min-w-0">
                    <p className="text-sm font-semibold text-slate-800">{demo.label}</p>
                    <p className="truncate text-xs text-slate-500">{demo.roleHint}</p>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
