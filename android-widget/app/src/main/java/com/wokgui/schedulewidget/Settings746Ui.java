package com.wokgui.schedulewidget;

/** 7.46: final settings geometry owner for the remaining visual regressions. */
final class Settings746Ui {
    private Settings746Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__settings746){window.refreshSettings746&&window.refreshSettings746();return}
                window.__settings746=true;

                const style=document.createElement('style');
                style.id='settings746Style';
                style.textContent=\`
                  /* Identical title-to-slider rhythm for both text-size controls. */
                  html body #settingsSheet #settingsText692 .fontApp745 .settingTitle,
                  html body #settingsSheet #settingsText692 .fontWidget745 .settingTitle{
                    margin:0 0 9px!important;
                    padding:0!important;
                  }
                  html body #settingsSheet #settingsText692 .fontApp745 .settingRow,
                  html body #settingsSheet #settingsText692 .fontWidget745 .settingRow{
                    margin-top:0!important;
                    padding-top:0!important;
                  }

                  /* No separator between the application and widget text-size rows. */
                  html body #settingsSheet #settingsText692 .fontApp745{
                    border-bottom:0!important;
                    box-shadow:none!important;
                  }
                  html body #settingsSheet #settingsText692 .fontWidget745{
                    border-top:0!important;
                    box-shadow:none!important;
                  }
                  html body #settingsSheet #settingsText692 .fontApp745:before,
                  html body #settingsSheet #settingsText692 .fontApp745:after,
                  html body #settingsSheet #settingsText692 .fontWidget745:before,
                  html body #settingsSheet #settingsText692 .fontWidget745:after,
                  html body #settingsSheet #settingsText692 .fontApp745 + hr,
                  html body #settingsSheet #settingsText692 hr + .fontWidget745{
                    border:0!important;
                    box-shadow:none!important;
                  }
                  html body #settingsSheet #settingsText692 .fontApp745 + hr{
                    display:none!important;
                  }

                  /* Requested visual breathing room. */
                  html body #settingsSheet #settingsDisplay692 .midiDays692>.w658Title{
                    margin-bottom:26px!important;
                  }
                  html body #settingsSheet #widgetDensity664{
                    margin-top:0!important;
                    padding-top:22px!important;
                  }

                  /* Profile selector is centered as a whole under "Profils". */
                  html body #settingsSheet #advancedSettings85 #advProfilesTitle{
                    text-align:center!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileRow746{
                    display:flex!important;
                    grid-template-columns:1fr!important;
                    align-items:center!important;
                    justify-content:center!important;
                    width:100%!important;
                    max-width:100%!important;
                    margin-left:auto!important;
                    margin-right:auto!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileRow746 .profileShell745{
                    flex:0 0 180px!important;
                    width:180px!important;
                    min-width:180px!important;
                    max-width:min(180px,calc(100vw - 84px))!important;
                    margin:0 auto!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileRow746 .profileVisual745{
                    inset:0 32px!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    overflow:hidden!important;
                    white-space:nowrap!important;
                    text-overflow:clip!important;
                    text-align:center!important;
                    line-height:36px!important;
                  }

                  /* Generic select shell: the visible text is centered across the whole
                     control; the arrow lives in its own lane and cannot shift the label. */
                  html body #settingsSheet .centerSelect746{
                    position:relative!important;
                    display:inline-block!important;
                    flex:0 0 auto!important;
                    height:36px!important;
                    min-height:36px!important;
                    vertical-align:middle!important;
                  }
                  html body #settingsSheet .centerSelect746>select{
                    position:absolute!important;
                    inset:0!important;
                    display:block!important;
                    width:100%!important;
                    min-width:100%!important;
                    max-width:100%!important;
                    height:36px!important;
                    min-height:36px!important;
                    margin:0!important;
                    padding:0!important;
                    opacity:0!important;
                    cursor:pointer!important;
                    z-index:3!important;
                  }
                  html body #settingsSheet .centerSelect746Visual{
                    position:absolute!important;
                    inset:0!important;
                    box-sizing:border-box!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    padding:0 28px!important;
                    border:1px solid #cfd9e5!important;
                    border-radius:11px!important;
                    background:#fff!important;
                    color:#233047!important;
                    font-size:.74rem!important;
                    font-weight:750!important;
                    line-height:1!important;
                    text-align:center!important;
                    white-space:nowrap!important;
                    overflow:hidden!important;
                    pointer-events:none!important;
                    z-index:1!important;
                  }
                  html body #settingsSheet .centerSelect746Arrow{
                    position:absolute!important;
                    right:12px!important;
                    top:50%!important;
                    width:8px!important;
                    height:8px!important;
                    transform:translateY(-65%) rotate(45deg)!important;
                    border-right:1.5px solid #68738a!important;
                    border-bottom:1.5px solid #68738a!important;
                    pointer-events:none!important;
                    z-index:2!important;
                  }
                  html body #settingsSheet #schoolAutoRow739{
                    align-items:center!important;
                  }
                  html body #settingsSheet #holidayRow746{
                    align-items:center!important;
                  }

                  /* Du / Au are structural labels, not transient helper text. */
                  html body #settingsSheet #advancedSettings85 .rangeDates746{
                    display:grid!important;
                    grid-template-columns:auto minmax(0,1fr) auto minmax(0,1fr)!important;
                    align-items:center!important;
                    column-gap:6px!important;
                    width:100%!important;
                  }
                  html body #settingsSheet #advancedSettings85 .rangeConnector746{
                    display:block!important;
                    visibility:visible!important;
                    opacity:1!important;
                    margin:0!important;
                    padding:0!important;
                    color:#59677a!important;
                    font-size:.73rem!important;
                    font-weight:760!important;
                    line-height:1!important;
                    white-space:nowrap!important;
                  }

                  /* Advanced settings must never animate into a second geometry. */
                  html body #settingsSheet #advancedSettings85,
                  html body #settingsSheet #advancedSettings85 *,
                  html body #settingsSheet #advancedContent85,
                  html body #settingsSheet #advancedContent85 *{
                    transition:none!important;
                    animation:none!important;
                  }

                  /* Version stays below the bottom Close button. */
                  html body #settingsSheet #appVersionInfo745{
                    display:block!important;
                    visibility:visible!important;
                    opacity:1!important;
                    width:100%!important;
                    margin:10px auto 2px!important;
                    text-align:center!important;
                  }
                \`;
                document.head.appendChild(style);

                let busy746=false,canvas746=null;

                function lang746(){
                  const value=String(document.getElementById('languageSelect')?.value||'fr').toLowerCase();
                  return value==='de'||value==='en'?value:'fr';
                }
                function tr746(fr,en,de){
                  const l=lang746();return l==='de'?de:(l==='en'?en:fr);
                }
                function imp746(el,prop,value){
                  if(!el)return;
                  if(el.style.getPropertyValue(prop)===value&&el.style.getPropertyPriority(prop)==='important')return;
                  el.style.setProperty(prop,value,'important');
                }
                function selectedText746(select){
                  const option=select?.options?.[select.selectedIndex];
                  return String(option?.textContent||'').trim();
                }
                function textWidth746(text,source){
                  try{
                    canvas746=canvas746||document.createElement('canvas');
                    const ctx=canvas746.getContext('2d');
                    const cs=getComputedStyle(source);
                    ctx.font=[cs.fontStyle,cs.fontWeight,cs.fontSize,cs.fontFamily].join(' ');
                    return Math.ceil(ctx.measureText(text||'').width);
                  }catch(e){return Math.max(28,String(text||'').length*7)}
                }

                function ensureCenteredSelect746(select,minWidth,maxWidth){
                  if(!select)return null;
                  let shell=select.closest('.centerSelect746');
                  if(!shell){
                    shell=document.createElement('span');
                    shell.className='centerSelect746';
                    select.parentNode.insertBefore(shell,select);
                    shell.appendChild(select);
                    const visual=document.createElement('span');
                    visual.className='centerSelect746Visual';
                    const arrow=document.createElement('span');
                    arrow.className='centerSelect746Arrow';
                    shell.append(visual,arrow);
                  }
                  const visual=shell.querySelector('.centerSelect746Visual');
                  const text=selectedText746(select);
                  if(visual&&visual.textContent!==text)visual.textContent=text;
                  const width=Math.max(minWidth,Math.min(maxWidth,textWidth746(text,select)+60));
                  imp746(shell,'width',width+'px');
                  imp746(shell,'min-width',width+'px');
                  imp746(shell,'max-width',width+'px');
                  imp746(select,'width','100%');
                  imp746(select,'min-width','100%');
                  imp746(select,'max-width','100%');
                  imp746(select,'height','36px');
                  imp746(select,'padding','0px');
                  imp746(select,'margin','0px');
                  imp746(select,'opacity','0');
                  return shell;
                }

                function stabilizeSelects746(){
                  const zone=document.getElementById('schoolZone');
                  if(zone){
                    const shell=ensureCenteredSelect746(zone,108,132);
                    if(shell)shell.classList.add('zoneSelect746');
                  }
                  const holiday=document.getElementById('advHoliday');
                  if(holiday){
                    const shell=ensureCenteredSelect746(holiday,104,188);
                    if(shell){
                      shell.classList.add('holidaySelect746');
                      const row=shell.closest('.advRow');
                      if(row)row.id='holidayRow746';
                    }
                  }
                }

                function stabilizeProfile746(){
                  const select=document.getElementById('advProfileSelect');if(!select)return;
                  const row=select.closest('.advRow');
                  if(row)row.classList.add('profileRow746');
                  const shell=select.parentElement;
                  if(shell){
                    shell.classList.add('profileShell745');
                    imp746(shell,'width','180px');
                    imp746(shell,'min-width','180px');
                    imp746(shell,'max-width','min(180px,calc(100vw - 84px))');
                    imp746(shell,'height','36px');
                    imp746(shell,'margin','0px auto');
                  }
                  let value=shell&&shell.querySelector('.profileVisual745');
                  if(!value&&shell){
                    value=document.createElement('span');
                    value.className='profileVisual745';
                    shell.appendChild(value);
                  }
                  if(value){
                    const text=selectedText746(select);
                    if(value.textContent!==text)value.textContent=text;
                    imp746(value,'inset','0px 32px');
                    imp746(value,'display','flex');
                    imp746(value,'align-items','center');
                    imp746(value,'justify-content','center');
                    imp746(value,'text-align','center');
                    imp746(value,'text-overflow','clip');
                    imp746(value,'line-height','36px');
                  }
                }

                function ensureRange746(){
                  const start=document.getElementById('advRangeStart');
                  const end=document.getElementById('advRangeEnd');
                  if(!start||!end)return;
                  let row=start.closest('.rangeDates746,.rangeDates725,.advInline');
                  if(!row||!row.contains(end))return;
                  row.classList.add('rangeDates725','rangeDates746');

                  const startNode=start.closest('.dateShell734')||start;
                  const endNode=end.closest('.dateShell734')||end;
                  row.querySelectorAll('.rangeFrom725:not(.rangeFrom746),.rangeTo725:not(.rangeTo746),.rangeConnector745:not(.rangeFrom746):not(.rangeTo746)').forEach(el=>el.remove());

                  let from=row.querySelector('.rangeFrom746');
                  if(!from){
                    from=document.createElement('span');
                    from.className='rangeConnector725 rangeConnector745 rangeConnector746 rangeFrom746';
                  }
                  let to=row.querySelector('.rangeTo746');
                  if(!to){
                    to=document.createElement('span');
                    to.className='rangeConnector725 rangeConnector745 rangeConnector746 rangeTo746';
                  }
                  const fromText=tr746('Du','From','Von'),toText=tr746('Au','To','Bis');
                  if(from.textContent!==fromText)from.textContent=fromText;
                  if(to.textContent!==toText)to.textContent=toText;
                  if(from.nextSibling!==startNode)row.insertBefore(from,startNode);
                  if(to.nextSibling!==endNode)row.insertBefore(to,endNode);
                }

                function equalizeTextSpacing746(){
                  const appTitle=document.getElementById('appFontTitle');
                  const widgetTitle=document.getElementById('widgetFontTitle');
                  for(const title of [appTitle,widgetTitle]){
                    if(!title)continue;
                    const box=title.closest('.settingBox');
                    if(box)box.classList.add(title===appTitle?'fontApp745':'fontWidget745');
                    imp746(title,'margin-bottom','9px');
                    const row=box&&box.querySelector('.settingRow');
                    if(row){
                      imp746(row,'margin-top','0px');
                      imp746(row,'padding-top','0px');
                    }
                  }
                }

                function ensureVersion746(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  const actions=sheet.querySelector('.settingsActions');if(!actions)return;
                  let version=document.getElementById('appVersionInfo745');
                  if(!version){
                    version=document.createElement('div');
                    version.id='appVersionInfo745';
                  }
                  if(version.parentElement!==sheet||actions.nextElementSibling!==version)
                    actions.insertAdjacentElement('afterend',version);
                  let name='7.46';
                  try{
                    const nativeName=window.AndroidSchedule&&AndroidSchedule.getAppVersionName?String(AndroidSchedule.getAppVersionName()||'').trim():'';
                    if(nativeName)name=nativeName;
                  }catch(e){}
                  const label='Version '+name;
                  if(version.textContent!==label)version.textContent=label;
                }

                function spacing746(){
                  const midiTitle=document.querySelector('#settingsDisplay692 .midiDays692>.w658Title');
                  if(midiTitle)imp746(midiTitle,'margin-bottom','26px');
                  const density=document.getElementById('widgetDensity664');
                  if(density){
                    imp746(density,'margin-top','0px');
                    imp746(density,'padding-top','22px');
                  }
                }

                function finalize746(){
                  if(busy746)return;
                  busy746=true;
                  try{
                    spacing746();
                    equalizeTextSpacing746();
                    stabilizeSelects746();
                    stabilizeProfile746();
                    ensureRange746();
                    ensureVersion746();
                    document.documentElement.dataset.edtSettings746='1';
                  }finally{busy746=false}
                }
                window.refreshSettings746=finalize746;

                /* Run before the gesture that can reveal Settings/Advanced. */
                document.addEventListener('pointerdown',event=>{
                  const target=event.target&&event.target.closest?event.target:null;
                  if(target?.closest('#settingsBtn')||target?.closest('#advancedSettings85>summary'))finalize746();
                },true);

                document.addEventListener('change',event=>{
                  const id=event.target&&event.target.id;
                  if(id==='schoolZone'||id==='advHoliday'||id==='advProfileSelect'||id==='languageSelect')queueMicrotask(finalize746);
                },true);

                const sheet=document.getElementById('settingsSheet');
                if(sheet){
                  let queued=false;
                  const observer=new MutationObserver(records=>{
                    if(busy746||queued)return;
                    if(!records.some(r=>r.type==='childList'))return;
                    queued=true;
                    queueMicrotask(()=>{queued=false;finalize746()});
                  });
                  observer.observe(sheet,{childList:true,subtree:true});
                }

                /* Every known settings refresh finishes with 7.46 geometry. */
                ['refreshSettingsV3','refreshAdvancedFeatures','refresh735','refresh736','refresh743','refreshSettings745'].forEach(name=>{
                  const old=window[name];
                  if(typeof old!=='function'||old.__settings746)return;
                  const wrapped=function(){
                    const result=old.apply(this,arguments);
                    finalize746();
                    return result;
                  };
                  wrapped.__settings746=true;
                  window[name]=wrapped;
                  try{eval(name+'=wrapped')}catch(e){}
                });

                finalize746();
              }catch(e){console.error('Settings746Ui',e)}
            })();
            """;
    }
}
