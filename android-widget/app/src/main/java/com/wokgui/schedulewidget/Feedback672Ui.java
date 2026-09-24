package com.wokgui.schedulewidget;

/** 6.72 pass: independent top and bottom widget bars with progress, solid colour, or none. */
final class Feedback672Ui {
    private Feedback672Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback672){window.refreshFeedback672&&window.refreshFeedback672();return}
                window.__feedback672=true;

                const style=document.createElement('style');style.id='feedback672Style';style.textContent=`
                  #widgetEdgeBars672{margin:2px 0 10px;padding:9px 8px 10px;border:1px solid #dce4ee;border-radius:13px;background:#f8fbfe}
                  #widgetEdgeBars672 .bar672Title{text-align:center;font-size:.76rem;font-weight:850;color:#233047;margin:0 0 8px}
                  #widgetEdgeBars672 .bar672Grid{display:grid;grid-template-columns:minmax(68px,.75fr) minmax(100px,1.05fr) 46px;gap:7px;align-items:center}
                  #widgetEdgeBars672 .bar672Label{text-align:center;font-size:.72rem;font-weight:800;color:#586579}
                  #widgetEdgeBars672 select{width:100%;max-width:150px;min-width:0;min-height:30px;justify-self:center;text-align:center;text-align-last:center;border:1px solid #cfd9e5;border-radius:8px;background:#fff;color:#233047;padding:5px 22px 5px 7px;font-size:.68rem;font-weight:750}
                  #widgetEdgeBars672 input[type=color]{width:46px;height:36px;padding:2px;border:1px solid #cfd9e5;border-radius:9px;background:#fff;justify-self:center;transition:opacity .12s ease}
                  #widgetEdgeBars672 input[type=color]:not(.bar672Active){visibility:hidden;opacity:0;pointer-events:none}
                  #widgetEdgeBars672 .bar672Divider{grid-column:1/-1;height:1px;background:#e4eaf1;margin:0}
                  @media(max-width:370px){#widgetEdgeBars672 .bar672Grid{grid-template-columns:64px minmax(94px,1fr) 42px;gap:5px}#widgetEdgeBars672 input[type=color]{width:40px}}
                `;document.head.appendChild(style);

                function language(){const value=document.getElementById('languageSelect')?.value||document.documentElement.lang||'fr';return String(value).toLowerCase().startsWith('de')?'de':(String(value).toLowerCase().startsWith('en')?'en':'fr')}
                function tr(fr,en,de){const l=language();return l==='de'?de:(l==='en'?en:fr)}
                function read(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function write(value){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(value))}catch(e){}}
                function mode(value){return value==='color'||value==='none'?value:'progress'}
                function color(value){return /^#[0-9a-f]{6}$/i.test(String(value||''))?String(value).toLowerCase():'#1677e8'}

                function install(){
                  const title=document.getElementById('advWidgetTitle'),box=title&&title.closest('.settingBox');if(!title||!box)return;
                  let root=document.getElementById('widgetEdgeBars672');
                  if(!root){
                    root=document.createElement('div');root.id='widgetEdgeBars672';
                    root.innerHTML='<div class="bar672Title"></div><div class="bar672Grid"><label class="bar672Label" for="widgetTopBarMode672"></label><select id="widgetTopBarMode672"><option value="progress"></option><option value="color"></option><option value="none"></option></select><input id="widgetTopBarColor672" type="color" value="#1677e8"><div class="bar672Divider"></div><label class="bar672Label" for="widgetBottomBarMode672"></label><select id="widgetBottomBarMode672"><option value="progress"></option><option value="color"></option><option value="none"></option></select><input id="widgetBottomBarColor672" type="color" value="#1677e8"></div>';
                    title.insertAdjacentElement('afterend',root);
                  }
                  const titleText=root.querySelector('.bar672Title'),labels=root.querySelectorAll('.bar672Label');
                  if(titleText)titleText.textContent=tr('Barres en haut et en bas','Top and bottom bars','Obere und untere Leiste');
                  if(labels[0])labels[0].textContent=tr('En haut','Top','Oben');if(labels[1])labels[1].textContent=tr('En bas','Bottom','Unten');
                  const optionLabels=[tr('Progression','Progress','Fortschritt'),tr('Couleur fixe','Solid colour','Feste Farbe'),tr('Aucune','None','Keine')];
                  const topMode=document.getElementById('widgetTopBarMode672'),bottomMode=document.getElementById('widgetBottomBarMode672');
                  [topMode,bottomMode].forEach(select=>{if(select)[...select.options].forEach((option,index)=>option.textContent=optionLabels[index])});
                  const current=read(),topColor=document.getElementById('widgetTopBarColor672'),bottomColor=document.getElementById('widgetBottomBarColor672');
                  topMode.value=mode(current.widgetTopBarMode);bottomMode.value=mode(current.widgetBottomBarMode);
                  topColor.value=color(current.widgetTopBarColor);bottomColor.value=color(current.widgetBottomBarColor);
                  const paint=()=>{topColor.classList.toggle('bar672Active',topMode.value==='color');bottomColor.classList.toggle('bar672Active',bottomMode.value==='color');topColor.disabled=topMode.value!=='color';bottomColor.disabled=bottomMode.value!=='color'};
                  const persist=()=>{const next=read();next.widgetTopBarMode=mode(topMode.value);next.widgetTopBarColor=color(topColor.value);next.widgetBottomBarMode=mode(bottomMode.value);next.widgetBottomBarColor=color(bottomColor.value);write(next);paint()};
                  for(const control of [topMode,bottomMode,topColor,bottomColor])if(control&&!control.__feedback672){control.__feedback672=true;control.addEventListener('change',persist)}
                  topColor.setAttribute('aria-label',tr('Couleur de la barre supérieure','Top bar colour','Farbe der oberen Leiste'));
                  bottomColor.setAttribute('aria-label',tr('Couleur de la barre inférieure','Bottom bar colour','Farbe der unteren Leiste'));
                  const courseProgress=document.getElementById('advShowProgressLabel');if(courseProgress)courseProgress.textContent=tr('Progression du cours en cours','Current class progress','Fortschritt der laufenden Stunde');
                  paint();
                }
                function refresh(){install()}
                window.refreshFeedback672=refresh;
                ['refreshSettingsLayout','refreshAdvancedFeatures','refreshSettingsV3','prepareSettingsOpen665'].forEach(name=>{
                  const old=window[name];if(typeof old!=='function'||old.__feedback672)return;
                  const wrapped=function(){const result=old.apply(this,arguments);queueMicrotask(refresh);return result};
                  wrapped.__feedback672=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}
                });
                const languageSelect=document.getElementById('languageSelect');if(languageSelect)languageSelect.addEventListener('change',()=>queueMicrotask(refresh));
                refresh();
              }catch(e){console.error('Feedback672Ui',e)}
            })();
            """;
    }
}