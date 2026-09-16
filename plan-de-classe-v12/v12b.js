(function(){
  const baseRenderCfg=renderCfg;
  renderCfg=function(){
    baseRenderCfg();
    const box=document.getElementById('rowCfg');
    if(!box)return;
    let note=document.getElementById('cfgExplainV12b');
    if(!note){
      note=document.createElement('p');
      note.id='cfgExplainV12b';
      note.className='small v12b-explain';
      box.parentNode.insertBefore(note,box);
    }
    note.textContent='Rangées : du tableau vers le fond de la salle. Colonnes : groupes de tables de gauche à droite, tous à la même distance du tableau dans un même rang.';
    const rows=[...box.querySelectorAll('.rowcfg')];
    rows.forEach((row,r)=>{
      const label=row.querySelector('.rowcfgtop b');
      if(label){
        let txt='Rang '+(r+1);
        if(r===0)txt+=' · devant';
        if(r===rows.length-1)txt+=(r===0?' / fond':' · fond');
        label.textContent=txt;
      }
      const cols=[...row.querySelectorAll('.v12-column')];
      cols.forEach((col,i)=>{
        const name=col.querySelector('.v12-colname');
        if(name){
          let txt='Colonne '+(i+1);
          if(i===0)txt+=' · gauche';
          if(i===cols.length-1)txt+=(i===0?' / droite':' · droite');
          name.textContent=txt;
        }
      });
    });
  };
  renderCfg();
})();
