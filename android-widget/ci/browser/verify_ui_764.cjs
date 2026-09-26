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
    const data={AdvancedSettings:'{"cycleLength":2,"singleWeek":false,"showLunchWeek":true,"showBreaksWeek":true}',UiSettings:'{"language":"fr","theme":"blue","appFontScale":1}'};
    window.__data764=data;window.alert=()=>{};window.confirm=()=>true;
    window.AndroidSchedule=new Proxy({},{get(target,key){if(typeof key!=='string')return;return(...args)=>{
      if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
      if(key==='loadSchedule')return data.Schedule||'';
      if(key==='loadUiSettings')return data.UiSettings;
      if(key==='loadAdvancedSettings')return data.AdvancedSettings;
      if(key==='listProfiles')return JSON.stringify({current:'main',profiles:[{id:'main',name:'Principal'}]});
      if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks'||key==='loadEffectiveCourses')return '[]';
      return data[key.replace(/^load/,'')]||'{}';
    }}});
  });
  await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
  for(const name of fs.readdirSync(chunks).sort()){
    await page.evaluate(fs.readFileSync(path.join(chunks,name),'utf8')+'\n//# sourceURL='+name);
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__feedback764&&window.__edtHeavyPanels648);

  const regular=await page.evaluate(()=>{
    render();window.refreshFeedback764();
    const rect=id=>document.getElementById(id).getBoundingClientRect(),bottom=document.querySelector('.bottom').getBoundingClientRect();
    const title=rect('todayTitle'),list=rect('todayList');
    return {todayFont:getComputedStyle(document.getElementById('todayTitle')).fontSize,titleRight:title.right,titleLeft:title.left,listGap:bottom.top-list.bottom,listMin:parseFloat(document.getElementById('todayList').style.minHeight)||0};
  });
  assert.ok(regular.titleLeft>=0&&regular.titleRight<=412,JSON.stringify(regular));
  assert.ok(regular.listMin>300,JSON.stringify(regular));
  assert.ok(regular.listGap>=10,JSON.stringify(regular));

  await page.evaluate(()=>{const ui=JSON.parse(window.__data764.UiSettings);ui.appFontScale=1.4;window.__data764.UiSettings=JSON.stringify(ui);document.documentElement.style.fontSize='22.4px';document.body.style.fontSize='21px';render();window.refreshFeedback764()});
  const large=await page.evaluate(()=>{const r=document.getElementById('todayTitle').getBoundingClientRect();return{left:r.left,right:r.right,width:r.width,scrollWidth:document.documentElement.scrollWidth,viewport:innerWidth,text:document.getElementById('todayTitle').textContent}});
  assert.ok(large.left>=0&&large.right<=large.viewport,JSON.stringify(large));
  assert.ok(large.scrollWidth<=large.viewport,JSON.stringify(large));

  await page.locator('.nav[data-mode="week"]').tap();
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  const week=await page.evaluate(()=>{
    const title=document.querySelector('#viewWeek .weekTop>h2'),grid=document.getElementById('weekGrid').getBoundingClientRect(),bottom=document.querySelector('.bottom').getBoundingClientRect();
    return {font:getComputedStyle(title).fontSize,todayFont:getComputedStyle(document.getElementById('todayTitle')).fontSize,gap:bottom.top-grid.bottom,left:title.getBoundingClientRect().left,right:title.getBoundingClientRect().right,viewport:innerWidth};
  });
  assert.equal(week.font,week.todayFont);
  assert.ok(week.left>=0&&week.right<=week.viewport,JSON.stringify(week));
  assert.ok(week.gap>=10&&week.gap<=14,JSON.stringify(week));

  await page.locator('.nav[data-mode="edit"]').tap();
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  const edit=await page.evaluate(()=>({title:document.getElementById('editDayTitle').textContent,cycle:document.getElementById('editCycleLabel764')?.textContent,importTop:document.getElementById('importPhoto').getBoundingClientRect().top,wrapTop:document.querySelector('main.wrap').getBoundingClientRect().top,historyAfterBulk:document.getElementById('editHistoryActions86')?.previousElementSibling?.id==='addBulkCourses'}));
  assert.match(edit.title,/\b20\d{2}\b/);
  assert.match(edit.cycle,/Semaine [A-D]/);
  assert.ok(edit.importTop-edit.wrapTop<=10,JSON.stringify(edit));
  assert.equal(edit.historyAfterBulk,true);

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));
  const settings=await page.evaluate(()=>{
    ['textSettings86','colorSettings86','breakSettings86','advancedSettings85'].forEach(id=>{const x=document.getElementById(id);if(x)x.open=true});window.refreshFeedback764();
    const density=document.getElementById('widgetDensity664'),auto=document.getElementById('advDensityAutoRow665'),calendar=document.getElementById('advCalendarTitle');
    const font=x=>getComputedStyle(document.getElementById(x)).fontSize,selectorFont=x=>getComputedStyle(document.querySelector(x)).fontSize,center=x=>{const r=document.getElementById(x).getBoundingClientRect();return(r.left+r.right)/2};
    const app=document.getElementById('appFontTitle').getBoundingClientRect(),textSummary=document.querySelector('#textSettings86>summary').getBoundingClientRect();
    const weekTitle=document.querySelector('#settingsWeekCycle678>.settingTitle').getBoundingClientRect(),weekSummary=document.querySelector('#weekTypeSettings86>summary').getBoundingClientRect();
    return {
      densitySection:density.closest('.settingsSection86')?.id,
      densityAfterWidgetFont:density.previousElementSibling?.contains(document.getElementById('widgetFont')),
      autoWidth:auto.getBoundingClientRect().width,autoHeight:auto.getBoundingClientRect().height,
      calendarDisplay:getComputedStyle(calendar).display,
      advancedFonts:['advReminderTitle','advExceptionsTitle','advProfilesTitle','advBackupTitle'].map(font),schoolFont:font('schoolTitle'),
      breakFonts:[font('breakNamesTitle763'),selectorFont('#week658LunchSettings .w658Title')],
      colorFonts:[font('appPaletteTitle'),selectorFont('#week658Settings .w658Title')],
      colorCenters:[Math.abs(center('appPaletteTitle')-center('paletteSettingRoot')),Math.abs(center('week658Settings')-center('colorSettings86'))],
      swatches:[...document.querySelectorAll('#week658Settings input[type=color]')].map(x=>x.getBoundingClientRect().width),
      firstGaps:[app.top-textSummary.bottom,weekTitle.top-weekSummary.bottom]
    };
  });
  assert.equal(settings.densitySection,'textSettings86');
  assert.equal(settings.densityAfterWidgetFont,true);
  assert.ok(settings.autoWidth<=240&&settings.autoHeight>34,JSON.stringify(settings));
  assert.equal(settings.calendarDisplay,'none');
  settings.advancedFonts.forEach(value=>assert.equal(value,settings.schoolFont));
  assert.equal(settings.breakFonts[0],settings.breakFonts[1]);
  assert.equal(settings.colorFonts[0],settings.colorFonts[1]);
  settings.colorCenters.forEach(value=>assert.ok(value<=1,JSON.stringify(settings)));
  settings.swatches.forEach(value=>assert.ok(value<=40,JSON.stringify(settings)));
  assert.ok(settings.firstGaps[0]<=settings.firstGaps[1]+6,JSON.stringify(settings));

  await page.evaluate(()=>{window.__data764.AdvancedSettings='{"cycleLength":2,"singleWeek":true,"showLunchWeek":true,"showBreaksWeek":true}';document.getElementById('settingsDone').click();render();window.refreshFeedback764()});
  const single=await page.evaluate(()=>({today:document.getElementById('todayDate').textContent,week:document.getElementById('weekCycleLabel757').textContent,edit:document.getElementById('editCycleLabel764').textContent,singleClass:document.documentElement.classList.contains('singleWeek764')}));
  assert.deepEqual(single,{today:'',week:'',edit:'',singleClass:true});

  const gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');
  const widget=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java'),'utf8');
  assert.match(gradle,/versionCode 767001/);assert.match(gradle,/versionName '7[.]67'/);
  assert.match(widget,/optBoolean\("singleWeek", false\)/);
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({regular,large,week,edit,settings,single,errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
