package com.wokgui.schedulewidget;

/** Timetable-photo OCR parsing. */
final class ImportParserUi {
    private ImportParserUi() {}

    static String script() {
        StringBuilder out = new StringBuilder(64 * 1024);
        out.append(layer0()).append('\n');
        return out.toString();
    }

    private static String layer0() {
        return """
            (function(){
              try{
                if(window.__ocrImport81V1)return;window.__ocrImport81V1=true;

                function norm(s){return String(s||'').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[’']/g,'').replace(/\\s+/g,' ').trim()}
                function clamp01(v){return Math.max(0,Math.min(1,Number(v)||0))}
                function median(values){const v=values.filter(Number.isFinite).sort((a,b)=>a-b);if(!v.length)return NaN;const m=Math.floor(v.length/2);return v.length%2?v[m]:(v[m-1]+v[m])/2}
                function typicalSpacing(values){const s=values.filter(Number.isFinite).sort((a,b)=>a-b),d=[];for(let i=1;i<s.length;i++)if(s[i]-s[i-1]>2)d.push(s[i]-s[i-1]);return d.length?median(d):Infinity}
                function nearest81(arr,value,key){let best=null,dist=Infinity;for(const x of arr){const d=Math.abs(key(x)-value);if(d<dist){dist=d;best=x}}return {item:best,dist}}
                function orderIndex(d){return [2,3,4,5,6,7,1].indexOf(Number(d))}
                function activeDays81(){try{return Array.isArray(DAYS)&&DAYS.length?DAYS.slice():[2,3,4,5,6]}catch(e){return [2,3,4,5,6]}}
                function kmeans1d(values,k){
                  const v=values.filter(Number.isFinite).sort((a,b)=>a-b);if(!v.length||k<1)return [];
                  if(k===1)return [v[Math.floor(v.length/2)]];
                  let c=Array.from({length:k},(_,i)=>v[Math.round(i*(v.length-1)/(k-1))]);
                  for(let n=0;n<14;n++){
                    const groups=Array.from({length:k},()=>[]);
                    v.forEach(x=>{let bi=0,bd=Infinity;c.forEach((q,i)=>{const d=Math.abs(q-x);if(d<bd){bd=d;bi=i}});groups[bi].push(x)});
                    const next=c.map((old,i)=>groups[i].length?groups[i].reduce((a,b)=>a+b,0)/groups[i].length:old).sort((a,b)=>a-b);
                    if(next.every((x,i)=>Math.abs(x-c[i])<.35)){c=next;break}c=next;
                  }
                  return c;
                }
                function clusterCenters(values,tolerance){
                  const v=values.filter(Number.isFinite).sort((a,b)=>a-b);if(!v.length)return [];
                  const groups=[[v[0]]];for(let i=1;i<v.length;i++){const g=groups[groups.length-1],avg=g.reduce((a,b)=>a+b,0)/g.length;if(Math.abs(v[i]-avg)<=tolerance)g.push(v[i]);else groups.push([v[i]])}
                  return groups.map(g=>g.reduce((a,b)=>a+b,0)/g.length);
                }

                function ocrDay81(text){
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

                function ocrTimes81(text,allowBare){
                  let s=String(text||'')
                    .replace(/(\\d)[oO](?=\\d|\\b)/g,(m,a)=>a+'0')
                    .replace(/([:hH.])\\s*[oO](?=\\d|\\b)/g,(m,a)=>a+'0')
                    .replace(/(\\d{1,2})\\s*[hH]\\s*([0-5]\\d)/g,'$1:$2')
                    .replace(/(\\d{1,2})\\s*[hH](?!\\d)/g,'$1:00')
                    .replace(/\\b(\\d{1,2})[.](\\d{2})\\b/g,'$1:$2');
                  const out=[],re=/\\b([0-2]?\\d):([0-5]\\d)\\b/g;let m;
                  while((m=re.exec(s))){const h=Number(m[1]);if(h<24){const v=String(h).padStart(2,'0')+':'+m[2];if(!out.includes(v))out.push(v)}}
                  if(!out.length&&allowBare){
                    const bare=s.match(/^\\s*([0-2]?\\d)\\s*[-–—]\\s*([0-2]?\\d)\\s*$/);
                    if(bare){const a=Number(bare[1]),b=Number(bare[2]);if(a<24&&b<24&&b>a){out.push(String(a).padStart(2,'0')+':00',String(b).padStart(2,'0')+':00')}}
                  }
                  return out;
                }

                function fitColumns(headers,days){
                  let pts=headers.map(h=>({i:orderIndex(h.d),x:h.x})).filter(p=>p.i>=0&&Number.isFinite(p.x));
                  function fit(a){
                    const mi=a.reduce((s,p)=>s+p.i,0)/a.length,mx=a.reduce((s,p)=>s+p.x,0)/a.length;let num=0,den=0;
                    a.forEach(p=>{num+=(p.i-mi)*(p.x-mx);den+=(p.i-mi)*(p.i-mi)});if(!den)return null;const slope=num/den;if(!Number.isFinite(slope)||slope<=4)return null;return {slope,intercept:mx-slope*mi};
                  }
                  let f=fit(pts);if(!f)return null;
                  if(pts.length>=3){const keep=pts.filter(p=>Math.abs(p.x-(f.intercept+f.slope*p.i))<=Math.abs(f.slope)*.48);if(keep.length>=2){const better=fit(keep);if(better)f=better}}
                  return days.map(d=>({d,x:f.intercept+f.slope*orderIndex(d),y:0}));
                }

                function inferColumns(lines,headers,days,genericHeader){
                  const warnings=[];
                  let dayPos=null;
                  if(headers.length>=2)dayPos=fitColumns(headers,days);
                  const nonGrid=lines.filter(l=>!ocrDay81(l.text)&&!ocrTimes81(l.text,false).length&&!genericHeader.test(l.text));
                  const candidateX=nonGrid.map(l=>l.cx);
                  if(!dayPos&&headers.length===1){
                    const centers=kmeans1d(candidateX,days.length),h=headers[0];
                    let step=typicalSpacing(centers);
                    if(Number.isFinite(step)&&step>4){dayPos=days.map(d=>({d,x:h.x+(orderIndex(d)-orderIndex(h.d))*step,y:h.y}));warnings.push('dayColumnsInferred')}
                  }
                  if(!dayPos){
                    const centers=kmeans1d(candidateX,days.length);if(centers.length<days.length)return {error:'Je ne reconnais pas assez clairement les colonnes des jours.'};
                    dayPos=days.map((d,i)=>({d,x:centers[i],y:0}));warnings.push('dayColumnsInferred');
                  }else if(headers.length<Math.min(3,days.length))warnings.push('dayColumnsInferred');
                  return {dayPos,warnings};
                }

                function parseOcrSchedule81(payload){
                  const lines=(payload&&payload.lines||[]).map(x=>({...x,l:Number(x.l),t:Number(x.t),r:Number(x.r),b:Number(x.b),cx:(Number(x.l)+Number(x.r))/2,cy:(Number(x.t)+Number(x.b))/2,text:String(x.text||'').trim()})).filter(x=>x.text&&Number.isFinite(x.cx)&&Number.isFinite(x.cy));
                  if(!lines.length)return {error:'Aucun texte détecté sur la photo.'};
                  const days=activeDays81(),activeSet=new Set(days.map(Number));
                  const genericHeader=/emploi du temps|timetable|stundenplan|horario|orario|professeur|teacher|lehrer|enseignant|classe|class|klasse|annee scolaire|school year|schuljahr/i;
                  const widths=lines.map(l=>Math.max(1,l.r-l.l)),heights=lines.map(l=>Math.max(1,l.b-l.t));
                  const textHeight=median(heights)||18,minX=Math.min(...lines.map(l=>l.l)),maxX=Math.max(...lines.map(l=>l.r)),imageWidth=Math.max(1,maxX-minX);

                  const headers=[];for(const line of lines){const d=ocrDay81(line.text);if(d&&activeSet.has(d))headers.push({d,x:line.cx,y:line.cy})}
                  const byDay=new Map();for(const h of headers){const old=byDay.get(h.d);if(!old||h.y<old.y)byDay.set(h.d,h)}
                  const uniqueHeaders=[...byDay.values()];
                  const inferred=inferColumns(lines,uniqueHeaders,days,genericHeader);if(inferred.error)return inferred;
                  const dayPos=inferred.dayPos,warnings=[...inferred.warnings];
                  const daySpacing=typicalSpacing(dayPos.map(x=>x.x));
                  const headerY=uniqueHeaders.length?median(uniqueHeaders.map(h=>h.y)):Math.min(...lines.map(l=>l.cy))-4;

                  let raw=[];
                  const bareTimeEdge=minX+imageWidth*.28;
                  for(const line of lines){
                    const ts=ocrTimes81(line.text,line.cx<=bareTimeEdge);if(!ts.length)continue;
                    raw.push({start:ts[0],end:ts[1]||'',y:line.cy,explicit:ts.length>1});
                  }
                  raw.sort((a,b)=>a.y-b.y);
                  for(let i=0;i<raw.length;i++)if(!raw[i].end){
                    const existing=(typeof slots!=='undefined'&&Array.isArray(slots))?slots.find(s=>s.start===raw[i].start):null;
                    const start=min(raw[i].start),next=raw.slice(i+1).find(x=>min(x.start)>start&&min(x.start)-start<=180);
                    raw[i].end=existing?existing.end:(next?next.start:clock(Math.min(23*60+59,start+60)));
                  }
                  const anchors=[],anchorMerge=Math.max(9,textHeight*.7);
                  for(const a of raw){if(!a.end||min(a.end)<=min(a.start))continue;const same=anchors.find(x=>Math.abs(x.y-a.y)<anchorMerge);if(!same)anchors.push(a);else if(a.explicit&&!same.explicit)Object.assign(same,a)}
                  anchors.sort((a,b)=>a.y-b.y);
                  let usedFallbackTimes=false;

                  const contentLines=lines.filter(l=>l.cy>headerY+Math.max(3,textHeight*.15)&&!ocrDay81(l.text));
                  if(anchors.length<2){
                    const candidates=contentLines.filter(l=>!genericHeader.test(l.text)&&!ocrTimes81(l.text,false).length),ys=candidates.map(l=>l.cy);
                    if(!ys.length)return {error:'Aucun cours exploitable détecté.'};
                    const available=(typeof slots!=='undefined'&&Array.isArray(slots)&&slots.length)?slots:[];if(!available.length)return {error:'Les horaires ne sont pas assez lisibles.'};
                    const rows=clusterCenters(ys,Math.max(12,textHeight*1.55));if(!rows.length)return {error:'Les lignes de cours ne sont pas assez lisibles.'};
                    const gaps=[];for(let i=1;i<rows.length;i++)gaps.push(rows[i]-rows[i-1]);let baseGap=median(gaps.filter(g=>g>textHeight*1.2));if(!Number.isFinite(baseGap)||baseGap<=0)baseGap=Math.max(textHeight*3,60);
                    let idx=Math.max(0,Math.round((rows[0]-headerY)/baseGap)-1),prev=rows[0];
                    const mapped=[];for(let i=0;i<rows.length;i++){if(i>0)idx+=Math.max(1,Math.round((rows[i]-prev)/baseGap));if(idx>=available.length)break;const s=available[idx];mapped.push({start:s.start,end:s.end,y:rows[i],explicit:false});prev=rows[i]}
                    if(!mapped.length)return {error:'Les horaires ne sont pas assez lisibles.'};anchors.splice(0,anchors.length,...mapped);usedFallbackTimes=true;warnings.push('timesInferred');
                  }
                  const rowSpacing=typicalSpacing(anchors.map(x=>x.y));
                  const minDayX=Math.min(...dayPos.map(x=>x.x)),maxDayX=Math.max(...dayPos.map(x=>x.x));
                  const minRowY=Math.min(...anchors.map(x=>x.y)),maxRowY=Math.max(...anchors.map(x=>x.y));

                  const cells=new Map();let wideSkipped=0,weakParts=0,acceptedParts=0;
                  for(const line of contentLines){
                    if(ocrTimes81(line.text,false).length)continue;let txt=line.text.replace(/\\s+/g,' ').trim();if(!txt||genericHeader.test(txt)||/^[-–—|]+$/.test(txt))continue;
                    const lineWidth=Math.max(1,line.r-line.l);
                    if(Number.isFinite(daySpacing)&&lineWidth>daySpacing*1.28){wideSkipped++;continue}
                    if(Number.isFinite(daySpacing)&&(line.cx<minDayX-daySpacing*.62||line.cx>maxDayX+daySpacing*.62))continue;
                    if(Number.isFinite(rowSpacing)&&(line.cy<minRowY-rowSpacing*.72||line.cy>maxRowY+rowSpacing*.72))continue;
                    const dn=nearest81(dayPos,line.cx,x=>x.x),an=nearest81(anchors,line.cy,x=>x.y);if(!dn.item||!an.item)continue;
                    if(Number.isFinite(daySpacing)&&dn.dist>daySpacing*.64)continue;
                    if(Number.isFinite(rowSpacing)&&an.dist>rowSpacing*.72)continue;
                    const dx=Number.isFinite(daySpacing)?dn.dist/Math.max(1,daySpacing*.5):.45,dy=Number.isFinite(rowSpacing)?an.dist/Math.max(1,rowSpacing*.5):.45;
                    let confidence=clamp01(1-dx*.30-dy*.42-(usedFallbackTimes?.18:0)-(uniqueHeaders.length<2?.10:0));if(confidence<.58)weakParts++;acceptedParts++;
                    const d=dn.item,a=an.item,key=d.d+'|'+a.start+'|'+a.end;if(!cells.has(key))cells.set(key,{day:d.d,start:a.start,end:a.end,parts:[],room:'',scores:[]});const cell=cells.get(key);
                    const rm=txt.match(/(?:salle|room|raum|aula)\\s*[:#-]?\\s*([A-Za-z]?\\d{1,4}[A-Za-z]?)\\b/i);
                    if(rm){cell.room=rm[1];txt=txt.replace(rm[0],'').trim()}
                    else if(/^([A-Za-z]?\\d{2,4}[A-Za-z]?)$/.test(txt)&&lineWidth<Math.max(80,daySpacing*.45)){cell.room=txt;txt=''}
                    if(txt){cell.parts.push(txt);cell.scores.push(confidence)}
                  }
                  if(wideSkipped)warnings.push('wideLinesSkipped');

                  const parsed={};days.forEach(d=>parsed[d]=[]);let uncertainCells=0;
                  for(const cell of cells.values()){
                    let label=[...new Set(cell.parts.map(x=>x.trim()).filter(Boolean))].join(' · ').replace(/\\s*·\\s*·+/g,' · ').trim();if(!label)continue;if(label.length>80)label=label.slice(0,80);
                    let slot=0;try{if(typeof slotForTimes==='function')slot=slotForTimes(cell.start,cell.end)}catch(e){}
                    const confidence=cell.scores.length?cell.scores.reduce((a,b)=>a+b,0)/cell.scores.length:.55,uncertain=confidence<.64;if(uncertain)uncertainCells++;
                    parsed[cell.day].push({start:cell.start,end:cell.end,label,room:cell.room,slot,confidence:Number(confidence.toFixed(2)),uncertain});
                  }
                  let count=0;for(const d of days){parsed[d].sort((a,b)=>min(a.start)-min(b.start));count+=parsed[d].length}
                  if(!count)return {error:'Le texte a été lu, mais aucun cours n’a pu être converti automatiquement.'};
                  if(uncertainCells)warnings.push('uncertainCells');

                  let score=1;
                  if(uniqueHeaders.length===0)score-=.22;else if(uniqueHeaders.length===1)score-=.14;else if(uniqueHeaders.length<Math.min(4,days.length))score-=.06;
                  if(usedFallbackTimes)score-=.24;if(wideSkipped)score-=Math.min(.12,wideSkipped*.03);if(acceptedParts)score-=Math.min(.16,(weakParts/acceptedParts)*.18);score=clamp01(score);
                  const quality=score>=.78?'high':(score>=.58?'medium':'low');
                  return {parsed,count,days,quality,qualityScore:Number(score.toFixed(2)),warningCodes:[...new Set(warnings)],diagnostics:{headers:uniqueHeaders.length,timeAnchors:anchors.length,wideSkipped,uncertainCells}};
                }

                window.parseOcrSchedule=parseOcrSchedule81;try{parseOcrSchedule=parseOcrSchedule81}catch(e){}
                window.applyOcrSchedule=function(raw){
                  try{
                    const button=document.getElementById('importPhoto'),status=document.getElementById('importStatus');if(button){button.disabled=false;button.textContent='Importer une photo d’emploi du temps'}
                    const payload=typeof raw==='string'?JSON.parse(raw):raw,result=parseOcrSchedule81(payload);if(result.error){if(status)status.textContent='';alert(result.error);return}
                    if(window.openTimetableImportReview)window.openTimetableImportReview(result);
                  }catch(e){if(window.applyOcrError)window.applyOcrError('La photo a été lue mais la conversion a échoué.')}
                };
              }catch(e){console.log('OcrImport81Ui',e)}
            })();
            """;
    }
}
