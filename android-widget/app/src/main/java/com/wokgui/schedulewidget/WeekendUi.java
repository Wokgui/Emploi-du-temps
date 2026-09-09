package com.wokgui.schedulewidget;

final class WeekendUi {
    private WeekendUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekendUiV1){
                  if(window.refreshWeekendUi)window.refreshWeekendUi();
                  return;
                }
                window.__weekendUiV1=true;

                const WEEKEND=[7,1];
                let enabledWeekend=[];
                let refreshing=false;

                function clone(o){return JSON.parse(JSON.stringify(o))}
                function lang(){
                  try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function shortName(d){
                  if(d===7)return tr('Sam','Sat','Sa');
                  if(d===1)return tr('Dim','Sun','So');
                  return '';
                }
                function fullName(d){
                  if(d===7)return tr('Samedi','Saturday','Samstag');
                  if(d===1)return tr('Dimanche','Sunday','Sonntag');
                  return '';
                }
                function nativeRoot(){try{return JSON.parse(AndroidSchedule.loadSchedule()||'{}')}catch(e){return {}}}
                function nativeEnabled(root){
                  const a=Array.isArray(root&&root._enabledDays)?root._enabledDays.map(Number):[];
                  return WEEKEND.filter(d=>a.includes(d));
                }
                function isEnabled(d){return enabledWeekend.includes(Number(d))}
                function ensureWeekDay(w,d){
                  try{
                    if(typeof weeks==='undefined'||!weeks[w])return null;
                    if(!weeks[w][d])weeks[w][d]={enabled:true,courses:[]};
                    if(!Array.isArray(weeks[w][d].courses))weeks[w][d].courses=[];
                    return weeks[w][d];
                  }catch(e){return null}
                }
                function mergeNativeWeekends(root){
                  try{
                    if(typeof weeks==='undefined')return;
                    const all=root&&root._weeks&&typeof root._weeks==='object'?root._weeks:{};
                    ['A','B','C','D'].forEach(w=>{
                      if(!weeks[w])return;
                      WEEKEND.forEach(d=>{
                        const saved=all[w]&&(all[w][String(d)]||all[w][d]);
                        const target=ensureWeekDay(w,d);
                        if(saved&&target){target.enabled=true;target.courses=clone(Array.isArray(saved.courses)?saved.courses:[])}
                      });
                    });
                  }catch(e){}
                }
                function syncDayNames(){
                  try{
                    if(typeof NAMES!=='undefined'){NAMES[7]=shortName(7);NAMES[1]=shortName(1)}
                    if(typeof FULL!=='undefined'){FULL[7]=fullName(7);FULL[1]=fullName(1)}
                  }catch(e){}
                }
                function syncGlobalDays(){
                  try{
                    if(typeof DAYS==='undefined'||!Array.isArray(DAYS))return;
                    const wanted=[2,3,4,5,6];
                    if(isEnabled(7))wanted.push(7);
                    if(isEnabled(1))wanted.push(1);
                    DAYS.splice(0,DAYS.length,...wanted);
                    if(typeof weeks!=='undefined'){
                      ['A','B','C','D'].forEach(w=>{
                        if(!weeks[w])return;
                        WEEKEND.forEach(d=>ensureWeekDay(w,d));
                      });
                    }
                  }catch(e){}
                }
                function updateWeekGridGeometry(){
                  try{
                    const grid=document.getElementById('weekGrid');
                    if(!grid||typeof DAYS==='undefined')return;
                    const first=window.innerWidth<=560?35:39;
                    grid.style.setProperty('grid-template-columns',first+'px repeat('+DAYS.length+',minmax(0,1fr))','important');
                    document.documentElement.classList.toggle('weekendScheduleEnabled',DAYS.length>5);
                  }catch(e){}
                }
                function saveEnabledAndCourses(){
                  try{
                    const root=(typeof exportState==='function')?exportState():nativeRoot();
                    AndroidSchedule.saveSchedule(JSON.stringify(root));
                  }catch(e){}
                }

                const style=document.createElement('style');
                style.textContent=`
                  .dayTab.weekendAdd{border-style:dashed!important;color:var(--blue)!important;background:#fff!important;font-weight:850!important}
                  .dayTab.weekendAdd:active{background:var(--soft)!important}
                  #removeWeekendDay{display:none;width:100%;margin:-1px 0 7px;padding:7px 9px;border:1px solid #efb7c2;border-radius:7px;background:#fff7f9;color:#c72c4a;font-size:.72rem;font-weight:850}
                  .weekendScheduleEnabled #weekGrid .wh.day{font-size:.60rem!important}
                  .weekendScheduleEnabled #weekGrid .wc .cellLabel{font-size:.54rem!important}
                  @media(max-width:560px){
                    .dayTabs{gap:3px!important}.dayTab{min-width:43px!important;padding-left:6px!important;padding-right:6px!important}
                    .weekendScheduleEnabled #weekGrid .wh.day{font-size:.56rem!important}
                    .weekendScheduleEnabled #weekGrid .wc .cellLabel{font-size:.50rem!important}
                  }
                `;
                document.head.appendChild(style);

                function installRemoveButton(){
                  const top=document.querySelector('#viewEdit .editTop');if(!top)return null;
                  let b=document.getElementById('removeWeekendDay');
                  if(!b){b=document.createElement('button');b.id='removeWeekendDay';b.type='button';top.insertAdjacentElement('afterend',b)}
                  return b;
                }
                function updateRemoveButton(){
                  const b=installRemoveButton();if(!b)return;
                  const d=(typeof selected!=='undefined')?Number(selected):0;
                  const show=WEEKEND.includes(d)&&isEnabled(d);
                  b.style.display=show?'block':'none';
                  if(!show)return;
                  b.textContent=tr('Supprimer '+fullName(d).toLowerCase(),'Remove '+fullName(d),'Remove '+fullName(d));
                  b.onclick=()=>removeWeekend(d);
                }
                function confirmCreate(d){
                  return confirm(tr(
                    'Créer '+fullName(d).toLowerCase()+' dans l’emploi du temps ? Vous pourrez le supprimer ensuite.',
                    'Add '+fullName(d)+' to the timetable? You can remove it later.',
                    fullName(d)+' zum Stundenplan hinzufügen? Der Tag kann später wieder entfernt werden.'
                  ));
                }
                function confirmRemove(d){
                  return confirm(tr(
                    'Supprimer '+fullName(d).toLowerCase()+' et tous ses cours dans toutes les semaines ?',
                    'Remove '+fullName(d)+' and all its classes from every week?',
                    fullName(d)+' und alle dort eingetragenen Stunden in allen Wochen entfernen?'
                  ));
                }
                function createWeekend(d){
                  d=Number(d);if(!WEEKEND.includes(d)||isEnabled(d)||!confirmCreate(d))return;
                  enabledWeekend.push(d);
                  WEEKEND.forEach(x=>{if(isEnabled(x)===false)return});
                  syncDayNames();syncGlobalDays();
                  try{['A','B','C','D'].forEach(w=>ensureWeekDay(w,d));selected=d}catch(e){}
                  saveEnabledAndCourses();
                  try{if(typeof render==='function')render()}catch(e){}
                  setTimeout(refresh,0);
                }
                function removeWeekend(d){
                  d=Number(d);if(!WEEKEND.includes(d)||!isEnabled(d)||!confirmRemove(d))return;
                  enabledWeekend=enabledWeekend.filter(x=>x!==d);
                  try{
                    if(typeof weeks!=='undefined')['A','B','C','D'].forEach(w=>{const day=ensureWeekDay(w,d);if(day)day.courses=[]});
                    if(typeof selected!=='undefined'&&Number(selected)===d)selected=6;
                  }catch(e){}
                  syncGlobalDays();
                  saveEnabledAndCourses();
                  try{if(typeof render==='function')render()}catch(e){}
                  setTimeout(refresh,0);
                }

                function decorateDayTabs(){
                  const box=document.getElementById('dayTabs');if(!box)return;
                  WEEKEND.forEach(d=>{
                    if(isEnabled(d))return;
                    if(box.querySelector('.weekendAdd[data-day="'+d+'"]'))return;
                    const b=document.createElement('button');b.type='button';b.className='dayTab weekendAdd';b.dataset.day=String(d);b.textContent='＋ '+shortName(d);b.onclick=()=>createWeekend(d);box.appendChild(b);
                  });
                  updateRemoveButton();
                }
                function wrapDayTabs(){
                  if(typeof window.renderDayTabs!=='function'||window.renderDayTabs.__weekendWrapped)return;
                  const old=window.renderDayTabs;
                  const wrapped=function(){syncDayNames();syncGlobalDays();const out=old.apply(this,arguments);decorateDayTabs();return out};
                  wrapped.__weekendWrapped=true;window.renderDayTabs=wrapped;
                }
                function wrapRenderEdit(){
                  if(typeof window.renderEdit!=='function'||window.renderEdit.__weekendWrapped)return;
                  const old=window.renderEdit;
                  const wrapped=function(){syncDayNames();syncGlobalDays();const out=old.apply(this,arguments);decorateDayTabs();updateRemoveButton();return out};
                  wrapped.__weekendWrapped=true;window.renderEdit=wrapped;
                }
                function wrapRenderWeek(){
                  if(typeof window.renderWeek!=='function'||window.renderWeek.__weekendWrapped)return;
                  const old=window.renderWeek;
                  const wrapped=function(){syncDayNames();syncGlobalDays();const out=old.apply(this,arguments);updateWeekGridGeometry();return out};
                  wrapped.__weekendWrapped=true;window.renderWeek=wrapped;
                }
                function wrapTodayKey(){
                  try{
                    const f=function(){const d=new Date().getDay();return d===0?1:d+1};
                    f.__weekendWrapped=true;window.todayKey=f;
                  }catch(e){}
                }
                function wrapExport(){
                  if(typeof window.exportState!=='function'||window.exportState.__weekendWrapped)return;
                  const old=window.exportState;
                  const wrapped=function(){
                    const root=old.apply(this,arguments)||{};
                    root._enabledDays=[2,3,4,5,6].concat(isEnabled(7)?[7]:[]).concat(isEnabled(1)?[1]:[]);
                    root._weeks=root._weeks||{};
                    ['A','B','C','D'].forEach(w=>{
                      if(typeof weeks==='undefined'||!weeks[w])return;
                      root._weeks[w]=root._weeks[w]||{};
                      WEEKEND.forEach(d=>{
                        const day=ensureWeekDay(w,d);
                        root._weeks[w][String(d)]={enabled:isEnabled(d),courses:clone(day&&Array.isArray(day.courses)?day.courses:[])};
                      });
                    });
                    return root;
                  };
                  wrapped.__weekendWrapped=true;window.exportState=wrapped;
                }
                function wrapSettingsRefresh(){
                  const name='refreshSettingsV3',old=window[name];
                  if(typeof old!=='function'||old.__weekendRefreshWrapped)return;
                  const wrapped=function(){const out=old.apply(this,arguments);refresh();return out};wrapped.__weekendRefreshWrapped=true;window[name]=wrapped;
                }

                function refresh(){
                  if(refreshing)return;refreshing=true;
                  try{
                    const root=nativeRoot();enabledWeekend=nativeEnabled(root);
                    syncDayNames();syncGlobalDays();mergeNativeWeekends(root);
                    wrapTodayKey();wrapExport();wrapDayTabs();wrapRenderEdit();wrapRenderWeek();wrapSettingsRefresh();
                    decorateDayTabs();updateRemoveButton();updateWeekGridGeometry();
                  }catch(e){}finally{refreshing=false}
                }
                window.refreshWeekendUi=refresh;
                refresh();

              }catch(e){console.log('WeekendUi',e)}
            })();
            """;
    }
}
