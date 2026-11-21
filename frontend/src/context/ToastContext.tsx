import {
  createContext,
  useCallback,
  useContext,
  useState,
  type ReactNode,
} from 'react';
import { AlertTriangle, CheckCircle2, Info, X, XCircle } from 'lucide-react';

export type ToastVariant = 'success' | 'error' | 'warning' | 'info';

interface ToastItem {
  id: number;
  variant: ToastVariant;
  title: string;
  description?: string;
}

interface ToastContextValue {
  notify: (variant: ToastVariant, title: string, description?: string) => void;
  success: (title: string, description?: string) => void;
  error: (title: string, description?: string) => void;
  warning: (title: string, description?: string) => void;
  info: (title: string, description?: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

let toastSeq = 0;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const dismiss = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const notify = useCallback(
    (variant: ToastVariant, title: string, description?: string) => {
      const id = ++toastSeq;
      setToasts((prev) => [...prev, { id, variant, title, description }]);
      window.setTimeout(() => dismiss(id), variant === 'error' ? 7000 : 4800);
    },
    [dismiss]
  );

  const value: ToastContextValue = {
    notify,
    success: (title, desc) => notify('success', title, desc),
    error: (title, desc) => notify('error', title, desc),
    warning: (title, desc) => notify('warning', title, desc),
    info: (title, desc) => notify('info', title, desc),
  };

  const styles: Record<ToastVariant, { box: string; icon: ReactNode }> = {
    success: {
      box: 'border-emerald-200 bg-emerald-50 text-emerald-950',
      icon: <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-600" />,
    },
    error: {
      box: 'border-red-200 bg-red-50 text-red-950',
      icon: <XCircle className="h-5 w-5 shrink-0 text-red-600" />,
    },
    warning: {
      box: 'border-amber-200 bg-amber-50 text-amber-950',
      icon: <AlertTriangle className="h-5 w-5 shrink-0 text-amber-600" />,
    },
    info: {
      box: 'border-sky-200 bg-sky-50 text-sky-950',
      icon: <Info className="h-5 w-5 shrink-0 text-sky-600" />,
    },
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        aria-live="polite"
        className="pointer-events-none fixed top-4 right-4 z-50 flex w-full max-w-sm flex-col gap-2.5 px-4 sm:px-0"
      >
        {toasts.map((toast) => {
          const style = styles[toast.variant];
          return (
            <div
              key={toast.id}
              role="status"
              className={`pointer-events-auto flex items-start gap-3 rounded-xl border p-3.5 shadow-lg transition-all ${style.box}`}
            >
              {style.icon}
              <div className="min-w-0 flex-1">
                <p className="text-sm font-semibold">{toast.title}</p>
                {toast.description && (
                  <p className="mt-0.5 text-xs opacity-90 leading-relaxed">
                    {toast.description}
                  </p>
                )}
              </div>
              <button
                type="button"
                onClick={() => dismiss(toast.id)}
                aria-label="Fechar notificação"
                className="rounded-lg p-1 opacity-60 transition hover:bg-black/5 hover:opacity-100"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error('useToast deve ser usado dentro de ToastProvider');
  }
  return ctx;
}
