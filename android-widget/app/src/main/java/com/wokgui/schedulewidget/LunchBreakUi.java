package com.wokgui.schedulewidget;

final class LunchBreakUi {
    private LunchBreakUi() {}

    static String script() {
        return """
            (function(){
              try {
                if (window.__lunchBreakInstalled) {
                  if (window.refreshLunchBreakUi) window.refreshLunchBreakUi();
                  return;
                }
                window.__lunchBreakInstalled = true;

                const st = document.createElement('style');
                st.id = 'lunchBreakStyle';
                st.textContent = `
                  .lunchToday{background:#fff8eb;position:relative}
                  .lunchToday:before{content:"";position:absolute;left:0;top:5px;bottom:5px;width:5px;background:#f0a33a}
                  .lunchToday .label{color:#9a5c09}
                  .lunchToday .room{color:#8b6a3a}
                  .lunchBadge{font-size:.75rem;font-weight:800;color:#9a5c09;background:#ffedca;border-radius:999px;padding:5px 8px}
                  .lunchWeekTime,.lunchWeekCell{min-height:52px!important;background:#fff8eb!important;color:#9a5c09!important}
                  .lunchWeekCell{font-weight:800;font-size:.82rem}
                  .lunchSettingInfo{margin-top:10px;padding:12px 14px;border:1px solid #f3d5a1;border-radius:8px;background:#fff8eb;color:#7d5826;line-height:1.35}
                  .lunchSettingInfo strong{color:#9a5c09}
                  .lunchSettingInfo small{display:block;margin-top:3px;color:#8a7353}
                `;
                document.head.appendChild(st);

                function minutes(t) {
                  const p = String(t || '00:00').split(':').map(Number);
                  return (p[0] || 0) * 60 + (p[1] || 0);
                }

                function lunch() {
                  try {
                    if (typeof slots !== 'undefined' && Array.isArray(slots) && slots.length >= 5) {
                      const start = String(slots[3].end || '12:00');
                      const end = String(slots[4].start || '13:00');
                      if (minutes(end) > minutes(start)) return {start, end};
                    }
                  } catch(e) {}
                  return {start:'12:00', end:'13:00'};
                }

                // La barre n'avance que pendant les cours réellement programmés.
                // Avant un cours, entre deux cours et pendant la pause de midi, elle reste figée.
                window.progressPercent = function() {
                  try {
                    const d = typeof todayKey === 'function' ? todayKey() : (new Date().getDay() + 1);
                    const courses = (typeof state !== 'undefined' && state[d] && Array.isArray(state[d].courses))
                      ? state[d].courses : [];
                    if (!courses.length) return 0;

                    const now = new Date();
                    const nowM = now.getHours() * 60 + now.getMinutes();
                    let total = 0;
                    let done = 0;
                    for (const c of courses) {
                      const start = minutes(c.start);
                      const end = minutes(c.end);
                      const duration = Math.max(0, end - start);
                      if (!duration) continue;
                      total += duration;
                      if (nowM >= end) done += duration;
                      else if (nowM > start) done += Math.min(duration, nowM - start);
                    }
                    return total > 0 ? Math.max(0, Math.min(100, done * 100 / total)) : 0;
                  } catch(e) {
                    return 0;
                  }
                };

                function timeFromRow(row) {
                  const node = row.querySelector('.time');
                  const found = String(node ? node.textContent : '').match(/[0-2][0-9]:[0-5][0-9]/g) || [];
                  return {start:found[0] || '', end:found[1] || found[0] || ''};
                }

                function decorateTimeline() {
                  const head = document.querySelector('.timelineHead strong');
                  if (head) head.textContent = "Avancement de l’emploi du temps";
                  const bar = document.getElementById('todayProgress');
                  if (bar) {
                    bar.style.width = window.progressPercent() + '%';
                    bar.style.background = 'linear-gradient(90deg,#1178e8,#2d91ef)';
                  }
                  try {
                    const d = typeof todayKey === 'function' ? todayKey() : (new Date().getDay() + 1);
                    const courses = (typeof state !== 'undefined' && state[d] && Array.isArray(state[d].courses))
                      ? state[d].courses : [];
                    const startEl = document.getElementById('scaleStart');
                    const endEl = document.getElementById('scaleEnd');
                    if (courses.length) {
                      if (startEl) startEl.textContent = courses[0].start;
                      if (endEl) endEl.textContent = courses[courses.length - 1].end;
                    }
                  } catch(e) {}
                }

                function decorateToday() {
                  const box = document.getElementById('todayList');
                  if (!box) return;
                  decorateTimeline();
                  if (box.querySelector('.lunchToday')) return;
                  const rows = [...box.querySelectorAll('.todayCourse')];
                  if (!rows.length) return;
                  const l = lunch();
                  const row = document.createElement('div');
                  row.className = 'todayCourse lunchToday';
                  row.innerHTML = '<div class="time"><strong>' + l.start + '</strong><br>' + l.end + '</div>' +
                    '<div><div class="label">Pause de midi</div><div class="room">Reprise à ' + l.end + '</div></div>' +
                    '<div class="lunchBadge">Pause</div>';

                  let before = null;
                  for (const courseRow of rows) {
                    const t = timeFromRow(courseRow);
                    if (t.start && minutes(t.start) >= minutes(l.end)) {
                      before = courseRow;
                      break;
                    }
                  }
                  if (before) box.insertBefore(row, before);
                  else box.appendChild(row);
                }

                function decorateWeek() {
                  const grid = document.getElementById('weekGrid');
                  if (!grid || grid.querySelector('.lunchWeekCell')) return;
                  const children = [...grid.children];
                  if (children.length < 6) return;
                  const l = lunch();
                  let before = null;
                  for (let i = 6; i < children.length; i += 6) {
                    const found = String(children[i].textContent || '').match(/[0-2][0-9]:[0-5][0-9]/g) || [];
                    if (found[0] && minutes(found[0]) >= minutes(l.end)) {
                      before = children[i];
                      break;
                    }
                  }

                  const frag = document.createDocumentFragment();
                  const time = document.createElement('div');
                  time.className = 'wh timecol lunchWeekTime';
                  time.innerHTML = l.start + '<br>' + l.end;
                  frag.appendChild(time);
                  for (let i = 0; i < 5; i++) {
                    const cell = document.createElement('div');
                    cell.className = 'wc lunchWeekCell';
                    cell.textContent = 'Pause de midi';
                    frag.appendChild(cell);
                  }
                  grid.insertBefore(frag, before);
                }

                function decorateEdit() {
                  const slotsBox = document.getElementById('slotSettings');
                  if (!slotsBox) return;
                  const l = lunch();
                  let info = document.getElementById('lunchSettingInfo');
                  if (!info) {
                    info = document.createElement('div');
                    info.id = 'lunchSettingInfo';
                    info.className = 'lunchSettingInfo';
                    slotsBox.insertAdjacentElement('afterend', info);
                  }
                  const html = '<strong>Pause de midi : ' + l.start + '–' + l.end + '</strong>' +
                    '<small>Elle est calculée automatiquement entre la fin de la 4e heure et le début de la 5e heure.</small>';
                  if (info.innerHTML !== html) info.innerHTML = html;
                }

                let queued = false;
                function refresh() {
                  if (queued) return;
                  queued = true;
                  setTimeout(() => {
                    queued = false;
                    decorateTimeline();
                    decorateToday();
                    decorateWeek();
                    decorateEdit();
                  }, 0);
                }
                window.refreshLunchBreakUi = refresh;

                ['todayList','weekGrid','slotSettings'].forEach(id => {
                  const node = document.getElementById(id);
                  if (node) new MutationObserver(refresh).observe(node, {childList:true, subtree:true});
                });
                document.querySelectorAll('.nav').forEach(b => b.addEventListener('click', () => setTimeout(refresh, 80)));
                setInterval(decorateTimeline, 30000);
                refresh();
              } catch(e) {
                console.log('Lunch break UI', e);
              }
            })();
            """;
    }
}
