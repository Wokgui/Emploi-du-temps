const {chromium}=require('playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const {pathToFileURL}=require('node:url');

const chunks=path.resolve(process.env.EDT_UI_CHUNKS||'smoke-browser/chunks');
const out=path.resolve(process.env.EDT_BROWSER_OUTPUT||'smoke-browser');
const asset=path.resolve(__dirname,'../../app/src/main/assets/index.html');
fs.mkdirSync(out,{recursive:true});

(async()=>{
  const browser=await chromium.launch({headless:true});
  const context=await browser.newContext({viewport:{width:412,height:915},isMobile:true,hasTouch:true});
  const page=await context.newPage();
  const errors=[],logs=[];
  page.on('pageerror',e=>errors.push(String(e)));
  page.on('console',m=>{logs.push(m.text());if(m.type()==='error')errors.push(m.text())});
  page.on('dialog',d=>d.accept());
  await page.addInitScript(()=>{
    const data={},calls={};window.__testAndroidData=data;window.__testAndroidCalls=calls;
    window.AndroidSchedule=new Proxy({}, {get(target,key){
      if(typeof key!=='string')return;
      return (...args)=>{
        calls[key]=(calls[key]||0)+1;
        if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
        if(key==='loadSchedule')return data.Schedule||'';
        if(key==='loadUiSettings')return data.UiSettings||'{"language":"fr","theme":"blue"}';
        if(key==='loadAdvancedSettings')return data.AdvancedSettings||'{"cycleLength":2,"singleWeek":false}';
        if(key==='listProfiles')return '{"profiles":[{"id":"default","name":"Défaut"}],"current":"default"}';
        if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks'||key==='loadEffectiveCourses')return '[]';
        return data[key.replace(/^load/,'')]||'{}';
      };
    }});
  });
  await page.goto(pathToFileURL(asset).href);
  for(const file of fs.readdirSync(chunks).sort()){
    await page.evaluate(fs.readFileSync(path.join(chunks,file),'utf8'));
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__edtActionChains651&&window.__edtNavigationCacheV2&&window.__edtRenderPipeline650);
  await page.evaluate(()=>window.setModeFromAndroid('week'));
  await page.waitForTimeout(100);

  await page.locator('#weekTabs .weekTab[data-week="B"]').tap();
  await page.waitForTimeout(50);
  const weekStress=await page.evaluate(()=>{
    const calls=__testAndroidCalls,chain=__edtActionChains651,nav=__edtNavigationCacheV2;
    const beforeSave=calls.saveSchedule||0,beforeListeners=window.__edtHeavyPerfPrelude648?__edtHeavyPerfPrelude648.counters.listenerAdds:0;
    const nodesBefore=document.getElementsByTagName('*').length,samples=[];
    for(let i=0;i<300;i++){
      const letter=i%2?'A':'B',el=document.querySelector('#weekTabs .weekTab[data-week="'+letter+'"]');
      const t=performance.now();chain.runClick(el,{type:'test'},el.onclick);samples.push(performance.now()-t);
    }
    const p50=a=>{const s=a.slice().sort((x,y)=>x-y);return s[Math.floor(s.length/2)]||0};
    return {saveDelta:(calls.saveSchedule||0)-beforeSave,nodeDelta:document.getElementsByTagName('*').length-nodesBefore,
      listenerDelta:(window.__edtHeavyPerfPrelude648?__edtHeavyPerfPrelude648.counters.listenerAdds:0)-beforeListeners,
      headP50:p50(samples.slice(0,40)),tailP50:p50(samples.slice(-40)),weekSelections:chain.stats.weekSelections,
      weekRenders:nav.cache.renders.week};
  });
  assert.equal(weekStress.saveDelta,0,'display-week switching must not persist the timetable');
  assert.equal(weekStress.listenerDelta,0,'week switching must not add listeners');
  assert.ok(weekStress.nodeDelta<=0,'week switching must not accumulate DOM nodes');
  assert.ok(weekStress.tailP50<=Math.max(weekStress.headP50*1.8,weekStress.headP50+2),'week switching must not progressively slow down');

  const currentWeek=await page.evaluate(()=>{
    const c=__testAndroidCalls,chain=__edtActionChains651,el=document.getElementById('currentWeekBtn');
    const s0=c.saveSchedule||0,w0=c.setCurrentWeek||0,r0=chain.stats.targetedRenders;
    for(let i=0;i<100;i++)chain.runClick(el,{type:'test'},el.onclick);
    return {saveDelta:(c.saveSchedule||0)-s0,setWeekDelta:(c.setCurrentWeek||0)-w0,targeted:chain.stats.targetedRenders-r0};
  });
  assert.equal(currentWeek.saveDelta,0,'current-week toggle must not rewrite the full schedule');
  assert.equal(currentWeek.setWeekDelta,100,'current-week toggle must use the dedicated native week setter');
  assert.ok(currentWeek.targeted<=100,'current-week toggle may refresh the visible target at most once per action');

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(()=>window.__edtHeavyPanels648&&__edtHeavyPanels648.isOpen('settings'));
  const advBefore=await page.evaluate(()=>({targeted:__edtActionChains651.stats.targetedRenders,suppressed:__edtActionChains651.stats.suppressedRenders}));
  await page.locator('#advDensity').selectOption('compact');
  await page.locator('#advFollowing').selectOption('2');
  await page.locator('#advShowRoom').uncheck();
  const advAfter=await page.evaluate(()=>({targeted:__edtActionChains651.stats.targetedRenders,suppressed:__edtActionChains651.stats.suppressedRenders}));
  assert.equal(advAfter.targeted-advBefore.targeted,0,'widget-only settings must not redraw timetable views');
  assert.ok(advAfter.suppressed-advBefore.suppressed>=3,'legacy timetable renders must be coalesced for widget-only settings');

  const cycleBefore=await page.evaluate(()=>({save:__testAndroidCalls.saveSchedule||0,targeted:__edtActionChains651.stats.targetedRenders}));
  await page.evaluate(()=>{
    const e=document.getElementById('advCycle');
    assertNode(e,'advCycle');
    e.value='3';e.dispatchEvent(new Event('change',{bubbles:true}));
    function assertNode(node,name){if(!node)throw new Error(name+' missing')}
  });
  const cycleAfter=await page.evaluate(()=>({save:__testAndroidCalls.saveSchedule||0,targeted:__edtActionChains651.stats.targetedRenders}));
  assert.equal(cycleAfter.save-cycleBefore.save,1,'cycle change must persist the schedule once');
  assert.ok(cycleAfter.targeted-cycleBefore.targeted<=1,'cycle change must target-render at most once');

  await page.evaluate(()=>{
    const from=document.getElementById('advCopyFrom'),to=document.getElementById('advCopyTo');
    if(!from||!to)throw new Error('copy-day controls missing');from.value='2';to.value='3';
  });
  const copyBefore=await page.evaluate(()=>({save:__testAndroidCalls.saveSchedule||0,targeted:__edtActionChains651.stats.targetedRenders,actions:__edtActionChains651.stats.actions}));
  await page.evaluate(()=>{const b=document.getElementById('advCopyDay');if(!b)throw new Error('advCopyDay missing');b.click()});
  await page.waitForFunction(before=>__edtActionChains651.stats.actions>before,copyBefore.actions);
  const copyAfter=await page.evaluate(()=>({save:__testAndroidCalls.saveSchedule||0,targeted:__edtActionChains651.stats.targetedRenders,actions:__edtActionChains651.stats.actions}));
  assert.equal(copyAfter.save-copyBefore.save,1,'copy-day must persist once');
  assert.ok(copyAfter.targeted-copyBefore.targeted<=1,'copy-day must refresh at most one visible view');

  const hasReset=await page.evaluate(()=>!!document.getElementById('resetText87'));
  if(hasReset){
    const resetBefore=await page.evaluate(()=>({targeted:__edtActionChains651.stats.targetedRenders,actions:__edtActionChains651.stats.actions}));
    await page.evaluate(()=>document.getElementById('resetText87').click());
    await page.waitForFunction(before=>__edtActionChains651.stats.actions>before,resetBefore.actions);
    const resetAfter=await page.evaluate(()=>({targeted:__edtActionChains651.stats.targetedRenders,actions:__edtActionChains651.stats.actions}));
    assert.equal(resetAfter.targeted-resetBefore.targeted,0,'settings section reset must not redraw the timetable');
  }
  await page.locator('#settingsX').tap();
  await page.waitForFunction(()=>!__edtHeavyPanels648.isOpen('settings'));

  await page.evaluate(()=>window.setModeFromAndroid('edit'));
  await page.waitForTimeout(50);
  const breakBefore=await page.evaluate(()=>__testAndroidCalls.saveSchedule||0);
  await page.locator('#gapLabel').fill('Interclasse');
  const afterTyping=await page.evaluate(()=>__testAndroidCalls.saveSchedule||0);
  assert.equal(afterTyping-breakBefore,0,'typing a break label must not persist repeatedly');
  await page.locator('#gapLabel').press('Tab');
  await page.waitForTimeout(180);
  const breakAfter=await page.evaluate(()=>__testAndroidCalls.saveSchedule||0);
  assert.equal(breakAfter-breakBefore,1,'break label validation must persist exactly once');

  const anchors=await page.evaluate(()=>{document.querySelector('#slotSettings .slotRow').dataset.anchor651='slot';document.querySelector('#dayTabs .dayTab:not(.weekendAdd)').dataset.anchor651='day';return __testAndroidCalls.saveSchedule||0});
  await page.locator('#editList .editCourse').first().tap();
  await page.locator('#deleteCourse').tap();
  await page.waitForTimeout(100);
  const deletion=await page.evaluate(()=>({save:(__testAndroidCalls.saveSchedule||0),slot:!!document.querySelector('#slotSettings .slotRow[data-anchor651="slot"]'),day:!!document.querySelector('#dayTabs .dayTab[data-anchor651="day"]')}));
  assert.equal(deletion.save-anchors,1,'course deletion must persist once');
  assert.equal(deletion.slot,true,'course deletion must keep slot controls mounted');
  assert.equal(deletion.day,true,'course deletion must keep day tabs mounted');

  const hasModeButton=await page.evaluate(()=>!!document.querySelector('#weekModeBar .weekModeChoice[data-m="1"]'));
  if(hasModeButton){
    const modeBefore=await page.evaluate(()=>({save:__testAndroidCalls.saveSchedule||0,targeted:__edtActionChains651.stats.targetedRenders,actions:__edtActionChains651.stats.actions}));
    await page.evaluate(()=>document.querySelector('#weekModeBar .weekModeChoice[data-m="1"]').click());
    await page.waitForFunction(before=>__edtActionChains651.stats.actions>before,modeBefore.actions);
    const modeNow=await page.evaluate(()=>({save:__testAndroidCalls.saveSchedule||0,targeted:__edtActionChains651.stats.targetedRenders,actions:__edtActionChains651.stats.actions}));
    await page.waitForTimeout(220);
    const modeLate=await page.evaluate(()=>({save:__testAndroidCalls.saveSchedule||0,targeted:__edtActionChains651.stats.targetedRenders,actions:__edtActionChains651.stats.actions}));
    assert.equal(modeNow.save-modeBefore.save,1,'single-week conversion must persist once');
    assert.ok(modeNow.targeted-modeBefore.targeted<=1,'single-week conversion must target-render at most once');
    assert.deepEqual(modeLate,modeNow,'single-week mode must not leave delayed work behind');
  }

  const report={weekStress,currentWeek,advBefore,advAfter,cycleBefore,cycleAfter,copyBefore,copyAfter,breakBefore,breakAfter,deletion,stats:await page.evaluate(()=>__edtActionChains651.stats)};
  fs.writeFileSync(path.join(out,'actions-651.json'),JSON.stringify(report,null,2));
  fs.writeFileSync(path.join(out,'actions-651.log'),logs.join('\n'));
  assert.deepEqual(errors,[],'6.51 action-chain JavaScript errors');
  console.log('browser_action_chains_651=passed',JSON.stringify(report));
  await context.close();await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
