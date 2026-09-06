package com.wokgui.schedulewidget;

final class CourseColorUi {
    private CourseColorUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__courseColorUiV2){
                  if(window.refreshCourseColors)window.refreshCourseColors();
                  return;
                }
                window.__courseColorUiV2=true;

                const COLORS={
                  butter:{bg:'#FFE078',edge:'#A86D00'},
                  apricot:{bg:'#FFBC68',edge:'#C56000'},
                  peach:{bg:'#FF9B7A',edge:'#BA4327'},
                  coral:{bg:'#FF7A67',edge:'#A92F24'},
                  terracotta:{bg:'#D98A6C',edge:'#81412E'},
                  rose:{bg:'#F08AA3',edge:'#9C3554'},
                  berry:{bg:'#D45E83',edge:'#732540'},
                  plum:{bg:'#A97191',edge:'#58364D'},
                  sand:{bg:'#D8AA5D',edge:'#80520F'},
                  olive:{bg:'#C1AF4A',edge:'#665B12'},

                  // Anciennes couleurs conservées pour les emplois du temps déjà personnalisés.
                  blue:{bg:'#DCEBFF',edge:'#0877F9'},
                  cyan:{bg:'#DFF7FA',edge:'#0097A7'},
                  teal:{bg:'#DDF4F0',edge:'#00897B'},
                  green:{bg:'#E3F3E4',edge:'#2E7D32'},
                  yellow:{bg:'#FFF5CC',edge:'#C99800'},
                  orange:{bg:'#FFE8D4',edge:'#EF6C00'},
                  violet:{bg:'#EDE7F6',edge:'#6750A4'},
                  red:{bg:'#FDE8E8',edge:'#D84343'},
                  graphite:{bg:'#ECEFF1',edge:'#546E7A'}
                };
                const PALETTE=['butter','apricot','peach','coral','terracotta','rose','berry','plum'];
                let picked='';
                let scope='cell';

                function language(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function label(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}
                function norm(s){return String(s||'').trim().replace(/ +/g,' ').toLocaleLowerCase()}

                const style=document.createElement('style');
                style.textContent=`
                  .courseColorField{margin-bottom:9px}
                  .courseColorLabel{display:block;color:var(--muted);font-size:.74rem;margin-bottom:5px}
                  .courseColorPalette{display:flex;flex-wrap:wrap;gap:7px;align-items:center}
                  .courseColorChoice{width:30px;height:30px;border-radius:50%;border:2px solid #fff;box-shadow:0 0 0 1px #cbd5e1;position:relative;padding:0}
                  .courseColorChoice.active{box-shadow:0 0 0 3px var(--blue)}
                  .courseColorChoice.none{background:#fff}
                  .courseColorChoice.none:before,.courseColorChoice.none:after{content:'';position:absolute;left:4px;right:4px;top:12px;height:2px;background:#a5afbd;transform:rotate(-40deg)}
                  .courseColorChoice.none:after{transform:rotate(40deg)}
                  .courseColorHint{font-size:.66rem;color:var(--muted);margin-top:5px;line-height:1.25}
                  .courseColorScope{display:flex;gap:5px;margin-top:8px}
                  .courseColorScope button{flex:1;min-width:0;border:1px solid var(--line);background:#fff;color:var(--muted);border-radius:999px;padding:7px 8px;font-size:.72rem;font-weight:800}
                  .courseColorScope button.active{background:var(--blue);border-color:var(--blue);color:#fff}

                  #todayList,#weekGrid{position:relative}
                  .scheduleNowRail{position:absolute;z-index:5;width:2px;background:color-mix(in srgb,var(--blue) 55%,transparent);border-radius:2px;pointer-events:none}
                  .scheduleNowDot{position:absolute;z-index:6;width:10px;height:10px;border-radius:50%;background:var(--blue);box-shadow:0 0 0 3px color-mix(in srgb,var(--blue) 18%,white);pointer-events:none;transform:translate(-4px,-5px)}
                  .scheduleNowTime{position:absolute;z-index:6;pointer-events:none;background:var(--blue);color:#fff;border-radius:999px;padding:2px 5px;font-size:.57rem;font-weight:850;line-height:1.1;white-space:nowrap;transform:translate(7px,-50%)}
                  #todayList .scheduleNowRail{left:69px;top:4px;bottom:4px}
                  #todayList .scheduleNowDot{left:69px}
                  #todayList .scheduleNowTime{left:69px}
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot{transition:top .18s ease,left .18s ease}
                `;
                document.head.appendChild(style);

                function ensurePicker(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseColorField');
                  if(!field){
                    field=document.createElement('div');field.id='courseColorField';field.className='courseColorField';
                    field.innerHTML='<span id="courseColorLabel" class="courseColorLabel"></span><div id="courseColorPalette" class="courseColorPalette"></div><div id="courseColorScope" class="courseColorScope"><button id="scopeCell" type="button"></button><button id="scopeClass" type="button"></button></div><div id="courseColorHint" class="courseColorHint"></div>';
                    const actions=form.querySelector('.sheetActions');form.insertBefore(field,actions||null);
                    const palette=document.getElementById('courseColorPalette');
                    const none=document.createElement('button');none.type='button';none.className='courseColorChoice none';none.dataset.color='';none.setAttribute('aria-label','Aucune couleur');palette.appendChild(none);
                    PALETTE.forEach(id=>{const b=document.createElement('button');b.type='button';b.className='courseColorChoice';b.dataset.color=id;b.style.background=COLORS[id].bg;b.style.borderColor=COLORS[id].edge;b.setAttribute('aria-label',id);palette.appendChild(b)});
                    palette.querySelectorAll('.courseColorChoice').forEach(b=>b.onclick=()=>selectColor(b.dataset.color||''));
                    document.getElementById('scopeCell').onclick=()=>selectScope('cell');
                    document.getElementById('scopeClass').onclick=()=>selectScope('class');
                  }
                  const title=document.getElementById('courseColorLabel');if(title)title.textContent=label('Couleur de la case','Cell colour','Farbe des Feldes');
                  const cell=document.getElementById('scopeCell');if(cell)cell.textContent=label('Cette case','This cell','Dieses Feld');
                  const cls=document.getElementById('scopeClass');if(cls)cls.textContent=label('Toute la classe','Whole class','Ganze Klasse');
                  const hint=document.getElementById('courseColorHint');if(hint)hint.textContent=scope==='class'?label('La couleur sera appliquée à toutes les cases portant le même nom de classe, dans toutes les semaines.','The colour will be applied to every cell with the same class name, in all weeks.','Die Farbe wird auf alle Felder mit demselben Klassennamen in allen Wochen angewendet.'):label('La couleur sera appliquée uniquement à cette case.','The colour will only be applied to this cell.','Die Farbe wird nur auf dieses Feld angewendet.');
                }

                function selectColor(id){
                  picked=COLORS[id]?id:'';
                  document.querySelectorAll('.courseColorChoice').forEach(b=>b.classList.toggle('active',(b.dataset.color||'')===picked));
                }
                function selectScope(value){
                  scope=value==='class'?'class':'cell';
                  const a=document.getElementById('scopeCell'),b=document.getElementById('scopeClass');
                  if(a)a.classList.toggle('active',scope==='cell');if(b)b.classList.toggle('active',scope==='class');
                  ensurePicker();
                }

                function currentEditedCourse(){
                  try{if(typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof selected==='undefined'||typeof editing==='undefined')return null;if(editing==null)return null;return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null}catch(e){return null}
                }

                function applyCourseColor(el,id,gradient){
                  if(!el)return;
                  const c=COLORS[id];
                  el.style.background='';el.style.boxShadow='';
                  if(!c)return;
                  el.style.background=gradient?('linear-gradient(90deg,'+c.bg+' 0%,#ffffff 97%)'):c.bg;
                  el.style.boxShadow='inset 0 0 0 1px '+c.edge;
                }

                function decorateWeek(){
                  try{
                    const grid=document.getElementById('weekGrid');if(!grid||typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof uniqueWeekTimes!=='function')return;
                    const ws=weeks[activeWeek];if(!ws)return;
                    const times=uniqueWeekTimes();const cells=Array.from(grid.querySelectorAll('.wc'));let p=0;
                    for(const t of times){for(const d of [2,3,4,5,6]){const cell=cells[p++];if(!cell)continue;const c=ws[d]&&ws[d].courses?ws[d].courses.find(x=>x.start===t.start&&x.end===t.end):null;applyCourseColor(cell,c&&c.color?c.color:'',false)}}
                  }catch(e){}
                }

                function decorateEdit(){
                  try{
                    if(typeof state==='undefined'||typeof selected==='undefined')return;
                    const list=state[selected]&&state[selected].courses?state[selected].courses:[];
                    document.querySelectorAll('#editList .editCourse').forEach((row,i)=>applyCourseColor(row,list[i]&&list[i].color?list[i].color:'',true));
                  }catch(e){}
                }

                function decorateToday(){
                  try{
                    if(typeof weeks==='undefined'||typeof currentWeek==='undefined'||typeof todayKey!=='function')return;
                    const d=todayKey(),list=weeks[currentWeek]&&weeks[currentWeek][d]?weeks[currentWeek][d].courses:[];
                    document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{
                      const start=(row.querySelector('.time strong')||{}).textContent||'';
                      const text=(row.querySelector('.label')||{}).textContent||'';
                      let c=list.find(x=>x.start===start&&text.indexOf(x.label)>=0);if(!c)c=list.find(x=>x.start===start);
                      applyCourseColor(row,c&&c.color?c.color:'',true);
                    });
                  }catch(e){}
                }

                function ensureNowParts(parent,prefix){
                  if(!parent)return null;
                  let rail=document.getElementById(prefix+'NowRail'),dot=document.getElementById(prefix+'NowDot'),time=document.getElementById(prefix+'NowTime');
                  if(!rail){rail=document.createElement('div');rail.id=prefix+'NowRail';rail.className='scheduleNowRail';parent.appendChild(rail)}
                  if(!dot){dot=document.createElement('div');dot.id=prefix+'NowDot';dot.className='scheduleNowDot';parent.appendChild(dot)}
                  if(prefix==='today'&&!time){time=document.createElement('div');time.id=prefix+'NowTime';time.className='scheduleNowTime';parent.appendChild(time)}
                  return {rail,dot,time};
                }

                function rowRange(row){
                  const time=row?row.querySelector('.time'):null;if(!time)return null;
                  const txt=time.textContent||'';const found=txt.match(/[0-2]?[0-9]:[0-5][0-9]/g);if(!found||found.length<2)return null;
                  return {start:min(found[0]),end:min(found[1])};
                }

                function updateTodayNow(){
                  try{
                    const box=document.getElementById('todayList');if(!box)return;
                    const parts=ensureNowParts(box,'today');
                    const now=new Date(),jsDay=now.getDay(),wanted=typeof todayKey==='function'?todayKey():null;
                    const isToday=wanted===jsDay+1;
                    if(!isToday){parts.rail.style.display=parts.dot.style.display='none';if(parts.time)parts.time.style.display='none';return}
                    const m=now.getHours()*60+now.getMinutes();let target=null,frac=0;
                    for(const row of box.querySelectorAll('.todayCourse')){const r=rowRange(row);if(r&&m>=r.start&&m<=r.end){target=row;frac=(m-r.start)/Math.max(1,r.end-r.start);break}}
                    if(!target){parts.rail.style.display=parts.dot.style.display='none';if(parts.time)parts.time.style.display='none';return}
                    const y=target.offsetTop+Math.max(2,Math.min(target.offsetHeight-2,target.offsetHeight*frac));
                    parts.rail.style.display=parts.dot.style.display='block';parts.dot.style.top=y+'px';
                    if(parts.time){parts.time.style.display='block';parts.time.style.top=y+'px';parts.time.textContent=String(now.getHours()).padStart(2,'0')+':'+String(now.getMinutes()).padStart(2,'0')}
                  }catch(e){}
                }

                function updateWeekNow(){
                  try{
                    const grid=document.getElementById('weekGrid');if(!grid||typeof uniqueWeekTimes!=='function')return;
                    const parts=ensureNowParts(grid,'week');const now=new Date(),jsDay=now.getDay();
                    if(jsDay<1||jsDay>5){parts.rail.style.display=parts.dot.style.display='none';return}
                    const times=uniqueWeekTimes();const m=now.getHours()*60+now.getMinutes();let rowIndex=-1,frac=0;
                    for(let i=0;i<times.length;i++){const s=min(times[i].start),e=min(times[i].end);if(m>=s&&m<=e){rowIndex=i;frac=(m-s)/Math.max(1,e-s);break}}
                    const cells=Array.from(grid.querySelectorAll('.wc'));
                    if(rowIndex<0||!cells.length){parts.rail.style.display=parts.dot.style.display='none';return}
                    const dayIndex=jsDay-1;const first=cells[dayIndex],target=cells[rowIndex*5+dayIndex],last=cells[(times.length-1)*5+dayIndex];
                    if(!first||!target||!last){parts.rail.style.display=parts.dot.style.display='none';return}
                    const left=target.offsetLeft+4,top=first.offsetTop+2,bottom=last.offsetTop+last.offsetHeight-2;
                    parts.rail.style.display=parts.dot.style.display='block';parts.rail.style.left=left+'px';parts.rail.style.top=top+'px';parts.rail.style.height=Math.max(2,bottom-top)+'px';
                    parts.dot.style.left=left+'px';parts.dot.style.top=(target.offsetTop+Math.max(2,Math.min(target.offsetHeight-2,target.offsetHeight*frac)))+'px';
                  }catch(e){}
                }

                function updateNowMarkers(){updateTodayNow();updateWeekNow()}
                function decorateAll(){decorateWeek();decorateEdit();decorateToday();setTimeout(updateNowMarkers,0)}

                function syncPicker(){ensurePicker();const c=currentEditedCourse();selectColor(c&&c.color?c.color:'');selectScope('cell')}

                const modal=document.getElementById('modal');
                if(modal){new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(syncPicker,0)}).observe(modal,{attributes:true,attributeFilter:['class']})}

                const form=document.getElementById('courseForm');
                if(form&&form.onsubmit&&!form.onsubmit.__courseColorWrappedV2){
                  const oldSubmit=form.onsubmit;
                  const wrapped=function(e){
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A';
                    const day=typeof selected!=='undefined'?selected:2;
                    const idx=typeof editing!=='undefined'?editing:null;
                    const old=(idx!=null&&weeks[week]&&weeks[week][day])?weeks[week][day].courses[idx]:null;
                    const oldClass=old?old.label:'';
                    const pre=typeof newPrefill!=='undefined'&&newPrefill?{start:newPrefill.start,end:newPrefill.end}:null;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0);
                    const text=((document.getElementById('fLabel')||{}).value||'').trim();
                    let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}else if(old){start=old.start;end=old.end}else if(pre){start=pre.start;end=pre.end}
                    const chosen=picked,chosenScope=scope;
                    const result=oldSubmit.call(this,e);
                    try{
                      const arr=weeks[week]&&weeks[week][day]?weeks[week][day].courses:[];
                      let target=arr.find(c=>c.start===start&&c.end===end&&c.label===text);if(!target)target=arr.find(c=>c.start===start&&c.end===end);
                      if(target)target.color=chosen;
                      if(chosenScope==='class'){
                        const match=norm(oldClass||text);
                        Object.keys(weeks).forEach(w=>{const ws=weeks[w];if(!ws)return;Object.keys(ws).forEach(d=>{const dd=ws[d];if(!dd||!Array.isArray(dd.courses))return;dd.courses.forEach(c=>{if(norm(c.label)===match)c.color=chosen})})});
                        if(target)target.color=chosen;
                      }
                      if(typeof save==='function')save();
                    }catch(err){}
                    setTimeout(decorateAll,0);
                    return result;
                  };
                  wrapped.__courseColorWrappedV2=true;form.onsubmit=wrapped;
                }

                ['weekGrid','editList','todayList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(decorateAll,0)).observe(el,{childList:true})});
                window.addEventListener('resize',()=>setTimeout(updateNowMarkers,20));
                setInterval(updateNowMarkers,60000);

                function refresh(){ensurePicker();decorateAll();if(modal&&modal.classList.contains('show'))syncPicker()}
                window.refreshCourseColors=refresh;
                refresh();
              }catch(e){console.log('Course colours',e)}
            })();
            """;
    }
}
