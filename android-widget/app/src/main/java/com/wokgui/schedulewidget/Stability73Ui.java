package com.wokgui.schedulewidget;

final class Stability73Ui {
    private Stability73Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability73V1){if(window.refreshStability73)window.refreshStability73();return}
                window.__stability73V1=true;
                const APP_VERSION='6.26';
                let applying=false;
                let lastPointerAt=0;
                let lastPointerMode=0;
                let normalizeQueued=false;

                function loadAdv(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}
                }
                function currentMode(){
                  const a=loadAdv();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2));
                }
                function deep(o){try{return JSON.parse(JSON.stringify(o))}catch(e){return o}}

                const style=document.createElement('style');
                style.id='stability73Style';
                style.textContent=`
                  /* 6.13: these four controls must never become untappable because an older
                     cycle layer temporarily leaves a busy class or pointer-events:none behind. */
                  html body #weekModeBar{position:relative!important;z-index:20!important;pointer-events:auto!important}
                  html body #weekModeBar .weekModeChoices{position:relative!important;z-index:21!important;pointer-events:auto!important}
                  html body #weekModeBar .weekModeChoice,
                  html body #weekModeBar .weekModeChoice.active,
                  html body.cycle69Busy #weekModeBar .weekModeChoice,
                  html body.cycleSwitchBusy #weekModeBar .weekModeChoice,
                  html body.cycleSwitchBusy #weekModeBar .weekModeChoice.active,
                  html body.cycle69Busy #weekModeBar .weekModeChoice.active{
                    pointer-events:auto!important;
                    touch-action:manipulation!important;
                    -webkit-user-select:none!important;
                    user-select:none!important;
                    -webkit-tap-highlight-color:transparent!important;
                    position:relative!important;
                    z-index:22!important;
                    cursor:pointer!important;
                  }
                  html body #weekModeBar .weekModeChoice:active{opacity:.68!important}
                `;
                document.head.appendChild(style);

                function ensureExtraWeekObjects(){
                  try{
                    if(typeof weeks==='undefined'||!weeks)return;
                    const empty=()=>{const w={};for(const d of (typeof DAYS!=='undefined'&&Array.isArray(DAYS)?DAYS:[2,3,4,5,6]))w[d]={enabled:true,courses:[]};return w};
                    if(!weeks.C)weeks.C=weeks.A?deep(weeks.A):empty();
                    if(!weeks.D)weeks.D=weeks.B?deep(weeks.B):empty();
                  }catch(e){}
                }

                function markMode(n){
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>{
                    const active=Number(b.dataset.m)===Number(n);
                    if(b.classList.contains('active')!==active)b.classList.toggle('active',active);
                    b.setAttribute('aria-pressed',active?'true':'false');
                  });
                }

                function normalizeButtons(){
                  normalizeQueued=false;
                  document.body.classList.remove('cycle69Busy','cycleSwitchBusy');
                  const bar=document.getElementById('weekModeBar');if(!bar)return;
                  const box=bar.querySelector('.weekModeChoices');if(!box)return;
                  const defs=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  defs.forEach(([v,label])=>{
                    let b=box.querySelector('.weekModeChoice[data-m="'+v+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;box.appendChild(b)}
                    b.type='button';b.hidden=false;b.removeAttribute('hidden');b.style.removeProperty('display');
                    if(b.textContent!==label)b.textContent=label;
                    /* Event handling is delegated at document level below. Removing old
                       onclick closures prevents stale busy locks from swallowing a tap. */
                    b.onclick=null;
                    b.style.setProperty('pointer-events','auto','important');
                    b.style.setProperty('touch-action','manipulation','important');
                  });
                  const order={'1':0,'2':1,'3':2,'4':3};
                  [...box.querySelectorAll('.weekModeChoice')].sort((a,b)=>(order[a.dataset.m]??99)-(order[b.dataset.m]??99)).forEach(b=>box.appendChild(b));
                  markMode(currentMode());
                }

                function queueNormalize(){
                  if(normalizeQueued)return;normalizeQueued=true;requestAnimationFrame(normalizeButtons);
                }

                function applyMode(n){
                  n=Number(n);if(![1,2,3,4].includes(n)||applying)return;
                  const old=currentMode();
                  markMode(n);
                  if(old===n){normalizeButtons();return}
                  applying=true;
                  try{
                    const a=loadAdv();
                    a.singleWeek=n===1;
                    a.cycleLength=n===1?2:n;
                    AndroidSchedule.saveAdvancedSettings(JSON.stringify(a));
                    ensureExtraWeekObjects();

                    const allowed=['A','B','C','D'].slice(0,n===1?1:n);
                    let cur=(typeof currentWeek!=='undefined'?currentWeek:'A');
                    let act=(typeof activeWeek!=='undefined'?activeWeek:cur);
                    if(!allowed.includes(cur))cur='A';
                    if(!allowed.includes(act))act=cur;
                    if(n===1){cur='A';act='A'}
                    try{if(typeof currentWeek!=='undefined')currentWeek=cur;if(typeof activeWeek!=='undefined')activeWeek=act}catch(e){}
                    try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(cur)}catch(e){}

                    const sel=document.getElementById('advCycle');if(sel)sel.value=String(a.cycleLength);
                    document.body.classList.toggle('singleWeekMode',n===1);

                    requestAnimationFrame(()=>{
                      try{
                        /* Reload the advanced closure from native storage, then render once.
                           This avoids the competing synthetic onchange/click chains used before. */
                        if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                        if(typeof render==='function')render();
                        if(window.refreshStability70)window.refreshStability70();
                        if(window.refreshStability71)window.refreshStability71();
                        if(window.refreshStability72)window.refreshStability72();
                      }catch(e){}
                      applying=false;
                      normalizeButtons();
                    });
                  }catch(e){applying=false;normalizeButtons()}
                }
                window.applyWeekMode73=applyMode;

                function resolveButton(e){
                  let b=e.target&&e.target.closest?e.target.closest('#weekModeBar .weekModeChoice'):null;
                  if(!b&&Number.isFinite(e.clientX)&&Number.isFinite(e.clientY)){
                    const p=document.elementFromPoint(e.clientX,e.clientY);b=p&&p.closest?p.closest('#weekModeBar .weekModeChoice'):null;
                  }
                  return b;
                }

                /* Capture before every legacy onclick handler. Pointer-up makes Android WebView
                   taps reliable even when a prior layer rebuilt a button between down and click. */
                document.addEventListener('pointerup',e=>{
                  const b=resolveButton(e);if(!b)return;
                  e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();
                  const n=Number(b.dataset.m);lastPointerAt=Date.now();lastPointerMode=n;applyMode(n);
                },true);
                document.addEventListener('click',e=>{
                  const b=resolveButton(e);if(!b)return;
                  e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();
                  const n=Number(b.dataset.m);
                  if(Date.now()-lastPointerAt<700&&n===lastPointerMode)return;
                  applyMode(n);
                },true);

                const bar=document.getElementById('weekModeBar');
                if(bar&&!bar.__stability73Observed){
                  bar.__stability73Observed=true;
                  new MutationObserver(()=>queueNormalize()).observe(bar,{childList:true,subtree:true,attributes:true,attributeFilter:['style','class','hidden']});
                }

                function refresh(){
                  const v=document.getElementById('appVersionInfo');if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION;
                  normalizeButtons();
                }
                window.refreshStability73=refresh;
                refresh();
              }catch(e){console.log('Stability73Ui',e)}
            })();
            """;
    }
}
