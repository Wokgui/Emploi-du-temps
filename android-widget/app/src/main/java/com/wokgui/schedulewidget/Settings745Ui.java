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
                    margin:0 0 14px!important;
                  }

                  /* Affichage du widget / Condensation du widget. */
                  html body #settingsSheet #widgetDensity664{
                    margin-top:14px!important;
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

                  row.querySelectorAll('.rangeFrom725,.rangeTo725,.rangeFrom745,.rangeTo745').forEach(el=>el.remove());

                  const from=document.createElement('span');
                  from.className='rangeConnector725 rangeConnector745 rangeFrom745';
                  from.textContent=tr745('Du','From','Von');
                  const to=document.createElement('span');
                  to.className='rangeConnector725 rangeConnector745 rangeTo745';
                  to.textContent=tr745('Au','To','Bis');

                  row.insertBefore(from,startNode);
                  row.insertBefore(to,endNode);
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
                    width:'100%',minWidth:'100%',maxWidth:'100%',height:'36px',minHeight:'36px',
                    margin:'0',padding:'0 32px'
                  };
                  for(const [key,val] of Object.entries(props)){
                    const css=key.replace(/[A-Z]/g,m=>'-'+m.toLowerCase());
                    if(select.style.getPropertyValue(css)!==val||select.style.getPropertyPriority(css)!=='important')
                      select.style.setProperty(css,val,'important');
                  }
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
                  observer.observe(sheet,{childList:true,subtree:true});
                }

                prepareAdvanced745();
                finalGeometry745();
              }catch(e){console.error('Settings745Ui',e)}
            })();
            """;
    }
}
