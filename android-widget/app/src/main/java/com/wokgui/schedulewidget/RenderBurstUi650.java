package com.wokgui.schedulewidget;

/** Coalesces the remaining synchronous duplicate render during schedule reload/profile switch. */
final class RenderBurstUi650 {
    private RenderBurstUi650() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtRenderBurst650)return;
                const stats={reloads:0,reloadRenders:0,reloadSuppressed:0};
                let reloadActive=false,reloadRenders=0;
                const baseRender=window.render;
                if(typeof baseRender!=='function')return;

                function assign(name,value){try{window[name]=value}catch(e){}try{(0,eval)(name+'=window["'+name+'"]')}catch(e){}}
                function render(){
                  if(reloadActive){
                    reloadRenders++;stats.reloadRenders++;
                    if(reloadRenders>1){stats.reloadSuppressed++;return}
                  }
                  return baseRender.apply(this,arguments);
                }
                render.__edtRenderBurst650=true;assign('render',render);

                const baseReload=window.reloadSchedule;
                if(typeof baseReload==='function'){
                  const reload=function(){
                    stats.reloads++;reloadActive=true;reloadRenders=0;
                    try{return baseReload.apply(this,arguments)}
                    finally{
                      reloadActive=false;
                      if(stats.reloads<=4||stats.reloads%20===0)console.log('EDT_PIPELINE_RELOAD|n='+stats.reloads+'|renders='+reloadRenders+'|suppressed='+stats.reloadSuppressed);
                    }
                  };
                  reload.__edtRenderBurst650=true;assign('reloadSchedule',reload);
                }

                window.__edtRenderBurst650={stats:stats};
                console.log('EDT_PIPELINE_BURST|ready|reload-only');
              }catch(error){console.error('RenderBurstUi650',error)}
            })();
            """;
    }
}
