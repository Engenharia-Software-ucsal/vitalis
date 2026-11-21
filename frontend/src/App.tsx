import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { useAuth } from './context/AuthContext';
import { LoginPage } from './pages/LoginPage';
import { PacientesPage } from './pages/PacientesPage';
import { TriagemPage } from './pages/TriagemPage';
import { ProntuarioPage } from './pages/ProntuarioPage';
import { LeitosPage } from './pages/LeitosPage';
import { hasAnyRole } from './utils/clinical';

function DefaultLanding() {
  const { user } = useAuth();
  if (hasAnyRole(user?.roles, ['LEITOS', 'GESTOR_LEITOS']) && !hasAnyRole(user?.roles, ['RECEPCAO', 'RECEPCIONISTA', 'ENFERMEIRO', 'MEDICO'])) {
    return <Navigate to="/leitos" replace />;
  }
  if (hasAnyRole(user?.roles, ['ENFERMEIRO', 'MEDICO']) && !hasAnyRole(user?.roles, ['RECEPCAO', 'RECEPCIONISTA'])) {
    return <Navigate to="/triagem" replace />;
  }
  return <Navigate to="/pacientes" replace />;
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3">
          <div className="h-9 w-9 animate-spin rounded-full border-4 border-blue-200 border-t-blue-700" />
          <p className="text-sm font-medium text-slate-600">Validando sessão...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Layout>{children}</Layout>;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DefaultLanding />
          </ProtectedRoute>
        }
      />
      <Route
        path="/pacientes"
        element={
          <ProtectedRoute>
            <PacientesPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/triagem"
        element={
          <ProtectedRoute>
            <TriagemPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/pacientes/:id/prontuario"
        element={
          <ProtectedRoute>
            <ProntuarioPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/leitos"
        element={
          <ProtectedRoute>
            <LeitosPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
