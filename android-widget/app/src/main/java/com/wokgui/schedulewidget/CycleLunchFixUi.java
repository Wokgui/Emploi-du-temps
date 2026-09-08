package com.wokgui.schedulewidget;

final class CycleLunchFixUi {
    private CycleLunchFixUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__cycleLunchFixV2){
                  if(window.refreshCycleLunchFix){
                    window.refreshCycleLunchFix();
                    setTimeout(window.refreshCycleLunchFix,90);
                    setTimeout(window.refreshCycleLunchFix,260);
                  }
                  return;
                }
                window.__cycleLunchFixV2=true;
                const APP_VERSION='6.6';
                let switching=false;
                let observer=null;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function letters(n){return ['A','B','C','D'].slice(0,Math.max(2,Math.min(4,Number(n)||2)))}

                const style=document.createElement('style');
                style.id='cycleLunchFixV2Style';
                style.textContent=`
                  /* Keep the cycle selector geometrically stable while its state changes. */
                  #weekModeBar{min-height:42px!important;box-sizing:border-box!important}
                  #weekModeBar .weekModeChoices{min-height:30px!important;align-items:stretch!important}
                  #weekModeBar .weekModeChoice{height:30px!important;box-sizing:border-box!important;transition:none!important;animation:none!important;transform:none!important}
                  body.cycleSwitchBusy #weekModeBar .weekModeChoice{pointer-events:none!important}

                  /* Single-week mode uses the available context-bar width cleanly. */
                  body.singleWeekMode #currentWeekBtn{margin-left:auto!important;margin-right:auto!important}
                  body.singleWeekMode #weekTabs{display:none!important}

                  /* Joined lunch cells overlap the grid seam by two pixels, so the top and bottom strokes are continuous. */
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell{position:relative!important;overflow:visible!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight{border-right-color:transparent!important;z-index:6!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight::after{
                    content:""!important;position:absolute!important;right:-2px!important;top:0!important;bottom:0!important;width:3px!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    border-top:2px solid var(--ft-midi-border,#C7AA62)!important;
                    border-bottom:2px solid var(--ft-midi-border,#C7AA62)!important;
                    box-sizing:border-box!important;z-index:12!important;pointer-events:none!important
                  }

                  /* Center both copy actions inside Week cycle. */
                  #settingsSheet .cycleCopyCentered .advButtons{justify-content:center!important;text-align:center!important}
                  #settingsSheet .cycleCopyCentered .advButton{margin-left:auto!important;margin-right:auto!important;text-align:center!important}
                `;
                document.head.appendChild(style);

                function applySingleWeekUi(){
                  const a=loadAdv(),single=a.singleWeek===true;
                  document.body.classList.toggle('singleWeekMode',single);
                  const tabs=document.getElementById('weekTabs'),cw=document.getElementById('currentWeekBtn'),letter=document.getElementById('weekTitleLetter');
                  if(single){
                    try{if(typeof currentWeek!=='undefined')currentWeek='A';if(typeof activeWeek!=='undefined')activeWeek='A'}catch(e){}
                    if(tabs)tabs.style.setProperty('display','none','important');
                    if(cw){cw.textContent=tr('Semaine unique','Single week','Einzelwoche');cw.onclick=null}
                    if(letter)letter.textContent='A';
                  }else{
                    if(tabs)tabs.style.removeProperty('display');
                  }
                }

                function centerCycleCopy(){
                  const title=document.getElementById('advCycleTitle'),box=title&&title.closest?title.closest('.settingBox'):null;
                  if(box)box.classList.add('cycleCopyCentered');
                }

                function markCycleButtons(){
                  const a=loadAdv(),mode=a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2));
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>{
                    b.classList.toggle('active',Number(b.dataset.m)===mode);
                  });
                }

                function renderOnce(){
                  try{if(typeof render==='function')render()}catch(e){}
                  if(window.refreshFinalPolish)window.refreshFinalPolish();
                  if(window.refreshWeekendUi)window.refreshWeekendUi();
                }

                function switchCycle(n){
                  n=Number(n);if(![1,2,3,4].includes(n)||switching)return;
                  switching=true;document.body.classList.add('cycleSwitchBusy');
                  try{
                    const a=loadAdv();
                    a.singleWeek=n===1;
                    a.cycleLength=n===1?2:n;
                    saveAdv(a);

                    if(n===1){
                      try{if(typeof currentWeek!=='undefined')currentWeek='A';if(typeof activeWeek!=='undefined')activeWeek='A'}catch(e){}
                      try{if(window.AndroidSchedule&&AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek('A')}catch(e){}
                    }else{
                      const allowed=letters(n);
                      try{
                        if(typeof currentWeek!=='undefined'&&!allowed.includes(currentWeek))currentWeek='A';
                        if(typeof activeWeek!=='undefined'&&!allowed.includes(activeWeek))activeWeek=(typeof currentWeek!=='undefined'?currentWeek:'A');
                      }catch(e){}
                    }

                    /* Do not dispatch the old hidden selector: it rendered several times and caused the flash/resize. */
                    const sel=document.getElementById('advCycle');if(sel&&n>1)sel.value=String(n);
                    if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                    applySingleWeekUi();markCycleButtons();centerCycleCopy();renderOnce();
                  }catch(e){}finally{
                    requestAnimationFrame(()=>requestAnimationFrame(()=>{document.body.classList.remove('cycleSwitchBusy');switching=false;markCycleButtons();applySingleWeekUi()}));
                  }
                }

                function bindCycleButtons(){
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>{
                    if(b.__cycleLunchFixBound)return;
                    b.__cycleLunchFixBound=true;
                    b.onclick=function(e){e.preventDefault();e.stopPropagation();switchCycle(Number(b.dataset.m));return false};
                  });
                  markCycleButtons();
                }

                function bridgeLunch(){
                  /* FinalPolish determines which adjacent cells really have the same lunch interval.
                     The pseudo-element above only bridges those exact groups. */
                  if(window.refreshFinalPolish)window.refreshFinalPolish();
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}

                function watchCycleBar(){
                  const root=document.getElementById('weekModeBar');if(!root)return;
                  if(observer)observer.disconnect();
                  observer=new MutationObserver(()=>{bindCycleButtons();markCycleButtons()});
                  observer.observe(root,{childList:true,subtree:true});
                }

                function refresh(){
                  bindCycleButtons();applySingleWeekUi();centerCycleCopy();setVersion();bridgeLunch();watchCycleBar();
                }
                window.refreshCycleLunchFix=refresh;

                refresh();
                [50,140,320,700,1250].forEach(ms=>setTimeout(refresh,ms));
              }catch(e){console.log('CycleLunchFixUi',e)}
            })();
            """;
    }
}
