package com.wokgui.schedulewidget;

/** 6.78 pass: settings-only week selection, durable navigation, and automatic labels. */
final class Feedback678Ui {
    private Feedback678Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback678){window.refreshFeedback678&&window.refreshFeedback678();return}
                window.__feedback678=true;
                let navigating=false,lastNavTarget='',lastNavAt=0;

                function advanced(){
                  try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}
                }
                function language(){
                  try{const value=JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language;return value==='en'||value==='de'?value:'fr'}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const value=language();return value==='en'?en:(value==='de'?de:fr)}
                function count(){const a=advanced();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2))}
                function letters(){return ['A','B','C','D'].slice(0,count())}
                function letter(){
                  try{return letters().includes(currentWeek)?currentWeek:'A'}catch(e){return 'A'}
                }
                function modeName(){
                  try{if(typeof mode!=='undefined'&&['today','week','edit'].includes(mode))return mode}catch(e){}
                  const active=document.querySelector('.nav.active[data-mode]');return active?active.dataset.mode:'today'
                }

                const style=document.createElement('style');style.id='feedback678Style';style.textContent=`
                  html body .contextBar,html body #contextBar{display:none!important}
                  html body #viewEdit #weekModeBar{display:none!important}
                  html body #settingsSheet .settingBox:has(#advCycleTitle){display:none!important}
                  #settingsWeekCycle678{display:block!important;margin:0 0 7px!important;padding:8px 9px!important;border:1px solid #d4dde8!important;border-radius:10px!important;background:#fff!important;box-shadow:0 1px 2px #1522380d!important}
                  #settingsWeekCycle678 .settingTitle{text-align:center!important;font-size:.79rem!important;font-weight:850!important;margin:0 0 6px!important}
                  .weekCycleChoices678{display:grid!important;grid-template-columns:repeat(4,minmax(0,1fr))!important;gap:4px!important;width:100%!important}
                  .weekCurrentChoices678{display:flex!important;justify-content:center!important;align-items:center!important;flex-wrap:wrap!important;gap:5px!important;width:100%!important}
                  .weekCurrentChoice678{flex:0 0 44px!important}
                  .weekCycleChoice678,.weekCurrentChoice678{min-width:0!important;min-height:27px!important;border:1px solid #d4dde8!important;border-radius:999px!important;background:#fff!important;color:#4d5667!important;padding:3px 4px!important;font-size:.62rem!important;font-weight:820!important;line-height:1!important;white-space:nowrap!important;touch-action:manipulation!important}
                  .weekCycleChoice678.active,.weekCurrentChoice678.active{background:var(--set-accent,var(--blue,#0877f9))!important;border-color:var(--set-accent,var(--blue,#0877f9))!important;color:#fff!important}
                  .weekCurrentSettings678{margin-top:7px!important;padding-top:6px!important;border-top:1px solid #edf0f4!important}
                  .weekCurrentLabel678{text-align:center!important;font-size:.66rem!important;font-weight:820!important;margin:0 0 5px!important;color:var(--ink,#111936)!important}
                  html.singleWeek678 .weekCurrentSettings678{display:none!important}
                  html body .bottom,html body .bottom .nav{pointer-events:auto!important;touch-action:manipulation!important}
                  html body #edtImportReview:not(.show),html body #ocrPreview86:not(.show){pointer-events:none!important}
                  html body #viewToday .dayTitle h2{white-space:nowrap!important}
                  html body #viewWeek .weekTop{display:none!important;margin:0!important;height:0!important;min-height:0!important}
                  html body #viewEdit{padding-bottom:clamp(8px,2vh,18px)!important}
                  html body #viewEdit>#importPhoto{display:flex!important;width:min(78%,380px)!important;margin-left:auto!important;margin-right:auto!important;justify-content:center!important}
                  html body #editActionRow680{display:flex!important;gap:8px!important;align-items:stretch!important}
                  html body #editActionRow680>#addCourse,html body #editActionRow680>#addBulkCourses{flex:1 1 0!important;width:auto!important;min-width:0!important;margin:0!important}
                `;document.head.appendChild(style);

                function ensureSettings(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return null;
                  let box=document.getElementById('settingsWeekCycle678');
                  if(!box){
                    box=document.createElement('div');box.id='settingsWeekCycle678';box.className='settingBox';
                    box.innerHTML='<div class="settingTitle"></div><div class="weekCycleChoices678"></div><div class="weekCurrentSettings678"><div class="weekCurrentLabel678"></div><div class="weekCurrentChoices678"></div></div>';
                    const head=sheet.querySelector('.settingsHead');if(head&&head.nextSibling)sheet.insertBefore(box,head.nextSibling);else sheet.insertBefore(box,sheet.firstChild);
                  }
                  const cycle=box.querySelector('.weekCycleChoices678'),defs=[['1','1 seule'],['2','A / B'],['3','A / B / C'],['4','A / B / C / D']];
                  defs.forEach(([value,label])=>{let button=cycle.querySelector('[data-count="'+value+'"]');if(!button){button=document.createElement('button');button.type='button';button.className='weekCycleChoice678';button.dataset.count=value;cycle.appendChild(button)}button.textContent=value==='1'?tr('1 seule','1 week','1 Woche'):label;button.onclick=()=>applyCycle(Number(value))});
                  const current=box.querySelector('.weekCurrentChoices678');['A','B','C','D'].forEach(value=>{let button=current.querySelector('[data-week="'+value+'"]');if(!button){button=document.createElement('button');button.type='button';button.className='weekCurrentChoice678';button.dataset.week=value;button.textContent=value;current.appendChild(button)}button.onclick=()=>selectCurrent(value)});
                  return box;
                }

                function syncSettings(){
                  const box=ensureSettings();if(!box)return;
                  box.querySelector('.settingTitle').textContent=tr('Type de semaine','Week type','Wochentyp');
                  box.querySelector('.weekCurrentLabel678').textContent=tr('Semaine en cours','Current week','Aktuelle Woche');
                  const n=count(),current=letter();document.documentElement.classList.toggle('singleWeek678',n===1);
                  box.querySelectorAll('.weekCycleChoice678').forEach(button=>{const active=Number(button.dataset.count)===n;button.classList.toggle('active',active);button.setAttribute('aria-pressed',active?'true':'false')});
                  box.querySelectorAll('.weekCurrentChoice678').forEach(button=>{const visible=letters().includes(button.dataset.week),active=button.dataset.week===current;button.hidden=!visible;button.style.display=visible?'flex':'none';button.style.alignItems='center';button.style.justifyContent='center';button.classList.toggle('active',active);button.setAttribute('aria-pressed',active?'true':'false')});
                }

                function refreshIndicators(){
                  const n=count(),single=n===1,w=letter();document.documentElement.classList.toggle('singleWeek678',single);
                  const today=document.getElementById('todayTitle');if(today){const base=String(today.textContent||tr('Aujourd’hui','Today','Heute')).split('·')[0].trim();today.textContent=single?base:(base+' · '+tr('Jour ','Day ','Tag ')+w)}
                  const week=document.querySelector('#viewWeek .weekTop h2');if(week){week.textContent=single?tr('Aperçu semaine','Week overview','Wochenübersicht'):(tr('Aperçu · Semaine ','Overview · Week ','Übersicht · Woche '));if(!single){const span=document.createElement('span');span.id='weekTitleLetter';span.className='weekLetter';span.textContent=w;week.appendChild(span)}}
                  const edit=document.getElementById('editDayTitle');if(edit){const base=String(edit.textContent||'').split('·')[0].trim();edit.textContent=single?base:(base+' · '+tr('Semaine ','Week ','Woche ')+w)}
                  syncSettings();
                }

                function unlockNavigation(){
                  document.querySelectorAll('.weekSwapCover669').forEach(node=>node.remove());
                  document.body.classList.remove('cycle69Busy','cycleSwitchBusy');
                  document.querySelectorAll('.nav[data-mode]').forEach(nav=>{nav.disabled=false;nav.style.setProperty('pointer-events','auto','important')});
                  const bottom=document.querySelector('.bottom');if(bottom)bottom.style.setProperty('pointer-events','auto','important');
                }

                function enforceView(target){
                  const id='view'+target.charAt(0).toUpperCase()+target.slice(1),view=document.getElementById(id);if(!view)return false;
                  document.querySelectorAll('main.wrap>.view').forEach(node=>node.classList.toggle('active',node===view));
                  document.querySelectorAll('.nav[data-mode]').forEach(nav=>nav.classList.toggle('active',nav.dataset.mode===target));
                  try{mode=target}catch(e){}
                  return true;
                }

                function navigate(target){
                  if(navigating||!['today','week','edit'].includes(target))return;navigating=true;unlockNavigation();
                  try{activeWeek=letter();if(typeof weeks!=='undefined'&&weeks[activeWeek])state=weeks[activeWeek]}catch(e){}
                  enforceView(target);refreshIndicators();
                  requestAnimationFrame(()=>{
                    try{if(typeof window.setModeFromAndroid==='function')window.setModeFromAndroid(target);else if(typeof window.setMode==='function')window.setMode(target)}catch(e){console.log('Feedback678Ui navigation',e)}
                    enforceView(target);refreshIndicators();
                    requestAnimationFrame(()=>{unlockNavigation();enforceView(target);refreshIndicators();if(target==='week'&&window.fitActiveWeek676)window.fitActiveWeek676();navigating=false});
                  });
                }
                window.navigateStable678=navigate;

                function selectCurrent(value){
                  if(!letters().includes(value))return;
                  try{currentWeek=value;activeWeek=value;if(typeof weeks!=='undefined'&&weeks[value])state=weeks[value]}catch(e){}
                  try{if(AndroidSchedule.setCurrentWeek)AndroidSchedule.setCurrentWeek(value)}catch(e){}
                  try{if(typeof render==='function')render()}catch(e){}
                  refreshIndicators();
                }

                function applyCycle(n){
                  if(![1,2,3,4].includes(n))return;
                  try{if(typeof window.applyWeekMode73==='function')window.applyWeekMode73(n);else{const a=advanced();a.singleWeek=n===1;a.cycleLength=n===1?2:n;AndroidSchedule.saveAdvancedSettings(JSON.stringify(a))}}catch(e){}
                  requestAnimationFrame(()=>requestAnimationFrame(()=>{try{activeWeek=letter()}catch(e){}refreshIndicators();unlockNavigation()}));
                }

                document.addEventListener('pointerdown',event=>{const nav=event.target&&event.target.closest?event.target.closest('.nav[data-mode]'):null;if(nav){unlockNavigation();document.querySelectorAll('.nav[data-mode]').forEach(item=>item.classList.toggle('active',item===nav))}},true);
                document.addEventListener('click',event=>{
                  const nav=event.target&&event.target.closest?event.target.closest('.nav[data-mode]'):null;if(!nav)return;
                  event.preventDefault();event.stopPropagation();if(event.stopImmediatePropagation)event.stopImmediatePropagation();
                  const target=nav.dataset.mode,now=Date.now();if(target===lastNavTarget&&now-lastNavAt<180)return;lastNavTarget=target;lastNavAt=now;navigate(target);
                },true);
                document.addEventListener('click',event=>{const action=event.target&&event.target.closest?event.target.closest('#edtImportReview .irApply,#edtImportReview .irCancel,#ocrPreviewImport86,#ocrPreviewCorrect86,#ocrPreviewCancel86'):null;if(action)setTimeout(()=>{unlockNavigation();refreshIndicators()},0)},true);

                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback678)return;const wrapped=function(){const result=old.apply(this,arguments);refreshIndicators();return result};wrapped.__feedback678=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}}
                ['renderToday','renderWeek','renderEdit','renderContext'].forEach(wrap);
                const languageSelect=document.getElementById('languageSelect');if(languageSelect)languageSelect.addEventListener('change',()=>requestAnimationFrame(refreshIndicators));
                const settings=document.getElementById('settingsModal');if(settings&&!settings.__feedback678Observed){settings.__feedback678Observed=true;new MutationObserver(()=>{if(settings.classList.contains('show')||settings.getAttribute('data-edt-open')==='true')refreshIndicators()}).observe(settings,{attributes:true,attributeFilter:['class','data-edt-open']})}

                try{activeWeek=letter();if(typeof weeks!=='undefined'&&weeks[activeWeek])state=weeks[activeWeek]}catch(e){}
                function refresh(){unlockNavigation();ensureSettings();refreshIndicators();enforceView(modeName())}
                window.refreshFeedback678=refresh;refresh();
              }catch(e){console.error('Feedback678Ui',e)}
            })();
            """;
    }
}