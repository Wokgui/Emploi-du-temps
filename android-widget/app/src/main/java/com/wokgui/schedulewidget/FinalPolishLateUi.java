package com.wokgui.schedulewidget;

final class FinalPolishLateUi {
    private FinalPolishLateUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__finalPolishLateV2){if(window.refreshFinalPolish)window.refreshFinalPolish();return}
                window.__finalPolishLateV2=true;
                let late=0;
                function rerun(delay){
                  if(late)clearTimeout(late);
                  const run=()=>{try{if(window.refreshFinalPolish)window.refreshFinalPolish()}catch(e){}};
                  if(delay&&delay>0)late=setTimeout(run,delay);else run();
                }
                function wrap(name,delay){
                  const old=window[name];if(typeof old!=='function'||old.__finalLateWrapped)return;
                  const w=function(){const r=old.apply(this,arguments);rerun(delay);return r};w.__finalLateWrapped=true;window[name]=w;
                }
                function install(){
                  /* Only settings/bulk refreshes need a late layout pass.
                     Lunch and week-grid observers were intentionally removed: they caused repeated repaint cycles and visible blinking. */
                  wrap('refreshSettingsV3',0);
                  wrap('refreshBulkCourseUi',0);
                  rerun(0);
                }
                install();
              }catch(e){console.log('FinalPolishLateUi',e)}
            })();
            """;
    }
}
