package com.wokgui.schedulewidget;

/** Final 6.61 settings cleanup: the configurable week section replaces legacy lunch/free controls. */
final class Feedback661Ui {
    private Feedback661Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback661)return;window.__feedback661=true;
                const style=document.createElement('style');style.id='feedback661Style';style.textContent=`
                  html body #settingsSheet #breakDisplaySetting,
                  html body #settingsSheet #fineSpecialColors{display:none!important}
                `;document.head.appendChild(style);
                const legacy=document.getElementById('breakDisplaySetting');if(legacy){legacy.setAttribute('aria-hidden','true');legacy.querySelectorAll('input,button,select').forEach(x=>x.tabIndex=-1)}
              }catch(e){console.error('Feedback661Ui',e)}
            })();
            """;
    }
}
