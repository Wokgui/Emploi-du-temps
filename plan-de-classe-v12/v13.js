(function(){
  'use strict';
  const JOINED='joined', ISOLATED='isolated', MODEL='v13-one-seat';
  let longPressTimer=null, longPressSeat=null, suppressSeatClickUntil=0;

  function tableCount(c=C()){return (c.rows||[]).reduce((sum,row)=>sum+rowTables(row),0)}
  function ensureModes(c=C()){
    if(!Array.isArray(c.rowModes))c.rowModes=[];
    while(c.rowModes.length<c.rows.length)c.rowModes.push([]);
    if(c.rowModes.length>c.rows.length)c.rowModes.length=c.rows.length;
    c.rows.forEach((row,r)=>{
      if(!Array.isArray(c.rowModes[r]))c.rowModes[r]=[];
      while(c.rowModes[r].length<row.length)c.rowModes[r].push(JOINED);
      if(c.rowModes[r].length>row.length)c.rowModes[r].length=row.length;
      c.rowModes[r]=c.rowModes[r].map(x=>x===ISOLATED?ISOLATED:JOINED);
    });
  }
  function ensureLandmarks(c){
    if(!c.landmarks)c.landmarks={tableau:{visible:true,slot:0,align:'center'},bureau:{visible:true,slot:0,align:'right'}};
    ['tableau','bureau'].forEach(k=>{
      if(!c.landmarks[k])c.landmarks[k]={visible:true,slot:0,align:k==='tableau'?'center':'right'};
      c.landmarks[k].slot=Math.max(0,Math.min(c.rows.length,+c.landmarks[k].slot||0));
      c.landmarks[k].align=['left','center','right'].includes(c.landmarks[k].align)?c.landmarks[k].align:(k==='tableau'?'center':'right');
    });
  }
  function normalizeClass(c=C()){
    ensureModes(c);ensureLandmarks(c);
    const n=tableCount(c);
    if(!Array.isArray(c.seats))c.seats=[];
    if(c.modelVersion!==MODEL && c.seatPerTable!==1){
      const old=[...c.seats], next=[];
      if(old.length>=n*2){
        for(let i=0;i<n;i++) next.push(old[i*2]||old[i*2+1]||null);
        const used=new Set(next.filter(Boolean));
        const overflow=[];
        for(let i=0;i<n;i++){
          const a=old[i*2],b=old[i*2+1];
          if(a&&b){const extra=next[i]===a?b:a;if(extra&&!used.has(extra)){used.add(extra);overflow.push(extra)}}
        }
        (c.students||[]).forEach(s=>{if(s?.id&&!used.has(s.id)){used.add(s.id);overflow.push(s.id)}});
        let p=0;for(let i=0;i<next.length&&p<overflow.length;i++)if(!next[i])next[i]=overflow[p++];
        c.seats=next;
      }else{
        const uniq=[];for(const id of old)if(id&&!uniq.includes(id))uniq.push(id);
        c.seats=new Array(n).fill(null);for(let i=0;i<Math.min(n,uniq.length);i++)c.seats[i]=uniq[i];
      }
    }
    while(c.seats.length<n)c.seats.push(null);
    if(c.seats.length>n)c.seats.length=n;
    c.seatPerTable=1;c.modelVersion=MODEL;
    return c;
  }
  cap=function(c=C()){return tableCount(c)};
  norm=function(c=C()){return normalizeClass(c)};
  S.classes.forEach(normalizeClass);

  function fitNames(){
    document.querySelectorAll('#room .seatname').forEach(n=>{
      const seat=n.closest('.seat');if(!seat)return;
      let size=Math.min(13,Math.max(7,seat.clientWidth/7.2));n.style.fontSize=size+'px';
      const reserve=seat.querySelector('.seaticons')?14:4;
      const max=Math.max(18,seat.clientWidth-reserve-8);
      while(n.scrollWidth>max&&size>6){size-=.5;n.style.fontSize=size+'px'}
    });
  }

  function onSeatClick(e){
    if(Date.now()<suppressSeatClickUntil)return;
    const i=+e.currentTarget.dataset.i;
    if(pick===null){pick=i;renderPlan();return}
    if(pick===i){pick=null;renderPlan();return}
    snap();seatSwap(pick,i);
  }
  function onSeatPointerDown(e){
    if(e.pointerType==='mouse'&&e.button!==0)return;
    clearTimeout(longPressTimer);longPressSeat=e.currentTarget;
    const i=+e.currentTarget.dataset.i,sid=C().seats[i];
    if(!sid)return;
    longPressTimer=setTimeout(()=>{
      if(longPressSeat!==e.currentTarget)return;
      suppressSeatClickUntil=Date.now()+650;
      if(navigator.vibrate)navigator.vibrate(20);
      v5OpenStudentEditor?.(sid);
    },560);
  }
  function clearSeatPress(){clearTimeout(longPressTimer);longPressTimer=null;longPressSeat=null}

  renderPlan=function(){
    const c=C();normalizeClass(c);const room=document.getElementById('room');room.innerHTML='';room.classList.remove('lmdrag');
    const stack=document.createElement('div');stack.className='rowsstack';let seatIndex=0;
    for(let r=0;r<c.rows.length;r++){
      stack.appendChild(renderLandslot(c,r));
      const row=document.createElement('div');row.className='row';
      const groups=document.createElement('div');groups.className='tablegroups';
      c.rows[r].forEach((count,ci)=>{
        const g=document.createElement('div');g.className='tgroup '+c.rowModes[r][ci];g.style.gridTemplateColumns=`repeat(${count},minmax(0,1fr))`;g.style.flex=`${count} 1 0`;
        for(let t=0;t<count;t++){
          const idx=seatIndex++,sid=c.seats[idx],st=sid?studentById(sid,c):null;
          const desk=document.createElement('div');desk.className='desk';
          const seat=document.createElement('button');seat.type='button';seat.className='seat'+(!st?' empty':'')+(st?.gender==='boy'?' boy':'')+(st?.gender==='girl'?' girl':'')+(pick===idx?' pick':'');seat.dataset.i=idx;
          if(st){
            const nm=document.createElement('span');nm.className='seatname';nm.textContent=st.name;seat.appendChild(nm);
            if(st.tags?.length){const icons=document.createElement('span');icons.className='seaticons';st.tags.forEach(tag=>icons.appendChild(iconSpan(tag)));seat.appendChild(icons)}
          }else{seat.textContent='Libre'}
          seat.onclick=onSeatClick;seat.onpointerdown=onSeatPointerDown;seat.onpointerup=clearSeatPress;seat.onpointercancel=clearSeatPress;seat.onpointerleave=clearSeatPress;
          desk.appendChild(seat);g.appendChild(desk);
        }
        groups.appendChild(g);
      });
      row.appendChild(groups);stack.appendChild(row);
    }
    stack.appendChild(renderLandslot(c,c.rows.length));room.appendChild(stack);
    document.getElementById('undo').disabled=!H.length;document.getElementById('redo').disabled=!F.length;
    requestAnimationFrame(fitNames);
  };

  function rowTitle(r,total){let s='Rang '+(r+1);if(r===0)s+=' · devant';if(r===total-1)s+=(r===0?' / fond':' · fond');return s}
  function colTitle(i,total){let s='Colonne '+(i+1);if(i===0)s+=' · gauche';if(i===total-1)s+=(i===0?' / droite':' · droite');return s}
  function makeStepButton(text,handler){const b=document.createElement('button');b.type='button';b.textContent=text;b.onclick=handler;return b}
  function mutate(fn,{cfg=true,plan=true}={}){snap();fn();normalizeClass(C());save();if(cfg)renderCfg();if(plan)renderPlan()}

  renderCfg=function(){
    const c=C();normalizeClass(c);document.getElementById('rowN').textContent=c.rows.length;
    const box=document.getElementById('rowCfg');box.innerHTML='';
    let note=document.getElementById('cfgExplainV13');if(!note){note=document.createElement('p');note.id='cfgExplainV13';note.className='small cfghelp';box.parentNode.insertBefore(note,box)}
    note.textContent='Les rangs vont du tableau vers le fond. Dans un même rang, les colonnes sont les groupes de tables répartis de gauche à droite.';
    c.rows.forEach((cols,r)=>{
      const card=document.createElement('section');card.className='rowcfg v13-rowcfg';
      const head=document.createElement('div');head.className='v13-rowhead';
      const title=document.createElement('b');title.textContent=rowTitle(r,c.rows.length);
      const colCount=document.createElement('div');colCount.className='v13-inline-step';
      const lab=document.createElement('span');lab.textContent='Colonnes';const val=document.createElement('span');val.className='v13-count';val.textContent=cols.length;
      colCount.append(lab,makeStepButton('−',()=>{if(cols.length<=1)return;mutate(()=>{c.rows[r].pop();c.rowModes[r].pop()})}),val,makeStepButton('＋',()=>{if(cols.length>=6||rowTables(cols)>=16)return;mutate(()=>{c.rows[r].push(1);c.rowModes[r].push(JOINED)})}));
      head.append(title,colCount);card.appendChild(head);
      const list=document.createElement('div');list.className='v13-cols';
      cols.forEach((tables,ci)=>{
        const col=document.createElement('div');col.className='v13-col';
        const ch=document.createElement('div');ch.className='v13-colhead';const cn=document.createElement('strong');cn.textContent=colTitle(ci,cols.length);const sum=document.createElement('span');sum.textContent=tables+' table'+(tables>1?'s':'');ch.append(cn,sum);
        const modes=document.createElement('div');modes.className='v13-modes';
        const bj=document.createElement('button');bj.type='button';bj.textContent='Tables collées';bj.className=c.rowModes[r][ci]===JOINED?'on':'';bj.onclick=()=>{if(c.rowModes[r][ci]===JOINED)return;mutate(()=>c.rowModes[r][ci]=JOINED)};
        const bi=document.createElement('button');bi.type='button';bi.textContent='Tables isolées';bi.className=c.rowModes[r][ci]===ISOLATED?'on':'';bi.onclick=()=>{if(c.rowModes[r][ci]===ISOLATED)return;mutate(()=>c.rowModes[r][ci]=ISOLATED)};modes.append(bj,bi);
        const count=document.createElement('div');count.className='v13-tablecount';const ctl=document.createElement('span');ctl.textContent='Nombre de tables';const step=document.createElement('div');step.className='step';const sv=document.createElement('span');sv.textContent=tables;
        step.append(makeStepButton('−',()=>{if(tables<=1)return;mutate(()=>c.rows[r][ci]--)}),sv,makeStepButton('＋',()=>{if(tables>=8||rowTables(c.rows[r])>=16)return;mutate(()=>c.rows[r][ci]++)}));count.append(ctl,step);
        col.append(ch,modes,count);list.appendChild(col);
      });
      card.appendChild(list);box.appendChild(card);
    });
    const ob=document.getElementById('objButtons');ob.innerHTML='';
    [['tableau','Tableau'],['bureau','Bureau']].forEach(([id,label])=>{const o=c.landmarks[id],b=document.createElement('button');b.type='button';b.className=o.visible?'ob':'pb';b.textContent=o.visible?label+' présent':'＋ Ajouter '+label;b.onclick=()=>mutate(()=>{o.visible=!o.visible;if(o.visible){o.slot=0;o.align=id==='tableau'?'center':'right'}},{cfg:true,plan:true});ob.appendChild(b)});
    const leg=document.getElementById('statusLegend');leg.innerHTML='';['perturbateur','problematique','bavard','silencieux','handicap'].forEach(tag=>{const pair=TAGS.find(x=>x[0]===tag),item=document.createElement('div');item.className='legenditem';const b=document.createElement('span');b.className='tagb on';b.dataset.tag=tag;b.innerHTML=SVG[tag];const t=document.createElement('span');t.textContent=pair?.[1]||tag;item.append(b,t);leg.appendChild(item)});
  };

  changeRows=function(delta){const c=C(),n=c.rows.length+delta;if(n<1||n>12)return;mutate(()=>{if(delta>0){c.rows.push([2]);c.rowModes.push([JOINED])}else{c.rows.pop();c.rowModes.pop()}})};
  newClass=async function(){const name=(await ask('Nom de la classe','Nouvelle classe'))?.trim();if(!name)return;const rows=INIT_ROWS.map(r=>[...r]),c={id:uid(),name,rows,students:[],seats:new Array(rows.reduce((a,r)=>a+rowTables(r),0)).fill(null),rowModes:rows.map(r=>r.map(()=>JOINED)),seatPerTable:1,modelVersion:MODEL,landmarks:{tableau:{visible:true,slot:0,align:'center'},bureau:{visible:true,slot:0,align:'right'}}};snap();S.classes.push(c);S.cur=c.id;save();render()};
  duplicateClass=function(){const src=C();normalizeClass(src);const c=JSON.parse(JSON.stringify(src));c.id=uid();c.name=src.name+' copie';const remap=new Map();c.students.forEach(s=>{const old=s.id;s.id=uid();remap.set(old,s.id)});c.seats=c.seats.map(id=>remap.get(id)||null);snap();S.classes.push(c);S.cur=c.id;save();render()};
  mix=function(){const c=C();normalizeClass(c);snap();const ids=c.students.map(s=>s.id);for(let i=ids.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[ids[i],ids[j]]=[ids[j],ids[i]]}c.seats=new Array(cap(c)).fill(null).map((_,i)=>ids[i]||null);pick=null;save();renderPlan()};

  document.getElementById('rmRow').onclick=()=>changeRows(-1);document.getElementById('addRow').onclick=()=>changeRows(1);document.getElementById('mix').onclick=mix;
  document.getElementById('undo').onclick=()=>{if(!H.length)return;F.push(JSON.stringify(S));restore(H.pop())};
  document.getElementById('redo').onclick=()=>{if(!F.length)return;H.push(JSON.stringify(S));restore(F.pop())};
  document.getElementById('newC').onclick=newClass;document.getElementById('dupC').onclick=duplicateClass;
  document.querySelectorAll('.nav').forEach(b=>b.onclick=()=>showView(b.dataset.v));

  render();
  window.__planV13={version:'13.0',settingsButtons:()=>document.querySelectorAll('#v-config button').length,seats:()=>document.querySelectorAll('#room .seat').length};
})();
