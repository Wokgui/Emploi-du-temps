package com.wokgui.schedulewidget;

/** 6.66 pass: atomic timetable frames and final advanced-settings ordering. */
final class Feedback666Ui {
    private Feedback666Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback666){window.refreshFeedback666&&window.refreshFeedback666();return}
                window.__feedback666=true;

                function language(){
                  const value=document.getElementById('languageSelect')?.value||'fr';
                  return value==='en'||value==='de'?value:'fr';
                }
                function dayOffTitle(){
                  const l=language();
                  return l==='de'?'Unterrichtsfreie Tage':(l==='en'?'Days off':'Jours sans cours');
                }
                function arrangeDaysOff(){
                  const title=document.getElementById('advCalendarTitle');
                  const calendar=title&&title.closest?title.closest('.settingBox'):null;
                  const school=document.getElementById('schoolCalendarSetting');
                  const content=document.getElementById('advancedContent85')||(calendar&&calendar.parentNode);
                  if(title)title.textContent=dayOffTitle();
                  if(school&&calendar&&content&&school.parentNode===content&&calendar.parentNode===content&&school.nextSibling!==calendar){
                    content.insertBefore(school,calendar);
                  }
                }

                function installAtomicWeek(){
                  const old=window.renderWeek;
                  if(typeof old!=='function'||old.__feedback666)return;
                  let rendering=false;
                  const wrapped=function(){
                    const visible=document.getElementById('weekGrid');
                    if(rendering||!visible||!visible.parentNode)return old.apply(this,arguments);
                    rendering=true;
                    const parent=visible.parentNode;
                    const candidate=visible.cloneNode(false);
                    const previousStyle=candidate.getAttribute('style');
                    visible.id='weekGridStable666';
                    candidate.id='weekGrid';
                    candidate.setAttribute('aria-hidden','true');
                    candidate.style.position='absolute';
                    candidate.style.visibility='hidden';
                    candidate.style.pointerEvents='none';
                    candidate.style.width=Math.max(1,visible.offsetWidth)+'px';
                    parent.insertBefore(candidate,visible.nextSibling);
                    try{
                      const result=old.apply(this,arguments);
                      if(previousStyle==null)candidate.removeAttribute('style');else candidate.setAttribute('style',previousStyle);
                      candidate.removeAttribute('aria-hidden');
                      parent.replaceChild(candidate,visible);
                      return result;
                    }catch(error){
                      candidate.remove();
                      visible.id='weekGrid';
                      throw error;
                    }finally{
                      rendering=false;
                    }
                  };
                  wrapped.__feedback666=true;
                  wrapped.__feedback666Original=old;
                  window.renderWeek=wrapped;
                  try{renderWeek=wrapped}catch(e){}
                }

                function refresh(){arrangeDaysOff();installAtomicWeek()}
                window.refreshFeedback666=refresh;

                ['refreshSettingsLayout','refreshAdvancedFeatures','refreshSettingsV3'].forEach(name=>{
                  const old=window[name];if(typeof old!=='function'||old.__feedback666)return;
                  const wrapped=function(){const result=old.apply(this,arguments);refresh();return result};
                  wrapped.__feedback666=true;window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}
                });
                const oldPrepare=window.prepareSettingsOpen665;
                if(typeof oldPrepare==='function'&&!oldPrepare.__feedback666){
                  const wrapped=function(){const result=oldPrepare.apply(this,arguments);refresh();return result};
                  wrapped.__feedback666=true;window.prepareSettingsOpen665=wrapped;
                }
                const languageSelect=document.getElementById('languageSelect');
                if(languageSelect)languageSelect.addEventListener('change',arrangeDaysOff);
                refresh();
              }catch(e){console.error('Feedback666Ui',e)}
            })();
            """;
    }
}
