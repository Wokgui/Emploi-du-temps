package com.wokgui.schedulewidget;

/** 7.47: full-page settings, instant week entry and viewport-fitted Today view. */
final class Layout747Ui {
    private Layout747Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__layout747){window.refreshLayout747&&window.refreshLayout747();return}
                window.__layout747=true;

                const style=document.createElement('style');
                style.id='layout747Style';
                style.textContent=`
                  /* Settings are a real page, not a floating bottom sheet. */
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
                  }
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
                  }
                  html body #settingsModal.show #settingsSheet,
                  html body #settingsModal.show #settingsSheet *{box-sizing:border-box!important}
                  html body #settingsModal.show #settingsSheet>details,
                  html body #settingsModal.show #settingsSheet>.settingBox,
                  html body #settingsModal.show #settingsSheet>#resetFooter692,
                  html body #settingsModal.show #settingsSheet>.settingsActions{
                    width:100%!important;
                    max-width:100%!important;
                    min-width:0!important;
                  }
                  html body #settingsModal.show #settingsSheet select,
                  html body #settingsModal.show #settingsSheet input,
                  html body #settingsModal.show #settingsSheet button{max-width:100%}
                  html body #settingsModal.show #resetFooter692{
                    display:grid!important;
                    grid-template-columns:minmax(0,1fr) minmax(0,1fr)!important;
                    gap:8px!important;
                  }
                  html body #settingsModal.show #resetFooter692 button{
                    width:100%!important;
                    min-width:0!important;
                    padding-left:6px!important;
                    padding-right:6px!important;
                    white-space:normal!important;
                    overflow-wrap:anywhere!important;
                    line-height:1.15!important;
                  }
                  html.settingsPage747 body{overflow:hidden!important}

                  /* Week header: previous, title and next always share one line. */
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

                  /* Today must fit between the app header and the fixed bottom navigation. */
                  html.edtToday747 body{overflow-y:hidden!important}
                  html.edtToday747 body main.wrap{
                    padding-bottom:0!important;
                    overflow:hidden!important;
                  }
                  html body #viewToday.active{
                    overflow:hidden!important;
                  }
                  html body #viewToday.active #todayList{
                    overflow:hidden!important;
                  }
                  html body #viewToday.active #todayList .todayCourse{
                    min-height:var(--today747-row-h,44px)!important;
                    height:var(--today747-row-h,auto)!important;
                    padding-top:2px!important;
                    padding-bottom:2px!important;
                  }
                  html body #viewToday.active.todayTight747 #todayList .label{font-size:.84rem!important;line-height:1.02!important}
                  html body #viewToday.active.todayTight747 #todayList .room{font-size:.66rem!important;line-height:1.02!important}
                  html body #viewToday.active.todayTight747 #todayList .time{font-size:.71rem!important;line-height:1.04!important}
                  html body .bottom{
                    position:fixed!important;
                    left:0!important;
                    right:0!important;
                    bottom:0!important;
                    z-index:50!important;
                  }
                `;
                document.head.appendChild(style);

                const weekView=()=>document.getElementById('viewWeek');
                const weekGrid=()=>document.getElementById('weekGrid');
                const bottom=()=>document.querySelector('.bottom');
                let weekEntering747=false;
                let weekFreshQueued747=false;

                function weekValid747(){
                  const g=weekGrid();
                  return !!(g&&g.querySelectorAll(':scope>.wh.day').length>0&&g.querySelectorAll(':scope>.wc').length>0);
                }

                function finishWeek747(){
                  try{if(typeof window.refreshWeekView744==='function')window.refreshWeekView744()}catch(e){}
                  try{if(typeof window.refreshWeekView746==='function')window.refreshWeekView746()}catch(e){}
                  const prev=document.getElementById('weekPrev728');
                  const next=document.getElementById('weekNext728');
                  if(prev){prev.style.setProperty('grid-column','1','important');prev.style.setProperty('grid-row','1','important')}
                  if(next){next.style.setProperty('grid-column','3','important');next.style.setProperty('grid-row','1','important')}
                  const stable=document.getElementById('weekTitleStable746');
                  if(stable){stable.style.setProperty('grid-column','2','important');stable.style.setProperty('grid-row','1','important')}
                  document.documentElement.dataset.edtWeek747=weekValid747()?'1':'0';
                }

                const previousWeek747=window.renderWeek;
                function fullWeek747(){
                  let result;
                  try{if(typeof previousWeek747==='function')result=previousWeek747.apply(this,arguments)}catch(e){console.error('Layout747Ui week',e)}
                  finishWeek747();
                  return result;
                }
                function queueFreshWeek747(){
                  if(weekFreshQueued747)return;
                  weekFreshQueued747=true;
                  requestAnimationFrame(()=>requestAnimationFrame(()=>{
                    weekFreshQueued747=false;
                    fullWeek747();
                  }));
                }
                function renderWeek747(){
                  /* On entry, keep the already prepared grid for the first painted frame.
                     The complete render follows one frame later, so the view never appears blank. */
                  if(weekEntering747&&weekValid747()){
                    weekEntering747=false;
                    finishWeek747();
                    queueFreshWeek747();
                    return;
                  }
                  weekEntering747=false;
                  return fullWeek747.apply(this,arguments);
                }
                renderWeek747.__layout747=true;
                if(typeof previousWeek747==='function'){
                  window.renderWeek=renderWeek747;
                  try{renderWeek=renderWeek747}catch(e){}
                }

                function primeWeek747(){
                  if(!weekValid747())fullWeek747();
                  else finishWeek747();
                }

                function fitToday747(){
                  const view=document.getElementById('viewToday');
                  const list=document.getElementById('todayList');
                  const nav=bottom();
                  if(!view||!list||!nav)return;
                  const active=view.classList.contains('active');
                  document.documentElement.classList.toggle('edtToday747',active&&!document.getElementById('settingsModal')?.classList.contains('show'));
                  if(!active)return;

                  const top=Math.round(view.getBoundingClientRect().top);
                  const navTop=Math.round(nav.getBoundingClientRect().top);
                  const available=Math.max(220,navTop-top-4);
                  view.style.setProperty('height',available+'px','important');

                  const listTop=Math.round(list.getBoundingClientRect().top);
                  const listAvail=Math.max(120,navTop-listTop-4);
                  const rows=[...list.querySelectorAll(':scope>.todayCourse')];
                  if(!rows.length){list.style.removeProperty('height');return}
                  const target=Math.max(34,Math.min(64,Math.floor((listAvail-2)/rows.length)));
                  view.style.setProperty('--today747-row-h',target+'px');
                  view.classList.toggle('todayTight747',target<49);
                  list.style.setProperty('height',Math.min(listAvail,target*rows.length+2)+'px','important');
                  list.style.setProperty('max-height',listAvail+'px','important');
                  document.documentElement.dataset.edtToday747=String(target);
                }

                const previousToday747=window.renderToday;
                if(typeof previousToday747==='function'){
                  const renderToday747=function(){
                    const result=previousToday747.apply(this,arguments);
                    fitToday747();
                    requestAnimationFrame(fitToday747);
                    return result;
                  };
                  renderToday747.__layout747=true;
                  window.renderToday=renderToday747;
                  try{renderToday=renderToday747}catch(e){}
                }

                function syncSettings747(){
                  const modal=document.getElementById('settingsModal');
                  const open=!!(modal&&modal.classList.contains('show'));
                  document.documentElement.classList.toggle('settingsPage747',open);
                  if(open){
                    modal.style.setProperty('display','block','important');
                    modal.style.setProperty('opacity','1','important');
                    modal.style.setProperty('visibility','visible','important');
                    modal.style.setProperty('pointer-events','auto','important');
                  }else{
                    document.documentElement.style.removeProperty('overflow');
                  }
                  fitToday747();
                  document.documentElement.dataset.edtSettingsPage747=open?'1':'0';
                }

                const settings=document.getElementById('settingsModal');
                if(settings&&!settings.__layout747Observed){
                  settings.__layout747Observed=true;
                  new MutationObserver(syncSettings747).observe(settings,{attributes:true,attributeFilter:['class','data-edt-open','style']});
                }

                document.addEventListener('pointerdown',event=>{
                  const weekNav=event.target&&event.target.closest?event.target.closest('.bottom .nav[data-mode="week"]'):null;
                  if(weekNav){
                    weekEntering747=true;
                    primeWeek747();
                  }
                },true);

                const activeObserver=new MutationObserver(()=>{
                  fitToday747();
                  if(weekView()?.classList.contains('active'))finishWeek747();
                });
                document.querySelectorAll('main.wrap>.view').forEach(v=>activeObserver.observe(v,{attributes:true,attributeFilter:['class']}));

                window.addEventListener('resize',()=>{fitToday747();finishWeek747()},{passive:true});
                window.addEventListener('orientationchange',()=>setTimeout(()=>{fitToday747();finishWeek747()},80),{passive:true});

                window.refreshLayout747=function(){
                  syncSettings747();
                  fitToday747();
                  finishWeek747();
                };

                /* Pre-render the week while hidden. Entering Week therefore has content immediately. */
                primeWeek747();
                syncSettings747();
                fitToday747();
                requestAnimationFrame(()=>{primeWeek747();fitToday747()});
                setTimeout(()=>{primeWeek747();fitToday747()},120);
              }catch(e){console.error('Layout747Ui',e)}
            })();
            """;
    }
}
