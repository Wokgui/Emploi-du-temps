package com.wokgui.schedulewidget;

/** 7.67 final owner for display settings, language consistency and the global reset. */
final class Feedback767Ui {
    private Feedback767Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback767){window.refreshFeedback767&&window.refreshFeedback767();return}
                window.__feedback767=true;
                const VERSION='7.67';
                let scheduled=false;
                function language(){
                  const selected=document.getElementById('languageSelect')?.value;
                  if(selected)return String(selected).toLowerCase().startsWith('de')?'de':(String(selected).toLowerCase().startsWith('en')?'en':'fr');
                  try{const l=String(JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr').toLowerCase();return l.startsWith('de')?'de':(l.startsWith('en')?'en':'fr')}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=language();return l==='de'?de:(l==='en'?en:fr)}
                function read(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function write(value){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(value))}catch(e){try{localStorage.setItem('edt-advanced',JSON.stringify(value))}catch(ignore){}}}
                function text(id,value){const node=document.getElementById(id);if(node&&node.textContent!==value)node.textContent=value}

                const style=document.createElement('style');style.id='feedback767Style';style.textContent=`
                  #settingsSheet #resetText87,#settingsSheet #resetColors87,#settingsSheet #resetWidget87{display:none!important}
                  #viewEdit #importPhoto,#viewEdit #addCourse,#viewEdit #addBulkCourses{font-size:.82rem!important;line-height:1.25!important;font-weight:850!important}
                  #viewEdit #editHistoryActions86{gap:10px!important;margin-top:10px!important}
                  #viewEdit #editHistoryActions86 button{min-height:48px!important;padding:11px 14px!important;border-radius:10px!important;font-size:.86rem!important;line-height:1.2!important}
                  #widgetSettings86 #displayGeneral767,#widgetSettings86 #displayWidget767{padding:8px 0!important}
                  #widgetSettings86 #displayWidget767{border-top:1px solid #e7edf3!important}
                  #widgetSettings86 .displayTitle767{margin:0 0 9px!important;text-align:center!important;color:var(--set-dark,var(--ink,#111936))!important;font-size:.72rem!important;font-weight:850!important;line-height:1.2!important}
                  #widgetSettings86 .displayGrid767{display:grid!important;grid-template-columns:minmax(90px,1.35fr) minmax(72px,1fr) minmax(72px,1fr)!important;align-items:stretch!important;border:1px solid #dce4ee!important;border-radius:11px!important;overflow:hidden!important;background:#fff!important}
                  #widgetSettings86 .displayCell767{min-width:0!important;min-height:39px!important;padding:7px 5px!important;display:flex!important;align-items:center!important;justify-content:center!important;border-top:1px solid #e5ebf1!important;border-left:1px solid #e5ebf1!important;box-sizing:border-box!important;text-align:center!important;font-size:.68rem!important;font-weight:800!important;color:#40516a!important}
                  #widgetSettings86 .displayCell767:nth-child(-n+3){border-top:0!important}
                  #widgetSettings86 .displayCell767:nth-child(3n+1){border-left:0!important;justify-content:flex-start!important;padding-left:8px!important;text-align:left!important;color:var(--ink,#111936)!important}
                  #widgetSettings86 .displayHead767{min-height:34px!important;background:#f5f8fc!important;justify-content:center!important;text-align:center!important;color:#0877f9!important}
                  #widgetSettings86 .displayCell767 input[type=checkbox]{width:20px!important;height:20px!important;margin:0!important;accent-color:var(--set-accent,#0877f9)!important}
                  #widgetSettings86 .displayCell767 select{width:100%!important;min-width:0!important;max-width:132px!important;padding:6px 20px 6px 5px!important;border:1px solid #cfd9e5!important;border-radius:8px!important;background:#fff!important;font-size:.64rem!important;text-align:center!important;text-align-last:center!important}
                  #widgetSettings86 #displayWidget767 .advRow{margin:6px 0!important}
                  #widgetSettings86 #widgetEdgeBars672{margin:8px 0 0!important;padding:8px 0 0!important;border:0!important;border-radius:0!important;background:transparent!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Divider{display:none!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Title{font-size:.72rem!important;color:var(--set-dark,var(--ink,#111936))!important;font-weight:850!important}
                  .appHideRoom767 #viewToday .room,.appHideRoom767 #viewWeek .room,.appHideRoom767 #viewWeek .cellRoom{display:none!important}
                  .appHideTimes767 #todayList .time{visibility:hidden!important;width:0!important;min-width:0!important;overflow:hidden!important}
                  .appHideTimes767 #weekGrid .wt,.appHideTimes767 #weekGrid .wh:first-child{color:transparent!important}
                  .appHideRemaining767 #viewToday .timelineCard{display:none!important}
                  @media(max-width:370px){#widgetSettings86 .displayGrid767{grid-template-columns:minmax(82px,1.25fr) minmax(66px,1fr) minmax(66px,1fr)!important}#widgetSettings86 .displayCell767{font-size:.63rem!important;padding-left:3px!important;padding-right:3px!important}}
                `;document.head.appendChild(style);

                const rows=[
                  {name:()=>tr('Salle','Room','Raum'),app:'advAppShowRoom767',widget:'advShowRoom',appKeys:['appShowRoom'],widgetKey:'showRoom'},
                  {name:()=>tr('Horaires','Times','Zeiten'),app:'advAppShowTimes767',widget:'advShowTimes',appKeys:['appShowTimes'],widgetKey:'showTimes'},
                  {name:()=>tr('Temps restant','Time left','Restzeit'),app:'advAppShowRemaining767',widget:'advShowRemaining',appKeys:['appShowRemaining'],widgetKey:'showRemaining'},
                  {name:()=>tr('Trous','Free periods','Freistunden'),app:'advAppShowBreaks767',widget:'advShowBreaks',appKeys:['showBreaksToday','showBreaksWeek'],widgetKey:'showBreaks'},
                  {name:()=>tr('Midi','Lunch','Mittag'),app:'advAppShowLunch767',widget:'advShowLunch',appKeys:['showLunchToday','showLunchWeek'],widgetKey:'showLunch'},
                  {name:()=>tr('Couleur par classe','Color by class','Farbe je Klasse'),app:'advAppClassColors767',widget:'advClassColors',appKeys:['appColorByClass'],widgetKey:'colorByClass'}
                ];

                function option(value,fr,en,de){const o=document.createElement('option');o.value=value;o.textContent=tr(fr,en,de);return o}
                function makeAccess(id){const s=document.createElement('select');s.id=id;s.append(option('normal','Normale','Normal','Normal'),option('high_contrast','Contraste élevé','High contrast','Hoher Kontrast'),option('colorblind','Palette daltonisme','Color-blind palette','Farbenblind-Palette'));return s}
                function cell(kind){const d=document.createElement('div');d.className='displayCell767'+(kind?' '+kind:'');return d}
                function detachControl(id){
                  const control=document.getElementById(id);if(!control)return null;
                  const parent=control.parentElement;control.remove();
                  if(parent&&parent.closest('#displayGeneral767')===null&&(parent.matches('.advCheck,.advRow')||!parent.children.length))parent.remove();
                  return control;
                }
                function ensureDisplay(){
                  const title=document.getElementById('advWidgetTitle'),box=title&&title.closest('.settingBox');if(!box)return;
                  let general=document.getElementById('displayGeneral767');
                  if(!general){
                    general=document.createElement('div');general.id='displayGeneral767';
                    const heading=document.createElement('div');heading.id='displayGeneralTitle767';heading.className='displayTitle767';general.appendChild(heading);
                    const grid=document.createElement('div');grid.className='displayGrid767';grid.id='displayGrid767';
                    grid.append(cell('displayHead767'),cell('displayHead767'),cell('displayHead767'));
                    rows.forEach((row,index)=>{
                      const label=cell();label.dataset.row=String(index);grid.appendChild(label);
                      const appCell=cell(),widgetCell=cell();
                      const app=document.createElement('input');app.type='checkbox';app.id=row.app;appCell.appendChild(app);
                      const widget=detachControl(row.widget)||document.createElement('input');widget.type='checkbox';widget.id=row.widget;widgetCell.appendChild(widget);
                      grid.append(appCell,widgetCell);
                    });
                    const accessLabel=cell();accessLabel.dataset.access='1';grid.appendChild(accessLabel);
                    const appAccessCell=cell(),widgetAccessCell=cell();appAccessCell.appendChild(makeAccess('advAppAccess767'));
                    const widgetAccess=detachControl('advAccess')||makeAccess('advAccess');widgetAccessCell.appendChild(widgetAccess);grid.append(appAccessCell,widgetAccessCell);
                    general.appendChild(grid);box.insertBefore(general,title.nextSibling);
                  }
                  let widget=document.getElementById('displayWidget767');
                  if(!widget){widget=document.createElement('div');widget.id='displayWidget767';const heading=document.createElement('div');heading.id='displayWidgetTitle767';heading.className='displayTitle767';widget.appendChild(heading);general.insertAdjacentElement('afterend',widget)}
                  for(const id of ['advFormat','advFollowing']){const control=document.getElementById(id),row=control&&control.closest('.advRow');if(row&&row.parentNode!==widget)widget.appendChild(row)}
                  const bars=document.getElementById('widgetEdgeBars672');if(bars&&bars.parentNode!==widget)widget.appendChild(bars);
                  for(const id of ['advShowPercent','advShowProgress','advShowWeekInfo']){const control=document.getElementById(id),label=control&&control.closest('.advCheck');if(label)label.remove()}
                  title.style.display='none';
                }

                function localize(){
                  const sectionTitles={languageSettings86:tr('Langue','Language','Sprache'),textSettings86:tr('Taille du texte','Text size','Textgröße'),weekTypeSettings86:tr('Semaines et horaires','Weeks and times','Wochen und Zeiten'),colorSettings86:tr('Couleurs','Colours','Farben'),breakSettings86:tr('Affichage des interruptions','Break display','Pausenanzeige'),widgetSettings86:tr('Affichage','Display','Anzeige'),advancedSettings85:tr('Réglages avancés','Advanced settings','Erweiterte Einstellungen')};
                  for(const id in sectionTitles){const root=document.getElementById(id),summary=root&&root.querySelector('summary');if(summary)summary.textContent=sectionTitles[id]}
                  text('settingsTitle',tr('Réglages','Settings','Einstellungen'));text('settingsReset',tr('Réinitialiser','Reset','Zurücksetzen'));text('settingsDone',tr('Fermer','Close','Schließen'));
                  text('displayGeneralTitle767',tr('Affichage général','General display','Allgemeine Anzeige'));text('displayWidgetTitle767',tr('Widget','Widget','Widget'));
                  const cells=document.querySelectorAll('#displayGrid767 .displayHead767');if(cells[1])cells[1].textContent=tr('Application','Application','App');if(cells[2])cells[2].textContent=tr('Widget','Widget','Widget');
                  document.querySelectorAll('#displayGrid767 [data-row]').forEach(node=>{const row=rows[Number(node.dataset.row)];if(row)node.textContent=row.name()});
                  const access=document.querySelector('#displayGrid767 [data-access]');if(access)access.textContent=tr('Accessibilité','Accessibility','Barrierefreiheit');
                  for(const id of ['advAppAccess767','advAccess']){const select=document.getElementById(id);if(select&&select.options.length>=3){select.options[0].textContent=tr('Normale','Normal','Normal');select.options[1].textContent=tr('Contraste élevé','High contrast','Hoher Kontrast');select.options[2].textContent=tr('Palette daltonisme','Color-blind palette','Farbenblind-Palette')}}
                  const bar=document.querySelector('#widgetEdgeBars672 .bar672Title');if(bar)bar.textContent=tr('Barres du widget en haut et en bas','Widget top and bottom bars','Widget-Leisten oben und unten');
                  text('advFormatLabel',tr('Format','Format','Format'));text('advFollowingLabel',tr('Cours suivants','Following classes','Folgende Stunden'));
                  text('addCourse',tr('＋ Ajouter un cours','＋ Add a class','＋ Stunde hinzufügen'));
                  text('addBulkCourses',tr('＋ Ajouter plusieurs cours à une classe','＋ Add several classes to a group','＋ Mehrere Stunden zu einer Klasse hinzufügen'));
                  text('importPhoto',tr('Importer une photo d’emploi du temps','Import a timetable photo','Stundenplan-Foto importieren'));
                  const version=document.getElementById('appVersionInfo');if(version)version.textContent='Version '+VERSION;
                }

                function saveAndApply(next){
                  write(next);
                  try{if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures()}catch(e){}
                  applyPageState(next);requestAnimationFrame(refresh);
                }
                function bind(){
                  const state=read();
                  rows.forEach(row=>{
                    const app=document.getElementById(row.app),widget=document.getElementById(row.widget);
                    if(app){app.checked=row.appKeys.every(key=>state[key]!==false);app.setAttribute('aria-label',row.name()+' · '+tr('Application','Application','App'));app.onchange=()=>{const next=read();row.appKeys.forEach(key=>next[key]=app.checked);saveAndApply(next)}}
                    if(widget){widget.checked=state[row.widgetKey]!==false;widget.setAttribute('aria-label',row.name()+' · Widget');widget.onchange=()=>{const next=read();next[row.widgetKey]=widget.checked;saveAndApply(next)}}
                  });
                  const appAccess=document.getElementById('advAppAccess767'),widgetAccess=document.getElementById('advAccess');
                  if(appAccess){appAccess.value=state.appAccessibility||'normal';appAccess.onchange=()=>{const next=read();next.appAccessibility=appAccess.value;saveAndApply(next)}}
                  if(widgetAccess){widgetAccess.value=state.accessibility||'normal';widgetAccess.onchange=()=>{const next=read();next.accessibility=widgetAccess.value;saveAndApply(next)}}
                }
                function applyPageState(value){
                  const root=document.documentElement;root.classList.toggle('appHideRoom767',value.appShowRoom===false);root.classList.toggle('appHideTimes767',value.appShowTimes===false);root.classList.toggle('appHideRemaining767',value.appShowRemaining===false);root.classList.toggle('accessHigh',value.appAccessibility==='high_contrast');
                }

                function ownLanguage(){
                  let select=document.getElementById('languageSelect');if(!select||select.__feedback767Language)return;
                  const clone=select.cloneNode(true);clone.__feedback767Language=true;clone.__stability80Owned=true;select.replaceWith(clone);select=clone;
                  try{const code=String(JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr');if(select.querySelector('option[value="'+code.replace(/"/g,'')+'"]'))select.value=code}catch(e){}
                  select.addEventListener('change',event=>{
                    event.preventDefault();event.stopPropagation();if(event.stopImmediatePropagation)event.stopImmediatePropagation();
                    const code=String(event.target.value||'fr');let ui={};try{ui=JSON.parse(AndroidSchedule.loadUiSettings()||'{}')}catch(e){}ui.language=code;
                    try{AndroidSchedule.saveUiSettings(JSON.stringify(ui))}catch(e){}
                    try{sessionStorage.setItem('edt-language-reload-mode',typeof mode==='string'?mode:'edit')}catch(e){}
                    try{if(AndroidSchedule.reloadForLanguage){AndroidSchedule.reloadForLanguage();return}}catch(e){}
                    location.reload();
                  },true);
                }

                function defaultAdvanced(){return {density:'normal',widgetDensityPercent:50,widgetAutoDensity:false,dayViewDensity765:50,upcomingCount:0,widgetFormat:'timeline',showRoom:true,showTimes:true,showRemaining:true,showPercent:true,showProgress:true,widgetTopBarMode:'progress',widgetTopBarColor:'#1677E8',widgetBottomBarMode:'progress',widgetBottomBarColor:'#1677E8',showBreaks:true,showLunch:true,showWeekInfo:true,colorByClass:false,accessibility:'normal',appShowRoom:true,appShowTimes:true,appShowRemaining:true,appColorByClass:false,appAccessibility:'normal',showBreaksToday:true,showBreaksWeek:true,showLunchToday:true,showLunchWeek:true,cycleLength:2,singleWeek:false,remindersEnabled:false,reminderMinutes:10,holidayMode:'alsace_moselle',exceptions:[],dayOffRanges:[],gapWidgetLabel:'',lunchWidgetLabel:'',widgetCourseLabels:{}}}
                function resetEverything(){
                  try{AndroidSchedule.saveUiSettings(JSON.stringify({appFontScale:1,widgetFontScale:1,language:'fr',theme:'blue'}))}catch(e){}
                  try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(defaultAdvanced()))}catch(e){}
                  try{AndroidSchedule.saveWidgetPalette('vivid')}catch(e){}
                  try{AndroidSchedule.saveSpecialColors(JSON.stringify({sync:true,appLunch:'#FFE4A8',appGap:'#EAF4FF',widgetLunch:'#FFE4A8',widgetGap:'#EAF4FF'}))}catch(e){}
                  try{const root=JSON.parse(AndroidSchedule.loadSchedule()||'{}');root._slots=[['08:00','09:00'],['09:00','10:00'],['10:00','11:00'],['11:00','12:00'],['13:00','14:00'],['14:00','15:00'],['16:00','17:00'],['17:00','18:00'],['18:00','19:00']].map((v,i)=>({n:i+1,start:v[0],end:v[1]}));root._slotConfigV2=true;root._breaks={gapLabel:'Trou',lunchLabel:'Midi',showGapBadge:false,showLunchBadge:false};root._enabledDays=[2,3,4,5,6];root._cycleLength=2;AndroidSchedule.saveSchedule(JSON.stringify(root))}catch(e){}
                  for(const key of ['edt-special-colors-v2','edt-palette-sync-v1','edt-advanced'])try{localStorage.removeItem(key)}catch(e){}
                  try{if(AndroidSchedule.resetAllSettings){AndroidSchedule.resetAllSettings();return}}catch(e){}
                  const select=document.getElementById('languageSelect');if(select)select.value='fr';try{if(window.refreshSettingsV3)window.refreshSettingsV3();if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();if(window.refreshSettingsLayout)window.refreshSettingsLayout()}catch(e){};refresh();
                }
                function ownGlobalReset(){
                  let button=document.getElementById('settingsReset');if(!button||button.__feedback767Reset)return;
                  const clone=button.cloneNode(true);clone.__feedback767Reset=true;button.replaceWith(clone);button=clone;
                  button.addEventListener('click',event=>{event.preventDefault();event.stopPropagation();if(event.stopImmediatePropagation)event.stopImmediatePropagation();resetEverything()},true);
                }

                function refresh(){scheduled=false;ownLanguage();ensureDisplay();localize();bind();ownGlobalReset();applyPageState(read());['resetText87','resetColors87','resetWidget87'].forEach(id=>document.getElementById(id)?.remove())}
                function schedule(){if(scheduled)return;scheduled=true;requestAnimationFrame(refresh)}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback767)return;const next=function(){const result=old.apply(this,arguments);schedule();return result};next.__feedback767=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                window.refreshFeedback767=refresh;
                ['render','renderToday','renderWeek','renderEdit','refreshSettingsLayout','refreshAdvancedFeatures','prepareSettingsOpen665'].forEach(wrap);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback767Ui',e)}
            })();
            """;
    }
}
