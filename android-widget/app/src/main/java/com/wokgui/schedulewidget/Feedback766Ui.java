package com.wokgui.schedulewidget;

/** 7.66 owner for density wording, settings-page presentation and final settings alignment. */
final class Feedback766Ui {
    private Feedback766Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback766){window.refreshFeedback766&&window.refreshFeedback766();return}
                window.__feedback766=true;
                function language(){try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr')).toLowerCase()}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=language();return l.startsWith('de')?de:(l.startsWith('en')?en:fr)}

                const style=document.createElement('style');style.id='feedback766Style';style.textContent=`
                  #settingsModal.settingsPage766{background:#fff!important;align-items:stretch!important;padding:0!important;overflow:hidden!important}
                  #settingsModal.settingsPage766 #settingsSheet{
                    width:100%!important;max-width:none!important;height:100vh!important;height:100dvh!important;
                    min-height:100vh!important;min-height:100dvh!important;max-height:100vh!important;max-height:100dvh!important;
                    margin:0!important;padding:calc(12px + env(safe-area-inset-top)) 14px calc(16px + env(safe-area-inset-bottom))!important;
                    border:0!important;border-radius:0!important;background:#fff!important;box-shadow:none!important;box-sizing:border-box!important;overflow-y:auto!important
                  }
                  #settingsModal.settingsPage766 #settingsSheet>.settingsHead{position:sticky!important;top:calc(-12px - env(safe-area-inset-top))!important;z-index:8!important;margin:0 -14px 8px!important;padding:12px 14px 8px!important;background:#fff!important;border-bottom:1px solid #edf0f4!important}
                  #settingsSheet #widgetSettings86 #widgetEdgeBars672 .bar672Title{
                    margin:0 0 8px!important;color:var(--set-dark,var(--ink,#111936))!important;font-size:.72rem!important;
                    font-weight:850!important;line-height:1.2!important;text-align:center!important
                  }
                  #settingsSheet #textSettings86 #advDensityAutoRow665,
                  #settingsSheet #colorSettings86 #paletteSettingRoot .paletteSyncRow{
                    display:flex!important;align-items:center!important;justify-content:center!important;gap:8px!important;
                    width:min(238px,calc(100% - 24px))!important;max-width:238px!important;box-sizing:border-box!important;
                    margin:8px auto 10px!important;padding:8px 10px!important;border:1px solid #cbd8e7!important;
                    border-radius:9px!important;background:#f8fbff!important;color:#40516a!important;
                    font-size:.72rem!important;font-weight:800!important;line-height:1.25!important;text-align:center!important;white-space:normal!important
                  }
                  #settingsSheet #colorSettings86 #paletteSettingRoot .paletteSyncRow input{
                    order:-1!important;width:18px!important;height:18px!important;flex:0 0 18px!important;margin:0!important;accent-color:#1689e8!important
                  }
                  #settingsSheet #advancedSettings85 .feedback664Profiles #advProfileSelect{
                    display:block!important;width:auto!important;min-width:0!important;max-width:calc(100% - 24px)!important;
                    margin:0 auto!important;padding:7px 32px 7px 12px!important;font-size:.72rem!important;font-weight:850!important;
                    line-height:1.2!important;text-align:center!important;text-align-last:center!important;white-space:nowrap!important
                  }
                  #settingsSheet #advancedSettings85 .feedback664Profiles #advProfileSelect option{font-size:.72rem!important;font-weight:850!important}
                  #viewEdit .feedback766ObsoleteSlotsHead{display:none!important}
                `;document.head.appendChild(style);

                function prepareSettingsPage(){
                  const modal=document.getElementById('settingsModal');if(!modal)return;
                  modal.classList.add('settingsPage766');modal.setAttribute('role','region');modal.removeAttribute('aria-modal');
                  modal.setAttribute('aria-label',tr('Page Réglages','Settings page','Einstellungsseite'));
                }
                function densityLabels(){
                  const widget=document.querySelector('#widgetDensity664 .feedback664DensityTitle');if(widget)widget.textContent=tr('Densité du widget','Widget density','Widget-Dichte');
                  const day=document.getElementById('dayDensityTitle765');if(day)day.textContent=tr('Densité de la vue jour','Day view density','Dichte der Tagesansicht');
                  const slider=document.getElementById('advDensitySlider665');if(slider)slider.setAttribute('aria-label',widget?widget.textContent:tr('Densité du widget','Widget density','Widget-Dichte'));
                }
                function polishPaletteTile(){
                  const row=document.querySelector('#paletteSettingRoot .paletteSyncRow'),input=document.getElementById('paletteSyncToggle'),label=document.getElementById('paletteSyncLabel');if(!row||!input||!label)return;
                  if(row.firstElementChild!==input)row.insertBefore(input,label);
                }
                function fitProfile(){
                  const select=document.getElementById('advProfileSelect'),title=document.getElementById('advProfilesTitle');if(!select||!select.options.length)return;
                  const option=select.options[select.selectedIndex]||select.options[0],computed=getComputedStyle(select),canvas=document.createElement('canvas'),context=canvas.getContext('2d');
                  context.font=computed.font;const width=Math.min(Math.max(88,Math.ceil(context.measureText(option.text||'').width+52)),Math.max(88,innerWidth-72));
                  select.style.setProperty('width',width+'px','important');
                  if(title)select.style.setProperty('font-size',getComputedStyle(title).fontSize,'important');
                }
                function cleanEditHeading(){
                  document.querySelectorAll('#viewEdit .sectionHead').forEach(head=>{
                    const text=(head.textContent||'').trim();if(/horaires des cours|class times|period times|unterrichtszeiten/i.test(text)){head.hidden=true;head.classList.add('feedback766ObsoleteSlotsHead')}
                  });
                }
                function polish(){prepareSettingsPage();densityLabels();polishPaletteTile();fitProfile();cleanEditHeading()}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback766)return;const next=function(){const result=old.apply(this,arguments);polish();return result};next.__feedback766=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                function refresh(){['renderEdit','render','refreshSettingsLayout','refreshAdvancedFeatures'].forEach(wrap);polish();requestAnimationFrame(polish)}
                window.refreshFeedback766=refresh;
                document.addEventListener('change',event=>{if(event.target&&['languageSelect','advProfileSelect'].includes(event.target.id))requestAnimationFrame(polish)},true);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(()=>requestAnimationFrame(polish)).observe(sheet,{childList:true,subtree:true});
                const edit=document.getElementById('viewEdit');if(edit)new MutationObserver(()=>requestAnimationFrame(cleanEditHeading)).observe(edit,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback766Ui',e)}
            })();
            """;
    }
}
