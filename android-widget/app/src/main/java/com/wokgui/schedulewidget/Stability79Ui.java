package com.wokgui.schedulewidget;

final class Stability79Ui {
    private Stability79Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability79V1){if(window.refreshStability79)window.refreshStability79();return}
                window.__stability79V1=true;
                const APP_VERSION='6.19';
                const PACK_PREFIX='edt-language-pack-v1-';
                const CATALOG_URL='https://raw.githubusercontent.com/Wokgui/Emploi-du-temps/main/language-packs/catalog.json';
                let refreshing=false,languageTimer=0,lastWeekPointer=0,lunchRaf=0;

                function loadUi(){try{return JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){return {language:'fr',widgetFontScale:1}}}
                function saveUi(o){try{AndroidSchedule.saveUiSettings(JSON.stringify(o))}catch(e){}}
                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function lang(){return String(loadUi().language||'fr')}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function weekCount(){const a=loadAdv();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2))}
                function allowedWeeks(){return ['A','B','C','D'].slice(0,weekCount())}

                const PALETTES={
                  vivid:['#F0335D','#FF7B2F'],pastel:['#F58BA6','#FFAD72'],warm:['#EF5968','#FF7B72'],
                  cool:['#3E91B8','#42B6BE'],soft:['#7B8FA4','#9AA7AF']
                };

                const style=document.createElement('style');
                style.id='stability79Style';
                style.textContent=`
                  /* Language comes first; both font sliders share one tile. */
                  #settingsSheet #languageBox79{margin-top:9px!important}
                  #settingsSheet #fontCombined79{margin-top:9px!important}
                  #settingsSheet #fontCombined79 #widgetFontTitle{margin-top:9px!important;padding-top:9px!important;border-top:1px solid #edf0f4!important}
                  #settingsSheet #fontCombined79 .settingTitle{text-align:center!important}

                  /* The obsolete explanatory sentence under the course palette is gone. */
                  #settingsSheet #paletteSyncHint{display:none!important}

                  /* Widget preview mirrors the current headerless rectangular list widget. */
                  #settingsSheet .widgetPreviewFrame79{padding:0!important;background:transparent!important;border-radius:0!important;overflow:hidden!important}
                  #settingsSheet .widgetMini79{width:100%!important;background:transparent!important;padding:0!important}
                  #settingsSheet .widgetMiniRow79{height:31px!important;display:flex!important;align-items:center!important;gap:4px!important;padding:0 3px 0 6px!important;box-sizing:border-box!important;border-radius:0!important;overflow:hidden!important}
                  #settingsSheet .widgetMiniText79{flex:1 1 auto!important;min-width:0!important;display:flex!important;flex-direction:column!important;justify-content:center!important;line-height:1.04!important}
                  #settingsSheet .widgetMiniTitle79{font-weight:900!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
                  #settingsSheet .widgetMiniMeta79{margin-top:2px!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;opacity:.94!important}
                  #settingsSheet .widgetMiniPill79{flex:0 0 auto!important;min-width:35px!important;max-width:48px!important;height:20px!important;padding:0 4px!important;display:flex!important;align-items:center!important;justify-content:center!important;border:1px solid #66000000!important;border-radius:10px!important;background:#f7ffffff!important;color:#22283a!important;font-weight:900!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important;box-sizing:border-box!important}

                  /* Tap “Cette semaine” to choose instead of cycling blindly. */
                  #weekPicker79{position:fixed!important;z-index:10050!important;display:none!important;padding:6px!important;border:1px solid #dce3eb!important;border-radius:10px!important;background:#fff!important;box-shadow:0 8px 24px #1020402b!important;grid-template-columns:1fr!important;gap:4px!important;box-sizing:border-box!important}
                  #weekPicker79.show{display:grid!important}
                  #weekPicker79 button{height:34px!important;margin:0!important;padding:0 10px!important;border:1px solid #dce3eb!important;border-radius:8px!important;background:#fff!important;color:var(--ink,#111936)!important;font-size:.75rem!important;font-weight:850!important;text-align:center!important;white-space:nowrap!important}
                  #weekPicker79 button.active{border-color:var(--set-accent,var(--blue))!important;background:var(--set-soft,var(--soft))!important;color:var(--set-dark,var(--blue2))!important}
                `;
                document.head.appendChild(style);

                function arrangeSettings(){
                  const sheet=document.getElementById('settingsSheet'),select=document.getElementById('languageSelect');
                  const appTitle=document.getElementById('appFontTitle'),widgetTitle=document.getElementById('widgetFontTitle');
                  if(!sheet||!select||!appTitle||!widgetTitle)return;
                  const languageBox=select.closest('.settingBox'),appBox=appTitle.closest('.settingBox'),widgetBox=widgetTitle.closest('.settingBox');
                  if(languageBox)languageBox.id='languageBox79';
                  if(appBox)appBox.id='fontCombined79';
                  if(appBox&&widgetBox&&widgetBox!==appBox){
                    const row=widgetBox.querySelector('.settingRow');
                    appBox.appendChild(widgetTitle);
                    if(row)appBox.appendChild(row);
                    widgetBox.remove();
                  }
                  if(languageBox&&appBox&&languageBox.nextElementSibling!==appBox)sheet.insertBefore(languageBox,appBox);
                }

                function removePaletteHint(){const h=document.getElementById('paletteSyncHint');if(h)h.remove()}

                function hexInk(hex){
                  const s=String(hex||'').replace('#','');if(s.length!==6)return '#fff';
                  const n=parseInt(s,16),r=(n>>16)&255,g=(n>>8)&255,b=n&255,l=.2126*r+.7152*g+.0722*b;
                  return l>168?'#17213a':'#ffffff';
                }
                function widgetPreviewColors(){
                  let id='vivid';try{id=String(AndroidSchedule.loadWidgetPalette?AndroidSchedule.loadWidgetPalette():'vivid')}catch(e){}
                  return PALETTES[id]||PALETTES.vivid;
                }
                function updateWidgetPreview(){
                  const label=document.getElementById('widgetPreviewLabel'),outer=label&&label.parentElement?label.parentElement.querySelector('.preview'):null;if(!outer)return;
                  outer.classList.add('widgetPreviewFrame79');
                  let mini=outer.querySelector('.widgetMini79');
                  if(!mini){
                    outer.innerHTML='<div class="widgetMini79"><div class="widgetMiniRow79"><div class="widgetMiniText79"><span class="widgetMiniTitle79">4G1 ALL</span><span class="widgetMiniMeta79">08:00 - 09:00</span></div><span class="widgetMiniPill79">10 h</span></div><div class="widgetMiniRow79"><div class="widgetMiniText79"><span class="widgetMiniTitle79">4G2 ALL</span><span class="widgetMiniMeta79">09:00 - 10:00</span></div><span class="widgetMiniPill79">11 h</span></div></div>';
                    mini=outer.querySelector('.widgetMini79');
                  }
                  const colors=widgetPreviewColors(),scale=Math.max(.8,Math.min(1.4,Number(loadUi().widgetFontScale)||1));
                  mini.querySelectorAll('.widgetMiniRow79').forEach((row,i)=>{const c=colors[i%colors.length],ink=hexInk(c);row.style.background=c;row.style.color=ink;const title=row.querySelector('.widgetMiniTitle79'),meta=row.querySelector('.widgetMiniMeta79'),pill=row.querySelector('.widgetMiniPill79');if(title)title.style.fontSize=(6.7*scale)+'px';if(meta)meta.style.fontSize=(5.4*scale)+'px';if(pill)pill.style.fontSize=(5.8*scale)+'px'});
                }

                function bindPreviewSlider(){
                  const w=document.getElementById('widgetFont');if(w&&!w.__preview79){w.__preview79=true;w.addEventListener('input',()=>requestAnimationFrame(updateWidgetPreview))}
                }

                function persistLanguage(code){
                  code=String(code||'fr').trim()||'fr';const ui=loadUi();ui.language=code;saveUi(ui);
                  if(languageTimer)clearTimeout(languageTimer);
                  languageTimer=setTimeout(()=>{
                    try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){}
                    requestAnimationFrame(()=>{try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){};refresh()});
                  },0);
                }

                async function catalog79(){
                  try{
                    if(window.AndroidSchedule&&AndroidSchedule.downloadLanguageCatalog){const raw=AndroidSchedule.downloadLanguageCatalog();if(raw){const j=JSON.parse(raw);if(j&&Array.isArray(j.languages))return j.languages}}
                  }catch(e){}
                  try{const r=await fetch(CATALOG_URL,{cache:'no-store'});if(r.ok){const j=await r.json();if(j&&Array.isArray(j.languages))return j.languages}}catch(e){}
                  return [{code:'es',name:'Español',url:'https://raw.githubusercontent.com/Wokgui/Emploi-du-temps/main/language-packs/es.json'}];
                }

                function installedCodes(){
                  try{return new Set(JSON.parse(AndroidSchedule.loadLanguagePacks?AndroidSchedule.loadLanguagePacks():'[]').map(x=>x.code))}catch(e){return new Set()}
                }

                async function downloadPack79(item,button,status){
                  button.disabled=true;button.textContent='…';
                  try{
                    let raw='';
                    if(window.AndroidSchedule&&AndroidSchedule.downloadLanguagePack)raw=AndroidSchedule.downloadLanguagePack(item.url)||'';
                    if(!raw){const r=await fetch(item.url,{cache:'no-store'});if(!r.ok)throw new Error('http');raw=await r.text();if(window.AndroidSchedule&&AndroidSchedule.saveLanguagePack&&!AndroidSchedule.saveLanguagePack(raw))throw new Error('native')}
                    const pack=JSON.parse(raw);if(!pack||pack.code!==item.code||!pack.strings)throw new Error('pack');
                    try{localStorage.setItem(PACK_PREFIX+pack.code,raw)}catch(e){}
                    const select=document.getElementById('languageSelect');
                    if(select&&!select.querySelector('option[value="'+pack.code+'"]')){const o=document.createElement('option');o.value=pack.code;o.textContent=pack.name||item.name||pack.code;select.appendChild(o)}
                    if(select){select.value=pack.code;select.dispatchEvent(new Event('change',{bubbles:true}))}
                    button.textContent='Langue téléchargée';button.disabled=true;if(status)status.textContent='Langue téléchargée';
                    try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){}
                  }catch(e){button.disabled=false;button.textContent='Télécharger';if(status)status.textContent='Téléchargement impossible';try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(ex){}}
                }

                async function openLanguagePanel79(){
                  const select=document.getElementById('languageSelect'),box=select&&select.closest('.settingBox');if(!box)return;
                  let panel=document.getElementById('languagePackPanel');if(!panel){panel=document.createElement('div');panel.id='languagePackPanel';box.appendChild(panel)}
                  panel.classList.add('show');panel.innerHTML='<div class="languagePackTitle">Langues additionnelles</div><div class="languagePackStatus">…</div>';
                  const status=panel.querySelector('.languagePackStatus'),list=await catalog79(),installed=installedCodes();if(status)status.textContent='';
                  list.forEach(item=>{const row=document.createElement('div');row.className='languagePackItem';const name=document.createElement('span');name.className='languagePackName';name.textContent=item.name||item.code;const b=document.createElement('button');b.type='button';b.className='languagePackAction';b.textContent=installed.has(item.code)?'Langue téléchargée':'Télécharger';b.disabled=installed.has(item.code);b.onclick=()=>downloadPack79(item,b,status);row.append(name,b);panel.insertBefore(row,status)});
                  try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){}
                }

                function bindLanguage(){
                  const select=document.getElementById('languageSelect');if(!select)return;
                  if(!select.__stability79Language){select.__stability79Language=true;select.addEventListener('change',e=>persistLanguage(e.target.value))}
                  const current=lang();if(select.querySelector('option[value="'+current+'"]')&&select.value!==current)select.value=current;
                  const btn=document.getElementById('languageDownloadBtn');if(btn){btn.onclick=e=>{e.preventDefault();e.stopPropagation();openLanguagePanel79();return false}}
                }

                function ensureWeekPicker(){
                  let p=document.getElementById('weekPicker79');if(!p){p=document.createElement('div');p.id='weekPicker79';document.body.appendChild(p)}return p;
                }
                function positionWeekPicker(){
                  const b=document.getElementById('currentWeekBtn'),p=document.getElementById('weekPicker79');if(!b||!p||!p.classList.contains('show'))return;
                  const r=b.getBoundingClientRect(),w=Math.max(136,Math.min(190,r.width+22));p.style.width=w+'px';p.style.left=Math.max(6,Math.min(window.innerWidth-w-6,r.left))+'px';p.style.top=Math.min(window.innerHeight-8,r.bottom+5)+'px';
                }
                function closeWeekPicker(){const p=document.getElementById('weekPicker79');if(p)p.classList.remove('show')}
                function chooseCurrentWeek(w){
                  if(!allowedWeeks().includes(w))return;closeWeekPicker();
                  try{if(typeof currentWeek!=='undefined')currentWeek=w;if(typeof activeWeek!=='undefined')activeWeek=w}catch(e){}
                  try{if(window.AndroidSchedule&&AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(w)}catch(e){}
                  try{if(typeof render==='function')render()}catch(e){}
                  try{if(window.refreshStability70)window.refreshStability70()}catch(e){}
                  stabilizeLunchText();refresh();try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){}
                }
                function openWeekPicker(){
                  if(weekCount()<=1)return;const p=ensureWeekPicker(),weeks79=allowedWeeks(),cur=typeof currentWeek!=='undefined'?currentWeek:'A';p.innerHTML='';
                  weeks79.forEach(w=>{const b=document.createElement('button');b.type='button';b.textContent='Semaine '+w;b.classList.toggle('active',w===cur);b.onclick=e=>{e.preventDefault();e.stopPropagation();chooseCurrentWeek(w)};p.appendChild(b)});
                  p.classList.add('show');positionWeekPicker();try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){}
                }
                function toggleWeekPicker(){const p=ensureWeekPicker();if(p.classList.contains('show'))closeWeekPicker();else openWeekPicker()}

                document.addEventListener('pointerup',e=>{
                  const b=e.target&&e.target.closest?e.target.closest('#currentWeekBtn'):null;if(!b)return;
                  e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();lastWeekPointer=Date.now();toggleWeekPicker();
                },true);
                document.addEventListener('click',e=>{
                  const b=e.target&&e.target.closest?e.target.closest('#currentWeekBtn'):null;if(!b)return;
                  e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();if(Date.now()-lastWeekPointer<650)return;toggleWeekPicker();
                },true);
                document.addEventListener('pointerdown',e=>{const p=document.getElementById('weekPicker79'),b=e.target&&e.target.closest?e.target.closest('#currentWeekBtn'):null;if(p&&p.classList.contains('show')&&!p.contains(e.target)&&!b)closeWeekPicker()},true);
                window.addEventListener('resize',positionWeekPicker);

                function lunchLabel(){
                  try{if(typeof lunchLabelText==='function')return String(lunchLabelText()||'').trim();if(typeof breaks!=='undefined'&&breaks)return String(breaks.lunchLabel||'Midi').replace(/\u200b/g,'').trim()}catch(e){}
                  const input=document.getElementById('lunchLabel');return input?String(input.value||'').trim():'Midi';
                }
                function stabilizeLunchText(){
                  const label=lunchLabel();
                  document.querySelectorAll('#weekGrid .lunchCell,#weekGrid .dynamicLunchCell,#weekGrid .nativeLunchCell,#weekGrid .finalLunchCell').forEach(cell=>{
                    const native=cell.querySelector(':scope > .nativeLunchLabel');
                    if(native){const spans=[...native.querySelectorAll('span')],text=spans.find(x=>!x.classList.contains('nativeLunchIcon'));if(text&&text.textContent!==label)text.textContent=label}
                    cell.querySelectorAll(':scope > .cellLabel,.dynamicLunchOverlay .cellLabel,.breakFitLabel').forEach(el=>{if(!el.closest('.nativeLunchLabel')&&el.textContent!==label)el.textContent=label});
                  });
                }
                function scheduleLunchStability(){if(lunchRaf)cancelAnimationFrame(lunchRaf);stabilizeLunchText();lunchRaf=requestAnimationFrame(()=>{stabilizeLunchText();requestAnimationFrame(stabilizeLunchText)})}
                window.stabilizeLunchText79=scheduleLunchStability;

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){
                  if(refreshing)return;refreshing=true;
                  try{arrangeSettings();removePaletteHint();bindLanguage();bindPreviewSlider();updateWidgetPreview();scheduleLunchStability();setVersion()}finally{refreshing=false}
                }
                window.refreshStability79=refresh;

                function wrap(name){
                  const old=window[name];if(typeof old!=='function'||old.__stability79)return;
                  const w=function(){const r=old.apply(this,arguments);if(name==='render'||name==='renderWeek')scheduleLunchStability();requestAnimationFrame(refresh);return r};w.__stability79=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['render','renderWeek','refreshSettingsV3','refreshCoursePaletteV4','refreshLocalization75','refreshStability74','refreshStability78'].forEach(wrap);

                refresh();requestAnimationFrame(refresh);
              }catch(e){console.log('Stability79Ui',e)}
            })();
            """;
    }
}
