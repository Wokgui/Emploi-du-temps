package com.wokgui.schedulewidget;

final class FinalPolishUi {
    private FinalPolishUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__finalPolishV1){
                  if(window.refreshFinalPolish)window.refreshFinalPolish();
                  return;
                }
                window.__finalPolishV1=true;
                const APP_VERSION='6.5';
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
                  #weekGrid>.wh.timecol{border-right:2px solid #cbd5e1!important}
                  #weekGrid>.wh.day{border-bottom:2px solid #cbd5e1!important}
                  #weekGrid>.wh.timecol:first-child{border-bottom:2px solid #cbd5e1!important}

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
                  if(choices.querySelectorAll('.weekModeChoice').length!==4){
                    choices.innerHTML='';
                    wanted.forEach(([v,label])=>{const b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;b.textContent=label;b.onclick=()=>chooseCycle(Number(v));choices.appendChild(b)});
                  }
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
                  const border=(getComputedStyle(document.documentElement).getPropertyValue('--ft-midi-border')||'#C7AA62').trim();
                  grid.querySelectorAll('.finalLunchCell').forEach(c=>{c.classList.remove('finalLunchCell','finalLunchJoinedRight');c.style.removeProperty('box-shadow');c.style.removeProperty('border-right-color')});
                  for(const row of rows){
                    const flags=row.cells.map((c,i)=>shouldLunch(row,i)&&(c.classList.contains('lunchCell')||c.classList.contains('dynamicLunchCell')||c.classList.contains('nativeLunchCell')));
                    let i=0;
                    while(i<flags.length){if(!flags[i]){i++;continue}let j=i;while(j+1<flags.length&&flags[j+1])j++;
                      for(let k=i;k<=j;k++){
                        const cell=row.cells[k];cell.classList.add('finalLunchCell');
                        const shadows=['inset 0 2px 0 '+border,'inset 0 -2px 0 '+border];
                        if(k===i)shadows.push('inset 2px 0 0 '+border);if(k===j)shadows.push('inset -2px 0 0 '+border);
                        cell.style.setProperty('box-shadow',shadows.join(','),'important');
                        if(k<j){cell.classList.add('finalLunchJoinedRight');cell.style.setProperty('border-right-color','transparent','important')}
                      }
                      i=j+1;
                    }
                  }
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
                const grid=document.getElementById('weekGrid');if(grid){new MutationObserver(()=>requestAnimationFrame(paintWeek)).observe(grid,{childList:true,subtree:false})}
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)paintWeek()});
                setInterval(paintWeek,15000);
                refresh();[40,120,300,700,1400].forEach(ms=>setTimeout(refresh,ms));
              }catch(e){console.log('FinalPolishUi',e)}
            })();
            """;
    }
}
