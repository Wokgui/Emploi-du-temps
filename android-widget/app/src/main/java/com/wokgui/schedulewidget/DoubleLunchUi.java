package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekGeometryV14){
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                  return;
                }
                window.__weekGeometryV14=true;
                document.documentElement.setAttribute('data-week-geometry','V14');

                const style=document.createElement('style');
                style.id='weekGeometryV14Style';
                style.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:49px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  }

                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,
                  #weekGrid #weekNowRailV8,#weekGrid #weekNowDotV8,
                  #weekGrid #finalWeekNowRailV12,#weekGrid #finalWeekNowDotV12,
                  #weekGrid #finalWeekNowRailV13,#weekGrid #finalWeekNowDotV13,
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,
                  #weekGrid .geoLunchLabel{display:none!important}

                  html body #weekGrid#weekGrid .wc.lunchCell,
                  html body #weekGrid#weekGrid .wc.dynamicLunchCell,
                  html body #weekGrid#weekGrid .wc.nativeLunchCell,
                  html body #weekGrid#weekGrid .wc.lunchCell:not(.dynamicLunchCell){
                    box-sizing:border-box!important;
                    margin:0!important;
                    padding:0!important;
                    border-left:0!important;
                    border-top:0!important;
                    border-radius:0!important;
                    outline:0!important;
                    box-shadow:none!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    background-clip:border-box!important;
                    color:var(--ft-midi-ink,#22283A)!important;
                    overflow:hidden!important;
                    position:relative!important;
                  }
                  html body #weekGrid#weekGrid .nativeLunchLabel{
                    position:absolute!important;inset:0!important;
                    display:flex!important;align-items:center!important;justify-content:center!important;
                    gap:4px!important;margin:0!important;padding:2px 3px!important;
                    border:0!important;outline:0!important;box-shadow:none!important;background:transparent!important;
                    box-sizing:border-box!important;white-space:nowrap!important;overflow:hidden!important;
                    color:var(--ft-midi-ink,#22283A)!important;font-weight:850!important;font-size:.60rem!important;line-height:1!important;
                    pointer-events:none!important;z-index:3!important;
                  }
                  html body #weekGrid#weekGrid .nativeLunchIcon{font-size:.84em!important;line-height:1!important}

                  #weekGrid .nativeNowFull,
                  #weekGrid .nativeNowPartial{
                    position:absolute!important;
                    left:-1.5px!important;
                    width:2px!important;
                    background:#1688F4!important;
                    z-index:140!important;
                    pointer-events:none!important;
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                  }
                  #weekGrid .nativeNowFull{top:-1px!important;bottom:-1px!important}
                  #weekGrid .nativeNowPartial{top:-1px!important}
                  #weekGrid .nativeNowDot{
                    position:absolute!important;
                    left:-.5px!important;
                    width:16px!important;height:16px!important;border-radius:50%!important;
                    transform:translate(-50%,-50%)!important;
                    background:#1688F4!important;border:4px solid #D9ECFF!important;
                    box-sizing:border-box!important;box-shadow:0 1px 4px #0B6ACB38!important;
                    z-index:141!important;pointer-events:none!important;
                  }
                  #weekGrid .wc{position:relative!important}
                  html body #weekGrid#weekGrid .wc.nativeNowTrackCell{overflow:visible!important;z-index:5!important}

                  .dualBreakInputs{display:grid;grid-template-columns:72px minmax(0,1fr);gap:5px 7px;align-items:center}
                  .dualBreakInputs .dualLabel{font-size:.66rem;color:var(--muted);font-weight:750}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important;align-items:start!important}
                  #courseWidgetLabelField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                let raf=0;
                let lateTimer=0;
                let syncing=false;
                let gridObserver=null;

                function keepStyleLast(){}

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
                  if(!grid)return rows;
                  const times=Array.from(grid.querySelectorAll(':scope > .wh.timecol'));
                  for(const time of times){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(found.length<2)continue;
                    const cells=[];
                    const dayCount=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.length:5;
                    let n=time.nextElementSibling;
                    while(n&&cells.length<dayCount){
                      if(n.classList&&n.classList.contains('wc'))cells.push(n);
                      n=n.nextElementSibling;
                    }
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);
                  return rows;
                }

                function clearMidiEdges(grid){
                  grid.querySelectorAll('[data-midi-edge-v14="1"]').forEach(el=>{
                    el.style.removeProperty('border-right-color');
                    el.style.removeProperty('border-bottom-color');
                    el.removeAttribute('data-midi-edge-v14');
                  });
                }

                function edge(el,prop,color){
                  if(!el)return;
                  el.style.setProperty(prop,color,'important');
                  el.setAttribute('data-midi-edge-v14','1');
                }

                function normalizeLunch(cell,label){
                  if(!cell)return;
                  cell.classList.add('nativeLunchCell');
                  cell.querySelectorAll('.geoLunchLabel').forEach(x=>x.remove());
                  const holder=cell.querySelector('.dynamicLunchOverlay')||cell;
                  let text=holder.querySelector('.cellLabel');
                  if(label){
                    if(!text){text=document.createElement('span');text.className='cellLabel breakFitLabel';holder.appendChild(text)}
                    if(text.textContent!==label)text.textContent=label;
                  }else if(text){text.remove()}
                  cell.style.setProperty('background','var(--ft-midi)','important');
                  cell.style.setProperty('box-shadow','none','important');
                  cell.style.setProperty('outline','0','important');
                  cell.style.setProperty('border-left','0','important');
                  cell.style.setProperty('border-top','0','important');
                  cell.style.setProperty('border-radius','0','important');
                  cell.style.setProperty('padding','0','important');
                  cell.style.setProperty('margin','0','important');
                }

                function paintLunch(grid,rows){
                  const label=lunchText();
                  clearMidiEdges(grid);
                  rows.forEach(row=>row.cells.forEach(cell=>{
                    if(!(cell.classList.contains('lunchCell')||cell.classList.contains('dynamicLunchCell')||cell.classList.contains('nativeLunchCell')))return;
                    normalizeLunch(cell,label);
                    cell.style.removeProperty('border-right-color');
                    cell.style.removeProperty('border-bottom-color');
                    cell.style.setProperty('box-shadow','none','important');
                    cell.style.setProperty('outline','0','important');
                  }));
                }

                function removeLegacyNow(grid){
                  grid.querySelectorAll('[id*="weekNow"],[id*="WeekNow"],.scheduleNowRail,.scheduleNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('.nativeNowFull,.nativeNowPartial,.nativeNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('.nativeNowTrackCell').forEach(x=>x.classList.remove('nativeNowTrackCell'));
                }

                function paintNow(grid,rows){
                  // 6.9: the final layer owns the only current-time indicator.
                  removeLegacyNow(grid);
                }

                function observeGrid(grid){
                  if(gridObserver)gridObserver.disconnect();
                  gridObserver=new MutationObserver(()=>{if(!syncing)scheduleGrid()});
                  gridObserver.observe(grid,{childList:true,subtree:false});
                }

                function syncGrid(){
                  const grid=document.getElementById('weekGrid');if(!grid||syncing)return;
                  syncing=true;
                  try{
                    keepStyleLast();
                    grid.style.setProperty('position','relative','important');
                    const rows=rowsOf(grid);
                    if(rows.length){paintLunch(grid,rows);paintNow(grid,rows)}
                    observeGrid(grid);
                  }finally{syncing=false}
                }

                function scheduleGrid(){
                  if(raf)cancelAnimationFrame(raf);
                  if(lateTimer)clearTimeout(lateTimer);
                  syncGrid();
                }
                window.refreshDoubleLunchUi=scheduleGrid;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function keyFor(week,day,start,end){return String(week||'A')+'|'+String(day||2)+'|'+String(start||'')+'|'+String(end||'')}

                function ensureCourseWidgetField(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseWidgetLabelField');if(field)return;
                  field=document.createElement('div');field.id='courseWidgetLabelField';field.className='field';
                  field.innerHTML='<label>Intitulé dans le widget (facultatif)</label><input id="fWidgetLabel" type="text" maxlength="80" placeholder="Vide = même intitulé que dans l\'application">';
                  const app=document.getElementById('fLabel'),appField=app?app.closest('.field'):null;
                  if(appField&&appField.nextSibling)appField.parentNode.insertBefore(field,appField.nextSibling);
                  else form.insertBefore(field,form.querySelector('.sheetActions'));
                }

                function editedCourse(){
                  try{if(typeof editing==='undefined'||editing==null)return null;return weeks[activeWeek][selected].courses[editing]||null}catch(e){return null}
                }

                function fillCourseWidgetField(){
                  ensureCourseWidgetField();const input=document.getElementById('fWidgetLabel');if(!input)return;
                  const c=editedCourse();if(!c){input.value='';return}
                  const a=loadAdv(),map=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                  input.value=String(map[keyFor(activeWeek,selected,c.start,c.end)]||'');
                }

                function wrapCourseSubmit(){
                  ensureCourseWidgetField();
                  const form=document.getElementById('courseForm');if(!form||!form.onsubmit||form.onsubmit.__widgetLabelsV14)return;
                  const old=form.onsubmit;
                  const wrapped=function(e){
                    const c=editedCourse(),oldKey=c?keyFor(activeWeek,selected,c.start,c.end):null;
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A',day=typeof selected!=='undefined'?selected:2;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0);let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}
                    else if(c){start=c.start;end=c.end}else if(typeof newPrefill!=='undefined'&&newPrefill){start=newPrefill.start;end=newPrefill.end}
                    const widgetLabel=String((document.getElementById('fWidgetLabel')||{}).value||'').trim();
                    const result=old.call(this,e);
                    try{
                      const a=loadAdv();a.widgetCourseLabels=(a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object')?a.widgetCourseLabels:{};
                      if(oldKey)delete a.widgetCourseLabels[oldKey];const newKey=keyFor(week,day,start,end);
                      if(widgetLabel)a.widgetCourseLabels[newKey]=widgetLabel;else delete a.widgetCourseLabels[newKey];saveAdv(a);
                    }catch(ex){}
                    return result;
                  };
                  wrapped.__widgetLabelsV14=true;form.onsubmit=wrapped;
                }

                function installBreakWidgetInputs(){
                  const defs=[['gapLabel','gapWidgetLabel'],['lunchLabel','lunchWidgetLabel']];const adv=loadAdv();
                  for(const [appId,key] of defs){
                    const app=document.getElementById(appId);if(!app)continue;
                    let host=app.closest('.dualBreakInputs'),widget=document.getElementById(appId+'Widget');
                    if(!host){
                      host=document.createElement('div');host.className='dualBreakInputs';const parent=app.parentElement;parent.insertBefore(host,app);
                      const aLabel=document.createElement('div');aLabel.className='dualLabel';aLabel.textContent='Application';
                      const wLabel=document.createElement('div');wLabel.className='dualLabel';wLabel.textContent='Widget';host.append(aLabel,app,wLabel);
                      widget=document.createElement('input');widget.id=appId+'Widget';widget.type='text';widget.maxLength=35;widget.placeholder='Vide = même intitulé';host.appendChild(widget);
                    }
                    if(widget&&document.activeElement!==widget)widget.value=String(adv[key]||'');
                    if(widget&&!widget.__labelV14){
                      widget.__labelV14=true;const persist=()=>{const a=loadAdv();a[key]=String(widget.value||'').trim();saveAdv(a)};
                      widget.addEventListener('change',persist);widget.addEventListener('blur',persist);
                    }
                  }
                }

                function installLabelUi(){
                  ensureCourseWidgetField();wrapCourseSubmit();installBreakWidgetInputs();
                  const modal=document.getElementById('modal');
                  if(modal&&!modal.__widgetLabelsV14){
                    modal.__widgetLabelsV14=true;
                    new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{fillCourseWidgetField();wrapCourseSubmit()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});
                  }
                }

                const grid=document.getElementById('weekGrid');
                if(grid){observeGrid(grid);if(window.ResizeObserver)new ResizeObserver(scheduleGrid).observe(grid)}
                window.addEventListener('resize',scheduleGrid);
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)scheduleGrid()});
                if(document.fonts&&document.fonts.ready)document.fonts.ready.then(scheduleGrid);

                installLabelUi();
                scheduleGrid();
                syncGrid();
                setInterval(()=>{if(window.paintWeek69)window.paintWeek69()},30000);
              }catch(e){console.log('Week geometry V14',e)}
            })();
            """;
    }
}
