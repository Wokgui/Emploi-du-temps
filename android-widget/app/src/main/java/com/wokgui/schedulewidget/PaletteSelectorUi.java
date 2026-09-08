package com.wokgui.schedulewidget;

final class PaletteSelectorUi {
    private PaletteSelectorUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__coursePaletteUiV3){
                  if(window.refreshCoursePaletteV3)window.refreshCoursePaletteV3();
                  return;
                }
                window.__coursePaletteUiV3=true;

                const WIDGET_KEY='edt-widget-palette-fallback-v1';
                const INDEX={butter:0,apricot:1,peach:2,coral:3,terracotta:4,rose:5,berry:6,plum:6,sand:4,olive:5,blue:0,cyan:1,teal:2,green:3,yellow:4,orange:5,violet:6,red:6,graphite:6};
                const WIDGET_PALETTES={
                  vivid:{fr:'Éclat',en:'Vivid',de:'Kräftig',colors:['#F0335D','#FF7B2F','#FFE47D','#21C877','#18B9BE','#2F83E8','#9B55E9']},
                  pastel:{fr:'Pastel',en:'Pastel',de:'Pastell',colors:['#F58BA6','#FFAD72','#FFE3A0','#8BD5A4','#79D0D4','#8CB7ED','#B59AE7']},
                  warm:{fr:'Chaud',en:'Warm',de:'Warm',colors:['#EF5968','#FF7B72','#FF9B59','#F7B487','#E6BF85','#D98B9B','#B98CA5']},
                  cool:{fr:'Froid',en:'Cool',de:'Kühl',colors:['#3E91B8','#42B6BE','#4DB78B','#84BF67','#6DA3E7','#6D82D7','#9874D0']},
                  soft:{fr:'Sobre',en:'Soft',de:'Dezent',colors:['#7B8FA4','#9AA7AF','#CDD0BC','#86A894','#7AA7AA','#8098B6','#998DA9']}
                };

                function language(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function currentWidget(){
                  try{if(window.AndroidSchedule&&AndroidSchedule.loadWidgetPalette){const v=AndroidSchedule.loadWidgetPalette();if(WIDGET_PALETTES[v])return v}}catch(e){}
                  const v=localStorage.getItem(WIDGET_KEY);return WIDGET_PALETTES[v]?v:'vivid';
                }
                function palette(){return WIDGET_PALETTES[currentWidget()]||WIDGET_PALETTES.vivid}
                function colorFor(id){const i=INDEX[id];const p=palette().colors;return Number.isInteger(i)?p[Math.max(0,Math.min(p.length-1,i))]:null}
                function widgetTitle(){const l=language();return l==='en'?'Timetable & widget palette':(l==='de'?'Stundenplan- und Widget-Farben':'Palette des cases et du widget')}
                function widgetHint(){const l=language();return l==='en'?'The same colours are used in the timetable and in the widget.':(l==='de'?'Dieselben Farben werden im Stundenplan und im Widget verwendet.':'Les mêmes couleurs sont utilisées dans l’emploi du temps et dans le widget.')}
                function breakTitle(){const l=language();return l==='en'?'Lunch and free periods':(l==='de'?'Mittag und Freistunden':'Midi et trous')}
                function breakHint(){const l=language();return l==='en'?'Choose whether these bands appear in the app and in the widget.':(l==='de'?'Wähle, ob diese Bänder in der App und im Widget erscheinen.':'Choisis si ces bandeaux apparaissent dans l’application et dans le widget.')}
                function lunchSwitch(){const l=language();return l==='en'?'Show lunch':(l==='de'?'Mittag anzeigen':'Afficher Midi')}
                function gapSwitch(){const l=language();return l==='en'?'Show free periods':(l==='de'?'Freistunden anzeigen':'Afficher les trous')}
                function light(hex){try{const n=parseInt(String(hex).replace('#',''),16),r=(n>>16)&255,g=(n>>8)&255,b=n&255;return ((.2126*r+.7152*g+.0722*b)/255)>.68}catch(e){return false}}

                const style=document.createElement('style');
                style.textContent=`
                  #widgetPaletteSetting .settingTitle,#breakDisplaySetting .settingTitle{margin-bottom:3px}
                  .coursePaletteHint{font-size:.66rem;color:var(--muted);margin-bottom:8px}
                  .coursePaletteGrid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:7px}
                  .coursePaletteBtn{border:2px solid var(--line);background:#fff;border-radius:10px;padding:7px 6px;min-width:0;text-align:left}
                  .coursePaletteBtn.active{border-color:var(--blue);background:color-mix(in srgb,var(--blue) 7%,white)}
                  .coursePaletteName{font-size:.70rem;font-weight:900;color:var(--ink);display:block;margin-bottom:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  .coursePaletteSwatches{display:flex;gap:2px}.coursePaletteSwatches i{display:block;flex:1;height:14px;border-radius:3px;min-width:0}

                  #todayList .todayCourse.lunch,#todayList .todayCourse.gap{
                    width:100%!important;box-sizing:border-box!important;margin-left:0!important;margin-right:0!important;
                    min-height:54px!important;border:0!important;border-radius:0!important;box-shadow:none!important;
                    padding-top:5px!important;padding-bottom:5px!important;
                  }
                  #todayList .todayCourse.lunch{background:var(--edt-midi-bg)!important;color:var(--edt-midi-ink)!important}
                  #todayList .todayCourse.gap{background:var(--edt-gap-bg)!important;color:var(--edt-gap-ink)!important}
                  #todayList .todayCourse.lunch .time,#todayList .todayCourse.lunch .room,#todayList .todayCourse.lunch .label{color:var(--edt-midi-ink)!important}
                  #todayList .todayCourse.gap .time,#todayList .todayCourse.gap .room,#todayList .todayCourse.gap .label{color:var(--edt-gap-ink)!important}
                  #todayList .todayCourse.lunch .label:before{content:'🍴';display:inline-block;margin-right:7px}
                  #todayList .todayCourse.gap .label:before{content:'☕';display:inline-block;margin-right:7px}
                  #todayList .todayCourse.lunch .badge,#todayList .todayCourse.gap .badge{border:0!important;box-shadow:none!important}
                  #weekGrid .wc.lunchCell{background:var(--edt-midi-bg)!important;color:var(--edt-midi-ink)!important;box-shadow:none!important}
                  #weekGrid .wc.gapCell{background:var(--edt-gap-bg)!important;color:var(--edt-gap-ink)!important;box-shadow:none!important}
                  #weekGrid .wc.lunchCell *{color:var(--edt-midi-ink)!important}
                  #weekGrid .wc.gapCell *{color:var(--edt-gap-ink)!important}
                  @media(max-width:390px){.coursePaletteGrid{grid-template-columns:1fr}.coursePaletteBtn{display:grid;grid-template-columns:92px 1fr;align-items:center;gap:7px}.coursePaletteName{margin:0}.coursePaletteSwatches i{height:16px}}
                `;
                document.head.appendChild(style);

                function applyBreakPalette(){
                  const p=palette().colors,lunch=p[Math.min(2,p.length-1)],gap=p[Math.min(6,p.length-1)];
                  document.documentElement.style.setProperty('--edt-midi-bg',lunch);
                  document.documentElement.style.setProperty('--edt-gap-bg',gap);
                  document.documentElement.style.setProperty('--edt-midi-ink',light(lunch)?'#4B3B09':'#FFFFFF');
                  document.documentElement.style.setProperty('--edt-gap-ink',light(gap)?'#33294A':'#FFFFFF');
                }

                function ensureSettings(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  const obsolete=document.getElementById('coursePaletteSetting');if(obsolete)obsolete.remove();
                  const actions=sheet.querySelector('.settingsActions');
                  let widget=document.getElementById('widgetPaletteSetting');
                  if(!widget){
                    widget=document.createElement('div');widget.id='widgetPaletteSetting';widget.className='settingBox';
                    widget.innerHTML='<div id="widgetPaletteTitle" class="settingTitle"></div><div id="widgetPaletteHint" class="coursePaletteHint"></div><div id="widgetPaletteGrid" class="coursePaletteGrid"></div>';
                    sheet.insertBefore(widget,actions||null);
                  }
                  const wt=document.getElementById('widgetPaletteTitle');if(wt)wt.textContent=widgetTitle();
                  const wh=document.getElementById('widgetPaletteHint');if(wh)wh.textContent=widgetHint();
                  renderWidgetButtons();

                  let breaks=document.getElementById('breakDisplaySetting');
                  if(!breaks){
                    breaks=document.createElement('div');breaks.id='breakDisplaySetting';breaks.className='settingBox';
                    breaks.innerHTML='<div id="breakDisplayTitle" class="settingTitle"></div><div id="breakDisplayHint" class="coursePaletteHint"></div>';
                    sheet.insertBefore(breaks,widget.nextSibling||actions||null);
                  }
                  const lunch=document.getElementById('advShowLunch'),gap=document.getElementById('advShowBreaks');
                  const lunchRow=lunch&&lunch.closest?lunch.closest('label'):null,gapRow=gap&&gap.closest?gap.closest('label'):null;
                  if(lunchRow&&lunchRow.parentNode!==breaks)breaks.appendChild(lunchRow);
                  if(gapRow&&gapRow.parentNode!==breaks)breaks.appendChild(gapRow);
                  const bt=document.getElementById('breakDisplayTitle');if(bt)bt.textContent=breakTitle();
                  const bh=document.getElementById('breakDisplayHint');if(bh)bh.textContent=breakHint();
                  const ll=document.getElementById('advShowLunchLabel');if(ll)ll.textContent=lunchSwitch();
                  const gl=document.getElementById('advShowBreaksLabel');if(gl)gl.textContent=gapSwitch();
                }

                function renderWidgetButtons(){
                  const grid=document.getElementById('widgetPaletteGrid');if(!grid)return;
                  const l=language(),selected=currentWidget();grid.innerHTML='';
                  Object.keys(WIDGET_PALETTES).forEach(id=>{
                    const p=WIDGET_PALETTES[id],b=document.createElement('button');b.type='button';b.className='coursePaletteBtn';b.dataset.widgetPalette=id;
                    b.classList.toggle('active',id===selected);
                    b.innerHTML='<span class="coursePaletteName">'+(p[l]||p.fr)+'</span><span class="coursePaletteSwatches">'+p.colors.map(c=>'<i style="background:'+c+'"></i>').join('')+'</span>';
                    b.onclick=()=>{
                      try{if(window.AndroidSchedule&&AndroidSchedule.saveWidgetPalette)AndroidSchedule.saveWidgetPalette(id);else localStorage.setItem(WIDGET_KEY,id)}catch(e){localStorage.setItem(WIDGET_KEY,id)}
                      localStorage.setItem(WIDGET_KEY,id);renderWidgetButtons();repaint();
                    };
                    grid.appendChild(b);
                  });
                }

                function apply(el,id,gradient){
                  if(!el)return;const c=colorFor(id);if(!c)return;
                  el.style.setProperty('background',gradient?('linear-gradient(90deg,'+c+' 0%,#ffffff 97%)'):c,'important');
                  el.style.setProperty('box-shadow','none','important');
                }

                function repaintPicker(){
                  document.querySelectorAll('.courseColorChoice:not(.none)').forEach(b=>{const c=colorFor(b.dataset.color||'');if(c){b.style.setProperty('background',c,'important');b.style.setProperty('border-color',c,'important')}});
                }
                function repaintWeek(){
                  try{const grid=document.getElementById('weekGrid');if(!grid||typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof uniqueWeekTimes!=='function')return;const ws=weeks[activeWeek];if(!ws)return;const times=uniqueWeekTimes();const cells=Array.from(grid.querySelectorAll('.wc'));let p=0;for(const t of times){for(const d of [2,3,4,5,6]){const cell=cells[p++];if(!cell)continue;const c=ws[d]&&ws[d].courses?ws[d].courses.find(x=>x.start===t.start&&x.end===t.end):null;if(c&&c.color)apply(cell,c.color,false)}}}catch(e){}
                }
                function repaintToday(){
                  try{if(typeof weeks==='undefined'||typeof currentWeek==='undefined'||typeof todayKey!=='function')return;const d=todayKey(),list=weeks[currentWeek]&&weeks[currentWeek][d]?weeks[currentWeek][d].courses:[];document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{const start=(row.querySelector('.time strong')||{}).textContent||'';const text=(row.querySelector('.label')||{}).textContent||'';let c=list.find(x=>x.start===start&&text.indexOf(x.label)>=0);if(!c)c=list.find(x=>x.start===start);if(c&&c.color)apply(row,c.color,true)})}catch(e){}
                }
                function repaintEdit(){
                  try{if(typeof state==='undefined'||typeof selected==='undefined')return;const list=state[selected]&&state[selected].courses?state[selected].courses:[];document.querySelectorAll('#editList .editCourse').forEach((row,i)=>{const c=list[i];if(c&&c.color)apply(row,c.color,true)})}catch(e){}
                }
                function repaint(){applyBreakPalette();repaintPicker();repaintWeek();repaintToday();repaintEdit();ensureSettings()}

                ['weekGrid','todayList','editList','courseColorPalette'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(repaint,0)).observe(el,{childList:true,subtree:true})});
                const settingsModal=document.getElementById('settingsModal');if(settingsModal)new MutationObserver(()=>setTimeout(ensureSettings,0)).observe(settingsModal,{attributes:true,attributeFilter:['class']});
                const settingsBtn=document.getElementById('settingsBtn');if(settingsBtn)settingsBtn.addEventListener('click',()=>setTimeout(ensureSettings,0));
                const form=document.getElementById('courseForm');if(form)form.addEventListener('submit',()=>setTimeout(repaint,30));

                const originalRefresh=window.refreshCourseColors;
                if(typeof originalRefresh==='function'&&!originalRefresh.__paletteWrappedV3){const wrapped=function(){const r=originalRefresh.apply(this,arguments);setTimeout(repaint,0);return r};wrapped.__paletteWrappedV3=true;window.refreshCourseColors=wrapped}

                window.refreshCoursePaletteV3=repaint;
                repaint();
              }catch(e){console.log('Palette selector',e)}
            })();
            """;
    }
}
