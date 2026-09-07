package com.wokgui.schedulewidget;

final class BulkCourseUi {
    private BulkCourseUi() {}

    static String script() {
        return """
            (function(){
              try{
                function dayLabel(d){return ({2:'Lun',3:'Mar',4:'Mer',5:'Jeu',6:'Ven'})[Number(d)]||'Jour'}
                function activeLetter(){
                  try{return (typeof activeWeek!=='undefined'&&activeWeek)?activeWeek:((typeof currentWeek!=='undefined'&&currentWeek)?currentWeek:'A')}
                  catch(e){return 'A'}
                }
                function selectedDay(){
                  try{return (typeof selected!=='undefined'&&[2,3,4,5,6].indexOf(Number(selected))>=0)?Number(selected):2}
                  catch(e){return 2}
                }

                function ensureButton(){
                  var base=document.getElementById('addCourse');
                  if(!base)return;
                  var btn=document.getElementById('addBulkCourses');
                  if(!btn){
                    btn=document.createElement('button');
                    btn.id='addBulkCourses';
                    btn.type='button';
                    btn.className='addBtn';
                    btn.textContent='＋ Ajouter plusieurs cours à une classe';
                    btn.style.marginTop='6px';
                    btn.style.borderStyle='solid';
                    btn.style.background='#eef6ff';
                    base.parentNode.insertBefore(btn,base.nextSibling);
                  }
                  btn.style.setProperty('display','block','important');
                  btn.style.setProperty('visibility','visible','important');
                  btn.style.setProperty('opacity','1','important');
                  btn.onclick=function(){
                    if(typeof window.openBulkCourses==='function')window.openBulkCourses();
                    else alert('Le formulaire d’ajout groupé n’est pas encore prêt. Ferme puis rouvre l’application.');
                  };
                }

                /* Le bouton est posé avant tout le reste : même si une autre interface plante, il reste visible. */
                ensureButton();
                window.refreshBulkCourseUi=ensureButton;

                function ensureStyle(){
                  if(document.getElementById('bulkCourseStyle'))return;
                  var style=document.createElement('style');style.id='bulkCourseStyle';
                  style.textContent=''
                    +'#addBulkCourses{margin-top:6px!important;background:#eef6ff!important;border-style:solid!important}'
                    +'#bulkCourseModal{position:fixed;inset:0;z-index:140;background:#0b17386b;display:none;align-items:flex-end}'
                    +'#bulkCourseModal.show{display:flex}'
                    +'#bulkCourseSheet{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:18px 18px 0 0;padding:14px 12px calc(14px + env(safe-area-inset-bottom));max-height:90vh;overflow:auto;color:var(--ink,#111936)}'
                    +'#bulkCourseSheet h3{margin:0 0 3px;text-align:center;font-size:1rem}'
                    +'.bulkWeek{text-align:center;color:var(--muted,#68738a);font-size:.72rem;margin-bottom:10px}'
                    +'.bulkField{margin-bottom:8px}.bulkField label{display:block;color:var(--muted,#68738a);font-size:.72rem;margin-bottom:3px}.bulkField input{width:100%;border:1px solid var(--line,#dce3eb);border-radius:7px;padding:9px;background:#fff;color:inherit}'
                    +'.bulkRows{border:1px solid var(--line,#dce3eb);border-radius:9px;overflow:hidden;background:#fff}'
                    +'.bulkRow{display:grid;grid-template-columns:72px minmax(0,1fr) 78px 30px;gap:5px;align-items:center;padding:7px;border-top:1px solid #e9edf2}.bulkRow:first-child{border-top:0}'
                    +'.bulkRow select,.bulkRow input{width:100%;min-width:0;border:1px solid var(--line,#dce3eb);border-radius:7px;padding:8px 4px;background:#fff;color:inherit;font-size:.72rem}'
                    +'.bulkRemove{width:30px;height:30px;border:0;border-radius:50%;background:#fff1f3;color:#c6284e;font-size:18px}'
                    +'.bulkAddLine{width:100%;margin-top:7px;padding:8px;border:1px dashed var(--blue,#0877f9);border-radius:7px;background:#fff;color:var(--blue,#0877f9);font-weight:800}'
                    +'.bulkHint{font-size:.67rem;color:var(--muted,#68738a);line-height:1.25;margin:6px 2px 0}'
                    +'.bulkActions{display:flex;justify-content:flex-end;gap:7px;margin-top:11px}';
                  document.head.appendChild(style);
                }

                function slotOptions(selectedSlot){
                  var html='';
                  try{
                    for(var i=0;i<slots.length;i++){
                      var s=slots[i],n=Number(s.n||i+1),sel=n===Number(selectedSlot)?' selected':'';
                      html+='<option value="'+n+'"'+sel+'>'+n+'e · '+s.start+'–'+s.end+'</option>';
                    }
                  }catch(e){}
                  return html;
                }
                function dayOptions(selectedValue){
                  var ds=[2,3,4,5,6],html='';
                  for(var i=0;i<ds.length;i++)html+='<option value="'+ds[i]+'"'+(ds[i]===Number(selectedValue)?' selected':'')+'>'+dayLabel(ds[i])+'</option>';
                  return html;
                }
                function firstFree(day){
                  try{
                    var letter=activeLetter(),list=weeks[letter][day].courses||[],used={};
                    for(var i=0;i<list.length;i++)if(Number(list[i].slot)>0)used[Number(list[i].slot)]=true;
                    for(var j=0;j<slots.length;j++){var n=Number(slots[j].n||j+1);if(!used[n])return n}
                  }catch(e){}
                  return 1;
                }
                function nextDay(day){var ds=[2,3,4,5,6],i=ds.indexOf(Number(day));return ds[(i+1+ds.length)%ds.length]}

                function addRow(day,slot,room){
                  var rows=document.getElementById('bulkRows');if(!rows)return;
                  var row=document.createElement('div');row.className='bulkRow';
                  row.innerHTML='<select class="bulkDay" aria-label="Jour">'+dayOptions(day)+'</select>'
                    +'<select class="bulkSlot" aria-label="Horaire">'+slotOptions(slot)+'</select>'
                    +'<input class="bulkRoom" type="text" maxlength="20" placeholder="Salle">'
                    +'<button class="bulkRemove" type="button" aria-label="Supprimer">×</button>';
                  row.querySelector('.bulkRoom').value=room||'';
                  row.querySelector('.bulkRemove').onclick=function(){if(rows.children.length>1)row.remove()};
                  row.querySelector('.bulkDay').onchange=function(){var d=Number(this.value),s=row.querySelector('.bulkSlot');s.innerHTML=slotOptions(firstFree(d))};
                  rows.appendChild(row);
                }

                function ensureModal(){
                  var modal=document.getElementById('bulkCourseModal');if(modal)return modal;
                  ensureStyle();
                  modal=document.createElement('div');modal.id='bulkCourseModal';
                  modal.innerHTML='<form id="bulkCourseSheet">'
                    +'<h3>Ajouter plusieurs cours</h3>'
                    +'<div class="bulkWeek">Même classe · semaine <b id="bulkWeekLetter">A</b></div>'
                    +'<div class="bulkField"><label>Classe / groupe</label><input id="bulkClassLabel" type="text" maxlength="80" required placeholder="Ex. 4G1 ALL"></div>'
                    +'<div id="bulkRows" class="bulkRows"></div>'
                    +'<button id="bulkAddLine" class="bulkAddLine" type="button">＋ Ajouter un autre jour / horaire</button>'
                    +'<div class="bulkHint">Tu indiques la classe une seule fois. Chaque ligne peut avoir son propre jour, horaire et salle.</div>'
                    +'<div class="bulkActions"><button id="bulkCancel" type="button" class="btn">Annuler</button><button type="submit" class="btn primary">Ajouter tous les cours</button></div>'
                    +'</form>';
                  document.body.appendChild(modal);

                  document.getElementById('bulkCancel').onclick=function(){modal.classList.remove('show')};
                  modal.onclick=function(e){if(e.target===modal)modal.classList.remove('show')};
                  document.getElementById('bulkAddLine').onclick=function(){
                    var rows=document.querySelectorAll('#bulkRows .bulkRow'),last=rows.length?rows[rows.length-1]:null;
                    var d=last?nextDay(Number(last.querySelector('.bulkDay').value)):selectedDay();
                    addRow(d,firstFree(d),'');
                  };
                  document.getElementById('bulkCourseSheet').onsubmit=function(e){
                    e.preventDefault();
                    try{
                      var label=document.getElementById('bulkClassLabel').value.trim();
                      if(!label){alert('Indique la classe ou le groupe.');return}
                      var letter=activeLetter(),rows=document.querySelectorAll('#bulkRows .bulkRow'),toAdd=[],seen={},conflicts=[];
                      for(var i=0;i<rows.length;i++){
                        var day=Number(rows[i].querySelector('.bulkDay').value),slot=Number(rows[i].querySelector('.bulkSlot').value),room=rows[i].querySelector('.bulkRoom').value.trim();
                        var s=slots[slot-1];if(!s)continue;
                        var key=day+'|'+s.start+'|'+s.end;
                        if(seen[key]){conflicts.push(dayLabel(day)+' '+s.start+'–'+s.end+' (doublon)');continue}
                        seen[key]=true;
                        var list=weeks[letter][day].courses;
                        var occupied=false;
                        for(var j=0;j<list.length;j++){
                          var cs=(typeof min==='function'?min(list[j].start):0),ce=(typeof min==='function'?min(list[j].end):0),ss=(typeof min==='function'?min(s.start):0),se=(typeof min==='function'?min(s.end):0);
                          if(cs<se&&ce>ss){occupied=true;break}
                        }
                        if(occupied){conflicts.push(dayLabel(day)+' '+s.start+'–'+s.end+' (déjà occupé)');continue}
                        toAdd.push({day:day,course:{start:s.start,end:s.end,label:label,room:room,slot:slot}});
                      }
                      if(conflicts.length){alert('Impossible d’ajouter :\n\n'+conflicts.join('\n'));return}
                      if(!toAdd.length){alert('Ajoute au moins un cours.');return}
                      for(var k=0;k<toAdd.length;k++)weeks[letter][toAdd[k].day].courses.push(toAdd[k].course);
                      if(typeof selected!=='undefined')selected=toAdd[0].day;
                      modal.classList.remove('show');
                      if(typeof save==='function')save();
                    }catch(err){alert('Impossible d’ajouter les cours en une fois.')}
                  };
                  return modal;
                }

                window.openBulkCourses=function(){
                  var modal=ensureModal(),rows=document.getElementById('bulkRows');
                  rows.innerHTML='';
                  document.getElementById('bulkClassLabel').value='';
                  document.getElementById('bulkWeekLetter').textContent=activeLetter();
                  var d=selectedDay();addRow(d,firstFree(d),'');
                  modal.classList.add('show');
                  setTimeout(function(){var f=document.getElementById('bulkClassLabel');if(f)f.focus()},60);
                };

                ensureButton();
                setInterval(ensureButton,1500);
              }catch(e){
                try{console.log('Bulk course UI',e)}catch(ignore){}
              }
            })();
            """;
    }
}
