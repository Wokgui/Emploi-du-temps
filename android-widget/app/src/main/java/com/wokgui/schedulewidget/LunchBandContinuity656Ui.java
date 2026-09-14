package com.wokgui.schedulewidget;

/** Final CSS pass: lunch remains one uninterrupted horizontal band across weekdays. */
final class LunchBandContinuity656Ui {
    private LunchBandContinuity656Ui() {}

    static String script() {
        return """
            (function(){
              if(document.getElementById('lunchBandContinuity656Style'))return;
              const s=document.createElement('style');
              s.id='lunchBandContinuity656Style';
              s.textContent=`
                #weekGrid .wc:is(.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic){
                  background:var(--ft-midi)!important;
                  color:var(--ft-midi-ink)!important;
                  border-left-width:0!important;
                  border-right-width:0!important;
                  border-radius:0!important;
                  box-shadow:1px 0 0 var(--ft-midi),-1px 0 0 var(--ft-midi)!important
                }
                #weekGrid .wc:is(.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell,.lunch655Synthetic) *{
                  color:var(--ft-midi-ink)!important
                }
              `;
              document.head.appendChild(s);
            })();
            """;
    }
}
