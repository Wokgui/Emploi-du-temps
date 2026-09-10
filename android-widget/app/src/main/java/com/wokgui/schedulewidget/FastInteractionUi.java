package com.wokgui.schedulewidget;

/** Keeps every clickable control responsive without per-element wrappers. */
final class FastInteractionUi {
    private FastInteractionUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtFastInteractionV3)return;

                const state={tokens:Object.create(null),scheduled:0,executed:0,cancelled:0,clicks:0,submits:0};
                const selector='button,.todayCourse,.editCourse,.wc';

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

                function flash(el){
                  if(!el||!el.classList)return;
                  el.classList.add('edtFastPressed');
                  setTimeout(function(){try{el.classList.remove('edtFastPressed')}catch(e){}},90);
                }

                function visualFor(el){
                  if(!el)return;
                  if(el.id==='settingsBtn'){
                    const m=document.getElementById('settingsModal');if(m)m.classList.add('show');return;
                  }
                  if(el.classList&&el.classList.contains('weekTab')&&el.dataset.week){
                    document.querySelectorAll('.weekTab').forEach(function(x){x.classList.toggle('active',x===el)});
                    const l=document.getElementById('weekTitleLetter');if(l)l.textContent=el.dataset.week;return;
                  }
                  if(el.classList&&el.classList.contains('dayTab')&&!el.classList.contains('weekendAdd')){
                    document.querySelectorAll('.dayTab:not(.weekendAdd)').forEach(function(x){x.classList.toggle('active',x===el)});
                  }
                }

                function controlFrom(target){
                  try{return target&&target.closest?target.closest(selector):null}catch(e){return null}
                }

                if(!document.getElementById('edtFastInteractionStyle')){
                  const style=document.createElement('style');style.id='edtFastInteractionStyle';
                  style.textContent=`
                    button,.todayCourse,.editCourse,.wc{touch-action:manipulation;-webkit-tap-highlight-color:transparent}
                    .edtFastPressed{filter:brightness(.96)!important}
                    button.edtFastPressed{opacity:.84!important}
                  `;
                  document.head.appendChild(style);
                }

                document.addEventListener('pointerdown',function(e){
                  const el=controlFrom(e.target);if(!el)return;
                  flash(el);
                  if(!(el.classList&&el.classList.contains('nav')))visualFor(el);
                },{capture:true,passive:true});

                document.addEventListener('click',function(e){
                  const el=controlFrom(e.target);if(!el)return;
                  // Bottom navigation has its own zero-render controller.
                  if(el.classList&&el.classList.contains('nav'))return;
                  const fn=el.onclick;
                  if(typeof fn!=='function')return;
                  try{e.preventDefault();e.stopPropagation();e.stopImmediatePropagation()}catch(ignore){}
                  state.clicks++;
                  console.log('EDT_FAST_INPUT|'+labelFor(el)+'|delegated');
                  const group=groupFor(el);
                  afterPaint(group,function(){fn.call(el,e)});
                },true);

                document.addEventListener('submit',function(e){
                  const form=e.target;
                  if(!form||form.tagName!=='FORM'||typeof form.onsubmit!=='function')return;
                  const fn=form.onsubmit;
                  try{e.preventDefault();e.stopPropagation();e.stopImmediatePropagation()}catch(ignore){}
                  state.submits++;
                  console.log('EDT_FAST_INPUT|'+(form.id||'form')+'-submit|delegated');
                  afterPaint('',function(){fn.call(form,e)});
                },true);

                window.__edtFastInteractionV3={state:state};
                // Compatibility hook for old callers: there is deliberately nothing to patch.
                window.refreshFastInteractionUi=function(){};
                console.log('EDT_FAST_MODE|single-delegated-router');
              }catch(e){console.log('FastInteractionUi',e)}
            })();
            """;
    }
}
