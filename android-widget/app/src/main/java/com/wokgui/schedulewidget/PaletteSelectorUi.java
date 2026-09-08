package com.wokgui.schedulewidget;

final class PaletteSelectorUi {
    private PaletteSelectorUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__coursePaletteUiV4){
                  if(window.refreshCoursePaletteV4)window.refreshCoursePaletteV4();
                  return;
                }
                window.__coursePaletteUiV4=true;

                const WIDGET_KEY='edt-widget-palette-fallback-v1';
                const APP_KEY='edt-app-palette-v1';
                const SYNC_KEY='edt-palette-sync-v1';
                const INDEX={butter:0,apricot:1,peach:2,coral:3,terracotta:4,rose:5,berry:6,plum:6,sand:4,olive:5,blue:0,cyan:1,teal:2,green:3,yellow:4,orange:5,violet:6,red:6,graphite:6};
                const WIDGET_PALETTES={
                  vivid:{fr:'Éclat',en:'Vivid',de:'Kräftig',colors:['#F0335D','#FF7B2F','#FFE47D','#21C877','#18B9BE','#2F83E8','#9B55E9']},
                  pastel:{fr:'Pastel',en:'Pastel',de:'Pastell',colors:['#F58BA6','#FFAD72','#FFE3A0','#8BD5A4','#79D0D4','#8CB7ED','#B59AE7']},
                  warm:{fr:'Chaud',en:'Warm',de:'Warm',colors:['#EF5968','#FF7B72','#FF9B59','#F7B487','#E6BF85','#D98B9B','#B98CA5']},
                  cool:{fr:'Froid',en:'Cool',de:'Kühl',colors:['#3E91B8','#42B6BE','#4DB78B','#84BF67','#6DA3E7','#6D82D7','#9874D0']},
                  soft:{fr:'Sobre',en:'Soft',de:'Dezent',colors:['#7B8FA4','#9AA7AF','#CDD0BC','#86A894','#7AA7AA','#8098B6','#998DA9']}
                };
                const MIDI_BG='#FFF9E8',MIDI_BORDER='#E4B84D',MIDI_INK='#22283A';
                const GAP_BG='#FFFFFF',GAP_BORDER='#DDE4EC',GAP_INK='#22283A';

                function language(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function currentWidget(){
                  try{if(window.AndroidSchedule&&AndroidSchedule.loadWidgetPalette){const v=AndroidSchedule.loadWidgetPalette();if(WIDGET_PALETTES[v])return v}}catch(e){}
                  const v=localStorage.getItem(WIDGET_KEY);return WIDGET_PALETTES[v]?v:'vivid';
                }
                function currentApp(){
                  const stored=localStorage.getItem(APP_KEY);
                  if(WIDGET_PALETTES[stored])return stored;
                  const initial=currentWidget();localStorage.setItem(APP_KEY,initial);return initial;
                }
                function palettesSynced(){return localStorage.getItem(SYNC_KEY)!=='0'}
                function saveWidget(id){
                  if(!WIDGET_PALETTES[id])id='vivid';
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveWidgetPalette)AndroidSchedule.saveWidgetPalette(id)}catch(e){}
                  localStorage.setItem(WIDGET_KEY,id);
                }
                function saveApp(id){
                  if(!WIDGET_PALETTES[id])id='vivid';
                  localStorage.setItem(APP_KEY,id);
                  if(palettesSynced())saveWidget(id);
                }
                function setPaletteSync(value){
                  localStorage.setItem(SYNC_KEY,value?'1':'0');
                  if(value)saveWidget(currentApp());
                }
                function appPalette(){return WIDGET_PALETTES[currentApp()]||WIDGET_PALETTES.vivid}
                function colorFor(id){const i=INDEX[id];const p=appPalette().colors;return Number.isInteger(i)?p[Math.max(0,Math.min(p.length-1,i))]:null}

                function text(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}
                function paletteTitle(){return palettesSynced()?text('Palette de l’application et du widget','App & widget palette','App- und Widget-Farben'):text('Palette de l’application','App palette','App-Farben')}
                function paletteHint(){return text('Les couleurs choisies pour les cases sont utilisées dans le widget tant que la synchronisation est activée.','Course colours are also used in the widget while palette sync is enabled.','Die Kursfarben werden auch im Widget verwendet, solange die Synchronisierung aktiv ist.')}
                function syncLabel(){return text('Utiliser la même palette dans l’application et le widget','Use the same palette in the app and widget','Gleiche Palette in App und Widget verwenden')}
                function separateWidgetTitle(){return text('Palette du widget','Widget palette','Widget-Farben')}
                function breakTitle(){return text('Midi et trous','Lunch and free periods','Mittag und Freistunden')}
                function breakHint(){return text('Midi utilise le style sable-champagne et les trous restent blancs. Choisis ici s’ils doivent apparaître.','Lunch uses the sand-champagne style and free periods stay white. Choose here whether to show them.','Mittag nutzt den Sand-Champagner-Stil und Freistunden bleiben weiß. Hier wählst du, ob sie angezeigt werden.')}
                function lunchSwitch(){return text('Afficher Midi','Show lunch','Mittag anzeigen')}
                function gapSwitch(){return text('Afficher les trous','Show free periods','Freistunden anzeigen')}

                const style=document.createElement('style');
                style.textContent=`
                  #paletteSettingRoot .settingTitle,#breakDisplaySetting .settingTitle{margin-bottom:3px}
                  .coursePaletteHint{font-size:.66rem;color:var(--muted);margin-bottom:8px}
                  .coursePaletteGrid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:7px}
                  .coursePaletteBtn{border:2px solid var(--line);background:#fff;border-radius:10px;padding:7px 6px;min-width:0;text-align:left}
                  .coursePaletteBtn.active{border-color:var(--blue);background:color-mix(in srgb,var(--blue) 7%,white)}
                  .coursePaletteName{font-size:.70rem;font-weight:900;color:var(--ink);display:block;margin-bottom:5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  .coursePaletteSwatches{display:flex;gap:2px}.coursePaletteSwatches i{display:block;flex:1;height:14px;border-radius:3px;min-width:0}
                  .paletteSubTitle{font-size:.68rem;font-weight:900;color:var(--ink);margin:8px 0 5px}
                  .paletteSyncRow{display:flex;align-items:center;justify-content:space-between;gap:10px;margin:7px 0 10px;padding:8px 10px;border:1px solid var(--line);border-radius:10px;background:#fff;color:var(--ink);font-size:.70rem;font-weight:800}
                  .paletteSyncRow input{width:19px;height:19px;accent-color:var(--blue);flex:0 0 auto}
                  #widgetPaletteSeparate{margin-top:10px;padding-top:8px;border-top:1px solid var(--line)}

                  #todayList .todayCourse.lunch,#todayList .todayCourse.gap{
                    width:100%!important;box-sizing:border-box!important;margin-left:0!important;margin-right:0!important;
                    min-height:54px!important;border-radius:0!important;box-shadow:none!important;
                    padding-top:5px!important;padding-bottom:5px!important;
                  }
                  #todayList .todayCourse.lunch{background:${MIDI_BG}!important;color:${MIDI_INK}!important;border:1px solid ${MIDI_BORDER}!important}
                  #todayList .todayCourse.gap{background:${GAP_BG}!important;color:${GAP_INK}!important;border:1px solid ${GAP_BORDER}!important}
                  #todayList .todayCourse.lunch .time,#todayList .todayCourse.lunch .room,#todayList .todayCourse.lunch .label{color:${MIDI_INK}!important}
                  #todayList .todayCourse.gap .time,#todayList .todayCourse.gap .room,#todayList .todayCourse.gap .label{color:${GAP_INK}!important}
                  #todayList .todayCourse.lunch .label:before{content:'🍴';display:inline-block;margin-right:7px}
                  #todayList .todayCourse.gap .label:before{content:none!important;display:none!important}
                  #todayList .todayCourse.lunch .badge,#todayList .todayCourse.gap .badge{border:0!important;box-shadow:none!important}

                  #weekGrid .wc.gapCell{background:${GAP_BG}!important;color:${GAP_INK}!important;box-shadow:inset 0 0 0 1px ${GAP_BORDER}!important}
                  #weekGrid .wc.gapCell *{color:${GAP_INK}!important}
                  #weekGrid .wc.gapCell .cellLabel:before{content:none!important;display:none!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell){background:${MIDI_BG}!important;color:${MIDI_INK}!important;box-shadow:inset 0 0 0 1px ${MIDI_BORDER}!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell) *{color:${MIDI_INK}!important}
                  #weekGrid .dynamicLunchCell{background:#fff!important;color:${MIDI_INK}!important}
                  #weekGrid .dynamicLunchOverlay{background:${MIDI_BG}!important;color:${MIDI_INK}!important;border:1px solid ${MIDI_BORDER}!important;box-shadow:none!important}
                  #weekGrid .dynamicLunchOverlay *{color:${MIDI_INK}!important}
                  #weekGrid .wc.lunchCell .cellLabel:before{content:'🍴';display:inline-block;margin-right:4px;font-size:.72em;vertical-align:middle}

                  @media(max-width:390px){.coursePaletteGrid{grid-template-columns:1fr}.coursePaletteBtn{display:grid;grid-template-columns:92px 1fr;align-items:center;gap:7px}.coursePaletteName{margin:0}.coursePaletteSwatches i{height:16px}}
                `;
                document.head.appendChild(style);

                function paletteButton(id,target,selected,onClick){
                  const p=WIDGET_PALETTES[id],l=language(),b=document.createElement('button');
                  b.type='button';b.className='coursePaletteBtn';b.dataset.palette=id;b.classList.toggle('active',id===selected);
                  b.innerHTML='<span class="coursePaletteName">'+(p[l]||p.fr)+'</span><span class="coursePaletteSwatches">'+p.colors.map(c=>'<i style="background:'+c+'"></i>').join('')+'</span>';
                  b.onclick=()=>onClick(id);target.appendChild(b);
                }
                function renderAppButtons(){
                  const grid=document.getElementById('appPaletteGrid');if(!grid)return;grid.innerHTML='';const selected=currentApp();
                  Object.keys(WIDGET_PALETTES).forEach(id=>paletteButton(id,grid,selected,value=>{saveApp(value);renderPaletteControls();repaint()}));
                }
                function renderWidgetButtons(){
                  const grid=document.getElementById('widgetPaletteGrid');if(!grid)return;grid.innerHTML='';const selected=currentWidget();
                  Object.keys(WIDGET_PALETTES).forEach(id=>paletteButton(id,grid,selected,value=>{saveWidget(value);renderPaletteControls()}));
                }
                function renderPaletteControls(){
                  const title=document.getElementById('appPaletteTitle');if(title)title.textContent=paletteTitle();
                  const hint=document.getElementById('paletteSyncHint');if(hint)hint.textContent=paletteHint();
                  const label=document.getElementById('paletteSyncLabel');if(label)label.textContent=syncLabel();
                  const toggle=document.getElementById('paletteSyncToggle');if(toggle)toggle.checked=palettesSynced();
                  const sep=document.getElementById('widgetPaletteSeparate');if(sep)sep.style.display=palettesSynced()?'none':'block';
                  const wt=document.getElementById('widgetPaletteSeparateTitle');if(wt)wt.textContent=separateWidgetTitle();
                  renderAppButtons();if(!palettesSynced())renderWidgetButtons();
                }

                function ensureSettings(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  ['coursePaletteSetting','widgetPaletteSetting'].forEach(id=>{const old=document.getElementById(id);if(old)old.remove()});
                  const actions=sheet.querySelector('.settingsActions');
                  let root=document.getElementById('paletteSettingRoot');
                  if(!root){
                    root=document.createElement('div');root.id='paletteSettingRoot';root.className='settingBox';
                    root.innerHTML='<div id="appPaletteTitle" class="settingTitle"></div><div id="paletteSyncHint" class="coursePaletteHint"></div><label class="paletteSyncRow"><span id="paletteSyncLabel"></span><input id="paletteSyncToggle" type="checkbox"></label><div id="appPaletteGrid" class="coursePaletteGrid"></div><div id="widgetPaletteSeparate"><div id="widgetPaletteSeparateTitle" class="paletteSubTitle"></div><div id="widgetPaletteGrid" class="coursePaletteGrid"></div></div>';
                    sheet.insertBefore(root,actions||null);
                    const toggle=root.querySelector('#paletteSyncToggle');if(toggle)toggle.addEventListener('change',()=>{setPaletteSync(toggle.checked);renderPaletteControls();repaint()});
                  }
                  renderPaletteControls();

                  let breaks=document.getElementById('breakDisplaySetting');
                  if(!breaks){
                    breaks=document.createElement('div');breaks.id='breakDisplaySetting';breaks.className='settingBox';
                    breaks.innerHTML='<div id="breakDisplayTitle" class="settingTitle"></div><div id="breakDisplayHint" class="coursePaletteHint"></div>';
                    sheet.insertBefore(breaks,root.nextSibling||actions||null);
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
                function ensureLinkedPalette(){
                  if(!palettesSynced())return;const a=currentApp(),w=currentWidget();if(a!==w)saveWidget(a);
                }
                function repaint(){ensureLinkedPalette();repaintPicker();repaintWeek();repaintToday();repaintEdit();ensureSettings()}

                ['weekGrid','todayList','editList','courseColorPalette'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(repaint,0)).observe(el,{childList:true,subtree:true})});
                const settingsModal=document.getElementById('settingsModal');if(settingsModal)new MutationObserver(()=>setTimeout(ensureSettings,0)).observe(settingsModal,{attributes:true,attributeFilter:['class']});
                const settingsBtn=document.getElementById('settingsBtn');if(settingsBtn)settingsBtn.addEventListener('click',()=>setTimeout(ensureSettings,0));
                const form=document.getElementById('courseForm');if(form)form.addEventListener('submit',()=>setTimeout(repaint,30));

                const originalRefresh=window.refreshCourseColors;
                if(typeof originalRefresh==='function'&&!originalRefresh.__paletteWrappedV4){const wrapped=function(){const r=originalRefresh.apply(this,arguments);setTimeout(repaint,0);return r};wrapped.__paletteWrappedV4=true;window.refreshCourseColors=wrapped}

                window.refreshCoursePaletteV4=repaint;
                repaint();
              }catch(e){console.log('Palette selector',e)}
            })();
            """;
    }
}
