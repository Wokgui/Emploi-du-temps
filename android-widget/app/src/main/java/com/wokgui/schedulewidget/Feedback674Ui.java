package com.wokgui.schedulewidget;

/** 6.74 pass: preserve bottom navigation and fit the active week without blank scroll. */
final class Feedback674Ui {
    private Feedback674Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback674)return;
                window.__feedback674=true;
                const stage=document.querySelector('main.wrap'),week=document.getElementById('viewWeek'),grid=document.getElementById('weekGrid');
                if(!stage||!week||!grid)return;
                if(!document.getElementById('feedback674Style')){
                  const style=document.createElement('style');style.id='feedback674Style';style.textContent=`
                    html body .todayCourse,html body .editCourse,html body #weekGrid .wc{touch-action:pan-y pinch-zoom!important;-webkit-tap-highlight-color:transparent!important}
                    html body main.wrap.edtInstantViews647{box-sizing:border-box!important}
                    html body main.wrap.edtInstantViews647.edtWeekTight674{min-height:0!important}
                    html body .editSwapCover673{z-index:11!important}
                    html body .bottom{z-index:12!important;isolation:isolate!important}
                  `;document.head.appendChild(style);
                }
                let raf=0;
                const isWeek=()=>week.classList.contains('active');
                const fit=()=>{
                  raf=0;const active=isWeek();stage.classList.toggle('edtWeekTight674',active);if(!active)return;
                  const h=Math.max(week.offsetHeight||0,week.scrollHeight||0);if(!h)return;
                  const css=getComputedStyle(stage),top=parseFloat(css.paddingTop)||0,bottom=parseFloat(css.paddingBottom)||0;
                  stage.style.height=Math.ceil(h+top+bottom)+'px';
                  requestAnimationFrame(()=>{
                    const root=document.documentElement,max=Math.max(0,Math.max(root.scrollHeight,document.body.scrollHeight)-innerHeight);
                    if(scrollY>max)scrollTo(0,max);
                  });
                };
                const queue=()=>{if(raf)return;raf=requestAnimationFrame(()=>requestAnimationFrame(fit))};
                new MutationObserver(queue).observe(week,{attributes:true,attributeFilter:['class']});
                new MutationObserver(queue).observe(grid,{childList:true,subtree:true});
                if(typeof ResizeObserver==='function'){
                  const observer=new ResizeObserver(queue);observer.observe(week);observer.observe(grid);window.__feedback674ResizeObserver=observer;
                }
                document.addEventListener('click',event=>{const target=event.target&&event.target.closest?event.target.closest('.nav[data-mode],#weekTabs .weekTab'):null;if(target)queue()},true);
                window.fitActiveWeek674=fit;window.refreshFeedback674=queue;queue();
              }catch(e){console.error('Feedback674Ui',e)}
            })();
            """;
    }
}