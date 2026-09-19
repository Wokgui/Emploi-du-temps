package com.wokgui.schedulewidget;

/** Final presentation pass for lunch/free-period settings and week lunch rendering. */
final class SettingsLunchPolish653Ui {
    private SettingsLunchPolish653Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__settingsLunchPolish653){
                  if(window.refreshSettingsLunchPolish653)window.refreshSettingsLunchPolish653();
                  return;
                }
                window.__settingsLunchPolish653=true;
                let queued=false;

                function language(){
                  try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr'))}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=language();return l==='en'?en:(l==='de'?de:fr)}
                function toMin(v){const m=String(v||'').match(/([0-9]{1,2}):([0-9]{2})/);return m?Number(m[1])*60+Number(m[2]):-1}

                const style=document.createElement('style');
                style.id='settingsLunchPolish653Style';
                style.textContent=`
                  /* The square already opens the complete colour picker; the extra shade bar
                     duplicated that job and made the colour section harder to understand. */
                  html body #fineSpecialColors .specialColorTone{display:none!important}
                  html body #fineSpecialColors .specialColorRow{
                    grid-template-columns:minmax(92px,1fr) 52px!important;
                    max-width:280px!important;
                    margin-left:auto!important;
                    margin-right:auto!important;
                    column-gap:10px!important
                  }
                  html body #fineSpecialColors .specialColorRow>span{text-align:right!important}
                  html body #fineSpecialColors .specialColorPick{justify-self:start!important}
                  html body #fineSpecialColors .specialWidgetTitle{text-align:center!important}

                  /* Keep the Widget-only controls on exactly the same baseline as Today/Week. */
                  html body #breakVisibility70{
                    grid-template-columns:106px minmax(0,1fr) minmax(0,1fr)!important;
                    grid-auto-rows:minmax(32px,auto)!important;
                    align-items:center!important;
                    row-gap:4px!important
                  }
                  html body #breakVisibility70 #breakWidgetTitle74{
                    grid-column:1!important;
                    grid-row:3!important;
                    align-self:center!important;
                    justify-self:stretch!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    margin:0!important;
                    padding:0!important;
                    line-height:1.1!important;
                    transform:none!important;
                    min-height:32px!important;
                    text-align:center!important
                  }
                  html body #breakVisibility70 #breakWidget70{
                    grid-column:2 / 4!important;
                    grid-row:3!important;
                    align-self:center!important;
                    display:grid!important;
                    grid-template-columns:1fr 1fr!important;
                    align-items:center!important;
                    gap:6px!important;
                    margin:0!important;
                    min-height:32px!important
                  }
                  html body #breakVisibility70 #breakWidget70 label{
                    min-height:32px!important;
                    align-items:center!important;
                    justify-content:center!important;
                    white-space:nowrap!important
                  }

                  /* 6.55: strong lunch limits cover day cells only; never the hour column. */
                  #weekGrid .lunch653Top{
                    border-bottom-width:var(--week-strong-line,2px)!important;
                    border-bottom-style:solid!important;
                    border-bottom-color:var(--ft-midi-border,#C7AA62)!important
                  }
                  #weekGrid .lunch653Bottom{
                    border-bottom-width:var(--week-strong-line,2px)!important;
                    border-bottom-style:solid!important;
                    border-bottom-color:var(--ft-midi-border,#C7AA62)!important
                  }

                  /* An empty weekday still shows Midi in the same lunch band as the other days. */
                  #weekGrid .wc.lunch655Synthetic{
                    background:var(--ft-midi)!important;
                    color:var(--ft-midi-ink)!important;
                    border-radius:0!important;
                    box-shadow:inset 0 0 0 1px var(--ft-midi-border)!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important
                  }
                  #weekGrid .wc.lunch655Synthetic *{color:var(--ft-midi-ink)!important}
                  #weekGrid .lunch655Label{
                    display:inline-flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    gap:3px!important;
                    font-weight:850!important;
                    text-align:center!important;
                    width:100%!important
                  }
                  #weekGrid .lunch655Label:before{
                    content:'🍴';
                    display:inline-block;
                    font-size:.72em;
                    vertical-align:middle
                  }

                  @media(max-width:390px){
                    html body #breakVisibility70{grid-template-columns:94px minmax(0,1fr) minmax(0,1fr)!important}
                    html body #breakVisibility70 #breakWidget70 label{font-size:.64rem!important;gap:3px!important}
                  }
                `;
                document.head.appendChild(style);

                function simplifyColourSettings(){
                  const box=document.getElementById('fineSpecialColors');if(!box)return;
                  const title=box.querySelector(':scope > .settingTitle');
                  if(title)title.textContent=tr('Couleurs de Midi et des trous','Lunch and free-period colours','Farben für Mittag und Freistunden');
                  const hint=box.querySelector(':scope > .coursePaletteHint');
                  if(hint)hint.textContent=tr(
                    'Appuie sur le carré pour choisir directement la couleur.',
                    'Tap the square to choose the colour directly.',
                    'Tippe auf das Quadrat, um die Farbe direkt auszuwählen.'
                  );
                  box.querySelectorAll('.specialColorTone').forEach(x=>{x.tabIndex=-1;x.setAttribute('aria-hidden','true')});
                  const separate=box.querySelector('.specialWidgetTitle');
                  if(separate)separate.textContent=tr('Couleurs du widget uniquement','Widget-only colours','Farben nur für das Widget');
                }

                function clarifyWidgetBreakRow(){
                  const root=document.getElementById('breakVisibility70');if(!root)return;
                  const widget=document.getElementById('breakWidget70');
                  let title=document.getElementById('breakWidgetTitle74');
                  if(!title){
                    const candidates=[...root.querySelectorAll(':scope > .breakVisTitle70')];
                    title=candidates.find(x=>/widget/i.test(String(x.textContent||'')));
                    if(title)title.id='breakWidgetTitle74';
                  }
                  if(title)title.textContent=tr('Widget uniquement','Widget only','Nur Widget');

                  if(widget){
                    const lunch=document.getElementById('advShowLunch'),gaps=document.getElementById('advShowBreaks');
                    const lunchLabel=lunch&&lunch.closest?lunch.closest('label'):null;
                    const gapsLabel=gaps&&gaps.closest?gaps.closest('label'):null;
                    if(lunchLabel&&lunchLabel.parentElement!==widget)widget.appendChild(lunchLabel);
                    if(gapsLabel&&gapsLabel.parentElement!==widget)widget.appendChild(gapsLabel);
                  }
                  const ll=document.getElementById('advShowLunchLabel');
                  const gl=document.getElementById('advShowBreaksLabel');
                  if(ll)ll.textContent=tr('Afficher midi','Show lunch','Mittag anzeigen');
                  if(gl)gl.textContent=tr('Afficher les trous','Show free periods','Freistunden anzeigen');
                }

                function rowsOf(grid){
                  const rows=[];if(!grid)return {rows,heads:[]};
                  const heads=[...grid.querySelectorAll(':scope > .wh.day')];
                  const timeColumns=[...grid.querySelectorAll(':scope > .wh.timecol')];
                  const dayCount=heads.length||5;
                  for(const time of timeColumns){
                    const found=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];
                    if(found.length<2)continue;
                    const cells=[];let n=time.nextElementSibling;
                    while(n&&cells.length<dayCount){if(n.classList&&n.classList.contains('wc'))cells.push(n);n=n.nextElementSibling}
                    if(cells.length===dayCount)rows.push({time,start:toMin(found[0]),end:toMin(found[1]),cells});
                  }
                  rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);
                  return {rows,heads};
                }

                function isLunchCell(cell){
                  return !!(cell&&(
                    cell.classList.contains('lunchCell')||
                    cell.classList.contains('dynamicLunchCell')||
                    cell.classList.contains('nativeLunchCell')||
                    cell.classList.contains('finalLunchCell')
                  ));
                }

                function clearSyntheticLunch(grid){
                  grid.querySelectorAll('.lunch655Label').forEach(x=>x.remove());
                  grid.querySelectorAll('.lunch655Synthetic').forEach(x=>x.classList.remove('lunch655Synthetic'));
                }

                function completeEmptyDays(rows,first,last){
                  if(first<0||last<first||!rows[first])return;
                  const dayCount=rows[first].cells.length;
                  for(let day=0;day<dayCount;day++){
                    const band=[];
                    for(let r=first;r<=last;r++)if(rows[r]&&rows[r].cells[day])band.push(rows[r].cells[day]);
                    if(!band.length)continue;
                    if(band.some(isLunchCell))continue;
                    if(band.some(cell=>String(cell.textContent||'').trim()!==''))continue;
                    band.forEach(cell=>cell.classList.add('lunch655Synthetic'));
                    const label=document.createElement('span');
                    label.className='cellLabel lunch655Label';
                    label.textContent=tr('Midi','Lunch','Mittag');
                    band[0].appendChild(label);
                  }
                }

                function paintFullWidthLunchBoundary(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  grid.querySelectorAll('.lunch653Top,.lunch653Bottom').forEach(x=>x.classList.remove('lunch653Top','lunch653Bottom'));
                  clearSyntheticLunch(grid);
                  if(grid.classList.contains('hideWeekLunch70'))return;
                  const data=rowsOf(grid),rows=data.rows,heads=data.heads;
                  if(!rows.length)return;
                  const flags=rows.map(row=>row.cells.some(isLunchCell));
                  let i=0;
                  while(i<flags.length){
                    if(!flags[i]){i++;continue}
                    let j=i;while(j+1<flags.length&&flags[j+1])j++;
                    completeEmptyDays(rows,i,j);
                    const topCells=i>0?rows[i-1].cells:heads;
                    topCells.forEach(cell=>cell&&cell.classList.add('lunch653Top'));
                    rows[j].cells.forEach(cell=>cell&&cell.classList.add('lunch653Bottom'));
                    i=j+1;
                  }
                }

                function refresh(){
                  queued=false;
                  simplifyColourSettings();
                  clarifyWidgetBreakRow();
                  paintFullWidthLunchBoundary();
                }
                function schedule(){if(queued)return;queued=true;requestAnimationFrame(refresh)}
                window.refreshSettingsLunchPolish653=refresh;

                const modal=document.getElementById('settingsModal');
                if(modal&&!modal.__settingsLunchPolish653Observed){
                  modal.__settingsLunchPolish653Observed=true;
                  new MutationObserver(()=>{if(modal.classList.contains('show'))schedule()}).observe(modal,{attributes:true,attributeFilter:['class']});
                }
                const special=document.getElementById('fineSpecialColors');
                if(special&&!special.__settingsLunchPolish653Observed){
                  special.__settingsLunchPolish653Observed=true;
                  new MutationObserver(schedule).observe(special,{childList:true,subtree:false});
                }
                const grid=document.getElementById('weekGrid');
                if(grid&&!grid.__settingsLunchPolish653Observed){
                  grid.__settingsLunchPolish653Observed=true;
                  new MutationObserver(schedule).observe(grid,{childList:true,attributes:true,attributeFilter:['class']});
                }
                refresh();
              }catch(e){console.log('SettingsLunchPolish653Ui',e)}
            })();
            """;
    }
}
