package com.wokgui.schedulewidget;

final class WeekViewStabilityUi {
    private WeekViewStabilityUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekViewStabilityV1){
                  if(window.refreshWeekViewStability)window.refreshWeekViewStability();
                  return;
                }
                window.__weekViewStabilityV1=true;

                function loadAdv(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}
                  catch(e){return {}}
                }
                function oneWeek(){return loadAdv().singleWeek===true}

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

                function applySingleUi(){
                  const one=oneWeek();
                  document.documentElement.classList.toggle('singleWeekMode',one);
                  const span=ensureTitleSpan();
                  if(!one)return;
                  ensureSingleState();
                  if(span)span.textContent='A';
                  const cw=document.getElementById('currentWeekBtn');
                  if(cw)cw.innerHTML='Semaine unique';
                  const edit=document.getElementById('editDayTitle');
                  if(edit)edit.textContent=(edit.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');
                  const today=document.getElementById('todayTitle');
                  if(today)today.textContent=(today.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');
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
                }

                function wrapRender(){
                  if(typeof window.render!=='function'||window.render.__weekStableWrapped)return;
                  const old=window.render;
                  const wrapped=function(){
                    if(oneWeek()){
                      ensureSingleState();
                      ensureTitleSpan();
                    }
                    const out=old.apply(this,arguments);
                    applySingleUi();
                    if(oneWeek()&&typeof mode!=='undefined'&&mode==='week'){
                      const grid=document.getElementById('weekGrid');
                      if(!grid||grid.children.length<6)setTimeout(repairWeek,0);
                    }
                    return out;
                  };
                  wrapped.__weekStableWrapped=true;
                  window.render=wrapped;
                }

                function wrapBulkRefresh(){
                  if(typeof window.refreshBulkCourseUi!=='function'||window.refreshBulkCourseUi.__weekStableWrapped)return;
                  const old=window.refreshBulkCourseUi;
                  const wrapped=function(){
                    ensureTitleSpan();
                    const out=old.apply(this,arguments);
                    ensureTitleSpan();
                    applySingleUi();
                    if(oneWeek()&&document.getElementById('viewWeek')?.classList.contains('active'))setTimeout(repairWeek,0);
                    return out;
                  };
                  wrapped.__weekStableWrapped=true;
                  window.refreshBulkCourseUi=wrapped;
                }

                function bindWeekTab(){
                  const nav=document.querySelector('.nav[data-mode="week"]');
                  if(!nav||nav.dataset.weekStableBound)return;
                  nav.dataset.weekStableBound='1';
                  nav.addEventListener('click',()=>{
                    if(oneWeek()){
                      ensureSingleState();
                      ensureTitleSpan();
                    }
                  },true);
                  nav.addEventListener('click',()=>setTimeout(repairWeek,0));
                }

                function refresh(){
                  ensureTitleSpan();
                  wrapRender();
                  wrapBulkRefresh();
                  bindWeekTab();
                  applySingleUi();
                  if(oneWeek()&&document.getElementById('viewWeek')?.classList.contains('active'))repairWeek();
                }

                window.refreshWeekViewStability=refresh;
                refresh();
                setTimeout(refresh,80);
                setTimeout(refresh,400);
              }catch(e){console.log('WeekViewStabilityUi',e)}
            })();
            """;
    }
}
