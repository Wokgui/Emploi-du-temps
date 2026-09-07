package com.wokgui.schedulewidget;

final class LunchBreakUi {
    private LunchBreakUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__lunchBreakUiV9){
                  if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                  return;
                }
                window.__lunchBreakUiV9=true;
                const EMPTY_MARK=String.fromCharCode(8203);

                const style=document.createElement('style');
                style.textContent=`
                  #todayClock{display:none!important}
                  #todayList #todayNowTime{display:none!important}
                  #todayList .todayCourse{grid-template-columns:58px minmax(0,1fr) auto!important;gap:4px!important}
                  #todayList .todayCourse .time{text-align:center!important}
                  #todayList #todayNowRail{left:61px!important}
                  #todayList #todayNowDot{left:61px!important}
                  .breakSettings .badgeToggle{display:none!important}
                  .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr)!important}
                  #todayList .todayCourse.gap>.badge,#todayList .todayCourse.lunch>.badge{display:none!important}
                  #weekGrid .dynamicLunchCell{position:relative;overflow:hidden!important;background:#fff7e6!important;color:var(--lunch);z-index:2}
                  #weekGrid .dynamicLunchEmpty{background:#fff!important}
                  #weekGrid .dynamicLunchOverlay{position:absolute;inset:2px 1px;z-index:9;display:flex;align-items:center;justify-content:center;padding:2px 3px;border:1px solid #efc66f;border-radius:6px;background:linear-gradient(90deg,#fff0c8 0%,#ffe2a6 100%);color:#8d5810;box-shadow:0 1px 2px #8d581012;pointer-events:none;white-space:nowrap;overflow:hidden}
                  #weekGrid .breakFitLabel{display:block!important;max-width:100%;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;line-height:1!important;-webkit-line-clamp:1!important;-webkit-box-orient:initial!important;font-size:.58rem!important}
                  body.largeAppText #weekGrid .breakFitLabel{font-size:.60rem!important}
                  #weekGrid #weekNowRail{left:var(--week-now-x,39px)!important;width:2px!important;border-radius:2px!important;opacity:.78}
                  #weekGrid #weekNowDot{left:var(--week-now-x,39px)!important;width:11px!important;height:11px!important;transform:translate(-5px,-5px)!important}
                  .courseBadgePill{display:inline-flex;align-items:center;max-width:100%;margin-left:5px;padding:2px 6px;border-radius:999px;background:#e8f2ff;color:#0868c7;font-size:.60rem;font-weight:850;line-height:1.05;vertical-align:middle;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  #weekGrid .courseBadgePill{display:inline-block;margin:2px 0 0;padding:1px 4px;font-size:.46rem;max-width:95%}
                  #editList .courseBadgePill{margin-left:6px}
                  #courseBadgeField input{width:100%;border:1px solid var(--line);border-radius:7px;padding:9px;font:inherit;background:#fff;color:var(--ink)}
                `;
                document.head.appendChild(style);

                function visibleLabel(value){
                  return String(value==null?'':value).split(EMPTY_MARK).join('').trim().slice(0,28);
                }
                function storedLabel(value){
                  const v=visibleLabel(value);
                  return v||EMPTY_MARK;
                }
                function gapLabelText(){return visibleLabel(typeof breaks!=='undefined'&&breaks?breaks.gapLabel:'Trou')}
                function lunchLabelText(){return visibleLabel(typeof breaks!=='undefined'&&breaks?breaks.lunchLabel:'Midi')}
                window.gapLabelText=gapLabelText;
                window.lunchLabelText=lunchLabelText;

                function lunchBounds(){
                  try{
                    const start=(Array.isArray(slots)&&slots[3]&&slots[3].end)?min(slots[3].end):12*60;
                    const end=(Array.isArray(slots)&&slots[4]&&slots[4].start)?min(slots[4].start):13*60;
                    return {start,end};
                  }catch(e){return {start:12*60,end:13*60}}
                }
                window.lunchBounds=lunchBounds;

                function lunchForDay(list){
                  try{
                    const courses=(Array.isArray(list)?list:[]).slice().sort((a,b)=>min(a.start)-min(b.start));
                    if(!courses.length)return null;
                    const bounds=lunchBounds(),start=bounds.start,end=bounds.end;
                    if(end<=start)return null;
                    const hasBefore=courses.some(c=>min(c.end)<=start);
                    const hasAfter=courses.some(c=>min(c.start)>=end);
                    if(!hasBefore||!hasAfter)return null;
                    if(courses.some(c=>min(c.start)<end&&min(c.end)>start))return null;
                    return {start:clock(start),end:clock(end),startM:start,endM:end,duration:end-start};
                  }catch(e){return null}
                }
                window.lunchForDay=lunchForDay;

                function gapsForDay(list){
                  const out=[],courses=(Array.isArray(list)?list:[]).slice().sort((a,b)=>min(a.start)-min(b.start)),l=lunchForDay(courses);
                  const ls=l?l.startM:-1,le=l?l.endM:-1;
                  for(let i=0;i<courses.length-1;i++){
                    let a=min(courses[i].end),b=min(courses[i+1].start);if(b<=a)continue;
                    if(!l||b<=ls||a>=le)out.push({start:a,end:b});
                    else{
                      if(a<ls)out.push({start:a,end:Math.min(b,ls)});
                      if(b>le)out.push({start:Math.max(a,le),end:b});
                    }
                  }
                  return out.filter(g=>g.end>g.start);
                }
                window.dynamicGapSegments=gapsForDay;

                function dynamicWeekTimes(){
                  const map=new Map();
                  for(const s of slots)map.set(s.start+'|'+s.end,{start:s.start,end:s.end,type:'slot'});
                  for(const d of DAYS)for(const c of state[d].courses)map.set(c.start+'|'+c.end,{start:c.start,end:c.end,type:'course'});
                  const out=[...map.values()];
                  if(DAYS.some(d=>lunchForDay(state[d].courses))){
                    const b=lunchBounds();out.push({start:clock(b.start),end:clock(b.end),type:'lunchDynamic'});
                  }
                  return out.sort((a,b)=>min(a.start)-min(b.start)||((a.type==='lunchDynamic')?-1:0)-((b.type==='lunchDynamic')?-1:0)||min(a.end)-min(b.end));
                }

                function renderBreakSettingsV9(){
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  if(gap&&document.activeElement!==gap)gap.value=gapLabelText();
                  if(lunch&&document.activeElement!==lunch)lunch.value=lunchLabelText();
                }

                let persistTimer=null;
                function syncBreakStateFromControls(){
                  if(typeof breaks==='undefined'||!breaks)return;
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  if(gap)breaks.gapLabel=storedLabel(gap.value);
                  if(lunch)breaks.lunchLabel=storedLabel(lunch.value);
                  breaks.showGapBadge=false;
                  breaks.showLunchBadge=false;
                }
                function persistBreakState(){
                  try{
                    syncBreakStateFromControls();
                    if(window.AndroidSchedule&&AndroidSchedule.saveSchedule&&typeof exportState==='function')AndroidSchedule.saveSchedule(JSON.stringify(exportState()));
                    else if(typeof localStorage!=='undefined'&&typeof exportState==='function')localStorage.setItem('edt',JSON.stringify(exportState()));
                  }catch(e){}
                }
                function queuePersist(){
                  syncBreakStateFromControls();
                  syncBreakCells();
                  if(persistTimer)clearTimeout(persistTimer);
                  persistTimer=setTimeout(persistBreakState,100);
                }
                function applyBreakSettingsV9(){
                  syncBreakStateFromControls();
                  persistBreakState();
                  syncBreakCells();
                  setTimeout(()=>{renderBreakSettingsV9();fitBreakLabels();polishWeekNowMarker();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors()},15);
                }

                function wireBreakSettings(){
                  document.querySelectorAll('.breakSettings .badgeToggle').forEach(el=>el.remove());
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  for(const input of [gap,lunch])if(input){
                    input.maxLength=28;
                    input.placeholder='Laisser vide pour aucun texte';
                    if(!input.__breakV9){
                      input.__breakV9=true;
                      input.onchange=null;
                      input.addEventListener('input',queuePersist);
                      input.addEventListener('change',applyBreakSettingsV9);
                      input.addEventListener('blur',applyBreakSettingsV9);
                    }
                  }
                  if(typeof breaks!=='undefined'&&breaks){breaks.showGapBadge=false;breaks.showLunchBadge=false}
                  renderBreakSettingsV9();
                }

                function ensureBreakLabel(row,label,selector){
                  if(!row)return;
                  const holder=row.children.length>1?row.children[1]:row;
                  let el=row.querySelector(selector);
                  if(label){
                    if(!el){el=document.createElement('div');el.className=selector.indexOf('cellLabel')>=0?'cellLabel breakFitLabel':'label';if(holder.firstChild)holder.insertBefore(el,holder.firstChild);else holder.appendChild(el)}
                    if(el.textContent!==label)el.textContent=label;
                  }else if(el){el.remove()}
                }
                function syncBreakCells(){
                  try{
                    const g=gapLabelText(),l=lunchLabelText();
                    document.querySelectorAll('#todayList .todayCourse.gap').forEach(row=>ensureBreakLabel(row,g,'.label'));
                    document.querySelectorAll('#todayList .todayCourse.lunch').forEach(row=>ensureBreakLabel(row,l,'.label'));
                    document.querySelectorAll('#todayList .todayCourse.gap>.badge,#todayList .todayCourse.lunch>.badge').forEach(x=>x.remove());
                    document.querySelectorAll('#weekGrid .gapCell').forEach(cell=>{
                      let el=cell.querySelector('.cellLabel');
                      if(g){if(!el){el=document.createElement('div');el.className='cellLabel breakFitLabel';cell.appendChild(el)}if(el.textContent!==g)el.textContent=g}
                      else if(el)el.remove();
                    });
                    document.querySelectorAll('#weekGrid .lunchCell').forEach(cell=>{
                      const holder=cell.querySelector('.dynamicLunchOverlay')||cell;let el=holder.querySelector('.cellLabel');
                      if(l){if(!el){el=document.createElement('span');el.className='cellLabel breakFitLabel';holder.appendChild(el)}if(el.textContent!==l)el.textContent=l}
                      else if(el)el.remove();
                    });
                  }catch(e){}
                }
                window.syncBreakCells=syncBreakCells;

                function fitBreakLabel(el){
                  try{
                    if(!el)return;
                    let size=document.body.classList.contains('largeAppText')?.60:.58;
                    el.style.setProperty('font-size',size+'rem','important');
                    const holder=el.parentElement||el,max=Math.max(8,holder.clientWidth-6);
                    while(el.scrollWidth>max&&size>.38){size-=.02;el.style.setProperty('font-size',size.toFixed(2)+'rem','important')}
                  }catch(e){}
                }
                function fitBreakLabels(){document.querySelectorAll('#weekGrid .breakFitLabel').forEach(fitBreakLabel)}

                function polishWeekNowMarker(){
                  try{
                    const grid=document.getElementById('weekGrid'),rail=document.getElementById('weekNowRail'),dot=document.getElementById('weekNowDot');
                    if(!grid||!rail||!dot)return;
                    const now=new Date(),jsDay=now.getDay();
                    if(jsDay<1||jsDay>5||typeof activeWeek==='undefined'||typeof currentWeek==='undefined'||activeWeek!==currentWeek){rail.style.display='none';dot.style.display='none';return}
                    const firstDayCell=grid.querySelector('.wc');
                    if(firstDayCell)grid.style.setProperty('--week-now-x',firstDayCell.offsetLeft+'px');
                    if(rail.style.display==='none'||dot.style.display==='none')return;
                    const top=parseFloat(rail.style.top),dotTop=parseFloat(dot.style.top);
                    if(Number.isFinite(top)&&Number.isFinite(dotTop))rail.style.height=Math.max(2,dotTop-top)+'px';
                  }catch(e){}
                }

                function courseBadge(c){return String(c&&c.badge?c.badge:'').trim().slice(0,24)}
                function badgeHtml(c){const b=courseBadge(c);return b?`<span class="courseBadgePill">${esc(b)}</span>`:''}

                function ensureCourseBadgeField(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseBadgeField');
                  if(!field){
                    field=document.createElement('div');field.id='courseBadgeField';field.className='field';
                    field.innerHTML='<label>Badge (facultatif)</label><input id="fCourseBadge" type="text" maxlength="24" placeholder="Ex. DST, Groupe, Important">';
                    const room=document.getElementById('fRoom');const roomField=room?room.closest('.field'):null;
                    if(roomField&&roomField.nextSibling)roomField.parentNode.insertBefore(field,roomField.nextSibling);else form.insertBefore(field,form.querySelector('.sheetActions'));
                  }
                }
                function currentEditedCourseV9(){
                  try{if(typeof editing==='undefined'||editing==null||typeof weeks==='undefined')return null;return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null}catch(e){return null}
                }
                function syncCourseBadgeField(){ensureCourseBadgeField();const input=document.getElementById('fCourseBadge');if(input)input.value=courseBadge(currentEditedCourseV9())}

                function wrapCourseSubmit(){
                  ensureCourseBadgeField();
                  const form=document.getElementById('courseForm');if(!form||!form.onsubmit||form.onsubmit.__courseBadgeWrappedV9)return;
                  const oldSubmit=form.onsubmit;
                  const wrapped=function(e){
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A',day=typeof selected!=='undefined'?selected:2,idx=typeof editing!=='undefined'?editing:null;
                    const old=(idx!=null&&weeks[week]&&weeks[week][day])?weeks[week][day].courses[idx]:null;
                    const pre=typeof newPrefill!=='undefined'&&newPrefill?{start:newPrefill.start,end:newPrefill.end}:null;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0),text=((document.getElementById('fLabel')||{}).value||'').trim();
                    const badge=((document.getElementById('fCourseBadge')||{}).value||'').trim().slice(0,24);
                    let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}else if(old){start=old.start;end=old.end}else if(pre){start=pre.start;end=pre.end}
                    const result=oldSubmit.call(this,e);
                    try{
                      const arr=weeks[week]&&weeks[week][day]?weeks[week][day].courses:[];
                      let target=arr.find(c=>c.start===start&&c.end===end&&c.label===text);if(!target)target=arr.find(c=>c.start===start&&c.end===end);
                      if(target){target.badge=badge;if(typeof save==='function')save()}
                      setTimeout(decorateCourseBadges,0);
                    }catch(ex){}
                    return result;
                  };
                  wrapped.__courseBadgeWrappedV9=true;form.onsubmit=wrapped;
                }

                function setPill(holder,badge){
                  if(!holder)return;let pill=holder.querySelector(':scope > .courseBadgePill');
                  if(badge){if(!pill){pill=document.createElement('span');pill.className='courseBadgePill';holder.appendChild(pill)}if(pill.textContent!==badge)pill.textContent=badge}
                  else if(pill)pill.remove();
                }
                function decorateCourseBadges(){
                  try{
                    if(typeof weeks==='undefined')return;
                    const td=typeof todayKey==='function'?todayKey():2,today=weeks[currentWeek]&&weeks[currentWeek][td]?weeks[currentWeek][td].courses:[];
                    document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{
                      const start=((row.querySelector('.time strong')||{}).textContent||'').trim();const c=today.find(x=>x.start===start);setPill(row.querySelector('.label'),courseBadge(c));
                    });
                    const grid=document.getElementById('weekGrid');
                    if(grid&&typeof state!=='undefined'){
                      const kids=Array.from(grid.children);for(let p=6;p+5<kids.length;p+=6){
                        const timeText=(kids[p].textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g);if(!timeText||!timeText.length)continue;const start=timeText[0];
                        for(let i=0;i<5;i++){const cell=kids[p+1+i];if(!cell||!cell.classList.contains('has'))continue;const c=state[DAYS[i]].courses.find(x=>x.start===start);setPill(cell.querySelector('.cellLabel'),courseBadge(c))}
                      }
                    }
                    if(typeof state!=='undefined'&&state[selected])document.querySelectorAll('#editList .editCourse').forEach((row,i)=>{const c=state[selected].courses[i];setPill(row.querySelector('.label'),courseBadge(c))});
                  }catch(e){}
                }
                window.decorateCourseBadges=decorateCourseBadges;

                function renderTodayDynamic(){
                  state=weeks[currentWeek];
                  const d=todayKey(),now=new Date(),list=state[d].courses,cur=currentCourse(d);
                  $('todayTitle').textContent=FULL[d]+' · semaine '+currentWeek;
                  $('todayDate').textContent=now.toLocaleDateString('fr-FR',{day:'numeric',month:'long'});
                  if($('todayClock')){$('todayClock').textContent='';$('todayClock').style.display='none'}
                  $('todayProgress').style.width=progressPercent()+'%';
                  $('scaleStart').textContent=list[0]?.start||slots[0].start;
                  $('scaleEnd').textContent=list[list.length-1]?.end||slots[6].end;
                  const box=$('todayList');box.innerHTML='';
                  if(!list.length){box.innerHTML='<div class="empty">Aucun cours aujourd’hui.</div>';return}
                  const events=list.map(c=>({type:'course',start:min(c.start),end:min(c.end),course:c}));
                  for(const g of gapsForDay(list))events.push({type:'gap',start:g.start,end:g.end});
                  const l=lunchForDay(list);if(l)events.push({type:'lunch',start:l.startM,end:l.endM,l});
                  events.sort((a,b)=>a.start-b.start||a.end-b.end);
                  for(const ev of events){
                    const row=document.createElement('div');
                    if(ev.type==='course'){
                      const c=ev.course;row.className='todayCourse'+(cur===c?' current':'');
                      row.innerHTML=`<div class="time"><strong>${esc(c.start)}</strong><br>${esc(c.end)}</div><div><div class="label">${esc(c.label)}${badgeHtml(c)}</div><div class="room">salle ${esc(c.room||'—')}${c.slot?` · heure ${c.slot}`:''}</div></div>${cur===c?'<div class="badge">En cours</div>':''}`;
                      row.onclick=()=>{activeWeek=currentWeek;selected=d;editing=state[d].courses.indexOf(c);openEditor(editing)};
                    }else if(ev.type==='gap'){
                      const label=gapLabelText(),labelHtml=label?'<div class="label">'+esc(label)+'</div>':'';
                      row.className='todayCourse gap';
                      row.innerHTML=`<div class="time"><strong>${clock(ev.start)}</strong><br>${clock(ev.end)}</div><div>${labelHtml}<div class="room">${durationLabel(ev.end-ev.start)} sans cours</div></div>`;
                    }else{
                      const label=lunchLabelText(),labelHtml=label?'<div class="label">'+esc(label)+'</div>':'';
                      row.className='todayCourse lunch';
                      row.innerHTML=`<div class="time"><strong>${esc(ev.l.start)}</strong><br>${esc(ev.l.end)}</div><div>${labelHtml}</div>`;
                    }
                    box.appendChild(row);
                  }
                  setTimeout(()=>{syncBreakCells();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors()},0);
                }

                function appendRegularWeekRow(box,t){
                  const th=document.createElement('div');th.className='wh timecol';th.innerHTML=`${esc(t.start)}<br>${esc(t.end)}`;box.appendChild(th);
                  for(const d of DAYS){
                    const c=state[d].courses.find(x=>x.start===t.start&&x.end===t.end),cell=document.createElement('div');
                    if(c){
                      cell.className='wc has';cell.innerHTML=`<div><div class="cellLabel">${esc(c.label)}${badgeHtml(c)}</div><div class="cellRoom">${esc(c.room||'—')}</div></div>`;
                      cell.onclick=()=>{selected=d;editing=state[d].courses.indexOf(c);openEditor(editing)};
                    }else{
                      const g=gapsForDay(state[d].courses).find(x=>Math.min(min(t.end),x.end)>Math.max(min(t.start),x.start));
                      if(g){const label=gapLabelText();cell.className='wc gapCell';cell.innerHTML=label?`<div class="cellLabel breakFitLabel">${esc(label)}</div>`:''}
                      else cell.className='wc emptyCell';
                      cell.onclick=()=>{selected=d;newPrefill={start:t.start,end:t.end};openEditor(null)};
                    }
                    box.appendChild(cell);
                  }
                }

                function appendLunchBandRow(box){
                  const bounds=lunchBounds(),th=document.createElement('div');
                  th.className='wh timecol';th.innerHTML=`${clock(bounds.start)}<br>${clock(bounds.end)}`;box.appendChild(th);
                  const label=lunchLabelText();
                  for(const d of DAYS){
                    const info=lunchForDay(state[d].courses),cell=document.createElement('div');
                    if(!info){cell.className='wc emptyCell dynamicLunchEmpty';box.appendChild(cell);continue}
                    cell.className='wc lunchCell dynamicLunchCell';
                    const overlay=document.createElement('div');overlay.className='dynamicLunchOverlay';
                    overlay.innerHTML=label?`<span class="cellLabel breakFitLabel">${esc(label)}</span>`:'';
                    cell.appendChild(overlay);box.appendChild(cell);
                  }
                }

                function renderWeekDynamic(){
                  state=weeks[activeWeek];$('weekTitleLetter').textContent=activeWeek;
                  const box=$('weekGrid'),times=dynamicWeekTimes();box.innerHTML='';
                  const corner=document.createElement('div');corner.className='wh timecol';corner.textContent='H';box.appendChild(corner);
                  for(const d of DAYS){const h=document.createElement('div');h.className='wh day';h.textContent=NAMES[d];box.appendChild(h)}
                  for(const t of times){if(t.type==='lunchDynamic')appendLunchBandRow(box);else appendRegularWeekRow(box,t)}
                  setTimeout(()=>{syncBreakCells();fitBreakLabels();polishWeekNowMarker();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors()},0);
                }

                function bindOverrides(){
                  window.uniqueWeekTimes=dynamicWeekTimes;window.renderToday=renderTodayDynamic;window.renderWeek=renderWeekDynamic;window.renderBreakSettings=renderBreakSettingsV9;window.applyBreakSettings=applyBreakSettingsV9;
                  try{uniqueWeekTimes=dynamicWeekTimes;renderToday=renderTodayDynamic;renderWeek=renderWeekDynamic;renderBreakSettings=renderBreakSettingsV9;applyBreakSettings=applyBreakSettingsV9}catch(e){}
                }

                bindOverrides();wireBreakSettings();ensureCourseBadgeField();wrapCourseSubmit();
                const modal=document.getElementById('modal');
                if(modal)new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(()=>{syncCourseBadgeField();wrapCourseSubmit()},0)}).observe(modal,{attributes:true,attributeFilter:['class']});
                for(const id of ['todayList','weekGrid','editList']){const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(()=>{syncBreakCells();decorateCourseBadges();fitBreakLabels()},0)).observe(el,{childList:true,subtree:true})}
                window.addEventListener('resize',()=>setTimeout(()=>{fitBreakLabels();polishWeekNowMarker();decorateCourseBadges()},30));
                setInterval(()=>setTimeout(polishWeekNowMarker,45),60000);

                function refresh(){
                  try{
                    bindOverrides();wireBreakSettings();ensureCourseBadgeField();wrapCourseSubmit();
                    if(typeof render==='function')render();
                    setTimeout(()=>{bindOverrides();wireBreakSettings();syncBreakCells();fitBreakLabels();decorateCourseBadges();if(window.refreshCourseColors)window.refreshCourseColors();setTimeout(polishWeekNowMarker,35)},15);
                  }catch(e){}
                }
                window.refreshLunchBreakUi=refresh;
                refresh();
              }catch(e){console.log('Lunch break UI',e)}
            })();
            """;
    }
}
