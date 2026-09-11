package com.wokgui.schedulewidget;

/** Application localization and downloaded-language support. */
final class LocalizationUi {
    private LocalizationUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(102 * 1024);
        out.append(layer0()).append('\n'); // Localization75Ui
        out.append(layer1()).append('\n'); // LayoutLanguage77Ui
        out.append(layer2()).append('\n'); // Stability78Ui
        out.append(layer3()).append('\n'); // Stability79Ui
        out.append(layer4()).append('\n'); // Stability80Ui
        return out.toString();
    }

    // Former Localization75Ui; isolated to stay below JVM constant limits.
    private static String layer0() {
        return """
            (function(){
              try{
                if(window.__localization75V1){if(window.refreshLocalization75)window.refreshLocalization75();return}
                window.__localization75V1=true;
                const APP_VERSION='6.31';
                const PACK_PREFIX='edt-language-pack-v1-';
                const CATALOG_URL='https://raw.githubusercontent.com/Wokgui/Emploi-du-temps/main/language-packs/catalog.json';
                let translating=false,queued=false;let langCache=null,installedCache=null,targetCacheLang=null,targetCache=null,reverseCache=null;const pendingRoots=new Set();let localizationObserver=null;

                const BUILTIN={
                  en:{
                    'Emploi du temps':'Timetable','Réglages':'Settings','Taille du texte de l’application':'App text size','Taille du texte du widget':'Widget text size','Langue':'Language','Thème':'Theme','Aperçu application':'App preview','Aperçu de l’application':'App preview','Aperçu widget':'Widget preview','Aperçu du widget':'Widget preview','Réinitialiser':'Reset','Fermer':'Close','Cette semaine':'This week','Semaine':'Week','Semaine unique':'Single week','Aujourd’hui':'Today','Modifier':'Edit','Avancement':'Progress','Importer une photo d’emploi du temps':'Import a timetable photo','Noms des interruptions':'Break names','Horaires des 7 heures':'7 period times','Horaires des 9 heures':'9 period times','Ajouter un cours':'Add a class','Ajouter plusieurs cours à une classe':'Add several classes to a group','Trou':'Free period','Trous':'Free periods','Pause':'Break','Pause de midi':'Lunch break','Midi':'Lunch','Badge':'Badge','Aperçu semaine':'Week preview','Heure de cours':'Class period','Classe / groupe':'Class / group','Salle':'Room','Supprimer':'Delete','Annuler':'Cancel','Enregistrer':'Save','En cours':'In class','cours':'classes','Aucun cours aujourd’hui.':'No class today.','Affichage du widget':'Widget display','Densité':'Density','Format':'Format','Cours suivants':'Following classes','Automatique':'Automatic','Compact':'Compact','Normale':'Normal','Normal':'Normal','Confortable':'Comfortable','Chronologie de la journée':'Day timeline','Maintenant + prochain':'Now + next','Horaires':'Times','Temps restant':'Time left','Pourcentage':'Percentage','Barre de progression':'Progress bar','Semaine et cycle':'Week and cycle','Couleur par classe':'Color by class','Accessibilité':'Accessibility','Contraste élevé':'High contrast','Palette daltonisme':'Color-blind palette','Cycle de semaines':'Week cycle','2 semaines (A/B)':'2 weeks (A/B)','3 semaines (A/B/C)':'3 weeks (A/B/C)','4 semaines (A/B/C/D)':'4 weeks (A/B/C/D)','Copier la semaine active vers la suivante':'Copy active week to the next one','Copier un jour':'Copy a day','De':'From','Vers':'To','Copier':'Copy','Rappels':'Reminders','Notifier avant le cours':'Notify before class','Vacances et jours sans cours':'Holidays and days off','Jours fériés automatiques':'Automatic public holidays','Désactivés':'Off','France':'France','Alsace-Moselle':'Alsace-Moselle','Ajouter une période sans cours':'Add a period with no classes','Libellé':'Label','Ajouter':'Add','Modifications exceptionnelles':'One-off changes','Ajouter une exception':'Add an exception','Aucune exception':'No exception','Cours annulé':'Cancelled class','Changement de salle':'Room change','Cours déplacé':'Moved class','Cours / réunion exceptionnel':'Extra class / meeting','Date':'Date','Heure du cours concerné':'Original class start','Classe / libellé du cours':'Class / original label','Nouvelle heure de début':'New start','Nouvelle heure de fin':'New end','Nouvelle salle':'New room','Nouveau libellé':'New label','Profils':'Profiles','Nouveau profil':'New profile','Renommer':'Rename','Sauvegarde':'Backup','Partager la sauvegarde':'Share backup','Restaurer une sauvegarde':'Restore backup','Créer vide ?':'Create blank?','Jour sans cours':'No class','À vérifier':'Check','Palette de l’application et du widget':'App & widget palette','Palette de l’application':'App palette','Palette du widget':'Widget palette','Utiliser la même palette dans l’application et le widget':'Use the same palette in the app and widget','Midi et trous':'Lunch and free periods','Afficher Midi':'Show lunch','Afficher les trous':'Show free periods','Couleurs complètes':'Full colours','Couleurs séparées du widget':'Separate widget colours','Nuancier complet':'Full colour picker','Plus sombre':'Darker','Plus clair':'Lighter','Vacances scolaires':'School holidays','Intégrer automatiquement les vacances':'Automatically include school holidays','Zone A':'Zone A','Zone B':'Zone B','Zone C':'Zone C','Éclat':'Vivid','Pastel':'Pastel','Chaud':'Warm','Froid':'Cool','Sobre':'Soft','Bleu':'Blue','Turquoise':'Teal','Violet':'Violet','Vert':'Green','Ambre':'Amber','Rose':'Pink','Rouge':'Red','Indigo':'Indigo','Cyan':'Cyan','Corail':'Coral','Bleu nuit':'Navy','Graphite':'Graphite','Lun':'Mon','Mar':'Tue','Mer':'Wed','Jeu':'Thu','Ven':'Fri','Sam':'Sat','Dim':'Sun','Lundi':'Monday','Mardi':'Tuesday','Mercredi':'Wednesday','Jeudi':'Thursday','Vendredi':'Friday','Samedi':'Saturday','Dimanche':'Sunday','1 seule':'1 week','Télécharger une langue…':'Download a language…','Télécharger':'Download','Langues additionnelles':'Additional languages','Langue téléchargée':'Language downloaded','Téléchargement impossible':'Download failed','Widget':'Widget'
                  },
                  de:{
                    'Emploi du temps':'Stundenplan','Réglages':'Einstellungen','Taille du texte de l’application':'Textgröße der App','Taille du texte du widget':'Textgröße des Widgets','Langue':'Sprache','Thème':'Design','Aperçu application':'App-Vorschau','Aperçu de l’application':'App-Vorschau','Aperçu widget':'Widget-Vorschau','Aperçu du widget':'Widget-Vorschau','Réinitialiser':'Zurücksetzen','Fermer':'Schließen','Cette semaine':'Diese Woche','Semaine':'Woche','Semaine unique':'Einzelwoche','Aujourd’hui':'Heute','Modifier':'Bearbeiten','Avancement':'Fortschritt','Importer une photo d’emploi du temps':'Stundenplan-Foto importieren','Noms des interruptions':'Bezeichnungen der Unterbrechungen','Horaires des 7 heures':'Zeiten der 7 Stunden','Horaires des 9 heures':'Zeiten der 9 Stunden','Ajouter un cours':'Stunde hinzufügen','Ajouter plusieurs cours à une classe':'Mehrere Stunden zu einer Klasse hinzufügen','Trou':'Freistunde','Trous':'Freistunden','Pause':'Pause','Pause de midi':'Mittagspause','Midi':'Mittag','Badge':'Badge','Aperçu semaine':'Wochenübersicht','Heure de cours':'Unterrichtsstunde','Classe / groupe':'Klasse / Gruppe','Salle':'Raum','Supprimer':'Löschen','Annuler':'Abbrechen','Enregistrer':'Speichern','En cours':'Läuft','cours':'Stunden','Aucun cours aujourd’hui.':'Heute kein Unterricht.','Affichage du widget':'Widget-Anzeige','Densité':'Dichte','Format':'Format','Cours suivants':'Folgende Stunden','Automatique':'Automatisch','Compact':'Kompakt','Normale':'Normal','Normal':'Normal','Confortable':'Komfortabel','Chronologie de la journée':'Tagesverlauf','Maintenant + prochain':'Jetzt + nächste Stunde','Horaires':'Zeiten','Temps restant':'Restzeit','Pourcentage':'Prozent','Barre de progression':'Fortschrittsbalken','Semaine et cycle':'Woche und Zyklus','Couleur par classe':'Farbe je Klasse','Accessibilité':'Barrierefreiheit','Contraste élevé':'Hoher Kontrast','Palette daltonisme':'Farbenblind-Palette','Cycle de semaines':'Wochenzyklus','2 semaines (A/B)':'2 Wochen (A/B)','3 semaines (A/B/C)':'3 Wochen (A/B/C)','4 semaines (A/B/C/D)':'4 Wochen (A/B/C/D)','Copier la semaine active vers la suivante':'Aktive Woche in die nächste kopieren','Copier un jour':'Tag kopieren','De':'Von','Vers':'Nach','Copier':'Kopieren','Rappels':'Erinnerungen','Notifier avant le cours':'Vor dem Unterricht erinnern','Vacances et jours sans cours':'Ferien und unterrichtsfreie Tage','Jours fériés automatiques':'Automatische Feiertage','Désactivés':'Aus','France':'Frankreich','Alsace-Moselle':'Elsass-Mosel','Ajouter une période sans cours':'Unterrichtsfreie Zeit hinzufügen','Libellé':'Bezeichnung','Ajouter':'Hinzufügen','Modifications exceptionnelles':'Einmalige Änderungen','Ajouter une exception':'Ausnahme hinzufügen','Aucune exception':'Keine Ausnahme','Cours annulé':'Stunde fällt aus','Changement de salle':'Raumänderung','Cours déplacé':'Stunde verlegt','Cours / réunion exceptionnel':'Zusätzliche Stunde / Besprechung','Date':'Datum','Heure du cours concerné':'Beginn der betroffenen Stunde','Classe / libellé du cours':'Klasse / ursprüngliche Bezeichnung','Nouvelle heure de début':'Neuer Beginn','Nouvelle heure de fin':'Neues Ende','Nouvelle salle':'Neuer Raum','Nouveau libellé':'Neue Bezeichnung','Profils':'Profile','Nouveau profil':'Neues Profil','Renommer':'Umbenennen','Sauvegarde':'Sicherung','Partager la sauvegarde':'Sicherung teilen','Restaurer une sauvegarde':'Sicherung wiederherstellen','Créer vide ?':'Leer erstellen?','Jour sans cours':'Unterrichtsfrei','À vérifier':'Prüfen','Palette de l’application et du widget':'App- und Widget-Farben','Palette de l’application':'App-Farben','Palette du widget':'Widget-Farben','Utiliser la même palette dans l’application et le widget':'Gleiche Palette in App und Widget verwenden','Midi et trous':'Mittag und Freistunden','Afficher Midi':'Mittag anzeigen','Afficher les trous':'Freistunden anzeigen','Couleurs complètes':'Vollständige Farben','Couleurs séparées du widget':'Separate Widget-Farben','Nuancier complet':'Vollständiger Farbwähler','Plus sombre':'Dunkler','Plus clair':'Heller','Vacances scolaires':'Schulferien','Intégrer automatiquement les vacances':'Schulferien automatisch übernehmen','Zone A':'Zone A','Zone B':'Zone B','Zone C':'Zone C','Éclat':'Kräftig','Pastel':'Pastell','Chaud':'Warm','Froid':'Kühl','Sobre':'Dezent','Bleu':'Blau','Turquoise':'Türkis','Violet':'Violett','Vert':'Grün','Ambre':'Amber','Rose':'Rosa','Rouge':'Rot','Indigo':'Indigo','Cyan':'Cyan','Corail':'Koralle','Bleu nuit':'Dunkelblau','Graphite':'Graphit','Lun':'Mo','Mar':'Di','Mer':'Mi','Jeu':'Do','Ven':'Fr','Sam':'Sa','Dim':'So','Lundi':'Montag','Mardi':'Dienstag','Mercredi':'Mittwoch','Jeudi':'Donnerstag','Vendredi':'Freitag','Samedi':'Samstag','Dimanche':'Sonntag','1 seule':'1 Woche','Télécharger une langue…':'Sprache herunterladen…','Télécharger':'Herunterladen','Langues additionnelles':'Zusätzliche Sprachen','Langue téléchargée':'Sprache heruntergeladen','Téléchargement impossible':'Download fehlgeschlagen','Widget':'Widget'
                  }
                };
                const MONTHS={
                  fr:['janvier','février','mars','avril','mai','juin','juillet','août','septembre','octobre','novembre','décembre'],
                  en:['January','February','March','April','May','June','July','August','September','October','November','December'],
                  de:['Januar','Februar','März','April','Mai','Juni','Juli','August','September','Oktober','November','Dezember']
                };

                function nativeUi(){try{return JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){return {language:'fr'}}}
                function invalidateLocalizationCache(){langCache=null;installedCache=null;targetCacheLang=null;targetCache=null;reverseCache=null}\n                function currentLang(){if(langCache)return langCache;const l=String(nativeUi().language||'fr');langCache=l||'fr';return langCache}
                function installedList(){if(installedCache)return installedCache;try{installedCache=JSON.parse(AndroidSchedule.loadLanguagePacks?AndroidSchedule.loadLanguagePacks():'[]');return installedCache}catch(e){installedCache=[];return installedCache}}
                function loadPack(code){
                  try{const cached=localStorage.getItem(PACK_PREFIX+code);if(cached)return JSON.parse(cached)}catch(e){}
                  try{if(AndroidSchedule.loadLanguagePack){const raw=AndroidSchedule.loadLanguagePack(code);if(raw){localStorage.setItem(PACK_PREFIX+code,raw);return JSON.parse(raw)}}}catch(e){}
                  return null;
                }
                function targetStrings(){
                  const l=currentLang();if(targetCache&&targetCacheLang===l)return targetCache;
                  targetCacheLang=l;
                  if(l==='fr'){targetCache={};return targetCache}
                  if(BUILTIN[l]){targetCache=BUILTIN[l];return targetCache}
                  const p=loadPack(l);targetCache=p&&p.strings?p.strings:{};return targetCache;
                }
                function reverseMap(){
                  if(reverseCache)return reverseCache;
                  const r={};
                  const add=(canonical,value)=>{if(value!=null&&String(value).trim())r[String(value).trim()]=canonical};
                  const keys=new Set();Object.values(BUILTIN).forEach(m=>Object.keys(m).forEach(k=>keys.add(k)));
                  keys.forEach(k=>add(k,k));
                  Object.values(BUILTIN).forEach(m=>Object.entries(m).forEach(([k,v])=>add(k,v)));
                  installedList().forEach(x=>{const p=loadPack(x.code);if(p&&p.strings)Object.entries(p.strings).forEach(([k,v])=>{add(k,k);add(k,v)})});
                  reverseCache=r;return reverseCache;
                }
                function tExact(text){
                  const s=String(text||'').trim();if(!s)return text;
                  const rev=reverseMap(),canonical=rev[s]||s,target=targetStrings();
                  if(currentLang()==='fr')return canonical;
                  return target[canonical]||s;
                }
                function weekWord(){return tExact('Semaine')}
                function dayWord(fr){return tExact(fr)}
                function translateDynamic(s){
                  let out=String(s||'');const trim=out.trim();if(!trim)return out;
                  const exact=tExact(trim);if(exact!==trim)return out.replace(trim,exact);
                  let m=trim.match(/^Cette semaine *: *([A-D])$/i);if(m)return out.replace(trim,tExact('Cette semaine')+' : '+m[1].toUpperCase());
                  m=trim.match(/^(?:Semaine|Week|Woche) +([A-D])$/i);if(m)return out.replace(trim,weekWord()+' '+m[1].toUpperCase());
                  m=trim.match(/^Aperçu semaine *([A-D])?$/i);if(m)return out.replace(trim,tExact('Aperçu semaine')+(m[1]?' '+m[1].toUpperCase():''));
                  m=trim.match(/^(Lundi|Mardi|Mercredi|Jeudi|Vendredi|Samedi|Dimanche|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag|Sonntag) *· *(?:Semaine|Week|Woche) +([A-D])$/i);
                  if(m){const d=tExact(m[1]);return out.replace(trim,d+' · '+weekWord()+' '+m[2].toUpperCase())}
                  m=trim.match(/^([0-9]+) +cours$/i);if(m){const l=currentLang();const word=l==='de'?'Stunden':(l==='en'?'classes':(l==='fr'?'cours':((loadPack(l)?.strings||{})['cours']||'cours')));return out.replace(trim,m[1]+' '+word)}
                  m=trim.match(/^([0-9]+)(?:ère|e) heure$/i);if(m){const n=m[1],l=currentLang();if(l==='en')return out.replace(trim,'Period '+n);if(l==='de')return out.replace(trim,n+'. Stunde');if(l==='fr')return out;if(l==='es')return out.replace(trim,n+'.ª hora')}
                  return out;
                }
                function replaceCalendarWords(text){
                  let s=String(text||''),target=targetStrings(),rev=reverseMap();
                  const tokens=['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche','Lun','Mar','Mer','Jeu','Ven','Sam','Dim'];
                  tokens.forEach(x=>{const y=currentLang()==='fr'?x:(target[x]||BUILTIN[currentLang()]?.[x]);if(y)s=s.replace(new RegExp('\\b'+x+'\\b','g'),y)});
                  MONTHS.fr.forEach((x,i)=>{let y=x;if(currentLang()==='en')y=MONTHS.en[i];else if(currentLang()==='de')y=MONTHS.de[i];else if(currentLang()!=='fr')y=target[x]||x;s=s.replace(new RegExp(x,'gi'),y)});
                  return s;
                }
                function translateTextNode(node){
                  if(!node||node.nodeType!==3)return;const p=node.parentElement;if(!p||/^(SCRIPT|STYLE|TEXTAREA)$/i.test(p.tagName))return;
                  const raw=node.nodeValue,trim=String(raw||'').trim();if(!trim)return;
                  const translated=translateDynamic(raw);if(translated!==raw){node.nodeValue=translated;return}
                  if(p.matches('.date,#todayDate,.wh.day,.dayTab,#editDayTitle,#schoolHint')){const cal=replaceCalendarWords(raw);if(cal!==raw)node.nodeValue=cal}
                }
                function translateAttributes(root){
                  const scope=root&&root.querySelectorAll?root:document;
                  scope.querySelectorAll('[placeholder],[title],[aria-label]').forEach(el=>{
                    ['placeholder','title','aria-label'].forEach(a=>{const v=el.getAttribute(a);if(v){const n=translateDynamic(v);if(n!==v)el.setAttribute(a,n)}})
                  });
                }
                function translateTree(root){
                  const walker=document.createTreeWalker(root||document.body,NodeFilter.SHOW_TEXT);let n;while((n=walker.nextNode()))translateTextNode(n);translateAttributes(root||document)
                }

                const style=document.createElement('style');style.id='localization75Style';style.textContent=`
                  #languageDownloadBtn{width:100%;margin-top:8px;padding:8px 10px;border:1px dashed var(--set-accent,var(--blue));border-radius:8px;background:#fff;color:var(--set-accent,var(--blue));font-size:.74rem;font-weight:850}
                  #languagePackPanel{display:none;margin-top:8px;padding:8px;border-top:1px solid var(--line,#dce3eb)}
                  #languagePackPanel.show{display:block}.languagePackTitle{text-align:center;font-size:.74rem;font-weight:850;margin-bottom:7px}.languagePackItem{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:6px 0;border-top:1px solid #edf0f4}.languagePackItem:first-of-type{border-top:0}.languagePackName{font-size:.74rem;font-weight:800}.languagePackAction{border:1px solid var(--line,#dce3eb);border-radius:7px;background:#fff;padding:6px 8px;font-size:.70rem;font-weight:800;color:var(--set-accent,var(--blue))}.languagePackStatus{text-align:center;font-size:.66rem;color:var(--muted,#68738a);min-height:1em;margin-top:5px}
                `;document.head.appendChild(style);

                function ensureLanguageUi(){
                  const select=document.getElementById('languageSelect');if(!select)return;
                  installedList().forEach(x=>{if(!select.querySelector('option[value="'+x.code+'"]')){const o=document.createElement('option');o.value=x.code;o.textContent=x.name;select.appendChild(o)}});
                  const box=select.closest('.settingBox');if(!box)return;
                  let btn=document.getElementById('languageDownloadBtn');if(!btn){btn=document.createElement('button');btn.type='button';btn.id='languageDownloadBtn';box.appendChild(btn);btn.onclick=()=>openPackPanel()}
                  btn.textContent=tExact('Télécharger une langue…');
                  let panel=document.getElementById('languagePackPanel');if(!panel){panel=document.createElement('div');panel.id='languagePackPanel';box.appendChild(panel)}
                  if(select.value!==currentLang()&&select.querySelector('option[value="'+currentLang()+'"]'))select.value=currentLang();
                  if(!select.__localization75Bound){select.__localization75Bound=true;select.addEventListener('change',()=>{setTimeout(()=>{ensureLanguageUi();fullRefresh()},0)})}
                }
                async function catalog(){
                  try{const r=await fetch(CATALOG_URL,{cache:'no-store'});if(r.ok){const j=await r.json();if(j&&Array.isArray(j.languages))return j.languages}}catch(e){}
                  return [{code:'es',name:'Español',url:'https://raw.githubusercontent.com/Wokgui/Emploi-du-temps/main/language-packs/es.json'}];
                }
                async function openPackPanel(){
                  ensureLanguageUi();const panel=document.getElementById('languagePackPanel');if(!panel)return;panel.classList.add('show');
                  panel.innerHTML='<div class="languagePackTitle">'+tExact('Langues additionnelles')+'</div><div class="languagePackStatus">…</div>';
                  const list=await catalog();const status=panel.querySelector('.languagePackStatus');if(status)status.textContent='';
                  const installed=new Set(installedList().map(x=>x.code));
                  list.forEach(item=>{const row=document.createElement('div');row.className='languagePackItem';const name=document.createElement('span');name.className='languagePackName';name.textContent=item.name;const b=document.createElement('button');b.type='button';b.className='languagePackAction';b.textContent=installed.has(item.code)?tExact('Langue téléchargée'):tExact('Télécharger');b.disabled=installed.has(item.code);b.onclick=()=>downloadPack(item,b);row.append(name,b);panel.insertBefore(row,status)});
                }
                async function downloadPack(item,button){
                  const panel=document.getElementById('languagePackPanel'),status=panel&&panel.querySelector('.languagePackStatus');button.disabled=true;button.textContent='…';
                  try{
                    const r=await fetch(item.url,{cache:'no-store'});if(!r.ok)throw new Error('http');const pack=await r.json();if(!pack||pack.code!==item.code||!pack.strings)throw new Error('pack');const raw=JSON.stringify(pack);
                    localStorage.setItem(PACK_PREFIX+pack.code,raw);if(AndroidSchedule.saveLanguagePack&&!AndroidSchedule.saveLanguagePack(raw))throw new Error('native');
                    ensureLanguageUi();const select=document.getElementById('languageSelect');if(select){select.value=pack.code;select.dispatchEvent(new Event('change',{bubbles:true}))}
                    button.textContent=tExact('Langue téléchargée');if(status)status.textContent=tExact('Langue téléchargée');
                  }catch(e){button.disabled=false;button.textContent=tExact('Télécharger');if(status)status.textContent=tExact('Téléchargement impossible')}
                }

                function withTranslationLock(work){
                  if(translating)return;translating=true;
                  try{
                    if(localizationObserver)localizationObserver.disconnect();
                    work();
                  }finally{
                    if(localizationObserver)localizationObserver.observe(document.body,{childList:true,subtree:true,characterData:true});
                    translating=false;
                  }
                }
                function setVersion(){const v=document.getElementById('appVersionInfo');if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION}
                function translateOnly(){
                  queued=false;if(translating)return;
                  const roots=[...pendingRoots];pendingRoots.clear();if(!roots.length)return;
                  withTranslationLock(()=>{
                    document.documentElement.lang=currentLang().replace('_','-');
                    const compact=[];
                    roots.filter(Boolean).forEach(root=>{
                      if(!root.isConnected)return;
                      if(compact.some(x=>x===root||x.contains(root)))return;
                      for(let i=compact.length-1;i>=0;i--)if(root.contains(compact[i]))compact.splice(i,1);
                      compact.push(root);
                    });
                    compact.forEach(root=>translateTree(root));setVersion();
                  });
                }
                function schedule(root){
                  if(root)pendingRoots.add(root.nodeType===1?root:(root.parentElement||document.body));
                  if(translating)return;queued=false;translateOnly();
                }
                function fullRefresh(){
                  invalidateLocalizationCache();pendingRoots.clear();queued=false;
                  withTranslationLock(()=>{
                    const l=currentLang();document.documentElement.lang=l.replace('_','-');
                    ensureLanguageUi();translateTree(document.body);setVersion();
                  });
                }
                window.refreshLocalization75=fullRefresh;
                function rootFor(name){
                  if(name==='renderToday')return document.getElementById('viewToday');
                  if(name==='renderWeek')return document.getElementById('viewWeek');
                  if(name==='renderEdit')return document.getElementById('viewEdit');
                  if(name==='renderContext')return document.querySelector('.contextBar');
                  if(name==='openEditor')return document.getElementById('modal');
                  return document.getElementById('settingsSheet')||document.body;
                }
                ['renderToday','renderWeek','renderEdit','renderContext','openEditor'].forEach(name=>{
                  const old=window[name];if(typeof old==='function'&&!old.__loc75){
                    const w=function(){const r=old.apply(this,arguments);const rr=rootFor(name);if(rr)pendingRoots.add(rr);translateOnly();return r};w.__loc75=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                  }
                });
                localizationObserver=null;
                fullRefresh();

              }catch(e){console.log('Localization75Ui',e)}
            })();
            """;
    }

    // Former LayoutLanguage77Ui; isolated to stay below JVM constant limits.
    private static String layer1() {
        return """
            (function(){
              try{
                if(window.__layoutLanguage77V1){if(window.refreshLayoutLanguage77)window.refreshLayoutLanguage77();return}
                window.__layoutLanguage77V1=true;
                const APP_VERSION='6.31';

                const style=document.createElement('style');
                style.id='layoutLanguage77Style';
                style.textContent=`
                  /* Settings title is visually centred despite the close button on the right. */
                  #settingsSheet .settingsHead{position:relative!important;justify-content:flex-end!important;min-height:36px!important}
                  #settingsSheet #settingsTitle{position:absolute!important;left:50%!important;top:50%!important;transform:translate(-50%,-50%)!important;width:max-content!important;max-width:calc(100% - 88px)!important;margin:0!important;text-align:center!important}

                  /* Requested centred advanced-settings headings. */
                  #advReminderTitle,#advCalendarTitle,#advExceptionsTitle,#advProfilesTitle,#advBackupTitle,
                  #schoolTitle,#advRangeTitle,#advExceptionFormTitle{width:100%!important;text-align:center!important}
                  #advExceptionsTitle + .advButtons,
                  #advBackupTitle + .advButtons,
                  #advProfilesTitle ~ .advButtons{justify-content:center!important}
                  #advAddException,#advAddRange{display:block!important;margin-left:auto!important;margin-right:auto!important;text-align:center!important}
                  #advAddRange{min-width:112px!important}
                  #schoolCalendarBlock .schoolTitle{text-align:center!important;width:100%!important}

                  /* Keep the add-period action clearly below its centred heading and fields. */
                  #advRangeTitle{margin-top:11px!important;margin-bottom:7px!important}
                  #advRangeTitle ~ .advButtons{justify-content:center!important}

                  /* Main app title: slightly lower in the top band on all three views. */
                  .header h1{position:relative!important;top:3px!important}

                  #languageSelect,#languageDownloadBtn{touch-action:manipulation!important;-webkit-tap-highlight-color:transparent!important}
                `;
                document.head.appendChild(style);

                function bindLanguage(){
                  const select=document.getElementById('languageSelect');
                  if(!select||select.__language77Bound)return;
                  select.__language77Bound=true;
                }

                function centreActionGroups(){
                  const ids=['advAddException','advAddRange','advShareBackup','advRestoreBackup','advNewProfile','advRenameProfile','advDeleteProfile'];
                  ids.forEach(id=>{const b=document.getElementById(id),p=b&&b.parentElement;if(p&&p.classList.contains('advButtons'))p.style.setProperty('justify-content','center','important')});
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){bindLanguage();centreActionGroups();setVersion()}
                window.refreshLayoutLanguage77=refresh;

                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__layout77)return;const w=function(){const r=old.apply(this,arguments);refresh();return r};w.__layout77=true;window[name]=w;try{eval(name+'=w')}catch(e){}}
                ['refreshSettingsV3','refreshAdvancedFeatures','refreshUiPolishSchool','refreshLocalization75'].forEach(wrap);
                refresh();
              }catch(e){console.log('LayoutLanguage77Ui',e)}
            })();
            """;
    }

    // Former Stability78Ui; isolated to stay below JVM constant limits.
    private static String layer2() {
        return """
            (function(){
              try{
                if(window.__stability78V1){if(window.refreshStability78)window.refreshStability78();return}
                window.__stability78V1=true;
                const APP_VERSION='6.31';
                let refreshing=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{return (JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr')}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}

                const style=document.createElement('style');
                style.id='stability78Style';
                style.textContent=`
                  /* Names of interruptions: application and widget are deliberately independent. */
                  #viewEdit .breakSettings{overflow:visible!important}
                  #viewEdit .breakNamesScope78{padding:7px 9px 3px!important;text-align:center!important;font-size:.69rem!important;font-weight:900!important;color:var(--set-dark,var(--blue))!important;border-top:1px solid #edf0f4!important}
                  #viewEdit .breakNamesScope78:first-child{border-top:0!important}
                  #viewEdit .breakSettings .breakRow{grid-template-columns:64px minmax(0,1fr) auto!important}
                  #viewEdit .breakSettings .breakWidgetRow78{grid-template-columns:64px minmax(0,1fr)!important;padding-right:8px!important}
                  #viewEdit .breakWidgetRow78 .breakName{text-align:center!important}
                  #viewEdit .breakWidgetRow78 input{width:100%!important;min-width:0!important;box-sizing:border-box!important}

                  /* Nine periods must all remain reachable above the fixed bottom navigation. */
                  #viewEdit #slotSettings{overflow:visible!important;margin-bottom:76px!important}
                  #viewEdit #slotSettings .slotRow{display:grid!important}

                  /* Midi / trous: no explanatory subtext or extra separator before the colour controls. */
                  #settingsSheet #breakDisplayHint,
                  #settingsSheet #breakDisplaySetting>.coursePaletteHint,
                  #settingsSheet #fineSpecialColors>.settingTitle,
                  #settingsSheet #fineSpecialColors>.coursePaletteHint{display:none!important}
                  #settingsSheet #breakVisibility70{border-top:0!important;padding-top:0!important;margin-top:5px!important}
                  #settingsSheet #fineSpecialColors.embeddedFullColors74,
                  #settingsSheet #breakDisplaySetting #fineSpecialColors{border-top:0!important;padding-top:4px!important;margin-top:5px!important}
                  #settingsSheet #fineSpecialColors .specialWidgetTitle{border-top:0!important;padding-top:4px!important}

                  /* Explicit Today / Week / Widget visibility rows. */
                  #settingsSheet #breakVisibility70{display:grid!important;grid-template-columns:92px minmax(0,1fr) minmax(0,1fr)!important;column-gap:7px!important;row-gap:9px!important;align-items:center!important}
                  #settingsSheet #breakVisibility70>.breakVisTitle70:first-child{display:none!important}
                  #settingsSheet #breakVisibility70>.breakVisRow70{display:contents!important}
                  #settingsSheet #breakVisibility70>.breakVisRow70>span,
                  #settingsSheet #breakWidgetTitle74{display:flex!important;align-items:center!important;justify-content:center!important;text-align:center!important;font-size:.72rem!important;font-weight:850!important;color:var(--ink,#111936)!important}
                  #settingsSheet #breakWidgetTitle74{grid-column:1!important;grid-row:3!important}
                  #settingsSheet #breakWidget70{grid-column:2 / 4!important;grid-row:3!important;display:grid!important;grid-template-columns:1fr 1fr!important;gap:7px!important;margin:0!important}
                  #settingsSheet #breakVisibility70 label,#settingsSheet #breakWidget70 label{display:flex!important;align-items:center!important;justify-content:center!important;gap:5px!important;margin:0!important;font-size:.71rem!important;white-space:nowrap!important}

                  /* Nothing animates while the final startup state is being settled. */
                  body.startup78 *{transition:none!important;animation:none!important}
                `;
                document.head.appendChild(style);

                function ensureBreakNames(){
                  const card=document.querySelector('#viewEdit .breakSettings');
                  const gap=document.getElementById('gapLabel'),lunch=document.getElementById('lunchLabel');
                  if(!card||!gap||!lunch)return;

                  let appTitle=document.getElementById('breakNamesApp78');
                  if(!appTitle){appTitle=document.createElement('div');appTitle.id='breakNamesApp78';appTitle.className='breakNamesScope78';card.insertBefore(appTitle,card.firstChild)}
                  appTitle.textContent=tr('Application','Application','App');

                  let widgetTitle=document.getElementById('breakNamesWidget78');
                  if(!widgetTitle){widgetTitle=document.createElement('div');widgetTitle.id='breakNamesWidget78';widgetTitle.className='breakNamesScope78';card.appendChild(widgetTitle)}
                  widgetTitle.textContent='Widget';

                  function ensureRow(id,labelText,inputId){
                    let row=document.getElementById(id),input=document.getElementById(inputId);
                    if(!row){row=document.createElement('div');row.id=id;row.className='breakRow breakWidgetRow78';const name=document.createElement('div');name.className='breakName';row.appendChild(name);input=document.createElement('input');input.id=inputId;input.type='text';input.maxLength=35;row.appendChild(input);card.appendChild(row)}
                    const name=row.querySelector('.breakName');if(name)name.textContent=labelText;
                    return input;
                  }
                  const wg=ensureRow('widgetGapRow78',tr('Trou','Free period','Freistunde'),'widgetGapLabel78');
                  const wl=ensureRow('widgetLunchRow78','Midi','widgetLunchLabel78');
                  const a=loadAdv();
                  if(document.activeElement!==wg)wg.value=(a.gapWidgetLabel||'').trim()||gap.value||tr('Trou','Free period','Freistunde');
                  if(document.activeElement!==wl)wl.value=(a.lunchWidgetLabel||'').trim()||lunch.value||'Midi';
                  if(!wg.__bound78){wg.__bound78=true;wg.addEventListener('change',()=>{const n=loadAdv();n.gapWidgetLabel=wg.value.trim();saveAdv(n)})}
                  if(!wl.__bound78){wl.__bound78=true;wl.addEventListener('change',()=>{const n=loadAdv();n.lunchWidgetLabel=wl.value.trim();saveAdv(n)})}

                  const appGapName=gap.closest('.breakRow')?.querySelector('.breakName');if(appGapName)appGapName.textContent=tr('Trou','Free period','Freistunde');
                  const appLunchName=lunch.closest('.breakRow')?.querySelector('.breakName');if(appLunchName)appLunchName.textContent='Midi';
                }

                function ensureNineSlots(){
                  try{
                    if(typeof slots!=='undefined'&&Array.isArray(slots)&&slots.length<9){
                      const defs=[['08:00','09:00'],['09:00','10:00'],['10:00','11:00'],['11:00','12:00'],['13:00','14:00'],['14:00','15:00'],['16:00','17:00'],['17:00','18:00'],['18:00','19:00']];
                      for(let i=slots.length;i<9;i++)slots.push({n:i+1,start:defs[i][0],end:defs[i][1]});
                    }
                    const box=document.getElementById('slotSettings');
                    if(box&&box.querySelectorAll(':scope > .slotRow').length!==9&&typeof renderSlots==='function')renderSlots();
                  }catch(e){}
                }

                function ensureWidgetVisibility(){
                  const root=document.getElementById('breakVisibility70'),widget=document.getElementById('breakWidget70');if(!root||!widget)return;
                  let title=document.getElementById('breakWidgetTitle74');
                  if(!title){
                    title=[...root.querySelectorAll(':scope > .breakVisTitle70')].find(x=>/widget/i.test(String(x.textContent||'')));
                    if(!title){title=document.createElement('div');title.className='breakVisTitle70';root.insertBefore(title,widget)}
                    title.id='breakWidgetTitle74';
                  }
                  title.textContent='Widget';
                  const lunch=document.getElementById('advShowLunch'),gap=document.getElementById('advShowBreaks');
                  const lr=lunch&&lunch.closest('label'),gr=gap&&gap.closest('label');
                  if(lr&&lr.parentElement!==widget)widget.appendChild(lr);
                  if(gr&&gr.parentElement!==widget)widget.appendChild(gr);
                  const ll=document.getElementById('advShowLunchLabel'),gl=document.getElementById('advShowBreaksLabel');
                  if(ll)ll.textContent='Midi';if(gl)gl.textContent=tr('Trous','Free periods','Freistunden');
                }

                function cleanMidiSettings(){
                  const full=document.getElementById('fineSpecialColors');if(full){full.classList.add('embeddedFullColors74');full.style.removeProperty('border-top')}
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){
                  if(refreshing)return;refreshing=true;
                  try{ensureBreakNames();ensureNineSlots();ensureWidgetVisibility();cleanMidiSettings();setVersion()}finally{refreshing=false}
                }
                window.refreshStability78=refresh;

                let refreshTimer84=0;
                function scheduleRefresh84(){if(refreshTimer84)return;refreshTimer84=setTimeout(()=>{refreshTimer84=0;refresh()},18)}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__stability78)return;const w=function(){const r=old.apply(this,arguments);scheduleRefresh84();return r};w.__stability78=true;window[name]=w;try{eval(name+'=w')}catch(e){}}
                ['renderEdit','renderSlots','refreshStability74'].forEach(wrap);

                document.body.classList.add('startup78');
                refresh();requestAnimationFrame(()=>{refresh();requestAnimationFrame(()=>document.body.classList.remove('startup78'))});
              }catch(e){console.log('Stability78Ui',e)}
            })();
            """;
    }

    // Former Stability79Ui; isolated to stay below JVM constant limits.
    private static String layer3() {
        return """
            (function(){
              try{
                if(window.__stability79V1){if(window.refreshStability79)window.refreshStability79();return}
                window.__stability79V1=true;
                const APP_VERSION='6.31';
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

    // Former Stability80Ui; isolated to stay below JVM constant limits.
    private static String layer4() {
        return """
            (function(){
              try{
                if(window.__stability80V1){if(window.refreshStability80)window.refreshStability80();return}
                window.__stability80V1=true;
                const APP_VERSION='6.31';
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
