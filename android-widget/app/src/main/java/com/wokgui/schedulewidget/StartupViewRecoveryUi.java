package com.wokgui.schedulewidget;

/** Restores the requested timetable view after every runtime UI layer has been injected. */
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
                    function restore(){
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
                        return !!(target&&target.classList.contains('active'));
                      }catch(e){
                        console.log('StartupViewRecoveryUi',e);
                        return false;
                      }
                    }
                    window.refreshStartupViewRecovery=restore;
                    restore();
                    setTimeout(restore,20);
                  }catch(e){console.log('StartupViewRecoveryUi',e)}
                })();
                """;
    }
}
