package com.wokgui.schedulewidget;

/** Keeps repeated bottom-tab navigation cheap by reusing already-rendered views. */
final class NavigationPerformanceUi {
    private NavigationPerformanceUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtNavigationCacheV1){
                  try{window.__edtNavigationCacheV1.rebind()}catch(e){}
                  return;
                }

                const cache={
                  sig:Object.create(null),
                  navs:0,
                  hits:0,
                  cancelled:0,
                  externalRenders:0,
                  renders:{today:0,week:0,edit:0},
                  token:0
                };

                function safe(v){
                  try{return JSON.stringify(v==null?null:v)||''}catch(e){return ''}
                }
                function advancedRaw(){
                  try{return window.AndroidSchedule&&AndroidSchedule.loadAdvancedSettings?String(AndroidSchedule.loadAdvancedSettings()||''):''}catch(e){return ''}
                }
                function currentMode(){
                  try{return (typeof mode!=='undefined'&&['today','week','edit'].includes(mode))?mode:'edit'}catch(e){return 'edit'}
                }
                function todayDay(){
                  try{return typeof todayKey==='function'?todayKey():(new Date().getDay()||1)+1}catch(e){return 2}
                }
                function signature(target){
                  try{
                    const slotsSig=typeof slots!=='undefined'?safe(slots):'';
                    const breaksSig=typeof breaks!=='undefined'?safe(breaks):'';
                    const daysSig=typeof DAYS!=='undefined'?safe(DAYS):'';
                    const adv=advancedRaw();
                    if(target==='week'){
                      const w=typeof activeWeek!=='undefined'?activeWeek:'A';
                      const data=(typeof weeks!=='undefined'&&weeks[w])?weeks[w]:null;
                      return 'w|'+w+'|'+daysSig+'|'+slotsSig+'|'+breaksSig+'|'+adv+'|'+safe(data);
                    }
                    if(target==='edit'){
                      const w=typeof activeWeek!=='undefined'?activeWeek:'A';
                      const d=typeof selected!=='undefined'?selected:2;
                      const data=(typeof weeks!=='undefined'&&weeks[w]&&weeks[w][d])?weeks[w][d]:null;
                      return 'e|'+w+'|'+d+'|'+daysSig+'|'+slotsSig+'|'+breaksSig+'|'+adv+'|'+safe(data);
                    }
                    const w=typeof currentWeek!=='undefined'?currentWeek:'A';
                    const d=todayDay();
                    const data=(typeof weeks!=='undefined'&&weeks[w]&&weeks[w][d])?weeks[w][d]:null;
                    const minute=Math.floor(Date.now()/60000);
                    return 't|'+w+'|'+d+'|'+minute+'|'+daysSig+'|'+slotsSig+'|'+breaksSig+'|'+adv+'|'+safe(data);
                  }catch(e){return target+'|fallback|'+Date.now()}
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
                  try{
                    document.querySelectorAll('.nav').forEach(function(b){b.classList.toggle('active',button?b===button:b.dataset.mode===target)});
                    document.querySelectorAll('.view').forEach(function(v){v.classList.remove('active')});
                    const id='view'+target.charAt(0).toUpperCase()+target.slice(1),view=document.getElementById(id);
                    if(view)view.classList.add('active');
                  }catch(e){}
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
                function lightweightRefresh(target){
                  try{
                    if(target==='week'&&typeof window.paintWeek70==='function')window.paintWeek70();
                    if(target==='today'&&typeof window.updateTemporalState==='function')window.updateTemporalState();
                  }catch(e){}
                }
                function renderTarget(target){
                  try{if(typeof mode!=='undefined')mode=target}catch(e){}
                  syncContext(target);
                  const before=signature(target);
                  if(!ready(target)||cache.sig[target]!==before){
                    if(target==='today'&&typeof renderToday==='function')renderToday();
                    else if(target==='week'&&typeof renderWeek==='function')renderWeek();
                    else if(target==='edit'&&typeof renderEdit==='function')renderEdit();
                    cache.renders[target]=(cache.renders[target]||0)+1;
                    cache.sig[target]=signature(target);
                    return 'render';
                  }
                  cache.hits++;
                  lightweightRefresh(target);
                  return 'hit';
                }
                function stats(n,settle){
                  if(n%25!==0)return;
                  let fastWrapped=-1;
                  try{fastWrapped=window.__edtFastInteractionV2?window.__edtFastInteractionV2.state.wrapped:-1}catch(e){}
                  console.log('EDT_NAV_STATS|navs='+cache.navs+'|hits='+cache.hits+'|renderToday='+cache.renders.today+'|renderWeek='+cache.renders.week+'|renderEdit='+cache.renders.edit+'|cancelled='+cache.cancelled+'|external='+cache.externalRenders+'|fastWrapped='+fastWrapped+'|settleMs='+Math.round(settle||0));
                }
                function own(button){
                  if(!button||!button.dataset||!['today','week','edit'].includes(button.dataset.mode))return;
                  button.__edtNavCacheOwned=true;
                  button.onclick=function(){
                    const target=button.dataset.mode;
                    const n=++cache.navs;
                    const started=performance.now();
                    activate(target,button);
                    console.log('EDT_NAV_INPUT|'+target+'|n='+n);
                    const token=++cache.token;
                    requestAnimationFrame(function(){
                      if(token!==cache.token){cache.cancelled++;stats(n,performance.now()-started);return}
                      const outcome=renderTarget(target);
                      requestAnimationFrame(function(){
                        const elapsed=performance.now()-started;
                        if(n%25===0)console.log('EDT_NAV_SETTLE|n='+n+'|target='+target+'|outcome='+outcome+'|ms='+Math.round(elapsed));
                        stats(n,elapsed);
                      });
                    });
                    return false;
                  };
                }
                function rebind(){document.querySelectorAll('.nav').forEach(own)}

                const oldRender=window.render;
                if(typeof oldRender==='function'&&!oldRender.__edtNavCacheWrapped){
                  const wrapped=function(){
                    const out=oldRender.apply(this,arguments);
                    try{const m=currentMode();cache.sig[m]=signature(m);cache.externalRenders++}catch(e){}
                    return out;
                  };
                  wrapped.__edtNavCacheWrapped=true;
                  wrapped.__edtNavCacheOriginal=oldRender;
                  window.render=wrapped;
                  try{render=wrapped}catch(e){}
                }

                document.addEventListener('pointerdown',function(e){
                  const button=e.target&&e.target.closest?e.target.closest('.nav'):null;
                  if(button&&button.dataset&&['today','week','edit'].includes(button.dataset.mode))activate(button.dataset.mode,button);
                },{capture:true,passive:true});

                try{cache.sig[currentMode()]=signature(currentMode())}catch(e){}
                window.__edtNavigationCacheV1={cache:cache,rebind:rebind,signature:signature};
                rebind();
                console.log('EDT_NAV_CACHE|ready');
              }catch(e){console.log('NavigationPerformanceUi',e)}
            })();
            """;
    }
}
