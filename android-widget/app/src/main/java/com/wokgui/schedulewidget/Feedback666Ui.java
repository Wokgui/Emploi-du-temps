package com.wokgui.schedulewidget;

/** 6.68 pass: off-screen week composition and final advanced-settings ordering. */
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
                    const live=document.getElementById('weekGrid');
                    if(rendering||!live||!live.parentNode)return old.apply(this,arguments);
                    const title=document.getElementById('weekTitleLetter');
                    const previousTitle=title?title.textContent:'';
                    const candidate=live.cloneNode(false);
                    const width=Math.max(1,Math.round(live.getBoundingClientRect().width||live.offsetWidth||1));
                    const originalStyle={
                      position:candidate.style.position,left:candidate.style.left,top:candidate.style.top,
                      width:candidate.style.width,visibility:candidate.style.visibility,
                      pointerEvents:candidate.style.pointerEvents,zIndex:candidate.style.zIndex
                    };
                    live.id='weekGridStable668';
                    candidate.id='weekGrid';
                    candidate.setAttribute('aria-hidden','true');
                    candidate.style.position='fixed';candidate.style.left='-10000px';candidate.style.top='0';
                    candidate.style.width=width+'px';candidate.style.visibility='hidden';
                    candidate.style.pointerEvents='none';candidate.style.zIndex='-1';
                    live.parentNode.insertBefore(candidate,live.nextSibling);
                    rendering=true;
                    try{
                      const result=old.apply(this,arguments);
                      const valid=candidate.children.length>=6&&candidate.querySelectorAll('.wh.day').length>0;
                      if(!valid){
                        if(title)title.textContent=previousTitle;
                        return result;
                      }
                      candidate.style.position=originalStyle.position;candidate.style.left=originalStyle.left;
                      candidate.style.top=originalStyle.top;candidate.style.width=originalStyle.width;
                      candidate.style.visibility=originalStyle.visibility;candidate.style.pointerEvents=originalStyle.pointerEvents;
                      candidate.style.zIndex=originalStyle.zIndex;candidate.removeAttribute('aria-hidden');
                      const nextClass=candidate.className;
                      const nextStyle=candidate.getAttribute('style');
                      const children=Array.from(candidate.childNodes);
                      candidate.remove();
                      live.id='weekGrid';
                      live.className=nextClass;
                      if(nextStyle===null||nextStyle==='')live.removeAttribute('style');else live.setAttribute('style',nextStyle);
                      live.replaceChildren(...children);
                      if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                      if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                      if(window.paintWeek69)window.paintWeek69();
                      return result;
                    }catch(error){
                      if(title)title.textContent=previousTitle;
                      throw error;
                    }finally{
                      if(candidate.isConnected)candidate.remove();
                      live.id='weekGrid';
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
