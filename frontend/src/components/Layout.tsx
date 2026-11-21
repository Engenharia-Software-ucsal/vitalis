import { useState, type ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  Activity,
  BedDouble,
  HeartPulse,
  LogOut,
  Menu,
  ShieldCheck,
  Users,
  X,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { hasAnyRole, normalizeRole } from '../utils/clinical';

interface NavItem {
  to: string;
  label: string;
  subtitle: string;
  icon: ReactNode;
  allowedRoles: string[];
}

const NAV_ITEMS: NavItem[] = [
  {
    to: '/pacientes',
    label: 'Pacientes',
    subtitle: 'Cadastro, contatos e PEP',
    icon: <Users className="h-5 w-5" />,
    allowedRoles: ['ADMIN', 'RECEPCAO', 'RECEPCIONISTA', 'ENFERMEIRO', 'MEDICO'],
  },
  {
    to: '/triagem',
    label: 'Triagem & Fila',
    subtitle: 'Manchester, qSOFA e NEWS2',
    icon: <Activity className="h-5 w-5" />,
    allowedRoles: ['ADMIN', 'RECEPCAO', 'RECEPCIONISTA', 'ENFERMEIRO', 'MEDICO'],
  },
  {
    to: '/leitos',
    label: 'Mapa de Leitos',
    subtitle: 'Alocações e higienização',
    icon: <BedDouble className="h-5 w-5" />,
    allowedRoles: ['ADMIN', 'LEITOS', 'GESTOR_LEITOS', 'ENFERMEIRO', 'MEDICO'],
  },
];

export function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const visibleNav = NAV_ITEMS.filter((item) =>
    hasAnyRole(user?.roles, item.allowedRoles)
  );
  const navToRender = visibleNav.length > 0 ? visibleNav : NAV_ITEMS;

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const renderNavLinks = (onSelect?: () => void) => (
    <nav className="flex flex-col gap-1.5 px-3 py-4" aria-label="Navegação principal">
      {navToRender.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          onClick={onSelect}
          className={({ isActive }) =>
            `flex min-h-12 items-center gap-3 rounded-xl px-3.5 py-2.5 transition ${
              isActive
                ? 'bg-blue-700 text-white shadow-sm'
                : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
            }`
          }
        >
          {({ isActive }) => (
            <>
              <span className={isActive ? 'text-sky-200' : 'text-slate-400'}>
                {item.icon}
              </span>
              <div className="min-w-0">
                <p className="text-sm font-semibold leading-tight">{item.label}</p>
                <p
                  className={`truncate text-xs ${
                    isActive ? 'text-blue-100' : 'text-slate-400'
                  }`}
                >
                  {item.subtitle}
                </p>
              </div>
            </>
          )}
        </NavLink>
      ))}
    </nav>
  );

  return (
    <div className="flex min-h-screen bg-slate-50 text-slate-900">
      {/* Desktop Sidebar */}
      <aside className="hidden w-72 shrink-0 flex-col border-r border-slate-200 bg-white lg:flex">
        <div className="flex items-center gap-3 border-b border-slate-100 px-5 py-5">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-700 text-white shadow-sm">
            <HeartPulse className="h-6 w-6" />
          </div>
          <div>
            <span className="text-lg font-bold tracking-tight text-slate-900">Vitalis</span>
            <p className="text-xs font-medium text-slate-500">Gestão Hospitalar & PEP</p>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto">{renderNavLinks()}</div>

        <div className="border-t border-slate-100 p-4">
          <div className="rounded-xl bg-slate-50 p-3.5 border border-slate-200/70">
            <div className="flex items-center gap-2 text-slate-800">
              <ShieldCheck className="h-4 w-4 text-blue-700 shrink-0" />
              <p className="truncate text-sm font-semibold">{user?.nome}</p>
            </div>
            <p className="mt-0.5 truncate text-xs text-slate-500">{user?.email}</p>
            <div className="mt-2 flex flex-wrap gap-1">
              {user?.roles.map((r) => (
                <span
                  key={r}
                  className="inline-flex items-center rounded-md bg-blue-100 px-2 py-0.5 text-[11px] font-semibold text-blue-800"
                >
                  {normalizeRole(r)}
                </span>
              ))}
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="mt-3 flex min-h-10 w-full items-center justify-center gap-2 rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
            >
              <LogOut className="h-4 w-4" />
              Encerrar sessão
            </button>
          </div>
        </div>
      </aside>

      {/* Mobile / Tablet Drawer */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 z-40 flex lg:hidden">
          <div
            className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs"
            onClick={() => setMobileMenuOpen(false)}
          />
          <aside className="relative z-10 flex w-72 max-w-[85vw] flex-col bg-white shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
              <div className="flex items-center gap-2.5">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-blue-700 text-white">
                  <HeartPulse className="h-5 w-5" />
                </div>
                <span className="text-base font-bold text-slate-900">Vitalis</span>
              </div>
              <button
                type="button"
                onClick={() => setMobileMenuOpen(false)}
                aria-label="Fechar menu"
                className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="flex-1 overflow-y-auto">
              {renderNavLinks(() => setMobileMenuOpen(false))}
            </div>
            <div className="border-t border-slate-100 p-4">
              <p className="truncate text-sm font-semibold text-slate-800">{user?.nome}</p>
              <p className="truncate text-xs text-slate-500">{user?.email}</p>
              <button
                type="button"
                onClick={handleLogout}
                className="mt-3 flex min-h-11 w-full items-center justify-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700"
              >
                <LogOut className="h-4 w-4" />
                Sair
              </button>
            </div>
          </aside>
        </div>
      )}

      {/* Main Content */}
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-30 flex items-center justify-between border-b border-slate-200 bg-white/95 px-4 py-3 backdrop-blur-xs lg:hidden">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setMobileMenuOpen(true)}
              aria-label="Abrir menu de navegação"
              className="flex h-11 w-11 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
            >
              <Menu className="h-5 w-5" />
            </button>
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-700 text-white">
                <HeartPulse className="h-4 w-4" />
              </div>
              <span className="font-bold text-slate-900">Vitalis</span>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <span className="hidden text-xs font-medium text-slate-600 sm:inline">
              {user?.nome}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              aria-label="Sair"
              className="flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-100"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </header>

        <main className="mx-auto w-full max-w-7xl flex-1 p-4 sm:p-6 lg:p-8">
          {children}
        </main>
      </div>
    </div>
  );
}
