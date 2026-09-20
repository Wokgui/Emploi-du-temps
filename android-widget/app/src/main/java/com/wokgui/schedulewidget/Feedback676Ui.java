package com.wokgui.schedulewidget;

/** 6.76 pass: one stable full-height week geometry with final lunch repaint. */
final class Feedback676Ui {
    private Feedback676Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback676){window.refreshFeedback676&&window.refreshFeedback676();return}
                window.__feedback676=true;
                const root=document.documentElement,body=document.body,stage=document.querySelector('main.wrap');
                const week=document.getElementById('viewWeek'),grid=document.getElementById('weekGrid');
                const scroller=week&&week.querySelector('.weekScroller'),bottom=document.querySelector('.bottom');
                if(!body||!stage||!week||!grid||!scroller||!bottom)return;
                root.classList.remove('edtWeekLocked675');body.classList.remove('edtWeekLocked675','edtWeekCompact675');
                stage.classList.remove('edtWeekScreen675');grid.style.removeProperty('--week675-row-height');
                if(!document.getElementById('feedback676Style')){
                  const style=document.createElement('style');style.id='feedback676Style';style.textContent=`
                    html.edtWeekFill676,html.edtWeekFill676 body{overflow-y:hidden!important;overscroll-behavior-y:none!important}
                    html body main.wrap.edtInstantViews647.edtWeekScreen676{min-height:0!important;padding-bottom:0!important;box-sizing:border-box!important}
                    html body #viewWeek .weekScroller.edtWeekScroller676{overflow:hidden!important;overscroll-behavior:none!important}
                    html body #viewWeek #weekGrid.edtWeekGrid676{height:var(--week676-grid-height)!important;grid-auto-rows:minmax(0,1fr)!important}
                    html body #viewWeek #weekGrid.edtWeekGrid676>.wh,
                    html body #viewWeek #weekGrid.edtWeekGrid676>.wc{height:auto!important;min-height:0!important;max-height:none!important;box-sizing:border-box!important;overflow:hidden!important}
                    html body.edtWeekCompact676 #viewWeek #weekGrid .wc .cellLabel{font-size:.53rem!important;line-height:1!important;-webkit-line-clamp:1!important}
                    html body.edtWeekCompact676 #viewWeek #weekGrid .wc .cellRoom{font-size:.46rem!important;line-height:1!important}
                    html body.edtWeekCompact676 #viewWeek #weekGrid .wh{font-size:.55rem!important;line-height:1!important;padding:1px!important}
                    html body.edtWeekCompact676 #viewWeek #weekGrid .week658LunchLabel,
                    html body.edtWeekCompact676 #viewWeek #weekGrid .week662GapLabel{font-size:10px!important;line-height:1!important}
                  `;document.head.appendChild(style);
                }
                grid.classList.add('edtWeekGrid676');scroller.classList.add('edtWeekScroller676');
                let raf=0,painting=false;
                const active=()=>week.classList.contains('active');
                function repaint(){
                  if(painting)return;painting=true;
                  try{
                    if(typeof window.refreshWeekAppearance658==='function')window.refreshWeekAppearance658();
                    if(typeof window.refreshLunchBreakUi==='function')window.refreshLunchBreakUi();
                    if(typeof window.refreshDoubleLunchUi==='function')window.refreshDoubleLunchUi();
                    if(typeof window.paintWeek69==='function')window.paintWeek69();
                  }finally{painting=false}
                }
                function unlock(){
                  root.classList.remove('edtWeekFill676');body.classList.remove('edtWeekFill676','edtWeekCompact676');
                  stage.classList.remove('edtWeekScreen676');
                }
                function fit(withPaint){
                  raf=0;
                  const isActive=active();if(isActive&&scrollY!==0)scrollTo(0,0);
                  const navTop=Math.floor(bottom.getBoundingClientRect().top),scrollTop=Math.ceil(scroller.getBoundingClientRect().top);
                  const available=Math.max(120,navTop-scrollTop-2);
                  const columns=Math.max(1,grid.querySelectorAll(':scope > .wh.day').length+1);
                  const rows=Math.max(1,Math.ceil(grid.children.length/columns));
                  grid.style.setProperty('--week676-grid-height',available+'px');scroller.style.height=available+'px';
                  body.classList.toggle('edtWeekCompact676',isActive&&available/rows<38);
                  const viewTop=week.getBoundingClientRect().top,offset=Math.max(0,scrollTop-viewTop);
                  week.style.height=Math.ceil(offset+available)+'px';
                  if(isActive){
                    root.classList.add('edtWeekFill676');body.classList.add('edtWeekFill676');stage.classList.add('edtWeekScreen676');
                    const css=getComputedStyle(stage),top=parseFloat(css.paddingTop)||0;stage.style.height=Math.ceil(top+week.offsetHeight)+'px';
                  }else unlock();
                  if(withPaint)repaint();
                  if(isActive)requestAnimationFrame(()=>{if(scrollY!==0)scrollTo(0,0)});
                }
                const queue=(paint)=>{if(raf)return;raf=requestAnimationFrame(()=>requestAnimationFrame(()=>fit(!!paint)))};
                const oldRender=window.renderWeek;
                if(typeof oldRender==='function'&&!oldRender.__feedback676){
                  const wrapped=function(){const result=oldRender.apply(this,arguments);fit(true);return result};
                  wrapped.__feedback676=true;wrapped.__feedback676Original=oldRender;window.renderWeek=wrapped;try{renderWeek=wrapped}catch(e){}
                }
                new MutationObserver(()=>queue(true)).observe(grid,{childList:true,subtree:false});
                new MutationObserver(()=>queue(true)).observe(week,{attributes:true,attributeFilter:['class']});
                addEventListener('resize',()=>queue(true),{passive:true});
                document.addEventListener('click',event=>{const target=event.target&&event.target.closest?event.target.closest('.nav[data-mode],#weekTabs .weekTab'):null;if(target)queue(true)},true);
                window.fitActiveWeek676=()=>fit(true);window.refreshFeedback676=()=>queue(true);fit(true);
              }catch(e){console.error('Feedback676Ui',e)}
            })();
            """;
    }
}
