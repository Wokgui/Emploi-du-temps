package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__doubleLunchUiV5){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi(true);
                  return;
                }
                window.__doubleLunchUiV5=true;

                const polish=document.createElement('style');
                polish.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:47px!important}
                  }

                  /* Pendant un recalcul structurel, on masque seulement la grille quelques
                     millisecondes : aucun état intermédiaire mal aligné ne peut être visible. */
                  #weekGrid.geometryPending{visibility:hidden!important}

                  /* Midi n'est plus dessiné comme un rectangle flottant. Les vraies cellules
                     de la grille portent le fond ; l'overlay ne sert plus qu'à centrer le texte. */
                  html body #viewWeek #weekGrid .wc.lunchCell,
                  html body #viewWeek #weekGrid .wc.dynamicLunchContinuation{
                    position:relative!important;
                    padding:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi)!important;
                    color:var(--ft-midi-ink)!important;
                  }
                  html body #viewWeek #weekGrid .dynamicLunchOverlay{
                    position:absolute!important;
                    z-index:25!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    margin:0!important;
                    padding:0!important;
                    background:transparent!important;
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    color:var(--ft-midi-ink)!important;
                    pointer-events:none!important;
                    box-sizing:border-box!important;
                  }
                  html body #viewWeek #weekGrid .dynamicLunchContinuation>*{visibility:hidden!important}
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot{transition:none!important}
                `;
                document.head.appendChild(polish);

                let frame=0,lateTimer=0,ignoreMutationsUntil=0;

                function toMin(v){
                  const p=String(v||'').split(':').map(Number);
                  return (p[0]||0)*60+(p[1]||0);
                }
                function cssVar(name,fallback){
                  const v=getComputedStyle(document.documentElement).getPropertyValue(name).trim();
                  return v||fallback;
                }
                function markEdge(el){if(el)el.setAttribute('data-midi-grid-edge','1')}
                function resetEdges(grid){
                  grid.querySelectorAll('[data-midi-grid-edge="1"]').forEach(el=>{
                    el.style.removeProperty('border-right-color');
                    el.style.removeProperty('border-bottom-color');
                    el.removeAttribute('data-midi-grid-edge');
                  });
                  grid.querySelectorAll('.dynamicLunchContinuation').forEach(el=>{
                    el.classList.remove('dynamicLunchContinuation');
                    el.style.removeProperty('background');
                  });
                  grid.querySelectorAll('.dynamicLunchCell').forEach(cell=>{
                    cell.removeAttribute('data-double-lunch');
                    cell.style.removeProperty('z-index');
                    cell.style.removeProperty('overflow');
                    const overlay=cell.querySelector('.dynamicLunchOverlay');
                    if(overlay){
                      ['top','left','right','bottom','width','height'].forEach(p=>overlay.style.removeProperty(p));
                    }
                  });
                }

                function paintLunchSegment(grid,kids,cells){
                  if(!cells.length)return;
                  const border=cssVar('--ft-midi-border','#D1B66A');
                  const bg=cssVar('--ft-midi','#FFF9E8');
                  cells.forEach(cell=>{
                    cell.style.setProperty('background','var(--ft-midi)','important');
                    cell.style.setProperty('box-shadow','none','important');
                    cell.style.setProperty('border-radius','0','important');
                  });

                  const first=cells[0],last=cells[cells.length-1];
                  const firstIdx=kids.indexOf(first);
                  const above=firstIdx>=6?kids[firstIdx-6]:null;
                  if(above){above.style.setProperty('border-bottom-color',border,'important');markEdge(above)}

                  cells.forEach((cell,i)=>{
                    const idx=kids.indexOf(cell);
                    const leftNeighbor=idx>0?kids[idx-1]:null;
                    if(leftNeighbor){leftNeighbor.style.setProperty('border-right-color',border,'important');markEdge(leftNeighbor)}
                    cell.style.setProperty('border-right-color',border,'important');markEdge(cell);
                    if(i<cells.length-1){
                      cell.style.setProperty('border-bottom-color',bg,'important');
                    }else{
                      cell.style.setProperty('border-bottom-color',border,'important');
                    }
                    markEdge(cell);
                  });

                  const overlay=first.querySelector('.dynamicLunchOverlay');
                  if(overlay){
                    const firstRect=first.getBoundingClientRect();
                    const lastRect=last.getBoundingClientRect();
                    overlay.style.setProperty('top','0px','important');
                    overlay.style.setProperty('left','0px','important');
                    overlay.style.setProperty('right','0px','important');
                    overlay.style.setProperty('bottom','auto','important');
                    overlay.style.setProperty('width','100%','important');
                    overlay.style.setProperty('height',Math.max(first.offsetHeight,lastRect.bottom-firstRect.top)+'px','important');
                  }
                }

                function syncLunchGeometry(grid){
                  if(typeof state==='undefined'||typeof DAYS==='undefined'||typeof slots==='undefined')return;
                  const kids=Array.from(grid.children);
                  if(kids.length<12)return;
                  resetEdges(grid);

                  const secondStart=slots[4]&&slots[4].start?toMin(slots[4].start):13*60;
                  const secondEnd=slots[4]&&slots[4].end?toMin(slots[4].end):14*60;
                  let secondRow=-1;
                  for(let p=6;p+5<kids.length;p+=6){
                    const times=(kids[p].textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(times.length>=2&&toMin(times[0])===secondStart&&toMin(times[1])===secondEnd){secondRow=p;break}
                  }

                  const dynamic=Array.from(grid.querySelectorAll('.dynamicLunchCell'));
                  dynamic.forEach(cell=>{
                    const idx=kids.indexOf(cell),dayIndex=(idx%6)-1;
                    if(dayIndex<0||dayIndex>=DAYS.length)return;
                    const segment=[cell];
                    if(secondRow>=0&&secondEnd>secondStart){
                      const day=state[DAYS[dayIndex]],courses=day&&Array.isArray(day.courses)?day.courses:[];
                      const occupied=courses.some(c=>toMin(c.start)<secondEnd&&toMin(c.end)>secondStart);
                      const hasAfter=courses.some(c=>toMin(c.start)>=secondEnd);
                      const next=kids[secondRow+1+dayIndex];
                      if(!occupied&&hasAfter&&next&&next.classList&&next.classList.contains('wc')){
                        next.classList.add('dynamicLunchContinuation');
                        cell.setAttribute('data-double-lunch','1');
                        cell.style.setProperty('overflow','visible','important');
                        cell.style.setProperty('z-index','20','important');
                        segment.push(next);
                      }
                    }
                    paintLunchSegment(grid,kids,segment);
                  });

                  grid.querySelectorAll('.wc.lunchCell:not(.dynamicLunchCell)').forEach(cell=>paintLunchSegment(grid,kids,[cell]));
                }

                function syncCurrentMarker(grid){
                  const rail=document.getElementById('weekNowRail');
                  const dot=document.getElementById('weekNowDot');
                  if(!rail||!dot)return;
                  const now=new Date(),day=now.getDay();
                  if(day<1||day>5){rail.style.display=dot.style.display='none';return}
                  try{
                    if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek){rail.style.display=dot.style.display='none';return}
                  }catch(e){}

                  const kids=Array.from(grid.children);
                  if(kids.length<12)return;
                  const headers=Array.from(grid.querySelectorAll('.wh.day'));
                  const header=headers[day-1];
                  if(!header)return;
                  const minute=now.getHours()*60+now.getMinutes();
                  let rowStart=-1,rowEnd=-1,target=null,first=null,last=null;
                  const dayOffset=day;
                  for(let p=6;p+5<kids.length;p+=6){
                    const timeCell=kids[p];
                    const times=(timeCell.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    const dayCell=kids[p+dayOffset];
                    if(!dayCell)continue;
                    if(!first)first=dayCell;
                    last=dayCell;
                    if(times.length>=2){
                      const s=toMin(times[0]),e=toMin(times[1]);
                      if(minute>=s&&minute<=e){rowStart=s;rowEnd=e;target=dayCell}
                    }
                  }
                  if(!first||!last||!target||rowEnd<=rowStart){rail.style.display=dot.style.display='none';return}

                  const g=grid.getBoundingClientRect();
                  const h=header.getBoundingClientRect();
                  const f=first.getBoundingClientRect();
                  const l=last.getBoundingClientRect();
                  const t=target.getBoundingClientRect();
                  const x=h.left-g.left;
                  const frac=Math.max(0,Math.min(1,(minute-rowStart)/(rowEnd-rowStart)));
                  const y=t.top-g.top+t.height*frac;
                  const top=f.top-g.top;
                  const bottom=l.bottom-g.top;

                  rail.style.setProperty('position','absolute','important');
                  rail.style.setProperty('width','2px','important');
                  rail.style.setProperty('left',(x-1)+'px','important');
                  rail.style.setProperty('top',top+'px','important');
                  rail.style.setProperty('height',Math.max(2,bottom-top)+'px','important');
                  rail.style.setProperty('transform','none','important');
                  rail.style.setProperty('display','block','important');

                  dot.style.setProperty('position','absolute','important');
                  dot.style.setProperty('width','11px','important');
                  dot.style.setProperty('height','11px','important');
                  dot.style.setProperty('left',(x-5.5)+'px','important');
                  dot.style.setProperty('top',(y-5.5)+'px','important');
                  dot.style.setProperty('transform','none','important');
                  dot.style.setProperty('display','block','important');
                }

                function synchronize(){
                  const grid=document.getElementById('weekGrid');
                  if(!grid)return;
                  ignoreMutationsUntil=performance.now()+80;
                  try{
                    grid.style.setProperty('position','relative','important');
                    syncLunchGeometry(grid);
                    syncCurrentMarker(grid);
                  }finally{
                    grid.classList.remove('geometryPending');
                  }
                }

                function schedule(structural){
                  const grid=document.getElementById('weekGrid');
                  if(!grid)return;
                  if(structural)grid.classList.add('geometryPending');
                  if(frame)cancelAnimationFrame(frame);
                  if(lateTimer)clearTimeout(lateTimer);
                  frame=requestAnimationFrame(()=>requestAnimationFrame(()=>{
                    synchronize();
                    lateTimer=setTimeout(synchronize,90);
                  }));
                }
                window.refreshDoubleLunchUi=function(){schedule(true)};

                function wrap(name){
                  const fn=window[name];
                  if(typeof fn!=='function'||fn.__gridGeometryWrapped)return;
                  const wrapped=function(){
                    const grid=document.getElementById('weekGrid');
                    if(grid)grid.classList.add('geometryPending');
                    const out=fn.apply(this,arguments);
                    schedule(true);
                    return out;
                  };
                  wrapped.__gridGeometryWrapped=true;
                  window[name]=wrapped;
                }
                wrap('render');wrap('renderWeek');

                const grid=document.getElementById('weekGrid');
                if(grid){
                  new MutationObserver(muts=>{
                    if(performance.now()<ignoreMutationsUntil)return;
                    const structural=muts.some(m=>m.type==='childList'&&m.target===grid);
                    schedule(structural);
                  }).observe(grid,{childList:true,subtree:true,attributes:true,attributeFilter:['style']});
                  if(window.ResizeObserver)new ResizeObserver(()=>schedule(false)).observe(grid);
                }
                window.addEventListener('resize',()=>schedule(false));
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)schedule(false)});
                setInterval(()=>schedule(false),60000);
                schedule(true);
              }catch(e){console.log('Grid geometry UI',e)}
            })();
            """;
    }
}
