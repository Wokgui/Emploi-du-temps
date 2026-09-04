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
                  .gapToday{background:#f4f1ff;position:relative}
                  .gapToday:before{content:"";position:absolute;left:0;top:5px;bottom:5px;width:5px;background:#8065d1}
                  .gapToday .label{color:#6244a8}
                  .gapToday .room{color:#75688e}
                  .gapBadge{font-size:.75rem;font-weight:800;color:#6244a8;background:#e9e2ff;border-radius:999px;padding:5px 8px}
                  .gapTodayNow{box-shadow:inset 0 0 0 2px #8065d1}
                  .gapWeekCell{background:#f4f1ff!important;color:#6244a8!important;font-weight:850!important}
                  .gapWeekCell:active{background:#e9e2ff!important}
                  .gapWeekTitle{font-size:.82rem;font-weight:850;line-height:1.1}
                  #viewWeek>.note,#viewEdit>.note,#lunchSettingInfo{display:none!important}
                  @media(max-width:560px){.header h1{transform:translateY(7px)}}
                `;
                document.head.appendChild(st);

                function minutes(t) {
                  const p = String(t || '00:00').split(':').map(Number);
                  return (p[0] || 0) * 60 + (p[1] || 0);
                }

                function clock(m) {
                  const h = Math.floor(m / 60), mm = m % 60;
                  return String(h).padStart(2,'0') + ':' + String(mm).padStart(2,'0');
                }

                function durationLabel(m) {
                  const h = Math.floor(m / 60), mm = m % 60;
                  if (h && mm) return h + ' h ' + mm;
                  if (h) return h + ' h';
                  return mm + ' min';
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

                function coursesForDay(d) {
                  try {
                    const list = (typeof state !== 'undefined' && state[d] && Array.isArray(state[d].courses))
                      ? state[d].courses : [];
                    return [...list].sort((a,b)=>minutes(a.start)-minutes(b.start));
                  } catch(e) {
                    return [];
                  }
                }

                function gapSegmentsForCourses(courses) {
                  const out = [];
                  if (!courses || courses.length < 2) return out;
                  const l = lunch(), ls = minutes(l.start), le = minutes(l.end);
                  for (let i = 0; i < courses.length - 1; i++) {
                    const from = minutes(courses[i].end);
                    const to = minutes(courses[i + 1].start);
                    if (to <= from) continue;
                    if (to <= ls || from >= le) {
                      out.push({start:from,end:to});
                    } else {
                      if (from < ls && ls > from) out.push({start:from,end:Math.min(to,ls)});
                      if (to > le && to > Math.max(from,le)) out.push({start:Math.max(from,le),end:to});
                    }
                  }
                  return out.filter(g=>g.end>g.start);
                }

                function currentGap(courses) {
                  const now = new Date(), m = now.getHours() * 60 + now.getMinutes();
                  return gapSegmentsForCourses(courses).find(g=>m>=g.start && m<g.end) || null;
                }

                function spansLunch(courses) {
                  const l = lunch(), ls = minutes(l.start), le = minutes(l.end);
                  return courses.some(c=>minutes(c.end)<=ls) && courses.some(c=>minutes(c.start)>=le);
                }

                window.progressPercent = function() {
                  try {
                    const d = typeof todayKey === 'function' ? todayKey() : (new Date().getDay() + 1);
                    const courses = coursesForDay(d);
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
                  const bar = document.getElementById('todayProgress');
                  if (bar) {
                    bar.style.width = window.progressPercent() + '%';
                    bar.style.background = 'linear-gradient(90deg,#1178e8,#2d91ef)';
                  }
                  try {
                    const d = typeof todayKey === 'function' ? todayKey() : (new Date().getDay() + 1);
                    const courses = coursesForDay(d);
                    const now = new Date(), nowM = now.getHours() * 60 + now.getMinutes();
                    const l = lunch(), ls = minutes(l.start), le = minutes(l.end);
                    const gap = currentGap(courses);
                    if (head) {
                      if (gap) {
                        head.textContent = 'Trou en cours · ' + clock(gap.start) + '–' + clock(gap.end);
                        head.style.color = '#7357b8';
                      } else if (courses.length && nowM >= ls && nowM < le) {
                        head.textContent = 'Pause de midi · avancement figé';
                        head.style.color = '#9a5c09';
                      } else {
                        head.textContent = "Avancement de l’emploi du temps";
                        head.style.color = '#08a7a5';
                      }
                    }
                    const startEl = document.getElementById('scaleStart');
                    const endEl = document.getElementById('scaleEnd');
                    if (courses.length) {
                      if (startEl) startEl.textContent = courses[0].start;
                      if (endEl) endEl.textContent = courses[courses.length - 1].end;
                    }
                  } catch(e) {}
                }

                function makeTodayGap(g) {
                  const now = new Date(), nowM = now.getHours() * 60 + now.getMinutes();
                  const active = nowM >= g.start && nowM < g.end;
                  const row = document.createElement('div');
                  row.className = 'todayCourse gapToday' + (active ? ' gapTodayNow' : '');
                  row.innerHTML = '<div class="time"><strong>' + clock(g.start) + '</strong><br>' + clock(g.end) + '</div>' +
                    '<div><div class="label">Trou dans l’emploi du temps</div><div class="room">' + durationLabel(g.end-g.start) + ' sans cours</div></div>' +
                    '<div class="gapBadge">' + (active ? 'Maintenant' : 'Trou') + '</div>';
                  return row;
                }

                function makeTodayLunch(l) {
                  const row = document.createElement('div');
                  row.className = 'todayCourse lunchToday';
                  row.innerHTML = '<div class="time"><strong>' + l.start + '</strong><br>' + l.end + '</div>' +
                    '<div><div class="label">Pause de midi</div><div class="room">Reprise à ' + l.end + '</div></div>' +
                    '<div class="lunchBadge">Pause</div>';
                  return row;
                }

                function decorateToday() {
                  const box = document.getElementById('todayList');
                  if (!box) return;
                  decorateTimeline();
                  if (box.querySelector('.lunchToday,.gapToday')) return;
                  const rows = [...box.querySelectorAll('.todayCourse')];
                  if (!rows.length) return;

                  const d = typeof todayKey === 'function' ? todayKey() : (new Date().getDay() + 1);
                  const courses = coursesForDay(d);
                  const additions = gapSegmentsForCourses(courses).map(g=>({type:'gap',start:g.start,end:g.end,g}));
                  const l = lunch();
                  if (spansLunch(courses)) additions.push({type:'lunch',start:minutes(l.start),end:minutes(l.end),l});
                  additions.sort((a,b)=>a.start-b.start);

                  for (const item of additions) {
                    const node = item.type === 'gap' ? makeTodayGap(item.g) : makeTodayLunch(item.l);
                    let before = null;
                    for (const courseRow of rows) {
                      const t = timeFromRow(courseRow);
                      if (t.start && minutes(t.start) >= item.end) {
                        before = courseRow;
                        break;
                      }
                    }
                    if (before) box.insertBefore(node, before);
                    else box.appendChild(node);
                  }
                }

                function decorateWeek() {
                  const grid = document.getElementById('weekGrid');
                  if (!grid || grid.querySelector('.lunchWeekCell')) return;
                  const children = [...grid.children];
                  if (children.length < 6) return;

                  const rowCount = Math.floor((children.length - 6) / 6);
                  const days = [2,3,4,5,6];
                  for (let r = 0; r < rowCount; r++) {
                    const base = 6 + r * 6;
                    const found = String(children[base].textContent || '').match(/[0-2][0-9]:[0-5][0-9]/g) || [];
                    if (!found[0] || !found[1]) continue;
                    const rs = minutes(found[0]), re = minutes(found[1]);
                    days.forEach((d,di)=>{
                      const cell = children[base + 1 + di];
                      if (!cell || cell.classList.contains('has')) return;
                      const gap = gapSegmentsForCourses(coursesForDay(d)).find(g=>Math.min(re,g.end)>Math.max(rs,g.start));
                      if (gap) {
                        cell.classList.add('gapWeekCell');
                        cell.innerHTML = '<div class="gapWeekTitle">Trou</div>';
                      }
                    });
                  }

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

                function removeExplanatoryText() {
                  document.querySelectorAll('#viewWeek>.note,#viewEdit>.note,#lunchSettingInfo').forEach(el => el.remove());
                }

                let queued = false;
                function refresh() {
                  if (queued) return;
                  queued = true;
                  setTimeout(() => {
                    queued = false;
                    removeExplanatoryText();
                    decorateTimeline();
                    decorateToday();
                    decorateWeek();
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
