package com.wokgui.schedulewidget;

/** Base settings UI and its persistent controls. */
final class BaseSettingsUi {
    private BaseSettingsUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(64 * 1024);
        out.append(layer0()).append('\n'); // PersonalizationUi2
        return out.toString();
    }

    // Former PersonalizationUi2; isolated to stay below JVM constant limits.
    private static String layer0() {
        return """
            (function(){
              try {
                if (window.__settingsV3) {
                  if (window.refreshSettingsV3) window.refreshSettingsV3();
                  return;
                }
                window.__settingsV3 = true;

                const THEMES = {
                  blue:{fr:'Bleu',en:'Blue',de:'Blau',a:'#0877f9',d:'#075fae',s:'#d9eafb',bg:'#f6f8fb',ink:'#111936',m:'#68738a',line:'#dce3eb'},
                  teal:{fr:'Turquoise',en:'Teal',de:'Türkis',a:'#00897b',d:'#00695c',s:'#ddf4f0',bg:'#f5f9f8',ink:'#102b28',m:'#60736f',line:'#d7e6e3'},
                  violet:{fr:'Violet',en:'Violet',de:'Violett',a:'#6750a4',d:'#4f378b',s:'#ede7f6',bg:'#f8f6fb',ink:'#241b35',m:'#6c6476',line:'#e1dbe9'},
                  green:{fr:'Vert',en:'Green',de:'Grün',a:'#2e7d32',d:'#1b5e20',s:'#e3f3e4',bg:'#f6faf6',ink:'#172b19',m:'#617162',line:'#d9e7da'},
                  amber:{fr:'Ambre',en:'Amber',de:'Amber',a:'#ef6c00',d:'#bf4e00',s:'#ffebd8',bg:'#fff9f4',ink:'#352015',m:'#75685e',line:'#eadfd5'}
                };

                const TXT = {
                  fr:{title:'Emploi du temps',settings:'Réglages',appFont:'Taille du texte de l’application',widgetFont:'Taille du texte du widget',language:'Langue',theme:'Thème',appPreview:'Aperçu application',widgetPreview:'Aperçu widget',reset:'Réinitialiser',close:'Fermer',thisWeek:'Cette semaine',week:'Semaine',today:'Aujourd’hui',edit:'Modifier',progress:'Avancement',importPhoto:'Importer une photo d’emploi du temps',breakNames:'Noms des interruptions',slots:'Horaires des 9 heures',add:'Ajouter un cours',gap:'Trou',lunch:'Pause de midi',badge:'Badge',weekPreview:'Aperçu semaine',period:'Heure de cours',group:'Classe / groupe',room:'Salle',delete:'Supprimer',cancel:'Annuler',save:'Enregistrer',current:'En cours',courses:'cours',empty:'Aucun cours aujourd’hui.'},
                  en:{title:'Timetable',settings:'Settings',appFont:'App text size',widgetFont:'Widget text size',language:'Language',theme:'Theme',appPreview:'App preview',widgetPreview:'Widget preview',reset:'Reset',close:'Close',thisWeek:'This week',week:'Week',today:'Today',edit:'Edit',progress:'Progress',importPhoto:'Import a timetable photo',breakNames:'Break names',slots:'9 period times',add:'Add a class',gap:'Free period',lunch:'Lunch break',badge:'Badge',weekPreview:'Week preview',period:'Class period',group:'Class / group',room:'Room',delete:'Delete',cancel:'Cancel',save:'Save',current:'In class',courses:'classes',empty:'No class today.'},
                  de:{title:'Stundenplan',settings:'Einstellungen',appFont:'Textgröße der App',widgetFont:'Textgröße des Widgets',language:'Sprache',theme:'Design',appPreview:'App-Vorschau',widgetPreview:'Widget-Vorschau',reset:'Zurücksetzen',close:'Schließen',thisWeek:'Diese Woche',week:'Woche',today:'Heute',edit:'Bearbeiten',progress:'Fortschritt',importPhoto:'Stundenplan-Foto importieren',breakNames:'Bezeichnungen der Unterbrechungen',slots:'Zeiten der 9 Stunden',add:'Stunde hinzufügen',gap:'Freistunde',lunch:'Mittagspause',badge:'Badge',weekPreview:'Wochenübersicht',period:'Unterrichtsstunde',group:'Klasse / Gruppe',room:'Raum',delete:'Löschen',cancel:'Abbrechen',save:'Speichern',current:'Läuft',courses:'Stunden',empty:'Heute kein Unterricht.'}
                };

                const DAYS = {
                  fr:{Lundi:'Lundi',Mardi:'Mardi',Mercredi:'Mercredi',Jeudi:'Jeudi',Vendredi:'Vendredi',Lun:'Lun',Mar:'Mar',Mer:'Mer',Jeu:'Jeu',Ven:'Ven'},
                  en:{Lundi:'Monday',Mardi:'Tuesday',Mercredi:'Wednesday',Jeudi:'Thursday',Vendredi:'Friday',Lun:'Mon',Mar:'Tue',Mer:'Wed',Jeu:'Thu',Ven:'Fri'},
                  de:{Lundi:'Montag',Mardi:'Dienstag',Mercredi:'Mittwoch',Jeudi:'Donnerstag',Vendredi:'Freitag',Lun:'Mo',Mar:'Di',Mer:'Mi',Jeu:'Do',Ven:'Fr'}
                };

                let state = {appFontScale:1,widgetFontScale:1,language:'fr',theme:'blue'};
                try {
                  const raw = window.AndroidSchedule && AndroidSchedule.loadUiSettings
                    ? AndroidSchedule.loadUiSettings()
                    : localStorage.getItem('edt-ui-settings');
                  if (raw) state = Object.assign(state, JSON.parse(raw));
                } catch(e) {}

                function language(){ return ['fr','en','de'].includes(state.language) ? state.language : 'fr'; }
                function text(){ return TXT[language()]; }
                function theme(){ return THEMES[state.theme] || THEMES.blue; }
                function clamp(v){ return Math.max(.8, Math.min(1.4, Number(v) || 1)); }
                function setText(el,value){ if(el && el.textContent !== value) el.textContent = value; }
                function persist(){
                  state.appFontScale=clamp(state.appFontScale);
                  state.widgetFontScale=clamp(state.widgetFontScale);
                  const raw=JSON.stringify(state);
                  try {
                    if(window.AndroidSchedule && AndroidSchedule.saveUiSettings) AndroidSchedule.saveUiSettings(raw);
                    else localStorage.setItem('edt-ui-settings',raw);
                  } catch(e) {}
                }

                const style=document.createElement('style');
                style.textContent=`
                  .header{position:relative!important;background:linear-gradient(135deg,var(--set-accent),var(--set-dark))!important}
                  #settingsBtn{position:absolute;right:10px;bottom:12px;width:36px;height:36px;border:0;border-radius:18px;background:#ffffff25;color:#fff;font-size:20px;padding:0;display:flex;align-items:center;justify-content:center}
                  .currentWeek{background:var(--set-soft)!important;color:var(--set-dark)!important}.currentWeek b{color:var(--set-accent)!important}
                  .weekTab.active,.btn.primary{background:var(--set-accent)!important;border-color:var(--set-accent)!important}.dayTab.active{background:linear-gradient(135deg,var(--set-accent),var(--set-dark))!important;border-color:var(--set-accent)!important}
                  .timeline>span{background:var(--set-accent)!important}.timelineHead strong,.count,.addBtn{color:var(--set-accent)!important}.addBtn{border-color:var(--set-accent)!important}.todayCourse.current:before{background:var(--set-accent)!important}.badge:not(.gap):not(.lunch){background:var(--set-accent)!important}
                  #settingsModal{position:fixed;inset:0;z-index:100;background:#0b17386b;display:none;align-items:flex-end}#settingsModal.show{display:flex}
                  #settingsSheet{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:18px 18px 0 0;padding:14px 14px calc(16px + env(safe-area-inset-bottom));max-height:88vh;overflow:auto;color:#111936}
                  .settingsHead{display:flex;align-items:center;justify-content:space-between}.settingsHead h2{margin:0;font-size:1.08rem}.settingsX{border:0;background:#eef2f6;border-radius:50%;width:32px;height:32px;font-size:18px}
                  .settingBox{border:1px solid #dde4ec;border-radius:10px;padding:10px;margin-top:9px}.settingTitle{font-weight:800;font-size:.82rem;margin-bottom:7px}.settingRow{display:flex;align-items:center;gap:8px}.settingRow input{width:100%;accent-color:var(--set-accent)}.settingValue{min-width:45px;text-align:right;font-weight:800;color:var(--set-accent);font-size:.78rem}.languageSelect{width:100%;padding:9px;border:1px solid #d8e0e8;border-radius:8px;background:#fff}
                  .themeGrid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:6px}.themeButton{border:2px solid transparent;border-radius:9px;padding:5px 3px;background:#f7f8fa}.themeButton.active{border-color:var(--set-accent);background:var(--set-soft)}.themeSwatch{display:block;height:25px;border-radius:6px}.themeName{display:block;font-size:.58rem;font-weight:800;margin-top:3px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
                  .previewGrid{display:grid;grid-template-columns:1fr 1fr;gap:8px}.previewLabel{font-size:.67rem;font-weight:800;color:#657087;margin-bottom:4px}.preview{border:1px solid #dce3eb;background:#fff;overflow:hidden}.previewAppTop{height:20px;background:linear-gradient(90deg,var(--set-accent),var(--set-dark));color:#fff;font-size:7px;font-weight:800;display:flex;align-items:center;justify-content:center}.previewBody{padding:5px}.previewLine{height:6px;border-radius:4px;background:var(--set-soft);margin:3px}.previewLine.accent{width:65%;background:var(--set-accent)}.previewWidget{background:var(--set-soft);padding:5px}.previewWidgetTitle{font-size:7px;font-weight:900;color:var(--set-ink)}.previewWidgetMeta{font-size:6px;color:var(--set-muted);margin-top:2px}.previewBar{height:4px;background:#d7e0e8;margin-top:4px}.previewBar i{display:block;width:56%;height:100%;background:var(--set-accent)}
                  .settingsActions{display:flex;justify-content:space-between;margin-top:12px}.settingsAction{padding:9px 12px;border:1px solid #d6dee7;border-radius:8px;background:#fff;font-weight:800}.settingsAction.primary{background:var(--set-accent);border-color:var(--set-accent);color:#fff}@media(max-width:380px){.themeGrid{grid-template-columns:repeat(3,1fr)}}
                `;
                document.head.appendChild(style);

                const header=document.querySelector('.header');
                if(header && !document.getElementById('settingsBtn')){
                  const button=document.createElement('button');button.id='settingsBtn';button.type='button';button.textContent='⚙';button.setAttribute('aria-label','Réglages');header.appendChild(button);
                }

                const modal=document.createElement('div');modal.id='settingsModal';
                modal.innerHTML=`<div id="settingsSheet"><div class="settingsHead"><h2 id="settingsTitle"></h2><button id="settingsX" class="settingsX" type="button">×</button></div><div class="previewGrid"><div><div id="appPreviewLabel" class="previewLabel"></div><div class="preview"><div id="previewAppTitle" class="previewAppTop"></div><div class="previewBody"><div class="previewLine accent"></div><div class="previewLine"></div><div class="previewLine"></div></div></div></div><div><div id="widgetPreviewLabel" class="previewLabel"></div><div class="preview"><div class="previewWidget"><div class="previewWidgetTitle">4G1 ALL · 4G2 ALL</div><div class="previewWidgetMeta">08:00 - 09:00</div><div class="previewBar"><i></i></div></div></div></div></div><div class="settingBox"><div id="appFontTitle" class="settingTitle"></div><div class="settingRow"><input id="appFont" type="range" min="80" max="140" step="5"><span id="appFontValue" class="settingValue"></span></div></div><div class="settingBox"><div id="widgetFontTitle" class="settingTitle"></div><div class="settingRow"><input id="widgetFont" type="range" min="80" max="140" step="5"><span id="widgetFontValue" class="settingValue"></span></div></div><div class="settingBox"><div id="languageTitle" class="settingTitle"></div><select id="languageSelect" class="languageSelect"><option value="fr">Français</option><option value="de">Deutsch</option><option value="en">English</option></select></div><div class="settingBox"><div id="themeTitle" class="settingTitle"></div><div id="themeGrid" class="themeGrid"></div></div><div class="settingsActions"><button id="settingsReset" class="settingsAction" type="button"></button><button id="settingsDone" class="settingsAction primary" type="button"></button></div></div>`;
                document.body.appendChild(modal);

                function applyTheme(){
                  const th=theme();
                  const vars={'--set-accent':th.a,'--set-dark':th.d,'--set-soft':th.s,'--set-ink':th.ink,'--set-muted':th.m,'--blue':th.a,'--blue2':th.d,'--teal':th.a,'--ink':th.ink,'--muted':th.m,'--line':th.line,'--soft':th.s,'--bg':th.bg};
                  Object.keys(vars).forEach(k=>document.documentElement.style.setProperty(k,vars[k]));
                  document.body.style.background=th.bg;
                  document.querySelectorAll('.themeButton').forEach(b=>b.classList.toggle('active',b.dataset.theme===state.theme));
                }

                function applyFont(){
                  state.appFontScale=clamp(state.appFontScale);state.widgetFontScale=clamp(state.widgetFontScale);
                  document.documentElement.style.fontSize=(16*state.appFontScale)+'px';
                  document.body.style.fontSize=(15*state.appFontScale)+'px';
                  const a=document.getElementById('appFont'),w=document.getElementById('widgetFont');
                  if(a)a.value=Math.round(state.appFontScale*100);if(w)w.value=Math.round(state.widgetFontScale*100);
                  setText(document.getElementById('appFontValue'),Math.round(state.appFontScale*100)+' %');
                  setText(document.getElementById('widgetFontValue'),Math.round(state.widgetFontScale*100)+' %');
                }

                function renderThemeButtons(){
                  const grid=document.getElementById('themeGrid');if(!grid)return;
                  Object.keys(THEMES).forEach(id=>{
                    const th=THEMES[id];let b=grid.querySelector('.themeButton[data-theme="'+id+'"]');
                    if(!b){
                      b=document.createElement('button');b.type='button';b.className='themeButton';b.dataset.theme=id;
                      const swatch=document.createElement('span');swatch.className='themeSwatch';
                      const name=document.createElement('span');name.className='themeName';b.append(swatch,name);
                      b.onclick=()=>{state.theme=id;applyTheme();persist()};grid.appendChild(b);
                    }
                    const swatch=b.querySelector('.themeSwatch'),name=b.querySelector('.themeName');
                    if(swatch)swatch.style.background='linear-gradient(135deg,'+th.a+','+th.s+')';
                    if(name&&name.textContent!==th[language()])name.textContent=th[language()];
                  });
                  applyTheme();
                }

                function translateDay(value){
                  const canonical={Lundi:'Lundi',Mardi:'Mardi',Mercredi:'Mercredi',Jeudi:'Jeudi',Vendredi:'Vendredi',Monday:'Lundi',Tuesday:'Mardi',Wednesday:'Mercredi',Thursday:'Jeudi',Friday:'Vendredi',Montag:'Lundi',Dienstag:'Mardi',Mittwoch:'Mercredi',Donnerstag:'Jeudi',Freitag:'Vendredi'};
                  const words=Object.keys(canonical);
                  let out=String(value||'');
                  for(let i=0;i<words.length;i++){const w=words[i];if(out.indexOf(w)===0){out=DAYS[language()][canonical[w]]+out.substring(w.length);break;}}
                  return out;
                }

                function localize(){
                  const t=text();
                  setText(document.querySelector('.header h1'),t.title);
                  const cw=document.getElementById('currentWeekBtn');if(cw){const l=cw.querySelector('b')?cw.querySelector('b').textContent:'A';if(window.setCurrentWeekLabel70)window.setCurrentWeekLabel70(l);else cw.textContent=t.thisWeek+' : '+l}
                  document.querySelectorAll('.weekTab').forEach(b=>setText(b,t.week+' '+b.dataset.week));
                  document.querySelectorAll('.nav').forEach(b=>{const icon=b.querySelector('span')?b.querySelector('span').outerHTML:'';const label=b.dataset.mode==='today'?t.today:(b.dataset.mode==='week'?t.week:t.edit);if(b.innerHTML!==icon+label)b.innerHTML=icon+label});
                  setText(document.querySelector('.timelineHead strong'),t.progress);
                  const imp=document.getElementById('importPhoto');if(imp&&!imp.disabled)setText(imp,t.importPhoto);
                  document.querySelectorAll('.sectionHead h3').forEach(h=>{const s=h.textContent.toLowerCase();if(s.indexOf('interrupt')>=0||s.indexOf('unterbrech')>=0||s.indexOf('noms')>=0)setText(h,t.breakNames);else if(s.indexOf('horaire')>=0||s.indexOf('zeiten')>=0||s.indexOf('period')>=0)setText(h,t.slots)});
                  setText(document.getElementById('addCourse'),'＋ '+t.add);
                  const breakNames=document.querySelectorAll('.breakName');if(breakNames[0])setText(breakNames[0],t.gap);if(breakNames[1])setText(breakNames[1],t.lunch);
                  document.querySelectorAll('.badgeToggle').forEach(el=>{const nodes=el.childNodes;for(let i=0;i<nodes.length;i++){if(nodes[i].nodeType===3){nodes[i].nodeValue=t.badge;break;}}});
                  const todayTitle=document.getElementById('todayTitle');if(todayTitle)setText(todayTitle,translateDay(todayTitle.textContent));
                  const editTitle=document.getElementById('editDayTitle');if(editTitle)setText(editTitle,translateDay(editTitle.textContent));
                  document.querySelectorAll('.wh.day').forEach(el=>{const reverse={Mon:'Lun',Tue:'Mar',Wed:'Mer',Thu:'Jeu',Fri:'Ven',Mo:'Lun',Di:'Mar',Mi:'Mer',Do:'Jeu',Fr:'Ven'};const base=reverse[el.textContent]||el.textContent;if(DAYS[language()][base])setText(el,DAYS[language()][base])});
                  document.querySelectorAll('.todayCourse.current .badge').forEach(el=>setText(el,t.current));
                  document.querySelectorAll('.todayCourse.gap .label,.gapCell .cellLabel').forEach(el=>setText(el,t.gap));
                  document.querySelectorAll('.todayCourse.lunch .label,.lunchCell .cellLabel').forEach(el=>setText(el,t.lunch));
                  const weekHead=document.querySelector('.weekTop h2');if(weekHead){const letter=document.getElementById('weekTitleLetter')?document.getElementById('weekTitleLetter').textContent:'A';weekHead.innerHTML=t.weekPreview+' <span id="weekTitleLetter" class="weekLetter">'+letter+'</span>'}
                  const count=document.getElementById('editCount');if(count){const n=parseInt(count.textContent,10)||0;setText(count,n+' '+t.courses)}
                  const empty=document.querySelector('#todayList .empty');if(empty)setText(empty,t.empty);
                  const labels=document.querySelectorAll('#courseForm .field label');if(labels[0])setText(labels[0],t.period);if(labels[1])setText(labels[1],t.group);if(labels[2])setText(labels[2],t.room);
                  setText(document.getElementById('deleteCourse'),t.delete);setText(document.getElementById('cancelEdit'),t.cancel);setText(document.querySelector('#courseForm button[type=submit]'),t.save);
                  renderSettingsText();
                }

                function renderSettingsText(){
                  const t=text();setText(document.getElementById('settingsTitle'),t.settings);setText(document.getElementById('appPreviewLabel'),t.appPreview);setText(document.getElementById('widgetPreviewLabel'),t.widgetPreview);setText(document.getElementById('previewAppTitle'),t.title);setText(document.getElementById('appFontTitle'),t.appFont);setText(document.getElementById('widgetFontTitle'),t.widgetFont);setText(document.getElementById('languageTitle'),t.language);setText(document.getElementById('themeTitle'),t.theme);setText(document.getElementById('settingsReset'),t.reset);setText(document.getElementById('settingsDone'),t.close);
                }

                function refresh(){ applyTheme();applyFont();localize(); }
                function wrap(name){}
                ['render','renderToday','renderWeek','renderEdit','renderContext','openEditor'].forEach(wrap);

                document.getElementById('settingsBtn').onclick=()=>{modal.classList.add('show');document.getElementById('languageSelect').value=language();renderThemeButtons();applyFont();renderSettingsText()};
                document.getElementById('settingsX').onclick=()=>modal.classList.remove('show');document.getElementById('settingsDone').onclick=()=>modal.classList.remove('show');modal.onclick=e=>{if(e.target===modal)modal.classList.remove('show')};
                document.getElementById('appFont').oninput=e=>{state.appFontScale=Number(e.target.value)/100;applyFont();persist()};
                document.getElementById('widgetFont').oninput=e=>{state.widgetFontScale=Number(e.target.value)/100;applyFont();persist()};
                document.getElementById('languageSelect').onchange=e=>{state.language=e.target.value;renderThemeButtons();refresh();persist()};
                document.getElementById('settingsReset').onclick=()=>{state={appFontScale:1,widgetFontScale:1,language:'fr',theme:'blue'};document.getElementById('languageSelect').value='fr';renderThemeButtons();refresh();persist()};

                window.refreshSettingsV3=refresh;
                renderThemeButtons();refresh();
              } catch(e) { console.log('Settings V3',e); }
            })();
            """;
    }

}
