package com.wokgui.schedulewidget;

/** Restores the requested timetable view once after the runtime UI has been injected. */
final class StartupViewRecoveryUi {
    private StartupViewRecoveryUi() {}

    static String script() {
        return """
                (function(){
                  try{
                    function validMode(value){
                      return value==='today'||value==='week'||value==='edit';
                    }
                    function currentMode(){
                      try{
                        if(typeof mode!=='undefined'&&validMode(mode))return mode;
                      }catch(e){}
                      var active=document.querySelector('.nav.active[data-mode]');
                      if(active&&validMode(active.dataset.mode))return active.dataset.mode;
                      return 'edit';
                    }
                    function state(tag){
                      try{
                        var active=[].map.call(document.querySelectorAll('.view.active'),function(v){return v.id}).join(',');
                        var body=document.body;
                        console.log('EDT_STARTUP_STATE|'+tag+
                          '|ready='+document.readyState+
                          '|mode='+currentMode()+
                          '|active='+active+
                          '|text='+(body?(body.innerText||'').length:-1)+
                          '|children='+(body?body.children.length:-1)+
                          '|size='+(body?body.clientWidth+'x'+body.clientHeight:'none'));
                      }catch(e){console.log('EDT_STARTUP_STATE|'+tag+'|error='+e)}
                    }
                    function restore(tag){
                      try{
                        var wanted=currentMode();
                        if(typeof window.setModeFromAndroid==='function'){
                          window.setModeFromAndroid(wanted);
                        }else{
                          try{if(typeof mode!=='undefined')mode=wanted}catch(e){}
                          var targetId='view'+wanted.charAt(0).toUpperCase()+wanted.slice(1);
                          document.querySelectorAll('.view').forEach(function(view){
                            view.classList.toggle('active',view.id===targetId);
                          });
                          document.querySelectorAll('.nav[data-mode]').forEach(function(nav){
                            nav.classList.toggle('active',nav.dataset.mode===wanted);
                          });
                          if(typeof window.render==='function')window.render();
                        }
                        var target=document.getElementById('view'+wanted.charAt(0).toUpperCase()+wanted.slice(1));
                        if(target&&!target.classList.contains('active'))target.classList.add('active');
                        document.documentElement.style.visibility='visible';
                        if(document.body){document.body.style.visibility='visible';document.body.style.opacity='1';}
                        state(tag);
                        return !!(target&&target.classList.contains('active'));
                      }catch(e){
                        console.log('StartupViewRecoveryUi',e);
                        state(tag+'-failed');
                        return false;
                      }
                    }
                    window.refreshStartupViewRecovery=function(){return restore('manual')};
                    restore('inject');
                  }catch(e){console.log('StartupViewRecoveryUi',e)}
                })();
                """;
    }
}
