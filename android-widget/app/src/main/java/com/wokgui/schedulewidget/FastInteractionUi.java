package com.wokgui.schedulewidget;

/** Keeps tap feedback paintable before expensive legacy render wrappers run. */
final class FastInteractionUi {
    private FastInteractionUi() {}

    static String script() {
        return """
            (function(){
              try{
                function afterPaint(fn){
                  requestAnimationFrame(function(){setTimeout(function(){try{fn()}catch(e){console.log('FastInteractionUi deferred',e)}},0)});
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
                  if(el.classList.contains('nav'))return 'nav-'+(el.dataset.mode||'unknown');
                  if(el.classList.contains('weekTab'))return 'week-'+(el.dataset.week||'unknown');
                  if(el.classList.contains('dayTab'))return 'day-'+(el.dataset.day||el.textContent||'unknown');
                  return el.id||'control';
                }
                function flash(el){
                  if(!el)return;
                  el.classList.add('edtFastPressed');
                  setTimeout(function(){try{el.classList.remove('edtFastPressed')}catch(e){}},140);
                }
                function heavyClick(el,visual){
                  if(!el||typeof el.onclick!=='function'||el.onclick.__edtFastProxy)return false;
                  var old=el.onclick;
                  var proxy=function(e){
                    flash(el);
                    console.log('EDT_FAST_INPUT|'+labelFor(el)+'|visual');
                    if(proxy.__running)return false;
                    proxy.__running=true;
                    try{if(visual)visual(e)}catch(ignore){}
                    afterPaint(function(){
                      try{old.call(el,e)}finally{proxy.__running=false}
                    });
                    return false;
                  };
                  proxy.__edtFastProxy=true;
                  el.onclick=proxy;
                  return true;
                }
                function patch(){
                  try{
                    var settings=document.getElementById('settingsBtn');
                    heavyClick(settings,function(){var m=document.getElementById('settingsModal');if(m)m.classList.add('show')});

                    document.querySelectorAll('.nav[data-mode]').forEach(function(b){
                      heavyClick(b,function(){activeView(b.dataset.mode)});
                    });
                    document.querySelectorAll('.weekTab[data-week]').forEach(function(b){
                      heavyClick(b,function(){
                        document.querySelectorAll('.weekTab').forEach(function(x){x.classList.toggle('active',x===b)});
                        var l=document.getElementById('weekTitleLetter');if(l)l.textContent=b.dataset.week;
                      });
                    });
                    document.querySelectorAll('.dayTab:not(.weekendAdd)').forEach(function(b){
                      heavyClick(b,function(){document.querySelectorAll('.dayTab:not(.weekendAdd)').forEach(function(x){x.classList.toggle('active',x===b)})});
                    });
                    heavyClick(document.getElementById('currentWeekBtn'));
                    heavyClick(document.getElementById('addCourse'));
                  }catch(e){console.log('FastInteractionUi patch',e)}
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

                if(!window.__edtFastInteractionObserver){
                  window.__edtFastInteractionObserver=true;
                  var queued=false;
                  new MutationObserver(function(){
                    if(queued)return;queued=true;
                    requestAnimationFrame(function(){queued=false;patch()});
                  }).observe(document.body,{childList:true,subtree:true});
                }
                window.refreshFastInteractionUi=patch;
                patch();
              }catch(e){console.log('FastInteractionUi',e)}
            })();
            """;
    }
}
