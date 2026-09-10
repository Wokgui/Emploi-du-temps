package com.wokgui.schedulewidget;

/** Final narrow-screen layout polish for 6.44. No recurring observers or timers. */
final class Polish644Ui {
    private Polish644Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(!document.getElementById('polish644Style')){
                  const style=document.createElement('style');
                  style.id='polish644Style';
                  style.textContent=`
                    /* Course editor: centered full-colour title. */
                    #fullCourseColorBox .fullColorHead{
                      display:grid!important;
                      grid-template-columns:minmax(0,1fr) auto minmax(0,1fr)!important;
                      align-items:center!important;
                      gap:6px!important;
                    }
                    #fullCourseColorBox .fullColorTitle{
                      grid-column:2!important;
                      text-align:center!important;
                      justify-self:center!important;
                    }
                    #fullCourseColorBox #fullCourseHex{
                      grid-column:3!important;
                      justify-self:end!important;
                      text-align:right!important;
                    }

                    /* Four course actions: one row, four equal columns, no overlap. */
                    #courseForm .sheetActions{
                      display:grid!important;
                      grid-template-columns:repeat(4,minmax(0,1fr))!important;
                      gap:5px!important;
                      width:100%!important;
                      align-items:stretch!important;
                    }
                    /* Adding a course only has Cancel/Save: let those two use the full row. */
                    #courseForm .sheetActions:has(#deleteCourse[hidden]){
                      grid-template-columns:repeat(2,minmax(0,1fr))!important;
                    }
                    #courseForm .sheetActions>.leftActions85,
                    #courseForm .sheetActions>.rightActions{display:contents!important}
                    #courseForm .sheetActions .btn{
                      min-width:0!important;
                      width:100%!important;
                      max-width:none!important;
                      padding:8px 2px!important;
                      font-size:.68rem!important;
                      line-height:1.05!important;
                      text-align:center!important;
                      white-space:nowrap!important;
                      overflow:hidden!important;
                      text-overflow:clip!important;
                    }

                    /* Reminder delay centered under the reminder toggle. */
                    .advRow:has(>#advReminderMinutes){justify-content:center!important}
                    .advRow:has(>#advReminderMinutes)>span:empty{display:none!important}
                    #advReminderMinutes{margin-inline:auto!important;text-align:center!important}

                    /* Profile actions: only this button group becomes one equal-width line. */
                    .advButtons:has(#advNewProfile){
                      display:grid!important;
                      grid-template-columns:repeat(3,minmax(0,1fr))!important;
                      gap:5px!important;
                      flex-wrap:nowrap!important;
                      width:100%!important;
                    }
                    .advButtons:has(#advNewProfile) .advButton{
                      min-width:0!important;
                      width:100%!important;
                      padding:7px 2px!important;
                      font-size:.66rem!important;
                      line-height:1.08!important;
                      text-align:center!important;
                      white-space:nowrap!important;
                    }

                    /* School-holiday activation and palette heading centered. */
                    #schoolCalendarBlock .schoolEnable{
                      justify-content:center!important;
                      text-align:center!important;
                    }
                    #schoolCalendarBlock:has(#schoolEnabled:not(:checked)) #schoolHint{display:none!important}
                    #appPaletteTitle{text-align:center!important;width:100%!important}

                    /* The card already says “Affichage du widget”: remove duplicate “Widget”. */
                    #widgetSettings86>.settingsSectionTitle86{display:none!important}
                    #widgetSettings86>.settingsSectionBody86{margin-top:0!important}

                    @media(max-width:380px){
                      #courseForm .sheetActions{gap:3px!important}
                      #courseForm .sheetActions .btn{font-size:.64rem!important;padding-inline:1px!important}
                      .advButtons:has(#advNewProfile){gap:3px!important}
                      .advButtons:has(#advNewProfile) .advButton{font-size:.62rem!important;padding-inline:1px!important}
                    }
                  `;
                  document.head.appendChild(style);
                }

                // Freeze the final wrapper order once all legacy layers have loaded.
                // FinalPolish already exists inside the chain from its startup pass. Mark
                // the current outer functions before every later refresh so its legacy
                // refresh() cannot wrap renderWeek/renderSlots again when another wrapper
                // hides the original __finalPolish marker.
                const markFinalPolishTargets=function(){
                  ['render','renderWeek','renderSlots','fillSlotOptions'].forEach(function(name){
                    try{if(typeof window[name]==='function')window[name].__finalPolish=true}catch(ignore){}
                  });
                };
                if(typeof window.refreshFinalPolish==='function'&&!window.refreshFinalPolish.__stable644){
                  const oldFinalRefresh=window.refreshFinalPolish;
                  const stableFinalRefresh=function(){markFinalPolishTargets();return oldFinalRefresh.apply(this,arguments)};
                  stableFinalRefresh.__stable644=true;
                  window.refreshFinalPolish=stableFinalRefresh;
                }

                if(window.__edtInstallWeekendWrappers)window.__edtInstallWeekendWrappers();
                markFinalPolishTargets();
                if(window.refreshFinalPolish)window.refreshFinalPolish();
              }catch(e){console.log('Polish644Ui',e)}
            })();
            """;
    }
}
