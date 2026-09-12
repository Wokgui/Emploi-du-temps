package com.wokgui.schedulewidget;

/** Coalesces known synchronous render bursts that still survive the legacy workflow layers. */
final class RenderBurstUi650 {
    private RenderBurstUi650() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtRenderBurst650)return;
                const stats={reloads:0,reloadSuppressed:0,ocrCorrections:0,ocrSuppressed:0};
                let reloadActive=false,reloadRenders=0,ocrActive=false,ocrRenders=0,ocrReleaseToken=0;
                const baseRender=window.render;
                if(typeof baseRender!=='function')return;

                function assign(name,value){try{window[name]=value}catch(e){}try{(0,eval)(name+'=window["'+name+'"]')}catch(e){}}
                function render(){
                  if(reloadActive&&reloadRenders++>0){stats.reloadSuppressed++;return}
                  if(ocrActive&&ocrRenders++>0){stats.ocrSuppressed++;return}
                  return baseRender.apply(this,arguments);
                }
                render.__edtRenderBurst650=true;assign('render',render);

                const baseReload=window.reloadSchedule;
                if(typeof baseReload==='function'){
                  const reload=function(){
                    stats.reloads++;reloadActive=true;reloadRenders=0;
                    try{return baseReload.apply(this,arguments)}
                    finally{reloadActive=false;if(stats.reloads<=4||stats.reloads%20===0)console.log('EDT_PIPELINE_RELOAD|n='+stats.reloads+'|renders='+reloadRenders+'|suppressed='+stats.reloadSuppressed)}
                  };
                  reload.__edtRenderBurst650=true;assign('reloadSchedule',reload);
                }

                function bindOcrCorrection(){
                  const button=document.getElementById('ocrPreviewCorrect86');
                  if(!button||typeof button.onclick!=='function'||button.onclick.__edtRenderBurst650)return;
                  const original=button.onclick;
                  const wrapped=function(event){
                    stats.ocrCorrections++;ocrActive=true;ocrRenders=0;const token=++ocrReleaseToken;
                    try{return original.call(this,event)}
                    finally{
                      requestAnimationFrame(function(){requestAnimationFrame(function(){if(token===ocrReleaseToken)ocrActive=false})});
                      if(stats.ocrCorrections<=4)console.log('EDT_PIPELINE_OCR|n='+stats.ocrCorrections+'|renders='+ocrRenders+'|suppressed='+stats.ocrSuppressed);
                    }
                  };
                  wrapped.__edtRenderBurst650=true;button.onclick=wrapped;
                }

                const baseApplyOcr=window.applyOcrSchedule;
                if(typeof baseApplyOcr==='function'){
                  const applyOcr=function(){const result=baseApplyOcr.apply(this,arguments);bindOcrCorrection();return result};
                  applyOcr.__edtRenderBurst650=true;assign('applyOcrSchedule',applyOcr);
                }
                bindOcrCorrection();

                window.__edtRenderBurst650={stats:stats,bindOcrCorrection:bindOcrCorrection};
                console.log('EDT_PIPELINE_BURST|ready');
              }catch(error){console.error('RenderBurstUi650',error)}
            })();
            """;
    }
}
