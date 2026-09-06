package com.wokgui.schedulewidget;

final class PaletteSelectorUi {
    private PaletteSelectorUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__coursePaletteUiV1){
                  if(window.refreshCoursePaletteV1)window.refreshCoursePaletteV1();
                  return;
                }
                window.__coursePaletteUiV1=true;

                const KEY='edt-course-palette-v1';
                const IDS=['butter','apricot','peach','coral','terracotta','rose','berry','plum'];
                const INDEX={butter:0,apricot:1,peach:2,coral:3,terracotta:4,rose:5,berry:6,plum:7,sand:4,olive:5,blue:0,cyan:1,teal:2,green:3,yellow:4,orange:5,violet:6,red:7,graphite:7};
                const PALETTES={
                  vibrant:{name:'Vibrant',colors:[
                    {bg:'#D72C78',edge:'#8E164C'},{bg:'#F04C9A',edge:'#A82367'},{bg:'#FF6657',edge:'#B2342B'},{bg:'#FF9A32',edge:'#B95B0B'},
                    {bg:'#FFC64B',edge:'#A77700'},{bg:'#83C94A',edge:'#4F7F24'},{bg:'#3A96B9',edge:'#23627A'},{bg:'#7B5BB6',edge:'#4D387A'}]},
                  energy:{name:'Énergie',colors:[
                    {bg:'#E63B43',edge:'#982128'},{bg:'#FF6757',edge:'#B6372C'},{bg:'#FF9838',edge:'#B65A10'},{bg:'#FFC847',edge:'#A77900'},
                    {bg:'#A6C94B',edge:'#667C25'},{bg:'#4CAF78',edge:'#2D704B'},{bg:'#3E9E8D',edge:'#27665D'},{bg:'#3C87E8',edge:'#24579A'}]},
                  aurora:{name:'Aurore corail',colors:[
                    {bg:'#EF5968',edge:'#A83242'},{bg:'#FF7B72',edge:'#B7473F'},{bg:'#FF9B59',edge:'#B55E20'},{bg:'#F7B487',edge:'#A96F48'},
                    {bg:'#E8C9AE',edge:'#9A7658'},{bg:'#F2B3AD',edge:'#A36C67'},{bg:'#D98B9B',edge:'#8C5360'},{bg:'#B98CA5',edge:'#73546A'}]}
                };

                function language(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function current(){const v=localStorage.getItem(KEY);return PALETTES[v]?v:'aurora'}
                function palette(){return PALETTES[current()]}
                function colorFor(id){const i=INDEX[id];return Number.isInteger(i)?palette().colors[Math.max(0,Math.min(7,i))]:null}
                function title(){const l=language();return l==='en'?'Class colour palette':(l==='de'?'Farben der Unterrichtsfelder':'Palette des cases')}
                function hint(){const l=language();return l==='en'?'Choose the colour family used for course cells.':(l==='de'?'Wähle die Farbpalette für Unterrichtsfelder.':'Choisis la famille de couleurs utilisée pour les cases de cours.')}

                const style=document.createElement('style');
                style.textContent=`
                  #coursePaletteSetting .settingTitle{margin-bottom:3px}
                  .coursePaletteHint{font-size:.66rem;color:var(--muted);margin-bottom:8px}
                  .coursePaletteGrid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:7px}
                  .coursePaletteBtn{border:2px solid var(--line);background:#fff;border-radius:10px;padding:7px 6px;min-width:0;text-align:left}
                  .coursePaletteBtn.active{border-color:var(--blue);background:color-mix(in srgb,var(--blue) 7%,white)}
                  .coursePaletteName{font-size:.70rem;font-weight:900;color:var(--ink);display:block;margin-bottom:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  .coursePaletteSwatches{display:flex;gap:2px}
                  .coursePaletteSwatches i{display:block;flex:1;height:14px;border-radius:3px;min-width:0}
                  @media(max-width:390px){.coursePaletteGrid{grid-template-columns:1fr}.coursePaletteBtn{display:grid;grid-template-columns:92px 1fr;align-items:center;gap:7px}.coursePaletteName{margin:0}.coursePaletteSwatches i{height:16px}}
                `;
                document.head.appendChild(style);

                function ensureSettings(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  let box=document.getElementById('coursePaletteSetting');
                  if(!box){
                    box=document.createElement('div');box.id='coursePaletteSetting';box.className='settingBox';
                    box.innerHTML='<div id="coursePaletteTitle" class="settingTitle"></div><div id="coursePaletteHint" class="coursePaletteHint"></div><div id="coursePaletteGrid" class="coursePaletteGrid"></div>';
                    const actions=sheet.querySelector('.settingsActions');sheet.insertBefore(box,actions||null);
                  }
                  const t=document.getElementById('coursePaletteTitle');if(t)t.textContent=title();
                  const h=document.getElementById('coursePaletteHint');if(h)h.textContent=hint();
                  renderButtons();
                }

                function renderButtons(){
                  const grid=document.getElementById('coursePaletteGrid');if(!grid)return;
                  grid.innerHTML='';
                  Object.keys(PALETTES).forEach(id=>{
                    const p=PALETTES[id],b=document.createElement('button');b.type='button';b.className='coursePaletteBtn';b.dataset.palette=id;
                    b.classList.toggle('active',id===current());
                    b.innerHTML='<span class="coursePaletteName">'+p.name+'</span><span class="coursePaletteSwatches">'+p.colors.map(c=>'<i style="background:'+c.bg+';box-shadow:inset 0 0 0 1px '+c.edge+'"></i>').join('')+'</span>';
                    b.onclick=()=>{localStorage.setItem(KEY,id);renderButtons();repaint();};
                    grid.appendChild(b);
                  });
                }

                function apply(el,id,gradient){
                  if(!el)return;const c=colorFor(id);if(!c)return;
                  const bg=gradient?('linear-gradient(90deg,'+c.bg+' 0%,#ffffff 97%)'):c.bg;
                  el.style.setProperty('background',bg,'important');
                  el.style.setProperty('box-shadow','inset 0 0 0 1px '+c.edge,'important');
                }

                function repaintPicker(){
                  document.querySelectorAll('.courseColorChoice:not(.none)').forEach(b=>{const c=colorFor(b.dataset.color||'');if(c){b.style.setProperty('background',c.bg,'important');b.style.setProperty('border-color',c.edge,'important')}});
                }

                function repaintWeek(){
                  try{
                    const grid=document.getElementById('weekGrid');if(!grid||typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof uniqueWeekTimes!=='function')return;
                    const ws=weeks[activeWeek];if(!ws)return;const times=uniqueWeekTimes();const cells=Array.from(grid.querySelectorAll('.wc'));let p=0;
                    for(const t of times){for(const d of [2,3,4,5,6]){const cell=cells[p++];if(!cell)continue;const c=ws[d]&&ws[d].courses?ws[d].courses.find(x=>x.start===t.start&&x.end===t.end):null;if(c&&c.color)apply(cell,c.color,false)}}
                  }catch(e){}
                }

                function repaintToday(){
                  try{
                    if(typeof weeks==='undefined'||typeof currentWeek==='undefined'||typeof todayKey!=='function')return;const d=todayKey(),list=weeks[currentWeek]&&weeks[currentWeek][d]?weeks[currentWeek][d].courses:[];
                    document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{const start=(row.querySelector('.time strong')||{}).textContent||'';const text=(row.querySelector('.label')||{}).textContent||'';let c=list.find(x=>x.start===start&&text.indexOf(x.label)>=0);if(!c)c=list.find(x=>x.start===start);if(c&&c.color)apply(row,c.color,true)});
                  }catch(e){}
                }

                function repaintEdit(){
                  try{
                    if(typeof state==='undefined'||typeof selected==='undefined')return;const list=state[selected]&&state[selected].courses?state[selected].courses:[];
                    document.querySelectorAll('#editList .editCourse').forEach((row,i)=>{const c=list[i];if(c&&c.color)apply(row,c.color,true)});
                  }catch(e){}
                }

                function repaint(){repaintPicker();repaintWeek();repaintToday();repaintEdit();ensureSettings()}

                ['weekGrid','todayList','editList','courseColorPalette'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(repaint,0)).observe(el,{childList:true,subtree:true})});
                const settingsModal=document.getElementById('settingsModal');if(settingsModal)new MutationObserver(()=>setTimeout(ensureSettings,0)).observe(settingsModal,{attributes:true,attributeFilter:['class']});
                const settingsBtn=document.getElementById('settingsBtn');if(settingsBtn)settingsBtn.addEventListener('click',()=>setTimeout(ensureSettings,0));
                const form=document.getElementById('courseForm');if(form)form.addEventListener('submit',()=>setTimeout(repaint,30));

                const originalRefresh=window.refreshCourseColors;
                if(typeof originalRefresh==='function'&&!originalRefresh.__paletteWrapped){
                  const wrapped=function(){const r=originalRefresh.apply(this,arguments);setTimeout(repaint,0);return r};wrapped.__paletteWrapped=true;window.refreshCourseColors=wrapped;
                }

                window.refreshCoursePaletteV1=repaint;
                repaint();
              }catch(e){console.log('Palette selector',e)}
            })();
            """;
    }
}
