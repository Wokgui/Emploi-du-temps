package com.wokgui.schedulewidget;

/** 7.61 UI polish requested on top of the published 7.60 build. */
final class UiPolish761Ui {
    private UiPolish761Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__uiPolish761){if(window.refreshUiPolish761)window.refreshUiPolish761();return}
                window.__uiPolish761=true;
                const style=document.createElement('style');style.id='uiPolish761Style';style.textContent=`
                  /* Settings: clearer hierarchy and no separator between interruptions and advanced settings. */
                  html body #settingsSheet .settingsSectionTitle86,
                  html body #settingsSheet .settingsSectionHead86,
                  html body #settingsSheet .settingsGroupTitle,
                  html body #settingsSheet .settingTitle{font-size:.94rem!important}
                  html body #settingsSheet #advCalendarTitle,
                  html body #settingsSheet #advProfilesTitle,
                  html body #settingsSheet #advBackupTitle,
                  html body #settingsSheet #advExceptionsTitle,
                  html body #settingsSheet [id*=Exception][id*=Title],
                  html body #settingsSheet #feedback663Visibility .feedback663Title{font-size:17px!important;font-weight:800!important}
                  html body #settingsSheet #week658LunchSettings .w658Title{font-size:17px!important;font-weight:800!important}
                  html body #settingsSheet #advancedSettings85{border-top:0!important;box-shadow:none!important}
                  html body #settingsSheet #advancedSettings85:before,
                  html body #settingsSheet #advancedSettings85:after{display:none!important;border:0!important}
                  html body #settingsSheet #breakDisplaySetting{border-bottom:0!important;margin-bottom:0!important}

                  /* Week: consume the available viewport down to the fixed bottom navigation. */
                  html body main.wrap.edtInstantViews647>#viewWeek.view.active{display:flex!important;flex-direction:column!important;min-height:calc(100dvh - var(--edt-week-top,0px) - var(--edt-bottom-nav,58px))!important;padding-bottom:0!important}
                  html body #viewWeek .weekTop{flex:0 0 auto!important}
                  html body #viewWeek .weekScroller{flex:1 1 auto!important;min-height:0!important;display:flex!important;overflow:hidden!important;padding-bottom:0!important}
                  html body #viewWeek #weekGrid{flex:1 1 auto!important;align-content:stretch!important;margin-bottom:0!important}
                  html body #viewWeek #weekGrid>.wh,html body #viewWeek #weekGrid>.wc{min-height:0!important}

                  /* Lunch rails share the exact grid boundary: no one-pixel floating line above the cells. */
                  html body #viewWeek #weekGrid#weekGrid>.week658LunchRail{height:1px!important;transform:translateY(0)!important}
                `;document.head.appendChild(style);

                function titleText(node){return String(node&&node.textContent||'').trim().toLowerCase()}
                function normalizeSettingsTitles(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  sheet.querySelectorAll('.settingTitle,.settingsSectionTitle86,.settingsSectionHead86,.settingsGroupTitle').forEach(node=>node.classList.add('ui761SettingsTitle'));
                  sheet.querySelectorAll('.settingTitle,[id$="Title"],[id*="Title"]').forEach(node=>{
                    const text=titleText(node);
                    if(text.includes('modifications exceptionnelles')||text.includes('exceptional changes')||text.includes('ausnahm')||text==='profils'||text==='profiles'||text==='profile'||text==='sauvegarde'||text==='backup')node.classList.add('ui761HolidayPeer');
                  });
                  const interruptions=document.getElementById('breakDisplaySetting');
                  if(interruptions){interruptions.style.borderBottom='0';interruptions.style.marginBottom='0'}
                  const advanced=document.getElementById('advancedSettings85');
                  if(advanced){advanced.style.borderTop='0';advanced.style.marginTop='0'}
                }

                function moveWeekColours(){
                  const lunch=document.getElementById('week658LunchSettings');
                  if(lunch){const title=lunch.querySelector('.w658Title');if(title)title.classList.add('ui761HolidayPeer')}
                  const colours=document.getElementById('week658Settings');if(!colours)return;
                  const colourGroup=[...document.querySelectorAll('#settingsSheet .settingsSection86,#settingsSheet .settingBox')].find(node=>/couleur|color|farbe/i.test(String(node.querySelector('.settingsSectionTitle86,.settingTitle,.settingsSectionHead86')?.textContent||'')));
                  const body=colourGroup&&colourGroup.querySelector('.settingsSectionBody86');
                  const target=body||colourGroup;
                  if(target&&colours.parentNode!==target)target.appendChild(colours);
                }

                function fitWeek(){
                  const view=document.getElementById('viewWeek'),bottom=document.querySelector('.bottom');if(!view||!bottom)return;
                  const top=Math.max(0,Math.round(view.getBoundingClientRect().top));
                  const bottomH=Math.max(48,Math.round(bottom.getBoundingClientRect().height));
                  document.documentElement.style.setProperty('--edt-week-top',top+'px');
                  document.documentElement.style.setProperty('--edt-bottom-nav',bottomH+'px');
                  const scroller=view.querySelector('.weekScroller'),grid=document.getElementById('weekGrid');if(!scroller||!grid)return;
                  const available=Math.max(0,window.innerHeight-bottomH-scroller.getBoundingClientRect().top);
                  scroller.style.height=available+'px';scroller.style.maxHeight=available+'px';
                  const heads=[...grid.children].filter(x=>x.classList&&x.classList.contains('wh')&&x.classList.contains('day'));
                  const times=[...grid.children].filter(x=>x.classList&&x.classList.contains('wh')&&x.classList.contains('timecol'));
                  if(!times.length)return;
                  const headerH=heads.length?Math.max(...heads.map(x=>x.getBoundingClientRect().height)):36;
                  const rowH=Math.max(25,Math.floor((available-headerH)/times.length));
                  [...grid.children].forEach(cell=>{if(cell.classList&&(cell.classList.contains('wc')||cell.classList.contains('wh'))&&!cell.classList.contains('day'))cell.style.height=rowH+'px'});
                  heads.forEach(cell=>cell.style.height=headerH+'px');
                  alignLunchRails();
                }

                function alignLunchRails(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  const rows=[...grid.querySelectorAll(':scope > .wh.timecol')];
                  grid.querySelectorAll(':scope > .week658LunchRail').forEach(rail=>{
                    const y=parseFloat(rail.style.top||'0');if(!Number.isFinite(y)||!rows.length)return;
                    let best=y,dist=Infinity;
                    rows.forEach(row=>{const a=row.offsetTop,b=row.offsetTop+row.offsetHeight;for(const edge of [a,b]){const d=Math.abs(edge-y);if(d<dist){dist=d;best=edge}}});
                    if(dist<=4)rail.style.top=Math.round(best)+'px';
                  });
                }

                let queued=false;
                function refresh(){queued=false;normalizeSettingsTitles();moveWeekColours();if(document.getElementById('viewWeek')?.classList.contains('active'))requestAnimationFrame(fitWeek)}
                function schedule(){if(queued)return;queued=true;requestAnimationFrame(refresh)}
                window.refreshUiPolish761=refresh;
                ['refreshSettingsLayout','refreshAdvancedFeatures','refreshSettingsV3','refreshWeekAppearance658','renderWeek'].forEach(name=>{
                  const old=window[name];if(typeof old!=='function'||old.__ui761)return;
                  const wrapped=function(){const result=old.apply(this,arguments);schedule();return result};wrapped.__ui761=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}
                });
                window.addEventListener('resize',schedule,{passive:true});
                const modal=document.getElementById('settingsModal');if(modal)new MutationObserver(schedule).observe(modal,{attributes:true,attributeFilter:['class']});
                refresh();
              }catch(e){console.error('UiPolish761Ui',e)}
            })();
            """;
    }
}
