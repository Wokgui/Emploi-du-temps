package com.wokgui.schedulewidget;

/** Keeps Today, Week and Edit laid out off-screen so a tab switch does not trigger a full relayout. */
final class InstantViewUi647 {
    private InstantViewUi647() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtInstantViews647)return;
                const stage=document.querySelector('main.wrap');
                if(!stage)return;
                const views={
                  today:document.getElementById('viewToday'),
                  week:document.getElementById('viewWeek'),
                  edit:document.getElementById('viewEdit')
                };
                if(!views.today||!views.week||!views.edit)return;

                const heights={today:0,week:0,edit:0};
                const nameFor=function(el){
                  return el===views.today?'today':(el===views.week?'week':(el===views.edit?'edit':''));
                };
                const activeName=function(){
                  if(views.today.classList.contains('active'))return 'today';
                  if(views.week.classList.contains('active'))return 'week';
                  return 'edit';
                };
                const padding=function(){
                  const cs=getComputedStyle(stage);
                  return {
                    top:parseFloat(cs.paddingTop)||0,
                    bottom:parseFloat(cs.paddingBottom)||0
                  };
                };
                const measure=function(name){
                  const el=views[name];
                  if(!el)return 0;
                  const h=el.offsetHeight||0;
                  if(h>0)heights[name]=h;
                  return h;
                };
                const fit=function(name){
                  name=name||activeName();
                  let h=heights[name]||measure(name);
                  if(!h)return;
                  const p=padding();
                  stage.style.height=Math.ceil(h+p.top+p.bottom)+'px';
                };

                const cs=getComputedStyle(stage);
                stage.style.setProperty('--edt647-left',cs.paddingLeft||'0px');
                stage.style.setProperty('--edt647-right',cs.paddingRight||'0px');
                stage.style.setProperty('--edt647-top',cs.paddingTop||'0px');
                measure(activeName());
                fit(activeName());

                if(!document.getElementById('edtInstantViews647Style')){
                  const style=document.createElement('style');
                  style.id='edtInstantViews647Style';
                  style.textContent=`
                    main.wrap.edtInstantViews647{position:relative;}
                    main.wrap.edtInstantViews647>.view{
                      display:block!important;
                      position:absolute!important;
                      left:var(--edt647-left,0px);
                      right:var(--edt647-right,0px);
                      top:var(--edt647-top,0px);
                      width:auto!important;
                      visibility:hidden;
                      pointer-events:none;
                      z-index:0;
                    }
                    main.wrap.edtInstantViews647>.view.active{
                      visibility:visible;
                      pointer-events:auto;
                      z-index:1;
                    }
                  `;
                  document.head.appendChild(style);
                }
                stage.classList.add('edtInstantViews647');

                const classChanged=function(){
                  const name=activeName();
                  fit(name);
                };
                Object.values(views).forEach(function(el){
                  new MutationObserver(classChanged).observe(el,{attributes:true,attributeFilter:['class']});
                });

                if(typeof ResizeObserver==='function'){
                  const ro=new ResizeObserver(function(entries){
                    entries.forEach(function(entry){
                      const name=nameFor(entry.target);
                      if(!name)return;
                      const h=entry.target.offsetHeight||0;
                      if(h>0)heights[name]=h;
                    });
                    fit(activeName());
                  });
                  Object.values(views).forEach(function(el){ro.observe(el)});
                  window.__edtInstantViews647ResizeObserver=ro;
                }

                requestAnimationFrame(function(){
                  measure('today');measure('week');measure('edit');fit(activeName());
                  console.log('EDT_INSTANT_STAGE|ready|today='+heights.today+'|week='+heights.week+'|edit='+heights.edit);
                });
                window.__edtInstantViews647={stage:stage,views:views,heights:heights,fit:fit};
              }catch(e){console.log('InstantViewUi647',e)}
            })();
            """;
    }
}
