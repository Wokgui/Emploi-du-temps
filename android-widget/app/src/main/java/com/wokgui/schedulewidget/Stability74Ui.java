package com.wokgui.schedulewidget;

final class Stability74Ui {
    private Stability74Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__stability74V1){if(window.refreshStability74)window.refreshStability74();return}
                window.__stability74V1=true;
                const APP_VERSION='6.14';
                let queued=false;
                let arranging=false;

                function lang(){
                  try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=lang();return l==='en'?en:(l==='de'?de:fr)}

                const style=document.createElement('style');
                style.id='stability74Style';
                style.textContent=`
                  /* Previews sit immediately above Theme, with each caption centred over its own preview. */
                  html body #settingsSheet .previewGrid{
                    margin:10px 0 9px!important;
                    gap:10px!important;
                    align-items:start!important
                  }
                  html body #settingsSheet .previewGrid>div{min-width:0!important}
                  html body #settingsSheet .previewLabel{
                    width:100%!important;
                    margin:0 0 6px!important;
                    text-align:center!important;
                    font-size:.76rem!important;
                    font-weight:850!important;
                    color:var(--ink,#111936)!important;
                    line-height:1.15!important
                  }
                  html body #settingsSheet .previewGrid .preview{margin:0 auto!important;width:100%!important;box-sizing:border-box!important}

                  /* Requested order: Theme -> palettes -> Midi/trous (including full colour controls). */
                  html body #paletteSettingRoot{margin-top:9px!important}
                  html body #breakDisplaySetting{margin-top:9px!important}

                  /* Full colours are now a subsection of Midi et trous, not a separate settings tile. */
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74{
                    border:0!important;
                    border-radius:0!important;
                    box-shadow:none!important;
                    background:transparent!important;
                    margin:11px 0 0!important;
                    padding:11px 0 0!important;
                    border-top:1px solid var(--line,#dce3eb)!important
                  }
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74>.settingTitle{
                    text-align:center!important;
                    margin:0 0 5px!important;
                    font-size:.76rem!important;
                    font-weight:850!important
                  }
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74>.coursePaletteHint{
                    text-align:center!important;
                    margin:0 0 9px!important;
                    line-height:1.3!important
                  }
                  html body #breakDisplaySetting #fineSpecialColors.embeddedFullColors74 .specialColorRow>span{text-align:center!important}

                  /* Midi/trous is exactly three aligned functional rows: Today, Week, Widget. */
                  html body #breakVisibility70{
                    display:grid!important;
                    grid-template-columns:92px minmax(0,1fr) minmax(0,1fr)!important;
                    column-gap:7px!important;
                    row-gap:9px!important;
                    align-items:center!important;
                    margin:8px 0 0!important;
                    padding:0!important;
                    border:0!important
                  }
                  html body #breakVisibility70>.breakVisTitle70:first-child{display:none!important}
                  html body #breakVisibility70>.breakVisRow70{display:contents!important}
                  html body #breakVisibility70>.breakVisRow70>span{
                    text-align:center!important;
                    font-size:.72rem!important;
                    font-weight:850!important;
                    color:var(--ink,#111936)!important
                  }
                  html body #breakVisibility70>.breakVisRow70 label,
                  html body #breakWidget70 label{
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    gap:5px!important;
                    margin:0!important;
                    min-width:0!important;
                    font-size:.71rem!important;
                    line-height:1.15!important;
                    white-space:nowrap!important
                  }
                  html body #breakVisibility70 #breakWidgetTitle74{
                    display:flex!important;
                    grid-column:1!important;
                    grid-row:3!important;
                    align-items:center!important;
                    justify-content:center!important;
                    margin:0!important;
                    padding:0!important;
                    text-align:center!important;
                    font-size:.72rem!important;
                    font-weight:850!important;
                    color:var(--ink,#111936)!important
                  }
                  html body #breakVisibility70 #breakWidget70{
                    grid-column:2 / 4!important;
                    grid-row:3!important;
                    display:grid!important;
                    grid-template-columns:1fr 1fr!important;
                    gap:7px!important;
                    align-items:center!important;
                    margin:0!important;
                    padding:0!important;
                    border:0!important
                  }
                  html body #breakVisibility70 #breakWidget70:empty{display:grid!important}

                  @media(max-width:390px){
                    html body #settingsSheet .previewGrid{gap:7px!important}
                    html body #breakVisibility70{grid-template-columns:78px minmax(0,1fr) minmax(0,1fr)!important;column-gap:4px!important}
                    html body #breakVisibility70>.breakVisRow70 label,
                    html body #breakWidget70 label{font-size:.67rem!important;gap:3px!important}
                  }
                `;
                document.head.appendChild(style);

                function findThemeBox(){
                  const t=document.getElementById('themeTitle');return t&&t.closest?t.closest('.settingBox'):null;
                }

                function arrangeOrder(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  const theme=findThemeBox(),preview=sheet.querySelector('.previewGrid'),palette=document.getElementById('paletteSettingRoot'),breaks=document.getElementById('breakDisplaySetting');
                  if(theme&&preview&&preview.nextElementSibling!==theme)sheet.insertBefore(preview,theme);
                  if(theme&&palette&&theme.nextElementSibling!==palette)theme.insertAdjacentElement('afterend',palette);
                  if(palette&&breaks&&palette.nextElementSibling!==breaks)palette.insertAdjacentElement('afterend',breaks);
                }

                function embedFullColors(){
                  const breaks=document.getElementById('breakDisplaySetting'),full=document.getElementById('fineSpecialColors');
                  if(!breaks||!full)return;
                  full.classList.add('embeddedFullColors74');
                  if(full.parentElement!==breaks)breaks.appendChild(full);
                }

                function fixPreviewLabels(){
                  const a=document.getElementById('appPreviewLabel'),w=document.getElementById('widgetPreviewLabel');
                  if(a)a.textContent=tr('Aperçu de l’application','App preview','App-Vorschau');
                  if(w)w.textContent=tr('Aperçu du widget','Widget preview','Widget-Vorschau');
                }

                function normalizeBreakWidgetRow(){
                  const root=document.getElementById('breakVisibility70');if(!root)return;
                  const widget=document.getElementById('breakWidget70');
                  let title=document.getElementById('breakWidgetTitle74');
                  if(!title){
                    const candidates=[...root.querySelectorAll(':scope > .breakVisTitle70')];
                    title=candidates.find(x=>x!==root.querySelector(':scope > .breakVisTitle70:first-child') && /widget/i.test(String(x.textContent||'')));
                    if(title)title.id='breakWidgetTitle74';
                  }
                  if(title)title.textContent='Widget';

                  /* Legacy refreshes may move these two labels. Put both real widget toggles back into the Widget row. */
                  if(widget){
                    const lunch=document.getElementById('advShowLunch'),gap=document.getElementById('advShowBreaks');
                    const lr=lunch&&lunch.closest?lunch.closest('label'):null;
                    const gr=gap&&gap.closest?gap.closest('label'):null;
                    if(lr&&lr.parentElement!==widget)widget.appendChild(lr);
                    if(gr&&gr.parentElement!==widget)widget.appendChild(gr);
                    const ll=document.getElementById('advShowLunchLabel'),gl=document.getElementById('advShowBreaksLabel');
                    if(ll)ll.textContent='Midi';
                    if(gl)gl.textContent=tr('Trous','Free periods','Freistunden');
                  }
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v&&v.textContent!=='Version '+APP_VERSION)v.textContent='Version '+APP_VERSION}

                function refresh(){
                  queued=false;if(arranging)return;arranging=true;
                  try{arrangeOrder();fixPreviewLabels();normalizeBreakWidgetRow();embedFullColors();setVersion()}finally{arranging=false}
                }
                window.refreshStability74=refresh;

                function schedule(){if(queued)return;queued=true;requestAnimationFrame(refresh)}
                function wrap(name){
                  const old=window[name];if(typeof old!=='function'||old.__stability74)return;
                  const w=function(){const r=old.apply(this,arguments);schedule();return r};w.__stability74=true;window[name]=w;try{eval(name+'=w')}catch(e){}
                }
                ['refreshSettingsV3','refreshCoursePaletteV4','refreshFineTuneUi','refreshAdvancedFeatures','refreshStability70','refreshStability71','refreshStability72','refreshStability73'].forEach(wrap);

                const sheet=document.getElementById('settingsSheet');
                if(sheet&&!sheet.__stability74Observed){
                  sheet.__stability74Observed=true;
                  new MutationObserver(()=>{if(!arranging)schedule()}).observe(sheet,{childList:true,subtree:true});
                }

                refresh();requestAnimationFrame(refresh);setTimeout(refresh,120);
              }catch(e){console.log('Stability74Ui',e)}
            })();
            """;
    }
}
