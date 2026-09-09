package com.wokgui.schedulewidget;

final class Stability80Ui {
    private Stability80Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability80V1){if(window.refreshStability80)window.refreshStability80();return}
                window.__stability80V1=true;
                const APP_VERSION='6.26';
                let settingsRaf=0,lunchRaf=0,downloadBusy=false;

                function loadUi(){try{return JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){return {language:'fr'}}}
                function saveUi(o){try{AndroidSchedule.saveUiSettings(JSON.stringify(o));return true}catch(e){return false}}
                function currentLanguage(){return String(loadUi().language||'fr')}
                function installed(){try{return JSON.parse(AndroidSchedule.loadLanguagePacks?AndroidSchedule.loadLanguagePacks():'[]')}catch(e){return []}}

                const style=document.createElement('style');
                style.id='stability80Style';
                style.textContent=`
                  /* The current-week menu is always centred on the screen, independently of the badge width. */
                  html body #weekPicker79{
                    left:50%!important;
                    right:auto!important;
                    transform:translateX(-50%)!important;
                    min-width:156px!important;
                    max-width:min(260px,calc(100vw - 24px))!important
                  }
                  #languagePackPanel.show{display:block!important}
                  #languagePackPanel .languageAny80{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:7px;align-items:center;margin-top:8px}
                  #languagePackPanel #languageCatalog80{width:100%;min-width:0;padding:9px;border:1px solid var(--line,#dce3eb);border-radius:8px;background:#fff;color:var(--ink,#111936);font-size:.74rem}
                  #languagePackPanel #languageGenerate80{padding:9px 11px;border:1px solid var(--set-accent,var(--blue));border-radius:8px;background:var(--set-accent,var(--blue));color:#fff;font-size:.72rem;font-weight:850;white-space:nowrap}
                  #languagePackPanel #languageGenerate80:disabled{opacity:.55}
                  #languagePackPanel .languagePackStatus{margin-top:7px;min-height:1.2em;text-align:center;font-size:.68rem;color:var(--muted,#68738a)}
                `;
                document.head.appendChild(style);

                function reloadForLanguage(){
                  try{sessionStorage.setItem('edt-language-reload-mode',typeof mode==='string'?mode:'edit')}catch(e){}
                  try{if(AndroidSchedule.reloadForLanguage){AndroidSchedule.reloadForLanguage();return}}catch(e){}
                  document.documentElement.style.visibility='hidden';location.reload();
                }

                function chooseLanguage(code){
                  code=String(code||'fr').trim()||'fr';
                  const ui=loadUi();
                  if(ui.language===code){try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){};return}
                  ui.language=code;
                  if(saveUi(ui))reloadForLanguage();
                }

                function ensureInstalledOptions(select){
                  if(!select)return;
                  const defs=[['fr','Français'],['de','Deutsch'],['en','English']];
                  defs.concat(installed().map(x=>[String(x.code||''),String(x.name||x.code||'')])).forEach(([code,name])=>{
                    if(!code)return;
                    let o=select.querySelector('option[value="'+code.replace(/"/g,'')+'"]');
                    if(!o){o=document.createElement('option');o.value=code;select.appendChild(o)}
                    o.textContent=name||code;
                  });
                }

                function ownLanguageSelect(){
                  let select=document.getElementById('languageSelect');if(!select)return null;
                  if(!select.__stability80Owned){
                    const clone=select.cloneNode(true);clone.__stability80Owned=true;
                    select.replaceWith(clone);select=clone;
                    select.addEventListener('change',e=>{
                      e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();
                      chooseLanguage(e.target.value);
                    },true);
                  }
                  ensureInstalledOptions(select);
                  const cur=currentLanguage();if(select.querySelector('option[value="'+cur.replace(/"/g,'')+'"]'))select.value=cur;
                  return select;
                }

                function supportedLanguages(){
                  try{const raw=AndroidSchedule.supportedTranslationLanguages?AndroidSchedule.supportedTranslationLanguages():'[]';const a=JSON.parse(raw||'[]');return Array.isArray(a)?a:[]}catch(e){return []}
                }

                function openAnyLanguagePanel(){
                  const select=ownLanguageSelect(),box=select&&select.closest('.settingBox');if(!box)return;
                  let panel=document.getElementById('languagePackPanel');if(!panel){panel=document.createElement('div');panel.id='languagePackPanel';box.appendChild(panel)}
                  panel.classList.add('show');
                  const already=new Set(['fr','de','en',...installed().map(x=>String(x.code||''))]);
                  const all=supportedLanguages().filter(x=>x&&x.code&&!already.has(String(x.code)));
                  panel.innerHTML='<div class="languagePackTitle">Télécharger une langue</div><div class="languageAny80"><select id="languageCatalog80"></select><button id="languageGenerate80" type="button">Télécharger</button></div><div class="languagePackStatus"></div>';
                  const picker=panel.querySelector('#languageCatalog80'),button=panel.querySelector('#languageGenerate80'),status=panel.querySelector('.languagePackStatus');
                  all.forEach(x=>{const o=document.createElement('option');o.value=String(x.code);o.textContent=String(x.name||x.code);picker.appendChild(o)});
                  if(!all.length){button.disabled=true;status.textContent='Toutes les langues disponibles sont déjà installées.'}
                  button.onclick=e=>{
                    e.preventDefault();e.stopPropagation();if(downloadBusy||!picker.value)return;
                    downloadBusy=true;button.disabled=true;status.textContent='Téléchargement du modèle de langue et traduction de toute l’interface…';
                    const item=all.find(x=>String(x.code)===picker.value)||{code:picker.value,name:picker.options[picker.selectedIndex]?.textContent||picker.value};
                    try{AndroidSchedule.generateLanguagePack(String(item.code),String(item.name||item.code))}
                    catch(ex){downloadBusy=false;button.disabled=false;status.textContent='Téléchargement impossible.'}
                  };
                }

                window.onGeneratedLanguagePack80=function(raw){
                  downloadBusy=false;
                  try{
                    const pack=typeof raw==='string'?JSON.parse(raw):raw;
                    const select=ownLanguageSelect();ensureInstalledOptions(select);
                    const status=document.querySelector('#languagePackPanel .languagePackStatus');if(status)status.textContent='Langue téléchargée. Application en cours…';
                    if(select&&pack&&pack.code){if(!select.querySelector('option[value="'+pack.code+'"]')){const o=document.createElement('option');o.value=pack.code;o.textContent=pack.name||pack.code;select.appendChild(o)}select.value=pack.code}
                    chooseLanguage(pack.code);
                  }catch(e){const status=document.querySelector('#languagePackPanel .languagePackStatus');if(status)status.textContent='La langue a été téléchargée mais n’a pas pu être appliquée.'}
                };
                window.onGeneratedLanguagePackError80=function(message){
                  downloadBusy=false;const button=document.getElementById('languageGenerate80');if(button)button.disabled=false;
                  const status=document.querySelector('#languagePackPanel .languagePackStatus');if(status)status.textContent=String(message||'Téléchargement impossible.');
                };

                function bindLanguageButton(){
                  const b=document.getElementById('languageDownloadBtn');if(!b)return;
                  b.textContent='Télécharger une langue…';
                  b.onclick=e=>{e.preventDefault();e.stopPropagation();openAnyLanguagePanel();return false};
                }

                function capitalizeFrenchWeek(){
                  if(currentLanguage()!=='fr')return;
                  ['todayTitle','editDayTitle','modalTitle'].forEach(id=>{
                    const el=document.getElementById(id);if(!el)return;
                    const v=String(el.textContent||'');
                    const n=v.replace(/\bsemaine\b/g,'Semaine');if(n!==v)el.textContent=n;
                  });
                }

                function wrapCap(name){
                  const old=window[name];if(typeof old!=='function'||old.__cap80)return;
                  const w=function(){const r=old.apply(this,arguments);capitalizeFrenchWeek();return r};w.__cap80=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }

                function lunchText80(){
                  try{const a=document.getElementById('lunchLabel');if(a&&String(a.value||'').replace(/\u200b/g,'').trim())return String(a.value).replace(/\u200b/g,'').trim()}catch(e){}
                  try{if(typeof breaks!=='undefined'&&breaks)return String(breaks.lunchLabel||'Midi').replace(/\u200b/g,'').trim()||'Midi'}catch(e){}
                  return 'Midi';
                }
                function fixLunchText(){
                  const label=lunchText80();
                  document.querySelectorAll('#weekGrid .lunchCell,#weekGrid .dynamicLunchCell,#weekGrid .nativeLunchCell,#weekGrid .finalLunchCell').forEach(cell=>{
                    const holder=cell.querySelector(':scope > .nativeLunchLabel');
                    if(holder){
                      let text=[...holder.querySelectorAll('span')].find(x=>!x.classList.contains('nativeLunchIcon'));
                      if(!text){text=document.createElement('span');holder.appendChild(text)}
                      if(text.textContent!==label)text.textContent=label;
                    }
                    cell.querySelectorAll(':scope > .cellLabel,.dynamicLunchOverlay .cellLabel,.breakFitLabel').forEach(el=>{if(!el.closest('.nativeLunchLabel')&&el.textContent!==label)el.textContent=label});
                  });
                }
                function queueLunch(){if(lunchRaf)cancelAnimationFrame(lunchRaf);fixLunchText();lunchRaf=requestAnimationFrame(fixLunchText)}
                function wrapLunch(name){const old=window[name];if(typeof old!=='function'||old.__lunch80)return;const w=function(){const r=old.apply(this,arguments);fixLunchText();return r};w.__lunch80=true;window[name]=w;try{eval(name+'=w')}catch(e){}}

                function restoreMode(){
                  let m='';try{m=sessionStorage.getItem('edt-language-reload-mode')||'';sessionStorage.removeItem('edt-language-reload-mode')}catch(e){}
                  if(['today','week','edit'].includes(m)){try{if(typeof setModeFromAndroid==='function')setModeFromAndroid(m)}catch(e){}}
                }

                function bindObservers(){
                  const sheet=document.getElementById('settingsSheet');if(sheet&&!sheet.__language80Observer){sheet.__language80Observer=true;new MutationObserver(()=>{if(settingsRaf)cancelAnimationFrame(settingsRaf);settingsRaf=requestAnimationFrame(()=>{ownLanguageSelect();bindLanguageButton()})}).observe(sheet,{childList:true,subtree:true})}
                  const grid=document.getElementById('weekGrid');if(grid&&!grid.__lunch80Observer){grid.__lunch80Observer=true;new MutationObserver(queueLunch).observe(grid,{childList:true,subtree:false})}
                }

                function refresh(){
                  ownLanguageSelect();bindLanguageButton();capitalizeFrenchWeek();fixLunchText();bindObservers();
                  ['renderToday','renderEdit','openEditor'].forEach(wrapCap);
                  ['renderWeek','setActiveWeek'].forEach(wrapLunch);
                  const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                }
                window.refreshStability80=refresh;
                refresh();restoreMode();refresh();
              }catch(e){console.log('Stability80Ui',e)}
            })();
            """;
    }
}
