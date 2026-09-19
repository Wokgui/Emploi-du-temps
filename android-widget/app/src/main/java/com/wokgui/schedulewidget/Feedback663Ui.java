package com.wokgui.schedulewidget;

/** Final 6.63 settings matrix for independent application/widget break visibility. */
final class Feedback663Ui {
    private Feedback663Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback663){window.refreshFeedback663&&window.refreshFeedback663();return}
                window.__feedback663=true;
                const style=document.createElement('style');style.id='feedback663Style';style.textContent=`
                  html body #settingsSheet #breakDisplaySetting{display:block!important;visibility:visible!important;text-align:center!important;padding:14px 12px!important}
                  #breakDisplaySetting>.settingTitle,#breakDisplaySetting>.coursePaletteHint,#breakDisplaySetting>#breakVisibility70,#breakDisplaySetting>#breakDisplayHint,#breakDisplaySetting>#breakWidget70{display:none!important}
                  #breakDisplaySetting .feedback663Matrix{display:grid!important;grid-template-columns:minmax(92px,1.2fr) repeat(2,minmax(72px,1fr));align-items:center;gap:0;width:100%;max-width:430px;margin:0 auto;border:1px solid #d9e0ea;border-radius:14px;overflow:hidden;background:#fff}
                  #breakDisplaySetting .feedback663Title{font-size:17px;font-weight:800;text-align:center;margin:0 0 11px;color:#17213a}
                  #breakDisplaySetting .feedback663Cell{min-height:48px;display:flex;align-items:center;justify-content:center;text-align:center;padding:8px 5px;border-right:1px solid #e3e8ef;border-bottom:1px solid #e3e8ef;font-weight:700;color:#17213a}
                  #breakDisplaySetting .feedback663Cell:nth-child(3n){border-right:0}
                  #breakDisplaySetting .feedback663Cell:nth-last-child(-n+3){border-bottom:0}
                  #breakDisplaySetting .feedback663Head{min-height:40px;color:#087fca;background:#f4f9fd;font-size:14px}
                  #breakDisplaySetting .feedback663RowHead{justify-content:center;font-size:14px}
                  #breakDisplaySetting .feedback663Cell label{width:100%;height:100%;display:flex;align-items:center;justify-content:center;margin:0;cursor:pointer}
                  #breakDisplaySetting .feedback663Cell input{width:21px;height:21px;margin:0;accent-color:#1189e8}
                `;document.head.appendChild(style);

                function read(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function write(next){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(next))}catch(e){}}
                function label(fr,en,de){const l=(document.documentElement.lang||'fr').toLowerCase();return l.startsWith('de')?de:(l.startsWith('en')?en:fr)}
                function install(){
                  const box=document.getElementById('breakDisplaySetting');if(!box)return;
                  box.setAttribute('aria-hidden','false');
                  let root=document.getElementById('feedback663Visibility');
                  if(!root){
                    root=document.createElement('div');root.id='feedback663Visibility';
                    root.innerHTML='<div class="feedback663Title">'+label('Affichage des interruptions','Break display','Pausenanzeige')+'</div><div class="feedback663Matrix"><div class="feedback663Cell feedback663Head"></div><div class="feedback663Cell feedback663Head">Midi</div><div class="feedback663Cell feedback663Head">'+label('Trous','Gaps','Freistunden')+'</div><div class="feedback663Cell feedback663RowHead">Application</div><div class="feedback663Cell"><label aria-label="Midi dans l’application"><input id="feedback663AppLunch" type="checkbox"></label></div><div class="feedback663Cell"><label aria-label="Trous dans l’application"><input id="feedback663AppGaps" type="checkbox"></label></div><div class="feedback663Cell feedback663RowHead">Widget</div><div class="feedback663Cell"><label aria-label="Midi dans le widget"><input id="feedback663WidgetLunch" type="checkbox"></label></div><div class="feedback663Cell"><label aria-label="Trous dans le widget"><input id="feedback663WidgetGaps" type="checkbox"></label></div></div>';
                    box.appendChild(root);
                  }
                  const a=read(),defs=[
                    ['feedback663AppLunch',a.showLunchToday!==false&&a.showLunchWeek!==false,['showLunchToday','showLunchWeek']],
                    ['feedback663AppGaps',a.showBreaksToday!==false&&a.showBreaksWeek!==false,['showBreaksToday','showBreaksWeek']],
                    ['feedback663WidgetLunch',a.showLunch!==false,['showLunch']],
                    ['feedback663WidgetGaps',a.showBreaks!==false,['showBreaks']]
                  ];
                  defs.forEach(([id,checked,keys])=>{const input=document.getElementById(id);if(!input)return;input.checked=checked;input.tabIndex=0;if(input.__feedback663)return;input.__feedback663=true;input.addEventListener('change',()=>{const next=read();keys.forEach(key=>next[key]=input.checked);write(next);if(window.applyBreakVisibility)window.applyBreakVisibility();if(typeof renderToday==='function')renderToday();if(typeof renderWeek==='function'&&document.getElementById('viewWeek')?.classList.contains('active'))renderWeek()})});
                }
                function refresh(){install()}
                window.refreshFeedback663=refresh;
                ['refreshAdvancedFeatures','refreshSettingsV3','renderEdit'].forEach(name=>{const old=window[name];if(typeof old!=='function'||old.__feedback663)return;const wrapped=function(){const result=old.apply(this,arguments);queueMicrotask(refresh);return result};wrapped.__feedback663=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}});
                refresh();
              }catch(e){console.error('Feedback663Ui',e)}
            })();
            """;
    }
}
