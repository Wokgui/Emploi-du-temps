package com.wokgui.schedulewidget;

/** Timetable-photo OCR parsing. */
final class ImportParserUi {
    private ImportParserUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(64 * 1024);
        out.append(layer0()).append('\n'); // OcrImport80Ui
        return out.toString();
    }

    // Former OcrImport80Ui; isolated to stay below JVM constant limits.
    private static String layer0() {
        return """
            (function(){
              try{
                if(window.__ocrImport80V1)return;window.__ocrImport80V1=true;

                function norm(s){return String(s||'').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[’']/g,'').replace(/\\s+/g,' ').trim()}
                function ocrDay80(text){
                  const s=' '+norm(text)+' ';
                  const tests=[
                    [2,/\\b(lun(?:di)?|mon(?:day)?|montag|lunes|lunedi|segunda(?:-feira)?|maandag)\\b/],
                    [3,/\\b(mar(?:di)?|tue(?:sday)?|dienstag|martes|martedi|terca(?:-feira)?|dinsdag)\\b/],
                    [4,/\\b(mer(?:credi)?|wed(?:nesday)?|mittwoch|miercoles|mercoledi|quarta(?:-feira)?|woensdag)\\b/],
                    [5,/\\b(jeu(?:di)?|thu(?:rsday)?|donnerstag|jueves|giovedi|quinta(?:-feira)?|donderdag)\\b/],
                    [6,/\\b(ven(?:dredi)?|fri(?:day)?|freitag|viernes|venerdi|sexta(?:-feira)?|vrijdag)\\b/],
                    [7,/\\b(sam(?:edi)?|sat(?:urday)?|samstag|sabado|sabato|zaterdag)\\b/],
                    [1,/\\b(dim(?:anche)?|sun(?:day)?|sonntag|domingo|domenica|zondag)\\b/]
                  ];
                  for(const [d,re] of tests)if(re.test(s))return d;return null;
                }

                function ocrTimes80(text){
                  let s=String(text||'')
                    .replace(/(\\d{1,2})\\s*[hH]\\s*([0-5]\\d)/g,'$1:$2')
                    .replace(/(\\d{1,2})\\s*[hH](?!\\d)/g,'$1:00')
                    .replace(/\\b(\\d{1,2})[.](\\d{2})\\b/g,'$1:$2');
                  const out=[],re=/\\b([0-2]?\\d):([0-5]\\d)\\b/g;let m;
                  while((m=re.exec(s))){const h=Number(m[1]);if(h<24){const v=String(h).padStart(2,'0')+':'+m[2];if(!out.includes(v))out.push(v)}}
                  return out;
                }

                function nearest80(arr,value,key){let best=null,dist=Infinity;for(const x of arr){const d=Math.abs(key(x)-value);if(d<dist){dist=d;best=x}}return {item:best,dist}}
                function orderIndex(d){return [2,3,4,5,6,7,1].indexOf(Number(d))}
                function activeDays80(){try{return Array.isArray(DAYS)&&DAYS.length?DAYS.slice():[2,3,4,5,6]}catch(e){return [2,3,4,5,6]}}
                function kmeans1d(values,k){
                  const v=values.filter(Number.isFinite).sort((a,b)=>a-b);if(!v.length||k<1)return [];
                  if(k===1)return [v[Math.floor(v.length/2)]];
                  let c=Array.from({length:k},(_,i)=>v[Math.round(i*(v.length-1)/(k-1))]);
                  for(let n=0;n<12;n++){
                    const groups=Array.from({length:k},()=>[]);
                    v.forEach(x=>{let bi=0,bd=Infinity;c.forEach((q,i)=>{const d=Math.abs(q-x);if(d<bd){bd=d;bi=i}});groups[bi].push(x)});
                    const next=c.map((old,i)=>groups[i].length?groups[i].reduce((a,b)=>a+b,0)/groups[i].length:old).sort((a,b)=>a-b);
                    if(next.every((x,i)=>Math.abs(x-c[i])<.5)){c=next;break}c=next;
                  }
                  return c;
                }
                function typicalSpacing(values){const s=values.slice().sort((a,b)=>a-b),d=[];for(let i=1;i<s.length;i++)if(s[i]-s[i-1]>2)d.push(s[i]-s[i-1]);if(!d.length)return Infinity;d.sort((a,b)=>a-b);return d[Math.floor(d.length/2)]}

                function parseOcrSchedule80(payload){
                  const lines=(payload&&payload.lines||[]).map(x=>({...x,cx:(Number(x.l)+Number(x.r))/2,cy:(Number(x.t)+Number(x.b))/2,text:String(x.text||'').trim()})).filter(x=>x.text&&Number.isFinite(x.cx)&&Number.isFinite(x.cy));
                  if(!lines.length)return {error:'Aucun texte détecté sur la photo.'};
                  const days=activeDays80(),activeSet=new Set(days.map(Number));

                  const headers=[];for(const line of lines){const d=ocrDay80(line.text);if(d&&activeSet.has(d))headers.push({d,x:line.cx,y:line.cy})}
                  const byDay=new Map();for(const h of headers){const old=byDay.get(h.d);if(!old||h.y<old.y)byDay.set(h.d,h)}
                  let dayPos=[...byDay.values()];
                  if(dayPos.length>=2){
                    const ordered=dayPos.sort((a,b)=>orderIndex(a.d)-orderIndex(b.d)),first=ordered[0],last=ordered[ordered.length-1],a=orderIndex(first.d),b=orderIndex(last.d);
                    const step=(last.x-first.x)/Math.max(1,b-a);dayPos=days.map(d=>({d,x:first.x+(orderIndex(d)-a)*step,y:first.y}));
                  }else{
                    const candidateX=lines.filter(l=>!ocrTimes80(l.text).length&&!ocrDay80(l.text)).map(l=>l.cx);
                    const centers=kmeans1d(candidateX,days.length);if(centers.length<days.length)return {error:'Je ne reconnais pas assez clairement les colonnes des jours.'};
                    dayPos=days.map((d,i)=>({d,x:centers[i],y:0}));
                  }
                  const daySpacing=typicalSpacing(dayPos.map(x=>x.x));
                  const headerY=headers.length?Math.max(...headers.map(h=>h.y)):Math.min(...lines.map(l=>l.cy))-4;

                  let raw=[];
                  for(const line of lines){
                    const ts=ocrTimes80(line.text);if(!ts.length)continue;
                    raw.push({start:ts[0],end:ts[1]||'',y:line.cy,explicit:ts.length>1});
                  }
                  raw.sort((a,b)=>a.y-b.y);
                  for(let i=0;i<raw.length;i++)if(!raw[i].end){
                    const existing=(typeof slots!=='undefined'&&Array.isArray(slots))?slots.find(s=>s.start===raw[i].start):null;
                    const start=min(raw[i].start),next=raw.slice(i+1).find(x=>min(x.start)>start&&min(x.start)-start<=180);
                    raw[i].end=existing?existing.end:(next?next.start:clock(Math.min(23*60+59,start+60)));
                  }
                  const anchors=[];
                  for(const a of raw){if(!a.end||min(a.end)<=min(a.start))continue;const same=anchors.find(x=>Math.abs(x.y-a.y)<10);if(!same)anchors.push(a);else if(a.explicit&&!same.explicit)Object.assign(same,a)}
                  anchors.sort((a,b)=>a.y-b.y);

                  const genericHeader=/emploi du temps|timetable|stundenplan|horario|orario|professeur|teacher|lehrer|enseignant|classe|class|klasse/i;
                  const contentLines=lines.filter(l=>l.cy>headerY+3&&!ocrDay80(l.text));
                  if(anchors.length<2){
                    const ys=contentLines.filter(l=>!genericHeader.test(l.text)).map(l=>l.cy);if(!ys.length)return {error:'Aucun cours exploitable détecté.'};
                    const available=(typeof slots!=='undefined'&&Array.isArray(slots)&&slots.length)?slots:[];if(!available.length)return {error:'Les horaires ne sont pas assez lisibles.'};
                    const lo=Math.min(...ys),hi=Math.max(...ys),step=(hi-lo)/Math.max(1,available.length-1);
                    anchors.splice(0,anchors.length,...available.map((s,i)=>({start:s.start,end:s.end,y:lo+i*step,explicit:false})));
                  }
                  const rowSpacing=typicalSpacing(anchors.map(x=>x.y));

                  const cells=new Map();
                  for(const line of contentLines){
                    if(ocrTimes80(line.text).length)continue;let txt=line.text.replace(/\\s+/g,' ').trim();if(!txt||genericHeader.test(txt)||/^[-–—|]+$/.test(txt))continue;
                    const dn=nearest80(dayPos,line.cx,x=>x.x),an=nearest80(anchors,line.cy,x=>x.y);if(!dn.item||!an.item)continue;
                    if(Number.isFinite(daySpacing)&&dn.dist>daySpacing*.72)continue;
                    if(Number.isFinite(rowSpacing)&&an.dist>rowSpacing*.62)continue;
                    const d=dn.item,a=an.item,key=d.d+'|'+a.start+'|'+a.end;if(!cells.has(key))cells.set(key,{day:d.d,start:a.start,end:a.end,parts:[],room:''});const cell=cells.get(key);
                    const rm=txt.match(/(?:salle|room|raum|aula)?\\s*([A-Za-z]?\\d{2,4}[A-Za-z]?)\\b/i);
                    if(rm&&(/salle|room|raum|aula/i.test(txt)||txt.trim()===rm[1])){cell.room=rm[1];txt=txt.replace(rm[0],'').trim()}
                    if(txt)cell.parts.push(txt);
                  }

                  const parsed={};days.forEach(d=>parsed[d]=[]);
                  for(const cell of cells.values()){
                    let label=[...new Set(cell.parts)].join(' · ').replace(/\\s*·\\s*·+/g,' · ').trim();if(!label)continue;if(label.length>80)label=label.slice(0,80);
                    let slot=0;try{if(typeof slotForTimes==='function')slot=slotForTimes(cell.start,cell.end)}catch(e){}
                    parsed[cell.day].push({start:cell.start,end:cell.end,label,room:cell.room,slot});
                  }
                  let count=0;for(const d of days){parsed[d].sort((a,b)=>min(a.start)-min(b.start));count+=parsed[d].length}
                  if(!count)return {error:'Le texte a été lu, mais aucun cours n’a pu être converti automatiquement.'};
                  return {parsed,count,days};
                }

                window.parseOcrSchedule=parseOcrSchedule80;try{parseOcrSchedule=parseOcrSchedule80}catch(e){}
                window.applyOcrSchedule=function(raw){
                  try{
                    const button=document.getElementById('importPhoto'),status=document.getElementById('importStatus');if(button){button.disabled=false;button.textContent='Importer une photo d’emploi du temps'}
                    const payload=typeof raw==='string'?JSON.parse(raw):raw,result=parseOcrSchedule80(payload);if(result.error){if(status)status.textContent='';alert(result.error);return}
                    const days=result.days||activeDays80();const summary=days.map(d=>(FULL[d]||String(d))+' : '+(result.parsed[d]||[]).length).join('\n');
                    if(confirm(result.count+' cours détectés pour la semaine '+activeWeek+'.\n\n'+summary+'\n\nRemplacer les jours détectés ? Tu pourras ensuite corriger chaque cours en appuyant dessus.')){
                      for(const d of days)if(result.parsed[d]&&result.parsed[d].length&&weeks[activeWeek]&&weeks[activeWeek][d])weeks[activeWeek][d].courses=result.parsed[d];
                      selected=days.find(d=>result.parsed[d]&&result.parsed[d].length)||selected;if(status)status.textContent=result.count+' cours importés. Vérifie les cases puis corrige si besoin.';save();
                    }else if(status)status.textContent='Import annulé.';
                  }catch(e){if(window.applyOcrError)window.applyOcrError('La photo a été lue mais la conversion a échoué.')}
                };
              }catch(e){console.log('OcrImport80Ui',e)}
            })();
            """;
    }

}
