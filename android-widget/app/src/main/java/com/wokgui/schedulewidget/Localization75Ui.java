package com.wokgui.schedulewidget;

final class Localization75Ui {
    private Localization75Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__localization75V1){if(window.refreshLocalization75)window.refreshLocalization75();return}
                window.__localization75V1=true;
                const APP_VERSION='6.15';
                const PACK_PREFIX='edt-language-pack-v1-';
                const CATALOG_URL='https://raw.githubusercontent.com/Wokgui/Emploi-du-temps/main/language-packs/catalog.json';
                let translating=false,queued=false;

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
                function currentLang(){const l=String(nativeUi().language||'fr');return l||'fr'}
                function installedList(){try{return JSON.parse(AndroidSchedule.loadLanguagePacks?AndroidSchedule.loadLanguagePacks():'[]')}catch(e){return []}}
                function loadPack(code){
                  try{const cached=localStorage.getItem(PACK_PREFIX+code);if(cached)return JSON.parse(cached)}catch(e){}
                  try{if(AndroidSchedule.loadLanguagePack){const raw=AndroidSchedule.loadLanguagePack(code);if(raw){localStorage.setItem(PACK_PREFIX+code,raw);return JSON.parse(raw)}}}catch(e){}
                  return null;
                }
                function targetStrings(){
                  const l=currentLang();if(l==='fr')return {};
                  if(BUILTIN[l])return BUILTIN[l];
                  const p=loadPack(l);return p&&p.strings?p.strings:{};
                }
                function reverseMap(){
                  const r={};
                  const add=(canonical,value)=>{if(value!=null&&String(value).trim())r[String(value).trim()]=canonical};
                  const keys=new Set();Object.values(BUILTIN).forEach(m=>Object.keys(m).forEach(k=>keys.add(k)));
                  keys.forEach(k=>add(k,k));
                  Object.values(BUILTIN).forEach(m=>Object.entries(m).forEach(([k,v])=>add(k,v)));
                  installedList().forEach(x=>{const p=loadPack(x.code);if(p&&p.strings)Object.entries(p.strings).forEach(([k,v])=>{add(k,k);add(k,v)})});
                  return r;
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
                  let m=trim.match(/^Cette semaine\s*:\s*([A-D])$/i);if(m)return out.replace(trim,tExact('Cette semaine')+' : '+m[1].toUpperCase());
                  m=trim.match(/^(?:Semaine|Week|Woche)\s+([A-D])$/i);if(m)return out.replace(trim,weekWord()+' '+m[1].toUpperCase());
                  m=trim.match(/^Aperçu semaine\s*([A-D])?$/i);if(m)return out.replace(trim,tExact('Aperçu semaine')+(m[1]?' '+m[1].toUpperCase():''));
                  m=trim.match(/^(Lundi|Mardi|Mercredi|Jeudi|Vendredi|Samedi|Dimanche|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Montag|Dienstag|Mittwoch|Donnerstag|Freitag|Samstag|Sonntag)\s*·\s*(?:Semaine|Week|Woche)\s+([A-D])$/i);
                  if(m){const d=tExact(m[1]);return out.replace(trim,d+' · '+weekWord()+' '+m[2].toUpperCase())}
                  m=trim.match(/^(\d+)\s+cours$/i);if(m){const l=currentLang();const word=l==='de'?'Stunden':(l==='en'?'classes':(l==='fr'?'cours':((loadPack(l)?.strings||{})['cours']||'cours')));return out.replace(trim,m[1]+' '+word)}
                  m=trim.match(/^(\d+)(?:ère|e) heure$/i);if(m){const n=m[1],l=currentLang();if(l==='en')return out.replace(trim,'Period '+n);if(l==='de')return out.replace(trim,n+'. Stunde');if(l==='fr')return out;if(l==='es')return out.replace(trim,n+'.ª hora')}
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

                function fullRefresh(){
                  if(translating)return;translating=true;
                  try{
                    const l=currentLang();document.documentElement.lang=l.replace('_','-');
                    ['refreshSettingsV3','refreshAdvancedFeatures','refreshUiPolishSchool','refreshCoursePaletteV4','refreshFineTuneUi','refreshLunchBreakUi','refreshDoubleLunchUi','refreshWeekendUi','refreshStability70','refreshStability71','refreshStability72','refreshStability73','refreshStability74'].forEach(n=>{try{if(typeof window[n]==='function')window[n]()}catch(e){}});
                    ensureLanguageUi();translateTree(document.body);
                    const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                  }finally{translating=false}
                }
                window.refreshLocalization75=fullRefresh;
                function schedule(){if(queued||translating)return;queued=true;requestAnimationFrame(()=>{queued=false;fullRefresh()})}
                ['render','renderToday','renderWeek','renderEdit','renderContext','openEditor','refreshSettingsV3','refreshAdvancedFeatures','refreshStability74'].forEach(name=>{const old=window[name];if(typeof old==='function'&&!old.__loc75){const w=function(){const r=old.apply(this,arguments);schedule();return r};w.__loc75=true;window[name]=w;try{eval(name+'=w')}catch(e){}}});
                const obs=new MutationObserver(m=>{if(translating)return;let relevant=false;for(const x of m){if(x.type==='childList'||x.type==='characterData'){relevant=true;break}}if(relevant)schedule()});obs.observe(document.body,{childList:true,subtree:true,characterData:true});
                fullRefresh();requestAnimationFrame(fullRefresh);setTimeout(fullRefresh,120);
              }catch(e){console.log('Localization75Ui',e)}
            })();
            """;
    }
}
