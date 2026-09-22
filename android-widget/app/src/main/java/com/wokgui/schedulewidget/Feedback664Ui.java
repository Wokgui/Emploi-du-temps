package com.wokgui.schedulewidget;

/** Final 6.64 pass: edit-day targeting, atomic week switching, and settings polish. */
final class Feedback664Ui {
    private Feedback664Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback664){window.refreshFeedback664&&window.refreshFeedback664();return}
                window.__feedback664=true;

                const style=document.createElement('style');style.id='feedback664Style';style.textContent=`
                  #settingsSheet>.previewGrid{display:none!important}
                  #widgetDensity664{margin-top:14px!important}
                  #widgetDensity664 .feedback664DensityTitle{text-align:center!important;font-size:.74rem!important;font-weight:800!important;color:#586579!important;margin:0 0 7px!important}
                  #widgetDensity664 .feedback664DensityRow{display:grid!important;grid-template-columns:minmax(82px,1fr) minmax(132px,1.25fr)!important;align-items:center!important;gap:10px!important;margin:0!important}
                  #widgetDensity664 .feedback664DensityRow>span{text-align:center!important;font-weight:700!important;color:#586579!important}
                  #widgetDensity664 .feedback664DensityRow>select{width:100%!important;max-width:220px!important;justify-self:center!important;text-align:center!important;text-align-last:center!important;border-radius:11px!important;padding:8px 28px 8px 10px!important}
                  #advancedSettings85 .feedback664Profiles>#advProfilesTitle{text-align:center!important;font-size:.86rem!important;font-weight:850!important;margin:0 0 8px!important}
                  #advancedSettings85 .feedback664Profiles>.advRow{justify-content:center!important;margin:0 0 9px!important}
                  #advancedSettings85 .feedback664Profiles #advProfileSelect{
                    display:block!important;box-sizing:border-box!important;
                    width:180px!important;max-width:min(180px,calc(100vw - 84px))!important;min-width:180px!important;
                    height:36px!important;min-height:36px!important;margin:0 auto!important;
                    padding:0 32px!important;border:1px solid #cfd9e5!important;border-radius:12px!important;
                    -webkit-appearance:none!important;appearance:none!important;
                    background-color:#fff!important;
                    background-image:linear-gradient(45deg,transparent 50%,#68738a 50%),linear-gradient(135deg,#68738a 50%,transparent 50%)!important;
                    background-position:calc(100% - 14px) 50%,calc(100% - 10px) 50%!important;
                    background-size:4px 4px,4px 4px!important;background-repeat:no-repeat!important;
                    text-align:center!important;text-align-last:center!important;text-overflow:clip!important;
                    font-weight:750!important;color:#233047!important;-webkit-text-fill-color:#233047!important
                  }
                  #advancedSettings85 .feedback664Profiles #advProfileSelect option{text-align:center!important}
                  #advancedSettings85 .feedback664Profiles>.advButtons{justify-content:center!important}
                `;document.head.appendChild(style);

                function language(){
                  const value=document.getElementById('languageSelect')?.value||'fr';
                  return value==='en'||value==='de'?value:'fr';
                }
                function tr(fr,en,de){const l=language();return l==='de'?de:(l==='en'?en:fr)}

                function removePreviews(){
                  const previews=document.querySelector('#settingsSheet>.previewGrid');
                  if(previews){previews.hidden=true;previews.setAttribute('aria-hidden','true')}
                }

                function installDensity(){
                  const group=document.getElementById('textSettings86'),body=group&&group.querySelector('.settingsSectionBody86');
                  const select=document.getElementById('advDensity'),row=select&&select.closest('.advRow');
                  if(!body||!row)return;
                  let box=document.getElementById('widgetDensity664');
                  if(!box){
                    box=document.createElement('div');box.id='widgetDensity664';box.className='settingBox';
                    const title=document.createElement('div');title.className='feedback664DensityTitle';box.appendChild(title);
                    body.appendChild(box);
                  }
                  const title=box.querySelector('.feedback664DensityTitle');
                  const titleText=tr('Condensation du widget','Widget compactness','Widget-Kompaktheit');
                  if(title&&title.textContent!==titleText)title.textContent=titleText;
                  row.classList.add('feedback664DensityRow');
                  if(row.parentNode!==box)box.appendChild(row);
                }

                function installProfiles(){
                  const title=document.getElementById('advProfilesTitle'),box=title&&title.closest('.settingBox');
                  if(box)box.classList.add('feedback664Profiles');
                }

                function weekCourses(week,day){
                  try{return weeks&&weeks[week]&&weeks[week][day]&&Array.isArray(weeks[week][day].courses)?weeks[week][day].courses:[]}catch(e){return []}
                }
                function preferredEditDay(){
                  const days=(typeof DAYS!=='undefined'&&Array.isArray(DAYS)&&DAYS.length)?DAYS.map(Number):[2,3,4,5,6];
                  const js=new Date().getDay(),current=js>=1&&js<=5?js+1:null;
                  const start=current&&days.includes(current)?days.indexOf(current):0;
                  const ordered=days.slice(start).concat(days.slice(0,start));
                  const week=(typeof currentWeek!=='undefined'?currentWeek:(typeof activeWeek!=='undefined'?activeWeek:'A'));
                  if(current&&weekCourses(week,current).length)return current;
                  const next=ordered.find(day=>weekCourses(week,day).length);
                  return next||current||days[0];
                }
                function prepareEdit(){
                  try{
                    if(typeof currentWeek!=='undefined'&&typeof activeWeek!=='undefined')activeWeek=currentWeek;
                    const wanted=preferredEditDay();
                    if(typeof selected!=='undefined'&&selected!==wanted){
                      selected=wanted;
                      if(window.invalidateTimetableViews)window.invalidateTimetableViews(['edit']);
                    }
                  }catch(e){}
                }

                document.addEventListener('pointerdown',event=>{
                  const edit=event.target&&event.target.closest?event.target.closest('.nav[data-mode="edit"]'):null;
                  if(edit)prepareEdit();
                },true);
                document.addEventListener('click',event=>{
                  const edit=event.target&&event.target.closest?event.target.closest('.nav[data-mode="edit"]'):null;
                  if(edit)prepareEdit();
                },true);

                function refresh(){removePreviews();installDensity();installProfiles()}
                window.refreshFeedback664=refresh;
                ['refreshSettingsLayout','refreshAdvancedFeatures','refreshSettingsV3'].forEach(name=>{
                  const old=window[name];if(typeof old!=='function'||old.__feedback664)return;
                  const wrapped=function(){const result=old.apply(this,arguments);queueMicrotask(refresh);return result};
                  wrapped.__feedback664=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}
                });
                refresh();
              }catch(e){console.error('Feedback664Ui',e)}
            })();
            """;
    }
}
