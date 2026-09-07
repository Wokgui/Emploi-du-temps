package com.wokgui.schedulewidget;

final class LunchBreakUi {
    private LunchBreakUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__lunchBreakUiV2){
                  if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                  return;
                }
                window.__lunchBreakUiV2=true;

                const style=document.createElement('style');
                style.textContent=`
                  #weekGrid .dynamicLunchCell{position:relative;overflow:visible!important;background:#fff7e6!important;color:var(--lunch);z-index:2}
                  #weekGrid .dynamicLunchCovered{background:#fff7e6!important;color:transparent}
                  #weekGrid .dynamicLunchEmpty{background:#fff!important}
                  #weekGrid .dynamicLunchOverlay{position:absolute;left:1px;top:2px;bottom:2px;z-index:9;display:flex;align-items:center;justify-content:center;padding:2px 5px;border:1px solid #efc66f;border-radius:6px;background:linear-gradient(90deg,#fff0c8 0%,#ffe2a6 100%);color:#8d5810;box-shadow:0 1px 2px #8d581012;pointer-events:none;white-space:nowrap;overflow:hidden}
                  #weekGrid .dynamicLunchOverlay .cellLabel{font-size:.58rem!important;line-height:1!important;-webkit-line-clamp:1!important}
                  body.largeAppText #weekGrid .dynamicLunchOverlay .cellLabel{font-size:.62rem!important}
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
                  $('todayClock').textContent=now.toLocaleTimeString('fr-FR',{hour:'2-digit',minute:'2-digit'});
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
                      row.innerHTML=`<div class="time"><strong>${clock(ev.start)}</strong><br>${clock(ev.end)}</div><div><div class="label">${esc(breaks.gapLabel)}</div><div class="room">${durationLabel(ev.end-ev.start)} sans cours</div></div>${breaks.showGapBadge?'<div class="badge gap">Trou</div>':''}`;
                    }else{
                      row.className='todayCourse lunch';
                      row.innerHTML=`<div class="time"><strong>${esc(ev.l.start)}</strong><br>${esc(ev.l.end)}</div><div><div class="label">Midi</div></div>`;
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
                      if(g){cell.className='wc gapCell';cell.innerHTML=`<div class="cellLabel">${esc(breaks.gapLabel)}</div>`}
                      else cell.className='wc emptyCell';
                      cell.onclick=()=>{selected=d;newPrefill={start:t.start,end:t.end};openEditor(null)};
                    }
                    box.appendChild(cell);
                  }
                }

                function appendLunchBandRow(box){
                  const bounds=lunchBounds();
                  const th=document.createElement('div');th.className='wh timecol';th.innerHTML=`${clock(bounds.start)}<br>${clock(bounds.end)}`;box.appendChild(th);
                  const infos=DAYS.map(d=>lunchForDay(state[d].courses));
                  let i=0;
                  while(i<DAYS.length){
                    const info=infos[i];
                    if(!info){const empty=document.createElement('div');empty.className='wc emptyCell dynamicLunchEmpty';box.appendChild(empty);i++;continue}
                    let run=1;
                    while(i+run<DAYS.length&&infos[i+run]&&infos[i+run].endM===info.endM&&infos[i+run].startM===info.startM)run++;
                    for(let k=0;k<run;k++){
                      const cell=document.createElement('div');
                      cell.className='wc lunchCell '+(k===0?'dynamicLunchCell':'dynamicLunchCovered');
                      if(k===0){
                        const overlay=document.createElement('div');overlay.className='dynamicLunchOverlay';overlay.style.width=(run*100)+'%';
                        overlay.innerHTML='<span class="cellLabel">Midi</span>';
                        cell.appendChild(overlay);
                      }
                      box.appendChild(cell);
                    }
                    i+=run;
                  }
                }

                function renderWeekDynamic(){
                  state=weeks[activeWeek];$('weekTitleLetter').textContent=activeWeek;
                  const box=$('weekGrid'),times=dynamicWeekTimes();box.innerHTML='';
                  const corner=document.createElement('div');corner.className='wh timecol';corner.textContent='H';box.appendChild(corner);
                  for(const d of DAYS){const h=document.createElement('div');h.className='wh day';h.textContent=NAMES[d];box.appendChild(h)}
                  for(const t of times){if(t.type==='lunchDynamic')appendLunchBandRow(box);else appendRegularWeekRow(box,t)}
                }

                window.uniqueWeekTimes=dynamicWeekTimes;
                window.renderToday=renderTodayDynamic;
                window.renderWeek=renderWeekDynamic;
                try{uniqueWeekTimes=dynamicWeekTimes;renderToday=renderTodayDynamic;renderWeek=renderWeekDynamic}catch(e){}

                function refresh(){
                  try{if(typeof render==='function')render();if(window.refreshCourseColors)setTimeout(window.refreshCourseColors,15)}catch(e){}
                }
                window.refreshLunchBreakUi=refresh;
                refresh();
              } catch(e) { console.log('Lunch break UI',e); }
            })();
            """;
    }
}
