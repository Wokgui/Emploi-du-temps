package com.wokgui.schedulewidget;

/** Final 6.60 interaction pass for navigation recovery, reminder alignment, and export UI. */
final class Feedback660Ui {
    private Feedback660Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback660){window.refreshFeedback660&&window.refreshFeedback660();return}
                window.__feedback660=true;
                const style=document.createElement('style');style.id='feedback660Style';style.textContent=`
                  label.advCheck:has(#advReminders){width:100%!important;box-sizing:border-box!important;justify-content:center!important;text-align:center!important;margin:8px auto 4px!important}
                  label.advCheck:has(#advReminders) #advReminderLabel{text-align:center!important}
                  .advRow:has(>#advReminderMinutes){justify-content:center!important;width:100%!important}
                  .advRow:has(>#advReminderMinutes)>span:empty{display:none!important}
                  #advReminderMinutes{margin-inline:auto!important;text-align:center!important}
                  #advancedSettingsRoot .advButtons.backup660{display:grid!important;grid-template-columns:minmax(0,1fr) minmax(0,1fr)!important;gap:6px!important;width:100%!important}
                  #advancedSettingsRoot .advButtons.backup660 #advExportAllSettings{grid-column:1 / -1!important;width:100%!important}
                  #advancedSettingsRoot .advButtons.backup660 .advButton{width:100%!important;min-width:0!important}
                `;document.head.appendChild(style);

                function language(){try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr'))}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}
                function valid(value){return value==='today'||value==='week'||value==='edit'}
                function current(preferred){
                  if(valid(preferred))return preferred;
                  try{if(typeof mode!=='undefined'&&valid(mode))return mode}catch(e){}
                  const nav=document.querySelector('.nav.active[data-mode]');if(nav&&valid(nav.dataset.mode))return nav.dataset.mode;
                  const view=document.querySelector('.view.active');if(view){const name=String(view.id||'').replace(/^view/,'').toLowerCase();if(valid(name))return name}
                  return 'today';
                }

                function installExport(){
                  const title=document.getElementById('advBackupTitle'),box=title&&title.closest('.settingBox'),buttons=box&&box.querySelector('.advButtons');if(!buttons)return;
                  buttons.classList.add('backup660');let button=document.getElementById('advExportAllSettings');
                  if(!button){button=document.createElement('button');button.id='advExportAllSettings';button.type='button';button.className='advButton primary';buttons.insertBefore(button,buttons.firstChild)}
                  button.textContent=tr('Exporter tous les réglages','Export all settings','Alle Einstellungen exportieren');
                  button.onclick=()=>{if(window.AndroidSchedule&&AndroidSchedule.exportAllSettings)AndroidSchedule.exportAllSettings()};
                }
                window.applyAllSettingsExported=ok=>alert(ok?tr('Tous les réglages ont été exportés.','All settings were exported.','Alle Einstellungen wurden exportiert.'):tr('Export impossible.','Export failed.','Export nicht möglich.'));
                function refresh(){installExport()}
                window.refreshFeedback660=refresh;
                try{if(typeof todayKey==='function'&&typeof selected!=='undefined'&&current()==='today')selected=todayKey()}catch(e){}
                try{if(current()==='today'&&typeof renderToday==='function')renderToday()}catch(e){}
                installExport();
              }catch(e){console.error('Feedback660Ui',e)}
            })();
            """;
    }
}
