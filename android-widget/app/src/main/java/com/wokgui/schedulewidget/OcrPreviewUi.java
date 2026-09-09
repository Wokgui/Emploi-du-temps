package com.wokgui.schedulewidget;

/** Safe OCR import preview: nothing is written until the user confirms selected days. */
final class OcrPreviewUi {
    private OcrPreviewUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__ocrPreviewV1){if(window.refreshOcrPreview)window.refreshOcrPreview();return}
                window.__ocrPreviewV1=true;
                const APP_VERSION='6.29';
                let pending=null;

                function language(){
                  try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr'))}catch(e){return 'fr'}
                }
                function pack(){
                  const l=language();if(l==='fr'||l==='en'||l==='de')return null;
                  try{const p=JSON.parse(AndroidSchedule.loadLanguagePack(l)||'{}');return p&&p.strings?p.strings:null}catch(e){return null}
                }
                function tr(fr,en,de){
                  const l=language();if(l==='en')return en;if(l==='de')return de;if(l==='fr')return fr;
                  const p=pack();return p&&p[fr]?p[fr]:fr;
                }
                function dayName(d){
                  const fr={1:'Dimanche',2:'Lundi',3:'Mardi',4:'Mercredi',5:'Jeudi',6:'Vendredi',7:'Samedi'};
                  const en={1:'Sunday',2:'Monday',3:'Tuesday',4:'Wednesday',5:'Thursday',6:'Friday',7:'Saturday'};
                  const de={1:'Sonntag',2:'Montag',3:'Dienstag',4:'Mittwoch',5:'Donnerstag',6:'Freitag',7:'Samstag'};
                  const l=language();if(l==='en')return en[d]||String(d);if(l==='de')return de[d]||String(d);if(l==='fr')return fr[d]||String(d);
                  const p=pack(),base=fr[d]||String(d);return p&&p[base]?p[base]:base;
                }
                function uncertain(c){return !c||!c.start||!c.end||!(Number(c.slot)>0)}

                const style=document.createElement('style');
                style.id='ocrPreviewStyle';
                style.textContent=`
                  #ocrPreview86{position:fixed;inset:0;z-index:180;background:#0b17386b;display:none;align-items:flex-end}
                  #ocrPreview86.show{display:flex}
                  #ocrPreviewSheet86{width:100%;max-width:780px;max-height:90vh;margin:auto;background:#fff;border-radius:16px 16px 0 0;padding:13px 12px calc(13px + env(safe-area-inset-bottom));overflow:auto;color:var(--ink,#111936)}
                  #ocrPreviewHead86{position:sticky;top:-13px;z-index:2;background:#fff;padding:2px 0 8px;border-bottom:1px solid #edf1f5}
                  #ocrPreviewTitle86{text-align:center;font-size:1rem;font-weight:900;margin:0}
                  #ocrPreviewSummary86{text-align:center;color:var(--muted,#68738a);font-size:.72rem;margin:4px 8px 0;line-height:1.3}
                  #ocrPreviewDays86{display:grid;gap:8px;margin-top:9px}
                  .ocrDay86{border:1px solid #dce4ec;border-radius:10px;overflow:hidden;background:#fff}
                  .ocrDayHead86{display:flex;align-items:center;gap:7px;padding:8px 9px;background:#f7f9fc;font-weight:850;font-size:.78rem}
                  .ocrDayHead86 input{width:18px;height:18px;accent-color:var(--set-accent,var(--blue,#0877f9))}
                  .ocrDayCount86{margin-left:auto;color:var(--muted,#68738a);font-size:.68rem}
                  .ocrCourse86{display:grid;grid-template-columns:76px minmax(0,1fr);gap:7px;padding:7px 9px;border-top:1px solid #edf1f5;align-items:center}
                  .ocrTime86{font-size:.68rem;font-weight:800;color:#536078;font-variant-numeric:tabular-nums;text-align:center}
                  .ocrLabel86{font-size:.77rem;font-weight:850;line-height:1.15;min-width:0;overflow-wrap:anywhere}
                  .ocrRoom86{font-size:.66rem;color:var(--muted,#68738a);margin-top:2px}
                  .ocrCourse86.uncertain86{background:#fff8ea}
                  .ocrWarn86{display:inline-block;margin-top:3px;padding:2px 5px;border-radius:999px;background:#ffedc8;color:#89590d;font-size:.60rem;font-weight:850}
                  #ocrPreviewLegend86{text-align:center;color:#7b6541;font-size:.66rem;margin:8px 5px 0;display:none}
                  #ocrPreviewActions86{position:sticky;bottom:calc(-13px - env(safe-area-inset-bottom));background:#fff;padding:9px 0 calc(2px + env(safe-area-inset-bottom));display:grid;grid-template-columns:1fr 1fr 1fr;gap:7px;margin-top:9px;border-top:1px solid #edf1f5}
                  #ocrPreviewActions86 button{min-height:39px;border:1px solid #d4dde7;border-radius:8px;background:#fff;font-size:.74rem;font-weight:850;padding:7px;touch-action:manipulation}
                  #ocrPreviewImport86{background:var(--set-accent,var(--blue,#0877f9))!important;color:#fff!important;border-color:var(--set-accent,var(--blue,#0877f9))!important}
                  #ocrPreviewCorrect86{color:var(--set-accent,var(--blue,#0877f9))!important;border-color:#bad5f4!important;background:#f3f9ff!important}
                  #ocrPreviewActions86 button:disabled{opacity:.4}
                `;
                document.head.appendChild(style);

                function ensureModal(){
                  let modal=document.getElementById('ocrPreview86');if(modal)return modal;
                  modal=document.createElement('div');modal.id='ocrPreview86';modal.setAttribute('role','dialog');modal.setAttribute('aria-modal','true');
                  const sheet=document.createElement('div');sheet.id='ocrPreviewSheet86';modal.appendChild(sheet);
                  const head=document.createElement('div');head.id='ocrPreviewHead86';sheet.appendChild(head);
                  const title=document.createElement('h3');title.id='ocrPreviewTitle86';head.appendChild(title);
                  const summary=document.createElement('div');summary.id='ocrPreviewSummary86';head.appendChild(summary);
                  const days=document.createElement('div');days.id='ocrPreviewDays86';sheet.appendChild(days);
                  const legend=document.createElement('div');legend.id='ocrPreviewLegend86';sheet.appendChild(legend);
                  const actions=document.createElement('div');actions.id='ocrPreviewActions86';sheet.appendChild(actions);
                  const cancel=document.createElement('button');cancel.id='ocrPreviewCancel86';cancel.type='button';cancel.onclick=close;actions.appendChild(cancel);
                  const correct=document.createElement('button');correct.id='ocrPreviewCorrect86';correct.type='button';correct.onclick=()=>apply(true);actions.appendChild(correct);
                  const imp=document.createElement('button');imp.id='ocrPreviewImport86';imp.type='button';imp.onclick=()=>apply(false);actions.appendChild(imp);
                  modal.addEventListener('click',e=>{if(e.target===modal)close()});
                  document.body.appendChild(modal);return modal;
                }
                function resetImportButton(){
                  const b=document.getElementById('importPhoto');if(b){b.disabled=false;b.textContent=tr('Importer une photo d’emploi du temps','Import a timetable photo','Stundenplan-Foto importieren')}
                }
                function selectedDays(){
                  return [...document.querySelectorAll('#ocrPreviewDays86 input[data-day]:checked')].map(x=>Number(x.dataset.day)).filter(Number.isFinite)
                }
                function updateActions(){
                  const ok=selectedDays().length>0;['ocrPreviewImport86','ocrPreviewCorrect86'].forEach(id=>{const b=document.getElementById(id);if(b)b.disabled=!ok})
                }
                function render(result){
                  const modal=ensureModal(),daysRoot=document.getElementById('ocrPreviewDays86');daysRoot.innerHTML='';
                  document.getElementById('ocrPreviewTitle86').textContent=tr('Aperçu avant import','Preview before import','Vorschau vor dem Import');
                  document.getElementById('ocrPreviewSummary86').textContent=result.count+' '+tr('cours détectés. Choisis les jours à remplacer.','classes detected. Choose the days to replace.','Stunden erkannt. Wähle die zu ersetzenden Tage.');
                  let hasUncertain=false;
                  (result.days||[]).forEach(d=>{
                    const courses=result.parsed[d]||[],card=document.createElement('section');card.className='ocrDay86';
                    const h=document.createElement('label');h.className='ocrDayHead86';card.appendChild(h);
                    const ck=document.createElement('input');ck.type='checkbox';ck.dataset.day=String(d);ck.checked=courses.length>0;ck.disabled=courses.length===0;ck.addEventListener('change',updateActions);h.appendChild(ck);
                    const name=document.createElement('span');name.textContent=dayName(d);h.appendChild(name);
                    const count=document.createElement('span');count.className='ocrDayCount86';count.textContent=courses.length+' '+tr('cours','classes','Stunden');h.appendChild(count);
                    courses.forEach(c=>{
                      const row=document.createElement('div');row.className='ocrCourse86'+(uncertain(c)?' uncertain86':'');if(uncertain(c))hasUncertain=true;
                      const time=document.createElement('div');time.className='ocrTime86';time.textContent=(c.start||'?')+'–'+(c.end||'?');row.appendChild(time);
                      const body=document.createElement('div');row.appendChild(body);
                      const label=document.createElement('div');label.className='ocrLabel86';label.textContent=c.label||tr('Cours sans nom','Unnamed class','Stunde ohne Namen');body.appendChild(label);
                      if(c.room){const room=document.createElement('div');room.className='ocrRoom86';room.textContent=tr('Salle ','Room ','Raum ')+c.room;body.appendChild(room)}
                      if(uncertain(c)){const w=document.createElement('span');w.className='ocrWarn86';w.textContent=tr('Horaire à vérifier','Check time','Zeit prüfen');body.appendChild(w)}
                      card.appendChild(row);
                    });
                    daysRoot.appendChild(card);
                  });
                  const legend=document.getElementById('ocrPreviewLegend86');legend.style.display=hasUncertain?'block':'none';legend.textContent=tr('Les lignes jaunes n’ont pas pu être rattachées avec certitude à l’une des 9 heures configurées.','Yellow rows could not be matched confidently to one of the 9 configured periods.','Gelbe Zeilen konnten keiner der 9 eingestellten Stunden sicher zugeordnet werden.');
                  document.getElementById('ocrPreviewCancel86').textContent=tr('Annuler','Cancel','Abbrechen');
                  document.getElementById('ocrPreviewCorrect86').textContent=tr('Corriger','Edit first','Zuerst korrigieren');
                  document.getElementById('ocrPreviewImport86').textContent=tr('Importer','Import','Importieren');
                  updateActions();modal.classList.add('show');
                }
                function close(){const m=document.getElementById('ocrPreview86');if(m)m.classList.remove('show');pending=null;resetImportButton()}
                function apply(editAfter){
                  if(!pending)return;const chosen=selectedDays();if(!chosen.length)return;
                  try{
                    const wk=typeof activeWeek!=='undefined'?activeWeek:'A';
                    for(const d of chosen){if(pending.parsed[d]&&weeks[wk]&&weeks[wk][d])weeks[wk][d].courses=pending.parsed[d]}
                    if(chosen.length)selected=chosen[0];
                    const count=chosen.reduce((n,d)=>n+(pending.parsed[d]||[]).length,0);
                    const status=document.getElementById('importStatus');if(status)status.textContent=count+' '+tr('cours importés.','classes imported.','Stunden importiert.');
                    if(typeof save==='function')save();
                    const modal=document.getElementById('ocrPreview86');if(modal)modal.classList.remove('show');pending=null;resetImportButton();
                    if(editAfter){
                      try{if(typeof setModeFromAndroid==='function')setModeFromAndroid('edit');else if(typeof setMode==='function')setMode('edit')}catch(e){}
                      try{if(typeof render==='function')render()}catch(e){}
                      setTimeout(()=>{try{const tab=document.querySelector('#dayTabs .dayTab[data-day="'+selected+'"]');if(tab)tab.click()}catch(e){}},0);
                    }
                  }catch(e){if(window.applyOcrError)window.applyOcrError(tr('La photo a été lue mais l’import a échoué.','The photo was read but the import failed.','Das Foto wurde gelesen, aber der Import ist fehlgeschlagen.'))}
                }

                window.applyOcrSchedule=function(raw){
                  resetImportButton();
                  try{
                    const payload=typeof raw==='string'?JSON.parse(raw):raw;
                    const result=window.parseOcrSchedule?window.parseOcrSchedule(payload):null;
                    if(!result){alert(tr('La conversion de la photo est indisponible.','Photo conversion is unavailable.','Die Foto-Konvertierung ist nicht verfügbar.'));return}
                    if(result.error){alert(result.error);return}
                    pending=result;render(result);
                  }catch(e){if(window.applyOcrError)window.applyOcrError(tr('La photo a été lue mais la conversion a échoué.','The photo was read but conversion failed.','Das Foto wurde gelesen, aber die Konvertierung ist fehlgeschlagen.'))}
                };
                function refresh(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                window.refreshOcrPreview=refresh;refresh();
              }catch(e){console.log('OcrPreviewUi',e)}
            })();
            """;
    }
}
