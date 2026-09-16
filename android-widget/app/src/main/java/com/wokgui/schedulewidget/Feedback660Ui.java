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
                  main.wrap.edtInstantViews647{min-height:calc(100vh - 160px)!important}
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
                let requested='',raf=0;
                function repair(preferred){
                  const target=current(preferred||requested);requested='';raf=0;
                  const id='view'+target.charAt(0).toUpperCase()+target.slice(1),view=document.getElementById(id);if(!view)return false;
                  const views=[...document.querySelectorAll('main.wrap>.view')];
                  views.forEach(node=>{const should=node===view;if(node.classList.contains('active')!==should)node.classList.toggle('active',should)});
                  document.querySelectorAll('.nav[data-mode]').forEach(nav=>nav.classList.toggle('active',nav.dataset.mode===target));
                  try{if(typeof mode!=='undefined')mode=target}catch(e){}
                  document.documentElement.style.visibility='visible';if(document.body){document.body.style.visibility='visible';document.body.style.opacity='1'}
                  const instant=window.__edtInstantViews647,stage=document.querySelector('main.wrap');
                  if(instant&&typeof instant.fit==='function')instant.fit(target);
                  if(stage){const height=Math.max(view.offsetHeight||0,view.scrollHeight||0);if(height>0){const css=getComputedStyle(stage),top=parseFloat(css.paddingTop)||0,bottom=parseFloat(css.paddingBottom)||0;stage.style.height=Math.ceil(height+top+bottom)+'px'}}
                  return view.classList.contains('active')&&getComputedStyle(view).visibility!=='hidden';
                }
                function schedule(target){if(valid(target))requested=target;if(raf)return;raf=requestAnimationFrame(()=>repair(requested))}
                document.addEventListener('pointerdown',event=>{const nav=event.target&&event.target.closest?event.target.closest('.nav[data-mode]'):null;if(nav)schedule(nav.dataset.mode)},true);
                document.addEventListener('click',event=>{const nav=event.target&&event.target.closest?event.target.closest('.nav[data-mode]'):null;if(nav)schedule(nav.dataset.mode)},true);
                document.querySelectorAll('main.wrap>.view').forEach(view=>new MutationObserver(()=>schedule()).observe(view,{attributes:true,attributeFilter:['class']}));
                const settings=document.getElementById('settingsModal');
                if(settings){
                  try{window.__edtAllowPanelObserver648=true;new MutationObserver(()=>{if(settings.getAttribute('data-edt-open')!=='true')schedule()}).observe(settings,{attributes:true,attributeFilter:['data-edt-open']})}finally{window.__edtAllowPanelObserver648=false}
                }

                function installExport(){
                  const title=document.getElementById('advBackupTitle'),box=title&&title.closest('.settingBox'),buttons=box&&box.querySelector('.advButtons');if(!buttons)return;
                  buttons.classList.add('backup660');let button=document.getElementById('advExportAllSettings');
                  if(!button){button=document.createElement('button');button.id='advExportAllSettings';button.type='button';button.className='advButton primary';buttons.insertBefore(button,buttons.firstChild)}
                  button.textContent=tr('Exporter tous les réglages','Export all settings','Alle Einstellungen exportieren');
                  button.onclick=()=>{if(window.AndroidSchedule&&AndroidSchedule.exportAllSettings)AndroidSchedule.exportAllSettings()};
                }
                window.applyAllSettingsExported=ok=>alert(ok?tr('Tous les réglages ont été exportés.','All settings were exported.','Alle Einstellungen wurden exportiert.'):tr('Export impossible.','Export failed.','Export nicht möglich.'));
                function refresh(){installExport();schedule()}
                window.refreshFeedback660=refresh;
                try{if(typeof todayKey==='function'&&typeof selected!=='undefined'&&current()==='today')selected=todayKey()}catch(e){}
                try{if(current()==='today'&&typeof renderToday==='function')renderToday()}catch(e){}
                repair(current());installExport();
              }catch(e){console.error('Feedback660Ui',e)}
            })();
            """;
    }
}
