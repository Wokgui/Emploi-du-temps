package com.wokgui.schedulewidget;

final class Stability69Ui {
    private Stability69Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability69V1){if(window.refreshStability69)window.refreshStability69();return}
                window.__stability69V1=true;
                const APP_VERSION='6.9';
                const STRONG='1.5px';
                let switching=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function toMin(v){const p=String(v||'').split(':').map(Number);return (p[0]||0)*60+(p[1]||0)}
                function currentMode(){const a=loadAdv();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2))}

                const style=document.createElement('style');
                style.id='stability69Style';
                style.textContent=`
                  :root{--week-strong-line:${STRONG}}

                  /* Four cycle choices are always present and always the same size. */
                  #weekModeBar .weekModeChoices{
                    display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;
                    grid-template-rows:32px!important;gap:4px!important;width:100%!important;min-width:0!important;
                    height:32px!important;min-height:32px!important;overflow:visible!important
                  }
                  #weekModeBar .weekModeChoice,#weekModeBar .weekModeChoice.active{
                    display:flex!important;align-items:center!important;justify-content:center!important;
                    width:100%!important;min-width:0!important;max-width:none!important;
                    height:32px!important;min-height:32px!important;max-height:32px!important;
                    margin:0!important;padding:0 2px!important;box-sizing:border-box!important;
                    border-width:1px!important;border-style:solid!important;border-radius:8px!important;
                    font-size:.61rem!important;font-weight:800!important;line-height:1!important;letter-spacing:-.01em!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;
                    transform:none!important;scale:1!important;box-shadow:none!important;
                    transition:none!important;animation:none!important;-webkit-tap-highlight-color:transparent!important
                  }
                  #weekModeBar .weekModeChoice[hidden]{display:flex!important}
                  body.cycle69Busy #weekModeBar .weekModeChoice{pointer-events:none!important}

                  /* Single-week mode keeps exactly the same context-bar geometry. */
                  body.singleWeekMode #weekTabs{display:grid!important;visibility:hidden!important;pointer-events:none!important}
                  #weekTabs,#weekTabs .weekTab,#currentWeekBtn{transition:none!important;animation:none!important;transform:none!important}

                  /* Restore the lighter strong separators and reuse exactly that thickness for lunch. */
                  #weekGrid>.wh.timecol{border-right-width:var(--week-strong-line)!important;border-right-style:solid!important;border-right-color:#cbd5e1!important}
                  #weekGrid>.wh.day{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}
                  #weekGrid>.wh.timecol:first-child{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}

                  /* Lunch has no rectangle/inner outline. Its strong edges ARE the existing grid lines. */
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell{
                    margin:0!important;padding:0!important;border-radius:0!important;outline:0!important;
                    box-shadow:none!important;background:var(--ft-midi,#FFF9E8)!important;overflow:hidden!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell::before{display:none!important;content:none!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight::after{display:none!important;content:none!important}
                  #weekGrid .lunch69TopLine{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}
                  #weekGrid .lunch69BottomLine{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}

                  /* Only one time indicator: horizontal, inside the current course cell. */
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,
                  #weekGrid [id*="WeekNow"],#weekGrid [id*="weekNow"],
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,
                  #weekGrid .nativeNowFull,#weekGrid .nativeNowPartial,#weekGrid .nativeNowDot,
                  #weekGrid .finalWeekNowRailV12,#weekGrid .finalWeekNowDotV12,
                  #weekGrid .finalWeekNowRailV13,#weekGrid .finalWeekNowDotV13{display:none!important}
                  #weekGrid .now69Course{position:relative!important;overflow:visible!important;z-index:24!important}
                  #weekGrid .now69Bar{position:absolute!important;left:0!important;right:0!important;height:2px!important;background:#1688F4!important;z-index:210!important;pointer-events:none!important}
                  #weekGrid .now69Dot{position:absolute!important;left:0!important;width:10px!important;height:10px!important;border-radius:50%!important;transform:translate(-50%,-50%)!important;background:#1688F4!important;border:2px solid #D9ECFF!important;box-sizing:border-box!important;z-index:211!important;pointer-events:none!important}
                `;
                document.head.appendChild(style);

                function ensureCycleChoices(){
                  const box=document.querySelector('#weekModeBar .weekModeChoices');if(!box)return;
                  const defs=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  for(const [v,label] of defs){
                    let b=box.querySelector('.weekModeChoice[data-m="'+v+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;box.appendChild(b)}
                    b.hidden=false;b.style.removeProperty('display');b.textContent=label;
                    b.onclick=e=>{e.preventDefault();e.stopPropagation();switchCycle(Number(v));return false};
                  }
                  const order=new Map(defs.map((x,i)=>[x[0],i]));
                  [...box.querySelectorAll('.weekModeChoice')].sort((a,b)=>(order.get(a.dataset.m)??99)-(order.get(b.dataset.m)??99)).forEach(b=>box.appendChild(b));
                  markCycleChoices();
                }

                function markCycleChoices(){
                  const m=currentMode();
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===m));
                }

                function ensureTopWeekTabs(){
                  const box=document.getElementById('weekTabs');if(!box)return;
                  const prefix=lang()==='de'?'Woche ':(lang()==='en'?'Week ':'Semaine ');
                  for(const w of ['A','B','C','D']){
                    let b=box.querySelector('.weekTab[data-week="'+w+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekTab';b.dataset.week=w;box.appendChild(b)}
                    b.textContent=prefix+w;
                  }
                }

                function syncCycleDom(n){
                  n=Number(n)||currentMode();ensureCycleChoices();ensureTopWeekTabs();
                  document.body.classList.toggle('singleWeekMode',n===1);
                  const box=document.getElementById('weekTabs');
                  if(box){
                    box.style.setProperty('visibility',n===1?'hidden':'visible','important');
                    box.style.setProperty('pointer-events',n===1?'none':'auto','important');
                    [...box.querySelectorAll('.weekTab')].forEach((b,i)=>{
                      const visible=n>1&&i<n;
                      b.style.setProperty('visibility',visible?'visible':'hidden','important');
                      b.style.pointerEvents=visible?'auto':'none';b.tabIndex=visible?0:-1;
                      const w=b.dataset.week;
                      b.classList.toggle('active',visible&&w===(typeof activeWeek!=='undefined'?activeWeek:'A'));
                      b.onclick=visible?(()=>{if(typeof activeWeek!=='undefined'&&activeWeek===w)return;if(typeof activeWeek!=='undefined')activeWeek=w;try{if(typeof render==='function')render()}catch(e){}}):null;
                    });
                  }
                  const cw=document.getElementById('currentWeekBtn');
                  if(cw){
                    if(n===1){cw.textContent=tr('Semaine unique','Single week','Einzelwoche');cw.onclick=null}
                    else{
                      const cur=typeof currentWeek!=='undefined'?currentWeek:'A';cw.innerHTML=tr('Cette semaine : ','This week: ','Diese Woche: ')+'<b>'+cur+'</b>';
                      cw.onclick=()=>{
                        const ls=['A','B','C','D'].slice(0,n),old=typeof currentWeek!=='undefined'?currentWeek:'A',idx=Math.max(0,ls.indexOf(old)),next=ls[(idx+1)%ls.length];
                        if(typeof currentWeek!=='undefined')currentWeek=next;if(typeof activeWeek!=='undefined')activeWeek=next;
                        try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(next)}catch(e){}
                        try{if(typeof render==='function')render()}catch(e){}syncCycleDom(n);
                      };
                    }
                  }
                  markCycleChoices();
                }

                function switchCycle(n){
                  n=Number(n);if(![1,2,3,4].includes(n)||switching)return;
                  const old=currentMode();if(old===n){syncCycleDom(n);return}
                  switching=true;document.body.classList.add('cycle69Busy');
                  try{
                    const a=loadAdv();a.singleWeek=n===1;a.cycleLength=n===1?2:n;saveAdv(a);
                    const beforeCurrent=typeof currentWeek!=='undefined'?currentWeek:'A';
                    const beforeActive=typeof activeWeek!=='undefined'?activeWeek:'A';
                    const allowed=['A','B','C','D'].slice(0,n===1?1:n);
                    let changedWeek=false;
                    if(n===1){
                      if(typeof currentWeek!=='undefined'&&currentWeek!=='A'){currentWeek='A';changedWeek=true}
                      if(typeof activeWeek!=='undefined'&&activeWeek!=='A'){activeWeek='A';changedWeek=true}
                      try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek('A')}catch(e){}
                    }else{
                      if(typeof currentWeek!=='undefined'&&!allowed.includes(currentWeek)){currentWeek='A';changedWeek=true;try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek('A')}catch(e){}}
                      if(typeof activeWeek!=='undefined'&&!allowed.includes(activeWeek)){activeWeek=typeof currentWeek!=='undefined'?currentWeek:'A';changedWeek=true}
                    }
                    const sel=document.getElementById('advCycle');if(sel&&n>1)sel.value=String(n);
                    syncCycleDom(n);
                    if(changedWeek){try{if(typeof render==='function')render()}catch(e){}syncCycleDom(n)}
                    else if(typeof mode!=='undefined'&&mode==='week'){paintWeek69()}
                  }catch(e){}finally{
                    document.body.classList.remove('cycle69Busy');switching=false;markCycleChoices();
                  }
                }
                window.switchCycle69=switchCycle;

                function rowsOf(grid){
                  const rows=[];if(!grid)return rows;
                  const dayCount=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.length:5;
                  for(const time of Array.from(grid.querySelectorAll(':scope > .wh.timecol'))){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(found.length<2)continue;
                    const cells=[];let n=time.nextElementSibling;
                    while(n&&cells.length<dayCount){if(n.classList&&n.classList.contains('wc'))cells.push(n);n=n.nextElementSibling}
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return rows;
                }

                function clearLunchLines(grid){
                  grid.querySelectorAll('.lunch69TopLine').forEach(x=>x.classList.remove('lunch69TopLine'));
                  grid.querySelectorAll('.lunch69BottomLine').forEach(x=>x.classList.remove('lunch69BottomLine'));
                  grid.querySelectorAll('.finalLunchCell,.finalLunchJoinedRight').forEach(c=>{
                    c.classList.remove('finalLunchCell','finalLunchJoinedRight');c.style.removeProperty('box-shadow');c.style.removeProperty('border-right-color');
                  });
                  grid.querySelectorAll('[data-midi-edge-v14="1"]').forEach(c=>{
                    c.style.removeProperty('border-right-color');c.style.removeProperty('border-bottom-color');c.removeAttribute('data-midi-edge-v14');
                  });
                }

                function isLunchCell(c){return !!(c&&(c.classList.contains('lunchCell')||c.classList.contains('dynamicLunchCell')||c.classList.contains('nativeLunchCell')))}
                function paintLunchLines(grid,rows){
                  clearLunchLines(grid);
                  rows.forEach((row,ri)=>row.cells.forEach((cell,di)=>{
                    if(!isLunchCell(cell))return;
                    cell.style.setProperty('box-shadow','none','important');cell.style.removeProperty('border-right-color');
                    cell.classList.add('lunch69BottomLine');
                    const above=ri>0?rows[ri-1].cells[di]:grid.querySelectorAll(':scope > .wh.day')[di];
                    if(above)above.classList.add('lunch69TopLine');
                  }));
                }

                function clearNow(grid){
                  grid.querySelectorAll('.now69Bar,.now69Dot,.finalNowBar,.finalNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('.now69Course,.finalNowCourse').forEach(x=>x.classList.remove('now69Course','finalNowCourse'));
                  grid.querySelectorAll('.nativeNowFull,.nativeNowPartial,.nativeNowDot,.scheduleNowRail,.scheduleNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('[id*="weekNow"],[id*="WeekNow"]').forEach(x=>x.remove());
                }

                function paintHorizontalNow(grid,rows){
                  clearNow(grid);if(!rows.length)return;
                  if(typeof mode!=='undefined'&&mode!=='week')return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}
                  const js=new Date(),day=js.getDay(),d=day===0?1:day+1;
                  const di=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.indexOf(d):-1;if(di<0)return;
                  const minute=js.getHours()*60+js.getMinutes();let row=null,frac=0;
                  for(const r of rows){if(minute>=r.start&&minute<r.end){row=r;frac=(minute-r.start)/Math.max(1,r.end-r.start);break}}
                  if(!row)return;const cell=row.cells[di];if(!cell||!cell.classList.contains('has'))return;
                  cell.classList.add('now69Course');const top=Math.max(0,Math.min(100,frac*100)).toFixed(4)+'%';
                  const bar=document.createElement('span');bar.className='now69Bar';bar.style.setProperty('top',top,'important');cell.appendChild(bar);
                  const dot=document.createElement('span');dot.className='now69Dot';dot.style.setProperty('top',top,'important');cell.appendChild(dot);
                }

                function paintWeek69(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;const rows=rowsOf(grid);paintLunchLines(grid,rows);paintHorizontalNow(grid,rows);
                }
                window.paintWeek69=paintWeek69;

                function wrapWeekRender(){
                  if(typeof window.renderWeek==='function'&&!window.renderWeek.__stability69){
                    const old=window.renderWeek;const w=function(){const r=old.apply(this,arguments);paintWeek69();return r};w.__stability69=true;window.renderWeek=w;try{renderWeek=w}catch(e){}
                  }
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){ensureCycleChoices();syncCycleDom(currentMode());wrapWeekRender();paintWeek69();setVersion()}
                window.refreshStability69=refresh;
                refresh();
                setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek69()},30000);
              }catch(e){console.log('Stability69Ui',e)}
            })();
            """;
    }
}
