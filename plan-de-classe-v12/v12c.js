(function(){
  const ONE_SEAT_VERSION = 'one-seat-v2';

  function totalTablesForRows(rows){
    return (rows||[]).reduce((sum,row)=>sum + (row||[]).reduce((a,b)=>a + Math.max(1, +b || 1), 0), 0);
  }
  function uniquePush(arr, value){ if(value && !arr.includes(value)) arr.push(value); }
  function ensureOneSeatClass(c){
    if(!c) return;
    ensureModesC(c);
    const target = totalTablesForRows(c.rows);
    const original = Array.isArray(c.seats) ? c.seats.slice() : [];
    const result = [];
    const extras = [];

    if(original.length >= target * 2){
      let pos = 0;
      for(let r=0;r<c.rows.length;r++){
        const cols = c.rows[r] || [];
        for(let gi=0;gi<cols.length;gi++){
          const tables = Math.max(1, +cols[gi] || 1);
          for(let t=0;t<tables;t++){
            const a = original[pos++] || null;
            const b = original[pos++] || null;
            const chosen = a || b || null;
            result.push(chosen);
            if(a && b){ uniquePush(extras, chosen===a ? b : a); }
          }
        }
      }
      while(pos < original.length){ uniquePush(extras, original[pos++]); }
    } else {
      for(const sid of original){ uniquePush(extras, sid); }
      while(result.length < target) result.push(null);
    }

    const used = new Set(result.filter(Boolean));
    const fillPool = [];
    extras.forEach(sid=>{ if(sid && !used.has(sid)){ used.add(sid); fillPool.push(sid); } });
    (c.students||[]).forEach(st=>{ if(st && st.id && !used.has(st.id)){ used.add(st.id); fillPool.push(st.id); } });
    let fp = 0;
    for(let i=0;i<Math.max(target, result.length);i++){
      if(result[i] == null && fp < fillPool.length) result[i] = fillPool[fp++];
    }

    if(result.length < target) while(result.length < target) result.push(null);
    if(result.length > target) result.length = target;
    c.seats = result;
    c.seatPerTable = 1;
    c.modelVersion = ONE_SEAT_VERSION;
  }

  function injectOneSeatStyles(){
    if(document.getElementById('v12d-style')) return;
    const st = document.createElement('style');
    st.id = 'v12d-style';
    st.textContent = `
      .desk{min-width:0;background:transparent !important;border:none !important;padding:0 !important;display:block !important;height:100%}
      .tgroup{display:grid;gap:clamp(6px,1.5vw,10px);min-width:0}
      .seat{min-width:0;min-height:24px;border:1px solid #c3cacf;border-radius:7px;background:#fff !important;display:flex;align-items:center;justify-content:center;gap:3px;padding:4px 6px;font-size:clamp(8px,2.4vw,12px);font-weight:780;touch-action:none;user-select:none;overflow:hidden;box-shadow:none}
      .seat.boy,.seat.girl{background:#fff !important;border-color:#c3cacf !important}
      .seat.empty{color:#aab0b4;font-weight:500}
      .seatname{display:block;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:100%;line-height:1.1}
      .seaticons{display:flex;gap:1px;flex:0 0 auto}
    `;
    document.head.appendChild(st);
  }

  cap = function(c=C()){
    return totalTablesForRows(c.rows);
  };

  norm = function(c=C()){
    if(!c.landmarks)c.landmarks={tableau:{visible:true,slot:0,align:'center'},bureau:{visible:true,slot:0,align:'right'}};
    ['tableau','bureau'].forEach(k=>{if(!c.landmarks[k])c.landmarks[k]={visible:true,slot:0,align:k==='tableau'?'center':'right'};});
    ensureOneSeatClass(c);
    const n = cap(c);
    if(c.seats.length < n) while(c.seats.length < n) c.seats.push(null);
    else if(c.seats.length > n) c.seats.length = n;
    ['tableau','bureau'].forEach(k=>{c.landmarks[k].slot=Math.max(0,Math.min(c.rows.length,c.landmarks[k].slot||0))});
  };

  newClass = async function(){
    let n=(await ask('Nom de la classe','Nouvelle classe'))?.trim();
    if(!n) return;
    let rows=INIT_ROWS.map(x=>[...x]);
    let seats = new Array(totalTablesForRows(rows)).fill(null);
    let c={id:uid(),name:n,rows,students:[],seats,rowModes:rows.map(row=>row.map(()=>JOINED)),seatPerTable:1,landmarks:{tableau:{visible:true,slot:0,align:'center'},bureau:{visible:true,slot:0,align:'right'}}};
    snap();S.classes.push(c);S.cur=c.id;save();render();
  };

  duplicateClass=function(){
    const src=C();norm(src);
    const c=JSON.parse(JSON.stringify(src));
    c.id=uid();c.name=src.name+' copie';
    c.students.forEach(s=>{
      const old=s.id;s.id=uid();
      c.seats=c.seats.map(x=>x===old?s.id:x);
    });
    norm(c);
    snap();S.classes.push(c);S.cur=c.id;save();render();
  };

  function fitSeatNamesV12(){
    document.querySelectorAll('.seatname').forEach(n=>{
      const seat=n.closest('.seat'); if(!seat)return;
      let size=Math.min(14,Math.max(8,Math.min(seat.clientHeight*0.42, seat.clientWidth/6.4)));
      n.style.fontSize=size+'px';
      const icons=seat.querySelector('.seaticons');
      const available=Math.max(22,seat.clientWidth-10-(icons?icons.clientWidth:0));
      while(n.scrollWidth>available && size>6.5){size-=0.5;n.style.fontSize=size+'px';}
    });
  }

  renderPlan = function(){
    let c=C(); norm(c); ensureModesC(c);
    let room=document.getElementById('room'); room.innerHTML=''; room.classList.remove('lmdrag');
    let stack=document.createElement('div'); stack.className='rowsstack'; let k=0;
    for(let r=0;r<c.rows.length;r++){
      stack.appendChild(renderLandslot(c,r));
      let row=document.createElement('div'); row.className='row';
      let rn=document.createElement('div'); rn.className='rn'; rn.textContent=r+1;
      let groups=document.createElement('div'); groups.className='tablegroups';
      for(let gi=0;gi<c.rows[r].length;gi++){
        let gcount=c.rows[r][gi], mode=c.rowModes[r][gi]||JOINED, g=document.createElement('div');
        g.className='tgroup '+mode; g.style.gridTemplateColumns=`repeat(${gcount},minmax(0,1fr))`; g.style.flex=`${gcount} 1 0`;
        for(let t=0;t<gcount;t++){
          let desk=document.createElement('div'); desk.className='desk';
          let idx=k++, sid=c.seats[idx], st=sid?studentById(sid,c):null, e=document.createElement('div');
          e.className='seat'+(!st?' empty':'')+(st?.gender==='boy'?' boy':'')+(st?.gender==='girl'?' girl':'')+(pick===idx?' pick':'');
          e.dataset.i=idx;
          if(st){
            let nm=document.createElement('span'); nm.className='seatname'; nm.textContent=st.name; e.appendChild(nm);
            if(st.tags?.length){ let icons=document.createElement('span'); icons.className='seaticons'; st.tags.forEach(tag=>icons.appendChild(iconSpan(tag))); e.appendChild(icons); }
          } else e.textContent='Libre';
          e.title=st?st.name:'Libre'; e.onpointerdown=seatDown; e.onclick=seatClick; desk.appendChild(e); g.appendChild(desk);
        }
        groups.appendChild(g);
      }
      row.append(rn,groups); stack.appendChild(row);
    }
    stack.appendChild(renderLandslot(c,c.rows.length)); room.appendChild(stack);
    document.getElementById('undo').disabled=!H.length; document.getElementById('redo').disabled=!F.length;
    requestAnimationFrame(fitSeatNamesV12);
  };

  injectOneSeatStyles();
  (S.classes||[]).forEach(c=>norm(c));
  document.getElementById('newC').onclick=newClass;
  document.getElementById('dupC').onclick=duplicateClass;
  save();
  render();
})();
