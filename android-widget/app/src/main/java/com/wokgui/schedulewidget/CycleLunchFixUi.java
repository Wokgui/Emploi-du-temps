package com.wokgui.schedulewidget;

final class CycleLunchFixUi {
    private CycleLunchFixUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__cycleLunchFixV3){
                  if(window.refreshCycleLunchFix)window.refreshCycleLunchFix();
                  return;
                }
                window.__cycleLunchFixV3=true;
                const APP_VERSION='6.26';
                let switching=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function letters(n){return ['A','B','C','D'].slice(0,Math.max(2,Math.min(4,Number(n)||2)))}

                const style=document.createElement('style');
                style.id='cycleLunchFixV3Style';
                style.textContent=`
                  /* The four cycle choices always occupy exactly the same geometry.
                     Active state changes colour only: never padding, border width, font size or weight. */
                  #weekModeBar{min-height:44px!important;box-sizing:border-box!important;contain:layout style!important}
                  #weekModeBar .weekModeChoices{
                    display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;
                    grid-template-rows:32px!important;gap:4px!important;height:32px!important;min-height:32px!important;
                    align-items:stretch!important;overflow:visible!important
                  }
                  #weekModeBar .weekModeChoice,
                  #weekModeBar .weekModeChoice.active{
                    width:100%!important;height:32px!important;min-height:32px!important;max-height:32px!important;min-width:0!important;
                    margin:0!important;padding:0 3px!important;box-sizing:border-box!important;
                    border-width:1px!important;border-style:solid!important;border-radius:8px!important;
                    font-size:.64rem!important;font-weight:800!important;line-height:30px!important;letter-spacing:0!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;
                    transform:none!important;scale:1!important;box-shadow:none!important;
                    transition:none!important;animation:none!important;-webkit-tap-highlight-color:transparent!important
                  }
                  body.cycleSwitchBusy #weekModeBar .weekModeChoice{pointer-events:none!important}
                  .contextBar{gap:5px!important;padding-left:7px!important;padding-right:7px!important;overflow:hidden!important}
                  #currentWeekBtn,#currentWeekBtn.currentWeek{flex:0 0 132px!important;width:132px!important;height:30px!important;min-height:30px!important;max-height:30px!important;margin:0!important;padding:0 7px!important;box-sizing:border-box!important;font-size:.68rem!important;line-height:1!important;display:flex!important;align-items:center!important;justify-content:center!important;white-space:nowrap!important;overflow:hidden!important;transition:none!important;animation:none!important;transform:none!important}
                  #weekTabs.weekTabs{flex:0 0 199px!important;width:199px!important;min-width:199px!important;max-width:199px!important;height:30px!important;display:grid!important;grid-template-columns:repeat(4,47.5px)!important;grid-template-rows:30px!important;gap:3px!important;align-items:stretch!important;overflow:hidden!important}
                  #weekTabs .weekTab,#weekTabs .weekTab.active{width:47.5px!important;height:30px!important;min-width:47.5px!important;max-width:47.5px!important;min-height:30px!important;max-height:30px!important;margin:0!important;padding:0 1px!important;box-sizing:border-box!important;border-width:1px!important;border-style:solid!important;border-radius:999px!important;font-size:.575rem!important;font-weight:800!important;line-height:1!important;letter-spacing:-.01em!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;display:flex!important;align-items:center!important;justify-content:center!important;transform:none!important;scale:1!important;box-shadow:none!important;transition:none!important;animation:none!important;-webkit-tap-highlight-color:transparent!important}
                  #weekTabs .weekTab[aria-hidden="true"]{visibility:hidden!important;pointer-events:none!important}
                  body.singleWeekMode #currentWeekBtn{margin:0!important}
                  body.singleWeekMode #weekTabs{visibility:hidden!important;display:grid!important}

                  /* Midi is painted by one stable pseudo-layer instead of alternating legacy borders/box-shadows.
                     Its outline is 2 px: exactly the same thickness as the hours-column and days-row separators. */
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell{
                    position:relative!important;overflow:visible!important;border-radius:0!important;outline:0!important;
                    background:var(--ft-midi,#FFF9E8)!important;box-shadow:none!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell::before{display:none!important;content:none!important;
                    content:""!important;position:absolute!important;left:0!important;right:0;top:0!important;bottom:0!important;
                    z-index:0!important;pointer-events:none!important;box-sizing:border-box!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    border-top:2px solid var(--ft-midi-border,#C7AA62)!important;
                    border-bottom:2px solid var(--ft-midi-border,#C7AA62)!important;
                    border-left:0 solid transparent!important;border-right:0 solid transparent!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wh.timecol + .wc.lunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc:not(.lunchCell) + .wc.lunchCell::before{
                    border-left-width:2px!important;border-left-color:var(--ft-midi-border,#C7AA62)!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell:not(:has(+ .wc.lunchCell))::before{
                    border-right-width:2px!important;border-right-color:var(--ft-midi-border,#C7AA62)!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell:has(+ .wc.lunchCell){
                    border-right-color:transparent!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell:has(+ .wc.lunchCell)::before{
                    right:-2px!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .dynamicLunchOverlay,
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .nativeLunchLabel{
                    inset:0!important;border:0!important;border-radius:0!important;box-shadow:none!important;
                    background:transparent!important;z-index:2!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .cellLabel,
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .breakFitLabel{position:relative!important;z-index:3!important}

                  /* Older finalLunch classes may still be present, but they no longer alter the visible band. */
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight{
                    box-shadow:none!important;border-radius:0!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight::after{display:none!important}

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
                  }else if(tabs){
                    tabs.style.removeProperty('display');
                  }
                }

                function centerCycleCopy(){
                  const title=document.getElementById('advCycleTitle'),box=title&&title.closest?title.closest('.settingBox'):null;
                  if(box)box.classList.add('cycleCopyCentered');
                }

                function markCycleButtons(){
                  const a=loadAdv(),mode=a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2));
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===mode));
                }

                function switchCycle(n){
                  n=Number(n);if(![1,2,3,4].includes(n)||switching)return;
                  const a0=loadAdv(),oldMode=a0.singleWeek===true?1:Math.max(2,Math.min(4,Number(a0.cycleLength)||2));
                  if(oldMode===n){markCycleButtons();return}
                  switching=true;document.body.classList.add('cycleSwitchBusy');
                  try{
                    const a=loadAdv();a.singleWeek=n===1;a.cycleLength=n===1?2:n;saveAdv(a);
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
                    const sel=document.getElementById('advCycle');if(sel&&n>1)sel.value=String(n);

                    /* One state refresh, then one timetable render. No synthetic onchange and no repeated delayed renders. */
                    if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                    applySingleWeekUi();centerCycleCopy();markCycleButtons();
                    try{if(typeof render==='function')render()}catch(e){}
                    requestAnimationFrame(()=>{
                      try{if(window.refreshFinalPolish)window.refreshFinalPolish()}catch(e){}
                      applySingleWeekUi();markCycleButtons();
                      document.body.classList.remove('cycleSwitchBusy');switching=false;
                    });
                  }catch(e){document.body.classList.remove('cycleSwitchBusy');switching=false}
                }
                window.switchCycleStable=switchCycle;

                function bindCycleButtons(){
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>{
                    if(b.__cycleLunchFixBound)return;b.__cycleLunchFixBound=true;
                    b.onclick=function(e){e.preventDefault();e.stopPropagation();switchCycle(Number(b.dataset.m));return false};
                  });
                  markCycleButtons();
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}

                function refresh(){bindCycleButtons();applySingleWeekUi();centerCycleCopy();setVersion()}
                window.refreshCycleLunchFix=refresh;

                refresh();
                /* One late pass is enough because this layer is injected after FineTuneUi. */

              }catch(e){console.log('CycleLunchFixUi',e)}
            })();
            """;
    }
}
