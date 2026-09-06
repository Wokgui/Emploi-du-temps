package com.wokgui.schedulewidget;

final class AdvancedFeaturesUi {
    private AdvancedFeaturesUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__advancedFeaturesV1){
                  if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                  return;
                }
                window.__advancedFeaturesV1=true;

                const A_TXT={
                  fr:{widgetDisplay:'Affichage du widget',density:'Densité',format:'Format',following:'Cours suivants',auto:'Automatique',compact:'Compact',normal:'Normal',comfortable:'Confortable',timeline:'Chronologie de la journée',nowNext:'Maintenant + prochain',room:'Salle',times:'Horaires',remaining:'Temps restant',percent:'Pourcentage',progress:'Barre de progression',gaps:'Trous',lunch:'Pause de midi',weekInfo:'Semaine et cycle',classColors:'Couleur par classe',access:'Accessibilité',accessNormal:'Normale',contrast:'Contraste élevé',colorblind:'Palette daltonisme',cycle:'Cycle de semaines',cycle2:'2 semaines (A/B)',cycle3:'3 semaines (A/B/C)',cycle4:'4 semaines (A/B/C/D)',copyWeek:'Copier la semaine active vers la suivante',copyDay:'Copier un jour',from:'De',to:'Vers',copy:'Copier',reminders:'Rappels',notify:'Notifier avant le cours',calendar:'Vacances et jours sans cours',holidays:'Jours fériés automatiques',holOff:'Désactivés',holFrance:'France',holAlsace:'Alsace-Moselle',addRange:'Ajouter une période sans cours',label:'Libellé',add:'Ajouter',exceptions:'Modifications exceptionnelles',addException:'Ajouter une exception',noException:'Aucune exception',cancelCourse:'Cours annulé',roomChange:'Changement de salle',moveCourse:'Cours déplacé',extraCourse:'Cours / réunion exceptionnel',date:'Date',referenceStart:'Heure du cours concerné',referenceLabel:'Classe / libellé du cours',newStart:'Nouvelle heure de début',newEnd:'Nouvelle heure de fin',newRoom:'Salle',newLabel:'Nouveau libellé',save:'Enregistrer',delete:'Supprimer',profiles:'Profils',newProfile:'Nouveau profil',rename:'Renommer',backup:'Sauvegarde',share:'Partager la sauvegarde',restore:'Restaurer une sauvegarde',blank:'Créer vide ?',dayOff:'Jour sans cours',uncertain:'À vérifier',importOk:'Sauvegarde restaurée.',importFail:'Ce fichier ne contient pas une sauvegarde valide.',duplicateQuestion:'Dupliquer le profil actuel ?'},
                  en:{widgetDisplay:'Widget display',density:'Density',format:'Format',following:'Following classes',auto:'Automatic',compact:'Compact',normal:'Normal',comfortable:'Comfortable',timeline:'Day timeline',nowNext:'Now + next',room:'Room',times:'Times',remaining:'Time left',percent:'Percentage',progress:'Progress bar',gaps:'Free periods',lunch:'Lunch break',weekInfo:'Week and cycle',classColors:'Color by class',access:'Accessibility',accessNormal:'Normal',contrast:'High contrast',colorblind:'Color-blind palette',cycle:'Week cycle',cycle2:'2 weeks (A/B)',cycle3:'3 weeks (A/B/C)',cycle4:'4 weeks (A/B/C/D)',copyWeek:'Copy active week to the next one',copyDay:'Copy a day',from:'From',to:'To',copy:'Copy',reminders:'Reminders',notify:'Notify before class',calendar:'Holidays and days off',holidays:'Automatic public holidays',holOff:'Off',holFrance:'France',holAlsace:'Alsace-Moselle',addRange:'Add a period with no classes',label:'Label',add:'Add',exceptions:'One-off changes',addException:'Add an exception',noException:'No exception',cancelCourse:'Cancelled class',roomChange:'Room change',moveCourse:'Moved class',extraCourse:'Extra class / meeting',date:'Date',referenceStart:'Original class start',referenceLabel:'Class / original label',newStart:'New start',newEnd:'New end',newRoom:'Room',newLabel:'New label',save:'Save',delete:'Delete',profiles:'Profiles',newProfile:'New profile',rename:'Rename',backup:'Backup',share:'Share backup',restore:'Restore backup',blank:'Create blank?',dayOff:'No class',uncertain:'Check',importOk:'Backup restored.',importFail:'This file is not a valid backup.',duplicateQuestion:'Duplicate current profile?'},
                  de:{widgetDisplay:'Widget-Anzeige',density:'Dichte',format:'Format',following:'Folgende Stunden',auto:'Automatisch',compact:'Kompakt',normal:'Normal',comfortable:'Komfortabel',timeline:'Tagesverlauf',nowNext:'Jetzt + nächste Stunde',room:'Raum',times:'Zeiten',remaining:'Restzeit',percent:'Prozent',progress:'Fortschrittsbalken',gaps:'Freistunden',lunch:'Mittagspause',weekInfo:'Woche und Zyklus',classColors:'Farbe je Klasse',access:'Barrierefreiheit',accessNormal:'Normal',contrast:'Hoher Kontrast',colorblind:'Farbenblind-Palette',cycle:'Wochenzyklus',cycle2:'2 Wochen (A/B)',cycle3:'3 Wochen (A/B/C)',cycle4:'4 Wochen (A/B/C/D)',copyWeek:'Aktive Woche in die nächste kopieren',copyDay:'Tag kopieren',from:'Von',to:'Nach',copy:'Kopieren',reminders:'Erinnerungen',notify:'Vor dem Unterricht erinnern',calendar:'Ferien und unterrichtsfreie Tage',holidays:'Automatische Feiertage',holOff:'Aus',holFrance:'Frankreich',holAlsace:'Elsass-Mosel',addRange:'Unterrichtsfreie Zeit hinzufügen',label:'Bezeichnung',add:'Hinzufügen',exceptions:'Einmalige Änderungen',addException:'Ausnahme hinzufügen',noException:'Keine Ausnahme',cancelCourse:'Stunde fällt aus',roomChange:'Raumänderung',moveCourse:'Stunde verlegt',extraCourse:'Zusätzliche Stunde / Besprechung',date:'Datum',referenceStart:'Beginn der betroffenen Stunde',referenceLabel:'Klasse / ursprüngliche Bezeichnung',newStart:'Neuer Beginn',newEnd:'Neues Ende',newRoom:'Raum',newLabel:'Neue Bezeichnung',save:'Speichern',delete:'Löschen',profiles:'Profile',newProfile:'Neues Profil',rename:'Umbenennen',backup:'Sicherung',share:'Sicherung teilen',restore:'Sicherung wiederherstellen',blank:'Leer erstellen?',dayOff:'Unterrichtsfrei',uncertain:'Prüfen',importOk:'Sicherung wiederhergestellt.',importFail:'Diese Datei ist keine gültige Sicherung.',duplicateQuestion:'Aktuelles Profil duplizieren?'}
                };

                let adv={density:'normal',upcomingCount:0,widgetFormat:'timeline',showRoom:true,showTimes:true,showRemaining:true,showPercent:true,showProgress:true,showBreaks:true,showLunch:true,showWeekInfo:true,colorByClass:false,accessibility:'normal',cycleLength:2,remindersEnabled:false,reminderMinutes:10,holidayMode:'alsace_moselle',exceptions:[],dayOffRanges:[]};
                function uiLang(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():null;if(raw){const o=JSON.parse(raw);if(o.language==='en'||o.language==='de')return o.language}}catch(e){}
                  return 'fr';
                }
                function T(){return A_TXT[uiLang()]||A_TXT.fr}
                function loadAdv(){
                  try{const raw=window.AndroidSchedule&&AndroidSchedule.loadAdvancedSettings?AndroidSchedule.loadAdvancedSettings():localStorage.getItem('edt-advanced');if(raw)adv=Object.assign(adv,JSON.parse(raw))}catch(e){}
                  if(!Array.isArray(adv.exceptions))adv.exceptions=[];if(!Array.isArray(adv.dayOffRanges))adv.dayOffRanges=[];adv.cycleLength=Math.max(2,Math.min(4,Number(adv.cycleLength)||2));
                }
                function saveAdv(){
                  try{const raw=JSON.stringify(adv);if(window.AndroidSchedule&&AndroidSchedule.saveAdvancedSettings)AndroidSchedule.saveAdvancedSettings(raw);else localStorage.setItem('edt-advanced',raw)}catch(e){}
                  applyAppAppearance();
                }
                loadAdv();

                function deep(o){return JSON.parse(JSON.stringify(o))}
                function emptyWeek(){const w={};for(const d of [2,3,4,5,6])w[d]={enabled:true,courses:[]};return w}
                function ensureExtraWeeks(){
                  if(typeof weeks==='undefined')return;
                  if(!weeks.C)weeks.C=weeks.A?deep(weeks.A):emptyWeek();
                  if(!weeks.D)weeks.D=weeks.B?deep(weeks.B):emptyWeek();
                }
                ensureExtraWeeks();

                function restoreCycleFromNative(){
                  try{
                    if(!(window.AndroidSchedule&&AndroidSchedule.loadSchedule))return;
                    const root=JSON.parse(AndroidSchedule.loadSchedule()||'{}');
                    ensureExtraWeeks();
                    if(root._weeks){for(const w of ['C','D'])if(root._weeks[w])weeks[w]=root._weeks[w]}
                    if(root._cycleLength)adv.cycleLength=Math.max(2,Math.min(4,Number(root._cycleLength)||adv.cycleLength));
                    const allowed=['A','B','C','D'].slice(0,adv.cycleLength);
                    if(allowed.indexOf(root._currentWeek)>=0)currentWeek=root._currentWeek;
                    if(allowed.indexOf(activeWeek)<0)activeWeek=currentWeek;
                  }catch(e){}
                  buildWeekTabs();
                }
                restoreCycleFromNative();

                if(typeof window.normalize==='function'&&!window.normalize.__advanced){
                  const oldNormalize=window.normalize;
                  const wrapped=function(){ensureExtraWeeks();oldNormalize();if(typeof normalizeWeek==='function'){normalizeWeek(weeks.C);normalizeWeek(weeks.D)}state=weeks[mode==='today'?currentWeek:activeWeek]};
                  wrapped.__advanced=true;window.normalize=wrapped;
                }
                if(typeof window.exportState==='function'&&!window.exportState.__advanced){
                  const oldExport=window.exportState;
                  const wrapped=function(){ensureExtraWeeks();const root=oldExport();if(!root._weeks)root._weeks={};root._weeks.C=deep(weeks.C);root._weeks.D=deep(weeks.D);root._currentWeek=currentWeek;root._cycleLength=adv.cycleLength;return root};
                  wrapped.__advanced=true;window.exportState=wrapped;
                }
                if(typeof window.reloadSchedule==='function'&&!window.reloadSchedule.__advanced){
                  const oldReload=window.reloadSchedule;
                  const wrapped=function(){oldReload();restoreCycleFromNative();if(typeof render==='function')render();setTimeout(refreshAdvancedFeatures,0)};
                  wrapped.__advanced=true;window.reloadSchedule=wrapped;
                }

                function letters(){return ['A','B','C','D'].slice(0,adv.cycleLength)}
                function buildWeekTabs(){
                  const box=document.getElementById('weekTabs');if(!box||typeof activeWeek==='undefined')return;
                  ensureExtraWeeks();const list=letters();if(list.indexOf(activeWeek)<0)activeWeek=currentWeek;if(list.indexOf(currentWeek)<0)currentWeek='A';box.innerHTML='';
                  for(const w of list){const b=document.createElement('button');b.className='weekTab'+(w===activeWeek?' active':'');b.dataset.week=w;b.textContent=(uiLang()==='de'?'Woche ':(uiLang()==='en'?'Week ':'Semaine '))+w;b.onclick=()=>{activeWeek=w;if(typeof render==='function')render();setTimeout(refreshAdvancedFeatures,0)};box.appendChild(b)}
                  const cw=document.getElementById('currentWeekBtn');if(cw){cw.onclick=()=>{const ls=letters();const idx=ls.indexOf(currentWeek);currentWeek=ls[(idx+1)%ls.length];activeWeek=currentWeek;if(typeof save==='function')save();if(window.AndroidSchedule&&AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(currentWeek);buildWeekTabs();setTimeout(refreshAdvancedFeatures,0)}}
                }

                const st=document.createElement('style');
                st.textContent=`
                  .advRow{display:flex;align-items:center;justify-content:space-between;gap:8px;margin:6px 0;font-size:.76rem}.advRow select,.advRow input[type=date],.advRow input[type=time],.advRow input[type=text]{min-width:0;border:1px solid #d8e0e8;border-radius:7px;padding:7px;background:#fff;color:inherit}.advCheck{display:flex;align-items:center;gap:7px;margin:6px 0;font-size:.76rem}.advCheck input{width:17px;height:17px;accent-color:var(--set-accent)}.advButtons{display:flex;gap:6px;flex-wrap:wrap}.advButton{border:1px solid #d6dee7;border-radius:7px;padding:7px 9px;background:#fff;color:#233047;font-weight:750;font-size:.72rem}.advButton.primary{background:var(--set-accent);border-color:var(--set-accent);color:#fff}.advButton.danger{color:#c72c4a;background:#fff6f8;border-color:#efbdc8}.advSmall{font-size:.67rem;color:#657087}.advList{margin-top:6px;border-top:1px solid #edf0f4}.advItem{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:6px 0;border-bottom:1px solid #edf0f4;font-size:.70rem}.advItemText{min-width:0;overflow:hidden;text-overflow:ellipsis}.advInline{display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-top:6px}.advInline.full{grid-template-columns:1fr}.advSectionTitle{font-size:.72rem;font-weight:850;color:var(--set-dark);margin-top:7px}.ocrUncertain{outline:2px solid #e69f00!important;outline-offset:-2px}.ocrFlag{display:inline-block;margin-left:6px;font-size:.60rem;font-weight:850;color:#a05c00;background:#fff0c7;border-radius:999px;padding:2px 5px}.exceptionBanner{margin:0 0 6px;padding:6px 8px;border:1px solid #f0d39a;background:#fff8e7;border-radius:7px;color:#725019;font-size:.72rem}.classTint{border-left:4px solid var(--class-color)!important}.accessHigh body{background:#fff!important;color:#000!important}.accessHigh .card,.accessHigh #settingsSheet{border-color:#666!important}.accessHigh .room,.accessHigh .time,.accessHigh .date{color:#222!important}.accessHigh .todayCourse.gap,.accessHigh .wc.gapCell{background:#f3f0ff!important}.accessHigh .todayCourse.lunch,.accessHigh .wc.lunchCell{background:#fff3dd!important}
                  #advModal{z-index:120}.advField{margin-bottom:8px}.advField label{display:block;color:#68738a;font-size:.70rem;margin-bottom:3px}.advField input,.advField select{width:100%;border:1px solid #d8e0e8;border-radius:7px;padding:9px;background:#fff}.advModalActions{display:flex;justify-content:flex-end;gap:7px;margin-top:10px}
                `;
                document.head.appendChild(st);

                function classColor(label){
                  const std=['#0877f9','#00897b','#6750a4','#2e7d32','#ef6c00','#c2185b'];
                  const cb=['#0072b2','#e69f00','#009e73','#cc79a7','#d55e00','#56b4e9'];
                  const p=adv.accessibility==='colorblind'?cb:std;let h=0;const s=String(label||'');for(let i=0;i<s.length;i++)h=((h*31)+s.charCodeAt(i))>>>0;return p[h%p.length]
                }
                function applyAppAppearance(){
                  document.documentElement.classList.toggle('accessHigh',adv.accessibility==='high_contrast');
                  if(!adv.colorByClass){document.querySelectorAll('.classTint').forEach(el=>{el.classList.remove('classTint');el.style.removeProperty('--class-color')});return}
                  document.querySelectorAll('.todayCourse').forEach(el=>{if(el.classList.contains('gap')||el.classList.contains('lunch'))return;const label=el.querySelector('.label');if(label){el.classList.add('classTint');el.style.setProperty('--class-color',classColor(label.textContent))}});
                  document.querySelectorAll('.wc.has').forEach(el=>{const label=el.querySelector('.cellLabel');if(label){el.classList.add('classTint');el.style.setProperty('--class-color',classColor(label.textContent))}});
                  document.querySelectorAll('.editCourse').forEach(el=>{const label=el.querySelector('.label');if(label){el.classList.add('classTint');el.style.setProperty('--class-color',classColor(label.textContent))}});
                }

                function dateKey(d){const y=d.getFullYear(),m=String(d.getMonth()+1).padStart(2,'0'),day=String(d.getDate()).padStart(2,'0');return y+'-'+m+'-'+day}
                function todayDay(){const d=new Date().getDay();return d>=1&&d<=5?d+1:2}
                function effectiveToday(){
                  try{
                    if(!(window.AndroidSchedule&&AndroidSchedule.loadEffectiveCourses))return null;
                    return JSON.parse(AndroidSchedule.loadEffectiveCourses(dateKey(new Date()))||'{}');
                  }catch(e){return null}
                }
                function renderEffectiveToday(){
                  if(typeof mode==='undefined'||mode!=='today')return;
                  const eff=effectiveToday();if(!eff)return;
                  const box=document.getElementById('todayList');if(!box)return;
                  const list=Array.isArray(eff.courses)?eff.courses:[];
                  if(eff.dayOff){box.innerHTML='<div class="empty">'+T().dayOff+'</div>';const p=document.getElementById('todayProgress');if(p)p.style.width='0%';return}
                  if(!list.length){box.innerHTML='<div class="empty">'+(uiLang()==='de'?'Heute kein Unterricht.':(uiLang()==='en'?'No class today.':'Aucun cours aujourd’hui.'))+'</div>';return}
                  const now=new Date(),nowM=now.getHours()*60+now.getMinutes();
                  const events=list.map(c=>({type:'course',start:min(c.start),end:min(c.end),course:c}));
                  const gaps=gapSegments(list);for(const g of gaps)events.push({type:'gap',start:g.start,end:g.end});
                  const l=lunch();if(l&&adv.showLunch&&list.some(c=>min(c.end)<=min(l.start))&&list.some(c=>min(c.start)>=min(l.end)))events.push({type:'lunch',start:min(l.start),end:min(l.end),l:l});
                  events.sort((a,b)=>a.start-b.start||a.end-b.end);box.innerHTML='';
                  let total=0,done=0;for(const c of list){const s=min(c.start),e=min(c.end),dur=Math.max(0,e-s);total+=dur;if(nowM>=e)done+=dur;else if(nowM>s)done+=Math.min(dur,nowM-s)}const pr=document.getElementById('todayProgress');if(pr)pr.style.width=(total?Math.max(0,Math.min(100,done*100/total)):0)+'%';
                  for(const ev of events){
                    if(ev.type==='gap'&&!adv.showBreaks)continue;
                    const row=document.createElement('div');
                    if(ev.type==='course'){
                      const c=ev.course,cur=nowM>=min(c.start)&&nowM<min(c.end);row.className='todayCourse'+(cur?' current':'')+(c.uncertain?' ocrUncertain':'');
                      const roomText=adv.showRoom?'<div class="room">'+(uiLang()==='de'?'Raum ':(uiLang()==='en'?'room ':'salle '))+esc(c.room||'—')+(c.slot?' · '+(uiLang()==='de'?'Stunde ':(uiLang()==='en'?'period ':'heure '))+c.slot:'')+'</div>':'';
                      const timeText=adv.showTimes?'<div class="time"><strong>'+esc(c.start)+'</strong><br>'+esc(c.end)+'</div>':'<div class="time"></div>';
                      row.innerHTML=timeText+'<div><div class="label">'+esc(c.label)+(c.uncertain?'<span class="ocrFlag">⚠ '+T().uncertain+'</span>':'')+'</div>'+roomText+'</div>'+(cur?'<div class="badge">'+(uiLang()==='de'?'Läuft':(uiLang()==='en'?'In class':'En cours'))+'</div>':'');
                      const base=(weeks[currentWeek]&&weeks[currentWeek][todayDay()])?weeks[currentWeek][todayDay()].courses:[];let idx=base.findIndex(x=>x.start===c.start&&x.label===c.label);if(idx<0)idx=base.findIndex(x=>x.label===c.label);if(idx>=0){const captured=idx;row.onclick=()=>{activeWeek=currentWeek;selected=todayDay();editing=captured;openEditor(captured)}}
                    }else if(ev.type==='gap'){
                      row.className='todayCourse gap';row.innerHTML='<div class="time"><strong>'+clock(ev.start)+'</strong><br>'+clock(ev.end)+'</div><div><div class="label">'+esc(breaks.gapLabel)+'</div><div class="room">'+durationLabel(ev.end-ev.start)+'</div></div>'+(breaks.showGapBadge?'<div class="badge gap">'+T().gaps+'</div>':'');
                    }else{
                      row.className='todayCourse lunch';row.innerHTML='<div class="time"><strong>'+esc(ev.l.start)+'</strong><br>'+esc(ev.l.end)+'</div><div><div class="label">'+esc(breaks.lunchLabel)+'</div><div class="room">'+(uiLang()==='de'?'Weiter um ':(uiLang()==='en'?'Back at ':'Reprise à '))+esc(ev.l.end)+'</div></div>'+(breaks.showLunchBadge?'<div class="badge lunch">'+T().lunch+'</div>':'');
                    }
                    box.appendChild(row);
                  }
                  const todays=adv.exceptions.filter(e=>e.date===dateKey(new Date()));if(todays.length){let b=document.getElementById('exceptionTodayBanner');if(!b){b=document.createElement('div');b.id='exceptionTodayBanner';b.className='exceptionBanner';box.parentNode.insertBefore(b,box)}b.textContent=todays.length+' '+(uiLang()==='de'?'Änderung(en) heute':(uiLang()==='en'?'change(s) today':'modification(s) exceptionnelle(s) aujourd’hui'))}else{const b=document.getElementById('exceptionTodayBanner');if(b)b.remove()}
                  applyAppAppearance();
                }

                function decorateUncertain(){
                  if(typeof state==='undefined'||typeof selected==='undefined')return;
                  const list=state[selected]&&state[selected].courses?state[selected].courses:[];document.querySelectorAll('.editCourse').forEach((row,i)=>{const c=list[i];row.classList.toggle('ocrUncertain',!!(c&&c.uncertain));if(c&&c.uncertain&&!row.querySelector('.ocrFlag')){const label=row.querySelector('.label');if(label){const f=document.createElement('span');f.className='ocrFlag';f.textContent='⚠ '+T().uncertain;label.appendChild(f)}}});
                }

                if(typeof window.parseOcrSchedule==='function'&&!window.parseOcrSchedule.__advanced){
                  const oldParse=window.parseOcrSchedule;const wrapped=function(payload){const r=oldParse(payload);if(r&&r.parsed){for(const d of [2,3,4,5,6])for(const c of r.parsed[d]||[]){const lab=String(c.label||'');c.uncertain=!c.room||lab.length<3||lab.length>55||lab.indexOf('�')>=0||lab.indexOf('?')>=0}}return r};wrapped.__advanced=true;window.parseOcrSchedule=wrapped;
                }

                function after(name,fn){const old=window[name];if(typeof old!=='function'||old.__advancedAfter)return;const wrapped=function(){const r=old.apply(this,arguments);setTimeout(fn,0);return r};wrapped.__advancedAfter=true;window[name]=wrapped}
                after('renderToday',()=>{renderEffectiveToday();applyAppAppearance()});
                after('renderWeek',applyAppAppearance);
                after('renderEdit',()=>{decorateUncertain();applyAppAppearance()});
                after('renderContext',buildWeekTabs);

                const sheet=document.getElementById('settingsSheet');
                const actions=document.querySelector('#settingsSheet .settingsActions');
                if(sheet&&actions&&!document.getElementById('advancedSettingsRoot')){
                  const root=document.createElement('div');root.id='advancedSettingsRoot';
                  root.innerHTML=`
                    <div class="settingBox"><div id="advWidgetTitle" class="settingTitle"></div>
                      <div class="advRow"><span id="advDensityLabel"></span><select id="advDensity"><option value="compact"></option><option value="normal"></option><option value="comfortable"></option></select></div>
                      <div class="advRow"><span id="advFormatLabel"></span><select id="advFormat"><option value="timeline"></option><option value="compact"></option></select></div>
                      <div class="advRow"><span id="advFollowingLabel"></span><select id="advFollowing"><option value="0"></option><option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option></select></div>
                      <label class="advCheck"><input id="advShowRoom" type="checkbox"><span id="advShowRoomLabel"></span></label><label class="advCheck"><input id="advShowTimes" type="checkbox"><span id="advShowTimesLabel"></span></label><label class="advCheck"><input id="advShowRemaining" type="checkbox"><span id="advShowRemainingLabel"></span></label><label class="advCheck"><input id="advShowPercent" type="checkbox"><span id="advShowPercentLabel"></span></label><label class="advCheck"><input id="advShowProgress" type="checkbox"><span id="advShowProgressLabel"></span></label><label class="advCheck"><input id="advShowBreaks" type="checkbox"><span id="advShowBreaksLabel"></span></label><label class="advCheck"><input id="advShowLunch" type="checkbox"><span id="advShowLunchLabel"></span></label><label class="advCheck"><input id="advShowWeekInfo" type="checkbox"><span id="advShowWeekInfoLabel"></span></label><label class="advCheck"><input id="advClassColors" type="checkbox"><span id="advClassColorsLabel"></span></label>
                      <div class="advRow"><span id="advAccessLabel"></span><select id="advAccess"><option value="normal"></option><option value="high_contrast"></option><option value="colorblind"></option></select></div>
                    </div>
                    <div class="settingBox"><div id="advCycleTitle" class="settingTitle"></div><div class="advRow"><span id="advCycleLabel"></span><select id="advCycle"><option value="2"></option><option value="3"></option><option value="4"></option></select></div><div class="advButtons"><button id="advCopyWeek" type="button" class="advButton"></button></div><div class="advSectionTitle" id="advCopyDayTitle"></div><div class="advInline"><select id="advCopyFrom"></select><select id="advCopyTo"></select></div><div class="advButtons" style="margin-top:6px"><button id="advCopyDay" type="button" class="advButton"></button></div></div>
                    <div class="settingBox"><div id="advReminderTitle" class="settingTitle"></div><label class="advCheck"><input id="advReminders" type="checkbox"><span id="advReminderLabel"></span></label><div class="advRow"><span></span><select id="advReminderMinutes"><option value="0">0 min</option><option value="5">5 min</option><option value="10">10 min</option><option value="15">15 min</option><option value="20">20 min</option><option value="30">30 min</option></select></div></div>
                    <div class="settingBox"><div id="advCalendarTitle" class="settingTitle"></div><div class="advRow"><span id="advHolidayLabel"></span><select id="advHoliday"><option value="off"></option><option value="france"></option><option value="alsace_moselle"></option></select></div><div class="advSectionTitle" id="advRangeTitle"></div><div class="advInline"><input id="advRangeStart" type="date"><input id="advRangeEnd" type="date"></div><div class="advInline full"><input id="advRangeLabel" type="text" maxlength="45"></div><div class="advButtons" style="margin-top:6px"><button id="advAddRange" type="button" class="advButton"></button></div><div id="advRangeList" class="advList"></div></div>
                    <div class="settingBox"><div id="advExceptionsTitle" class="settingTitle"></div><div class="advButtons"><button id="advAddException" type="button" class="advButton primary"></button></div><div id="advExceptionList" class="advList"></div></div>
                    <div class="settingBox"><div id="advProfilesTitle" class="settingTitle"></div><div class="advRow"><select id="advProfileSelect" style="width:100%"></select></div><div class="advButtons"><button id="advNewProfile" type="button" class="advButton"></button><button id="advRenameProfile" type="button" class="advButton"></button><button id="advDeleteProfile" type="button" class="advButton danger"></button></div></div>
                    <div class="settingBox"><div id="advBackupTitle" class="settingTitle"></div><div class="advButtons"><button id="advShareBackup" type="button" class="advButton"></button><button id="advRestoreBackup" type="button" class="advButton"></button></div></div>`;
                  sheet.insertBefore(root,actions);
                }

                const advModal=document.createElement('div');advModal.id='advModal';advModal.className='modal';
                advModal.innerHTML=`<form id="advExceptionForm" class="sheet"><h3 id="advExceptionFormTitle"></h3><div class="advField"><label id="advFDateLabel"></label><input id="advFDate" type="date" required></div><div class="advField"><label>Type</label><select id="advFType"><option value="cancel"></option><option value="room"></option><option value="move"></option><option value="extra"></option></select></div><div id="advReferenceFields"><div class="advField"><label id="advFRefStartLabel"></label><input id="advFRefStart" type="time"></div><div class="advField"><label id="advFRefLabelLabel"></label><input id="advFRefLabel" type="text" maxlength="80"></div></div><div id="advNewFields"><div class="advField"><label id="advFStartLabel"></label><input id="advFStart" type="time"></div><div class="advField"><label id="advFEndLabel"></label><input id="advFEnd" type="time"></div><div class="advField"><label id="advFLabelLabel"></label><input id="advFLabel" type="text" maxlength="80"></div><div class="advField"><label id="advFRoomLabel"></label><input id="advFRoom" type="text" maxlength="20"></div></div><div class="advModalActions"><button id="advFCancel" type="button" class="btn"></button><button type="submit" class="btn primary" id="advFSave"></button></div></form>`;
                document.body.appendChild(advModal);

                function setTxt(id,v){const e=document.getElementById(id);if(e)e.textContent=v}
                function locAdv(){
                  const t=T();setTxt('advWidgetTitle',t.widgetDisplay);setTxt('advDensityLabel',t.density);setTxt('advFormatLabel',t.format);setTxt('advFollowingLabel',t.following);setTxt('advShowRoomLabel',t.room);setTxt('advShowTimesLabel',t.times);setTxt('advShowRemainingLabel',t.remaining);setTxt('advShowPercentLabel',t.percent);setTxt('advShowProgressLabel',t.progress);setTxt('advShowBreaksLabel',t.gaps);setTxt('advShowLunchLabel',t.lunch);setTxt('advShowWeekInfoLabel',t.weekInfo);setTxt('advClassColorsLabel',t.classColors);setTxt('advAccessLabel',t.access);setTxt('advCycleTitle',t.cycle);setTxt('advCycleLabel',t.cycle);setTxt('advCopyWeek',t.copyWeek);setTxt('advCopyDayTitle',t.copyDay);setTxt('advCopyDay',t.copy);setTxt('advReminderTitle',t.reminders);setTxt('advReminderLabel',t.notify);setTxt('advCalendarTitle',t.calendar);setTxt('advHolidayLabel',t.holidays);setTxt('advRangeTitle',t.addRange);setTxt('advAddRange',t.add);setTxt('advExceptionsTitle',t.exceptions);setTxt('advAddException',t.addException);setTxt('advProfilesTitle',t.profiles);setTxt('advNewProfile',t.newProfile);setTxt('advRenameProfile',t.rename);setTxt('advDeleteProfile',t.delete);setTxt('advBackupTitle',t.backup);setTxt('advShareBackup',t.share);setTxt('advRestoreBackup',t.restore);setTxt('advExceptionFormTitle',t.addException);setTxt('advFDateLabel',t.date);setTxt('advFRefStartLabel',t.referenceStart);setTxt('advFRefLabelLabel',t.referenceLabel);setTxt('advFStartLabel',t.newStart);setTxt('advFEndLabel',t.newEnd);setTxt('advFLabelLabel',t.newLabel);setTxt('advFRoomLabel',t.newRoom);setTxt('advFCancel',uiLang()==='de'?'Abbrechen':(uiLang()==='en'?'Cancel':'Annuler'));setTxt('advFSave',t.save);
                  const den=document.getElementById('advDensity');if(den){den.options[0].text=t.compact;den.options[1].text=t.normal;den.options[2].text=t.comfortable}const fmt=document.getElementById('advFormat');if(fmt){fmt.options[0].text=t.timeline;fmt.options[1].text=t.nowNext}const fol=document.getElementById('advFollowing');if(fol)fol.options[0].text=t.auto;const ac=document.getElementById('advAccess');if(ac){ac.options[0].text=t.accessNormal;ac.options[1].text=t.contrast;ac.options[2].text=t.colorblind}const cyc=document.getElementById('advCycle');if(cyc){cyc.options[0].text=t.cycle2;cyc.options[1].text=t.cycle3;cyc.options[2].text=t.cycle4}const hol=document.getElementById('advHoliday');if(hol){hol.options[0].text=t.holOff;hol.options[1].text=t.holFrance;hol.options[2].text=t.holAlsace}const typ=document.getElementById('advFType');if(typ){typ.options[0].text=t.cancelCourse;typ.options[1].text=t.roomChange;typ.options[2].text=t.moveCourse;typ.options[3].text=t.extraCourse}
                  const rl=document.getElementById('advRangeLabel');if(rl)rl.placeholder=t.label;
                  renderCopyDayOptions();
                }

                function syncControls(){
                  const vals={advDensity:adv.density,advFormat:adv.widgetFormat,advFollowing:String(adv.upcomingCount||0),advAccess:adv.accessibility,advCycle:String(adv.cycleLength),advReminderMinutes:String(adv.reminderMinutes),advHoliday:adv.holidayMode};for(const id in vals){const e=document.getElementById(id);if(e)e.value=vals[id]}
                  const checks={advShowRoom:'showRoom',advShowTimes:'showTimes',advShowRemaining:'showRemaining',advShowPercent:'showPercent',advShowProgress:'showProgress',advShowBreaks:'showBreaks',advShowLunch:'showLunch',advShowWeekInfo:'showWeekInfo',advClassColors:'colorByClass',advReminders:'remindersEnabled'};for(const id in checks){const e=document.getElementById(id);if(e)e.checked=adv[checks[id]]!==false}
                }

                function renderCopyDayOptions(){const a=document.getElementById('advCopyFrom'),b=document.getElementById('advCopyTo');if(!a||!b)return;const names=uiLang()==='de'?['Montag','Dienstag','Mittwoch','Donnerstag','Freitag']:(uiLang()==='en'?['Monday','Tuesday','Wednesday','Thursday','Friday']:['Lundi','Mardi','Mercredi','Jeudi','Vendredi']);const oldA=a.value||String(typeof selected==='number'?selected:2),oldB=b.value||'3';a.innerHTML='';b.innerHTML='';[2,3,4,5,6].forEach((d,i)=>{for(const el of [a,b]){const o=document.createElement('option');o.value=String(d);o.textContent=names[i];el.appendChild(o)}});a.value=oldA;b.value=oldB}

                function renderRanges(){const box=document.getElementById('advRangeList');if(!box)return;box.innerHTML='';adv.dayOffRanges.forEach((r,i)=>{const row=document.createElement('div');row.className='advItem';row.innerHTML='<div class="advItemText"><b>'+esc(r.label||T().dayOff)+'</b><br><span class="advSmall">'+esc(r.start||'')+' → '+esc(r.end||r.start||'')+'</span></div><button type="button" class="advButton danger">×</button>';row.querySelector('button').onclick=()=>{adv.dayOffRanges.splice(i,1);saveAdv();renderRanges();if(typeof render==='function')render()};box.appendChild(row)});}
                function exceptionTypeName(type){const t=T();if(type==='cancel')return t.cancelCourse;if(type==='room')return t.roomChange;if(type==='move')return t.moveCourse;return t.extraCourse}
                function renderExceptions(){const box=document.getElementById('advExceptionList');if(!box)return;box.innerHTML='';if(!adv.exceptions.length){box.innerHTML='<div class="advSmall" style="padding:7px 0">'+T().noException+'</div>';return}adv.exceptions.slice().sort((a,b)=>String(a.date).localeCompare(String(b.date))).forEach(e=>{const realIndex=adv.exceptions.indexOf(e);const row=document.createElement('div');row.className='advItem';const detail=e.type==='extra'?(e.start+' · '+(e.label||'')):((e.refStart||'')+' · '+(e.refLabel||''));row.innerHTML='<div class="advItemText"><b>'+esc(e.date||'')+' · '+esc(exceptionTypeName(e.type))+'</b><br><span class="advSmall">'+esc(detail)+'</span></div><button type="button" class="advButton danger">×</button>';row.querySelector('button').onclick=()=>{adv.exceptions.splice(realIndex,1);saveAdv();renderExceptions();if(typeof render==='function')render()};box.appendChild(row)});}

                function openExceptionForm(){const now=new Date();document.getElementById('advFDate').value=dateKey(now);document.getElementById('advFType').value='cancel';document.getElementById('advFRefStart').value='';document.getElementById('advFRefLabel').value='';document.getElementById('advFStart').value='';document.getElementById('advFEnd').value='';document.getElementById('advFLabel').value='';document.getElementById('advFRoom').value='';updateExceptionFields();advModal.classList.add('show')}
                function updateExceptionFields(){const type=document.getElementById('advFType').value;document.getElementById('advReferenceFields').style.display=type==='extra'?'none':'block';document.getElementById('advNewFields').style.display=(type==='cancel'?'none':'block');const lab=document.getElementById('advFStartLabel');if(lab)lab.textContent=type==='room'?T().referenceStart:T().newStart}
                document.getElementById('advFType').onchange=updateExceptionFields;document.getElementById('advFCancel').onclick=()=>advModal.classList.remove('show');advModal.onclick=e=>{if(e.target===advModal)advModal.classList.remove('show')};
                document.getElementById('advExceptionForm').onsubmit=e=>{e.preventDefault();const type=document.getElementById('advFType').value;const obj={date:document.getElementById('advFDate').value,type:type,refStart:document.getElementById('advFRefStart').value,refLabel:document.getElementById('advFRefLabel').value.trim(),start:document.getElementById('advFStart').value,end:document.getElementById('advFEnd').value,label:document.getElementById('advFLabel').value.trim(),room:document.getElementById('advFRoom').value.trim()};if(type==='room'){obj.room=document.getElementById('advFRoom').value.trim()}adv.exceptions.push(obj);saveAdv();advModal.classList.remove('show');renderExceptions();if(typeof render==='function')render()};

                function loadProfiles(){
                  const sel=document.getElementById('advProfileSelect');if(!sel)return;try{const root=JSON.parse(window.AndroidSchedule&&AndroidSchedule.listProfiles?AndroidSchedule.listProfiles():'{}');sel.innerHTML='';for(const p of root.profiles||[]){const o=document.createElement('option');o.value=p.id;o.textContent=p.name;sel.appendChild(o)}sel.value=root.current||''}catch(e){}
                }
                function activateProfile(id){if(!(window.AndroidSchedule&&AndroidSchedule.activateProfile))return;AndroidSchedule.activateProfile(id);if(window.reloadSchedule)window.reloadSchedule();loadProfiles();setTimeout(refreshAdvancedFeatures,0)}

                function bind(){
                  const selectMap={advDensity:'density',advFormat:'widgetFormat',advAccess:'accessibility',advHoliday:'holidayMode'};for(const id in selectMap){const el=document.getElementById(id);if(el)el.onchange=e=>{adv[selectMap[id]]=e.target.value;saveAdv();if(typeof render==='function')render()}}
                  const numeric={advFollowing:'upcomingCount',advCycle:'cycleLength',advReminderMinutes:'reminderMinutes'};for(const id in numeric){const el=document.getElementById(id);if(el)el.onchange=e=>{adv[numeric[id]]=Number(e.target.value);saveAdv();if(id==='advCycle'){ensureExtraWeeks();if(letters().indexOf(currentWeek)<0)currentWeek='A';if(letters().indexOf(activeWeek)<0)activeWeek=currentWeek;buildWeekTabs();if(typeof save==='function')save()}if(typeof render==='function')render()}}
                  const checks={advShowRoom:'showRoom',advShowTimes:'showTimes',advShowRemaining:'showRemaining',advShowPercent:'showPercent',advShowProgress:'showProgress',advShowBreaks:'showBreaks',advShowLunch:'showLunch',advShowWeekInfo:'showWeekInfo',advClassColors:'colorByClass',advReminders:'remindersEnabled'};for(const id in checks){const el=document.getElementById(id);if(el)el.onchange=e=>{adv[checks[id]]=e.target.checked;saveAdv();if(typeof render==='function')render()}}
                  const cw=document.getElementById('advCopyWeek');if(cw)cw.onclick=()=>{ensureExtraWeeks();const ls=letters(),i=ls.indexOf(activeWeek),target=ls[(i+1)%ls.length];if(target===activeWeek)return;weeks[target]=deep(weeks[activeWeek]);if(typeof save==='function')save();buildWeekTabs()};
                  const cd=document.getElementById('advCopyDay');if(cd)cd.onclick=()=>{const from=Number(document.getElementById('advCopyFrom').value),to=Number(document.getElementById('advCopyTo').value);if(from===to)return;weeks[activeWeek][to]=deep(weeks[activeWeek][from]);if(typeof save==='function')save();if(typeof render==='function')render()};
                  const ar=document.getElementById('advAddRange');if(ar)ar.onclick=()=>{const s=document.getElementById('advRangeStart').value,e=document.getElementById('advRangeEnd').value||s,l=document.getElementById('advRangeLabel').value.trim();if(!s)return;adv.dayOffRanges.push({start:s,end:e,label:l});saveAdv();renderRanges()};
                  const ae=document.getElementById('advAddException');if(ae)ae.onclick=openExceptionForm;
                  const ps=document.getElementById('advProfileSelect');if(ps)ps.onchange=e=>activateProfile(e.target.value);
                  const np=document.getElementById('advNewProfile');if(np)np.onclick=()=>{if(!(window.AndroidSchedule&&AndroidSchedule.createProfile))return;const name=prompt(T().newProfile);if(!name)return;const dup=confirm(T().duplicateQuestion);const id=AndroidSchedule.createProfile(name,dup);loadProfiles();activateProfile(id)};
                  const rp=document.getElementById('advRenameProfile');if(rp)rp.onclick=()=>{const sel=document.getElementById('advProfileSelect');if(!sel||!sel.value)return;const name=prompt(T().rename,sel.options[sel.selectedIndex]?sel.options[sel.selectedIndex].text:'');if(name&&window.AndroidSchedule&&AndroidSchedule.renameProfile){AndroidSchedule.renameProfile(sel.value,name);loadProfiles()}};
                  const dp=document.getElementById('advDeleteProfile');if(dp)dp.onclick=()=>{const sel=document.getElementById('advProfileSelect');if(!sel||!sel.value||!(window.AndroidSchedule&&AndroidSchedule.deleteProfile))return;if(confirm(T().delete+' ?')){AndroidSchedule.deleteProfile(sel.value);loadProfiles();if(window.reloadSchedule)window.reloadSchedule()}};
                  const sb=document.getElementById('advShareBackup');if(sb)sb.onclick=()=>{if(window.AndroidSchedule&&AndroidSchedule.shareBackup)AndroidSchedule.shareBackup()};const rb=document.getElementById('advRestoreBackup');if(rb)rb.onclick=()=>{if(window.AndroidSchedule&&AndroidSchedule.pickBackup)AndroidSchedule.pickBackup()};
                }
                bind();

                window.applyBackupImported=ok=>{alert(ok?T().importOk:T().importFail);if(ok&&window.reloadSchedule)window.reloadSchedule()};

                function refreshAdvancedFeatures(){
                  loadAdv();restoreCycleFromNative();locAdv();syncControls();renderRanges();renderExceptions();loadProfiles();buildWeekTabs();decorateUncertain();renderEffectiveToday();applyAppAppearance();
                }
                window.refreshAdvancedFeatures=refreshAdvancedFeatures;

                const oldSettingsClick=document.getElementById('settingsBtn')?document.getElementById('settingsBtn').onclick:null;
                if(document.getElementById('settingsBtn'))document.getElementById('settingsBtn').onclick=function(e){if(oldSettingsClick)oldSettingsClick.call(this,e);setTimeout(refreshAdvancedFeatures,0)};

                refreshAdvancedFeatures();
              } catch(e) { console.log('Advanced features',e); }
            })();
            """;
    }
}
