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
                let lastSubmit=null,submitTransitions=0,submitAssignments=0,clicks=0,editorOpens=0,editorCloses=0;
                const ids=new WeakMap();let nextId=1;

                function fnId(fn){
                  if(typeof fn!=='function')return 0;
                  if(!ids.has(fn))ids.set(fn,nextId++);
                  return ids.get(fn);
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
                  console.log('EDT_RUNTIME_644|reason='+reason+'|nodes='+nodes+'|dupIds='+duplicateIds()+'|renderChanged='+changed.length+'|submitId='+submitId+'|submitChangedLast='+(changedSubmit?1:0)+'|submitTransitions='+submitTransitions+'|submitAssignments='+submitAssignments+'|submitMarkers='+markerFlags(submit)+'|fastWrapped='+wrapped+'|formWrapped='+formWrapped+'|editorOpens='+editorOpens+'|editorCloses='+editorCloses);
                  if(changed.length)console.log('EDT_RUNTIME_RENDER_MUTATION|'+changed.join(','));
                  if(changedSubmit)console.log('EDT_RUNTIME_SUBMIT_TRANSITION|id='+submitId+'|markers='+markerFlags(submit));
                  return {nodes:nodes,duplicates:duplicateIds(),renderChanged:changed,submitId:submitId,submitTransitions:submitTransitions,submitAssignments:submitAssignments,fastWrapped:wrapped,formWrapped:formWrapped,editorOpens:editorOpens,editorCloses:editorCloses};
                }

                // Trace every future assignment to the native DOM onsubmit property while
                // preserving Chromium's own getter/setter semantics. This identifies the
                // exact legacy wrapper that still replaces the course form during use.
                const courseForm=document.getElementById('courseForm');
                if(courseForm){
                  let proto=courseForm,desc=null;
                  while(proto&&!desc){
                    const d=Object.getOwnPropertyDescriptor(proto,'onsubmit');
                    if(d&&typeof d.set==='function'&&typeof d.get==='function')desc=d;
                    else proto=Object.getPrototypeOf(proto);
                  }
                  if(proto&&desc&&!proto.__edtSubmitSpy644){
                    const nativeGet=desc.get,nativeSet=desc.set;
                    Object.defineProperty(proto,'onsubmit',{
                      configurable:desc.configurable,enumerable:desc.enumerable,
                      get:function(){return nativeGet.call(this)},
                      set:function(fn){
                        if(this&&this.id==='courseForm'){
                          submitAssignments++;
                          const current=nativeGet.call(this);
                          let stack='';try{stack=String(new Error().stack||'').split('\\n').slice(1,7).join(' <- ')}catch(ignore){}
                          console.log('EDT_SUBMIT_ASSIGN|count='+submitAssignments+'|fromId='+fnId(current)+'|fromMarkers='+markerFlags(current)+'|toId='+fnId(fn)+'|toMarkers='+markerFlags(fn)+'|stack='+stack);
                        }
                        return nativeSet.call(this,fn);
                      }
                    });
                    proto.__edtSubmitSpy644=true;
                  }
                }

                const modal=document.getElementById('modal');
                if(modal){
                  let shown=modal.classList.contains('show');
                  new MutationObserver(function(){
                    const next=modal.classList.contains('show');
                    if(next&&!shown){editorOpens++;console.log('EDT_EDITOR_OPEN|count='+editorOpens);snapshot('editor-open-'+editorOpens)}
                    else if(!next&&shown){editorCloses++;console.log('EDT_EDITOR_CLOSE|count='+editorCloses)}
                    shown=next;
                  }).observe(modal,{attributes:true,attributeFilter:['class']});
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