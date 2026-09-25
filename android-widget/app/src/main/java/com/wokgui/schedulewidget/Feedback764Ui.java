package com.wokgui.schedulewidget;

/** Final 7.64 owner for responsive headers, tall Today layout and settings alignment. */
final class Feedback764Ui {
    private Feedback764Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback764){window.refreshFeedback764&&window.refreshFeedback764();return}
                window.__feedback764=true;
                function language(){try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr')).toLowerCase()}catch(e){return 'fr'}}
                function locale(){const l=language();return l.startsWith('de')?'de-DE':(l.startsWith('en')?'en-GB':'fr-FR')}
                function tr(fr,en,de){const l=language();return l.startsWith('de')?de:(l.startsWith('en')?en:fr)}
                function advanced(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function cycleCount(){const a=advanced();return a.singleWeek===true?1:Math.max(2,Math.min(4,Number(a.cycleLength)||2))}
                function start(value){const d=new Date(value||Date.now());d.setHours(0,0,0,0);return d}
                function monday(value){const d=start(value),day=d.getDay();d.setDate(d.getDate()+(day===0?-6:1-day));return d}
                function add(value,days){const d=start(value);d.setDate(d.getDate()+Number(days||0));return d}
                function currentLetter(){try{return String(currentWeek||'A').toUpperCase()}catch(e){return 'A'}}
                function activeLetter(){try{return String(activeWeek||currentWeek||'A').toUpperCase()}catch(e){return 'A'}}
                function weekOffset(letter){const count=cycleCount();if(count===1)return 0;const letters=['A','B','C','D'].slice(0,count),from=Math.max(0,letters.indexOf(currentLetter())),to=letters.indexOf(letter);if(to<0)return 0;let n=(to-from+count)%count;if(n>count/2)n-=count;return n}
                function selectedDate(){let day=2;try{day=Number(selected)||2}catch(e){}const dayOffset=day===1?6:Math.max(0,Math.min(6,day-2));return add(monday(new Date()),weekOffset(activeLetter())*7+dayOffset)}
                function dated(date){const weekday=date.toLocaleDateString(locale(),{weekday:'long'}),name=weekday?weekday.charAt(0).toUpperCase()+weekday.slice(1):'',longDate=date.toLocaleDateString(locale(),{day:'numeric',month:'long',year:'numeric'});if(language().startsWith('fr'))return name+' le '+(date.getDate()===1?'1er '+date.toLocaleDateString(locale(),{month:'long',year:'numeric'}):longDate);if(language().startsWith('de'))return name+', '+longDate;return name+' '+longDate}

                const style=document.createElement('style');style.id='feedback764Style';style.textContent=`
                  #viewToday .dayTitle>h2,#viewWeek .weekTop>h2{
                    min-width:0!important;max-width:100%!important;margin:0!important;text-align:center!important;
                    color:var(--ink,#111936)!important;font-family:inherit!important;font-size:clamp(15px,4.15vw,18px)!important;
                    font-weight:900!important;line-height:1.15!important;letter-spacing:0!important;white-space:normal!important;
                    overflow:visible!important;overflow-wrap:anywhere!important;text-overflow:clip!important
                  }
                  #todayDate,#weekCycleLabel757,#editCycleLabel764{font-size:12px!important;font-weight:800!important;line-height:1.2!important;color:var(--muted,#68738a)!important;text-align:center!important}
                  html.singleWeek764 #todayDate,html.singleWeek764 #weekCycleLabel757,html.singleWeek764 #editCycleLabel764{display:none!important}
                  #viewToday .todayList{display:flex!important;flex-direction:column!important;overflow:visible!important}
                  #viewToday .todayList>.todayCourse,#viewToday .todayList>.empty{flex:1 0 auto!important;box-sizing:border-box!important}
                  #viewToday .todayCourse{grid-template-columns:minmax(54px,4.2em) minmax(0,1fr) auto!important;min-width:0!important}
                  #viewToday .todayCourse>div:nth-child(2),#viewToday .todayCourse .label,#viewToday .todayCourse .room{min-width:0!important;overflow-wrap:anywhere!important}
                  #viewEdit .sectionHead:has(#editDayTitle){display:grid!important;grid-template-columns:1fr!important;justify-items:center!important;gap:2px!important;text-align:center!important;margin-top:8px!important}
                  #editDayTitle{font-size:clamp(15px,4.15vw,18px)!important;font-weight:900!important;line-height:1.15!important;text-align:center!important;overflow-wrap:anywhere!important}
                  #editCycleLabel764,#editCount{display:block!important;text-align:center!important;margin:0!important}
                  #editCount{font-size:.72rem!important}
                  #settingsSheet #advancedSettings85 #advCalendarTitle{display:none!important}
                  #settingsSheet #advancedSettings85 #advReminderTitle,#settingsSheet #advancedSettings85 #advExceptionsTitle,
                  #settingsSheet #advancedSettings85 #advProfilesTitle,#settingsSheet #advancedSettings85 #advBackupTitle,
                  #settingsSheet #advancedSettings85 #schoolTitle{
                    width:100%!important;margin:0 0 6px!important;text-align:center!important;color:var(--set-dark,var(--ink,#111936))!important;
                    font-size:.72rem!important;font-weight:850!important;line-height:1.2!important
                  }
                  #settingsSheet #breakSettings86 #breakNamesSettings763>.settingTitle,#settingsSheet #breakSettings86 #week658LunchSettings .w658Title{
                    margin:4px 0 9px!important;text-align:center!important;color:var(--ink,#111936)!important;
                    font-size:var(--settings86-heading-size,.94rem)!important;font-weight:900!important;line-height:1.15!important
                  }
                  #colorSettings86 #paletteSettingRoot>.settingTitle,#colorSettings86 #week658Settings .w658Title{
                    margin:0 0 6px!important;text-align:center!important;color:#586579!important;font-size:.74rem!important;font-weight:850!important;line-height:1.2!important
                  }
                  #colorSettings86 #week658Settings .w658Colors{grid-template-columns:minmax(0,1fr) 42px!important;gap:6px 9px!important;width:min(280px,100%)!important;margin:0 auto!important;font-size:.72rem!important}
                  #colorSettings86 #week658Settings input[type=color]{width:38px!important;height:30px!important;justify-self:end!important;padding:2px!important}
                `;document.head.appendChild(style);

                function fitToday(){
                  const view=document.getElementById('viewToday'),list=document.getElementById('todayList'),bottom=document.querySelector('.bottom');if(!view||!list||!bottom)return;
                  if(!view.classList.contains('active')){list.style.removeProperty('min-height');return}
                  const available=Math.max(120,Math.floor(bottom.getBoundingClientRect().top-list.getBoundingClientRect().top-12));list.style.minHeight=available+'px';
                }
                function editHeader(){
                  const title=document.getElementById('editDayTitle'),count=document.getElementById('editCount'),head=title&&title.parentElement;if(!title||!head)return;
                  let cycle=document.getElementById('editCycleLabel764');if(!cycle){cycle=document.createElement('span');cycle.id='editCycleLabel764';cycle.className='count';head.insertBefore(cycle,count||null)}
                  title.textContent=dated(selectedDate());cycle.textContent=cycleCount()===1?'':tr('Semaine ','Week ','Woche ')+activeLetter();
                }
                function cleanSingleWeek(){
                  const single=cycleCount()===1;document.documentElement.classList.toggle('singleWeek764',single);
                  if(single){const today=document.getElementById('todayDate'),week=document.getElementById('weekCycleLabel757');if(today)today.textContent='';if(week)week.textContent=''}
                }
                function polishSettings(){
                  const calendar=document.getElementById('advCalendarTitle');if(calendar){calendar.hidden=true;calendar.setAttribute('aria-hidden','true')}
                  const density=document.getElementById('widgetDensity664'),widgetFont=document.getElementById('widgetFont'),textBody=document.querySelector('#textSettings86>.settingsSectionBody86');
                  const fontBox=widgetFont&&widgetFont.closest('.settingBox');if(density&&textBody&&fontBox&&density.previousElementSibling!==fontBox)fontBox.insertAdjacentElement('afterend',density);
                }
                function afterRender(){cleanSingleWeek();editHeader();fitToday();polishSettings();requestAnimationFrame(()=>{fitToday();editHeader();polishSettings()})}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback764)return;const next=function(){const result=old.apply(this,arguments);afterRender();return result};next.__feedback764=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                function install(){wrap('renderToday');wrap('renderWeek');wrap('renderEdit');wrap('render')}
                function refresh(){install();afterRender()}
                window.refreshFeedback764=refresh;
                addEventListener('resize',()=>requestAnimationFrame(fitToday),{passive:true});
                document.addEventListener('change',event=>{if(event.target&&['appFont','languageSelect'].includes(event.target.id))requestAnimationFrame(afterRender)},true);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(()=>requestAnimationFrame(polishSettings)).observe(sheet,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback764Ui',e)}
            })();
            """;
    }
}
