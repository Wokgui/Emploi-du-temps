package com.wokgui.schedulewidget;

/** Editing workflow, safe import preview, compact settings and undo/redo. */
final class WorkflowUi {
    private WorkflowUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(74 * 1024);
        out.append(layer0()).append('\n'); // Workflow85Ui
        out.append(layer1()).append('\n'); // SettingsLayoutUi
        out.append(layer2()).append('\n'); // EditHistoryUi
        out.append(layer3()).append('\n'); // OcrPreviewUi
        out.append(layer4()).append('\n'); // SettingsResetUi
        return out.toString();
    }

    // Former Workflow85Ui; isolated to stay below JVM constant limits.
    private static String layer0() {
        return """
            (function(){
              try{
                if(window.__workflow85V1){if(window.refreshWorkflow85)window.refreshWorkflow85();return}
                window.__workflow85V1=true;
                const APP_VERSION='6.31';
                let arranging=false,arrangeTimer=0;

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
                function refresh(){ensureDuplicate85();arrangeAdvanced85();setVersion85()}
                window.refreshWorkflow85=refresh;
                (window.__edtCoursePanelPreparers648||(window.__edtCoursePanelPreparers648=[])).push({id:'duplicate-course',run:syncDuplicate85});

                wrapOpenEditor85();ensureDuplicate85();installFastPress85();
                const sm=document.getElementById('settingsModal');if(sm)new MutationObserver(()=>{if(sm.classList.contains('show'))scheduleArrange85()}).observe(sm,{attributes:true,attributeFilter:['class']});
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(scheduleArrange85).observe(sheet,{childList:true});
                refresh();
              }catch(e){console.log('Workflow85Ui',e)}
            })();
            """;
    }

    // Former SettingsLayoutUi; isolated to stay below JVM constant limits.
    private static String layer1() {
        return """
            (function(){
              try{
                if(window.__settingsLayoutV1){if(window.refreshSettingsLayout)window.refreshSettingsLayout();return}
                window.__settingsLayoutV1=true;
                const APP_VERSION='6.31';
                let arranging=false,timer=0;

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

                const style=document.createElement('style');
                style.id='settingsLayoutStyle';
                style.textContent=`
                  #settingsSheet>.settingsSection86{padding:9px 10px!important}
                  #settingsSheet>.settingsSection86>.settingsSectionTitle86{
                    margin:0 0 3px!important;text-align:center!important;font-size:.82rem!important;
                    font-weight:900!important;color:var(--ink,#111936)!important
                  }
                  #settingsSheet>.settingsSection86>.settingsSectionBody86{display:block!important}
                  #settingsSheet>.settingsSection86>.settingsSectionBody86>.settingBox{
                    margin:0!important;padding:8px 0!important;border:0!important;border-radius:0!important;
                    box-shadow:none!important;background:transparent!important
                  }
                  #settingsSheet>.settingsSection86>.settingsSectionBody86>.settingBox+.settingBox{
                    border-top:1px solid #e7edf3!important
                  }
                  #settingsSheet>.settingsSection86>.settingsSectionBody86>.settingBox>.settingTitle{
                    text-align:left!important;margin:0 0 6px!important;font-size:.74rem!important;color:#586579!important
                  }
                  #settingsSheet>.settingsSection86 #appFontTitle,
                  #settingsSheet>.settingsSection86 #widgetFontTitle{font-weight:800!important}
                  #settingsSheet>.settingsSection86 #themeTitle,
                  #settingsSheet>.settingsSection86 #advWidgetTitle{font-weight:800!important}
                  #settingsSheet>.settingsSection86 .settingRow{min-height:32px!important}
                  #settingsSheet>.settingsSection86 .settingValue{min-width:48px!important}
                  #settingsSheet>.settingsSection86 .coursePaletteHint{margin-top:2px!important}
                  #settingsSheet>.settingsSection86 .themeGrid{margin-top:2px!important}
                  #settingsSheet>#advancedSettings85{margin-top:9px!important}
                  #settingsSheet .settingsActions{margin-top:10px!important}
                `;
                document.head.appendChild(style);

                function boxFor(id){
                  const el=document.getElementById(id);if(!el)return null;
                  if(el.classList&&el.classList.contains('settingBox'))return el;
                  return el.closest?el.closest('.settingBox'):null;
                }
                function uniqueBoxes(ids){
                  const out=[];
                  ids.forEach(id=>{const b=boxFor(id);if(b&&!out.includes(b)&&!b.classList.contains('settingsSection86')&&b.id!=='advancedSettings85')out.push(b)});
                  return out;
                }
                function ensureGroup(id,title,ids){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return null;
                  const boxes=uniqueBoxes(ids);if(!boxes.length)return null;
                  let group=document.getElementById(id),body;
                  if(!group){
                    group=document.createElement('div');group.id=id;group.className='settingBox settingsSection86';
                    const h=document.createElement('div');h.className='settingsSectionTitle86';group.appendChild(h);
                    body=document.createElement('div');body.className='settingsSectionBody86';group.appendChild(body);
                    boxes[0].parentNode.insertBefore(group,boxes[0]);
                  }else body=group.querySelector('.settingsSectionBody86');
                  const h=group.querySelector('.settingsSectionTitle86');if(h)h.textContent=title;
                  boxes.forEach(b=>{if(b.parentNode!==body)body.appendChild(b)});
                  return group;
                }

                function arrange(){
                  timer=0;if(arranging)return;arranging=true;
                  try{
                    ensureGroup('textSettings86',tr('Taille du texte','Text size','Textgröße'),['appFont','widgetFont']);
                    ensureGroup('colorSettings86',tr('Couleurs','Colors','Farben'),['themeTitle','paletteSettingRoot','fineSpecialColors']);
                    ensureGroup('widgetSettings86',tr('Widget','Widget','Widget'),['advWidgetTitle','breakDisplaySetting']);
                    const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                  }finally{arranging=false}
                }
                function schedule(){if(timer||arranging)return;timer=setTimeout(arrange,20)}
                function refresh(){arrange()}
                window.refreshSettingsLayout=refresh;

                const sheet=document.getElementById('settingsSheet');
                if(sheet&&!sheet.__settingsLayoutObserved){
                  sheet.__settingsLayoutObserved=true;
                  new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});
                }
                const modal=document.getElementById('settingsModal');
                if(modal&&!modal.__settingsLayoutObserved){
                  modal.__settingsLayoutObserved=true;
                  new MutationObserver(()=>{if(modal.classList.contains('show'))schedule()}).observe(modal,{attributes:true,attributeFilter:['class']});
                }
                arrange();
              }catch(e){console.log('SettingsLayoutUi',e)}
            })();
            """;
    }

    // Former EditHistoryUi; isolated to stay below JVM constant limits.
    private static String layer2() {
        return """
            (function(){
              try{
                if(window.__editHistoryV1){if(window.refreshEditHistory)window.refreshEditHistory();return}
                window.__editHistoryV1=true;
                const APP_VERSION='6.31';
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

    // Former OcrPreviewUi; isolated to stay below JVM constant limits.
    private static String layer3() {
        return """
            (function(){
              try{
                if(window.__ocrPreviewV1){if(window.refreshOcrPreview)window.refreshOcrPreview();return}
                window.__ocrPreviewV1=true;
                const APP_VERSION='6.31';
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

    // Former SettingsResetUi; isolated to stay below JVM constant limits.
    private static String layer4() {
        return """
            (function(){
              try{
                if(window.__settingsResetV1){if(window.refreshSettingsReset)window.refreshSettingsReset();return}
                window.__settingsResetV1=true;
                const APP_VERSION='6.31';

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
                function ui(){try{return JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){return {}}}
                function adv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function refreshAll(){
                  try{if(window.refreshSettingsV3)window.refreshSettingsV3()}catch(e){}
                  try{if(window.refreshFineTuneUi)window.refreshFineTuneUi()}catch(e){}
                  try{if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures()}catch(e){}
                  try{if(window.refreshStability81)window.refreshStability81()}catch(e){}
                  try{if(window.refreshSettingsLayout)window.refreshSettingsLayout()}catch(e){}
                  try{if(typeof render==='function')render()}catch(e){}
                }

                const style=document.createElement('style');
                style.id='settingsResetStyle';
                style.textContent=`
                  .settingsSectionReset87{display:block;margin:6px auto 0;padding:6px 9px;border:1px solid #d5dee8;border-radius:8px;background:#fff;color:#59677a;font-size:.66rem;font-weight:850;touch-action:manipulation}
                  .settingsSectionReset87:active{opacity:.66}
                `;
                document.head.appendChild(style);

                function addButton(groupId,id,handler){
                  const group=document.getElementById(groupId);if(!group)return;
                  let b=document.getElementById(id);if(!b){b=document.createElement('button');b.id=id;b.type='button';b.className='settingsSectionReset87';b.onclick=handler;group.appendChild(b)}
                  b.textContent=tr('Réinitialiser cette section','Reset this section','Diesen Bereich zurücksetzen');
                }
                function resetText(){
                  const o=ui();o.appFontScale=1;o.widgetFontScale=1;AndroidSchedule.saveUiSettings(JSON.stringify(o));refreshAll()
                }
                function resetColors(){
                  const o=ui();o.theme='blue';AndroidSchedule.saveUiSettings(JSON.stringify(o));
                  try{AndroidSchedule.saveWidgetPalette('vivid')}catch(e){}
                  try{AndroidSchedule.saveSpecialColors(JSON.stringify({sync:true,appLunch:'#FFF9E8',appGap:'#FFFFFF',widgetLunch:'#FFF9E8',widgetGap:'#FFFFFF'}))}catch(e){}
                  refreshAll()
                }
                function resetWidget(){
                  const a=adv();
                  Object.assign(a,{density:'normal',upcomingCount:0,widgetFormat:'timeline',showRoom:true,showTimes:true,showRemaining:true,showPercent:true,showProgress:true,showBreaks:true,showLunch:true,showWeekInfo:true,gapWidgetLabel:'',lunchWidgetLabel:''});
                  AndroidSchedule.saveAdvancedSettings(JSON.stringify(a));refreshAll()
                }
                function ensure(){
                  addButton('textSettings86','resetText87',resetText);
                  addButton('colorSettings86','resetColors87',resetColors);
                  addButton('widgetSettings86','resetWidget87',resetWidget);
                  const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                }
                function refresh(){ensure()}
                window.refreshSettingsReset=refresh;
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(()=>requestAnimationFrame(ensure)).observe(sheet,{childList:true,subtree:true});
                ensure();
              }catch(e){console.log('SettingsResetUi',e)}
            })();
            """;
    }

}
