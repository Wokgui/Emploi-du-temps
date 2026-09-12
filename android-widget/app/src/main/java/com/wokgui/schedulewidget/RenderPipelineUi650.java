package com.wokgui.schedulewidget;

/**
 * Removes the remaining multi-step render chains from common editing interactions.
 *
 * <p>The legacy feature layers remain authoritative for data mutations. This layer only
 * coalesces their repeated save calls during one course submit, keeps static edit controls
 * mounted when switching weekday, and avoids rebuilding the edit view after a slot-time edit.
 */
final class RenderPipelineUi650 {
    private RenderPipelineUi650() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtRenderPipeline650)return;

                const stats={
                  courseSubmitTransactions:0,saveRequests:0,saveExecutions:0,coalescedSaves:0,
                  fastDaySwitches:0,slotEdits:0,suppressedRenders:0,editListBuilds:0
                };
                let submitTransaction=false,pendingSave=false,suppressRender=false;
                const nativeSave=window.save;
                const nativeRender=window.render;
                if(typeof nativeSave!=='function'||typeof nativeRender!=='function')return;

                function assignGlobal(name,value){
                  try{window[name]=value}catch(e){}
                  try{(0,eval)(name+'=window["'+name+'"]')}catch(e){}
                }

                function wrappedRender(){
                  if(suppressRender){stats.suppressedRenders++;return}
                  return nativeRender.apply(this,arguments);
                }
                wrappedRender.__edtPipeline650=true;
                assignGlobal('render',wrappedRender);

                function wrappedSave(){
                  stats.saveRequests++;
                  if(submitTransaction){pendingSave=true;stats.coalescedSaves++;return}
                  stats.saveExecutions++;
                  return nativeSave.apply(this,arguments);
                }
                wrappedSave.__edtPipeline650=true;
                assignGlobal('save',wrappedSave);

                function language(){
                  const value=String(document.documentElement.lang||'fr').toLowerCase();
                  return value.startsWith('de')?'de':(value.startsWith('en')?'en':'fr');
                }
                function emptyLabel(){const l=language();return l==='de'?'Keine Stunde an diesem Tag.':(l==='en'?'No class this day.':'Aucun cours ce jour.')}
                function courseCount(n){const l=language();return n+' '+(l==='de'?'Stunden':(l==='en'?(n===1?'class':'classes'):'cours'))}
                function escapeText(value){
                  if(typeof window.esc==='function')return window.esc(value);
                  return String(value==null?'':value).replace(/[&<>\"']/g,function(ch){return {'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#39;'}[ch]})
                }
                function slotSuffix(course){
                  if(!course||!course.slot)return language()==='fr'?' · horaire libre':'';
                  const n=Number(course.slot);
                  if(language()==='de')return ' · '+n+'. Stunde';
                  if(language()==='en')return ' · '+n+(n===1?'st':(n===2?'nd':(n===3?'rd':'th')))+' period';
                  return ' · '+(n===1?'1ère heure':n+'ème heure');
                }

                function buildEditList(){
                  try{
                    if(typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof selected==='undefined')return false;
                    state=weeks[activeWeek];
                    const day=state&&state[selected];if(!day||!Array.isArray(day.courses))return false;
                    const title=document.getElementById('editDayTitle'),count=document.getElementById('editCount'),box=document.getElementById('editList');
                    if(!box)return false;
                    const full=typeof FULL!=='undefined'?FULL[selected]:String(selected);
                    if(title)title.textContent=full+' · '+(language()==='de'?'Woche ':(language()==='en'?'Week ':'Semaine '))+activeWeek;
                    if(count)count.textContent=courseCount(day.courses.length);
                    const fragment=document.createDocumentFragment();
                    if(!day.courses.length){const empty=document.createElement('div');empty.className='empty';empty.textContent=emptyLabel();fragment.appendChild(empty)}
                    day.courses.forEach(function(course,index){
                      const row=document.createElement('div');row.className='editCourse';
                      const room=language()==='de'?'Raum ':(language()==='en'?'room ':'salle ');
                      row.innerHTML='<div class="time"><strong>'+escapeText(course.start)+'</strong><br>'+escapeText(course.end)+'</div><div><div class="label">'+escapeText(course.label)+'</div><div class="room">'+room+escapeText(course.room||'—')+slotSuffix(course)+'</div></div>';
                      row.onclick=function(){if(window.__edtHeavyPanels648)window.__edtHeavyPanels648.openCourse(index);else if(typeof openEditor==='function')openEditor(index)};
                      fragment.appendChild(row);
                    });
                    box.replaceChildren(fragment);stats.editListBuilds++;
                    if(window.invalidateTimetableViews)window.invalidateTimetableViews(['today','week']);
                    return true;
                  }catch(error){console.error('EDT_PIPELINE_ERROR|edit-list|'+error);return false}
                }

                function fastSwitchDay(day){
                  day=Number(day);
                  if(![2,3,4,5,6].includes(day))return false;
                  selected=day;
                  document.querySelectorAll('#dayTabs .dayTab:not(.weekendAdd)').forEach(function(button){button.classList.toggle('active',Number(button.dataset.day)===day)});
                  if(!buildEditList())return false;
                  stats.fastDaySwitches++;
                  if(stats.fastDaySwitches<=4||stats.fastDaySwitches%50===0)console.log('EDT_PIPELINE_DAY|n='+stats.fastDaySwitches+'|day='+day+'|slotRows='+(document.querySelectorAll('#slotSettings .slotRow').length));
                  return false;
                }

                function bindDayTabs(){
                  document.querySelectorAll('#dayTabs .dayTab:not(.weekendAdd)').forEach(function(button){
                    const day=Number(button.dataset.day);if(![2,3,4,5,6].includes(day))return;
                    button.onclick=function(){return fastSwitchDay(day)};
                  });
                }
                const nativeRenderDayTabs=window.renderDayTabs;
                if(typeof nativeRenderDayTabs==='function'){
                  const wrapped=function(){const result=nativeRenderDayTabs.apply(this,arguments);bindDayTabs();return result};
                  wrapped.__edtPipeline650=true;assignGlobal('renderDayTabs',wrapped);
                }
                bindDayTabs();

                const courseForm=document.getElementById('courseForm');
                if(courseForm&&typeof courseForm.onsubmit==='function'){
                  const nativeSubmit=courseForm.onsubmit;
                  courseForm.onsubmit=function(event){
                    stats.courseSubmitTransactions++;submitTransaction=true;pendingSave=false;
                    let result;
                    try{result=nativeSubmit.call(this,event)}
                    finally{
                      submitTransaction=false;
                      if(pendingSave){pendingSave=false;stats.saveExecutions++;nativeSave()}
                      if(stats.courseSubmitTransactions<=4||stats.courseSubmitTransactions%25===0)console.log('EDT_PIPELINE_SUBMIT|n='+stats.courseSubmitTransactions+'|saveRequests='+stats.saveRequests+'|saveExecutions='+stats.saveExecutions+'|coalesced='+stats.coalescedSaves);
                    }
                    return result;
                  };
                  courseForm.onsubmit.__edtPipeline650=true;
                }

                document.addEventListener('change',function(event){
                  const input=event.target;
                  if(!input||input.tagName!=='INPUT'||input.type!=='time'||!input.closest('#slotSettings'))return;
                  const row=input.closest('.slotRow'),root=document.getElementById('slotSettings');if(!row||!root)return;
                  const rows=Array.from(root.querySelectorAll(':scope > .slotRow')),index=rows.indexOf(row);if(index<0||typeof slots==='undefined'||!slots[index])return;
                  event.stopPropagation();event.stopImmediatePropagation();
                  const fields=row.querySelectorAll('input[type="time"]');if(fields.length<2)return;
                  slots[index].start=fields[0].value;slots[index].end=fields[1].value;
                  try{for(const w of ['A','B','C','D'])for(const d of (typeof DAYS!=='undefined'?DAYS:[2,3,4,5,6]))for(const c of ((weeks[w]&&weeks[w][d]&&weeks[w][d].courses)||[]))if(Number(c.slot)===index+1){c.start=slots[index].start;c.end=slots[index].end}}catch(e){}
                  stats.slotEdits++;suppressRender=true;
                  try{wrappedSave()}finally{suppressRender=false}
                  buildEditList();
                  console.log('EDT_PIPELINE_SLOT|n='+stats.slotEdits+'|slot='+(index+1)+'|suppressedRenders='+stats.suppressedRenders);
                },true);

                window.__edtRenderPipeline650={stats:stats,fastSwitchDay:fastSwitchDay,buildEditList:buildEditList};
                console.log('EDT_PIPELINE|ready|version=6.50');
              }catch(error){console.error('RenderPipelineUi650',error)}
            })();
            """;
    }
}
