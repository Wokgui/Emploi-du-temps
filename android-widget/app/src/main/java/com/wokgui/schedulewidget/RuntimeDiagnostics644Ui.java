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
                let lastSubmit=null,submitTransitions=0,clicks=0;
                const ids=new WeakMap();let nextId=1;

                function fnId(fn){
                  if(typeof fn!=='function')return 0;
                  if(!ids.has(fn))ids.set(fn,nextId++);
                  return ids.get(fn);
                }
                function duplicateIds(){
                  const seen=new Set(),dups=new Set();
                  document.querySelectorAll('[id]').forEach(el=>{if(seen.has(el.id))dups.add(el.id);else seen.add(el.id)});
                  return dups.size;
                }
                function changedFunctions(){
                  const changed=[];
                  names.forEach(n=>{if(baseline[n]&&window[n]!==baseline[n])changed.push(n)});
                  return changed;
                }
                function markerFlags(fn){
                  if(typeof fn!=='function')return 'none';
                  const out=[];
                  if(fn.__edtFastProxy)out.push('fast');
                  if(fn.__fullColorWrapped)out.push('fullColor');
                  if(fn.__courseColorWrappedV2)out.push('courseColor');
                  if(fn.__courseBadgeWrappedV9)out.push('courseBadge');
                  if(fn.__widgetLabelsV14)out.push('widgetLabel');
                  return out.length?out.join('+'):'unmarked';
                }
                function snapshot(reason){
                  const changed=changedFunctions();
                  const nodes=document.getElementsByTagName('*').length;
                  const fast=window.__edtFastInteractionV2&&window.__edtFastInteractionV2.state;
                  const wrapped=fast?fast.wrapped:-1,formWrapped=fast?fast.formWrapped:-1;
                  const form=document.getElementById('courseForm');
                  const submit=form&&typeof form.onsubmit==='function'?form.onsubmit:null;
                  const submitId=fnId(submit);
                  let changedSubmit=false;
                  if(lastSubmit&&submit&&submit!==lastSubmit){submitTransitions++;changedSubmit=true}
                  if(submit)lastSubmit=submit;
                  console.log('EDT_RUNTIME_644|reason='+reason+'|nodes='+nodes+'|dupIds='+duplicateIds()+'|renderChanged='+changed.length+'|submitId='+submitId+'|submitChangedLast='+(changedSubmit?1:0)+'|submitTransitions='+submitTransitions+'|submitMarkers='+markerFlags(submit)+'|fastWrapped='+wrapped+'|formWrapped='+formWrapped);
                  if(changed.length)console.log('EDT_RUNTIME_RENDER_MUTATION|'+changed.join(','));
                  if(changedSubmit)console.log('EDT_RUNTIME_SUBMIT_TRANSITION|id='+submitId+'|markers='+markerFlags(submit));
                  return {nodes:nodes,duplicates:duplicateIds(),renderChanged:changed,submitId:submitId,submitTransitions:submitTransitions,fastWrapped:wrapped,formWrapped:formWrapped};
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
