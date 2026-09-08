package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekGeometryV12){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__weekGeometryV12=true;

                const style=document.createElement('style');
                style.id='weekGeometryV12Style';
                style.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:49px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  }

                  /* Une seule géométrie : tous les anciens overlays et marqueurs sont neutralisés. */
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,
                  #weekGrid #weekNowRailV8,#weekGrid #weekNowDotV8,
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,
                  #weekGrid .geoLunchLabel,#weekGrid .dynamicLunchOverlay{display:none!important}

                  /* Midi EST la cellule de grille : aucune boîte interne, aucun outline, aucune ombre. */
                  html body #weekGrid .wc.lunchCell,
                  html body #weekGrid .wc.dynamicLunchCell,
                  html body #weekGrid .wc.nativeLunchCell{
                    box-sizing:border-box!important;
                    margin:0!important;
                    padding:2px!important;
                    border-radius:0!important;
                    outline:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    color:var(--ft-midi-ink,#22283A)!important;
                    overflow:hidden!important;
                    position:relative!important;
                    z-index:auto!important;
                  }
                  html body #weekGrid .wc.nativeLunchCell>*{visibility:visible!important}
                  html body #weekGrid .nativeLunchLabel{
                    position:absolute!important;inset:0!important;
                    display:flex!important;align-items:center!important;justify-content:center!important;
                    gap:4px!important;margin:0!important;padding:2px 3px!important;
                    border:0!important;outline:0!important;box-shadow:none!important;background:transparent!important;
                    box-sizing:border-box!important;white-space:nowrap!important;overflow:hidden!important;
                    color:var(--ft-midi-ink,#22283A)!important;font-weight:850!important;font-size:.60rem!important;line-height:1!important;
                    pointer-events:none!important;
                  }
                  html body #weekGrid .nativeLunchLabel .nativeLunchIcon{font-size:.84em!important;line-height:1!important}

                  /* Marqueur temporel unique, positionné dans le même repère que les cellules. */
                  #weekGrid #finalWeekNowRailV12{
                    position:absolute!important;z-index:140!important;width:2px!important;background:#1688F4!important;
                    pointer-events:none!important;display:none;border-radius:0!important;transform:none!important;
                  }
                  #weekGrid #finalWeekNowDotV12{
                    position:absolute!important;z-index:141!important;width:16px!important;height:16px!important;border-radius:50%!important;
                    background:#1688F4!important;border:4px solid #D9ECFF!important;box-sizing:border-box!important;
                    box-shadow:0 1px 4px #0B6ACB38!important;pointer-events:none!important;display:none;transform:none!important;
                  }

                  .dualBreakInputs{display:grid;grid-template-columns:72px minmax(0,1fr);gap:5px 7px;align-items:center}
                  .dualBreakInputs .dualLabel{font-size:.66rem;color:var(--muted);font-weight:750}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important;align-items:start!important}
                  #courseWidgetLabelField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                let raf=0;
                let lateTimer=0;
                let gridObserver=null;

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

                function clearNativeEdges(grid){
                  grid.querySelectorAll('[data-native-midi-edge="1"]').forEach(el=>{
                    el.style.removeProperty('border-right-color');
                    el.style.removeProperty('border-bottom-color');
                    el.removeAttribute('data-native-midi-edge');
                  });
                }

                function paintEdge(el,prop,color){
                  if(!el)return;
                  /* L'épaisseur et le style restent ceux de la grille : seule la couleur change. */
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
                  /* Aucune géométrie n'est ajoutée ici : fond seulement. */
                  cell.style.setProperty('background','var(--ft-midi)','important');
                  cell.style.setProperty('box-shadow','none','important');
                  cell.style.setProperty('outline','0','important');
                  cell.style.setProperty('border-radius','0','important');
                  cell.style.setProperty('margin','0','important');
                }

                function paintLunch(grid,rows){
                  const border=(getComputedStyle(document.documentElement).getPropertyValue('--ft-midi-border')||'#CBBE9E').trim();
                  const label=lunchText();
                  clearNativeEdges(grid);

                  rows.forEach((row,rowIndex)=>{
                    row.cells.forEach((cell,dayIndex)=>{
                      const isLunch=cell.classList.contains('lunchCell')||cell.classList.contains('dynamicLunchCell')||cell.classList.contains('nativeLunchCell');
                      if(!isLunch)return;
                      normalizeLunchCell(cell,label);

                      /* Bord droit et bas = bordures natives de CETTE cellule. */
                      paintEdge(cell,'border-right-color',border);
                      paintEdge(cell,'border-bottom-color',border);

                      /* Bord gauche = border-right de la cellule précédente, exactement sur la ligne de grille. */
                      const leftNeighbor=cell.previousElementSibling;
                      paintEdge(leftNeighbor,'border-right-color',border);

                      /* Bord haut = border-bottom de la cellule située juste au-dessus. */
                      if(rowIndex>0)paintEdge(rows[rowIndex-1].cells[dayIndex],'border-bottom-color',border);
                      else paintEdge(grid.querySelectorAll(':scope > .wh.day')[dayIndex],'border-bottom-color',border);
                    });
                  });
                }

                function ensureNowLayers(grid){
                  let rail=document.getElementById('finalWeekNowRailV12');
                  let dot=document.getElementById('finalWeekNowDotV12');
                  if(!rail){rail=document.createElement('div');rail.id='finalWeekNowRailV12';grid.appendChild(rail)}
                  if(!dot){dot=document.createElement('div');dot.id='finalWeekNowDotV12';grid.appendChild(dot)}
                  return {rail,dot};
                }

                function paintNow(grid,rows){
                  const {rail,dot}=ensureNowLayers(grid);
                  rail.style.display='none';dot.style.display='none';
                  const now=new Date(),day=now.getDay();
                  if(day<1||day>5||!rows.length)return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}

                  const minute=now.getHours()*60+now.getMinutes();
                  let row=null;
                  for(const r of rows){if(minute>=r.start&&minute<r.end){row=r;break}}
                  if(!row)return;

                  const dayIndex=day-1;
                  const first=rows[0].cells[dayIndex];
                  const target=row.cells[dayIndex];
                  if(!first||!target)return;
                  const frac=Math.max(0,Math.min(1,(minute-row.start)/Math.max(1,row.end-row.start)));

                  /* offsetLeft/offsetTop ont exactement le même repère que left/top d'un enfant absolute du grid.
                     La ligne grise appartient au border-right de la cellule précédente : son centre est x - 0,5px. */
                  const x=target.offsetLeft;
                  const boundaryCenter=x-0.5;
                  const top=first.offsetTop;
                  const y=target.offsetTop+target.offsetHeight*frac;

                  rail.style.setProperty('left',(boundaryCenter-1)+'px','important');
                  rail.style.setProperty('top',top+'px','important');
                  rail.style.setProperty('height',Math.max(2,y-top)+'px','important');
                  rail.style.setProperty('display','block','important');

                  dot.style.setProperty('left',(boundaryCenter-8)+'px','important');
                  dot.style.setProperty('top',(y-8)+'px','important');
                  dot.style.setProperty('display','block','important');
                }

                function observeGrid(grid){
                  if(gridObserver)gridObserver.disconnect();
                  gridObserver=new MutationObserver(()=>scheduleGrid());
                  /* Le vrai render() remplace des enfants directs du grid. Nos changements internes ne rebouclent donc pas. */
                  gridObserver.observe(grid,{childList:true,subtree:false});
                }

                function syncGrid(){
                  const grid=document.getElementById('weekGrid');
                  if(!grid)return;
                  keepStyleLast();
                  grid.style.setProperty('position','relative','important');
                  const rows=rowsOf(grid);
                  if(!rows.length){observeGrid(grid);return}
                  paintLunch(grid,rows);
                  paintNow(grid,rows);
                  observeGrid(grid);
                }

                function scheduleGrid(){
                  if(raf)cancelAnimationFrame(raf);
                  if(lateTimer)clearTimeout(lateTimer);
                  raf=requestAnimationFrame(()=>requestAnimationFrame(syncGrid));
                  /* Un second passage après les autres couches injectées garantit la géométrie finale sans clignotement visible. */
                  lateTimer=setTimeout(syncGrid,90);
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
                  field=document.createElement('div');field.id='courseWidgetLabelField';field.className='field';
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
                  if(!form||!form.onsubmit||form.onsubmit.__widgetLabelsV12)return;
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
                      if(widgetLabel)a.widgetCourseLabels[newKey]=widgetLabel;else delete a.widgetCourseLabels[newKey];
                      saveAdv(a);
                    }catch(ex){}
                    return result;
                  };
                  wrapped.__widgetLabelsV12=true;form.onsubmit=wrapped;
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
                      widget=document.createElement('input');widget.id=appId+'Widget';widget.type='text';widget.maxLength=35;widget.placeholder='Vide = même intitulé';
                      host.appendChild(widget);
                    }
                    if(widget&&document.activeElement!==widget)widget.value=String(adv[key]||'');
                    if(widget&&!widget.__labelV12){
                      widget.__labelV12=true;
                      const persist=()=>{const a=loadAdv();a[key]=String(widget.value||'').trim();saveAdv(a)};
                      widget.addEventListener('change',persist);widget.addEventListener('blur',persist);
                    }
                  }
                }

                function installLabelUi(){
                  ensureCourseWidgetField();wrapCourseSubmit();installBreakWidgetInputs();
                  const modal=document.getElementById('modal');
                  if(modal&&!modal.__widgetLabelsV12){
                    modal.__widgetLabelsV12=true;
                    new MutationObserver(()=>{
                      if(modal.classList.contains('show'))setTimeout(()=>{fillCourseWidgetField();wrapCourseSubmit()},0);
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
                setTimeout(scheduleGrid,35);
                setTimeout(scheduleGrid,160);
                setInterval(()=>{if(document.visibilityState!=='hidden')paintNow(document.getElementById('weekGrid'),rowsOf(document.getElementById('weekGrid')))},60000);

                installLabelUi();
                scheduleGrid();
              }catch(e){console.log('Week geometry V12',e)}
            })();
            """;
    }
}
