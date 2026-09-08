package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__doubleLunchUiV4){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__doubleLunchUiV4=true;

                const polish=document.createElement('style');
                polish.textContent=`
                  /* Un peu plus de hauteur pour mieux occuper l'écran en vue semaine. */
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:47px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:46px!important}
                  }

                  /* Midi utilise les vraies limites des cellules du tableau : plus de liseré
                     dessiné un pixel à l'intérieur, donc les rectangles coïncident avec la grille. */
                  html body #viewWeek #weekGrid .wc.lunchCell{
                    position:relative!important;
                    padding:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi)!important;
                    overflow:visible!important;
                  }
                  html body #viewWeek #weekGrid .wc.lunchCell:not(.dynamicLunchCell)::after{
                    content:'';
                    position:absolute;
                    inset:-1px;
                    border:1px solid var(--ft-midi-border)!important;
                    border-radius:0!important;
                    box-sizing:border-box!important;
                    pointer-events:none;
                    z-index:15;
                  }
                  html body #viewWeek #weekGrid .dynamicLunchOverlay{
                    box-sizing:border-box!important;
                    border:1px solid var(--ft-midi-border)!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi)!important;
                  }
                `;
                document.head.appendChild(polish);

                function toMin(v){
                  try{
                    if(typeof min==='function')return min(v);
                    const p=String(v||'').split(':').map(Number);
                    return (p[0]||0)*60+(p[1]||0);
                  }catch(e){return 0}
                }

                function alignCurrentRail(){
                  try{
                    const grid=document.getElementById('weekGrid');
                    const rail=document.getElementById('weekNowRail');
                    const dot=document.getElementById('weekNowDot');
                    if(!grid||!rail||!dot)return;
                    const day=new Date().getDay();
                    if(day<1||day>5)return;
                    const headers=Array.from(grid.querySelectorAll('.wh.day'));
                    const header=headers[day-1];
                    if(!header)return;
                    const g=grid.getBoundingClientRect();
                    const h=header.getBoundingClientRect();
                    const boundary=h.left-g.left;
                    /* Le rail fait 2 px : son bord gauche doit être 1 px avant la ligne
                       pour que son axe soit exactement superposé à la ligne verticale. */
                    rail.style.setProperty('left',(boundary-1)+'px','important');
                    dot.style.setProperty('left',boundary+'px','important');
                  }catch(e){}
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
                    if(secondEnd<=secondStart){alignCurrentRail();return}

                    let secondRow=-1;
                    const startText=(typeof clock==='function')?clock(secondStart):String(slots[4]?.start||'13:00');
                    const endText=(typeof clock==='function')?clock(secondEnd):String(slots[4]?.end||'14:00');
                    for(let p=6;p+5<kids.length;p+=6){
                      const times=(kids[p].textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                      if(times[0]===startText&&times[1]===endText){secondRow=p;break}
                    }
                    if(secondRow<0){alignCurrentRail();return}

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

                      /* Un élément absolute est positionné depuis l'intérieur de la bordure
                         de la cellule. -1 px le remet exactement sur les lignes de la grille. */
                      overlay.style.setProperty('top','-1px','important');
                      overlay.style.setProperty('left','-1px','important');
                      overlay.style.setProperty('right','-1px','important');
                      overlay.style.setProperty('bottom','auto','important');
                      overlay.style.setProperty('width','auto','important');
                      overlay.style.setProperty('height',Math.max(cell.offsetHeight,totalHeight)+'px','important');
                      overlay.style.setProperty('margin','0','important');
                      overlay.style.setProperty('padding','0','important');
                      overlay.style.setProperty('box-sizing','border-box','important');
                      overlay.style.setProperty('align-items','center','important');
                    });
                    alignCurrentRail();
                  }catch(e){}
                }

                function schedule(){
                  if(window.__doubleLunchFrame)cancelAnimationFrame(window.__doubleLunchFrame);
                  window.__doubleLunchFrame=requestAnimationFrame(()=>requestAnimationFrame(()=>{
                    apply();
                    alignCurrentRail();
                    setTimeout(alignCurrentRail,40);
                    setTimeout(alignCurrentRail,180);
                  }));
                }
                window.refreshDoubleLunchUi=schedule;

                const grid=document.getElementById('weekGrid');
                if(grid)new MutationObserver(schedule).observe(grid,{childList:true,subtree:true});
                window.addEventListener('resize',schedule);
                setInterval(()=>{apply();alignCurrentRail()},60000);
                schedule();
              }catch(e){console.log('Double lunch UI',e)}
            })();
            """;
    }
}
