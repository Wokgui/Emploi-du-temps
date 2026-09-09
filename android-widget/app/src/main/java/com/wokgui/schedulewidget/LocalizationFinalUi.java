package com.wokgui.schedulewidget;

/** Final dynamic localization and language-download bindings. */
final class LocalizationFinalUi {
    private LocalizationFinalUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(64 * 1024);
        out.append(layer0()).append('\n'); // Stability81Ui
        return out.toString();
    }

    // Former Stability81Ui; isolated to stay below JVM constant limits.
    private static String layer0() {
        return """
            (function(){
              try{
                if(window.__stability81V1){if(window.refreshStability81)window.refreshStability81();return}
                window.__stability81V1=true;
                const APP_VERSION='6.31';
                let translating81=false,downloadBusy81=false;

                const EXTRA81={
                  en:{
                    'Application':'Application','Widget':'Widget','Taille du texte':'Text size','Télécharger une langue':'Download a language',
                    'Toutes les langues disponibles sont déjà installées.':'All available languages are already installed.',
                    'Téléchargement du modèle de langue et traduction de toute l’interface…':'Downloading the language model and translating the whole interface…',
                    'Langue téléchargée. Application en cours…':'Language downloaded. Applying…',
                    'La langue a été téléchargée mais n’a pas pu être appliquée.':'The language was downloaded but could not be applied.',
                    'Langue non prise en charge':'Language not supported','Intitulé dans le widget (facultatif)':'Widget label (optional)',
                    'Vide = même intitulé que dans l’application':'Blank = same label as in the app','Badge (facultatif)':'Badge (optional)',
                    'Horaire libre':'Custom time','Aucun cours ce jour.':'No class that day.','Analyse de la photo…':'Analysing photo…',
                    'Lecture du texte et repérage des cases.':'Reading text and locating timetable cells.','Import annulé.':'Import cancelled.',
                    'Aucun texte détecté sur la photo.':'No text detected in the photo.','Je ne reconnais pas assez clairement les colonnes des jours.':'The day columns are not clear enough to recognise.',
                    'Aucun cours exploitable détecté.':'No usable classes detected.','Les horaires ne sont pas assez lisibles.':'The times are not clear enough.',
                    'Le texte a été lu, mais aucun cours n’a pu être converti automatiquement.':'The text was read, but no class could be converted automatically.',
                    'Horaire actuel':'Current time slot','Horaire conservé':'Time kept','Horaire personnalisé':'Custom time','Horaire appliqué':'Applied time',
                    'salle':'room','heure':'period','Télécharger une langue…':'Download a language…','Télécharger':'Download'
                  },
                  de:{
                    'Application':'Anwendung','Widget':'Widget','Taille du texte':'Textgröße','Télécharger une langue':'Sprache herunterladen',
                    'Toutes les langues disponibles sont déjà installées.':'Alle verfügbaren Sprachen sind bereits installiert.',
                    'Téléchargement du modèle de langue et traduction de toute l’interface…':'Sprachmodell wird heruntergeladen und die gesamte Oberfläche übersetzt…',
                    'Langue téléchargée. Application en cours…':'Sprache heruntergeladen. Wird angewendet…',
                    'La langue a été téléchargée mais n’a pas pu être appliquée.':'Die Sprache wurde heruntergeladen, konnte aber nicht angewendet werden.',
                    'Langue non prise en charge':'Sprache nicht unterstützt','Intitulé dans le widget (facultatif)':'Bezeichnung im Widget (optional)',
                    'Vide = même intitulé que dans l’application':'Leer = gleiche Bezeichnung wie in der App','Badge (facultatif)':'Badge (optional)',
                    'Horaire libre':'Freie Uhrzeit','Aucun cours ce jour.':'An diesem Tag kein Unterricht.','Analyse de la photo…':'Foto wird analysiert…',
                    'Lecture du texte et repérage des cases.':'Text wird gelesen und Stundenplanfelder werden erkannt.','Import annulé.':'Import abgebrochen.',
                    'Aucun texte détecté sur la photo.':'Auf dem Foto wurde kein Text erkannt.','Je ne reconnais pas assez clairement les colonnes des jours.':'Die Spalten der Wochentage sind nicht deutlich genug erkennbar.',
                    'Aucun cours exploitable détecté.':'Keine verwertbaren Stunden erkannt.','Les horaires ne sont pas assez lisibles.':'Die Uhrzeiten sind nicht deutlich genug lesbar.',
                    'Le texte a été lu, mais aucun cours n’a pu être converti automatiquement.':'Der Text wurde gelesen, aber keine Stunde konnte automatisch übernommen werden.',
                    'Horaire actuel':'Aktuelle Zeit','Horaire conservé':'Zeit beibehalten','Horaire personnalisé':'Benutzerdefinierte Zeit','Horaire appliqué':'Übernommene Zeit',
                    'salle':'Raum','heure':'Stunde','Télécharger une langue…':'Sprache herunterladen…','Télécharger':'Herunterladen'
                  }
                };

                function loadUi81(){try{return JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){return {language:'fr'}}}
                function language81(){return String(loadUi81().language||'fr')}
                function installed81(){try{return JSON.parse(AndroidSchedule.loadLanguagePacks?AndroidSchedule.loadLanguagePacks():'[]')}catch(e){return []}}
                function loadPack81(code){try{const raw=AndroidSchedule.loadLanguagePack?AndroidSchedule.loadLanguagePack(code):'';return raw?JSON.parse(raw):null}catch(e){return null}}
                function map81(){const l=language81();if(EXTRA81[l])return EXTRA81[l];const p=loadPack81(l);return p&&p.strings?p.strings:{}}
                function text81(fr){if(language81()==='fr')return fr;const m=map81();return m[fr]||fr}

                function periodFr81(n){return n===1?'1ère heure':n+'ème heure'}
                function period81(n){
                  const l=language81();
                  if(l==='fr')return periodFr81(n);
                  if(l==='de')return n+'. Stunde';
                  if(l==='en'){
                    const x=n%100,s=(x>=11&&x<=13)?'th':(n%10===1?'st':(n%10===2?'nd':(n%10===3?'rd':'th')));
                    return n+s+' period';
                  }
                  const m=map81();return m[periodFr81(n)]||periodFr81(n);
                }

                function translateExtraText81(value){
                  const raw=String(value==null?'':value),trim=raw.trim();if(!trim)return raw;
                  const m=map81();if(m[trim])return raw.replace(trim,m[trim]);
                  const lang=language81();
                  let dyn=trim.match(/^(Lundi|Mardi|Mercredi|Jeudi|Vendredi|Samedi|Dimanche|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag|Sonntag) *· *(?:Semaine|Week|Woche) +([A-D])$/i);
                  if(dyn){
                    const canon={Lundi:'Lundi',Mardi:'Mardi',Mercredi:'Mercredi',Jeudi:'Jeudi',Vendredi:'Vendredi',Samedi:'Samedi',Dimanche:'Dimanche',Monday:'Lundi',Tuesday:'Mardi',Wednesday:'Mercredi',Thursday:'Jeudi',Friday:'Vendredi',Saturday:'Samedi',Sunday:'Dimanche',Montag:'Lundi',Dienstag:'Mardi',Mittwoch:'Mercredi',Donnerstag:'Jeudi',Freitag:'Vendredi',Samstag:'Samedi',Sonntag:'Dimanche'};
                    const fr=canon[dyn[1]]||dyn[1];
                    const daysEn={Lundi:'Monday',Mardi:'Tuesday',Mercredi:'Wednesday',Jeudi:'Thursday',Vendredi:'Friday',Samedi:'Saturday',Dimanche:'Sunday'};
                    const daysDe={Lundi:'Montag',Mardi:'Dienstag',Mercredi:'Mittwoch',Jeudi:'Donnerstag',Vendredi:'Freitag',Samedi:'Samstag',Dimanche:'Sonntag'};
                    const day=lang==='en'?daysEn[fr]:(lang==='de'?daysDe[fr]:(lang==='fr'?fr:(m[fr]||fr)));
                    const week=lang==='en'?'Week':(lang==='de'?'Woche':(lang==='fr'?'Semaine':(m['Semaine']||'Semaine')));
                    return raw.replace(trim,day+' · '+week+' '+dyn[2].toUpperCase());
                  }
                  dyn=trim.match(/^([0-9]+ *h(?: *[0-9]+)?|[0-9]+ *min) *sans cours$/i);
                  if(dyn){
                    const tail=lang==='en'?'with no class':(lang==='de'?'ohne Unterricht':(lang==='fr'?'sans cours':(m['sans cours']||'sans cours')));
                    return raw.replace(trim,dyn[1]+' '+tail);
                  }
                  dyn=trim.match(/^Reprise à +(.*)$/i);
                  if(dyn){
                    const lead=lang==='en'?'Back at':(lang==='de'?'Weiter um':(lang==='fr'?'Reprise à':(m['Reprise à']||'Reprise à')));
                    return raw.replace(trim,lead+' '+dyn[1]);
                  }
                  for(let n=1;n<=9;n++){
                    const fr=periodFr81(n),legacy=n+'e h',legacy2=n+'e heure';
                    if(trim===fr||trim===legacy||trim===legacy2)return raw.replace(trim,period81(n));
                  }
                  if(language81()==='en'){
                    if(trim.indexOf('salle ')===0)return raw.replace(trim,'room '+trim.substring(6));
                    if(trim.indexOf('Horaire actuel (')===0)return raw.replace(trim,'Current time slot '+trim.substring(15));
                    if(trim.indexOf('Horaire conservé : ')===0)return raw.replace(trim,'Time kept: '+trim.substring(19));
                    if(trim.indexOf('Horaire appliqué : ')===0)return raw.replace(trim,'Applied time: '+trim.substring(19));
                  }
                  if(language81()==='de'){
                    if(trim.indexOf('salle ')===0)return raw.replace(trim,'Raum '+trim.substring(6));
                    if(trim.indexOf('Horaire actuel (')===0)return raw.replace(trim,'Aktuelle Zeit '+trim.substring(15));
                    if(trim.indexOf('Horaire conservé : ')===0)return raw.replace(trim,'Zeit beibehalten: '+trim.substring(19));
                    if(trim.indexOf('Horaire appliqué : ')===0)return raw.replace(trim,'Übernommene Zeit: '+trim.substring(19));
                  }
                  return raw;
                }

                function translateExtras81(root){
                  if(!root)return;
                  const walker=document.createTreeWalker(root,NodeFilter.SHOW_TEXT);let node;
                  while((node=walker.nextNode())){
                    const p=node.parentElement;if(!p||p.closest('script,style'))continue;
                    const next=translateExtraText81(node.nodeValue);if(next!==node.nodeValue)node.nodeValue=next;
                  }
                  root.querySelectorAll('[title],[aria-label],[placeholder]').forEach(el=>{
                    ['title','aria-label','placeholder'].forEach(a=>{if(!el.hasAttribute(a))return;const v=el.getAttribute(a),n=translateExtraText81(v);if(n!==v)el.setAttribute(a,n)});
                  });
                }

                function fixPeriodLabels81(){
                  document.querySelectorAll('#slotSettings .slotRow').forEach((row,i)=>{const n=row.querySelector('.slotNum');if(n)n.textContent=period81(i+1)});
                  const sel=document.getElementById('fSlot');if(sel){[...sel.options].forEach(o=>{const n=Number(o.value);if(!(n>=1&&n<=9))return;let times='';try{const s=slots[n-1];if(s)times=' · '+s.start+'–'+s.end}catch(e){}o.textContent=period81(n)+times})}
                }

                function applyTranslation81(root){
                  if(translating81)return;translating81=true;
                  try{
                    const scope=root&&root.nodeType===1?root:document.body;
                    if(scope===document.body||scope.id==='viewEdit'||scope.id==='settingsSheet')fixPeriodLabels81();
                    translateExtras81(scope);
                    const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                    document.documentElement.lang=language81().replace('_','-');
                  }finally{translating81=false}
                }

                function ensureLanguageSelect81(){
                  let select=document.getElementById('languageSelect');if(!select)return null;
                  if(!select.__language81Owned){
                    const clone=select.cloneNode(true);clone.__language81Owned=true;clone.__stability80Owned=true;clone.__language77Bound=true;
                    select.replaceWith(clone);select=clone;
                    select.addEventListener('change',e=>{
                      e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();
                      const code=String(e.target.value||'fr');
                      try{
                        const next=loadUi81();next.language=code;AndroidSchedule.saveUiSettings(JSON.stringify(next));
                        try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(ex){}
                        applyTranslation81(document.body);
                      }catch(ex){}
                    },true);
                  }
                  const choices=[['fr','Français'],['de','Deutsch'],['en','English']].concat(installed81().map(x=>[String(x.code||''),String(x.name||x.code||'')]));
                  choices.forEach(([code,name])=>{if(!code)return;let o=[...select.options].find(x=>x.value===code);if(!o){o=document.createElement('option');o.value=code;select.appendChild(o)}o.textContent=name||code});
                  select.value=language81();return select;
                }

                function supported81(){try{const a=JSON.parse(AndroidSchedule.supportedTranslationLanguages?AndroidSchedule.supportedTranslationLanguages():'[]');return Array.isArray(a)?a:[]}catch(e){return []}}
                function ensureDownloadUi81(){
                  const select=ensureLanguageSelect81(),box=select&&select.closest('.settingBox');if(!box)return;
                  const old=document.getElementById('languageDownloadBtn');if(old)old.style.display='none';
                  let button=document.getElementById('languageDownloadBtn81');
                  if(!button){button=document.createElement('button');button.id='languageDownloadBtn81';button.type='button';button.className='languagePackAction';button.style.width='100%';button.style.marginTop='8px';box.appendChild(button)}
                  button.textContent=text81('Télécharger une langue…');button.onclick=e=>{e.preventDefault();e.stopPropagation();openLanguages81()};
                  let panel=document.getElementById('languagePackPanel81');if(!panel){panel=document.createElement('div');panel.id='languagePackPanel81';panel.className='languagePackPanel81';box.appendChild(panel)}
                }

                function openLanguages81(){
                  ensureDownloadUi81();const panel=document.getElementById('languagePackPanel81');if(!panel)return;
                  const installedCodes=new Set(['fr','de','en'].concat(installed81().map(x=>String(x.code||''))));
                  const all=supported81().filter(x=>x&&x.code&&!installedCodes.has(String(x.code))).sort((a,b)=>String(a.name||a.code).localeCompare(String(b.name||b.code),'fr'));
                  panel.classList.add('show');panel.innerHTML='';
                  const title=document.createElement('div');title.className='languagePackTitle';title.textContent=text81('Télécharger une langue');panel.appendChild(title);
                  const row=document.createElement('div');row.className='languageAny81';panel.appendChild(row);
                  const picker=document.createElement('select');picker.id='languageCatalog81';row.appendChild(picker);
                  all.forEach(x=>{const o=document.createElement('option');o.value=String(x.code);o.textContent=String(x.name||x.code);picker.appendChild(o)});
                  const dl=document.createElement('button');dl.id='languageGenerate81';dl.type='button';dl.textContent=text81('Télécharger');row.appendChild(dl);
                  const status=document.createElement('div');status.className='languagePackStatus';panel.appendChild(status);
                  if(!all.length){dl.disabled=true;status.textContent=text81('Toutes les langues disponibles sont déjà installées.')}
                  dl.onclick=e=>{
                    e.preventDefault();e.stopPropagation();if(downloadBusy81||!picker.value)return;
                    downloadBusy81=true;dl.disabled=true;status.textContent=text81('Téléchargement du modèle de langue et traduction de toute l’interface…');
                    const item=all.find(x=>String(x.code)===picker.value)||{code:picker.value,name:picker.options[picker.selectedIndex]?picker.options[picker.selectedIndex].textContent:picker.value};
                    try{AndroidSchedule.generateLanguagePack(String(item.code),String(item.name||item.code))}catch(ex){downloadBusy81=false;dl.disabled=false;status.textContent=text81('Téléchargement impossible')}
                  };
                }

                window.onGeneratedLanguagePack80=function(raw){
                  downloadBusy81=false;
                  try{
                    const pack=typeof raw==='string'?JSON.parse(raw):raw;const panel=document.getElementById('languagePackPanel81'),status=panel&&panel.querySelector('.languagePackStatus');
                    if(status)status.textContent=text81('Langue téléchargée. Application en cours…');
                    if(pack&&pack.code){
                      try{
                        const next=loadUi81();next.language=String(pack.code);AndroidSchedule.saveUiSettings(JSON.stringify(next));
                        try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){}
                        applyTranslation81(document.body);ensureLanguageSelect81();
                        return;
                      }catch(e){}
                    }
                    if(status)status.textContent=text81('La langue a été téléchargée mais n’a pas pu être appliquée.');
                  }catch(e){const status=document.querySelector('#languagePackPanel81 .languagePackStatus');if(status)status.textContent=text81('La langue a été téléchargée mais n’a pas pu être appliquée.')}
                };
                window.onGeneratedLanguagePackError80=function(message){downloadBusy81=false;const b=document.getElementById('languageGenerate81');if(b)b.disabled=false;const s=document.querySelector('#languagePackPanel81 .languagePackStatus');if(s)s.textContent=String(message||text81('Téléchargement impossible'))};

                const style=document.createElement('style');style.id='stability81Style';style.textContent=`
                  button,.nav,.weekTab,.dayTab,select,input,label,[role="button"]{touch-action:manipulation!important;-webkit-tap-highlight-color:transparent!important}
                  button,.nav,.weekTab,.dayTab{transition:none!important}
                  button:active,.nav:active,.weekTab:active,.dayTab:active,.weekModeChoice:active,.advButton:active,.settingsAction:active{opacity:.72!important}
                  #slotSettings .slotRow{grid-template-columns:92px 1fr 1fr!important}
                  #slotSettings .slotNum{white-space:nowrap!important;text-align:left!important;font-size:.70rem!important}
                  #languageDownloadBtn{display:none!important}
                  #languagePackPanel81{display:none;margin-top:8px;padding:8px;border-top:1px solid var(--line,#dce3eb)}
                  #languagePackPanel81.show{display:block!important}
                  #languagePackPanel81 .languagePackTitle{text-align:center;font-size:.74rem;font-weight:850;margin-bottom:7px}
                  #languagePackPanel81 .languageAny81{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:7px;align-items:center}
                  #languagePackPanel81 select{width:100%;min-width:0;padding:9px;border:1px solid var(--line,#dce3eb);border-radius:8px;background:#fff;color:var(--ink,#111936);font-size:.74rem}
                  #languagePackPanel81 button,#languageDownloadBtn81{padding:9px 11px;border:1px solid var(--set-accent,var(--blue));border-radius:8px;background:var(--set-accent,var(--blue));color:#fff;font-size:.72rem;font-weight:850}
                  #languagePackPanel81 button:disabled{opacity:.55}
                  #languagePackPanel81 .languagePackStatus{margin-top:7px;min-height:1.2em;text-align:center;font-size:.68rem;color:var(--muted,#68738a)}
                  @media(max-width:390px){#slotSettings .slotRow{grid-template-columns:82px 1fr 1fr!important}#slotSettings .slotNum{font-size:.64rem!important}}
                `;document.head.appendChild(style);

                function root81(name){
                  if(name==='renderToday')return document.getElementById('viewToday');
                  if(name==='renderWeek'||name==='setActiveWeek')return document.getElementById('viewWeek');
                  if(name==='renderEdit'||name==='renderSlots'||name==='fillSlotOptions')return document.getElementById('viewEdit');
                  if(name==='renderContext')return document.querySelector('.contextBar');
                  if(name==='openEditor')return document.getElementById('modal');
                  return document.querySelector('.view.active')||document.body;
                }
                function wrap81(name){
                  const old=window[name];if(typeof old!=='function'||old.__language81)return;
                  const w=function(){const r=old.apply(this,arguments);applyTranslation81(root81(name));return r};w.__language81=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['renderToday','renderWeek','renderEdit','renderContext','renderSlots','fillSlotOptions','openEditor','setActiveWeek'].forEach(wrap81);
                const bottom=document.querySelector('.bottom');if(bottom)bottom.__language81=true;

                function refresh81(){ensureLanguageSelect81();ensureDownloadUi81();fixPeriodLabels81();applyTranslation81()}
                window.refreshStability81=refresh81;
                refresh81();
              }catch(e){console.log('Stability81Ui',e)}
            })();
            """;
    }

}
