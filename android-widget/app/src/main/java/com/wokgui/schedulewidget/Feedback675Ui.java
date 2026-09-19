package com.wokgui.schedulewidget;

/** 6.75 pass: keep Week pinned to the viewport and compress rows only when necessary. */
final class Feedback675Ui {
    private Feedback675Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback675){window.refreshFeedback675&&window.refreshFeedback675();return}
                window.__feedback675=true;
                const root=document.documentElement,body=document.body,stage=document.querySelector('main.wrap');
                const week=document.getElementById('viewWeek'),grid=document.getElementById('weekGrid');
                const scroller=week&&week.querySelector('.weekScroller'),bottom=document.querySelector('.bottom');
                if(!body||!stage||!week||!grid||!scroller||!bottom)return;
                if(!document.getElementById('feedback675Style')){
                  const style=document.createElement('style');style.id='feedback675Style';style.textContent=`
                    html.edtWeekLocked675,html.edtWeekLocked675 body{overflow-y:hidden!important;overscroll-behavior-y:none!important}
                    html body main.wrap.edtInstantViews647.edtWeekScreen675{min-height:0!important;padding-bottom:0!important;box-sizing:border-box!important}
                    html body.edtWeekLocked675 #viewWeek .weekScroller{overflow:hidden!important;overscroll-behavior:none!important}
                    html body.edtWeekLocked675 #viewWeek #weekGrid>.wh,
                    html body.edtWeekLocked675 #viewWeek #weekGrid>.wc{height:var(--week675-row-height)!important;min-height:var(--week675-row-height)!important;max-height:var(--week675-row-height)!important;box-sizing:border-box!important}
                    html body.edtWeekCompact675 #viewWeek #weekGrid .wc .cellLabel{font-size:.53rem!important;line-height:1!important;-webkit-line-clamp:1!important}
                    html body.edtWeekCompact675 #viewWeek #weekGrid .wc .cellRoom{font-size:.46rem!important;line-height:1!important}
                    html body.edtWeekCompact675 #viewWeek #weekGrid .wh{font-size:.55rem!important;line-height:1!important;padding:1px!important}
                    html body.edtWeekCompact675 #viewWeek #weekGrid .week658LunchLabel,
                    html body.edtWeekCompact675 #viewWeek #weekGrid .week662GapLabel{font-size:10px!important;line-height:1!important}
                  `;document.head.appendChild(style);
                }
                let raf=0,lastMode='';
                const active=()=>week.classList.contains('active');
                function clearSizing(){
                  root.classList.remove('edtWeekLocked675');body.classList.remove('edtWeekLocked675','edtWeekCompact675');
                  stage.classList.remove('edtWeekScreen675');grid.style.removeProperty('--week675-row-height');
                  scroller.style.removeProperty('height');week.style.removeProperty('height');
                }
                function fit(){
                  raf=0;if(window.__feedback676)return;
                  if(!active()){
                    if(lastMode==='week'){
                      clearSizing();lastMode='';scrollTo(0,0);
                      requestAnimationFrame(()=>{const instant=window.__edtInstantViews647;if(instant&&typeof instant.fit==='function')instant.fit()});
                    }
                    return;
                  }
                  lastMode='week';root.classList.add('edtWeekLocked675');body.classList.add('edtWeekLocked675');stage.classList.add('edtWeekScreen675');
                  if(scrollY!==0)scrollTo(0,0);
                  grid.style.removeProperty('--week675-row-height');body.classList.remove('edtWeekCompact675');
                  scroller.style.removeProperty('height');week.style.removeProperty('height');
                  const navTop=Math.floor(bottom.getBoundingClientRect().top),scrollTop=Math.ceil(scroller.getBoundingClientRect().top);
                  const available=Math.max(120,navTop-scrollTop-2);
                  const columns=Math.max(1,grid.querySelectorAll(':scope > .wh.day').length+1);
                  const rows=Math.max(1,Math.ceil(grid.children.length/columns));
                  const natural=Math.ceil(grid.getBoundingClientRect().height);
                  let rowHeight=natural>available?Math.max(18,Math.floor((available-2)/rows)):0;
                  if(rowHeight){grid.style.setProperty('--week675-row-height',rowHeight+'px');body.classList.toggle('edtWeekCompact675',rowHeight<38)}
                  let gridHeight=Math.ceil(grid.getBoundingClientRect().height);
                  if(gridHeight>available&&rowHeight>18){
                    rowHeight=Math.max(18,rowHeight-Math.ceil((gridHeight-available)/rows));
                    grid.style.setProperty('--week675-row-height',rowHeight+'px');body.classList.toggle('edtWeekCompact675',rowHeight<38);
                    gridHeight=Math.ceil(grid.getBoundingClientRect().height);
                  }
                  const shown=Math.min(available,gridHeight);scroller.style.height=shown+'px';
                  const viewTop=week.getBoundingClientRect().top,scrollerOffset=Math.max(0,scrollTop-viewTop);
                  week.style.height=Math.ceil(scrollerOffset+shown)+'px';
                  const css=getComputedStyle(stage),top=parseFloat(css.paddingTop)||0;
                  stage.style.height=Math.ceil(top+week.offsetHeight)+'px';
                  requestAnimationFrame(()=>{if(scrollY!==0)scrollTo(0,0)});
                }
                const queue=()=>{if(raf)return;raf=requestAnimationFrame(()=>requestAnimationFrame(fit))};
                new MutationObserver(queue).observe(week,{attributes:true,attributeFilter:['class']});
                new MutationObserver(queue).observe(grid,{childList:true,subtree:false});
                addEventListener('resize',queue,{passive:true});
                document.addEventListener('click',event=>{const target=event.target&&event.target.closest?event.target.closest('.nav[data-mode],#weekTabs .weekTab'):null;if(target)queue()},true);
                window.fitActiveWeek675=fit;window.refreshFeedback675=queue;queue();
              }catch(e){console.error('Feedback675Ui',e)}
            })();
            """;
    }
}
