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
                `;document.head.appendChild(style);

                function language(){const value=document.getElementById('languageSelect')?.value||'fr';return value==='en'||value==='de'?value:'fr'}
                function labels(){const l=language();return l==='de'?['Kompakt','Normal','Komfortabel']:(l==='en'?['Compact','Normal','Comfortable']:['Compact','Normal','Confortable'])}
                function densityIndex(value){return value==='compact'?0:(value==='comfortable'?2:1)}
                function densityValue(index){return Number(index)<=0?'compact':(Number(index)>=2?'comfortable':'normal')}
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
                    control.innerHTML='<input id="advDensitySlider665" type="range" min="0" max="2" step="1"><div class="feedback665DensityScale"><span></span><span></span><span></span></div><output id="advDensityValue665" class="feedback665DensityValue"></output>';
                    row.appendChild(control);
                  }
                  const slider=document.getElementById('advDensitySlider665'),output=document.getElementById('advDensityValue665'),words=labels(),ticks=control.querySelectorAll('.feedback665DensityScale span');
                  ticks.forEach((tick,index)=>tick.textContent=words[index]);
                  const sync=()=>{const index=densityIndex(select.value);slider.value=String(index);output.textContent=words[index];slider.setAttribute('aria-valuetext',words[index])};
                  if(!slider.__feedback665){
                    slider.__feedback665=true;
                    slider.setAttribute('aria-label',language()==='de'?'Widget-Kompaktheit':(language()==='en'?'Widget compactness':'Condensation du widget'));
                    slider.addEventListener('input',()=>{const current=labels(),index=Number(slider.value);output.textContent=current[index];slider.setAttribute('aria-valuetext',current[index])});
                    slider.addEventListener('change',()=>{const value=densityValue(slider.value);if(select.value!==value)select.value=value;select.dispatchEvent(new Event('change',{bubbles:true}))});
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
