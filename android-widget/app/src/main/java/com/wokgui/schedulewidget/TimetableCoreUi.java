package com.wokgui.schedulewidget;

/** Core timetable features, settings, colors, breaks and bulk editing. */
final class TimetableCoreUi {
    private TimetableCoreUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(215 * 1024);
        out.append(layer0()).append('\n'); // WeekendUi
        out.append(layer1()).append('\n'); // FinalPolishUi
        out.append(layer2()).append('\n'); // FinalPolishLateUi
        out.append(layer3()).append('\n'); // AdvancedFeaturesUi
        out.append(layer4()).append('\n'); // UiPolishAndSchoolCalendarUi
        out.append(layer5()).append('\n'); // CourseColorUi
        out.append(layer6()).append('\n'); // PaletteSelectorUi
        out.append(layer7()).append('\n'); // LunchBreakUi
        out.append(layer8()).append('\n'); // DoubleLunchUi
        out.append(layer9()).append('\n'); // BulkCourseUi
        return out.toString();
    }

    // Former WeekendUi; isolated to stay below JVM constant limits.
    private static String layer0() {
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

    // Former FinalPolishUi; isolated to stay below JVM constant limits.
    private static String layer1() {
        return """
            (function(){
              try{
                if(window.__finalPolishV1){
                  if(window.refreshFinalPolish)window.refreshFinalPolish();
                  return;
                }
                window.__finalPolishV1=true;
                const APP_VERSION='6.31';
                let refreshing=false;

                function ui(){try{return JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){return {}}}
                function adv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function lang(){const l=ui().language;return l==='en'||l==='de'?l:'fr'}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function toMin(v){const p=String(v||'').split(':').map(Number);return (p[0]||0)*60+(p[1]||0)}
                function dayKeyNow(){const d=new Date().getDay();return d===0?1:d+1}
                function slotName(i){
                  const n=i+1;
                  if(lang()==='en')return n+(n===1?'st':n===2?'nd':n===3?'rd':'th')+' period';
                  if(lang()==='de')return n+'. Stunde';
                  return n===1?'1ère heure':n+'ème heure';
                }

                const style=document.createElement('style');
                style.id='finalPolishV1Style';
                style.textContent=`
                  /* Header: content centered lower, between the S23 Ultra camera area and the bottom edge. */
                  .header{height:88px!important;padding-top:26px!important;box-sizing:border-box!important;align-items:center!important}
                  .header h1{position:relative!important;top:0!important;margin:0!important;line-height:1.05!important}
                  #settingsBtn{top:57px!important;bottom:auto!important;transform:translateY(-50%)!important}

                  /* No visual transition/ghosting when switching weeks or tabs. */
                  .view,.weekGrid,.weekTabs,.weekTab,.wc,.wh,#weekGrid *{transition:none!important;animation:none!important}

                  /* Today: narrower, centered time column. */
                  #todayList .todayCourse{grid-template-columns:48px minmax(0,1fr) auto!important;gap:5px!important}
                  #todayList .todayCourse .time{width:48px!important;text-align:center!important;justify-self:center!important}
                  #todayList #todayNowRail,#todayList #todayNowDot{left:51px!important}

                  /* Week table separators. */
                  #weekGrid>.wh.timecol{border-right:1.5px solid #cbd5e1!important}
                  #weekGrid>.wh.day{border-bottom:1.5px solid #cbd5e1!important}
                  #weekGrid>.wh.timecol:first-child{border-bottom:1.5px solid #cbd5e1!important}

                  /* Only the final horizontal marker is visible. All old vertical rails are hidden. */
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,
                  #weekGrid [id*="WeekNow"],#weekGrid [id*="weekNow"],
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,
                  #weekGrid .nativeNowFull,#weekGrid .nativeNowPartial,#weekGrid .nativeNowDot{display:none!important}
                  #weekGrid .finalNowCourse{position:relative!important;overflow:visible!important;z-index:25!important}
                  #weekGrid .finalNowBar{position:absolute!important;left:0!important;right:0!important;height:2px!important;background:#1688F4!important;z-index:190!important;pointer-events:none!important;box-shadow:0 0 0 .3px #1688F4!important}
                  #weekGrid .finalNowDot{position:absolute!important;left:0!important;width:10px!important;height:10px!important;border-radius:50%!important;transform:translate(-50%,-50%)!important;background:#1688F4!important;border:2px solid #D9ECFF!important;box-sizing:border-box!important;z-index:191!important;pointer-events:none!important}

                  /* Lunch is one clean band: strong top/bottom strokes; adjacent identical lunch cells share them. */
                  #weekGrid .finalLunchCell{border-radius:0!important;outline:0!important;margin:0!important;padding:0!important;background:var(--ft-midi,#FFF9E8)!important;overflow:hidden!important}
                  #weekGrid .finalLunchCell .dynamicLunchOverlay,#weekGrid .finalLunchCell .nativeLunchLabel{inset:0!important;border:0!important;border-radius:0!important;box-shadow:none!important;background:transparent!important}
                  #weekGrid .finalLunchJoinedRight{border-right-color:transparent!important}

                  /* Quick week cycle higher, importer centered. */
                  #weekModeBar{margin:0 0 7px!important;padding:6px 7px!important;display:grid!important;grid-template-columns:auto minmax(0,1fr)!important;align-items:center!important}
                  #weekModeBar .weekModeChoices{display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;gap:4px!important}
                  #weekModeBar .weekModeChoice{min-width:0!important;padding:6px 2px!important;font-size:.64rem!important}
                  #importPhoto{min-height:42px!important;display:flex!important;align-items:center!important;justify-content:center!important;text-align:center!important;margin:0 0 7px!important}

                  /* Same look and typography for the two add-course tiles. */
                  #addCourse,#addBulkCourses{width:100%!important;margin-top:7px!important;padding:9px!important;border:1.5px solid var(--blue,#0877f9)!important;border-radius:7px!important;background:#edf6ff!important;color:var(--blue,#0877f9)!important;font-size:.84rem!important;font-weight:800!important;line-height:1.15!important}

                  /* Settings: clearer cards and centered category titles. */
                  #settingsSheet>.settingBox{border:1.5px solid #cbd6e2!important;box-shadow:0 1px 3px #15223810!important}
                  #settingsSheet .settingTitle{text-align:center!important;font-weight:850!important}
                  #settingsSheet .settingsHead{position:relative!important;justify-content:center!important}
                  #settingsSheet .settingsHead h2{text-align:center!important;width:100%!important}
                  #settingsSheet .settingsX{position:absolute!important;right:0!important}
                  #appVersionInfo{text-align:center!important;margin:0 0 7px!important;color:#7a8494!important;font-size:.68rem!important;font-weight:800!important}
                  #fineSpecialColors .settingTitle,#breakDisplaySetting .settingTitle{margin-bottom:4px!important}
                  #fineSpecialColors .coursePaletteHint,#breakDisplaySetting .coursePaletteHint{margin-top:0!important;margin-bottom:8px!important;line-height:1.28!important}
                  #advExceptionsTitle~.advButtons,#advProfilesTitle~.advButtons,#advBackupTitle~.advButtons{justify-content:center!important}
                  #advExceptionsTitle~.advButtons .advButton,#advProfilesTitle~.advButtons .advButton,#advBackupTitle~.advButtons .advButton{min-width:112px;text-align:center}

                  /* Realistic live font previews. */
                  .previewAppTop{font-size:calc(7px * var(--preview-app-scale,1))!important;height:calc(20px * var(--preview-app-scale,1))!important}
                  .previewBody{padding:calc(5px * var(--preview-app-scale,1))!important}
                  .previewBody .previewLine{height:calc(6px * var(--preview-app-scale,1))!important;margin:calc(3px * var(--preview-app-scale,1))!important}
                  .previewWidget{padding:calc(5px * var(--preview-widget-scale,1))!important}
                  .previewWidgetTitle{font-size:calc(7px * var(--preview-widget-scale,1))!important;line-height:1.05!important}
                  .previewWidgetMeta{font-size:calc(6px * var(--preview-widget-scale,1))!important;margin-top:calc(2px * var(--preview-widget-scale,1))!important}
                  .previewBar{height:calc(4px * var(--preview-widget-scale,1))!important;margin-top:calc(4px * var(--preview-widget-scale,1))!important}

                  /* Slots and weekend tabs stay readable even with seven visible day buttons. */
                  #slotSettings .slotNum{text-align:center!important;white-space:nowrap!important}
                  .dayTabs{gap:3px!important}
                  .dayTab{min-width:43px!important;padding-left:6px!important;padding-right:6px!important}
                  @media(max-width:560px){
                    #weekModeBar{grid-template-columns:1fr!important}.weekModeLabel{text-align:center;margin-bottom:4px}.weekModeChoices{width:100%}
                    #weekGrid>.wh.timecol{font-size:.55rem!important}
                  }
                `;
                document.head.appendChild(style);

                function ensureNineSlots(){
                  try{
                    if(typeof slots==='undefined'||!Array.isArray(slots))return;
                    const defaults=[['17:00','18:00'],['18:00','19:00']];
                    while(slots.length<9){const i=slots.length-7,p=defaults[Math.max(0,Math.min(1,i))];slots.push({n:slots.length+1,start:p[0],end:p[1]})}
                    if(slots.length>9)slots.splice(9);
                    slots.forEach((s,i)=>s.n=i+1);
                    const head=document.querySelector('#viewEdit .sectionHead h3');
                    document.querySelectorAll('#viewEdit .sectionHead h3').forEach(h=>{if(/Horaires|period times|Zeiten/i.test(h.textContent||''))h.textContent=tr('Horaires des 9 heures','9 period times','Zeiten der 9 Stunden')});
                  }catch(e){}
                }

                function prettySlotRows(){
                  ensureNineSlots();
                  const rows=document.querySelectorAll('#slotSettings .slotRow');
                  rows.forEach((row,i)=>{const n=row.querySelector('.slotNum');if(n)n.textContent=slotName(i)});
                  const sel=document.getElementById('fSlot');
                  if(sel){[...sel.options].forEach(o=>{const n=Number(o.value);if(n>0&&slots[n-1])o.textContent=slotName(n-1)+' · '+slots[n-1].start+'–'+slots[n-1].end})}
                }

                function wrapSlots(){
                  if(typeof window.renderSlots==='function'&&!window.renderSlots.__finalPolish){
                    const old=window.renderSlots;const w=function(){ensureNineSlots();const r=old.apply(this,arguments);prettySlotRows();return r};w.__finalPolish=true;window.renderSlots=w;
                  }
                  if(typeof window.fillSlotOptions==='function'&&!window.fillSlotOptions.__finalPolish){
                    const old=window.fillSlotOptions;const w=function(){ensureNineSlots();const r=old.apply(this,arguments);prettySlotRows();return r};w.__finalPolish=true;window.fillSlotOptions=w;
                  }
                }

                function installCycleBar(){
                  const bar=document.getElementById('weekModeBar'),view=document.getElementById('viewEdit'),importBtn=document.getElementById('importPhoto');
                  if(!bar||!view)return;
                  if(importBtn&&bar.nextElementSibling!==importBtn)view.insertBefore(bar,importBtn);
                  const choices=bar.querySelector('.weekModeChoices');if(!choices)return;
                  const wanted=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  wanted.forEach(([v,label])=>{
                    let b=choices.querySelector('.weekModeChoice[data-m="'+v+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;choices.appendChild(b)}
                    b.hidden=false;b.style.removeProperty('display');b.textContent=label;
                    b.onclick=()=>{if(window.switchCycle69)window.switchCycle69(Number(v));else chooseCycle(Number(v))};
                  });
                  const order=new Map(wanted.map((x,i)=>[x[0],i]));
                  [...choices.querySelectorAll('.weekModeChoice')].sort((a,b)=>(order.get(a.dataset.m)??99)-(order.get(b.dataset.m)??99)).forEach(b=>choices.appendChild(b));
                  const a=adv(),m=a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2));
                  choices.querySelectorAll('.weekModeChoice').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===m));
                }
                function chooseCycle(n){
                  const sel=document.getElementById('advCycle');
                  if(sel){sel.value=String(n);sel.dispatchEvent(new Event('change',{bubbles:true}));setTimeout(refresh,0);return}
                  try{const a=adv();a.singleWeek=n===1;a.cycleLength=n===1?2:n;AndroidSchedule.saveAdvancedSettings(JSON.stringify(a));if(window.reloadSchedule)window.reloadSchedule()}catch(e){}
                }

                function updatePreviews(){
                  const o=ui(),a=Number(o.appFontScale)||1,w=Number(o.widgetFontScale)||1;
                  document.documentElement.style.setProperty('--preview-app-scale',Math.max(.8,Math.min(1.4,a)));
                  document.documentElement.style.setProperty('--preview-widget-scale',Math.max(.8,Math.min(1.4,w)));
                }

                function reorderSettings(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  let version=document.getElementById('appVersionInfo');
                  if(!version){version=document.createElement('div');version.id='appVersionInfo'}
                  version.textContent='Version '+APP_VERSION;
                  const head=sheet.querySelector('.settingsHead');if(head&&version.nextElementSibling!==head)sheet.insertBefore(version,head);

                  const themeTitle=document.getElementById('themeTitle'),themeBox=themeTitle?themeTitle.closest('.settingBox'):null;
                  const palette=document.getElementById('paletteSettingRoot'),full=document.getElementById('fineSpecialColors');
                  if(themeBox&&palette){themeBox.insertAdjacentElement('afterend',palette);if(full)palette.insertAdjacentElement('afterend',full)}

                  const widgetTitle=document.getElementById('advWidgetTitle'),widgetBox=widgetTitle?widgetTitle.closest('.settingBox'):null;
                  const breaks=document.getElementById('breakDisplaySetting');if(widgetBox&&breaks)widgetBox.insertAdjacentElement('afterend',breaks);
                }

                function rowsOf(grid){
                  const rows=[];if(!grid)return rows;
                  const dayCount=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.length:5;
                  const times=Array.from(grid.querySelectorAll(':scope > .wh.timecol'));
                  for(const time of times){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(found.length<2)continue;
                    const cells=[];let n=time.nextElementSibling;
                    while(n&&cells.length<dayCount){if(n.classList&&n.classList.contains('wc'))cells.push(n);n=n.nextElementSibling}
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return rows;
                }

                function clearFinalNow(grid){
                  grid.querySelectorAll('.finalNowBar,.finalNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('.finalNowCourse').forEach(x=>x.classList.remove('finalNowCourse'));
                }
                function paintFinalNow(grid,rows){
                  clearFinalNow(grid);if(!rows.length)return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}
                  const d=dayKeyNow(),dayIndex=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.indexOf(d):-1;if(dayIndex<0)return;
                  const now=new Date(),minute=now.getHours()*60+now.getMinutes();let row=null,frac=0;
                  for(const r of rows){if(minute>=r.start&&minute<r.end){row=r;frac=(minute-r.start)/Math.max(1,r.end-r.start);break}}
                  if(!row)return;const cell=row.cells[dayIndex];if(!cell||!cell.classList.contains('has'))return;
                  cell.classList.add('finalNowCourse');
                  const pct=Math.max(0,Math.min(100,frac*100)).toFixed(4)+'%';
                  const bar=document.createElement('span');bar.className='finalNowBar';bar.style.setProperty('top',pct,'important');cell.appendChild(bar);
                  const dot=document.createElement('span');dot.className='finalNowDot';dot.style.setProperty('top',pct,'important');cell.appendChild(dot);
                }

                function shouldLunch(row,dayIndex){
                  try{
                    const d=DAYS[dayIndex],list=state&&state[d]&&Array.isArray(state[d].courses)?state[d].courses:[];
                    if(typeof lunchForDay==='function'){
                      const l=lunchForDay(list);return !!(l&&l.startM===row.start&&l.endM===row.end);
                    }
                    const before=list.some(c=>toMin(c.end)<=row.start),after=list.some(c=>toMin(c.start)>=row.end),overlap=list.some(c=>toMin(c.start)<row.end&&toMin(c.end)>row.start);
                    return before&&after&&!overlap;
                  }catch(e){return false}
                }
                function paintLunchGroups(grid,rows){
                  grid.querySelectorAll('.finalLunchCell,.finalLunchJoinedRight').forEach(c=>{
                    c.classList.remove('finalLunchCell','finalLunchJoinedRight');
                    c.style.removeProperty('box-shadow');c.style.removeProperty('border-right-color');
                  });
                  grid.querySelectorAll('.lunch69TopLine,.lunch69BottomLine').forEach(c=>c.classList.remove('lunch69TopLine','lunch69BottomLine'));
                  rows.forEach((row,ri)=>row.cells.forEach((cell,di)=>{
                    if(!shouldLunch(row,di)||!(cell.classList.contains('lunchCell')||cell.classList.contains('dynamicLunchCell')||cell.classList.contains('nativeLunchCell')))return;
                    cell.classList.add('lunch69BottomLine');
                    cell.style.setProperty('box-shadow','none','important');cell.style.removeProperty('border-right-color');
                    const above=ri>0?rows[ri-1].cells[di]:grid.querySelectorAll(':scope > .wh.day')[di];
                    if(above)above.classList.add('lunch69TopLine');
                  }));
                }

                function paintWeek(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  const rows=rowsOf(grid);paintLunchGroups(grid,rows);paintFinalNow(grid,rows);
                }
                function wrapWeekRender(){
                  if(typeof window.renderWeek==='function'&&!window.renderWeek.__finalPolish){
                    const old=window.renderWeek;const w=function(){const r=old.apply(this,arguments);paintWeek();return r};w.__finalPolish=true;window.renderWeek=w;
                  }
                  if(typeof window.render==='function'&&!window.render.__finalPolish){
                    const old=window.render;const w=function(){const r=old.apply(this,arguments);if(typeof mode!=='undefined'&&mode==='week')paintWeek();return r};w.__finalPolish=true;window.render=w;
                  }
                }

                function refresh(){
                  if(refreshing)return;refreshing=true;
                  try{
                    ensureNineSlots();wrapSlots();wrapWeekRender();installCycleBar();reorderSettings();prettySlotRows();updatePreviews();paintWeek();
                    if(window.refreshWeekendUi)window.refreshWeekendUi();
                  }catch(e){}finally{refreshing=false}
                }
                window.refreshFinalPolish=refresh;

                document.addEventListener('input',e=>{if(e.target&&(['appFont','widgetFont'].includes(e.target.id)))setTimeout(updatePreviews,0)},true);
                const grid=document.getElementById('weekGrid');if(grid){new MutationObserver(()=>paintWeek()).observe(grid,{childList:true,subtree:false})}
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)paintWeek()});
                setInterval(paintWeek,15000);
                refresh();
              }catch(e){console.log('FinalPolishUi',e)}
            })();
            """;
    }

    // Former FinalPolishLateUi; isolated to stay below JVM constant limits.
    private static String layer2() {
        return """
            (function(){
              try{
                if(window.__finalPolishLateV2){if(window.refreshFinalPolish)window.refreshFinalPolish();return}
                window.__finalPolishLateV2=true;
                let late=0;
                function rerun(delay){
                  if(late)clearTimeout(late);
                  const run=()=>{try{if(window.refreshFinalPolish)window.refreshFinalPolish()}catch(e){}};
                  if(delay&&delay>0)late=setTimeout(run,delay);else run();
                }
                function wrap(name,delay){
                  const old=window[name];if(typeof old!=='function'||old.__finalLateWrapped)return;
                  const w=function(){const r=old.apply(this,arguments);rerun(delay);return r};w.__finalLateWrapped=true;window[name]=w;
                }
                function install(){
                  /* Only settings/bulk refreshes need a late layout pass.
                     Lunch and week-grid observers were intentionally removed: they caused repeated repaint cycles and visible blinking. */
                  wrap('refreshSettingsV3',0);
                  wrap('refreshBulkCourseUi',0);
                  rerun(0);
                }
                install();
              }catch(e){console.log('FinalPolishLateUi',e)}
            })();
            """;
    }

    // Former AdvancedFeaturesUi; isolated to stay below JVM constant limits.
    private static String layer3() {
        return """
            (function(){
              try {
                if(window.__advancedFeaturesV1){
                  if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                  return;
                }
                window.__advancedFeaturesV1=true;

                const A_TXT={
                  fr:{widgetDisplay:'Affichage du widget',density:'Densité',format:'Format',following:'Cours suivants',auto:'Automatique',compact:'Compact',normal:'Normal',comfortable:'Confortable',timeline:'Chronologie de la journée',nowNext:'Maintenant + prochain',room:'Salle',times:'Horaires',remaining:'Temps restant',percent:'Pourcentage',progress:'Barre de progression',gaps:'Trous',lunch:'Pause de midi',weekInfo:'Semaine et cycle',classColors:'Couleur par classe',access:'Accessibilité',accessNormal:'Normale',contrast:'Contraste élevé',colorblind:'Palette daltonisme',cycle:'Cycle de semaines',cycle2:'2 semaines (A/B)',cycle3:'3 semaines (A/B/C)',cycle4:'4 semaines (A/B/C/D)',copyWeek:'Copier la semaine active vers la suivante',copyDay:'Copier un jour',from:'De',to:'Vers',copy:'Copier',reminders:'Rappels',notify:'Notifier avant le cours',calendar:'Vacances et jours sans cours',holidays:'Jours fériés automatiques',holOff:'Désactivés',holFrance:'France',holAlsace:'Alsace-Moselle',addRange:'Ajouter une période sans cours',label:'Libellé',add:'Ajouter',exceptions:'Modifications exceptionnelles',addException:'Ajouter une exception',noException:'Aucune exception',cancelCourse:'Cours annulé',roomChange:'Changement de salle',moveCourse:'Cours déplacé',extraCourse:'Cours / réunion exceptionnel',date:'Date',referenceStart:'Heure du cours concerné',referenceLabel:'Classe / libellé du cours',newStart:'Nouvelle heure de début',newEnd:'Nouvelle heure de fin',newRoom:'Salle',newLabel:'Nouveau libellé',save:'Enregistrer',delete:'Supprimer',profiles:'Profils',newProfile:'Nouveau profil',rename:'Renommer',backup:'Sauvegarde',share:'Partager la sauvegarde',restore:'Restaurer une sauvegarde',blank:'Créer vide ?',dayOff:'Jour sans cours',uncertain:'À vérifier',importOk:'Sauvegarde restaurée.',importFail:'Ce fichier ne contient pas une sauvegarde valide.',duplicateQuestion:'Dupliquer le profil actuel ?'},
                  en:{widgetDisplay:'Widget display',density:'Density',format:'Format',following:'Following classes',auto:'Automatic',compact:'Compact',normal:'Normal',comfortable:'Comfortable',timeline:'Day timeline',nowNext:'Now + next',room:'Room',times:'Times',remaining:'Time left',percent:'Percentage',progress:'Progress bar',gaps:'Free periods',lunch:'Lunch break',weekInfo:'Week and cycle',classColors:'Color by class',access:'Accessibility',accessNormal:'Normal',contrast:'High contrast',colorblind:'Color-blind palette',cycle:'Week cycle',cycle2:'2 weeks (A/B)',cycle3:'3 weeks (A/B/C)',cycle4:'4 weeks (A/B/C/D)',copyWeek:'Copy active week to the next one',copyDay:'Copy a day',from:'From',to:'To',copy:'Copy',reminders:'Reminders',notify:'Notify before class',calendar:'Holidays and days off',holidays:'Automatic public holidays',holOff:'Off',holFrance:'France',holAlsace:'Alsace-Moselle',addRange:'Add a period with no classes',label:'Label',add:'Add',exceptions:'One-off changes',addException:'Add an exception',noException:'No exception',cancelCourse:'Cancelled class',roomChange:'Room change',moveCourse:'Moved class',extraCourse:'Extra class / meeting',date:'Date',referenceStart:'Original class start',referenceLabel:'Class / original label',newStart:'New start',newEnd:'New end',newRoom:'Room',newLabel:'New label',save:'Save',delete:'Delete',profiles:'Profiles',newProfile:'New profile',rename:'Rename',backup:'Backup',share:'Share backup',restore:'Restore backup',blank:'Create blank?',dayOff:'No class',uncertain:'Check',importOk:'Backup restored.',importFail:'This file is not a valid backup.',duplicateQuestion:'Duplicate current profile?'},
                  de:{widgetDisplay:'Widget-Anzeige',density:'Dichte',format:'Format',following:'Folgende Stunden',auto:'Automatisch',compact:'Kompakt',normal:'Normal',comfortable:'Komfortabel',timeline:'Tagesverlauf',nowNext:'Jetzt + nächste Stunde',room:'Raum',times:'Zeiten',remaining:'Restzeit',percent:'Prozent',progress:'Fortschrittsbalken',gaps:'Freistunden',lunch:'Mittagspause',weekInfo:'Woche und Zyklus',classColors:'Farbe je Klasse',access:'Barrierefreiheit',accessNormal:'Normal',contrast:'Hoher Kontrast',colorblind:'Farbenblind-Palette',cycle:'Wochenzyklus',cycle2:'2 Wochen (A/B)',cycle3:'3 Wochen (A/B/C)',cycle4:'4 Wochen (A/B/C/D)',copyWeek:'Aktive Woche in die nächste kopieren',copyDay:'Tag kopieren',from:'Von',to:'Nach',copy:'Kopieren',reminders:'Erinnerungen',notify:'Vor dem Unterricht erinnern',calendar:'Ferien und unterrichtsfreie Tage',holidays:'Automatische Feiertage',holOff:'Aus',holFrance:'Frankreich',holAlsace:'Elsass-Mosel',addRange:'Unterrichtsfreie Zeit hinzufügen',label:'Bezeichnung',add:'Hinzufügen',exceptions:'Einmalige Änderungen',addException:'Ausnahme hinzufügen',noException:'Keine Ausnahme',cancelCourse:'Stunde fällt aus',roomChange:'Raumänderung',moveCourse:'Stunde verlegt',extraCourse:'Zusätzliche Stunde / Besprechung',date:'Datum',referenceStart:'Beginn der betroffenen Stunde',referenceLabel:'Klasse / ursprüngliche Bezeichnung',newStart:'Neuer Beginn',newEnd:'Neues Ende',newRoom:'Raum',newLabel:'Neue Bezeichnung',save:'Speichern',delete:'Löschen',profiles:'Profile',newProfile:'Neues Profil',rename:'Umbenennen',backup:'Sicherung',share:'Sicherung teilen',restore:'Sicherung wiederherstellen',blank:'Leer erstellen?',dayOff:'Unterrichtsfrei',uncertain:'Prüfen',importOk:'Sicherung wiederhergestellt.',importFail:'Diese Datei ist keine gültige Sicherung.',duplicateQuestion:'Aktuelles Profil duplizieren?'}
                };

                let adv={density:'normal',upcomingCount:0,widgetFormat:'timeline',showRoom:true,showTimes:true,showRemaining:true,showPercent:true,showProgress:true,showBreaks:true,showLunch:true,showWeekInfo:true,colorByClass:false,accessibility:'normal',cycleLength:2,remindersEnabled:false,reminderMinutes:10,holidayMode:'alsace_moselle',exceptions:[],dayOffRanges:[]};
                function uiLang(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function T(){return A_TXT[uiLang()]||A_TXT.fr}
                function loadAdv(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadAdvancedSettings?AndroidSchedule.loadAdvancedSettings():localStorage.getItem('edt-advanced');if(raw)adv=Object.assign(adv,JSON.parse(raw))}catch(e){}
                  if(!Array.isArray(adv.exceptions))adv.exceptions=[];if(!Array.isArray(adv.dayOffRanges))adv.dayOffRanges=[];adv.cycleLength=Math.max(2,Math.min(4,Number(adv.cycleLength)||2));
                }
                function saveAdv(){
                  try{const raw=JSON.stringify(adv);if(window.AndroidSchedule&&AndroidSchedule.saveAdvancedSettings)AndroidSchedule.saveAdvancedSettings(raw);else localStorage.setItem('edt-advanced',raw)}catch(e){}
                  applyAppAppearance();
                }
                loadAdv();

                function deep(o){return JSON.parse(JSON.stringify(o))}
                function emptyWeek(){const w={};for(const d of [2,3,4,5,6])w[d]={enabled:true,courses:[]};return w}
                function ensureExtraWeeks(){
                  if(typeof weeks==='undefined')return;
                  if(!weeks.C)weeks.C=weeks.A?deep(weeks.A):emptyWeek();
                  if(!weeks.D)weeks.D=weeks.B?deep(weeks.B):emptyWeek();
                }
                ensureExtraWeeks();

                function restoreCycleFromNative(){
                  try{
                    if(!(window.AndroidSchedule&&AndroidSchedule.loadSchedule))return;
                    const root=JSON.parse(AndroidSchedule.loadSchedule()||'{}');
                    ensureExtraWeeks();
                    if(root._weeks){for(const w of ['C','D'])if(root._weeks[w])weeks[w]=root._weeks[w]}
                    if(root._cycleLength)adv.cycleLength=Math.max(2,Math.min(4,Number(root._cycleLength)||adv.cycleLength));
                    const allowed=['A','B','C','D'].slice(0,adv.cycleLength);
                    if(allowed.indexOf(root._currentWeek)>=0)currentWeek=root._currentWeek;
                    if(allowed.indexOf(activeWeek)<0)activeWeek=currentWeek;
                  }catch(e){}
                  buildWeekTabs();
                }
                restoreCycleFromNative();

                if(typeof window.normalize==='function'&&!window.normalize.__advanced){
                  const oldNormalize=window.normalize;
                  const wrapped=function(){ensureExtraWeeks();oldNormalize();if(typeof normalizeWeek==='function'){normalizeWeek(weeks.C);normalizeWeek(weeks.D)}state=weeks[mode==='today'?currentWeek:activeWeek]};
                  wrapped.__advanced=true;window.normalize=wrapped;
                }
                if(typeof window.exportState==='function'&&!window.exportState.__advanced){
                  const oldExport=window.exportState;
                  const wrapped=function(){ensureExtraWeeks();const root=oldExport();if(!root._weeks)root._weeks={};root._weeks.C=deep(weeks.C);root._weeks.D=deep(weeks.D);root._currentWeek=currentWeek;root._cycleLength=adv.cycleLength;return root};
                  wrapped.__advanced=true;window.exportState=wrapped;
                }
                if(typeof window.reloadSchedule==='function'&&!window.reloadSchedule.__advanced){
                  const oldReload=window.reloadSchedule;
                  const wrapped=function(){oldReload();restoreCycleFromNative();if(typeof render==='function')render();setTimeout(refreshAdvancedFeatures,18)};
                  wrapped.__advanced=true;window.reloadSchedule=wrapped;
                }

                function letters(){return ['A','B','C','D'].slice(0,adv.cycleLength)}
                function buildWeekTabs(){
                  const box=document.getElementById('weekTabs');if(!box||typeof activeWeek==='undefined')return;
                  ensureExtraWeeks();const list=letters();if(list.indexOf(activeWeek)<0)activeWeek=currentWeek;if(list.indexOf(currentWeek)<0)currentWeek='A';
                  const prefix=(uiLang()==='de'?'Woche ':(uiLang()==='en'?'Week ':'Semaine '));
                  for(const w of ['A','B','C','D']){
                    let b=box.querySelector('.weekTab[data-week="'+w+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekTab';b.dataset.week=w;box.appendChild(b)}
                    const visible=list.indexOf(w)>=0;
                    b.textContent=prefix+w;
                    b.className='weekTab'+(w===(mode==='today'?currentWeek:activeWeek)?' active':'');
                    b.style.visibility=visible?'visible':'hidden';
                    b.style.pointerEvents=visible?'auto':'none';
                    b.setAttribute('aria-hidden',visible?'false':'true');
                    b.tabIndex=visible?0:-1;
                    b.onclick=visible?(()=>{if(activeWeek===w)return;activeWeek=w;if(typeof render==='function')render()}):null;
                  }
                  const cw=document.getElementById('currentWeekBtn');
                  if(cw){
                    if(adv.singleWeek===true)cw.onclick=null;
                    else cw.onclick=()=>{const ls=letters(),idx=ls.indexOf(currentWeek);currentWeek=ls[(idx+1)%ls.length];activeWeek=currentWeek;if(window.AndroidSchedule&&AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(currentWeek);if(typeof save==='function')save();else if(typeof render==='function')render()};
                  }
                }

                const st=document.createElement('style');
                st.textContent=`
                  .advRow{display:flex;align-items:center;justify-content:space-between;gap:8px;margin:6px 0;font-size:.76rem}.advRow select,.advRow input[type=date],.advRow input[type=time],.advRow input[type=text]{min-width:0;border:1px solid #d8e0e8;border-radius:7px;padding:7px;background:#fff;color:inherit}.advCheck{display:flex;align-items:center;gap:7px;margin:6px 0;font-size:.76rem}.advCheck input{width:17px;height:17px;accent-color:var(--set-accent)}.advButtons{display:flex;gap:6px;flex-wrap:wrap}.advButton{border:1px solid #d6dee7;border-radius:7px;padding:7px 9px;background:#fff;color:#233047;font-weight:750;font-size:.72rem}.advButton.primary{background:var(--set-accent);border-color:var(--set-accent);color:#fff}.advButton.danger{color:#c72c4a;background:#fff6f8;border-color:#efbdc8}.advSmall{font-size:.67rem;color:#657087}.advList{margin-top:6px;border-top:1px solid #edf0f4}.advItem{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:6px 0;border-bottom:1px solid #edf0f4;font-size:.70rem}.advItemText{min-width:0;overflow:hidden;text-overflow:ellipsis}.advInline{display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-top:6px}.advInline.full{grid-template-columns:1fr}.advSectionTitle{font-size:.72rem;font-weight:850;color:var(--set-dark);margin-top:7px}.ocrUncertain{outline:2px solid #e69f00!important;outline-offset:-2px}.ocrFlag{display:inline-block;margin-left:6px;font-size:.60rem;font-weight:850;color:#a05c00;background:#fff0c7;border-radius:999px;padding:2px 5px}.exceptionBanner{margin:0 0 6px;padding:6px 8px;border:1px solid #f0d39a;background:#fff8e7;border-radius:7px;color:#725019;font-size:.72rem}.classTint{border-left:4px solid var(--class-color)!important}.accessHigh body{background:#fff!important;color:#000!important}.accessHigh .card,.accessHigh #settingsSheet{border-color:#666!important}.accessHigh .room,.accessHigh .time,.accessHigh .date{color:#222!important}.accessHigh .todayCourse.gap,.accessHigh .wc.gapCell{background:#f3f0ff!important}.accessHigh .todayCourse.lunch,.accessHigh .wc.lunchCell{background:#fff3dd!important}
                  #advModal{z-index:120}.advField{margin-bottom:8px}.advField label{display:block;color:#68738a;font-size:.70rem;margin-bottom:3px}.advField input,.advField select{width:100%;border:1px solid #d8e0e8;border-radius:7px;padding:9px;background:#fff}.advModalActions{display:flex;justify-content:flex-end;gap:7px;margin-top:10px}
                `;
                document.head.appendChild(st);

                function classColor(label){
                  const std=['#0877f9','#00897b','#6750a4','#2e7d32','#ef6c00','#c2185b'];
                  const cb=['#0072b2','#e69f00','#009e73','#cc79a7','#d55e00','#56b4e9'];
                  const p=adv.accessibility==='colorblind'?cb:std;let h=0;const s=String(label||'');for(let i=0;i<s.length;i++)h=((h*31)+s.charCodeAt(i))>>>0;return p[h%p.length]
                }
                function applyAppAppearance(){
                  document.documentElement.classList.toggle('accessHigh',adv.accessibility==='high_contrast');
                  if(!adv.colorByClass){document.querySelectorAll('.classTint').forEach(el=>{el.classList.remove('classTint');el.style.removeProperty('--class-color')});return}
                  document.querySelectorAll('.todayCourse').forEach(el=>{if(el.classList.contains('gap')||el.classList.contains('lunch'))return;const label=el.querySelector('.label');if(label){el.classList.add('classTint');el.style.setProperty('--class-color',classColor(label.textContent))}});
                  document.querySelectorAll('.wc.has').forEach(el=>{const label=el.querySelector('.cellLabel');if(label){el.classList.add('classTint');el.style.setProperty('--class-color',classColor(label.textContent))}});
                  document.querySelectorAll('.editCourse').forEach(el=>{const label=el.querySelector('.label');if(label){el.classList.add('classTint');el.style.setProperty('--class-color',classColor(label.textContent))}});
                }

                function dateKey(d){const y=d.getFullYear(),m=String(d.getMonth()+1).padStart(2,'0'),day=String(d.getDate()).padStart(2,'0');return y+'-'+m+'-'+day}
                function todayDay(){const d=new Date().getDay();return d>=1&&d<=5?d+1:2}
                function effectiveToday(){
                  try{
                    if(!(window.AndroidSchedule&&AndroidSchedule.loadEffectiveCourses))return null;
                    return JSON.parse(AndroidSchedule.loadEffectiveCourses(dateKey(new Date()))||'{}');
                  }catch(e){return null}
                }
                function renderEffectiveToday(){
                  if(typeof mode==='undefined'||mode!=='today')return;
                  const eff=effectiveToday();if(!eff)return;
                  const box=document.getElementById('todayList');if(!box)return;
                  const list=Array.isArray(eff.courses)?eff.courses:[];
                  if(eff.dayOff){box.innerHTML='<div class="empty">'+T().dayOff+'</div>';const p=document.getElementById('todayProgress');if(p)p.style.width='0%';return}
                  if(!list.length){box.innerHTML='<div class="empty">'+(uiLang()==='de'?'Heute kein Unterricht.':(uiLang()==='en'?'No class today.':'Aucun cours aujourd’hui.'))+'</div>';return}
                  const now=new Date(),nowM=now.getHours()*60+now.getMinutes();
                  const events=list.map(c=>({type:'course',start:min(c.start),end:min(c.end),course:c}));
                  const gaps=gapSegments(list);for(const g of gaps)events.push({type:'gap',start:g.start,end:g.end});
                  const l=lunch();if(l&&(adv.showLunchToday!==false)&&list.some(c=>min(c.end)<=min(l.start))&&list.some(c=>min(c.start)>=min(l.end)))events.push({type:'lunch',start:min(l.start),end:min(l.end),l:l});
                  events.sort((a,b)=>a.start-b.start||a.end-b.end);box.innerHTML='';
                  let total=0,done=0;for(const c of list){const s=min(c.start),e=min(c.end),dur=Math.max(0,e-s);total+=dur;if(nowM>=e)done+=dur;else if(nowM>s)done+=Math.min(dur,nowM-s)}const pr=document.getElementById('todayProgress');if(pr)pr.style.width=(total?Math.max(0,Math.min(100,done*100/total)):0)+'%';
                  for(const ev of events){
                    if(ev.type==='gap'&&adv.showBreaksToday===false)continue;
                    const row=document.createElement('div');
                    if(ev.type==='course'){
                      const c=ev.course,cur=nowM>=min(c.start)&&nowM<min(c.end);row.className='todayCourse'+(cur?' current':'')+(c.uncertain?' ocrUncertain':'');
                      const roomText=adv.showRoom?'<div class="room">'+(uiLang()==='de'?'Raum ':(uiLang()==='en'?'room ':'salle '))+esc(c.room||'—')+(c.slot?' · '+(uiLang()==='de'?'Stunde ':(uiLang()==='en'?'period ':'heure '))+c.slot:'')+'</div>':'';
                      const timeText=adv.showTimes?'<div class="time"><strong>'+esc(c.start)+'</strong><br>'+esc(c.end)+'</div>':'<div class="time"></div>';
                      row.innerHTML=timeText+'<div><div class="label">'+esc(c.label)+(c.uncertain?'<span class="ocrFlag">⚠ '+T().uncertain+'</span>':'')+'</div>'+roomText+'</div>'+(cur?'<div class="badge">'+(uiLang()==='de'?'Läuft':(uiLang()==='en'?'In class':'En cours'))+'</div>':'');
                      const base=(weeks[currentWeek]&&weeks[currentWeek][todayDay()])?weeks[currentWeek][todayDay()].courses:[];let idx=base.findIndex(x=>x.start===c.start&&x.label===c.label);if(idx<0)idx=base.findIndex(x=>x.label===c.label);if(idx>=0){const captured=idx;row.onclick=()=>{activeWeek=currentWeek;selected=todayDay();editing=captured;openEditor(captured)}}
                    }else if(ev.type==='gap'){
                      row.className='todayCourse gap';row.innerHTML='<div class="time"><strong>'+clock(ev.start)+'</strong><br>'+clock(ev.end)+'</div><div><div class="label">'+esc(breaks.gapLabel)+'</div><div class="room">'+durationLabel(ev.end-ev.start)+'</div></div>'+(breaks.showGapBadge?'<div class="badge gap">'+T().gaps+'</div>':'');
                    }else{
                      row.className='todayCourse lunch';row.innerHTML='<div class="time"><strong>'+esc(ev.l.start)+'</strong><br>'+esc(ev.l.end)+'</div><div><div class="label">'+esc(breaks.lunchLabel)+'</div><div class="room">'+(uiLang()==='de'?'Weiter um ':(uiLang()==='en'?'Back at ':'Reprise à '))+esc(ev.l.end)+'</div></div>'+(breaks.showLunchBadge?'<div class="badge lunch">'+T().lunch+'</div>':'');
                    }
                    box.appendChild(row);
                  }
                  const todays=adv.exceptions.filter(e=>e.date===dateKey(new Date()));if(todays.length){let b=document.getElementById('exceptionTodayBanner');if(!b){b=document.createElement('div');b.id='exceptionTodayBanner';b.className='exceptionBanner';box.parentNode.insertBefore(b,box)}b.textContent=todays.length+' '+(uiLang()==='de'?'Änderung(en) heute':(uiLang()==='en'?'change(s) today':'modification(s) exceptionnelle(s) aujourd’hui'))}else{const b=document.getElementById('exceptionTodayBanner');if(b)b.remove()}
                  applyAppAppearance();
                }

                function decorateUncertain(){
                  if(typeof state==='undefined'||typeof selected==='undefined')return;
                  const list=state[selected]&&state[selected].courses?state[selected].courses:[];document.querySelectorAll('.editCourse').forEach((row,i)=>{const c=list[i];row.classList.toggle('ocrUncertain',!!(c&&c.uncertain));if(c&&c.uncertain&&!row.querySelector('.ocrFlag')){const label=row.querySelector('.label');if(label){const f=document.createElement('span');f.className='ocrFlag';f.textContent='⚠ '+T().uncertain;label.appendChild(f)}}});
                }

                if(typeof window.parseOcrSchedule==='function'&&!window.parseOcrSchedule.__advanced){
                  const oldParse=window.parseOcrSchedule;const wrapped=function(payload){const r=oldParse(payload);if(r&&r.parsed){for(const d of [2,3,4,5,6])for(const c of r.parsed[d]||[]){const lab=String(c.label||'');c.uncertain=!c.room||lab.length<3||lab.length>55||lab.indexOf('�')>=0||lab.indexOf('?')>=0}}return r};wrapped.__advanced=true;window.parseOcrSchedule=wrapped;
                }

                function after(name,fn){const old=window[name];if(typeof old!=='function'||old.__advancedAfter)return;const wrapped=function(){const r=old.apply(this,arguments);try{fn()}catch(e){}return r};wrapped.__advancedAfter=true;window[name]=wrapped}
                after('renderToday',()=>{renderEffectiveToday();applyAppAppearance()});
                after('renderWeek',applyAppAppearance);
                after('renderEdit',()=>{decorateUncertain();applyAppAppearance()});
                after('renderContext',buildWeekTabs);

                const sheet=document.getElementById('settingsSheet');
                const actions=document.querySelector('#settingsSheet .settingsActions');
                if(sheet&&actions&&!document.getElementById('advancedSettingsRoot')){
                  const root=document.createElement('div');root.id='advancedSettingsRoot';
                  root.innerHTML=`
                    <div class="settingBox"><div id="advWidgetTitle" class="settingTitle"></div>
                      <div class="advRow"><span id="advDensityLabel"></span><select id="advDensity"><option value="compact"></option><option value="normal"></option><option value="comfortable"></option></select></div>
                      <div class="advRow"><span id="advFormatLabel"></span><select id="advFormat"><option value="timeline"></option><option value="compact"></option></select></div>
                      <div class="advRow"><span id="advFollowingLabel"></span><select id="advFollowing"><option value="0"></option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select></div>
                      <label class="advCheck"><input id="advShowRoom" type="checkbox"><span id="advShowRoomLabel"></span></label><label class="advCheck"><input id="advShowTimes" type="checkbox"><span id="advShowTimesLabel"></span></label><label class="advCheck"><input id="advShowRemaining" type="checkbox"><span id="advShowRemainingLabel"></span></label><label class="advCheck"><input id="advShowPercent" type="checkbox"><span id="advShowPercentLabel"></span></label><label class="advCheck"><input id="advShowProgress" type="checkbox"><span id="advShowProgressLabel"></span></label><label class="advCheck"><input id="advShowBreaks" type="checkbox"><span id="advShowBreaksLabel"></span></label><label class="advCheck"><input id="advShowLunch" type="checkbox"><span id="advShowLunchLabel"></span></label><label class="advCheck"><input id="advShowWeekInfo" type="checkbox"><span id="advShowWeekInfoLabel"></span></label><label class="advCheck"><input id="advClassColors" type="checkbox"><span id="advClassColorsLabel"></span></label>
                      <div class="advRow"><span id="advAccessLabel"></span><select id="advAccess"><option value="normal"></option><option value="high_contrast"></option><option value="colorblind"></option></select></div>
                    </div>
                    <div class="settingBox"><div id="advCycleTitle" class="settingTitle"></div><div class="advRow"><span id="advCycleLabel"></span><select id="advCycle"><option value="2"></option><option value="3"></option><option value="4"></option></select></div><div class="advButtons"><button id="advCopyWeek" type="button" class="advButton"></button></div><div class="advSectionTitle" id="advCopyDayTitle"></div><div class="advInline"><select id="advCopyFrom"></select><select id="advCopyTo"></select></div><div class="advButtons" style="margin-top:6px"><button id="advCopyDay" type="button" class="advButton"></button></div></div>
                    <div class="settingBox"><div id="advReminderTitle" class="settingTitle"></div><label class="advCheck"><input id="advReminders" type="checkbox"><span id="advReminderLabel"></span></label><div class="advRow"><span></span><select id="advReminderMinutes"><option value="0">0 min</option><option value="5">5 min</option><option value="10">10 min</option><option value="15">15 min</option><option value="20">20 min</option><option value="30">30 min</option></select></div></div>
                    <div class="settingBox"><div id="advCalendarTitle" class="settingTitle"></div><div class="advRow"><span id="advHolidayLabel"></span><select id="advHoliday"><option value="off"></option><option value="france"></option><option value="alsace_moselle"></option></select></div><div class="advSectionTitle" id="advRangeTitle"></div><div class="advInline"><input id="advRangeStart" type="date"><input id="advRangeEnd" type="date"></div><div class="advInline full"><input id="advRangeLabel" type="text" maxlength="45"></div><div class="advButtons" style="margin-top:6px"><button id="advAddRange" type="button" class="advButton"></button></div><div id="advRangeList" class="advList"></div></div>
                    <div class="settingBox"><div id="advExceptionsTitle" class="settingTitle"></div><div class="advButtons"><button id="advAddException" type="button" class="advButton primary"></button></div><div id="advExceptionList" class="advList"></div></div>
                    <div class="settingBox"><div id="advProfilesTitle" class="settingTitle"></div><div class="advRow"><select id="advProfileSelect" style="width:100%"></select></div><div class="advButtons"><button id="advNewProfile" type="button" class="advButton"></button><button id="advRenameProfile" type="button" class="advButton"></button><button id="advDeleteProfile" type="button" class="advButton danger"></button></div></div>
                    <div class="settingBox"><div id="advBackupTitle" class="settingTitle"></div><div class="advButtons"><button id="advShareBackup" type="button" class="advButton"></button><button id="advRestoreBackup" type="button" class="advButton"></button></div></div>`;
                  sheet.insertBefore(root,actions);
                }

                const advModal=document.createElement('div');advModal.id='advModal';advModal.className='modal';
                advModal.innerHTML=`<form id="advExceptionForm" class="sheet"><h3 id="advExceptionFormTitle"></h3><div class="advField"><label id="advFDateLabel"></label><input id="advFDate" type="date" required></div><div class="advField"><label>Type</label><select id="advFType"><option value="cancel"></option><option value="room"></option><option value="move"></option><option value="extra"></option></select></div><div id="advReferenceFields"><div class="advField"><label id="advFRefStartLabel"></label><input id="advFRefStart" type="time"></div><div class="advField"><label id="advFRefLabelLabel"></label><input id="advFRefLabel" type="text" maxlength="80"></div></div><div id="advNewFields"><div class="advField"><label id="advFStartLabel"></label><input id="advFStart" type="time"></div><div class="advField"><label id="advFEndLabel"></label><input id="advFEnd" type="time"></div><div class="advField"><label id="advFLabelLabel"></label><input id="advFLabel" type="text" maxlength="80"></div><div class="advField"><label id="advFRoomLabel"></label><input id="advFRoom" type="text" maxlength="20"></div></div><div class="advModalActions"><button id="advFCancel" type="button" class="btn"></button><button type="submit" class="btn primary" id="advFSave"></button></div></form>`;
                document.body.appendChild(advModal);

                function setTxt(id,v){const e=document.getElementById(id);if(e)e.textContent=v}
                function locAdv(){
                  const t=T();setTxt('advWidgetTitle',t.widgetDisplay);setTxt('advDensityLabel',t.density);setTxt('advFormatLabel',t.format);setTxt('advFollowingLabel',t.following);setTxt('advShowRoomLabel',t.room);setTxt('advShowTimesLabel',t.times);setTxt('advShowRemainingLabel',t.remaining);setTxt('advShowPercentLabel',t.percent);setTxt('advShowProgressLabel',t.progress);setTxt('advShowBreaksLabel',t.gaps);setTxt('advShowLunchLabel',t.lunch);setTxt('advShowWeekInfoLabel',t.weekInfo);setTxt('advClassColorsLabel',t.classColors);setTxt('advAccessLabel',t.access);setTxt('advCycleTitle',t.cycle);setTxt('advCycleLabel',t.cycle);setTxt('advCopyWeek',t.copyWeek);setTxt('advCopyDayTitle',t.copyDay);setTxt('advCopyDay',t.copy);setTxt('advReminderTitle',t.reminders);setTxt('advReminderLabel',t.notify);setTxt('advCalendarTitle',t.calendar);setTxt('advHolidayLabel',t.holidays);setTxt('advRangeTitle',t.addRange);setTxt('advAddRange',t.add);setTxt('advExceptionsTitle',t.exceptions);setTxt('advAddException',t.addException);setTxt('advProfilesTitle',t.profiles);setTxt('advNewProfile',t.newProfile);setTxt('advRenameProfile',t.rename);setTxt('advDeleteProfile',t.delete);setTxt('advBackupTitle',t.backup);setTxt('advShareBackup',t.share);setTxt('advRestoreBackup',t.restore);setTxt('advExceptionFormTitle',t.addException);setTxt('advFDateLabel',t.date);setTxt('advFRefStartLabel',t.referenceStart);setTxt('advFRefLabelLabel',t.referenceLabel);setTxt('advFStartLabel',t.newStart);setTxt('advFEndLabel',t.newEnd);setTxt('advFLabelLabel',t.newLabel);setTxt('advFRoomLabel',t.newRoom);setTxt('advFCancel',uiLang()==='de'?'Abbrechen':(uiLang()==='en'?'Cancel':'Annuler'));setTxt('advFSave',t.save);
                  const den=document.getElementById('advDensity');if(den){den.options[0].text=t.compact;den.options[1].text=t.normal;den.options[2].text=t.comfortable}const fmt=document.getElementById('advFormat');if(fmt){fmt.options[0].text=t.timeline;fmt.options[1].text=t.nowNext}const fol=document.getElementById('advFollowing');if(fol)fol.options[0].text=t.auto;const ac=document.getElementById('advAccess');if(ac){ac.options[0].text=t.accessNormal;ac.options[1].text=t.contrast;ac.options[2].text=t.colorblind}const cyc=document.getElementById('advCycle');if(cyc){cyc.options[0].text=t.cycle2;cyc.options[1].text=t.cycle3;cyc.options[2].text=t.cycle4}const hol=document.getElementById('advHoliday');if(hol){hol.options[0].text=t.holOff;hol.options[1].text=t.holFrance;hol.options[2].text=t.holAlsace}const typ=document.getElementById('advFType');if(typ){typ.options[0].text=t.cancelCourse;typ.options[1].text=t.roomChange;typ.options[2].text=t.moveCourse;typ.options[3].text=t.extraCourse}
                  const rl=document.getElementById('advRangeLabel');if(rl)rl.placeholder=t.label;
                  renderCopyDayOptions();
                }

                function syncControls(){
                  const vals={advDensity:adv.density,advFormat:adv.widgetFormat,advFollowing:String(adv.upcomingCount||0),advAccess:adv.accessibility,advCycle:String(adv.cycleLength),advReminderMinutes:String(adv.reminderMinutes),advHoliday:adv.holidayMode};for(const id in vals){const e=document.getElementById(id);if(e)e.value=vals[id]}
                  const checks={advShowRoom:'showRoom',advShowTimes:'showTimes',advShowRemaining:'showRemaining',advShowPercent:'showPercent',advShowProgress:'showProgress',advShowBreaks:'showBreaks',advShowLunch:'showLunch',advShowWeekInfo:'showWeekInfo',advClassColors:'colorByClass',advReminders:'remindersEnabled'};for(const id in checks){const e=document.getElementById(id);if(e)e.checked=adv[checks[id]]!==false}
                }

                function renderCopyDayOptions(){const a=document.getElementById('advCopyFrom'),b=document.getElementById('advCopyTo');if(!a||!b)return;const names=uiLang()==='de'?['Montag','Dienstag','Mittwoch','Donnerstag','Freitag']:(uiLang()==='en'?['Monday','Tuesday','Wednesday','Thursday','Friday']:['Lundi','Mardi','Mercredi','Jeudi','Vendredi']);const oldA=a.value||String(typeof selected==='number'?selected:2),oldB=b.value||'3';a.innerHTML='';b.innerHTML='';[2,3,4,5,6].forEach((d,i)=>{for(const el of [a,b]){const o=document.createElement('option');o.value=String(d);o.textContent=names[i];el.appendChild(o)}});a.value=oldA;b.value=oldB}

                function renderRanges(){const box=document.getElementById('advRangeList');if(!box)return;box.innerHTML='';adv.dayOffRanges.forEach((r,i)=>{const row=document.createElement('div');row.className='advItem';row.innerHTML='<div class="advItemText"><b>'+esc(r.label||T().dayOff)+'</b><br><span class="advSmall">'+esc(r.start||'')+' → '+esc(r.end||r.start||'')+'</span></div><button type="button" class="advButton danger">×</button>';row.querySelector('button').onclick=()=>{adv.dayOffRanges.splice(i,1);saveAdv();renderRanges();if(typeof render==='function')render()};box.appendChild(row)});}
                function exceptionTypeName(type){const t=T();if(type==='cancel')return t.cancelCourse;if(type==='room')return t.roomChange;if(type==='move')return t.moveCourse;return t.extraCourse}
                function renderExceptions(){const box=document.getElementById('advExceptionList');if(!box)return;box.innerHTML='';if(!adv.exceptions.length){box.innerHTML='<div class="advSmall" style="padding:7px 0;text-align:center;width:100%">'+T().noException+'</div>';return}adv.exceptions.slice().sort((a,b)=>String(a.date).localeCompare(String(b.date))).forEach(e=>{const realIndex=adv.exceptions.indexOf(e);const row=document.createElement('div');row.className='advItem';const detail=e.type==='extra'?(e.start+' · '+(e.label||'')):((e.refStart||'')+' · '+(e.refLabel||''));row.innerHTML='<div class="advItemText"><b>'+esc(e.date||'')+' · '+esc(exceptionTypeName(e.type))+'</b><br><span class="advSmall">'+esc(detail)+'</span></div><button type="button" class="advButton danger">×</button>';row.querySelector('button').onclick=()=>{adv.exceptions.splice(realIndex,1);saveAdv();renderExceptions();if(typeof render==='function')render()};box.appendChild(row)});}

                function openExceptionForm(){const now=new Date();document.getElementById('advFDate').value=dateKey(now);document.getElementById('advFType').value='cancel';document.getElementById('advFRefStart').value='';document.getElementById('advFRefLabel').value='';document.getElementById('advFStart').value='';document.getElementById('advFEnd').value='';document.getElementById('advFLabel').value='';document.getElementById('advFRoom').value='';updateExceptionFields();advModal.classList.add('show')}
                function updateExceptionFields(){const type=document.getElementById('advFType').value;document.getElementById('advReferenceFields').style.display=type==='extra'?'none':'block';document.getElementById('advNewFields').style.display=(type==='cancel'?'none':'block');const lab=document.getElementById('advFStartLabel');if(lab)lab.textContent=type==='room'?T().referenceStart:T().newStart}
                document.getElementById('advFType').onchange=updateExceptionFields;document.getElementById('advFCancel').onclick=()=>advModal.classList.remove('show');advModal.onclick=e=>{if(e.target===advModal)advModal.classList.remove('show')};
                document.getElementById('advExceptionForm').onsubmit=e=>{e.preventDefault();const type=document.getElementById('advFType').value;const obj={date:document.getElementById('advFDate').value,type:type,refStart:document.getElementById('advFRefStart').value,refLabel:document.getElementById('advFRefLabel').value.trim(),start:document.getElementById('advFStart').value,end:document.getElementById('advFEnd').value,label:document.getElementById('advFLabel').value.trim(),room:document.getElementById('advFRoom').value.trim()};if(type==='room'){obj.room=document.getElementById('advFRoom').value.trim()}adv.exceptions.push(obj);saveAdv();advModal.classList.remove('show');renderExceptions();if(typeof render==='function')render()};

                function loadProfiles(){
                  const sel=document.getElementById('advProfileSelect');if(!sel)return;try{const root=JSON.parse(window.AndroidSchedule&&AndroidSchedule.listProfiles?AndroidSchedule.listProfiles():'{}');sel.innerHTML='';for(const p of root.profiles||[]){const o=document.createElement('option');o.value=p.id;o.textContent=p.name;sel.appendChild(o)}sel.value=root.current||''}catch(e){}
                }
                function activateProfile(id){if(!(window.AndroidSchedule&&AndroidSchedule.activateProfile))return;AndroidSchedule.activateProfile(id);if(window.reloadSchedule)window.reloadSchedule();loadProfiles();setTimeout(refreshAdvancedFeatures,18)}

                function bind(){
                  const selectMap={advDensity:'density',advFormat:'widgetFormat',advAccess:'accessibility',advHoliday:'holidayMode'};for(const id in selectMap){const el=document.getElementById(id);if(el)el.onchange=e=>{adv[selectMap[id]]=e.target.value;saveAdv();if(typeof render==='function')render()}}
                  const numeric={advFollowing:'upcomingCount',advCycle:'cycleLength',advReminderMinutes:'reminderMinutes'};for(const id in numeric){const el=document.getElementById(id);if(el)el.onchange=e=>{adv[numeric[id]]=Number(e.target.value);saveAdv();if(id==='advCycle'){ensureExtraWeeks();if(letters().indexOf(currentWeek)<0)currentWeek='A';if(letters().indexOf(activeWeek)<0)activeWeek=currentWeek;buildWeekTabs();if(typeof save==='function')save()}if(typeof render==='function')render()}}
                  const checks={advShowRoom:'showRoom',advShowTimes:'showTimes',advShowRemaining:'showRemaining',advShowPercent:'showPercent',advShowProgress:'showProgress',advShowBreaks:'showBreaks',advShowLunch:'showLunch',advShowWeekInfo:'showWeekInfo',advClassColors:'colorByClass',advReminders:'remindersEnabled'};for(const id in checks){const el=document.getElementById(id);if(el)el.onchange=e=>{adv[checks[id]]=e.target.checked;saveAdv();if(typeof render==='function')render()}}
                  const cw=document.getElementById('advCopyWeek');if(cw)cw.onclick=()=>{ensureExtraWeeks();const ls=letters(),i=ls.indexOf(activeWeek),target=ls[(i+1)%ls.length];if(target===activeWeek)return;weeks[target]=deep(weeks[activeWeek]);if(typeof save==='function')save();buildWeekTabs()};
                  const cd=document.getElementById('advCopyDay');if(cd)cd.onclick=()=>{const from=Number(document.getElementById('advCopyFrom').value),to=Number(document.getElementById('advCopyTo').value);if(from===to)return;weeks[activeWeek][to]=deep(weeks[activeWeek][from]);if(typeof save==='function')save();if(typeof render==='function')render()};
                  const ar=document.getElementById('advAddRange');if(ar)ar.onclick=()=>{const s=document.getElementById('advRangeStart').value,e=document.getElementById('advRangeEnd').value||s,l=document.getElementById('advRangeLabel').value.trim();if(!s)return;adv.dayOffRanges.push({start:s,end:e,label:l});saveAdv();renderRanges()};
                  const ae=document.getElementById('advAddException');if(ae)ae.onclick=openExceptionForm;
                  const ps=document.getElementById('advProfileSelect');if(ps)ps.onchange=e=>activateProfile(e.target.value);
                  const np=document.getElementById('advNewProfile');if(np)np.onclick=()=>{if(!(window.AndroidSchedule&&AndroidSchedule.createProfile))return;const name=prompt(T().newProfile);if(!name)return;const dup=confirm(T().duplicateQuestion);const id=AndroidSchedule.createProfile(name,dup);loadProfiles();activateProfile(id)};
                  const rp=document.getElementById('advRenameProfile');if(rp)rp.onclick=()=>{const sel=document.getElementById('advProfileSelect');if(!sel||!sel.value)return;const name=prompt(T().rename,sel.options[sel.selectedIndex]?sel.options[sel.selectedIndex].text:'');if(name&&window.AndroidSchedule&&AndroidSchedule.renameProfile){AndroidSchedule.renameProfile(sel.value,name);loadProfiles()}};
                  const dp=document.getElementById('advDeleteProfile');if(dp)dp.onclick=()=>{const sel=document.getElementById('advProfileSelect');if(!sel||!sel.value||!(window.AndroidSchedule&&AndroidSchedule.deleteProfile))return;if(confirm(T().delete+' ?')){AndroidSchedule.deleteProfile(sel.value);loadProfiles();if(window.reloadSchedule)window.reloadSchedule()}};
                  const sb=document.getElementById('advShareBackup');if(sb)sb.onclick=()=>{if(window.AndroidSchedule&&AndroidSchedule.shareBackup)AndroidSchedule.shareBackup()};const rb=document.getElementById('advRestoreBackup');if(rb)rb.onclick=()=>{if(window.AndroidSchedule&&AndroidSchedule.pickBackup)AndroidSchedule.pickBackup()};
                }
                bind();

                window.applyBackupImported=ok=>{alert(ok?T().importOk:T().importFail);if(ok&&window.reloadSchedule)window.reloadSchedule()};

                function refreshAdvancedFeatures(){
                  loadAdv();restoreCycleFromNative();locAdv();syncControls();renderRanges();renderExceptions();loadProfiles();buildWeekTabs();decorateUncertain();renderEffectiveToday();applyAppAppearance();
                }
                window.refreshAdvancedFeatures=refreshAdvancedFeatures;

                const oldSettingsClick=document.getElementById('settingsBtn')?document.getElementById('settingsBtn').onclick:null;
                if(document.getElementById('settingsBtn'))document.getElementById('settingsBtn').onclick=function(e){if(oldSettingsClick)oldSettingsClick.call(this,e);setTimeout(refreshAdvancedFeatures,18)};

                refreshAdvancedFeatures();
              } catch(e) { console.log('Advanced features',e); }
            })();
            """;
    }

    // Former UiPolishAndSchoolCalendarUi; isolated to stay below JVM constant limits.
    private static String layer4() {
        return """
            (function(){
              try {
                if(window.__uiPolishSchoolV1){
                  if(window.refreshUiPolishSchool)window.refreshUiPolishSchool();
                  return;
                }
                window.__uiPolishSchoolV1=true;

                const EXTRA_THEMES={
                  rose:{fr:'Rose',en:'Pink',de:'Rosa',a:'#D81B60',d:'#AD1457',s:'#FCE4EC',bg:'#FFF7FA',ink:'#341723',m:'#75616A',line:'#E9D9DF'},
                  red:{fr:'Rouge',en:'Red',de:'Rot',a:'#D84343',d:'#B72E2E',s:'#FDE8E8',bg:'#FFF8F8',ink:'#351919',m:'#776060',line:'#EADADA'},
                  indigo:{fr:'Indigo',en:'Indigo',de:'Indigo',a:'#3F51B5',d:'#303F9F',s:'#E8EAF6',bg:'#F8F8FD',ink:'#1D2342',m:'#666A7D',line:'#DADCEB'},
                  cyan:{fr:'Cyan',en:'Cyan',de:'Cyan',a:'#0097A7',d:'#007C91',s:'#E0F7FA',bg:'#F5FCFD',ink:'#123036',m:'#60757A',line:'#D5E8EB'},
                  coral:{fr:'Corail',en:'Coral',de:'Koralle',a:'#E76F51',d:'#C95035',s:'#FCE9E3',bg:'#FFF8F5',ink:'#3A211B',m:'#79675F',line:'#ECDDD7'},
                  navy:{fr:'Bleu nuit',en:'Navy',de:'Dunkelblau',a:'#2457A7',d:'#193E7A',s:'#E5EDFA',bg:'#F7F9FD',ink:'#17243B',m:'#617086',line:'#D8E0EC'},
                  graphite:{fr:'Graphite',en:'Graphite',de:'Graphit',a:'#546E7A',d:'#37474F',s:'#ECEFF1',bg:'#F7F8F9',ink:'#1D292E',m:'#68757B',line:'#DCE1E3'}
                };

                const SCHOOL={
                  '2025-2026':{
                    common:[['2025-10-18','2025-11-02','Toussaint'],['2025-12-20','2026-01-04','Noël'],['2026-07-04','2026-08-31','Été'],['2026-05-15','2026-05-15','Pont de l’Ascension']],
                    A:[['2026-02-07','2026-02-22','Hiver'],['2026-04-04','2026-04-19','Printemps']],
                    B:[['2026-02-14','2026-03-01','Hiver'],['2026-04-11','2026-04-26','Printemps']],
                    C:[['2026-02-21','2026-03-08','Hiver'],['2026-04-18','2026-05-03','Printemps']]
                  },
                  '2026-2027':{
                    common:[['2026-10-17','2026-11-01','Toussaint'],['2026-12-19','2027-01-03','Noël'],['2027-07-03','2027-08-31','Été'],['2027-05-07','2027-05-07','Pont de l’Ascension']],
                    A:[['2027-02-13','2027-02-28','Hiver'],['2027-04-10','2027-04-25','Printemps']],
                    B:[['2027-02-20','2027-03-07','Hiver'],['2027-04-17','2027-05-02','Printemps']],
                    C:[['2027-02-06','2027-02-21','Hiver'],['2027-04-03','2027-04-18','Printemps']]
                  },
                  '2027-2028':{
                    common:[['2027-10-23','2027-11-07','Toussaint'],['2027-12-18','2028-01-02','Noël'],['2028-07-04','2028-08-31','Été'],['2028-05-26','2028-05-26','Pont de l’Ascension']],
                    A:[['2028-02-19','2028-03-05','Hiver'],['2028-04-22','2028-05-08','Printemps']],
                    B:[['2028-02-05','2028-02-20','Hiver'],['2028-04-08','2028-04-23','Printemps']],
                    C:[['2028-02-12','2028-02-27','Hiver'],['2028-04-15','2028-05-01','Printemps']]
                  }
                };

                function ui(){
                  try{return JSON.parse(window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():'{}')}catch(e){return {}}
                }
                function lang(){const l=ui().language;return l==='en'||l==='de'?l:'fr'}
                function tx(fr,en,de){return lang()==='en'?en:(lang()==='de'?de:fr)}
                function advanced(){
                  try{return JSON.parse(window.AndroidSchedule&&AndroidSchedule.loadAdvancedSettings?AndroidSchedule.loadAdvancedSettings():localStorage.getItem('edt-advanced')||'{}')}catch(e){return {}}
                }
                function saveAdvanced(o){
                  const raw=JSON.stringify(o);
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveAdvancedSettings)AndroidSchedule.saveAdvancedSettings(raw);else localStorage.setItem('edt-advanced',raw)}catch(e){}
                }
                function saveUi(o){
                  const raw=JSON.stringify(o);
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveUiSettings)AndroidSchedule.saveUiSettings(raw);else localStorage.setItem('edt-ui-settings',raw)}catch(e){}
                }

                const style=document.createElement('style');
                style.textContent=`
                  .header h1{font-size:1.16rem!important;letter-spacing:-.01em}
                  .weekTabs{min-width:0}.weekTab{min-width:0}
                  body.largeAppText .contextBar{height:auto!important;min-height:39px;flex-wrap:wrap;align-content:center;padding-top:5px;padding-bottom:5px}
                  body.largeAppText .currentWeek{flex:1 1 100%;max-width:100%;text-align:center}
                  body.largeAppText .weekTabs{display:flex!important;flex:1 1 100%;width:100%;min-width:0;gap:4px}
                  body.largeAppText .weekTab{flex:1 1 0;min-width:0!important;padding:5px 3px!important;white-space:normal;line-height:1.05}
                  body.threeWeekCycle:not(.largeAppText) .weekTabs{flex:1;min-width:0}
                  body.threeWeekCycle:not(.largeAppText) .weekTab{padding-left:5px;padding-right:5px}
                  #schoolCalendarBlock{margin-top:9px;padding-top:8px;border-top:1px solid #edf0f4}
                  #schoolCalendarBlock .schoolTitle{font-size:.72rem;font-weight:850;color:var(--set-dark);margin-bottom:6px}
                  #schoolCalendarBlock .schoolEnable{display:flex;align-items:center;gap:7px;font-size:.76rem;margin-bottom:7px}
                  #schoolCalendarBlock .schoolEnable input{width:17px;height:17px;accent-color:var(--set-accent)}
                  #schoolCalendarBlock .schoolGrid{display:grid;grid-template-columns:1fr 1fr;gap:6px}
                  #schoolCalendarBlock select{width:100%;min-width:0;border:1px solid #d8e0e8;border-radius:7px;padding:7px;background:#fff;color:inherit}
                  #schoolCalendarBlock .schoolHint{font-size:.66rem;color:#68738a;margin-top:6px;line-height:1.25}
                  .themeButton.extraTheme .themeName{font-size:.56rem}
                `;
                document.head.appendChild(style);

                function applyResponsive(){
                  const o=ui();const scale=Number(o.appFontScale)||1;
                  document.body.classList.toggle('largeAppText',scale>1.12);
                  const a=advanced();document.body.classList.toggle('threeWeekCycle',Number(a.cycleLength)===3);
                }

                function applyExtraTheme(id){
                  const th=EXTRA_THEMES[id];if(!th)return;
                  const vars={'--set-accent':th.a,'--set-dark':th.d,'--set-soft':th.s,'--set-ink':th.ink,'--set-muted':th.m,'--blue':th.a,'--blue2':th.d,'--teal':th.a,'--ink':th.ink,'--muted':th.m,'--line':th.line,'--soft':th.s,'--bg':th.bg};
                  Object.keys(vars).forEach(k=>document.documentElement.style.setProperty(k,vars[k]));document.body.style.background=th.bg;
                  document.querySelectorAll('.themeButton').forEach(b=>b.classList.toggle('active',b.dataset.theme===id));
                }

                let stickyTheme=EXTRA_THEMES[ui().theme]?ui().theme:null;
                let themeBusy=false;
                function restoreStickyTheme(){
                  if(!stickyTheme)return;setTimeout(()=>{const o=ui();if(o.theme!==stickyTheme){o.theme=stickyTheme;saveUi(o)}applyExtraTheme(stickyTheme);renderExtraThemes()},15);
                }
                function renderExtraThemes(){
                  if(themeBusy)return;const grid=document.getElementById('themeGrid');if(!grid)return;themeBusy=true;
                  const nativeCurrent=ui().theme||'blue';const current=stickyTheme||nativeCurrent;
                  Object.keys(EXTRA_THEMES).forEach(id=>{
                    let b=grid.querySelector('[data-theme="'+id+'"]');const th=EXTRA_THEMES[id];
                    if(!b){b=document.createElement('button');b.type='button';b.className='themeButton extraTheme';b.dataset.theme=id;b.innerHTML='<span class="themeSwatch" style="background:linear-gradient(135deg,'+th.a+','+th.s+')"></span><span class="themeName"></span>';grid.appendChild(b)}
                    const n=b.querySelector('.themeName');if(n)n.textContent=th[lang()]||th.fr;
                    b.classList.toggle('active',current===id);
                    b.onclick=()=>{stickyTheme=id;const o=ui();o.theme=id;saveUi(o);applyExtraTheme(id);setTimeout(refresh,20)};
                  });
                  if(EXTRA_THEMES[current])applyExtraTheme(current);
                  themeBusy=false;
                }

                function defaultSchoolYear(){
                  const d=new Date(),y=d.getFullYear(),m=d.getMonth()+1;return (m>=8?y:y-1)+'-'+(m>=8?y+1:y);
                }
                function vacationName(name){
                  const names={
                    'Toussaint':{en:'Autumn break',de:'Herbstferien'},'Noël':{en:'Christmas break',de:'Weihnachtsferien'},'Hiver':{en:'Winter break',de:'Winterferien'},'Printemps':{en:'Spring break',de:'Frühlingsferien'},'Été':{en:'Summer break',de:'Sommerferien'},'Pont de l’Ascension':{en:'Ascension break',de:'Christi-Himmelfahrt-Brückentag'}
                  };const n=names[name];return n?(lang()==='en'?n.en:(lang()==='de'?n.de:name)):name;
                }
                function schoolRanges(year,zone){
                  const data=SCHOOL[year];if(!data)return [];
                  return (data.common||[]).concat(data[zone]||[]).map(x=>({start:x[0],end:x[1],label:tx('Vacances scolaires · ','School holidays · ','Schulferien · ')+vacationName(x[2]),source:'schoolCalendar'}));
                }
                function applySchoolCalendar(){
                  const a=advanced();const enabled=document.getElementById('schoolEnabled')?document.getElementById('schoolEnabled').checked:false;
                  const year=document.getElementById('schoolYear')?document.getElementById('schoolYear').value:defaultSchoolYear();
                  const zone=document.getElementById('schoolZone')?document.getElementById('schoolZone').value:'B';
                  a.schoolVacationEnabled=enabled;a.schoolVacationYear=year;a.schoolVacationZone=zone;
                  if(!Array.isArray(a.dayOffRanges))a.dayOffRanges=[];
                  a.dayOffRanges=a.dayOffRanges.filter(r=>r&&r.source!=='schoolCalendar');
                  if(enabled)a.dayOffRanges=a.dayOffRanges.concat(schoolRanges(year,zone));
                  saveAdvanced(a);applyResponsive();
                  if(window.refreshAdvancedFeatures)setTimeout(window.refreshAdvancedFeatures,20);
                  if(typeof render==='function')setTimeout(render,30);
                  syncSchoolControls();
                }
                function syncSchoolControls(){
                  const a=advanced(),e=document.getElementById('schoolEnabled'),y=document.getElementById('schoolYear'),z=document.getElementById('schoolZone'),h=document.getElementById('schoolHint');
                  if(e)e.checked=a.schoolVacationEnabled===true;if(y)y.value=SCHOOL[a.schoolVacationYear]?a.schoolVacationYear:(SCHOOL[defaultSchoolYear()]?defaultSchoolYear():'2026-2027');if(z)z.value=['A','B','C'].includes(a.schoolVacationZone)?a.schoolVacationZone:'B';
                  if(h){const yy=y?y.value:'',zz=z?z.value:'B',count=schoolRanges(yy,zz).length;h.textContent=(e&&e.checked)?tx(count+' périodes intégrées automatiquement pour la zone '+zz+'.',''+count+' periods automatically included for zone '+zz+'.',count+' Zeiträume für Zone '+zz+' automatisch übernommen.'):tx('Active cette option pour que l’application et le widget ignorent automatiquement les vacances scolaires.','Enable this so the app and widget automatically skip school holidays.','Aktivieren, damit App und Widget Schulferien automatisch überspringen.')}
                }
                function ensureSchoolControls(){
                  if(document.getElementById('schoolCalendarBlock')){syncSchoolControls();return}
                  const title=document.getElementById('advCalendarTitle');const box=title?title.closest('.settingBox'):null;if(!box)return;
                  const block=document.createElement('div');block.id='schoolCalendarBlock';
                  block.innerHTML='<div id="schoolTitle" class="schoolTitle"></div><label class="schoolEnable"><input id="schoolEnabled" type="checkbox"><span id="schoolEnableLabel"></span></label><div class="schoolGrid"><select id="schoolYear"><option value="2025-2026">2025–2026</option><option value="2026-2027">2026–2027</option><option value="2027-2028">2027–2028</option></select><select id="schoolZone"><option value="A">Zone A</option><option value="B">Zone B</option><option value="C">Zone C</option></select></div><div id="schoolHint" class="schoolHint"></div>';
                  box.insertBefore(block,box.querySelector('#advRangeTitle'));
                  document.getElementById('schoolEnabled').onchange=applySchoolCalendar;document.getElementById('schoolYear').onchange=applySchoolCalendar;document.getElementById('schoolZone').onchange=applySchoolCalendar;
                  syncSchoolControls();
                }
                function localizeSchool(){
                  const t=document.getElementById('schoolTitle'),e=document.getElementById('schoolEnableLabel');if(t)t.textContent=tx('Vacances scolaires','School holidays','Schulferien');if(e)e.textContent=tx('Intégrer automatiquement les vacances','Automatically include school holidays','Schulferien automatisch übernehmen');
                  syncSchoolControls();
                }

                function refresh(){applyResponsive();ensureSchoolControls();localizeSchool();renderExtraThemes()}
                window.refreshUiPolishSchool=refresh;

                const oldRefresh=window.refreshSettingsV3;
                if(typeof oldRefresh==='function'&&!oldRefresh.__polishWrapped){const w=function(){const r=oldRefresh.apply(this,arguments);setTimeout(refresh,0);return r};w.__polishWrapped=true;window.refreshSettingsV3=w}
                const oldAdv=window.refreshAdvancedFeatures;
                if(typeof oldAdv==='function'&&!oldAdv.__polishWrapped){const w=function(){const r=oldAdv.apply(this,arguments);setTimeout(refresh,0);return r};w.__polishWrapped=true;window.refreshAdvancedFeatures=w}

                const settings=document.getElementById('settingsBtn');if(settings)settings.addEventListener('click',()=>setTimeout(refresh,30));
                const appFont=document.getElementById('appFont');if(appFont){appFont.addEventListener('input',()=>{setTimeout(applyResponsive,0);restoreStickyTheme()});appFont.addEventListener('change',restoreStickyTheme)}
                const widgetFont=document.getElementById('widgetFont');if(widgetFont){widgetFont.addEventListener('input',restoreStickyTheme);widgetFont.addEventListener('change',restoreStickyTheme)}
                const language=document.getElementById('languageSelect');if(language)language.addEventListener('change',restoreStickyTheme);
                const cycle=document.getElementById('advCycle');if(cycle)cycle.addEventListener('change',()=>setTimeout(applyResponsive,0));
                const grid=document.getElementById('themeGrid');if(grid){
                  grid.addEventListener('click',e=>{const b=e.target.closest('.themeButton');if(b&&!b.classList.contains('extraTheme'))stickyTheme=null});
                  const mo=new MutationObserver(()=>{if(!themeBusy)setTimeout(renderExtraThemes,0)});mo.observe(grid,{childList:true});
                }
                refresh();
              } catch(e) { console.log('UI polish/school calendar',e); }
            })();
            """;
    }

    // Former CourseColorUi; isolated to stay below JVM constant limits.
    private static String layer5() {
        return """
            (function(){
              try {
                if(window.__courseColorUiV2){
                  if(window.refreshCourseColors)window.refreshCourseColors();
                  return;
                }
                window.__courseColorUiV2=true;

                const COLORS={
                  butter:{bg:'#FFE078',edge:'#A86D00'},
                  apricot:{bg:'#FFBC68',edge:'#C56000'},
                  peach:{bg:'#FF9B7A',edge:'#BA4327'},
                  coral:{bg:'#FF7A67',edge:'#A92F24'},
                  terracotta:{bg:'#D98A6C',edge:'#81412E'},
                  rose:{bg:'#F08AA3',edge:'#9C3554'},
                  berry:{bg:'#D45E83',edge:'#732540'},
                  plum:{bg:'#A97191',edge:'#58364D'},
                  sand:{bg:'#D8AA5D',edge:'#80520F'},
                  olive:{bg:'#C1AF4A',edge:'#665B12'},

                  // Anciennes couleurs conservées pour les emplois du temps déjà personnalisés.
                  blue:{bg:'#DCEBFF',edge:'#0877F9'},
                  cyan:{bg:'#DFF7FA',edge:'#0097A7'},
                  teal:{bg:'#DDF4F0',edge:'#00897B'},
                  green:{bg:'#E3F3E4',edge:'#2E7D32'},
                  yellow:{bg:'#FFF5CC',edge:'#C99800'},
                  orange:{bg:'#FFE8D4',edge:'#EF6C00'},
                  violet:{bg:'#EDE7F6',edge:'#6750A4'},
                  red:{bg:'#FDE8E8',edge:'#D84343'},
                  graphite:{bg:'#ECEFF1',edge:'#546E7A'}
                };
                const PALETTE=['butter','apricot','peach','coral','terracotta','rose','berry','plum'];
                let picked='';
                let pickedTouched=false;
                let scope='cell';

                function language(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function label(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}
                function norm(s){return String(s||'').trim().replace(/ +/g,' ').toLocaleLowerCase()}

                const style=document.createElement('style');
                style.textContent=`
                  .courseColorField{margin-bottom:9px}
                  .courseColorLabel{display:block;color:var(--muted);font-size:.74rem;margin-bottom:5px}
                  .courseColorPalette{display:flex;flex-wrap:wrap;gap:7px;align-items:center}
                  .courseColorChoice{width:30px;height:30px;border-radius:50%;border:2px solid #fff;box-shadow:0 0 0 1px #cbd5e1;position:relative;padding:0}
                  .courseColorChoice.active{box-shadow:0 0 0 3px var(--blue)}
                  .courseColorChoice.none{background:#fff}
                  .courseColorChoice.none:before,.courseColorChoice.none:after{content:'';position:absolute;left:4px;right:4px;top:12px;height:2px;background:#a5afbd;transform:rotate(-40deg)}
                  .courseColorChoice.none:after{transform:rotate(40deg)}
                  .courseColorHint{font-size:.66rem;color:var(--muted);margin-top:5px;line-height:1.25}
                  .courseColorScope{display:flex;gap:5px;margin-top:8px}
                  .courseColorScope button{flex:1;min-width:0;border:1px solid var(--line);background:#fff;color:var(--muted);border-radius:999px;padding:7px 8px;font-size:.72rem;font-weight:800}
                  .courseColorScope button.active{background:var(--blue);border-color:var(--blue);color:#fff}

                  #todayList,#weekGrid{position:relative}
                  .scheduleNowRail{position:absolute;z-index:5;width:2px;background:color-mix(in srgb,var(--blue) 55%,transparent);border-radius:2px;pointer-events:none}
                  .scheduleNowDot{position:absolute;z-index:6;width:10px;height:10px;border-radius:50%;background:var(--blue);box-shadow:0 0 0 3px color-mix(in srgb,var(--blue) 18%,white);pointer-events:none;transform:translate(-4px,-5px)}
                  .scheduleNowTime{position:absolute;z-index:6;pointer-events:none;background:var(--blue);color:#fff;border-radius:999px;padding:2px 5px;font-size:.57rem;font-weight:850;line-height:1.1;white-space:nowrap;transform:translate(7px,-50%)}
                  #todayList .scheduleNowRail{left:69px;top:4px;bottom:4px}
                  #todayList .scheduleNowDot{left:69px}
                  #todayList .scheduleNowTime{left:69px}
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot{transition:top .18s ease,left .18s ease}
                `;
                document.head.appendChild(style);

                function ensurePicker(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseColorField');
                  if(!field){
                    field=document.createElement('div');field.id='courseColorField';field.className='courseColorField';
                    field.innerHTML='<span id="courseColorLabel" class="courseColorLabel"></span><div id="courseColorPalette" class="courseColorPalette"></div><div id="courseColorScope" class="courseColorScope"><button id="scopeCell" type="button"></button><button id="scopeClass" type="button"></button></div><div id="courseColorHint" class="courseColorHint"></div>';
                    const actions=form.querySelector('.sheetActions');form.insertBefore(field,actions||null);
                    const palette=document.getElementById('courseColorPalette');
                    const none=document.createElement('button');none.type='button';none.className='courseColorChoice none';none.dataset.color='';none.setAttribute('aria-label','Aucune couleur');palette.appendChild(none);
                    PALETTE.forEach(id=>{const b=document.createElement('button');b.type='button';b.className='courseColorChoice';b.dataset.color=id;b.style.background=COLORS[id].bg;b.style.borderColor=COLORS[id].edge;b.setAttribute('aria-label',id);palette.appendChild(b)});
                    palette.querySelectorAll('.courseColorChoice').forEach(b=>b.onclick=()=>{pickedTouched=true;selectColor(b.dataset.color||'')});
                    document.getElementById('scopeCell').onclick=()=>selectScope('cell');
                    document.getElementById('scopeClass').onclick=()=>selectScope('class');
                  }
                  const title=document.getElementById('courseColorLabel');if(title)title.textContent=label('Couleur de la case','Cell colour','Farbe des Feldes');
                  const cell=document.getElementById('scopeCell');if(cell)cell.textContent=label('Cette case','This cell','Dieses Feld');
                  const cls=document.getElementById('scopeClass');if(cls)cls.textContent=label('Toute la classe','Whole class','Ganze Klasse');
                  const hint=document.getElementById('courseColorHint');if(hint)hint.textContent=scope==='class'?label('La couleur sera appliquée à toutes les cases portant le même nom de classe, dans toutes les semaines.','The colour will be applied to every cell with the same class name, in all weeks.','Die Farbe wird auf alle Felder mit demselben Klassennamen in allen Wochen angewendet.'):label('La couleur sera appliquée uniquement à cette case.','The colour will only be applied to this cell.','Die Farbe wird nur auf dieses Feld angewendet.');
                }

                function selectColor(id){
                  picked=COLORS[id]?id:'';
                  document.querySelectorAll('.courseColorChoice').forEach(b=>b.classList.toggle('active',(b.dataset.color||'')===picked));
                }
                function selectScope(value){
                  scope=value==='class'?'class':'cell';
                  const a=document.getElementById('scopeCell'),b=document.getElementById('scopeClass');
                  if(a)a.classList.toggle('active',scope==='cell');if(b)b.classList.toggle('active',scope==='class');
                  ensurePicker();
                }

                function currentEditedCourse(){
                  try{if(typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof selected==='undefined'||typeof editing==='undefined')return null;if(editing==null)return null;return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null}catch(e){return null}
                }

                function applyCourseColor(el,id,gradient){
                  if(!el)return;
                  const c=COLORS[id];
                  el.style.background='';el.style.boxShadow='';
                  if(!c)return;
                  el.style.background=gradient?('linear-gradient(90deg,'+c.bg+' 0%,#ffffff 97%)'):c.bg;
                  el.style.boxShadow='inset 0 0 0 1px '+c.edge;
                }

                function decorateWeek(){
                  try{
                    const grid=document.getElementById('weekGrid');if(!grid||typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof uniqueWeekTimes!=='function')return;
                    const ws=weeks[activeWeek];if(!ws)return;
                    const times=uniqueWeekTimes();const cells=Array.from(grid.querySelectorAll('.wc'));let p=0;
                    for(const t of times){for(const d of [2,3,4,5,6]){const cell=cells[p++];if(!cell)continue;const c=ws[d]&&ws[d].courses?ws[d].courses.find(x=>x.start===t.start&&x.end===t.end):null;applyCourseColor(cell,c&&c.color?c.color:'',false)}}
                  }catch(e){}
                }

                function decorateEdit(){
                  try{
                    if(typeof state==='undefined'||typeof selected==='undefined')return;
                    const list=state[selected]&&state[selected].courses?state[selected].courses:[];
                    document.querySelectorAll('#editList .editCourse').forEach((row,i)=>applyCourseColor(row,list[i]&&list[i].color?list[i].color:'',true));
                  }catch(e){}
                }

                function decorateToday(){
                  try{
                    if(typeof weeks==='undefined'||typeof currentWeek==='undefined'||typeof todayKey!=='function')return;
                    const d=todayKey(),list=weeks[currentWeek]&&weeks[currentWeek][d]?weeks[currentWeek][d].courses:[];
                    document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{
                      const start=(row.querySelector('.time strong')||{}).textContent||'';
                      const text=(row.querySelector('.label')||{}).textContent||'';
                      let c=list.find(x=>x.start===start&&text.indexOf(x.label)>=0);if(!c)c=list.find(x=>x.start===start);
                      applyCourseColor(row,c&&c.color?c.color:'',true);
                    });
                  }catch(e){}
                }

                function ensureNowParts(parent,prefix){
                  if(!parent)return null;
                  let rail=document.getElementById(prefix+'NowRail'),dot=document.getElementById(prefix+'NowDot'),time=document.getElementById(prefix+'NowTime');
                  if(!rail){rail=document.createElement('div');rail.id=prefix+'NowRail';rail.className='scheduleNowRail';parent.appendChild(rail)}
                  if(!dot){dot=document.createElement('div');dot.id=prefix+'NowDot';dot.className='scheduleNowDot';parent.appendChild(dot)}
                  if(prefix==='today'&&!time){time=document.createElement('div');time.id=prefix+'NowTime';time.className='scheduleNowTime';parent.appendChild(time)}
                  return {rail,dot,time};
                }

                function rowRange(row){
                  const time=row?row.querySelector('.time'):null;if(!time)return null;
                  const txt=time.textContent||'';const found=txt.match(/[0-2]?[0-9]:[0-5][0-9]/g);if(!found||found.length<2)return null;
                  return {start:min(found[0]),end:min(found[1])};
                }

                function updateTodayNow(){
                  try{
                    const box=document.getElementById('todayList');if(!box)return;
                    const parts=ensureNowParts(box,'today');
                    const now=new Date(),jsDay=now.getDay(),wanted=typeof todayKey==='function'?todayKey():null;
                    const isToday=wanted===jsDay+1;
                    if(!isToday){parts.rail.style.display=parts.dot.style.display='none';if(parts.time)parts.time.style.display='none';return}
                    const m=now.getHours()*60+now.getMinutes();let target=null,frac=0;
                    for(const row of box.querySelectorAll('.todayCourse')){const r=rowRange(row);if(r&&m>=r.start&&m<=r.end){target=row;frac=(m-r.start)/Math.max(1,r.end-r.start);break}}
                    if(!target){parts.rail.style.display=parts.dot.style.display='none';if(parts.time)parts.time.style.display='none';return}
                    const y=target.offsetTop+Math.max(2,Math.min(target.offsetHeight-2,target.offsetHeight*frac));
                    parts.rail.style.display=parts.dot.style.display='block';parts.dot.style.top=y+'px';
                    if(parts.time){parts.time.style.display='block';parts.time.style.top=y+'px';parts.time.textContent=String(now.getHours()).padStart(2,'0')+':'+String(now.getMinutes()).padStart(2,'0')}
                  }catch(e){}
                }

                function updateWeekNow(){
                  try{
                    const grid=document.getElementById('weekGrid');if(!grid||typeof uniqueWeekTimes!=='function')return;
                    const parts=ensureNowParts(grid,'week');const now=new Date(),jsDay=now.getDay();
                    if(jsDay<1||jsDay>5){parts.rail.style.display=parts.dot.style.display='none';return}
                    const times=uniqueWeekTimes();const m=now.getHours()*60+now.getMinutes();let rowIndex=-1,frac=0;
                    for(let i=0;i<times.length;i++){const s=min(times[i].start),e=min(times[i].end);if(m>=s&&m<=e){rowIndex=i;frac=(m-s)/Math.max(1,e-s);break}}
                    const cells=Array.from(grid.querySelectorAll('.wc'));
                    if(rowIndex<0||!cells.length){parts.rail.style.display=parts.dot.style.display='none';return}
                    const dayIndex=jsDay-1;const first=cells[dayIndex],target=cells[rowIndex*5+dayIndex],last=cells[(times.length-1)*5+dayIndex];
                    if(!first||!target||!last){parts.rail.style.display=parts.dot.style.display='none';return}
                    const left=target.offsetLeft+4,top=first.offsetTop+2,bottom=last.offsetTop+last.offsetHeight-2;
                    parts.rail.style.display=parts.dot.style.display='block';parts.rail.style.left=left+'px';parts.rail.style.top=top+'px';parts.rail.style.height=Math.max(2,bottom-top)+'px';
                    parts.dot.style.left=left+'px';parts.dot.style.top=(target.offsetTop+Math.max(2,Math.min(target.offsetHeight-2,target.offsetHeight*frac)))+'px';
                  }catch(e){}
                }

                function updateNowMarkers(){updateTodayNow();updateWeekNow()}
                function decorateAll(){decorateWeek();decorateEdit();decorateToday();updateNowMarkers()}

                function syncPicker(){ensurePicker();pickedTouched=false;const c=currentEditedCourse();selectColor(c&&c.color?c.color:'');selectScope('cell')}

                const modal=document.getElementById('modal');
                if(modal){new MutationObserver(()=>{if(modal.classList.contains('show'))syncPicker()}).observe(modal,{attributes:true,attributeFilter:['class']})}

                const form=document.getElementById('courseForm');
                if(form&&form.onsubmit&&!form.onsubmit.__courseColorWrappedV2){
                  const oldSubmit=form.onsubmit;
                  const wrapped=function(e){
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A';
                    const day=typeof selected!=='undefined'?selected:2;
                    const idx=typeof editing!=='undefined'?editing:null;
                    const old=(idx!=null&&weeks[week]&&weeks[week][day])?weeks[week][day].courses[idx]:null;
                    const oldClass=old?old.label:'';
                    const pre=typeof newPrefill!=='undefined'&&newPrefill?{start:newPrefill.start,end:newPrefill.end}:null;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0);
                    const text=((document.getElementById('fLabel')||{}).value||'').trim();
                    let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}else if(old){start=old.start;end=old.end}else if(pre){start=pre.start;end=pre.end}
                    const chosen=picked,chosenScope=scope;
                    const result=oldSubmit.call(this,e);
                    try{
                      const arr=weeks[week]&&weeks[week][day]?weeks[week][day].courses:[];
                      let target=arr.find(c=>c.start===start&&c.end===end&&c.label===text);if(!target)target=arr.find(c=>c.start===start&&c.end===end);
                      if(pickedTouched&&target)target.color=chosen;
                      if(pickedTouched&&chosenScope==='class'){
                        const match=norm(oldClass||text);
                        Object.keys(weeks).forEach(w=>{const ws=weeks[w];if(!ws)return;Object.keys(ws).forEach(d=>{const dd=ws[d];if(!dd||!Array.isArray(dd.courses))return;dd.courses.forEach(c=>{if(norm(c.label)===match)c.color=chosen})})});
                        if(target)target.color=chosen;
                      }
                      if(typeof save==='function')save();
                    }catch(err){}
                    scheduleDecorate84();
                    return result;
                  };
                  wrapped.__courseColorWrappedV2=true;form.onsubmit=wrapped;
                }

                let decorateTimer84=0;
                function scheduleDecorate84(){if(decorateTimer84)return;decorateTimer84=setTimeout(()=>{decorateTimer84=0;decorateAll()},18)}
                ['weekGrid','editList','todayList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(scheduleDecorate84).observe(el,{childList:true})});
                window.addEventListener('resize',()=>setTimeout(updateNowMarkers,20));
                setInterval(updateNowMarkers,60000);

                function refresh(){ensurePicker();decorateAll();if(modal&&modal.classList.contains('show'))syncPicker()}
                window.refreshCourseColors=refresh;
                refresh();
              }catch(e){console.log('Course colours',e)}
            })();
            """;
    }

    // Former PaletteSelectorUi; isolated to stay below JVM constant limits.
    private static String layer6() {
        return """
            (function(){
              try {
                if(window.__coursePaletteUiV4){
                  if(window.refreshCoursePaletteV4)window.refreshCoursePaletteV4();
                  return;
                }
                window.__coursePaletteUiV4=true;

                const WIDGET_KEY='edt-widget-palette-fallback-v1';
                const APP_KEY='edt-app-palette-v1';
                const SYNC_KEY='edt-palette-sync-v1';
                const INDEX={butter:0,apricot:1,peach:2,coral:3,terracotta:4,rose:5,berry:6,plum:6,sand:4,olive:5,blue:0,cyan:1,teal:2,green:3,yellow:4,orange:5,violet:6,red:6,graphite:6};
                const WIDGET_PALETTES={
                  vivid:{fr:'Éclat',en:'Vivid',de:'Kräftig',colors:['#F0335D','#FF7B2F','#FFE47D','#21C877','#18B9BE','#2F83E8','#9B55E9']},
                  pastel:{fr:'Pastel',en:'Pastel',de:'Pastell',colors:['#F58BA6','#FFAD72','#FFE3A0','#8BD5A4','#79D0D4','#8CB7ED','#B59AE7']},
                  warm:{fr:'Chaud',en:'Warm',de:'Warm',colors:['#EF5968','#FF7B72','#FF9B59','#F7B487','#E6BF85','#D98B9B','#B98CA5']},
                  cool:{fr:'Froid',en:'Cool',de:'Kühl',colors:['#3E91B8','#42B6BE','#4DB78B','#84BF67','#6DA3E7','#6D82D7','#9874D0']},
                  soft:{fr:'Sobre',en:'Soft',de:'Dezent',colors:['#7B8FA4','#9AA7AF','#CDD0BC','#86A894','#7AA7AA','#8098B6','#998DA9']}
                };
                const MIDI_BG='#FFF9E8',MIDI_BORDER='#E4B84D',MIDI_INK='#22283A';
                const GAP_BG='#FFFFFF',GAP_BORDER='#DDE4EC',GAP_INK='#22283A';

                function language(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function currentWidget(){
                  try{if(window.AndroidSchedule&&AndroidSchedule.loadWidgetPalette){const v=AndroidSchedule.loadWidgetPalette();if(WIDGET_PALETTES[v])return v}}catch(e){}
                  const v=localStorage.getItem(WIDGET_KEY);return WIDGET_PALETTES[v]?v:'vivid';
                }
                function currentApp(){
                  const stored=localStorage.getItem(APP_KEY);
                  if(WIDGET_PALETTES[stored])return stored;
                  const initial=currentWidget();localStorage.setItem(APP_KEY,initial);return initial;
                }
                function palettesSynced(){return localStorage.getItem(SYNC_KEY)!=='0'}
                function saveWidget(id){
                  if(!WIDGET_PALETTES[id])id='vivid';
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveWidgetPalette)AndroidSchedule.saveWidgetPalette(id)}catch(e){}
                  localStorage.setItem(WIDGET_KEY,id);
                }
                function saveApp(id){
                  if(!WIDGET_PALETTES[id])id='vivid';
                  localStorage.setItem(APP_KEY,id);
                  if(palettesSynced())saveWidget(id);
                }
                function setPaletteSync(value){
                  localStorage.setItem(SYNC_KEY,value?'1':'0');
                  if(value)saveWidget(currentApp());
                }
                function appPalette(){return WIDGET_PALETTES[currentApp()]||WIDGET_PALETTES.vivid}
                function colorFor(id){const i=INDEX[id];const p=appPalette().colors;return Number.isInteger(i)?p[Math.max(0,Math.min(p.length-1,i))]:null}

                function text(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}
                function paletteTitle(){return palettesSynced()?text('Palette de l’application et du widget','App & widget palette','App- und Widget-Farben'):text('Palette de l’application','App palette','App-Farben')}
                function paletteHint(){return text('Les couleurs choisies pour les cases sont utilisées dans le widget tant que la synchronisation est activée.','Course colours are also used in the widget while palette sync is enabled.','Die Kursfarben werden auch im Widget verwendet, solange die Synchronisierung aktiv ist.')}
                function syncLabel(){return text('Utiliser la même palette dans l’application et le widget','Use the same palette in the app and widget','Gleiche Palette in App und Widget verwenden')}
                function separateWidgetTitle(){return text('Palette du widget','Widget palette','Widget-Farben')}
                function breakTitle(){return text('Midi et trous','Lunch and free periods','Mittag und Freistunden')}
                function breakHint(){return text('Midi utilise le style sable-champagne et les trous restent blancs. Choisis ici s’ils doivent apparaître.','Lunch uses the sand-champagne style and free periods stay white. Choose here whether to show them.','Mittag nutzt den Sand-Champagner-Stil und Freistunden bleiben weiß. Hier wählst du, ob sie angezeigt werden.')}
                function lunchSwitch(){return text('Afficher Midi','Show lunch','Mittag anzeigen')}
                function gapSwitch(){return text('Afficher les trous','Show free periods','Freistunden anzeigen')}

                const style=document.createElement('style');
                style.textContent=`
                  #paletteSettingRoot .settingTitle,#breakDisplaySetting .settingTitle{margin-bottom:3px}
                  .coursePaletteHint{font-size:.66rem;color:var(--muted);margin-bottom:8px}
                  .coursePaletteGrid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:7px}
                  .coursePaletteBtn{border:2px solid var(--line);background:#fff;border-radius:10px;padding:7px 6px;min-width:0;text-align:left}
                  .coursePaletteBtn.active{border-color:var(--blue);background:color-mix(in srgb,var(--blue) 7%,white)}
                  .coursePaletteName{font-size:.70rem;font-weight:900;color:var(--ink);display:block;margin-bottom:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  .coursePaletteSwatches{display:flex;gap:2px}.coursePaletteSwatches i{display:block;flex:1;height:14px;border-radius:3px;min-width:0}
                  .paletteSubTitle{font-size:.68rem;font-weight:900;color:var(--ink);margin:8px 0 5px}
                  .paletteSyncRow{display:flex;align-items:center;justify-content:space-between;gap:10px;margin:7px 0 10px;padding:8px 10px;border:1px solid var(--line);border-radius:10px;background:#fff;color:var(--ink);font-size:.70rem;font-weight:800}
                  .paletteSyncRow input{width:19px;height:19px;accent-color:var(--blue);flex:0 0 auto}
                  #widgetPaletteSeparate{margin-top:10px;padding-top:8px;border-top:1px solid var(--line)}

                  #todayList .todayCourse.lunch,#todayList .todayCourse.gap{
                    width:100%!important;box-sizing:border-box!important;margin-left:0!important;margin-right:0!important;
                    min-height:54px!important;border-radius:0!important;box-shadow:none!important;
                    padding-top:5px!important;padding-bottom:5px!important;
                  }
                  #todayList .todayCourse.lunch{background:${MIDI_BG}!important;color:${MIDI_INK}!important;border:1px solid ${MIDI_BORDER}!important}
                  #todayList .todayCourse.gap{background:${GAP_BG}!important;color:${GAP_INK}!important;border:1px solid ${GAP_BORDER}!important}
                  #todayList .todayCourse.lunch .time,#todayList .todayCourse.lunch .room,#todayList .todayCourse.lunch .label{color:${MIDI_INK}!important}
                  #todayList .todayCourse.gap .time,#todayList .todayCourse.gap .room,#todayList .todayCourse.gap .label{color:${GAP_INK}!important}
                  #todayList .todayCourse.lunch .label:before{content:'🍴';display:inline-block;margin-right:7px}
                  #todayList .todayCourse.gap .label:before{content:none!important;display:none!important}
                  #todayList .todayCourse.lunch .badge,#todayList .todayCourse.gap .badge{border:0!important;box-shadow:none!important}

                  #weekGrid .wc.gapCell{background:${GAP_BG}!important;color:${GAP_INK}!important;box-shadow:inset 0 0 0 1px ${GAP_BORDER}!important}
                  #weekGrid .wc.gapCell *{color:${GAP_INK}!important}
                  #weekGrid .wc.gapCell .cellLabel:before{content:none!important;display:none!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell){background:${MIDI_BG}!important;color:${MIDI_INK}!important;box-shadow:inset 0 0 0 1px ${MIDI_BORDER}!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell) *{color:${MIDI_INK}!important}
                  #weekGrid .dynamicLunchCell{background:#fff!important;color:${MIDI_INK}!important}
                  #weekGrid .dynamicLunchOverlay{background:${MIDI_BG}!important;color:${MIDI_INK}!important;border:1px solid ${MIDI_BORDER}!important;box-shadow:none!important}
                  #weekGrid .dynamicLunchOverlay *{color:${MIDI_INK}!important}
                  #weekGrid .wc.lunchCell .cellLabel:before{content:'🍴';display:inline-block;margin-right:4px;font-size:.72em;vertical-align:middle}

                  @media(max-width:390px){.coursePaletteGrid{grid-template-columns:1fr}.coursePaletteBtn{display:grid;grid-template-columns:92px 1fr;align-items:center;gap:7px}.coursePaletteName{margin:0}.coursePaletteSwatches i{height:16px}}
                `;
                document.head.appendChild(style);

                function paletteButton(id,target,selected,onClick){
                  const p=WIDGET_PALETTES[id],l=language(),b=document.createElement('button');
                  b.type='button';b.className='coursePaletteBtn';b.dataset.palette=id;b.classList.toggle('active',id===selected);
                  b.innerHTML='<span class="coursePaletteName">'+(p[l]||p.fr)+'</span><span class="coursePaletteSwatches">'+p.colors.map(c=>'<i style="background:'+c+'"></i>').join('')+'</span>';
                  b.onclick=()=>onClick(id);target.appendChild(b);
                }
                function renderAppButtons(){
                  const grid=document.getElementById('appPaletteGrid');if(!grid)return;grid.innerHTML='';const selected=currentApp();
                  Object.keys(WIDGET_PALETTES).forEach(id=>paletteButton(id,grid,selected,value=>{saveApp(value);renderPaletteControls();repaint()}));
                }
                function renderWidgetButtons(){
                  const grid=document.getElementById('widgetPaletteGrid');if(!grid)return;grid.innerHTML='';const selected=currentWidget();
                  Object.keys(WIDGET_PALETTES).forEach(id=>paletteButton(id,grid,selected,value=>{saveWidget(value);renderPaletteControls()}));
                }
                function renderPaletteControls(){
                  const title=document.getElementById('appPaletteTitle');if(title)title.textContent=paletteTitle();
                  const hint=document.getElementById('paletteSyncHint');if(hint)hint.textContent=paletteHint();
                  const label=document.getElementById('paletteSyncLabel');if(label)label.textContent=syncLabel();
                  const toggle=document.getElementById('paletteSyncToggle');if(toggle)toggle.checked=palettesSynced();
                  const sep=document.getElementById('widgetPaletteSeparate');if(sep)sep.style.display=palettesSynced()?'none':'block';
                  const wt=document.getElementById('widgetPaletteSeparateTitle');if(wt)wt.textContent=separateWidgetTitle();
                  renderAppButtons();if(!palettesSynced())renderWidgetButtons();
                }

                function ensureSettings(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  ['coursePaletteSetting','widgetPaletteSetting'].forEach(id=>{const old=document.getElementById(id);if(old)old.remove()});
                  const actions=sheet.querySelector('.settingsActions');
                  let root=document.getElementById('paletteSettingRoot');
                  if(!root){
                    root=document.createElement('div');root.id='paletteSettingRoot';root.className='settingBox';
                    root.innerHTML='<div id="appPaletteTitle" class="settingTitle"></div><div id="paletteSyncHint" class="coursePaletteHint"></div><label class="paletteSyncRow"><span id="paletteSyncLabel"></span><input id="paletteSyncToggle" type="checkbox"></label><div id="appPaletteGrid" class="coursePaletteGrid"></div><div id="widgetPaletteSeparate"><div id="widgetPaletteSeparateTitle" class="paletteSubTitle"></div><div id="widgetPaletteGrid" class="coursePaletteGrid"></div></div>';
                    sheet.insertBefore(root,actions||null);
                    const toggle=root.querySelector('#paletteSyncToggle');if(toggle)toggle.addEventListener('change',()=>{setPaletteSync(toggle.checked);renderPaletteControls();repaint()});
                  }
                  renderPaletteControls();

                  let breaks=document.getElementById('breakDisplaySetting');
                  if(!breaks){
                    breaks=document.createElement('div');breaks.id='breakDisplaySetting';breaks.className='settingBox';
                    breaks.innerHTML='<div id="breakDisplayTitle" class="settingTitle"></div><div id="breakDisplayHint" class="coursePaletteHint"></div>';
                    sheet.insertBefore(breaks,root.nextSibling||actions||null);
                  }
                  const lunch=document.getElementById('advShowLunch'),gap=document.getElementById('advShowBreaks');
                  const lunchRow=lunch&&lunch.closest?lunch.closest('label'):null,gapRow=gap&&gap.closest?gap.closest('label'):null;
                  if(lunchRow&&lunchRow.parentNode!==breaks)breaks.appendChild(lunchRow);
                  if(gapRow&&gapRow.parentNode!==breaks)breaks.appendChild(gapRow);
                  const bt=document.getElementById('breakDisplayTitle');if(bt)bt.textContent=breakTitle();
                  const bh=document.getElementById('breakDisplayHint');if(bh)bh.textContent=breakHint();
                  const ll=document.getElementById('advShowLunchLabel');if(ll)ll.textContent=lunchSwitch();
                  const gl=document.getElementById('advShowBreaksLabel');if(gl)gl.textContent=gapSwitch();
                }

                function apply(el,id,gradient){
                  if(!el)return;const c=colorFor(id);if(!c)return;
                  el.style.setProperty('background',gradient?('linear-gradient(90deg,'+c+' 0%,#ffffff 97%)'):c,'important');
                  el.style.setProperty('box-shadow','none','important');
                }
                function repaintPicker(){
                  document.querySelectorAll('.courseColorChoice:not(.none)').forEach(b=>{const c=colorFor(b.dataset.color||'');if(c){b.style.setProperty('background',c,'important');b.style.setProperty('border-color',c,'important')}});
                }
                function repaintWeek(){
                  try{const grid=document.getElementById('weekGrid');if(!grid||typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof uniqueWeekTimes!=='function')return;const ws=weeks[activeWeek];if(!ws)return;const times=uniqueWeekTimes();const cells=Array.from(grid.querySelectorAll('.wc'));let p=0;for(const t of times){for(const d of [2,3,4,5,6]){const cell=cells[p++];if(!cell)continue;const c=ws[d]&&ws[d].courses?ws[d].courses.find(x=>x.start===t.start&&x.end===t.end):null;if(c&&c.color)apply(cell,c.color,false)}}}catch(e){}
                }
                function repaintToday(){
                  try{if(typeof weeks==='undefined'||typeof currentWeek==='undefined'||typeof todayKey!=='function')return;const d=todayKey(),list=weeks[currentWeek]&&weeks[currentWeek][d]?weeks[currentWeek][d].courses:[];document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{const start=(row.querySelector('.time strong')||{}).textContent||'';const text=(row.querySelector('.label')||{}).textContent||'';let c=list.find(x=>x.start===start&&text.indexOf(x.label)>=0);if(!c)c=list.find(x=>x.start===start);if(c&&c.color)apply(row,c.color,true)})}catch(e){}
                }
                function repaintEdit(){
                  try{if(typeof state==='undefined'||typeof selected==='undefined')return;const list=state[selected]&&state[selected].courses?state[selected].courses:[];document.querySelectorAll('#editList .editCourse').forEach((row,i)=>{const c=list[i];if(c&&c.color)apply(row,c.color,true)})}catch(e){}
                }
                function ensureLinkedPalette(){
                  if(!palettesSynced())return;const a=currentApp(),w=currentWidget();if(a!==w)saveWidget(a);
                }
                function repaint(){ensureLinkedPalette();repaintPicker();repaintWeek();repaintToday();repaintEdit();ensureSettings()}

                ['weekGrid','todayList','editList','courseColorPalette'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(repaint,0)).observe(el,{childList:true,subtree:true})});
                const settingsModal=document.getElementById('settingsModal');if(settingsModal)new MutationObserver(()=>setTimeout(ensureSettings,0)).observe(settingsModal,{attributes:true,attributeFilter:['class']});
                const settingsBtn=document.getElementById('settingsBtn');if(settingsBtn)settingsBtn.addEventListener('click',()=>setTimeout(ensureSettings,0));
                const form=document.getElementById('courseForm');if(form)form.addEventListener('submit',()=>setTimeout(repaint,30));

                const originalRefresh=window.refreshCourseColors;
                if(typeof originalRefresh==='function'&&!originalRefresh.__paletteWrappedV4){const wrapped=function(){const r=originalRefresh.apply(this,arguments);setTimeout(repaint,0);return r};wrapped.__paletteWrappedV4=true;window.refreshCourseColors=wrapped}

                window.refreshCoursePaletteV4=repaint;
                repaint();
              }catch(e){console.log('Palette selector',e)}
            })();
            """;
    }

    // Former LunchBreakUi; isolated to stay below JVM constant limits.
    private static String layer7() {
        return """
            (function(){
              try {
                if(window.__lunchBreakUiV9){
                  if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                  return;
                }
                window.__lunchBreakUiV9=true;
                const EMPTY_MARK=String.fromCharCode(8203);

                const style=document.createElement('style');
                style.textContent=`
                  #todayClock{display:none!important}
                  #todayList #todayNowTime{display:none!important}
                  #todayList .todayCourse{grid-template-columns:58px minmax(0,1fr) auto!important;gap:4px!important}
                  #todayList .todayCourse .time{text-align:center!important}
                  #todayList #todayNowRail{left:61px!important}
                  #todayList #todayNowDot{left:61px!important}
                  .breakSettings .badgeToggle{display:none!important}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important}
                  #todayList .todayCourse.gap>.badge,#todayList .todayCourse.lunch>.badge{display:none!important}
                  #weekGrid .dynamicLunchCell{position:relative;overflow:hidden!important;background:#fff7e6!important;color:var(--lunch);z-index:2}
                  #weekGrid .dynamicLunchEmpty{background:#fff!important}
                  #weekGrid .dynamicLunchOverlay{position:absolute;inset:2px 1px;z-index:9;display:flex;align-items:center;justify-content:center;padding:2px 3px;border:1px solid #efc66f;border-radius:6px;background:linear-gradient(90deg,#fff0c8 0%,#ffe2a6 100%);color:#8d5810;box-shadow:0 1px 2px #8d581012;pointer-events:none;white-space:nowrap;overflow:hidden}
                  #weekGrid .breakFitLabel{display:block!important;max-width:100%;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;line-height:1!important;-webkit-line-clamp:1!important;-webkit-box-orient:initial!important;font-size:.58rem!important}
                  body.largeAppText #weekGrid .breakFitLabel{font-size:.60rem!important}
                  #weekGrid #weekNowRail{left:var(--week-now-x,39px)!important;width:2px!important;border-radius:2px!important;opacity:.78}
                  #weekGrid #weekNowDot{left:var(--week-now-x,39px)!important;width:11px!important;height:11px!important;transform:translate(-5px,-5px)!important}
                  .courseBadgePill{display:inline-flex;align-items:center;max-width:100%;margin-left:5px;padding:2px 6px;border-radius:999px;background:#e8f2ff;color:#0868c7;font-size:.60rem;font-weight:850;line-height:1.05;vertical-align:middle;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  #weekGrid .courseBadgePill{display:inline-block;margin:2px 0 0;padding:1px 4px;font-size:.46rem;max-width:95%}
                  #editList .courseBadgePill{margin-left:6px}
                  #courseBadgeField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                function visibleLabel(value){
                  return String(value==null?'':value).split(EMPTY_MARK).join('').trim().slice(0,28);
                }
                function storedLabel(value){
                  const v=visibleLabel(value);
                  return v||EMPTY_MARK;
                }
                function gapLabelText(){return visibleLabel(typeof breaks!=='undefined'&&breaks?breaks.gapLabel:'Trou')}
                function lunchLabelText(){return visibleLabel(typeof breaks!=='undefined'&&breaks?breaks.lunchLabel:'Midi')}
                window.gapLabelText=gapLabelText;
                window.lunchLabelText=lunchLabelText;

                function lunchBounds(){
                  try{
                    const start=(Array.isArray(slots)&&slots[3]&&slots[3].end)?min(slots[3].end):12*60;
                    const end=(Array.isArray(slots)&&slots[4]&&slots[4].start)?min(slots[4].start):13*60;
                    return {start,end};
                  }catch(e){return {start:12*60,end:13*60}}
                }
                window.lunchBounds=lunchBounds;

                function lunchForDay(list){
                  try{
                    const courses=(Array.isArray(list)?list:[]).slice().sort((a,b)=>min(a.start)-min(b.start));
                    if(!courses.length)return null;
                    const bounds=lunchBounds(),start=bounds.start,end=bounds.end;
                    if(end<=start)return null;
                    const hasBefore=courses.some(c=>min(c.end)<=start);
                    const hasAfter=courses.some(c=>min(c.start)>=end);
                    if(!hasBefore||!hasAfter)return null;
                    if(courses.some(c=>min(c.start)<end&&min(c.end)>start))return null;
                    return {start:clock(start),end:clock(end),startM:start,endM:end,duration:end-start};
                  }catch(e){return null}
                }
                window.lunchForDay=lunchForDay;

                function gapsForDay(list){
                  const out=[],courses=(Array.isArray(list)?list:[]).slice().sort((a,b)=>min(a.start)-min(b.start)),l=lunchForDay(courses);
                  const ls=l?l.startM:-1,le=l?l.endM:-1;
                  for(let i=0;i<courses.length-1;i++){
                    let a=min(courses[i].end),b=min(courses[i+1].start);if(b<=a)continue;
                    if(!l||b<=ls||a>=le)out.push({start:a,end:b});
                    else{
                      if(a<ls)out.push({start:a,end:Math.min(b,ls)});
                      if(b>le)out.push({start:Math.max(a,le),end:b});
                    }
                  }
                  return out.filter(g=>g.end>g.start);
                }
                window.dynamicGapSegments=gapsForDay;

                function dynamicWeekTimes(){
                  const map=new Map();
                  for(const s of slots)map.set(s.start+'|'+s.end,{start:s.start,end:s.end,type:'slot'});
                  for(const d of DAYS)for(const c of state[d].courses)map.set(c.start+'|'+c.end,{start:c.start,end:c.end,type:'course'});
                  const out=[...map.values()];
                  if(DAYS.some(d=>lunchForDay(state[d].courses))){
                    const b=lunchBounds();out.push({start:clock(b.start),end:clock(b.end),type:'lunchDynamic'});
                  }
                  return out.sort((a,b)=>min(a.start)-min(b.start)||((a.type==='lunchDynamic')?-1:0)-((b.type==='lunchDynamic')?-1:0)||min(a.end)-min(b.end));
                }

                function renderBreakSettingsV9(){
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  if(gap&&document.activeElement!==gap)gap.value=gapLabelText();
                  if(lunch&&document.activeElement!==lunch)lunch.value=lunchLabelText();
                }

                let persistTimer=null;
                function syncBreakStateFromControls(){
                  if(typeof breaks==='undefined'||!breaks)return;
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  if(gap)breaks.gapLabel=storedLabel(gap.value);
                  if(lunch)breaks.lunchLabel=storedLabel(lunch.value);
                  breaks.showGapBadge=false;
                  breaks.showLunchBadge=false;
                }
                function persistBreakState(){
                  try{
                    syncBreakStateFromControls();
                    if(window.AndroidSchedule&&AndroidSchedule.saveSchedule&&typeof exportState==='function')AndroidSchedule.saveSchedule(JSON.stringify(exportState()));
                    else if(typeof localStorage!=='undefined'&&typeof exportState==='function')localStorage.setItem('edt',JSON.stringify(exportState()));
                  }catch(e){}
                }
                function queuePersist(){
                  syncBreakStateFromControls();
                  syncBreakCells();
                  if(persistTimer)clearTimeout(persistTimer);
                  persistTimer=setTimeout(persistBreakState,100);
                }
                function applyBreakSettingsV9(){
                  syncBreakStateFromControls();
                  persistBreakState();
                  syncBreakCells();
                  setTimeout(()=>{renderBreakSettingsV9();fitBreakLabels();polishWeekNowMarker();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors()},15);
                }

                function wireBreakSettings(){
                  document.querySelectorAll('.breakSettings .badgeToggle').forEach(el=>el.remove());
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  for(const input of [gap,lunch])if(input){
                    input.maxLength=28;
                    input.placeholder='Laisser vide pour aucun texte';
                    if(!input.__breakV9){
                      input.__breakV9=true;
                      input.onchange=null;
                      input.addEventListener('input',queuePersist);
                      input.addEventListener('change',applyBreakSettingsV9);
                      input.addEventListener('blur',applyBreakSettingsV9);
                    }
                  }
                  if(typeof breaks!=='undefined'&&breaks){breaks.showGapBadge=false;breaks.showLunchBadge=false}
                  renderBreakSettingsV9();
                }

                function ensureBreakLabel(row,label,selector){
                  if(!row)return;
                  const holder=row.children.length>1?row.children[1]:row;
                  let el=row.querySelector(selector);
                  if(label){
                    if(!el){el=document.createElement('div');el.className=selector.indexOf('cellLabel')>=0?'cellLabel breakFitLabel':'label';if(holder.firstChild)holder.insertBefore(el,holder.firstChild);else holder.appendChild(el)}
                    if(el.textContent!==label)el.textContent=label;
                  }else if(el){el.remove()}
                }
                function syncBreakCells(){
                  try{
                    const g=gapLabelText(),l=lunchLabelText();
                    document.querySelectorAll('#todayList .todayCourse.gap').forEach(row=>ensureBreakLabel(row,g,'.label'));
                    document.querySelectorAll('#todayList .todayCourse.lunch').forEach(row=>ensureBreakLabel(row,l,'.label'));
                    document.querySelectorAll('#todayList .todayCourse.gap>.badge,#todayList .todayCourse.lunch>.badge').forEach(x=>x.remove());
                    document.querySelectorAll('#weekGrid .gapCell').forEach(cell=>{
                      let el=cell.querySelector('.cellLabel');
                      if(g){if(!el){el=document.createElement('div');el.className='cellLabel breakFitLabel';cell.appendChild(el)}if(el.textContent!==g)el.textContent=g}
                      else if(el)el.remove();
                    });
                    document.querySelectorAll('#weekGrid .lunchCell').forEach(cell=>{
                      const holder=cell.querySelector('.dynamicLunchOverlay')||cell;let el=holder.querySelector('.cellLabel');
                      if(l){if(!el){el=document.createElement('span');el.className='cellLabel breakFitLabel';holder.appendChild(el)}if(el.textContent!==l)el.textContent=l}
                      else if(el)el.remove();
                    });
                  }catch(e){}
                }
                window.syncBreakCells=syncBreakCells;

                function fitBreakLabel(el){
                  try{
                    if(!el)return;
                    let size=document.body.classList.contains('largeAppText')?.60:.58;
                    el.style.setProperty('font-size',size+'rem','important');
                    const holder=el.parentElement||el,max=Math.max(8,holder.clientWidth-6);
                    while(el.scrollWidth>max&&size>.38){size-=.02;el.style.setProperty('font-size',size.toFixed(2)+'rem','important')}
                  }catch(e){}
                }
                function fitBreakLabels(){document.querySelectorAll('#weekGrid .breakFitLabel').forEach(fitBreakLabel)}

                function polishWeekNowMarker(){
                  try{
                    const grid=document.getElementById('weekGrid'),rail=document.getElementById('weekNowRail'),dot=document.getElementById('weekNowDot');
                    if(!grid||!rail||!dot)return;
                    const now=new Date(),jsDay=now.getDay();
                    if(jsDay<1||jsDay>5||typeof activeWeek==='undefined'||typeof currentWeek==='undefined'||activeWeek!==currentWeek){rail.style.display='none';dot.style.display='none';return}
                    const firstDayCell=grid.querySelector('.wc');
                    if(firstDayCell)grid.style.setProperty('--week-now-x',firstDayCell.offsetLeft+'px');
                    if(rail.style.display==='none'||dot.style.display==='none')return;
                    const top=parseFloat(rail.style.top),dotTop=parseFloat(dot.style.top);
                    if(Number.isFinite(top)&&Number.isFinite(dotTop))rail.style.height=Math.max(2,dotTop-top)+'px';
                  }catch(e){}
                }

                function courseBadge(c){return String(c&&c.badge?c.badge:'').trim().slice(0,24)}
                function badgeHtml(c){const b=courseBadge(c);return b?`<span class="courseBadgePill">${esc(b)}</span>`:''}

                function ensureCourseBadgeField(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseBadgeField');
                  if(!field){
                    field=document.createElement('div');field.id='courseBadgeField';field.className='field';
                    field.innerHTML='<label>Badge (facultatif)</label><input id="fCourseBadge" type="text" maxlength="24" placeholder="Ex. DST, Groupe, Important">';
                    const room=document.getElementById('fRoom');const roomField=room?room.closest('.field'):null;
                    if(roomField&&roomField.nextSibling)roomField.parentNode.insertBefore(field,roomField.nextSibling);else form.insertBefore(field,form.querySelector('.sheetActions'));
                  }
                }
                function currentEditedCourseV9(){
                  try{if(typeof editing==='undefined'||editing==null||typeof weeks==='undefined')return null;return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null}catch(e){return null}
                }
                function syncCourseBadgeField(){ensureCourseBadgeField();const input=document.getElementById('fCourseBadge');if(input)input.value=courseBadge(currentEditedCourseV9())}

                function wrapCourseSubmit(){
                  ensureCourseBadgeField();
                  const form=document.getElementById('courseForm');if(!form||!form.onsubmit||form.onsubmit.__courseBadgeWrappedV9)return;
                  const oldSubmit=form.onsubmit;
                  const wrapped=function(e){
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A',day=typeof selected!=='undefined'?selected:2,idx=typeof editing!=='undefined'?editing:null;
                    const old=(idx!=null&&weeks[week]&&weeks[week][day])?weeks[week][day].courses[idx]:null;
                    const pre=typeof newPrefill!=='undefined'&&newPrefill?{start:newPrefill.start,end:newPrefill.end}:null;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0),text=((document.getElementById('fLabel')||{}).value||'').trim();
                    const badge=((document.getElementById('fCourseBadge')||{}).value||'').trim().slice(0,24);
                    let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}else if(old){start=old.start;end=old.end}else if(pre){start=pre.start;end=pre.end}
                    const result=oldSubmit.call(this,e);
                    try{
                      const arr=weeks[week]&&weeks[week][day]?weeks[week][day].courses:[];
                      let target=arr.find(c=>c.start===start&&c.end===end&&c.label===text);if(!target)target=arr.find(c=>c.start===start&&c.end===end);
                      if(target){target.badge=badge;if(typeof save==='function')save()}
                      setTimeout(decorateCourseBadges,0);
                    }catch(ex){}
                    return result;
                  };
                  wrapped.__courseBadgeWrappedV9=true;form.onsubmit=wrapped;
                }

                function setPill(holder,badge){
                  if(!holder)return;let pill=holder.querySelector(':scope > .courseBadgePill');
                  if(badge){if(!pill){pill=document.createElement('span');pill.className='courseBadgePill';holder.appendChild(pill)}if(pill.textContent!==badge)pill.textContent=badge}
                  else if(pill)pill.remove();
                }
                function decorateCourseBadges(){
                  try{
                    if(typeof weeks==='undefined')return;
                    const td=typeof todayKey==='function'?todayKey():2,today=weeks[currentWeek]&&weeks[currentWeek][td]?weeks[currentWeek][td].courses:[];
                    document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{
                      const start=((row.querySelector('.time strong')||{}).textContent||'').trim();const c=today.find(x=>x.start===start);setPill(row.querySelector('.label'),courseBadge(c));
                    });
                    const grid=document.getElementById('weekGrid');
                    if(grid&&typeof state!=='undefined'){
                      const kids=Array.from(grid.children);for(let p=6;p+5<kids.length;p+=6){
                        const timeText=(kids[p].textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g);if(!timeText||!timeText.length)continue;const start=timeText[0];
                        for(let i=0;i<5;i++){const cell=kids[p+1+i];if(!cell||!cell.classList.contains('has'))continue;const c=state[DAYS[i]].courses.find(x=>x.start===start);setPill(cell.querySelector('.cellLabel'),courseBadge(c))}
                      }
                    }
                    if(typeof state!=='undefined'&&state[selected])document.querySelectorAll('#editList .editCourse').forEach((row,i)=>{const c=state[selected].courses[i];setPill(row.querySelector('.label'),courseBadge(c))});
                  }catch(e){}
                }
                window.decorateCourseBadges=decorateCourseBadges;

                function renderTodayDynamic(){
                  state=weeks[currentWeek];
                  const d=todayKey(),now=new Date(),list=state[d].courses,cur=currentCourse(d);
                  $('todayTitle').textContent=FULL[d]+' · semaine '+currentWeek;
                  $('todayDate').textContent=now.toLocaleDateString('fr-FR',{day:'numeric',month:'long'});
                  if($('todayClock')){$('todayClock').textContent='';$('todayClock').style.display='none'}
                  $('todayProgress').style.width=progressPercent()+'%';
                  $('scaleStart').textContent=list[0]?.start||slots[0].start;
                  $('scaleEnd').textContent=list[list.length-1]?.end||slots[slots.length-1].end;
                  const box=$('todayList');box.innerHTML='';
                  if(!list.length){box.innerHTML='<div class="empty">Aucun cours aujourd’hui.</div>';return}
                  const events=list.map(c=>({type:'course',start:min(c.start),end:min(c.end),course:c}));
                  for(const g of gapsForDay(list))events.push({type:'gap',start:g.start,end:g.end});
                  const l=lunchForDay(list);if(l)events.push({type:'lunch',start:l.startM,end:l.endM,l});
                  events.sort((a,b)=>a.start-b.start||a.end-b.end);
                  for(const ev of events){
                    const row=document.createElement('div');
                    if(ev.type==='course'){
                      const c=ev.course;row.className='todayCourse'+(cur===c?' current':'');
                      row.innerHTML=`<div class="time"><strong>${esc(c.start)}</strong><br>${esc(c.end)}</div><div><div class="label">${esc(c.label)}${badgeHtml(c)}</div><div class="room">salle ${esc(c.room||'—')}${c.slot?` · heure ${c.slot}`:''}</div></div>${cur===c?'<div class="badge">En cours</div>':''}`;
                      row.onclick=()=>{activeWeek=currentWeek;selected=d;editing=state[d].courses.indexOf(c);openEditor(editing)};
                    }else if(ev.type==='gap'){
                      const label=gapLabelText(),labelHtml=label?'<div class="label">'+esc(label)+'</div>':'';
                      row.className='todayCourse gap';
                      row.innerHTML=`<div class="time"><strong>${clock(ev.start)}</strong><br>${clock(ev.end)}</div><div>${labelHtml}<div class="room">${durationLabel(ev.end-ev.start)} sans cours</div></div>`;
                    }else{
                      const label=lunchLabelText(),labelHtml=label?'<div class="label">'+esc(label)+'</div>':'';
                      row.className='todayCourse lunch';
                      row.innerHTML=`<div class="time"><strong>${esc(ev.l.start)}</strong><br>${esc(ev.l.end)}</div><div>${labelHtml}</div>`;
                    }
                    box.appendChild(row);
                  }
                  syncBreakCells();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors();
                }

                function appendRegularWeekRow(box,t){
                  const th=document.createElement('div');th.className='wh timecol';th.innerHTML=`${esc(t.start)}<br>${esc(t.end)}`;box.appendChild(th);
                  for(const d of DAYS){
                    const c=state[d].courses.find(x=>x.start===t.start&&x.end===t.end),cell=document.createElement('div');
                    if(c){
                      cell.className='wc has';cell.innerHTML=`<div><div class="cellLabel">${esc(c.label)}${badgeHtml(c)}</div><div class="cellRoom">${esc(c.room||'—')}</div></div>`;
                      cell.onclick=()=>{selected=d;editing=state[d].courses.indexOf(c);openEditor(editing)};
                    }else{
                      const g=gapsForDay(state[d].courses).find(x=>Math.min(min(t.end),x.end)>Math.max(min(t.start),x.start));
                      if(g){const label=gapLabelText();cell.className='wc gapCell';cell.innerHTML=label?`<div class="cellLabel breakFitLabel">${esc(label)}</div>`:''}
                      else cell.className='wc emptyCell';
                      cell.onclick=()=>{selected=d;newPrefill={start:t.start,end:t.end};openEditor(null)};
                    }
                    box.appendChild(cell);
                  }
                }

                function appendLunchBandRow(box){
                  const bounds=lunchBounds(),th=document.createElement('div');
                  th.className='wh timecol';th.innerHTML=`${clock(bounds.start)}<br>${clock(bounds.end)}`;box.appendChild(th);
                  const label=lunchLabelText();
                  for(const d of DAYS){
                    const info=lunchForDay(state[d].courses),cell=document.createElement('div');
                    if(!info){cell.className='wc emptyCell dynamicLunchEmpty';box.appendChild(cell);continue}
                    cell.className='wc lunchCell dynamicLunchCell';
                    const overlay=document.createElement('div');overlay.className='dynamicLunchOverlay';
                    overlay.innerHTML=label?`<span class="cellLabel breakFitLabel">${esc(label)}</span>`:'';
                    cell.appendChild(overlay);box.appendChild(cell);
                  }
                }

                function renderWeekDynamic(){
                  state=weeks[activeWeek];$('weekTitleLetter').textContent=activeWeek;
                  const box=$('weekGrid'),times=dynamicWeekTimes();box.innerHTML='';
                  const corner=document.createElement('div');corner.className='wh timecol';corner.textContent='H';box.appendChild(corner);
                  for(const d of DAYS){const h=document.createElement('div');h.className='wh day';h.textContent=NAMES[d];box.appendChild(h)}
                  for(const t of times){if(t.type==='lunchDynamic')appendLunchBandRow(box);else appendRegularWeekRow(box,t)}
                  syncBreakCells();fitBreakLabels();polishWeekNowMarker();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors();
                }

                function bindOverrides(){
                  window.uniqueWeekTimes=dynamicWeekTimes;window.renderToday=renderTodayDynamic;window.renderWeek=renderWeekDynamic;window.renderBreakSettings=renderBreakSettingsV9;window.applyBreakSettings=applyBreakSettingsV9;
                  try{uniqueWeekTimes=dynamicWeekTimes;renderToday=renderTodayDynamic;renderWeek=renderWeekDynamic;renderBreakSettings=renderBreakSettingsV9;applyBreakSettings=applyBreakSettingsV9}catch(e){}
                }

                bindOverrides();wireBreakSettings();ensureCourseBadgeField();wrapCourseSubmit();
                const modal=document.getElementById('modal');
                if(modal)new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{syncCourseBadgeField();wrapCourseSubmit()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});
                for(const id of ['todayList','weekGrid','editList']){const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(()=>{syncBreakCells();decorateCourseBadges();fitBreakLabels()},0)).observe(el,{childList:true,subtree:true})}
                window.addEventListener('resize',()=>setTimeout(()=>{fitBreakLabels();polishWeekNowMarker();decorateCourseBadges()},30));
                setInterval(()=>setTimeout(polishWeekNowMarker,45),60000);

                function refresh(){
                  try{
                    bindOverrides();wireBreakSettings();ensureCourseBadgeField();wrapCourseSubmit();
                    syncBreakCells();fitBreakLabels();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors();polishWeekNowMarker();
                  }catch(e){}
                }
                window.refreshLunchBreakUi=refresh;
                refresh();
              }catch(e){console.log('Lunch break UI',e)}
            })();
            """;
    }

    // Former DoubleLunchUi; isolated to stay below JVM constant limits.
    private static String layer8() {
        return """
            (function(){
              try{
                if(window.__weekGeometryV14){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__weekGeometryV14=true;
                document.documentElement.setAttribute('data-week-geometry','V14');

                const style=document.createElement('style');
                style.id='weekGeometryV14Style';
                style.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:49px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  }

                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,
                  #weekGrid #weekNowRailV8,#weekGrid #weekNowDotV8,
                  #weekGrid #finalWeekNowRailV12,#weekGrid #finalWeekNowDotV12,
                  #weekGrid #finalWeekNowRailV13,#weekGrid #finalWeekNowDotV13,
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,
                  #weekGrid .geoLunchLabel{display:none!important}

                  html body #weekGrid#weekGrid .wc.lunchCell,
                  html body #weekGrid#weekGrid .wc.dynamicLunchCell,
                  html body #weekGrid#weekGrid .wc.nativeLunchCell,
                  html body #weekGrid#weekGrid .wc.lunchCell:not(.dynamicLunchCell){
                    box-sizing:border-box!important;
                    margin:0!important;
                    padding:0!important;
                    border-left:0!important;
                    border-top:0!important;
                    border-radius:0!important;
                    outline:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    background-clip:border-box!important;
                    color:var(--ft-midi-ink,#22283A)!important;
                    overflow:hidden!important;
                    position:relative!important;
                  }
                  html body #weekGrid#weekGrid .nativeLunchLabel{
                    position:absolute!important;inset:0!important;
                    display:flex!important;align-items:center!important;justify-content:center!important;
                    gap:4px!important;margin:0!important;padding:2px 3px!important;
                    border:0!important;outline:0!important;box-shadow:none!important;background:transparent!important;
                    box-sizing:border-box!important;white-space:nowrap!important;overflow:hidden!important;
                    color:var(--ft-midi-ink,#22283A)!important;font-weight:850!important;font-size:.60rem!important;line-height:1!important;
                    pointer-events:none!important;z-index:3!important;
                  }
                  html body #weekGrid#weekGrid .nativeLunchIcon{font-size:.84em!important;line-height:1!important}

                  #weekGrid .nativeNowFull,
                  #weekGrid .nativeNowPartial{
                    position:absolute!important;
                    left:-1.5px!important;
                    width:2px!important;
                    background:#1688F4!important;
                    z-index:140!important;
                    pointer-events:none!important;
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                  }
                  #weekGrid .nativeNowFull{top:-1px!important;bottom:-1px!important}
                  #weekGrid .nativeNowPartial{top:-1px!important}
                  #weekGrid .nativeNowDot{
                    position:absolute!important;
                    left:-.5px!important;
                    width:16px!important;height:16px!important;border-radius:50%!important;
                    transform:translate(-50%,-50%)!important;
                    background:#1688F4!important;border:4px solid #D9ECFF!important;
                    box-sizing:border-box!important;box-shadow:0 1px 4px #0B6ACB38!important;
                    z-index:141!important;pointer-events:none!important;
                  }
                  #weekGrid .wc{position:relative!important}
                  html body #weekGrid#weekGrid .wc.nativeNowTrackCell{overflow:visible!important;z-index:5!important}

                  .dualBreakInputs{display:grid;grid-template-columns:72px minmax(0,1fr);gap:5px 7px;align-items:center}
                  .dualBreakInputs .dualLabel{font-size:.66rem;color:var(--muted);font-weight:750}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important;align-items:start!important}
                  #courseWidgetLabelField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                let raf=0;
                let lateTimer=0;
                let syncing=false;
                let gridObserver=null;

                function keepStyleLast(){}

                function toMin(v){
                  const p=String(v||'').split(':').map(Number);
                  return (p[0]||0)*60+(p[1]||0);
                }

                function lunchText(){
                  try{
                    if(typeof lunchLabelText==='function')return String(lunchLabelText()||'').trim();
                    if(typeof breaks!=='undefined'&&breaks)return String(breaks.lunchLabel||'Midi').trim();
                  }catch(e){}
                  return 'Midi';
                }

                function rowsOf(grid){
                  const rows=[];
                  if(!grid)return rows;
                  const times=Array.from(grid.querySelectorAll(':scope > .wh.timecol'));
                  for(const time of times){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(found.length<2)continue;
                    const cells=[];
                    const dayCount=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.length:5;
                    let n=time.nextElementSibling;
                    while(n&&cells.length<dayCount){
                      if(n.classList&&n.classList.contains('wc'))cells.push(n);
                      n=n.nextElementSibling;
                    }
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);
                  return rows;
                }

                function clearMidiEdges(grid){
                  grid.querySelectorAll('[data-midi-edge-v14="1"]').forEach(el=>{
                    el.style.removeProperty('border-right-color');
                    el.style.removeProperty('border-bottom-color');
                    el.removeAttribute('data-midi-edge-v14');
                  });
                }

                function edge(el,prop,color){
                  if(!el)return;
                  el.style.setProperty(prop,color,'important');
                  el.setAttribute('data-midi-edge-v14','1');
                }

                function normalizeLunch(cell,label){
                  if(!cell)return;
                  cell.classList.add('nativeLunchCell');
                  cell.querySelectorAll('.geoLunchLabel').forEach(x=>x.remove());
                  const holder=cell.querySelector('.dynamicLunchOverlay')||cell;
                  let text=holder.querySelector('.cellLabel');
                  if(label){
                    if(!text){text=document.createElement('span');text.className='cellLabel breakFitLabel';holder.appendChild(text)}
                    if(text.textContent!==label)text.textContent=label;
                  }else if(text){text.remove()}
                  cell.style.setProperty('background','var(--ft-midi)','important');
                  cell.style.setProperty('box-shadow','none','important');
                  cell.style.setProperty('outline','0','important');
                  cell.style.setProperty('border-left','0','important');
                  cell.style.setProperty('border-top','0','important');
                  cell.style.setProperty('border-radius','0','important');
                  cell.style.setProperty('padding','0','important');
                  cell.style.setProperty('margin','0','important');
                }

                function paintLunch(grid,rows){
                  const label=lunchText();
                  clearMidiEdges(grid);
                  rows.forEach(row=>row.cells.forEach(cell=>{
                    if(!(cell.classList.contains('lunchCell')||cell.classList.contains('dynamicLunchCell')||cell.classList.contains('nativeLunchCell')))return;
                    normalizeLunch(cell,label);
                    cell.style.removeProperty('border-right-color');
                    cell.style.removeProperty('border-bottom-color');
                    cell.style.setProperty('box-shadow','none','important');
                    cell.style.setProperty('outline','0','important');
                  }));
                }

                function removeLegacyNow(grid){
                  grid.querySelectorAll('[id*="weekNow"],[id*="WeekNow"],.scheduleNowRail,.scheduleNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('.nativeNowFull,.nativeNowPartial,.nativeNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('.nativeNowTrackCell').forEach(x=>x.classList.remove('nativeNowTrackCell'));
                }

                function paintNow(grid,rows){
                  // 6.9: the final layer owns the only current-time indicator.
                  removeLegacyNow(grid);
                }

                function observeGrid(grid){
                  if(gridObserver)gridObserver.disconnect();
                  gridObserver=new MutationObserver(()=>{if(!syncing)scheduleGrid()});
                  gridObserver.observe(grid,{childList:true,subtree:false});
                }

                function syncGrid(){
                  const grid=document.getElementById('weekGrid');if(!grid||syncing)return;
                  syncing=true;
                  try{
                    keepStyleLast();
                    grid.style.setProperty('position','relative','important');
                    const rows=rowsOf(grid);
                    if(rows.length){paintLunch(grid,rows);paintNow(grid,rows)}
                    observeGrid(grid);
                  }finally{syncing=false}
                }

                function scheduleGrid(){
                  if(raf)cancelAnimationFrame(raf);
                  if(lateTimer)clearTimeout(lateTimer);
                  syncGrid();
                }
                window.refreshDoubleLunchUi=scheduleGrid;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function keyFor(week,day,start,end){return String(week||'A')+'|'+String(day||2)+'|'+String(start||'')+'|'+String(end||'')}

                function ensureCourseWidgetField(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseWidgetLabelField');if(field)return;
                  field=document.createElement('div');field.id='courseWidgetLabelField';field.className='field';
                  field.innerHTML='<label>Intitulé dans le widget (facultatif)</label><input id="fWidgetLabel" type="text" maxlength="80" placeholder="Vide = même intitulé que dans l\'application">';
                  const app=document.getElementById('fLabel'),appField=app?app.closest('.field'):null;
                  if(appField&&appField.nextSibling)appField.parentNode.insertBefore(field,appField.nextSibling);
                  else form.insertBefore(field,form.querySelector('.sheetActions'));
                }

                function editedCourse(){
                  try{if(typeof editing==='undefined'||editing==null)return null;return weeks[activeWeek][selected].courses[editing]||null}catch(e){return null}
                }

                function fillCourseWidgetField(){
                  ensureCourseWidgetField();const input=document.getElementById('fWidgetLabel');if(!input)return;
                  const c=editedCourse();if(!c){input.value='';return}
                  const a=loadAdv(),map=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                  input.value=String(map[keyFor(activeWeek,selected,c.start,c.end)]||'');
                }

                function wrapCourseSubmit(){
                  ensureCourseWidgetField();
                  const form=document.getElementById('courseForm');if(!form||!form.onsubmit||form.onsubmit.__widgetLabelsV14)return;
                  const old=form.onsubmit;
                  const wrapped=function(e){
                    const c=editedCourse(),oldKey=c?keyFor(activeWeek,selected,c.start,c.end):null;
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A',day=typeof selected!=='undefined'?selected:2;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0);let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}
                    else if(c){start=c.start;end=c.end}else if(typeof newPrefill!=='undefined'&&newPrefill){start=newPrefill.start;end=newPrefill.end}
                    const widgetLabel=String((document.getElementById('fWidgetLabel')||{}).value||'').trim();
                    const result=old.call(this,e);
                    try{
                      const a=loadAdv();a.widgetCourseLabels=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                      if(oldKey)delete a.widgetCourseLabels[oldKey];const newKey=keyFor(week,day,start,end);
                      if(widgetLabel)a.widgetCourseLabels[newKey]=widgetLabel;else delete a.widgetCourseLabels[newKey];saveAdv(a);
                    }catch(ex){}
                    return result;
                  };
                  wrapped.__widgetLabelsV14=true;form.onsubmit=wrapped;
                }

                function installBreakWidgetInputs(){
                  const defs=[['gapLabel','gapWidgetLabel'],['lunchLabel','lunchWidgetLabel']];const adv=loadAdv();
                  for(const [appId,key] of defs){
                    const app=document.getElementById(appId);if(!app)continue;
                    let host=app.closest('.dualBreakInputs'),widget=document.getElementById(appId+'Widget');
                    if(!host){
                      host=document.createElement('div');host.className='dualBreakInputs';const parent=app.parentElement;parent.insertBefore(host,app);
                      const aLabel=document.createElement('div');aLabel.className='dualLabel';aLabel.textContent='Application';
                      const wLabel=document.createElement('div');wLabel.className='dualLabel';wLabel.textContent='Widget';host.append(aLabel,app,wLabel);
                      widget=document.createElement('input');widget.id=appId+'Widget';widget.type='text';widget.maxLength=35;widget.placeholder='Vide = même intitulé';host.appendChild(widget);
                    }
                    if(widget&&document.activeElement!==widget)widget.value=String(adv[key]||'');
                    if(widget&&!widget.__labelV14){
                      widget.__labelV14=true;const persist=()=>{const a=loadAdv();a[key]=String(widget.value||'').trim();saveAdv(a)};
                      widget.addEventListener('change',persist);widget.addEventListener('blur',persist);
                    }
                  }
                }

                function installLabelUi(){
                  ensureCourseWidgetField();wrapCourseSubmit();installBreakWidgetInputs();
                  const modal=document.getElementById('modal');
                  if(modal&&!modal.__widgetLabelsV14){
                    modal.__widgetLabelsV14=true;
                    new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{fillCourseWidgetField();wrapCourseSubmit()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});
                  }
                }

                const grid=document.getElementById('weekGrid');
                if(grid){observeGrid(grid);if(window.ResizeObserver)new ResizeObserver(scheduleGrid).observe(grid)}
                window.addEventListener('resize',scheduleGrid);
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)scheduleGrid()});
                if(document.fonts&&document.fonts.ready)document.fonts.ready.then(scheduleGrid);

                installLabelUi();
                scheduleGrid();
                syncGrid();
                setInterval(()=>{if(window.paintWeek69)window.paintWeek69()},30000);
              }catch(e){console.log('Week geometry V14',e)}
            })();
            """;
    }

    // Former BulkCourseUi; isolated to stay below JVM constant limits.
    private static String layer9() {
        return """
            (function(){
              try{
                const DAYS=[2,3,4,5,6], DN={2:'Lun',3:'Mar',4:'Mer',5:'Jeu',6:'Ven'};
                const deep=o=>JSON.parse(JSON.stringify(o));
                function loadRoot(){try{return JSON.parse(AndroidSchedule.loadSchedule()||'{}')}catch(e){return {}}}
                function saveRoot(root){try{AndroidSchedule.saveSchedule(JSON.stringify(root));if(window.reloadSchedule)window.reloadSchedule()}catch(e){alert('Impossible d’enregistrer.')}}
                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function oneWeek(){return loadAdv().singleWeek===true}
                function ensureWeek(root,w){root._weeks=root._weeks||{};root._weeks[w]=root._weeks[w]||{};DAYS.forEach(d=>{root._weeks[w][String(d)]=root._weeks[w][String(d)]||{enabled:true,courses:[]};root._weeks[w][String(d)].courses=root._weeks[w][String(d)].courses||[]})}
                function syncOne(root){ensureWeek(root,'A');root._weeks.B=deep(root._weeks.A);root._weeks.C=deep(root._weeks.A);root._weeks.D=deep(root._weeks.A);root._currentWeek='A';return root}
                function activeWeek(root){if(oneWeek())return 'A';const b=document.querySelector('#weekTabs .weekTab.active');return b?.dataset?.week||root._currentWeek||'A'}
                function selDay(){try{if([2,3,4,5,6].includes(Number(selected)))return Number(selected)}catch(e){};return 2}
                function slots(root){return Array.isArray(root._slots)&&root._slots.length?root._slots:[{start:'08:00',end:'09:00'},{start:'09:00',end:'10:00'},{start:'10:00',end:'11:00'},{start:'11:00',end:'12:00'},{start:'13:00',end:'14:00'},{start:'14:00',end:'15:00'},{start:'16:00',end:'17:00'}]}
                function mins(t){const p=String(t||'0:0').split(':').map(Number);return p[0]*60+p[1]}
                function firstFree(root,w,d){ensureWeek(root,w);const used=new Set(root._weeks[w][String(d)].courses.map(c=>Number(c.slot)).filter(Boolean));for(let i=1;i<=slots(root).length;i++)if(!used.has(i))return i;return 1}

                function style(){if(document.getElementById('bulkFixedStyle'))return;const s=document.createElement('style');s.id='bulkFixedStyle';s.textContent=`
                  #addBulkCourses{display:block!important;width:100%;margin-top:7px;padding:9px;border:1.5px solid var(--blue,#0877f9);border-radius:7px;background:#edf6ff;color:var(--blue,#0877f9);font-weight:800}
                  #weekModeBar{display:flex;align-items:center;gap:6px;margin:0 0 8px;padding:6px 8px;border:1px solid var(--line,#dce3eb);border-radius:8px;background:#fff}.weekModeLabel{font-size:.72rem;font-weight:800;white-space:nowrap}.weekModeChoices{display:flex;gap:4px;flex:1}.weekModeChoice{flex:1;border:1px solid #cfd9e5;border-radius:999px;background:#fff;padding:6px 4px;font-size:.68rem;font-weight:800;color:#4d5667}.weekModeChoice.active{background:var(--blue,#0877f9);border-color:var(--blue,#0877f9);color:#fff}
                  #bulkModalFixed{position:fixed;inset:0;z-index:220;background:#0b17386b;display:none;align-items:flex-end}#bulkModalFixed.show{display:flex}#bulkSheetFixed{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:18px 18px 0 0;padding:14px 12px calc(14px + env(safe-area-inset-bottom));max-height:92vh;overflow:auto;color:var(--ink,#111936)}#bulkSheetFixed h3{text-align:center;margin:0 0 10px}.bulkClass{margin-bottom:8px}.bulkClass label{display:block;color:#68738a;font-size:.72rem;margin-bottom:3px}.bulkClass input{width:100%;border:1px solid #dce3eb;border-radius:7px;padding:9px}.bulkRows{border:1px solid #dce3eb;border-radius:8px;overflow:hidden}.bulkRow{display:grid;grid-template-columns:66px minmax(0,1fr) 78px 28px;gap:5px;align-items:center;padding:7px;border-top:1px solid #e9edf2}.bulkRow:first-child{border-top:0}.bulkRow select,.bulkRow input{width:100%;min-width:0;border:1px solid #dce3eb;border-radius:6px;padding:7px 4px;background:#fff;font-size:.70rem}.bulkRm{border:0;background:#fff1f3;color:#c6284e;border-radius:50%;width:28px;height:28px;font-size:18px}.bulkMore{width:100%;margin-top:7px;padding:8px;border:1px dashed var(--blue,#0877f9);border-radius:7px;background:#fff;color:var(--blue,#0877f9);font-weight:800}.bulkActions{display:flex;justify-content:flex-end;gap:7px;margin-top:10px}.singleWeekMode #weekTabs{display:none!important}.singleWeekMode #weekTitleLetter{display:none!important}`;document.head.appendChild(s)}

                function ensureButton(){style();const base=document.getElementById('addCourse');if(!base)return;let b=document.getElementById('addBulkCourses');if(!b){b=document.createElement('button');b.id='addBulkCourses';b.type='button';b.textContent='＋ Ajouter plusieurs cours à une classe';base.insertAdjacentElement('afterend',b)}b.onclick=openBulk}
                function ensureSingleState(){if(!oneWeek())return;try{currentWeek='A';activeWeek='A';if(typeof weeks!=='undefined'&&weeks.A)state=weeks.A}catch(e){}}
                function repairWeek(force){
                  if(!oneWeek())return;
                  const view=document.getElementById('viewWeek');if(!view||!view.classList.contains('active'))return;
                  ensureSingleState();
                  const grid=document.getElementById('weekGrid');
                  const needsRender=force===true||!grid||grid.children.length<6;
                  if(needsRender){
                    try{if(typeof renderWeek==='function')renderWeek();else if(typeof render==='function')render()}catch(e){}
                  }
                  if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                }
                function bindWeekRepair(){
                  const nav=document.querySelector('.nav[data-mode="week"]');
                  if(nav&&!nav.dataset.singleWeekRepair){
                    nav.dataset.singleWeekRepair='1';
                    nav.addEventListener('click',()=>setTimeout(()=>repairWeek(true),30));
                  }
                }
                function setMode(n){
                  const a=loadAdv();a.singleWeek=n===1;a.cycleLength=n===3?3:2;saveAdv(a);
                  if(n===1){
                    const r=syncOne(loadRoot());saveRoot(r);
                    try{AndroidSchedule.setCurrentWeek('A')}catch(e){}
                    ensureSingleState();
                  }else{
                    try{if(window.reloadSchedule)window.reloadSchedule()}catch(e){}
                  }
                  setTimeout(()=>{
                    if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                    ensureSingleState();
                    if(typeof render==='function')render();
                    refresh();
                  },140)
                }
                function ensureModeBar(){const edit=document.getElementById('viewEdit');if(!edit)return;let bar=document.getElementById('weekModeBar');if(!bar){bar=document.createElement('div');bar.id='weekModeBar';bar.innerHTML='<span class="weekModeLabel">Semaines :</span><div class="weekModeChoices"><button type="button" class="weekModeChoice" data-m="1">1 seule</button><button type="button" class="weekModeChoice" data-m="2">A / B</button><button type="button" class="weekModeChoice" data-m="3">A / B / C</button></div>';const anchor=document.getElementById('importStatus');anchor.insertAdjacentElement('afterend',bar);bar.querySelectorAll('button').forEach(b=>b.onclick=()=>{const target=Number(b.dataset.m);bar.querySelectorAll('button').forEach(x=>x.classList.toggle('active',x===b));requestAnimationFrame(()=>setMode(target))})}const a=loadAdv(),m=a.singleWeek===true?1:(Number(a.cycleLength)>=3?3:2);bar.querySelectorAll('button').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===m))}
                function applySingleUi(){const one=oneWeek();document.documentElement.classList.toggle('singleWeekMode',one);if(!one)return;ensureSingleState();const cw=document.getElementById('currentWeekBtn');if(cw){cw.innerHTML='Semaine unique';cw.onclick=()=>{}}const e=document.getElementById('editDayTitle');if(e)e.textContent=(e.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');const t=document.getElementById('todayTitle');if(t)t.textContent=(t.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');const wh=document.querySelector('.weekTop h2');if(wh)wh.textContent='Aperçu semaine'}
                function wrapSave(){if(typeof window.save!=='function'||window.save.__oneWrapped)return;const old=window.save;const f=function(){if(oneWeek()){try{weeks.B=deep(weeks.A);if(weeks.C)weeks.C=deep(weeks.A);if(weeks.D)weeks.D=deep(weeks.A);currentWeek='A';activeWeek='A';state=weeks.A}catch(e){}}return old.apply(this,arguments)};f.__oneWrapped=true;window.save=f}

                function dayOpts(v){return DAYS.map(d=>`<option value="${d}" ${d===Number(v)?'selected':''}>${DN[d]}</option>`).join('')}
                function slotOpts(root,v){return slots(root).map((s,i)=>`<option value="${i+1}" ${i+1===Number(v)?'selected':''}>${i+1}e · ${s.start}–${s.end}</option>`).join('')}
                function addRow(root,w,d,sl,room=''){const box=document.getElementById('bulkRowsFixed');const row=document.createElement('div');row.className='bulkRow';row.innerHTML=`<select class="bd">${dayOpts(d)}</select><select class="bs">${slotOpts(root,sl)}</select><input class="br" placeholder="Salle" maxlength="20"><button type="button" class="bulkRm">×</button>`;row.querySelector('.br').value=room;row.querySelector('.bulkRm').onclick=()=>{if(box.children.length>1)row.remove()};row.querySelector('.bd').onchange=()=>{const r=loadRoot(),ww=activeWeek(r),dd=Number(row.querySelector('.bd').value);row.querySelector('.bs').innerHTML=slotOpts(r,firstFree(r,ww,dd))};box.appendChild(row)}
                function ensureModal(){let m=document.getElementById('bulkModalFixed');if(m)return m;m=document.createElement('div');m.id='bulkModalFixed';m.innerHTML='<form id="bulkSheetFixed"><h3>Ajouter plusieurs cours à une classe</h3><div class="bulkClass"><label>Classe / groupe</label><input id="bulkLabelFixed" required maxlength="80"></div><div id="bulkRowsFixed" class="bulkRows"></div><button id="bulkMoreFixed" type="button" class="bulkMore">＋ Ajouter un autre jour / horaire</button><div class="bulkActions"><button type="button" id="bulkCancelFixed" class="btn">Annuler</button><button type="submit" class="btn primary">Ajouter tous les cours</button></div></form>';document.body.appendChild(m);document.getElementById('bulkCancelFixed').onclick=()=>m.classList.remove('show');m.onclick=e=>{if(e.target===m)m.classList.remove('show')};document.getElementById('bulkMoreFixed').onclick=()=>{const r=loadRoot(),w=activeWeek(r),rows=[...document.querySelectorAll('#bulkRowsFixed .bulkRow')],last=rows.at(-1),d=last?DAYS[(DAYS.indexOf(Number(last.querySelector('.bd').value))+1)%DAYS.length]:selDay();addRow(r,w,d,firstFree(r,w,d))};document.getElementById('bulkSheetFixed').onsubmit=e=>{e.preventDefault();let r=loadRoot(),w=activeWeek(r);ensureWeek(r,w);const label=document.getElementById('bulkLabelFixed').value.trim();if(!label)return;const ss=slots(r),seen=new Set(),adds=[],errs=[];document.querySelectorAll('#bulkRowsFixed .bulkRow').forEach(row=>{const d=Number(row.querySelector('.bd').value),n=Number(row.querySelector('.bs').value),room=row.querySelector('.br').value.trim(),s=ss[n-1];if(!s)return;const k=d+'|'+n;if(seen.has(k)){errs.push(DN[d]+' '+s.start+' : doublon');return}seen.add(k);const list=r._weeks[w][String(d)].courses;const occupied=list.some(c=>mins(c.start)<mins(s.end)&&mins(c.end)>mins(s.start));if(occupied){errs.push(DN[d]+' '+s.start+' : déjà occupé');return}adds.push({d,c:{start:s.start,end:s.end,label,room,slot:n}})});if(errs.length){alert(errs.join(' • '));return}adds.forEach(x=>r._weeks[w][String(x.d)].courses.push(x.c));if(oneWeek())r=syncOne(r);m.classList.remove('show');saveRoot(r)};return m}
                function openBulk(){const r=loadRoot(),w=activeWeek(r),m=ensureModal();ensureWeek(r,w);document.getElementById('bulkLabelFixed').value='';document.getElementById('bulkRowsFixed').innerHTML='';const d=selDay();addRow(r,w,d,firstFree(r,w,d));m.classList.add('show')}

                function refresh(){ensureSingleState();ensureButton();ensureModeBar();wrapSave();bindWeekRepair();applySingleUi()}
                window.openBulkCourses=openBulk;window.refreshBulkCourseUi=refresh;refresh();setTimeout(refresh,100);setTimeout(refresh,500);
              }catch(e){console.log('BulkCourseUi',e)}
            })();
            """;
    }

}
