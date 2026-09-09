package com.wokgui.schedulewidget;

final class Stability70Ui {
    private Stability70Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability70V1){if(window.refreshStability70)window.refreshStability70();return}
                window.__stability70V1=true;
                const APP_VERSION='6.26';
                const STRONG='1.5px';
                let painting=false;

                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function lang(){try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}}
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}
                function toMin(v){const p=String(v||'').split(':').map(Number);return (p[0]||0)*60+(p[1]||0)}
                function modeCount(){const a=loadAdv();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2))}

                const style=document.createElement('style');
                style.id='stability70Style';
                style.textContent=`
                  :root{--week-strong-line:${STRONG}}

                  /* Stable top context bar: fixed geometry, no transition or resize when a week changes. */
                  .contextBar{gap:5px!important;padding-left:7px!important;padding-right:7px!important;overflow:hidden!important}
                  #currentWeekBtn,#currentWeekBtn.currentWeek{
                    flex:0 0 136px!important;width:136px!important;min-width:136px!important;max-width:136px!important;
                    height:30px!important;min-height:30px!important;max-height:30px!important;margin:0!important;padding:0 7px!important;
                    display:flex!important;align-items:center!important;justify-content:center!important;box-sizing:border-box!important;
                    font-size:.68rem!important;line-height:1!important;white-space:nowrap!important;overflow:hidden!important;
                    transition:none!important;animation:none!important;transform:none!important;box-shadow:none!important
                  }
                  #currentWeekBtn .cwLetter70{font-weight:900!important;color:var(--set-accent,var(--blue))!important}
                  #weekTabs.weekTabs{flex:1 1 auto!important;min-width:0!important;height:30px!important;display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:30px!important;gap:3px!important;overflow:hidden!important}
                  #weekTabs .weekTab,#weekTabs .weekTab.active{
                    width:100%!important;min-width:0!important;max-width:none!important;height:30px!important;min-height:30px!important;max-height:30px!important;
                    margin:0!important;padding:0 1px!important;display:flex!important;align-items:center!important;justify-content:center!important;
                    box-sizing:border-box!important;border-width:1px!important;border-style:solid!important;border-radius:999px!important;
                    font-size:.575rem!important;font-weight:800!important;line-height:1!important;white-space:nowrap!important;overflow:hidden!important;
                    transition:none!important;animation:none!important;transform:none!important;box-shadow:none!important
                  }
                  #weekTabs .weekTab[aria-hidden="true"]{visibility:hidden!important;pointer-events:none!important}
                  body.singleWeekMode #weekTabs{display:grid!important;visibility:hidden!important}

                  /* Four cycle choices are permanent and identical in size. */
                  #weekModeBar{margin-top:-4px!important;margin-bottom:4px!important;padding-top:5px!important;padding-bottom:5px!important}
                  #weekModeBar .weekModeChoices{display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;grid-template-rows:32px!important;gap:4px!important;width:100%!important;height:32px!important;overflow:visible!important}
                  #weekModeBar .weekModeChoice,#weekModeBar .weekModeChoice.active{
                    display:flex!important;align-items:center!important;justify-content:center!important;width:100%!important;min-width:0!important;max-width:none!important;
                    height:32px!important;min-height:32px!important;max-height:32px!important;margin:0!important;padding:0 2px!important;box-sizing:border-box!important;
                    border-width:1px!important;border-style:solid!important;border-radius:8px!important;font-size:.60rem!important;font-weight:800!important;line-height:1!important;
                    white-space:nowrap!important;overflow:hidden!important;text-overflow:clip!important;transition:none!important;animation:none!important;transform:none!important;box-shadow:none!important
                  }
                  #weekModeBar .weekModeChoice[hidden]{display:flex!important}
                  #importPhoto{height:42px!important;min-height:42px!important;margin:5px 0!important;display:flex!important;align-items:center!important;justify-content:center!important;text-align:center!important}
                  #importStatus:empty{display:none!important}
                  #viewEdit .editTop{margin-top:0!important}

                  /* Strong separators use the actual grid borders, never an inset outline. */
                  #weekGrid>.wh.timecol{border-right-width:var(--week-strong-line)!important;border-right-style:solid!important;border-right-color:#cbd5e1!important}
                  #weekGrid>.wh.day{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}
                  #weekGrid>.wh.timecol:first-child{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:#cbd5e1!important}
                  #weekGrid .lunch70Top{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}
                  #weekGrid .lunch70Bottom{border-bottom-width:var(--week-strong-line)!important;border-bottom-style:solid!important;border-bottom-color:var(--ft-midi-border,#C7AA62)!important}
                  #weekGrid .lunch70Left,#weekGrid .lunch70Right{border-right-width:var(--week-strong-line)!important;border-right-style:solid!important;border-right-color:var(--ft-midi-border,#C7AA62)!important}
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell{
                    margin:0!important;padding:0!important;border-radius:0!important;outline:0!important;box-shadow:none!important;background:var(--ft-midi,#FFF9E8)!important;overflow:hidden!important
                  }
                  html body #viewWeek #weekGrid#weekGrid .wc.lunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.dynamicLunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.nativeLunchCell::before,
                  html body #viewWeek #weekGrid#weekGrid .wc.finalLunchCell::before,
                  #weekGrid .finalLunchJoinedRight::after{display:none!important;content:none!important}

                  /* Hide every legacy vertical clock marker. The only visible marker is .now70Bar/.now70Dot. */
                  #weekGrid::before,#weekGrid::after{content:none!important;display:none!important}
                  #weekGrid #weekNowRail,#weekGrid #weekNowDot,#weekGrid [id*="WeekNow"],#weekGrid [id*="weekNow"],
                  #weekGrid .scheduleNowRail,#weekGrid .scheduleNowDot,#weekGrid .nativeNowFull,#weekGrid .nativeNowPartial,#weekGrid .nativeNowDot,
                  #weekGrid .now69Bar,#weekGrid .now69Dot,#weekGrid .finalNowBar,#weekGrid .finalNowDot,
                  #weekGrid .finalWeekNowRailV12,#weekGrid .finalWeekNowDotV12,#weekGrid .finalWeekNowRailV13,#weekGrid .finalWeekNowDotV13{display:none!important}
                  #weekGrid .now70Course{position:relative!important;overflow:visible!important;z-index:26!important}
                  #weekGrid .now70Bar{position:absolute!important;left:0!important;right:0!important;width:auto!important;height:2px!important;background:#1688F4!important;z-index:240!important;pointer-events:none!important;display:block!important}
                  #weekGrid .now70Dot{position:absolute!important;left:0!important;width:10px!important;height:10px!important;border-radius:50%!important;transform:translate(-50%,-50%)!important;background:#1688F4!important;border:2px solid #D9ECFF!important;box-sizing:border-box!important;z-index:241!important;pointer-events:none!important;display:block!important}

                  /* Independent application visibility for lunch/free periods; widget toggles remain separate. */
                  body.hideTodayLunch70 #todayList .todayCourse.lunch,body.hideTodayGaps70 #todayList .todayCourse.gap{display:none!important}
                  #weekGrid.hideWeekLunch70 .wc.lunchCell,#weekGrid.hideWeekLunch70 .wc.dynamicLunchCell,#weekGrid.hideWeekLunch70 .wc.nativeLunchCell{background:#fff!important;color:transparent!important;box-shadow:none!important}
                  #weekGrid.hideWeekLunch70 .wc.lunchCell *,#weekGrid.hideWeekLunch70 .wc.dynamicLunchCell *,#weekGrid.hideWeekLunch70 .wc.nativeLunchCell *{visibility:hidden!important}
                  #weekGrid.hideWeekGaps70 .wc.gapCell{background:#fff!important;color:transparent!important;box-shadow:none!important}
                  #weekGrid.hideWeekGaps70 .wc.gapCell *{visibility:hidden!important}

                  /* Settings alignment requested for the principal personalization groups. */
                  #appFontTitle,#widgetFontTitle,#languageTitle,#themeTitle,#appPaletteTitle,#widgetPaletteSeparateTitle,#breakDisplayTitle,
                  #fineSpecialColors>.settingTitle,#fineSpecialColors .coursePaletteHint{text-align:center!important}
                  #languageSelect{display:block!important;max-width:280px!important;margin-left:auto!important;margin-right:auto!important;text-align:center!important;text-align-last:center!important}
                  #themeGrid .themeButton,#paletteSettingRoot .coursePaletteBtn{text-align:center!important}
                  #paletteSettingRoot .coursePaletteHint,#breakDisplaySetting .coursePaletteHint{text-align:center!important}
                  #fineSpecialColors .specialColorRow>span{text-align:center!important}
                  #viewEdit .sectionHead h3{width:100%!important;text-align:center!important;flex:1 1 auto!important}

                  /* The two add-course actions are deliberately the same component. */
                  #addCourse,#addBulkCourses{
                    width:100%!important;height:42px!important;min-height:42px!important;margin-top:7px!important;padding:9px!important;
                    border:1.5px solid var(--blue,#0877f9)!important;border-radius:7px!important;background:#edf6ff!important;color:var(--blue,#0877f9)!important;
                    font-size:.84rem!important;font-weight:800!important;line-height:1.15!important;text-align:center!important;box-sizing:border-box!important
                  }

                  #breakVisibility70{margin-top:7px;border-top:1px solid #edf0f4;padding-top:7px}
                  #breakVisibility70 .breakVisTitle70{text-align:center;font-size:.68rem;font-weight:900;color:var(--set-dark,var(--blue));margin:5px 0}
                  #breakVisibility70 .breakVisRow70{display:grid;grid-template-columns:88px 1fr 1fr;gap:6px;align-items:center;margin:5px 0;font-size:.69rem}
                  #breakVisibility70 .breakVisRow70>span{font-weight:800;text-align:center}
                  #breakVisibility70 label,#breakWidget70 label{display:flex;align-items:center;justify-content:center;gap:5px;margin:0!important;font-size:.68rem!important}
                  #breakVisibility70 input,#breakWidget70 input{width:17px!important;height:17px!important;accent-color:var(--set-accent,var(--blue))}
                  #breakWidget70{display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-top:5px}
                `;
                document.head.appendChild(style);

                function setCurrentWeekLabel(letter){
                  const cw=document.getElementById('currentWeekBtn');if(!cw)return;
                  letter=String(letter||'A');
                  let p=cw.querySelector('.cwPrefix70'),b=cw.querySelector('.cwLetter70');
                  if(!p||!b){cw.textContent='';p=document.createElement('span');p.className='cwPrefix70';b=document.createElement('span');b.className='cwLetter70';cw.append(p,b)}
                  const prefix=tr('Cette semaine :\u00A0','This week: ','Diese Woche: ');
                  if(p.textContent!==prefix)p.textContent=prefix;if(b.textContent!==letter)b.textContent=letter;
                }
                window.setCurrentWeekLabel70=setCurrentWeekLabel;

                function ensureCycleChoices(){
                  const box=document.querySelector('#weekModeBar .weekModeChoices');if(!box)return;
                  const defs=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  for(const [v,label] of defs){
                    let b=box.querySelector('.weekModeChoice[data-m="'+v+'"]');
                    if(!b){b=document.createElement('button');b.type='button';b.className='weekModeChoice';b.dataset.m=v;box.appendChild(b)}
                    b.hidden=false;b.removeAttribute('hidden');b.style.removeProperty('display');b.textContent=label;
                    b.onclick=e=>{e.preventDefault();e.stopPropagation();const target=Number(v);box.querySelectorAll('.weekModeChoice').forEach(x=>x.classList.toggle('active',Number(x.dataset.m)===target));requestAnimationFrame(()=>{if(window.switchCycle69)window.switchCycle69(target);else if(window.switchCycleStable)window.switchCycleStable(target);syncCycleUi()});return false};
                  }
                  const order=new Map(defs.map((x,i)=>[x[0],i]));
                  [...box.querySelectorAll('.weekModeChoice')].sort((a,b)=>(order.get(a.dataset.m)??99)-(order.get(b.dataset.m)??99)).forEach(b=>box.appendChild(b));
                  const m=modeCount();box.querySelectorAll('.weekModeChoice').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===m));
                  const sel=document.getElementById('advCycle');
                  if(sel&&!sel.querySelector('option[value="4"]')){const o=document.createElement('option');o.value='4';o.textContent=tr('4 semaines','4 weeks','4 Wochen');sel.appendChild(o)}
                }

                function ensureTopTabs(){
                  const box=document.getElementById('weekTabs');if(!box)return;
                  const prefix=tr('Semaine ','Week ','Woche '),n=modeCount();
                  for(const w of ['A','B','C','D']){
                    let b=box.querySelector('.weekTab[data-week="'+w+'"]');if(!b){b=document.createElement('button');b.type='button';b.className='weekTab';b.dataset.week=w;box.appendChild(b)}
                    if(b.textContent!==prefix+w)b.textContent=prefix+w;
                    const idx='ABCD'.indexOf(w),visible=n>1&&idx>=0&&idx<n;
                    b.setAttribute('aria-hidden',visible?'false':'true');b.style.visibility=visible?'visible':'hidden';b.style.pointerEvents=visible?'auto':'none';b.tabIndex=visible?0:-1;
                    const active=typeof activeWeek!=='undefined'?activeWeek:'A';b.classList.toggle('active',visible&&active===w);
                    b.onclick=visible?(()=>{if(typeof activeWeek!=='undefined'&&activeWeek===w)return;if(typeof activeWeek!=='undefined')activeWeek=w;try{if(typeof render==='function')render()}catch(e){}syncCycleUi();paintWeek70()}):null;
                  }
                  document.body.classList.toggle('singleWeekMode',n===1);
                  box.style.visibility=n===1?'hidden':'visible';
                }

                function bindCurrentWeekButton(){
                  const cw=document.getElementById('currentWeekBtn');if(!cw)return;
                  const n=modeCount(),cur=typeof currentWeek!=='undefined'?currentWeek:'A';setCurrentWeekLabel(cur);
                  if(n===1){cw.onclick=null;return}
                  cw.onclick=()=>{
                    const list=['A','B','C','D'].slice(0,n),old=typeof currentWeek!=='undefined'?currentWeek:'A',idx=Math.max(0,list.indexOf(old)),next=list[(idx+1)%list.length];
                    if(typeof currentWeek!=='undefined')currentWeek=next;if(typeof activeWeek!=='undefined')activeWeek=next;
                    try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(next)}catch(e){}
                    try{if(AndroidSchedule.saveSchedule&&typeof exportState==='function')AndroidSchedule.saveSchedule(JSON.stringify(exportState()))}catch(e){}
                    setCurrentWeekLabel(next);ensureTopTabs();
                    try{if(typeof render==='function')render()}catch(e){}syncCycleUi();paintWeek70();
                  };
                }

                function syncCycleUi(){ensureCycleChoices();ensureTopTabs();bindCurrentWeekButton()}

                function rowsOf(grid){
                  const rows=[];if(!grid)return rows;const dayCount=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.length:5;
                  for(const time of Array.from(grid.querySelectorAll(':scope > .wh.timecol'))){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(found.length<2)continue;
                    const cells=[];let n=time.nextElementSibling;while(n&&cells.length<dayCount){if(n.classList&&n.classList.contains('wc'))cells.push(n);n=n.nextElementSibling}
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return rows;
                }

                function clearLunchEdges(grid){
                  grid.querySelectorAll('.lunch70Top,.lunch70Bottom,.lunch70Left,.lunch70Right').forEach(x=>x.classList.remove('lunch70Top','lunch70Bottom','lunch70Left','lunch70Right'));
                  grid.querySelectorAll('.lunch69TopLine,.lunch69BottomLine').forEach(x=>x.classList.remove('lunch69TopLine','lunch69BottomLine'));
                  grid.querySelectorAll('.finalLunchCell,.finalLunchJoinedRight').forEach(c=>{c.classList.remove('finalLunchCell','finalLunchJoinedRight');c.style.removeProperty('box-shadow');c.style.removeProperty('border-right-color')});
                  grid.querySelectorAll('[data-midi-edge-v14="1"]').forEach(c=>{c.style.removeProperty('border-right-color');c.style.removeProperty('border-bottom-color');c.removeAttribute('data-midi-edge-v14')});
                }
                function lunchCell(c){return !!(c&&(c.classList.contains('lunchCell')||c.classList.contains('dynamicLunchCell')||c.classList.contains('nativeLunchCell')))}
                function paintLunchEdges(grid,rows){
                  clearLunchEdges(grid);const a=loadAdv();if(a.showLunchWeek===false)return;
                  const heads=grid.querySelectorAll(':scope > .wh.day');
                  rows.forEach((row,ri)=>{
                    const flags=row.cells.map(c=>lunchCell(c));let i=0;
                    while(i<flags.length){if(!flags[i]){i++;continue}let j=i;while(j+1<flags.length&&flags[j+1])j++;
                      for(let k=i;k<=j;k++){
                        const cell=row.cells[k];cell.style.setProperty('box-shadow','none','important');cell.classList.add('lunch70Bottom');
                        const above=ri>0?rows[ri-1].cells[k]:heads[k];if(above)above.classList.add('lunch70Top');
                      }
                      const first=row.cells[i],last=row.cells[j],left=first?first.previousElementSibling:null;
                      if(left)left.classList.add('lunch70Left');if(last)last.classList.add('lunch70Right');i=j+1;
                    }
                  });
                }

                function removeLegacyNow(grid){
                  grid.querySelectorAll('.now70Bar,.now70Dot').forEach(x=>x.remove());grid.querySelectorAll('.now70Course').forEach(x=>x.classList.remove('now70Course'));
                  grid.querySelectorAll('.now69Bar,.now69Dot,.finalNowBar,.finalNowDot,.nativeNowFull,.nativeNowPartial,.nativeNowDot,.scheduleNowRail,.scheduleNowDot').forEach(x=>x.remove());
                  grid.querySelectorAll('[id*="weekNow"],[id*="WeekNow"]').forEach(x=>x.remove());
                  grid.querySelectorAll('.nativeNowTrackCell,.now69Course,.finalNowCourse').forEach(x=>x.classList.remove('nativeNowTrackCell','now69Course','finalNowCourse'));
                  grid.querySelectorAll('span').forEach(x=>{const s=((x.id||'')+' '+(typeof x.className==='string'?x.className:'')).toLowerCase();if(!x.classList.contains('now70Bar')&&!x.classList.contains('now70Dot')&&(s.includes('nowrail')||s.includes('nowdot')||s.includes('nownow')||s.includes('schedulerail')))x.remove()});
                }

                function paintHorizontalNow(grid,rows){
                  removeLegacyNow(grid);if(typeof mode!=='undefined'&&mode!=='week')return;
                  try{if(typeof activeWeek!=='undefined'&&typeof currentWeek!=='undefined'&&activeWeek!==currentWeek)return}catch(e){}
                  const now=new Date(),js=now.getDay(),day=js===0?1:js+1,di=(typeof DAYS!=='undefined'&&Array.isArray(DAYS))?DAYS.indexOf(day):-1;if(di<0)return;
                  const list=(typeof weeks!=='undefined'&&typeof currentWeek!=='undefined'&&weeks[currentWeek]&&weeks[currentWeek][day]&&Array.isArray(weeks[currentWeek][day].courses))?weeks[currentWeek][day].courses:[];
                  const minute=now.getHours()*60+now.getMinutes(),course=list.find(c=>minute>=toMin(c.start)&&minute<toMin(c.end));if(!course)return;
                  const row=rows.find(r=>r.start===toMin(course.start)&&r.end===toMin(course.end));if(!row)return;const cell=row.cells[di];if(!cell||!cell.classList.contains('has'))return;
                  const frac=(minute-toMin(course.start))/Math.max(1,toMin(course.end)-toMin(course.start)),top=Math.max(0,Math.min(100,frac*100)).toFixed(4)+'%';
                  cell.classList.add('now70Course');const bar=document.createElement('span');bar.className='now70Bar';bar.style.setProperty('top',top,'important');cell.appendChild(bar);
                  const dot=document.createElement('span');dot.className='now70Dot';dot.style.setProperty('top',top,'important');cell.appendChild(dot);
                }

                function applyBreakVisibility(){
                  const a=loadAdv(),grid=document.getElementById('weekGrid');
                  document.body.classList.toggle('hideTodayLunch70',a.showLunchToday===false);document.body.classList.toggle('hideTodayGaps70',a.showBreaksToday===false);
                  if(grid){grid.classList.toggle('hideWeekLunch70',a.showLunchWeek===false);grid.classList.toggle('hideWeekGaps70',a.showBreaksWeek===false)}
                }

                function paintWeek70(){
                  if(painting)return;painting=true;
                  try{const grid=document.getElementById('weekGrid');if(!grid)return;applyBreakVisibility();const rows=rowsOf(grid);paintLunchEdges(grid,rows);paintHorizontalNow(grid,rows)}finally{painting=false}
                }
                window.paintWeek70=paintWeek70;

                function installBreakVisibility(){
                  const box=document.getElementById('breakDisplaySetting');if(!box)return;let root=document.getElementById('breakVisibility70');
                  if(!root){root=document.createElement('div');root.id='breakVisibility70';const hint=document.getElementById('breakDisplayHint');if(hint&&hint.nextSibling)box.insertBefore(root,hint.nextSibling);else box.appendChild(root)}
                  root.innerHTML='<div class="breakVisTitle70">'+tr('Application','Application','App')+'</div><div class="breakVisRow70"><span>'+tr('Aujourd’hui','Today','Heute')+'</span><label><input id="showLunchToday70" type="checkbox"> Midi</label><label><input id="showGapsToday70" type="checkbox"> '+tr('Trous','Free','Freistunden')+'</label></div><div class="breakVisRow70"><span>'+tr('Semaine','Week','Woche')+'</span><label><input id="showLunchWeek70" type="checkbox"> Midi</label><label><input id="showGapsWeek70" type="checkbox"> '+tr('Trous','Free','Freistunden')+'</label></div><div class="breakVisTitle70">Widget</div><div id="breakWidget70"></div>';
                  const a=loadAdv(),defs=[['showLunchToday70','showLunchToday'],['showGapsToday70','showBreaksToday'],['showLunchWeek70','showLunchWeek'],['showGapsWeek70','showBreaksWeek']];
                  defs.forEach(([id,key])=>{const el=document.getElementById(id);if(!el)return;el.checked=a[key]!==false;el.onchange=()=>{const n=loadAdv();n[key]=el.checked;saveAdv(n);applyBreakVisibility();if(typeof mode!=='undefined'&&mode==='today'&&typeof renderToday==='function')renderToday();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()}});
                  const widget=document.getElementById('breakWidget70'),l=document.getElementById('advShowLunch'),g=document.getElementById('advShowBreaks');
                  if(widget){const lr=l&&l.closest('label'),gr=g&&g.closest('label');if(lr)widget.appendChild(lr);if(gr)widget.appendChild(gr)}
                  const ll=document.getElementById('advShowLunchLabel'),gl=document.getElementById('advShowBreaksLabel');if(ll)ll.textContent='Midi';if(gl)gl.textContent=tr('Trous','Free','Freistunden');
                }

                function polishSettings(){
                  installBreakVisibility();
                  const title=document.querySelectorAll('#viewEdit .sectionHead h3');title.forEach(h=>{const s=(h.textContent||'').toLowerCase();if(s.includes('horaire')||s.includes('period')||s.includes('stunden'))h.textContent=tr('Horaires des 9 heures','9 period times','Zeiten der 9 Stunden')});
                  const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION;
                }

                function wrap(name,after){
                  const old=window[name];if(typeof old!=='function'||old.__stability70)return;
                  const w=function(){const r=old.apply(this,arguments);try{after()}catch(e){}return r};w.__stability70=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                function installWrappers(){
                  wrap('renderContext',()=>{syncCycleUi()});
                  wrap('renderWeek',()=>{syncCycleUi();paintWeek70()});
                  wrap('renderToday',()=>{applyBreakVisibility()});
                  wrap('renderEdit',()=>{syncCycleUi();polishSettings()});
                  wrap('refreshAdvancedFeatures',()=>{syncCycleUi();polishSettings();applyBreakVisibility();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()});
                  wrap('refreshSettingsV3',()=>{polishSettings();syncCycleUi()});
                  wrap('refreshCoursePaletteV4',()=>{polishSettings();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()});
                  wrap('refreshFineTuneUi',()=>{polishSettings();if(typeof mode!=='undefined'&&mode==='week')paintWeek70()});
                }

                const grid=document.getElementById('weekGrid');
                if(grid&&!grid.__stability70Observed){
                  grid.__stability70Observed=true;
                  new MutationObserver(ms=>{
                    if(painting)return;
                    const external=ms.some(m=>Array.from(m.addedNodes||[]).some(n=>!(n.nodeType===1&&(n.classList.contains('now70Bar')||n.classList.contains('now70Dot')))));
                    if(external)paintWeek70();
                  }).observe(grid,{childList:true,subtree:true});
                }

                function refresh(){installWrappers();syncCycleUi();polishSettings();applyBreakVisibility();paintWeek70()}
                window.refreshStability70=refresh;
                refresh();
                setInterval(()=>{if(typeof mode!=='undefined'&&mode==='week')paintWeek70()},30000);
              }catch(e){console.log('Stability70Ui',e)}
            })();
            """;
    }
}
