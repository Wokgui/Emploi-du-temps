package com.wokgui.schedulewidget;

/** Queues an OCR result safely until the expensive photo-import UI has loaded while idle. */
final class LazyImportBootstrapUi {
    private LazyImportBootstrapUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtLazyImportBootstrap)return;
                window.__edtLazyImportBootstrap=true;
                window.__edtPendingOcrRaw=null;

                window.applyOcrSchedule=function(raw){
                  window.__edtPendingOcrRaw=raw;
                  var button=document.getElementById('importPhoto'),status=document.getElementById('importStatus');
                  if(button){button.disabled=true;button.textContent='Préparation de l’import…'}
                  if(status)status.textContent='Préparation de la vérification des cours détectés.';
                };
              }catch(e){console.log('LazyImportBootstrapUi',e)}
            })();
            """;
    }
}
