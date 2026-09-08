package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__weekGeometryV8){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__weekGeometryV8=true;

                const style=document.createElement('style');
                style.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:49px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  }

                  #weekGrid #weekNowRail,#weekGrid #weekNowDot{display:none!important}
                  #weekGrid .dynamicLunchOverlay{display:none!important}

                  #weekGrid .geoLunchCell{
                    padding:0!important;
                    border-radius:0!important;
                    background:var(--ft-midi)!important;
                    color:var(--ft-midi-ink)!important;
                    box-shadow:none!important;
                    overflow:hidden!important;
                  }
                  #weekGrid .geoLunchCell>*{visibility:hidden!important}
                  #weekGrid .geoLunchLabel{
                    position:absolute;z-index:90;display:flex;align-items:center;justify-content:center;
                    gap:4px;padding:0 3px;box-sizing:border-box;pointer-events:none;
                    color:var(--ft-midi-ink);font-weight:850;font-size:.60rem;line-height:1;
                    white-space:nowrap;overflow:hidden;text-overflow:clip;
                  }

                  #weekGrid #weekNowRailV8{
                    position:absolute;z-index:95;width:2px;background:#1888f2;border-radius:0;
                    pointer-events:none;display:none;
                  }
                  #weekGrid #weekNowDotV8{
                    position:absolute;z-index:96;width:14px;height:14px;border-radius:50%;
                    background:#1688f4;border:4px solid #d9ecff;box-sizing:content-box;
                    box-shadow:0 1px 4px #0b6acb38;pointer-events:none;display:none;
                  }

                  .dualBreakInputs{display:grid;grid-template-columns:72px minmax(0,1fr);gap:5px 7px;align-items:center}
                  .dualBreakInputs .dualLabel{font-size:.66rem;color:var(--muted);font-weight:750}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important;align-items:start!important}
                  #courseWidgetLabelField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                let raf=0,late=0,mutating=false;

                function toMin(v){
                  const p=String(v||'').split(':').map(Number);
                  return (p[0]||0)*60+(p[1]||0);
                }
                function cssVar(name,fallback){
                  const v=getComputedStyle(document.documentElement).getPropertyValue(name).trim();
                  return v||fallback;
                }
                function rowsOf(grid){
                  const rows=[];
                  const timeCells=Array.from(grid.querySelectorAll('.wh.timecol'));
                  for(const t of timeCells){
                    const found=(t.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(found.length<2)continue;
                    const cells=[];let n=t.nextElementSibling;
                    while(n&&cells.length<5){
                      if(n.classList&&n.classList.contains('wc'))cells.push(n);
                      n=n.nextElementSibling;
                    }
                    if(cells.length===5)rows.push({time:t,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);
                  return rows;
                }
                function ensureLayers(grid){
                  let rail=document.getElementById('weekNowRailV8');
                  let dot=document.getElementById('weekNowDotV8');
                  if(!rail){rail=document.createElement('div');rail.id='weekNowRailV8';grid.appendChild(rail)}
                  if(!dot){dot=document.createElement('div');dot.id='weekNowDotV8';grid.appendChild(dot)}
                  return {rail,dot};
                }
                function resetPaint(grid){
                  grid.querySelectorAll('.geoLunchLabel').forEach(e=>e.remove());
                  grid.querySelectorAll('.geoLunchCell').forEach(cell=>{
                    cell.classList.remove('geoLunchCell');
                    for(const p of ['background','box-shadow','border-radius','padding','overflow','border-right-color','border-bottom-color'])cell.style.removeProperty(p);
                  });
                  grid.querySelectorAll('[data-geo-edge="1"]').forEach(el=>{
                    el.style.removeProperty('border-right-color');
                    el.style.removeProperty('border-bottom-color');
                    el.removeAttribute('data-geo-edge');
                  });
                }
                function edge(el,prop,value){
                  if(!el)return;
                  el.style.setProperty(prop,value,'important');
                  el.setAttribute('data-geo-edge','1');
                }
                function lunchFor(dayIndex){
                  try{
                    if(typeof DAYS==='undefined'||typeof state==='undefined')return null;
                    const d=DAYS[dayIndex],list=state[d]&&Array.isArray(state[d].courses)?state[d].courses:[];
                    if(typeof lunchForDay==='function')return lunchForDay(list);
                    if(typeof lunch==='function'){
                      const l=lunch();if(!l)return null;
                      const s=toMin(l.start),e=toMin(l.end);
                      const before=list.some(c=>toMin(c.end)<=s),after=list.some(c=>toMin(c.start)>=e);
                      const occupied=list.some(c=>toMin(c.start)<e&&toMin(c.end)>s);
                      return before&&after&&!occupied?{startM:s,endM:e}:null;
                    }
                  }catch(e){}
                  return null;
                }
                function lunchLabel(){
                  try{return typeof lunchLabelText==='function'?lunchLabelText():String((breaks&&breaks.lunchLabel)||'Midi').trim()}
                  catch(e){return 'Midi'}
                }
                function paintLunch(grid,rows){
                  const bg=cssVar('--ft-midi','#FFF9E8');
                  const border=cssVar('--ft-midi-border','#CBBE9E');
                  const label=lunchLabel();
                  for(let dayIndex=0;dayIndex<5;dayIndex++){
                    const l=lunchFor(dayIndex);if(!l)continue;
                    const ls=Number(l.startM!=null?l.startM:toMin(l.start));
                    const le=Number(l.endM!=null?l.endM:toMin(l.end));
                    if(!(le>ls))continue;
                    const segment=[];
                    for(const row of rows){
                      if(Math.min(row.end,le)>Math.max(row.start,ls))segment.push({row,cell:row.cells[dayIndex]});
                    }
                    if(!segment.length)continue;

                    segment.forEach(({cell},i)=>{
                      cell.classList.add('geoLunchCell');
                      cell.style.setProperty('background','var(--ft-midi)','important');
                      cell.style.setProperty('box-shadow','none','important');
                      cell.style.setProperty('border-radius','0','important');
                      cell.style.setProperty('padding','0','important');
                      cell.style.setProperty('overflow','hidden','important');
                      const leftNeighbor=cell.previousElementSibling;
                      edge(leftNeighbor,'border-right-color',border);
                      edge(cell,'border-right-color',border);
                      edge(cell,'border-bottom-color',i===segment.length-1?border:bg);
                    });

                    const firstIndex=rows.indexOf(segment[0].row);
                    const above=firstIndex>0?rows[firstIndex-1].cells[dayIndex]:grid.querySelectorAll('.wh.day')[dayIndex];
                    edge(above,'border-bottom-color',border);

                    if(label){
                      const first=segment[0].cell,last=segment[segment.length-1].cell;
                      const lab=document.createElement('div');lab.className='geoLunchLabel';
                      lab.innerHTML='<span aria-hidden="true">🍴</span><span></span>';
                      lab.lastElementChild.textContent=label;
                      lab.style.left=first.offsetLeft+'px';
                      lab.style.top=first.offsetTop+'px';
                      lab.style.width=first.offsetWidth+'px';
                      lab.style.height=Math.max(first.offsetHeight,last.offsetTop+last.offsetHeight-first.offsetTop)+'px';
                      grid.appendChild(lab);
                    }
                  }
                }
                function paintNow(grid,rows){
                  const layers=ensureLayers(grid),rail=layers.rail,dot=layers.dot;
                  rail.style.display=dot.style.display='none';
                  const now=new Date(),day=now.getDay();
                  if(day<1||day>5||!rows.length)return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}
                  const minute=now.getHours()*60+now.getMinutes();
                  const dayIndex=day-1;
                  let target=null;
                  for(const row of rows){if(minute>=row.start&&minute<row.end){target=row;break}}
                  if(!target)return;
                  const first=rows[0].cells[dayIndex],cell=target.cells[dayIndex];
                  if(!first||!cell)return;
                  const frac=Math.max(0,Math.min(1,(minute-target.start)/Math.max(1,target.end-target.start)));
                  const x=cell.offsetLeft;
                  const top=first.offsetTop;
                  const y=cell.offsetTop+cell.offsetHeight*frac;
                  rail.style.setProperty('left',(x-1)+'px','important');
                  rail.style.setProperty('top',top+'px','important');
                  rail.style.setProperty('height',Math.max(2,y-top)+'px','important');
                  rail.style.setProperty('display','block','important');
                  dot.style.setProperty('left',(x-11)+'px','important');
                  dot.style.setProperty('top',(y-11)+'px','important');
                  dot.style.setProperty('display','block','important');
                }
                function syncGeometry(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  mutating=true;
                  try{
                    grid.style.setProperty('position','relative','important');
                    grid.style.setProperty('overflow','hidden','important');
                    const rows=rowsOf(grid);if(!rows.length)return;
                    resetPaint(grid);
                    paintLunch(grid,rows);
                    paintNow(grid,rows);
                  }finally{mutating=false}
                }
                function scheduleGeometry(){
                  if(raf)cancelAnimationFrame(raf);if(late)clearTimeout(late);
                  raf=requestAnimationFrame(()=>requestAnimationFrame(()=>{
                    syncGeometry();late=setTimeout(syncGeometry,120);
                  }));
                }
                window.refreshDoubleLunchUi=scheduleGeometry;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function keyFor(week,day,start,end){return String(week||'A')+'|'+String(day||2)+'|'+String(start||'')+'|'+String(end||'')}
                function ensureCourseWidgetField(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseWidgetLabelField');
                  if(!field){
                    field=document.createElement('div');field.id='courseWidgetLabelField';field.className='field';
                    field.innerHTML='<label>Intitulé dans le widget (facultatif)</label><input id="fWidgetLabel" type="text" maxlength="80" placeholder="Vide = même intitulé que dans l\'application">';
                    const app=document.getElementById('fLabel'),appField=app?app.closest('.field'):null;
                    if(appField&&appField.nextSibling)appField.parentNode.insertBefore(field,appField.nextSibling);else form.appendChild(field);
                  }
                }
                function currentCourse(){
                  try{if(typeof editing==='undefined'||editing==null)return null;return weeks[activeWeek][selected].courses[editing]||null}catch(e){return null}
                }
                function fillCourseWidgetField(){
                  ensureCourseWidgetField();const input=document.getElementById('fWidgetLabel');if(!input)return;
                  const c=currentCourse();if(!c){input.value='';return}
                  const a=loadAdv(),map=a.widgetCourseLabels||{};
                  input.value=String(map[keyFor(activeWeek,selected,c.start,c.end)]||'');
                }
                function wrapCourseSubmitLabels(){
                  ensureCourseWidgetField();
                  const form=document.getElementById('courseForm');if(!form||!form.onsubmit||form.onsubmit.__widgetLabelsV8)return;
                  const old=form.onsubmit;
                  const wrapped=function(e){
                    const oldCourse=currentCourse();
                    const oldKey=oldCourse?keyFor(activeWeek,selected,oldCourse.start,oldCourse.end):null;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0);
                    let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}
                    else if(oldCourse){start=oldCourse.start;end=oldCourse.end}
                    else if(typeof newPrefill!=='undefined'&&newPrefill){start=newPrefill.start;end=newPrefill.end}
                    const widgetLabel=String((document.getElementById('fWidgetLabel')||{}).value||'').trim();
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A',day=typeof selected!=='undefined'?selected:2;
                    const out=old.call(this,e);
                    try{
                      const a=loadAdv();a.widgetCourseLabels=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                      if(oldKey)delete a.widgetCourseLabels[oldKey];
                      const newKey=keyFor(week,day,start,end);
                      if(widgetLabel)a.widgetCourseLabels[newKey]=widgetLabel;else delete a.widgetCourseLabels[newKey];
                      saveAdv(a);
                    }catch(ex){}
                    return out;
                  };
                  wrapped.__widgetLabelsV8=true;form.onsubmit=wrapped;
                }
                function installBreakWidgetInputs(){
                  const pairs=[['gapLabel','gapWidgetLabel'],['lunchLabel','lunchWidgetLabel']];
                  const a=loadAdv();
                  for(const [appId,key] of pairs){
                    const app=document.getElementById(appId);if(!app)continue;
                    let host=app.closest('.dualBreakInputs');
                    let widget=document.getElementById(appId+'Widget');
                    if(!host){
                      host=document.createElement('div');host.className='dualBreakInputs';
                      const parent=app.parentElement;parent.insertBefore(host,app);
                      const l1=document.createElement('div');l1.className='dualLabel';l1.textContent='Application';host.appendChild(l1);host.appendChild(app);
                      const l2=document.createElement('div');l2.className='dualLabel';l2.textContent='Widget';host.appendChild(l2);
                      widget=document.createElement('input');widget.id=appId+'Widget';widget.type='text';widget.maxLength=35;widget.placeholder='Vide = même intitulé';host.appendChild(widget);
                    }
                    if(widget&&document.activeElement!==widget)widget.value=String(a[key]||'');
                    if(widget&&!widget.__widgetLabelBound){
                      widget.__widgetLabelBound=true;
                      const persist=()=>{const x=loadAdv();x[key]=String(widget.value||'').trim();saveAdv(x)};
                      widget.addEventListener('change',persist);widget.addEventListener('blur',persist);
                    }
                  }
                }
                function installLabelsUi(){
                  ensureCourseWidgetField();wrapCourseSubmitLabels();installBreakWidgetInputs();
                  const modal=document.getElementById('modal');
                  if(modal&&!modal.__widgetLabelsObserver){
                    modal.__widgetLabelsObserver=true;
                    new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{fillCourseWidgetField();wrapCourseSubmitLabels()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});
                  }
                }

                const grid=document.getElementById('weekGrid');
                if(grid){
                  new MutationObserver(muts=>{
                    if(mutating)return;
                    const external=muts.some(m=>{
                      const nodes=[...m.addedNodes,...m.removedNodes];
                      if(!nodes.length)return false;
                      return nodes.some(n=>!(n.nodeType===1&&(n.classList?.contains('geoLunchLabel')||n.id==='weekNowRailV8'||n.id==='weekNowDotV8')));
                    });
                    if(external)scheduleGeometry();
                  }).observe(grid,{childList:true});
                  if(window.ResizeObserver)new ResizeObserver(()=>scheduleGeometry()).observe(grid);
                }
                window.addEventListener('resize',scheduleGeometry);
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)scheduleGeometry()});
                setInterval(scheduleGeometry,60000);
                if(document.fonts&&document.fonts.ready)document.fonts.ready.then(scheduleGeometry);
                installLabelsUi();
                scheduleGeometry();
              }catch(e){console.log('Week geometry V8',e)}
            })();
            """;
    }
}
