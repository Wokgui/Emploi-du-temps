package com.wokgui.schedulewidget;

/** Session edit history for timetable changes. Keeps the editor reversible without touching saved data format. */
final class EditHistoryUi {
    private EditHistoryUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__editHistoryV1){if(window.refreshEditHistory)window.refreshEditHistory();return}
                window.__editHistoryV1=true;
                const APP_VERSION='6.28';
                const undo=[],redo=[];
                let applying=false,lastSnapshot='',lastSaveAt=0;

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
                function snapshot(){
                  try{if(typeof exportState==='function')return JSON.stringify(exportState())}catch(e){}
                  try{return AndroidSchedule.loadSchedule()||''}catch(e){return ''}
                }
                function fingerprint(raw){
                  try{const o=JSON.parse(raw||'{}');delete o._currentWeek;return JSON.stringify(o)}catch(e){return String(raw||'')}
                }
                function same(a,b){return fingerprint(a)===fingerprint(b)}
                function trimStack(s){while(s.length>20)s.shift()}

                const style=document.createElement('style');
                style.id='editHistoryStyle';
                style.textContent=`
                  #undoLast85{display:none!important}
                  #editHistoryActions86{display:grid;grid-template-columns:1fr 1fr;gap:7px;margin:7px 0 0}
                  #editHistoryActions86 button{
                    min-height:35px;border:1px solid #cbd8e7;border-radius:8px;background:#fff;
                    color:#40516a;font-size:.72rem;font-weight:850;padding:7px 9px;touch-action:manipulation
                  }
                  #editHistoryActions86 button:disabled{opacity:.38}
                  #editHistoryActions86 button:not(:disabled):active{opacity:.68}
                `;
                document.head.appendChild(style);

                function ensureButtons(){
                  const add=document.getElementById('addCourse');if(!add)return;
                  let row=document.getElementById('editHistoryActions86');
                  if(!row){
                    row=document.createElement('div');row.id='editHistoryActions86';
                    const u=document.createElement('button');u.id='undoEdit86';u.type='button';u.onclick=doUndo;row.appendChild(u);
                    const r=document.createElement('button');r.id='redoEdit86';r.type='button';r.onclick=doRedo;row.appendChild(r);
                    add.insertAdjacentElement('afterend',row);
                  }
                  refreshButtons();
                }
                function refreshButtons(){
                  const u=document.getElementById('undoEdit86'),r=document.getElementById('redoEdit86');
                  if(u){u.textContent=tr('↶ Annuler','↶ Undo','↶ Rückgängig');u.disabled=undo.length===0}
                  if(r){r.textContent=tr('↷ Rétablir','↷ Redo','↷ Wiederholen');r.disabled=redo.length===0}
                  const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                }
                function apply(raw){
                  applying=true;
                  try{
                    AndroidSchedule.saveSchedule(raw);
                    if(typeof reloadSchedule==='function')reloadSchedule();
                    else if(typeof render==='function')render();
                  }catch(e){}
                  setTimeout(()=>{lastSnapshot=snapshot();applying=false;refreshButtons()},30);
                }
                function doUndo(){
                  if(!undo.length||applying)return;
                  const current=snapshot(),target=undo.pop();
                  if(current)redo.push(current);trimStack(redo);apply(target);refreshButtons();
                }
                function doRedo(){
                  if(!redo.length||applying)return;
                  const current=snapshot(),target=redo.pop();
                  if(current)undo.push(current);trimStack(undo);apply(target);refreshButtons();
                }
                function wrapSave(){
                  const old=window.save;if(typeof old!=='function'||old.__editHistory86)return;
                  const wrapped=function(){
                    const before=snapshot();
                    const result=old.apply(this,arguments);
                    const after=snapshot();
                    if(!applying&&!same(before,after)){
                      const now=Date.now();
                      if(now-lastSaveAt>120&&before){undo.push(before);trimStack(undo)}
                      redo.length=0;lastSaveAt=now;lastSnapshot=after;
                    }
                    refreshButtons();
                    return result;
                  };
                  wrapped.__editHistory86=true;window.save=wrapped;try{eval('save=wrapped')}catch(e){}
                }
                function refresh(){ensureButtons();refreshButtons()}
                window.refreshEditHistory=refresh;
                wrapSave();lastSnapshot=snapshot();ensureButtons();refresh();
              }catch(e){console.log('EditHistoryUi',e)}
            })();
            """;
    }
}
