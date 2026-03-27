// DOS-style game loader
// Include this in any game HTML to show a DOS loading screen before the game starts.
// Usage: add <script src="loader.js"></script> BEFORE the game's own script.
// The game should wait for window._gameReady before starting its loop.

(function() {
  const gameName = document.title || 'GAME';
  const exeName = gameName.toUpperCase().replace(/\s+/g, '').substring(0, 8) + '.EXE';

  // Create overlay
  const overlay = document.createElement('div');
  overlay.id = 'dos-loader';
  overlay.style.cssText = `
    position: fixed; inset: 0; z-index: 99999;
    background: #000; color: #AAA;
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 13px; line-height: 1.6;
    padding: 16px; white-space: pre;
    overflow: hidden;
  `;
  document.body.appendChild(overlay);

  // Fake file sizes
  const fileSize = Math.floor(Math.random() * 200 + 50) + ',';
  const fileSizeEnd = String(Math.floor(Math.random() * 999)).padStart(3, '0');

  const lines = [
    { text: `C:\\GAMES>${exeName}`, cls: '#FFF', delay: 300 },
    { text: '', delay: 100 },
    { text: `Loading ${exeName}...`, cls: '#AAA', delay: 200 },
    { text: `Reading ${fileSize}${fileSizeEnd} bytes`, cls: '#AAA', delay: 150 },
    { text: 'Initializing graphics...', cls: '#AAA', delay: 200 },
    { text: 'Sound Blaster 16 detected at 220h, IRQ 5, DMA 1', cls: '#AAA', delay: 100 },
    { text: 'Allocating conventional memory... OK', cls: '#AAA', delay: 150 },
    { text: '', delay: 100 },
    { text: `${gameName} (c) 19${70 + Math.floor(Math.random() * 25)}`, cls: '#FFF', delay: 200 },
    { text: '', delay: 200 },
  ];

  let i = 0;
  function type() {
    if (i >= lines.length) {
      // Done — remove overlay
      setTimeout(() => {
        overlay.style.transition = 'opacity 0.3s';
        overlay.style.opacity = '0';
        setTimeout(() => {
          overlay.remove();
          window._gameReady = true;
          window.dispatchEvent(new Event('gameready'));
        }, 300);
      }, 200);
      return;
    }

    const line = lines[i];
    const span = document.createElement('span');
    span.style.color = line.cls || '#AAA';
    span.textContent = line.text + '\n';
    overlay.appendChild(span);
    i++;
    setTimeout(type, line.delay);
  }

  setTimeout(type, 100);
})();
