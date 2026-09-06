package com.wokgui.schedulewidget;

final class UiPolishAndSchoolCalendarUi {
    private UiPolishAndSchoolCalendarUi() {}

    static String script() {
        return """
            (function(){
              try {
                if(window.__uiPolishSchoolV1){
                  if(window.refreshUiPolishSchool)window.refreshUiPolishSchool();
                  return;
                }
                window.__uiPolishSchoolV1=true;

                const EXTRA_THEMES={
                  rose:{fr:'Rose',en:'Pink',de:'Rosa',a:'#D81B60',d:'#AD1457',s:'#FCE4EC',bg:'#FFF7FA',ink:'#341723',m:'#75616A',line:'#E9D9DF'},
                  red:{fr:'Rouge',en:'Red',de:'Rot',a:'#D84343',d:'#B72E2E',s:'#FDE8E8',bg:'#FFF8F8',ink:'#351919',m:'#776060',line:'#EADADA'},
                  indigo:{fr:'Indigo',en:'Indigo',de:'Indigo',a:'#3F51B5',d:'#303F9F',s:'#E8EAF6',bg:'#F8F8FD',ink:'#1D2342',m:'#666A7D',line:'#DADCEB'},
                  cyan:{fr:'Cyan',en:'Cyan',de:'Cyan',a:'#0097A7',d:'#007C91',s:'#E0F7FA',bg:'#F5FCFD',ink:'#123036',m:'#60757A',line:'#D5E8EB'},
                  coral:{fr:'Corail',en:'Coral',de:'Koralle',a:'#E76F51',d:'#C95035',s:'#FCE9E3',bg:'#FFF8F5',ink:'#3A211B',m:'#79675F',line:'#ECDDD7'},
                  navy:{fr:'Bleu nuit',en:'Navy',de:'Dunkelblau',a:'#2457A7',d:'#193E7A',s:'#E5EDFA',bg:'#F7F9FD',ink:'#17243B',m:'#617086',line:'#D8E0EC'},
                  graphite:{fr:'Graphite',en:'Graphite',de:'Graphit',a:'#546E7A',d:'#37474F',s:'#ECEFF1',bg:'#F7F8F9',ink:'#1D292E',m:'#68757B',line:'#DCE1E3'}
                };

                const SCHOOL={
                  '2025-2026':{
                    common:[['2025-10-18','2025-11-02','Toussaint'],['2025-12-20','2026-01-04','Noël'],['2026-07-04','2026-08-31','Été'],['2026-05-15','2026-05-15','Pont de l’Ascension']],
                    A:[['2026-02-07','2026-02-22','Hiver'],['2026-04-04','2026-04-19','Printemps']],
                    B:[['2026-02-14','2026-03-01','Hiver'],['2026-04-11','2026-04-26','Printemps']],
                    C:[['2026-02-21','2026-03-08','Hiver'],['2026-04-18','2026-05-03','Printemps']]
                  },
                  '2026-2027':{
                    common:[['2026-10-17','2026-11-01','Toussaint'],['2026-12-19','2027-01-03','Noël'],['2027-07-03','2027-08-31','Été'],['2027-05-07','2027-05-07','Pont de l’Ascension']],
                    A:[['2027-02-13','2027-02-28','Hiver'],['2027-04-10','2027-04-25','Printemps']],
                    B:[['2027-02-20','2027-03-07','Hiver'],['2027-04-17','2027-05-02','Printemps']],
                    C:[['2027-02-06','2027-02-21','Hiver'],['2027-04-03','2027-04-18','Printemps']]
                  },
                  '2027-2028':{
                    common:[['2027-10-23','2027-11-07','Toussaint'],['2027-12-18','2028-01-02','Noël'],['2028-07-04','2028-08-31','Été'],['2028-05-26','2028-05-26','Pont de l’Ascension']],
                    A:[['2028-02-19','2028-03-05','Hiver'],['2028-04-22','2028-05-08','Printemps']],
                    B:[['2028-02-05','2028-02-20','Hiver'],['2028-04-08','2028-04-23','Printemps']],
                    C:[['2028-02-12','2028-02-27','Hiver'],['2028-04-15','2028-05-01','Printemps']]
                  }
                };

                function ui(){
                  try{return JSON.parse(window.AndroidSchedule&&AndroidSchedule.loadUiSettings?AndroidSchedule.loadUiSettings():'{}')}catch(e){return {}}
                }
                function lang(){const l=ui().language;return l==='en'||l==='de'?l:'fr'}
                function tx(fr,en,de){return lang()==='en'?en:(lang()==='de'?de:fr)}
                function advanced(){
                  try{return JSON.parse(window.AndroidSchedule&&AndroidSchedule.loadAdvancedSettings?AndroidSchedule.loadAdvancedSettings():localStorage.getItem('edt-advanced')||'{}')}catch(e){return {}}
                }
                function saveAdvanced(o){
                  const raw=JSON.stringify(o);
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveAdvancedSettings)AndroidSchedule.saveAdvancedSettings(raw);else localStorage.setItem('edt-advanced',raw)}catch(e){}
                }
                function saveUi(o){
                  const raw=JSON.stringify(o);
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveUiSettings)AndroidSchedule.saveUiSettings(raw);else localStorage.setItem('edt-ui-settings',raw)}catch(e){}
                }

                const style=document.createElement('style');
                style.textContent=`
                  .header h1{font-size:1.16rem!important;letter-spacing:-.01em}
                  .weekTabs{min-width:0}.weekTab{min-width:0}
                  body.largeAppText .contextBar{height:auto!important;min-height:39px;flex-wrap:wrap;align-content:center;padding-top:5px;padding-bottom:5px}
                  body.largeAppText .currentWeek{flex:1 1 100%;max-width:100%;text-align:center}
                  body.largeAppText .weekTabs{display:flex!important;flex:1 1 100%;width:100%;min-width:0;gap:4px}
                  body.largeAppText .weekTab{flex:1 1 0;min-width:0!important;padding:5px 3px!important;white-space:normal;line-height:1.05}
                  body.threeWeekCycle:not(.largeAppText) .weekTabs{flex:1;min-width:0}
                  body.threeWeekCycle:not(.largeAppText) .weekTab{padding-left:5px;padding-right:5px}
                  #schoolCalendarBlock{margin-top:9px;padding-top:8px;border-top:1px solid #edf0f4}
                  #schoolCalendarBlock .schoolTitle{font-size:.72rem;font-weight:850;color:var(--set-dark);margin-bottom:6px}
                  #schoolCalendarBlock .schoolEnable{display:flex;align-items:center;gap:7px;font-size:.76rem;margin-bottom:7px}
                  #schoolCalendarBlock .schoolEnable input{width:17px;height:17px;accent-color:var(--set-accent)}
                  #schoolCalendarBlock .schoolGrid{display:grid;grid-template-columns:1fr 1fr;gap:6px}
                  #schoolCalendarBlock select{width:100%;min-width:0;border:1px solid #d8e0e8;border-radius:7px;padding:7px;background:#fff;color:inherit}
                  #schoolCalendarBlock .schoolHint{font-size:.66rem;color:#68738a;margin-top:6px;line-height:1.25}
                  .themeButton.extraTheme .themeName{font-size:.56rem}
                `;
                document.head.appendChild(style);

                function applyResponsive(){
                  const o=ui();const scale=Number(o.appFontScale)||1;
                  document.body.classList.toggle('largeAppText',scale>1.12);
                  const a=advanced();document.body.classList.toggle('threeWeekCycle',Number(a.cycleLength)===3);
                }

                function applyExtraTheme(id){
                  const th=EXTRA_THEMES[id];if(!th)return;
                  const vars={'--set-accent':th.a,'--set-dark':th.d,'--set-soft':th.s,'--set-ink':th.ink,'--set-muted':th.m,'--blue':th.a,'--blue2':th.d,'--teal':th.a,'--ink':th.ink,'--muted':th.m,'--line':th.line,'--soft':th.s,'--bg':th.bg};
                  Object.keys(vars).forEach(k=>document.documentElement.style.setProperty(k,vars[k]));document.body.style.background=th.bg;
                  document.querySelectorAll('.themeButton').forEach(b=>b.classList.toggle('active',b.dataset.theme===id));
                }

                let stickyTheme=EXTRA_THEMES[ui().theme]?ui().theme:null;
                let themeBusy=false;
                function restoreStickyTheme(){
                  if(!stickyTheme)return;setTimeout(()=>{const o=ui();if(o.theme!==stickyTheme){o.theme=stickyTheme;saveUi(o)}applyExtraTheme(stickyTheme);renderExtraThemes()},15);
                }
                function renderExtraThemes(){
                  if(themeBusy)return;const grid=document.getElementById('themeGrid');if(!grid)return;themeBusy=true;
                  const nativeCurrent=ui().theme||'blue';const current=stickyTheme||nativeCurrent;
                  Object.keys(EXTRA_THEMES).forEach(id=>{
                    let b=grid.querySelector('[data-theme="'+id+'"]');const th=EXTRA_THEMES[id];
                    if(!b){b=document.createElement('button');b.type='button';b.className='themeButton extraTheme';b.dataset.theme=id;b.innerHTML='<span class="themeSwatch" style="background:linear-gradient(135deg,'+th.a+','+th.s+')"></span><span class="themeName"></span>';grid.appendChild(b)}
                    const n=b.querySelector('.themeName');if(n)n.textContent=th[lang()]||th.fr;
                    b.classList.toggle('active',current===id);
                    b.onclick=()=>{stickyTheme=id;const o=ui();o.theme=id;saveUi(o);applyExtraTheme(id);setTimeout(refresh,20)};
                  });
                  if(EXTRA_THEMES[current])applyExtraTheme(current);
                  themeBusy=false;
                }

                function defaultSchoolYear(){
                  const d=new Date(),y=d.getFullYear(),m=d.getMonth()+1;return (m>=8?y:y-1)+'-'+(m>=8?y+1:y);
                }
                function vacationName(name){
                  const names={
                    'Toussaint':{en:'Autumn break',de:'Herbstferien'},'Noël':{en:'Christmas break',de:'Weihnachtsferien'},'Hiver':{en:'Winter break',de:'Winterferien'},'Printemps':{en:'Spring break',de:'Frühlingsferien'},'Été':{en:'Summer break',de:'Sommerferien'},'Pont de l’Ascension':{en:'Ascension break',de:'Christi-Himmelfahrt-Brückentag'}
                  };const n=names[name];return n?(lang()==='en'?n.en:(lang()==='de'?n.de:name)):name;
                }
                function schoolRanges(year,zone){
                  const data=SCHOOL[year];if(!data)return [];
                  return (data.common||[]).concat(data[zone]||[]).map(x=>({start:x[0],end:x[1],label:tx('Vacances scolaires · ','School holidays · ','Schulferien · ')+vacationName(x[2]),source:'schoolCalendar'}));
                }
                function applySchoolCalendar(){
                  const a=advanced();const enabled=document.getElementById('schoolEnabled')?document.getElementById('schoolEnabled').checked:false;
                  const year=document.getElementById('schoolYear')?document.getElementById('schoolYear').value:defaultSchoolYear();
                  const zone=document.getElementById('schoolZone')?document.getElementById('schoolZone').value:'B';
                  a.schoolVacationEnabled=enabled;a.schoolVacationYear=year;a.schoolVacationZone=zone;
                  if(!Array.isArray(a.dayOffRanges))a.dayOffRanges=[];
                  a.dayOffRanges=a.dayOffRanges.filter(r=>r&&r.source!=='schoolCalendar');
                  if(enabled)a.dayOffRanges=a.dayOffRanges.concat(schoolRanges(year,zone));
                  saveAdvanced(a);applyResponsive();
                  if(window.refreshAdvancedFeatures)setTimeout(window.refreshAdvancedFeatures,20);
                  if(typeof render==='function')setTimeout(render,30);
                  syncSchoolControls();
                }
                function syncSchoolControls(){
                  const a=advanced(),e=document.getElementById('schoolEnabled'),y=document.getElementById('schoolYear'),z=document.getElementById('schoolZone'),h=document.getElementById('schoolHint');
                  if(e)e.checked=a.schoolVacationEnabled===true;if(y)y.value=SCHOOL[a.schoolVacationYear]?a.schoolVacationYear:(SCHOOL[defaultSchoolYear()]?defaultSchoolYear():'2026-2027');if(z)z.value=['A','B','C'].includes(a.schoolVacationZone)?a.schoolVacationZone:'B';
                  if(h){const yy=y?y.value:'',zz=z?z.value:'B',count=schoolRanges(yy,zz).length;h.textContent=(e&&e.checked)?tx(count+' périodes intégrées automatiquement pour la zone '+zz+'.',''+count+' periods automatically included for zone '+zz+'.',count+' Zeiträume für Zone '+zz+' automatisch übernommen.'):tx('Active cette option pour que l’application et le widget ignorent automatiquement les vacances scolaires.','Enable this so the app and widget automatically skip school holidays.','Aktivieren, damit App und Widget Schulferien automatisch überspringen.')}
                }
                function ensureSchoolControls(){
                  if(document.getElementById('schoolCalendarBlock')){syncSchoolControls();return}
                  const title=document.getElementById('advCalendarTitle');const box=title?title.closest('.settingBox'):null;if(!box)return;
                  const block=document.createElement('div');block.id='schoolCalendarBlock';
                  block.innerHTML='<div id="schoolTitle" class="schoolTitle"></div><label class="schoolEnable"><input id="schoolEnabled" type="checkbox"><span id="schoolEnableLabel"></span></label><div class="schoolGrid"><select id="schoolYear"><option value="2025-2026">2025–2026</option><option value="2026-2027">2026–2027</option><option value="2027-2028">2027–2028</option></select><select id="schoolZone"><option value="A">Zone A</option><option value="B">Zone B</option><option value="C">Zone C</option></select></div><div id="schoolHint" class="schoolHint"></div>';
                  box.insertBefore(block,box.querySelector('#advRangeTitle'));
                  document.getElementById('schoolEnabled').onchange=applySchoolCalendar;document.getElementById('schoolYear').onchange=applySchoolCalendar;document.getElementById('schoolZone').onchange=applySchoolCalendar;
                  syncSchoolControls();
                }
                function localizeSchool(){
                  const t=document.getElementById('schoolTitle'),e=document.getElementById('schoolEnableLabel');if(t)t.textContent=tx('Vacances scolaires','School holidays','Schulferien');if(e)e.textContent=tx('Intégrer automatiquement les vacances','Automatically include school holidays','Schulferien automatisch übernehmen');
                  syncSchoolControls();
                }

                function refresh(){applyResponsive();ensureSchoolControls();localizeSchool();renderExtraThemes()}
                window.refreshUiPolishSchool=refresh;

                const oldRefresh=window.refreshSettingsV3;
                if(typeof oldRefresh==='function'&&!oldRefresh.__polishWrapped){const w=function(){const r=oldRefresh.apply(this,arguments);setTimeout(refresh,0);return r};w.__polishWrapped=true;window.refreshSettingsV3=w}
                const oldAdv=window.refreshAdvancedFeatures;
                if(typeof oldAdv==='function'&&!oldAdv.__polishWrapped){const w=function(){const r=oldAdv.apply(this,arguments);setTimeout(refresh,0);return r};w.__polishWrapped=true;window.refreshAdvancedFeatures=w}

                const settings=document.getElementById('settingsBtn');if(settings)settings.addEventListener('click',()=>setTimeout(refresh,30));
                const appFont=document.getElementById('appFont');if(appFont){appFont.addEventListener('input',()=>{setTimeout(applyResponsive,0);restoreStickyTheme()});appFont.addEventListener('change',restoreStickyTheme)}
                const widgetFont=document.getElementById('widgetFont');if(widgetFont){widgetFont.addEventListener('input',restoreStickyTheme);widgetFont.addEventListener('change',restoreStickyTheme)}
                const language=document.getElementById('languageSelect');if(language)language.addEventListener('change',restoreStickyTheme);
                const cycle=document.getElementById('advCycle');if(cycle)cycle.addEventListener('change',()=>setTimeout(applyResponsive,0));
                const grid=document.getElementById('themeGrid');if(grid){
                  grid.addEventListener('click',e=>{const b=e.target.closest('.themeButton');if(b&&!b.classList.contains('extraTheme'))stickyTheme=null});
                  const mo=new MutationObserver(()=>{if(!themeBusy)setTimeout(renderExtraThemes,0)});mo.observe(grid,{childList:true});
                }
                refresh();
              } catch(e) { console.log('UI polish/school calendar',e); }
            })();
            """;
    }
}
