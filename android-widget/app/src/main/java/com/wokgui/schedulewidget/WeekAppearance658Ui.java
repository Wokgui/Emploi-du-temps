package com.wokgui.schedulewidget;

/** 6.58 week-view visual hierarchy and per-day lunch configuration. */
final class WeekAppearance658Ui {
    private WeekAppearance658Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__weekAppearance658){window.refreshWeekAppearance658&&window.refreshWeekAppearance658();return}
                window.__weekAppearance658=true;
                const KEY='weekAppearance658';
                const DEF={free:'#E6F2FF',course:'#FFFFFF',lunch:'#FFE08A',days:{}};
                const DAYN=['','Dim','Lun','Mar','Mer','Jeu','Ven','Sam'];
                let queued=false;
                function load(){try{const x=JSON.parse(localStorage.getItem(KEY)||'{}');return Object.assign({},DEF,x,{days:Object.assign({},DEF.days,x.days||{})})}catch(e){return JSON.parse(JSON.stringify(DEF))}}
                function save(s){localStorage.setItem(KEY,JSON.stringify(s));schedule()}
                function lang(){try{return String((JSON.parse(AndroidSchedule.loadUiSettings()||'{}').language||'fr'))}catch(e){return 'fr'}}
                function tr(fr,en,de){return lang()==='en'?en:(lang()==='de'?de:fr)}
                function toMin(v){const m=String(v||'').match(/([0-9]{1,2}):([0-9]{2})/);return m?+m[1]*60 + +m[2]:-1}
                function rowsOf(grid){const rows=[],heads=[...grid.querySelectorAll(':scope > .wh.day')],times=[...grid.querySelectorAll(':scope > .wh.timecol')],n=heads.length||5;for(const time of times){const f=(time.textContent||'').match(/[0-2]?[0-9]:[0-5][0-9]/g)||[];if(f.length<2)continue;const cells=[];let x=time.nextElementSibling;while(x&&cells.length<n){if(x.classList&&x.classList.contains('wc'))cells.push(x);x=x.nextElementSibling}if(cells.length===n)rows.push({time,start:toMin(f[0]),end:toMin(f[1]),cells})}rows.sort((a,b)=>a.time.offsetTop-b.time.offsetTop);return {rows,heads}}
                function dayKey(head,i){const t=String(head&&head.textContent||'').toLowerCase();if(/lun|mon|mo\b/.test(t))return 2;if(/mar|tue|di\b/.test(t))return 3;if(/mer|wed|mi\b/.test(t))return 4;if(/jeu|thu|do\b/.test(t))return 5;if(/ven|fri|fr\b/.test(t))return 6;if(/sam|sat|sa\b/.test(t))return 7;if(/dim|sun|so\b/.test(t))return 1;return [2,3,4,5,6,7,1][i]||i+2}
                function nativeLunch(c){return c.classList.contains('lunchCell')||c.classList.contains('dynamicLunchCell')||c.classList.contains('nativeLunchCell')||c.classList.contains('finalLunchCell')||c.classList.contains('lunch655Synthetic')}
                function cleanCell(c){c.classList.remove('week658Course','week658Free','week658Lunch','week658LunchTop','week658LunchBottom');c.querySelectorAll(':scope > .week658LunchLabel').forEach(x=>x.remove())}
                function paint(){const grid=document.getElementById('weekGrid');if(!grid)return;const s=load(),data=rowsOf(grid);if(!data.rows.length)return;grid.style.setProperty('--week658-free',s.free);grid.style.setProperty('--week658-course',s.course);grid.style.setProperty('--week658-lunch',s.lunch);data.rows.forEach(r=>r.cells.forEach(c=>{cleanCell(c);const txt=String(c.textContent||'').replace(/Midi|Lunch|Mittag/g,'').trim();if(txt&&!nativeLunch(c))c.classList.add('week658Course');else c.classList.add('week658Free')}));data.heads.forEach((h,di)=>{const d=dayKey(h,di),cfg=s.days[d]||{enabled:true,start:720};if(cfg.enabled===false)return;const start=Number(cfg.start||720),end=start+60;const band=data.rows.filter(r=>r.start<end&&r.end>start);if(!band.length)return;band.forEach((r,ri)=>{const c=r.cells[di];if(!c)return;c.classList.remove('week658Course','week658Free');c.classList.add('week658Lunch');if(ri===0)c.classList.add('week658LunchTop');if(ri===band.length-1)c.classList.add('week658LunchBottom');c.querySelectorAll('.lunch655Label,.week658LunchLabel').forEach(x=>x.remove());[...c.childNodes].filter(n=>n.nodeType===3&&/Midi|Lunch|Mittag/.test(n.textContent||'')).forEach(n=>n.remove())});const first=band[0].cells[di];if(first){const lab=document.createElement('span');lab.className='week658LunchLabel';lab.textContent=tr('Midi','Lunch','Mittag');first.appendChild(lab)}})}
                const style=document.createElement('style');style.id='weekAppearance658Style';style.textContent=`
                  #weekGrid .wc.week658Free{background:var(--week658-free,#E6F2FF)!important;color:#53627a!important;box-shadow:none!important}
                  #weekGrid .wc.week658Course{background:var(--week658-course,#fff)!important}
                  #weekGrid .wc.week658Lunch{position:relative;background:var(--week658-lunch,#FFE08A)!important;color:#59491d!important;box-shadow:none!important;border-radius:0!important;overflow:hidden!important}
                  #weekGrid .wc.week658Lunch:before,#weekGrid .wc.week658Lunch:after,#weekGrid .wc.week658Lunch .lunch655Label{content:none!important;display:none!important}
                  #weekGrid .week658LunchLabel{position:absolute!important;inset:0!important;display:flex!important;align-items:center!important;justify-content:center!important;width:auto!important;height:auto!important;margin:0!important;padding:0!important;font-weight:800!important;font-size:.88em!important;line-height:1!important;text-align:center!important;white-space:nowrap!important}
                  #weekGrid .wc.week658LunchTop{border-top:1px solid #d5b84d!important}
                  #weekGrid .wc.week658LunchBottom{border-bottom:1px solid #d5b84d!important}
                  #weekGrid .lunch653Top,#weekGrid .lunch653Bottom{border-bottom-color:transparent!important}
                  #week658Settings{margin:8px 0 0!important;padding:10px!important;border:1px solid #dbe3ef!important;border-radius:10px!important;background:#f8fafc!important;position:static!important;inset:auto!important;width:auto!important;height:auto!important;max-width:none!important;box-shadow:none!important;transform:none!important;z-index:auto!important}
                  #week658Settings .w658Title{text-align:left;font-weight:850;margin:4px 0 9px}
                  #week658Settings .w658Colors{display:grid;grid-template-columns:1fr 52px;gap:7px 10px;align-items:center}
                  #week658Settings input[type=color]{width:48px;height:34px;padding:2px;border:1px solid #ccd5e2;border-radius:7px;background:#fff}
                  #week658Settings .w658Days{margin-top:12px;display:grid;gap:6px}
                  #week658Settings .w658Day{display:grid;grid-template-columns:42px minmax(110px,1fr) 108px;gap:7px;align-items:center}
                  #week658Settings .w658Day select{min-height:34px;border:1px solid #ccd5e2;border-radius:7px;background:#fff;padding:3px}
                  #week658Settings .w658Day label{display:flex;align-items:center;gap:5px;font-size:.78rem}
                  @media(max-width:380px){#week658Settings{padding:8px!important}#week658Settings .w658Day{grid-template-columns:34px minmax(0,1fr) 82px;gap:4px}#week658Settings .w658Day label{font-size:.72rem}#week658Settings .w658Day select{font-size:.72rem;padding:2px}}
                `;document.head.appendChild(style);
                function settingsHost(){const sheet=document.getElementById('settingsSheet');if(!sheet)return null;return sheet.querySelector('#colorSettings86 .settingsSectionBody86')||sheet}
                function installSettings(){const host=settingsHost();if(!host)return;let box=document.getElementById('week658Settings');if(box){if(box.parentNode!==host)host.appendChild(box);return}const s=load();box=document.createElement('section');box.id='week658Settings';box.className='settingBox';box.innerHTML='<div class="w658Title">'+tr('Couleurs de la vue semaine','Week view colours','Farben der Wochenansicht')+'</div><div class="w658Colors"><span>'+tr('Cases libres','Free cells','Freie Felder')+'</span><input id="w658Free" type="color"><span>'+tr('Cours','Classes','Unterricht')+'</span><input id="w658Course" type="color"><span>'+tr('Midi','Lunch','Mittag')+'</span><input id="w658Lunch" type="color"></div><div class="w658Days"><div class="w658Title">'+tr('Midi par jour','Lunch by day','Mittag pro Tag')+'</div></div>';const actions=document.querySelector('#settingsSheet .settingsActions');if(host.id==='settingsSheet'&&actions)host.insertBefore(box,actions);else host.appendChild(box);box.querySelector('#w658Free').value=s.free;box.querySelector('#w658Course').value=s.course;box.querySelector('#w658Lunch').value=s.lunch;[['w658Free','free'],['w658Course','course'],['w658Lunch','lunch']].forEach(([id,k])=>box.querySelector('#'+id).addEventListener('input',e=>{const x=load();x[k]=e.target.value;save(x)}));const days=box.querySelector('.w658Days');[2,3,4,5,6,7,1].forEach(d=>{if(!document.documentElement.classList.contains('weekendScheduleEnabled')&&(d===7||d===1))return;const cfg=s.days[d]||{enabled:true,start:720},row=document.createElement('div');row.className='w658Day';row.innerHTML='<b>'+DAYN[d]+'</b><label><input type="checkbox" '+(cfg.enabled===false?'':'checked')+'> '+tr('Midi','Lunch','Mittag')+'</label><select><option value="720">12 h – 13 h</option><option value="780">13 h – 14 h</option></select>';row.querySelector('select').value=String(cfg.start||720);row.querySelector('input').onchange=e=>{const x=load();x.days[d]=Object.assign({},x.days[d]||{start:720},{enabled:e.target.checked});save(x)};row.querySelector('select').onchange=e=>{const x=load();x.days[d]=Object.assign({},x.days[d]||{enabled:true},{start:+e.target.value});save(x)};days.appendChild(row)})}
                function refresh(){queued=false;installSettings();paint()}
                function schedule(){if(queued)return;queued=true;requestAnimationFrame(refresh)}
                window.refreshWeekAppearance658=refresh;const grid=document.getElementById('weekGrid');if(grid)new MutationObserver(schedule).observe(grid,{childList:true,subtree:false});const modal=document.getElementById('settingsModal');if(modal)new MutationObserver(()=>{if(modal.classList.contains('show'))schedule()}).observe(modal,{attributes:true,attributeFilter:['class']});refresh();
              }catch(e){console.log('WeekAppearance658Ui',e)}
            })();
            """;
    }
}
