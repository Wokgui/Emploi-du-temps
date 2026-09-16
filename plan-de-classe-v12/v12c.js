(function(){
  const JOINED='joined', ISOLATED='isolated';

  function ensureModesC(c=C()){
    if(!Array.isArray(c.rowModes)) c.rowModes=[];
    while(c.rowModes.length<c.rows.length)c.rowModes.push([]);
    if(c.rowModes.length>c.rows.length)c.rowModes.length=c.rows.length;
    c.rows.forEach((row,r)=>{
      if(!Array.isArray(c.rowModes[r]))c.rowModes[r]=[];
      while(c.rowModes[r].length<row.length)c.rowModes[r].push(JOINED);
      if(c.rowModes[r].length>row.length)c.rowModes[r].length=row.length;
      c.rowModes[r]=c.rowModes[r].map(x=>x===ISOLATED?ISOLATED:JOINED);
    });
    return c;
  }

  function singleCap(c=C()){
    return c.rows.reduce((sum,row)=>sum+rowTables(row),0);
  }

  cap=function(c=C()){ return singleCap(c); };

  function normalizeOneSeat(c=C()){
    ensureModesC(c);
    const n=singleCap(c);
    if(!Array.isArray(c.seats)) c.seats=[];

    if(c.seatPerTable!==1){
      const ordered=[], seen=new Set();
      c.seats.forEach(id=>{
        if(id && !seen.has(id)){
          seen.add(id);
          ordered.push(id);
        }
      });
      c.seats=new Array(n).fill(null);
      for(let i=0;i<n;i++) c.seats[i]=ordered[i]||null;
      c.seatPerTable=1;
    } else if(c.seats.length<n){
      while(c.seats.length<n)c.seats.push(null);
    } else if(c.seats.length>n){
      c.seats.length=n;
    }

    if(!c.landmarks)c.landmarks={tableau:{visible:true,slot:0,align:'center'},bureau:{visible:true,slot:0,align:'right'}};
    ['tableau','bureau'].forEach(k=>{
      if(!c.landmarks[k])c.landmarks[k]={visible:true,slot:0,align:k==='tableau'?'center':'right'};
      c.landmarks[k].slot=Math.max(0,Math.min(c.rows.length,c.landmarks[k].slot||0));
    });
    return c;
  }

  norm=function(c=C()){ return normalizeOneSeat(c); };
  S.classes.forEach(normalizeOneSeat);

  renderPlan=function(){
    const c=C();norm(c);ensureModesC(c);
    const room=document.getElementById('room');room.innerHTML='';room.classList.remove('lmdrag');
    const stack=document.createElement('div');stack.className='rowsstack';let k=0;

    for(let r=0;r<c.rows.length;r++){
      stack.appendChild(renderLandslot(c,r));
      const row=document.createElement('div');row.className='row';
      const rn=document.createElement('div');rn.className='rn';rn.textContent=r+1;
      const groups=document.createElement('div');groups.className='tablegroups';

      for(let gi=0;gi<c.rows[r].length;gi++){
        const gcount=c.rows[r][gi], mode=c.rowModes[r][gi]||JOINED;
        const g=document.createElement('div');
        g.className='tgroup '+mode;
        g.style.gridTemplateColumns=`repeat(${gcount},minmax(0,1fr))`;
        g.style.flex=`${gcount} 1 0`;

        for(let t=0;t<gcount;t++){
          const desk=document.createElement('div');desk.className='desk';
          const idx=k++,sid=c.seats[idx],st=sid?studentById(sid,c):null;
          const e=document.createElement('div');
          e.className='seat'+(!st?' empty':'')+(st?.gender==='boy'?' boy':'')+(st?.gender==='girl'?' girl':'')+(pick===idx?' pick':'');
          e.dataset.i=idx;
          if(st){
            const nm=document.createElement('span');nm.className='seatname';nm.textContent=st.name;e.appendChild(nm);
            if(st.tags?.length){
              const icons=document.createElement('span');icons.className='seaticons';
              st.tags.forEach(tag=>icons.appendChild(iconSpan(tag)));
              e.appendChild(icons);
            }
          } else e.textContent='Libre';
          e.title=st?st.name:'Libre';
          e.onpointerdown=seatDown;
          e.onclick=seatClick;
          desk.appendChild(e);
          g.appendChild(desk);
        }
        groups.appendChild(g);
      }
      row.append(rn,groups);
      stack.appendChild(row);
    }

    stack.appendChild(renderLandslot(c,c.rows.length));
    room.appendChild(stack);
    document.getElementById('undo').disabled=!H.length;
    document.getElementById('redo').disabled=!F.length;
  };

  function freshClassC(name){
    const rows=INIT_ROWS.map(x=>[...x]);
    return {
      id:uid(),name,
      rows,
      students:[],
      seats:new Array(rows.reduce((sum,row)=>sum+rowTables(row),0)).fill(null),
      rowModes:rows.map(row=>row.map(()=>JOINED)),
      seatPerTable:1,
      landmarks:{tableau:{visible:true,slot:0,align:'center'},bureau:{visible:true,slot:0,align:'right'}}
    };
  }

  newClass=async function(){
    const n=(await ask('Nom de la classe','Nouvelle classe'))?.trim();
    if(!n)return;
    const c=freshClassC(n);
    snap();S.classes.push(c);S.cur=c.id;save();render();
  };

  duplicateClass=function(){
    const src=C();normalizeOneSeat(src);
    const c=JSON.parse(JSON.stringify(src));
    c.id=uid();c.name=src.name+' copie';
    c.students.forEach(s=>{
      const old=s.id;s.id=uid();
      c.seats=c.seats.map(x=>x===old?s.id:x);
    });
    normalizeOneSeat(c);
    snap();S.classes.push(c);S.cur=c.id;save();render();
  };

  document.getElementById('newC').onclick=newClass;
  document.getElementById('dupC').onclick=duplicateClass;

  norm();renderClasses();renderPlan();renderStudents();renderCfg();save();
})();
