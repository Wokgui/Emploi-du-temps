package com.wokgui.schedulewidget;

/** 7.48: stable navigation/date state, real settings page and final edit/week layout. */
final class Layout747Ui {
    private Layout747Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__layout748){
                  if(window.refreshLayout747)window.refreshLayout747();
                  return;
                }
                window.__layout747=true;
                window.__layout748=true;

                let style=document.getElementById('layout747Style');
                if(!style){
                  style=document.createElement('style');
                  style.id='layout747Style';
                  document.head.appendChild(style);
                }
                style.textContent=`
                  /* Settings is a real page. HeavyPanel owns open/close through data-edt-open. */
                  html body #settingsModal.edtHeavyPanel648[data-edt-open="true"],
                  html body #settingsModal.show{
                    position:fixed!important;
                    inset:0!important;
                    z-index:10000!important;
                    display:block!important;
                    width:100vw!important;
                    height:100dvh!important;
                    max-width:none!important;
                    max-height:none!important;
                    margin:0!important;
                    padding:0!important;
                    background:#fff!important;
                    opacity:1!important;
                    visibility:visible!important;
                    pointer-events:auto!important;
                    overflow:hidden!important;
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    backdrop-filter:none!important;
                    clip-path:none!important;
                    contain:none!important;
                  }
                  html body #settingsModal.edtHeavyPanel648[data-edt-open="true"] #settingsSheet,
                  html body #settingsModal.show #settingsSheet{
                    position:relative!important;
                    inset:auto!important;
                    display:block!important;
                    width:100%!important;
                    max-width:100%!important;
                    min-width:0!important;
                    height:100dvh!important;
                    max-height:100dvh!important;
                    margin:0!important;
                    padding:max(12px,env(safe-area-inset-top)) 12px max(18px,env(safe-area-inset-bottom))!important;
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    background:#fff!important;
                    overflow-x:hidden!important;
                    overflow-y:auto!important;
                    overscroll-behavior:contain!important;
                    transform:none!important;
                    scale:1!important;
                  }
                  html body #settingsModal[data-edt-open="true"] #settingsSheet,
                  html body #settingsModal[data-edt-open="true"] #settingsSheet *{
                    box-sizing:border-box!important;
                  }
                  html.settingsPage748 body{overflow:hidden!important}

                  /* Keep the 7.46 Advanced geometry, but never let long rows escape the page. */
                  html body #settingsModal[data-edt-open="true"] #advancedSettings85,
                  html body #settingsModal[data-edt-open="true"] #advancedContent85,
                  html body #settingsModal[data-edt-open="true"] #advancedContent85>.settingBox{
                    width:100%!important;
                    max-width:100%!important;
                    min-width:0!important;
                  }
                  html body #settingsModal[data-edt-open="true"] #advancedContent85{
                    overflow-x:hidden!important;
                  }
                  html body #settingsModal[data-edt-open="true"] #schoolAutoRow739{
                    display:grid!important;
                    grid-template-columns:minmax(0,1fr) auto!important;
                    align-items:center!important;
                    gap:8px!important;
                    width:100%!important;
                    max-width:100%!important;
                    min-width:0!important;
                    margin-left:0!important;
                    margin-right:0!important;
                  }
                  html body #settingsModal[data-edt-open="true"] #schoolAutoRow739>*{
                    min-width:0!important;
                    max-width:100%!important;
                  }
                  html body #settingsModal[data-edt-open="true"] #schoolAutoRow739 label,
                  html body #settingsModal[data-edt-open="true"] #schoolAutoRow739 .settingLabel{
                    white-space:normal!important;
                    overflow:visible!important;
                    text-overflow:clip!important;
                  }
                  html body #settingsModal[data-edt-open="true"] .rangeDates725,
                  html body #settingsModal[data-edt-open="true"] #schoolCalendarBlock,
                  html body #settingsModal[data-edt-open="true"] .holidayFlat725{
                    width:100%!important;
                    max-width:100%!important;
                    min-width:0!important;
                  }
                  html body #settingsModal[data-edt-open="true"] #resetFooter692{
                    width:100%!important;
                    max-width:100%!important;
                    display:grid!important;
                    grid-template-columns:minmax(0,1fr) minmax(0,1fr)!important;
                    gap:8px!important;
                  }
                  html body #settingsModal[data-edt-open="true"] #resetFooter692 button{
                    width:100%!important;
                    min-width:0!important;
                    white-space:normal!important;
                    overflow-wrap:anywhere!important;
                  }

                  /* Week: both arrows and the title are permanently on one row. */
                  html body #viewWeek .weekTop{
                    display:grid!important;
                    grid-template-columns:48px minmax(0,1fr) 48px!important;
                    grid-template-rows:48px!important;
                    align-items:center!important;
                    column-gap:8px!important;
                    row-gap:0!important;
                    min-height:48px!important;
                    margin:0 0 7px!important;
                  }
                  html body #viewWeek #weekPrev728{
                    grid-column:1!important;
                    grid-row:1!important;
                    align-self:center!important;
                    justify-self:start!important;
                  }
                  html body #viewWeek #weekNext728{
                    grid-column:3!important;
                    grid-row:1!important;
                    align-self:center!important;
                    justify-self:end!important;
                  }
                  html body #viewWeek #weekTitleStable746{
                    grid-column:2!important;
                    grid-row:1!important;
                    align-self:center!important;
                    justify-self:center!important;
                    width:auto!important;
                    max-width:100%!important;
                    min-width:0!important;
                    margin:0!important;
                  }
                  html body #viewWeek .weekTop>h2{
                    grid-column:2!important;
                    grid-row:1!important;
                    position:absolute!important;
                    width:1px!important;
                    height:1px!important;
                    overflow:hidden!important;
                    opacity:0!important;
                    pointer-events:none!important;
                  }
                  html body #viewWeek.active,
                  html body #viewWeek.active .weekScroller,
                  html body #viewWeek.active #weekGrid{
                    visibility:visible!important;
                    opacity:1!important;
                  }
                  html body #viewWeek #weekGrid{
                    transition:none!important;
                    animation:none!important;
                  }

                  /* Today always ends above the fixed bottom navigation. */
                  html.edtToday748 body{overflow-y:hidden!important}
                  html.edtToday748 body main.wrap{
                    padding-bottom:0!important;
                    overflow:hidden!important;
                  }
                  html body #viewToday.active{overflow:hidden!important}
                  html body #viewToday.active #todayList{overflow:hidden!important}
                  html body #viewToday.active #todayList .todayCourse{
                    min-height:var(--today748-row-h,44px)!important;
                    height:var(--today748-row-h,auto)!important;
                    padding-top:2px!important;
                    padding-bottom:2px!important;
                  }
                  html body #viewToday.active.todayTight748 #todayList .label{font-size:.84rem!important;line-height:1.02!important}
                  html body #viewToday.active.todayTight748 #todayList .room{font-size:.66rem!important;line-height:1.02!important}
                  html body #viewToday.active.todayTight748 #todayList .time{font-size:.71rem!important;line-height:1.04!important}

                  /* Edit: the three large actions fit their text and stay centered. */
                  html body #viewEdit #importPhoto,
                  html body #viewEdit #addCourse,
                  html body #viewEdit #addBulkCourses{
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    width:max-content!important;
                    max-width:calc(100% - 24px)!important;
                    min-width:0!important;
                    min-height:42px!important;
                    height:auto!important;
                    padding:10px 22px!important;
                    margin-left:auto!important;
                    margin-right:auto!important;
                    text-align:center!important;
                    white-space:normal!important;
                    line-height:1.15!important;
                  }

                  html body .bottom{
                    position:fixed!important;
                    left:0!important;
                    right:0!important;
                    bottom:0!important;
                    z-index:50!important;
                  }
                `;

                const weekView=()=>document.getElementById('viewWeek');
                const weekGrid=()=>document.getElementById('weekGrid');
                const bottom=()=>document.querySelector('.bottom');
                let weekRendering748=false;
                let weekRetry748=0;
                let weekRetryRaf748=0;
                let settingsWasOpen748=false;
                let pinnedDay748=0;

                function weekValid748(){
                  const g=weekGrid();
                  if(!g)return false;
                  const heads=g.querySelectorAll(':scope>.wh.day').length;
                  const times=g.querySelectorAll(':scope>.wh.timecol').length;
                  const cells=g.querySelectorAll(':scope>.wc').length;
                  return heads>0&&times>0&&cells>=heads;
                }

                function finishWeek748(){
                  try{if(typeof window.refreshWeekView744==='function')window.refreshWeekView744()}catch(e){}
                  try{if(typeof window.refreshWeekView746==='function')window.refreshWeekView746()}catch(e){}
                  try{if(typeof window.fitActiveWeek676==='function'&&weekView()?.classList.contains('active'))window.fitActiveWeek676()}catch(e){}
                  const prev=document.getElementById('weekPrev728');
                  const next=document.getElementById('weekNext728');
                  const stable=document.getElementById('weekTitleStable746');
                  if(prev){prev.style.setProperty('grid-column','1','important');prev.style.setProperty('grid-row','1','important')}
                  if(next){next.style.setProperty('grid-column','3','important');next.style.setProperty('grid-row','1','important')}
                  if(stable){stable.style.setProperty('grid-column','2','important');stable.style.setProperty('grid-row','1','important')}
                  document.documentElement.dataset.edtWeek748=weekValid748()?'1':'0';
                }

                function ensureLegacyWeekLetter748(){
                  let letter=document.getElementById('weekTitleLetter');
                  if(letter)return letter;
                  const h2=document.querySelector('#viewWeek .weekTop>h2');
                  if(!h2)return null;
                  letter=document.createElement('span');
                  letter.id='weekTitleLetter';
                  letter.className='weekLetter';
                  letter.setAttribute('aria-hidden','true');
                  letter.style.setProperty('display','none','important');
                  h2.appendChild(letter);
                  return letter;
                }

                const previousWeek748=window.renderWeek;
                function scheduleWeekRepair748(){
                  if(weekValid748()||weekRetryRaf748)return;
                  weekRetryRaf748=requestAnimationFrame(()=>{
                    weekRetryRaf748=0;
                    if(weekValid748())return;
                    if(weekRetry748<2){
                      weekRetry748++;
                      renderWeek748();
                    }else{
                      weekRetry748=0;
                      try{
                        if(typeof window.reloadSchedule==='function')window.reloadSchedule();
                        else if(typeof previousWeek748==='function')previousWeek748();
                      }catch(e){}
                      finishWeek748();
                    }
                  });
                }
                function renderWeek748(){
                  if(weekRendering748){
                    return typeof previousWeek748==='function'?previousWeek748.apply(this,arguments):undefined;
                  }
                  weekRendering748=true;
                  let result;
                  try{
                    ensureLegacyWeekLetter748();
                    if(typeof previousWeek748==='function')result=previousWeek748.apply(this,arguments);
                  }catch(e){
                    console.error('Layout748Ui week',e);
                  }finally{
                    weekRendering748=false;
                  }
                  finishWeek748();
                  if(weekValid748())weekRetry748=0;
                  else scheduleWeekRepair748();
                  return result;
                }
                renderWeek748.__layout748=true;
                if(typeof previousWeek748==='function'){
                  window.renderWeek=renderWeek748;
                  try{renderWeek=renderWeek748}catch(e){}
                }

                function capturePinnedDay748(date){
                  try{
                    const d=date instanceof Date?date:window.__edt728DayDate;
                    if(!(d instanceof Date)||Number.isNaN(d.getTime()))return false;
                    pinnedDay748=new Date(d.getFullYear(),d.getMonth(),d.getDate(),12,0,0,0).getTime();
                    window.__edt748PinnedDay=pinnedDay748;
                    window.__edt728DayDate=new Date(pinnedDay748);
                    return true;
                  }catch(e){return false}
                }
                function restorePinnedDay748(){
                  if(!pinnedDay748)return;
                  try{
                    window.__edt728DayDate=new Date(pinnedDay748);
                    window.__edt748PinnedDay=pinnedDay748;
                  }catch(e){}
                }
                function movePinnedDay748(delta){
                  try{
                    const source=(window.__edt728DayDate instanceof Date&&!Number.isNaN(window.__edt728DayDate.getTime()))
                      ?window.__edt728DayDate:new Date();
                    const next=new Date(source.getFullYear(),source.getMonth(),source.getDate(),12,0,0,0);
                    next.setDate(next.getDate()+delta);
                    capturePinnedDay748(next);
                    if(typeof window.renderToday==='function')window.renderToday();
                    return true;
                  }catch(e){return false}
                }

                function bindDayNav748(){
                  const defs=[['dayPrev728',-1],['dayNext728',1]];
                  defs.forEach(([id,delta])=>{
                    const button=document.getElementById(id);
                    if(!button||button.__layout748DayOwner)return;
                    button.__layout748DayOwner=true;
                    button.onclick=function(event){
                      if(event){
                        event.preventDefault();
                        event.stopPropagation();
                      }
                      movePinnedDay748(delta);
                      fitToday748();
                      return false;
                    };
                  });
                }

                function finalProfile748(){
                  const select=document.getElementById('advProfileSelect');if(!select)return;
                  const shell=select.closest('.profileShell745')||select.parentElement;
                  if(shell){
                    shell.style.setProperty('position','relative','important');
                    shell.style.setProperty('display','block','important');
                    shell.style.setProperty('width','180px','important');
                    shell.style.setProperty('min-width','180px','important');
                    shell.style.setProperty('max-width','min(180px,calc(100vw - 84px))','important');
                    shell.style.setProperty('height','36px','important');
                    shell.style.setProperty('margin','0 auto','important');
                    shell.style.setProperty('border','1px solid #cfd9e5','important');
                    shell.style.setProperty('border-radius','12px','important');
                    shell.style.setProperty('background','#fff','important');
                    shell.style.setProperty('overflow','hidden','important');
                  }
                  select.style.setProperty('position','absolute','important');
                  select.style.setProperty('inset','0','important');
                  select.style.setProperty('width','100%','important');
                  select.style.setProperty('min-width','100%','important');
                  select.style.setProperty('max-width','100%','important');
                  select.style.setProperty('height','36px','important');
                  select.style.setProperty('margin','0','important');
                  select.style.setProperty('padding','0','important');
                  select.style.setProperty('opacity','0','important');
                  select.style.setProperty('color','transparent','important');
                  select.style.setProperty('-webkit-text-fill-color','transparent','important');
                  select.style.setProperty('appearance','none','important');
                  select.style.setProperty('-webkit-appearance','none','important');
                  select.style.setProperty('cursor','pointer','important');
                  select.style.setProperty('z-index','5','important');
                  const visual=shell&&shell.querySelector(':scope > .profileVisual745');
                  if(visual){
                    const option=select.options&&select.options[select.selectedIndex];
                    const text=String(option?.textContent||'').trim();
                    if(visual.textContent!==text)visual.textContent=text;
                    visual.style.setProperty('z-index','3','important');
                  }
                  const arrow=shell&&shell.querySelector(':scope > .profileArrow745');
                  if(arrow)arrow.style.setProperty('z-index','4','important');
                }

                window.__edtMovePinnedDay748=movePinnedDay748;
                window.__edtCalendarToday748=calendarToday748;

                function fitToday748(){
                  const view=document.getElementById('viewToday');
                  const list=document.getElementById('todayList');
                  const nav=bottom();
                  if(!view||!list||!nav)return;
                  const active=view.classList.contains('active');
                  const settingsOpen=document.getElementById('settingsModal')?.getAttribute('data-edt-open')==='true'
                    ||document.getElementById('settingsModal')?.classList.contains('show');
                  document.documentElement.classList.toggle('edtToday748',active&&!settingsOpen);
                  if(!active)return;

                  const top=Math.round(view.getBoundingClientRect().top);
                  const navTop=Math.round(nav.getBoundingClientRect().top);
                  const available=Math.max(220,navTop-top-4);
                  view.style.setProperty('height',available+'px','important');

                  const listTop=Math.round(list.getBoundingClientRect().top);
                  const listAvail=Math.max(120,navTop-listTop-4);
                  const rows=[...list.querySelectorAll(':scope>.todayCourse')];
                  if(!rows.length){
                    list.style.removeProperty('height');
                    list.style.removeProperty('max-height');
                    document.documentElement.dataset.edtToday748='empty';
                    return;
                  }
                  const target=Math.max(34,Math.min(64,Math.floor((listAvail-2)/rows.length)));
                  view.style.setProperty('--today748-row-h',target+'px');
                  view.classList.toggle('todayTight748',target<49);
                  list.style.setProperty('height',Math.min(listAvail,target*rows.length+2)+'px','important');
                  list.style.setProperty('max-height',listAvail+'px','important');
                  document.documentElement.dataset.edtToday748=String(target);
                }

                const previousToday748=window.renderToday;
                const calendarToday748=window.renderTodayCalendar728;
                let renderToday748=null;
                if(typeof previousToday748==='function'){
                  renderToday748=function(){
                    restorePinnedDay748();
                    let result;
                    const renderer=pinnedDay748&&typeof calendarToday748==='function'?calendarToday748:previousToday748;
                    result=renderer.apply(this,arguments);
                    restorePinnedDay748();
                    bindDayNav748();
                    fitToday748();
                    requestAnimationFrame(()=>{bindDayNav748();fitToday748()});
                    return result;
                  };
                  renderToday748.__layout748=true;
                  window.renderToday=renderToday748;
                  try{renderToday=renderToday748}catch(e){}
                }

                function balanceEdit748(){
                  const view=document.getElementById('viewEdit');
                  const button=document.getElementById('importPhoto');
                  const days=view&&view.querySelector('.editTop');
                  const header=document.querySelector('.header');
                  if(!view||!button||!days||!header)return;
                  const br=button.getBoundingClientRect(),dr=days.getBoundingClientRect(),hr=header.getBoundingClientRect();
                  const topGap=Math.max(0,br.top-hr.bottom);
                  const currentGap=Math.max(0,dr.top-br.bottom);
                  const currentMargin=parseFloat(getComputedStyle(button).marginBottom)||0;
                  const wanted=Math.max(0,Math.min(48,currentMargin+(topGap-currentGap)));
                  button.style.setProperty('margin-bottom',wanted+'px','important');
                  document.documentElement.dataset.edtEditGap748=Math.round(wanted).toString();
                }

                function settingsOpen748(){
                  const modal=document.getElementById('settingsModal');
                  return !!(modal&&(modal.getAttribute('data-edt-open')==='true'||modal.classList.contains('show')));
                }

                function syncSettings748(){
                  const modal=document.getElementById('settingsModal');
                  const open=settingsOpen748();
                  document.documentElement.classList.toggle('settingsPage748',open);
                  document.documentElement.dataset.edtSettingsPage748=open?'1':'0';
                  if(open&&!settingsWasOpen748){
                    settingsWasOpen748=true;
                    queueMicrotask(()=>{
                      try{if(typeof window.prepareSettingsOpen665==='function')window.prepareSettingsOpen665()}catch(e){}
                      try{if(typeof window.refreshWorkflow85==='function')window.refreshWorkflow85()}catch(e){}
                      try{if(typeof window.refreshSettings745==='function')window.refreshSettings745()}catch(e){}
                      try{if(typeof window.refreshSettings746==='function')window.refreshSettings746()}catch(e){}
                      finalProfile748();
                      requestAnimationFrame(()=>{
                        try{if(typeof window.refreshSettings745==='function')window.refreshSettings745()}catch(e){}
                        try{if(typeof window.refreshSettings746==='function')window.refreshSettings746()}catch(e){}
                        finalProfile748();
                      });
                    });
                  }else if(!open){
                    settingsWasOpen748=false;
                  }
                  fitToday748();
                }

                const settings=document.getElementById('settingsModal');
                if(settings&&!settings.__layout748Observed){
                  settings.__layout748Observed=true;
                  new MutationObserver(syncSettings748).observe(settings,{attributes:true,attributeFilter:['class','data-edt-open']});
                }

                document.addEventListener('pointerdown',event=>{
                  const nav=event.target&&event.target.closest?event.target.closest('.bottom .nav[data-mode]'):null;
                  if(nav&&nav.dataset.mode==='today'){
                    pinnedDay748=0;
                    window.__edt748PinnedDay=0;
                    window.__edt728DayDate=new Date();
                  }
                  if(nav&&nav.dataset.mode==='week'){
                    /* Build the real grid before the tab becomes visible. */
                    renderWeek748();
                  }
                },true);

                document.addEventListener('pointerup',event=>{
                  const dayNav=event.target&&event.target.closest?event.target.closest('#dayPrev728,#dayNext728'):null;
                  if(dayNav)requestAnimationFrame(()=>{bindDayNav748();capturePinnedDay748();fitToday748()});
                },true);

                const activeObserver=new MutationObserver(()=>{
                  fitToday748();
                  balanceEdit748();
                  finalProfile748();
                  if(weekView()?.classList.contains('active')){
                    if(!weekValid748())scheduleWeekRepair748();
                    else finishWeek748();
                  }
                });
                document.querySelectorAll('main.wrap>.view').forEach(v=>activeObserver.observe(v,{attributes:true,attributeFilter:['class']}));

                const grid=weekGrid();
                if(grid&&!grid.__layout748Observed){
                  grid.__layout748Observed=true;
                  new MutationObserver(()=>{
                    if(weekRendering748)return;
                    if(weekView()?.classList.contains('active')&&!weekValid748())scheduleWeekRepair748();
                  }).observe(grid,{childList:true,subtree:false});
                }

                window.addEventListener('resize',()=>{fitToday748();balanceEdit748();finishWeek748()},{passive:true});
                window.addEventListener('orientationchange',()=>setTimeout(()=>{fitToday748();balanceEdit748();finishWeek748()},80),{passive:true});

                function installRenderOwners748(){
                  if(typeof renderToday748==='function'){
                    window.renderToday=renderToday748;
                    try{renderToday=renderToday748}catch(e){}
                  }
                  window.renderWeek=renderWeek748;
                  try{renderWeek=renderWeek748}catch(e){}
                  bindDayNav748();
                }

                window.__edtInstallOwners748=installRenderOwners748;

                const legacyLunchRefresh748=window.refreshLunchBreakUi;
                if(typeof legacyLunchRefresh748==='function'&&!legacyLunchRefresh748.__layout748Owner){
                  const ownedLunchRefresh748=function(){
                    const result=legacyLunchRefresh748.apply(this,arguments);
                    installRenderOwners748();
                    return result;
                  };
                  ownedLunchRefresh748.__layout748Owner=true;
                  window.refreshLunchBreakUi=ownedLunchRefresh748;
                }

                window.refreshLayout747=function(){
                  installRenderOwners748();
                  syncSettings748();
                  fitToday748();
                  balanceEdit748();
                  if(weekView()?.classList.contains('active')){
                    if(weekValid748())finishWeek748();
                    else scheduleWeekRepair748();
                  }
                };

                /* Do not pre-render Week at startup: 7.47's early render raced native schedule loading. */
                installRenderOwners748();
                syncSettings748();
                bindDayNav748();
                fitToday748();
                balanceEdit748();
                finalProfile748();
                requestAnimationFrame(()=>{installRenderOwners748();bindDayNav748();fitToday748();balanceEdit748();finalProfile748()});
              }catch(e){try{document.documentElement.dataset.edtLayout748Error=String(e);document.documentElement.dataset.edtLayout748Stack=String(e&&e.stack||'')}catch(_){}console.error('Layout748Ui',e)}
            })();
            """;
    }
}
