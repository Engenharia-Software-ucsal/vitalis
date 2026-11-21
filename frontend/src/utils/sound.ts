let audioCtx: AudioContext | null = null;

function getAudioContext(): AudioContext | null {
  if (typeof window === 'undefined') return null;
  const AudioCtx =
    window.AudioContext ||
    (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
  if (!AudioCtx) return null;
  if (!audioCtx) {
    audioCtx = new AudioCtx();
  }
  if (audioCtx.state === 'suspended') {
    void audioCtx.resume();
  }
  return audioCtx;
}

export function playEmergencyAlertSound(): void {
  const ctx = getAudioContext();
  if (!ctx) return;

  const now = ctx.currentTime;
  const pulses = [
    { start: 0, freq: 880, duration: 0.16 },
    { start: 0.22, freq: 1046.5, duration: 0.16 },
    { start: 0.44, freq: 880, duration: 0.22 },
  ];

  for (const pulse of pulses) {
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(pulse.freq, now + pulse.start);

    gain.gain.setValueAtTime(0.0001, now + pulse.start);
    gain.gain.exponentialRampToValueAtTime(0.18, now + pulse.start + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + pulse.start + pulse.duration);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start(now + pulse.start);
    osc.stop(now + pulse.start + pulse.duration + 0.02);
  }
}
