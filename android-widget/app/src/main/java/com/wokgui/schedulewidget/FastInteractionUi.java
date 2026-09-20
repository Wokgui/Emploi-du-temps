package com.wokgui.schedulewidget;

/** Keeps every clickable control responsive with one stable delegated router. */
final class FastInteractionUi {
    private FastInteractionUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtFastInteractionV3)return;

                const state={tokens:Object.create(null),scheduled:0,executed:0,cancelled:0,clicks:0,submits:0,scrollClicksBlocked:0};
                const selector='button,.todayCourse,.editCourse,.wc';
                let pointerHandled=null,pointerHandledAt=0,courseGesture=null;
                const courseSelector='.todayCourse,.editCourse,.wc';
                function isCourseControl(el){return !!(el&&el.matches&&el.matches(courseSelector))}
                function beginCourseGesture(event,el){
                  if(!isCourseControl(el)||event.pointerType==='mouse')return;
                  courseGesture={el:el,id:event.pointerId,x:event.clientX,y:event.clientY,started:performance.now(),duration:0,moved:false};
                }
                function moveCourseGesture(event){
                  const g=courseGesture;if(!g||g.id!==event.pointerId)return;
                  if(Math.abs(event.clientX-g.x)>8||Math.abs(event.clientY-g.y)>8)g.moved=true
                }
                function endCourseGesture(event){
                  const g=courseGesture;if(!g||g.id!==event.pointerId)return;
                  g.duration=performance.now()-g.started;
                  setTimeout(()=>{if(courseGesture===g)courseGesture=null},800);
                }
                function blockCourseClick(event,el){
                  if(!isCourseControl(el))return false;
                  const g=courseGesture;if(!g||g.el!==el)return false;
                  const blocked=g.moved||g.duration>320||performance.now()-g.started>320;
                  courseGesture=null;if(!blocked)return false;
                  state.scrollClicksBlocked++;
                  try{event.preventDefault();event.stopPropagation();event.stopImmediatePropagation()}catch(ignore){}
                  return true;
                }

                function afterPaint(key,fn){
                  const token=key?((state.tokens[key]||0)+1):0;
                  if(key)state.tokens[key]=token;
                  state.scheduled++;
                  requestAnimationFrame(function(){
                    if(key&&state.tokens[key]!==token){state.cancelled++;return}
                    try{state.executed++;fn()}catch(e){console.log('FastInteractionUi deferred',e)}
                  });
                }

                function labelFor(el){
                  if(!el)return 'unknown';
                  if(el.id==='settingsBtn')return 'settings';
                  if(el.id==='currentWeekBtn')return 'current-week';
                  if(el.id==='addCourse')return 'add-course';
                  if(el.id==='cancelEdit')return 'cancel-edit';
                  if(el.id==='deleteCourse')return 'delete-course';
                  if(el.classList&&el.classList.contains('nav'))return 'nav-'+(el.dataset.mode||'unknown');
                  if(el.classList&&el.classList.contains('weekTab'))return 'week-'+(el.dataset.week||'unknown');
                  if(el.classList&&el.classList.contains('dayTab'))return 'day-'+(el.dataset.day||el.textContent||'unknown');
                  if(el.classList&&el.classList.contains('editCourse'))return 'edit-course';
                  if(el.classList&&el.classList.contains('todayCourse'))return 'today-course';
                  if(el.classList&&el.classList.contains('wc'))return 'week-cell';
                  return el.id||'control';
                }

                function groupFor(el){
                  if(!el||!el.classList)return '';
                  if(el.classList.contains('weekTab')||el.id==='currentWeekBtn')return 'week-selection';
                  if(el.classList.contains('dayTab'))return 'day-selection';
                  return '';
                }

                function visualFor(el){
                  if(!el)return;
                  if(el.id==='settingsBtn'){
                    // Prepare the complete settings DOM while it is still hidden. Showing the
                    // modal here used to expose one unfinished frame before the real click.
                    try{if(window.prepareSettingsOpen665)window.prepareSettingsOpen665()}catch(e){}
                    return;
                  }
                  if(el.classList&&el.classList.contains('weekTab')&&el.dataset.week){
                    // Keep the old week fully coherent until ActionChainUi651 replaces
                    // the active tab, title and grid together in its single commit.
                    return;
                  }
                  if(el.classList&&el.classList.contains('dayTab')&&!el.classList.contains('weekendAdd')){
                    document.querySelectorAll('.dayTab:not(.weekendAdd)').forEach(function(x){x.classList.toggle('active',x===el)});
                  }
                }

                function controlFrom(target){
                  try{return target&&target.closest?target.closest(selector):null}catch(e){return null}
                }
                function logSettle(kind,label,n,started){
                  const elapsed=Math.round(performance.now()-started);
                  if(n<=6||n%20===0)console.log('EDT_FAST_SETTLE|kind='+kind+'|label='+label+'|n='+n+'|ms='+elapsed);
                  if(kind==='click'&&n%100===0){
                    console.log('EDT_FAST_STATS|clicks='+state.clicks+'|submits='+state.submits+'|scheduled='+state.scheduled+'|executed='+state.executed+'|cancelled='+state.cancelled+'|routers=1|wrappers=0');
                  }
                }
                function invokeClick(el,event,fn){
                  const chain=window.__edtActionChains651;
                  if(chain&&typeof chain.runClick==='function'&&chain.runClick(el,event,fn))return;
                  fn.call(el,event);
                }
                function invokeSubmit(form,event,fn){
                  const pipeline=window.__edtRenderPipeline650;
                  if(form&&form.id==='courseForm'&&pipeline&&typeof pipeline.runCourseSubmit==='function')return pipeline.runCourseSubmit(fn,form,event);
                  const chain=window.__edtActionChains651;
                  if(chain&&typeof chain.runSubmit==='function'&&chain.runSubmit(form,event,fn))return;
                  return fn.call(form,event);
                }

                if(!document.getElementById('edtFastInteractionStyle')){
                  const style=document.createElement('style');style.id='edtFastInteractionStyle';
                  style.textContent=`
                    button{touch-action:manipulation;-webkit-tap-highlight-color:transparent}
                    .todayCourse,.editCourse,.wc{touch-action:pan-y pinch-zoom;-webkit-tap-highlight-color:transparent}
                    button:active,.todayCourse:active,.editCourse:active,.wc:active{filter:brightness(.96)!important}
                    button:active{opacity:.84!important}
                  `;
                  document.head.appendChild(style);
                }

                const warmSettings=()=>{try{if(window.prepareSettingsOpen665)window.prepareSettingsOpen665()}catch(e){}};
                if('requestIdleCallback' in window)requestIdleCallback(warmSettings,{timeout:700});
                else setTimeout(warmSettings,220);

                document.addEventListener('pointerdown',function(e){
                  const el=controlFrom(e.target);if(!el)return;
                  if(el.classList&&el.classList.contains('nav'))return;
                  beginCourseGesture(e,el);
                  visualFor(el);

                  // Settings is special: showing its full-screen modal on pointer-down can
                  // move the pointer-up target away from the gear button, so a browser click
                  // is not guaranteed. Execute the existing action here exactly once while
                  // preserving the immediate visual response.
                  if(el.id==='settingsBtn'&&typeof el.onclick==='function'){
                    const fn=el.onclick,n=++state.clicks,started=performance.now();
                    pointerHandled=el;pointerHandledAt=started;
                    console.log('EDT_FAST_INPUT|settings|visual|direct');
                    invokeClick(el,e,fn);logSettle('click','settings',n,started);
                  }
                },{capture:true,passive:true});
                document.addEventListener('pointermove',moveCourseGesture,{capture:true,passive:true});
                document.addEventListener('pointerup',endCourseGesture,{capture:true,passive:true});
                document.addEventListener('pointercancel',()=>{courseGesture=null},{capture:true,passive:true});
                document.addEventListener('click',function(e){
                  const el=controlFrom(e.target);if(!el)return;
                  if(el.classList&&el.classList.contains('nav'))return;
                  if(blockCourseClick(e,el))return;
                  // Stability73 is the single physical owner of the week-cycle strip. Its
                  // pointer-up route is repaired to call the 6.51 coordinator directly. Do not
                  // add a second click route here or WebView can execute the same mode twice.
                  if(el.classList&&el.classList.contains('weekModeChoice'))return;
                  if(el===pointerHandled&&performance.now()-pointerHandledAt<900){
                    pointerHandled=null;
                    try{e.preventDefault();e.stopPropagation();e.stopImmediatePropagation()}catch(ignore){}
                    return;
                  }
                  const fn=el.onclick;
                  if(typeof fn!=='function')return;
                  try{e.preventDefault();e.stopPropagation();e.stopImmediatePropagation()}catch(ignore){}
                  const n=++state.clicks,started=performance.now(),label=labelFor(el);
                  console.log('EDT_FAST_INPUT|'+label+'|visual|delegated');
                  const group=groupFor(el);
                  afterPaint(group,function(){invokeClick(el,e,fn);logSettle('click',label,n,started)});
                },true);

                document.addEventListener('submit',function(e){
                  const form=e.target;
                  if(!form||form.tagName!=='FORM'||typeof form.onsubmit!=='function')return;
                  const fn=form.onsubmit;
                  try{e.preventDefault();e.stopPropagation();e.stopImmediatePropagation()}catch(ignore){}
                  const n=++state.submits,started=performance.now(),label=(form.id||'form')+'-submit';
                  console.log('EDT_FAST_INPUT|'+label+'|visual|delegated');
                  afterPaint('',function(){invokeSubmit(form,e,fn);logSettle('submit',label,n,started)});
                },true);

                window.__edtFastInteractionV3={state:state};
                window.refreshFastInteractionUi=function(){};
                console.log('EDT_FAST_MODE|single-delegated-router');
              }catch(e){console.log('FastInteractionUi',e)}
            })();
            """;
    }
}
