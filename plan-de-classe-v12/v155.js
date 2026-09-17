(function(){
  'use strict';

  let currentSeatIndex = null;
  let activePress = null;
  let suppressNativeClickUntil = 0;

  function cancelActivePress(){
    if(!activePress)return;
    if(activePress.timer){clearTimeout(activePress.timer);activePress.timer=null;}
    try{
      if(activePress.seat && activePress.pointerId!==null && activePress.seat.hasPointerCapture?.(activePress.pointerId)){
        activePress.seat.releasePointerCapture(activePress.pointerId);
      }
    }catch(e){}
    activePress=null;
  }

  function openEditorForIndex(idx){
    const c=C();
    const sid=c.seats[idx];
    if(!sid)return;
    currentSeatIndex=idx;
    ensureDeleteSeatButton();
    const fn=(typeof window.v5OpenStudentEditor==='function')
      ? window.v5OpenStudentEditor
      : (window.__planV15 && typeof window.__planV15.openStudentEditor==='function' ? window.__planV15.openStudentEditor : null);
    if(typeof fn==='function'){fn(sid);ensureDeleteSeatButton();}
  }

  function tapSeat(idx){
    if(Date.now()<suppressNativeClickUntil)return;
    if(pick===null){pick=idx;renderPlan();return;}
    if(pick===idx){pick=null;renderPlan();return;}
    snap();
    seatSwap(pick,idx);
  }

  function bindStableSeat(seat){
    seat.onclick=null;
    seat.onpointerdown=null;
    seat.onpointerup=null;
    seat.onpointercancel=null;
    seat.onpointerleave=null;

    const start=(e)=>{
      if(e.pointerType==='mouse' && e.button!==0)return;
      const idx=Number(seat.dataset.i);
      if(!Number.isFinite(idx))return;

      cancelActivePress();
      e.preventDefault();
      e.stopPropagation();

      const sid=C().seats[idx];
      const press={seat,idx,pointerId:e.pointerId,x:e.clientX,y:e.clientY,moved:false,long:false,timer:null};
      activePress=press;
      try{seat.setPointerCapture?.(e.pointerId);}catch(err){}

      if(sid){
        press.timer=setTimeout(()=>{
          if(activePress!==press || press.moved)return;
          press.timer=null;
          press.long=true;
          suppressNativeClickUntil=Date.now()+900;
          try{navigator.vibrate?.(24);}catch(err){}
          openEditorForIndex(idx);
        },460);
      }
    };

    const move=(e)=>{
      const p=activePress;
      if(!p || p.seat!==seat)return;
      if(Math.hypot(e.clientX-p.x,e.clientY-p.y)>14){
        p.moved=true;
        if(p.timer){clearTimeout(p.timer);p.timer=null;}
      }
    };

    const end=(e)=>{
      const p=activePress;
      if(!p || p.seat!==seat)return;
      e.preventDefault();
      e.stopPropagation();
      const doTap=!p.long && !p.moved;
      const idx=p.idx;
      cancelActivePress();
      if(doTap)tapSeat(idx);
    };

    const cancel=()=>{
      if(activePress?.seat===seat)cancelActivePress();
    };

    seat.addEventListener('pointerdown',start,{passive:false});
    seat.addEventListener('pointermove',move,{passive:false});
    seat.addEventListener('pointerup',end,{passive:false});
    seat.addEventListener('pointercancel',cancel,{passive:false});
    seat.addEventListener('contextmenu',e=>{e.preventDefault();e.stopPropagation();});
    seat.addEventListener('dragstart',e=>e.preventDefault());
    seat.addEventListener('click',e=>{e.preventDefault();e.stopImmediatePropagation();},true);
  }

  function installStableSeatInteractions(){
    cancelActivePress();
    document.querySelectorAll('#room .seat').forEach(bindStableSeat);
  }

  function locateSeat(c,idx){
    let offset=0;
    for(let r=0;r<c.rows.length;r++){
      for(let ci=0;ci<c.rows[r].length;ci++){
        const count=Math.max(1,+c.rows[r][ci]||1);
        if(idx>=offset && idx<offset+count)return{r,ci,t:idx-offset,count};
        offset+=count;
      }
    }
    return null;
  }

  function removeSeatAt(idx){
    const c=C();
    const loc=locateSeat(c,idx);
    if(!loc)return;
    if(c.rows.length===1 && c.rows[0].length===1 && c.rows[0][0]===1){
      toast('Il faut garder au moins une place.');
      return;
    }

    const sid=c.seats[idx];
    const st=sid?studentById(sid,c):null;
    const label=st?.name ? `Supprimer la place de ${st.name} ?` : 'Supprimer cette place ?';
    if(!confirm(label))return;

    snap();
    c.seats.splice(idx,1);

    if(loc.count>1){
      c.rows[loc.r][loc.ci]=loc.count-1;
    }else if(c.rows[loc.r].length>1){
      c.rows[loc.r].splice(loc.ci,1);
      if(Array.isArray(c.rowModes?.[loc.r]))c.rowModes[loc.r].splice(loc.ci,1);
    }else{
      c.rows.splice(loc.r,1);
      if(Array.isArray(c.rowModes))c.rowModes.splice(loc.r,1);
      if(c.landmarks){
        ['tableau','bureau'].forEach(k=>{
          const o=c.landmarks[k];
          if(o && Number.isFinite(+o.slot) && +o.slot>loc.r)o.slot=Math.max(0,+o.slot-1);
        });
      }
    }

    norm(c);
    pick=null;
    currentSeatIndex=null;
    save();
    document.getElementById('studentEditorV15')?.classList.remove('on');
    renderCfg();
    renderStudents();
    renderPlan();
    toast('Place supprimée');
  }

  function ensureDeleteSeatButton(){
    const bg=document.getElementById('studentEditorV15');
    if(!bg)return;
    const modal=bg.querySelector('.v15-student-editor');
    if(!modal)return;
    let btn=document.getElementById('deleteSeatV155');
    if(!btn){
      btn=document.createElement('button');
      btn.id='deleteSeatV155';
      btn.type='button';
      btn.className='v155-delete-seat';
      btn.textContent='Supprimer cette place du plan';
      btn.onclick=()=>{
        if(currentSeatIndex===null)return;
        removeSeatAt(currentSeatIndex);
      };
      const actions=modal.querySelector('.v15-editor-actions');
      if(actions)actions.insertAdjacentElement('afterend',btn);else modal.appendChild(btn);
    }
    btn.hidden=currentSeatIndex===null;
  }

  const baseRenderPlan=renderPlan;
  renderPlan=function(){
    baseRenderPlan();
    installStableSeatInteractions();
  };

  document.addEventListener('pointerdown',e=>{
    const bg=document.getElementById('studentEditorV15');
    if(bg && e.target===bg){currentSeatIndex=null;}
  },true);

  renderPlan();
  window.__planV155={removeSeatAt,installStableSeatInteractions};
})();
