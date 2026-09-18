package com.wokgui.schedulewidget;

/** 6.65 pass: first-frame stability and a functional condensed-widget density slider. */
final class Feedback665Ui {
    private Feedback665Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback665){window.refreshFeedback665&&window.refreshFeedback665();return}
                window.__feedback665=true;

                const style=document.createElement('style');style.id='feedback665Style';style.textContent=`
                  /* The raw week renderer writes the default gap label before the final week
                     painter runs. Never expose that unfinished label for a single frame. */
                  html body #viewWeek #weekGrid .wc.gapCell>.cellLabel{display:none!important;visibility:hidden!important}
                  #widgetDensity664 .feedback664DensityRow{display:block!important}
                  #widgetDensity664 #advDensityLabel,#widgetDensity664 #advDensity{display:none!important}
                  #widgetDensity664 .feedback665DensityControl{display:grid;grid-template-columns:1fr auto;gap:8px 12px;align-items:center;width:100%;max-width:390px;margin:0 auto}
                  #widgetDensity664 .feedback665DensityControl input[type=range]{grid-column:1/-1;width:100%;height:30px;margin:0;accent-color:#1689e8}
                  #widgetDensity664 .feedback665DensityScale{grid-column:1/-1;display:grid;grid-template-columns:repeat(3,1fr);font-size:.67rem;color:#64748b;font-weight:700;margin-top:-6px}
                  #widgetDensity664 .feedback665DensityScale span:nth-child(2){text-align:center}
                  #widgetDensity664 .feedback665DensityScale span:last-child{text-align:right}
                  #widgetDensity664 .feedback665DensityValue{grid-column:1/-1;justify-self:center;min-width:112px;padding:5px 12px;border-radius:999px;background:#edf6ff;color:#0877c9;text-align:center;font-size:.75rem;font-weight:850}
                  #widgetDensity664 .feedback665DensityAuto{display:flex;align-items:center;justify-content:center;gap:8px;margin:9px auto 0;color:#586579;font-size:.73rem;font-weight:750;text-align:center}
                  #widgetDensity664 .feedback665DensityAuto input{width:18px;height:18px;accent-color:#1689e8}
                `;document.head.appendChild(style);

                function language(){const value=document.getElementById('languageSelect')?.value||'fr';return value==='en'||value==='de'?value:'fr'}
                function labels(){const l=language();return l==='de'?['Sehr kompakt','Normal','Luftig']:(l==='en'?['Very compact','Normal','Spacious']:['Très condensé','Normal','Aéré'])}
                function legacyPercent(value){return value==='compact'?20:(value==='comfortable'?80:50)}
                function densityValue(percent){return Number(percent)<34?'compact':(Number(percent)>66?'comfortable':'normal')}
                function clampPercent(value){return Math.max(0,Math.min(100,Math.round(Number(value)||0)))}
                function autoLabel(){const l=language();return l==='de'?'Automatisch an die Widget-Größe anpassen, um den ganzen Tag anzuzeigen':(l==='en'?'Automatically adapt to widget size to show the whole day':'Adapter automatiquement à la taille du widget pour afficher toute la journée')}
                function advanced(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}

                function applyWeekVisibilityBeforePaint(){
                  const grid=document.getElementById('weekGrid');if(!grid)return;
                  const a=advanced();grid.classList.toggle('hideWeekLunch70',a.showLunchWeek===false);grid.classList.toggle('hideWeekGaps70',a.showBreaksWeek===false);
                }
                function installDensitySlider(){
                  const select=document.getElementById('advDensity'),box=document.getElementById('widgetDensity664');if(!select||!box)return;
                  const row=select.closest('.advRow');if(!row)return;
                  select.tabIndex=-1;select.setAttribute('aria-hidden','true');
                  let control=document.getElementById('advDensityControl665');
                  if(!control){
                    control=document.createElement('div');control.id='advDensityControl665';control.className='feedback665DensityControl';
                    control.innerHTML='<input id="advDensitySlider665" type="range" min="0" max="100" step="1"><div class="feedback665DensityScale"><span></span><span></span><span></span></div><output id="advDensityValue665" class="feedback665DensityValue"></output>';
                    row.appendChild(control);
                  }
                  let auto=document.getElementById('advDensityAutoRow665');
                  if(!auto){
                    auto=document.createElement('label');auto.id='advDensityAutoRow665';auto.className='feedback665DensityAuto';
                    auto.innerHTML='<input id="advDensityAuto665" type="checkbox"><span></span>';
                    row.appendChild(auto);
                  }
                  const slider=document.getElementById('advDensitySlider665'),output=document.getElementById('advDensityValue665'),autoInput=document.getElementById('advDensityAuto665'),words=labels(),ticks=control.querySelectorAll('.feedback665DensityScale span');
                  slider.min='0';slider.max='100';slider.step='1';
                  ticks.forEach((tick,index)=>tick.textContent=words[index]);
                  auto.querySelector('span').textContent=autoLabel();
                  const paint=()=>{const percent=clampPercent(slider.value);output.textContent=percent+' %';output.setAttribute('aria-label',percent+' %');slider.setAttribute('aria-valuetext',percent+' %');slider.disabled=autoInput.checked;output.textContent=autoInput.checked?(language()==='de'?'Automatisch':(language()==='en'?'Automatic':'Automatique')):percent+' %'};
                  const persist=()=>{const a=advanced(),percent=clampPercent(slider.value);a.widgetDensityPercent=percent;a.widgetAutoDensity=autoInput.checked;a.density=densityValue(percent);select.value=a.density;try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(a))}catch(e){}paint()};
                  const sync=()=>{const a=advanced(),percent=Number.isFinite(Number(a.widgetDensityPercent))?clampPercent(a.widgetDensityPercent):legacyPercent(select.value);slider.value=String(percent);autoInput.checked=a.widgetAutoDensity===true;paint()};
                  if(!slider.__feedback665){
                    slider.__feedback665=true;
                    slider.setAttribute('aria-label',language()==='de'?'Widget-Kompaktheit':(language()==='en'?'Widget compactness':'Condensation du widget'));
                    slider.addEventListener('input',paint);
                    slider.addEventListener('change',persist);
                    autoInput.addEventListener('change',persist);
                    select.addEventListener('change',()=>queueMicrotask(sync));
                  }
                  sync();
                }

                let preparing=false,settingsPrepared=false;
                function refresh(){installDensitySlider();applyWeekVisibilityBeforePaint()}
                function prepareSettings(){
                  if(preparing)return;if(settingsPrepared){refresh();return}preparing=true;
                  try{
                    if(window.refreshSettingsV3)window.refreshSettingsV3();
                    if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                    if(window.refreshSettingsLayout)window.refreshSettingsLayout();
                    if(window.refreshFeedback664)window.refreshFeedback664();
                    refresh();
                    settingsPrepared=true;
                  }finally{preparing=false}
                }
                window.prepareSettingsOpen665=prepareSettings;
                window.refreshFeedback665=refresh;

                const oldWeek=window.renderWeek;
                if(typeof oldWeek==='function'&&!oldWeek.__feedback665){
                  const wrapped=function(){applyWeekVisibilityBeforePaint();return oldWeek.apply(this,arguments)};
                  wrapped.__feedback665=true;window.renderWeek=wrapped;try{renderWeek=wrapped}catch(e){}
                }
                ['refreshSettingsLayout','refreshAdvancedFeatures','refreshSettingsV3'].forEach(name=>{
                  const old=window[name];if(typeof old!=='function'||old.__feedback665)return;
                  const wrapped=function(){const result=old.apply(this,arguments);queueMicrotask(refresh);return result};
                  wrapped.__feedback665=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}
                });
                prepareSettings();
              }catch(e){console.error('Feedback665Ui',e)}
            })();
            """;
    }
}
