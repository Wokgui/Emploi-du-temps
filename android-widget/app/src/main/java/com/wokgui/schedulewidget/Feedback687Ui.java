package com.wokgui.schedulewidget;

/** Final 6.87 feedback pass: fixed-height Today view and compact settings alignment. */
final class Feedback687Ui {
    private Feedback687Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback687){window.refreshFeedback687&&window.refreshFeedback687();return}
                window.__feedback687=true;

                const style=document.createElement('style');
                style.id='feedback687Style';
                style.textContent=`
                  body.todayNoScroll687{overflow-y:hidden!important;overscroll-behavior-y:none!important}
                  body.todayNoScroll687 .wrap{padding-bottom:0!important;overflow:hidden!important}
                  body.todayNoScroll687 #viewToday{overflow:hidden!important;margin:0!important}
                  body.todayNoScroll687 #todayList{overflow:hidden!important}
                  body.todayNoScroll687 #todayList .todayCourse{
                    min-height:0!important;height:var(--today-row-h,42px)!important;
                    padding-top:3px!important;padding-bottom:3px!important
                  }
                  body.todayNoScroll687 #viewToday.todayTight687 .todayCourse{gap:5px!important}
                  body.todayNoScroll687 #viewToday.todayTight687 .label{font-size:.80rem!important;line-height:1.05!important}
                  body.todayNoScroll687 #viewToday.todayTight687 .time{font-size:.68rem!important;line-height:1.04!important}
                  body.todayNoScroll687 #viewToday.todayTight687 .room{font-size:.64rem!important;margin-top:1px!important}
                  body.todayNoScroll687 #viewToday.todayTight687 .badge{font-size:.58rem!important;padding:3px 5px!important}
                  body.todayNoScroll687 #viewToday.todayVeryTight687 .label{font-size:.74rem!important}
                  body.todayNoScroll687 #viewToday.todayVeryTight687 .time{font-size:.63rem!important}
                  body.todayNoScroll687 #viewToday.todayVeryTight687 .room{font-size:.58rem!important}
                  body.todayNoScroll687 #viewToday.todayVeryTight687 .dayTitle{margin-bottom:3px!important}
                  body.todayNoScroll687 #viewToday.todayVeryTight687 .timelineCard{padding-top:5px!important;padding-bottom:5px!important;margin-bottom:4px!important}

                  #widgetEdgeBars672 .bar672Grid{
                    grid-template-columns:62px minmax(96px,126px) 38px!important;
                    justify-content:center!important;gap:6px!important
                  }
                  #widgetEdgeBars672 .bar672Label{
                    font-size:.66rem!important;font-weight:800!important;text-align:right!important
                  }
                  #widgetEdgeBars672 select{
                    width:126px!important;max-width:126px!important;min-width:0!important;min-height:29px!important;
                    padding:4px 22px 4px 7px!important;border-radius:8px!important;
                    font-size:.64rem!important;line-height:1.05!important;font-weight:750!important;
                    text-align:center!important;text-align-last:center!important;justify-self:start!important
                  }
                  #widgetEdgeBars672 input[type=color]{
                    width:34px!important;height:30px!important;border-radius:7px!important;padding:2px!important
                  }
                  @media(max-width:370px){
                    #widgetEdgeBars672 .bar672Grid{grid-template-columns:56px minmax(90px,118px) 34px!important;gap:5px!important}
                    #widgetEdgeBars672 select{width:118px!important;max-width:118px!important;font-size:.61rem!important}
                    #widgetEdgeBars672 input[type=color]{width:32px!important;height:28px!important}
                  }

                  #settingsSheet .settingBox:has(#languageSelect) #languageTitle,
                  #settingsSheet .settingBox:has(#advProfileSelect) #advProfilesTitle{
                    width:100%!important;text-align:center!important
                  }
                  #settingsSheet #languageSelect,
                  #settingsSheet #advProfileSelect{
                    display:block!important;width:min(72%,300px)!important;max-width:300px!important;
                    margin:0 auto!important;text-align:center!important;text-align-last:center!important;
                    font-size:.72rem!important;padding:7px 26px 7px 10px!important
                  }
                  #settingsSheet .advRow:has(#advProfileSelect){
                    display:block!important;width:100%!important;margin:0!important
                  }
                `;
                document.head.appendChild(style);

                let fitQueued=false;
                function isToday(){
                  const view=document.getElementById('viewToday');
                  return !!(view&&view.classList.contains('active'));
                }
                function fitToday(){
                  fitQueued=false;
                  const view=document.getElementById('viewToday'),list=document.getElementById('todayList'),bottom=document.querySelector('.bottom');
                  if(!view||!list||!bottom)return;
                  const active=isToday();
                  document.body.classList.toggle('todayNoScroll687',active);
                  if(!active){view.style.removeProperty('height');view.style.removeProperty('--today-row-h');view.classList.remove('todayTight687','todayVeryTight687');return}
                  try{if(document.scrollingElement)document.scrollingElement.scrollTop=0}catch(e){}
                  const top=view.getBoundingClientRect().top,bottomTop=bottom.getBoundingClientRect().top;
                  const available=Math.max(220,Math.floor(bottomTop-top-3));
                  view.style.height=available+'px';
                  view.style.setProperty('--today-row-h','42px');
                  view.classList.remove('todayTight687','todayVeryTight687');
                  const rows=[...list.querySelectorAll(':scope > .todayCourse')];
                  if(!rows.length)return;
                  const listTop=Math.max(0,Math.ceil(list.getBoundingClientRect().top-view.getBoundingClientRect().top));
                  const usable=Math.max(1,available-listTop-2);
                  const rowHeight=Math.max(24,Math.min(44,Math.floor((usable-2)/rows.length)));
                  view.style.setProperty('--today-row-h',rowHeight+'px');
                  view.classList.toggle('todayTight687',rowHeight<38);
                  view.classList.toggle('todayVeryTight687',rowHeight<32);
                }
                function queueFit(){
                  if(fitQueued)return;fitQueued=true;
                  requestAnimationFrame(()=>requestAnimationFrame(fitToday));
                }
                const oldToday=window.renderToday;
                if(typeof oldToday==='function'&&!oldToday.__feedback687Fit){
                  const wrapped=function(){const result=oldToday.apply(this,arguments);queueFit();return result};
                  wrapped.__feedback687Fit=true;wrapped.__feedback687Original=oldToday;
                  window.renderToday=wrapped;try{renderToday=wrapped}catch(e){}
                }
                document.addEventListener('pointerdown',event=>{
                  const nav=event.target&&event.target.closest?event.target.closest('.nav[data-mode]'):null;
                  if(nav)requestAnimationFrame(queueFit);
                },true);
                window.addEventListener('resize',queueFit,{passive:true});
                const todayList=document.getElementById('todayList');
                if(todayList&&!todayList.__feedback687Observed){
                  todayList.__feedback687Observed=true;
                  new MutationObserver(queueFit).observe(todayList,{childList:true,subtree:false});
                }

                function refresh(){queueFit()}
                window.refreshFeedback687=refresh;
                refresh();
              }catch(e){console.error('Feedback687Ui',e)}
            })();
            """;
    }
}
