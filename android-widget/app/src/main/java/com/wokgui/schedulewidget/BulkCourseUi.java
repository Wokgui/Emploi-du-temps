package com.wokgui.schedulewidget;

final class BulkCourseUi {
    private BulkCourseUi() {}

    static String script() {
        return """
            (function(){
              try{
                var DAYS=[2,3,4,5,6];
                var DAY_NAMES={2:'Lun',3:'Mar',4:'Mer',5:'Jeu',6:'Ven'};

                function loadScheduleRoot(){
                  try{
                    if(window.AndroidSchedule&&AndroidSchedule.loadSchedule){
                      return JSON.parse(AndroidSchedule.loadSchedule()||'{}');
                    }
                  }catch(e){}
                  return {};
                }
                function saveScheduleRoot(root){
                  try{
                    if(window.AndroidSchedule&&AndroidSchedule.saveSchedule){
                      AndroidSchedule.saveSchedule(JSON.stringify(root));
                      if(window.reloadSchedule)window.reloadSchedule();
                    }
                  }catch(e){alert('Impossible d’enregistrer les cours.');}
                }
                function activeWeekLetter(root){
                  var active=document.querySelector('#weekTabs .weekTab.active');
                  if(active&&active.dataset&&active.dataset.week)return active.dataset.week;
                  return (root&&root._currentWeek)||'A';
                }
                function selectedDay(){
                  try{
                    if(typeof selected!=='undefined'&&DAYS.indexOf(Number(selected))>=0)return Number(selected);
                  }catch(e){}
                  var active=document.querySelector('#dayTabs .dayTab.active');
                  if(active){
                    var txt=(active.textContent||'').trim().toLowerCase();
                    var map={lun:2,mar:3,mer:4,jeu:5,ven:6};
                    if(map[txt])return map[txt];
                  }
                  return 2;
                }
                function slotsFrom(root){
                  if(root&&Array.isArray(root._slots)&&root._slots.length)return root._slots;
                  return [
                    {start:'08:00',end:'09:00'},{start:'09:00',end:'10:00'},{start:'10:00',end:'11:00'},
                    {start:'11:00',end:'12:00'},{start:'13:00',end:'14:00'},{start:'14:00',end:'15:00'},
                    {start:'16:00',end:'17:00'}
                  ];
                }
                function ensureWeek(root,letter){
                  if(!root._weeks)root._weeks={};
                  if(!root._weeks[letter])root._weeks[letter]={};
                  for(var i=0;i<DAYS.length;i++){
                    var d=String(DAYS[i]);
                    if(!root._weeks[letter][d])root._weeks[letter][d]={enabled:true,courses:[]};
                    if(!Array.isArray(root._weeks[letter][d].courses))root._weeks[letter][d].courses=[];
                  }
                }
                function toMinutes(t){
                  var p=String(t||'00:00').split(':');return (Number(p[0])||0)*60+(Number(p[1])||0);
                }

                function ensureStyles(){
                  if(document.getElementById('bulkCourseStyleV3'))return;
                  var s=document.createElement('style');s.id='bulkCourseStyleV3';
                  s.textContent=''
                    +'#addBulkCourses{display:block!important;visibility:visible!important;opacity:1!important;width:100%;margin-top:7px;padding:9px;border:1.5px solid var(--blue,#0877f9);border-radius:7px;background:#eef6ff;color:var(--blue,#0877f9);font-size:.84rem;font-weight:800}'
                    +'#bulkCourseModal{position:fixed;inset:0;z-index:180;background:#0b17386b;display:none;align-items:flex-end}'
                    +'#bulkCourseModal.show{display:flex}'
                    +'#bulkCourseSheet{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:18px 18px 0 0;padding:14px 12px calc(14px + env(safe-area-inset-bottom));max-height:91vh;overflow:auto;color:var(--ink,#111936)}'
                    +'#bulkCourseSheet h3{margin:0 0 3px;text-align:center;font-size:1.05rem}.bulkWeek{text-align:center;color:var(--muted,#68738a);font-size:.72rem;margin-bottom:10px}'
                    +'.bulkField{margin-bottom:8px}.bulkField label{display:block;color:var(--muted,#68738a);font-size:.72rem;margin-bottom:3px}.bulkField input{width:100%;border:1px solid var(--line,#dce3eb);border-radius:7px;padding:9px;background:#fff;color:inherit}'
                    +'.bulkRows{border:1px solid var(--line,#dce3eb);border-radius:9px;overflow:hidden;background:#fff}.bulkRow{display:grid;grid-template-columns:70px minmax(0,1fr) 80px 30px;gap:5px;align-items:center;padding:7px;border-top:1px solid #e9edf2}.bulkRow:first-child{border-top:0}'
                    +'.bulkRow select,.bulkRow input{width:100%;min-width:0;border:1px solid var(--line,#dce3eb);border-radius:7px;padding:8px 4px;background:#fff;color:inherit;font-size:.72rem}.bulkRemove{width:30px;height:30px;border:0;border-radius:50%;background:#fff1f3;color:#c6284e;font-size:18px}'
                    +'.bulkAddLine{width:100%;margin-top:7px;padding:8px;border:1px dashed var(--blue,#0877f9);border-radius:7px;background:#fff;color:var(--blue,#0877f9);font-weight:800}.bulkHint{font-size:.67rem;color:var(--muted,#68738a);line-height:1.25;margin:6px 2px 0}.bulkActions{display:flex;justify-content:flex-end;gap:7px;margin-top:11px}'
                    +'#weekCycleQuick{border:1px solid #dde4ec;border-radius:10px;padding:10px;margin-top:9px}.weekCycleChoices{display:grid;grid-template-columns:1fr 1fr;gap:7px}.weekCycleChoice{padding:9px 8px;border:1px solid #cfd9e5;border-radius:8px;background:#fff;color:#334155;font-weight:800}.weekCycleChoice.active{background:var(--set-accent,var(--blue,#0877f9));border-color:var(--set-accent,var(--blue,#0877f9));color:#fff}.weekCycleHint{font-size:.67rem;color:#68738a;margin-top:6px}';
                  document.head.appendChild(s);
                }

                function ensureButton(){
                  ensureStyles();
                  var base=document.getElementById('addCourse');if(!base)return;
                  var btn=document.getElementById('addBulkCourses');
                  if(!btn){
                    btn=document.createElement('button');btn.id='addBulkCourses';btn.type='button';btn.textContent='＋ Ajouter plusieurs cours à une classe';
                    base.insertAdjacentElement('afterend',btn);
                  }
                  btn.style.setProperty('display','block','important');
                  btn.style.setProperty('visibility','visible','important');
                  btn.style.setProperty('opacity','1','important');
                  btn.onclick=openBulk;
                }

                function dayOptions(selected){
                  var html='';for(var i=0;i<DAYS.length;i++){var d=DAYS[i];html+='<option value="'+d+'"'+(d===Number(selected)?' selected':'')+'>'+DAY_NAMES[d]+'</option>';}return html;
                }
                function slotOptions(root,selected){
                  var slots=slotsFrom(root),html='';for(var i=0;i<slots.length;i++){var n=i+1,sl=slots[i];html+='<option value="'+n+'"'+(n===Number(selected)?' selected':'')+'>'+n+'e · '+sl.start+'–'+sl.end+'</option>';}return html;
                }
                function firstFree(root,letter,day){
                  ensureWeek(root,letter);var courses=root._weeks[letter][String(day)].courses||[],used={};
                  for(var i=0;i<courses.length;i++)if(Number(courses[i].slot)>0)used[Number(courses[i].slot)]=true;
                  var slots=slotsFrom(root);for(var j=1;j<=slots.length;j++)if(!used[j])return j;return 1;
                }
                function nextDay(day){var i=DAYS.indexOf(Number(day));return DAYS[(i+1+DAYS.length)%DAYS.length];}

                function ensureModal(){
                  ensureStyles();
                  var modal=document.getElementById('bulkCourseModal');if(modal)return modal;
                  modal=document.createElement('div');modal.id='bulkCourseModal';
                  modal.innerHTML='<form id="bulkCourseSheet">'
                    +'<h3>Ajouter plusieurs cours à une classe</h3><div class="bulkWeek">Semaine <b id="bulkWeekLetter">A</b></div>'
                    +'<div class="bulkField"><label>Classe / groupe</label><input id="bulkClassLabel" type="text" maxlength="80" required placeholder="Ex. 4G1 ALL · 4G2 ALL"></div>'
                    +'<div id="bulkRows" class="bulkRows"></div>'
                    +'<button id="bulkAddLine" class="bulkAddLine" type="button">＋ Ajouter un autre jour / horaire</button>'
                    +'<div class="bulkHint">La classe est saisie une seule fois. Chaque ligne peut avoir son propre jour, horaire et salle.</div>'
                    +'<div class="bulkActions"><button id="bulkCancel" type="button" class="btn">Annuler</button><button type="submit" class="btn primary">Ajouter tous les cours</button></div></form>';
                  document.body.appendChild(modal);
                  document.getElementById('bulkCancel').onclick=function(){modal.classList.remove('show');};
                  modal.onclick=function(e){if(e.target===modal)modal.classList.remove('show');};
                  document.getElementById('bulkAddLine').onclick=function(){
                    var root=loadScheduleRoot(),letter=activeWeekLetter(root),rows=document.querySelectorAll('#bulkRows .bulkRow'),last=rows.length?rows[rows.length-1]:null;
                    var d=last?nextDay(Number(last.querySelector('.bulkDay').value)):selectedDay();addRow(root,letter,d,firstFree(root,letter,d),'');
                  };
                  document.getElementById('bulkCourseSheet').onsubmit=function(e){
                    e.preventDefault();var root=loadScheduleRoot(),letter=activeWeekLetter(root);ensureWeek(root,letter);
                    var label=(document.getElementById('bulkClassLabel').value||'').trim();if(!label){alert('Indique la classe ou le groupe.');return;}
                    var slots=slotsFrom(root),rows=document.querySelectorAll('#bulkRows .bulkRow'),seen={},toAdd=[],errors=[];
                    for(var i=0;i<rows.length;i++){
                      var day=Number(rows[i].querySelector('.bulkDay').value),slot=Number(rows[i].querySelector('.bulkSlot').value),room=(rows[i].querySelector('.bulkRoom').value||'').trim(),sl=slots[slot-1];if(!sl)continue;
                      var key=day+'|'+sl.start+'|'+sl.end;if(seen[key]){errors.push(DAY_NAMES[day]+' '+sl.start+'–'+sl.end+' : doublon');continue;}seen[key]=true;
                      var list=root._weeks[letter][String(day)].courses||[],occupied=false;
                      for(var j=0;j<list.length;j++){if(toMinutes(list[j].start)<toMinutes(sl.end)&&toMinutes(list[j].end)>toMinutes(sl.start)){occupied=true;break;}}
                      if(occupied){errors.push(DAY_NAMES[day]+' '+sl.start+'–'+sl.end+' : déjà occupé');continue;}
                      toAdd.push({day:day,course:{start:sl.start,end:sl.end,label:label,room:room,slot:slot}});
                    }
                    if(errors.length){alert('Impossible d’ajouter :\n\n'+errors.join('\n'));return;}if(!toAdd.length){alert('Ajoute au moins une ligne.');return;}
                    for(var k=0;k<toAdd.length;k++)root._weeks[letter][String(toAdd[k].day)].courses.push(toAdd[k].course);
                    modal.classList.remove('show');saveScheduleRoot(root);setTimeout(ensureButton,120);
                  };
                  return modal;
                }
                function addRow(root,letter,day,slot,room){
                  var rows=document.getElementById('bulkRows');if(!rows)return;var row=document.createElement('div');row.className='bulkRow';
                  row.innerHTML='<select class="bulkDay">'+dayOptions(day)+'</select><select class="bulkSlot">'+slotOptions(root,slot)+'</select><input class="bulkRoom" type="text" maxlength="20" placeholder="Salle"><button class="bulkRemove" type="button">×</button>';
                  row.querySelector('.bulkRoom').value=room||'';
                  row.querySelector('.bulkRemove').onclick=function(){if(rows.children.length>1)row.remove();};
                  row.querySelector('.bulkDay').onchange=function(){var d=Number(this.value),r=loadScheduleRoot(),l=activeWeekLetter(r);row.querySelector('.bulkSlot').innerHTML=slotOptions(r,firstFree(r,l,d));};
                  rows.appendChild(row);
                }
                function openBulk(){
                  var root=loadScheduleRoot(),letter=activeWeekLetter(root),modal=ensureModal(),rows=document.getElementById('bulkRows');ensureWeek(root,letter);rows.innerHTML='';document.getElementById('bulkClassLabel').value='';document.getElementById('bulkWeekLetter').textContent=letter;
                  var d=selectedDay();addRow(root,letter,d,firstFree(root,letter,d),'');modal.classList.add('show');setTimeout(function(){var f=document.getElementById('bulkClassLabel');if(f)f.focus();},50);
                }

                function readAdv(){
                  try{return JSON.parse(window.AndroidSchedule&&AndroidSchedule.loadAdvancedSettings?AndroidSchedule.loadAdvancedSettings():'{}');}catch(e){return {};}
                }
                function setCycle(n){
                  var adv=readAdv();adv.cycleLength=n;
                  try{if(window.AndroidSchedule&&AndroidSchedule.saveAdvancedSettings)AndroidSchedule.saveAdvancedSettings(JSON.stringify(adv));}catch(e){}
                  setTimeout(function(){if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();refreshWeekCycle();},80);
                }
                function ensureWeekCycle(){
                  var sheet=document.getElementById('settingsSheet');if(!sheet)return;
                  var box=document.getElementById('weekCycleQuick');
                  if(!box){
                    box=document.createElement('div');box.id='weekCycleQuick';box.innerHTML='<div class="settingTitle">Semaines utilisées</div><div class="weekCycleChoices"><button id="cycleAB" class="weekCycleChoice" type="button">A / B</button><button id="cycleABC" class="weekCycleChoice" type="button">A / B / C</button></div><div class="weekCycleHint">Active ou désactive la semaine C. Les emplois du temps A, B et C restent enregistrés séparément.</div>';
                    var first=sheet.querySelector('.settingBox');if(first)first.insertAdjacentElement('beforebegin',box);else sheet.appendChild(box);
                    document.getElementById('cycleAB').onclick=function(){setCycle(2);};document.getElementById('cycleABC').onclick=function(){setCycle(3);};
                  }
                  var old=document.getElementById('advCycle');if(old&&old.closest('.advRow'))old.closest('.advRow').style.display='none';
                  refreshWeekCycle();
                }
                function refreshWeekCycle(){
                  var adv=readAdv(),n=Number(adv.cycleLength)||2,a=document.getElementById('cycleAB'),b=document.getElementById('cycleABC');if(a)a.classList.toggle('active',n<3);if(b)b.classList.toggle('active',n>=3);
                }

                function refreshAll(){ensureButton();ensureWeekCycle();}
                window.openBulkCourses=openBulk;
                window.refreshBulkCourseUi=refreshAll;
                refreshAll();
                setTimeout(refreshAll,100);setTimeout(refreshAll,500);setInterval(ensureButton,1200);
                if(document.body)new MutationObserver(function(){ensureButton();ensureWeekCycle();}).observe(document.body,{childList:true,subtree:true});
              }catch(e){try{console.log('BulkCourseUi',e);}catch(ignore){}}
            })();
            """;
    }
}
