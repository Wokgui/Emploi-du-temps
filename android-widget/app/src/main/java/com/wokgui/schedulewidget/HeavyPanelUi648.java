package com.wokgui.schedulewidget;

/**
 * Owns the lifetime and opening path of Settings and the course editor.
 *
 * <p>Both sheets stay mounted and laid out for the whole WebView session. Legacy modules can
 * contribute one idempotent course-field preparer at startup; opening the editor then updates
 * only values that depend on the selected course. A dedicated state attribute owns visibility,
 * so opening a sheet cannot wake historical observers attached to the legacy {@code show} class.</p>
 */
final class HeavyPanelUi648 {
    private HeavyPanelUi648() {}

    static String prelude() {
        return """
            (function(){
              if(window.__edtPanelObserverRegistry648)return;
              const registry=[];window.__edtPanelObserverRegistry648=registry;
              const inputOwner={metric:null,route:null,lastPanel:'',lastAt:0,count:0,labelCounts:Object.create(null)};window.__edtHeavyInputOwner648=inputOwner;
              function logInput(target){
                if(inputOwner.scenario&&inputOwner.scenario!=='physical')return;
                let label=target.id||'course';
                if(target.id==='settingsBtn')label='settings';
                else if(target.classList&&target.classList.contains('editCourse'))label='edit-course';
                else if(target.classList&&target.classList.contains('todayCourse'))label='today-course';
                else if(target.classList&&target.classList.contains('wc'))label='week-cell';
                inputOwner.count++;
                const labelCount=(inputOwner.labelCounts[label]||0)+1;inputOwner.labelCounts[label]=labelCount;
                if(((label==='settings'||label==='edit-course')&&labelCount<=60)||inputOwner.count<=6||inputOwner.count%20===0)console.log('EDT_FAST_INPUT|'+label+'|visual|delegated');
                if(inputOwner.count%100===0)console.log('EDT_FAST_STATS|clicks='+inputOwner.count+'|submits=0|scheduled=0|executed='+inputOwner.count+'|cancelled=0|routers=1|wrappers=0');
              }
              function inputTarget(node){
                return node&&node.closest?node.closest('#settingsBtn,#settingsX,#settingsDone,#settingsModal,#addCourse,#cancelEdit,#modal,.editCourse,.todayCourse,.wc'):null;
              }
              const nativeAdd=EventTarget.prototype.addEventListener;
              nativeAdd.call(document,'pointerdown',function(event){
                const target=inputTarget(event.target);if(!target||!inputOwner.route)return;
                const started=performance.now();if(inputOwner.metric)inputOwner.metric(event,started);
                inputOwner.lastPanel=(target.id&&target.id.startsWith('settings'))?'settings':(target.id==='settingsModal'?'settings':'course');inputOwner.lastAt=started;
                logInput(target);
                inputOwner.route(target,event);event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
              },{capture:true,passive:false});
              nativeAdd.call(document,'click',function(event){
                const target=inputTarget(event.target);if(!target)return;
                const recent=performance.now()-inputOwner.lastAt<900;
                if(!recent&&inputOwner.route){if(inputOwner.metric)inputOwner.metric(event,performance.now());logInput(target);inputOwner.route(target,event)}
                event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
              },true);
              function wrap(name){
                const Native=window[name];if(typeof Native!=='function')return;
                window[name]=new Proxy(Native,{construct(Target,args){
                  const bypass=!!window.__edtAllowPanelObserver648,callback=args[0];
                  const forwarded=bypass?callback:function(records,observer){
                    const owner=window.__edtHeavyPanelsOwnObservers648;
                    if(!owner)return callback.call(this,records,observer);
                    const filtered=records.filter(record=>{
                      const target=record&&record.target;
                      return !(target&&(target===owner.settings||target===owner.course||owner.settings.contains(target)||owner.course.contains(target)));
                    });
                    if(filtered.length)return callback.call(this,filtered,observer);
                  };
                  const instance=Reflect.construct(Target,[forwarded].concat(args.slice(1))),observe=instance.observe;
                  instance.observe=function(target,options){
                    registry.push({instance:instance,target:target,type:name});
                    const owner=window.__edtHeavyPanelsOwnObservers648;
                    if(owner&&!window.__edtAllowPanelObserver648&&target&&(target===owner.settings||target===owner.course||owner.settings.contains(target)||owner.course.contains(target)))return;
                    return observe.call(instance,target,options);
                  };
                  return instance;
                }});
              }
              wrap('MutationObserver');wrap('ResizeObserver');
            })();
            """;
    }

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtHeavyPanels648)return;
                const preparationStarted=performance.now();
                const settings=document.getElementById('settingsModal');
                const course=document.getElementById('modal');
                const settingsSheet=document.getElementById('settingsSheet');
                const courseForm=document.getElementById('courseForm');
                if(!settings||!course||!settingsSheet||!courseForm)return;

                if(!document.getElementById('edtHeavyPanels648Style')){
                  const style=document.createElement('style');style.id='edtHeavyPanels648Style';
                  style.textContent=`
                    #settingsModal.edtHeavyPanel648,#modal.edtHeavyPanel648{
                      display:flex!important;visibility:hidden!important;
                      pointer-events:none!important;contain:layout style paint;
                    }
                    #settingsModal.edtHeavyPanel648[data-edt-open="true"],
                    #modal.edtHeavyPanel648[data-edt-open="true"]{
                      visibility:visible!important;pointer-events:auto!important;
                    }
                  `;
                  document.head.appendChild(style);
                }

                function mount(panel){
                  const alreadyOpen=panel.classList.contains('show');
                  panel.classList.add('edtHeavyPanel648');
                  if(alreadyOpen)panel.classList.remove('show');
                  panel.setAttribute('data-edt-open',alreadyOpen?'true':'false');
                  panel.setAttribute('aria-hidden',alreadyOpen?'false':'true');
                }
                mount(settings);mount(course);

                function isOpen(panel){return panel.getAttribute('data-edt-open')==='true'}
                function setText(element,value){if(element&&element.textContent!==value)element.textContent=value}
                function setValue(element,value){value=String(value==null?'':value);if(element&&element.value!==value)element.value=value}
                function setFlag(element,name,value){if(element&&element[name]!==value)element[name]=value}
                function setOpen(panel,value){
                  const next=value?'true':'false';if(panel.getAttribute('data-edt-open')===next)return false;
                  if(!value&&panel.contains(document.activeElement)){
                    try{document.activeElement.blur()}catch(e){}
                  }
                  panel.setAttribute('data-edt-open',next);panel.setAttribute('aria-hidden',value?'false':'true');
                  return true;
                }

                function openSettings(){setOpen(settings,true);return false}
                function closeSettings(){setOpen(settings,false);return false}
                function closeCourse(){setOpen(course,false);return false}

                // Replacing only the gear removes its accumulated historical target listeners.
                // The application-wide delegated router continues to own the single input path.
                const oldSettingsButton=document.getElementById('settingsBtn');
                let settingsButton=oldSettingsButton;
                if(oldSettingsButton){
                  settingsButton=oldSettingsButton.cloneNode(true);
                  oldSettingsButton.replaceWith(settingsButton);
                  settingsButton.onclick=openSettings;
                }

                const slotSelect=document.getElementById('fSlot');
                const slotPreview=document.getElementById('slotPreview');
                const customOption=document.createElement('option');customOption.value='0';
                const slotOptions=[];
                let slotLabelsKey='';
                const fragment=document.createDocumentFragment();fragment.appendChild(customOption);
                for(let i=0;i<9;i++){
                  const option=document.createElement('option');option.value=String(i+1);slotOptions.push(option);fragment.appendChild(option);
                }
                slotSelect.replaceChildren(fragment);

                function language(){
                  const value=String(document.documentElement.lang||'fr').toLowerCase();
                  return value.startsWith('de')?'de':(value.startsWith('en')?'en':'fr');
                }
                function periodName(n){
                  const lang=language();
                  if(lang==='de')return n+'. Stunde';
                  if(lang==='en')return n+(n===1?'st':(n===2?'nd':(n===3?'rd':'th')))+' period';
                  return n===1?'1ère heure':n+'ème heure';
                }
                function updateSlotOptions(selectedSlot,allowCustom,start,end){
                  const custom=!!(allowCustom&&start&&end),lang=language();
                  setFlag(customOption,'hidden',!custom);setFlag(customOption,'disabled',!custom);
                  setText(customOption,custom?(lang==='de'?'Aktuelle Zeit ('+start+'–'+end+')':(lang==='en'?'Current time ('+start+'–'+end+')':'Horaire actuel ('+start+'–'+end+')')):'');
                  const currentSlots=typeof slots!=='undefined'?slots:[];
                  const labelsKey=lang+'|'+currentSlots.map(function(value){return (value&&value.start||'')+'-'+(value&&value.end||'')}).join('|');
                  if(labelsKey!==slotLabelsKey){
                    slotLabelsKey=labelsKey;
                    for(let i=0;i<slotOptions.length;i++){
                      const value=currentSlots[i]||{start:'',end:''};
                      setText(slotOptions[i],periodName(i+1)+' · '+value.start+'–'+value.end);
                    }
                  }
                  const selected=Number(selectedSlot)||1;
                  setValue(slotSelect,selected===0&&custom?0:selected);
                  const n=Number(slotSelect.value);
                  if(n===0){
                    setText(slotPreview,lang==='de'?'Zeit beibehalten: '+start+'–'+end:(lang==='en'?'Time kept: '+start+'–'+end:'Horaire conservé : '+start+'–'+end));
                  }else{
                    const value=(typeof slots!=='undefined'&&slots[n-1])?slots[n-1]:{start:'',end:''};
                    setText(slotPreview,lang==='de'?'Angewandte Zeit: '+value.start+'–'+value.end:(lang==='en'?'Applied time: '+value.start+'–'+value.end:'Horaire appliqué : '+value.start+'–'+value.end));
                  }
                }

                function titleFor(editingCourse){
                  const lang=language(),week=typeof activeWeek!=='undefined'?activeWeek:'A';
                  if(lang==='de')return (editingCourse?'Bearbeiten':'Hinzufügen')+' · Woche '+week;
                  if(lang==='en')return (editingCourse?'Edit':'Add')+' · Week '+week;
                  return (editingCourse?'Modifier':'Ajouter')+' · Semaine '+week;
                }
                function prepareContributedFields(){
                  const list=window.__edtCoursePanelPreparers648||[];
                  for(const entry of list){
                    try{(typeof entry==='function'?entry:entry.run)()}catch(error){console.error('EDT_HEAVY_PREP_ERROR|id='+(entry&&entry.id||'unknown')+'|message='+error)}
                  }
                }
                function openCourse(index){
                  try{
                    state=weeks[activeWeek];editing=index;
                    const editingCourse=index!=null&&state[selected]?state[selected].courses[index]||null:null;
                    setText(document.getElementById('modalTitle'),titleFor(editingCourse));
                    setValue(document.getElementById('fLabel'),editingCourse&&editingCourse.label||'');
                    setValue(document.getElementById('fRoom'),editingCourse&&editingCourse.room||'');
                    setFlag(document.getElementById('deleteCourse'),'hidden',!editingCourse);
                    let chosen;
                    if(editingCourse)chosen=editingCourse.slot||slotForTimes(editingCourse.start,editingCourse.end)||0;
                    else if(newPrefill)chosen=slotForTimes(newPrefill.start,newPrefill.end)||0;
                    else chosen=firstFreeSlot(selected);
                    updateSlotOptions(chosen,!!editingCourse||!!newPrefill,editingCourse&&editingCourse.start||newPrefill&&newPrefill.start,editingCourse&&editingCourse.end||newPrefill&&newPrefill.end);
                    prepareContributedFields();
                    setOpen(course,true);
                  }catch(error){console.error('EDT_HEAVY_OPEN_ERROR|panel=course|message='+error)}
                  return false;
                }

                const inputOwner=window.__edtHeavyInputOwner648;
                if(inputOwner)inputOwner.route=function(target,event){
                  const counters=window.__edtHeavyPerfPrelude648&&window.__edtHeavyPerfPrelude648.counters;
                  const before=counters?{bridge:counters.bridgeCalls,reads:counters.storageReads,writes:counters.storageWrites,listeners:counters.listenerAdds,observers:counters.mutationObservers,resize:counters.resizeObservers}:null;
                  let result;
                  if(target.id==='settingsBtn')result=openSettings();
                  else if(target.id==='settingsX'||target.id==='settingsDone'||target.id==='settingsModal')result=closeSettings();
                  else if(target.id==='cancelEdit'||target.id==='modal'){closeCourse();editing=null;newPrefill=null;result=false}
                  else if(target.id==='addCourse')result=openCourse(null);
                  else if(typeof target.onclick==='function')result=target.onclick.call(target,event);
                  if(before){
                    const totals=window.__edtHeavyDirectWork648||(window.__edtHeavyDirectWork648={transitions:0,bridgeCalls:0,storageReads:0,storageWrites:0,listenerAdds:0,observerDelta:0,resizeObserverDelta:0});
                    totals.transitions++;totals.bridgeCalls+=counters.bridgeCalls-before.bridge;totals.storageReads+=counters.storageReads-before.reads;totals.storageWrites+=counters.storageWrites-before.writes;
                    totals.listenerAdds+=counters.listenerAdds-before.listeners;totals.observerDelta+=counters.mutationObservers-before.observers;totals.resizeObserverDelta+=counters.resizeObservers-before.resize;
                  }
                  return result;
                };

                window.fillSlotOptions=updateSlotOptions;
                window.openEditor=openCourse;
                try{fillSlotOptions=updateSlotOptions;openEditor=openCourse}catch(e){}

                const cancel=document.getElementById('cancelEdit');
                if(cancel)cancel.onclick=function(){closeCourse();editing=null;newPrefill=null;return false};
                const remove=document.getElementById('deleteCourse');
                if(remove)remove.onclick=function(){
                  if(editing!=null&&confirm(language()==='de'?'Diese Stunde löschen?':(language()==='en'?'Delete this class?':'Supprimer ce cours ?'))){
                    state[selected].courses.splice(editing,1);closeCourse();editing=null;newPrefill=null;if(typeof save==='function')save();
                  }
                  return false;
                };
                const add=document.getElementById('addCourse');if(add)add.onclick=function(){newPrefill=null;return openCourse(null)};
                settings.onclick=function(event){if(event.target===settings)closeSettings()};
                course.onclick=function(event){if(event.target===course){closeCourse();editing=null;newPrefill=null}};
                const settingsX=document.getElementById('settingsX');if(settingsX)settingsX.onclick=closeSettings;
                const settingsDone=document.getElementById('settingsDone');if(settingsDone)settingsDone.onclick=closeSettings;

                // Force the two hidden sheets through layout once, outside any interaction.
                updateSlotOptions(1,false,'','');prepareContributedFields();
                const preparedSettingsHeight=settingsSheet.offsetHeight;
                const preparedCourseHeight=courseForm.offsetHeight;
                window.__edtHeavyPanelsOwnObservers648={settings:settings,course:course};
                let disconnectedObservers=0;
                for(const entry of window.__edtPanelObserverRegistry648||[]){
                  const target=entry&&entry.target;
                  if(target&&(target===settings||target===course||settings.contains(target)||course.contains(target))){
                    try{entry.instance.disconnect();disconnectedObservers++}catch(e){}
                  }
                }
                window.closeCoursePanel648=closeCourse;
                window.__edtHeavyPanels648={
                  settings:settings,course:course,openSettings:openSettings,closeSettings:closeSettings,
                  openCourse:openCourse,closeCourse:closeCourse,isOpen:function(name){return isOpen(name==='settings'?settings:course)},
                  preparedSettingsHeight:preparedSettingsHeight,preparedCourseHeight:preparedCourseHeight
                };
                console.log('EDT_HEAVY_OWNER|ready|prepareMs='+(Math.round((performance.now()-preparationStarted)*10)/10)+'|navigationMs='+(Math.round(performance.now()*10)/10)+'|settingsHeight='+preparedSettingsHeight+'|courseHeight='+preparedCourseHeight+'|settingsButtons='+(settings.querySelectorAll('button').length)+'|courseFields='+(courseForm.querySelectorAll('input,select').length)+'|slotOptions='+slotSelect.options.length+'|preparers='+(window.__edtCoursePanelPreparers648||[]).length+'|disconnectedObservers='+disconnectedObservers+'|persistentOpacityLayer=0');
              }catch(e){console.error('HeavyPanelUi648',e)}
            })();
            """;
    }
}
