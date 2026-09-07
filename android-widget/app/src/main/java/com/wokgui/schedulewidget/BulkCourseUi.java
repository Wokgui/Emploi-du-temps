package com.wokgui.schedulewidget;

final class BulkCourseUi {
    private BulkCourseUi() {}

    static String script() {
        return """
            (function(){
              try{
                const DAYS=[2,3,4,5,6], DN={2:'Lun',3:'Mar',4:'Mer',5:'Jeu',6:'Ven'};
                const deep=o=>JSON.parse(JSON.stringify(o));
                function loadRoot(){try{return JSON.parse(AndroidSchedule.loadSchedule()||'{}')}catch(e){return {}}}
                function saveRoot(root){try{AndroidSchedule.saveSchedule(JSON.stringify(root));if(window.reloadSchedule)window.reloadSchedule()}catch(e){alert('Impossible d’enregistrer.')}}
                function loadAdv(){try{return JSON.parse(AndroidSchedule.loadAdvancedSettings()||'{}')}catch(e){return {}}}
                function saveAdv(o){try{AndroidSchedule.saveAdvancedSettings(JSON.stringify(o))}catch(e){}}
                function oneWeek(){return loadAdv().singleWeek===true}
                function ensureWeek(root,w){root._weeks=root._weeks||{};root._weeks[w]=root._weeks[w]||{};DAYS.forEach(d=>{root._weeks[w][String(d)]=root._weeks[w][String(d)]||{enabled:true,courses:[]};root._weeks[w][String(d)].courses=root._weeks[w][String(d)].courses||[]})}
                function syncOne(root){ensureWeek(root,'A');root._weeks.B=deep(root._weeks.A);root._weeks.C=deep(root._weeks.A);root._weeks.D=deep(root._weeks.A);root._currentWeek='A';return root}
                function activeWeek(root){if(oneWeek())return 'A';const b=document.querySelector('#weekTabs .weekTab.active');return b?.dataset?.week||root._currentWeek||'A'}
                function selDay(){try{if([2,3,4,5,6].includes(Number(selected)))return Number(selected)}catch(e){};return 2}
                function slots(root){return Array.isArray(root._slots)&&root._slots.length?root._slots:[{start:'08:00',end:'09:00'},{start:'09:00',end:'10:00'},{start:'10:00',end:'11:00'},{start:'11:00',end:'12:00'},{start:'13:00',end:'14:00'},{start:'14:00',end:'15:00'},{start:'16:00',end:'17:00'}]}
                function mins(t){const p=String(t||'0:0').split(':').map(Number);return p[0]*60+p[1]}
                function firstFree(root,w,d){ensureWeek(root,w);const used=new Set(root._weeks[w][String(d)].courses.map(c=>Number(c.slot)).filter(Boolean));for(let i=1;i<=slots(root).length;i++)if(!used.has(i))return i;return 1}

                function style(){if(document.getElementById('bulkFixedStyle'))return;const s=document.createElement('style');s.id='bulkFixedStyle';s.textContent=`
                  #addBulkCourses{display:block!important;width:100%;margin-top:7px;padding:9px;border:1.5px solid var(--blue,#0877f9);border-radius:7px;background:#edf6ff;color:var(--blue,#0877f9);font-weight:800}
                  #weekModeBar{display:flex;align-items:center;gap:6px;margin:0 0 8px;padding:6px 8px;border:1px solid var(--line,#dce3eb);border-radius:8px;background:#fff}.weekModeLabel{font-size:.72rem;font-weight:800;white-space:nowrap}.weekModeChoices{display:flex;gap:4px;flex:1}.weekModeChoice{flex:1;border:1px solid #cfd9e5;border-radius:999px;background:#fff;padding:6px 4px;font-size:.68rem;font-weight:800;color:#4d5667}.weekModeChoice.active{background:var(--blue,#0877f9);border-color:var(--blue,#0877f9);color:#fff}
                  #bulkModalFixed{position:fixed;inset:0;z-index:220;background:#0b17386b;display:none;align-items:flex-end}#bulkModalFixed.show{display:flex}#bulkSheetFixed{width:100%;max-width:780px;margin:auto;background:#fff;border-radius:18px 18px 0 0;padding:14px 12px calc(14px + env(safe-area-inset-bottom));max-height:92vh;overflow:auto;color:var(--ink,#111936)}#bulkSheetFixed h3{text-align:center;margin:0 0 10px}.bulkClass{margin-bottom:8px}.bulkClass label{display:block;color:#68738a;font-size:.72rem;margin-bottom:3px}.bulkClass input{width:100%;border:1px solid #dce3eb;border-radius:7px;padding:9px}.bulkRows{border:1px solid #dce3eb;border-radius:8px;overflow:hidden}.bulkRow{display:grid;grid-template-columns:66px minmax(0,1fr) 78px 28px;gap:5px;align-items:center;padding:7px;border-top:1px solid #e9edf2}.bulkRow:first-child{border-top:0}.bulkRow select,.bulkRow input{width:100%;min-width:0;border:1px solid #dce3eb;border-radius:6px;padding:7px 4px;background:#fff;font-size:.70rem}.bulkRm{border:0;background:#fff1f3;color:#c6284e;border-radius:50%;width:28px;height:28px;font-size:18px}.bulkMore{width:100%;margin-top:7px;padding:8px;border:1px dashed var(--blue,#0877f9);border-radius:7px;background:#fff;color:var(--blue,#0877f9);font-weight:800}.bulkActions{display:flex;justify-content:flex-end;gap:7px;margin-top:10px}.singleWeekMode #weekTabs{display:none!important}.singleWeekMode #weekTitleLetter{display:none!important}`;document.head.appendChild(s)}

                function ensureButton(){style();const base=document.getElementById('addCourse');if(!base)return;let b=document.getElementById('addBulkCourses');if(!b){b=document.createElement('button');b.id='addBulkCourses';b.type='button';b.textContent='＋ Ajouter plusieurs cours à une classe';base.insertAdjacentElement('afterend',b)}b.onclick=openBulk}
                function ensureSingleState(){if(!oneWeek())return;try{currentWeek='A';activeWeek='A';if(typeof weeks!=='undefined'&&weeks.A)state=weeks.A}catch(e){}}
                function repairWeek(force){
                  if(!oneWeek())return;
                  const view=document.getElementById('viewWeek');if(!view||!view.classList.contains('active'))return;
                  ensureSingleState();
                  const grid=document.getElementById('weekGrid');
                  const needsRender=force===true||!grid||grid.children.length<6;
                  if(needsRender){
                    try{if(typeof renderWeek==='function')renderWeek();else if(typeof render==='function')render()}catch(e){}
                  }
                  if(window.refreshLunchBreakUi)window.refreshLunchBreakUi();
                  if(window.refreshDoubleLunchUi)window.refreshDoubleLunchUi();
                }
                function bindWeekRepair(){
                  const nav=document.querySelector('.nav[data-mode="week"]');
                  if(nav&&!nav.dataset.singleWeekRepair){
                    nav.dataset.singleWeekRepair='1';
                    nav.addEventListener('click',()=>setTimeout(()=>repairWeek(true),30));
                  }
                }
                function setMode(n){
                  const a=loadAdv();a.singleWeek=n===1;a.cycleLength=n===3?3:2;saveAdv(a);
                  if(n===1){
                    const r=syncOne(loadRoot());saveRoot(r);
                    try{AndroidSchedule.setCurrentWeek('A')}catch(e){}
                    ensureSingleState();
                  }else{
                    try{if(window.reloadSchedule)window.reloadSchedule()}catch(e){}
                  }
                  setTimeout(()=>{
                    if(window.refreshAdvancedFeatures)window.refreshAdvancedFeatures();
                    ensureSingleState();
                    if(typeof render==='function')render();
                    refresh();
                  },140)
                }
                function ensureModeBar(){const edit=document.getElementById('viewEdit');if(!edit)return;let bar=document.getElementById('weekModeBar');if(!bar){bar=document.createElement('div');bar.id='weekModeBar';bar.innerHTML='<span class="weekModeLabel">Semaines :</span><div class="weekModeChoices"><button type="button" class="weekModeChoice" data-m="1">1 seule</button><button type="button" class="weekModeChoice" data-m="2">A / B</button><button type="button" class="weekModeChoice" data-m="3">A / B / C</button></div>';const anchor=document.getElementById('importStatus');anchor.insertAdjacentElement('afterend',bar);bar.querySelectorAll('button').forEach(b=>b.onclick=()=>setMode(Number(b.dataset.m)))}const a=loadAdv(),m=a.singleWeek===true?1:(Number(a.cycleLength)>=3?3:2);bar.querySelectorAll('button').forEach(b=>b.classList.toggle('active',Number(b.dataset.m)===m))}
                function applySingleUi(){const one=oneWeek();document.documentElement.classList.toggle('singleWeekMode',one);if(!one)return;ensureSingleState();const cw=document.getElementById('currentWeekBtn');if(cw){cw.innerHTML='Semaine unique';cw.onclick=()=>{}}const e=document.getElementById('editDayTitle');if(e)e.textContent=(e.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');const t=document.getElementById('todayTitle');if(t)t.textContent=(t.textContent||'').replace(/ · semaine [A-D]/i,'').replace(/ - semaine [A-D]/i,'');const wh=document.querySelector('.weekTop h2');if(wh)wh.textContent='Aperçu semaine'}
                function wrapSave(){if(typeof window.save!=='function'||window.save.__oneWrapped)return;const old=window.save;const f=function(){if(oneWeek()){try{weeks.B=deep(weeks.A);if(weeks.C)weeks.C=deep(weeks.A);if(weeks.D)weeks.D=deep(weeks.A);currentWeek='A';activeWeek='A';state=weeks.A}catch(e){}}return old.apply(this,arguments)};f.__oneWrapped=true;window.save=f}

                function dayOpts(v){return DAYS.map(d=>`<option value="${d}" ${d===Number(v)?'selected':''}>${DN[d]}</option>`).join('')}
                function slotOpts(root,v){return slots(root).map((s,i)=>`<option value="${i+1}" ${i+1===Number(v)?'selected':''}>${i+1}e · ${s.start}–${s.end}</option>`).join('')}
                function addRow(root,w,d,sl,room=''){const box=document.getElementById('bulkRowsFixed');const row=document.createElement('div');row.className='bulkRow';row.innerHTML=`<select class="bd">${dayOpts(d)}</select><select class="bs">${slotOpts(root,sl)}</select><input class="br" placeholder="Salle" maxlength="20"><button type="button" class="bulkRm">×</button>`;row.querySelector('.br').value=room;row.querySelector('.bulkRm').onclick=()=>{if(box.children.length>1)row.remove()};row.querySelector('.bd').onchange=()=>{const r=loadRoot(),ww=activeWeek(r),dd=Number(row.querySelector('.bd').value);row.querySelector('.bs').innerHTML=slotOpts(r,firstFree(r,ww,dd))};box.appendChild(row)}
                function ensureModal(){let m=document.getElementById('bulkModalFixed');if(m)return m;m=document.createElement('div');m.id='bulkModalFixed';m.innerHTML='<form id="bulkSheetFixed"><h3>Ajouter plusieurs cours à une classe</h3><div class="bulkClass"><label>Classe / groupe</label><input id="bulkLabelFixed" required maxlength="80"></div><div id="bulkRowsFixed" class="bulkRows"></div><button id="bulkMoreFixed" type="button" class="bulkMore">＋ Ajouter un autre jour / horaire</button><div class="bulkActions"><button type="button" id="bulkCancelFixed" class="btn">Annuler</button><button type="submit" class="btn primary">Ajouter tous les cours</button></div></form>';document.body.appendChild(m);document.getElementById('bulkCancelFixed').onclick=()=>m.classList.remove('show');m.onclick=e=>{if(e.target===m)m.classList.remove('show')};document.getElementById('bulkMoreFixed').onclick=()=>{const r=loadRoot(),w=activeWeek(r),rows=[...document.querySelectorAll('#bulkRowsFixed .bulkRow')],last=rows.at(-1),d=last?DAYS[(DAYS.indexOf(Number(last.querySelector('.bd').value))+1)%DAYS.length]:selDay();addRow(r,w,d,firstFree(r,w,d))};document.getElementById('bulkSheetFixed').onsubmit=e=>{e.preventDefault();let r=loadRoot(),w=activeWeek(r);ensureWeek(r,w);const label=document.getElementById('bulkLabelFixed').value.trim();if(!label)return;const ss=slots(r),seen=new Set(),adds=[],errs=[];document.querySelectorAll('#bulkRowsFixed .bulkRow').forEach(row=>{const d=Number(row.querySelector('.bd').value),n=Number(row.querySelector('.bs').value),room=row.querySelector('.br').value.trim(),s=ss[n-1];if(!s)return;const k=d+'|'+n;if(seen.has(k)){errs.push(DN[d]+' '+s.start+' : doublon');return}seen.add(k);const list=r._weeks[w][String(d)].courses;const occupied=list.some(c=>mins(c.start)<mins(s.end)&&mins(c.end)>mins(s.start));if(occupied){errs.push(DN[d]+' '+s.start+' : déjà occupé');return}adds.push({d,c:{start:s.start,end:s.end,label,room,slot:n}})});if(errs.length){alert(errs.join(' • '));return}adds.forEach(x=>r._weeks[w][String(x.d)].courses.push(x.c));if(oneWeek())r=syncOne(r);m.classList.remove('show');saveRoot(r)};return m}
                function openBulk(){const r=loadRoot(),w=activeWeek(r),m=ensureModal();ensureWeek(r,w);document.getElementById('bulkLabelFixed').value='';document.getElementById('bulkRowsFixed').innerHTML='';const d=selDay();addRow(r,w,d,firstFree(r,w,d));m.classList.add('show')}

                function refresh(){ensureSingleState();ensureButton();ensureModeBar();wrapSave();bindWeekRepair();applySingleUi()}
                window.openBulkCourses=openBulk;window.refreshBulkCourseUi=refresh;refresh();setTimeout(refresh,100);setTimeout(refresh,500);
              }catch(e){console.log('BulkCourseUi',e)}
            })();
            """;
    }
}
