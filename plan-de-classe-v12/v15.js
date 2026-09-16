(function(){
  'use strict';
  const DEFAULTS={boy:'#eaf4ff',boyb:'#76aee8',girl:'#fff0f6',girlb:'#df8db1'};
  const ATTACHED_LAYOUT_MARK='attached-layout-2026-09-16-v1';

  function palette(){
    if(!S.palette||typeof S.palette!=='object')S.palette={};
    for(const [k,v] of Object.entries(DEFAULTS))if(!S.palette[k])S.palette[k]=v;
    return S.palette;
  }
  function applyPalette(){
    const p=palette(),st=document.documentElement.style;
    st.setProperty('--boy',p.boy);st.setProperty('--boyb',p.boyb);st.setProperty('--girl',p.girl);st.setProperty('--girlb',p.girlb);
  }

  function importAttachedClassOnce(){
    if(S[ATTACHED_LAYOUT_MARK])return;
    const name='3G34 ALL';
    const visualTopToBottom=[
      [null,null,null,null,null,null],
      ['Victor',null,null,'Manuela',null,'Evan'],
      ['Matteo','Rosie','Anaïs','Roufaida',null,'Redouane'],
      ['Tristan','Elynna','Mila','Nina','Aurélian','Azra'],
      ['Lina','Abigail','Arthur','Lamine','Janna','Zoé'],
      [null,'Ermin','Théo','Timéo','Adam','Victoire'],
      ['Hidaya','Matias','Naim','Léon','Mathilde','Louay']
    ];
    const seatNames=visualTopToBottom.slice().reverse().flat();
    let c=S.classes.find(x=>x.name===name);
    if(!c){
      c={id:uid(),name,rows:[],students:[],seats:[],landmarks:{}};
      S.classes.push(c);
    }
    const oldByName=new Map((c.students||[]).map(st=>[(st.name||'').toLocaleLowerCase('fr'),st]));
    const names=[];
    seatNames.forEach(n=>{if(n&&!names.includes(n))names.push(n)});
    c.students=names.map(n=>{
      const old=oldByName.get(n.toLocaleLowerCase('fr'));
      return old?{...old,name:n}:mkStudent(n);
    });
    const idByName=new Map(c.students.map(st=>[st.name,st.id]));
    c.rows=Array.from({length:7},()=>[2,2,2]);
    c.rowModes=Array.from({length:7},()=>['joined','joined','joined']);
    c.seats=seatNames.map(n=>n?idByName.get(n):null);
    c.seatPerTable=1;
    c.modelVersion='v14-one-seat';
    c.landmarks={tableau:{visible:true,slot:7,align:'center'},bureau:{visible:true,slot:7,align:'right'}};
    S.cur=c.id;
    S[ATTACHED_LAYOUT_MARK]=true;
    save();
  }

  function fillLegend(container){
    if(!container)return;
    container.innerHTML='';
    ['perturbateur','problematique','bavard','silencieux','handicap'].forEach(tag=>{
      const pair=TAGS.find(x=>x[0]===tag),item=document.createElement('div');item.className='legenditem';
      const b=document.createElement('span');b.className='tagb on';b.dataset.tag=tag;b.innerHTML=SVG[tag];
      const t=document.createElement('span');t.textContent=pair?.[1]||tag;item.append(b,t);container.appendChild(item);
    });
  }

  function addStudentLegend(){
    const view=document.getElementById('v-students');if(!view)return null;
    let card=document.getElementById('studentLegendCardV15');
    if(!card){
      card=document.createElement('div');card.id='studentLegendCardV15';card.className='card section student-legend-card';
      const h=document.createElement('h2');h.textContent='Signification des icônes';
      const box=document.createElement('div');box.id='studentLegendV15';box.className='statuslegend studentlegend';
      card.append(h,box);view.appendChild(card);
    }
    fillLegend(document.getElementById('studentLegendV15'));
    return card;
  }

  function addPaletteCard(){
    const view=document.getElementById('v-students');if(!view)return;
    const legend=addStudentLegend();
    let card=document.getElementById('paletteCardV15');
    if(!card){
      card=document.createElement('div');card.id='paletteCardV15';card.className='card section v15-palette';
      const h=document.createElement('h2');h.textContent='Couleurs garçon / fille';
      const grid=document.createElement('div');grid.className='v15-palette-grid';
      const reset=document.createElement('button');reset.type='button';reset.className='ob v15-palette-reset';reset.textContent='Couleurs par défaut';
      reset.onclick=()=>{S.palette={...DEFAULTS};applyPalette();save();renderStudents();renderPlan()};
      card.append(h,grid,reset);
    }
    if(legend&&card.nextSibling!==legend)view.insertBefore(card,legend);
    const grid=card.querySelector('.v15-palette-grid');grid.innerHTML='';
    const p=palette();
    [['boy','Garçon'],['girl','Fille']].forEach(([key,label])=>{
      const item=document.createElement('div');item.className='v15-palette-item';
      const lab=document.createElement('label');lab.textContent=label;
      const input=document.createElement('input');input.type='color';input.value=p[key];input.setAttribute('aria-label','Couleur '+label.toLowerCase());
      input.oninput=()=>{p[key]=input.value;applyPalette();save();renderStudents();renderPlan()};
      item.append(lab,input);grid.appendChild(item);
    });
  }

  function hideSettingsLegend(){
    const old=document.getElementById('statusLegend');const card=old?.closest('.card.section');if(card)card.style.display='none';
  }

  const oldCfg=renderCfg;
  renderCfg=function(){applyPalette();oldCfg();hideSettingsLegend()};
  const oldStudents=renderStudents;
  renderStudents=function(){oldStudents();addStudentLegend();addPaletteCard()};
  const oldRestore=restore;
  restore=function(s){oldRestore(s);applyPalette()};

  importAttachedClassOnce();
  applyPalette();
  renderClasses();renderCfg();renderStudents();renderPlan();save();
  window.__planV15={version:'15.2'};
})();
