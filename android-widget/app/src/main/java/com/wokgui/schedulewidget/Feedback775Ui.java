package com.wokgui.schedulewidget;

/** 7.77 final owner for the settings heading and widget bar labels. */
final class Feedback775Ui {
    private Feedback775Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback775){window.refreshFeedback775&&window.refreshFeedback775();return}
                window.__feedback775=true;
                const VERSION='7.77';
                let scheduled=false;
                const style=document.createElement('style');style.id='feedback775Style';style.textContent=`
                  #settingsSheet #settingsGear774{left:0!important;width:25px!important;height:25px!important}
                  #settingsSheet #settingsGear774 svg{width:23px!important;height:23px!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Label{white-space:normal!important;overflow-wrap:normal!important;word-break:normal!important;line-height:1.18!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar775Line{display:block!important;white-space:nowrap!important}
                `;document.head.appendChild(style);

                function placeGear(){
                  const head=document.querySelector('#settingsSheet .settingsHead'),title=document.getElementById('settingsTitle'),gear=document.getElementById('settingsGear774');
                  if(!head||!title||!gear)return;
                  const h=head.getBoundingClientRect(),t=title.getBoundingClientRect(),g=gear.getBoundingClientRect();
                  gear.style.setProperty('left',Math.max(4,t.left-h.left-g.width-5)+'px','important');
                }
                function splitBarLabels(){
                  const raw=document.getElementById('languageSelect')?.value||document.documentElement.lang||'fr',lang=String(raw).toLowerCase();
                  const labels=document.querySelectorAll('#widgetEdgeBars672 .bar672Label');if(labels.length<2)return;
                  if(lang.startsWith('fr')){
                    labels[0].innerHTML='<span class="bar775Line">Affichage de la barre</span> <span class="bar775Line">du haut</span>';
                    labels[1].innerHTML='<span class="bar775Line">Affichage de la barre</span> <span class="bar775Line">du bas</span>';
                  }
                }
                function refresh(){
                  scheduled=false;splitBarLabels();requestAnimationFrame(placeGear);
                  const version=document.getElementById('appVersionInfo');if(version)version.textContent='Version '+VERSION;
                }
                function schedule(){if(scheduled)return;scheduled=true;requestAnimationFrame(refresh)}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback775)return;const next=function(){const result=old.apply(this,arguments);schedule();return result};next.__feedback775=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                window.refreshFeedback775=refresh;
                ['render','renderToday','renderWeek','renderEdit','refreshSettingsLayout','refreshAdvancedFeatures','prepareSettingsOpen665'].forEach(wrap);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true,characterData:true});
                window.addEventListener('resize',schedule,{passive:true});
                refresh();
              }catch(e){console.error('Feedback775Ui',e)}
            })();
            """;
    }
}
