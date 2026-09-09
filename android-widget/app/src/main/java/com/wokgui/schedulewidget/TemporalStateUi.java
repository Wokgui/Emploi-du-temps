package com.wokgui.schedulewidget;

/** Clock-driven Today-view behavior shared by normal runtime and deterministic tests. */
final class TemporalStateUi {
    private TemporalStateUi() {}

    static String script() {
        return """
                (function(){
                  if(window.__temporalState635)return;
                  window.__temporalState635=true;

                  function currentText(){
                    try{
                      var lang=(document.documentElement.lang||localStorage.getItem('language')||'fr').toLowerCase();
                      if(lang.indexOf('de')===0)return 'Aktuell';
                      if(lang.indexOf('en')===0)return 'Current';
                    }catch(e){}
                    return 'En cours';
                  }

                  window.progressPercent=function(){
                    try{
                      var jsDay=new Date().getDay();
                      var d=jsDay>=1&&jsDay<=5?jsDay+1:null;
                      if(d===null||typeof weeks==='undefined'||!weeks[currentWeek]||!weeks[currentWeek][d])return 0;
                      var list=weeks[currentWeek][d].courses||[];
                      if(!list.length)return 0;
                      var now=new Date(),nowM=now.getHours()*60+now.getMinutes();
                      var first=min(list[0].start),last=min(list[list.length-1].end);
                      if(last<=first||nowM<=first)return 0;
                      if(nowM>=last)return 100;
                      return Math.max(0,Math.min(100,(nowM-first)*100/(last-first)));
                    }catch(e){return 0}
                  };

                  function markCurrentBreak(){
                    try{
                      var now=new Date(),nowM=now.getHours()*60+now.getMinutes();
                      document.querySelectorAll('#todayList .todayCourse.gap,#todayList .todayCourse.lunch').forEach(function(row){
                        row.classList.remove('current');
                        var existing=row.querySelector('.badge[data-temporal-current="1"]');
                        if(existing)existing.remove();
                        var time=row.querySelector('.time');
                        if(!time)return;
                        var values=(time.textContent||'').match(/[0-9]{1,2}:[0-9]{2}/g)||[];
                        if(values.length<2)return;
                        var start=min(values[0]),end=min(values[1]);
                        if(nowM>=start&&nowM<end){
                          row.classList.add('current');
                          var badge=row.querySelector('.badge');
                          if(!badge){
                            badge=document.createElement('div');
                            badge.className='badge';
                            badge.dataset.temporalCurrent='1';
                            row.appendChild(badge);
                          }
                          badge.textContent=currentText();
                        }
                      });
                      var p=Math.round(window.progressPercent());
                      var bar=document.getElementById('todayProgress');
                      if(bar)bar.setAttribute('aria-label','Avancement '+p+' %');
                    }catch(e){}
                  }

                  if(typeof renderToday==='function'){
                    var baseRenderToday=renderToday;
                    renderToday=function(){
                      baseRenderToday();
                      markCurrentBreak();
                    };
                    // MainActivity's deterministic-clock hook recognizes this flag and
                    // only swaps the clock; rendering remains owned by this module.
                    window.__edtTemporalWrapped=true;
                  }

                  window.refreshTemporalState635=markCurrentBreak;
                })();
                """;
    }
}
