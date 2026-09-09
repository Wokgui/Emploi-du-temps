package com.wokgui.schedulewidget;

final class Workflow85Ui {
    private Workflow85Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__workflow85V1){if(window.refreshWorkflow85)window.refreshWorkflow85();return}
                window.__workflow85V1=true;
                const APP_VERSION='6.26';
                let arranging=false,arrangeTimer=0,undoSuppress=false,lastSaveAt=0;
                const undoStack=[];
                let lastSnapshot='',lastFingerprint='';

                function lang85(){
                  try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr'))}catch(e){return 'fr'}
                }
                function packMap85(){
                  const l=lang85();if(l==='fr'||l==='en'||l==='de')return null;
                  try{const p=JSON.parse(AndroidSchedule.loadLanguagePack(l)||'{}');return p&&p.strings?p.strings:null}catch(e){return null}
                }
                function tr85(fr,en,de){
                  const l=lang85();if(l==='en')return en;if(l==='de')return de;if(l==='fr')return fr;
                  const p=packMap85();return p&&p[fr]?p[fr]:fr;
                }

                const style=document.createElement('style');
                style.id='workflow85Style';
                style.textContent=`
                  button,.nav,.weekTab,.dayTab,.weekModeChoice,.advButton,.settingsAction,.btn,.addBtn,.importBtn,summary{touch-action:manipulation!important;-webkit-tap-highlight-color:transparent!important}
                  .press85{opacity:.68!important}
                  #fullCourseColorBox:not(.fullColorEnabled84) .fullColorControls{display:none!important}
                  #fullCourseColorBox:not(.fullColorEnabled84) #fullCourseHex{opacity:.45!important}
                  .leftActions85{display:flex;align-items:center;gap:7px;min-width:0}
                  #duplicateCourse85{color:var(--blue);border-color:#b9d5f4;background:#f4f9ff}
                  #undoLast85{display:block;width:max-content;max-width:100%;margin:7px auto 0;padding:7px 11px;border:1px solid #cbd8e7;border-radius:8px;background:#fff;color:#40516a;font-size:.72rem;font-weight:800}
                  #undoLast85:disabled{opacity:.42}
                  #advancedSettings85{padding:0!important;overflow:hidden!important}
                  #advancedSettings85>summary{list-style:none;cursor:pointer;padding:11px 12px;text-align:center;font-size:.78rem;font-weight:900;color:var(--ink);position:relative}
                  #advancedSettings85>summary::-webkit-details-marker{display:none}
                  #advancedSettings85>summary:after{content:'⌄';position:absolute;right:12px;top:50%;transform:translateY(-50%);color:var(--muted);font-size:.95rem}
                  #advancedSettings85[open]>summary:after{content:'⌃'}
                  #advancedContent85{padding:0 8px 8px}
                  #advancedContent85>.settingBox{margin:7px 0!important;box-shadow:none!important}
                  #languageExtra85{padding:9px 0 2px}
                  #languageExtra85 .settingTitle{text-align:center!important;margin-bottom:6px!important}
                  #languageExtra85 #languageDownloadBtn81{display:block!important;margin:0 auto!important}
                  #languageExtra85 #languagePackPanel81{margin-top:8px!important}
                `;
                document.head.appendChild(style);

                function currentFullSnapshot85(){
                  try{if(typeof exportState==='function')return JSON.stringify(exportState())}catch(e){}
                  try{return AndroidSchedule.loadSchedule()||''}catch(e){return ''}
                }
                function fingerprint85(raw){
                  try{const o=JSON.parse(raw||'{}');delete o._currentWeek;return JSON.stringify(o)}catch(e){return String(raw||'')}
                }
                function refreshUndoButton85(){
                  const b=document.getElementById('undoLast85');if(!b)return;
                  b.disabled=undoStack.length===0;
                  b.textContent=tr85('↶ Annuler la dernière modification','↶ Undo last change','↶ Letzte Änderung rückgängig');
                }
                function ensureUndo85(){
                  const add=document.getElementById('addCourse');if(!add)return;
                  let b=document.getElementById('undoLast85');
                  if(!b){b=document.createElement('button');b.id='undoLast85';b.type='button';add.insertAdjacentElement('afterend',b);b.onclick=undo85}
                  refreshUndoButton85();
                }
                function initUndo85(){
                  lastSnapshot=currentFullSnapshot85();lastFingerprint=fingerprint85(lastSnapshot);refreshUndoButton85();
                }
                function wrapSave85(){
                  const old=window.save;if(typeof old!=='function'||old.__undo85)return;
                  const wrapped=function(){
                    const now=Date.now(),current=currentFullSnapshot85(),fp=fingerprint85(current),changed=fp!==lastFingerprint;
                    if(!undoSuppress&&changed){
                      if(now-lastSaveAt>260&&lastSnapshot){undoStack.push(lastSnapshot);if(undoStack.length>12)undoStack.shift()}
                      lastSaveAt=now;
                    }
                    const r=old.apply(this,arguments);
                    lastSnapshot=currentFullSnapshot85();lastFingerprint=fingerprint85(lastSnapshot);refreshUndoButton85();
                    return r;
                  };
                  wrapped.__undo85=true;window.save=wrapped;try{eval('save=wrapped')}catch(e){}
                }
                function undo85(){
                  if(!undoStack.length)return;
                  const snap=undoStack.pop();undoSuppress=true;
                  try{
                    AndroidSchedule.saveSchedule(snap);
                    if(typeof reloadSchedule==='function')reloadSchedule();
                    else if(typeof render==='function')render();
                  }catch(e){}
                  setTimeout(()=>{lastSnapshot=currentFullSnapshot85();lastFingerprint=fingerprint85(lastSnapshot);undoSuppress=false;refreshUndoButton85();},30);
                }

                function ensureDuplicate85(){
                  const actions=document.querySelector('#courseForm .sheetActions'),del=document.getElementById('deleteCourse'),right=actions&&actions.querySelector('.rightActions');if(!actions||!del||!right)return;
                  let left=actions.querySelector('.leftActions85');
                  if(!left){left=document.createElement('div');left.className='leftActions85';actions.insertBefore(left,right);left.appendChild(del)}
                  let dup=document.getElementById('duplicateCourse85');
                  if(!dup){dup=document.createElement('button');dup.id='duplicateCourse85';dup.type='button';dup.className='btn';left.appendChild(dup);dup.onclick=duplicate85}
                  dup.textContent=tr85('Dupliquer','Duplicate','Duplizieren');
                  syncDuplicate85();
                }
                function syncDuplicate85(){
                  const b=document.getElementById('duplicateCourse85');if(!b)return;
                  let show=false;try{show=typeof editing!=='undefined'&&editing!=null}catch(e){}
                  b.hidden=!show;
                }
                function duplicate85(){
                  try{
                    if(typeof editing==='undefined'||editing==null||typeof state==='undefined'||typeof selected==='undefined')return;
                    const c=state[selected]&&state[selected].courses?state[selected].courses[editing]:null;if(!c)return;
                    const chosen=c.slot||(typeof slotForTimes==='function'?slotForTimes(c.start,c.end):0)||1;
                    editing=null;newPrefill={start:c.start,end:c.end};
                    const title=document.getElementById('modalTitle');if(title)title.textContent=tr85('Ajouter · semaine ','Add · week ','Hinzufügen · Woche ')+(typeof activeWeek!=='undefined'?activeWeek:'A');
                    const lab=document.getElementById('fLabel'),room=document.getElementById('fRoom');if(lab)lab.value=c.label||'';if(room)room.value=c.room||'';
                    const del=document.getElementById('deleteCourse');if(del)del.hidden=true;
                    if(typeof fillSlotOptions==='function')fillSlotOptions(chosen,true,c.start,c.end);
                    syncDuplicate85();
                  }catch(e){}
                }
                function wrapOpenEditor85(){
                  const old=window.openEditor;if(typeof old!=='function'||old.__duplicate85)return;
                  const wrapped=function(){const r=old.apply(this,arguments);syncDuplicate85();return r};wrapped.__duplicate85=true;window.openEditor=wrapped;try{eval('openEditor=wrapped')}catch(e){}
                }

                function advancedTitle85(){return tr85('Réglages avancés','Advanced settings','Erweiterte Einstellungen')}
                function arrangeAdvanced85(){
                  if(arranging)return;arranging=true;
                  try{
                    const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                    const actions=sheet.querySelector('.settingsActions');
                    let details=document.getElementById('advancedSettings85'),content=document.getElementById('advancedContent85');
                    if(!details){
                      details=document.createElement('details');details.id='advancedSettings85';details.className='settingBox';
                      const summary=document.createElement('summary');summary.id='advancedSummary85';details.appendChild(summary);
                      content=document.createElement('div');content.id='advancedContent85';details.appendChild(content);
                      sheet.insertBefore(details,actions||null);
                      details.addEventListener('toggle',()=>{try{localStorage.setItem('edt-advanced-open85',details.open?'1':'0')}catch(e){}});
                      try{details.open=localStorage.getItem('edt-advanced-open85')==='1'}catch(e){}
                    }
                    const summary=document.getElementById('advancedSummary85');if(summary)summary.textContent=advancedTitle85();
                    ['advReminderTitle','advCalendarTitle','advExceptionsTitle','advProfilesTitle','advBackupTitle'].forEach(id=>{
                      const t=document.getElementById(id),box=t&&t.closest?t.closest('.settingBox'):null;if(box&&box!==details&&box.parentNode!==content)content.appendChild(box);
                    });
                    const school=document.getElementById('schoolCalendarBlock');if(school&&school.parentNode!==content)content.appendChild(school);

                    const dl=document.getElementById('languageDownloadBtn81'),panel=document.getElementById('languagePackPanel81');
                    if(dl||panel){
                      let lb=document.getElementById('languageExtra85');
                      if(!lb){lb=document.createElement('div');lb.id='languageExtra85';lb.className='settingBox';const h=document.createElement('div');h.id='languageExtraTitle85';h.className='settingTitle';lb.appendChild(h);content.insertBefore(lb,content.firstChild)}
                      const h=document.getElementById('languageExtraTitle85');if(h)h.textContent=tr85('Langues supplémentaires','Additional languages','Zusätzliche Sprachen');
                      if(dl&&dl.parentNode!==lb)lb.appendChild(dl);if(panel&&panel.parentNode!==lb)lb.appendChild(panel);
                    }
                  }finally{arranging=false}
                }
                function scheduleArrange85(){if(arrangeTimer)return;arrangeTimer=setTimeout(()=>{arrangeTimer=0;arrangeAdvanced85()},24)}

                function installFastPress85(){
                  let active=null;
                  document.addEventListener('pointerdown',e=>{const b=e.target&&e.target.closest?e.target.closest('button,.nav,.weekTab,.dayTab,.weekModeChoice,.advButton,.settingsAction,.btn,.addBtn,.importBtn,summary'):null;if(!b||b.disabled)return;active=b;b.classList.add('press85')},{capture:true,passive:true});
                  const clear=()=>{if(active){active.classList.remove('press85');active=null}};
                  document.addEventListener('pointerup',clear,{capture:true,passive:true});document.addEventListener('pointercancel',clear,{capture:true,passive:true});
                }

                function setVersion85(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){ensureUndo85();ensureDuplicate85();arrangeAdvanced85();refreshUndoButton85();setVersion85()}
                window.refreshWorkflow85=refresh;

                wrapSave85();wrapOpenEditor85();ensureUndo85();ensureDuplicate85();installFastPress85();
                const sm=document.getElementById('settingsModal');if(sm)new MutationObserver(()=>{if(sm.classList.contains('show'))scheduleArrange85()}).observe(sm,{attributes:true,attributeFilter:['class']});
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(scheduleArrange85).observe(sheet,{childList:true});
                initUndo85();refresh();
              }catch(e){console.log('Workflow85Ui',e)}
            })();
            """;
    }
}
