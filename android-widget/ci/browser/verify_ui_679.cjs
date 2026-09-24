const {chromium}=require('playwright');
const fs=require('fs');
const path=require('path');
const assert=require('assert/strict');
const {pathToFileURL}=require('url');
const root=path.resolve(__dirname,'../..');
const chunks=path.resolve(process.env.EDT_UI_CHUNKS||'smoke-browser/chunks');
const baseline=process.env.EDT_BASELINE==='1';
(async()=>{
  const browser=await chromium.launch({headless:true});
  const context=await browser.newContext({viewport:{width:412,height:915},isMobile:true,hasTouch:true});
  const page=await context.newPage(),errors=[];
  page.on('pageerror',error=>errors.push(String(error)));
  page.on('console',message=>{if(message.type()==='error')errors.push(message.text())});
  await page.addInitScript(()=>{
    const m={observers:0,activeObservers:0,timers:0,activeTimers:0,intervals:0,listeners:0,rafs:0};
    window.__uiMetrics679=m;
    const NativeObserver=window.MutationObserver;
    window.MutationObserver=class extends NativeObserver{
      constructor(callback){super(callback);this.active=false;m.observers++}
      observe(...args){if(!this.active){this.active=true;m.activeObservers++}return super.observe(...args)}
      disconnect(){if(this.active){this.active=false;m.activeObservers--}return super.disconnect()}
    };
    const timer=window.setTimeout.bind(window),clear=window.clearTimeout.bind(window),pending=new Set();
    window.setTimeout=(fn,ms,...args)=>{m.timers++;const id=timer(()=>{pending.delete(id);m.activeTimers=pending.size;fn(...args)},ms);pending.add(id);m.activeTimers=pending.size;return id};
    window.clearTimeout=id=>{pending.delete(id);m.activeTimers=pending.size;return clear(id)};
    const interval=window.setInterval.bind(window),raf=window.requestAnimationFrame.bind(window),add=EventTarget.prototype.addEventListener;
    window.setInterval=(...args)=>{m.intervals++;return interval(...args)};
    window.requestAnimationFrame=fn=>{m.rafs++;return raf(fn)};
    EventTarget.prototype.addEventListener=function(...args){m.listeners++;return add.apply(this,args)};
    const data={AdvancedSettings:'{"cycleLength":2,"singleWeek":false,"showLunchWeek":true,"showBreaksWeek":true}',UiSettings:'{"language":"fr","theme":"blue"}',currentProfile:'main'};
    window.__uiData679=data;window.confirm=()=>true;window.alert=()=>{};
    window.AndroidSchedule=new Proxy({},{get(target,key){if(typeof key!=='string')return;return(...args)=>{
      if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
      if(key==='setCurrentWeek'){data.CurrentWeek=args[0];return true}
      if(key==='loadSchedule')return data.Schedule||'';
      if(key==='loadUiSettings')return data.UiSettings;
      if(key==='loadAdvancedSettings')return data.AdvancedSettings;
      if(key==='listProfiles')return JSON.stringify({current:data.currentProfile,profiles:[{id:'main',name:'Principal'},{id:'second',name:'Secondaire'}]});
      if(key==='activateProfile'){data.currentProfile=args[0];return true}
      if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks'||key==='loadEffectiveCourses')return '[]';
      return data[key.replace(/^load/,'')]||'{}';
    }}});
  });
  await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
  for(const name of fs.readdirSync(chunks).sort()){
    await page.evaluate(fs.readFileSync(path.join(chunks,name),'utf8')+'\n//# sourceURL='+name);
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__edtHeavyPanels648&&document.getElementById('todayList').children.length>0);
  const report={baseline,initial:await page.evaluate(()=>({...__uiMetrics679}))};
  report.navigation=await page.evaluate(async()=>{
    const counts={today:0,week:0,edit:0};
    for(const [name,kind] of [['renderToday','today'],['renderWeek','week'],['renderEdit','edit']]){
      const old=window[name],wrapped=function(){counts[kind]++;return old.apply(this,arguments)};
      window[name]=wrapped;try{eval(name+'=wrapped')}catch(e){}
    }
    const active=[];
    for(const kind of ['week','today','edit','week','today']){
      document.querySelector('.nav[data-mode="'+kind+'"]').click();
      await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
      active.push([...document.querySelectorAll('main.wrap>.view.active')].map(x=>x.id));
    }
    return {counts,active,staleToday:document.querySelectorAll('#viewToday .week658Lunch,#viewToday .weekSwapCover669').length};
  });
  await page.screenshot({path:path.resolve('smoke-browser/ui-679-today.png')});
  await page.evaluate(()=>document.querySelector('.nav[data-mode="edit"]').click());
  await page.screenshot({path:path.resolve('smoke-browser/ui-679-edit.png')});
  await page.evaluate(()=>document.querySelector('.nav[data-mode="today"]').click());
  report.advanced=await page.evaluate(async()=>{
    document.getElementById('settingsBtn').click();
    const details=document.getElementById('advancedSettings85'),summary=details.querySelector('summary'),times=[],before={...__uiMetrics679};
    for(let i=0;i<5;i++){
      if(details.open)summary.click();
      const start=performance.now();summary.click();
      await new Promise(resolve=>requestAnimationFrame(resolve));times.push(performance.now()-start);
      summary.click();
      await new Promise(resolve=>requestAnimationFrame(resolve));
    }
    return {openMs:times,delta:Object.fromEntries(Object.keys(before).map(k=>[k,__uiMetrics679[k]-before[k]])),
      profile:document.getElementById('advProfileSelect')?.value||null};
  });
  await page.evaluate(()=>{const details=document.getElementById('advancedSettings85');details.open=true;document.getElementById('advProfilesTitle')?.scrollIntoView()});
  await page.screenshot({path:path.resolve('smoke-browser/ui-679-profiles.png')});
  report.profileGeometry=await page.evaluate(()=>{
    const title=document.getElementById('advProfilesTitle')?.getBoundingClientRect();
    const select=document.getElementById('advProfileSelect')?.getBoundingClientRect();
    return {titleCenter:title?(title.left+title.right)/2:null,selectCenter:select?(select.left+select.right)/2:null};
  });
  await page.evaluate(()=>document.getElementById('advRangeStart')?.scrollIntoView());
  await page.screenshot({path:path.resolve('smoke-browser/ui-679-dates.png')});
  await page.evaluate(()=>{document.getElementById('advancedSettings85').open=false;document.getElementById('settingsSheet').scrollTop=0});
  await page.screenshot({path:path.resolve('smoke-browser/ui-679-settings.png')});
  report.settingsLayout=await page.evaluate(()=>{
    const expected=['languageSettings86','textSettings86','weekTypeSettings86','colorSettings86','breakSettings86','widgetSettings86','advancedSettings85'];
    const sheet=document.getElementById('settingsSheet');
    const order=[...sheet.children].filter(x=>expected.includes(x.id)).map(x=>x.id);
    const collapsed=expected.every(id=>{const el=document.getElementById(id);return el&&el.tagName==='DETAILS'&&!el.open});
    const language=document.getElementById('languageSettings86'),select=document.getElementById('languageSelect'),download=document.getElementById('languageDownloadBtn81');
    if(language)language.open=true;
    const languageWidth=select?.getBoundingClientRect().width||0;
    if(language)language.open=false;
    const parents={
      language:select?.closest('.settingsSection86')?.id||null,
      download:download?.closest('.settingsSection86')?.id||null,
      week:document.getElementById('settingsWeekCycle678')?.closest('.settingsSection86')?.id||null,
      weekColors:document.getElementById('week658Settings')?.closest('.settingsSection86')?.id||null,
      breaks:document.getElementById('breakDisplaySetting')?.closest('.settingsSection86')?.id||null,
      widget:document.getElementById('advWidgetTitle')?.closest('.settingsSection86')?.id||null,
      density:document.getElementById('widgetDensity664')?.closest('.settingsSection86')?.id||null
    };
    const widget=document.getElementById('widgetSettings86');widget.open=true;
    const selects=[...widget.querySelectorAll('select')].filter(x=>getComputedStyle(x).display!=='none').map(x=>({id:x.id,h:x.getBoundingClientRect().height,font:parseFloat(getComputedStyle(x).fontSize)}));
    widget.open=false;
    const summaries=expected.map(id=>({id,font:parseFloat(getComputedStyle(document.querySelector('#'+id+'>summary')).fontSize)}));
    return {expected,order,collapsed,languageWidth,parents,legacyLanguageExtra:!!document.getElementById('languageExtra85'),selects,summaries};
  });
  report.calendarNavigation=await page.evaluate(async()=>{
    document.getElementById('settingsDone')?.click();
    document.querySelector('.nav[data-mode="today"]').click();
    await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
    const todayBefore={title:document.getElementById('todayTitle').textContent,date:document.getElementById('todayDate').textContent};
    document.getElementById('todayNext757').click();await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
    const todayNext={title:document.getElementById('todayTitle').textContent,date:document.getElementById('todayDate').textContent};
    document.getElementById('todayPrev757').click();await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
    document.querySelector('.nav[data-mode="week"]').click();await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
    const weekBefore={title:document.querySelector('#viewWeek .weekTop h2').childNodes[0]?.textContent||document.querySelector('#viewWeek .weekTop h2').textContent,cycle:document.getElementById('weekCycleLabel757').textContent,dates:[...document.querySelectorAll('#weekGrid .weekDayDate757')].map(x=>x.textContent)};
    document.getElementById('weekNext757').click();await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
    const weekNext={title:document.querySelector('#viewWeek .weekTop h2').childNodes[0]?.textContent||document.querySelector('#viewWeek .weekTop h2').textContent,cycle:document.getElementById('weekCycleLabel757').textContent,dates:[...document.querySelectorAll('#weekGrid .weekDayDate757')].map(x=>x.textContent)};
    document.getElementById('weekPrev757').click();await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
    return {todayBefore,todayNext,weekBefore,weekNext,arrows:['todayPrev757','todayNext757','weekPrev757','weekNext757'].every(id=>!!document.getElementById(id))};
  });
  report.breaks=await page.evaluate(async()=>{
    document.getElementById('settingsDone')?.click();document.querySelector('.nav[data-mode="week"]').click();
    const grid=document.getElementById('weekGrid');
    const state=()=>({lunch:grid.querySelectorAll('.week658Lunch').length,gap:grid.querySelectorAll('.week658Gap').length,
      labels:grid.querySelectorAll('.week658LunchLabel,.week662GapLabel').length,lines:grid.classList.contains('week662LunchLines')});
    const initial=state(),change=(id,value)=>{const input=document.getElementById(id);input.checked=value;input.dispatchEvent(new Event('change',{bubbles:true}))};
    change('feedback663AppLunch',false);change('feedback663AppGaps',false);
    await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
    const disabled=state();
    change('feedback663AppLunch',true);change('feedback663AppGaps',true);
    await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
    return {initial,disabled,restored:state()};
  });
  await page.screenshot({path:path.resolve('smoke-browser/ui-679-week.png')});
  report.weekSwap=await page.evaluate(async()=>{
    const grid=document.getElementById('weekGrid'),tab=letter=>document.querySelector('.weekTab[data-week="'+letter+'"]');
    const signature=()=>grid.innerHTML;
    let changedAfterFrame=0,covers=0,firstChange=null;
    for(let i=0;i<20;i++){
      tab(i%2?'A':'B').click();
      const immediately=signature();
      await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
      const settled=signature();
      if(settled!==immediately){changedAfterFrame++;if(!firstChange){let at=0;while(at<Math.min(immediately.length,settled.length)&&immediately[at]===settled[at])at++;firstChange={iteration:i,at,before:immediately.slice(at,at+160),after:settled.slice(at,at+160)}}}
      covers+=document.querySelectorAll('.weekSwapCover669,.editSwapCover673').length;
    }
    return {changedAfterFrame,covers,firstChange};
  });
  report.editSwap=await page.evaluate(async()=>{
    document.querySelector('.nav[data-mode="edit"]').click();
    const list=document.getElementById('editList'),tabs=document.getElementById('weekTabs');
    let changedAfterFrame=0,covers=0;
    for(let i=0;i<20;i++){
      tabs.querySelector('.weekTab[data-week="'+(i%2?'A':'B')+'"]').click();
      const immediate=list.innerHTML;
      await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
      if(list.innerHTML!==immediate)changedAfterFrame++;
      covers+=document.querySelectorAll('.editSwapCover673').length;
    }
    return {changedAfterFrame,covers};
  });
  report.profileAndCycle=await page.evaluate(async()=>{
    document.getElementById('settingsBtn').click();
    const profile=document.getElementById('advProfileSelect');profile.value='second';profile.dispatchEvent(new Event('change',{bubbles:true}));
    await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
    const selected=window.__uiData679.currentProfile;
    document.querySelector('#settingsWeekCycle678 [data-count="3"]').click();
    await new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
    const added={count:JSON.parse(__uiData679.AdvancedSettings).cycleLength,choices:[...document.querySelectorAll('#settingsWeekCycle678 .weekCurrentChoice678')].filter(x=>x.style.display!=='none').map(x=>x.dataset.week)};
    document.querySelector('#settingsWeekCycle678 [data-count="2"]').click();
    return {selected,added};
  });
  report.final=await page.evaluate(()=>({...__uiMetrics679}));report.errors=errors;
  const output=path.resolve(process.env.EDT_UI_REPORT||'smoke-browser/ui-679-report.json');
  fs.writeFileSync(output,JSON.stringify(report,null,2));console.log(JSON.stringify(report,null,2));
  if(!baseline){assert.deepEqual(errors,[]);assert.ok(report.navigation.active.every(x=>x.length===1));
    assert.equal(report.navigation.staleToday,0);
    assert.deepEqual(report.settingsLayout.order,report.settingsLayout.expected);assert.equal(report.settingsLayout.collapsed,true);
    assert.equal(report.settingsLayout.legacyLanguageExtra,false);assert.ok(report.settingsLayout.languageWidth>0&&report.settingsLayout.languageWidth<220);
    assert.deepEqual(report.settingsLayout.parents,{language:'languageSettings86',download:'languageSettings86',week:'weekTypeSettings86',weekColors:'colorSettings86',breaks:'breakSettings86',widget:'widgetSettings86',density:'widgetSettings86'});
    assert.ok(report.settingsLayout.summaries.every(x=>Math.abs(x.font-report.settingsLayout.summaries[0].font)<0.1));
    assert.ok(report.settingsLayout.selects.every(x=>x.h<=35&&x.font<=12));
    assert.equal(report.calendarNavigation.arrows,true);assert.notEqual(report.calendarNavigation.todayBefore.date,report.calendarNavigation.todayNext.date);
    assert.match(report.calendarNavigation.weekBefore.title,/Semaine du [0-9]{2}\/[0-9]{2} au [0-9]{2}\/[0-9]{2}/);
    assert.notEqual(report.calendarNavigation.weekBefore.title,report.calendarNavigation.weekNext.title);assert.ok(report.calendarNavigation.weekBefore.dates.length>=5);assert.ok(report.calendarNavigation.weekBefore.dates.every(x=>/^[0-9]{2}\/[0-9]{2}$/.test(x)));
    assert.equal(report.breaks.disabled.lunch,0);assert.equal(report.breaks.disabled.gap,0);assert.equal(report.breaks.disabled.labels,0);
    assert.equal(report.breaks.disabled.lines,false);assert.ok(report.breaks.restored.lunch>0);assert.ok(report.breaks.restored.gap>0);
    assert.ok(Math.abs(report.profileGeometry.titleCenter-report.profileGeometry.selectCenter)<1);
    assert.equal(report.weekSwap.covers,0);assert.equal(report.weekSwap.changedAfterFrame,0);
    assert.equal(report.editSwap.covers,0);assert.equal(report.editSwap.changedAfterFrame,0);
    assert.equal(report.profileAndCycle.selected,'second');assert.equal(report.profileAndCycle.added.count,3);
    assert.deepEqual(report.profileAndCycle.added.choices,['A','B','C'])}
  await browser.close();
})().catch(error=>{console.error(error);process.exitCode=1});
