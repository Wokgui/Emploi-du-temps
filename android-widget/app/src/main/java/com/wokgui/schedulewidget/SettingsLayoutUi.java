package com.wokgui.schedulewidget;

/** Groups related settings without changing the existing controls or their event handlers. */
final class SettingsLayoutUi {
    private SettingsLayoutUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__settingsLayoutV1){if(window.refreshSettingsLayout)window.refreshSettingsLayout();return}
                window.__settingsLayoutV1=true;
                const APP_VERSION='6.27';
                let arranging=false,timer=0;

                function language(){
                  try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr'))}catch(e){return 'fr'}
                }
                function pack(){
                  const l=language();if(l==='fr'||l==='en'||l==='de')return null;
                  try{const p=JSON.parse(AndroidSchedule.loadLanguagePack(l)||'{}');return p&&p.strings?p.strings:null}catch(e){return null}
                }
                function tr(fr,en,de){
                  const l=language();if(l==='en')return en;if(l==='de')return de;if(l==='fr')return fr;
                  const p=pack();return p&&p[fr]?p[fr]:fr;
                }

                const style=document.createElement('style');
                style.id='settingsLayoutStyle';
                style.textContent=`
                  #settingsSheet>.settingsSection86{padding:9px 10px!important}
                  #settingsSheet>.settingsSection86>.settingsSectionTitle86{
                    margin:0 0 3px!important;text-align:center!important;font-size:.82rem!important;
                    font-weight:900!important;color:var(--ink,#111936)!important
                  }
                  #settingsSheet>.settingsSection86>.settingsSectionBody86{display:block!important}
                  #settingsSheet>.settingsSection86>.settingsSectionBody86>.settingBox{
                    margin:0!important;padding:8px 0!important;border:0!important;border-radius:0!important;
                    box-shadow:none!important;background:transparent!important
                  }
                  #settingsSheet>.settingsSection86>.settingsSectionBody86>.settingBox+.settingBox{
                    border-top:1px solid #e7edf3!important
                  }
                  #settingsSheet>.settingsSection86>.settingsSectionBody86>.settingBox>.settingTitle{
                    text-align:left!important;margin:0 0 6px!important;font-size:.74rem!important;color:#586579!important
                  }
                  #settingsSheet>.settingsSection86 #appFontTitle,
                  #settingsSheet>.settingsSection86 #widgetFontTitle{font-weight:800!important}
                  #settingsSheet>.settingsSection86 #themeTitle,
                  #settingsSheet>.settingsSection86 #advWidgetTitle{font-weight:800!important}
                  #settingsSheet>.settingsSection86 .settingRow{min-height:32px!important}
                  #settingsSheet>.settingsSection86 .settingValue{min-width:48px!important}
                  #settingsSheet>.settingsSection86 .coursePaletteHint{margin-top:2px!important}
                  #settingsSheet>.settingsSection86 .themeGrid{margin-top:2px!important}
                  #settingsSheet>#advancedSettings85{margin-top:9px!important}
                  #settingsSheet .settingsActions{margin-top:10px!important}
                `;
                document.head.appendChild(style);

                function boxFor(id){
                  const el=document.getElementById(id);if(!el)return null;
                  if(el.classList&&el.classList.contains('settingBox'))return el;
                  return el.closest?el.closest('.settingBox'):null;
                }
                function uniqueBoxes(ids){
                  const out=[];
                  ids.forEach(id=>{const b=boxFor(id);if(b&&!out.includes(b)&&!b.classList.contains('settingsSection86')&&b.id!=='advancedSettings85')out.push(b)});
                  return out;
                }
                function ensureGroup(id,title,ids){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return null;
                  const boxes=uniqueBoxes(ids);if(!boxes.length)return null;
                  let group=document.getElementById(id),body;
                  if(!group){
                    group=document.createElement('div');group.id=id;group.className='settingBox settingsSection86';
                    const h=document.createElement('div');h.className='settingsSectionTitle86';group.appendChild(h);
                    body=document.createElement('div');body.className='settingsSectionBody86';group.appendChild(body);
                    boxes[0].parentNode.insertBefore(group,boxes[0]);
                  }else body=group.querySelector('.settingsSectionBody86');
                  const h=group.querySelector('.settingsSectionTitle86');if(h)h.textContent=title;
                  boxes.forEach(b=>{if(b.parentNode!==body)body.appendChild(b)});
                  return group;
                }

                function arrange(){
                  timer=0;if(arranging)return;arranging=true;
                  try{
                    ensureGroup('textSettings86',tr('Taille du texte','Text size','Textgröße'),['appFont','widgetFont']);
                    ensureGroup('colorSettings86',tr('Couleurs','Colors','Farben'),['themeTitle','paletteSettingRoot','fineSpecialColors']);
                    ensureGroup('widgetSettings86',tr('Widget','Widget','Widget'),['advWidgetTitle','breakDisplaySetting']);
                    const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                  }finally{arranging=false}
                }
                function schedule(){if(timer||arranging)return;timer=setTimeout(arrange,20)}
                function refresh(){arrange()}
                window.refreshSettingsLayout=refresh;

                const sheet=document.getElementById('settingsSheet');
                if(sheet&&!sheet.__settingsLayoutObserved){
                  sheet.__settingsLayoutObserved=true;
                  new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});
                }
                const modal=document.getElementById('settingsModal');
                if(modal&&!modal.__settingsLayoutObserved){
                  modal.__settingsLayoutObserved=true;
                  new MutationObserver(()=>{if(modal.classList.contains('show'))schedule()}).observe(modal,{attributes:true,attributeFilter:['class']});
                }
                arrange();
              }catch(e){console.log('SettingsLayoutUi',e)}
            })();
            """;
    }
}
