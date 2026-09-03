package com.wokgui.schedulewidget;

final class PronoteCourseUi {
    private PronoteCourseUi() {}

    static String script() {
        return """
            (function(){
              try {
                if (!document.getElementById('pronoteCourseStyle')) {
                  const st = document.createElement('style');
                  st.id = 'pronoteCourseStyle';
                  st.textContent = `
                    .pronote-course-btn{margin-top:7px;border:1px solid #bfe9e6;border-radius:7px;background:#ecfbfa;color:#087f7d;padding:7px 10px;font-size:.79rem;font-weight:800}
                    .pronote-week-btn{margin-top:7px;border:1px solid #cfe5ff;border-radius:6px;background:#eef7ff;color:#0967bd;padding:5px 7px;font-size:.7rem;font-weight:800}
                    #pronoteCourseModal{position:fixed;inset:0;z-index:60;background:#0b17385c;display:none;align-items:flex-end}
                    #pronoteCourseModal.show{display:flex}
                    .pronote-course-sheet{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:15px 15px 0 0;padding:18px;max-height:82%;overflow:auto}
                    .pronote-course-head{display:flex;align-items:center;justify-content:space-between;gap:12px}
                    .pronote-course-head h3{margin:0;color:#111936;font-size:1.15rem}
                    .pronote-close{width:42px;height:42px;border:1px solid #dce3eb;border-radius:8px;background:#fff;color:#4d596e;font-size:1.2rem}
                    .pronote-course-meta{margin:5px 0 15px;color:#68738a;font-size:.84rem}
                    .pronote-home-section{border-top:1px solid #e7edf2;padding:13px 0}
                    .pronote-home-section:first-of-type{border-top:0}
                    .pronote-home-label{font-size:.77rem;font-weight:850;letter-spacing:.02em;text-transform:uppercase;color:#08a7a5}
                    .pronote-home-text{margin-top:6px;color:#26334b;line-height:1.42;font-size:.93rem;white-space:pre-wrap}
                    .pronote-home-empty{color:#8892a2}
                    .pronote-sync-now{width:100%;margin-top:10px;border:0;border-radius:8px;background:#1178e8;color:#fff;padding:12px 14px;font-weight:800}
                    @media(max-width:560px){.pronote-course-sheet{padding:16px}.pronote-course-btn{padding:6px 9px}}
                  `;
                  document.head.appendChild(st);
                }

                if (!document.getElementById('pronoteCourseModal')) {
                  const modal = document.createElement('div');
                  modal.id = 'pronoteCourseModal';
                  modal.innerHTML = `
                    <div class="pronote-course-sheet" role="dialog" aria-modal="true" aria-labelledby="pronoteCourseTitle">
                      <div class="pronote-course-head">
                        <h3 id="pronoteCourseTitle">Devoirs</h3>
                        <button id="pronoteCourseClose" class="pronote-close" type="button" aria-label="Fermer">×</button>
                      </div>
                      <div id="pronoteCourseMeta" class="pronote-course-meta"></div>
                      <section class="pronote-home-section">
                        <div class="pronote-home-label">Pour ce cours · cahier de textes</div>
                        <div id="pronoteLessonText" class="pronote-home-text"></div>
                      </section>
                      <section class="pronote-home-section">
                        <div class="pronote-home-label">À faire pour la prochaine fois</div>
                        <div id="pronoteNextText" class="pronote-home-text"></div>
                      </section>
                      <button id="pronoteSyncNow" class="pronote-sync-now" type="button">Synchroniser maintenant</button>
                    </div>`;
                  document.body.appendChild(modal);

                  const close = () => modal.classList.remove('show');
                  document.getElementById('pronoteCourseClose').addEventListener('click', close);
                  modal.addEventListener('click', e => { if (e.target === modal) close(); });
                  document.getElementById('pronoteSyncNow').addEventListener('click', () => {
                    try { AndroidPronoteApp.syncNow(); } catch(e) {}
                    const meta = document.getElementById('pronoteCourseMeta');
                    meta.textContent = 'Synchronisation PRONOTE lancée…';
                    setTimeout(() => {
                      if (window.pronoteCurrentCourse) showHomework(window.pronoteCurrentCourse);
                    }, 7000);
                  });
                }

                function escText(value, emptyText) {
                  const s = String(value || '').trim();
                  return s || emptyText;
                }

                function showHomework(label) {
                  window.pronoteCurrentCourse = label;
                  let data = {};
                  try { data = JSON.parse(AndroidPronoteApp.courseHomework(label) || '{}'); } catch(e) {}
                  const title = document.getElementById('pronoteCourseTitle');
                  const meta = document.getElementById('pronoteCourseMeta');
                  const lesson = document.getElementById('pronoteLessonText');
                  const next = document.getElementById('pronoteNextText');
                  title.textContent = label || 'Devoirs';
                  const when = Number(data.importedAt || 0);
                  meta.textContent = when ? 'Dernière synchronisation : ' + new Date(when).toLocaleString('fr-FR') : 'PRONOTE pas encore synchronisé';
                  lesson.textContent = escText(data.lesson, 'Aucun contenu de cahier de textes associé à ce cours.');
                  next.textContent = escText(data.nextHomework, 'Aucun devoir à faire détecté pour la prochaine fois.');
                  lesson.classList.toggle('pronote-home-empty', !String(data.lesson || '').trim());
                  next.classList.toggle('pronote-home-empty', !String(data.nextHomework || '').trim());
                  document.getElementById('pronoteCourseModal').classList.add('show');
                }
                window.showPronoteHomework = showHomework;

                function makeButton(label, small) {
                  const b = document.createElement('button');
                  b.type = 'button';
                  b.className = small ? 'pronote-week-btn' : 'pronote-course-btn';
                  b.textContent = 'Devoirs';
                  b.addEventListener('click', e => {
                    e.preventDefault();
                    e.stopPropagation();
                    showHomework(label);
                  });
                  return b;
                }

                function decorateToday() {
                  document.querySelectorAll('#todayList .todayCourse').forEach(row => {
                    if (row.querySelector('.pronote-course-btn')) return;
                    const labelNode = row.querySelector('.label');
                    if (!labelNode) return;
                    const holder = labelNode.parentElement || row;
                    holder.appendChild(makeButton(labelNode.textContent || '', false));
                  });
                }

                function decorateWeek() {
                  document.querySelectorAll('#weekGrid .wc.has').forEach(cell => {
                    if (cell.querySelector('.pronote-week-btn')) return;
                    const labelNode = cell.querySelector('.cellLabel');
                    if (!labelNode) return;
                    const holder = labelNode.parentElement || cell;
                    holder.appendChild(makeButton(labelNode.textContent || '', true));
                  });
                }

                function decorateEdit() {
                  document.querySelectorAll('#editList .editCourse').forEach(row => {
                    if (row.querySelector('.pronote-course-btn')) return;
                    const labelNode = row.querySelector('.label');
                    if (!labelNode) return;
                    const holder = labelNode.parentElement || row;
                    holder.appendChild(makeButton(labelNode.textContent || '', false));
                  });
                }

                function decorateAll() {
                  decorateToday();
                  decorateWeek();
                  decorateEdit();
                }
                window.refreshPronoteCourseUi = decorateAll;

                ['todayList','weekGrid','editList'].forEach(id => {
                  const node = document.getElementById(id);
                  if (node && !node.dataset.pronoteObserved) {
                    node.dataset.pronoteObserved = '1';
                    new MutationObserver(() => setTimeout(decorateAll, 0)).observe(node, {childList:true, subtree:true});
                  }
                });
                document.querySelectorAll('.nav').forEach(b => {
                  if (!b.dataset.pronoteCourseHook) {
                    b.dataset.pronoteCourseHook = '1';
                    b.addEventListener('click', () => setTimeout(decorateAll, 100));
                  }
                });
                decorateAll();
              } catch(e) {
                console.log('Pronote course UI', e);
              }
            })();
            """;
    }
}
