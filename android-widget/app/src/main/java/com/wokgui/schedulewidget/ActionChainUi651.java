package com.wokgui.schedulewidget;

/**
 * 6.51 interaction coordinator.
 *
 * <p>Legacy feature layers remain responsible for their data. This final layer gives one user
 * action one commit: duplicate whole-view renders are suppressed while the action runs, caches
 * are invalidated once, and at most the visible timetable target is refreshed afterwards.
 */
final class ActionChainUi651 {
    private ActionChainUi651() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtActionChains651)return;

                const stats={
                  actions:0,suppressedRenders:0,targetedRenders:0,noRenderCommits:0,
                  weekSelections:0,currentWeekChanges:0,advancedChanges:0,copies:0,
                  deletes:0,profileChanges:0,schoolChanges:0,weekModeChanges:0,
                  localRefreshes:0,advancedRefreshes:0,historySyncs:0,breakChanges:0,
                  ocrImports:0,undoRedo:0,bulkSaves:0
                };
                let depth=0,action=null;
                const baseRender=window.render,baseSave=window.save;
                if(typeof baseRender!=='function'||typeof baseSave!=='function')return;

                function assign(name,value){
                  try{window[name]=value}catch(e){}
                  try{(0,eval)(name+'=window["'+name+'"]')}catch(e){}
                }
                function modeNow(){try{return ['today','week','edit'].includes(mode)?mode:'edit'}catch(e){return 'edit'}}
                function clone(o){try{return JSON.parse(JSON.stringify(o))}catch(e){return o}}
                function nav(){return window.__edtNavigationCacheV2||null}
                function pipeline(){return window.__edtRenderPipeline650||null}
                function invalidate(targets){
                  try{if(window.invalidateTimetableViews)window.invalidateTimetableViews(targets)}catch(e){}
                }
                function mark(targets,renderCurrent,fastEdit){
                  if(!action)return;
                  (Array.isArray(targets)?targets:[targets]).filter(Boolean).forEach(t=>action.targets.add(t));
                  if(renderCurrent)action.renderCurrent=true;
                  if(fastEdit)action.fastEdit=true;
                }
                function markScheduleChanged(){if(action)action.scheduleChanged=true}
                function markSyncContext(){if(action)action.syncContext=true}

                function wrappedRender(){
                  if(depth>0){stats.suppressedRenders++;if(action)action.renderRequests++;return}
                  return baseRender.apply(this,arguments);
                }
                wrappedRender.__edtActionChains651=true;assign('render',wrappedRender);

                function wrappedSave(){
                  if(action){action.saveCalls++;action.scheduleChanged=true}
                  return baseSave.apply(this,arguments);
                }
                wrappedSave.__edtActionChains651=true;assign('save',wrappedSave);

                function activateTarget(target){
                  const n=nav(),button=document.querySelector('.nav[data-mode="'+target+'"]');
                  if(n&&typeof n.activate==='function')n.activate(target,button);
                  else{
                    document.querySelectorAll('.nav').forEach(b=>b.classList.toggle('active',b.dataset.mode===target));
                    document.querySelectorAll('.view').forEach(v=>v.classList.remove('active'));
                    const view=document.getElementById('view'+target.charAt(0).toUpperCase()+target.slice(1));if(view)view.classList.add('active');
                    try{mode=target}catch(e){}
                  }
                }
                function renderTarget(target,fastEdit){
                  const n=nav(),p=pipeline();
                  if(target==='edit'&&fastEdit&&p&&typeof p.buildEditList==='function'){
                    p.buildEditList();
                    if(n&&typeof n.markClean==='function')n.markClean('edit');
                    stats.targetedRenders++;return 'edit-list';
                  }
                  if(n&&typeof n.renderTarget==='function'){
                    const result=n.renderTarget(target);stats.targetedRenders++;return result;
                  }
                  if(target==='today'&&typeof renderToday==='function')renderToday();
                  else if(target==='week'&&typeof renderWeek==='function')renderWeek();
                  else if(target==='edit'&&typeof renderEdit==='function')renderEdit();
                  stats.targetedRenders++;return 'fallback';
                }
                function syncHistory(){
                  try{if(window.__edtSyncEditHistory651){window.__edtSyncEditHistory651();stats.historySyncs++}}catch(e){}
                }

                function queueLocalRefresh(fn){
                  if(typeof fn!=='function')return;
                  if(action){if(!action.localRefreshes.includes(fn))action.localRefreshes.push(fn);return}
                  try{fn();stats.localRefreshes++}catch(e){}
                }
                function queueAdvancedRefresh(){
                  if(action){action.advancedRefresh=true;return 0}
                  try{if(window.refreshAdvancedFeatures){window.refreshAdvancedFeatures();stats.advancedRefreshes++}}catch(e){}
                  return 0;
                }
                function schoolCalendarChanged(){
                  stats.schoolChanges++;
                  if(action){action.advancedRefresh=true;mark(['today','week'],true,false);return}
                  run('school-calendar',function(){if(action)action.advancedRefresh=true;mark(['today','week'],true,false)},{});
                }

                function finish(ctx){
                  // Keep suppression active while legacy follow-up refreshes run.
                  for(const fn of ctx.localRefreshes){try{fn();stats.localRefreshes++}catch(e){}}
                  if(ctx.advancedRefresh){try{if(window.refreshAdvancedFeatures){window.refreshAdvancedFeatures();stats.advancedRefreshes++}}catch(e){}}
                  if(ctx.syncContext){try{if(typeof renderContext==='function')renderContext()}catch(e){}}
                  const targets=[...ctx.targets];if(targets.length)invalidate(targets);

                  depth=0;action=null;
                  const active=modeNow();
                  if(ctx.renderCurrent&&targets.includes(active))renderTarget(active,ctx.fastEdit&&active==='edit');
                  else stats.noRenderCommits++;
                  if(ctx.scheduleChanged)syncHistory();
                  stats.actions++;
                  if(stats.actions<=8||stats.actions%50===0){
                    console.log('EDT_ACTION_651|n='+stats.actions+'|name='+ctx.name+'|save='+ctx.saveCalls+'|renderReq='+ctx.renderRequests+'|targets='+targets.join(',')+'|targeted='+stats.targetedRenders+'|suppressed='+stats.suppressedRenders);
                  }
                }
                function run(name,fn,options){
                  if(depth>0)return fn();
                  const ctx={name:name||'action',targets:new Set(),renderCurrent:false,fastEdit:false,syncContext:false,scheduleChanged:false,saveCalls:0,renderRequests:0,localRefreshes:[],advancedRefresh:false};
                  action=ctx;depth=1;
                  if(options&&options.targets)mark(options.targets,options.renderCurrent!==false,options.fastEdit===true);
                  if(options&&options.scheduleChanged)ctx.scheduleChanged=true;
                  if(options&&options.syncContext)ctx.syncContext=true;
                  let result,error;
                  try{result=fn()}catch(e){error=e}
                  finally{finish(ctx)}
                  if(error)throw error;
                  return result;
                }

                function advanced(){
                  try{return JSON.parse(window.AndroidSchedule&&AndroidSchedule.loadAdvancedSettings?AndroidSchedule.loadAdvancedSettings():'{}')}catch(e){return {}}
                }
                function weekLetters(){
                  const a=advanced();if(a.singleWeek===true)return ['A'];
                  const n=Math.max(2,Math.min(4,Number(a.cycleLength)||2));return ['A','B','C','D'].slice(0,n);
                }
                function selectWeek(letter){
                  const letters=weekLetters();if(!letters.includes(letter))return true;
                  return run('select-week',function(){
                    if(typeof activeWeek!=='undefined'&&activeWeek===letter)return;
                    try{activeWeek=letter}catch(e){}
                    stats.weekSelections++;mark(['week','edit'],true,modeNow()==='edit');markSyncContext();
                  },{}),true;
                }
                function advanceCurrentWeek(){
                  const letters=weekLetters();if(letters.length<2)return true;
                  return run('current-week',function(){
                    let current='A';try{current=currentWeek}catch(e){}
                    const i=Math.max(0,letters.indexOf(current)),next=letters[(i+1)%letters.length];
                    try{currentWeek=next;activeWeek=next}catch(e){}
                    try{if(window.AndroidSchedule&&AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(next)}catch(e){}
                    stats.currentWeekChanges++;mark(['today','week','edit'],true,modeNow()==='edit');markSyncContext();
                  },{}),true;
                }

                function setMode651(target){
                  if(!['today','week','edit'].includes(target))return;
                  if(target==='today'){try{activeWeek=currentWeek}catch(e){}}
                  activateTarget(target);
                  if(depth>0){mark([target],true,target==='edit');return}
                  renderTarget(target,false);
                }
                setMode651.__edtActionChains651=true;assign('setModeFromAndroid',setMode651);assign('setMode',setMode651);

                function changeWeekMode(n){
                  n=Number(n);if(![1,2,3].includes(n))return true;
                  run('week-mode-'+n,function(){
                    const a=advanced();a.singleWeek=n===1;a.cycleLength=n===3?3:2;
                    try{if(window.AndroidSchedule&&AndroidSchedule.saveAdvancedSettings)AndroidSchedule.saveAdvancedSettings(JSON.stringify(a))}catch(e){}
                    const letters=n===1?['A']:['A','B','C'].slice(0,n===3?3:2);
                    try{
                      if(typeof weeks!=='undefined'){
                        if(!weeks.C&&weeks.A)weeks.C=clone(weeks.A);if(!weeks.D&&weeks.B)weeks.D=clone(weeks.B);
                        if(n===1&&weeks.A){weeks.B=clone(weeks.A);weeks.C=clone(weeks.A);weeks.D=clone(weeks.A);currentWeek='A';activeWeek='A';state=weeks.A}
                        else{if(!letters.includes(currentWeek))currentWeek='A';if(!letters.includes(activeWeek))activeWeek=currentWeek}
                      }
                    }catch(e){}
                    if(n===1){
                      try{
                        const root=JSON.parse(AndroidSchedule.loadSchedule()||'{}');root._weeks=root._weeks||{};
                        if(root._weeks.A){root._weeks.B=clone(root._weeks.A);root._weeks.C=clone(root._weeks.A);root._weeks.D=clone(root._weeks.A)}
                        root._currentWeek='A';root._cycleLength=2;AndroidSchedule.saveSchedule(JSON.stringify(root));markScheduleChanged();
                      }catch(e){}
                    }
                    try{if(window.AndroidSchedule&&AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(typeof currentWeek!=='undefined'?currentWeek:'A')}catch(e){}
                    try{if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures()}catch(e){}
                    try{if(window.refreshBulkCourseUi)window.refreshBulkCourseUi()}catch(e){}
                    stats.weekModeChanges++;mark(['today','week','edit'],true,modeNow()==='edit');markSyncContext();
                  },{});
                  return true;
                }

                function runClick(el,event,fn){
                  if(!el||typeof fn!=='function')return false;
                  const id=el.id||'';
                  if(el.classList&&el.classList.contains('weekTab')&&!el.classList.contains('weekendAdd')&&el.dataset.week)return selectWeek(el.dataset.week);
                  if(id==='currentWeekBtn')return advanceCurrentWeek();
                  if(el.classList&&el.classList.contains('weekModeChoice'))return changeWeekMode(Number(el.dataset.m));

                  if(id==='deleteCourse'){
                    stats.deletes++;run('delete-course',()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,fastEdit:true});return true;
                  }
                  if(id==='advCopyWeek'){
                    stats.copies++;run('copy-week',()=>fn.call(el,event),{targets:['week','edit'],renderCurrent:false});return true;
                  }
                  if(id==='advCopyDay'){
                    stats.copies++;run('copy-day',()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,fastEdit:true});return true;
                  }
                  if(id==='resetText87'||id==='resetColors87'||id==='resetWidget87'){
                    run('settings-reset',()=>fn.call(el,event),{});return true;
                  }
                  if(id==='advNewProfile'||id==='advDeleteProfile'){
                    stats.profileChanges++;run('profile-'+id,()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,scheduleChanged:true});return true;
                  }
                  if(id==='undoEdit86'||id==='redoEdit86'){
                    stats.undoRedo++;run(id,()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,scheduleChanged:true});return true;
                  }
                  if(id==='ocrPreviewCorrect86'||id==='ocrPreviewImport86'){
                    stats.ocrImports++;run(id,()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,fastEdit:id==='ocrPreviewCorrect86'});return true;
                  }
                  if(el.classList&&el.classList.contains('weekendAdd')||id==='removeWeekendDay'){
                    run('weekend-day',()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,scheduleChanged:true});return true;
                  }
                  if(id==='advAddRange'){
                    run('add-day-off-range',()=>fn.call(el,event),{targets:['today','week'],renderCurrent:true});return true;
                  }
                  if(el.closest&&el.closest('#advRangeList')){
                    run('delete-day-off-range',()=>fn.call(el,event),{targets:['today','week'],renderCurrent:true});return true;
                  }
                  if(el.closest&&el.closest('#advExceptionList')){
                    run('delete-exception',()=>fn.call(el,event),{targets:['today','week'],renderCurrent:true});return true;
                  }
                  return false;
                }

                function runSubmit(form,event,fn){
                  if(!form||typeof fn!=='function')return false;
                  if(form.id==='courseForm')return false; // 6.50 owns course-submit coalescing.
                  if(form.id==='advExceptionForm'){
                    run('save-exception',()=>fn.call(form,event),{targets:['today','week'],renderCurrent:true});return true;
                  }
                  if(form.id==='bulkSheetFixed'){
                    stats.bulkSaves++;run('bulk-courses',()=>fn.call(form,event),{targets:['today','week','edit'],renderCurrent:true,scheduleChanged:true});return true;
                  }
                  return false;
                }

                const noRenderAdvanced=new Set(['advDensity','advFormat','advFollowing','advAccess','advReminderMinutes','advShowRoom','advShowTimes','advShowRemaining','advShowPercent','advShowProgress','advShowBreaks','advShowLunch','advShowWeekInfo','advClassColors','advReminders']);
                document.addEventListener('change',function(event){
                  const el=event.target;if(!el||!el.id)return;
                  const id=el.id,fn=el.onchange;
                  if(id==='gapLabel'||id==='lunchLabel'){
                    event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
                    stats.breakChanges++;
                    run('break-label',function(){if(typeof window.applyBreakSettings==='function')window.applyBreakSettings();markScheduleChanged();mark(['today','week'],false,false)},{});
                    return;
                  }
                  if(id==='schoolEnabled'||id==='schoolYear'||id==='schoolZone'){
                    if(typeof fn!=='function')return;
                    event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
                    run('school-'+id,()=>fn.call(el,event),{});return;
                  }
                  if(id==='advProfileSelect'){
                    if(typeof fn!=='function')return;
                    event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
                    stats.profileChanges++;run('profile-select',()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,scheduleChanged:true});return;
                  }
                  if(id==='advCycle'){
                    if(typeof fn!=='function')return;
                    event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
                    stats.advancedChanges++;run('advanced-cycle',()=>fn.call(el,event),{targets:['today','week','edit'],renderCurrent:true,fastEdit:true});return;
                  }
                  if(id==='advHoliday'){
                    if(typeof fn!=='function')return;
                    event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
                    stats.advancedChanges++;run('advanced-holiday',()=>fn.call(el,event),{targets:['today','week'],renderCurrent:true});return;
                  }
                  if(noRenderAdvanced.has(id)){
                    if(typeof fn!=='function')return;
                    event.preventDefault();event.stopPropagation();event.stopImmediatePropagation();
                    stats.advancedChanges++;run('advanced-'+id,()=>fn.call(el,event),{});return;
                  }
                },true);

                window.__edtActionChains651={
                  stats:stats,run:run,runClick:runClick,runSubmit:runSubmit,
                  queueLocalRefresh:queueLocalRefresh,queueAdvancedRefresh:queueAdvancedRefresh,
                  schoolCalendarChanged:schoolCalendarChanged,setMode:setMode651,
                  mark:mark,markScheduleChanged:markScheduleChanged
                };
                console.log('EDT_ACTION_651|ready|single-commit');
              }catch(error){console.error('ActionChainUi651',error)}
            })();
            """;
    }
}
