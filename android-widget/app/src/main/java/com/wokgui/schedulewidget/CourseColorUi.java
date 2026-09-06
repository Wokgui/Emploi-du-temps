package com.wokgui.schedulewidget;

final class CourseColorUi {
    private CourseColorUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__courseColorUiV1){
                  if(window.refreshCourseColors)window.refreshCourseColors();
                  return;
                }
                window.__courseColorUiV1=true;

                const COLORS={
                  blue:{bg:'#DCEBFF',edge:'#0877F9'},
                  cyan:{bg:'#DFF7FA',edge:'#0097A7'},
                  teal:{bg:'#DDF4F0',edge:'#00897B'},
                  green:{bg:'#E3F3E4',edge:'#2E7D32'},
                  yellow:{bg:'#FFF5CC',edge:'#C99800'},
                  orange:{bg:'#FFE8D4',edge:'#EF6C00'},
                  rose:{bg:'#FCE4EC',edge:'#D81B60'},
                  violet:{bg:'#EDE7F6',edge:'#6750A4'},
                  red:{bg:'#FDE8E8',edge:'#D84343'},
                  graphite:{bg:'#ECEFF1',edge:'#546E7A'}
                };
                let picked='';

                function language(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function label(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}

                const style=document.createElement('style');
                style.textContent=`
                  .courseColorField{margin-bottom:9px}
                  .courseColorLabel{display:block;color:var(--muted);font-size:.74rem;margin-bottom:5px}
                  .courseColorPalette{display:flex;flex-wrap:wrap;gap:7px;align-items:center}
                  .courseColorChoice{width:29px;height:29px;border-radius:50%;border:2px solid #fff;box-shadow:0 0 0 1px #cbd5e1;position:relative;padding:0}
                  .courseColorChoice.active{box-shadow:0 0 0 3px var(--blue)}
                  .courseColorChoice.none{background:#fff}
                  .courseColorChoice.none:before,.courseColorChoice.none:after{content:'';position:absolute;left:4px;right:4px;top:12px;height:2px;background:#a5afbd;transform:rotate(-40deg)}
                  .courseColorChoice.none:after{transform:rotate(40deg)}
                  .courseColorHint{font-size:.66rem;color:var(--muted);margin-top:5px}
                `;
                document.head.appendChild(style);

                function ensurePicker(){
                  const form=document.getElementById('courseForm');if(!form)return;
                  let field=document.getElementById('courseColorField');
                  if(!field){
                    field=document.createElement('div');field.id='courseColorField';field.className='courseColorField';
                    field.innerHTML='<span id="courseColorLabel" class="courseColorLabel"></span><div id="courseColorPalette" class="courseColorPalette"></div><div id="courseColorHint" class="courseColorHint"></div>';
                    const actions=form.querySelector('.sheetActions');form.insertBefore(field,actions||null);
                    const palette=document.getElementById('courseColorPalette');
                    const none=document.createElement('button');none.type='button';none.className='courseColorChoice none';none.dataset.color='';none.setAttribute('aria-label','Aucune couleur');palette.appendChild(none);
                    Object.keys(COLORS).forEach(id=>{const b=document.createElement('button');b.type='button';b.className='courseColorChoice';b.dataset.color=id;b.style.background=COLORS[id].bg;b.style.borderColor=COLORS[id].edge;b.setAttribute('aria-label',id);palette.appendChild(b)});
                    palette.querySelectorAll('.courseColorChoice').forEach(b=>b.onclick=()=>selectColor(b.dataset.color||''));
                  }
                  const title=document.getElementById('courseColorLabel');if(title)title.textContent=label('Couleur de la case','Cell colour','Farbe des Feldes');
                  const hint=document.getElementById('courseColorHint');if(hint)hint.textContent=label('La première pastille conserve l’affichage neutre.','The first dot keeps the neutral appearance.','Der erste Punkt behält die neutrale Darstellung.');
                }

                function selectColor(id){
                  picked=COLORS[id]?id:'';
                  document.querySelectorAll('.courseColorChoice').forEach(b=>b.classList.toggle('active',(b.dataset.color||'')===picked));
                }

                function currentEditedCourse(){
                  try{if(typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof selected==='undefined'||typeof editing==='undefined')return null;if(editing==null)return null;return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null}catch(e){return null}
                }

                function applyCourseColor(el,id,gradient){
                  if(!el)return;
                  const c=COLORS[id];
                  el.style.background='';el.style.boxShadow='';
                  if(!c)return;
                  el.style.background=gradient?('linear-gradient(90deg,'+c.bg+' 0%,#ffffff 96%)'):c.bg;
                  el.style.boxShadow='inset 0 0 0 1px '+c.edge+'35';
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

                function decorateAll(){decorateWeek();decorateEdit();decorateToday()}

                function syncPicker(){ensurePicker();const c=currentEditedCourse();selectColor(c&&c.color?c.color:'')}

                const modal=document.getElementById('modal');
                if(modal){new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(syncPicker,0)}).observe(modal,{attributes:true,attributeFilter:['class']})}

                const form=document.getElementById('courseForm');
                if(form&&form.onsubmit&&!form.onsubmit.__courseColorWrapped){
                  const oldSubmit=form.onsubmit;
                  const wrapped=function(e){
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A';
                    const day=typeof selected!=='undefined'?selected:2;
                    const idx=typeof editing!=='undefined'?editing:null;
                    const old=(idx!=null&&weeks[week]&&weeks[week][day])?weeks[week][day].courses[idx]:null;
                    const pre=typeof newPrefill!=='undefined'&&newPrefill?{start:newPrefill.start,end:newPrefill.end}:null;
                    const slot=Number((document.getElementById('fSlot')||{}).value||0);
                    const text=(document.getElementById('fLabel')||{}).value||'';
                    let start='',end='';
                    if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}else if(old){start=old.start;end=old.end}else if(pre){start=pre.start;end=pre.end}
                    const chosen=picked;
                    const result=oldSubmit.call(this,e);
                    try{
                      const arr=weeks[week]&&weeks[week][day]?weeks[week][day].courses:[];
                      let target=arr.find(c=>c.start===start&&c.end===end&&c.label===String(text).trim());
                      if(!target)target=arr.find(c=>c.start===start&&c.end===end);
                      if(target){target.color=chosen;if(typeof save==='function')save()}
                    }catch(err){}
                    setTimeout(decorateAll,0);
                    return result;
                  };
                  wrapped.__courseColorWrapped=true;form.onsubmit=wrapped;
                }

                ['weekGrid','editList','todayList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(decorateAll,0)).observe(el,{childList:true})});

                function refresh(){ensurePicker();decorateAll();if(modal&&modal.classList.contains('show'))syncPicker()}
                window.refreshCourseColors=refresh;
                refresh();
              }catch(e){console.log('Course colours',e)}
            })();
            """;
    }
}
