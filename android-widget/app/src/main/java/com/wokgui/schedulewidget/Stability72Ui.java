package com.wokgui.schedulewidget;

final class Stability72Ui {
    private Stability72Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability72V1){if(window.refreshStability72)window.refreshStability72();return}
                window.__stability72V1=true;
                const APP_VERSION='6.26';

                const style=document.createElement('style');
                style.id='stability72Style';
                style.textContent=`
                  /* 6.12: the week selector uses two full-width rows.
                     This prevents 3/4 full labels from ever overlapping on phone screens. */
                  html body .contextBar{
                    display:grid!important;
                    grid-template-columns:minmax(0,1fr)!important;
                    grid-template-rows:31px 31px!important;
                    align-items:center!important;
                    justify-items:stretch!important;
                    height:69px!important;
                    min-height:69px!important;
                    padding:4px 9px 5px!important;
                    row-gap:2px!important;
                    column-gap:0!important;
                    overflow:visible!important;
                    background:var(--bg,#f6f8fb)!important;
                    box-sizing:border-box!important;
                  }

                  html body .contextBar #currentWeekBtn,
                  html body .contextBar #currentWeekBtn.currentWeek{
                    grid-row:1!important;
                    grid-column:1!important;
                    width:100%!important;
                    min-width:0!important;
                    max-width:none!important;
                    flex:none!important;
                    height:31px!important;
                    min-height:31px!important;
                    max-height:31px!important;
                    margin:0!important;
                    padding:0 4px!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    background:transparent!important;
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    font-size:.82rem!important;
                    font-weight:800!important;
                    line-height:1!important;
                    white-space:nowrap!important;
                    overflow:visible!important;
                  }

                  html body .contextBar #weekTabs.weekTabs{
                    grid-row:2!important;
                    grid-column:1!important;
                    width:100%!important;
                    min-width:0!important;
                    max-width:none!important;
                    height:31px!important;
                    min-height:31px!important;
                    margin:0!important;
                    padding:0!important;
                    display:flex!important;
                    align-items:stretch!important;
                    justify-content:stretch!important;
                    gap:5px!important;
                    overflow:visible!important;
                    visibility:visible!important;
                  }

                  html body .contextBar #weekTabs .weekTab,
                  html body .contextBar #weekTabs .weekTab.active{
                    flex:1 1 0!important;
                    width:auto!important;
                    min-width:0!important;
                    max-width:none!important;
                    height:31px!important;
                    min-height:31px!important;
                    max-height:31px!important;
                    margin:0!important;
                    padding:0 2px!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    background:transparent!important;
                    border:0!important;
                    border-bottom:2px solid transparent!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    color:#536078!important;
                    font-size:.78rem!important;
                    font-weight:800!important;
                    line-height:1!important;
                    letter-spacing:-.01em!important;
                    white-space:nowrap!important;
                    overflow:visible!important;
                    text-overflow:clip!important;
                    transition:none!important;
                    animation:none!important;
                    transform:none!important;
                  }
                  html body .contextBar #weekTabs .weekTab.active{
                    color:var(--set-accent,var(--blue))!important;
                    border-bottom-color:var(--set-accent,var(--blue))!important;
                  }
                  html body .contextBar #weekTabs .weekTab[aria-hidden="true"]{
                    display:none!important;
                    visibility:hidden!important;
                    pointer-events:none!important;
                  }

                  /* One-week mode collapses the second row completely. */
                  html body.singleWeekMode .contextBar{
                    grid-template-rows:31px!important;
                    height:39px!important;
                    min-height:39px!important;
                    padding-top:4px!important;
                    padding-bottom:4px!important;
                  }
                  html body.singleWeekMode .contextBar #weekTabs.weekTabs{display:none!important}

                  @media(max-width:390px){
                    html body .contextBar{padding-left:6px!important;padding-right:6px!important}
                    html body .contextBar #currentWeekBtn,
                    html body .contextBar #currentWeekBtn.currentWeek{font-size:.80rem!important}
                    html body .contextBar #weekTabs.weekTabs{gap:2px!important}
                    html body .contextBar #weekTabs .weekTab,
                    html body .contextBar #weekTabs .weekTab.active{
                      padding-left:1px!important;padding-right:1px!important;
                      font-size:.72rem!important;letter-spacing:-.025em!important
                    }
                  }
                `;
                document.head.appendChild(style);

                function refresh(){
                  const v=document.getElementById('appVersionInfo');
                  if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION;
                  /* Remove stale fixed widths left inline by earlier layers. CSS above is authoritative. */
                  const cw=document.getElementById('currentWeekBtn');
                  if(cw){cw.style.removeProperty('width');cw.style.removeProperty('min-width');cw.style.removeProperty('max-width');}
                  const tabs=document.getElementById('weekTabs');
                  if(tabs&& !document.body.classList.contains('singleWeekMode'))tabs.style.setProperty('visibility','visible','important');
                }
                window.refreshStability72=refresh;

                function wrap(name){
                  const old=window[name];if(typeof old!=='function'||old.__stability72)return;
                  const w=function(){const r=old.apply(this,arguments);requestAnimationFrame(refresh);return r};
                  w.__stability72=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['renderContext','renderWeek','renderEdit','refreshStability70','refreshStability71'].forEach(wrap);

                refresh();
                requestAnimationFrame(refresh);
              }catch(e){console.log('Stability72Ui',e)}
            })();
            """;
    }
}
