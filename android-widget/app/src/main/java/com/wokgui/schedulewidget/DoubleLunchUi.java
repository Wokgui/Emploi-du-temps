package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__doubleLunchUiV2){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__doubleLunchUiV2=true;

                function toMin(v){
                  try{
                    if(typeof min==='function')return min(v);
                    const p=String(v||'').split(':').map(Number);
                    return (p[0]||0)*60+(p[1]||0);
                  }catch(e){return 0}
                }

                function reset(grid){
                  grid.querySelectorAll('.dynamicLunchCell').forEach(cell=>{
                    cell.removeAttribute('data-double-lunch');
                    cell.style.removeProperty('overflow');
                    cell.style.removeProperty('z-index');
                    const overlay=cell.querySelector('.dynamicLunchOverlay');
                    if(overlay){
                      overlay.style.removeProperty('top');
                      overlay.style.removeProperty('left');
                      overlay.style.removeProperty('right');
                      overlay.style.removeProperty('bottom');
                      overlay.style.removeProperty('width');
                      overlay.style.removeProperty('height');
                      overlay.style.removeProperty('margin');
                      overlay.style.removeProperty('padding');
                      overlay.style.removeProperty('box-sizing');
                      overlay.style.removeProperty('align-items');
                    }
                  });
                }

                function apply(){
                  try{
                    const grid=document.getElementById('weekGrid');
                    if(!grid||typeof state==='undefined'||typeof DAYS==='undefined'||typeof slots==='undefined')return;
                    reset(grid);
                    const kids=Array.from(grid.children);
                    const secondStart=slots[4]&&slots[4].start?toMin(slots[4].start):13*60;
                    const secondEnd=slots[4]&&slots[4].end?toMin(slots[4].end):14*60;
                    if(secondEnd<=secondStart)return;

                    let secondRow=-1;
                    const startText=(typeof clock==='function')?clock(secondStart):String(slots[4]?.start||'13:00');
                    const endText=(typeof clock==='function')?clock(secondEnd):String(slots[4]?.end||'14:00');
                    for(let p=6;p+5<kids.length;p+=6){
                      const times=(kids[p].textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                      if(times[0]===startText&&times[1]===endText){secondRow=p;break}
                    }
                    if(secondRow<0)return;

                    grid.querySelectorAll('.dynamicLunchCell').forEach(cell=>{
                      const idx=kids.indexOf(cell),dayIndex=(idx%6)-1;
                      if(dayIndex<0||dayIndex>=DAYS.length)return;
                      const day=state[DAYS[dayIndex]],courses=day&&Array.isArray(day.courses)?day.courses:[];
                      const occupied13=courses.some(c=>toMin(c.start)<secondEnd&&toMin(c.end)>secondStart);
                      const hasCourseAfter=courses.some(c=>toMin(c.start)>=secondEnd);
                      if(occupied13||!hasCourseAfter)return;

                      const nextCell=kids[secondRow+1+dayIndex];
                      const overlay=cell.querySelector('.dynamicLunchOverlay');
                      if(!nextCell||!overlay)return;
                      const totalHeight=(nextCell.offsetTop+nextCell.offsetHeight)-cell.offsetTop;
                      if(totalHeight<=cell.offsetHeight+2)return;

                      cell.setAttribute('data-double-lunch','1');
                      cell.style.setProperty('overflow','visible','important');
                      cell.style.setProperty('z-index','20','important');

                      // The lunch surface must coincide exactly with the temporal grid:
                      // no 1/2 px inset, no rounded card floating inside the cell.
                      overlay.style.setProperty('top','0px','important');
                      overlay.style.setProperty('left','0px','important');
                      overlay.style.setProperty('right','0px','important');
                      overlay.style.setProperty('bottom','auto','important');
                      overlay.style.setProperty('width','auto','important');
                      overlay.style.setProperty('height',Math.max(cell.offsetHeight,totalHeight)+'px','important');
                      overlay.style.setProperty('margin','0','important');
                      overlay.style.setProperty('padding','0','important');
                      overlay.style.setProperty('box-sizing','border-box','important');
                      overlay.style.setProperty('align-items','center','important');
                    });
                  }catch(e){}
                }

                function schedule(){
                  if(window.__doubleLunchFrame)cancelAnimationFrame(window.__doubleLunchFrame);
                  window.__doubleLunchFrame=requestAnimationFrame(()=>requestAnimationFrame(apply));
                }
                window.refreshDoubleLunchUi=schedule;

                const grid=document.getElementById('weekGrid');
                if(grid)new MutationObserver(schedule).observe(grid,{childList:true,subtree:true});
                window.addEventListener('resize',schedule);
                setInterval(schedule,60000);
                schedule();
              }catch(e){console.log('Double lunch UI',e)}
            })();
            """;
    }
}
