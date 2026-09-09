package com.wokgui.schedulewidget;

/** Small, local reset actions for the main settings groups. */
final class SettingsResetUi {
    private SettingsResetUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__settingsResetV1){if(window.refreshSettingsReset)window.refreshSettingsReset();return}
                window.__settingsResetV1=true;
                const APP_VERSION='6.29';

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
                function ui(){try{return JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){return {}}}
                function adv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function refreshAll(){
                  try{if(window.refreshSettingsV3)window.refreshSettingsV3()}catch(e){}
                  try{if(window.refreshFineTuneUi)window.refreshFineTuneUi()}catch(e){}
                  try{if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures()}catch(e){}
                  try{if(window.refreshStability81)window.refreshStability81()}catch(e){}
                  try{if(window.refreshSettingsLayout)window.refreshSettingsLayout()}catch(e){}
                  try{if(typeof render==='function')render()}catch(e){}
                }

                const style=document.createElement('style');
                style.id='settingsResetStyle';
                style.textContent=`
                  .settingsSectionReset87{display:block;margin:6px auto 0;padding:6px 9px;border:1px solid #d5dee8;border-radius:8px;background:#fff;color:#59677a;font-size:.66rem;font-weight:850;touch-action:manipulation}
                  .settingsSectionReset87:active{opacity:.66}
                `;
                document.head.appendChild(style);

                function addButton(groupId,id,handler){
                  const group=document.getElementById(groupId);if(!group)return;
                  let b=document.getElementById(id);if(!b){b=document.createElement('button');b.id=id;b.type='button';b.className='settingsSectionReset87';b.onclick=handler;group.appendChild(b)}
                  b.textContent=tr('Réinitialiser cette section','Reset this section','Diesen Bereich zurücksetzen');
                }
                function resetText(){
                  const o=ui();o.appFontScale=1;o.widgetFontScale=1;AndroidSchedule.saveUiSettings(JSON.stringify(o));refreshAll()
                }
                function resetColors(){
                  const o=ui();o.theme='blue';AndroidSchedule.saveUiSettings(JSON.stringify(o));
                  try{AndroidSchedule.saveWidgetPalette('vivid')}catch(e){}
                  try{AndroidSchedule.saveSpecialColors(JSON.stringify({sync:true,appLunch:'#FFF9E8',appGap:'#FFFFFF',widgetLunch:'#FFF9E8',widgetGap:'#FFFFFF'}))}catch(e){}
                  refreshAll()
                }
                function resetWidget(){
                  const a=adv();
                  Object.assign(a,{density:'normal',upcomingCount:0,widgetFormat:'timeline',showRoom:true,showTimes:true,showRemaining:true,showPercent:true,showProgress:true,showBreaks:true,showLunch:true,showWeekInfo:true,gapWidgetLabel:'',lunchWidgetLabel:''});
                  AndroidSchedule.saveAdvancedSettings(JSON.stringify(a));refreshAll()
                }
                function ensure(){
                  addButton('textSettings86','resetText87',resetText);
                  addButton('colorSettings86','resetColors87',resetColors);
                  addButton('widgetSettings86','resetWidget87',resetWidget);
                  const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                }
                function refresh(){ensure()}
                window.refreshSettingsReset=refresh;
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(()=>requestAnimationFrame(ensure)).observe(sheet,{childList:true,subtree:true});
                ensure();
              }catch(e){console.log('SettingsResetUi',e)}
            })();
            """;
    }
}
