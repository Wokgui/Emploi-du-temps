package com.wokgui.schedulewidget;

final class WeekViewStabilityUi {
    private WeekViewStabilityUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekViewStabilityV5){
                  if(window.refreshWeekViewStability)window.refreshWeekViewStability();
                  return;
                }
                window.__weekViewStabilityV5=true;

                const APP_VERSION='5.9';
                let fixingCycle=false;
                function clone(o){return JSON.parse(JSON.stringify(o))}
                function loadAdv(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}
                  catch(e){return {}}
                }
                function saveAdvObj(o){
                  try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}
                }
                function oneWeek(){return loadAdv().singleWeek===true}
                function uiLang(){
                  try{const raw=AndroidSchedule.loadUiSettings();const o=JSON.parse(raw||'{}');return o.language==='de'||o.language==='en'?o.language:'fr'}catch(e){return 'fr'}
                }

                /*
                 * Final visual corrections for the week grid.
                 * - Midi fills the whole cell edge-to-edge, with no rounded inner card.
                 * - Trou keeps only the normal grid lines, with no extra rectangle.
                 * - The current-time rail sits exactly on the LEFT border of today's column.
                 */
                const polish=document.createElement('style');
                polish.textContent=`
                  html body #weekGrid .wc.lunchCell{padding:0!important;border-radius:0!important}
                  html body #weekGrid .wc.lunchCell:not(.dynamicLunchCell){
                    background:var(--ft-midi)!important;
                    color:var(--ft-midi-ink)!important;
                    box-shadow:inset 0 0 0 1px var(--ft-midi-border)!important;
                  }
                  html body #weekGrid .dynamicLunchCell{
                    padding:0!important;
                    background:var(--ft-midi)!important;
                    color:var(--ft-midi-ink)!important;
                    border-radius:0!important;
                    overflow:visible!important;
                    box-shadow:none!important;
                  }
                  html body #weekGrid .dynamicLunchOverlay{
                    left:0!important;right:0!important;top:0!important;bottom:0!important;
                    width:auto!important;height:auto!important;margin:0!important;padding:0!important;
                    background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;
                    border:0!important;border-radius:0!important;
                    box-shadow:inset 0 0 0 1px var(--ft-midi-border)!important;
                  }
                  html body #weekGrid .wc.gapCell{box-shadow:none!important;border-radius:0!important}
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot{transition:none!important}
                `;
                document.head.appendChild(polish);

                function minuteOf(t){
                  const p=String(t||'00:00').split(':').map(Number);
                  return (p[0]||0)*60+(p[1]||0);
                }

                function fixCurrentDayMarker(){
                  try{
                    const grid=document.getElementById('weekGrid');
                    const rail=document.getElementById('weekNowRail');
                    const dot=document.getElementById('weekNowDot');
                    if(!grid||!rail||!dot||typeof uniqueWeekTimes!=='function')return;
                    const now=new Date(),day=now.getDay();
                    if(day<1||day>5){rail.style.display=dot.style.display='none';return}
                    const times=uniqueWeekTimes();
                    const m=now.getHours()*60+now.getMinutes();
                    let row=-1,frac=0;
                    for(let i=0;i<times.length;i++){
                      const s=minuteOf(times[i].start),e=minuteOf(times[i].end);
                      if(m>=s&&m<=e){row=i;frac=(m-s)/Math.max(1,e-s);break}
                    }
                    const cells=Array.from(grid.querySelectorAll('.wc'));
                    if(row<0||!cells.length){rail.style.display=dot.style.display='none';return}
                    const dayIndex=day-1;
                    const first=cells[dayIndex],target=cells[row*5+dayIndex],last=cells[(times.length-1)*5+dayIndex];
                    if(!first||!target||!last)return;
                    // Exact column border: no +4px inset, so the marker stays out of the course text.
                    const left=target.offsetLeft;
                    const top=first.offsetTop+2;
                    const bottom=last.offsetTop+last.offsetHeight-2;
                    rail.style.display=dot.style.display='block';
                    rail.style.left=left+'px';rail.style.top=top+'px';rail.style.height=Math.max(2,bottom-top)+'px';
                    dot.style.left=left+'px';
                    dot.style.top=(target.offsetTop+Math.max(2,Math.min(target.offsetHeight-2,target.offsetHeight*frac)))+'px';
                  }catch(e){}
                }

                function ensureTitleSpan(){
                  const h=document.querySelector('.weekTop h2');
                  if(!h)return null;
                  let span=document.getElementById('weekTitleLetter');
                  if(!span){
                    h.innerHTML='Aperçu semaine <span id="weekTitleLetter" class="weekLetter">A</span>';
                    span=document.getElementById('weekTitleLetter');
                  }
                  return span;
                }

                function ensureSingleState(){
                  if(!oneWeek())return;
                  try{
                    currentWeek='A';
                    activeWeek='A';
                    if(typeof weeks!=='undefined'&&weeks.A)state=weeks.A;
                  }catch(e){}
                }

                function paintBadge(){
                  const cw=document.getElementById('currentWeekBtn');
                  if(!cw)return;
                  if(oneWeek()){
                    if(cw.textContent!=='Semaine unique')cw.textContent='Semaine unique';
                    cw.setAttribute('aria-label','Semaine unique');
                  }
                }

                function applySingleUi(){
                  const one=oneWeek();
                  document.documentElement.classList.toggle('singleWeekMode',one);
                  const span=ensureTitleSpan();
                  if(!one)return;
                  ensureSingleState();
                  if(span)span.textContent='A';
                  paintBadge();
                  const edit=document.getElementById('editDayTitle');
                  if(edit)edit.textContent=(edit.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');
                  const today=document.getElementById('todayTitle');
                  if(today)today.textContent=(today.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');
                }

                function wrapRenderContext(){
                  if(typeof window.renderContext!=='function'||window.renderContext.__singleStableWrapped)return;
                  const old=window.renderContext;
                  const wrapped=function(){
                    if(oneWeek()){
                      ensureSingleState();
                      const cw=document.getElementById('currentWeekBtn');
                      if(cw){
                        cw.textContent='Semaine unique';
                        cw.setAttribute('aria-label','Semaine unique');
                      }
                      document.querySelectorAll('.weekTab').forEach(b=>b.classList.toggle('active',b.dataset.week==='A'));
                      return;
                    }
                    return old.apply(this,arguments);
                  };
                  wrapped.__singleStableWrapped=true;
                  window.renderContext=wrapped;
                }

                function wrapToggleCurrentWeek(){
                  if(typeof window.toggleCurrentWeek!=='function'||window.toggleCurrentWeek.__singleStableWrapped)return;
                  const old=window.toggleCurrentWeek;
                  const wrapped=function(){
                    if(oneWeek()){
                      ensureSingleState();
                      paintBadge();
                      return;
                    }
                    return old.apply(this,arguments);
                  };
                  wrapped.__singleStableWrapped=true;
                  window.toggleCurrentWeek=wrapped;
                }

                function repairWeek(){
                  if(!oneWeek())return;
                  const view=document.getElementById('viewWeek');
                  if(!view||!view.classList.contains('active'))return;
                  ensureSingleState();
                  ensureTitleSpan();
                  const grid=document.getElementById('weekGrid');
                  if(!grid||grid.children.length<6){
                    try{
                      if(typeof renderWeek==='function')renderWeek();
                      else if(typeof render==='function')render();
                    }catch(e){
                      ensureTitleSpan();
                      try{if(typeof renderWeek==='function')renderWeek()}catch(ignore){}
                    }
                  }
                  applySingleUi();
                  if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  setTimeout(fixCurrentDayMarker,0);
                }

                function wrapRender(){
                  if(typeof window.render!=='function'||window.render.__weekStableV5Wrapped)return;
                  const old=window.render;
                  const wrapped=function(){
                    if(oneWeek()){
                      ensureSingleState();
                      ensureTitleSpan();
                    }
                    const out=old.apply(this,arguments);
                    applySingleUi();
                    setTimeout(fixCurrentDayMarker,0);
                    if(oneWeek()&&typeof mode!=='undefined'&&mode==='week'){
                      const grid=document.getElementById('weekGrid');
                      if(!grid||grid.children.length<6)setTimeout(repairWeek,0);
                    }
                    return out;
                  };
                  wrapped.__weekStableV5Wrapped=true;
                  window.render=wrapped;
                }

                function wrapBulkRefresh(){
                  if(typeof window.refreshBulkCourseUi!=='function'||window.refreshBulkCourseUi.__weekStableV5Wrapped)return;
                  const old=window.refreshBulkCourseUi;
                  const wrapped=function(){
                    ensureTitleSpan();
                    const out=old.apply(this,arguments);
                    ensureTitleSpan();
                    applySingleUi();
                    setTimeout(fixCurrentDayMarker,0);
                    if(oneWeek()&&document.getElementById('viewWeek')?.classList.contains('active'))setTimeout(repairWeek,0);
                    return out;
                  };
                  wrapped.__weekStableV5Wrapped=true;
                  window.refreshBulkCourseUi=wrapped;
                }

                function bindWeekTab(){
                  const nav=document.querySelector('.nav[data-mode="week"]');
                  if(!nav||nav.dataset.weekStableV5Bound)return;
                  nav.dataset.weekStableV5Bound='1';
                  nav.addEventListener('click',()=>{
                    if(oneWeek()){
                      ensureSingleState();
                      ensureTitleSpan();
                    }
                  },true);
                  nav.addEventListener('click',()=>setTimeout(()=>{repairWeek();fixCurrentDayMarker()},0));
                }

                function syncSingleSchedule(){
                  try{
                    const root=JSON.parse(AndroidSchedule.loadSchedule()||'{}');
                    root._weeks=root._weeks||{};
                    if(!root._weeks.A&&typeof weeks!=='undefined'&&weeks.A)root._weeks.A=clone(weeks.A);
                    if(root._weeks.A){
                      root._weeks.B=clone(root._weeks.A);
                      root._weeks.C=clone(root._weeks.A);
                      root._weeks.D=clone(root._weeks.A);
                    }
                    root._currentWeek='A';
                    root._cycleLength=2;
                    AndroidSchedule.saveSchedule(JSON.stringify(root));
                  }catch(e){}
                  try{
                    if(typeof weeks!=='undefined'&&weeks.A){
                      weeks.B=clone(weeks.A);weeks.C=clone(weeks.A);weeks.D=clone(weeks.A);
                    }
                    currentWeek='A';activeWeek='A';if(typeof weeks!=='undefined'&&weeks.A)state=weeks.A;
                    AndroidSchedule.setCurrentWeek('A');
                  }catch(e){}
                }

                function applyCycleChoice(n){
                  n=Number(n)||2;
                  const a=loadAdv();
                  if(n===1){
                    a.singleWeek=true;
                    a.cycleLength=2;
                    saveAdvObj(a);
                    syncSingleSchedule();
                  }else{
                    a.singleWeek=false;
                    a.cycleLength=Math.max(2,Math.min(4,n));
                    saveAdvObj(a);
                    try{
                      const root=JSON.parse(AndroidSchedule.loadSchedule()||'{}');
                      root._cycleLength=a.cycleLength;
                      AndroidSchedule.saveSchedule(JSON.stringify(root));
                    }catch(e){}
                  }
                  document.documentElement.classList.toggle('singleWeekMode',n===1);
                  try{if(typeof render==='function')render()}catch(e){}
                  setTimeout(()=>{
                    if(window.refreshBulkCourseUi)window.refreshBulkCourseUi();
                    if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                    applySingleUi();
                    installCycleChoice();
                    fixCurrentDayMarker();
                  },0);
                }

                function cycleLabels(){
                  const lang=uiLang();
                  return lang==='de'?{1:'1 Woche (einheitlich)',2:'2 Wochen (A/B)',3:'3 Wochen (A/B/C)',4:'4 Wochen (A/B/C/D)'}:
                    (lang==='en'?{1:'1 week (single)',2:'2 weeks (A/B)',3:'3 weeks (A/B/C)',4:'4 weeks (A/B/C/D)'}:{1:'1 semaine (unique)',2:'2 semaines (A/B)',3:'3 semaines (A/B/C)',4:'4 semaines (A/B/C/D)'});
                }

                function installCycleChoice(){
                  const sel=document.getElementById('advCycle');
                  if(!sel||fixingCycle)return;
                  fixingCycle=true;
                  try{
                    const labels=cycleLabels();
                    const wanted=['1','2','3','4'];
                    const current=[...sel.options].map(o=>o.value);
                    const structureWrong=current.length!==4||wanted.some((v,i)=>current[i]!==v);
                    if(structureWrong){
                      sel.innerHTML='';
                      wanted.forEach(v=>{
                        const o=document.createElement('option');
                        o.value=v;o.textContent=labels[v];sel.appendChild(o);
                      });
                    }else{
                      [...sel.options].forEach(o=>{const txt=labels[o.value];if(txt&&o.textContent!==txt)o.textContent=txt});
                    }
                    const a=loadAdv();
                    const desired=a.singleWeek===true?'1':String(Math.max(2,Math.min(4,Number(a.cycleLength)||2)));
                    if(sel.value!==desired)sel.value=desired;
                    if(!sel.dataset.singleWeekChoiceBoundV5){
                      sel.dataset.singleWeekChoiceBoundV5='1';
                      sel.addEventListener('change',e=>{
                        e.stopImmediatePropagation();
                        applyCycleChoice(e.target.value);
                      },true);
                    }
                    if(!sel.__cycleStableObserver){
                      const observer=new MutationObserver(()=>{
                        if(!fixingCycle)queueMicrotask(installCycleChoice);
                      });
                      observer.observe(sel,{childList:true,subtree:true,characterData:true});
                      sel.__cycleStableObserver=observer;
                    }
                  }finally{
                    fixingCycle=false;
                  }
                }

                function installVersionInfo(){
                  const actions=document.querySelector('#settingsSheet .settingsActions');
                  if(!actions)return;
                  let v=document.getElementById('appVersionInfo');
                  if(!v){
                    v=document.createElement('div');v.id='appVersionInfo';
                    v.style.cssText='text-align:center;margin:12px 0 -2px;color:#7a8494;font-size:.68rem;font-weight:700';
                    actions.insertAdjacentElement('beforebegin',v);
                  }
                  v.textContent='Version '+APP_VERSION;
                }

                function wrapAdvancedRefresh(){
                  if(typeof window.refreshAdvancedFeatures!=='function'||window.refreshAdvancedFeatures.__singleChoiceV5Wrapped)return;
                  const old=window.refreshAdvancedFeatures;
                  const wrapped=function(){
                    const out=old.apply(this,arguments);
                    installCycleChoice();
                    installVersionInfo();
                    return out;
                  };
                  wrapped.__singleChoiceV5Wrapped=true;
                  window.refreshAdvancedFeatures=wrapped;
                }

                function refresh(){
                  ensureTitleSpan();
                  wrapRenderContext();
                  wrapToggleCurrentWeek();
                  wrapRender();
                  wrapBulkRefresh();
                  wrapAdvancedRefresh();
                  bindWeekTab();
                  installCycleChoice();
                  installVersionInfo();
                  applySingleUi();
                  if(oneWeek()&&document.getElementById('viewWeek')?.classList.contains('active'))repairWeek();
                  setTimeout(fixCurrentDayMarker,0);
                }

                window.refreshWeekViewStability=refresh;
                const grid=document.getElementById('weekGrid');
                if(grid&&!grid.__markerPolishObserver){
                  const obs=new MutationObserver(()=>setTimeout(fixCurrentDayMarker,0));
                  obs.observe(grid,{childList:true,subtree:true});
                  grid.__markerPolishObserver=obs;
                }
                if(!window.__weekMarkerPolishTimer)window.__weekMarkerPolishTimer=setInterval(fixCurrentDayMarker,15000);
                refresh();
                setTimeout(refresh,80);
                setTimeout(refresh,400);
              }catch(e){console.log('WeekViewStabilityUi',e)}
            })();
            """;
    }
}
