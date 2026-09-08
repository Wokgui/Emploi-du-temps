package com.wokgui.schedulewidget;

final class Stability78Ui {
    private Stability78Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability78V1){if(window.refreshStability78)window.refreshStability78();return}
                window.__stability78V1=true;
                const APP_VERSION='6.18';
                let refreshing=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{return (JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr')}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}

                const style=document.createElement('style');
                style.id='stability78Style';
                style.textContent=`
                  /* Names of interruptions: application and widget are deliberately independent. */
                  #viewEdit .breakSettings{overflow:visible!important}
                  #viewEdit .breakNamesScope78{padding:7px 9px 3px!important;text-align:center!important;font-size:.69rem!important;font-weight:900!important;color:var(--set-dark,var(--blue))!important;border-top:1px solid #edf0f4!important}
                  #viewEdit .breakNamesScope78:first-child{border-top:0!important}
                  #viewEdit .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr) auto!important}
                  #viewEdit .breakSettings .breakWidgetRow78{grid-template-columns:64px minmax(0,1fr)!important;padding-right:8px!important}
                  #viewEdit .breakWidgetRow78 .breakName{text-align:center!important}
                  #viewEdit .breakWidgetRow78 input{width:100%!important;min-width:0!important;box-sizing:border-box!important}

                  /* Nine periods must all remain reachable above the fixed bottom navigation. */
                  #viewEdit #slotSettings{overflow:visible!important;margin-bottom:76px!important}
                  #viewEdit #slotSettings .slotRow{display:grid!important}

                  /* Midi / trous: no explanatory subtext or extra separator before the colour controls. */
                  #settingsSheet #breakDisplayHint,
                  #settingsSheet #breakDisplaySetting>.coursePaletteHint,
                  #settingsSheet #fineSpecialColors>.settingTitle,
                  #settingsSheet #fineSpecialColors>.coursePaletteHint{display:none!important}
                  #settingsSheet #breakVisibility70{border-top:0!important;padding-top:0!important;margin-top:5px!important}
                  #settingsSheet #fineSpecialColors.embeddedFullColors74,
                  #settingsSheet #breakDisplaySetting #fineSpecialColors{border-top:0!important!important;padding-top:4px!important;margin-top:5px!important}
                  #settingsSheet #fineSpecialColors .specialWidgetTitle{border-top:0!important;padding-top:4px!important}

                  /* Explicit Today / Week / Widget visibility rows. */
                  #settingsSheet #breakVisibility70{display:grid!important;grid-template-columns:92px minmax(0,1fr) minmax(0,1fr)!important;column-gap:7px!important;row-gap:9px!important;align-items:center!important}
                  #settingsSheet #breakVisibility70>.breakVisTitle70:first-child{display:none!important}
                  #settingsSheet #breakVisibility70>.breakVisRow70{display:contents!important}
                  #settingsSheet #breakVisibility70>.breakVisRow70>span,
                  #settingsSheet #breakWidgetTitle74{display:flex!important;align-items:center!important;justify-content:center!important;text-align:center!important;font-size:.72rem!important;font-weight:850!important;color:var(--ink,#111936)!important}
                  #settingsSheet #breakWidgetTitle74{grid-column:1!important;grid-row:3!important}
                  #settingsSheet #breakWidget70{grid-column:2 / 4!important;grid-row:3!important;display:grid!important;grid-template-columns:1fr 1fr!important;gap:7px!important;margin:0!important}
                  #settingsSheet #breakVisibility70 label,#settingsSheet #breakWidget70 label{display:flex!important;align-items:center!important;justify-content:center!important;gap:5px!important;margin:0!important;font-size:.71rem!important;white-space:nowrap!important}

                  /* Nothing animates while the final startup state is being settled. */
                  body.startup78 *{transition:none!important;animation:none!important}
                `;
                document.head.appendChild(style);

                function ensureBreakNames(){
                  const card=document.querySelector('#viewEdit .breakSettings');
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  if(!card||!gap||!lunch)return;

                  let appTitle=document.getElementById('breakNamesApp78');
                  if(!appTitle){appTitle=document.createElement('div');appTitle.id='breakNamesApp78';appTitle.className='breakNamesScope78';card.insertBefore(appTitle,card.firstChild)}
                  appTitle.textContent=tr('Application','Application','App');

                  let widgetTitle=document.getElementById('breakNamesWidget78');
                  if(!widgetTitle){widgetTitle=document.createElement('div');widgetTitle.id='breakNamesWidget78';widgetTitle.className='breakNamesScope78';card.appendChild(widgetTitle)}
                  widgetTitle.textContent='Widget';

                  function ensureRow(id,labelText,inputId){
                    let row=document.getElementById(id),input=document.getElementById(inputId);
                    if(!row){row=document.createElement('div');row.id=id;row.className='breakRow breakWidgetRow78';const name=document.createElement('div');name.className='breakName';row.appendChild(name);input=document.createElement('input');input.id=inputId;input.type='text';input.maxLength=35;row.appendChild(input);card.appendChild(row)}
                    const name=row.querySelector('.breakName');if(name)name.textContent=labelText;
                    return input;
                  }
                  const wg=ensureRow('widgetGapRow78',tr('Trou','Free period','Freistunde'),'widgetGapLabel78');
                  const wl=ensureRow('widgetLunchRow78','Midi','widgetLunchLabel78');
                  const a=loadAdv();
                  if(document.activeElement!==wg)wg.value=(a.gapWidgetLabel||'').trim()||gap.value||tr('Trou','Free period','Freistunde');
                  if(document.activeElement!==wl)wl.value=(a.lunchWidgetLabel||'').trim()||lunch.value||'Midi';
                  if(!wg.__bound78){wg.__bound78=true;wg.addEventListener('change',()=>{const n=loadAdv();n.gapWidgetLabel=wg.value.trim();saveAdv(n)})}
                  if(!wl.__bound78){wl.__bound78=true;wl.addEventListener('change',()=>{const n=loadAdv();n.lunchWidgetLabel=wl.value.trim();saveAdv(n)})}

                  const appGapName=gap.closest('.breakRow')?.querySelector('.breakName');if(appGapName)appGapName.textContent=tr('Trou','Free period','Freistunde');
                  const appLunchName=lunch.closest('.breakRow')?.querySelector('.breakName');if(appLunchName)appLunchName.textContent='Midi';
                }

                function ensureNineSlots(){
                  try{
                    if(typeof slots!=='undefined'&&Array.isArray(slots)&&slots.length<9){
                      const defs=[['08:00','09:00'],['09:00','10:00'],['10:00','11:00'],['11:00','12:00'],['13:00','14:00'],['14:00','15:00'],['16:00','17:00'],['17:00','18:00'],['18:00','19:00']];
                      for(let i=slots.length;i<9;i++)slots.push({n:i+1,start:defs[i][0],end:defs[i][1]});
                    }
                    const box=document.getElementById('slotSettings');
                    if(box&&box.querySelectorAll(':scope > .slotRow').length!==9&&typeof renderSlots==='function')renderSlots();
                  }catch(e){}
                }

                function ensureWidgetVisibility(){
                  const root=document.getElementById('breakVisibility70'),widget=document.getElementById('breakWidget70');if(!root||!widget)return;
                  let title=document.getElementById('breakWidgetTitle74');
                  if(!title){
                    title=[...root.querySelectorAll(':scope > .breakVisTitle70')].find(x=>/widget/i.test(String(x.textContent||'')));
                    if(!title){title=document.createElement('div');title.className='breakVisTitle70';root.insertBefore(title,widget)}
                    title.id='breakWidgetTitle74';
                  }
                  title.textContent='Widget';
                  const lunch=document.getElementById('advShowLunch'),gap=document.getElementById('advShowBreaks');
                  const lr=lunch&&lunch.closest('label'),gr=gap&&gap.closest('label');
                  if(lr&&lr.parentElement!==widget)widget.appendChild(lr);
                  if(gr&&gr.parentElement!==widget)widget.appendChild(gr);
                  const ll=document.getElementById('advShowLunchLabel'),gl=document.getElementById('advShowBreaksLabel');
                  if(ll)ll.textContent='Midi';if(gl)gl.textContent=tr('Trous','Free periods','Freistunden');
                }

                function cleanMidiSettings(){
                  const full=document.getElementById('fineSpecialColors');if(full){full.classList.add('embeddedFullColors74');full.style.removeProperty('border-top')}
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){
                  if(refreshing)return;refreshing=true;
                  try{ensureBreakNames();ensureNineSlots();ensureWidgetVisibility();cleanMidiSettings();setVersion()}finally{refreshing=false}
                }
                window.refreshStability78=refresh;

                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__stability78)return;const w=function(){const r=old.apply(this,arguments);requestAnimationFrame(refresh);return r};w.__stability78=true;window[name]=w;try{eval(name+'=w')}catch(e){}}
                ['renderEdit','renderSlots','refreshAdvancedFeatures','refreshFineTuneUi','refreshStability70','refreshStability74','refreshLayoutLanguage77'].forEach(wrap);

                document.body.classList.add('startup78');
                refresh();requestAnimationFrame(()=>{refresh();requestAnimationFrame(()=>document.body.classList.remove('startup78'))});
              }catch(e){console.log('Stability78Ui',e)}
            })();
            """;
    }
}
