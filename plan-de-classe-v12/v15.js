(function(){
  'use strict';
  const DEFAULTS={boy:'#eaf4ff',boyb:'#76aee8',girl:'#fff0f6',girlb:'#df8db1'};
  function palette(){
    if(!S.palette||typeof S.palette!=='object')S.palette={};
    for(const [k,v] of Object.entries(DEFAULTS))if(!S.palette[k])S.palette[k]=v;
    return S.palette;
  }
  function applyPalette(){
    const p=palette(),st=document.documentElement.style;
    st.setProperty('--boy',p.boy);st.setProperty('--boyb',p.boyb);st.setProperty('--girl',p.girl);st.setProperty('--girlb',p.girlb);
  }
  function addPaletteCard(){
    const config=document.getElementById('v-config'),obj=document.getElementById('objButtons');
    if(!config||!obj)return;
    const objectCard=obj.closest('.card');
    let card=document.getElementById('paletteCardV15');
    if(!card){
      card=document.createElement('div');card.id='paletteCardV15';card.className='card section v15-palette';
      const h=document.createElement('h2');h.textContent='Couleurs garçon / fille';
      const grid=document.createElement('div');grid.className='v15-palette-grid';
      const reset=document.createElement('button');reset.type='button';reset.className='ob v15-palette-reset';reset.textContent='Couleurs par défaut';
      reset.onclick=()=>{S.palette={...DEFAULTS};applyPalette();save();renderCfg();renderStudents();renderPlan()};
      card.append(h,grid,reset);config.insertBefore(card,objectCard);
    }
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
    const view=document.getElementById('v-students');if(!view)return;
    let card=document.getElementById('studentLegendCardV15');
    if(!card){
      card=document.createElement('div');card.id='studentLegendCardV15';card.className='card section student-legend-card';
      const h=document.createElement('h2');h.textContent='Signification des icônes';
      const box=document.createElement('div');box.id='studentLegendV15';box.className='statuslegend studentlegend';
      card.append(h,box);view.appendChild(card);
    }
    fillLegend(document.getElementById('studentLegendV15'));
  }
  function hideSettingsLegend(){
    const old=document.getElementById('statusLegend');const card=old?.closest('.card.section');if(card)card.style.display='none';
  }
  const oldCfg=renderCfg;
  renderCfg=function(){applyPalette();oldCfg();addPaletteCard();hideSettingsLegend()};
  const oldStudents=renderStudents;
  renderStudents=function(){oldStudents();addStudentLegend()};
  const oldRestore=restore;
  restore=function(s){oldRestore(s);applyPalette()};
  applyPalette();
  renderCfg();renderStudents();renderPlan();save();
  window.__planV15={version:'15.1'};
})();
