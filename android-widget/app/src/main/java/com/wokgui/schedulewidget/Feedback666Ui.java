package com.wokgui.schedulewidget;

/** 6.71 pass: keep the previous week visible until the final replacement paint. */
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
                  let rendering=false,coverToken=0;
                  const removeCovers=()=>document.querySelectorAll('.weekSwapCover669').forEach(node=>node.remove());
                  const finishSwap=cover=>{
                    if(!cover||!cover.isConnected)return;
                    const token=Number(cover.dataset.weekSwapToken671||0);
                    setTimeout(()=>requestAnimationFrame(()=>requestAnimationFrame(()=>{
                      if(token===coverToken&&cover.isConnected)cover.remove();
                    })),0);
                  };
                  const beginSwap=()=>{
                    const live=document.getElementById('weekGrid');
                    if(!live||!live.parentNode)return null;
                    const parent=live.parentNode,rect=live.getBoundingClientRect(),parentRect=parent.getBoundingClientRect();
                    removeCovers();
                    const cover=live.cloneNode(true),token=++coverToken;
                    cover.classList.add('weekSwapCover669');cover.setAttribute('aria-hidden','true');
                    cover.dataset.weekSwapToken671=String(token);
                    cover.style.position='absolute';cover.style.left=(rect.left-parentRect.left+parent.scrollLeft)+'px';
                    cover.style.top=(rect.top-parentRect.top+parent.scrollTop)+'px';cover.style.width=Math.max(1,Math.round(rect.width))+'px';
                    cover.style.height=Math.max(1,Math.round(rect.height))+'px';cover.style.margin='0';cover.style.pointerEvents='none';
                    cover.style.visibility='visible';cover.style.opacity='1';cover.style.zIndex='30';cover.style.background=getComputedStyle(live).backgroundColor||'#f7f9fc';
                    if(getComputedStyle(parent).position==='static')parent.style.position='relative';
                    parent.appendChild(cover);
                    return cover;
                  };
                  window.beginWeekSwap669=beginSwap;
                  window.finishWeekSwap671=finishSwap;
                  const wrapped=function(){
                    if(rendering)return old.apply(this,arguments);
                    const cover=beginSwap();
                    rendering=true;
                    try{
                      const result=old.apply(this,arguments);
                      if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                      if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                      if(window.paintWeek69)window.paintWeek69();
                      return result;
                    }catch(error){if(cover)cover.remove();throw error}
                    finally{rendering=false;if(cover)finishSwap(cover)}
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
