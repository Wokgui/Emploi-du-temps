package com.wokgui.schedulewidget;

final class BulkCourseUi {
    private BulkCourseUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__bulkCourseUiV1){
                  if(window.refreshBulkCourseUi)window.refreshBulkCourseUi();
                  return;
                }
                window.__bulkCourseUiV1=true;

                const style=document.createElement('style');
                style.textContent=`
                  #settingsBtn{display:flex!important;visibility:visible!important;opacity:1!important;z-index:20!important}
                  #addBulkCourses{margin-top:6px;background:#eef6ff!important;border-style:solid!important}
                  #bulkCourseModal{position:fixed;inset:0;z-index:120;background:#0b17386b;display:none;align-items:flex-end}
                  #bulkCourseModal.show{display:flex}
                  #bulkCourseSheet{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:18px 18px 0 0;padding:14px 12px calc(14px + env(safe-area-inset-bottom));max-height:90vh;overflow:auto;color:var(--ink)}
                  #bulkCourseSheet h3{margin:0 0 4px;text-align:center;font-size:1rem}
                  .bulkWeek{text-align:center;color:var(--muted);font-size:.72rem;margin-bottom:10px}
                  .bulkField{margin-bottom:8px}.bulkField label{display:block;color:var(--muted);font-size:.72rem;margin-bottom:3px}.bulkField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;background:#fff;color:var(--ink)}
                  .bulkRows{border:1px solid var(--line);border-radius:9px;overflow:hidden;background:#fff}
                  .bulkRow{display:grid;grid-template-columns:82px minmax(0,1fr) 82px 30px;gap:5px;align-items:center;padding:7px;border-top:1px solid #e9edf2}
                  .bulkRow:first-child{border-top:0}
                  .bulkRow select,.bulkRow input{width:100%;min-width:0;border:1px solid var(--line);border-radius:7px;padding:8px 5px;background:#fff;color:var(--ink);font-size:.74rem}
                  .bulkRemove{width:30px;height:30px;border:0;border-radius:50%;background:#fff1f3;color:#c6284e;font-size:18px;line-height:1}
                  .bulkAddLine{width:100%;margin-top:7px;padding:8px;border:1px dashed var(--blue);border-radius:7px;background:#fff;color:var(--blue);font-weight:800}
                  .bulkHint{font-size:.67rem;color:var(--muted);line-height:1.25;margin:6px 2px 0}
                  .bulkActions{display:flex;justify-content:flex-end;gap:7px;margin-top:11px}
                  @media(max-width:430px){.bulkRow{grid-template-columns:70px minmax(0,1fr) 72px 28px;gap:4px;padding:6px 5px}.bulkRow select,.bulkRow input{font-size:.68rem;padding:8px 3px}}
                `;
                document.head.appendChild(style);

                function ensureSettingsButton(){
                  const header=document.querySelector('.header');
                  if(!header)return;
                  let btn=document.getElementById('settingsBtn'),created=false;
                  if(!btn){
                    btn=document.createElement('button');btn.id='settingsBtn';btn.type='button';btn.textContent='⚙';btn.setAttribute('aria-label','Réglages');header.appendChild(btn);created=true;
                  }
                  btn.style.setProperty('display','flex','important');
                  btn.style.setProperty('visibility','visible','important');
                  btn.style.setProperty('opacity','1','important');
                  if(created||!btn.onclick){
                    btn.onclick=()=>{
                      const settingsModal=document.getElementById('settingsModal');
                      if(settingsModal){
                        settingsModal.classList.add('show');
                        try{if(window.refreshSettingsV3)window.refreshSettingsV3()}catch(e){}
                        try{if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures()}catch(e){}
                        try{if(window.refreshUiPolishSchool)window.refreshUiPolishSchool()}catch(e){}
                      }
                    };
                  }
                }

                function dayName(d){return ({2:'Lun',3:'Mar',4:'Mer',5:'Jeu',6:'Ven'})[d]||'Jour'}
                function safeSelected(){try{return DAYS.includes(selected)?selected:DAYS[0]}catch(e){return 2}}
                function currentWeekLetter(){try{return activeWeek||currentWeek||'A'}catch(e){return 'A'}}

                const modal=document.createElement('div');modal.id='bulkCourseModal';
                modal.innerHTML=`
                  <form id="bulkCourseSheet">
                    <h3>Ajouter plusieurs cours</h3>
                    <div class="bulkWeek">Même classe · semaine <b id="bulkWeekLetter">A</b></div>
                    <div class="bulkField"><label>Classe / groupe</label><input id="bulkClassLabel" type="text" maxlength="80" required placeholder="Ex. 4G1 ALL · 4G2 ALL"></div>
                    <div id="bulkRows" class="bulkRows"></div>
                    <button id="bulkAddLine" class="bulkAddLine" type="button">＋ Ajouter un autre jour / horaire</button>
                    <div class="bulkHint">Chaque ligne peut avoir son propre jour, son heure de cours et sa salle. Les cours déjà présents ne sont pas remplacés.</div>
                    <div class="bulkActions"><button id="bulkCancel" type="button" class="btn">Annuler</button><button type="submit" class="btn primary">Ajouter tous les cours</button></div>
                  </form>`;
                document.body.appendChild(modal);

                function slotOptions(selectedSlot){
                  try{return slots.map(s=>`<option value="${s.n}"${Number(selectedSlot)===Number(s.n)?' selected':''}>${s.n}e · ${s.start}–${s.end}</option>`).join('')}catch(e){return ''}
                }
                function dayOptions(selectedDay){
                  try{return DAYS.map(d=>`<option value="${d}"${Number(selectedDay)===Number(d)?' selected':''}>${dayName(d)}</option>`).join('')}catch(e){return ''}
                }
                function firstFree(day){
                  try{
                    const arr=weeks[currentWeekLetter()]&&weeks[currentWeekLetter()][day]?weeks[currentWeekLetter()][day].courses:[];
                    const used=new Set(arr.map(c=>Number(c.slot||0)).filter(Boolean));
                    return slots.find(s=>!used.has(Number(s.n)))?.n||1;
                  }catch(e){return 1}
                }
                function nextDayAfter(day){
                  try{const i=DAYS.indexOf(Number(day));return DAYS[(i+1+DAYS.length)%DAYS.length]}catch(e){return safeSelected()}
                }
                function addRow(day,slot,room){
                  const rows=document.getElementById('bulkRows');if(!rows)return;
                  const row=document.createElement('div');row.className='bulkRow';
                  row.innerHTML=`<select class="bulkDay" aria-label="Jour">${dayOptions(day)}</select><select class="bulkSlot" aria-label="Horaire">${slotOptions(slot)}</select><input class="bulkRoom" type="text" maxlength="20" placeholder="Salle" value="${typeof esc==='function'?esc(room||''):String(room||'')}"><button type="button" class="bulkRemove" aria-label="Supprimer cette ligne">×</button>`;
                  row.querySelector('.bulkRemove').onclick=()=>{if(rows.children.length>1)row.remove()};
                  row.querySelector('.bulkDay').onchange=e=>{const s=row.querySelector('.bulkSlot');if(s)s.innerHTML=slotOptions(firstFree(Number(e.target.value)))};
                  rows.appendChild(row);
                }
                function clearAndOpen(){
                  const rows=document.getElementById('bulkRows');if(!rows)return;
                  rows.innerHTML='';document.getElementById('bulkClassLabel').value='';document.getElementById('bulkWeekLetter').textContent=currentWeekLetter();
                  const d=safeSelected();addRow(d,firstFree(d),'');
                  modal.classList.add('show');
                  setTimeout(()=>document.getElementById('bulkClassLabel').focus(),60);
                }
                function close(){modal.classList.remove('show')}

                function ensureBulkButton(){
                  const base=document.getElementById('addCourse');if(!base)return;
                  let btn=document.getElementById('addBulkCourses');
                  if(!btn){btn=document.createElement('button');btn.id='addBulkCourses';btn.type='button';btn.className='addBtn';btn.textContent='＋ Ajouter plusieurs cours à une classe';base.insertAdjacentElement('afterend',btn)}
                  btn.onclick=clearAndOpen;
                }

                document.getElementById('bulkAddLine').onclick=()=>{
                  const rows=document.querySelectorAll('#bulkRows .bulkRow');
                  const last=rows.length?rows[rows.length-1]:null;
                  const d=last?nextDayAfter(Number(last.querySelector('.bulkDay').value)):safeSelected();
                  addRow(d,firstFree(d),'');
                };
                document.getElementById('bulkCancel').onclick=close;
                modal.onclick=e=>{if(e.target===modal)close()};
                document.getElementById('bulkCourseSheet').onsubmit=e=>{
                  e.preventDefault();
                  try{
                    const label=document.getElementById('bulkClassLabel').value.trim();if(!label){alert('Indique la classe ou le groupe.');return}
                    const week=currentWeekLetter(),rows=Array.from(document.querySelectorAll('#bulkRows .bulkRow'));
                    const wanted=[],seen=new Set(),conflicts=[];
                    for(const row of rows){
                      const day=Number(row.querySelector('.bulkDay').value),slot=Number(row.querySelector('.bulkSlot').value),room=row.querySelector('.bulkRoom').value.trim();
                      const s=slots[slot-1];if(!s)continue;
                      const key=day+'|'+s.start+'|'+s.end;
                      if(seen.has(key)){conflicts.push(dayName(day)+' '+s.start+'–'+s.end+' (doublon dans la saisie)');continue}
                      seen.add(key);
                      const list=weeks[week][day].courses;
                      if(list.some(c=>min(c.start)<min(s.end)&&min(c.end)>min(s.start))){conflicts.push(dayName(day)+' '+s.start+'–'+s.end+' (déjà occupé)');continue}
                      wanted.push({day,course:{start:s.start,end:s.end,label,room,slot}});
                    }
                    if(conflicts.length){alert('Impossible d’ajouter ces lignes :\n\n'+conflicts.join('\n')+'\n\nModifie-les puis réessaie.');return}
                    if(!wanted.length){alert('Ajoute au moins une ligne de cours.');return}
                    wanted.forEach(x=>weeks[week][x.day].courses.push(x.course));
                    selected=wanted[0].day;close();
                    if(typeof save==='function')save();
                    if(window.decorateCourseBadges)setTimeout(window.decorateCourseBadges,20);
                  }catch(err){alert('Impossible d’ajouter les cours en une fois.')}
                };

                function refresh(){ensureSettingsButton();ensureBulkButton()}
                window.refreshBulkCourseUi=refresh;
                refresh();
                const header=document.querySelector('.header');if(header)new MutationObserver(refresh).observe(header,{childList:true});
                const edit=document.getElementById('viewEdit');if(edit)new MutationObserver(()=>setTimeout(ensureBulkButton,0)).observe(edit,{childList:true,subtree:true});
              }catch(e){console.log('Bulk course UI',e)}
            })();
            """;
    }
}
