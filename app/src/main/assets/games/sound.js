// Retro sound effects via Web Audio API
// Synthesized beeps — no audio files needed
(function() {
  let ctx;
  function getCtx() {
    if (!ctx) {
      ctx = new (window.AudioContext || window.webkitAudioContext)();
    }
    return ctx;
  }

  // Resume on first touch (autoplay policy)
  document.addEventListener('touchstart', () => { try { getCtx().resume(); } catch(e){} }, { once: true });
  document.addEventListener('click', () => { try { getCtx().resume(); } catch(e){} }, { once: true });

  function beep(freq, duration, type, vol) {
    try {
      const c = getCtx();
      const o = c.createOscillator();
      const g = c.createGain();
      o.type = type || 'square';
      o.frequency.value = freq;
      g.gain.value = vol || 0.08;
      g.gain.exponentialRampToValueAtTime(0.001, c.currentTime + duration);
      o.connect(g);
      g.connect(c.destination);
      o.start(c.currentTime);
      o.stop(c.currentTime + duration);
    } catch(e) {}
  }

  function noise(duration, vol) {
    try {
      const c = getCtx();
      const bufSize = c.sampleRate * duration;
      const buf = c.createBuffer(1, bufSize, c.sampleRate);
      const data = buf.getChannelData(0);
      for (let i = 0; i < bufSize; i++) data[i] = (Math.random() * 2 - 1) * 0.5;
      const src = c.createBufferSource();
      const g = c.createGain();
      src.buffer = buf;
      g.gain.value = vol || 0.06;
      g.gain.exponentialRampToValueAtTime(0.001, c.currentTime + duration);
      src.connect(g);
      g.connect(c.destination);
      src.start();
    } catch(e) {}
  }

  window.SFX = {
    // Pong
    paddleHit:  () => beep(440, 0.05, 'square', 0.1),
    wallHit:    () => beep(220, 0.03, 'square', 0.06),
    score:      () => beep(880, 0.15, 'sine', 0.1),

    // Snake
    eat:        () => beep(660, 0.06, 'square', 0.08),
    die:        () => { beep(200, 0.2, 'sawtooth', 0.1); setTimeout(() => beep(100, 0.3, 'sawtooth', 0.08), 150); },

    // Tetris
    move:       () => beep(300, 0.02, 'square', 0.04),
    rotate:     () => beep(500, 0.03, 'square', 0.06),
    drop:       () => beep(150, 0.06, 'triangle', 0.08),
    lineClear:  () => { beep(523, 0.08, 'square', 0.1); setTimeout(() => beep(659, 0.08, 'square', 0.1), 80); setTimeout(() => beep(784, 0.12, 'square', 0.1), 160); },
    tetris:     () => { for(let i=0;i<4;i++) setTimeout(() => beep(523+i*130, 0.1, 'square', 0.12), i*70); },
    levelUp:    () => { beep(440, 0.1, 'sine', 0.1); setTimeout(() => beep(660, 0.1, 'sine', 0.1), 100); setTimeout(() => beep(880, 0.15, 'sine', 0.12), 200); },

    // Missile Command / Paratrooper
    shoot:      () => beep(800, 0.04, 'sawtooth', 0.06),
    explosion:  () => noise(0.3, 0.1),
    bigExplosion: () => { noise(0.5, 0.15); beep(60, 0.4, 'sine', 0.08); },
    missileWarn: () => beep(180, 0.08, 'sawtooth', 0.04),
    cityDestroyed: () => { noise(0.4, 0.12); beep(80, 0.5, 'sine', 0.1); },

    // Arkanoid
    brickHit:   () => beep(520, 0.04, 'square', 0.07),
    brickBreak: () => beep(700, 0.06, 'square', 0.09),
    paddleBounce: () => beep(350, 0.03, 'triangle', 0.08),
    loseLife:   () => { beep(300, 0.15, 'sawtooth', 0.1); setTimeout(() => beep(150, 0.2, 'sawtooth', 0.08), 120); },
    powerUp:    () => { beep(440, 0.06, 'sine', 0.1); setTimeout(() => beep(880, 0.1, 'sine', 0.1), 60); },

    // Minesweeper
    click:      () => beep(600, 0.02, 'square', 0.05),
    flag:       () => beep(400, 0.04, 'triangle', 0.06),
    boom:       () => { noise(0.6, 0.2); beep(50, 0.5, 'sine', 0.1); },
    win:        () => { for(let i=0;i<5;i++) setTimeout(() => beep(440+i*110, 0.12, 'sine', 0.1), i*100); },

    // Cheburashka-RKN (Exodus)
    stomp:      () => beep(55, 0.15, 'sine', 0.12),
    crunch:     () => { noise(0.15, 0.1); beep(80, 0.1, 'sawtooth', 0.08); },

    // General
    gameOver:   () => { beep(400, 0.2, 'sawtooth', 0.1); setTimeout(() => beep(300, 0.2, 'sawtooth', 0.1), 200); setTimeout(() => beep(200, 0.4, 'sawtooth', 0.1), 400); },
    menuSelect: () => beep(500, 0.04, 'square', 0.06),
  };
})();
