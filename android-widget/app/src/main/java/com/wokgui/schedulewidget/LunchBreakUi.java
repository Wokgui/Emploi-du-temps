package com.wokgui.schedulewidget;

final class LunchBreakUi {
    private LunchBreakUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__lunchBreakUiV5){
                  if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                  return;
                }
                window.__lunchBreakUiV5=true;

                const style=document.createElement('style');
                style.textContent=`
                  #todayClock{display:none!important}
                  #todayList #todayNowTime{display:none!important}
                  #todayList .todayCourse{grid-template-columns:58px minmax(0,1fr) auto!important;gap:4px!important}
                  #todayList #todayNowRail{left:61px!important}
                  #todayList #todayNowDot{left:61px!important}
                  #weekGrid .dynamicLunchCell{position:relative;overflow:hidden!important;background:#fff7e6!important;color:var(--lunch);z-index:2}
                  #weekGrid .dynamicLunchEmpty{background:#fff!important}
                  #weekGrid .dynamicLunchOverlay{position:absolute;inset:2px 1px;z-index:9;display:flex;align-items:center;justify-content:center;padding:2px 3px;border:1px solid #efc66f;border-radius:6px;background:linear-gradient(90deg,#fff0c8 0%,#ffe2a6 100%);color:#8d5810;box-shadow:0 1px 2px #8d581012;pointer-events:none;white-space:nowrap;overflow:hidden}
                  #weekGrid .breakFitLabel{display:block!important;max-width:100%;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;line-height:1!important;-webkit-line-clamp:1!important;-webkit-box-orient:initial!important;font-size:.58rem!important}
                  body.largeAppText #weekGrid .breakFitLabel{font-size:.60rem!important}
                  #weekGrid #weekNowRail{left:var(--week-now-x,38px)!important;width:2px!important;border-radius:2px!important;opacity:.78}
                  #weekGrid #weekNowDot{left:var(--week-now-x,38px)!important;width:11px!important;height:11px!important}
                `;
                document.head.appendChild(style);

                function lunchBounds(){
                  try{
                    const start=(Array.isArray(slots)&&slots[3]&&slots[3].end)?min(slots[3].end):12*60;
                    const end=(Array.isArray(slots)&&slots[4]&&slots[4].start)?min(slots[4].start):13*60;
                    return {start,end};
                  }catch(e){return {start:12*60,end:13*60}}
                }
                window.lunchBounds=lunchBounds;

                function lunchLabelText(){
                  const raw=String((typeof breaks!=='undefined'&&breaks&&breaks.lunchLabel)||'').trim();
                  if(!raw||raw.toLocaleLowerCase()==='pause de midi')return 'Midi';
                  return raw;
                }

                function gapLabelText(){
                  const raw=String((typeof breaks!=='undefined'&&breaks&&breaks.gapLabel)||'').trim();
                  return raw||'Trou';
                }

                let breakSaveTimer=null;
                function commitBreakLabels(){
                  try{
                    if(typeof breaks==='undefined'||!breaks)return;
                    const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                    if(!gap&&!lunch)return;
                    const nextGap=((gap?gap.value:breaks.gapLabel)||'Trou').trim().slice(0,28)||'Trou';
                    const rawLunch=((lunch?lunch.value:breaks.lunchLabel)||'Midi').trim().slice(0,28)||'Midi';
                    const nextLunch=rawLunch.toLocaleLowerCase()==='pause de midi'?'Midi':rawLunch;
                    if(breaks.gapLabel===nextGap&&breaks.lunchLabel===nextLunch)return;
                    breaks.gapLabel=nextGap;
                    breaks.lunchLabel=nextLunch;
                    if(gap&&gap.value!==nextGap)gap.value=nextGap;
                    if(lunch&&lunch.value!==nextLunch)lunch.value=nextLunch;
                    if(typeof save==='function')save();
                    else if(window.AndroidSchedule&&AndroidSchedule.saveSchedule&&typeof exportState==='function')AndroidSchedule.saveSchedule(JSON.stringify(exportState()));
                    setTimeout(()=>{fitBreakLabels();polishWeekNowMarker()},25);
                  }catch(e){}
                }

                function queueBreakSave(){
                  if(breakSaveTimer)clearTimeout(breakSaveTimer);
                  breakSaveTimer=setTimeout(commitBreakLabels,180);
                }

                function syncBreakInputs(){
                  try{
                    const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                    if(typeof breaks!=='undefined'&&breaks&&String(breaks.lunchLabel||'').trim().toLocaleLowerCase()==='pause de midi'){
                      breaks.lunchLabel='Midi';
                      if(typeof save==='function'&&!window.__lunchDefaultMigratedV5){window.__lunchDefaultMigratedV5=true;setTimeout(()=>save(),0)}
                    }
                    if(gap){
                      gap.maxLength=28;gap.placeholder='Trou';
                      if(typeof breaks!=='undefined'&&breaks&&document.activeElement!==gap)gap.value=gapLabelText();
                      if(!gap.__breakLiveV5){
                        gap.__breakLiveV5=true;
                        gap.addEventListener('input',queueBreakSave);
                        gap.addEventListener('change',commitBreakLabels);
                        gap.addEventListener('blur',commitBreakLabels);
                      }
                    }
                    if(lunch){
                      lunch.maxLength=28;lunch.placeholder='Midi';
                      if(document.activeElement!==lunch)lunch.value=lunchLabelText();
                      if(!lunch.__breakLiveV5){
                        lunch.__breakLiveV5=true;
                        lunch.addEventListener('input',queueBreakSave);
                        lunch.addEventListener('change',commitBreakLabels);
                        lunch.addEventListener('blur',commitBreakLabels);
                      }
                    }
                  }catch(e){}
                }

                function fitBreakLabel(el){
                  try{
                    if(!el)return;
                    el.style.removeProperty('font-size');
                    let size=document.body.classList.contains('largeAppText')?.60:.58;
                    el.style.setProperty('font-size',size+'rem','important');
                    const holder=el.parentElement||el;
                    const max=Math.max(8,holder.clientWidth-6);
                    while(el.scrollWidth>max&&size>.38){size-=.02;el.style.setProperty('font-size',size.toFixed(2)+'rem','important')}
                  }catch(e){}
                }

                function fitBreakLabels(){
                  document.querySelectorAll('#weekGrid .breakFitLabel').forEach(fitBreakLabel);
                }
                window.fitBreakLabels=fitBreakLabels;

                function polishWeekNowMarker(){
                  try{
                    const grid=document.getElementById('weekGrid'),rail=document.getElementById('weekNowRail'),dot=document.getElementById('weekNowDot');
                    if(!grid||!rail||!dot)return;
                    const now=new Date(),jsDay=now.getDay();
                    if(jsDay<1||jsDay>5||typeof activeWeek==='undefined'||typeof currentWeek==='undefined'||activeWeek!==currentWeek){rail.style.display='none';dot.style.display='none';return}
                    const firstDayCell=grid.querySelector('.wc');
                    if(firstDayCell){
                      const separatorX=Math.max(0,firstDayCell.offsetLeft-1);
                      grid.style.setProperty('--week-now-x',separatorX+'px');
                    }
                    if(rail.style.display==='none'||dot.style.display==='none')return;
                    const top=parseFloat(rail.style.top),dotTop=parseFloat(dot.style.top);
                    if(!Number.isFinite(top)||!Number.isFinite(dotTop))return;
                    rail.style.height=Math.max(2,dotTop-top)+'px';
                  }catch(e){}
                }
                window.polishWeekNowMarker=polishWeekNowMarker;

                function lunchForDay(list){
                  try{
                    const courses=(Array.isArray(list)?list:[]).slice().sort((a,b)=>min(a.start)-min(b.start));
                    if(!courses.length)return null;
                    const bounds=lunchBounds(),start=bounds.start,end=bounds.end;
                    if(end<=start)return null;
                    const hasBefore=courses.some(c=>min(c.end)<=start);
                    const hasAfter=courses.some(c=>min(c.start)>=end);
                    if(!hasBefore||!hasAfter)return null;
                    const overlaps=courses.some(c=>min(c.start)<end&&min(c.end)>start);
                    if(overlaps)return null;
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
                    const b=lunchBounds();
                    out.push({start:clock(b.start),end:clock(b.end),type:'lunchDynamic'});
                  }
                  return out.sort((a,b)=>min(a.start)-min(b.start)||((a.type==='lunchDynamic')?-1:0)-((b.type==='lunchDynamic')?-1:0)||min(a.end)-min(b.end));
                }

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
                      row.innerHTML=`<div class="time"><strong>${esc(c.start)}</strong><br>${esc(c.end)}</div><div><div class="label">${esc(c.label)}</div><div class="room">salle ${esc(c.room||'—')}${c.slot?` · heure ${c.slot}`:''}</div></div>${cur===c?'<div class="badge">En cours</div>':''}`;
                      row.onclick=()=>{activeWeek=currentWeek;selected=d;editing=state[d].courses.indexOf(c);openEditor(editing)};
                    }else if(ev.type==='gap'){
                      row.className='todayCourse gap';
                      const gapBadge=breaks.showGapBadge?'<div class="badge gap">'+esc(gapLabelText())+'</div>':'';
                      row.innerHTML=`<div class="time"><strong>${clock(ev.start)}</strong><br>${clock(ev.end)}</div><div><div class="label">${esc(gapLabelText())}</div><div class="room">${durationLabel(ev.end-ev.start)} sans cours</div></div>${gapBadge}`;
                    }else{
                      row.className='todayCourse lunch';
                      row.innerHTML=`<div class="time"><strong>${esc(ev.l.start)}</strong><br>${esc(ev.l.end)}</div><div><div class="label">${esc(lunchLabelText())}</div></div>`;
                    }
                    box.appendChild(row);
                  }
                }

                function appendRegularWeekRow(box,t){
                  const th=document.createElement('div');th.className='wh timecol';th.innerHTML=`${esc(t.start)}<br>${esc(t.end)}`;box.appendChild(th);
                  for(const d of DAYS){
                    const c=state[d].courses.find(x=>x.start===t.start&&x.end===t.end),cell=document.createElement('div');
                    if(c){
                      cell.className='wc has';
                      cell.innerHTML=`<div><div class="cellLabel">${esc(c.label)}</div><div class="cellRoom">${esc(c.room||'—')}</div></div>`;
                      cell.onclick=()=>{selected=d;editing=state[d].courses.indexOf(c);openEditor(editing)};
                    }else{
                      const g=gapsForDay(state[d].courses).find(x=>Math.min(min(t.end),x.end)>Math.max(min(t.start),x.start));
                      if(g){cell.className='wc gapCell';cell.innerHTML=`<div class="cellLabel breakFitLabel">${esc(gapLabelText())}</div>`}
                      else cell.className='wc emptyCell';
                      cell.onclick=()=>{selected=d;newPrefill={start:t.start,end:t.end};openEditor(null)};
                    }
                    box.appendChild(cell);
                  }
                }

                function appendLunchBandRow(box){
                  const bounds=lunchBounds();
                  const th=document.createElement('div');th.className='wh timecol';th.innerHTML=`${clock(bounds.start)}<br>${clock(bounds.end)}`;box.appendChild(th);
                  for(const d of DAYS){
                    const info=lunchForDay(state[d].courses),cell=document.createElement('div');
                    if(!info){cell.className='wc emptyCell dynamicLunchEmpty';box.appendChild(cell);continue}
                    cell.className='wc lunchCell dynamicLunchCell';
                    const overlay=document.createElement('div');overlay.className='dynamicLunchOverlay';
                    overlay.innerHTML=`<span class="cellLabel breakFitLabel">${esc(lunchLabelText())}</span>`;
                    cell.appendChild(overlay);box.appendChild(cell);
                  }
                }

                function renderWeekDynamic(){
                  state=weeks[activeWeek];$('weekTitleLetter').textContent=activeWeek;
                  const box=$('weekGrid'),times=dynamicWeekTimes();box.innerHTML='';
                  const corner=document.createElement('div');corner.className='wh timecol';corner.textContent='H';box.appendChild(corner);
                  for(const d of DAYS){const h=document.createElement('div');h.className='wh day';h.textContent=NAMES[d];box.appendChild(h)}
                  for(const t of times){if(t.type==='lunchDynamic')appendLunchBandRow(box);else appendRegularWeekRow(box,t)}
                  setTimeout(()=>{fitBreakLabels();polishWeekNowMarker()},0);
                }

                window.uniqueWeekTimes=dynamicWeekTimes;
                window.renderToday=renderTodayDynamic;
                window.renderWeek=renderWeekDynamic;
                try{uniqueWeekTimes=dynamicWeekTimes;renderToday=renderTodayDynamic;renderWeek=renderWeekDynamic}catch(e){}

                const weekGrid=document.getElementById('weekGrid');
                if(weekGrid)new MutationObserver(()=>setTimeout(()=>{fitBreakLabels();polishWeekNowMarker()},0)).observe(weekGrid,{childList:true,subtree:false});
                window.addEventListener('resize',()=>setTimeout(()=>{fitBreakLabels();polishWeekNowMarker()},30));
                setInterval(()=>setTimeout(polishWeekNowMarker,45),60000);

                function refresh(){
                  try{
                    syncBreakInputs();
                    if(typeof render==='function')render();
                    setTimeout(()=>{
                      syncBreakInputs();fitBreakLabels();
                      if(window.refreshCourseColors)window.refreshCourseColors();
                      setTimeout(polishWeekNowMarker,40);
                    },15);
                  }catch(e){}
                }
                window.refreshLunchBreakUi=refresh;
                refresh();
              } catch(e) { console.log('Lunch break UI',e); }
            })();
            """;
    }
}
