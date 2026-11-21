import { useEffect, type ReactNode } from 'react';
import { AlertTriangle, ShieldAlert, X } from 'lucide-react';

interface ConfirmModalProps {
  open: boolean;
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'primary' | 'warning' | 'danger';
  loading?: boolean;
  confirmDisabled?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  children?: ReactNode;
}

export function ConfirmModal({
  open,
  title,
  description,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  variant = 'primary',
  loading = false,
  confirmDisabled = false,
  onConfirm,
  onCancel,
  children,
}: ConfirmModalProps) {
  useEffect(() => {
    if (!open) return;
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !loading) onCancel();
    };
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [open, loading, onCancel]);

  if (!open) return null;

  const variantClasses = {
    primary: {
      iconWrap: 'bg-blue-100 text-blue-700',
      button: 'bg-blue-700 hover:bg-blue-800 text-white focus:ring-blue-500',
      icon: <AlertTriangle className="h-5 w-5" />,
    },
    warning: {
      iconWrap: 'bg-amber-100 text-amber-700',
      button: 'bg-amber-600 hover:bg-amber-700 text-white focus:ring-amber-500',
      icon: <AlertTriangle className="h-5 w-5" />,
    },
    danger: {
      iconWrap: 'bg-red-100 text-red-700',
      button: 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500',
      icon: <ShieldAlert className="h-5 w-5" />,
    },
  }[variant];

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-modal-title"
    >
      <div className="w-full max-w-lg rounded-2xl border border-slate-200 bg-white shadow-2xl">
        <div className="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-4">
          <div className="flex items-center gap-3">
            <div className={`rounded-xl p-2.5 ${variantClasses.iconWrap}`}>
              {variantClasses.icon}
            </div>
            <h2 id="confirm-modal-title" className="text-lg font-semibold text-slate-900">
              {title}
            </h2>
          </div>
          <button
            type="button"
            disabled={loading}
            onClick={onCancel}
            aria-label="Fechar modal"
            className="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <div className="px-6 py-4">
          {description && (
            <p className="text-sm leading-relaxed text-slate-600">{description}</p>
          )}
          {children && <div className={description ? 'mt-4' : ''}>{children}</div>}
        </div>

        <div className="flex flex-col-reverse gap-2.5 border-t border-slate-100 bg-slate-50/70 px-6 py-4 sm:flex-row sm:justify-end rounded-b-2xl">
          <button
            type="button"
            disabled={loading}
            onClick={onCancel}
            className="min-h-11 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:opacity-50"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            disabled={loading || confirmDisabled}
            onClick={onConfirm}
            className={`min-h-11 rounded-xl px-5 py-2.5 text-sm font-semibold shadow-xs transition focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${variantClasses.button}`}
          >
            {loading ? 'Processando...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
