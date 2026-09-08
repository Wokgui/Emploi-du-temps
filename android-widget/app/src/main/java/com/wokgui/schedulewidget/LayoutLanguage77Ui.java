package com.wokgui.schedulewidget;

final class LayoutLanguage77Ui {
    private LayoutLanguage77Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__layoutLanguage77V1){if(window.refreshLayoutLanguage77)window.refreshLayoutLanguage77();return}
                window.__layoutLanguage77V1=true;
                const APP_VERSION='6.17';

                const style=document.createElement('style');
                style.id='layoutLanguage77Style';
                style.textContent=`
                  /* Settings title is visually centred despite the close button on the right. */
                  #settingsSheet .settingsHead{position:relative!important;justify-content:flex-end!important;min-height:36px!important}
                  #settingsSheet #settingsTitle{position:absolute!important;left:50%!important;top:50%!important;transform:translate(-50%,-50%)!important;width:max-content!important;max-width:calc(100% - 88px)!important;margin:0!important;text-align:center!important}

                  /* Requested centred advanced-settings headings. */
                  #advReminderTitle,#advCalendarTitle,#advExceptionsTitle,#advProfilesTitle,#advBackupTitle,
                  #schoolTitle,#advRangeTitle,#advExceptionFormTitle{width:100%!important;text-align:center!important}
                  #advExceptionsTitle + .advButtons,
                  #advBackupTitle + .advButtons,
                  #advProfilesTitle ~ .advButtons{justify-content:center!important}
                  #advAddException,#advAddRange{display:block!important;margin-left:auto!important;margin-right:auto!important;text-align:center!important}
                  #advAddRange{min-width:112px!important}
                  #schoolCalendarBlock .schoolTitle{text-align:center!important;width:100%!important}

                  /* Keep the add-period action clearly below its centred heading and fields. */
                  #advRangeTitle{margin-top:11px!important;margin-bottom:7px!important}
                  #advRangeTitle ~ .advButtons{justify-content:center!important}

                  /* Main app title: slightly lower in the top band on all three views. */
                  .header h1{position:relative!important;top:3px!important}

                  #languageSelect,#languageDownloadBtn{touch-action:manipulation!important;-webkit-tap-highlight-color:transparent!important}
                `;
                document.head.appendChild(style);

                function bindLanguage(){
                  const select=document.getElementById('languageSelect');
                  if(!select||select.__language77Bound)return;
                  select.__language77Bound=true;
                  select.addEventListener('change',()=>{
                    /* PersonalizationUi persists first; then force the complete translation pass. */
                    setTimeout(()=>{try{if(window.refreshLocalization75)window.refreshLocalization75()}catch(e){};refresh()},0);
                  });
                }

                function centreActionGroups(){
                  const ids=['advAddException','advAddRange','advShareBackup','advRestoreBackup','advNewProfile','advRenameProfile','advDeleteProfile'];
                  ids.forEach(id=>{const b=document.getElementById(id),p=b&&b.parentElement;if(p&&p.classList.contains('advButtons'))p.style.setProperty('justify-content','center','important')});
                }

                function setVersion(){const v=document.getElementById('appVersionInfo');if(v)v.textContent='Version '+APP_VERSION}
                function refresh(){bindLanguage();centreActionGroups();setVersion()}
                window.refreshLayoutLanguage77=refresh;

                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__layout77)return;const w=function(){const r=old.apply(this,arguments);requestAnimationFrame(refresh);return r};w.__layout77=true;window[name]=w;try{eval(name+'=w')}catch(e){}}
                ['refreshSettingsV3','refreshAdvancedFeatures','refreshUiPolishSchool','refreshLocalization75'].forEach(wrap);
                refresh();requestAnimationFrame(refresh);setTimeout(refresh,120);
              }catch(e){console.log('LayoutLanguage77Ui',e)}
            })();
            """;
    }
}
