package com.wokgui.schedulewidget;

final class FinalPolishLateUi {
    private FinalPolishLateUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__finalPolishLateV1){if(window.refreshFinalPolish)window.refreshFinalPolish();return}
                window.__finalPolishLateV1=true;
                let late=0;
                function rerun(delay){
                  if(late)clearTimeout(late);
                  late=setTimeout(()=>{try{if(window.refreshFinalPolish)window.refreshFinalPolish()}catch(e){}},delay==null?0:delay);
                }
                function wrap(name,delay){
                  const old=window[name];if(typeof old!=='function'||old.__finalLateWrapped)return;
                  const w=function(){const r=old.apply(this,arguments);rerun(delay);return r};w.__finalLateWrapped=true;window[name]=w;
                }
                function install(){
                  wrap('refreshSettingsV3',0);
                  wrap('refreshAdvancedFeatures',0);
                  wrap('refreshBulkCourseUi',0);
                  wrap('refreshLunchBreakUi',30);
                  wrap('refreshDoubleLunchUi',140);
                  const grid=document.getElementById('weekGrid');
                  if(grid&&!grid.__finalLateObserver){
                    grid.__finalLateObserver=true;
                    new MutationObserver(()=>rerun(140)).observe(grid,{childList:true,subtree:false});
                  }
                  rerun(0);
                }
                install();[80,220,500,1000,1800].forEach(ms=>setTimeout(install,ms));
              }catch(e){console.log('FinalPolishLateUi',e)}
            })();
            """;
    }
}
