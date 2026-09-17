(function(){
  'use strict';

  const DEFAULT_THEME='#0b6b57';

  function cleanHex(hex){
    let s=String(hex||'').trim();
    if(!/^#[0-9a-f]{6}$/i.test(s))s=DEFAULT_THEME;
    return s.toLowerCase();
  }
  function rgb(hex){
    const h=cleanHex(hex).slice(1);
    return [parseInt(h.slice(0,2),16),parseInt(h.slice(2,4),16),parseInt(h.slice(4,6),16)];
  }
  function toHex(a){return '#'+a.map(v=>Math.max(0,Math.min(255,Math.round(v))).toString(16).padStart(2,'0')).join('')}
  function mix(hex,target,amount){
    const a=rgb(hex),b=rgb(target);
    return toHex(a.map((v,i)=>v+(b[i]-v)*amount));
  }
  function themeColor(){
    if(!S.themeColor)S.themeColor=DEFAULT_THEME;
    S.themeColor=cleanHex(S.themeColor);
    return S.themeColor;
  }
  function applyThemeColor(){
    const base=themeColor();
    const dark=mix(base,'#000000',.30);
    const light=mix(base,'#ffffff',.14);
    const leaf=mix(base,'#ffffff',.68);
    const glow=mix(base,'#ffffff',.34);
    const soft=mix(base,'#ffffff',.86);
    const st=document.documentElement.style;
    st.setProperty('--theme-base',base);
    st.setProperty('--theme-dark',dark);
    st.setProperty('--theme-light',light);
    st.setProperty('--theme-leaf',leaf);
    st.setProperty('--theme-glow',glow);
    st.setProperty('--theme-soft',soft);
    st.setProperty('--topSolid',base);
    st.setProperty('--mint',soft);
    try{window.AndroidBridge?.setThemeColor?.(dark)}catch(e){}
  }

  function addThemeControl(){
    const quick=document.getElementById('quickLayoutV14');
    if(!quick)return;
    let row=document.getElementById('themeColorV156');
    if(row)return;
    row=document.createElement('div');
    row.id='themeColorV156';
    row.className='v156-theme-row';

    const label=document.createElement('div');
    label.className='v156-theme-label';
    label.textContent='Couleur du bandeau';

    const input=document.createElement('input');
    input.type='color';
    input.className='v156-theme-color';
    input.value=themeColor();
    input.setAttribute('aria-label','Couleur du bandeau');
    input.oninput=()=>{
      S.themeColor=cleanHex(input.value);
      applyThemeColor();
      save();
    };

    const reset=document.createElement('button');
    reset.type='button';
    reset.className='ob v156-theme-reset';
    reset.textContent='Forêt';
    reset.onclick=()=>{
      S.themeColor=DEFAULT_THEME;
      input.value=DEFAULT_THEME;
      applyThemeColor();
      save();
    };

    const note=document.createElement('div');
    note.className='v156-theme-note';
    note.textContent='Le dégradé et les motifs restent identiques ; seule la couleur change.';

    row.append(label,input,reset,note);
    quick.appendChild(row);
  }

  const baseRenderCfg=renderCfg;
  renderCfg=function(){
    applyThemeColor();
    baseRenderCfg();
    addThemeControl();
  };

  const baseRestore=restore;
  restore=function(s){
    baseRestore(s);
    applyThemeColor();
  };

  applyThemeColor();
  renderCfg();
  save();
  window.__planTheme156={apply:applyThemeColor,defaultColor:DEFAULT_THEME};
})();
