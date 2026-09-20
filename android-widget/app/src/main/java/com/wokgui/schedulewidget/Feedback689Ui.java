package com.wokgui.schedulewidget;

/** 6.89 settings polish and compact control sizing. */
final class Feedback689Ui {
    private Feedback689Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback689){window.refreshFeedback689&&window.refreshFeedback689();return}
                window.__feedback689=true;

                const style=document.createElement('style');
                style.id='feedback689Style';
                style.textContent=`
                  /* Settings are one continuous surface: no card/tile outlines. */
                  #settingsSheet .settingBox,
                  #settingsSheet #settingsWeekCycle678,
                  #settingsSheet #widgetEdgeBars672,
                  #settingsSheet #widgetDensity664,
                  #settingsSheet #paletteSettingRoot,
                  #settingsSheet #schoolCalendarSetting{
                    border:0!important;box-shadow:none!important;background:transparent!important;
                    border-radius:0!important
                  }
                  #settingsSheet .settingBox{padding-left:2px!important;padding-right:2px!important}
                  #settingsSheet .settingsSectionBody86>.settingBox{border-top:0!important;border-bottom:0!important}
                  #settingsSheet #settingsWeekCycle678{padding-left:2px!important;padding-right:2px!important}
                  #settingsSheet #widgetEdgeBars672{padding-left:0!important;padding-right:0!important}

                  /* Center theme / palette headings and their choices. */
                  #settingsSheet #themeTitle,
                  #settingsSheet #appPaletteTitle,
                  #settingsSheet #widgetPaletteSeparateTitle,
                  #settingsSheet [id$="PaletteTitle"],
                  #settingsSheet .paletteTitle{
                    width:100%!important;text-align:center!important
                  }
                  #settingsSheet #themeGrid,
                  #settingsSheet #appPaletteGrid,
                  #settingsSheet #widgetPaletteGrid,
                  #settingsSheet #widgetPaletteSeparateGrid,
                  #settingsSheet .paletteGrid{
                    justify-content:center!important;justify-items:center!important
                  }
                  #settingsSheet .coursePaletteGrid{display:grid!important;visibility:visible!important;opacity:1!important}
                  #settingsSheet .coursePaletteBtn{display:block!important;visibility:visible!important;opacity:1!important;background:#fff!important}
                  #settingsSheet .coursePaletteSwatches{display:flex!important;visibility:visible!important;opacity:1!important;min-height:14px!important}
                  #settingsSheet .coursePaletteSwatches i{display:block!important;visibility:visible!important;opacity:1!important;min-height:14px!important}

                  /* Widget top/bottom rows: controls hug their selected text. */
                  #widgetEdgeBars672 .bar672Grid{
                    display:grid!important;
                    grid-template-columns:max-content max-content 34px!important;
                    width:max-content!important;max-width:100%!important;
                    margin:0 auto!important;column-gap:6px!important;row-gap:7px!important
                  }
                  #widgetEdgeBars672 .bar672Label{
                    text-align:right!important;font-size:.66rem!important
                  }
                  #widgetEdgeBars672 select{
                    width:auto!important;min-width:0!important;max-width:150px!important;
                    min-height:28px!important;height:28px!important;
                    padding:3px 19px 3px 7px!important;
                    font-size:.64rem!important;line-height:1!important;
                    text-align:center!important;text-align-last:center!important
                  }
                  #widgetEdgeBars672 select option{text-align:center!important}
                  #widgetEdgeBars672 input[type=color]{
                    width:30px!important;height:28px!important
                  }

                  /* Accessibility is compact and centered too. */
                  #settingsSheet .advRow:has(#advAccess){
                    display:grid!important;grid-template-columns:max-content max-content!important;
                    justify-content:center!important;align-items:center!important;gap:7px!important
                  }
                  #settingsSheet #advAccess{
                    width:auto!important;min-width:0!important;max-width:176px!important;
                    min-height:28px!important;height:28px!important;
                    padding:3px 21px 3px 7px!important;
                    text-align:center!important;text-align-last:center!important;
                    font-size:.70rem!important
                  }

                  /* Profile selector: smaller height, width tied to selected profile, more breathing room below. */
                  #settingsSheet .feedback664Profiles>#advProfilesTitle{text-align:center!important}
                  #settingsSheet .feedback664Profiles>.advRow:has(#advProfileSelect){
                    display:flex!important;justify-content:center!important;
                    margin:0 0 14px!important
                  }
                  #settingsSheet #advProfileSelect{
                    width:auto!important;min-width:0!important;max-width:228px!important;
                    min-height:31px!important;height:31px!important;
                    padding:4px 23px 4px 9px!important;
                    font-size:.71rem!important;line-height:1.15!important;
                    text-align:center!important;text-align-last:center!important
                  }
                  #settingsSheet .feedback664Profiles>.advButtons{
                    margin-top:4px!important;justify-content:center!important
                  }

                  /* Automatic widget fitting: checkbox stays visually attached to its sentence. */
                  #widgetDensity664 .feedback665DensityAuto{
                    display:flex!important;width:100%!important;max-width:100%!important;
                    gap:4px!important;margin:7px auto 0!important;
                    align-items:center!important;justify-content:center!important;text-align:center!important
                  }
                  #widgetDensity664 .feedback665DensityAuto span{
                    display:inline-block!important;text-align:center!important;line-height:1.18!important
                  }
                  #widgetDensity664 .feedback665DensityAuto input{
                    flex:0 0 auto!important;margin:0!important;width:17px!important;height:17px!important
                  }

                  /* Slightly slimmer import-photo action. */
                  html body #viewEdit>#importPhoto{
                    min-height:30px!important;height:auto!important;
                    padding:3px 12px!important;line-height:1.08!important
                  }
                  html body #viewEdit{padding-bottom:16px!important}
                  html body #slotSettings .slotAdd{margin-bottom:14px!important}
                  html body #breakDisplaySetting .feedback663Title,
                  html body #week658Settings .w658Title{
                    font-size:.86rem!important;font-weight:850!important;line-height:1.15!important;text-align:center!important
                  }
                  html body #breakDisplaySetting,
                  html body #week658Settings,
                  html body #breakDisplaySetting+#week658Settings,
                  html body #week658Settings+#breakDisplaySetting{
                    border-top:0!important;border-bottom:0!important
                  }

                  /* Week lunch has one single authoritative horizontal outline. */
                  html body #weekGrid .wc:is(.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic,.week658Lunch),
                  html body #weekGrid .dynamicLunchOverlay{
                    border-top-color:transparent!important;border-bottom-color:transparent!important;
                    box-shadow:none!important
                  }
                  html body #weekGrid .lunch653Top,
                  html body #weekGrid .lunch653Bottom,
                  html body #weekGrid .week658LunchRowTop,
                  html body #weekGrid .week658LunchRowBottom{
                    border-top-color:transparent!important;border-bottom-color:transparent!important;box-shadow:none!important
                  }
                  html body #weekGrid.hideWeekLunch70 .wc:is(.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic){
                    background:var(--week658-free,#E6F2FF)!important;color:#53627a!important;box-shadow:none!important
                  }
                  html body #weekGrid.hideWeekLunch70 .wc:is(.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic) .dynamicLunchOverlay{
                    background:transparent!important;color:inherit!important;box-shadow:none!important
                  }
                `;
                document.head.appendChild(style);

                function selectedText(select){
                  if(!select)return '';
                  const option=select.options&&select.selectedIndex>=0?select.options[select.selectedIndex]:null;
                  return String(option?option.textContent:select.value||'').trim();
                }
                function measure(select,min,max){
                  if(!select)return;
                  const text=selectedText(select);
                  const span=document.createElement('span'),cs=getComputedStyle(select);
                  span.textContent=text||' ';
                  span.style.cssText='position:absolute;visibility:hidden;white-space:nowrap;pointer-events:none;font:'+cs.font+';font-weight:'+cs.fontWeight+';';
                  document.body.appendChild(span);
                  const width=Math.ceil(span.getBoundingClientRect().width)+31;
                  span.remove();
                  select.style.setProperty('width',Math.max(min,Math.min(max,width))+'px','important');
                }
                function fitAll(){
                  measure(document.getElementById('widgetTopBarMode672'),76,150);
                  measure(document.getElementById('widgetBottomBarMode672'),76,150);
                  measure(document.getElementById('advAccess'),82,176);
                  measure(document.getElementById('advProfileSelect'),96,228);
                }
                function bind(select,min,max){
                  if(!select||select.__feedback689)return;
                  select.__feedback689=true;
                  select.addEventListener('change',()=>requestAnimationFrame(()=>measure(select,min,max)),{passive:true});
                  const observer=new MutationObserver(()=>requestAnimationFrame(()=>measure(select,min,max)));
                  observer.observe(select,{childList:true,subtree:true,characterData:true});
                }
                function refresh(){
                  bind(document.getElementById('widgetTopBarMode672'),76,150);
                  bind(document.getElementById('widgetBottomBarMode672'),76,150);
                  bind(document.getElementById('advAccess'),82,176);
                  bind(document.getElementById('advProfileSelect'),96,228);
                  fitAll();
                }
                window.refreshFeedback689=refresh;

                ['refreshSettingsV3','refreshAdvancedFeatures','refreshSettingsLayout','refreshFeedback672','refreshFeedback665'].forEach(name=>{
                  const old=window[name];if(typeof old!=='function'||old.__feedback689)return;
                  const wrapped=function(){const result=old.apply(this,arguments);queueMicrotask(refresh);return result};
                  wrapped.__feedback689=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}
                });
                refresh();
                requestAnimationFrame(refresh);
              }catch(e){console.error('Feedback689Ui',e)}
            })();
            """;
    }
}
