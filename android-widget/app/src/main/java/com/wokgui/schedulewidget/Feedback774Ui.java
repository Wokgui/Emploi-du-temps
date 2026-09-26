package com.wokgui.schedulewidget;

/** 7.74 final owner for seamless class colours and settings finishing. */
final class Feedback774Ui {
    private Feedback774Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback774){window.refreshFeedback774&&window.refreshFeedback774();return}
                window.__feedback774=true;
                const VERSION='7.74';
                let scheduled=false;
                const style=document.createElement('style');style.id='feedback774Style';style.textContent=`
                  #settingsSheet .settingsHead{position:relative!important;justify-content:flex-end!important}
                  #settingsSheet #settingsGear774{position:absolute!important;left:14px!important;top:50%!important;transform:translateY(-50%)!important;width:27px!important;height:27px!important;display:flex!important;align-items:center!important;justify-content:center!important;color:#253047!important;pointer-events:none!important}
                  #settingsSheet #settingsGear774 svg{display:block!important;width:25px!important;height:25px!important;overflow:visible!important}
                  #settingsSheet #settingsTitle{left:50%!important;right:auto!important;transform:translate(-50%,-50%)!important;text-align:center!important}
                  #colorSettings86 #themeGrid{grid-template-columns:repeat(auto-fit,minmax(78px,1fr))!important;align-items:stretch!important;gap:7px!important}
                  #colorSettings86 #themeGrid .themeButton{display:flex!important;flex-direction:column!important;align-items:stretch!important;justify-content:flex-start!important;min-width:0!important;min-height:78px!important;height:auto!important;padding:5px!important;box-sizing:border-box!important;overflow:visible!important}
                  #colorSettings86 #themeGrid .themeSwatch{flex:0 0 25px!important;width:100%!important}
                  #colorSettings86 #themeGrid .themeName{flex:1 1 auto!important;width:100%!important;min-height:2.3em!important;display:flex!important;align-items:center!important;justify-content:center!important;white-space:normal!important;overflow:visible!important;text-overflow:clip!important;overflow-wrap:break-word!important;word-break:normal!important;hyphens:auto!important;line-height:1.15!important}
                  #advancedSettings85 #advRangeLabel{appearance:none!important;-webkit-appearance:none!important;box-sizing:border-box!important;width:100%!important;border:1px solid #cfd9e5!important;outline:0!important;box-shadow:none!important;background:#fff!important}
                  #advancedSettings85 #advRangeLabel:focus{border-color:var(--set-accent,#0877f9)!important;outline:0!important;box-shadow:0 0 0 1px var(--set-accent,#0877f9)!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.classTint{position:relative!important;overflow:visible!important;isolation:isolate!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.classTint:before{top:-1px!important;bottom:-1px!important;height:auto!important;border-radius:0!important;box-shadow:none!important}
                  html body #todayList .todayCourse.classTint,html body #editList .editCourse.classTint{position:relative!important;overflow:visible!important;border-left:0!important}
                  html body #todayList .todayCourse.classTint:before,html body #editList .editCourse.classTint:before{content:''!important;position:absolute!important;left:0!important;top:-1px!important;bottom:-1px!important;width:4px!important;height:auto!important;background:var(--class-color)!important;border:0!important;border-radius:0!important;box-shadow:none!important;z-index:3!important;pointer-events:none!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Course.classFill,html body #viewWeek #weekGrid#weekGrid .wc.has.classFill,html body #todayList .todayCourse.classFill:not(.gap):not(.lunch),html body #editList .editCourse.classFill{background:var(--class-color)!important;background-color:var(--class-color)!important;border-color:var(--class-color)!important;box-shadow:none!important}
                  html body #viewWeek #weekGrid#weekGrid>.week658LunchRail{height:2px!important;min-height:2px!important;max-height:2px!important}
                  @media(max-width:350px){#colorSettings86 #themeGrid{grid-template-columns:repeat(auto-fit,minmax(72px,1fr))!important}}
                `;document.head.appendChild(style);

                function installGear(){
                  const head=document.querySelector('#settingsSheet .settingsHead');if(!head)return;
                  let gear=document.getElementById('settingsGear774');if(!gear){
                    gear=document.createElement('span');gear.id='settingsGear774';gear.setAttribute('aria-hidden','true');
                    gear.innerHTML='<svg viewBox="0 0 24 24" focusable="false" aria-hidden="true"><path fill="currentColor" d="M19.43 12.98c.04-.32.07-.65.07-.98s-.03-.66-.08-.98l2.11-1.65a.5.5 0 0 0 .12-.64l-2-3.46a.5.5 0 0 0-.61-.22l-2.49 1a7.3 7.3 0 0 0-1.69-.98L14.5 2.42A.49.49 0 0 0 14 2h-4a.49.49 0 0 0-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98l-2.49-1a.49.49 0 0 0-.61.22l-2 3.46a.49.49 0 0 0 .12.64l2.11 1.65c-.04.32-.08.66-.08.98s.03.66.08.98l-2.11 1.65a.5.5 0 0 0-.12.64l2 3.46a.5.5 0 0 0 .61.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.04.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.58 1.69-.98l2.49 1c.23.08.49 0 .61-.22l2-3.46a.5.5 0 0 0-.12-.64l-2.11-1.65ZM12 15.5A3.5 3.5 0 1 1 12 8a3.5 3.5 0 0 1 0 7.5Z"/></svg>';
                    head.insertBefore(gear,head.firstChild);
                  }
                }
                function removeDuplicateBreakRows(){
                  for(const id of ['advAppShowBreaks767','advAppShowLunch767','advShowBreaks','advShowLunch']){
                    const control=document.getElementById(id);if(!control)continue;const cell=control.closest('.displayCell767');
                    if(cell){const label=cell.previousElementSibling,widget=cell.nextElementSibling;label?.remove();cell.remove();widget?.remove();continue}
                    const legacy=control.closest('.advCheck,.advRow');if(legacy)legacy.remove();else control.remove();
                  }
                }
                function refresh(){
                  scheduled=false;installGear();removeDuplicateBreakRows();
                  const version=document.getElementById('appVersionInfo');if(version)version.textContent='Version '+VERSION;
                }
                function schedule(){if(scheduled)return;scheduled=true;requestAnimationFrame(refresh)}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback774)return;const next=function(){const result=old.apply(this,arguments);schedule();return result};next.__feedback774=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                window.refreshFeedback774=refresh;
                ['render','renderToday','renderWeek','renderEdit','refreshSettingsLayout','refreshAdvancedFeatures','prepareSettingsOpen665'].forEach(wrap);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback774Ui',e)}
            })();
            """;
    }
}
