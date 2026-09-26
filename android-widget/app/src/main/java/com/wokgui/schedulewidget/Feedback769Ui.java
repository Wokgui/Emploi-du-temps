package com.wokgui.schedulewidget;

/** 7.71 final owner for settings typography and compact widget controls. */
final class Feedback769Ui {
    private Feedback769Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback769){window.refreshFeedback769&&window.refreshFeedback769();return}
                window.__feedback769=true;
                const VERSION='7.71';
                let scheduled=false;
                const style=document.createElement('style');style.id='feedback769Style';style.textContent=`
                  #settingsSheet{--settings-content-font:.72rem}
                  #settingsSheet .settingsSectionBody86{font-size:var(--settings-content-font)!important}
                  #settingsSheet .settingsSectionBody86 *{font-size:var(--settings-content-font)!important}
                  #widgetSettings86 .displayGrid767{grid-template-columns:minmax(92px,.9fr) repeat(2,minmax(112px,1.15fr))!important}
                  #widgetSettings86 .displayCell767{overflow-wrap:anywhere!important;line-height:1.16!important}
                  #widgetSettings86 .displayHead767{background:#fff!important;color:var(--ink,#111936)!important;font-weight:800!important}
                  #widgetSettings86 .accessCell769{padding-left:2px!important;padding-right:2px!important}
                  #widgetSettings86 .accessCell769 select{width:100%!important;max-width:none!important;min-width:0!important;padding:6px 12px 6px 2px!important;font-weight:750!important;-webkit-appearance:none!important;appearance:none!important;background-image:linear-gradient(45deg,transparent 50%,#667085 50%),linear-gradient(135deg,#667085 50%,transparent 50%)!important;background-position:calc(100% - 9px) 50%,calc(100% - 5px) 50%!important;background-size:4px 4px!important;background-repeat:no-repeat!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Grid{display:block!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar771Row{display:grid!important;grid-template-columns:minmax(0,1fr) minmax(125px,176px) auto!important;align-items:center!important;gap:8px!important;min-height:30px!important;margin:6px 0!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Label{grid-column:auto!important;grid-row:auto!important;text-align:left!important;line-height:1.25!important;font-weight:400!important;color:inherit!important}
                  #widgetSettings86 #widgetEdgeBars672 select{grid-column:auto!important;grid-row:auto!important;width:100%!important;max-width:176px!important;min-width:0!important;min-height:31px!important;justify-self:end!important;padding:5px 24px 5px 8px!important;border-radius:8px!important;text-align:center!important;text-align-last:center!important}
                  #widgetSettings86 #widgetEdgeBars672 input[type=color]{grid-column:auto!important;grid-row:auto!important;width:38px!important;height:31px!important}
                  #widgetSettings86 #widgetEdgeBars672 input[type=color]:not(.bar672Active){display:none!important}
                  #colorSettings86 .themeName,#colorSettings86 .coursePaletteName{white-space:normal!important;overflow:visible!important;text-overflow:clip!important;overflow-wrap:anywhere!important;line-height:1.12!important;text-align:center!important}
                  #colorSettings86 .themeName{display:flex!important;align-items:center!important;justify-content:center!important;min-height:2.24em!important}
                  #slotSettings{overflow:visible!important}
                  #slotSettings .slotRow{grid-template-columns:minmax(112px,.95fr) repeat(2,minmax(0,1fr))!important;overflow:visible!important}
                  #slotSettings .slotLead{display:grid!important;grid-template-columns:minmax(0,1fr) 24px!important;align-items:center!important;gap:4px!important;min-width:0!important;overflow:visible!important}
                  #slotSettings .slotNum{min-width:0!important;overflow:hidden!important;text-overflow:ellipsis!important;white-space:nowrap!important}
                  #slotSettings .slotRemove{display:flex!important;visibility:visible!important;position:relative!important;z-index:2!important;flex:0 0 24px!important;width:24px!important;height:24px!important;padding:0!important}
                  #slotSettings .slotRemove[hidden]{display:none!important}
                  @media(max-width:370px){
                    #widgetSettings86 .displayGrid767{grid-template-columns:minmax(84px,.82fr) repeat(2,minmax(104px,1.1fr))!important}
                    #widgetSettings86 .accessCell769 select{padding-right:11px!important;background-position:calc(100% - 8px) 50%,calc(100% - 4px) 50%!important}
                    #widgetSettings86 #widgetEdgeBars672 .bar771Row{grid-template-columns:minmax(0,1fr) minmax(108px,145px) auto!important;gap:5px!important}
                    #slotSettings .slotRow{grid-template-columns:minmax(104px,.95fr) repeat(2,minmax(0,1fr))!important}
                  }
                `;document.head.appendChild(style);
                function arrangeBars(){
                  const grid=document.querySelector('#widgetEdgeBars672 .bar672Grid');if(!grid)return;
                  const divider=grid.querySelector('.bar672Divider');if(divider)divider.remove();
                  const labels=grid.querySelectorAll('.bar672Label'),topMode=document.getElementById('widgetTopBarMode672'),bottomMode=document.getElementById('widgetBottomBarMode672'),topColor=document.getElementById('widgetTopBarColor672'),bottomColor=document.getElementById('widgetBottomBarColor672');
                  if(labels.length<2||!topMode||!bottomMode||!topColor||!bottomColor)return;
                  let top=document.getElementById('widgetTopBarRow771'),bottom=document.getElementById('widgetBottomBarRow771');
                  if(!top){top=document.createElement('div');top.id='widgetTopBarRow771';top.className='advRow bar771Row'}
                  if(!bottom){bottom=document.createElement('div');bottom.id='widgetBottomBarRow771';bottom.className='advRow bar771Row'}
                  top.append(labels[0],topMode,topColor);bottom.append(labels[1],bottomMode,bottomColor);grid.append(top,bottom);
                }
                function matchContentFont(){
                  const sheet=document.getElementById('settingsSheet'),profile=document.getElementById('advProfilesTitle')||document.querySelector('#advancedSettings85 .advSectionTitle');if(!sheet)return;
                  const size=profile?getComputedStyle(profile).fontSize:'11.52px';sheet.style.setProperty('--settings-content-font',size);
                  sheet.querySelectorAll('.settingsSectionBody86 *').forEach(node=>node.style.setProperty('font-size',size,'important'));
                }
                function fitAccessLabels(){
                  const raw=document.getElementById('languageSelect')?.value||document.documentElement.lang||'fr',lang=String(raw).toLowerCase().startsWith('de')?'de':(String(raw).toLowerCase().startsWith('en')?'en':'fr');
                  for(const id of ['advAppAccess767','advAccess']){const select=document.getElementById(id);if(!select||select.options.length<3)continue;select.options[2].textContent=lang==='de'?'Daltonismus':(lang==='en'?'Colour-blind':'Palette daltonisme')}
                }
                function refresh(){
                  scheduled=false;
                  const format=document.getElementById('advFormat');const formatRow=format&&format.closest('.advRow');if(formatRow)formatRow.remove();
                  const following=document.getElementById('advFollowing');const followingRow=following&&following.closest('.advRow');if(followingRow)followingRow.remove();
                  for(const id of ['advAppAccess767','advAccess']){const select=document.getElementById(id);const cell=select&&select.closest('.displayCell767');if(cell)cell.classList.add('accessCell769')}
                  arrangeBars();matchContentFont();fitAccessLabels();
                  const version=document.getElementById('appVersionInfo');if(version)version.textContent='Version '+VERSION;
                }
                function schedule(){if(scheduled)return;scheduled=true;requestAnimationFrame(refresh)}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback769)return;const next=function(){const result=old.apply(this,arguments);schedule();return result};next.__feedback769=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                window.refreshFeedback769=refresh;
                ['render','refreshSettingsLayout','refreshAdvancedFeatures','prepareSettingsOpen665'].forEach(wrap);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback769Ui',e)}
            })();
            """;
    }
}
