package com.wokgui.schedulewidget;

/** Final 7.65 owner for settings typography, separators and Today-view density. */
final class Feedback765Ui {
    private Feedback765Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback765){window.refreshFeedback765&&window.refreshFeedback765();return}
                window.__feedback765=true;
                function language(){try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr')).toLowerCase()}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=language();return l.startsWith('de')?de:(l.startsWith('en')?en:fr)}
                function advanced(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function dayDensity(){const value=Number(advanced().dayViewDensity765);return Number.isFinite(value)?Math.max(0,Math.min(100,Math.round(value))):100}
                function saveDayDensity(value){
                  const a=advanced();a.dayViewDensity765=Math.max(0,Math.min(100,Math.round(Number(value)||0)));
                  try{if(typeof adv!=='undefined'&&adv)adv.dayViewDensity765=a.dayViewDensity765}catch(e){}
                  try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(a))}catch(e){}
                }

                const style=document.createElement('style');style.id='feedback765Style';style.textContent=`
                  #settingsSheet #textSettings86 #appFontTitle,#settingsSheet #textSettings86 #widgetFontTitle,
                  #settingsSheet #textSettings86 #dayDensityTitle765,#settingsSheet #textSettings86 #widgetDensity664 .feedback664DensityTitle,
                  #settingsSheet #weekTypeSettings86 #settingsWeekCycle678>.settingTitle,#settingsSheet #weekTypeSettings86 .weekCurrentLabel678,
                  #settingsSheet #weekTypeSettings86 #slotSettingsGroup759>.settingTitle,#settingsSheet #colorSettings86 #themeTitle,
                  #settingsSheet #colorSettings86 #paletteSettingRoot>.settingTitle,#settingsSheet #colorSettings86 #week658Settings .w658Title,
                  #settingsSheet #breakSettings86 #breakNamesSettings763>#breakNamesTitle763,#settingsSheet #breakSettings86 #week658LunchSettings .w658Title{
                    color:var(--set-dark,var(--ink,#111936))!important;font-size:.72rem!important;font-weight:850!important;
                    line-height:1.2!important;text-align:center!important
                  }
                  #settingsSheet #textSettings86>.settingsSectionBody86 *:not(input[type=range]):not(input[type=color]):not(input[type=checkbox]),
                  #settingsSheet #weekTypeSettings86>.settingsSectionBody86 *:not(input[type=range]):not(input[type=color]):not(input[type=checkbox]),
                  #settingsSheet #colorSettings86>.settingsSectionBody86 *:not(input[type=range]):not(input[type=color]):not(input[type=checkbox]),
                  #settingsSheet #breakSettings86>.settingsSectionBody86 *:not(input[type=range]):not(input[type=color]):not(input[type=checkbox]){
                    font-size:.72rem!important
                  }
                  #settingsSheet #weekTypeSettings86 #slotSettingsGroup759>.settingTitle{text-align:center!important}
                  #settingsSheet #textSettings86 #fontCombined79 #widgetFontTitle{border-top:1px solid #e1e7ee!important}
                  #settingsSheet #textSettings86 #dayDensity765{margin-top:9px!important;padding-top:9px!important;border-top:1px solid #e1e7ee!important}
                  #settingsSheet #textSettings86 #widgetDensity664{border-top:1px solid #e1e7ee!important}
                  #settingsSheet #weekTypeSettings86 .weekCurrentSettings678{border-top:1px solid #e1e7ee!important}
                  #settingsSheet>.settingsSection86>.settingsSectionBody86>.settingBox+.settingBox{border-top:1px solid #e1e7ee!important}
                  #dayDensity765 input[type=range]{display:block!important;width:100%!important;height:30px!important;margin:2px 0 0!important;accent-color:var(--set-accent,var(--blue,#0877f9))!important}
                  #dayDensityValue765{display:block!important;width:max-content!important;min-width:48px!important;margin:-2px auto 0!important;padding:3px 8px!important;border-radius:999px!important;background:#edf6ff!important;color:var(--set-dark,#0877c9)!important;font-size:.72rem!important;font-weight:850!important;text-align:center!important}
                  #settingsSheet #widgetSettings86 .formatRow765{display:grid!important;grid-template-columns:minmax(54px,.55fr) minmax(190px,1.45fr)!important;gap:8px!important;align-items:center!important;width:100%!important}
                  #settingsSheet #widgetSettings86 .formatRow765>span{min-width:0!important;font-size:.72rem!important}
                  #settingsSheet #widgetSettings86 #advFormat{display:block!important;width:100%!important;min-width:0!important;max-width:none!important;box-sizing:border-box!important;padding-left:8px!important;padding-right:24px!important;font-size:.72rem!important;text-overflow:clip!important}
                  #settingsSheet #advancedSettings85 #advExceptionList{margin-top:0!important;border-top:0!important}
                  #settingsSheet #advancedSettings85 #schoolCalendarSetting{
                    margin:0!important;padding:0 0 9px!important;border:0!important;border-bottom:1px solid #e1e7ee!important;
                    border-radius:0!important;background:transparent!important;box-shadow:none!important
                  }
                  #settingsSheet #advancedSettings85 #advRangeTitle{margin-top:9px!important;padding-top:9px!important;border-top:1px solid #e1e7ee!important}
                `;document.head.appendChild(style);

                function ensureDayDensity(){
                  const widgetTitle=document.getElementById('widgetFontTitle'),fontBox=widgetTitle&&widgetTitle.closest('.settingBox');if(!widgetTitle||!fontBox)return;
                  let root=document.getElementById('dayDensity765');
                  if(!root){
                    root=document.createElement('div');root.id='dayDensity765';
                    root.innerHTML='<div id="dayDensityTitle765" class="settingTitle"></div><input id="dayDensitySlider765" type="range" min="0" max="100" step="1"><output id="dayDensityValue765"></output>';
                    fontBox.appendChild(root);
                    const slider=root.querySelector('#dayDensitySlider765');
                    slider.addEventListener('input',()=>{saveDayDensity(slider.value);syncDayDensity();adjustToday()});
                    slider.addEventListener('change',()=>{saveDayDensity(slider.value);syncDayDensity();adjustToday()});
                  }
                  const title=document.getElementById('dayDensityTitle765');if(title)title.textContent=tr('Condensation de la vue jour','Day view compactness','Kompaktheit der Tagesansicht');
                  syncDayDensity();
                }
                function syncDayDensity(){const value=dayDensity(),slider=document.getElementById('dayDensitySlider765'),output=document.getElementById('dayDensityValue765');if(slider&&document.activeElement!==slider)slider.value=String(value);if(output)output.textContent=value+' %'}
                function prepareFormat(){const select=document.getElementById('advFormat'),row=select&&select.closest('.advRow');if(row)row.classList.add('formatRow765')}
                function adjustToday(){
                  const view=document.getElementById('viewToday'),list=document.getElementById('todayList'),bottom=document.querySelector('.bottom');if(!view||!list||!bottom||!view.classList.contains('active'))return;
                  const full=Math.max(120,Math.floor(bottom.getBoundingClientRect().top-list.getBoundingClientRect().top-12));
                  const ratio=.64+.36*(dayDensity()/100);list.style.minHeight=Math.max(120,Math.floor(full*ratio))+'px';
                }
                function polish(){ensureDayDensity();prepareFormat();requestAnimationFrame(()=>{ensureDayDensity();prepareFormat();adjustToday()})}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback765)return;const next=function(){const result=old.apply(this,arguments);polish();return result};next.__feedback765=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                function refresh(){wrap('renderToday');wrap('render');polish();adjustToday()}
                window.refreshFeedback765=refresh;
                addEventListener('resize',()=>requestAnimationFrame(adjustToday),{passive:true});
                document.addEventListener('change',event=>{if(event.target&&event.target.id==='languageSelect')requestAnimationFrame(polish)},true);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(()=>requestAnimationFrame(()=>{ensureDayDensity();prepareFormat()})).observe(sheet,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback765Ui',e)}
            })();
            """;
    }
}
