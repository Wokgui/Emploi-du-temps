(function(){
  const JOINED='joined', ISOLATED='isolated';
  let saveTimer=0, planFrame1=0, planFrame2=0;

  function ensureModesF(c=C()){
    if(!Array.isArray(c.rowModes)) c.rowModes=[];
    while(c.rowModes.length<c.rows.length)c.rowModes.push([]);
    if(c.rowModes.length>c.rows.length)c.rowModes.length=c.rows.length;
    c.rows.forEach((row,r)=>{
      if(!Array.isArray(c.rowModes[r]))c.rowModes[r]=[];
      while(c.rowModes[r].length<row.length)c.rowModes[r].push(JOINED);
      if(c.rowModes[r].length>row.length)c.rowModes[r].length=row.length;
      c.rowModes[r]=c.rowModes[r].map(x=>x===ISOLATED?ISOLATED:JOINED);
    });
  }

  function saveSoon(){
    clearTimeout(saveTimer);
    saveTimer=setTimeout(()=>save(),45);
  }
  function planSoon(){
    if(planFrame1)cancelAnimationFrame(planFrame1);
    if(planFrame2)cancelAnimationFrame(planFrame2);
    planFrame1=requestAnimationFrame(()=>{
      planFrame2=requestAnimationFrame(()=>{
        planFrame1=planFrame2=0;
        renderPlan();
      });
    });
  }
  function settingsStructuralRefresh(){
    norm(C()); ensureModesF(C());
    renderCfg();
    saveSoon();
    planSoon();
  }

  function fastDelegated(root, selector, handler){
    if(!root || root.dataset.fastDelegated==='1')return;
    root.dataset.fastDelegated='1';
    let active=null,sx=0,sy=0,moved=false,suppressUntil=0;
    root.addEventListener('pointerdown',e=>{
      const b=e.target.closest(selector); if(!b||!root.contains(b)||b.disabled)return;
      active=b;sx=e.clientX;sy=e.clientY;moved=false;b.classList.add('fast-pressed');
    },{passive:true});
    root.addEventListener('pointermove',e=>{
      if(!active)return;
      if(Math.hypot(e.clientX-sx,e.clientY-sy)>11){moved=true;active.classList.remove('fast-pressed');}
    },{passive:true});
    const clear=()=>{if(active)active.classList.remove('fast-pressed');active=null;};
    root.addEventListener('pointercancel',clear,{passive:true});
    root.addEventListener('pointerup',e=>{
      const b=e.target.closest(selector);
      const ok=active && !moved && b===active && !active.disabled;
      if(active)active.classList.remove('fast-pressed');
      const target=active; active=null;
      if(!ok)return;
      e.preventDefault();e.stopPropagation();suppressUntil=Date.now()+500;
      handler(e,target);
    });
    root.addEventListener('click',e=>{
      if(Date.now()<suppressUntil && e.target.closest(selector)){e.preventDefault();e.stopPropagation();}
    },true);
  }

  function fastDirect(el,handler){
    if(!el||el.dataset.fastDirect==='1')return;
    el.dataset.fastDirect='1';
    let sx=0,sy=0,moved=false,suppressUntil=0;
    el.onclick=null;
    el.addEventListener('pointerdown',e=>{if(el.disabled)return;sx=e.clientX;sy=e.clientY;moved=false;el.classList.add('fast-pressed');},{passive:true});
    el.addEventListener('pointermove',e=>{if(Math.hypot(e.clientX-sx,e.clientY-sy)>11){moved=true;el.classList.remove('fast-pressed');}},{passive:true});
    el.addEventListener('pointercancel',()=>el.classList.remove('fast-pressed'),{passive:true});
    el.addEventListener('pointerup',e=>{
      el.classList.remove('fast-pressed');if(moved||el.disabled)return;
      e.preventDefault();e.stopPropagation();suppressUntil=Date.now()+500;handler(e);
    });
    el.addEventListener('click',e=>{if(Date.now()<suppressUntil){e.preventDefault();e.stopPropagation();}},true);
  }

  const rowBox=document.getElementById('rowCfg');
  if(rowBox){
    rowBox.onclick=null;
    fastDelegated(rowBox,'button[data-a]',(e,b)=>{
      const c=C(), r=+b.dataset.r, ci=b.dataset.c===undefined?null:+b.dataset.c, a=b.dataset.a;
      ensureModesF(c); if(!c.rows[r])return;
      if(a==='col-'||a==='col+'){
        const delta=a==='col+'?1:-1, next=c.rows[r].length+delta;
        if(next<1||next>6)return;
        if(delta>0&&rowTables(c.rows[r])>=16){toast('16 tables maximum par rang');return;}
        snap();
        if(delta>0){c.rows[r].push(1);c.rowModes[r].push(JOINED)}else{c.rows[r].pop();c.rowModes[r].pop()}
        settingsStructuralRefresh();
        return;
      }
      if(a==='table-'||a==='table+'){
        if(ci===null||c.rows[r][ci]===undefined)return;
        const delta=a==='table+'?1:-1, next=c.rows[r][ci]+delta, total=rowTables(c.rows[r])+delta;
        if(next<1||next>8||total>16)return;
        snap(); c.rows[r][ci]=next; norm(c);
        const col=b.closest('.v12-column');
        if(col){
          const topTotal=col.querySelector('.v12-coltop .small');
          const stepVal=col.querySelector('.v12-tables .step span');
          const txt=next+' table'+(next>1?'s':'');
          if(topTotal)topTotal.textContent=txt;if(stepVal)stepVal.textContent=txt;
        }
        saveSoon();planSoon();
        return;
      }
      if(a==='joined'||a==='isolated'){
        if(ci===null)return;
        const mode=a==='isolated'?ISOLATED:JOINED;if(c.rowModes[r][ci]===mode)return;
        snap();c.rowModes[r][ci]=mode;
        const wrap=b.closest('.v12-mode');if(wrap){wrap.querySelectorAll('button').forEach(x=>x.classList.remove('on'));b.classList.add('on');}
        saveSoon();planSoon();
      }
    });
  }

  const obj=document.getElementById('objButtons');
  if(obj){
    obj.onclick=null;
    fastDelegated(obj,'button[data-obj]',(e,b)=>{
      const c=C(), id=b.dataset.obj, o=c.landmarks[id];if(!o)return;
      snap();o.visible=!o.visible;if(o.visible){o.slot=0;o.align=id==='tableau'?'center':'right'};
      b.className=o.visible?'ob':'pb';b.textContent=o.visible?(id==='tableau'?'Tableau présent':'Bureau présent'):'＋ Ajouter '+(id==='tableau'?'Tableau':'Bureau');
      saveSoon();planSoon();
    });
  }

  const rm=document.getElementById('rmRow'), add=document.getElementById('addRow');
  fastDirect(rm,()=>{
    const c=C();ensureModesF(c);if(c.rows.length<=1)return;snap();c.rows.pop();c.rowModes.pop();settingsStructuralRefresh();
  });
  fastDirect(add,()=>{
    const c=C();ensureModesF(c);if(c.rows.length>=12)return;snap();c.rows.push([2]);c.rowModes.push([JOINED]);settingsStructuralRefresh();
  });

  const mixBtn=document.getElementById('mix');
  fastDirect(mixBtn,()=>{
    const c=C();snap();const ids=c.students.map(s=>s.id);
    for(let i=ids.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[ids[i],ids[j]]=[ids[j],ids[i]];}
    c.seats=c.seats.map((_,i)=>ids[i]||null);saveSoon();planSoon();
  });

  fastDirect(document.getElementById('undo'),()=>{
    if(!H.length)return;F.push(JSON.stringify(S));S=JSON.parse(H.pop());saveSoon();requestAnimationFrame(()=>render());
  });
  fastDirect(document.getElementById('redo'),()=>{
    if(!F.length)return;H.push(JSON.stringify(S));S=JSON.parse(F.pop());saveSoon();requestAnimationFrame(()=>render());
  });

  document.querySelectorAll('.premium-foot .nav').forEach(nav=>{
    fastDirect(nav,()=>showView(nav.dataset.v));
  });

  const style=document.createElement('style');
  style.id='v12f-fast-style';
  style.textContent=`
    #v-plan button,#v-config button,.premium-foot .nav{touch-action:manipulation;-webkit-tap-highlight-color:transparent;transition:transform .045s ease,filter .045s ease,background-color .08s ease}
    #v-plan button.fast-pressed,#v-config button.fast-pressed,.premium-foot .nav.fast-pressed{transform:scale(.965);filter:brightness(.96)}
    #rowCfg button,#objButtons button,#addRow,#rmRow,#mix,#undo,#redo{cursor:pointer;user-select:none}
  `;
  document.head.appendChild(style);
})();
