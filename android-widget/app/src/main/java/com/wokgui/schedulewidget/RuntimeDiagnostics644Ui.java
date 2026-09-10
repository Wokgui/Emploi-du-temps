package com.wokgui.schedulewidget;

/** Lightweight diagnostics used to prove that long sessions no longer grow wrapper chains. */
final class RuntimeDiagnostics644Ui {
    private RuntimeDiagnostics644Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtRuntimeDiagnostics644)return;
                window.__edtRuntimeDiagnostics644=true;

                const names=[
                  'render','renderContext','renderToday','renderWeek','renderEdit','openEditor',
                  'refreshSettingsV3','refreshAdvancedFeatures','refreshFineTuneUi',
                  'refreshStability69','refreshStability70','refreshStability71','refreshStability72',
                  'refreshStability73','refreshStability74','refreshWeekendUi','refreshWorkflow85',
                  'refreshSettingsLayout'
                ];
                const baseline=Object.create(null);
                names.forEach(n=>{if(typeof window[n]==='function')baseline[n]=window[n]});
                const form=document.getElementById('courseForm');
                const baselineSubmit=form&&typeof form.onsubmit==='function'?form.onsubmit:null;
                let clicks=0;

                function duplicateIds(){
                  const seen=new Set(),dups=new Set();
                  document.querySelectorAll('[id]').forEach(el=>{if(seen.has(el.id))dups.add(el.id);else seen.add(el.id)});
                  return dups.size;
                }
                function changedFunctions(){
                  const changed=[];
                  names.forEach(n=>{if(baseline[n]&&window[n]!==baseline[n])changed.push(n)});
                  const currentForm=document.getElementById('courseForm');
                  if(baselineSubmit&&currentForm&&currentForm.onsubmit!==baselineSubmit)changed.push('courseForm.onsubmit');
                  return changed;
                }
                function snapshot(reason){
                  const changed=changedFunctions();
                  const nodes=document.getElementsByTagName('*').length;
                  const fast=window.__edtFastInteractionV2&&window.__edtFastInteractionV2.state;
                  const wrapped=fast?fast.wrapped:-1;
                  console.log('EDT_RUNTIME_644|reason='+reason+'|nodes='+nodes+'|dupIds='+duplicateIds()+'|changed='+changed.length+'|fastWrapped='+wrapped);
                  if(changed.length)console.log('EDT_RUNTIME_MUTATION|'+changed.join(','));
                  return {nodes:nodes,duplicates:duplicateIds(),changed:changed,fastWrapped:wrapped};
                }

                document.addEventListener('click',function(){
                  clicks++;
                  if(clicks%16===0)requestAnimationFrame(()=>snapshot('click-'+clicks));
                },true);
                window.edtRuntime644Snapshot=snapshot;
                snapshot('baseline');
              }catch(e){console.log('RuntimeDiagnostics644Ui',e)}
            })();
            """;
    }
}
