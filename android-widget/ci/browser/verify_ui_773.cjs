const {chromium}=require('playwright');
const fs=require('fs');
const path=require('path');
const assert=require('node:assert/strict');
const {pathToFileURL}=require('url');

const root=path.resolve(__dirname,'../..');
const chunks=path.resolve(process.env.EDT_UI_CHUNKS||'smoke-browser/chunks');

(async()=>{
  const browser=await chromium.launch({headless:true,...(process.env.EDT_BROWSER_CHANNEL?{channel:process.env.EDT_BROWSER_CHANNEL}:{})});
  const context=await browser.newContext({viewport:{width:412,height:915},isMobile:true,hasTouch:true});
  const page=await context.newPage(),errors=[];
  page.on('pageerror',error=>errors.push(String(error)));
  page.on('console',message=>{if(message.type()==='error')errors.push(message.text())});
  await page.addInitScript(()=>{
    const schedule={_currentWeek:'A',_weeks:{A:{'2':{enabled:true,courses:[{start:'08:00',end:'09:00',label:'6G3',room:'12',slot:1,color:'violet'}]},'6':{enabled:true,courses:[{start:'11:00',end:'12:00',label:'5G1-2-3 ALL',room:'217',slot:4,color:'coral'},{start:'13:00',end:'14:00',label:'5G4 ALL',room:'217',slot:5,color:'green'}]}}},_breaks:{gapLabel:'Trou',lunchLabel:'Midi'},_slots:[['08:00','09:00'],['09:00','10:00'],['10:00','11:00'],['11:00','12:00'],['13:00','14:00'],['14:00','15:00'],['16:00','17:00']].map((v,i)=>({n:i+1,start:v[0],end:v[1]}))};
    const data={UiSettings:JSON.stringify({language:'fr',theme:'blue',appFontScale:1,widgetFontScale:1}),AdvancedSettings:JSON.stringify({cycleLength:2,appClassColorMode:'fill',appColorByClass:true,widgetClassColorMode:'fill',colorByClass:true,appAccessibility:'normal',dayViewDensity765:0,showBreaksToday:true,showBreaksWeek:true,showLunchToday:true,showLunchWeek:true}),Schedule:JSON.stringify(schedule)};
    window.__data773=data;window.alert=()=>{};window.confirm=()=>true;
    window.AndroidSchedule=new Proxy({},{get(target,key){if(typeof key!=='string')return;return(...args)=>{
      if(key==='saveAdvancedSettings'){data.AdvancedSettings=args[0];return true}if(key==='loadAdvancedSettings')return data.AdvancedSettings;
      if(key==='saveUiSettings'){data.UiSettings=args[0];return true}if(key==='loadUiSettings')return data.UiSettings;
      if(key==='saveSchedule'){data.Schedule=args[0];return true}if(key==='loadSchedule')return data.Schedule;
      if(key==='loadEffectiveCourses')return JSON.stringify({courses:JSON.parse(data.Schedule)._weeks.A['6'].courses});
      if(key==='listProfiles')return JSON.stringify({current:'main',profiles:[{id:'main',name:'Principal'}]});
      if(key==='loadWidgetPalette')return 'vivid';if(key==='loadSpecialColors')return '{}';
      if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks')return '[]';return '{}';
    }}});
  });
  await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
  for(const name of fs.readdirSync(chunks).sort()){
    await page.evaluate(fs.readFileSync(path.join(chunks,name),'utf8')+'\n//# sourceURL='+name);
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__feedback769&&window.refreshFeedback769Views&&window.applyDayDensity765);
  const settle=()=>page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve))));
  const saveMode=async(mode,density)=>{await page.evaluate(({mode,density})=>{const a=JSON.parse(window.__data773.AdvancedSettings);a.appClassColorMode=mode;a.appColorByClass=mode!=='none';if(density!==undefined)a.dayViewDensity765=density;AndroidSchedule.saveAdvancedSettings(JSON.stringify(a));window.refreshAdvancedFeatures();window.refreshFeedback769Views()},{mode,density});await settle()};

  await page.evaluate(()=>setMode('week'));await saveMode('fill');
  const filled=await page.evaluate(()=>[...document.querySelectorAll('#weekGrid .wc.has')].map(cell=>({fill:cell.classList.contains('classFill'),stripe:cell.classList.contains('classTint'),background:getComputedStyle(cell).backgroundColor,color:cell.style.getPropertyValue('--class-color'),ink:getComputedStyle(cell.querySelector('.cellLabel')).color})));
  assert.ok(filled.length>=1,JSON.stringify(filled));for(const cell of filled){assert.equal(cell.fill,true,JSON.stringify(cell));assert.equal(cell.stripe,false,JSON.stringify(cell));assert.ok(cell.color,JSON.stringify(cell));assert.notEqual(cell.background,'rgb(255, 255, 255)',JSON.stringify(cell));assert.notEqual(cell.background,'rgba(0, 0, 0, 0)',JSON.stringify(cell))}

  for(let i=0;i<40;i++){const mode=i%2?'fill':'stripe';await saveMode(mode);await page.evaluate(()=>{renderWeek();window.refreshFeedback769Views()})}
  await saveMode('fill');
  const stableWeek=await page.evaluate(()=>({fill:document.querySelectorAll('#weekGrid .wc.has.classFill').length,stripe:document.querySelectorAll('#weekGrid .wc.has.classTint').length,total:document.querySelectorAll('#weekGrid .wc.has').length,white:[...document.querySelectorAll('#weekGrid .wc.has')].filter(x=>getComputedStyle(x).backgroundColor==='rgb(255, 255, 255)').length}));
  assert.equal(stableWeek.fill,stableWeek.total,JSON.stringify(stableWeek));assert.equal(stableWeek.stripe,0,JSON.stringify(stableWeek));assert.equal(stableWeek.white,0,JSON.stringify(stableWeek));

  await page.evaluate(()=>setMode('today'));await saveMode('fill',0);await page.evaluate(()=>window.applyDayDensity765());
  const loose=await page.evaluate(()=>({height:parseFloat(document.getElementById('todayList').style.minHeight),density:document.getElementById('todayList').dataset.densityPercent,courses:[...document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)')].map(x=>({fill:x.classList.contains('classFill'),stripe:x.classList.contains('classTint'),background:getComputedStyle(x).backgroundColor}))}));
  assert.equal(loose.density,'0');assert.ok(loose.courses.length>=1,JSON.stringify(loose));for(const cell of loose.courses){assert.equal(cell.fill,true,JSON.stringify(cell));assert.equal(cell.stripe,false,JSON.stringify(cell));assert.notEqual(cell.background,'rgb(255, 255, 255)',JSON.stringify(cell))}
  await saveMode('fill',100);
  for(let i=0;i<40;i++)await page.evaluate(()=>{render();window.refreshFeedback769Views()});
  const dense=await page.evaluate(()=>({height:parseFloat(document.getElementById('todayList').style.minHeight),density:document.getElementById('todayList').dataset.densityPercent,fill:document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch).classFill').length,stripe:document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch).classTint').length,total:document.querySelectorAll('#todayList .todayCourse:not(.gap):not(.lunch)').length}));
  assert.equal(dense.density,'100',JSON.stringify(dense));assert.ok(dense.height<loose.height,JSON.stringify({loose,dense}));assert.equal(dense.fill,dense.total,JSON.stringify(dense));assert.equal(dense.stripe,0,JSON.stringify(dense));

  await page.locator('#settingsBtn').tap();await page.waitForFunction(()=>window.__edtHeavyPanels648&&window.__edtHeavyPanels648.isOpen('settings'));
  await page.evaluate(()=>{const advanced=document.getElementById('advancedSettings85');if(advanced)advanced.open=true;window.refreshAdvancedFeatures();window.refreshFeedback769()});await settle();
  const typography=await page.evaluate(()=>{const reference=parseFloat(getComputedStyle(document.getElementById('advProfilesTitle')).fontSize),selectors=['#advProfileSelect','#advNewProfile','#advRenameProfile','#advDeleteProfile','#advShareBackup','#advRestoreBackup','#advHoliday','#advReminderMinutes','#schoolYear','#schoolZone','#advRangeStart','#advRangeLabel'];return {reference,controls:selectors.map(selector=>document.querySelector(selector)).filter(Boolean).map(node=>({id:node.id,font:parseFloat(getComputedStyle(node).fontSize),height:node.getBoundingClientRect().height})),checks:[...document.querySelectorAll('#advancedSettings85 input[type=checkbox]')].map(node=>({id:node.id,width:node.getBoundingClientRect().width,height:node.getBoundingClientRect().height}))}});
  assert.ok(typography.controls.length>=10,JSON.stringify(typography));for(const control of typography.controls){assert.ok(Math.abs(control.font-typography.reference)<.15,JSON.stringify({typography,control}));assert.ok(control.height>=typography.reference*2.1,JSON.stringify(control))}for(const check of typography.checks){assert.ok(check.width>=typography.reference*1.4,JSON.stringify(check));assert.ok(check.height>=typography.reference*1.4,JSON.stringify(check))}

  const preview=fs.readFileSync(path.join(root,'app/src/main/res/layout/widget_preview_condensed.xml'),'utf8'),info=fs.readFileSync(path.join(root,'app/src/main/res/xml/widget_info_condensed.xml'),'utf8'),fallback=fs.readFileSync(path.join(root,'app/src/main/res/drawable/widget_preview_condensed_image.xml'),'utf8');
  assert.match(preview,/layout_height="match_parent"/);assert.match(info,/minHeight="108dp"/);assert.match(info,/targetCellHeight="2"/);assert.match(fallback,/height="108dp"/);
  const gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');assert.match(gradle,/versionCode 779001/);assert.match(gradle,/versionName '7[.]79'/);
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({filled,stableWeek,density:{loose:loose.height,dense:dense.height},typography,errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
