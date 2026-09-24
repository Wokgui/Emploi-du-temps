package com.wokgui.schedulewidget;

/** Owns calendar navigation and dated Today/Week headers. */
final class CalendarNavigation757Ui {
    private CalendarNavigation757Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__calendarNavigation757){if(window.refreshCalendarNavigation757)window.refreshCalendarNavigation757();return}
                window.__calendarNavigation757=true;
                let dayOffset757=0,weekOffset757=0;
                const DAY_MS757=86400000,WEEK_MS757=604800000;
                function start757(value){const d=new Date(value||Date.now());d.setHours(0,0,0,0);return d}
                function add757(value,n){const d=start757(value);d.setDate(d.getDate()+Number(n||0));return d}
                function monday757(value){const d=start757(value),js=d.getDay();d.setDate(d.getDate()+(js===0?-6:1-js));return d}
                function same757(a,b){return a.getFullYear()===b.getFullYear()&&a.getMonth()===b.getMonth()&&a.getDate()===b.getDate()}
                function dayKey757(d){const js=d.getDay();return js===0?1:js+1}
                function key757(d){return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')}
                function language757(){const l=String(document.documentElement.lang||'fr').toLowerCase();return l.startsWith('de')?'de':(l.startsWith('en')?'en':'fr')}
                function tr757(fr,en,de){const l=language757();return l==='en'?en:(l==='de'?de:fr)}
                function locale757(){return language757()==='en'?'en-GB':(language757()==='de'?'de-DE':'fr-FR')}
                function short757(d){return String(d.getDate()).padStart(2,'0')+'/'+String(d.getMonth()+1).padStart(2,'0')}
                function long757(d){return d.toLocaleDateString(locale757(),{day:'numeric',month:'long',year:'numeric'})}
                function weekday757(d){const s=d.toLocaleDateString(locale757(),{weekday:'long'});return s?s.charAt(0).toUpperCase()+s.slice(1):''}
                function dayName757(i){const n={fr:['Lun','Mar','Mer','Jeu','Ven','Sam','Dim'],en:['Mon','Tue','Wed','Thu','Fri','Sat','Sun'],de:['Mo','Di','Mi','Do','Fr','Sa','So']};return n[language757()][i]||n.fr[i]||''}
                function loadAdv757(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function cycle757(){const a=loadAdv757();if(a.singleWeek===true)return 1;return Math.max(2,Math.min(4,Number(a.cycleLength)||2))}
                function current757(){try{return String(currentWeek||'A').toUpperCase()}catch(e){return 'A'}}
                function weekLetter757(date){const count=cycle757();if(count===1)return 'A';const letters=['A','B','C','D'].slice(0,count),base=Math.max(0,letters.indexOf(current757())),diff=Math.round((monday757(date)-monday757(new Date()))/WEEK_MS757);return letters[((base+diff)%count+count)%count]||'A'}
                function viewedDate757(){return add757(new Date(),dayOffset757)}
                function viewedMonday757(){return add757(monday757(new Date()),weekOffset757*7)}
                function actual757(d){return same757(start757(d),start757(new Date()))}
                window.edtViewedDate757=viewedDate757;window.edtWeekLetterForDate757=weekLetter757;window.edtDayKeyForDate757=dayKey757;window.edtIsActualDate757=actual757;

                const style=document.createElement('style');style.id='calendarNavigation757Style';style.textContent=
                  '#viewToday .dayTitle{display:grid!important;grid-template-columns:36px minmax(0,1fr) 36px!important;grid-template-rows:auto auto!important;align-items:center!important;column-gap:5px!important}'+
                  '#todayPrev757{grid-column:1!important;grid-row:1!important}#todayTitle{grid-column:2!important;grid-row:1!important;margin:0!important;text-align:center!important}#todayNext757{grid-column:3!important;grid-row:1!important}#todayDate{grid-column:2!important;grid-row:2!important;text-align:center!important;margin-top:2px!important}'+
                  '#viewWeek .weekTop{display:grid!important;grid-template-columns:36px minmax(0,1fr) 36px!important;grid-template-rows:auto auto!important;align-items:center!important;column-gap:5px!important}'+
                  '#weekPrev757{grid-column:1!important;grid-row:1!important}#viewWeek .weekTop>h2{grid-column:2!important;grid-row:1!important;margin:0!important;text-align:center!important}#weekNext757{grid-column:3!important;grid-row:1!important}'+
                  '#weekCycleLabel757{grid-column:2!important;grid-row:2!important;text-align:center!important;margin-top:2px!important;font-size:.70rem!important;font-weight:800!important;color:var(--muted,#68738a)!important}'+
                  '.calendarArrow757{width:34px!important;height:34px!important;min-width:34px!important;padding:0!important;border:0!important;border-radius:50%!important;background:transparent!important;color:var(--ink,#111936)!important;font-size:1.28rem!important;font-weight:700!important;line-height:34px!important;text-align:center!important;box-shadow:none!important}'+
                  '.calendarArrow757:active{background:#edf3f9!important}#weekGrid .wh.day{display:flex!important;flex-direction:column!important;align-items:center!important;justify-content:center!important;gap:1px!important;line-height:1.05!important}'+
                  '#weekGrid .weekDayDate757{display:block!important;margin-top:2px!important;font-size:.52rem!important;font-weight:700!important;color:var(--muted,#68738a)!important}#viewWeek .weekLetter{display:none!important}'+
                  'html.edtOtherWeek757 #weekGrid .now70Bar,html.edtOtherWeek757 #weekGrid .now70Dot,html.edtOtherWeek757 #weekGrid [id*=weekNow],html.edtOtherWeek757 #weekGrid [id*=WeekNow]{display:none!important}';
                document.head.appendChild(style);

                function arrow757(id,label){let b=document.getElementById(id);if(!b){b=document.createElement('button');b.id=id;b.type='button';b.className='calendarArrow757';b.textContent=label}return b}
                function ensureToday757(){const bar=document.querySelector('#viewToday .dayTitle'),h=document.getElementById('todayTitle');if(!bar||!h)return;const l=arrow757('todayPrev757','‹'),r=arrow757('todayNext757','›');l.setAttribute('aria-label',tr757('Jour précédent','Previous day','Vorheriger Tag'));r.setAttribute('aria-label',tr757('Jour suivant','Next day','Nächster Tag'));if(l.parentNode!==bar)bar.insertBefore(l,h);if(r.parentNode!==bar)bar.appendChild(r);if(!l.__c757){l.__c757=true;l.onclick=e=>{e.preventDefault();dayOffset757--;renderToday757()}}if(!r.__c757){r.__c757=true;r.onclick=e=>{e.preventDefault();dayOffset757++;renderToday757()}}}
                function ensureWeek757(){const bar=document.querySelector('#viewWeek .weekTop'),h=bar&&bar.querySelector('h2');if(!bar||!h)return;const l=arrow757('weekPrev757','‹'),r=arrow757('weekNext757','›');l.setAttribute('aria-label',tr757('Semaine précédente','Previous week','Vorherige Woche'));r.setAttribute('aria-label',tr757('Semaine suivante','Next week','Nächste Woche'));if(l.parentNode!==bar)bar.insertBefore(l,h);if(r.parentNode!==bar)bar.appendChild(r);let s=document.getElementById('weekCycleLabel757');if(!s){s=document.createElement('div');s.id='weekCycleLabel757';bar.appendChild(s)}if(!l.__c757){l.__c757=true;l.onclick=e=>{e.preventDefault();weekOffset757--;renderWeek757()}}if(!r.__c757){r.__c757=true;r.onclick=e=>{e.preventDefault();weekOffset757++;renderWeek757()}}}
                function todayHead757(date,letter){ensureToday757();const h=document.getElementById('todayTitle'),d=document.getElementById('todayDate');if(h)h.textContent=weekday757(date)+(cycle757()>1?' · '+tr757('semaine ','week ','Woche ')+letter:'');if(d)d.textContent=long757(date)}
                function weekHead757(){ensureWeek757();const begin=viewedMonday757(),end=add757(begin,6),letter=weekLetter757(begin),h=document.querySelector('#viewWeek .weekTop h2'),s=document.getElementById('weekCycleLabel757');if(h){h.textContent=tr757('Semaine du ','Week ','Woche ')+short757(begin)+tr757(' au ',' to ',' bis ')+short757(end);let keep=document.getElementById('weekTitleLetter');if(!keep){keep=document.createElement('span');keep.id='weekTitleLetter';keep.className='weekLetter';h.appendChild(keep)}keep.textContent=letter}if(s)s.textContent=tr757('Semaine ','Week ','Woche ')+letter;document.documentElement.classList.toggle('edtOtherWeek757',weekOffset757!==0);[...document.querySelectorAll('#weekGrid .wh.day')].forEach((cell,i)=>{const date=add757(begin,i);cell.textContent='';const name=document.createElement('span');name.textContent=dayName757(i);const sm=document.createElement('small');sm.className='weekDayDate757';sm.textContent=short757(date);cell.append(name,sm)})}

                function esc757(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;')}
                function min757(v){const p=String(v||'0:0').split(':');return (Number(p[0])||0)*60+(Number(p[1])||0)}
                function clock757(m){return String(Math.floor(m/60)).padStart(2,'0')+':'+String(m%60).padStart(2,'0')}
                function effective757(date){try{if(AndroidSchedule.loadEffectiveCourses){const raw=AndroidSchedule.loadEffectiveCourses(key757(date));if(raw){const x=JSON.parse(raw);if(x&&Array.isArray(x.courses))return x}}}catch(e){}const w=weekLetter757(date),d=dayKey757(date);try{return {courses:weeks[w]&&weeks[w][d]&&Array.isArray(weeks[w][d].courses)?weeks[w][d].courses:[],dayOff:false}}catch(e){return {courses:[],dayOff:false}}}
                function gaps757(list){try{if(window.dynamicGapSegments)return window.dynamicGapSegments(list)||[]}catch(e){}const out=[],a=list.slice().sort((x,y)=>min757(x.start)-min757(y.start));for(let i=0;i<a.length-1;i++){const s=min757(a[i].end),e=min757(a[i+1].start);if(e>s)out.push({start:s,end:e})}return out}
                function lunch757(list){try{return window.lunchForDay?window.lunchForDay(list):null}catch(e){return null}}
                function renderViewedToday757(){
                  const date=viewedDate757(),letter=weekLetter757(date);if(dayOffset757===0){todayHead757(date,current757());return}
                  const day=dayKey757(date),eff=effective757(date),list=Array.isArray(eff.courses)?eff.courses.slice():[],box=document.getElementById('todayList'),adv=loadAdv757();todayHead757(date,letter);if(!box)return;box.innerHTML='';const banner=document.getElementById('exceptionTodayBanner');if(banner)banner.remove();const progress=document.getElementById('todayProgress');
                  if(eff.dayOff){box.innerHTML='<div class="empty">'+tr757('Jour sans cours','No class','Unterrichtsfrei')+'</div>';if(progress)progress.style.width='0%';return}
                  if(!list.length){box.innerHTML='<div class="empty">'+tr757('Aucun cours ce jour.','No class that day.','An diesem Tag kein Unterricht.')+'</div>';if(progress)progress.style.width='0%';return}
                  const actual=actual757(date),now=new Date(),nowM=now.getHours()*60+now.getMinutes(),events=list.map(c=>({type:'course',start:min757(c.start),end:min757(c.end),course:c}));
                  if(adv.showBreaksToday!==false)gaps757(list).forEach(g=>events.push({type:'gap',start:g.start,end:g.end}));const lunch=lunch757(list);if(lunch&&adv.showLunchToday!==false)events.push({type:'lunch',start:lunch.startM,end:lunch.endM,l:lunch});events.sort((a,b)=>a.start-b.start||a.end-b.end);
                  let total=0,done=0;list.forEach(c=>{const s=min757(c.start),e=min757(c.end),dur=Math.max(0,e-s);total+=dur;if(actual){if(nowM>=e)done+=dur;else if(nowM>s)done+=Math.min(dur,nowM-s)}});if(progress)progress.style.width=(actual?(total?Math.max(0,Math.min(100,done*100/total)):0):(date<start757(new Date())?100:0))+'%';
                  const ss=document.getElementById('scaleStart'),se=document.getElementById('scaleEnd');if(ss)ss.textContent=list[0].start||'';if(se)se.textContent=list[list.length-1].end||'';
                  events.forEach(ev=>{const row=document.createElement('div');if(ev.type==='course'){const c=ev.course,cur=actual&&nowM>=ev.start&&nowM<ev.end;row.className='todayCourse'+(cur?' current':'');row.innerHTML='<div class="time"><strong>'+esc757(c.start)+'</strong><br>'+esc757(c.end)+'</div><div><div class="label">'+esc757(c.label)+'</div><div class="room">'+tr757('salle ','room ','Raum ')+esc757(c.room||'—')+'</div></div>'+(cur?'<div class="badge">'+tr757('En cours','In class','Läuft')+'</div>':'');try{const base=weeks[letter]&&weeks[letter][day]?weeks[letter][day].courses:[],i=base.findIndex(x=>x.start===c.start&&x.label===c.label);if(i>=0)row.onclick=()=>{activeWeek=letter;selected=day;editing=i;openEditor(i)}}catch(e){}}else if(ev.type==='gap'){row.className='todayCourse gap';row.innerHTML='<div class="time"><strong>'+clock757(ev.start)+'</strong><br>'+clock757(ev.end)+'</div><div><div class="label">'+esc757(window.gapLabelText?window.gapLabelText():'Trou')+'</div></div>'}else{row.className='todayCourse lunch';row.innerHTML='<div class="time"><strong>'+esc757(ev.l.start)+'</strong><br>'+esc757(ev.l.end)+'</div><div><div class="label">'+esc757(window.lunchLabelText?window.lunchLabelText():'Midi')+'</div></div>'}box.appendChild(row)});
                  try{if(window.syncBreakCells)window.syncBreakCells();if(window.refreshCourseColors)window.refreshCourseColors()}catch(e){}
                }
                function renderToday757(){try{window.renderToday()}catch(e){renderViewedToday757()}}
                function renderWeek757(){try{activeWeek=weekLetter757(viewedMonday757())}catch(e){}try{window.renderWeek()}catch(e){weekHead757()}}
                function wrap757(name,before,after){const old=window[name];if(typeof old!=='function'||old.__calendar757)return;const w=function(){if(before)try{before()}catch(e){}const r=old.apply(this,arguments);if(after)try{after()}catch(e){}return r};w.__calendar757=true;window[name]=w;try{eval(name+'=w')}catch(e){}}
                function install757(){wrap757('renderToday',null,renderViewedToday757);wrap757('renderWeek',()=>{try{activeWeek=weekLetter757(viewedMonday757())}catch(e){}},weekHead757)}
                function offset757(letter){const count=cycle757();if(count===1)return 0;const letters=['A','B','C','D'].slice(0,count),a=Math.max(0,letters.indexOf(current757())),b=letters.indexOf(String(letter||'A').toUpperCase());if(b<0)return 0;let d=(b-a+count)%count;if(d>count/2)d-=count;return d}
                document.addEventListener('pointerdown',e=>{const t=e.target&&e.target.closest?e.target.closest('.weekTab[data-week]'):null;if(t)weekOffset757=offset757(t.dataset.week);const p=e.target&&e.target.closest?e.target.closest('#weekPicker79 button'):null;if(p)weekOffset757=0},true);
                document.addEventListener('change',e=>{if(e.target&&e.target.id==='languageSelect')requestAnimationFrame(()=>{renderViewedToday757();weekHead757()})},true);
                function refresh757(){install757();ensureToday757();ensureWeek757();renderViewedToday757();weekHead757()}
                window.refreshCalendarNavigation757=refresh757;refresh757();
              }catch(e){console.error('CalendarNavigation757Ui',e)}
            })();
            """;
    }
}
