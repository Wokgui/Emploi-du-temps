package com.wokgui.schedulewidget;

/** Final owner for the 7.45 settings geometry and first-frame advanced rendering. */
final class Settings745Ui {
    private Settings745Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__settings745){window.refreshSettings745&&window.refreshSettings745();return}
                window.__settings745=true;

                const style=document.createElement('style');
                style.id='settings745Style';
                style.textContent=`
                  /* Midi par jour : même respiration visuelle que les autres sous-sections. */
                  html body #settingsSheet .midiDays692>.w658Title{
                    margin:0 0 21px!important;
                  }

                  /* Affichage du widget / Condensation du widget. */
                  html body #settingsSheet #widgetDensity664{
                    margin-top:0!important;
                    padding-top:0!important;
                  }

                  /* Pas de petit séparateur entre les deux tailles de texte. */
                  html body #settingsSheet #settingsText692 .fontApp745,
                  html body #settingsSheet #settingsText692 .fontWidget745{
                    border-top:0!important;
                    border-bottom:0!important;
                    box-shadow:none!important;
                  }
                  html body #settingsSheet #settingsText692 .fontApp745:before,
                  html body #settingsSheet #settingsText692 .fontApp745:after,
                  html body #settingsSheet #settingsText692 .fontWidget745:before,
                  html body #settingsSheet #settingsText692 .fontWidget745:after{
                    display:none!important;
                    content:none!important;
                  }

                  /* Profil : géométrie fixe, texte indépendant de la flèche. */
                  html body #settingsSheet #advancedSettings85 .profileShell745{
                    position:relative!important;
                    display:block!important;
                    width:180px!important;
                    min-width:180px!important;
                    max-width:calc(100vw - 84px)!important;
                    height:36px!important;
                    margin:0 auto!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileShell745 #advProfileSelect{
                    position:absolute!important;
                    inset:0!important;
                    display:block!important;
                    box-sizing:border-box!important;
                    width:100%!important;
                    min-width:100%!important;
                    max-width:100%!important;
                    height:36px!important;
                    min-height:36px!important;
                    margin:0!important;
                    padding:0 32px!important;
                    border:1px solid #cfd9e5!important;
                    border-radius:12px!important;
                    -webkit-appearance:none!important;
                    appearance:none!important;
                    background:#fff!important;
                    background-image:none!important;
                    color:transparent!important;
                    -webkit-text-fill-color:transparent!important;
                    text-shadow:none!important;
                    cursor:pointer!important;
                    z-index:2!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileShell745 #advProfileSelect option{
                    color:#233047!important;
                    -webkit-text-fill-color:#233047!important;
                    text-align:center!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileVisual745{
                    position:absolute!important;
                    inset:0 30px!important;
                    display:flex!important;
                    align-items:center!important;
                    justify-content:center!important;
                    overflow:hidden!important;
                    white-space:nowrap!important;
                    text-overflow:ellipsis!important;
                    color:#233047!important;
                    font-size:.74rem!important;
                    font-weight:750!important;
                    line-height:1!important;
                    text-align:center!important;
                    pointer-events:none!important;
                    z-index:3!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileArrow745{
                    position:absolute!important;
                    right:12px!important;
                    top:50%!important;
                    width:8px!important;
                    height:8px!important;
                    transform:translateY(-65%) rotate(45deg)!important;
                    border-right:1.5px solid #68738a!important;
                    border-bottom:1.5px solid #68738a!important;
                    pointer-events:none!important;
                    z-index:4!important;
                  }
                  html body #settingsSheet #advancedSettings85 .profileValue735,
                  html body #settingsSheet #advancedSettings85 .profileArrow734{
                    display:none!important;
                  }

                  /* Du / Au restent visibles avec les champs de dates centrés. */
                  html body #settingsSheet #advancedSettings85 .rangeDates725{
                    display:grid!important;
                    grid-template-columns:auto minmax(0,1fr) auto minmax(0,1fr)!important;
                    gap:6px!important;
                    align-items:center!important;
                    width:100%!important;
                  }
                  html body #settingsSheet #advancedSettings85 .rangeConnector745{
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

                  /* Version sous Fermer, sans modifier la hauteur du bouton. */
                  html body #settingsSheet #appVersionInfo745{
                    display:block!important;
                    width:100%!important;
                    margin:10px auto 2px!important;
                    padding:0!important;
                    color:#8a94a6!important;
                    font-size:.63rem!important;
                    font-weight:650!important;
                    line-height:1.2!important;
                    text-align:center!important;
                    letter-spacing:.01em!important;
                  }

                  /* L'ouverture avancée doit être un affichage définitif, pas une transition. */
                  html body #settingsSheet #advancedSettings85,
                  html body #settingsSheet #advancedSettings85 *,
                  html body #settingsSheet #advancedContent85,
                  html body #settingsSheet #advancedContent85 *{
                    animation-duration:0s!important;
                    transition-duration:0s!important;
                  }
                `;
                document.head.appendChild(style);

                let busy=false;
                function lang745(){
                  const value=String(document.getElementById('languageSelect')?.value||'fr').toLowerCase();
                  return value==='de'||value==='en'?value:'fr';
                }
                function tr745(fr,en,de){
                  const l=lang745();return l==='de'?de:(l==='en'?en:fr);
                }
                function setImp745(el,prop,value){
                  if(!el)return;
                  if(el.style.getPropertyValue(prop)===value&&el.style.getPropertyPriority(prop)==='important')return;
                  el.style.setProperty(prop,value,'important');
                }

                function applySpacing745(){
                  const midiTitle=document.querySelector('#settingsDisplay692 .midiDays692>.w658Title');
                  if(midiTitle)setImp745(midiTitle,'margin-bottom','20px');
                  const density=document.getElementById('widgetDensity664');
                  if(density){
                    setImp745(density,'margin-top','0px');
                    setImp745(density,'padding-top','0px');
                  }
                }

                function stabilizeAdvancedRows745(){
                  const reminder=document.querySelector('#advancedSettings85 .reminderLine725');
                  if(reminder){
                    reminder.style.setProperty('display','flex','important');
                    reminder.style.setProperty('align-items','center','important');
                    reminder.style.setProperty('justify-content','center','important');
                    reminder.style.setProperty('gap','10px','important');
                    reminder.style.setProperty('width','max-content','important');
                    reminder.style.setProperty('max-width','100%','important');
                    reminder.style.setProperty('min-height','36px','important');
                    reminder.style.setProperty('margin','4px auto 0','important');
                    const reminderSelect=document.getElementById('advReminderMinutes');
                    if(reminderSelect){
                      setImp745(reminderSelect,'flex','0 0 96px');
                      setImp745(reminderSelect,'width','96px');
                      setImp745(reminderSelect,'min-width','96px');
                      setImp745(reminderSelect,'max-width','96px');
                      setImp745(reminderSelect,'box-sizing','border-box');
                      setImp745(reminderSelect,'padding-left','27px');
                      setImp745(reminderSelect,'padding-right','27px');
                      setImp745(reminderSelect,'margin','0px');
                      setImp745(reminderSelect,'text-align','center');
                      setImp745(reminderSelect,'text-align-last','center');
                    }
                  }
                  const schoolSetting=document.getElementById('schoolCalendarSetting');
                  if(schoolSetting){
                    schoolSetting.classList.add('edtAdvFlat724','edtAdvSep724');
                    setImp745(schoolSetting,'box-sizing','border-box');
                    setImp745(schoolSetting,'width','100%');
                    setImp745(schoolSetting,'max-width','100%');
                    setImp745(schoolSetting,'margin','0px');
                    setImp745(schoolSetting,'padding-left','0px');
                    setImp745(schoolSetting,'padding-right','0px');
                    setImp745(schoolSetting,'border-left-width','0px');
                    setImp745(schoolSetting,'border-right-width','0px');
                  }
                  const zone=document.getElementById('schoolZone');
                  const enabled=document.getElementById('schoolEnabled');
                  const row=document.getElementById('schoolAutoRow739');
                  const enableRow=enabled&&(enabled.closest('.schoolEnable')||enabled.parentElement);
                  if(row){
                    row.style.setProperty('display','grid','important');
                    row.style.setProperty('grid-template-columns','minmax(0,1fr) auto','important');
                    row.style.setProperty('align-items','center','important');
                    row.style.setProperty('justify-content','stretch','important');
                    row.style.setProperty('gap','10px','important');
                    row.style.setProperty('width','100%','important');
                    row.style.setProperty('max-width','100%','important');
                    row.style.setProperty('box-sizing','border-box','important');
                    row.style.setProperty('min-height','34px','important');
                    row.style.setProperty('margin','0 auto 8px','important');
                  }
                  if(enableRow){
                    enableRow.style.setProperty('display','flex','important');
                    enableRow.style.setProperty('align-items','center','important');
                    enableRow.style.setProperty('justify-content','flex-start','important');
                    enableRow.style.setProperty('gap','6px','important');
                    enableRow.style.setProperty('width','100%','important');
                    enableRow.style.setProperty('min-width','0','important');
                    enableRow.style.setProperty('flex','1 1 auto','important');
                    enableRow.style.setProperty('margin','0','important');
                  }
                  if(zone){
                    setImp745(zone,'flex','0 0 108px');
                    setImp745(zone,'width','108px');
                    setImp745(zone,'min-width','108px');
                    setImp745(zone,'max-width','108px');
                    setImp745(zone,'box-sizing','border-box');
                    setImp745(zone,'margin','0px');
                  }
                }

                function markTextBoxes745(){
                  const app=document.getElementById('appFontTitle')?.closest('.settingBox');
                  const widget=document.getElementById('widgetFontTitle')?.closest('.settingBox');
                  if(app)app.classList.add('fontApp745');
                  if(widget)widget.classList.add('fontWidget745');
                }

                function ensureRangeLabels745(){
                  const start=document.getElementById('advRangeStart');
                  const end=document.getElementById('advRangeEnd');
                  if(!start||!end)return;
                  let row=start.closest('.rangeDates725')||end.closest('.rangeDates725');
                  if(!row){
                    let p=start.parentElement;
                    while(p&&p!==document.body&&!p.contains(end))p=p.parentElement;
                    row=p;
                  }
                  if(!row)return;
                  row.classList.add('rangeDates725');

                  const startNode=start.closest('.dateShell734')||start;
                  const endNode=end.closest('.dateShell734')||end;

                  row.querySelectorAll('.rangeFrom725:not(.rangeFrom745),.rangeTo725:not(.rangeTo745)').forEach(el=>el.remove());

                  let from=row.querySelector('.rangeFrom745');
                  if(!from){
                    from=document.createElement('span');
                    from.className='rangeConnector725 rangeConnector745 rangeFrom745';
                  }
                  let to=row.querySelector('.rangeTo745');
                  if(!to){
                    to=document.createElement('span');
                    to.className='rangeConnector725 rangeConnector745 rangeTo745';
                  }
                  const fromText=tr745('Du','From','Von'),toText=tr745('Au','To','Bis');
                  if(from.textContent!==fromText)from.textContent=fromText;
                  if(to.textContent!==toText)to.textContent=toText;

                  if(from.nextSibling!==startNode)row.insertBefore(from,startNode);
                  if(to.nextSibling!==endNode)row.insertBefore(to,endNode);
                }

                function selectedProfileText745(select){
                  const option=select?.options?.[select.selectedIndex];
                  return String(option?.textContent||'').trim();
                }
                function syncProfile745(){
                  const select=document.getElementById('advProfileSelect');if(!select)return;
                  let shell=select.parentElement;
                  if(!shell)return;
                  shell.classList.add('profileShell745');

                  shell.querySelectorAll('.profileValue735,.profileArrow734').forEach(el=>el.remove());

                  let value=shell.querySelector('.profileVisual745');
                  if(!value){
                    value=document.createElement('span');
                    value.className='profileVisual745';
                    shell.appendChild(value);
                  }
                  let arrow=shell.querySelector('.profileArrow745');
                  if(!arrow){
                    arrow=document.createElement('span');
                    arrow.className='profileArrow745';
                    shell.appendChild(arrow);
                  }
                  const text=selectedProfileText745(select);
                  if(value.textContent!==text)value.textContent=text;

                  const props={
                    position:'absolute',inset:'0px',display:'block',boxSizing:'border-box',
                    width:'100%',minWidth:'100%',maxWidth:'100%',height:'36px',minHeight:'36px',
                    margin:'0px',padding:'0px 32px',appearance:'none',webkitAppearance:'none',
                    backgroundImage:'none',color:'transparent',webkitTextFillColor:'transparent',
                    textShadow:'none',textAlign:'center',textAlignLast:'center',textOverflow:'clip'
                  };
                  for(const [key,val] of Object.entries(props)){
                    let css=key.replace(/[A-Z]/g,m=>'-'+m.toLowerCase());
                    if(css==='webkit-appearance')css='-webkit-appearance';
                    if(css==='webkit-text-fill-color')css='-webkit-text-fill-color';
                    setImp745(select,css,val);
                  }
                  setImp745(shell,'position','relative');
                  setImp745(shell,'display','block');
                  setImp745(shell,'width','180px');
                  setImp745(shell,'min-width','180px');
                  setImp745(shell,'max-width','calc(100vw - 84px)');
                  setImp745(shell,'height','36px');
                  setImp745(shell,'margin','0px auto');
                }

                function ensureVersion745(){
                  const sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  const actions=sheet.querySelector('.settingsActions');if(!actions)return;
                  let version=document.getElementById('appVersionInfo745');
                  if(!version){
                    version=document.createElement('div');
                    version.id='appVersionInfo745';
                  }
                  if(version.parentElement!==sheet||actions.nextElementSibling!==version)
                    actions.insertAdjacentElement('afterend',version);
                  let name='7.45';
                  try{
                    const nativeName=window.AndroidSchedule&&AndroidSchedule.getAppVersionName?String(AndroidSchedule.getAppVersionName()||'').trim():'';
                    if(nativeName)name=nativeName;
                  }catch(e){}
                  const label='Version '+name;
                  if(version.textContent!==label)version.textContent=label;
                }

                function finalGeometry745(){
                  applySpacing745();
                  stabilizeAdvancedRows745();
                  markTextBoxes745();
                  ensureRangeLabels745();
                  syncProfile745();
                  ensureVersion745();
                  document.documentElement.dataset.edt745Final='1';
                }

                function prepareAdvanced745(){
                  if(busy)return;
                  busy=true;
                  try{
                    try{if(typeof window.prewarmAdvanced737==='function')window.prewarmAdvanced737()}catch(e){}
                    try{if(typeof window.refreshAdvancedFeatures==='function')window.refreshAdvancedFeatures()}catch(e){}
                    try{if(typeof window.refreshFeedback664==='function')window.refreshFeedback664()}catch(e){}
                    try{if(typeof window.refreshFeedback665==='function')window.refreshFeedback665()}catch(e){}
                    try{if(typeof window.refresh743==='function')window.refresh743()}catch(e){}
                    finalGeometry745();
                  }finally{busy=false}
                }
                window.prepareAdvanced745=prepareAdvanced745;
                window.refreshSettings745=function(){prepareAdvanced745();finalGeometry745()};

                /* Any legacy refresh must end in the same final geometry before paint. */
                ['refresh734','refresh735','refresh736','refreshAdvancedFeatures','refreshFeedback664','refreshFeedback665','refresh743'].forEach(name=>{
                  const old=window[name];
                  if(typeof old!=='function'||old.__settings745)return;
                  const wrapped=function(){
                    const result=old.apply(this,arguments);
                    finalGeometry745();
                    return result;
                  };
                  wrapped.__settings745=true;
                  window[name]=wrapped;
                  try{eval(name+'=wrapped')}catch(e){}
                });

                /* Prepare while still hidden, before the native <details> changes state. */
                document.addEventListener('pointerdown',event=>{
                  const target=event.target&&event.target.closest?event.target:null;
                  if(target?.closest('#settingsBtn'))prepareAdvanced745();
                  if(target?.closest('#advancedSettings85>summary'))prepareAdvanced745();
                },true);

                /* Own the advanced toggle so the first painted open frame is already final. */
                document.addEventListener('click',event=>{
                  const summary=event.target&&event.target.closest?event.target.closest('#advancedSettings85>summary'):null;
                  if(!summary)return;
                  const adv=document.getElementById('advancedSettings85');if(!adv)return;
                  event.preventDefault();
                  event.stopPropagation();
                  if(event.stopImmediatePropagation)event.stopImmediatePropagation();
                  const open=!adv.open;
                  if(open)prepareAdvanced745();
                  adv.open=open;
                  finalGeometry745();
                  /* 7.46 is loaded after this handler. Delegate dynamically so a
                     click-only/keyboard open also paints the final geometry immediately. */
                  try{if(typeof window.refreshSettings746==='function')window.refreshSettings746()}catch(e){}
                },true);

                document.addEventListener('change',event=>{
                  if(event.target?.id==='advProfileSelect')syncProfile745();
                  if(event.target?.id==='languageSelect')ensureRangeLabels745();
                },true);

                const sheet=document.getElementById('settingsSheet');
                if(sheet){
                  let queued=false;
                  const observer=new MutationObserver(()=>{
                    if(busy||queued)return;
                    queued=true;
                    queueMicrotask(()=>{
                      queued=false;
                      finalGeometry745();
                    });
                  });
                  observer.observe(sheet,{childList:true,subtree:true,attributes:true,attributeFilter:['style','class']});
                }

                /* Heavy advanced refreshes run only when Settings/Advanced is about to open. */
                finalGeometry745();
              }catch(e){console.error('Settings745Ui',e)}
            })();
            """;
    }
}
