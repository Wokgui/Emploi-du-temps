package com.wokgui.schedulewidget;

final class PersonalizationUi {
    private PersonalizationUi() {}

    static String script() {
        return """
            (function(){
              try {
                if (window.__personalizationInstalled) {
                  if (window.refreshPersonalizationUi) window.refreshPersonalizationUi();
                  return;
                }
                window.__personalizationInstalled = true;

                const THEMES = {
                  blue:{name:{fr:'Bleu',en:'Blue',de:'Blau'},accent:'#0877f9',dark:'#075fae',soft:'#d9eafb',bg:'#f6f8fb',ink:'#111936',muted:'#68738a',line:'#dce3eb'},
                  teal:{name:{fr:'Turquoise',en:'Teal',de:'Türkis'},accent:'#00897b',dark:'#00695c',soft:'#ddf4f0',bg:'#f5f9f8',ink:'#102b28',muted:'#60736f',line:'#d7e6e3'},
                  violet:{name:{fr:'Violet',en:'Violet',de:'Violett'},accent:'#6750a4',dark:'#4f378b',soft:'#ede7f6',bg:'#f8f6fb',ink:'#241b35',muted:'#6c6476',line:'#e1dbe9'},
                  green:{name:{fr:'Vert',en:'Green',de:'Grün'},accent:'#2e7d32',dark:'#1b5e20',soft:'#e3f3e4',bg:'#f6faf6',ink:'#172b19',muted:'#617162',line:'#d9e7da'},
                  amber:{name:{fr:'Ambre',en:'Amber',de:'Amber'},accent:'#ef6c00',dark:'#bf4e00',soft:'#ffebd8',bg:'#fff9f4',ink:'#352015',muted:'#75685e',line:'#eadfd5'}
                };

                const TXT = {
                  fr:{settings:'Réglages',appFont:'Taille du texte de l’application',widgetFont:'Taille du texte du widget',language:'Langue',theme:'Thème',reset:'Réinitialiser',close:'Fermer',appPreview:'Aperçu application',widgetPreview:'Aperçu widget',thisWeek:'Cette semaine',week:'Semaine',today:'Aujourd’hui',edit:'Modifier',progress:'Avancement',importPhoto:'Importer une photo d’emploi du temps',interruptions:'Noms des interruptions',schedules:'Horaires des 7 heures',addCourse:'Ajouter un cours',gap:'Trou',lunch:'Pause de midi',badge:'Badge',previewWeek:'Aperçu semaine',courseTime:'Heure de cours',group:'Classe / groupe',room:'Salle',delete:'Supprimer',cancel:'Annuler',save:'Enregistrer',current:'En cours',noCourse:'Aucun cours aujourd’hui.',hour:'heure',weekLower:'semaine',courses:'cours',kept:'Horaire conservé',applied:'Horaire appliqué'},
                  en:{settings:'Settings',appFont:'App text size',widgetFont:'Widget text size',language:'Language',theme:'Theme',reset:'Reset',close:'Close',appPreview:'App preview',widgetPreview:'Widget preview',thisWeek:'This week',week:'Week',today:'Today',edit:'Edit',progress:'Progress',importPhoto:'Import a timetable photo',interruptions:'Break names',schedules:'7 period times',addCourse:'Add a class',gap:'Free period',lunch:'Lunch break',badge:'Badge',previewWeek:'Week preview',courseTime:'Class period',group:'Class / group',room:'Room',delete:'Delete',cancel:'Cancel',save:'Save',current:'In class',noCourse:'No class today.',hour:'period',weekLower:'week',courses:'classes',kept:'Current time kept',applied:'Applied time'},
                  de:{settings:'Einstellungen',appFont:'Textgröße der App',widgetFont:'Textgröße des Widgets',language:'Sprache',theme:'Design',reset:'Zurücksetzen',close:'Schließen',appPreview:'App-Vorschau',widgetPreview:'Widget-Vorschau',thisWeek:'Diese Woche',week:'Woche',today:'Heute',edit:'Bearbeiten',progress:'Fortschritt',importPhoto:'Stundenplan-Foto importieren',interruptions:'Bezeichnungen der Unterbrechungen',schedules:'Zeiten der 7 Stunden',addCourse:'Stunde hinzufügen',gap:'Freistunde',lunch:'Mittagspause',badge:'Badge',previewWeek:'Wochenübersicht',courseTime:'Unterrichtsstunde',group:'Klasse / Gruppe',room:'Raum',delete:'Löschen',cancel:'Abbrechen',save:'Speichern',current:'Läuft',noCourse:'Heute kein Unterricht.',hour:'Stunde',weekLower:'Woche',courses:'Stunden',kept:'Zeit beibehalten',applied:'Zeit übernommen'}
                };

                let settings = {appFontScale:1,widgetFontScale:1,language:'fr',theme:'blue'};
                try {
                  if (window.AndroidSchedule && AndroidSchedule.loadUiSettings) {
                    const raw = AndroidSchedule.loadUiSettings();
                    if (raw) settings = Object.assign(settings, JSON.parse(raw));
                  } else {
                    const raw = localStorage.getItem('edt-ui-settings');
                    if (raw) settings = Object.assign(settings, JSON.parse(raw));
                  }
                } catch(e) {}

                function clamp(v){return Math.max(.8,Math.min(1.4,Number(v)||1));}
                function lang(){return ['fr','en','de'].includes(settings.language)?settings.language:'fr';}
                function T(){return TXT[lang()];}
                function theme(){return THEMES[settings.theme]||THEMES.blue;}
                function setText(el,value){if(el && el.textContent!==value) el.textContent=value;}

                const style = document.createElement('style');
                style.id = 'personalizationStyle';
                style.textContent = `
                  .header{position:relative!important;background:linear-gradient(135deg,var(--theme-accent),var(--theme-dark))!important}
                  #settingsBtn{position:absolute;right:10px;bottom:12px;width:36px;height:36px;border:0;border-radius:18px;background:#ffffff24;color:#fff;font-size:20px;display:flex;align-items:center;justify-content:center;padding:0}
                  .currentWeek{background:var(--theme-soft)!important;color:var(--theme-dark)!important}.currentWeek b{color:var(--theme-accent)!important}
                  .weekTab.active,.btn.primary{background:var(--theme-accent)!important;border-color:var(--theme-accent)!important}.dayTab.active{background:linear-gradient(135deg,var(--theme-accent),var(--theme-dark))!important;border-color:var(--theme-accent)!important}
                  .timeline>span{background:var(--theme-accent)!important}.timelineHead strong,.count,.addBtn{color:var(--theme-accent)!important}.addBtn{border-color:var(--theme-accent)!important}
                  .todayCourse.current:before{background:var(--theme-accent)!important}.badge:not(.gap):not(.lunch){background:var(--theme-accent)!important}
                  #settingsModal{position:fixed;inset:0;z-index:90;background:#0b17386b;display:none;align-items:flex-end}#settingsModal.show{display:flex}
                  #settingsSheet{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:18px 18px 0 0;padding:15px 14px calc(16px + env(safe-area-inset-bottom));max-height:88vh;overflow:auto;color:#111936}
                  .settingsHead{display:flex;align-items:center;justify-content:space-between;margin-bottom:10px}.settingsHead h2{margin:0;font-size:1.08rem}.settingsClose{border:0;background:#eef2f6;border-radius:50%;width:32px;height:32px;font-size:18px}
                  .settingGroup{background:#fff;border:1px solid #dde4ec;border-radius:10px;padding:10px;margin-top:9px}.settingTitle{font-weight:800;font-size:.82rem;margin-bottom:7px}.settingRow{display:flex;align-items:center;gap:9px}.settingRow input[type=range]{width:100%;accent-color:var(--theme-accent)}.settingValue{min-width:45px;text-align:right;font-weight:800;color:var(--theme-accent);font-size:.78rem}
                  .langSelect{width:100%;padding:9px;border:1px solid #d8e0e8;border-radius:8px;background:#fff;color:#182239}
                  .themeGrid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:6px}.themeChoice{border:2px solid transparent;border-radius:9px;padding:5px 3px;background:#f7f8fa;text-align:center}.themeChoice.active{border-color:var(--theme-accent);background:var(--theme-soft)}.themeSwatch{display:block;height:25px;border-radius:6px;margin-bottom:3px}.themeName{font-size:.58rem;font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  .previewGrid{display:grid;grid-template-columns:1fr 1fr;gap:8px}.previewLabel{font-size:.67rem;font-weight:800;color:#657087;margin-bottom:4px}.appMini,.widgetMini{border:1px solid #dce3eb;background:#fff;overflow:hidden}.appMiniTop{height:20px;background:linear-gradient(90deg,var(--theme-accent),var(--theme-dark));color:#fff;font-size:7px;font-weight:800;display:flex;align-items:center;justify-content:center}.appMiniBody{padding:5px}.miniLine{height:6px;border-radius:4px;background:var(--theme-soft);margin:3px 0}.miniLine.accent{width:65%;background:var(--theme-accent)}.widgetMiniTop{background:var(--theme-soft);padding:5px}.widgetMiniTitle{font-size:7px;font-weight:900;color:var(--theme-ink)}.widgetMiniMeta{font-size:6px;color:var(--theme-muted);margin-top:2px}.widgetMiniBar{height:4px;background:#d7e0e8;margin-top:4px}.widgetMiniBar>i{display:block;width:56%;height:100%;background:var(--theme-accent)}
                  .settingsActions{display:flex;justify-content:space-between;gap:8px;margin-top:12px}.settingsAction{padding:9px 12px;border:1px solid #d6dee7;border-radius:8px;background:#fff;font-weight:800}.settingsAction.primary{background:var(--theme-accent);border-color:var(--theme-accent);color:#fff}
                  @media(max-width:380px){.themeGrid{grid-template-columns:repeat(3,1fr)}}
                `;
                document.head.appendChild(style);

                const header = document.querySelector('.header');
                if (header && !document.getElementById('settingsBtn')) {
                  const btn = document.createElement('button');
                  btn.id='settingsBtn';btn.type='button';btn.textContent='⚙';btn.setAttribute('aria-label','Réglages');
                  header.appendChild(btn);
                }

                const modal = document.createElement('div');
                modal.id='settingsModal';
                modal.innerHTML = `
                  <div id="settingsSheet">
                    <div class="settingsHead"><h2 id="settingsTitle"></h2><button id="settingsCloseX" class="settingsClose" type="button">×</button></div>
                    <div class="previewGrid">
                      <div><div id="appPreviewLabel" class="previewLabel"></div><div class="appMini"><div class="appMiniTop" id="appMiniTitle">Emploi du temps</div><div class="appMiniBody"><div class="miniLine accent"></div><div class="miniLine"></div><div class="miniLine"></div></div></div></div>
                      <div><div id="widgetPreviewLabel" class="previewLabel"></div><div class="widgetMini"><div class="widgetMiniTop"><div class="widgetMiniTitle">4G1 ALL · 4G2 ALL</div><div class="widgetMiniMeta">08:00 - 09:00</div><div class="widgetMiniBar"><i></i></div></div></div></div>
                    </div>
                    <div class="settingGroup"><div id="appFontTitle" class="settingTitle"></div><div class="settingRow"><input id="appFontRange" type="range" min="80" max="140" step="5"><div id="appFontValue" class="settingValue"></div></div></div>
                    <div class="settingGroup"><div id="widgetFontTitle" class="settingTitle"></div><div class="settingRow"><input id="widgetFontRange" type="range" min="80" max="140" step="5"><div id="widgetFontValue" class="settingValue"></div></div></div>
                    <div class="settingGroup"><div id="languageTitle" class="settingTitle"></div><select id="languageSelect" class="langSelect"><option value="fr">Français</option><option value="de">Deutsch</option><option value="en">English</option></select></div>
                    <div class="settingGroup"><div id="themeTitle" class="settingTitle"></div><div id="themeGrid" class="themeGrid"></div></div>
                    <div class="settingsActions"><button id="settingsReset" class="settingsAction" type="button"></button><button id="settingsDone" class="settingsAction primary" type="button"></button></div>
                  </div>`;
                document.body.appendChild(modal);

                function cssVar(name,value){document.documentElement.style.setProperty(name,value);}
                function applyTheme(){
                  const th=theme();
                  cssVar('--theme-accent',th.accent);cssVar('--theme-dark',th.dark);cssVar('--theme-soft',th.soft);cssVar('--theme-ink',th.ink);cssVar('--theme-muted',th.muted);
                  cssVar('--blue',th.accent);cssVar('--blue2',th.dark);cssVar('--teal',th.accent);cssVar('--ink',th.ink);cssVar('--muted',th.muted);cssVar('--line',th.line);cssVar('--soft',th.soft);cssVar('--bg',th.bg);
                  document.body.style.background=th.bg;
                  document.querySelectorAll('.themeChoice').forEach(b=>b.classList.toggle('active',b.dataset.theme===settings.theme));
                }

                function applyFont(){
                  settings.appFontScale=clamp(settings.appFontScale);settings.widgetFontScale=clamp(settings.widgetFontScale);
                  document.documentElement.style.fontSize=(16*settings.appFontScale)+'px';
                  document.body.style.fontSize=(15*settings.appFontScale)+'px';
                  const ar=document.getElementById('appFontRange'),wr=document.getElementById('widgetFontRange');
                  if(ar) ar.value=Math.round(settings.appFontScale*100);if(wr) wr.value=Math.round(settings.widgetFontScale*100);
                  setText(document.getElementById('appFontValue'),Math.round(settings.appFontScale*100)+' %');setText(document.getElementById('widgetFontValue'),Math.round(settings.widgetFontScale*100)+' %');
                }

                const dayNames={
                  fr:{Lundi:'Lundi',Mardi:'Mardi',Mercredi:'Mercredi',Jeudi:'Jeudi',Vendredi:'Vendredi',Lun:'Lun',Mar:'Mar',Mer:'Mer',Jeu:'Jeu',Ven:'Ven'},
                  en:{Lundi:'Monday',Mardi:'Tuesday',Mercredi:'Wednesday',Jeudi:'Thursday',Vendredi:'Friday',Lun:'Mon',Mar:'Tue',Mer:'Wed',Jeu:'Thu',Ven:'Fri'},
                  de:{Lundi:'Montag',Mardi:'Dienstag',Mercredi:'Mittwoch',Jeudi:'Donnerstag',Vendredi:'Freitag',Lun:'Mo',Mar:'Di',Mer:'Mi',Jeu:'Do',Ven:'Fr'}
                };
                const allDayWords=['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Monday','Tuesday','Wednesday','Thursday','Friday','Montag','Dienstag','Mittwoch','Donnerstag','Freitag'];
                function translateDayText(text){
                  let result=String(text||'');
                  const canonical={Monday:'Lundi',Tuesday:'Mardi',Wednesday:'Mercredi',Thursday:'Jeudi',Friday:'Vendredi',Montag:'Lundi',Dienstag:'Mardi',Mittwoch:'Mercredi',Donnerstag:'Jeudi',Freitag:'Vendredi'};
                  for(const word of allDayWords){if(result.startsWith(word)){const base=canonical[word]||word;result=dayNames[lang()][base]+result.slice(word.length);break;}}
                  result=result.replace(/\b(semaine|week|Woche)\b/gi,T().weekLower);
                  return result;
                }

                function translateRoomText(text){
                  let s=String(text||'');
                  const roomWord=lang()==='de'?'Raum':(lang()==='en'?'room':'salle');
                  const hourWord=T().hour;
                  s=s.replace(/^(salle|room|Raum)\s+/i,roomWord+' ');
                  s=s.replace(/\s·\s(heure|period|Stunde)\s+/i,' · '+hourWord+' ');
                  return s;
                }

                let localizationQueued=false;
                function localizeDom(){
                  if(localizationQueued)return;localizationQueued=true;
                  requestAnimationFrame(()=>{
                    localizationQueued=false;const t=T();
                    setText(document.querySelector('.header h1'),lang()==='de'?'Stundenplan':(lang()==='en'?'Timetable':'Emploi du temps'));
                    const currentBtn=document.getElementById('currentWeekBtn');if(currentBtn){const b=currentBtn.querySelector('b');const letter=b?b.textContent:'A';currentBtn.innerHTML=t.thisWeek+' : <b>'+letter+'</b>';}
                    document.querySelectorAll('.weekTab').forEach(b=>setText(b,t.week+' '+b.dataset.week));
                    document.querySelectorAll('.nav').forEach(b=>{const icon=b.querySelector('span')?.outerHTML||'';const label=b.dataset.mode==='today'?t.today:(b.dataset.mode==='week'?t.week:t.edit);const wanted=icon+label;if(b.innerHTML!==wanted)b.innerHTML=wanted;});
                    setText(document.querySelector('.timelineHead strong'),t.progress);
                    const imp=document.getElementById('importPhoto');if(imp&&!imp.disabled)setText(imp,t.importPhoto);
                    const heads=[...document.querySelectorAll('.sectionHead h3')];heads.forEach(h=>{const x=h.textContent.trim();if(/interrupt|Unterbrech|Noms/i.test(x))setText(h,t.interruptions);else if(/Horaires|period|Stunden|7 heures/i.test(x))setText(h,t.schedules);});
                    const add=document.getElementById('addCourse');if(add)setText(add,'＋ '+t.addCourse);
                    const breakNames=document.querySelectorAll('.breakName');if(breakNames[0])setText(breakNames[0],t.gap);if(breakNames[1])setText(breakNames[1],t.lunch);
                    document.querySelectorAll('.badgeToggle').forEach(l=>{let node=[...l.childNodes].find(n=>n.nodeType===3);if(node&&node.nodeValue.trim()!==t.badge)node.nodeValue=t.badge;});
                    const dayTitle=document.getElementById('todayTitle');if(dayTitle)setText(dayTitle,translateDayText(dayTitle.textContent));
                    const editTitle=document.getElementById('editDayTitle');if(editTitle)setText(editTitle,translateDayText(editTitle.textContent));
                    const weekHead=document.querySelector('.weekTop h2');if(weekHead){const letter=document.getElementById('weekTitleLetter')?.textContent||'A';weekHead.innerHTML=t.previewWeek+' <span id="weekTitleLetter" class="weekLetter">'+letter+'</span>';}
                    document.querySelectorAll('.todayCourse .room,.editCourse .room').forEach(el=>setText(el,translateRoomText(el.textContent)));
                    document.querySelectorAll('.todayCourse.gap .label,.gapCell .cellLabel').forEach(el=>setText(el,t.gap));
                    document.querySelectorAll('.todayCourse.lunch .label,.lunchCell .cellLabel').forEach(el=>setText(el,t.lunch));
                    document.querySelectorAll('.badge.gap').forEach(el=>setText(el,t.gap));document.querySelectorAll('.badge.lunch').forEach(el=>setText(el,t.lunch));document.querySelectorAll('.todayCourse.current .badge').forEach(el=>setText(el,t.current));
                    document.querySelectorAll('.wh.day').forEach(el=>{const fr={Mon:'Lun',Tue:'Mar',Wed:'Mer',Thu:'Jeu',Fri:'Ven',Mo:'Lun',Di:'Mar',Mi:'Mer',Do:'Jeu',Fr:'Ven'};const base=fr[el.textContent]||el.textContent;setText(el,dayNames[lang()][base]||base);});
                    const emptyToday=document.querySelector('#todayList .empty');if(emptyToday)setText(emptyToday,t.noCourse);
                    const count=document.getElementById('editCount');if(count){const n=(count.textContent.match(/\d+/)||['0'])[0];setText(count,n+' '+t.courses);}
                    const modalTitle=document.getElementById('modalTitle');if(modalTitle){let x=modalTitle.textContent;x=x.replace(/^(Modifier|Edit|Bearbeiten)/,t.edit).replace(/^(Ajouter|Add|Hinzufügen)/,t.addCourse).replace(/\b(semaine|week|Woche)\b/gi,t.weekLower);setText(modalTitle,x);}
                    const labels=document.querySelectorAll('#courseForm .field label');if(labels[0])setText(labels[0],t.courseTime);if(labels[1])setText(labels[1],t.group);if(labels[2])setText(labels[2],t.room);
                    setText(document.getElementById('deleteCourse'),t.delete);setText(document.getElementById('cancelEdit'),t.cancel);const saveBtn=document.querySelector('#courseForm button[type=submit]');if(saveBtn)setText(saveBtn,t.save);
                    const preview=document.getElementById('slotPreview');if(preview){let x=preview.textContent.replace(/Horaire conservé|Current time kept|Zeit beibehalten/g,t.kept).replace(/Horaire appliqué|Applied time|Zeit übernommen/g,t.applied);setText(preview,x);}
                    document.querySelectorAll('#fSlot option').forEach((o,i)=>{let x=o.textContent;x=x.replace(/\bheure\b|\bperiod\b|\bStunde\b/gi,t.hour);setText(o,x);});
                    renderSettingsText();
                  });
                }

                function renderThemes(){
                  const grid=document.getElementById('themeGrid');if(!grid)return;grid.innerHTML='';
                  Object.entries(THEMES).forEach(([id,th])=>{const b=document.createElement('button');b.type='button';b.className='themeChoice';b.dataset.theme=id;b.innerHTML='<span class="themeSwatch" style="background:linear-gradient(135deg,'+th.accent+','+th.soft+')"></span><span class="themeName">'+th.name[lang()]+'</span>';b.onclick=()=>{settings.theme=id;applyTheme();persist();renderSettingsText();};grid.appendChild(b);});
                  applyTheme();
                }

                function renderSettingsText(){
                  const t=T();setText(document.getElementById('settingsTitle'),t.settings);setText(document.getElementById('appPreviewLabel'),t.appPreview);setText(document.getElementById('widgetPreviewLabel'),t.widgetPreview);setText(document.getElementById('appFontTitle'),t.appFont);setText(document.getElementById('widgetFontTitle'),t.widgetFont);setText(document.getElementById('languageTitle'),t.language);setText(document.getElementById('themeTitle'),t.theme);setText(document.getElementById('settingsReset'),t.reset);setText(document.getElementById('settingsDone'),t.close);setText(document.getElementById('appMiniTitle'),lang()==='de'?'Stundenplan':(lang()==='en'?'Timetable':'Emploi du temps'));
                  const grid=document.getElementById('themeGrid');if(grid&&grid.children.length){[...grid.children].forEach(b=>{const n=b.querySelector('.themeName');if(n)n.textContent=THEMES[b.dataset.theme].name[lang()];});}
                }

                function persist(){
                  settings.appFontScale=clamp(settings.appFontScale);settings.widgetFontScale=clamp(settings.widgetFontScale);
                  const raw=JSON.stringify(settings);
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveUiSettings)AndroidSchedule.saveUiSettings(raw);else localStorage.setItem('edt-ui-settings',raw);}catch(e){}
                }

                function applyAll(){applyTheme();applyFont();localizeDom();}
                window.refreshPersonalizationUi=applyAll;

                document.getElementById('settingsBtn').onclick=()=>{document.getElementById('settingsModal').classList.add('show');renderThemes();renderSettingsText();applyFont();document.getElementById('languageSelect').value=lang();};
                document.getElementById('settingsCloseX').onclick=()=>document.getElementById('settingsModal').classList.remove('show');
                document.getElementById('settingsDone').onclick=()=>document.getElementById('settingsModal').classList.remove('show');
                document.getElementById('settingsModal').onclick=e=>{if(e.target===document.getElementById('settingsModal'))document.getElementById('settingsModal').classList.remove('show');};
                document.getElementById('appFontRange').oninput=e=>{settings.appFontScale=Number(e.target.value)/100;applyFont();persist();};
                document.getElementById('widgetFontRange').oninput=e=>{settings.widgetFontScale=Number(e.target.value)/100;applyFont();persist();};
                document.getElementById('languageSelect').onchange=e=>{settings.language=e.target.value;renderThemes();localizeDom();persist();};
                document.getElementById('settingsReset').onclick=()=>{settings={appFontScale:1,widgetFontScale:1,language:'fr',theme:'blue'};document.getElementById('languageSelect').value='fr';renderThemes();applyAll();persist();};

                new MutationObserver(()=>localizeDom()).observe(document.body,{childList:true,subtree:true,characterData:true});
                applyAll();renderThemes();
              } catch(e) {
                console.log('Personalization UI', e);
              }
            })();
            """;
    }
}
