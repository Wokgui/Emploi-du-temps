package com.wokgui.schedulewidget;

final class PronoteUi {
    private PronoteUi() {}

    static String script() {
        return """
            (function(){
              try {
                if (!document.getElementById('pronoteExtraStyle')) {
                  const st = document.createElement('style');
                  st.id = 'pronoteExtraStyle';
                  st.textContent = `
                    .pronote-card{margin-top:18px;padding:16px;border-left:5px solid #08a7a5}
                    .pronote-head{display:flex;align-items:center;justify-content:space-between;gap:10px}
                    .pronote-title{font-weight:850;color:#111936;font-size:1.05rem}
                    .pronote-status{color:#68738a;font-size:.86rem;margin-top:5px;line-height:1.35}
                    .pronote-actions{display:flex;gap:8px;margin-top:12px;flex-wrap:wrap}
                    .pronote-btn{border:1px solid #dce3eb;border-radius:8px;background:#fff;color:#1178e8;padding:10px 12px;font-weight:760}
                    .pronote-btn.primary{background:#08a7a5;border-color:#08a7a5;color:#fff}
                    .pronote-homework{margin-top:14px}
                    .pronote-homework h3,.pronote-imported h3{margin:0 0 8px;font-size:1.02rem}
                    .pronote-task{padding:11px 0;border-top:1px solid #e9edf2}
                    .pronote-task:first-of-type{border-top:0}
                    .pronote-task-course{font-weight:800;color:#087f7d}
                    .pronote-task-text{margin-top:4px;color:#4d596e;font-size:.88rem;line-height:1.4}
                    .pronote-dot{display:inline-flex;align-items:center;justify-content:center;position:absolute;right:4px;top:4px;min-width:22px;height:20px;border-radius:99px;background:#08a7a5;color:#fff;font-size:10px;font-weight:850;padding:0 5px}
                    .wc.pronote-cell{position:relative}
                    .pronote-muted{color:#68738a;font-size:.86rem;padding:8px 0;line-height:1.4}
                    .pronote-imported{margin-top:14px;padding-top:12px;border-top:1px solid #e9edf2}
                    .pronote-raw{padding:9px 0;border-top:1px solid #edf1f5}
                    .pronote-raw:first-of-type{border-top:0}
                    .pronote-raw-index{font-size:.72rem;color:#08a7a5;font-weight:850;text-transform:uppercase;letter-spacing:.04em}
                    .pronote-raw-text{margin-top:3px;color:#4d596e;font-size:.86rem;line-height:1.38}
                  `;
                  document.head.appendChild(st);
                }

                const edit = document.getElementById('viewEdit');
                if (edit && !document.getElementById('pronoteSettingsCard')) {
                  const card = document.createElement('section');
                  card.id = 'pronoteSettingsCard';
                  card.className = 'card pronote-card';
                  card.innerHTML = `
                    <div class="pronote-head">
                      <div>
                        <div class="pronote-title">PRONOTE · Cahier de textes / Travail à faire</div>
                        <div id="pronoteStatusText" class="pronote-status">Aucun import pour le moment</div>
                      </div>
                    </div>
                    <div class="pronote-actions">
                      <button id="pronoteOpen" class="pronote-btn primary" type="button">Connecter / synchroniser</button>
                      <button id="pronoteClear" class="pronote-btn" type="button">Effacer l’import</button>
                    </div>
                    <div id="pronoteImportedList" class="pronote-imported"></div>`;
                  edit.appendChild(card);
                  document.getElementById('pronoteOpen').onclick = () => AndroidPronoteApp.openPronote();
                  document.getElementById('pronoteClear').onclick = () => {
                    AndroidPronoteApp.clear();
                    setTimeout(() => window.refreshPronoteUi && window.refreshPronoteUi(), 80);
                  };
                }

                const today = document.getElementById('viewToday');
                if (today && !document.getElementById('pronoteTodayCard')) {
                  const card = document.createElement('section');
                  card.id = 'pronoteTodayCard';
                  card.className = 'card pronote-card pronote-homework';
                  card.style.display = 'none';
                  today.appendChild(card);
                }

                function clean(t) {
                  let s = String(t || '');
                  s = s.replaceAll(String.fromCharCode(10), ' ')
                       .replaceAll(String.fromCharCode(13), ' ')
                       .replaceAll(String.fromCharCode(9), ' ')
                       .trim();
                  while (s.includes('  ')) s = s.replaceAll('  ', ' ');
                  return s;
                }

                function escapeHtml(t) {
                  return String(t || '')
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;');
                }

                function norm(t) {
                  const chars = clean(t).normalize('NFD').split('').filter(ch => {
                    const n = ch.charCodeAt(0);
                    return n < 768 || n > 879;
                  }).join('');
                  return chars.toUpperCase().replace(/[^A-Z0-9]/g, '');
                }

                function tokens(label) {
                  const u = clean(label).toUpperCase();
                  const out = u.match(/[3-6]G[0-9](?:BIL)?/g) || [];
                  const grouped = u.match(/([3-6]G)([0-9](?:-[0-9])+)/);
                  if (grouped) grouped[2].split('-').forEach(n => out.push(grouped[1] + n));
                  return [...new Set(out)];
                }

                function blockText(b) {
                  return clean(((b && b.context) || '') + ' ' + ((b && b.text) || ''));
                }

                function snapshot() {
                  try { return JSON.parse(AndroidPronoteApp.homework() || '{}'); }
                  catch (e) { return {}; }
                }

                function status() {
                  try { return JSON.parse(AndroidPronoteApp.status() || '{}'); }
                  catch (e) { return {}; }
                }

                function matches(label, data) {
                  const ts = tokens(label);
                  const blocks = Array.isArray(data.blocks) ? data.blocks : [];
                  if (!ts.length) return [];
                  return blocks.filter(b => {
                    const n = norm(blockText(b));
                    return ts.some(t => n.includes(norm(t)));
                  });
                }

                function snippet(t, max = 300) {
                  let s = clean(t);
                  if (s.length > max) s = s.slice(0, max - 1) + '…';
                  return s;
                }

                function uniqueBlocks(data) {
                  const blocks = Array.isArray(data.blocks) ? data.blocks : [];
                  const seen = new Set();
                  const out = [];
                  for (const b of blocks) {
                    const t = blockText(b);
                    if (!t) continue;
                    const key = norm(t).slice(0, 500);
                    if (seen.has(key)) continue;
                    seen.add(key);
                    out.push(b);
                    if (out.length >= 12) break;
                  }
                  return out;
                }

                function renderStatus() {
                  const s = status();
                  const el = document.getElementById('pronoteStatusText');
                  if (!el) return;
                  if (!s.hasData) {
                    el.textContent = 'Aucune donnée importée. Ouvre PRONOTE, affiche le cahier de textes ou Travail à faire, puis touche Importer.';
                    return;
                  }
                  const d = new Date(Number(s.importedAt || 0));
                  el.textContent = 'Dernier import : ' + d.toLocaleString('fr-FR') + ' · ' + Number(s.blocks || 0) + ' bloc(s) détecté(s)';
                }

                function renderImported() {
                  const holder = document.getElementById('pronoteImportedList');
                  if (!holder) return;
                  const data = snapshot();
                  const rows = uniqueBlocks(data);
                  if (!rows.length) {
                    holder.innerHTML = '<div class="pronote-muted">Aucun élément exploitable n’a été détecté dans la dernière page importée.</div>';
                    return;
                  }
                  holder.innerHTML = '<h3>Éléments réellement importés</h3>' + rows.map((b, i) => {
                    const text = snippet(blockText(b), 420);
                    return '<div class="pronote-raw"><div class="pronote-raw-index">Élément ' + (i + 1) + '</div><div class="pronote-raw-text">' + escapeHtml(text) + '</div></div>';
                  }).join('');
                }

                function renderToday() {
                  const card = document.getElementById('pronoteTodayCard');
                  if (!card) return;
                  const data = snapshot();
                  const imported = uniqueBlocks(data);
                  const day = new Date().getDay() + 1;
                  let courses = [];
                  try {
                    courses = (window.state && state[day] && Array.isArray(state[day].courses)) ? state[day].courses : [];
                  } catch (e) {}

                  const rows = [];
                  const used = new Set();
                  for (const c of courses) {
                    for (const b of matches(c.label, data)) {
                      const txt = blockText(b);
                      const key = norm(txt).slice(0, 500);
                      if (!txt || used.has(key)) continue;
                      used.add(key);
                      rows.push({course: c.label, text: txt});
                      if (rows.length >= 8) break;
                    }
                    if (rows.length >= 8) break;
                  }

                  if (rows.length) {
                    card.style.display = 'block';
                    card.innerHTML = '<h3>Travail à faire · PRONOTE</h3>' + rows.map(r =>
                      '<div class="pronote-task"><div class="pronote-task-course">' + escapeHtml(r.course) + '</div><div class="pronote-task-text">' + escapeHtml(snippet(r.text, 360)) + '</div></div>'
                    ).join('');
                  } else if (imported.length) {
                    card.style.display = 'block';
                    card.innerHTML = '<h3>PRONOTE synchronisé</h3><div class="pronote-muted">Des éléments ont bien été importés, mais aucun n’a pu être associé automatiquement aux cours d’aujourd’hui. Ils restent visibles dans Modifier → PRONOTE.</div>';
                  } else {
                    card.style.display = 'none';
                    card.innerHTML = '';
                  }
                }

                function renderWeek() {
                  const data = snapshot();
                  document.querySelectorAll('#weekGrid .pronote-dot').forEach(x => x.remove());
                  document.querySelectorAll('#weekGrid .wc.has').forEach(cell => {
                    const labelEl = cell.querySelector('.cellLabel');
                    const label = labelEl ? labelEl.textContent : cell.textContent;
                    if (matches(label, data).length) {
                      cell.classList.add('pronote-cell');
                      const dot = document.createElement('span');
                      dot.className = 'pronote-dot';
                      dot.textContent = 'P';
                      dot.title = 'Cahier de textes / travail PRONOTE détecté';
                      cell.appendChild(dot);
                    }
                  });
                }

                window.refreshPronoteUi = function() {
                  renderStatus();
                  renderImported();
                  renderToday();
                  renderWeek();
                };

                document.querySelectorAll('.nav').forEach(b => {
                  if (!b.dataset.pronoteHook) {
                    b.dataset.pronoteHook = '1';
                    b.addEventListener('click', () => setTimeout(window.refreshPronoteUi, 120));
                  }
                });

                window.refreshPronoteUi();
              } catch (e) {
                console.log('Pronote UI', e);
              }
            })();
            """;
    }
}
