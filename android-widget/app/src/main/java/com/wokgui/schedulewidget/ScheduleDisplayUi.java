package com.wokgui.schedulewidget;

/** Schedule display, cycle handling and interaction stability. */
final class ScheduleDisplayUi {
    private ScheduleDisplayUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(154 * 1024);
        out.append(layer0()).append('\n'); // FineTuneUi
        out.append(layer1()).append('\n'); // CycleLunchFixUi
        out.append(layer2()).append('\n'); // Stability69Ui
        out.append(layer3()).append('\n'); // Stability70Ui
        out.append(layer4()).append('\n'); // Stability71Ui
        out.append(layer5()).append('\n'); // Stability72Ui
        out.append(layer6()).append('\n'); // Stability73Ui
        out.append(layer7()).append('\n'); // Stability74Ui
        return out.toString();
    }

    // Former FineTuneUi; isolated to stay below JVM constant limits.
    private static String layer0() {
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
                  .fullColorEnable84{display:flex;align-items:center;justify-content:space-between;gap:10px;margin:2px 0 8px;padding:7px 8px;border:1px solid var(--line);border-radius:8px;background:#fff;font-size:.68rem;font-weight:850;color:var(--ink)}
                  .fullColorEnable84 input{width:18px;height:18px;accent-color:var(--blue)}
                  #fullCourseColorBox:not(.fullColorEnabled84) .fullColorControls{opacity:.40}
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

                  /* Midi reste coloré avec un liseré assorti, mais occupe toute la case sans encadré arrondi interne. */
                  #todayList .todayCourse.lunch{background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;border:0!important;border-radius:0!important;box-shadow:inset 0 0 0 1px var(--ft-midi-border)!important}
                  #todayList .todayCourse.gap{background:var(--ft-gap)!important;color:var(--ft-gap-ink)!important;border:0!important;border-radius:0!important;box-shadow:none!important}
                  #todayList .todayCourse.lunch .time,#todayList .todayCourse.lunch .room,#todayList .todayCourse.lunch .label{color:var(--ft-midi-ink)!important}
                  #todayList .todayCourse.gap .time,#todayList .todayCourse.gap .room,#todayList .todayCourse.gap .label{color:var(--ft-gap-ink)!important}
                  #todayList .todayCourse.lunch .label:before{content:'🍴'!important;display:inline-block!important;margin-right:6px!important}
                  #todayList .todayCourse.gap .label:before{content:none!important;display:none!important}

                  /* Trou : aucun rectangle intérieur, juste la cellule blanche du tableau. */
                  #weekGrid .wc.gapCell{background:var(--ft-gap)!important;color:var(--ft-gap-ink)!important;box-shadow:none!important;border-radius:0!important}
                  #weekGrid .wc.gapCell *{color:var(--ft-gap-ink)!important}
                  #weekGrid .wc.gapCell .cellLabel:before{content:none!important;display:none!important}

                  /* Midi : cellule totalement rectangulaire, liseré bord-à-bord de la teinte choisie. */
                  #weekGrid .wc.lunchCell{border-radius:0!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell){background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;box-shadow:inset 0 0 0 1px var(--ft-midi-border)!important}
                  #weekGrid .wc.lunchCell:not(.dynamicLunchCell) *{color:var(--ft-midi-ink)!important}
                  #weekGrid .dynamicLunchCell{background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;border-radius:0!important;overflow:hidden!important}
                  #weekGrid .dynamicLunchOverlay{inset:0!important;background:var(--ft-midi)!important;color:var(--ft-midi-ink)!important;border:0!important;border-radius:0!important;box-shadow:inset 0 0 0 1px var(--ft-midi-border)!important}
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
                  let enable=document.getElementById('fullCourseEnable84');
                  if(!enable){
                    const label=document.createElement('label');label.className='fullColorEnable84';
                    label.innerHTML='<span>'+tr('Activer le nuancier complet','Enable full colour picker','Vollständigen Farbwähler aktivieren')+'</span><input id="fullCourseEnable84" type="checkbox">';
                    box.insertBefore(label,box.querySelector('.fullColorControls')||null);enable=label.querySelector('input');
                    enable.addEventListener('change',()=>{
                      const pick=document.getElementById('fullCourseColor'),tone=document.getElementById('fullCourseTone');
                      if(pick)pick.disabled=!enable.checked;if(tone)tone.disabled=!enable.checked;
                      box.classList.toggle('fullColorEnabled84',enable.checked);
                      if(enable.checked&&pick){pick.dispatchEvent(new Event('input',{bubbles:true}))}else{customActive=false;updateCoursePickerPreview()}
                    });
                  }else{const s=enable.closest('label')?.querySelector('span');if(s)s.textContent=tr('Activer le nuancier complet','Enable full colour picker','Vollständigen Farbwähler aktivieren')}
                  const pick84=document.getElementById('fullCourseColor'),tone84=document.getElementById('fullCourseTone');
                  if(pick84)pick84.disabled=!enable.checked;if(tone84)tone84.disabled=!enable.checked;box.classList.toggle('fullColorEnabled84',enable.checked);
                  document.querySelectorAll('#courseColorPalette .courseColorChoice').forEach(b=>{
                    if(!b.dataset.fullFineBound){b.dataset.fullFineBound='1';b.addEventListener('click',()=>{customActive=false;box.classList.remove('fullColorActive');const e=document.getElementById('fullCourseEnable84');if(e){e.checked=false;const p=document.getElementById('fullCourseColor'),t=document.getElementById('fullCourseTone');if(p)p.disabled=true;if(t)t.disabled=true;box.classList.remove('fullColorEnabled84')}})}
                  });
                  updateCoursePickerPreview();
                }
                function updateCoursePickerPreview(){
                  const box=document.getElementById('fullCourseColorBox'),pick=document.getElementById('fullCourseColor'),tone=document.getElementById('fullCourseTone'),hex=document.getElementById('fullCourseHex');if(!box||!pick||!tone)return;
                  const nextBase=isHex(customBase)?customBase:'#2F83E8',nextTone=String(customTone),nextHex=currentCustom();
                  if(pick.value.toUpperCase()!==nextBase.toUpperCase())pick.value=nextBase;
                  if(tone.value!==nextTone)tone.value=nextTone;
                  if(tone.style.getPropertyValue('--tone-base')!==pick.value)tone.style.setProperty('--tone-base',pick.value);
                  if(hex&&hex.textContent!==nextHex)hex.textContent=nextHex;
                  box.classList.toggle('fullColorActive',customActive);
                }
                function editedCourse(){
                  try{if(typeof weeks==='undefined'||typeof activeWeek==='undefined'||typeof selected==='undefined'||typeof editing==='undefined'||editing==null)return null;return weeks[activeWeek]&&weeks[activeWeek][selected]?weeks[activeWeek][selected].courses[editing]||null:null}catch(e){return null}
                }
                function syncCoursePicker(prepared){
                  if(!prepared)ensureFullCoursePicker();const c=editedCourse();
                  customActive=false;
                  if(c&&isHex(c.color)){customBase=c.color.toUpperCase();customTone=0}else{customBase='#2F83E8';customTone=0}
                  const e=document.getElementById('fullCourseEnable84');if(e)e.checked=false;
                  const p=document.getElementById('fullCourseColor'),t=document.getElementById('fullCourseTone');if(p&&!p.disabled)p.disabled=true;if(t&&!t.disabled)t.disabled=true;
                  const box=document.getElementById('fullCourseColorBox');if(box)box.classList.toggle('fullColorEnabled84',false);
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
                    const chosen=(document.getElementById('fullCourseEnable84')?.checked===true&&customActive)?currentCustom():null;
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
                        if(typeof save==='function')save();if(typeof render==='function')render();setTimeout(()=>{repaintLiteralCourses();try{if(window.AndroidSchedule&&AndroidSchedule.saveSchedule&&typeof exportState==='function')AndroidSchedule.saveSchedule(JSON.stringify(exportState()))}catch(ignore){}},18);
                      }catch(ignore){}
                    }
                    return result;
                  };
                  wrapped.__fullColorWrapped=true;form.onsubmit=wrapped;
                }

                function bindSyncToggle(){
                  const t=document.getElementById('paletteSyncToggle');if(!t||t.dataset.fineSyncBound)return;t.dataset.fineSyncBound='1';
                  t.addEventListener('change',()=>setTimeout(()=>{const o=loadSpecial();o.sync=t.checked;if(o.sync){o.widgetLunch=o.appLunch;o.widgetGap=o.appGap}saveSpecial(o);renderSpecialControls();applySpecialCss()},18));
                }

                function refresh(){
                  applySpecialCss();renderSpecialControls();ensureFullCoursePicker();wrapCourseSubmit();bindSyncToggle();repaintLiteralCourses();
                }
                window.refreshFineTuneUi=refresh;
                (window.__edtCoursePanelPreparers648||(window.__edtCoursePanelPreparers648=[])).push({id:'full-course-color',run:function(){syncCoursePicker(true)}});

                let literalTimer84=0;
                function scheduleLiteral84(){if(literalTimer84)return;literalTimer84=setTimeout(()=>{literalTimer84=0;repaintLiteralCourses()},18)}
                const modal=document.getElementById('modal');if(modal)new MutationObserver(()=>{if(modal.classList.contains('show'))syncCoursePicker()}).observe(modal,{attributes:true,attributeFilter:['class']});
                ['weekGrid','todayList','editList'].forEach(id=>{const el=document.getElementById(id);if(el)new MutationObserver(scheduleLiteral84).observe(el,{childList:true,subtree:true})});
                const settings=document.getElementById('settingsModal');if(settings)new MutationObserver(()=>{if(settings.classList.contains('show'))setTimeout(refresh,18)}).observe(settings,{attributes:true,attributeFilter:['class']});
                refresh();
              }catch(e){console.log('FineTuneUi',e)}
            })();
            """;
    }

    // Former CycleLunchFixUi; isolated to stay below JVM constant limits.
    private static String layer1() {
        return """
            (function(){
              try{
                if(window.__cycleLunchFixV3){
                  if(window.refreshCycleLunchFix)window.refreshCycleLunchFix();
                  return;
                }
                window.__cycleLunchFixV3=true;
                const APP_VERSION='6.31';
                let switching=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function letters(n){return ['A','B','C','D'].slice(0,Math.max(2,Math.min(4,Number(n)||2)))}

                const style=document.createElement('style');
                style.id='cycleLunchFixV3Style';
                style.textContent=`
                  /* The four cycle choices always occupy exactly the same geometry.
                     Active state changes colour only: never padding, border width, font size or weight. */
                  #weekModeBar{min-height:44px!important;box-sizing:border-box!important;contain:layout style!important}
                  #weekModeBar .weekModeChoices{
                    display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;
                    grid-template-rows:32px!important;gap:4px!important;height:32px!important;min-height:32px!important;
                    align-items:stretch!important;overflow:visible!important
                  }
                  #weekModeBar .weekModeChoice,
                  #weekModeBar .weekModeChoice.active{
                    width:100%!important;height:32px!important;min-height:32px!important;max-height:32px!important;min-width:0!important;
                    margin:0!important;padding:0 3px!important;box-sizing:border-box!important;
                    border-width:1px!important;border-style:solid!important;border-radius:8px!important;
                    font-size:.64rem!important;font-weight:800!important;line-height:30px!important;letter-spacing:0!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;
                    transform:none!important;scale:1!important;box-shadow:none!important;
                    transition:none!important;animation:none!important;-webkit-tap-highlight-color:transparent!important
                  }
                  body.cycleSwitchBusy #weekModeBar .weekModeChoice{pointer-events:none!important}
                  .contextBar{gap:5px!important;padding-left:7px!important;padding-right:7px!important;overflow:hidden!important}
                  #currentWeekBtn,#currentWeekBtn.currentWeek{flex:0 0 132px!important;width:132px!important;height:30px!important;min-height:30px!important;max-height:30px!important;margin:0!important;padding:0 7px!important;box-sizing:border-box!important;font-size:.68rem!important;line-height:1!important;display:flex!important;align-items:center!important;justify-content:center!important;white-space:nowrap!important;overflow:hidden!important;transition:none!important;animation:none!important;transform:none!important}
                  #weekTabs.weekTabs{flex:0 0 199px!important;width:199px!important;min-width:199px!important;max-width:199px!important;height:30px!important;display:grid!important;grid-template-columns:repeat(4,47.5px)!important;grid-template-rows:30px!important;gap:3px!important;align-items:stretch!important;overflow:hidden!important}
                  #weekTabs .weekTab,#weekTabs .weekTab.active{width:47.5px!important;height:30px!important;min-width:47.5px!important;max-width:47.5px!important;min-height:30px!important;max-height:30px!important;margin:0!important;padding:0 1px!important;box-sizing:border-box!important;border-width:1px!important;border-style:solid!important;border-radius:999px!important;font-size:.575rem!important;font-weight:800!important;line-height:1!important;letter-spacing:-.01em!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;display:flex!important;align-items:center!important;justify-content:center!important;transform:none!important;scale:1!important;box-shadow:none!important;transition:none!important;animation:none!important;-webkit-tap-highlight-color:transparent!important}
                  #weekTabs .weekTab[aria-hidden="true"]{visibility:hidden!important;pointer-events:none!important}
                  body.singleWeekMode #currentWeekBtn{margin:0!important}
                  body.singleWeekMode #weekTabs{visibility:hidden!important;display:grid!important}

                  /* Midi is painted by one stable pseudo-layer instead of alternating legacy borders/box-shadows.
                     Its outline is 2 px: exactly the same thickness as the hours-column and days-row separators. */
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell{
                    position:relative!important;overflow:visible!important;border-radius:0!important;outline:0!important;
                    background:var(--ft-midi,#FFF9E8)!important;box-shadow:none!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell::before{display:none!important;content:none!important;
                    content:""!important;position:absolute!important;left:0!important;right:0;top:0!important;bottom:0!important;
                    z-index:0!important;pointer-events:none!important;box-sizing:border-box!important;
                    background:var(--ft-midi,#FFF9E8)!important;
                    border-top:2px solid var(--ft-midi-border,#C7AA62)!important;
                    border-bottom:2px solid var(--ft-midi-border,#C7AA62)!important;
                    border-left:0 solid transparent!important;border-right:0 solid transparent!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wh.timecol + .wc.lunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc:not(.lunchCell) + .wc.lunchCell::before{
                    border-left-width:2px!important;border-left-color:var(--ft-midi-border,#C7AA62)!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell:not(:has(+ .wc.lunchCell))::before{
                    border-right-width:2px!important;border-right-color:var(--ft-midi-border,#C7AA62)!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell:has(+ .wc.lunchCell){
                    border-right-color:transparent!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell:has(+ .wc.lunchCell)::before{
                    right:-2px!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .dynamicLunchOverlay,
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .nativeLunchLabel{
                    inset:0!important;border:0!important;border-radius:0!important;box-shadow:none!important;
                    background:transparent!important;z-index:2!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .cellLabel,
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell .breakFitLabel{position:relative!important;z-index:3!important}

                  /* Older finalLunch classes may still be present, but they no longer alter the visible band. */
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight{
                    box-shadow:none!important;border-radius:0!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight::after{display:none!important}

                  /* Center both copy actions inside Week cycle. */
                  #settingsSheet .cycleCopyCentered .advButtons{justify-content:center!important;text-align:center!important}
                  #settingsSheet .cycleCopyCentered .advButton{margin-left:auto!important;margin-right:auto!important;text-align:center!important}
                `;
                document.head.appendChild(style);

                function applySingleWeekUi(){
                  const a=loadAdv(),single=a.singleWeek===true;
                  document.body.classList.toggle('singleWeekMode',single);
                  const tabs=document.getElementById('weekTabs'),cw=document.getElementById('currentWeekBtn'),letter=document.getElementById('weekTitleLetter');
                  if(single){
                    try{if(typeof currentWeek!=='undefined')currentWeek='A';if(typeof activeWeek!=='undefined')activeWeek='A'}catch(e){}
                    if(tabs)tabs.style.setProperty('display','none','important');
                    if(cw){cw.textContent=tr('Semaine unique','Single week','Einzelwoche');cw.onclick=null}
                    if(letter)letter.textContent='A';
                  }else if(tabs){
                    tabs.style.removeProperty('display');
                  }
                }

                function centerCycleCopy(){
                  const title=document.getElementById('advCycleTitle'),box=title&&title.closest?title.closest('.settingBox'):null;
                  if(box)box.classList.add('cycleCopyCentered');
                }

                function markCycleButtons(){
                  const a=loadAdv(),mode=a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2));
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===mode));
                }

                function switchCycle(n){
                  n=Number(n);if(![1,2,3,4].includes(n)||switching)return;
                  const a0=loadAdv(),oldMode=a0.singleWeek===true?1:Math.max(2,Math.min(4,Number(a0.cycleLength)||2));
                  if(oldMode===n){markCycleButtons();return}
                  switching=true;document.body.classList.add('cycleSwitchBusy');
                  try{
                    const a=loadAdv();a.singleWeek=n===1;a.cycleLength=n===1?2:n;saveAdv(a);
                    if(n===1){
                      try{if(typeof currentWeek!=='undefined')currentWeek='A';if(typeof activeWeek!=='undefined')activeWeek='A'}catch(e){}
                      try{if(window.AndroidSchedule&&AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek('A')}catch(e){}
                    }else{
                      const allowed=letters(n);
                      try{
                        if(typeof currentWeek!=='undefined'&&!allowed.includes(currentWeek))currentWeek='A';
                        if(typeof activeWeek!=='undefined'&&!allowed.includes(activeWeek))activeWeek=(typeof currentWeek!=='undefined'?currentWeek:'A');
                      }catch(e){}
                    }
                    const sel=document.getElementById('advCycle');if(sel&&n>1)sel.value=String(n);

                    /* One state refresh, then one timetable render. No synthetic onchange and no repeated delayed renders. */
                    if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                    applySingleWeekUi();centerCycleCopy();markCycleButtons();
                    try{if(typeof render==='function')render()}catch(e){}
                    requestAnimationFrame(()=>{
                      try{if(window.refreshFinalPolish)window.refreshFinalPolish()}catch(e){}
                      applySingleWeekUi();markCycleButtons();
                      document.body.classList.remove('cycleSwitchBusy');switching=false;
                    });
                  }catch(e){document.body.classList.remove('cycleSwitchBusy');switching=false}
                }
                window.switchCycleStable=switchCycle;

                function bindCycleButtons(){
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>{
                    if(b.__cycleLunchFixBound)return;b.__cycleLunchFixBound=true;
                    b.onclick=function(e){e.preventDefault();e.stopPropagation();switchCycle(Number(b.dataset.m));return false};
                  });
                  markCycleButtons();
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}

                function refresh(){bindCycleButtons();applySingleWeekUi();centerCycleCopy();setVersion()}
                window.refreshCycleLunchFix=refresh;

                refresh();
                /* One late pass is enough because this layer is injected after FineTuneUi. */

              }catch(e){console.log('CycleLunchFixUi',e)}
            })();
            """;
    }

    // Former Stability69Ui; isolated to stay below JVM constant limits.
    private static String layer2() {
        return """
            (function(){
              try{
                if(window.__stability69V1){if(window.refreshStability69)window.refreshStability69();return}
                window.__stability69V1=true;
                const APP_VERSION='6.31';
                const STRONG='1.5px';
                let switching=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function toMin(v){const p=String(v||'').split(':').map(Number);return (p[0]||0)*60+(p[1]||0)}
                function currentMode(){const a=loadAdv();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2))}

                const style=document.createElement('style');
                style.id='stability69Style';
                style.textContent=`
                  :root{--week-strong-line:${STRONG}}

                  /* Four cycle choices are always present and always the same size. */
                  #weekModeBar .weekModeChoices{
                    display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;
                    grid-template-rows:32px!important;gap:4px!important;width:100%!important;min-width:0!important;
                    height:32px!important;min-height:32px!important;overflow:visible!important
                  }
                  #weekModeBar .weekModeChoice,#weekModeBar .weekModeChoice.active{
                    display:flex!important;align-items:center!important;justify-content:center!important;
                    width:100%!important;min-width:0!important;max-width:none!important;
                    height:32px!important;min-height:32px!important;max-height:32px!important;
                    margin:0!important;padding:0 2px!important;box-sizing:border-box!important;
                    border-width:1px!important;border-style:solid!important;border-radius:8px!important;
                    font-size:.61rem!important;font-weight:800!important;line-height:1!important;letter-spacing:-.01em!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;
                    transform:none!important;scale:1!important;box-shadow:none!important;
                    transition:none!important;animation:none!important;-webkit-tap-highlight-color:transparent!important
                  }
                  #weekModeBar .weekModeChoice[hidden]{display:flex!important}
                  body.cycle69Busy #weekModeBar .weekModeChoice{pointer-events:none!important}

                  /* Single-week mode keeps exactly the same context-bar geometry. */
                  body.singleWeekMode #weekTabs{display:grid!important;visibility:hidden!important;pointer-events:none!important}
                  #weekTabs,#weekTabs .weekTab,#currentWeekBtn{transition:none!important;animation:none!important;transform:none!important}

                  /* Restore the lighter strong separators and reuse exactly that thickness for lunch. */
                  #weekGrid>.wh.timecol{border-right-width:var(--week-strong-line)!important;border-right-style:solid!important;border-right-color:#cbd5e1!important}
                  #weekGrid>.wh.day{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}
                  #weekGrid>.wh.timecol:first-child{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}

                  /* Lunch has no rectangle/inner outline. Its strong edges ARE the existing grid lines. */
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell{
                    margin:0!important;padding:0!important;border-radius:0!important;outline:0!important;
                    box-shadow:none!important;background:var(--ft-midi,#FFF9E8)!important;overflow:hidden!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell::before{display:none!important;content:none!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchJoinedRight::after{display:none!important;content:none!important}
                  #weekGrid .lunch69TopLine{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}
                  #weekGrid .lunch69BottomLine{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}

                  /* Only one time indicator: horizontal, inside the current course cell. */
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,
                  #weekGrid [id*="WeekNow"],#weekGrid [id*="weekNow"],
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,
                  #weekGrid .nativeNowFull,#weekGrid .nativeNowPartial,#weekGrid .nativeNowDot,
                  #weekGrid .finalWeekNowRailV12,#weekGrid .finalWeekNowDotV12,
                  #weekGrid .finalWeekNowRailV13,#weekGrid .finalWeekNowDotV13{display:none!important}
                  #weekGrid .now69Course{position:relative!important;overflow:visible!important;z-index:24!important}
                  #weekGrid .now69Bar{position:absolute!important;left:0!important;right:0!important;height:2px!important;background:#1688F4!important;z-index:210!important;pointer-events:none!important}
                  #weekGrid .now69Dot{position:absolute!important;left:0!important;width:10px!important;height:10px!important;border-radius:50%!important;transform:translate(-50%,-50%)!important;background:#1688F4!important;border:2px solid #D9ECFF!important;box-sizing:border-box!important;z-index:211!important;pointer-events:none!important}
                `;
                document.head.appendChild(style);

                function ensureCycleChoices(){
                  const box=document.querySelector('#weekModeBar .weekModeChoices');if(!box)return;
                  const defs=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  for(const [v,label] of defs){
                    let b=box.querySelector('.weekModeChoice[data-m="'+v+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;box.appendChild(b)}
                    b.hidden=false;b.style.removeProperty('display');b.textContent=label;
                    b.onclick=e=>{e.preventDefault();e.stopPropagation();const target=Number(v);box.querySelectorAll('.weekModeChoice').forEach(x=>x.classList.toggle('active',Number(x.dataset.m)===target));requestAnimationFrame(()=>switchCycle(target));return false};
                  }
                  const order=new Map(defs.map((x,i)=>[x[0],i]));
                  [...box.querySelectorAll('.weekModeChoice')].sort((a,b)=>(order.get(a.dataset.m)??99)-(order.get(b.dataset.m)??99)).forEach(b=>box.appendChild(b));
                  markCycleChoices();
                }

                function markCycleChoices(){
                  const m=currentMode();
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===m));
                }

                function ensureTopWeekTabs(){
                  const box=document.getElementById('weekTabs');if(!box)return;
                  const prefix=lang()==='de'?'Woche ':(lang()==='en'?'Week ':'Semaine ');
                  for(const w of ['A','B','C','D']){
                    let b=box.querySelector('.weekTab[data-week="'+w+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekTab';b.dataset.week=w;box.appendChild(b)}
                    b.textContent=prefix+w;
                  }
                }

                function syncCycleDom(n){
                  n=Number(n)||currentMode();ensureCycleChoices();ensureTopWeekTabs();
                  document.body.classList.toggle('singleWeekMode',n===1);
                  const box=document.getElementById('weekTabs');
                  if(box){
                    box.style.setProperty('visibility',n===1?'hidden':'visible','important');
                    box.style.setProperty('pointer-events',n===1?'none':'auto','important');
                    [...box.querySelectorAll('.weekTab')].forEach((b,i)=>{
                      const visible=n>1&&i<n;
                      b.style.setProperty('visibility',visible?'visible':'hidden','important');
                      b.style.pointerEvents=visible?'auto':'none';b.tabIndex=visible?0:-1;
                      const w=b.dataset.week;
                      b.classList.toggle('active',visible&&w===(typeof activeWeek!=='undefined'?activeWeek:'A'));
                      b.onclick=visible?(()=>{if(typeof activeWeek!=='undefined'&&activeWeek===w)return;if(typeof activeWeek!=='undefined')activeWeek=w;try{if(typeof render==='function')render()}catch(e){}}):null;
                    });
                  }
                  const cw=document.getElementById('currentWeekBtn');
                  if(cw){
                    if(n===1){cw.textContent=tr('Semaine unique','Single week','Einzelwoche');cw.onclick=null}
                    else{
                      const cur=typeof currentWeek!=='undefined'?currentWeek:'A';if(window.setCurrentWeekLabel70)window.setCurrentWeekLabel70(cur);else cw.textContent=tr('Cette semaine : ','This week: ','Diese Woche: ')+cur;
                      cw.onclick=()=>{
                        const ls=['A','B','C','D'].slice(0,n),old=typeof currentWeek!=='undefined'?currentWeek:'A',idx=Math.max(0,ls.indexOf(old)),next=ls[(idx+1)%ls.length];
                        if(typeof currentWeek!=='undefined')currentWeek=next;if(typeof activeWeek!=='undefined')activeWeek=next;
                        try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(next)}catch(e){}
                        try{if(typeof render==='function')render()}catch(e){}syncCycleDom(n);
                      };
                    }
                  }
                  markCycleChoices();
                }

                function switchCycle(n){
                  n=Number(n);if(![1,2,3,4].includes(n)||switching)return;
                  const old=currentMode();if(old===n){syncCycleDom(n);return}
                  switching=true;document.body.classList.add('cycle69Busy');
                  try{
                    const a=loadAdv();a.singleWeek=n===1;a.cycleLength=n===1?2:n;saveAdv(a);
                    const beforeCurrent=typeof currentWeek!=='undefined'?currentWeek:'A';
                    const beforeActive=typeof activeWeek!=='undefined'?activeWeek:'A';
                    const allowed=['A','B','C','D'].slice(0,n===1?1:n);
                    let changedWeek=false;
                    if(n===1){
                      if(typeof currentWeek!=='undefined'&&currentWeek!=='A'){currentWeek='A';changedWeek=true}
                      if(typeof activeWeek!=='undefined'&&activeWeek!=='A'){activeWeek='A';changedWeek=true}
                      try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek('A')}catch(e){}
                    }else{
                      if(typeof currentWeek!=='undefined'&&!allowed.includes(currentWeek)){currentWeek='A';changedWeek=true;try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek('A')}catch(e){}}
                      if(typeof activeWeek!=='undefined'&&!allowed.includes(activeWeek)){activeWeek=typeof currentWeek!=='undefined'?currentWeek:'A';changedWeek=true}
                    }
                    const sel=document.getElementById('advCycle');if(sel&&n>1)sel.value=String(n);
                    syncCycleDom(n);
                    if(changedWeek){try{if(typeof render==='function')render()}catch(e){}syncCycleDom(n)}
                    else if(typeof mode!=='undefined'&&mode==='week'){paintWeek69()}
                  }catch(e){}finally{
                    document.body.classList.remove('cycle69Busy');switching=false;markCycleChoices();
                  }
                }
                window.switchCycle69=switchCycle;

                function rowsOf(grid){
                  const rows=[];if(!grid)return rows;
                  const dayCount=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.length:5;
                  for(const time of Array.from(grid.querySelectorAll(':scope > .wh.timecol'))){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(found.length<2)continue;
                    const cells=[];let n=time.nextElementSibling;
                    while(n&&cells.length<dayCount){if(n.classList&&n.classList.contains('wc'))cells.push(n);n=n.nextElementSibling}
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return rows;
                }

                function clearLunchLines(grid){
                  grid.querySelectorAll('.lunch69TopLine').forEach(x=>x.classList.remove('lunch69TopLine'));
                  grid.querySelectorAll('.lunch69BottomLine').forEach(x=>x.classList.remove('lunch69BottomLine'));
                  grid.querySelectorAll('.finalLunchCell,.finalLunchJoinedRight').forEach(c=>{
                    c.classList.remove('finalLunchCell','finalLunchJoinedRight');c.style.removeProperty('box-shadow');c.style.removeProperty('border-right-color');
                  });
                  grid.querySelectorAll('[data-midi-edge-v14="1"]').forEach(c=>{
                    c.style.removeProperty('border-right-color');c.style.removeProperty('border-bottom-color');c.removeAttribute('data-midi-edge-v14');
                  });
                }

                function isLunchCell(c){return !!(c&&(c.classList.contains('lunchCell')||c.classList.contains('dynamicLunchCell')||c.classList.contains('nativeLunchCell')))}
                function paintLunchLines(grid,rows){
                  clearLunchLines(grid);
                  rows.forEach((row,ri)=>row.cells.forEach((cell,di)=>{
                    if(!isLunchCell(cell))return;
                    cell.style.setProperty('box-shadow','none','important');cell.style.removeProperty('border-right-color');
                    cell.classList.add('lunch69BottomLine');
                    const above=ri>0?rows[ri-1].cells[di]:grid.querySelectorAll(':scope > .wh.day')[di];
                    if(above)above.classList.add('lunch69TopLine');
                  }));
                }

                function clearNow(grid){
                  grid.querySelectorAll('.now69Bar,.now69Dot,.finalNowBar,.finalNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('.now69Course,.finalNowCourse').forEach(x=>x.classList.remove('now69Course','finalNowCourse'));
                  grid.querySelectorAll('.nativeNowFull,.nativeNowPartial,.nativeNowDot,.scheduleNowRail,.scheduleNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('[id*="weekNow"],[id*="WeekNow"]').forEach(x=>x.remove());
                }

                function paintHorizontalNow(grid,rows){
                  clearNow(grid);if(!rows.length)return;
                  if(typeof mode!=='undefined'&&mode!=='week')return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}
                  const js=new Date(),day=js.getDay(),d=day===0?1:day+1;
                  const di=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.indexOf(d):-1;if(di<0)return;
                  const minute=js.getHours()*60+js.getMinutes();let row=null,frac=0;
                  for(const r of rows){if(minute>=r.start&&minute<r.end){row=r;frac=(minute-r.start)/Math.max(1,r.end-r.start);break}}
                  if(!row)return;const cell=row.cells[di];if(!cell||!cell.classList.contains('has'))return;
                  cell.classList.add('now69Course');const top=Math.max(0,Math.min(100,frac*100)).toFixed(4)+'%';
                  const bar=document.createElement('span');bar.className='now69Bar';bar.style.setProperty('top',top,'important');cell.appendChild(bar);
                  const dot=document.createElement('span');dot.className='now69Dot';dot.style.setProperty('top',top,'important');cell.appendChild(dot);
                }

                function paintWeek69(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;const rows=rowsOf(grid);paintLunchLines(grid,rows);paintHorizontalNow(grid,rows);
                }
                window.paintWeek69=paintWeek69;

                function wrapWeekRender(){
                  if(typeof window.renderWeek==='function'&&!window.renderWeek.__stability69){
                    const old=window.renderWeek;const w=function(){const r=old.apply(this,arguments);paintWeek69();return r};w.__stability69=true;window.renderWeek=w;try{renderWeek=w}catch(e){}
                  }
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){ensureCycleChoices();syncCycleDom(currentMode());wrapWeekRender();paintWeek69();setVersion()}
                window.refreshStability69=refresh;
                refresh();
                setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek69()},30000);
              }catch(e){console.log('Stability69Ui',e)}
            })();
            """;
    }

    // Former Stability70Ui; isolated to stay below JVM constant limits.
    private static String layer3() {
        return """
            (function(){
              try{
                if(window.__stability70V1){if(window.refreshStability70)window.refreshStability70();return}
                window.__stability70V1=true;
                const APP_VERSION='6.31';
                const STRONG='1.5px';
                let painting=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function toMin(v){const p=String(v||'').split(':').map(Number);return (p[0]||0)*60+(p[1]||0)}
                function modeCount(){const a=loadAdv();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2))}

                const style=document.createElement('style');
                style.id='stability70Style';
                style.textContent=`
                  :root{--week-strong-line:${STRONG}}

                  /* Stable top context bar: fixed geometry, no transition or resize when a week changes. */
                  .contextBar{gap:5px!important;padding-left:7px!important;padding-right:7px!important;overflow:hidden!important}
                  #currentWeekBtn,#currentWeekBtn.currentWeek{
                    flex:0 0 136px!important;width:136px!important;min-width:136px!important;max-width:136px!important;
                    height:30px!important;min-height:30px!important;max-height:30px!important;margin:0!important;padding:0 7px!important;
                    display:flex!important;align-items:center!important;justify-content:center!important;box-sizing:border-box!important;
                    font-size:.68rem!important;line-height:1!important;white-space:nowrap!important;overflow:hidden!important;
                    transition:none!important;animation:none!important;transform:none!important;box-shadow:none!important
                  }
                  #currentWeekBtn .cwLetter70{font-weight:900!important;color:var(--set-accent,var(--blue))!important}
                  #weekTabs.weekTabs{flex:1 1 auto!important;min-width:0!important;height:30px!important;display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:30px!important;gap:3px!important;overflow:hidden!important}
                  #weekTabs .weekTab,#weekTabs .weekTab.active{
                    width:100%!important;min-width:0!important;max-width:none!important;height:30px!important;min-height:30px!important;max-height:30px!important;
                    margin:0!important;padding:0 1px!important;display:flex!important;align-items:center!important;justify-content:center!important;
                    box-sizing:border-box!important;border-width:1px!important;border-style:solid!important;border-radius:999px!important;
                    font-size:.575rem!important;font-weight:800!important;line-height:1!important;white-space:nowrap!important;overflow:hidden!important;
                    transition:none!important;animation:none!important;transform:none!important;box-shadow:none!important
                  }
                  #weekTabs .weekTab[aria-hidden="true"]{visibility:hidden!important;pointer-events:none!important}
                  body.singleWeekMode #weekTabs{display:grid!important;visibility:hidden!important}

                  /* Four cycle choices are permanent and identical in size. */
                  #weekModeBar{margin-top:-4px!important;margin-bottom:4px!important;padding-top:5px!important;padding-bottom:5px!important}
                  #weekModeBar .weekModeChoices{display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:32px!important;gap:4px!important;width:100%!important;height:32px!important;overflow:visible!important}
                  #weekModeBar .weekModeChoice,#weekModeBar .weekModeChoice.active{
                    display:flex!important;align-items:center!important;justify-content:center!important;width:100%!important;min-width:0!important;max-width:none!important;
                    height:32px!important;min-height:32px!important;max-height:32px!important;margin:0!important;padding:0 2px!important;box-sizing:border-box!important;
                    border-width:1px!important;border-style:solid!important;border-radius:8px!important;font-size:.60rem!important;font-weight:800!important;line-height:1!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;transition:none!important;animation:none!important;transform:none!important;box-shadow:none!important
                  }
                  #weekModeBar .weekModeChoice[hidden]{display:flex!important}
                  #importPhoto{height:42px!important;min-height:42px!important;margin:5px 0!important;display:flex!important;align-items:center!important;justify-content:center!important;text-align:center!important}
                  #importStatus:empty{display:none!important}
                  #viewEdit .editTop{margin-top:0!important}

                  /* Strong separators use the actual grid borders, never an inset outline. */
                  #weekGrid>.wh.timecol{border-right-width:var(--week-strong-line)!important;border-right-style:solid!important;border-right-color:#cbd5e1!important}
                  #weekGrid>.wh.day{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}
                  #weekGrid>.wh.timecol:first-child{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}
                  #weekGrid .lunch70Top{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}
                  #weekGrid .lunch70Bottom{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}
                  #weekGrid .lunch70Left,#weekGrid .lunch70Right{border-right-width:var(--week-strong-line)!important;border-right-style:solid!important;border-right-color:var(--ft-midi-border,#C7AA62)!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell{
                    margin:0!important;padding:0!important;border-radius:0!important;outline:0!important;box-shadow:none!important;background:var(--ft-midi,#FFF9E8)!important;overflow:hidden!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell::before,
                  #weekGrid .finalLunchJoinedRight::after{display:none!important;content:none!important}

                  /* Hide every legacy vertical clock marker. The only visible marker is .now70Bar/.now70Dot. */
                  #weekGrid::before,#weekGrid::after{content:none!important;display:none!important}
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,#weekGrid [id*="WeekNow"],#weekGrid [id*="weekNow"],
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,#weekGrid .nativeNowFull,#weekGrid .nativeNowPartial,#weekGrid .nativeNowDot,
                  #weekGrid .now69Bar,#weekGrid .now69Dot,#weekGrid .finalNowBar,#weekGrid .finalNowDot,
                  #weekGrid .finalWeekNowRailV12,#weekGrid .finalWeekNowDotV12,#weekGrid .finalWeekNowRailV13,#weekGrid .finalWeekNowDotV13{display:none!important}
                  #weekGrid .now70Course{position:relative!important;overflow:visible!important;z-index:26!important}
                  #weekGrid .now70Bar{position:absolute!important;left:0!important;right:0!important;width:auto!important;height:2px!important;background:#1688F4!important;z-index:240!important;pointer-events:none!important;display:block!important}
                  #weekGrid .now70Dot{position:absolute!important;left:0!important;width:10px!important;height:10px!important;border-radius:50%!important;transform:translate(-50%,-50%)!important;background:#1688F4!important;border:2px solid #D9ECFF!important;box-sizing:border-box!important;z-index:241!important;pointer-events:none!important;display:block!important}

                  /* Independent application visibility for lunch/free periods; widget toggles remain separate. */
                  body.hideTodayLunch70 #todayList .todayCourse.lunch,body.hideTodayGaps70 #todayList .todayCourse.gap{display:none!important}
                  #weekGrid.hideWeekLunch70 .wc.lunchCell,#weekGrid.hideWeekLunch70 .wc.dynamicLunchCell,#weekGrid.hideWeekLunch70 .wc.nativeLunchCell{background:#fff!important;color:transparent!important;box-shadow:none!important}
                  #weekGrid.hideWeekLunch70 .wc.lunchCell *,#weekGrid.hideWeekLunch70 .wc.dynamicLunchCell *,#weekGrid.hideWeekLunch70 .wc.nativeLunchCell *{visibility:hidden!important}
                  #weekGrid.hideWeekGaps70 .wc.gapCell{background:#fff!important;color:transparent!important;box-shadow:none!important}
                  #weekGrid.hideWeekGaps70 .wc.gapCell *{visibility:hidden!important}

                  /* Settings alignment requested for the principal personalization groups. */
                  #appFontTitle,#widgetFontTitle,#languageTitle,#themeTitle,#appPaletteTitle,#widgetPaletteSeparateTitle,#breakDisplayTitle,
                  #fineSpecialColors>.settingTitle,#fineSpecialColors .coursePaletteHint{text-align:center!important}
                  #languageSelect{display:block!important;max-width:280px!important;margin-left:auto!important;margin-right:auto!important;text-align:center!important;text-align-last:center!important}
                  #themeGrid .themeButton,#paletteSettingRoot .coursePaletteBtn{text-align:center!important}
                  #paletteSettingRoot .coursePaletteHint,#breakDisplaySetting .coursePaletteHint{text-align:center!important}
                  #fineSpecialColors .specialColorRow>span{text-align:center!important}
                  #viewEdit .sectionHead h3{width:100%!important;text-align:center!important;flex:1 1 auto!important}

                  /* The two add-course actions are deliberately the same component. */
                  #addCourse,#addBulkCourses{
                    width:100%!important;height:42px!important;min-height:42px!important;margin-top:7px!important;padding:9px!important;
                    border:1.5px solid var(--blue,#0877f9)!important;border-radius:7px!important;background:#edf6ff!important;color:var(--blue,#0877f9)!important;
                    font-size:.84rem!important;font-weight:800!important;line-height:1.15!important;text-align:center!important;box-sizing:border-box!important
                  }

                  #breakVisibility70{margin-top:7px;border-top:1px solid #edf0f4;padding-top:7px}
                  #breakVisibility70 .breakVisTitle70{text-align:center;font-size:.68rem;font-weight:900;color:var(--set-dark,var(--blue));margin:5px 0}
                  #breakVisibility70 .breakVisRow70{display:grid;grid-template-columns:88px 1fr 1fr;gap:6px;align-items:center;margin:5px 0;font-size:.69rem}
                  #breakVisibility70 .breakVisRow70>span{font-weight:800;text-align:center}
                  #breakVisibility70 label,#breakWidget70 label{display:flex;align-items:center;justify-content:center;gap:5px;margin:0!important;font-size:.68rem!important}
                  #breakVisibility70 input,#breakWidget70 input{width:17px!important;height:17px!important;accent-color:var(--set-accent,var(--blue))}
                  #breakWidget70{display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-top:5px}
                `;
                document.head.appendChild(style);

                function setCurrentWeekLabel(letter){
                  const cw=document.getElementById('currentWeekBtn');if(!cw)return;
                  letter=String(letter||'A');
                  let p=cw.querySelector('.cwPrefix70'),b=cw.querySelector('.cwLetter70');
                  if(!p||!b){cw.textContent='';p=document.createElement('span');p.className='cwPrefix70';b=document.createElement('span');b.className='cwLetter70';cw.append(p,b)}
                  const prefix=tr('Cette semaine :\u00A0','This week: ','Diese Woche: ');
                  if(p.textContent!==prefix)p.textContent=prefix;if(b.textContent!==letter)b.textContent=letter;
                }
                window.setCurrentWeekLabel70=setCurrentWeekLabel;

                function ensureCycleChoices(){
                  const box=document.querySelector('#weekModeBar .weekModeChoices');if(!box)return;
                  const defs=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  for(const [v,label] of defs){
                    let b=box.querySelector('.weekModeChoice[data-m="'+v+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;box.appendChild(b)}
                    b.hidden=false;b.removeAttribute('hidden');b.style.removeProperty('display');b.textContent=label;
                    b.onclick=e=>{e.preventDefault();e.stopPropagation();const target=Number(v);box.querySelectorAll('.weekModeChoice').forEach(x=>x.classList.toggle('active',Number(x.dataset.m)===target));requestAnimationFrame(()=>{if(window.switchCycle69)window.switchCycle69(target);else if(window.switchCycleStable)window.switchCycleStable(target);syncCycleUi()});return false};
                  }
                  const order=new Map(defs.map((x,i)=>[x[0],i]));
                  [...box.querySelectorAll('.weekModeChoice')].sort((a,b)=>(order.get(a.dataset.m)??99)-(order.get(b.dataset.m)??99)).forEach(b=>box.appendChild(b));
                  const m=modeCount();box.querySelectorAll('.weekModeChoice').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===m));
                  const sel=document.getElementById('advCycle');
                  if(sel&&!sel.querySelector('option[value="4"]')){const o=document.createElement('option');o.value='4';o.textContent=tr('4 semaines','4 weeks','4 Wochen');sel.appendChild(o)}
                }

                function ensureTopTabs(){
                  const box=document.getElementById('weekTabs');if(!box)return;
                  const prefix=tr('Semaine ','Week ','Woche '),n=modeCount();
                  for(const w of ['A','B','C','D']){
                    let b=box.querySelector('.weekTab[data-week="'+w+'"]');if(!b){b=document.createElement('button');b.type='button';b.className='weekTab';b.dataset.week=w;box.appendChild(b)}
                    if(b.textContent!==prefix+w)b.textContent=prefix+w;
                    const idx='ABCD'.indexOf(w),visible=n>1&&idx>=0&&idx<n;
                    b.setAttribute('aria-hidden',visible?'false':'true');b.style.visibility=visible?'visible':'hidden';b.style.pointerEvents=visible?'auto':'none';b.tabIndex=visible?0:-1;
                    const active=typeof activeWeek!=='undefined'?activeWeek:'A';b.classList.toggle('active',visible&&active===w);
                    b.onclick=visible?(()=>{if(typeof activeWeek!=='undefined'&&activeWeek===w)return;if(typeof activeWeek!=='undefined')activeWeek=w;try{if(typeof render==='function')render()}catch(e){}syncCycleUi();paintWeek70()}):null;
                  }
                  document.body.classList.toggle('singleWeekMode',n===1);
                  box.style.visibility=n===1?'hidden':'visible';
                }

                function bindCurrentWeekButton(){
                  const cw=document.getElementById('currentWeekBtn');if(!cw)return;
                  const n=modeCount(),cur=typeof currentWeek!=='undefined'?currentWeek:'A';setCurrentWeekLabel(cur);
                  if(n===1){cw.onclick=null;return}
                  cw.onclick=()=>{
                    const list=['A','B','C','D'].slice(0,n),old=typeof currentWeek!=='undefined'?currentWeek:'A',idx=Math.max(0,list.indexOf(old)),next=list[(idx+1)%list.length];
                    if(typeof currentWeek!=='undefined')currentWeek=next;if(typeof activeWeek!=='undefined')activeWeek=next;
                    try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(next)}catch(e){}
                    try{if(AndroidSchedule.saveSchedule&&typeof exportState==='function')AndroidSchedule.saveSchedule(JSON.stringify(exportState()))}catch(e){}
                    setCurrentWeekLabel(next);ensureTopTabs();
                    try{if(typeof render==='function')render()}catch(e){}syncCycleUi();paintWeek70();
                  };
                }

                function syncCycleUi(){ensureCycleChoices();ensureTopTabs();bindCurrentWeekButton()}

                function rowsOf(grid){
                  const rows=[];if(!grid)return rows;const dayCount=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.length:5;
                  for(const time of Array.from(grid.querySelectorAll(':scope > .wh.timecol'))){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(found.length<2)continue;
                    const cells=[];let n=time.nextElementSibling;while(n&&cells.length<dayCount){if(n.classList&&n.classList.contains('wc'))cells.push(n);n=n.nextElementSibling}
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return rows;
                }

                function clearLunchEdges(grid){
                  grid.querySelectorAll('.lunch70Top,.lunch70Bottom,.lunch70Left,.lunch70Right').forEach(x=>x.classList.remove('lunch70Top','lunch70Bottom','lunch70Left','lunch70Right'));
                  grid.querySelectorAll('.lunch69TopLine,.lunch69BottomLine').forEach(x=>x.classList.remove('lunch69TopLine','lunch69BottomLine'));
                  grid.querySelectorAll('.finalLunchCell,.finalLunchJoinedRight').forEach(c=>{c.classList.remove('finalLunchCell','finalLunchJoinedRight');c.style.removeProperty('box-shadow');c.style.removeProperty('border-right-color')});
                  grid.querySelectorAll('[data-midi-edge-v14="1"]').forEach(c=>{c.style.removeProperty('border-right-color');c.style.removeProperty('border-bottom-color');c.removeAttribute('data-midi-edge-v14')});
                }
                function lunchCell(c){return !!(c&&(c.classList.contains('lunchCell')||c.classList.contains('dynamicLunchCell')||c.classList.contains('nativeLunchCell')))}
                function paintLunchEdges(grid,rows){
                  clearLunchEdges(grid);const a=loadAdv();if(a.showLunchWeek===false)return;
                  const heads=grid.querySelectorAll(':scope > .wh.day');
                  rows.forEach((row,ri)=>{
                    const flags=row.cells.map(c=>lunchCell(c));let i=0;
                    while(i<flags.length){if(!flags[i]){i++;continue}let j=i;while(j+1<flags.length&&flags[j+1])j++;
                      for(let k=i;k<=j;k++){
                        const cell=row.cells[k];cell.style.setProperty('box-shadow','none','important');cell.classList.add('lunch70Bottom');
                        const above=ri>0?rows[ri-1].cells[k]:heads[k];if(above)above.classList.add('lunch70Top');
                      }
                      const first=row.cells[i],last=row.cells[j],left=first?first.previousElementSibling:null;
                      if(left)left.classList.add('lunch70Left');if(last)last.classList.add('lunch70Right');i=j+1;
                    }
                  });
                }

                function removeLegacyNow(grid){
                  grid.querySelectorAll('.now70Bar,.now70Dot').forEach(x=>x.remove());grid.querySelectorAll('.now70Course').forEach(x=>x.classList.remove('now70Course'));
                  grid.querySelectorAll('.now69Bar,.now69Dot,.finalNowBar,.finalNowDot,.nativeNowFull,.nativeNowPartial,.nativeNowDot,.scheduleNowRail,.scheduleNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('[id*="weekNow"],[id*="WeekNow"]').forEach(x=>x.remove());
                  grid.querySelectorAll('.nativeNowTrackCell,.now69Course,.finalNowCourse').forEach(x=>x.classList.remove('nativeNowTrackCell','now69Course','finalNowCourse'));
                  grid.querySelectorAll('span').forEach(x=>{const s=((x.id||'')+' '+(typeof x.className==='string'?x.className:'')).toLowerCase();if(!x.classList.contains('now70Bar')&&!x.classList.contains('now70Dot')&&(s.includes('nowrail')||s.includes('nowdot')||s.includes('nownow')||s.includes('schedulerail')))x.remove()});
                }

                function paintHorizontalNow(grid,rows){
                  removeLegacyNow(grid);if(typeof mode!=='undefined'&&mode!=='week')return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}
                  const now=new Date(),js=now.getDay(),day=js===0?1:js+1,di=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.indexOf(day):-1;if(di<0)return;
                  const list=(typeof weeks!=='undefined'&&typeof currentWeek!=='undefined'&&weeks[currentWeek]&&weeks[currentWeek][day]&&Array.isArray(weeks[currentWeek][day].courses))?weeks[currentWeek][day].courses:[];
                  const minute=now.getHours()*60+now.getMinutes(),course=list.find(c=>minute>=toMin(c.start)&&minute<toMin(c.end));if(!course)return;
                  const row=rows.find(r=>r.start===toMin(course.start)&&r.end===toMin(course.end));if(!row)return;const cell=row.cells[di];if(!cell||!cell.classList.contains('has'))return;
                  const frac=(minute-toMin(course.start))/Math.max(1,toMin(course.end)-toMin(course.start)),top=Math.max(0,Math.min(100,frac*100)).toFixed(4)+'%';
                  cell.classList.add('now70Course');const bar=document.createElement('span');bar.className='now70Bar';bar.style.setProperty('top',top,'important');cell.appendChild(bar);
                  const dot=document.createElement('span');dot.className='now70Dot';dot.style.setProperty('top',top,'important');cell.appendChild(dot);
                }

                function applyBreakVisibility(){
                  const a=loadAdv(),grid=document.getElementById('weekGrid');
                  document.body.classList.toggle('hideTodayLunch70',a.showLunchToday===false);document.body.classList.toggle('hideTodayGaps70',a.showBreaksToday===false);
                  if(grid){grid.classList.toggle('hideWeekLunch70',a.showLunchWeek===false);grid.classList.toggle('hideWeekGaps70',a.showBreaksWeek===false)}
                }

                function paintWeek70(){
                  if(painting)return;painting=true;
                  try{const grid=document.getElementById('weekGrid');if(!grid)return;applyBreakVisibility();const rows=rowsOf(grid);paintLunchEdges(grid,rows);paintHorizontalNow(grid,rows)}finally{painting=false}
                }
                window.paintWeek70=paintWeek70;

                function installBreakVisibility(){
                  const box=document.getElementById('breakDisplaySetting');if(!box)return;let root=document.getElementById('breakVisibility70');
                  if(!root){root=document.createElement('div');root.id='breakVisibility70';const hint=document.getElementById('breakDisplayHint');if(hint&&hint.nextSibling)box.insertBefore(root,hint.nextSibling);else box.appendChild(root)}
                  root.innerHTML='<div class="breakVisTitle70">'+tr('Application','Application','App')+'</div><div class="breakVisRow70"><span>'+tr('Aujourd’hui','Today','Heute')+'</span><label><input id="showLunchToday70" type="checkbox"> Midi</label><label><input id="showGapsToday70" type="checkbox"> '+tr('Trous','Free','Freistunden')+'</label></div><div class="breakVisRow70"><span>'+tr('Semaine','Week','Woche')+'</span><label><input id="showLunchWeek70" type="checkbox"> Midi</label><label><input id="showGapsWeek70" type="checkbox"> '+tr('Trous','Free','Freistunden')+'</label></div><div class="breakVisTitle70">Widget</div><div id="breakWidget70"></div>';
                  const a=loadAdv(),defs=[['showLunchToday70','showLunchToday'],['showGapsToday70','showBreaksToday'],['showLunchWeek70','showLunchWeek'],['showGapsWeek70','showBreaksWeek']];
                  defs.forEach(([id,key])=>{const el=document.getElementById(id);if(!el)return;el.checked=a[key]!==false;el.onchange=()=>{const n=loadAdv();n[key]=el.checked;saveAdv(n);applyBreakVisibility();if(typeof mode!=='undefined'&&mode==='today'&&typeof renderToday==='function')renderToday();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()}});
                  const widget=document.getElementById('breakWidget70'),l=document.getElementById('advShowLunch'),g=document.getElementById('advShowBreaks');
                  if(widget){const lr=l&&l.closest('label'),gr=g&&g.closest('label');if(lr)widget.appendChild(lr);if(gr)widget.appendChild(gr)}
                  const ll=document.getElementById('advShowLunchLabel'),gl=document.getElementById('advShowBreaksLabel');if(ll)ll.textContent='Midi';if(gl)gl.textContent=tr('Trous','Free','Freistunden');
                }

                function polishSettings(){
                  installBreakVisibility();
                  const title=document.querySelectorAll('#viewEdit .sectionHead h3');title.forEach(h=>{const s=(h.textContent||'').toLowerCase();if(s.includes('horaire')||s.includes('period')||s.includes('stunden'))h.textContent=tr('Horaires des 9 heures','9 period times','Zeiten der 9 Stunden')});
                  const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                }

                function wrap(name,after){
                  const old=window[name];if(typeof old!=='function'||old.__stability70)return;
                  const w=function(){const r=old.apply(this,arguments);try{after()}catch(e){}return r};w.__stability70=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                function installWrappers(){
                  wrap('renderContext',()=>{syncCycleUi()});
                  wrap('renderWeek',()=>{syncCycleUi();paintWeek70()});
                  wrap('renderToday',()=>{applyBreakVisibility()});
                  wrap('renderEdit',()=>{syncCycleUi();polishSettings()});
                  wrap('refreshAdvancedFeatures',()=>{syncCycleUi();polishSettings();applyBreakVisibility();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()});
                  wrap('refreshSettingsV3',()=>{polishSettings();syncCycleUi()});
                  wrap('refreshCoursePaletteV4',()=>{polishSettings();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()});
                  wrap('refreshFineTuneUi',()=>{polishSettings();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()});
                }

                const grid=document.getElementById('weekGrid');
                if(grid&&!grid.__stability70Observed){
                  grid.__stability70Observed=true;
                  new MutationObserver(ms=>{
                    if(painting)return;
                    const external=ms.some(m=>Array.from(m.addedNodes||[]).some(n=>!(n.nodeType===1&&(n.classList.contains('now70Bar')||n.classList.contains('now70Dot')))));
                    if(external)paintWeek70();
                  }).observe(grid,{childList:true,subtree:true});
                }

                function refresh(){installWrappers();syncCycleUi();polishSettings();applyBreakVisibility();paintWeek70()}
                window.refreshStability70=refresh;
                refresh();
                setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek70()},30000);
              }catch(e){console.log('Stability70Ui',e)}
            })();
            """;
    }

    // Former Stability71Ui; isolated to stay below JVM constant limits.
    private static String layer4() {
        return """
            (function(){
              try{
                if(window.__stability71V1){if(window.refreshStability71)window.refreshStability71();return}
                window.__stability71V1=true;
                const APP_VERSION='6.31';
                let refreshQueued=false;

                const style=document.createElement('style');
                style.id='stability71Style';
                style.textContent=`
                  :root{--s71-heading-size:.84rem}

                  /* Week context and cycle selector are plain controls integrated into the page, not pills/cards. */
                  html body .contextBar{
                    height:43px!important;min-height:43px!important;padding:6px 8px!important;gap:4px!important;
                    background:var(--bg,#f6f8fb)!important;border-bottom:1px solid var(--line,#dce3eb)!important;
                    box-shadow:none!important;overflow:hidden!important
                  }
                  html body #currentWeekBtn,
                  html body #currentWeekBtn.currentWeek{
                    flex:0 0 136px!important;width:136px!important;min-width:136px!important;max-width:136px!important;
                    height:31px!important;min-height:31px!important;max-height:31px!important;margin:0!important;padding:0 3px!important;
                    display:flex!important;align-items:center!important;justify-content:center!important;box-sizing:border-box!important;
                    background:transparent!important;border:0!important;border-radius:0!important;box-shadow:none!important;
                    color:var(--ink,#111936)!important;font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1!important;
                    white-space:nowrap!important;transition:none!important;animation:none!important;transform:none!important
                  }
                  html body #currentWeekBtn .cwLetter70{color:var(--set-accent,var(--blue))!important;font-weight:900!important}
                  html body #weekTabs.weekTabs{
                    flex:1 1 auto!important;min-width:0!important;height:31px!important;display:grid!important;
                    grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:31px!important;gap:2px!important;overflow:hidden!important
                  }
                  html body #weekTabs .weekTab,
                  html body #weekTabs .weekTab.active{
                    width:100%!important;min-width:0!important;max-width:none!important;height:31px!important;min-height:31px!important;max-height:31px!important;
                    margin:0!important;padding:0 1px!important;display:flex!important;align-items:center!important;justify-content:center!important;
                    box-sizing:border-box!important;background:transparent!important;border:0!important;border-bottom:2px solid transparent!important;
                    border-radius:0!important;box-shadow:none!important;color:#536078!important;
                    font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1!important;letter-spacing:-.015em!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;transition:none!important;animation:none!important;transform:none!important
                  }
                  html body #weekTabs .weekTab.active{color:var(--set-accent,var(--blue))!important;border-bottom-color:var(--set-accent,var(--blue))!important}

                  html body #weekModeBar{
                    min-height:0!important;height:auto!important;margin:2px 0 5px!important;padding:8px 0 9px!important;
                    background:transparent!important;border:0!important;border-radius:0!important;box-shadow:none!important;contain:none!important
                  }
                  html body #weekModeBar .weekModeLabel{
                    font-size:var(--s71-heading-size)!important;font-weight:800!important;color:var(--ink,#111936)!important;line-height:1.15!important
                  }
                  html body #weekModeBar .weekModeChoices{
                    display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:34px!important;
                    width:100%!important;height:34px!important;min-height:34px!important;gap:3px!important;overflow:visible!important
                  }
                  html body #weekModeBar .weekModeChoice,
                  html body #weekModeBar .weekModeChoice.active{
                    display:flex!important;align-items:center!important;justify-content:center!important;width:100%!important;min-width:0!important;max-width:none!important;
                    height:34px!important;min-height:34px!important;max-height:34px!important;margin:0!important;padding:0 1px!important;
                    background:transparent!important;border:0!important;border-bottom:2px solid transparent!important;border-radius:0!important;box-shadow:none!important;
                    color:#536078!important;font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1!important;letter-spacing:-.015em!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;transition:none!important;animation:none!important;transform:none!important
                  }
                  html body #weekModeBar .weekModeChoice.active{color:var(--set-accent,var(--blue))!important;border-bottom-color:var(--set-accent,var(--blue))!important}

                  /* Edit view headings: same type size, centered and given more breathing room. */
                  html body #viewEdit .sectionHead{margin:14px 3px 10px!important}
                  html body #viewEdit .sectionHead h2,
                  html body #viewEdit .sectionHead h3{
                    font-size:var(--s71-heading-size)!important;font-weight:800!important;line-height:1.18!important
                  }
                  html body #viewEdit .sectionHead:has(#editDayTitle){
                    display:flex!important;flex-direction:column!important;align-items:center!important;justify-content:center!important;gap:3px!important;
                    margin-top:13px!important;margin-bottom:10px!important
                  }
                  html body #editDayTitle{width:100%!important;text-align:center!important;margin:0!important}
                  html body #editCount{position:static!important;transform:none!important;width:100%!important;text-align:center!important;font-size:.68rem!important;line-height:1!important}
                  html body #viewEdit .sectionHead:has(h3){margin-top:16px!important;margin-bottom:11px!important}

                  /* Midi / trous: Today, Week and Widget are three aligned rows. */
                  html body #breakVisibility70{
                    margin-top:7px!important;padding-top:5px!important;border-top:0!important;
                    display:grid!important;grid-template-columns:88px minmax(0,1fr) minmax(0,1fr)!important;
                    column-gap:6px!important;row-gap:8px!important;align-items:center!important
                  }
                  html body #breakVisibility70>.breakVisTitle70:first-child{display:none!important}
                  html body #breakVisibility70>.breakVisRow70{display:contents!important}
                  html body #breakVisibility70>.breakVisRow70>span{
                    font-size:.72rem!important;font-weight:800!important;text-align:center!important;align-self:center!important
                  }
                  html body #breakVisibility70>.breakVisRow70 label,
                  html body #breakWidget70 label{
                    display:flex!important;align-items:center!important;justify-content:center!important;gap:5px!important;
                    margin:0!important;font-size:.72rem!important;line-height:1.1!important
                  }
                  html body #breakVisibility70>.breakVisTitle70:has(+ #breakWidget70){
                    display:flex!important;align-items:center!important;justify-content:center!important;margin:0!important;
                    font-size:.72rem!important;font-weight:800!important;color:var(--ink,#111936)!important;text-align:center!important
                  }
                  html body #breakWidget70{
                    grid-column:2 / 4!important;display:grid!important;grid-template-columns:1fr 1fr!important;gap:6px!important;margin:0!important
                  }

                  /* The redundant Week cycle settings card is hidden; the four-choice control above remains authoritative. */
                  html body #settingsSheet .settingBox:has(#advCycleTitle){display:none!important}

                  /* Settings title: larger and with more space below it. */
                  html body #settingsSheet .settingsHead{
                    min-height:38px!important;margin:0 0 12px!important;padding:0 0 7px!important;align-items:center!important
                  }
                  html body #settingsSheet .settingsHead h2{
                    font-size:1.18rem!important;font-weight:850!important;line-height:1.15!important;margin:0!important
                  }

                  @media(max-width:560px){
                    :root{--s71-heading-size:.82rem}
                    html body #currentWeekBtn,html body #currentWeekBtn.currentWeek{flex-basis:132px!important;width:132px!important;min-width:132px!important;max-width:132px!important}
                    html body #weekTabs .weekTab,html body #weekTabs .weekTab.active{letter-spacing:-.025em!important}
                    html body #weekModeBar{padding-top:9px!important;padding-bottom:10px!important}
                  }
                `;
                document.head.appendChild(style);

                function hideCycleSettings(){
                  const t=document.getElementById('advCycleTitle'),box=t&&t.closest?t.closest('.settingBox'):null;
                  if(box)box.style.setProperty('display','none','important');
                }

                function polishEditTitle(){
                  const h=document.getElementById('editDayTitle');
                  if(h){
                    let txt=String(h.textContent||'');
                    if(lang()==='fr')txt=txt.replace(/ · semaine /i,' · Semaine ');
                    if(h.textContent!==txt)h.textContent=txt;
                  }
                }

                function lang(){
                  try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                }

                function polishBreaks(){
                  const root=document.getElementById('breakVisibility70');if(!root)return;
                  const titles=[...root.querySelectorAll('.breakVisTitle70')];
                  const widgetTitle=titles.find(x=>x.nextElementSibling&&x.nextElementSibling.id==='breakWidget70');
                  if(widgetTitle&&widgetTitle.textContent!=='Widget')widgetTitle.textContent='Widget';
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION}

                function refresh(){
                  refreshQueued=false;hideCycleSettings();polishEditTitle();polishBreaks();setVersion();
                }
                window.refreshStability71=refresh;

                function scheduleRefresh(){
                  if(refreshQueued)return;refreshQueued=true;requestAnimationFrame(refresh);
                }

                function wrap(name){
                  const old=window[name];if(typeof old!=='function'||old.__stability71)return;
                  const w=function(){const r=old.apply(this,arguments);scheduleRefresh();return r};w.__stability71=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['renderEdit','refreshSettingsV3','refreshAdvancedFeatures','refreshFineTuneUi','refreshStability70','refreshWeekendUi'].forEach(wrap);

                function observe(id){
                  const root=document.getElementById(id);if(!root||root.__stability71Observed)return;
                  root.__stability71Observed=true;new MutationObserver(()=>scheduleRefresh()).observe(root,{childList:true,subtree:true});
                }
                ['settingsSheet','viewEdit','contextBar'].forEach(observe);

                refresh();setTimeout(()=>{['settingsSheet','viewEdit','contextBar'].forEach(observe);refresh()},120);
              }catch(e){console.log('Stability71Ui',e)}
            })();
            """;
    }

    // Former Stability72Ui; isolated to stay below JVM constant limits.
    private static String layer5() {
        return """
            (function(){
              try{
                if(window.__stability72V1){if(window.refreshStability72)window.refreshStability72();return}
                window.__stability72V1=true;
                const APP_VERSION='6.31';

                const style=document.createElement('style');
                style.id='stability72Style';
                style.textContent=`
                  /* 6.12: the week selector uses two full-width rows.
                     This prevents 3/4 full labels from ever overlapping on phone screens. */
                  html body .contextBar{
                    display:grid!important;
                    grid-template-columns:minmax(0,1fr)!important;
                    grid-template-rows:31px 31px!important;
                    align-items:center!important;
                    justify-items:stretch!important;
                    height:69px!important;
                    min-height:69px!important;
                    padding:4px 9px 5px!important;
                    row-gap:2px!important;
                    column-gap:0!important;
                    overflow:visible!important;
                    background:var(--bg,#f6f8fb)!important;
                    box-sizing:border-box!important;
                  }

                  html body .contextBar #currentWeekBtn,
                  html body .contextBar #currentWeekBtn.currentWeek{
                    grid-row:1!important;
                    grid-column:1!important;
                    width:100%!important;
                    min-width:0!important;
                    max-width:none!important;
                    flex:none!important;
                    height:31px!important;
                    min-height:31px!important;
                    max-height:31px!important;
                    margin:0!important;
                    padding:0 4px!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    background:transparent!important;
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    font-size:.82rem!important;
                    font-weight:800!important;
                    line-height:1!important;
                    white-space:nowrap!important;
                    overflow:visible!important;
                  }

                  html body .contextBar #weekTabs.weekTabs{
                    grid-row:2!important;
                    grid-column:1!important;
                    width:100%!important;
                    min-width:0!important;
                    max-width:none!important;
                    height:31px!important;
                    min-height:31px!important;
                    margin:0!important;
                    padding:0!important;
                    display:flex!important;
                    align-items:stretch!important;
                    justify-content:stretch!important;
                    gap:5px!important;
                    overflow:visible!important;
                    visibility:visible!important;
                  }

                  html body .contextBar #weekTabs .weekTab,
                  html body .contextBar #weekTabs .weekTab.active{
                    flex:1 1 0!important;
                    width:auto!important;
                    min-width:0!important;
                    max-width:none!important;
                    height:31px!important;
                    min-height:31px!important;
                    max-height:31px!important;
                    margin:0!important;
                    padding:0 2px!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    background:transparent!important;
                    border:0!important;
                    border-bottom:2px solid transparent!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    color:#536078!important;
                    font-size:.78rem!important;
                    font-weight:800!important;
                    line-height:1!important;
                    letter-spacing:-.01em!important;
                    white-space:nowrap!important;
                    overflow:visible!important;
                    text-overflow:clip!important;
                    transition:none!important;
                    animation:none!important;
                    transform:none!important;
                  }
                  html body .contextBar #weekTabs .weekTab.active{
                    color:var(--set-accent,var(--blue))!important;
                    border-bottom-color:var(--set-accent,var(--blue))!important;
                  }
                  html body .contextBar #weekTabs .weekTab[aria-hidden="true"]{
                    display:none!important;
                    visibility:hidden!important;
                    pointer-events:none!important;
                  }

                  /* One-week mode collapses the second row completely. */
                  html body.singleWeekMode .contextBar{
                    grid-template-rows:31px!important;
                    height:39px!important;
                    min-height:39px!important;
                    padding-top:4px!important;
                    padding-bottom:4px!important;
                  }
                  html body.singleWeekMode .contextBar #weekTabs.weekTabs{display:none!important}

                  @media(max-width:390px){
                    html body .contextBar{padding-left:6px!important;padding-right:6px!important}
                    html body .contextBar #currentWeekBtn,
                    html body .contextBar #currentWeekBtn.currentWeek{font-size:.80rem!important}
                    html body .contextBar #weekTabs.weekTabs{gap:2px!important}
                    html body .contextBar #weekTabs .weekTab,
                    html body .contextBar #weekTabs .weekTab.active{
                      padding-left:1px!important;padding-right:1px!important;
                      font-size:.72rem!important;letter-spacing:-.025em!important
                    }
                  }
                `;
                document.head.appendChild(style);

                function refresh(){
                  const v=document.getElementById('appVersionInfo');
                  if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION;
                  /* Remove stale fixed widths left inline by earlier layers. CSS above is authoritative. */
                  const cw=document.getElementById('currentWeekBtn');
                  if(cw){cw.style.removeProperty('width');cw.style.removeProperty('min-width');cw.style.removeProperty('max-width');}
                  const tabs=document.getElementById('weekTabs');
                  if(tabs&& !document.body.classList.contains('singleWeekMode'))tabs.style.setProperty('visibility','visible','important');
                }
                window.refreshStability72=refresh;

                function wrap(name){
                  const old=window[name];if(typeof old!=='function'||old.__stability72)return;
                  const w=function(){const r=old.apply(this,arguments);requestAnimationFrame(refresh);return r};
                  w.__stability72=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['renderContext','renderWeek','renderEdit','refreshStability70','refreshStability71'].forEach(wrap);

                refresh();
                requestAnimationFrame(refresh);
              }catch(e){console.log('Stability72Ui',e)}
            })();
            """;
    }

    // Former Stability73Ui; isolated to stay below JVM constant limits.
    private static String layer6() {
        return """
            (function(){
              try{
                if(window.__stability73V1){if(window.refreshStability73)window.refreshStability73();return}
                window.__stability73V1=true;
                const APP_VERSION='6.31';
                let applying=false;
                let lastPointerAt=0;
                let lastPointerMode=0;
                let normalizeQueued=false;

                function loadAdv(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}
                }
                function currentMode(){
                  const a=loadAdv();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2));
                }
                function deep(o){try{return JSON.parse(JSON.stringify(o))}catch(e){return o}}

                const style=document.createElement('style');
                style.id='stability73Style';
                style.textContent=`
                  /* 6.13: these four controls must never become untappable because an older
                     cycle layer temporarily leaves a busy class or pointer-events:none behind. */
                  html body #weekModeBar{position:relative!important;z-index:20!important;pointer-events:auto!important}
                  html body #weekModeBar .weekModeChoices{position:relative!important;z-index:21!important;pointer-events:auto!important}
                  html body #weekModeBar .weekModeChoice,
                  html body #weekModeBar .weekModeChoice.active,
                  html body.cycle69Busy #weekModeBar .weekModeChoice,
                  html body.cycleSwitchBusy #weekModeBar .weekModeChoice,
                  html body.cycleSwitchBusy #weekModeBar .weekModeChoice.active,
                  html body.cycle69Busy #weekModeBar .weekModeChoice.active{
                    pointer-events:auto!important;
                    touch-action:manipulation!important;
                    -webkit-user-select:none!important;
                    user-select:none!important;
                    -webkit-tap-highlight-color:transparent!important;
                    position:relative!important;
                    z-index:22!important;
                    cursor:pointer!important;
                  }
                  html body #weekModeBar .weekModeChoice:active{opacity:.68!important}
                `;
                document.head.appendChild(style);

                function ensureExtraWeekObjects(){
                  try{
                    if(typeof weeks==='undefined'||!weeks)return;
                    const empty=()=>{const w={};for(const d of (typeof DAYS!=='undefined'&&Array.isArray(DAYS)?DAYS:[2,3,4,5,6]))w[d]={enabled:true,courses:[]};return w};
                    if(!weeks.C)weeks.C=weeks.A?deep(weeks.A):empty();
                    if(!weeks.D)weeks.D=weeks.B?deep(weeks.B):empty();
                  }catch(e){}
                }

                function markMode(n){
                  document.querySelectorAll('#weekModeBar .weekModeChoice').forEach(b=>{
                    const active=Number(b.dataset.m)===Number(n);
                    if(b.classList.contains('active')!==active)b.classList.toggle('active',active);
                    b.setAttribute('aria-pressed',active?'true':'false');
                  });
                }

                function normalizeButtons(){
                  normalizeQueued=false;
                  document.body.classList.remove('cycle69Busy','cycleSwitchBusy');
                  const bar=document.getElementById('weekModeBar');if(!bar)return;
                  const box=bar.querySelector('.weekModeChoices');if(!box)return;
                  const defs=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  defs.forEach(([v,label])=>{
                    let b=box.querySelector('.weekModeChoice[data-m="'+v+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;box.appendChild(b)}
                    b.type='button';b.hidden=false;b.removeAttribute('hidden');b.style.removeProperty('display');
                    if(b.textContent!==label)b.textContent=label;
                    /* Event handling is delegated at document level below. Removing old
                       onclick closures prevents stale busy locks from swallowing a tap. */
                    b.onclick=null;
                    b.style.setProperty('pointer-events','auto','important');
                    b.style.setProperty('touch-action','manipulation','important');
                  });
                  const order={'1':0,'2':1,'3':2,'4':3};
                  // Keep the existing order without moving already-correct buttons. The
                  // child-list observer below must not queue itself again on every frame.
                  [...box.querySelectorAll('.weekModeChoice')].sort((a,b)=>(order[a.dataset.m]??99)-(order[b.dataset.m]??99)).forEach((b,i)=>{
                    if(box.children[i]!==b)box.insertBefore(b,box.children[i]||null);
                  });
                  markMode(currentMode());
                }

                function queueNormalize(){
                  if(normalizeQueued)return;normalizeQueued=true;requestAnimationFrame(normalizeButtons);
                }

                function applyMode(n){
                  n=Number(n);if(![1,2,3,4].includes(n)||applying)return;
                  const old=currentMode();
                  markMode(n);
                  if(old===n){normalizeButtons();return}
                  applying=true;
                  try{
                    const a=loadAdv();
                    a.singleWeek=n===1;
                    a.cycleLength=n===1?2:n;
                    AndroidSchedule.saveAdvancedSettings(JSON.stringify(a));
                    ensureExtraWeekObjects();

                    const allowed=['A','B','C','D'].slice(0,n===1?1:n);
                    let cur=(typeof currentWeek!=='undefined'?currentWeek:'A');
                    let act=(typeof activeWeek!=='undefined'?activeWeek:cur);
                    if(!allowed.includes(cur))cur='A';
                    if(!allowed.includes(act))act=cur;
                    if(n===1){cur='A';act='A'}
                    try{if(typeof currentWeek!=='undefined')currentWeek=cur;if(typeof activeWeek!=='undefined')activeWeek=act}catch(e){}
                    try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(cur)}catch(e){}

                    const sel=document.getElementById('advCycle');if(sel)sel.value=String(a.cycleLength);
                    document.body.classList.toggle('singleWeekMode',n===1);

                    requestAnimationFrame(()=>{
                      try{
                        /* Reload the advanced closure from native storage, then render once.
                           This avoids the competing synthetic onchange/click chains used before. */
                        if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                        if(typeof render==='function')render();
                        if(window.refreshStability70)window.refreshStability70();
                        if(window.refreshStability71)window.refreshStability71();
                        if(window.refreshStability72)window.refreshStability72();
                      }catch(e){}
                      applying=false;
                      normalizeButtons();
                    });
                  }catch(e){applying=false;normalizeButtons()}
                }
                window.applyWeekMode73=applyMode;

                function resolveButton(e){
                  let b=e.target&&e.target.closest?e.target.closest('#weekModeBar .weekModeChoice'):null;
                  if(!b&&Number.isFinite(e.clientX)&&Number.isFinite(e.clientY)){
                    const p=document.elementFromPoint(e.clientX,e.clientY);b=p&&p.closest?p.closest('#weekModeBar .weekModeChoice'):null;
                  }
                  return b;
                }

                /* Capture before every legacy onclick handler. Pointer-up makes Android WebView
                   taps reliable even when a prior layer rebuilt a button between down and click. */
                document.addEventListener('pointerup',e=>{
                  const b=resolveButton(e);if(!b)return;
                  e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();
                  const n=Number(b.dataset.m);lastPointerAt=Date.now();lastPointerMode=n;applyMode(n);
                },true);
                document.addEventListener('click',e=>{
                  const b=resolveButton(e);if(!b)return;
                  e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();
                  const n=Number(b.dataset.m);
                  if(Date.now()-lastPointerAt<700&&n===lastPointerMode)return;
                  applyMode(n);
                },true);

                const bar=document.getElementById('weekModeBar');
                if(bar&&!bar.__stability73Observed){
                  bar.__stability73Observed=true;
                  new MutationObserver(()=>queueNormalize()).observe(bar,{childList:true,subtree:true,attributes:true,attributeFilter:['style','class','hidden']});
                }

                function refresh(){
                  const v=document.getElementById('appVersionInfo');if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION;
                  normalizeButtons();
                }
                window.refreshStability73=refresh;
                refresh();
              }catch(e){console.log('Stability73Ui',e)}
            })();
            """;
    }

    // Former Stability74Ui; isolated to stay below JVM constant limits.
    private static String layer7() {
        return """
            (function(){
              try{
                if(window.__stability74V1){if(window.refreshStability74)window.refreshStability74();return}
                window.__stability74V1=true;
                const APP_VERSION='6.31';
                let queued=false;
                let arranging=false;

                function lang(){
                  try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}

                const style=document.createElement('style');
                style.id='stability74Style';
                style.textContent=`
                  /* Previews sit immediately above Theme, with each caption centred over its own preview. */
                  html body #settingsSheet .previewGrid{
                    margin:10px 0 9px!important;
                    gap:10px!important;
                    align-items:start!important
                  }
                  html body #settingsSheet .previewGrid>div{min-width:0!important}
                  html body #settingsSheet .previewLabel{
                    width:100%!important;
                    margin:0 0 6px!important;
                    text-align:center!important;
                    font-size:.76rem!important;
                    font-weight:850!important;
                    color:var(--ink,#111936)!important;
                    line-height:1.15!important
                  }
                  html body #settingsSheet .previewGrid .preview{margin:0 auto!important;width:100%!important;box-sizing:border-box!important}

                  /* Requested order: Theme -> palettes -> Midi/trous (including full colour controls). */
                  html body #paletteSettingRoot{margin-top:9px!important}
                  html body #breakDisplaySetting{margin-top:9px!important}

                  /* Full colours are now a subsection of Midi et trous, not a separate settings tile. */
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74{
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    background:transparent!important;
                    margin:11px 0 0!important;
                    padding:11px 0 0!important;
                    border-top:1px solid var(--line,#dce3eb)!important
                  }
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74>.settingTitle{
                    text-align:center!important;
                    margin:0 0 5px!important;
                    font-size:.76rem!important;
                    font-weight:850!important
                  }
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74>.coursePaletteHint{
                    text-align:center!important;
                    margin:0 0 9px!important;
                    line-height:1.3!important
                  }
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74 .specialColorRow>span{text-align:center!important}

                  /* Midi/trous is exactly three aligned functional rows: Today, Week, Widget. */
                  html body #breakVisibility70{
                    display:grid!important;
                    grid-template-columns:92px minmax(0,1fr) minmax(0,1fr)!important;
                    column-gap:7px!important;
                    row-gap:9px!important;
                    align-items:center!important;
                    margin:8px 0 0!important;
                    padding:0!important;
                    border:0!important
                  }
                  html body #breakVisibility70>.breakVisTitle70:first-child{display:none!important}
                  html body #breakVisibility70>.breakVisRow70{display:contents!important}
                  html body #breakVisibility70>.breakVisRow70>span{
                    text-align:center!important;
                    font-size:.72rem!important;
                    font-weight:850!important;
                    color:var(--ink,#111936)!important
                  }
                  html body #breakVisibility70>.breakVisRow70 label,
                  html body #breakWidget70 label{
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    gap:5px!important;
                    margin:0!important;
                    min-width:0!important;
                    font-size:.71rem!important;
                    line-height:1.15!important;
                    white-space:nowrap!important
                  }
                  html body #breakVisibility70 #breakWidgetTitle74{
                    display:flex!important;
                    grid-column:1!important;
                    grid-row:3!important;
                    align-items:center!important;
                    justify-content:center!important;
                    margin:0!important;
                    padding:0!important;
                    text-align:center!important;
                    font-size:.72rem!important;
                    font-weight:850!important;
                    color:var(--ink,#111936)!important
                  }
                  html body #breakVisibility70 #breakWidget70{
                    grid-column:2 / 4!important;
                    grid-row:3!important;
                    display:grid!important;
                    grid-template-columns:1fr 1fr!important;
                    gap:7px!important;
                    align-items:center!important;
                    margin:0!important;
                    padding:0!important;
                    border:0!important
                  }
                  html body #breakVisibility70 #breakWidget70:empty{display:grid!important}

                  @media(max-width:390px){
                    html body #settingsSheet .previewGrid{gap:7px!important}
                    html body #breakVisibility70{grid-template-columns:78px minmax(0,1fr) minmax(0,1fr)!important;column-gap:4px!important}
                    html body #breakVisibility70>.breakVisRow70 label,
                    html body #breakWidget70 label{font-size:.67rem!important;gap:3px!important}
                  }
                `;
                document.head.appendChild(style);

                function findThemeBox(){
                  const t=document.getElementById('themeTitle');return t&&t.closest?t.closest('.settingBox'):null;
                }

                function arrangeOrder(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  const theme=findThemeBox(),preview=sheet.querySelector('.previewGrid'),palette=document.getElementById('paletteSettingRoot'),breaks=document.getElementById('breakDisplaySetting');
                  if(theme&&preview&&preview.nextElementSibling!==theme)sheet.insertBefore(preview,theme);
                  if(theme&&palette&&theme.nextElementSibling!==palette)theme.insertAdjacentElement('afterend',palette);
                  if(palette&&breaks&&palette.nextElementSibling!==breaks)palette.insertAdjacentElement('afterend',breaks);
                }

                function embedFullColors(){
                  const breaks=document.getElementById('breakDisplaySetting'),full=document.getElementById('fineSpecialColors');
                  if(!breaks||!full)return;
                  full.classList.add('embeddedFullColors74');
                  if(full.parentElement!==breaks)breaks.appendChild(full);
                }

                function fixPreviewLabels(){
                  const a=document.getElementById('appPreviewLabel'),w=document.getElementById('widgetPreviewLabel');
                  if(a)a.textContent=tr('Aperçu de l’application','App preview','App-Vorschau');
                  if(w)w.textContent=tr('Aperçu du widget','Widget preview','Widget-Vorschau');
                }

                function normalizeBreakWidgetRow(){
                  const root=document.getElementById('breakVisibility70');if(!root)return;
                  const widget=document.getElementById('breakWidget70');
                  let title=document.getElementById('breakWidgetTitle74');
                  if(!title){
                    const candidates=[...root.querySelectorAll(':scope > .breakVisTitle70')];
                    title=candidates.find(x=>x!==root.querySelector(':scope > .breakVisTitle70:first-child') && /widget/i.test(String(x.textContent||'')));
                    if(title)title.id='breakWidgetTitle74';
                  }
                  if(title)title.textContent='Widget';

                  /* Legacy refreshes may move these two labels. Put both real widget toggles back into the Widget row. */
                  if(widget){
                    const lunch=document.getElementById('advShowLunch'),gap=document.getElementById('advShowBreaks');
                    const lr=lunch&&lunch.closest?lunch.closest('label'):null;
                    const gr=gap&&gap.closest?gap.closest('label'):null;
                    if(lr&&lr.parentElement!==widget)widget.appendChild(lr);
                    if(gr&&gr.parentElement!==widget)widget.appendChild(gr);
                    const ll=document.getElementById('advShowLunchLabel'),gl=document.getElementById('advShowBreaksLabel');
                    if(ll)ll.textContent='Midi';
                    if(gl)gl.textContent=tr('Trous','Free periods','Freistunden');
                  }
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION}

                function refresh(){
                  queued=false;if(arranging)return;arranging=true;
                  try{arrangeOrder();fixPreviewLabels();normalizeBreakWidgetRow();embedFullColors();setVersion()}finally{arranging=false}
                }
                window.refreshStability74=refresh;

                function schedule(){if(queued)return;queued=true;requestAnimationFrame(refresh)}
                function wrap(name){
                  const old=window[name];if(typeof old!=='function'||old.__stability74)return;
                  const w=function(){const r=old.apply(this,arguments);schedule();return r};w.__stability74=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['refreshSettingsV3','refreshCoursePaletteV4','refreshFineTuneUi','refreshAdvancedFeatures','refreshStability70','refreshStability71','refreshStability72','refreshStability73'].forEach(wrap);

                const sheet=document.getElementById('settingsSheet');
                if(sheet&&!sheet.__stability74Observed){
                  sheet.__stability74Observed=true;
                  new MutationObserver(()=>{if(!arranging)schedule()}).observe(sheet,{childList:true,subtree:true});
                }

                refresh();requestAnimationFrame(refresh);setTimeout(refresh,120);
              }catch(e){console.log('Stability74Ui',e)}
            })();
            """;
    }

}
