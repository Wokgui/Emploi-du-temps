(function(){
  const JOINED='joined', ISOLATED='isolated';

  function ensureModesE(c=C()){
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

  function rerenderSettings(){
    norm(C());
    ensureModesE(C());
    save();
    renderCfg();
    renderPlan();
  }

  function rowLabel(r,total){
    let s='Rang '+(r+1);
    if(r===0)s+=' · devant';
    if(r===total-1)s+=(r===0?' / fond':' · fond');
    return s;
  }
  function colLabel(i,total){
    let s='Colonne '+(i+1);
    if(i===0)s+=' · gauche';
    if(i===total-1)s+=(i===0?' / droite':' · droite');
    return s;
  }
  function btn(text,action,r,ci){
    const b=document.createElement('button');
    b.type='button'; b.textContent=text; b.dataset.a=action;
    if(r!==undefined)b.dataset.r=r;
    if(ci!==undefined)b.dataset.c=ci;
    return b;
  }

  renderCfg=function(){
    const c=C(); norm(c); ensureModesE(c);
    const rn=document.getElementById('rowN'); if(rn)rn.textContent=c.rows.length;
    const box=document.getElementById('rowCfg'); if(!box)return;
    box.innerHTML='';

    let note=document.getElementById('cfgExplainV12b');
    if(!note){
      note=document.createElement('p'); note.id='cfgExplainV12b'; note.className='small v12b-explain';
      box.parentNode.insertBefore(note,box);
    }
    note.textContent='Rangées : du tableau vers le fond de la salle. Colonnes : groupes de tables de gauche à droite, tous à la même distance du tableau dans un même rang.';

    c.rows.forEach((cols,r)=>{
      const d=document.createElement('div'); d.className='rowcfg';
      const top=document.createElement('div'); top.className='rowcfgtop';
      const label=document.createElement('b'); label.textContent=rowLabel(r,c.rows.length);
      const cc=document.createElement('div'); cc.className='v12-colcount';
      const title=document.createElement('span'); title.textContent='Colonnes';
      const step=document.createElement('div'); step.className='step';
      const minus=btn('−','col-',r), count=document.createElement('span'), plus=btn('＋','col+',r);
      count.textContent=cols.length; step.append(minus,count,plus); cc.append(title,step); top.append(label,cc); d.appendChild(top);

      const list=document.createElement('div'); list.className='v12-columns';
      cols.forEach((tables,ci)=>{
        const col=document.createElement('div'); col.className='v12-column';
        const ct=document.createElement('div'); ct.className='v12-coltop';
        const cn=document.createElement('span'); cn.className='v12-colname'; cn.textContent=colLabel(ci,cols.length);
        const total=document.createElement('span'); total.className='small'; total.textContent=tables+' table'+(tables>1?'s':''); ct.append(cn,total);

        const mode=document.createElement('div'); mode.className='v12-mode';
        const joined=btn('Tables collées','joined',r,ci), isolated=btn('Tables isolées','isolated',r,ci);
        joined.className=c.rowModes[r][ci]===JOINED?'on':''; isolated.className=c.rowModes[r][ci]===ISOLATED?'on':''; mode.append(joined,isolated);

        const tr=document.createElement('div'); tr.className='v12-tables';
        const tl=document.createElement('span'); tl.textContent='Tables dans cette colonne';
        const ts=document.createElement('div'); ts.className='step';
        const tm=btn('−','table-',r,ci), tv=document.createElement('span'), tp=btn('＋','table+',r,ci);
        tv.textContent=tables+' table'+(tables>1?'s':''); ts.append(tm,tv,tp); tr.append(tl,ts);
        col.append(ct,mode,tr); list.appendChild(col);
      });
      d.appendChild(list); box.appendChild(d);
    });

    const ob=document.getElementById('objButtons');
    if(ob){
      ob.innerHTML='';
      [['tableau','Tableau'],['bureau','Bureau']].forEach(([id,label])=>{
        const o=c.landmarks[id], b=document.createElement('button'); b.type='button'; b.dataset.obj=id;
        b.className=o.visible?'ob':'pb'; b.textContent=o.visible?label+' présent':'＋ Ajouter '+label; ob.appendChild(b);
      });
    }

    const leg=document.getElementById('statusLegend');
    if(leg){
      leg.innerHTML='';
      ['perturbateur','problematique','bavard','silencieux','handicap'].forEach(tag=>{
        const pair=TAGS.find(x=>x[0]===tag), item=document.createElement('div'); item.className='legenditem';
        const b=document.createElement('span'); b.className='tagb on'; b.dataset.tag=tag; b.innerHTML=SVG[tag];
        const t=document.createElement('span'); t.textContent=pair?pair[1]:tag; item.append(b,t); leg.appendChild(item);
      });
    }
  };

  const box=document.getElementById('rowCfg');
  if(box){
    box.onclick=function(e){
      const b=e.target.closest('button[data-a]'); if(!b || !box.contains(b))return;
      e.preventDefault(); e.stopPropagation();
      const c=C(), r=+b.dataset.r, ci=b.dataset.c===undefined?null:+b.dataset.c, a=b.dataset.a;
      ensureModesE(c);
      if(!c.rows[r])return;
      if(a==='col-'||a==='col+'){
        const delta=a==='col+'?1:-1, next=c.rows[r].length+delta;
        if(next<1||next>6)return;
        if(delta>0 && rowTables(c.rows[r])>=16){toast('16 tables maximum par rang');return;}
        snap();
        if(delta>0){c.rows[r].push(1);c.rowModes[r].push(JOINED)}else{c.rows[r].pop();c.rowModes[r].pop()}
      }else if(a==='table-'||a==='table+'){
        if(ci===null||c.rows[r][ci]===undefined)return;
        const delta=a==='table+'?1:-1, next=c.rows[r][ci]+delta, total=rowTables(c.rows[r])+delta;
        if(next<1||next>8||total>16)return;
        snap(); c.rows[r][ci]=next;
      }else if(a==='joined'||a==='isolated'){
        if(ci===null)return;
        const mode=a==='isolated'?ISOLATED:JOINED; if(c.rowModes[r][ci]===mode)return;
        snap(); c.rowModes[r][ci]=mode;
      }
      rerenderSettings();
    };
  }

  const ob=document.getElementById('objButtons');
  if(ob){
    ob.onclick=function(e){
      const b=e.target.closest('button[data-obj]'); if(!b||!ob.contains(b))return;
      e.preventDefault(); e.stopPropagation();
      const c=C(), id=b.dataset.obj, o=c.landmarks[id]; if(!o)return;
      snap(); o.visible=!o.visible; if(o.visible){o.slot=0;o.align=id==='tableau'?'center':'right'};
      save(); renderCfg(); renderPlan();
    };
  }

  changeRows=function(delta){
    const c=C(); ensureModesE(c); const n=c.rows.length+delta; if(n<1||n>12)return;
    snap(); if(delta>0){c.rows.push([2]);c.rowModes.push([JOINED])}else{c.rows.pop();c.rowModes.pop()}; rerenderSettings();
  };
  const rm=document.getElementById('rmRow'), add=document.getElementById('addRow');
  if(rm)rm.onclick=()=>changeRows(-1);
  if(add)add.onclick=()=>changeRows(1);

  const style=document.createElement('style');
  style.textContent='#v-config button{touch-action:manipulation;pointer-events:auto!important}#rowCfg button,#objButtons button{position:relative;z-index:1}';
  document.head.appendChild(style);

  renderCfg();
})();
