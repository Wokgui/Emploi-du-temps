(function(){
  const JOINED='joined', ISOLATED='isolated';
  function ensureModes(c=C()){
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
  const normBase=norm;
  norm=function(c=C()){normBase(c);ensureModes(c)};
  S.classes.forEach(ensureModes);

  function fitSeatNamesV12(){
    document.querySelectorAll('.seatname').forEach(n=>{
      const seat=n.closest('.seat'); if(!seat)return;
      let size=Math.min(10,Math.max(7,seat.clientWidth/7.5)); n.style.fontSize=size+'px';
      const icons=seat.querySelector('.seaticons');
      const available=Math.max(18,seat.clientWidth-5-(icons?icons.clientWidth:0));
      while(n.scrollWidth>available && size>5.5){size-=0.5;n.style.fontSize=size+'px'}
    });
  }

  renderPlan=function(){
    let c=C();norm(c);ensureModes(c);
    let room=document.getElementById('room');room.innerHTML='';room.classList.remove('lmdrag');
    let stack=document.createElement('div');stack.className='rowsstack';let k=0;
    for(let r=0;r<c.rows.length;r++){
      stack.appendChild(renderLandslot(c,r));
      let row=document.createElement('div');row.className='row';
      let rn=document.createElement('div');rn.className='rn';rn.textContent=r+1;
      let groups=document.createElement('div');groups.className='tablegroups';
      for(let gi=0;gi<c.rows[r].length;gi++){
        let gcount=c.rows[r][gi],mode=c.rowModes[r][gi]||JOINED,g=document.createElement('div');
        g.className='tgroup '+mode;g.style.gridTemplateColumns=`repeat(${gcount},minmax(0,1fr))`;g.style.flex=`${gcount} 1 0`;
        for(let t=0;t<gcount;t++){
          let desk=document.createElement('div');desk.className='desk';
          for(let j=0;j<2;j++){
            let idx=k++,sid=c.seats[idx],st=sid?studentById(sid,c):null,e=document.createElement('div');
            e.className='seat'+(!st?' empty':'')+(st?.gender==='boy'?' boy':'')+(st?.gender==='girl'?' girl':'')+(pick===idx?' pick':'');e.dataset.i=idx;
            if(st){let nm=document.createElement('span');nm.className='seatname';nm.textContent=st.name;e.appendChild(nm);if(st.tags?.length){let icons=document.createElement('span');icons.className='seaticons';st.tags.forEach(tag=>icons.appendChild(iconSpan(tag)));e.appendChild(icons)}}else e.textContent='Libre';
            e.title=st?st.name:'Libre';e.onpointerdown=seatDown;e.onclick=seatClick;desk.appendChild(e)
          }
          g.appendChild(desk)
        }
        groups.appendChild(g)
      }
      row.append(rn,groups);stack.appendChild(row)
    }
    stack.appendChild(renderLandslot(c,c.rows.length));room.appendChild(stack);
    document.getElementById('undo').disabled=!H.length;document.getElementById('redo').disabled=!F.length;
    requestAnimationFrame(fitSeatNamesV12)
  };

  function makeControlButton(cls,txt,onclick){const b=document.createElement('button');b.className=cls;b.textContent=txt;b.onclick=onclick;return b}
  function setColCount(r,delta){
    let c=C();ensureModes(c);let row=c.rows[r];let modes=c.rowModes[r];let n=row.length+delta;
    if(n<1||n>6)return;
    if(delta>0 && rowTables(row)>=16){toast('16 tables maximum par rang');return}
    snap();if(delta>0){row.push(1);modes.push(JOINED)}else{row.pop();modes.pop()}norm(c);save();renderCfg();renderPlan()
  }
  function setColTables(r,ci,delta){
    let c=C(),row=c.rows[r],next=row[ci]+delta,total=rowTables(row)+delta;
    if(next<1||next>8||total>16)return;
    snap();row[ci]=next;norm(c);save();renderCfg();renderPlan()
  }
  function setColMode(r,ci,mode){let c=C();ensureModes(c);if(c.rowModes[r][ci]===mode)return;snap();c.rowModes[r][ci]=mode;save();renderCfg();renderPlan()}

  renderCfg=function(){
    let c=C();norm(c);ensureModes(c);document.getElementById('rowN').textContent=c.rows.length;
    let box=document.getElementById('rowCfg');box.innerHTML='';
    c.rows.forEach((cols,r)=>{
      let d=document.createElement('div');d.className='rowcfg';
      let top=document.createElement('div');top.className='rowcfgtop';let label=document.createElement('b');label.textContent='Rang '+(r+1);
      let cc=document.createElement('div');cc.className='v12-colcount';let title=document.createElement('span');title.textContent='Colonnes';
      let step=document.createElement('div');step.className='step';let minus=makeControlButton('','−',()=>setColCount(r,-1));let count=document.createElement('span');count.textContent=cols.length;let plus=makeControlButton('','＋',()=>setColCount(r,1));step.append(minus,count,plus);cc.append(title,step);top.append(label,cc);d.appendChild(top);
      let list=document.createElement('div');list.className='v12-columns';
      cols.forEach((tables,ci)=>{
        let col=document.createElement('div');col.className='v12-column';
        let ct=document.createElement('div');ct.className='v12-coltop';let cn=document.createElement('span');cn.className='v12-colname';cn.textContent='Colonne '+(ci+1);let total=document.createElement('span');total.className='small';total.textContent=tables+' table'+(tables>1?'s':'');ct.append(cn,total);
        let mode=document.createElement('div');mode.className='v12-mode';
        let joined=makeControlButton(c.rowModes[r][ci]===JOINED?'on':'','Tables collées',()=>setColMode(r,ci,JOINED));
        let isolated=makeControlButton(c.rowModes[r][ci]===ISOLATED?'on':'','Tables isolées',()=>setColMode(r,ci,ISOLATED));mode.append(joined,isolated);
        let tr=document.createElement('div');tr.className='v12-tables';let tl=document.createElement('span');tl.textContent='Tables dans cette colonne';let ts=document.createElement('div');ts.className='step';
        let tm=makeControlButton('','−',()=>setColTables(r,ci,-1));let tv=document.createElement('span');tv.textContent=tables+' table'+(tables>1?'s':'');let tp=makeControlButton('','＋',()=>setColTables(r,ci,1));ts.append(tm,tv,tp);tr.append(tl,ts);col.append(ct,mode,tr);list.appendChild(col)
      });
      d.appendChild(list);box.appendChild(d)
    });
    let ob=document.getElementById('objButtons');ob.innerHTML='';[['tableau','Tableau'],['bureau','Bureau']].forEach(([id,label])=>{let o=c.landmarks[id],b=document.createElement('button');b.className=o.visible?'ob':'pb';b.textContent=o.visible?label+' présent':'＋ Ajouter '+label;b.onclick=()=>{snap();o.visible=!o.visible;if(o.visible){o.slot=0;o.align=id==='tableau'?'center':'right'}save();render()};ob.appendChild(b)});
    let leg=document.getElementById('statusLegend');leg.innerHTML='';
    const order=['perturbateur','problematique','bavard','silencieux','handicap'];
    order.forEach(tag=>{let pair=TAGS.find(x=>x[0]===tag),label=pair?pair[1]:tag,item=document.createElement('div');item.className='legenditem';let b=document.createElement('span');b.className='tagb on';b.dataset.tag=tag;b.innerHTML=SVG[tag];let t=document.createElement('span');t.textContent=label;item.append(b,t);leg.appendChild(item)})
  };

  changeRows=function(delta){let c=C();ensureModes(c);let n=c.rows.length+delta;if(n<1||n>12)return;snap();if(delta>0){c.rows.push([2]);c.rowModes.push([JOINED])}else{c.rows.pop();c.rowModes.pop()}norm(c);save();render()};

  renderStudents=function(){
    let c=C();document.getElementById('cname').textContent=c.name;let box=document.getElementById('students');box.innerHTML='';let arr=[...c.students].sort((a,b)=>a.name.localeCompare(b.name,'fr'));
    arr.forEach(st=>{
      let d=document.createElement('div');d.className='stu'+(st.gender==='boy'?' boy':'')+(st.gender==='girl'?' girl':'');
      let h=document.createElement('div');h.className='stuhead';let inp=document.createElement('input');inp.value=st.name;inp.placeholder='Nom';inp.onchange=()=>{let v=inp.value.trim();if(!v){inp.value=st.name;return}snap();st.name=v;save();renderPlan()};let x=document.createElement('button');x.className='x';x.textContent='⌫';x.onclick=()=>delStudent(st.id);h.append(inp,x);
      let ctrl=document.createElement('div');ctrl.className='stuctrl';let genders=document.createElement('div');genders.className='genderdots';
      [['boy','Garçon'],['girl','Fille']].forEach(([g,title])=>{let b=document.createElement('button');b.className='dot '+g+(st.gender===g?' on':'');b.textContent=title;b.title=title;b.onclick=()=>{snap();st.gender=st.gender===g?'':g;save();renderStudents();renderPlan()};genders.appendChild(b)});
      let tags=document.createElement('div');tags.className='tagset';TAGS.forEach(([tag,label])=>{let b=document.createElement('button');b.className='tagb'+(st.tags?.includes(tag)?' on':'');b.dataset.tag=tag;b.title=label;b.setAttribute('aria-label',label);b.innerHTML=SVG[tag];b.onclick=()=>{snap();st.tags=st.tags||[];let i=st.tags.indexOf(tag);if(i>=0)st.tags.splice(i,1);else st.tags.push(tag);save();renderStudents();renderPlan()};tags.appendChild(b)});ctrl.append(genders,tags);d.append(h,ctrl);box.appendChild(d)
    });
    let u=unseated(c).length;if(u){let p=document.createElement('div');p.className='small';p.textContent=u+' élève'+(u>1?'s':'')+' sans place dans le plan.';box.prepend(p)}
  };

  const refreshEditorBase=v5RefreshEditor;
  v5RefreshEditor=function(){refreshEditorBase();document.querySelectorAll('#editTags .tagb').forEach(b=>b.title=b.getAttribute('aria-label')||b.title)};

  const planIco=document.querySelector('.nav[data-v="plan"] .navico');
  if(planIco){planIco.classList.add('plan-nav-svg');planIco.innerHTML='<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="5" rx="1.5"/><rect x="3" y="12" width="5" height="4" rx="1"/><rect x="10" y="12" width="5" height="4" rx="1"/><rect x="17" y="12" width="4" height="4" rx="1"/><path d="M5.5 16v3M12.5 16v3M19 16v3"/></svg>'}

  norm();renderClasses();renderPlan();renderStudents();renderCfg();save();
})();
