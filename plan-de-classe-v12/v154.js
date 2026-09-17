(function(){
  'use strict';
  let suppressClickUntil=0;

  function openEditorForSeat(seat){
    const idx=Number(seat.dataset.i);
    const c=C();
    const sid=Number.isFinite(idx)?c.seats[idx]:null;
    if(!sid)return;
    const fn=(typeof window.v5OpenStudentEditor==='function')?window.v5OpenStudentEditor:(window.__planV15&&typeof window.__planV15.openStudentEditor==='function'?window.__planV15.openStudentEditor:null);
    if(typeof fn==='function')fn(sid);
  }

  function installReliableLongPress(){
    document.querySelectorAll('#room .seat').forEach(seat=>{
      if(seat.dataset.longPress154==='1')return;
      seat.dataset.longPress154='1';

      // Remove the previous long-press handlers from v14; keep the normal tap/click swap handler.
      seat.onpointerdown=null;
      seat.onpointerup=null;
      seat.onpointercancel=null;
      seat.onpointerleave=null;

      let timer=null,startX=0,startY=0,pointerId=null;
      const cancel=()=>{
        if(timer){clearTimeout(timer);timer=null;}
        if(pointerId!==null){
          try{if(seat.hasPointerCapture&&seat.hasPointerCapture(pointerId))seat.releasePointerCapture(pointerId)}catch(e){}
        }
        pointerId=null;
      };

      seat.addEventListener('pointerdown',e=>{
        if(e.pointerType==='mouse'&&e.button!==0)return;
        const idx=Number(seat.dataset.i),sid=C().seats[idx];
        if(!sid)return;
        cancel();
        startX=e.clientX;startY=e.clientY;pointerId=e.pointerId;
        try{if(seat.setPointerCapture)seat.setPointerCapture(e.pointerId)}catch(err){}
        timer=setTimeout(()=>{
          timer=null;
          suppressClickUntil=Date.now()+900;
          try{navigator.vibrate&&navigator.vibrate(25)}catch(err){}
          openEditorForSeat(seat);
        },430);
      });

      seat.addEventListener('pointermove',e=>{
        if(!timer)return;
        const dx=e.clientX-startX,dy=e.clientY-startY;
        if(Math.hypot(dx,dy)>16)cancel();
      });
      seat.addEventListener('pointerup',cancel);
      seat.addEventListener('pointercancel',cancel);
      seat.addEventListener('contextmenu',e=>e.preventDefault());
      seat.addEventListener('click',e=>{
        if(Date.now()<suppressClickUntil){
          e.preventDefault();
          e.stopImmediatePropagation();
        }
      },true);
    });
  }

  const baseRenderPlan=renderPlan;
  renderPlan=function(){
    baseRenderPlan();
    installReliableLongPress();
  };

  renderPlan();
  window.__planLongPress154={install:installReliableLongPress};
})();
