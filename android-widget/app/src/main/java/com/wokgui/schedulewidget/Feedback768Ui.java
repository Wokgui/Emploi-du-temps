package com.wokgui.schedulewidget;

/** 7.68 final owner for widget bar labels and compact edit actions. */
final class Feedback768Ui {
    private Feedback768Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback768){window.refreshFeedback768&&window.refreshFeedback768();return}
                window.__feedback768=true;
                const VERSION='7.68';
                let scheduled=false;
                function language(){
                  const selected=document.getElementById('languageSelect')?.value;
                  if(selected)return String(selected).toLowerCase().startsWith('de')?'de':(String(selected).toLowerCase().startsWith('en')?'en':'fr');
                  try{const l=String(JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr').toLowerCase();return l.startsWith('de')?'de':(l.startsWith('en')?'en':'fr')}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=language();return l==='de'?de:(l==='en'?en:fr)}
                const style=document.createElement('style');style.id='feedback768Style';style.textContent=`
                  #widgetSettings86 #widgetEdgeBars672 .bar672Title{display:none!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Grid{grid-template-columns:minmax(126px,1.25fr) minmax(96px,1fr) 46px!important}
                  #viewEdit #importPhoto,#viewEdit #addCourse,#viewEdit #addBulkCourses{height:auto!important;min-height:38px!important;padding:7px 11px!important;font-size:.78rem!important;line-height:1.2!important}
                  @media(max-width:370px){#widgetSettings86 #widgetEdgeBars672 .bar672Grid{grid-template-columns:minmax(116px,1.15fr) minmax(90px,1fr) 40px!important;gap:5px!important}}
                `;document.head.appendChild(style);
                function refresh(){
                  scheduled=false;
                  const title=document.querySelector('#widgetEdgeBars672 .bar672Title');if(title){title.textContent='';title.hidden=true}
                  const labels=document.querySelectorAll('#widgetEdgeBars672 .bar672Label');
                  if(labels[0])labels[0].textContent=tr('Affichage de la barre du haut','Top bar display','Anzeige der oberen Leiste');
                  if(labels[1])labels[1].textContent=tr('Affichage de la barre du bas','Bottom bar display','Anzeige der unteren Leiste');
                  const version=document.getElementById('appVersionInfo');if(version)version.textContent='Version '+VERSION;
                }
                function schedule(){if(scheduled)return;scheduled=true;requestAnimationFrame(refresh)}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback768)return;const next=function(){const result=old.apply(this,arguments);schedule();return result};next.__feedback768=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                window.refreshFeedback768=refresh;
                ['render','renderEdit','refreshSettingsLayout','refreshAdvancedFeatures','prepareSettingsOpen665'].forEach(wrap);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback768Ui',e)}
            })();
            """;
    }
}
