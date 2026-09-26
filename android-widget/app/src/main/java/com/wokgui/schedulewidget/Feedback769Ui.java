package com.wokgui.schedulewidget;

/** 7.70 final owner for the symmetric widget settings layout. */
final class Feedback769Ui {
    private Feedback769Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback769){window.refreshFeedback769&&window.refreshFeedback769();return}
                window.__feedback769=true;
                const VERSION='7.70';
                let scheduled=false;
                const style=document.createElement('style');style.id='feedback769Style';style.textContent=`
                  #widgetSettings86 .displayGrid767{grid-template-columns:minmax(112px,1.1fr) repeat(2,minmax(96px,1fr))!important}
                  #widgetSettings86 .displayHead767{background:#fff!important;color:var(--ink,#111936)!important;font-size:.68rem!important;font-weight:800!important}
                  #widgetSettings86 .accessCell769{padding-left:2px!important;padding-right:2px!important}
                  #widgetSettings86 .accessCell769 select{width:100%!important;max-width:none!important;min-width:0!important;padding:6px 13px 6px 2px!important;font-size:8px!important;font-weight:750!important;-webkit-appearance:none!important;appearance:none!important;background-image:linear-gradient(45deg,transparent 50%,#667085 50%),linear-gradient(135deg,#667085 50%,transparent 50%)!important;background-position:calc(100% - 9px) 50%,calc(100% - 5px) 50%!important;background-size:4px 4px!important;background-repeat:no-repeat!important}
                  #widgetSettings86 #widgetFollowingRow769{display:grid!important;grid-template-columns:minmax(148px,1.3fr) minmax(125px,1fr)!important;gap:8px!important;align-items:center!important}
                  #widgetSettings86 #widgetFollowingRow769>span{text-align:left!important}
                  #widgetSettings86 #widgetFollowingRow769>select{width:100%!important;max-width:none!important;min-width:0!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Grid{grid-template-columns:minmax(148px,1.3fr) minmax(125px,1fr) auto!important;grid-template-rows:auto auto!important;grid-auto-flow:row!important;gap:8px!important}
                  #widgetSettings86 #widgetEdgeBars672 .bar672Label{text-align:left!important;line-height:1.25!important}
                  #widgetSettings86 #widgetEdgeBars672 label[for="widgetTopBarMode672"]{grid-column:1!important;grid-row:1!important}
                  #widgetSettings86 #widgetTopBarMode672{grid-column:2!important;grid-row:1!important}
                  #widgetSettings86 #widgetTopBarColor672{grid-column:3!important;grid-row:1!important}
                  #widgetSettings86 #widgetEdgeBars672 label[for="widgetBottomBarMode672"]{grid-column:1!important;grid-row:2!important}
                  #widgetSettings86 #widgetBottomBarMode672{grid-column:2!important;grid-row:2!important}
                  #widgetSettings86 #widgetBottomBarColor672{grid-column:3!important;grid-row:2!important}
                  #widgetSettings86 #widgetEdgeBars672 input[type=color]:not(.bar672Active){display:none!important}
                  @media(max-width:370px){
                    #widgetSettings86 .displayGrid767{grid-template-columns:minmax(106px,1.08fr) repeat(2,minmax(88px,1fr))!important}
                    #widgetSettings86 .accessCell769 select{font-size:7px!important;padding-right:12px!important;background-position:calc(100% - 8px) 50%,calc(100% - 4px) 50%!important}
                    #widgetSettings86 #widgetFollowingRow769{grid-template-columns:minmax(126px,1.2fr) minmax(108px,1fr)!important;gap:5px!important}
                    #widgetSettings86 #widgetEdgeBars672 .bar672Grid{grid-template-columns:minmax(126px,1.2fr) minmax(108px,1fr) auto!important;gap:5px!important}
                  }
                `;document.head.appendChild(style);
                function refresh(){
                  scheduled=false;
                  const format=document.getElementById('advFormat');const formatRow=format&&format.closest('.advRow');if(formatRow)formatRow.remove();
                  const following=document.getElementById('advFollowing');const followingRow=following&&following.closest('.advRow');if(followingRow)followingRow.id='widgetFollowingRow769';
                  for(const id of ['advAppAccess767','advAccess']){const select=document.getElementById(id);const cell=select&&select.closest('.displayCell767');if(cell)cell.classList.add('accessCell769')}
                  const version=document.getElementById('appVersionInfo');if(version)version.textContent='Version '+VERSION;
                }
                function schedule(){if(scheduled)return;scheduled=true;requestAnimationFrame(refresh)}
                function wrap(name){const old=window[name];if(typeof old!=='function'||old.__feedback769)return;const next=function(){const result=old.apply(this,arguments);schedule();return result};next.__feedback769=true;window[name]=next;try{eval(name+'=next')}catch(e){}}
                window.refreshFeedback769=refresh;
                ['render','refreshSettingsLayout','refreshAdvancedFeatures','prepareSettingsOpen665'].forEach(wrap);
                const sheet=document.getElementById('settingsSheet');if(sheet)new MutationObserver(schedule).observe(sheet,{childList:true,subtree:true});
                refresh();
              }catch(e){console.error('Feedback769Ui',e)}
            })();
            """;
    }
}
