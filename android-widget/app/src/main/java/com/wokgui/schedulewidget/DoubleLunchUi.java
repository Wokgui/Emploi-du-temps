package com.wokgui.schedulewidget;

final class DoubleLunchUi {
    private DoubleLunchUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__gridGeometryV6){
                  if(window.refreshDisplayLabelsUi)window.refreshDisplayLabelsUi();
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi(true);
                  return;
                }
                window.__gridGeometryV6=true;

                const style=document.createElement('style');
                style.textContent=`
                  html body #viewWeek #weekGrid .wh,
                  html body #viewWeek #weekGrid .wc{min-height:48px!important}
                  @media(max-width:560px){
                    html body #viewWeek #weekGrid .wh,
                    html body #viewWeek #weekGrid .wc{min-height:47px!important}
                  }

                  /* Aucun état géométrique intermédiaire n'est montré. */
                  #weekGrid.geometryPending{visibility:hidden!important}

                  /* Les anciens marqueurs restent dans le DOM pour compatibilité mais ne sont
                     plus dessinés : le rail est désormais porté directement par les cellules. */
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot{
                    visibility:hidden!important;opacity:0!important;pointer-events:none!important
                  }

                  #weekGrid .weekTodayRailCell{position:relative!important}
                  #weekGrid .weekTodayRailCell::before{
                    content:'';position:absolute;left:-1px;top:0;bottom:0;width:2px;
                    background:#248cf1;z-index:45;pointer-events:none
                  }
                  #weekGrid #gridNowDotV6{
                    position:absolute;width:12px;height:12px;border-radius:50%;
                    background:#0877f9;border:3px solid #d9ecff;
                    box-sizing:content-box;transform:translate(-50%,-50%);
                    z-index:60;pointer-events:none;box-shadow:0 1px 2px #0b5fa533
                  }

                  /* Midi est une vraie surface rectangulaire mesurée sur les cellules du tableau.
                     Le cadre est peint par-dessus le fond, sans arrondi ni coin transparent. */
                  #weekGrid .lunchSurfaceCell,
                  #weekGrid .dynamicLunchContinuation{
                    position:relative!important;background:transparent!important;
                    color:var(--ft-midi-ink)!important;border-radius:0!important;
                    box-shadow:none!important;z-index:22!important
                  }
                  #weekGrid .lunchSurfaceCell>*{position:relative;z-index:28}
                  #weekGrid .dynamicLunchContinuation>*{visibility:hidden!important}
                  #weekGrid .dynamicLunchOverlay{
                    position:absolute!important;z-index:30!important;display:flex!important;
                    align-items:center!important;justify-content:center!important;
                    margin:0!important;padding:0!important;background:transparent!important;
                    border:0!important;border-radius:0!important;box-shadow:none!important;
                    color:var(--ft-midi-ink)!important;pointer-events:none!important;
                    box-sizing:border-box!important
                  }
                  #weekGrid .lunchSegmentSurfaceV6{
                    position:absolute;z-index:18;pointer-events:none;
                    background:var(--ft-midi)!important;
                    border:1px solid var(--ft-midi-border)!important;
                    border-radius:0!important;box-sizing:border-box!important;
                    background-clip:border-box!important
                  }

                  /* Deux intitulés distincts : application / widget. */
                  .breakSettings .breakWidgetLegendV6{
                    display:grid;grid-template-columns:64px minmax(0,1fr) minmax(0,1fr);
                    gap:7px;padding:5px 8px 3px;color:var(--muted);font-size:.62rem;
                    font-weight:800;border-bottom:1px solid #eef1f5
                  }
                  html body .breakSettings .breakRow{
                    grid-template-columns:64px minmax(0,1fr) minmax(0,1fr)!important
                  }
                  .breakSettings .widgetBreakLabelV6{min-width:0;width:100%;padding:7px 8px;
                    border:1px solid var(--line);border-radius:6px;color:var(--ink);
                    background:#fff;font-size:.76rem}
                  #courseWidgetLabelFieldV6 .fieldHintV6{margin-top:3px;color:var(--muted);font-size:.63rem}
                `;
                document.head.appendChild(style);

                let frame=0,lateTimer=0,ignoreMutationsUntil=0,labelSaveTimer=0;

                function toMin(v){
                  const p=String(v||'').split(':').map(Number);
                  return (p[0]||0)*60+(p[1]||0);
                }
                function clean(v,max){return String(v==null?'':v).trim().slice(0,max||40)}
                function labelKey(v){return clean(v,80).toLocaleLowerCase('fr-FR')}
                function loadAdv(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}
                }
                function saveAdv(o){
                  try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o||{}))}catch(e){}
                }

                /* ---------- Intitulés application / widget ---------- */
                function installBreakWidgetLabels(){
                  const box=document.querySelector('.breakSettings');
                  if(!box)return;
                  let legend=box.querySelector('.breakWidgetLegendV6');
                  if(!legend){
                    legend=document.createElement('div');legend.className='breakWidgetLegendV6';
                    legend.innerHTML='<span></span><span>Application</span><span>Widget</span>';
                    box.insertBefore(legend,box.firstChild);
                  }
                  const adv=loadAdv();
                  const defs=[
                    ['gapLabel','gapWidgetLabelV6','widgetGapLabel'],
                    ['lunchLabel','lunchWidgetLabelV6','widgetLunchLabel']
                  ];
                  defs.forEach(([appId,widgetId,key])=>{
                    const app=document.getElementById(appId);if(!app)return;
                    const row=app.closest('.breakRow');if(!row)return;
                    let input=document.getElementById(widgetId);
                    if(!input){
                      input=document.createElement('input');input.type='text';input.id=widgetId;
                      input.maxLength=28;input.className='widgetBreakLabelV6';
                      input.placeholder='Même intitulé';row.appendChild(input);
                    }
                    if(document.activeElement!==input)input.value=clean(adv[key]||'',28);
                    if(!input.dataset.boundV6){
                      input.dataset.boundV6='1';
                      input.addEventListener('input',queueWidgetBreakSave);
                      input.addEventListener('change',saveWidgetBreakLabels);
                      input.addEventListener('blur',saveWidgetBreakLabels);
                    }
                  });
                }
                function saveWidgetBreakLabels(){
                  if(labelSaveTimer){clearTimeout(labelSaveTimer);labelSaveTimer=0}
                  const a=loadAdv();
                  const g=document.getElementById('gapWidgetLabelV6');
                  const l=document.getElementById('lunchWidgetLabelV6');
                  if(g)a.widgetGapLabel=clean(g.value,28);
                  if(l)a.widgetLunchLabel=clean(l.value,28);
                  saveAdv(a);
                }
                function queueWidgetBreakSave(){
                  if(labelSaveTimer)clearTimeout(labelSaveTimer);
                  labelSaveTimer=setTimeout(saveWidgetBreakLabels,220);
                }

                function ensureCourseWidgetField(){
                  const form=document.getElementById('courseForm');
                  const appInput=document.getElementById('fLabel');
                  if(!form||!appInput)return;
                  const appField=appInput.closest('.field');
                  if(appField){const lab=appField.querySelector('label');if(lab)lab.textContent='Intitulé dans l’application'}
                  let field=document.getElementById('courseWidgetLabelFieldV6');
                  if(!field){
                    field=document.createElement('div');field.id='courseWidgetLabelFieldV6';field.className='field';
                    field.innerHTML='<label>Intitulé dans le widget</label><input id="fWidgetLabelV6" type="text" maxlength="60" placeholder="Même intitulé que dans l’application"><div class="fieldHintV6">Laisser vide pour reprendre automatiquement l’intitulé de l’application.</div>';
                    if(appField&&appField.nextSibling)appField.parentNode.insertBefore(field,appField.nextSibling);
                    else form.insertBefore(field,form.querySelector('.sheetActions'));
                  }
                }
                function currentEditedCourse(){
                  try{
                    if(typeof editing==='undefined'||editing==null||typeof weeks==='undefined')return null;
                    return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null;
                  }catch(e){return null}
                }
                function syncCourseWidgetField(){
                  ensureCourseWidgetField();
                  const input=document.getElementById('fWidgetLabelV6');if(!input)return;
                  const c=currentEditedCourse(),a=loadAdv();
                  const map=a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object'?a.widgetCourseLabels:{};
                  input.value=c?clean(map[labelKey(c.label)]||'',60):'';
                }
                function saveCourseWidgetLabel(oldLabel,newLabel,widgetLabel){
                  const a=loadAdv();
                  const map=a.widgetCourseLabels&&typeof a.widgetCourseLabels==='object'?a.widgetCourseLabels:{};
                  const oldKey=labelKey(oldLabel),newKey=labelKey(newLabel),value=clean(widgetLabel,60);
                  if(oldKey&&oldKey!==newKey)delete map[oldKey];
                  if(newKey){if(value)map[newKey]=value;else delete map[newKey]}
                  a.widgetCourseLabels=map;saveAdv(a);
                }
                function wrapCourseSubmitForWidgetLabel(){
                  ensureCourseWidgetField();
                  const form=document.getElementById('courseForm');
                  if(!form||!form.onsubmit||form.onsubmit.__widgetLabelV6)return;
                  const old=form.onsubmit;
                  const wrapped=function(e){
                    const current=currentEditedCourse();
                    const oldLabel=current?current.label:'';
                    const newLabel=clean((document.getElementById('fLabel')||{}).value||'',80);
                    const widgetLabel=clean((document.getElementById('fWidgetLabelV6')||{}).value||'',60);
                    const out=old.call(this,e);
                    setTimeout(()=>saveCourseWidgetLabel(oldLabel,newLabel,widgetLabel),0);
                    return out;
                  };
                  wrapped.__widgetLabelV6=true;form.onsubmit=wrapped;
                }
                function installDisplayLabelsUi(){
                  installBreakWidgetLabels();
                  ensureCourseWidgetField();
                  wrapCourseSubmitForWidgetLabel();
                }
                window.refreshDisplayLabelsUi=installDisplayLabelsUi;

                const modal=document.getElementById('modal');
                if(modal)new MutationObserver(()=>{
                  if(modal.classList.contains('show'))setTimeout(()=>{
                    ensureCourseWidgetField();syncCourseWidgetField();wrapCourseSubmitForWidgetLabel();
                  },0);
                }).observe(modal,{attributes:true,attributeFilter:['class']});

                /* ---------- Géométrie semaine ---------- */
                function resetGeometry(grid){
                  grid.querySelectorAll('.lunchSegmentSurfaceV6').forEach(x=>x.remove());
                  grid.querySelectorAll('.lunchSurfaceCell').forEach(x=>x.classList.remove('lunchSurfaceCell'));
                  grid.querySelectorAll('.dynamicLunchContinuation').forEach(x=>x.classList.remove('dynamicLunchContinuation'));
                  grid.querySelectorAll('.weekTodayRailCell').forEach(x=>x.classList.remove('weekTodayRailCell'));
                  const dot=document.getElementById('gridNowDotV6');if(dot)dot.remove();
                  grid.querySelectorAll('.dynamicLunchCell').forEach(cell=>{
                    cell.removeAttribute('data-double-lunch');
                    cell.style.removeProperty('z-index');cell.style.removeProperty('overflow');
                    const overlay=cell.querySelector('.dynamicLunchOverlay');
                    if(overlay){['top','left','right','bottom','width','height'].forEach(p=>overlay.style.removeProperty(p))}
                  });
                }

                function makeLunchSurface(grid,cells){
                  if(!cells||!cells.length)return;
                  const first=cells[0],last=cells[cells.length-1];
                  cells.forEach(c=>c.classList.add('lunchSurfaceCell'));
                  const surface=document.createElement('div');surface.className='lunchSegmentSurfaceV6';
                  surface.style.left=first.offsetLeft+'px';
                  surface.style.top=first.offsetTop+'px';
                  surface.style.width=first.offsetWidth+'px';
                  surface.style.height=((last.offsetTop+last.offsetHeight)-first.offsetTop)+'px';
                  grid.appendChild(surface);
                  const overlay=first.querySelector('.dynamicLunchOverlay');
                  if(overlay){
                    overlay.style.setProperty('top','0px','important');overlay.style.setProperty('left','0px','important');
                    overlay.style.setProperty('right','0px','important');overlay.style.setProperty('bottom','auto','important');
                    overlay.style.setProperty('width','100%','important');
                    overlay.style.setProperty('height',surface.style.height,'important');
                  }
                }

                function syncLunchGeometry(grid){
                  if(typeof state==='undefined'||typeof DAYS==='undefined'||typeof slots==='undefined')return;
                  const kids=Array.from(grid.children).filter(x=>!x.classList.contains('lunchSegmentSurfaceV6'));
                  if(kids.length<12)return;
                  const secondStart=slots[4]&&slots[4].start?toMin(slots[4].start):13*60;
                  const secondEnd=slots[4]&&slots[4].end?toMin(slots[4].end):14*60;
                  let secondRow=-1;
                  for(let p=6;p+5<kids.length;p+=6){
                    const times=(kids[p].textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(times.length>=2&&toMin(times[0])===secondStart&&toMin(times[1])===secondEnd){secondRow=p;break}
                  }

                  Array.from(grid.querySelectorAll('.dynamicLunchCell')).forEach(cell=>{
                    const idx=kids.indexOf(cell),dayIndex=(idx%6)-1;if(dayIndex<0||dayIndex>=DAYS.length)return;
                    const segment=[cell];
                    if(secondRow>=0&&secondEnd>secondStart){
                      const day=state[DAYS[dayIndex]],courses=day&&Array.isArray(day.courses)?day.courses:[];
                      const occupied=courses.some(c=>toMin(c.start)<secondEnd&&toMin(c.end)>secondStart);
                      const hasAfter=courses.some(c=>toMin(c.start)>=secondEnd);
                      const next=kids[secondRow+1+dayIndex];
                      if(!occupied&&hasAfter&&next&&next.classList&&next.classList.contains('wc')){
                        next.classList.add('dynamicLunchContinuation');
                        cell.setAttribute('data-double-lunch','1');cell.style.setProperty('overflow','visible','important');
                        segment.push(next);
                      }
                    }
                    makeLunchSurface(grid,segment);
                  });
                  grid.querySelectorAll('.wc.lunchCell:not(.dynamicLunchCell)').forEach(cell=>makeLunchSurface(grid,[cell]));
                }

                function syncCurrentMarker(grid){
                  const legacyRail=document.getElementById('weekNowRail'),legacyDot=document.getElementById('weekNowDot');
                  if(legacyRail)legacyRail.style.setProperty('visibility','hidden','important');
                  if(legacyDot)legacyDot.style.setProperty('visibility','hidden','important');
                  grid.querySelectorAll('.weekTodayRailCell').forEach(x=>x.classList.remove('weekTodayRailCell'));
                  const oldDot=document.getElementById('gridNowDotV6');if(oldDot)oldDot.remove();

                  const now=new Date(),day=now.getDay();
                  if(day<1||day>5)return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}

                  const kids=Array.from(grid.children).filter(x=>!x.classList.contains('lunchSegmentSurfaceV6'));
                  if(kids.length<12)return;
                  const minute=now.getHours()*60+now.getMinutes();
                  let target=null,rowStart=-1,rowEnd=-1;
                  const dayCells=[];
                  for(let p=6;p+5<kids.length;p+=6){
                    const cell=kids[p+day];if(!cell||!cell.classList.contains('wc'))continue;
                    dayCells.push(cell);cell.classList.add('weekTodayRailCell');
                    const times=(kids[p].textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(times.length>=2){
                      const s=toMin(times[0]),e=toMin(times[1]);
                      if(minute>=s&&minute<=e){target=cell;rowStart=s;rowEnd=e}
                    }
                  }
                  if(!target||rowEnd<=rowStart)return;
                  const frac=Math.max(0,Math.min(1,(minute-rowStart)/(rowEnd-rowStart)));
                  const dot=document.createElement('div');dot.id='gridNowDotV6';
                  dot.style.left=target.offsetLeft+'px';
                  dot.style.top=(target.offsetTop+target.offsetHeight*frac)+'px';
                  grid.appendChild(dot);
                }

                function synchronize(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  ignoreMutationsUntil=performance.now()+100;
                  try{
                    grid.style.setProperty('position','relative','important');
                    resetGeometry(grid);syncLunchGeometry(grid);syncCurrentMarker(grid);installDisplayLabelsUi();
                  }finally{grid.classList.remove('geometryPending')}
                }
                function schedule(structural){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  if(structural)grid.classList.add('geometryPending');
                  if(frame)cancelAnimationFrame(frame);if(lateTimer)clearTimeout(lateTimer);
                  frame=requestAnimationFrame(()=>requestAnimationFrame(()=>{
                    synchronize();lateTimer=setTimeout(synchronize,110);
                  }));
                }
                window.refreshDoubleLunchUi=function(structural){schedule(structural!==false)};

                function wrap(name){
                  const fn=window[name];if(typeof fn!=='function'||fn.__geometryV6)return;
                  const wrapped=function(){
                    const grid=document.getElementById('weekGrid');if(grid)grid.classList.add('geometryPending');
                    const out=fn.apply(this,arguments);schedule(true);return out;
                  };
                  wrapped.__geometryV6=true;window[name]=wrapped;
                }
                wrap('render');wrap('renderWeek');

                const grid=document.getElementById('weekGrid');
                if(grid){
                  new MutationObserver(muts=>{
                    if(performance.now()<ignoreMutationsUntil)return;
                    const structural=muts.some(m=>m.type==='childList'&&m.target===grid);
                    schedule(structural);
                  }).observe(grid,{childList:true,subtree:true});
                  if(window.ResizeObserver)new ResizeObserver(()=>schedule(false)).observe(grid);
                }
                const breaksBox=document.querySelector('.breakSettings');
                if(breaksBox)new MutationObserver(()=>setTimeout(installDisplayLabelsUi,0)).observe(breaksBox,{childList:true,subtree:true});
                window.addEventListener('resize',()=>schedule(false));
                document.addEventListener('visibilitychange',()=>{if(!document.hidden)schedule(false)});
                setInterval(()=>schedule(false),60000);
                installDisplayLabelsUi();schedule(true);
              }catch(e){console.log('Grid geometry V6',e)}
            })();
            """;
    }
}
