package com.wokgui.schedulewidget;

/** Final week-view owner: colours, free cells, and configurable per-day lunch bands. */
final class WeekAppearance658Ui {
    private WeekAppearance658Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekAppearance658){window.refreshWeekAppearance658&&window.refreshWeekAppearance658();return}
                window.__weekAppearance658=true;
                const KEY='weekAppearance658';
                const DEF={free:'#E6F2FF',course:'#FFFFFF',lunch:'#FFE08A',days:{}};
                const DAYN=['','Dim','Lun','Mar','Mer','Jeu','Ven','Sam'];
                let painting=false,migrated=false;
                function language(){try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr'))}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}
                function toMin(v){const m=String(v||'').match(/([0-2]?[0-9]):([0-5][0-9])/);return m?Number(m[1])*60+Number(m[2]):-1}
                function clock(v){v=Math.max(0,Math.min(1439,Number(v)||0));return String(Math.floor(v/60)).padStart(2,'0')+':'+String(v%60).padStart(2,'0')}
                function colour(value,fallback){return /^#[0-9a-f]{6}$/i.test(String(value||''))?String(value).toUpperCase():fallback}
                function normalize(raw){
                  raw=raw&&typeof raw==='object'?raw:{};
                  const out={free:colour(raw.free,DEF.free),course:colour(raw.course,DEF.course),lunch:colour(raw.lunch,DEF.lunch),days:{}};
                  const days=raw.days&&typeof raw.days==='object'?raw.days:{};
                  [1,2,3,4,5,6,7].forEach(d=>{const item=days[d]||days[String(d)]||{},start=Math.max(0,Math.min(1380,Number(item.start==null?720:item.start)));let end=Math.max(0,Math.min(1440,Number(item.end==null?start+60:item.end)));if(end<=start)end=Math.min(1440,start+60);out.days[d]={enabled:item.enabled!==false,start:start,end:end}});
                  return out;
                }
                function nativeRoot(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function scheduleRoot(){try{return JSON.parse(AndroidSchedule.loadSchedule()||'{}')}catch(e){return {}}}
                function visibleBreakLabel(kind,fallback){
                  try{
                    const fn=kind==='lunch'?window.lunchLabelText:window.gapLabelText;
                    if(typeof fn==='function')return String(fn()||'').split('\u200B').join('').trim().slice(0,28);
                    const root=scheduleRoot(),value=root&&root._breaks?root._breaks[kind+'Label']:fallback;
                    return String(value==null?'':value).split('\u200B').join('').trim().slice(0,28);
                  }catch(e){return fallback}
                }
                function lunchLabel(){const value=visibleBreakLabel('lunch',tr('Midi','Lunch','Mittag'));return /^(pause de midi|lunch break|mittagspause)$/i.test(value)?tr('Midi','Lunch','Mittag'):value}
                function namedGapLabel(){const defaultValue=tr('Trou','Free period','Freistunde'),value=visibleBreakLabel('gap',defaultValue);return value||defaultValue}
                function persistNative(settings,root){
                  try{
                    root=root&&typeof root==='object'?root:nativeRoot();root.weekAppearance658=settings;
                    try{if(typeof adv!=='undefined'&&adv)adv.weekAppearance658=settings}catch(e){}
                    if(window.AndroidSchedule&&AndroidSchedule.saveAdvancedSettings)AndroidSchedule.saveAdvancedSettings(JSON.stringify(root));
                  }catch(e){}
                }
                function load(){
                  let root=nativeRoot(),raw=root.weekAppearance658,fromNative=!!(raw&&typeof raw==='object');
                  if(!fromNative){try{raw=JSON.parse(localStorage.getItem(KEY)||'{}')}catch(e){raw={}}}
                  const out=normalize(raw);
                  if(!fromNative&&!migrated){migrated=true;persistNative(out,root)}
                  return out;
                }
                function save(settings){settings=normalize(settings);try{localStorage.setItem(KEY,JSON.stringify(settings))}catch(e){}persistNative(settings);if(typeof renderWeek==='function')renderWeek();else paint()}
                function rowsOf(grid){
                  const rows=[],heads=[...grid.querySelectorAll(':scope > .wh.day')],times=[...grid.querySelectorAll(':scope > .wh.timecol')],n=heads.length||5;
                  for(const time of times){const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(found.length<2)continue;const cells=[];let node=time.nextElementSibling;while(node&&cells.length<n){if(node.classList&&node.classList.contains('wc'))cells.push(node);node=node.nextElementSibling}if(cells.length===n)rows.push({time:time,start:toMin(found[0]),end:toMin(found[1]),cells:cells})}
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return {rows:rows,heads:heads};
                }
                function dayKey(head,index){const text=String(head&&head.textContent||'').toLowerCase();if(/lun|mon|mo\b/.test(text))return 2;if(/mar|tue|di\b/.test(text))return 3;if(/mer|wed|mi\b/.test(text))return 4;if(/jeu|thu|do\b/.test(text))return 5;if(/ven|fri|fr\b/.test(text))return 6;if(/sam|sat|sa\b/.test(text))return 7;if(/dim|sun|so\b/.test(text))return 1;return [2,3,4,5,6,7,1][index]||index+2}
                function courseCell(cell){return !!(cell&&(cell.classList.contains('has')||cell.querySelector('.cellRoom')))}
                function gapCell(cell){return !!(cell&&cell.classList.contains('gapCell'))}
                function cleanInline(element){['background','background-color','box-shadow','outline','border-left','border-right','border-top','border-bottom','border-left-color','border-right-color','border-top-color','border-bottom-color','border-radius','padding','margin','color'].forEach(p=>element.style.removeProperty(p));element.removeAttribute('data-midi-edge-v14')}
                function cleanCell(cell){
                  cell.classList.remove('week658Course','week658Free','week658Gap','week658Lunch','week658LunchTop','week658LunchBottom','week658LunchRowTop','week658LunchRowBottom','week658LunchCourseBoundary','week662NamedGap');
                  cell.querySelectorAll(':scope > .week658LunchLabel,:scope > .week662GapLabel').forEach(x=>x.remove());cleanInline(cell);
                }
                function courseOverlaps(day,start,end){
                  try{const list=weeks[activeWeek]&&weeks[activeWeek][day]?weeks[activeWeek][day].courses:[];return (list||[]).some(c=>toMin(c.start)<end&&toMin(c.end)>start)}catch(e){return false}
                }
                function paint(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;const settings=load(),advanced=nativeRoot(),showLunch=advanced.showLunchWeek!==false,showGaps=advanced.showBreaksWeek!==false,data=rowsOf(grid);if(!data.rows.length)return;
                  grid.style.setProperty('--week658-free',settings.free);grid.style.setProperty('--week658-course',settings.course);grid.style.setProperty('--week658-lunch',settings.lunch);
                  const gapText=namedGapLabel();
                  data.rows.forEach(row=>{row.time.classList.remove('week658LunchTime','week658LunchTop','week658LunchBottom','week658LunchRowTop','week658LunchRowBottom');cleanInline(row.time);row.cells.forEach(cell=>{cleanCell(cell);if(courseCell(cell))cell.classList.add('week658Course');else{cell.classList.add('week658Free');if(gapCell(cell)){if(showGaps){cell.classList.add('week658Gap');if(gapText){cell.classList.add('week662NamedGap');const label=document.createElement('span');label.className='week662GapLabel';label.textContent=gapText;cell.appendChild(label)}}else{cell.classList.remove('week658Gap','week662NamedGap')}}if(!showLunch&&cell.classList.contains('lunchCell')){cell.classList.remove('lunchCell','dynamicLunchCell');cell.classList.add('emptyCell');cell.textContent=''}}})});
                  data.heads.forEach((head,dayIndex)=>{
                    const day=dayKey(head,dayIndex),cfg=settings.days[day]||{enabled:true,start:720,end:780};if(!showLunch||cfg.enabled===false||courseOverlaps(day,cfg.start,cfg.end))return;
                    const band=data.rows.filter(row=>row.start<cfg.end&&row.end>cfg.start);if(!band.length)return;
                    const painted=[];
                    band.forEach(row=>{const cell=row.cells[dayIndex];if(!cell||courseCell(cell))return;cell.classList.remove('week658Course','week658Free','week658Gap');cell.classList.add('week658Lunch');painted.push(cell)});
                    if(!painted.length)return;painted[0].classList.add('week658LunchTop');painted[painted.length-1].classList.add('week658LunchBottom');
                    const text=lunchLabel();if(text){const label=document.createElement('span');label.className='week658LunchLabel';label.textContent=text;painted[0].appendChild(label)}
                  });
                  const flags=data.rows.map(row=>row.cells.some(cell=>cell.classList.contains('week658Lunch')));let index=0;
                  while(index<flags.length){
                    if(!flags[index]){index++;continue}let last=index;while(last+1<flags.length&&flags[last+1])last++;
                    for(let i=index;i<=last;i++)data.rows[i].time.classList.add('week658LunchTime');
                    const top=data.rows[index],bottom=data.rows[last];
                    [top.time,...top.cells].forEach(cell=>cell.classList.add('week658LunchRowTop'));
                    [bottom.time,...bottom.cells].forEach(cell=>cell.classList.add('week658LunchRowBottom'));
                    index=last+1;
                  }
                }
                const style=document.createElement('style');style.id='weekAppearance658Style';style.textContent=`
                  html.edtWeekFit658,html.edtWeekFit658 body{overflow-y:hidden!important;overscroll-behavior-y:none!important}
                  html.edtWeekFit658 body main.wrap{min-height:0!important;padding-bottom:0!important}
                  html.edtWeekFit658 body #viewWeek .weekScroller{overflow:hidden!important}
                  html.edtWeekFit658 body #viewWeek #weekGrid>.wh,html.edtWeekFit658 body #viewWeek #weekGrid>.wc{height:auto!important;min-height:0!important;max-height:none!important;box-sizing:border-box!important;overflow:hidden!important}
                  html.edtWeekCompact658 body #viewWeek #weekGrid .wc .cellLabel{font-size:.53rem!important;line-height:1!important;-webkit-line-clamp:1!important}
                  html.edtWeekCompact658 body #viewWeek #weekGrid .wc .cellRoom{font-size:.46rem!important;line-height:1!important}
                  html.edtWeekCompact658 body #viewWeek #weekGrid .wh{font-size:.55rem!important;line-height:1!important;padding:1px!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Free{background:var(--week658-free,#E6F2FF)!important;color:#53627a!important;box-shadow:none!important;border-radius:0!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Gap>*,html body #viewWeek #weekGrid#weekGrid .wc.week658Free:is(.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic)>*{display:none!important;visibility:hidden!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Gap.week662NamedGap>.week662GapLabel{position:absolute!important;inset:0!important;display:flex!important;visibility:visible!important;align-items:center!important;justify-content:center!important;padding:1px!important;color:#235f76!important;font-weight:800!important;font-size:11px!important;line-height:1!important;text-align:center!important;z-index:10!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Course{background:var(--week658-course,#fff)!important;box-shadow:none!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Lunch{position:relative!important;background:var(--week658-lunch,#FFE08A)!important;color:#59491d!important;box-shadow:none!important;border-left-color:transparent!important;border-right-color:transparent!important;border-radius:0!important;overflow:hidden!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Lunch>:not(.week658LunchLabel){display:none!important;visibility:hidden!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.week658Lunch:before,html body #viewWeek #weekGrid#weekGrid .wc.week658Lunch:after{content:none!important;display:none!important}
                  html body #viewWeek #weekGrid#weekGrid .week658LunchLabel{position:absolute!important;inset:0!important;display:flex!important;visibility:visible!important;align-items:center!important;justify-content:center!important;width:auto!important;height:auto!important;margin:0!important;padding:0!important;color:#59491d!important;font-family:inherit!important;font-weight:800!important;font-size:13px!important;line-height:1!important;text-align:center!important;white-space:nowrap!important;z-index:10!important}
                  html body #viewWeek #weekGrid#weekGrid .wh.timecol.week658LunchTime{background:var(--week658-lunch,#FFE08A)!important;color:#59491d!important;border-right-color:transparent!important}
                  html body #viewWeek #weekGrid#weekGrid .week658LunchRowTop{box-shadow:inset 0 2px #D5B84D!important;border-top-color:transparent!important}
                  html body #viewWeek #weekGrid#weekGrid .week658LunchRowBottom{box-shadow:inset 0 -2px #D5B84D!important;border-bottom-color:transparent!important}
                  html body #viewWeek #weekGrid#weekGrid .week658LunchRowTop.week658LunchRowBottom{box-shadow:inset 0 2px #D5B84D,inset 0 -2px #D5B84D!important}
                  html body #viewWeek #weekGrid#weekGrid .week658LunchTop{border-top-color:transparent!important}
                  html body #viewWeek #weekGrid#weekGrid .week658LunchBottom{border-bottom-color:transparent!important}
                  #week658Settings{margin:8px 0 0!important;padding:10px!important;border:1px solid #dbe3ef!important;border-radius:10px!important;background:#f8fafc!important;position:static!important;inset:auto!important;width:auto!important;height:auto!important;max-width:none!important;box-shadow:none!important;transform:none!important;z-index:auto!important}
                  #week658Settings .w658Title{text-align:center;font-weight:850;margin:4px 0 9px}
                  #week658Settings .w658Colors{display:grid;grid-template-columns:1fr 52px;gap:7px 10px;align-items:center}
                  #week658Settings input[type=color]{width:48px;height:34px;padding:2px;border:1px solid #ccd5e2;border-radius:7px;background:#fff}
                  #week658Settings .w658Days{margin-top:12px;display:grid;gap:6px}
                  #week658Settings .w658DayHead,#week658Settings .w658Day{display:grid;grid-template-columns:34px 70px minmax(74px,1fr) minmax(74px,1fr);gap:5px;align-items:center}
                  #week658Settings .w658DayHead{font-size:.66rem;color:#667085;text-align:center;font-weight:750}
                  #week658Settings .w658Day label{display:flex;align-items:center;justify-content:center;gap:4px;font-size:.74rem}
                  #week658Settings .w658Day input[type=time]{width:100%;min-width:0;box-sizing:border-box;min-height:34px;border:1px solid #ccd5e2;border-radius:7px;background:#fff;padding:2px;font-size:.70rem}
                  @media(max-width:380px){#week658Settings{padding:8px!important}#week658Settings .w658DayHead,#week658Settings .w658Day{grid-template-columns:30px 56px minmax(68px,1fr) minmax(68px,1fr);gap:3px}#week658Settings .w658Day label{font-size:.68rem;gap:2px}#week658Settings .w658Day input[type=time]{font-size:.64rem;padding:1px}}
                `;document.head.appendChild(style);
                function settingsHost(){const sheet=document.getElementById('settingsSheet');if(!sheet)return null;return sheet.querySelector('#colorSettings86 .settingsSectionBody86')||sheet}
                function installSettings(){
                  const host=settingsHost();if(!host)return;let box=document.getElementById('week658Settings');if(box&&box.querySelector('.w658Start')){if(box.parentNode!==host)host.appendChild(box);return}if(box)box.remove();
                  const settings=load();box=document.createElement('section');box.id='week658Settings';box.className='settingBox';
                  box.innerHTML='<div class="w658Title">'+tr('Couleurs de la vue semaine','Week view colours','Farben der Wochenansicht')+'</div><div class="w658Colors"><span>'+tr('Cases libres','Free cells','Freie Felder')+'</span><input id="w658Free" type="color"><span>'+tr('Cours','Classes','Unterricht')+'</span><input id="w658Course" type="color"><span>'+tr('Midi','Lunch','Mittag')+'</span><input id="w658Lunch" type="color"></div><div class="w658Days"><div class="w658Title">'+tr('Midi par jour','Lunch by day','Mittag pro Tag')+'</div><div class="w658DayHead"><span></span><span>'+tr('Afficher','Show','Anzeigen')+'</span><span>'+tr('Début','Start','Beginn')+'</span><span>'+tr('Fin','End','Ende')+'</span></div></div>';
                  const actions=document.querySelector('#settingsSheet .settingsActions');if(host.id==='settingsSheet'&&actions)host.insertBefore(box,actions);else host.appendChild(box);
                  box.querySelector('#w658Free').value=settings.free;box.querySelector('#w658Course').value=settings.course;box.querySelector('#w658Lunch').value=settings.lunch;
                  [['w658Free','free'],['w658Course','course'],['w658Lunch','lunch']].forEach(([id,key])=>box.querySelector('#'+id).addEventListener('input',event=>{const value=load();value[key]=event.target.value;save(value)}));
                  const days=box.querySelector('.w658Days');[2,3,4,5,6,7,1].forEach(day=>{
                    if(!document.documentElement.classList.contains('weekendScheduleEnabled')&&(day===7||day===1))return;
                    const cfg=settings.days[day]||{enabled:true,start:720,end:780},row=document.createElement('div');row.className='w658Day';
                    row.innerHTML='<b>'+DAYN[day]+'</b><label><input class="w658Enabled" type="checkbox" '+(cfg.enabled===false?'':'checked')+'><span>'+tr('Midi','Lunch','Mittag')+'</span></label><input class="w658Start" type="time" step="300" value="'+clock(cfg.start)+'"><input class="w658End" type="time" step="300" value="'+clock(cfg.end)+'">';
                    const enabled=row.querySelector('.w658Enabled'),start=row.querySelector('.w658Start'),end=row.querySelector('.w658End');
                    const commit=()=>{const value=load(),startValue=toMin(start.value);let endValue=toMin(end.value);if(startValue<0)return;if(endValue<=startValue){endValue=Math.min(1440,startValue+60);end.value=clock(endValue)}value.days[day]={enabled:enabled.checked,start:startValue,end:endValue};save(value)};
                    enabled.onchange=commit;start.onchange=commit;end.onchange=commit;days.appendChild(row);
                  });
                }
                function paintOnce(){if(painting)return;painting=true;try{paint();const grid=document.getElementById('weekGrid');if(grid)grid.__weekAppearanceLastNode=grid.lastElementChild}finally{painting=false}}
                function fitWeek(){
                  const view=document.getElementById('viewWeek'),grid=document.getElementById('weekGrid');
                  const scroller=view&&view.querySelector('.weekScroller'),stage=document.querySelector('main.wrap'),bottom=document.querySelector('.bottom');
                  if(!view||!grid||!scroller||!stage||!bottom)return;
                  const active=view.classList.contains('active');
                  document.documentElement.classList.toggle('edtWeekFit658',active);
                  if(!active){document.documentElement.classList.remove('edtWeekCompact658');grid.style.removeProperty('height');grid.style.removeProperty('grid-auto-rows');scroller.style.removeProperty('height');view.style.removeProperty('height');stage.style.removeProperty('height');return}
                  const available=Math.max(120,Math.floor(bottom.getBoundingClientRect().top-scroller.getBoundingClientRect().top-2));
                  const columns=Math.max(1,grid.querySelectorAll(':scope > .wh.day').length+1),rows=Math.max(1,Math.ceil(grid.children.length/columns));
                  grid.style.height=available+'px';grid.style.gridAutoRows='minmax(0,1fr)';scroller.style.height=available+'px';
                  document.documentElement.classList.toggle('edtWeekCompact658',available/rows<38);
                  view.style.height=Math.ceil(scroller.getBoundingClientRect().top-view.getBoundingClientRect().top+available)+'px';
                  stage.style.height=Math.ceil(bottom.getBoundingClientRect().top-stage.getBoundingClientRect().top)+'px';
                  if(scrollY!==0)scrollTo(0,0);
                }
                function refresh(){installSettings();fitWeek();paintOnce()}
                window.refreshWeekAppearance658=refresh;
                window.fitActiveWeek658=()=>{fitWeek();paintOnce()};
                const oldWeek=window.renderWeek;
                if(typeof oldWeek==='function'){
                  const renderCompleteWeek=function(){const result=oldWeek.apply(this,arguments);fitWeek();paintOnce();return result};
                  window.renderWeek=renderCompleteWeek;try{renderWeek=renderCompleteWeek}catch(e){}
                }
                addEventListener('resize',()=>{fitWeek();paintOnce()},{passive:true});
                refresh();
              }catch(e){console.error('WeekAppearance658Ui',e)}
            })();
            """;
    }
}
