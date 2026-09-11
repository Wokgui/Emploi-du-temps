package com.wokgui.schedulewidget;

/** Final visual cleanup: lunch is text-only in Today and Week views. */
final class LunchIconCleanupUi {
    private LunchIconCleanupUi() {}

    static String script() {
        return """
                (function(){
                  try{
                    if(window.__lunchIconCleanupV1)return;
                    window.__lunchIconCleanupV1=true;
                    var style=document.createElement('style');
                    style.id='lunchIconCleanupV1Style';
                    style.textContent=`
                      #todayList .todayCourse.lunch .label:before,
                      #weekGrid .wc.lunchCell .cellLabel:before{
                        content:none!important;
                        display:none!important;
                        margin:0!important;
                      }
                    `;
                    document.head.appendChild(style);
                  }catch(e){}
                })();
                """;
    }
}
