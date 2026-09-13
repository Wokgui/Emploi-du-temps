package com.wokgui.schedulewidget;

/**
 * Uses a layout-preserving visibility switch for the two pre-mounted heavy panels.
 *
 * <p>6.48 kept the sheets mounted with a full-viewport clip-path. Measurements on 6.52 showed
 * no DOM/listener/observer work during opening, but Settings still paid a repeatable paint cost
 * in the dedicated 300-cycle run. Visibility keeps the already-prepared layout while avoiding
 * clip-path rasterization on every open/close. A single startup prepaint happens before user
 * interaction so the first reveal does not become the one remaining cold paint path.</p>
 */
final class HeavyPanelExposureUi652 {
    private HeavyPanelExposureUi652() {}

    static String script() {
        return """
            (function(){
              try{
                const style=document.getElementById('edtHeavyPanels648Style');
                if(!style||window.__edtHeavyPanelExposure652)return;
                style.textContent=`
                  #settingsModal.edtHeavyPanel648,#modal.edtHeavyPanel648{
                    display:flex!important;
                    visibility:hidden;
                    clip-path:none!important;
                    contain:layout style paint;
                  }
                  #settingsModal.edtHeavyPanel648[data-edt-open="true"],
                  #modal.edtHeavyPanel648[data-edt-open="true"]{
                    visibility:visible;
                  }
                  #settingsModal.edtHeavyPanel648[data-edt-prepaint="true"],
                  #modal.edtHeavyPanel648[data-edt-prepaint="true"]{
                    visibility:visible!important;
                    opacity:0!important;
                    pointer-events:none!important;
                  }
                `;
                const panels=[document.getElementById('settingsModal'),document.getElementById('modal')].filter(Boolean);
                panels.forEach(function(panel){panel.setAttribute('data-edt-prepaint','true')});
                requestAnimationFrame(function(){
                  panels.forEach(function(panel){panel.getBoundingClientRect()});
                  requestAnimationFrame(function(){
                    panels.forEach(function(panel){panel.removeAttribute('data-edt-prepaint')});
                    window.__edtHeavyPanelExposure652Ready=true;
                  });
                });
                window.__edtHeavyPanelExposure652=true;
                console.log('EDT_HEAVY_EXPOSURE|652|visibility-prepaint');
              }catch(e){console.error('HeavyPanelExposureUi652',e)}
            })();
            """;
    }
}
