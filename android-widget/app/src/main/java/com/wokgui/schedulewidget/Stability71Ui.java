package com.wokgui.schedulewidget;

final class Stability71Ui {
    private Stability71Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability71V1){if(window.refreshStability71)window.refreshStability71();return}
                window.__stability71V1=true;
                const APP_VERSION='6.11';
                let refreshQueued=false;

                const style=document.createElement('style');
                style.id='stability71Style';
                style.textContent=`
                  :root{--s71-heading-size:.84rem}

                  /* Week context and cycle selector are plain controls integrated into the page, not pills/cards. */
                  html body .contextBar{
                    height:43px!important;min-height:43px!important;padding:6px 8px!important;gap:4px!important;
                    background:var(--bg,#f6f8fb)!important;border-bottom:1px solid var(--line,#dce3eb)!important;
                    box-shadow:none!important;overflow:hidden!important
                  }
                  html body #currentWeekBtn,
                  html body #currentWeekBtn.currentWeek{
                    flex:0 0 136px!important;width:136px!important;min-width:136px!important;max-width:136px!important;
                    height:31px!important;min-height:31px!important;max-height:31px!important;margin:0!important;padding:0 3px!important;
                    display:flex!important;align-items:center!important;justify-content:center!important;box-sizing:border-box!important;
                    background:transparent!important;border:0!important;border-radius:0!important;box-shadow:none!important;
                    color:var(--ink,#111936)!important;font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1!important;
                    white-space:nowrap!important;transition:none!important;animation:none!important;transform:none!important
                  }
                  html body #currentWeekBtn .cwLetter70{color:var(--set-accent,var(--blue))!important;font-weight:900!important}
                  html body #weekTabs.weekTabs{
                    flex:1 1 auto!important;min-width:0!important;height:31px!important;display:grid!important;
                    grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:31px!important;gap:2px!important;overflow:hidden!important
                  }
                  html body #weekTabs .weekTab,
                  html body #weekTabs .weekTab.active{
                    width:100%!important;min-width:0!important;max-width:none!important;height:31px!important;min-height:31px!important;max-height:31px!important;
                    margin:0!important;padding:0 1px!important;display:flex!important;align-items:center!important;justify-content:center!important;
                    box-sizing:border-box!important;background:transparent!important;border:0!important;border-bottom:2px solid transparent!important;
                    border-radius:0!important;box-shadow:none!important;color:#536078!important;
                    font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1!important;letter-spacing:-.015em!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;transition:none!important;animation:none!important;transform:none!important
                  }
                  html body #weekTabs .weekTab.active{color:var(--set-accent,var(--blue))!important;border-bottom-color:var(--set-accent,var(--blue))!important}

                  html body #weekModeBar{
                    min-height:0!important;height:auto!important;margin:2px 0 5px!important;padding:8px 0 9px!important;
                    background:transparent!important;border:0!important;border-radius:0!important;box-shadow:none!important;contain:none!important
                  }
                  html body #weekModeBar .weekModeLabel{
                    font-size:var(--s71-heading-size)!important;font-weight:800!important;color:var(--ink,#111936)!important;line-height:1.15!important
                  }
                  html body #weekModeBar .weekModeChoices{
                    display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:34px!important;
                    width:100%!important;height:34px!important;min-height:34px!important;gap:3px!important;overflow:visible!important
                  }
                  html body #weekModeBar .weekModeChoice,
                  html body #weekModeBar .weekModeChoice.active{
                    display:flex!important;align-items:center!important;justify-content:center!important;width:100%!important;min-width:0!important;max-width:none!important;
                    height:34px!important;min-height:34px!important;max-height:34px!important;margin:0!important;padding:0 1px!important;
                    background:transparent!important;border:0!important;border-bottom:2px solid transparent!important;border-radius:0!important;box-shadow:none!important;
                    color:#536078!important;font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1!important;letter-spacing:-.015em!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;transition:none!important;animation:none!important;transform:none!important
                  }
                  html body #weekModeBar .weekModeChoice.active{color:var(--set-accent,var(--blue))!important;border-bottom-color:var(--set-accent,var(--blue))!important}

                  /* Edit view headings: same type size, centered and given more breathing room. */
                  html body #viewEdit .sectionHead{margin:14px 3px 10px!important}
                  html body #viewEdit .sectionHead h2,
                  html body #viewEdit .sectionHead h3{
                    font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1.18!important
                  }
                  html body #viewEdit .sectionHead:has(#editDayTitle){
                    display:flex!important;flex-direction:column!important;align-items:center!important;justify-content:center!important;gap:3px!important;
                    margin-top:13px!important;margin-bottom:10px!important
                  }
                  html body #editDayTitle{width:100%!important;text-align:center!important;margin:0!important}
                  html body #editCount{position:static!important;transform:none!important;width:100%!important;text-align:center!important;font-size:.68rem!important;line-height:1!important}
                  html body #viewEdit .sectionHead:has(h3){margin-top:16px!important;margin-bottom:11px!important}

                  /* Midi / trous: Today, Week and Widget are three aligned rows. */
                  html body #breakVisibility70{
                    margin-top:7px!important;padding-top:5px!important;border-top:0!important;
                    display:grid!important;grid-template-columns:88px minmax(0,1fr) minmax(0,1fr)!important;
                    column-gap:6px!important;row-gap:8px!important;align-items:center!important
                  }
                  html body #breakVisibility70>.breakVisTitle70:first-child{display:none!important}
                  html body #breakVisibility70>.breakVisRow70{display:contents!important}
                  html body #breakVisibility70>.breakVisRow70>span{
                    font-size:.72rem!important;font-weight:800!important;text-align:center!important;align-self:center!important
                  }
                  html body #breakVisibility70>.breakVisRow70 label,
                  html body #breakWidget70 label{
                    display:flex!important;align-items:center!important;justify-content:center!important;gap:5px!important;
                    margin:0!important;font-size:.72rem!important;line-height:1.1!important
                  }
                  html body #breakVisibility70>.breakVisTitle70:has(+ #breakWidget70){
                    display:flex!important;align-items:center!important;justify-content:center!important;margin:0!important;
                    font-size:.72rem!important;font-weight:800!important;color:var(--ink,#111936)!important;text-align:center!important
                  }
                  html body #breakWidget70{
                    grid-column:2 / 4!important;display:grid!important;grid-template-columns:1fr 1fr!important;gap:6px!important;margin:0!important
                  }

                  /* The redundant Week cycle settings card is hidden; the four-choice control above remains authoritative. */
                  html body #settingsSheet .settingBox:has(#advCycleTitle){display:none!important}

                  /* Settings title: larger and with more space below it. */
                  html body #settingsSheet .settingsHead{
                    min-height:38px!important;margin:0 0 12px!important;padding:0 0 7px!important;align-items:center!important
                  }
                  html body #settingsSheet .settingsHead h2{
                    font-size:1.18rem!important;font-weight:850!important;line-height:1.15!important;margin:0!important
                  }

                  @media(max-width:560px){
                    :root{--s71-heading-size:.82rem}
                    html body #currentWeekBtn,html body #currentWeekBtn.currentWeek{flex-basis:132px!important;width:132px!important;min-width:132px!important;max-width:132px!important}
                    html body #weekTabs .weekTab,html body #weekTabs .weekTab.active{letter-spacing:-.025em!important}
                    html body #weekModeBar{padding-top:9px!important;padding-bottom:10px!important}
                  }
                `;
                document.head.appendChild(style);

                function hideCycleSettings(){
                  const t=document.getElementById('advCycleTitle'),box=t&&t.closest?t.closest('.settingBox'):null;
                  if(box)box.style.setProperty('display','none','important');
                }

                function polishEditTitle(){
                  const h=document.getElementById('editDayTitle');
                  if(h){
                    let txt=String(h.textContent||'');
                    if(lang()==='fr')txt=txt.replace(/ · semaine /i,' · Semaine ');
                    if(h.textContent!==txt)h.textContent=txt;
                  }
                }

                function lang(){
                  try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                }

                function polishBreaks(){
                  const root=document.getElementById('breakVisibility70');if(!root)return;
                  const titles=[...root.querySelectorAll('.breakVisTitle70')];
                  const widgetTitle=titles.find(x=>x.nextElementSibling&&x.nextElementSibling.id==='breakWidget70');
                  if(widgetTitle&&widgetTitle.textContent!=='Widget')widgetTitle.textContent='Widget';
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION}

                function refresh(){
                  refreshQueued=false;hideCycleSettings();polishEditTitle();polishBreaks();setVersion();
                }
                window.refreshStability71=refresh;

                function scheduleRefresh(){
                  if(refreshQueued)return;refreshQueued=true;requestAnimationFrame(refresh);
                }

                function wrap(name){
                  const old=window[name];if(typeof old!=='function'||old.__stability71)return;
                  const w=function(){const r=old.apply(this,arguments);scheduleRefresh();return r};w.__stability71=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['renderEdit','refreshSettingsV3','refreshAdvancedFeatures','refreshFineTuneUi','refreshStability70','refreshWeekendUi'].forEach(wrap);

                function observe(id){
                  const root=document.getElementById(id);if(!root||root.__stability71Observed)return;
                  root.__stability71Observed=true;new MutationObserver(()=>scheduleRefresh()).observe(root,{childList:true,subtree:true});
                }
                ['settingsSheet','viewEdit','contextBar'].forEach(observe);

                refresh();setTimeout(()=>{['settingsSheet','viewEdit','contextBar'].forEach(observe);refresh()},120);
              }catch(e){console.log('Stability71Ui',e)}
            })();
            """;
    }
}
