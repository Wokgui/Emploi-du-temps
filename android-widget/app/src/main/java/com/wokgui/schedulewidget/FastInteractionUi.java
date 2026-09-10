package com.wokgui.schedulewidget;

/** Keeps controls responsive without accumulating work during long sessions. */
final class FastInteractionUi {
    private FastInteractionUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtFastInteractionV2){
                  try{window.__edtFastInteractionV2.patchTree(document.body)}catch(e){}
                  return;
                }

                const state={tokens:Object.create(null),scheduled:0,executed:0,cancelled:0,wrapped:0,formWrapped:0,patchTrees:0,pointerPatches:0};

                function afterPaint(key,fn){
                  const token=key?((state.tokens[key]||0)+1):0;
                  if(key)state.tokens[key]=token;
                  state.scheduled++;
                  requestAnimationFrame(function(){setTimeout(function(){
                    if(key&&state.tokens[key]!==token){state.cancelled++;maybeStats();return}
                    try{state.executed++;fn()}catch(e){console.log('FastInteractionUi deferred',e)}
                    maybeStats();
                  },0)});
                }

                function activeView(target){
                  try{
                    document.querySelectorAll('.nav').forEach(function(b){b.classList.toggle('active',b.dataset.mode===target)});
                    document.querySelectorAll('.view').forEach(function(v){v.classList.remove('active')});
                    var id='view'+target.charAt(0).toUpperCase()+target.slice(1),v=document.getElementById(id);
                    if(v)v.classList.add('active');
                  }catch(e){}
                }

                function labelFor(el){
                  if(!el)return 'unknown';
                  if(el.id==='settingsBtn')return 'settings';
                  if(el.id==='currentWeekBtn')return 'current-week';
                  if(el.id==='addCourse')return 'add-course';
                  if(el.classList&&el.classList.contains('nav'))return 'nav-'+(el.dataset.mode||'unknown');
                  if(el.classList&&el.classList.contains('weekTab'))return 'week-'+(el.dataset.week||'unknown');
                  if(el.classList&&el.classList.contains('dayTab'))return 'day-'+(el.dataset.day||el.textContent||'unknown');
                  return el.id||'control';
                }

                function groupFor(el){
                  if(!el||!el.classList)return '';
                  if(el.classList.contains('nav'))return 'navigation';
                  if(el.classList.contains('weekTab')||el.id==='currentWeekBtn')return 'week-selection';
                  if(el.classList.contains('dayTab'))return 'day-selection';
                  return '';
                }

                function flash(el){
                  if(!el||!el.classList)return;
                  el.classList.add('edtFastPressed');
                  setTimeout(function(){try{el.classList.remove('edtFastPressed')}catch(e){}},120);
                }

                function visualFor(el){
                  if(!el)return null;
                  if(el.id==='settingsBtn')return function(){var m=document.getElementById('settingsModal');if(m)m.classList.add('show')};
                  if(el.classList&&el.classList.contains('nav')&&el.dataset.mode)return function(){activeView(el.dataset.mode)};
                  if(el.classList&&el.classList.contains('weekTab')&&el.dataset.week)return function(){
                    document.querySelectorAll('.weekTab').forEach(function(x){x.classList.toggle('active',x===el)});
                    var l=document.getElementById('weekTitleLetter');if(l)l.textContent=el.dataset.week;
                  };
                  if(el.classList&&el.classList.contains('dayTab')&&!el.classList.contains('weekendAdd'))return function(){
                    document.querySelectorAll('.dayTab:not(.weekendAdd)').forEach(function(x){x.classList.toggle('active',x===el)});
                  };
                  return null;
                }

                function heavyClick(el){
                  if(!el||typeof el.onclick!=='function'||el.onclick.__edtFastProxy)return false;
                  const old=el.onclick,group=groupFor(el),visual=visualFor(el);
                  const proxy=function(e){
                    flash(el);
                    try{if(visual)visual(e)}catch(ignore){}
                    console.log('EDT_FAST_INPUT|'+labelFor(el)+'|visual');

                    if(!group&&proxy.__running)return false;
                    if(!group)proxy.__running=true;
                    afterPaint(group,function(){
                      try{old.call(el,e)}finally{if(!group)proxy.__running=false}
                    });
                    return false;
                  };
                  proxy.__edtFastProxy=true;
                  proxy.__edtFastOriginal=old;
                  el.onclick=proxy;
                  state.wrapped++;
                  return true;
                }

                function heavySubmit(form){
                  if(!form||typeof form.onsubmit!=='function'||form.onsubmit.__edtFastProxy)return false;
                  const old=form.onsubmit;
                  const proxy=function(e){
                    try{if(e&&e.preventDefault)e.preventDefault()}catch(ignore){}
                    var submit=form.querySelector('button[type="submit"],input[type="submit"]');
                    flash(submit||form);
                    console.log('EDT_FAST_INPUT|'+(form.id||'form')+'-submit|visual');
                    if(proxy.__running)return false;
                    proxy.__running=true;
                    afterPaint('',function(){
                      try{old.call(form,e)}finally{proxy.__running=false}
                    });
                    return false;
                  };
                  proxy.__edtFastProxy=true;
                  proxy.__edtFastOriginal=old;
                  form.onsubmit=proxy;
                  state.wrapped++;
                  state.formWrapped++;
                  console.log('EDT_FAST_FORM_WRAP|form='+(form.id||'form')+'|count='+state.formWrapped);
                  return true;
                }

                function patchOne(el){
                  if(!el||el.nodeType!==1)return;
                  if(el.matches&&el.matches('button,.todayCourse,.editCourse,.wc'))heavyClick(el);
                  if(el.matches&&el.matches('form'))heavySubmit(el);
                }

                function patchTree(root){
                  if(!root||root.nodeType!==1)return;
                  state.patchTrees++;
                  patchOne(root);
                  if(root.querySelectorAll){
                    root.querySelectorAll('button,.todayCourse,.editCourse,.wc').forEach(heavyClick);
                    root.querySelectorAll('form').forEach(heavySubmit);
                  }
                }

                function patchTarget(target){
                  state.pointerPatches++;
                  if(!target||!target.closest)return;
                  var el=target.closest('button,.todayCourse,.editCourse,.wc');
                  if(el)heavyClick(el);
                  var form=target.closest('form');
                  if(form)heavySubmit(form);
                }

                function maybeStats(){
                  if(state.scheduled>0&&state.scheduled%12===0){
                    console.log('EDT_FAST_STATS|scheduled='+state.scheduled+'|executed='+state.executed+'|cancelled='+state.cancelled+'|wrapped='+state.wrapped+'|formWrapped='+state.formWrapped+'|patchTrees='+state.patchTrees+'|pointerPatches='+state.pointerPatches);
                  }
                }

                if(!document.getElementById('edtFastInteractionStyle')){
                  var style=document.createElement('style');style.id='edtFastInteractionStyle';
                  style.textContent=`
                    button,.todayCourse,.editCourse,.wc{touch-action:manipulation;-webkit-tap-highlight-color:transparent}
                    .edtFastPressed{filter:brightness(.96)!important}
                    button.edtFastPressed{opacity:.82!important}
                  `;
                  document.head.appendChild(style);
                }

                // No MutationObserver: new/dynamic controls are patched lazily on pointer-down.
                document.addEventListener('pointerdown',function(e){patchTarget(e.target)},{capture:true,passive:true});

                window.__edtFastInteractionV2={state:state,patchTree:patchTree,patchTarget:patchTarget};
                window.refreshFastInteractionUi=function(){patchTree(document.body)};
                patchTree(document.body);
                console.log('EDT_FAST_MODE|delegated-no-observer');
              }catch(e){console.log('FastInteractionUi',e)}
            })();
            """;
    }
}
