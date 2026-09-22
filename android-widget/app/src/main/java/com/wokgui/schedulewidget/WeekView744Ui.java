package com.wokgui.schedulewidget;

/** Final owner for the dated week header, unlimited calendar navigation and week-lunch visibility. */
final class WeekView744Ui {
    private WeekView744Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtWeekView744){window.refreshWeekView744&&window.refreshWeekView744();return}
                window.__edtWeekView744=true;

                const DAY_SHORT_744={1:'Dim',2:'Lun',3:'Mar',4:'Mer',5:'Jeu',6:'Ven',7:'Sam'};
                const WEEK_MS_744=7*24*60*60*1000;
                let queued744=false;

                const style=document.createElement('style');
                style.id='edtWeekView744Style';
                style.textContent=`
                  html body #viewWeek .weekTop{
                    display:grid!important;
                    grid-template-columns:42px minmax(0,1fr) 42px!important;
                    align-items:center!important;
                    gap:8px!important;
                    margin:0 0 7px!important
                  }
                  html body #viewWeek .weekTop h2{
                    display:flex!important;
                    align-items:baseline!important;
                    justify-content:center!important;
                    flex-wrap:wrap!important;
                    column-gap:14px!important;
                    row-gap:2px!important;
                    min-width:0!important;
                    margin:0!important;
                    text-align:center!important;
                    line-height:1.08!important
                  }
                  html body #viewWeek #weekName744,
                  html body #viewWeek #weekRange744{
                    display:inline-block!important;
                    font-size:1rem!important;
                    white-space:nowrap!important
                  }
                  html body #viewWeek #weekName744{
                    color:var(--ink,#111936)!important;
                    font-weight:850!important
                  }
                  html body #viewWeek #weekRange744{
                    color:#536078!important;
                    font-weight:760!important
                  }
                  html body #viewWeek #weekPrev728,
                  html body #viewWeek #weekNext728{
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    width:42px!important;height:42px!important;
                    min-width:42px!important;min-height:42px!important;
                    margin:0!important;padding:0!important;
                    border:1px solid #d7e0e9!important;
                    border-radius:50%!important;
                    background:#fff!important;
                    color:var(--set-accent,var(--blue,#0877f9))!important;
                    font-size:1.42rem!important;
                    font-weight:850!important;
                    line-height:1!important
                  }
                  html body #viewWeek #weekGrid .wh.day{
                    display:flex!important;
                    flex-direction:column!important;
                    align-items:center!important;
                    justify-content:center!important;
                    gap:3px!important;
                    text-align:center!important;
                    line-height:1!important
                  }
                  html body #viewWeek #weekGrid .weekDayName744{
                    display:block!important;
                    color:#0877b9!important;
                    font-size:.65rem!important;
                    font-weight:850!important;
                    white-space:nowrap!important
                  }
                  html body #viewWeek #weekGrid .weekDayDate744{
                    display:block!important;
                    margin:0!important;
                    color:#657087!important;
                    font-size:.56rem!important;
                    font-weight:760!important;
                    white-space:nowrap!important
                  }

                  html body #viewWeek #weekGrid{
                    position:relative!important
                  }

                  /* Visible lunch: one colour owner and one pair of horizontal lines. */
                  html body #viewWeek #weekGrid.week744LunchVisible .wc.week658Lunch{
                    background:var(--week658-lunch,#FFE08A)!important;
                    background-color:var(--week658-lunch,#FFE08A)!important;
                    color:#59491d!important;
                    border-top-color:transparent!important;
                    border-bottom-color:transparent!important;
                    box-shadow:none!important;
                    outline:0!important;
                    border-radius:0!important
                  }
                  html body #viewWeek #weekGrid.week744LunchVisible .wh.timecol.week658LunchTime{
                    background:var(--week658-lunch,#FFE08A)!important;
                    background-color:var(--week658-lunch,#FFE08A)!important;
                    color:#59491d!important;
                    border-top-color:transparent!important;
                    border-bottom-color:transparent!important;
                    box-shadow:none!important;
                    outline:0!important
                  }
                  html body #viewWeek #weekGrid.week744LunchVisible.week662LunchLines:before{
                    display:none!important
                  }
                  html body #viewWeek #weekGrid.week744LunchVisible:after{
                    content:''!important;
                    position:absolute!important;
                    inset:0!important;
                    display:block!important;
                    background:var(--week744-lunch-lines,none)!important;
                    pointer-events:none!important;
                    z-index:80!important;
                    border-radius:inherit!important
                  }

                  /* Hidden lunch: 12:00-13:00 is an ordinary free row, including the time cell. */
                  html body #viewWeek #weekGrid.week744NoLunch.week662LunchLines:before,
                  html body #viewWeek #weekGrid.week744NoLunch:after{
                    display:none!important;
                    background:none!important
                  }
                  html body #viewWeek #weekGrid.week744NoLunch .wc:is(.week658Lunch,.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic){
                    background:var(--week658-free,#E6F2FF)!important;
                    background-color:var(--week658-free,#E6F2FF)!important;
                    color:transparent!important;
                    border-top:0!important;
                    border-bottom:1px solid #e6ebf0!important;
                    box-shadow:none!important;
                    outline:0!important;
                    border-radius:0!important
                  }
                  html body #viewWeek #weekGrid.week744NoLunch .wc:is(.week658Lunch,.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic)>*,
                  html body #viewWeek #weekGrid.week744NoLunch .week658LunchLabel,
                  html body #viewWeek #weekGrid.week744NoLunch .dynamicLunchOverlay{
                    visibility:hidden!important;
                    opacity:0!important;
                    display:none!important
                  }
                  html body #viewWeek #weekGrid.week744NoLunch .wh.timecol.week658LunchTime{
                    background:var(--week744-time-bg,#f7faff)!important;
                    background-color:var(--week744-time-bg,#f7faff)!important;
                    color:var(--week744-time-ink,#5f6d82)!important;
                    border-top:0!important;
                    border-bottom:1px solid var(--week744-grid-line,#e6ebf0)!important;
                    box-shadow:none!important;
                    outline:0!important
                  }
                `;
                document.head.appendChild(style);

                function addDays744(date,days){
                  const d=new Date(date.getFullYear(),date.getMonth(),date.getDate(),12,0,0,0);
                  d.setDate(d.getDate()+days);return d;
                }
                function monday744(date){
                  const d=new Date(date.getFullYear(),date.getMonth(),date.getDate(),12,0,0,0);
                  const js=d.getDay(),delta=js===0?-6:1-js;d.setDate(d.getDate()+delta);return d;
                }
                function selectedMonday744(){
                  const d=window.__edt728WeekMonday;
                  if(d instanceof Date&&!Number.isNaN(d.getTime()))return monday744(d);
                  const now=monday744(new Date());window.__edt728WeekMonday=now;return now;
                }
                function setMonday744(date){
                  window.__edt728WeekMonday=monday744(date);
                }
                function cycleLength744(){
                  try{
                    const a=JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}');
                    const n=Math.max(1,Math.min(4,Number(a.cycleLength)||2));return a.singleWeek===true?1:n;
                  }catch(e){return 2}
                }
                function weekLetter744(date){
                  try{if(typeof weekLetter728==='function')return String(weekLetter728(date)||'A')}catch(e){}
                  try{if(typeof window.weekLetter728==='function')return String(window.weekLetter728(date)||'A')}catch(e){}
                  const order=['A','B','C','D'],len=cycleLength744(),now=monday744(new Date());
                  let base='A';try{if(typeof currentWeek!=='undefined'&&order.includes(currentWeek))base=currentWeek}catch(e){}
                  const diff=Math.round((monday744(date)-now)/WEEK_MS_744);
                  const start=Math.max(0,order.indexOf(base)),idx=((start+diff)%len+len)%len;
                  return order[idx]||'A';
                }
                function enabledDays744(){
                  try{
                    if(typeof DAYS!=='undefined'&&Array.isArray(DAYS)&&DAYS.length)return DAYS.map(Number);
                  }catch(e){}
                  try{
                    const root=JSON.parse(AndroidSchedule.loadSchedule()||'{}');
                    if(Array.isArray(root._enabledDays)&&root._enabledDays.length)return root._enabledDays.map(Number);
                  }catch(e){}
                  return [2,3,4,5,6];
                }
                function dayDate744(monday,day){
                  const delta=day===1?6:Math.max(0,day-2);return addDays744(monday,delta);
                }
                function fmt744(date){
                  return date.toLocaleDateString('fr-FR',{day:'2-digit',month:'2-digit'});
                }
                function advanced744(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}
                }

                function ensureNav744(){
                  const root=document.querySelector('#viewWeek .weekTop'),title=root&&root.querySelector('h2');
                  if(!root||!title)return;
                  let prev=document.getElementById('weekPrev728');
                  if(!prev){
                    prev=document.createElement('button');prev.id='weekPrev728';prev.type='button';prev.className='calendarNav728';
                    prev.textContent='‹';root.insertBefore(prev,title);
                  }
                  let next=document.getElementById('weekNext728');
                  if(!next){
                    next=document.createElement('button');next.id='weekNext728';next.type='button';next.className='calendarNav728';
                    next.textContent='›';root.appendChild(next);
                  }
                  prev.setAttribute('aria-label','Semaine précédente');
                  next.setAttribute('aria-label','Semaine suivante');
                  prev.onclick=function(event){
                    event.preventDefault();event.stopPropagation();
                    setMonday744(addDays744(selectedMonday744(),-7));
                    render744();
                    return false;
                  };
                  next.onclick=function(event){
                    event.preventDefault();event.stopPropagation();
                    setMonday744(addDays744(selectedMonday744(),7));
                    render744();
                    return false;
                  };
                }

                function decorateHeader744(){
                  ensureNav744();
                  const monday=selectedMonday744(),sunday=addDays744(monday,6),letter=weekLetter744(monday);
                  const title=document.querySelector('#viewWeek .weekTop h2');
                  if(title){
                    let name=document.getElementById('weekName744'),range=document.getElementById('weekRange744');
                    if(!name||!range||name.parentElement!==title||range.parentElement!==title){
                      title.replaceChildren();
                      name=document.createElement('span');name.id='weekName744';
                      range=document.createElement('span');range.id='weekRange744';
                      title.append(name,range);
                    }
                    name.textContent='Semaine '+letter;
                    range.textContent=fmt744(monday)+' – '+fmt744(sunday);
                  }
                  const days=enabledDays744(),headers=[...document.querySelectorAll('#weekGrid>.wh.day')];
                  headers.forEach((head,index)=>{
                    const day=days[index]||[2,3,4,5,6,7,1][index]||2,date=dayDate744(monday,day);
                    const dayText=DAY_SHORT_744[day]||'',dateText=fmt744(date);
                    let name=head.querySelector('.weekDayName744'),dateEl=head.querySelector('.weekDayDate744');
                    if(!name||!dateEl){
                      head.replaceChildren();
                      name=document.createElement('span');name.className='weekDayName744';
                      dateEl=document.createElement('span');dateEl.className='weekDayDate744';
                      head.append(name,dateEl);
                    }
                    if(name.textContent!==dayText)name.textContent=dayText;
                    if(dateEl.textContent!==dateText)dateEl.textContent=dateText;
                  });
                  try{activeWeek=letter;if(typeof weeks!=='undefined'&&weeks[letter])state=weeks[letter]}catch(e){}
                }

                function rows744(grid){
                  const heads=[...grid.querySelectorAll(':scope>.wh.day')],count=heads.length||5,rows=[];
                  for(const time of grid.querySelectorAll(':scope>.wh.timecol')){
                    const vals=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(vals.length<2)continue;
                    const cells=[];let node=time.nextElementSibling;
                    while(node&&cells.length<count){if(node.classList&&node.classList.contains('wc'))cells.push(node);node=node.nextElementSibling}
                    if(cells.length===count)rows.push({time,cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return rows;
                }
                function setTimeReference744(grid){
                  const ref=[...grid.querySelectorAll(':scope>.wh.timecol')].find(el=>!/week658LunchTime/.test(el.className)&&/[0-9]{1,2}:[0-9]{2}/.test(el.textContent||''));
                  if(!ref)return;
                  const cs=getComputedStyle(ref);
                  grid.style.setProperty('--week744-time-bg',cs.backgroundColor||'#f7faff');
                  grid.style.setProperty('--week744-time-ink',cs.color||'#5f6d82');
                  grid.style.setProperty('--week744-grid-line',cs.borderBottomColor||'#e6ebf0');
                }
                function lunchLines744(grid){
                  const rows=rows744(grid).filter(row=>row.time.classList.contains('week658LunchTime'));
                  if(!rows.length){grid.style.removeProperty('--week744-lunch-lines');return}
                  const groups=[];let current=[];
                  rows.forEach(row=>{
                    if(!current.length){current=[row];return}
                    const prev=current[current.length-1],prevBottom=prev.time.offsetTop+prev.time.offsetHeight;
                    if(row.time.offsetTop<=prevBottom+2)current.push(row);
                    else{groups.push(current);current=[row]}
                  });
                  if(current.length)groups.push(current);
                  const layers=[];
                  groups.forEach(group=>{
                    const first=group[0].time,last=group[group.length-1].time;
                    const top=Math.max(0,Math.round(first.offsetTop));
                    const bottom=Math.max(0,Math.round(last.offsetTop+last.offsetHeight-2));
                    layers.push('linear-gradient(#D5B84D,#D5B84D) 0 '+top+'px / 100% 2px no-repeat');
                    layers.push('linear-gradient(#D5B84D,#D5B84D) 0 '+bottom+'px / 100% 2px no-repeat');
                  });
                  grid.style.setProperty('--week744-lunch-lines',layers.join(','));
                }
                function enforceLunch744(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  const hide=advanced744().showLunchWeek===false;
                  grid.classList.toggle('week744NoLunch',hide);
                  grid.classList.toggle('week744LunchVisible',!hide);
                  setTimeReference744(grid);
                  if(hide){
                    grid.classList.remove('week662LunchLines');
                    grid.style.removeProperty('--week662-lunch-lines');
                    grid.style.removeProperty('--week744-lunch-lines');
                  }else{
                    lunchLines744(grid);
                  }
                }

                function finish744(){
                  queued744=false;
                  decorateHeader744();
                  enforceLunch744();
                  document.documentElement.dataset.edtWeek744='1';
                }
                function schedule744(){
                  if(queued744)return;queued744=true;requestAnimationFrame(finish744);
                }

                const previousRender744=window.renderWeek;
                function render744(){
                  const monday=selectedMonday744(),letter=weekLetter744(monday);
                  try{activeWeek=letter;if(typeof weeks!=='undefined'&&weeks[letter])state=weeks[letter]}catch(e){}
                  let result;
                  try{if(typeof previousRender744==='function')result=previousRender744.apply(this,arguments)}catch(e){}
                  finish744();
                  return result;
                }
                window.renderWeek=render744;
                try{renderWeek=render744}catch(e){}
                window.refreshWeekView744=finish744;

                const grid=document.getElementById('weekGrid');
                if(grid&&!grid.__edt744Observer){
                  grid.__edt744Observer=true;
                  new MutationObserver(schedule744).observe(grid,{childList:true,subtree:false});
                }
                const top=document.querySelector('#viewWeek .weekTop');
                if(top&&!top.__edt744Observer){
                  top.__edt744Observer=true;
                  new MutationObserver(schedule744).observe(top,{childList:true,subtree:true});
                }

                document.addEventListener('change',event=>{
                  if(event.target&&event.target.id==='feedback663AppLunch')requestAnimationFrame(()=>{
                    try{if(document.getElementById('viewWeek')?.classList.contains('active'))render744();else finish744()}catch(e){}
                  });
                },true);
                document.addEventListener('pointerdown',event=>{
                  const nav=event.target&&event.target.closest?event.target.closest('.bottom .nav[data-mode="week"]'):null;
                  if(nav)requestAnimationFrame(finish744);
                },true);

                finish744();
              }catch(e){console.error('WeekView744Ui',e)}
            })();
            """;
    }
}
