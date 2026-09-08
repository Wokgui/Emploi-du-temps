package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekGeometryV13){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__weekGeometryV13=true;

                const style=document.createElement('style');
                style.id='weekGeometryV13Style';
                style.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:49px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  }

                  /* Une seule géométrie finale : les anciens rails et overlays ne sont jamais visibles. */
                  #weekGrid#weekGrid #weekNowRail,#weekGrid#weekGrid #weekNowDot,
                  #weekGrid#weekGrid #weekNowRailV8,#weekGrid#weekGrid #weekNowDotV8,
                  #weekGrid#weekGrid #finalWeekNowRail,#weekGrid#weekGrid #finalWeekNowDot,
                  #weekGrid#weekGrid #finalWeekNowRailV12,#weekGrid#weekGrid #finalWeekNowDotV12,
                  #weekGrid#weekGrid .scheduleNowRail,#weekGrid#weekGrid .scheduleNowDot,
                  #weekGrid#weekGrid .geoLunchLabel,#weekGrid#weekGrid .dynamicLunchOverlay{
                    display:none!important
                  }

                  /* Midi n'est pas une carte dans la cellule : Midi EST la cellule. */
                  html body #weekGrid#weekGrid>.wc.lunchCell,
                  html body #weekGrid#weekGrid>.wc.dynamicLunchCell,
                  html body #weekGrid#weekGrid>.wc.nativeLunchCell{
                    position:relative!important;
                    box-sizing:border-box!important;
                    margin:0!important;
                    padding:0!important;
                    border-left:0!important;
                    border-top:0!important;
                    border-right-width:1px!important;
                    border-bottom-width:1px!important;
                    border-right-style:solid!important;
                    border-bottom-style:solid!important;
                    border-radius:0!important;
                    outline:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    color:var(--ft-midi-ink,#22283A)!important;
                    overflow:hidden!important;
                    z-index:auto!important
                  }
                  html body #weekGrid#weekGrid>.wc.nativeLunchCell>*{visibility:visible!important}
                  html body #weekGrid#weekGrid .nativeLunchLabelV13{
                    position:absolute!important;
                    inset:0!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    gap:4px!important;
                    margin:0!important;
                    padding:2px 3px!important;
                    border:0!important;
                    outline:0!important;
                    box-shadow:none!important;
                    background:transparent!important;
                    white-space:nowrap!important;
                    overflow:hidden!important;
                    color:var(--ft-midi-ink,#22283A)!important;
                    font-weight:850!important;
                    font-size:.60rem!important;
                    line-height:1!important;
                    pointer-events:none!important
                  }
                  html body #weekGrid#weekGrid .nativeLunchLabelV13 .nativeLunchIconV13{
                    font-size:.84em!important;
                    line-height:1!important
                  }

                  /* Le repère temporel est ancré directement à la cellule du jour : aucune coordonnée X globale. */
                  html body #weekGrid#weekGrid>.wc.nativeNowHostV13{
                    position:relative!important;
                    overflow:visible!important
                  }
                  #weekGrid#weekGrid .nativeNowSegmentV13{
                    position:absolute!important;
                    left:-1.5px!important;
                    top:-1px!important;
                    width:2px!important;
                    background:#1688F4!important;
                    z-index:160!important;
                    pointer-events:none!important;
                    border:0!important;
                    margin:0!important;
                    padding:0!important;
                    box-shadow:none!important
                  }
                  #weekGrid#weekGrid .nativeNowDotV13{
                    position:absolute!important;
                    left:-8.5px!important;
                    width:16px!important;
                    height:16px!important;
                    border-radius:50%!important;
                    background:#1688F4!important;
                    border:4px solid #D9ECFF!important;
                    box-sizing:border-box!important;
                    box-shadow:0 1px 4px #0B6ACB38!important;
                    z-index:161!important;
                    pointer-events:none!important;
                    margin:0!important;
                    padding:0!important
                  }

                  .dualBreakInputs{display:grid;grid-template-columns:72px minmax(0,1fr);gap:5px 7px;align-items:center}
                  .dualBreakInputs .dualLabel{font-size:.66rem;color:var(--muted);font-weight:750}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important;align-items:start!important}
                  #courseWidgetLabelField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                let raf=0,lateTimer=0,gridObserver=null;

                function keepStyleLast(){
                  if(style.parentNode&&document.head.lastElementChild!==style)document.head.appendChild(style);
                }
                const headObserver=new MutationObserver(()=>{
                  if(document.head.lastElementChild!==style)requestAnimationFrame(keepStyleLast);
                });
                headObserver.observe(document.head,{childList:true});

                function toMin(v){
                  const p=String(v||'').split(':').map(Number);
                  return (p[0]||0)*60+(p[1]||0);
                }

                function lunchText(){
                  try{
                    if(typeof lunchLabelText==='function')return String(lunchLabelText()||'').trim();
                    if(typeof breaks!=='undefined'&&breaks)return String(breaks.lunchLabel||'Midi').trim();
                  }catch(e){}
                  return 'Midi';
                }

                function rowsOf(grid){
                  if(!grid)return [];
                  const rows=[];
                  const times=Array.from(grid.querySelectorAll(':scope > .wh.timecol'));
                  for(const time of times){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(found.length<2)continue;
                    const cells=[];
                    let n=time.nextElementSibling;
                    while(n&&cells.length<5){
                      if(n.classList&&n.classList.contains('wc'))cells.push(n);
                      n=n.nextElementSibling;
                    }
                    if(cells.length===5)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);
                  return rows;
                }

                function clearLunchEdges(grid){
                  grid.querySelectorAll('[data-midi-edge-v13="1"]').forEach(el=>{
                    el.style.removeProperty('border-right-color');
                    el.style.removeProperty('border-bottom-color');
                    el.style.removeProperty('border-right-width');
                    el.style.removeProperty('border-bottom-width');
                    el.style.removeProperty('border-right-style');
                    el.style.removeProperty('border-bottom-style');
                    el.removeAttribute('data-midi-edge-v13');
                  });
                }

                function colorExistingEdge(el,prop,color){
                  if(!el)return;
                  const widthProp=prop.replace('-color','-width');
                  const styleProp=prop.replace('-color','-style');
                  el.style.setProperty(widthProp,'1px','important');
                  el.style.setProperty(styleProp,'solid','important');
                  el.style.setProperty(prop,color,'important');
                  el.setAttribute('data-midi-edge-v13','1');
                }

                function normalizeLunchCell(cell,label){
                  if(!cell)return;
                  cell.classList.add('nativeLunchCell');
                  cell.querySelectorAll('.dynamicLunchOverlay,.geoLunchLabel,.nativeLunchLabel,.nativeLunchLabelV13').forEach(x=>x.remove());
                  const holder=document.createElement('div');
                  holder.className='nativeLunchLabelV13';
                  if(label){
                    const icon=document.createElement('span');
                    icon.className='nativeLunchIconV13';
                    icon.setAttribute('aria-hidden','true');
                    icon.textContent='🍴';
                    const text=document.createElement('span');
                    text.textContent=label;
                    holder.append(icon,text);
                  }
                  cell.replaceChildren(holder);
                  cell.style.setProperty('background','var(--ft-midi)','important');
                  cell.style.setProperty('border-left','0','important');
                  cell.style.setProperty('border-top','0','important');
                  cell.style.setProperty('box-shadow','none','important');
                  cell.style.setProperty('outline','0','important');
                  cell.style.setProperty('border-radius','0','important');
                  cell.style.setProperty('margin','0','important');
                  cell.style.setProperty('padding','0','important');
                }

                function paintLunch(grid,rows){
                  const border=(getComputedStyle(document.documentElement).getPropertyValue('--ft-midi-border')||'#CBBE9E').trim();
                  const label=lunchText();
                  clearLunchEdges(grid);
                  const headers=Array.from(grid.querySelectorAll(':scope > .wh.day'));

                  rows.forEach((row,rowIndex)=>{
                    row.cells.forEach((cell,dayIndex)=>{
                      const isLunch=cell.classList.contains('lunchCell')||
                                    cell.classList.contains('dynamicLunchCell')||
                                    cell.classList.contains('nativeLunchCell');
                      if(!isLunch)return;
                      normalizeLunchCell(cell,label);

                      /* Les quatre côtés sont exactement les traits natifs de grille. */
                      colorExistingEdge(cell,'border-right-color',border);
                      colorExistingEdge(cell,'border-bottom-color',border);
                      colorExistingEdge(cell.previousElementSibling,'border-right-color',border);
                      if(rowIndex>0)colorExistingEdge(rows[rowIndex-1].cells[dayIndex],'border-bottom-color',border);
                      else colorExistingEdge(headers[dayIndex],'border-bottom-color',border);
                    });
                  });
                }

                function clearNow(grid){
                  grid.querySelectorAll('.nativeNowSegmentV13,.nativeNowDotV13').forEach(x=>x.remove());
                  grid.querySelectorAll('.nativeNowHostV13').forEach(x=>x.classList.remove('nativeNowHostV13'));
                }

                function addSegment(cell,heightPx,dotY){
                  if(!cell)return;
                  cell.classList.add('nativeNowHostV13');
                  const seg=document.createElement('span');
                  seg.className='nativeNowSegmentV13';
                  seg.style.setProperty('height',Math.max(0,heightPx)+'px','important');
                  cell.appendChild(seg);
                  if(Number.isFinite(dotY)){
                    const dot=document.createElement('span');
                    dot.className='nativeNowDotV13';
                    dot.style.setProperty('top',(dotY-8)+'px','important');
                    cell.appendChild(dot);
                  }
                }

                function paintNow(grid,rows){
                  if(!grid)return;
                  clearNow(grid);
                  const now=new Date(),day=now.getDay();
                  if(day<1||day>5||!rows.length)return;
                  try{
                    if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return;
                  }catch(e){}

                  const minute=now.getHours()*60+now.getMinutes();
                  let currentIndex=-1,frac=0;
                  for(let i=0;i<rows.length;i++){
                    const r=rows[i];
                    if(minute>=r.start&&minute<r.end){
                      currentIndex=i;
                      frac=Math.max(0,Math.min(1,(minute-r.start)/Math.max(1,r.end-r.start)));
                      break;
                    }
                  }
                  if(currentIndex<0)return;

                  const dayIndex=day-1;
                  for(let i=0;i<currentIndex;i++){
                    const cell=rows[i].cells[dayIndex];
                    if(cell)addSegment(cell,cell.clientHeight+2,NaN);
                  }
                  const current=rows[currentIndex].cells[dayIndex];
                  if(current){
                    const y=Math.max(0,Math.min(current.clientHeight,current.clientHeight*frac));
                    addSegment(current,y+1,y);
                  }
                }

                function observeGrid(grid){
                  if(gridObserver)gridObserver.disconnect();
                  gridObserver=new MutationObserver(()=>scheduleGrid());
                  gridObserver.observe(grid,{childList:true,subtree:false});
                }

                function syncGrid(){
                  const grid=document.getElementById('weekGrid');
                  if(!grid)return;
                  keepStyleLast();
                  const rows=rowsOf(grid);
                  if(rows.length){
                    paintLunch(grid,rows);
                    paintNow(grid,rows);
                  }
                  observeGrid(grid);
                }

                function scheduleGrid(){
                  if(raf)cancelAnimationFrame(raf);
                  if(lateTimer)clearTimeout(lateTimer);
                  raf=requestAnimationFrame(()=>requestAnimationFrame(syncGrid));
                  lateTimer=setTimeout(syncGrid,100);
                }
                window.refreshDoubleLunchUi=scheduleGrid;

                function loadAdv(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}
                  catch(e){return {}}
                }
                function saveAdv(o){
                  try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}
                }
                function keyFor(week,day,start,end){
                  return String(week||'A')+'|'+String(day||2)+'|'+String(start||'')+'|'+String(end||'');
                }

                function ensureCourseWidgetField(){
                  const form=document.getElementById('courseForm');
                  if(!form)return;
                  let field=document.getElementById('courseWidgetLabelField');
                  if(field)return;
                  field=document.createElement('div');
                  field.id='courseWidgetLabelField';
                  field.className='field';
                  field.innerHTML='<label>Intitulé dans le widget (facultatif)</label><input id="fWidgetLabel" type="text" maxlength="80" placeholder="Vide = même intitulé que dans l\'application">';
                  const app=document.getElementById('fLabel'),appField=app?app.closest('.field'):null;
                  if(appField&&appField.nextSibling)appField.parentNode.insertBefore(field,appField.nextSibling);
                  else form.insertBefore(field,form.querySelector('.sheetActions'));
                }

                function editedCourse(){
                  try{
                    if(typeof editing==='undefined'||editing==null)return null;
                    return weeks[activeWeek][selected].courses[editing]||null;
                  }catch(e){return null}
                }

                function fillCourseWidgetField(){
                  ensureCourseWidgetField();
                  const input=document.getElementById('fWidgetLabel');
                  if(!input)return;
                  const c=editedCourse();
                  if(!c){input.value='';return}
                  const a=loadAdv(),map=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                  input.value=String(map[keyFor(activeWeek,selected,c.start,c.end)]||'');
                }

                function wrapCourseSubmit(){
                  ensureCourseWidgetField();
                  const form=document.getElementById('courseForm');
                  if(!form||!form.onsubmit||form.onsubmit.__widgetLabelsV13)return;
                  const old=form.onsubmit;
                  const wrapped=function(e){
                    const c=editedCourse(),oldKey=c?keyFor(activeWeek,selected,c.start,c.end):null;
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A';
                    const day=typeof selected!=='undefined'?selected:2;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0);
                    let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}
                    else if(c){start=c.start;end=c.end}
                    else if(typeof newPrefill!=='undefined'&&newPrefill){start=newPrefill.start;end=newPrefill.end}
                    const widgetLabel=String((document.getElementById('fWidgetLabel')||{}).value||'').trim();
                    const result=old.call(this,e);
                    try{
                      const a=loadAdv();
                      a.widgetCourseLabels=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                      if(oldKey)delete a.widgetCourseLabels[oldKey];
                      const newKey=keyFor(week,day,start,end);
                      if(widgetLabel)a.widgetCourseLabels[newKey]=widgetLabel;
                      else delete a.widgetCourseLabels[newKey];
                      saveAdv(a);
                    }catch(ex){}
                    return result;
                  };
                  wrapped.__widgetLabelsV13=true;
                  form.onsubmit=wrapped;
                }

                function installBreakWidgetInputs(){
                  const defs=[['gapLabel','gapWidgetLabel'],['lunchLabel','lunchWidgetLabel']];
                  const adv=loadAdv();
                  for(const [appId,key] of defs){
                    const app=document.getElementById(appId);
                    if(!app)continue;
                    let host=app.closest('.dualBreakInputs');
                    let widget=document.getElementById(appId+'Widget');
                    if(!host){
                      host=document.createElement('div');
                      host.className='dualBreakInputs';
                      const parent=app.parentElement;
                      parent.insertBefore(host,app);
                      const aLabel=document.createElement('div');
                      aLabel.className='dualLabel';
                      aLabel.textContent='Application';
                      const wLabel=document.createElement('div');
                      wLabel.className='dualLabel';
                      wLabel.textContent='Widget';
                      host.append(aLabel,app,wLabel);
                      widget=document.createElement('input');
                      widget.id=appId+'Widget';
                      widget.type='text';
                      widget.maxLength=35;
                      widget.placeholder='Vide = même intitulé';
                      host.appendChild(widget);
                    }
                    if(widget&&document.activeElement!==widget)widget.value=String(adv[key]||'');
                    if(widget&&!widget.__labelV13){
                      widget.__labelV13=true;
                      const persist=()=>{
                        const a=loadAdv();
                        a[key]=String(widget.value||'').trim();
                        saveAdv(a);
                      };
                      widget.addEventListener('change',persist);
                      widget.addEventListener('blur',persist);
                    }
                  }
                }

                function installLabelUi(){
                  ensureCourseWidgetField();
                  wrapCourseSubmit();
                  installBreakWidgetInputs();
                  const modal=document.getElementById('modal');
                  if(modal&&!modal.__widgetLabelsV13){
                    modal.__widgetLabelsV13=true;
                    new MutationObserver(()=>{
                      if(modal.classList.contains('show'))setTimeout(()=>{
                        fillCourseWidgetField();
                        wrapCourseSubmit();
                      },0);
                    }).observe(modal,{attributes:true,attributeFilter:['class']});
                  }
                }

                const grid=document.getElementById('weekGrid');
                if(grid){
                  observeGrid(grid);
                  if(window.ResizeObserver)new ResizeObserver(scheduleGrid).observe(grid);
                }
                window.addEventListener('resize',scheduleGrid);
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)scheduleGrid()});
                if(document.fonts&&document.fonts.ready)document.fonts.ready.then(scheduleGrid);
                setTimeout(scheduleGrid,30);
                setTimeout(scheduleGrid,180);
                setInterval(()=>{
                  const g=document.getElementById('weekGrid');
                  if(g)paintNow(g,rowsOf(g));
                },60000);

                installLabelUi();
                scheduleGrid();
              }catch(e){
                console.log('Week geometry V13',e);
              }
            })();
            """;
    }
}
