package com.wokgui.schedulewidget;

/** Final 6.62 pass: view isolation, one break-label editor, and live custom labels. */
final class Feedback662Ui {
    private Feedback662Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback662){window.refreshFeedback662&&window.refreshFeedback662();return}
                window.__feedback662=true;
                const style=document.createElement('style');style.id='feedback662Style';style.textContent=`
                  html body main.wrap.edtInstantViews647>#viewWeek.view:not(.active) #weekGrid#weekGrid .week658LunchLabel,
                  html body main.wrap.edtInstantViews647>#viewWeek.view:not(.active) #weekGrid#weekGrid .week662GapLabel{display:none!important;visibility:hidden!important}
                  #viewEdit .breakNamesScope78,#viewEdit .breakWidgetRow78{display:none!important}
                  #breakNamesSettings763 .breakSettings .breakRow{align-items:stretch!important}
                  #breakNamesSettings763 .breakSettings .breakName{display:flex!important;align-items:center!important;justify-content:center!important;text-align:center!important;align-self:stretch!important;padding:0 2px!important}
                  #breakNamesSettings763 .breakSettings .dualBreakInputs .dualLabel{text-align:center!important;align-self:center!important}
                `;document.head.appendChild(style);

                function removeDuplicateWidgetRows(){
                  document.querySelectorAll('#viewEdit .breakNamesScope78,#viewEdit .breakWidgetRow78').forEach(node=>node.remove());
                }
                function repaint(){
                  removeDuplicateWidgetRows();
                  if(window.refreshWeekAppearance658)window.refreshWeekAppearance658();
                }
                function wireLabels(){
                  ['gapLabel','lunchLabel','gapLabelWidget','lunchLabelWidget'].forEach(id=>{
                    const input=document.getElementById(id);if(!input||input.__feedback662)return;input.__feedback662=true;
                    input.addEventListener('input',repaint);
                  });
                }
                function refresh(){removeDuplicateWidgetRows();wireLabels();repaint()}
                window.refreshFeedback662=refresh;
                refresh();
              }catch(e){console.error('Feedback662Ui',e)}
            })();
            """;
    }
}
