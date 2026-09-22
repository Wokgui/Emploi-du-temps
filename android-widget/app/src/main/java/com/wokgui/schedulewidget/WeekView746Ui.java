package com.wokgui.schedulewidget;

/** 7.46: stable week paint owner; prevents blank intermediate frames and title flicker. */
final class WeekView746Ui {
    private WeekView746Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekView746){window.refreshWeekView746&&window.refreshWeekView746();return}
                window.__weekView746=true;

                const style=document.createElement('style');
                style.id='weekView746Style';
                style.textContent=\`
                  html body #viewWeek .weekTop{
                    position:relative!important;
                    min-height:36px!important;
                  }
                  /* Keep the legacy h2 as the layout/semantic source but never expose
                     its intermediate rewrites. The stable mirror below is the only paint. */
                  html body #viewWeek .weekTop>h2{
                    opacity:0!important;
                    pointer-events:none!important;
                  }
                  html body #viewWeek #weekTitleStable746{
                    grid-column:2!important;
                    grid-row:1!important;
                    align-self:center!important;
                    justify-self:center!important;
                    display:flex!important;
                    align-items:baseline!important;
                    justify-content:center!important;
                    gap:7px!important;
                    min-width:0!important;
                    min-height:20px!important;
                    margin:0!important;
                    padding:0!important;
                    line-height:1.08!important;
                    white-space:nowrap!important;
                    pointer-events:none!important;
                    z-index:5!important;
                  }
                  html body #viewWeek #weekTitleStable746 .weekName746,
                  html body #viewWeek #weekTitleStable746 .weekRange746{
                    display:inline-block!important;
                    font-size:1rem!important;
                    white-space:nowrap!important;
                  }
                  html body #viewWeek #weekTitleStable746 .weekName746{
                    color:var(--ink,#111936)!important;
                    font-weight:850!important;
                  }
                  html body #viewWeek #weekTitleStable746 .weekRange746{
                    color:#536078!important;
                    font-weight:760!important;
                  }
                  html body #viewWeek.active #weekGrid{
                    visibility:visible!important;
                    opacity:1!important;
                    min-height:var(--week746-stable-height,0px)!important;
                    transition:none!important;
                    animation:none!important;
                  }
                \`;
                document.head.appendChild(style);

                let rendering746=false;
                let settling746=false;
                let retry746=false;
                let lastGoodHtml746='';
                let lastGoodCount746=0;
                const stableHeights746=new Map();

                function grid746(){return document.getElementById('weekGrid')}
                function title746(){return document.querySelector('#viewWeek .weekTop>h2')}
                function validGrid746(grid){
                  if(!grid)return false;
                  const heads=grid.querySelectorAll(':scope>.wh.day').length;
                  const times=grid.querySelectorAll(':scope>.wh.timecol').length;
                  const cells=grid.querySelectorAll(':scope>.wc').length;
                  return heads>0&&times>0&&cells>=heads;
                }
                function geometryKey746(grid){
                  const rows=grid?grid.querySelectorAll(':scope>.wh.timecol').length:0;
                  const width=grid?Math.round((grid.clientWidth||0)/20):0;
                  const font=Math.round((parseFloat(getComputedStyle(document.documentElement).fontSize)||16)*10);
                  return rows+':'+width+':'+font;
                }
                function lockHeight746(grid){
                  if(!grid)return;
                  const h=Math.ceil(grid.getBoundingClientRect().height||0);
                  if(h>20)grid.style.setProperty('--week746-stable-height',h+'px');
                }
                function rememberHeight746(grid){
                  if(!grid)return;
                  const h=Math.ceil(grid.scrollHeight||grid.getBoundingClientRect().height||0);
                  if(h<=20)return;
                  const key=geometryKey746(grid);
                  const old=stableHeights746.get(key)||0;
                  const stable=Math.max(old,h);
                  stableHeights746.set(key,stable);
                  grid.style.setProperty('--week746-stable-height',stable+'px');
                }

                function ensureStableTitle746(){
                  const root=document.querySelector('#viewWeek .weekTop');
                  const legacy=title746();
                  if(!root||!legacy)return;
                  let mirror=document.getElementById('weekTitleStable746');
                  if(!mirror){
                    mirror=document.createElement('div');
                    mirror.id='weekTitleStable746';
                    mirror.setAttribute('aria-hidden','true');
                    const name=document.createElement('span');name.className='weekName746';
                    const range=document.createElement('span');range.className='weekRange746';
                    mirror.append(name,range);
                    root.appendChild(mirror);
                  }
                  const sourceName=legacy.querySelector('#weekName744');
                  const sourceRange=legacy.querySelector('#weekRange744');
                  const name=mirror.querySelector('.weekName746');
                  const range=mirror.querySelector('.weekRange746');
                  const nextName=String(sourceName?.textContent||'').trim();
                  const nextRange=String(sourceRange?.textContent||'').trim();

                  /* Never replace a valid painted title with an intermediate empty one. */
                  if(nextName&&/^Semaine\\s+/i.test(nextName)&&name.textContent!==nextName)name.textContent=nextName;
                  if(nextRange&&/[0-9]{2}\\/[0-9]{2}/.test(nextRange)&&range.textContent!==nextRange)range.textContent=nextRange;

                  if(!name.textContent){
                    const raw=String(legacy.textContent||'').trim();
                    if(raw)name.textContent=raw;
                  }
                }

                function rememberGood746(grid){
                  if(!validGrid746(grid))return false;
                  const html=grid.innerHTML;
                  if(html&&html.length>50){
                    lastGoodHtml746=html;
                    lastGoodCount746=grid.childElementCount;
                  }
                  rememberHeight746(grid);
                  return true;
                }

                function recover746(grid){
                  if(!grid||validGrid746(grid))return true;
                  if(lastGoodHtml746){
                    grid.innerHTML=lastGoodHtml746;
                    grid.dataset.edt746Recovered='1';
                    return validGrid746(grid);
                  }
                  return false;
                }

                function settle746(){
                  if(settling746)return;
                  settling746=true;
                  try{
                    const grid=grid746();
                    ensureStableTitle746();
                    const ok=rememberGood746(grid)||recover746(grid);
                    if(ok){
                      rememberGood746(grid);
                      retry746=false;
                      document.documentElement.dataset.edtWeek746='1';
                    }else if(!retry746&&document.getElementById('viewWeek')?.classList.contains('active')){
                      retry746=true;
                      requestAnimationFrame(()=>{
                        retry746=false;
                        try{if(typeof window.renderWeek==='function')window.renderWeek()}catch(e){}
                      });
                    }
                  }finally{settling746=false}
                }

                const previousRender746=window.renderWeek;
                function render746(){
                  if(rendering746){
                    return typeof previousRender746==='function'?previousRender746.apply(this,arguments):undefined;
                  }
                  rendering746=true;
                  const grid=grid746();
                  lockHeight746(grid);
                  let result;
                  try{
                    if(typeof previousRender746==='function')result=previousRender746.apply(this,arguments);
                  }catch(e){
                    console.error('WeekView746Ui render',e);
                  }
                  try{
                    if(typeof window.refreshWeekView744==='function')window.refreshWeekView744();
                  }catch(e){}
                  settle746();
                  rendering746=false;
                  return result;
                }
                render746.__week746=true;
                window.renderWeek=render746;
                try{renderWeek=render746}catch(e){}
                window.refreshWeekView746=settle746;

                const grid=grid746();
                if(grid&&!grid.__edt746Observed){
                  grid.__edt746Observed=true;
                  let queued=false;
                  new MutationObserver(()=>{
                    if(queued||rendering746||settling746)return;
                    queued=true;
                    queueMicrotask(()=>{queued=false;settle746()});
                  }).observe(grid,{childList:true,subtree:false});
                }
                const legacyTitle=title746();
                if(legacyTitle&&!legacyTitle.__edt746Observed){
                  legacyTitle.__edt746Observed=true;
                  let queued=false;
                  new MutationObserver(()=>{
                    if(queued)return;
                    queued=true;
                    queueMicrotask(()=>{queued=false;ensureStableTitle746()});
                  }).observe(legacyTitle,{childList:true,subtree:true,characterData:true});
                }

                document.addEventListener('pointerdown',event=>{
                  const nav=event.target&&event.target.closest?event.target.closest('.bottom .nav[data-mode="week"],#weekPrev728,#weekNext728'):null;
                  if(nav){
                    lockHeight746(grid746());
                    ensureStableTitle746();
                  }
                },true);

                document.addEventListener('visibilitychange',()=>{
                  if(!document.hidden)queueMicrotask(settle746);
                });

                settle746();
              }catch(e){console.error('WeekView746Ui',e)}
            })();
            """;
    }
}
