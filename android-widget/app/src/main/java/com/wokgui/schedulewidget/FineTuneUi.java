package com.wokgui.schedulewidget;

final class FineTuneUi {
    private FineTuneUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__fineTuneUiV1){
                  if(window.refreshFineTuneUi)window.refreshFineTuneUi();
                  return;
                }
                window.__fineTuneUiV1=true;

                const SPECIAL_KEY='edt-special-colors-v2';
                const SYNC_KEY='edt-palette-sync-v1';
                let customActive=false;
                let customBase='#2F83E8';
                let customTone=0;

                function lang(){
                  try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function isHex(v){return /^#[0-9a-f]{6}$/i.test(String(v||''))}
                function clamp(v,a,b){return Math.max(a,Math.min(b,v))}
                function hexRgb(hex){const n=parseInt(String(hex).slice(1),16);return {r:(n>>16)&255,g:(n>>8)&255,b:n&255}}
                function rgbHex(r,g,b){return '#'+[r,g,b].map(v=>clamp(Math.round(v),0,255).toString(16).padStart(2,'0')).join('').toUpperCase()}
                function toneColor(hex,tone){
                  if(!isHex(hex))return '#2F83E8';
                  const c=hexRgb(hex),t=clamp(Number(tone)||0,-45,45)/100;
                  if(t>=0)return rgbHex(c.r+(255-c.r)*t,c.g+(255-c.g)*t,c.b+(255-c.b)*t);
                  const k=1+t;return rgbHex(c.r*k,c.g*k,c.b*k);
                }
                function darker(hex){return toneColor(hex,-24)}
                function contrast(hex){const c=hexRgb(hex),l=(.2126*c.r+.7152*c.g+.0722*c.b)/255;return l>.64?'#1D2638':'#FFFFFF'}
                function currentCustom(){return toneColor(customBase,customTone)}
                function norm(s){return String(s||'').trim().replace(/ +/g,' ').toLocaleLowerCase()}

                const css=document.createElement('style');
                css.textContent=`
                  /* La semaine remplit un peu mieux l'écran sans devenir tassée. */
                  #viewWeek .wh,#viewWeek .wc{min-height:43px!important}
                  #viewWeek .weekTop{margin-bottom:8px!important}
                  #viewWeek .weekScroller{padding-bottom:5px}

                  .fullColorBox{margin-top:9px;padding:9px;border:1px solid var(--line);border-radius:10px;background:#fbfcfe}
                  .fullColorHead{display:flex;align-items:center;justify-content:space-between;gap:8px;margin-bottom:7px}
                  .fullColorTitle{font-size:.70rem;font-weight:900;color:var(--ink)}
                  .fullColorValue{font-size:.62rem;font-weight:800;color:var(--muted);font-variant-numeric:tabular-nums}
                  .fullColorControls{display:grid;grid-template-columns:46px minmax(0,1fr);align-items:center;gap:9px}
                  .fullColorControls input[type=color]{width:46px;height:38px;padding:2px;border:1px solid var(--line);border-radius:8px;background:#fff}
                  .fullToneWrap{min-width:0}
                  .fullToneLabel{display:flex;justify-content:space-between;font-size:.60rem;color:var(--muted);margin-bottom:3px}
                  .fullTone{width:100%;height:20px;accent-color:var(--blue)}
                  .fullTone::-webkit-slider-runnable-track{height:8px;border-radius:999px;background:linear-gradient(90deg,#111 0%,var(--tone-base,#2F83E8) 50%,#fff 100%);border:1px solid #d7dee8}
                  .fullTone::-webkit-slider-thumb{margin-top:-5px}
                  .fullColorActive{box-shadow:0 0 0 2px color-mix(in srgb,var(--blue) 28%,transparent);border-color:var(--blue)!important}
                  .specialColorGrid{display:grid;grid-template-columns:1fr;gap:8px;margin-top:8px}
                  .specialColorRow{display:grid;grid-template-columns:58px 46px minmax(0,1fr);align-items:center;gap:7px}
                  .specialColorRow>span{font-size:.68rem;font-weight:850;color:var(--ink)}
                  .specialColorRow input[type=color]{width:44px;height:34px;padding:2px;border:1px solid var(--line);border-radius:8px;background:#fff}
                  .specialColorRow input[type=range]{width:100%;accent-color:var(--blue)}
                  .specialWidgetTitle{font-size:.66rem;font-weight:900;color:var(--ink);margin-top:10px;padding-top:8px;border-top:1px solid var(--line)}

                  /* Les couleurs Midi/Trou sont calculées dynamiquement. Le liseré Midi suit la couleur. */
                  #todayList .todayCourse.lunch{background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;border:1px solid var(--ft-midi-border)!important}
                  #todayList .todayCourse.gap{background:var(--ft-gap)!important;color:var(--ft-gap-ink)!important;border:1px solid var(--ft-gap-border)!important}
                  #todayList .todayCourse.lunch .time,#todayList .todayCourse.lunch .room,#todayList .todayCourse.lunch .label{color:var(--ft-midi-ink)!important}
                  #todayList .todayCourse.gap .time,#todayList .todayCourse.gap .room,#todayList .todayCourse.gap .label{color:var(--ft-gap-ink)!important}
                  #todayList .todayCourse.lunch .label:before{content:'🍴'!important;display:inline-block!important;margin-right:6px!important}
                  #todayList .todayCourse.gap .label:before{content:none!important;display:none!important}
                  #weekGrid .wc.gapCell{background:var(--ft-gap)!important;color:var(--ft-gap-ink)!important;box-shadow:inset 0 0 0 1px var(--ft-gap-border)!important}
                  #weekGrid .wc.gapCell *{color:var(--ft-gap-ink)!important}
                  #weekGrid .wc.gapCell .cellLabel:before{content:none!important;display:none!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell){background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;box-shadow:inset 0 0 0 1px var(--ft-midi-border)!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell) *{color:var(--ft-midi-ink)!important}
                  #weekGrid .dynamicLunchCell{background:#fff!important;color:var(--ft-midi-ink)!important}
                  #weekGrid .dynamicLunchOverlay{background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;border:1px solid var(--ft-midi-border)!important;box-shadow:none!important}
                  #weekGrid .dynamicLunchOverlay *{color:var(--ft-midi-ink)!important}
                  #weekGrid .wc.lunchCell .cellLabel:before{content:'🍴'!important;display:inline-block!important;margin-right:3px!important;font-size:.72em!important;vertical-align:middle!important}
                  @media(max-width:560px){#viewWeek .wh,#viewWeek .wc{min-height:42px!important}}
                `;
                document.head.appendChild(css);

                function defaults(){return {sync:true,appLunch:'#FFF9E8',appGap:'#FFFFFF',widgetLunch:'#FFF9E8',widgetGap:'#FFFFFF'}}
                function loadSpecial(){
                  let o=null;
                  try{if(window.AndroidSchedule&&AndroidSchedule.loadSpecialColors)o=JSON.parse(AndroidSchedule.loadSpecialColors()||'{}')}catch(e){}
                  if(!o){try{o=JSON.parse(localStorage.getItem(SPECIAL_KEY)||'{}')}catch(e){o={}}}
                  const d=defaults();Object.keys(d).forEach(k=>{if(k==='sync'){if(typeof o[k]!=='boolean')o[k]=d[k]}else if(!isHex(o[k]))o[k]=d[k]});
                  const paletteSync=localStorage.getItem(SYNC_KEY)!=='0';o.sync=paletteSync;
                  if(o.sync){o.widgetLunch=o.appLunch;o.widgetGap=o.appGap}
                  return o;
                }
                function saveSpecial(o){
                  const d=defaults();Object.keys(d).forEach(k=>{if(k!=='sync'&&!isHex(o[k]))o[k]=d[k]});
                  if(o.sync){o.widgetLunch=o.appLunch;o.widgetGap=o.appGap}
                  try{localStorage.setItem(SPECIAL_KEY,JSON.stringify(o))}catch(e){}
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveSpecialColors)AndroidSchedule.saveSpecialColors(JSON.stringify(o))}catch(e){}
                }
                function applySpecialCss(){
                  const o=loadSpecial();
                  const root=document.documentElement;
                  root.style.setProperty('--ft-midi',o.appLunch);
                  root.style.setProperty('--ft-midi-border',darker(o.appLunch));
                  root.style.setProperty('--ft-midi-ink',contrast(o.appLunch));
                  root.style.setProperty('--ft-gap',o.appGap);
                  root.style.setProperty('--ft-gap-border',darker(o.appGap));
                  root.style.setProperty('--ft-gap-ink',contrast(o.appGap));
                }

                function setSpecialColor(key,base,tone){
                  const o=loadSpecial();o[key]=toneColor(base,tone);o.sync=localStorage.getItem(SYNC_KEY)!=='0';
                  if(o.sync){o.widgetLunch=o.appLunch;o.widgetGap=o.appGap}
                  saveSpecial(o);applySpecialCss();renderSpecialControls();
                }

                function specialRow(key,labelText,value){
                  return '<div class="specialColorRow" data-special="'+key+'"><span>'+labelText+'</span><input class="specialColorPick" type="color" value="'+value+'"><input class="specialColorTone" type="range" min="-35" max="35" value="0" aria-label="Variation"></div>';
                }
                function wireSpecialRows(box){
                  box.querySelectorAll('.specialColorRow').forEach(row=>{
                    const key=row.dataset.special,pick=row.querySelector('.specialColorPick'),tone=row.querySelector('.specialColorTone');
                    const commit=()=>setSpecialColor(key,pick.value,Number(tone.value)||0);
                    pick.addEventListener('input',()=>{tone.value='0';commit()});
                    tone.addEventListener('input',commit);
                  });
                }
                function renderSpecialControls(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  let box=document.getElementById('fineSpecialColors');
                  if(!box){box=document.createElement('div');box.id='fineSpecialColors';box.className='settingBox';const actions=sheet.querySelector('.settingsActions');sheet.insertBefore(box,actions||null)}
                  const o=loadSpecial(),synced=localStorage.getItem(SYNC_KEY)!=='0';
                  box.innerHTML='<div class="settingTitle">'+tr('Couleurs complètes','Full colours','Vollständige Farben')+'</div><div class="coursePaletteHint">'+tr('Appuie sur le carré pour le nuancier complet. La barre ajuste ensuite la nuance.','Tap the square for the full colour picker. The bar then adjusts the shade.','Tippe auf das Quadrat für den vollständigen Farbwähler. Der Regler passt danach die Nuance an.')+'</div><div class="specialColorGrid">'+specialRow('appLunch','Midi',o.appLunch)+specialRow('appGap',tr('Trou','Free','Freistunde'),o.appGap)+'</div>'+(synced?'':'<div class="specialWidgetTitle">'+tr('Couleurs séparées du widget','Separate widget colours','Separate Widget-Farben')+'</div><div class="specialColorGrid">'+specialRow('widgetLunch','Midi · widget',o.widgetLunch)+specialRow('widgetGap',tr('Trou · widget','Free · widget','Freistunde · Widget'),o.widgetGap)+'</div>');
                  wireSpecialRows(box);
                }

                function ensureFullCoursePicker(){
                  const field=document.getElementById('courseColorField');if(!field)return;
                  let box=document.getElementById('fullCourseColorBox');
                  if(!box){
                    box=document.createElement('div');box.id='fullCourseColorBox';box.className='fullColorBox';
                    box.innerHTML='<div class="fullColorHead"><span class="fullColorTitle"></span><span id="fullCourseHex" class="fullColorValue"></span></div><div class="fullColorControls"><input id="fullCourseColor" type="color" value="#2F83E8"><div class="fullToneWrap"><div class="fullToneLabel"><span>'+tr('Plus sombre','Darker','Dunkler')+'</span><span>'+tr('Plus clair','Lighter','Heller')+'</span></div><input id="fullCourseTone" class="fullTone" type="range" min="-35" max="35" value="0"></div></div>';
                    field.insertBefore(box,document.getElementById('courseColorScope')||null);
                    const pick=box.querySelector('#fullCourseColor'),tone=box.querySelector('#fullCourseTone');
                    const activate=()=>{customActive=true;customBase=pick.value;customTone=Number(tone.value)||0;updateCoursePickerPreview()};
                    pick.addEventListener('input',()=>{tone.value='0';activate()});tone.addEventListener('input',activate);
                  }
                  const title=box.querySelector('.fullColorTitle');if(title)title.textContent=tr('Nuancier complet','Full colour picker','Vollständiger Farbwähler');
                  document.querySelectorAll('#courseColorPalette .courseColorChoice').forEach(b=>{
                    if(!b.dataset.fullFineBound){b.dataset.fullFineBound='1';b.addEventListener('click',()=>{customActive=false;box.classList.remove('fullColorActive')})}
                  });
                  updateCoursePickerPreview();
                }
                function updateCoursePickerPreview(){
                  const box=document.getElementById('fullCourseColorBox'),pick=document.getElementById('fullCourseColor'),tone=document.getElementById('fullCourseTone'),hex=document.getElementById('fullCourseHex');if(!box||!pick||!tone)return;
                  pick.value=isHex(customBase)?customBase:'#2F83E8';tone.value=String(customTone);tone.style.setProperty('--tone-base',pick.value);if(hex)hex.textContent=currentCustom();box.classList.toggle('fullColorActive',customActive);
                }
                function editedCourse(){
                  try{if(typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof selected==='undefined'||typeof editing==='undefined'||editing==null)return null;return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null}catch(e){return null}
                }
                function syncCoursePicker(){
                  ensureFullCoursePicker();const c=editedCourse();
                  if(c&&isHex(c.color)){customActive=true;customBase=c.color.toUpperCase();customTone=0}else{customActive=false;customBase='#2F83E8';customTone=0}
                  updateCoursePickerPreview();
                }

                function applyLiteral(el,hex,gradient){
                  if(!el||!isHex(hex))return;
                  const ink=contrast(hex),muted=ink==='#FFFFFF'?'#F4F7FB':'#37465B';
                  el.style.setProperty('background',gradient?('linear-gradient(90deg,'+hex+' 0%,#ffffff 97%)'):hex,'important');
                  el.style.setProperty('box-shadow','inset 0 0 0 1px '+darker(hex),'important');
                  el.querySelectorAll('.label,.cellLabel').forEach(x=>x.style.setProperty('color',ink,'important'));
                  el.querySelectorAll('.room,.time,.cellRoom').forEach(x=>x.style.setProperty('color',muted,'important'));
                }
                function repaintLiteralCourses(){
                  try{
                    if(typeof weeks!=='undefined'&&typeof activeWeek!=='undefined'&&typeof uniqueWeekTimes==='function'){
                      const ws=weeks[activeWeek],times=uniqueWeekTimes(),cells=Array.from(document.querySelectorAll('#weekGrid .wc'));let p=0;
                      for(const t of times)for(const d of [2,3,4,5,6]){const cell=cells[p++];if(!cell)continue;const c=ws&&ws[d]&&ws[d].courses?ws[d].courses.find(x=>x.start===t.start&&x.end===t.end):null;if(c&&isHex(c.color))applyLiteral(cell,c.color,false)}
                    }
                    if(typeof state!=='undefined'&&typeof selected!=='undefined'){
                      const list=state[selected]&&state[selected].courses?state[selected].courses:[];document.querySelectorAll('#editList .editCourse').forEach((row,i)=>{const c=list[i];if(c&&isHex(c.color))applyLiteral(row,c.color,true)})
                    }
                    if(typeof weeks!=='undefined'&&typeof currentWeek!=='undefined'&&typeof todayKey==='function'){
                      const d=todayKey(),list=weeks[currentWeek]&&weeks[currentWeek][d]?weeks[currentWeek][d].courses:[];
                      document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').forEach(row=>{const start=(row.querySelector('.time strong')||{}).textContent||'';let c=list.find(x=>x.start===start);if(c&&isHex(c.color))applyLiteral(row,c.color,true)})
                    }
                  }catch(e){}
                }

                function wrapCourseSubmit(){
                  const form=document.getElementById('courseForm');if(!form||!form.onsubmit||form.onsubmit.__fullColorWrapped)return;
                  const old=form.onsubmit;
                  const wrapped=function(e){
                    const chosen=customActive?currentCustom():null;
                    const week=typeof activeWeek!=='undefined'?activeWeek:'A',day=typeof selected!=='undefined'?selected:2,idx=typeof editing!=='undefined'?editing:null;
                    const oldCourse=(idx!=null&&typeof weeks!=='undefined'&&weeks[week]&&weeks[week][day])?weeks[week][day].courses[idx]:null;
                    const oldClass=oldCourse?oldCourse.label:'';
                    const slot=Number((document.getElementById('fSlot')||{}).value||0),text=((document.getElementById('fLabel')||{}).value||'').trim();
                    let start='',end='';if(slot>0&&typeof slots!=='undefined'&&slots[slot-1]){start=slots[slot-1].start;end=slots[slot-1].end}else if(oldCourse){start=oldCourse.start;end=oldCourse.end}
                    const classScope=document.getElementById('scopeClass')?.classList.contains('active')===true;
                    const result=old.call(this,e);
                    if(chosen){
                      try{
                        const arr=weeks[week]&&weeks[week][day]?weeks[week][day].courses:[];let target=arr.find(c=>c.start===start&&c.end===end&&c.label===text);if(!target)target=arr.find(c=>c.start===start&&c.end===end);if(target)target.color=chosen;
                        if(classScope){const match=norm(oldClass||text);Object.keys(weeks).forEach(w=>{const ws=weeks[w];if(!ws)return;Object.keys(ws).forEach(d=>{const dd=ws[d];if(dd&&Array.isArray(dd.courses))dd.courses.forEach(c=>{if(norm(c.label)===match)c.color=chosen})})})}
                        if(typeof save==='function')save();if(typeof render==='function')render();setTimeout(()=>{repaintLiteralCourses();try{if(window.AndroidSchedule&&AndroidSchedule.saveSchedule&&typeof exportState==='function')AndroidSchedule.saveSchedule(JSON.stringify(exportState()))}catch(ignore){}},0);
                      }catch(ignore){}
                    }
                    return result;
                  };
                  wrapped.__fullColorWrapped=true;form.onsubmit=wrapped;
                }

                function bindSyncToggle(){
                  const t=document.getElementById('paletteSyncToggle');if(!t||t.dataset.fineSyncBound)return;t.dataset.fineSyncBound='1';
                  t.addEventListener('change',()=>setTimeout(()=>{const o=loadSpecial();o.sync=t.checked;if(o.sync){o.widgetLunch=o.appLunch;o.widgetGap=o.appGap}saveSpecial(o);renderSpecialControls();applySpecialCss()},0));
                }

                function refresh(){
                  applySpecialCss();renderSpecialControls();ensureFullCoursePicker();wrapCourseSubmit();bindSyncToggle();repaintLiteralCourses();
                }
                window.refreshFineTuneUi=refresh;

                const modal=document.getElementById('modal');if(modal)new MutationObserver(()=>{if(modal.classList.contains('show'))setTimeout(syncCoursePicker,0)}).observe(modal,{attributes:true,attributeFilter:['class']});
                ['weekGrid','todayList','editList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(()=>setTimeout(repaintLiteralCourses,0)).observe(el,{childList:true,subtree:true})});
                const settings=document.getElementById('settingsModal');if(settings)new MutationObserver(()=>setTimeout(refresh,0)).observe(settings,{attributes:true,attributeFilter:['class']});
                setTimeout(refresh,0);setTimeout(refresh,120);setTimeout(refresh,500);
              }catch(e){console.log('FineTuneUi',e)}
            })();
            """;
    }
}
