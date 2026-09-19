package com.wokgui.schedulewidget;

/** Final visual and layout ownership for 6.80 preview feedback. */
final class Feedback680Ui {
    private Feedback680Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback680)return;
                window.__feedback680=true;

                const style=document.createElement('style');
                style.id='feedback680Style';
                style.textContent=`
                  html body #viewEdit>#importPhoto{
                    display:flex!important;align-items:center!important;justify-content:center!important;
                    width:min(78%,380px)!important;margin-left:auto!important;margin-right:auto!important;text-align:center!important
                  }

                  html body #editActionRow680{
                    display:flex!important;align-items:stretch!important;justify-content:center!important;
                    gap:8px!important;width:100%!important;margin:7px auto 0!important
                  }
                  html body #editActionRow680>#addCourse,
                  html body #editActionRow680>#addBulkCourses{
                    flex:1 1 0!important;width:auto!important;min-width:0!important;min-height:46px!important;
                    margin:0!important;padding:7px 8px!important;text-align:center!important;white-space:normal!important
                  }

                  html body #viewEdit{padding-bottom:clamp(8px,2vh,18px)!important}
                  html body #courseColorHint{display:none!important}

                  html body #settingsWeekCycle678{
                    margin-bottom:7px!important;padding:8px 9px!important;border-width:1px!important;border-radius:10px!important
                  }
                  html body #settingsWeekCycle678 .settingTitle{font-size:.79rem!important;margin-bottom:6px!important}
                  html body #settingsWeekCycle678 .weekCycleChoice678,
                  html body #settingsWeekCycle678 .weekCurrentChoice678{
                    min-height:27px!important;padding:3px 4px!important;font-size:.62rem!important
                  }
                  html body #settingsWeekCycle678 .weekCurrentChoice678{flex-basis:44px!important}

                  html body #viewWeek #weekGrid#weekGrid .wh.timecol.week658LunchTime,
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Lunch,
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.lunch655Synthetic{
                    background:var(--week658-lunch,#FFE08A)!important;
                    background-color:var(--week658-lunch,#FFE08A)!important;
                    background-image:none!important;opacity:1!important;filter:none!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Lunch *,
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .dynamicLunchOverlay,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell .dynamicLunchOverlay,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell .nativeLunchLabel,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell .dynamicLunchOverlay{
                    background-color:transparent!important;background-image:none!important
                  }

                  html body #slotSettings .slotRemove{display:flex!important;align-items:center!important;justify-content:center!important}
                `;
                document.head.appendChild(style);

                function relabelHours(){
                  document.querySelectorAll('#viewEdit .sectionHead h3').forEach(h=>{
                    if(/Horaires|period times|Zeiten/i.test(h.textContent||''))h.textContent='Horaires des cours';
                  });
                }
                function arrangeActions(){
                  const list=document.getElementById('editList'),add=document.getElementById('addCourse');if(!list||!add)return;
                  let row=document.getElementById('editActionRow680');
                  if(!row){row=document.createElement('div');row.id='editActionRow680';list.insertAdjacentElement('afterend',row)}
                  if(add.parentNode!==row)row.appendChild(add);
                  const bulk=document.getElementById('addBulkCourses');if(bulk&&bulk.parentNode!==row)row.appendChild(bulk);
                }
                function refresh(){relabelHours();arrangeActions()}
                window.refreshFeedback680=refresh;
                refresh();
                requestAnimationFrame(refresh);
                setTimeout(refresh,120);
              }catch(e){console.error('Feedback680Ui',e)}
            })();
            """;
    }
}
