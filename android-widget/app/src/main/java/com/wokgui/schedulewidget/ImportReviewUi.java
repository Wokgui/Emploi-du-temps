package com.wokgui.schedulewidget;

/** Editable confirmation step for timetable-photo imports. */
final class ImportReviewUi {
    private ImportReviewUi() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtImportReviewV2)return;
                window.__edtImportReviewV2=true;

                function uiLang(){
                  try{const o=JSON.parse(AndroidSchedule.loadUiSettings()||'{}');return o.language==='en'||o.language==='de'?o.language:'fr'}catch(e){return 'fr'}
                }
                function tr(fr,en,de){const l=uiLang();return l==='en'?en:(l==='de'?de:fr)}
                function dayName(d){
                  try{if(typeof FULL!=='undefined'&&FULL[d])return FULL[d]}catch(e){}
                  const m={1:['Dimanche','Sunday','Sonntag'],2:['Lundi','Monday','Montag'],3:['Mardi','Tuesday','Dienstag'],4:['Mercredi','Wednesday','Mittwoch'],5:['Jeudi','Thursday','Donnerstag'],6:['Vendredi','Friday','Freitag'],7:['Samedi','Saturday','Samstag']};
                  const a=m[Number(d)]||[String(d),String(d),String(d)],l=uiLang();return l==='en'?a[1]:(l==='de'?a[2]:a[0]);
                }
                function esc(s){return String(s??'').replace(/[&<>\"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#39;'}[c]))}
                function activeDays(){try{return Array.isArray(DAYS)&&DAYS.length?DAYS.slice():[2,3,4,5,6]}catch(e){return [2,3,4,5,6]}}
                function slotFor(start,end){try{return typeof slotForTimes==='function'?slotForTimes(start,end):0}catch(e){return 0}}
                function qualityLabel(q){return q==='high'?tr('Lecture bonne','Good recognition','Gute Erkennung'):(q==='medium'?tr('Lecture moyenne','Average recognition','Mittlere Erkennung'):tr('Lecture à vérifier','Recognition to review','Erkennung prüfen'))}
                function warningText(code){
                  if(code==='dayColumnsInferred')return tr('Certains jours ont été reconstruits à partir de la position des colonnes.','Some days were reconstructed from column positions.','Einige Tage wurden aus den Spaltenpositionen rekonstruiert.');
                  if(code==='timesInferred')return tr('Certains horaires ont été déduits à partir de tes créneaux configurés.','Some times were inferred from your configured periods.','Einige Zeiten wurden aus deinen eingestellten Stunden abgeleitet.');
                  if(code==='wideLinesSkipped')return tr('Une ou plusieurs lignes trop larges ont été ignorées pour éviter de mélanger deux jours.','One or more unusually wide lines were ignored to avoid mixing two days.','Eine oder mehrere zu breite Zeilen wurden ignoriert, um zwei Tage nicht zu vermischen.');
                  if(code==='uncertainCells')return tr('Les lignes légèrement jaunes sont celles dont la position est la moins certaine.','Light yellow rows are the least certain detections.','Hellgelbe Zeilen sind die unsichersten Erkennungen.');
                  return '';
                }

                const style=document.createElement('style');
                style.id='edtImportReviewStyle';
                style.textContent=`
                  #edtImportReview{position:fixed;inset:0;z-index:120;background:#08152c66;display:none;align-items:flex-end;justify-content:center;padding-top:max(18px,env(safe-area-inset-top))}
                  #edtImportReview.show{display:flex}
                  #edtImportReview .irSheet{width:min(920px,100%);max-height:94vh;background:#fff;border-radius:18px 18px 0 0;display:flex;flex-direction:column;box-shadow:0 -12px 40px #06152b2c;overflow:hidden}
                  #edtImportReview .irHead{padding:14px 15px 10px;border-bottom:1px solid #e4e9ef;background:#fbfdff}
                  #edtImportReview .irHead h3{margin:0;font-size:1.04rem;text-align:center;color:#12203d}
                  #edtImportReview .irMeta{margin-top:4px;text-align:center;color:#66748a;font-size:.74rem;line-height:1.35}
                  #edtImportReview .irList{padding:9px 9px 88px;overflow:auto;background:#f6f8fb}
                  #edtImportReview .irDay{margin:0 0 9px;background:#fff;border:1px solid #dfe6ee;border-radius:10px;overflow:hidden}
                  #edtImportReview .irDayHead{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:8px 10px;background:#f7fbff;border-bottom:1px solid #e6edf4;font-weight:850;color:#1268b3;font-size:.82rem}
                  #edtImportReview .irDayCount{font-size:.67rem;color:#738095;font-weight:750}
                  #edtImportReview .irRow{display:grid;grid-template-columns:112px 1fr 76px 38px;gap:6px;padding:8px;border-top:1px solid #edf0f4;align-items:center}
                  #edtImportReview .irRow:first-of-type{border-top:0}
                  #edtImportReview .irRow.irUncertain{background:#fff9e8;box-shadow:inset 3px 0 #e1ad29}
                  #edtImportReview .irTimes{display:grid;grid-template-columns:1fr 1fr;gap:4px}
                  #edtImportReview input,#edtImportReview select{min-width:0;width:100%;border:1px solid #cfd9e5;border-radius:7px;background:#fff;color:#14203a;padding:7px 6px;font-size:.73rem}
                  #edtImportReview .irLabel{font-weight:700}
                  #edtImportReview .irDelete{width:36px;height:36px;border:1px solid #efc0ca;border-radius:8px;background:#fff6f8;color:#c3294a;font-size:1.1rem;line-height:1}
                  #edtImportReview .irAdd{width:100%;border:1.5px dashed #1b79d1;border-radius:9px;background:#fff;color:#126bc1;padding:9px;font-weight:800;font-size:.78rem}
                  #edtImportReview .irFoot{position:absolute;left:0;right:0;bottom:0;display:flex;gap:8px;padding:10px 12px calc(10px + env(safe-area-inset-bottom));background:#fffffff4;border-top:1px solid #dfe6ee;backdrop-filter:blur(12px)}
                  #edtImportReview .irFoot button{flex:1;padding:10px;border-radius:9px;font-weight:850;border:1px solid #cfd9e5;background:#fff;color:#314057}
                  #edtImportReview .irApply{background:#0877f9!important;border-color:#0877f9!important;color:#fff!important}
                  #edtImportReview .irWarning{margin:2px 3px 9px;color:#6e7788;font-size:.68rem;line-height:1.45}
                  #edtImportReview .irWarning strong{color:#4f5c70}
                  @media(max-width:560px){#edtImportReview .irRow{grid-template-columns:92px minmax(0,1fr) 64px 36px;gap:5px;padding:7px 6px}#edtImportReview input,#edtImportReview select{font-size:.68rem;padding:7px 4px}}
                `;
                document.head.appendChild(style);

                const overlay=document.createElement('div');
                overlay.id='edtImportReview';
                overlay.innerHTML=`
                  <div class="irSheet">
                    <div class="irHead"><h3></h3><div class="irMeta"></div></div>
                    <div class="irList"></div>
                    <div class="irFoot"><button type="button" class="irCancel"></button><button type="button" class="irApply"></button></div>
                  </div>`;
                document.body.appendChild(overlay);
                const list=overlay.querySelector('.irList'),title=overlay.querySelector('h3'),meta=overlay.querySelector('.irMeta');
                const cancel=overlay.querySelector('.irCancel'),apply=overlay.querySelector('.irApply');
                let reviewRows=[],originalDetectedDays=[],reviewWarnings=[],reviewQuality='high';

                function makeRow(course,day){
                  const confidence=Number(course&&course.confidence);return {day:Number(day),start:String(course&&course.start||''),end:String(course&&course.end||''),label:String(course&&course.label||''),room:String(course&&course.room||''),confidence:Number.isFinite(confidence)?confidence:1,uncertain:!!(course&&course.uncertain)};
                }
                function collectRows(){
                  reviewRows=[];
                  overlay.querySelectorAll('.irRow').forEach(row=>{
                    const d=Number(row.querySelector('.irDaySelect').value),start=row.querySelector('.irStart').value,end=row.querySelector('.irEnd').value,label=row.querySelector('.irLabel').value.trim(),room=row.querySelector('.irRoom').value.trim();
                    if(label)reviewRows.push({day:d,start,end,label,room,confidence:Number(row.dataset.confidence||1),uncertain:row.classList.contains('irUncertain')});
                  });
                }
                function dayOptions(selected){return activeDays().map(d=>`<option value="${d}"${Number(d)===Number(selected)?' selected':''}>${esc(dayName(d))}</option>`).join('')}
                function rowHtml(r){const c=Number.isFinite(Number(r.confidence))?Number(r.confidence):1,u=!!r.uncertain||c<.64;return `<div class="irRow${u?' irUncertain':''}" data-confidence="${c}"><select class="irDaySelect" aria-label="${esc(tr('Jour','Day','Tag'))}">${dayOptions(r.day)}</select><div><input class="irLabel" maxlength="80" value="${esc(r.label)}" aria-label="${esc(tr('Classe ou cours','Class or course','Klasse oder Kurs'))}"><div class="irTimes"><input class="irStart" type="time" value="${esc(r.start)}" aria-label="${esc(tr('Début','Start','Beginn'))}"><input class="irEnd" type="time" value="${esc(r.end)}" aria-label="${esc(tr('Fin','End','Ende'))}"></div></div><input class="irRoom" maxlength="20" value="${esc(r.room)}" placeholder="${esc(tr('Salle','Room','Raum'))}" aria-label="${esc(tr('Salle','Room','Raum'))}"><button type="button" class="irDelete" aria-label="${esc(tr('Supprimer','Delete','Löschen'))}">×</button></div>`}

                function renderReview(){
                  const grouped=new Map();activeDays().forEach(d=>grouped.set(Number(d),[]));reviewRows.forEach(r=>{if(!grouped.has(Number(r.day)))grouped.set(Number(r.day),[]);grouped.get(Number(r.day)).push(r)});
                  const warnings=reviewWarnings.map(warningText).filter(Boolean);
                  let warning=`<strong>${esc(tr('Vérifie les cours détectés avant de les appliquer.','Check the detected classes before applying them.','Prüfe die erkannten Stunden vor dem Übernehmen.'))}</strong> ${esc(tr('Les jours sans cours détecté ne seront pas effacés automatiquement.','Days with no detected classes will not be cleared automatically.','Tage ohne erkannte Stunden werden nicht automatisch geleert.'))}`;
                  if(warnings.length)warning+='<br>'+warnings.map(x=>'• '+esc(x)).join('<br>');
                  list.innerHTML=`<div class="irWarning">${warning}</div>`;
                  grouped.forEach((rows,d)=>{
                    if(!rows.length&&!originalDetectedDays.includes(Number(d)))return;
                    const section=document.createElement('section');section.className='irDay';
                    section.innerHTML=`<div class="irDayHead"><span>${esc(dayName(d))}</span><span class="irDayCount">${rows.length} ${esc(tr('cours',rows.length===1?'class':'classes',rows.length===1?'Stunde':'Stunden'))}</span></div>`+rows.map(rowHtml).join('');
                    list.appendChild(section);
                  });
                  const add=document.createElement('button');add.type='button';add.className='irAdd';add.textContent=tr('＋ Ajouter un cours','＋ Add a class','＋ Stunde hinzufügen');add.onclick=()=>{collectRows();const days=activeDays(),d=days[0]||2;reviewRows.push({day:d,start:'08:00',end:'09:00',label:'',room:'',confidence:1,uncertain:false});renderReview();setTimeout(()=>{const rows=list.querySelectorAll('.irRow');const last=rows[rows.length-1];if(last)last.querySelector('.irLabel').focus()},0)};list.appendChild(add);
                  list.querySelectorAll('.irDelete').forEach(btn=>btn.onclick=()=>{collectRows();const row=btn.closest('.irRow');if(!row)return;const all=[...list.querySelectorAll('.irRow')],idx=all.indexOf(row);if(idx>=0)reviewRows.splice(idx,1);renderReview()});
                }

                function openReview(result){
                  const days=result.days||activeDays();reviewRows=[];originalDetectedDays=[];reviewWarnings=Array.isArray(result.warningCodes)?result.warningCodes.slice():[];reviewQuality=result.quality||'high';
                  days.forEach(d=>{const rows=result.parsed&&result.parsed[d]||[];if(rows.length)originalDetectedDays.push(Number(d));rows.forEach(c=>reviewRows.push(makeRow(c,d)))});
                  title.textContent=tr('Vérifier l’import · semaine ','Review import · week ','Import prüfen · Woche ')+(typeof activeWeek!=='undefined'?activeWeek:'A');
                  meta.textContent=qualityLabel(reviewQuality)+' · '+result.count+' '+tr(result.count>1?'cours détectés':'cours détecté',result.count===1?'class detected':'classes detected',result.count===1?'Stunde erkannt':'Stunden erkannt');
                  cancel.textContent=tr('Annuler','Cancel','Abbrechen');apply.textContent=tr('Appliquer','Apply','Übernehmen');
                  renderReview();overlay.classList.add('show');
                }
                function closeReview(){overlay.classList.remove('show')}
                cancel.onclick=()=>{closeReview();const s=document.getElementById('importStatus');if(s)s.textContent=tr('Import annulé.','Import cancelled.','Import abgebrochen.')};
                overlay.addEventListener('click',e=>{if(e.target===overlay)closeReview()});
                apply.onclick=()=>{
                  collectRows();
                  const invalid=reviewRows.find(r=>!r.start||!r.end||r.end<=r.start||!r.label.trim());
                  if(invalid){alert(tr('Corrige les lignes incomplètes : chaque cours doit avoir un nom et un horaire de fin après le début.','Fix incomplete rows: each class needs a name and an end time after its start.','Korrigiere unvollständige Zeilen: Jede Stunde braucht einen Namen und ein Endzeitpunkt nach dem Beginn.'));return}
                  const byDay=new Map();reviewRows.forEach(r=>{const d=Number(r.day);if(!byDay.has(d))byDay.set(d,[]);byDay.get(d).push({start:r.start,end:r.end,label:r.label.trim(),room:r.room.trim(),slot:slotFor(r.start,r.end)})});
                  try{
                    let applied=0;byDay.forEach((rows,d)=>{if(!rows.length)return;rows.sort((a,b)=>String(a.start).localeCompare(String(b.start)));if(typeof weeks!=='undefined'&&weeks[activeWeek]&&weeks[activeWeek][d]){weeks[activeWeek][d].courses=rows;applied+=rows.length}});
                    if(!applied){alert(tr('Aucun cours à importer.','No classes to import.','Keine Stunden zum Importieren.'));return}
                    selected=[...byDay.keys()][0]||selected;closeReview();const s=document.getElementById('importStatus');if(s)s.textContent=applied+' '+tr(applied>1?'cours importés et vérifiés.':'cours importé et vérifié.',applied===1?'class imported and reviewed.':'classes imported and reviewed.',applied===1?'Stunde importiert und geprüft.':'Stunden importiert und geprüft.');if(typeof save==='function')save();
                  }catch(e){if(window.applyOcrError)window.applyOcrError(tr('La validation de l’import a échoué.','Import validation failed.','Importprüfung fehlgeschlagen.'))}
                };

                window.applyOcrSchedule=function(raw){
                  try{
                    const button=document.getElementById('importPhoto'),status=document.getElementById('importStatus');if(button){button.disabled=false;button.textContent=tr('Importer une photo d’emploi du temps','Import a timetable photo','Stundenplanfoto importieren')}
                    const payload=typeof raw==='string'?JSON.parse(raw):raw,result=window.parseOcrSchedule?window.parseOcrSchedule(payload):null;
                    if(!result)throw new Error('parser unavailable');
                    if(result.error){if(status)status.textContent='';alert(result.error);return}
                    openReview(result);
                  }catch(e){if(window.applyOcrError)window.applyOcrError(tr('La photo a été lue mais la conversion a échoué.','The photo was read but conversion failed.','Das Foto wurde gelesen, aber die Umwandlung ist fehlgeschlagen.'))}
                };
                window.openTimetableImportReview=openReview;
              }catch(e){console.log('ImportReviewUi',e)}
            })();
            """;
    }
}
