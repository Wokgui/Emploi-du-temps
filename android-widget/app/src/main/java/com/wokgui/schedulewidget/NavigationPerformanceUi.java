package com.wokgui.schedulewidget;

/** Makes bottom-tab switches display-only until underlying data is actually invalidated. */
final class NavigationPerformanceUi {
    private NavigationPerformanceUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtNavigationCacheV2){
                  try{window.__edtNavigationCacheV2.rebind()}catch(e){}
                  return;
                }

                const cache={
                  dirty:{today:true,week:true,edit:true},
                  navs:0,hits:0,cancelled:0,externalRenders:0,
                  renders:{today:0,week:0,edit:0},token:0
                };

                function currentMode(){
                  try{return (typeof mode!=='undefined'&&['today','week','edit'].includes(mode))?mode:'edit'}catch(e){return 'edit'}
                }
                function ready(target){
                  const id=target==='today'?'todayList':(target==='week'?'weekGrid':'editList');
                  const root=document.getElementById(id);
                  if(!root||root.childElementCount===0)return false;
                  if(target==='edit'){
                    const tabs=document.getElementById('dayTabs'),slotBox=document.getElementById('slotSettings');
                    if(!tabs||tabs.childElementCount===0||!slotBox||slotBox.childElementCount===0)return false;
                  }
                  return true;
                }
                function activate(target,button){
                  document.querySelectorAll('.nav').forEach(function(b){b.classList.toggle('active',button?b===button:b.dataset.mode===target)});
                  document.querySelectorAll('.view').forEach(function(v){v.classList.remove('active')});
                  const id='view'+target.charAt(0).toUpperCase()+target.slice(1),view=document.getElementById(id);
                  if(view)view.classList.add('active');
                  try{if(typeof mode!=='undefined')mode=target}catch(e){}
                }
                function syncContext(target){
                  try{
                    const wanted=target==='today'?(typeof currentWeek!=='undefined'?currentWeek:'A'):(typeof activeWeek!=='undefined'?activeWeek:'A');
                    document.querySelectorAll('.weekTab').forEach(function(b){b.classList.toggle('active',b.dataset.week===wanted)});
                    if(target==='week'){
                      const letter=document.getElementById('weekTitleLetter');
                      if(letter&&letter.textContent!==String(wanted))letter.textContent=String(wanted);
                    }
                  }catch(e){}
                }
                function invalidate(targets){
                  if(!targets){cache.dirty.today=cache.dirty.week=cache.dirty.edit=true;return}
                  (Array.isArray(targets)?targets:[targets]).forEach(function(t){if(Object.prototype.hasOwnProperty.call(cache.dirty,t))cache.dirty[t]=true});
                }
                function renderTarget(target){
                  syncContext(target);
                  if(!ready(target)||cache.dirty[target]){
                    if(target==='today'&&typeof renderToday==='function')renderToday();
                    else if(target==='week'&&typeof renderWeek==='function')renderWeek();
                    else if(target==='edit'&&typeof renderEdit==='function')renderEdit();
                    cache.renders[target]=(cache.renders[target]||0)+1;
                    cache.dirty[target]=false;
                    return 'render';
                  }
                  cache.hits++;
                  return 'hit';
                }
                function stats(n,settle){
                  if(n%50!==0)return;
                  console.log('EDT_NAV_STATS|navs='+cache.navs+'|hits='+cache.hits+'|renderToday='+cache.renders.today+'|renderWeek='+cache.renders.week+'|renderEdit='+cache.renders.edit+'|cancelled='+cache.cancelled+'|external='+cache.externalRenders+'|fastWrapped=0|dirtyToday='+(cache.dirty.today?1:0)+'|dirtyWeek='+(cache.dirty.week?1:0)+'|dirtyEdit='+(cache.dirty.edit?1:0)+'|settleMs='+Math.round(settle||0));
                }
                function own(button){
                  if(!button||!button.dataset||!['today','week','edit'].includes(button.dataset.mode))return;
                  if(button.__edtZeroRenderOwned)return;
                  button.__edtZeroRenderOwned=true;
                  button.onclick=function(){
                    const target=button.dataset.mode,n=++cache.navs,started=performance.now();
                    activate(target,button);
                    console.log('EDT_NAV_INPUT|'+target+'|n='+n);
                    const token=++cache.token;
                    requestAnimationFrame(function(){
                      if(token!==cache.token){cache.cancelled++;stats(n,performance.now()-started);return}
                      const outcome=renderTarget(target);
                      requestAnimationFrame(function(){
                        const elapsed=performance.now()-started;
                        if(n%50===0)console.log('EDT_NAV_SETTLE|n='+n+'|target='+target+'|outcome='+outcome+'|ms='+Math.round(elapsed));
                        stats(n,elapsed);
                      });
                    });
                    return false;
                  };
                }
                function rebind(){document.querySelectorAll('.nav').forEach(own)}

                const oldRender=window.render;
                if(typeof oldRender==='function'&&!oldRender.__edtZeroRenderWrapped){
                  const wrapped=function(){
                    invalidate();
                    const out=oldRender.apply(this,arguments);
                    try{cache.dirty[currentMode()]=false;cache.externalRenders++}catch(e){}
                    return out;
                  };
                  wrapped.__edtZeroRenderWrapped=true;
                  wrapped.__edtZeroRenderOriginal=oldRender;
                  window.render=wrapped;
                  try{render=wrapped}catch(e){}
                }

                document.addEventListener('pointerdown',function(e){
                  const button=e.target&&e.target.closest?e.target.closest('.nav'):null;
                  if(button&&button.dataset&&['today','week','edit'].includes(button.dataset.mode))activate(button.dataset.mode,button);
                },{capture:true,passive:true});

                try{cache.dirty[currentMode()]=false}catch(e){}
                window.__edtNavigationCacheV2={cache:cache,rebind:rebind,invalidate:invalidate};
                window.invalidateTimetableViews=invalidate;
                rebind();
                console.log('EDT_NAV_CACHE|ready|zero-render');
              }catch(e){console.log('NavigationPerformanceUi',e)}
            })();
            """;
    }
}
