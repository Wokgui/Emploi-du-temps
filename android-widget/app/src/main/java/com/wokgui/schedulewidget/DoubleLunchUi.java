package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekGeometryV11){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__weekGeometryV11=true;

                const style=document.createElement('style');
                style.id='weekGeometryV11Style';
                style.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:49px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  }

                  /* Tous les anciens marqueurs et toutes les anciennes surcouches Midi sont neutralisés. */
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,
                  #weekGrid #weekNowRailV8,#weekGrid #weekNowDotV8,
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,
                  #weekGrid .geoLunchLabel,#weekGrid .dynamicLunchOverlay{display:none!important}

                  /* Midi est maintenant la vraie cellule de la grille. Aucune bordure supplémentaire. */
                  #weekGrid .wc.lunchCell,
                  #weekGrid .wc.dynamicLunchCell,
                  #weekGrid .wc.nativeLunchCell{
                    padding:0!important;
                    margin:0!important;
                    border-radius:0!important;
                    outline:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    color:var(--ft-midi-ink,#22283A)!important;
                    overflow:hidden!important;
                    position:relative!important;
                  }
                  #weekGrid .wc.lunchCell>*,
                  #weekGrid .wc.dynamicLunchCell>*,
                  #weekGrid .wc.nativeLunchCell>*{visibility:visible!important}
                  #weekGrid .nativeLunchLabel{
                    width:100%;height:100%;display:flex;align-items:center;justify-content:center;
                    gap:4px;padding:2px 3px;box-sizing:border-box;white-space:nowrap;overflow:hidden;
                    color:var(--ft-midi-ink,#22283A)!important;font-weight:850;font-size:.60rem;line-height:1;
                    pointer-events:none;
                  }
                  #weekGrid .nativeLunchLabel .nativeLunchIcon{font-size:.84em;line-height:1}

                  /* Marqueur courant unique. */
                  #weekGrid #finalWeekNowRail{
                    position:absolute;z-index:120;width:2px;background:#1688F4;pointer-events:none;
                    display:none;border-radius:0;transform:none!important;
                  }
                  #weekGrid #finalWeekNowDot{
                    position:absolute;z-index:121;width:16px;height:16px;border-radius:50%;
                    background:#1688F4;border:4px solid #D9ECFF;box-sizing:border-box;
                    box-shadow:0 1px 4px #0B6ACB38;pointer-events:none;display:none;transform:none!important;
                  }

                  .dualBreakInputs{display:grid;grid-template-columns:72px minmax(0,1fr);gap:5px 7px;align-items:center}
                  .dualBreakInputs .dualLabel{font-size:.66rem;color:var(--muted);font-weight:750}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important;align-items:start!important}
                  #courseWidgetLabelField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                let raf=0;
                let internal=false;

                function moveStyleLast(){
                  if(style.parentNode)document.head.appendChild(style);
                }

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
                  const rows=[];
                  const times=Array.from(grid.querySelectorAll('.wh.timecol'));
                  for(const time of times){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(found.length<2)continue;
                    const cells=[];
                    let n=time.nextElementSibling;
                    while(n&&cells.length<5){
                      if(n.classList&&n.classList.contains('wc'))cells.push(n);
                      n=n.nextElementSibling;
                    }
                    if(cells.length===5)rows.push({
                      time,
                      start:toMin(found[0]),
                      end:toMin(found[1]),
                      cells
                    });
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);
                  return rows;
                }

                function clearNativeEdges(grid){
                  grid.querySelectorAll('[data-native-midi-edge="1"]').forEach(el=>{
                    el.style.removeProperty('border-right-color');
                    el.style.removeProperty('border-bottom-color');
                    el.removeAttribute('data-native-midi-edge');
                  });
                }

                function paintEdge(el,prop,color){
                  if(!el)return;
                  el.style.setProperty(prop,color,'important');
                  el.setAttribute('data-native-midi-edge','1');
                }

                function normalizeLunchCell(cell,label){
                  if(!cell)return;
                  cell.classList.add('nativeLunchCell');
                  cell.querySelectorAll('.dynamicLunchOverlay,.geoLunchLabel').forEach(x=>x.remove());
                  let holder=cell.querySelector(':scope > .nativeLunchLabel');
                  if(!holder){
                    holder=document.createElement('div');
                    holder.className='nativeLunchLabel';
                    cell.replaceChildren(holder);
                  }
                  holder.replaceChildren();
                  if(label){
                    const icon=document.createElement('span');
                    icon.className='nativeLunchIcon';
                    icon.setAttribute('aria-hidden','true');
                    icon.textContent='🍴';
                    const text=document.createElement('span');
                    text.textContent=label;
                    holder.append(icon,text);
                  }
                  cell.style.setProperty('background','var(--ft-midi)','important');
                  cell.style.setProperty('box-shadow','none','important');
                  cell.style.setProperty('outline','0','important');
                  cell.style.setProperty('border-radius','0','important');
                  cell.style.setProperty('padding','0','important');
                  cell.style.setProperty('margin','0','important');
                }

                function paintLunch(grid,rows){
                  const border=(getComputedStyle(document.documentElement).getPropertyValue('--ft-midi-border')||'#CBBE9E').trim();
                  const label=lunchText();
                  clearNativeEdges(grid);

                  rows.forEach((row,rowIndex)=>{
                    row.cells.forEach((cell,dayIndex)=>{
                      const isLunch=cell.classList.contains('lunchCell')||cell.classList.contains('dynamicLunchCell');
                      if(!isLunch)return;
                      normalizeLunchCell(cell,label);

                      /* On ne change JAMAIS l'épaisseur : uniquement la couleur des vraies lignes 1 px. */
                      paintEdge(cell,'border-right-color',border);
                      paintEdge(cell,'border-bottom-color',border);

                      const left=cell.previousElementSibling;
                      paintEdge(left,'border-right-color',border);

                      if(rowIndex>0){
                        const above=rows[rowIndex-1].cells[dayIndex];
                        paintEdge(above,'border-bottom-color',border);
                      }else{
                        const header=grid.querySelectorAll('.wh.day')[dayIndex];
                        paintEdge(header,'border-bottom-color',border);
                      }
                    });
                  });
                }

                function ensureNowLayers(grid){
                  let rail=document.getElementById('finalWeekNowRail');
                  let dot=document.getElementById('finalWeekNowDot');
                  if(!rail){rail=document.createElement('div');rail.id='finalWeekNowRail';grid.appendChild(rail)}
                  if(!dot){dot=document.createElement('div');dot.id='finalWeekNowDot';grid.appendChild(dot)}
                  return {rail,dot};
                }

                function paintNow(grid,rows){
                  const {rail,dot}=ensureNowLayers(grid);
                  rail.style.display='none';
                  dot.style.display='none';
                  const now=new Date(),day=now.getDay();
                  if(day<1||day>5||!rows.length)return;
                  try{
                    if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return;
                  }catch(e){}

                  const minute=now.getHours()*60+now.getMinutes();
                  let row=null;
                  for(const r of rows){if(minute>=r.start&&minute<r.end){row=r;break}}
                  if(!row)return;

                  const dayIndex=day-1;
                  const first=rows[0].cells[dayIndex];
                  const target=row.cells[dayIndex];
                  if(!first||!target)return;

                  const gr=grid.getBoundingClientRect();
                  const fr=first.getBoundingClientRect();
                  const tr=target.getBoundingClientRect();
                  const frac=Math.max(0,Math.min(1,(minute-row.start)/Math.max(1,row.end-row.start)));

                  /* Le bord gauche réel de la cellule est la seule source de vérité. */
                  const x=tr.left-gr.left;
                  const top=fr.top-gr.top;
                  const y=tr.top-gr.top+tr.height*frac;

                  rail.style.setProperty('left',(x-1)+'px','important');
                  rail.style.setProperty('top',top+'px','important');
                  rail.style.setProperty('height',Math.max(2,y-top)+'px','important');
                  rail.style.setProperty('display','block','important');

                  dot.style.setProperty('left',(x-8)+'px','important');
                  dot.style.setProperty('top',(y-8)+'px','important');
                  dot.style.setProperty('display','block','important');
                }

                function syncGrid(){
                  const grid=document.getElementById('weekGrid');
                  if(!grid)return;
                  internal=true;
                  try{
                    moveStyleLast();
                    grid.style.setProperty('position','relative','important');
                    const rows=rowsOf(grid);
                    if(!rows.length)return;
                    paintLunch(grid,rows);
                    paintNow(grid,rows);
                  }finally{
                    internal=false;
                  }
                }

                function scheduleGrid(){
                  if(raf)cancelAnimationFrame(raf);
                  raf=requestAnimationFrame(()=>requestAnimationFrame(syncGrid));
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
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseWidgetLabelField');
                  if(field)return;
                  field=document.createElement('div');
                  field.id='courseWidgetLabelField';field.className='field';
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
                  const input=document.getElementById('fWidgetLabel');if(!input)return;
                  const c=editedCourse();if(!c){input.value='';return}
                  const a=loadAdv(),map=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                  input.value=String(map[keyFor(activeWeek,selected,c.start,c.end)]||'');
                }

                function wrapCourseSubmit(){
                  ensureCourseWidgetField();
                  const form=document.getElementById('courseForm');
                  if(!form||!form.onsubmit||form.onsubmit.__widgetLabelsV11)return;
                  const old=form.onsubmit;
                  const wrapped=function(e){
                    const c=editedCourse();
                    const oldKey=c?keyFor(activeWeek,selected,c.start,c.end):null;
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
                  wrapped.__widgetLabelsV11=true;
                  form.onsubmit=wrapped;
                }

                function installBreakWidgetInputs(){
                  const defs=[['gapLabel','gapWidgetLabel'],['lunchLabel','lunchWidgetLabel']];
                  const adv=loadAdv();
                  for(const [appId,key] of defs){
                    const app=document.getElementById(appId);if(!app)continue;
                    let host=app.closest('.dualBreakInputs');
                    let widget=document.getElementById(appId+'Widget');
                    if(!host){
                      host=document.createElement('div');host.className='dualBreakInputs';
                      const parent=app.parentElement;parent.insertBefore(host,app);
                      const aLabel=document.createElement('div');aLabel.className='dualLabel';aLabel.textContent='Application';
                      const wLabel=document.createElement('div');wLabel.className='dualLabel';wLabel.textContent='Widget';
                      host.append(aLabel,app,wLabel);
                      widget=document.createElement('input');
                      widget.id=appId+'Widget';widget.type='text';widget.maxLength=35;widget.placeholder='Vide = même intitulé';
                      host.appendChild(widget);
                    }
                    if(widget&&document.activeElement!==widget)widget.value=String(adv[key]||'');
                    if(widget&&!widget.__labelV11){
                      widget.__labelV11=true;
                      const persist=()=>{
                        const a=loadAdv();a[key]=String(widget.value||'').trim();saveAdv(a);
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
                  if(modal&&!modal.__widgetLabelsV11){
                    modal.__widgetLabelsV11=true;
                    new MutationObserver(()=>{
                      if(modal.classList.contains('show'))setTimeout(()=>{fillCourseWidgetField();wrapCourseSubmit()},0);
                    }).observe(modal,{attributes:true,attributeFilter:['class']});
                  }
                }

                const grid=document.getElementById('weekGrid');
                if(grid){
                  new MutationObserver(muts=>{
                    if(internal)return;
                    const relevant=muts.some(m=>{
                      const nodes=[...m.addedNodes,...m.removedNodes];
                      if(!nodes.length)return false;
                      return nodes.some(n=>!(n.nodeType===1&&(n.id==='finalWeekNowRail'||n.id==='finalWeekNowDot'||n.classList?.contains('nativeLunchLabel'))));
                    });
                    if(relevant)scheduleGrid();
                  }).observe(grid,{childList:true,subtree:true});
                  if(window.ResizeObserver)new ResizeObserver(scheduleGrid).observe(grid);
                }

                window.addEventListener('resize',scheduleGrid);
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)scheduleGrid()});
                setInterval(scheduleGrid,60000);
                if(document.fonts&&document.fonts.ready)document.fonts.ready.then(scheduleGrid);

                installLabelUi();
                scheduleGrid();
                setTimeout(()=>{moveStyleLast();scheduleGrid()},120);
                setTimeout(()=>{moveStyleLast();scheduleGrid()},350);
              }catch(e){console.log('Week geometry V11',e)}
            })();
            """;
    }
}
