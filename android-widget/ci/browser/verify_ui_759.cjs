const {chromium}=require('playwright');
const fs=require('fs');
const path=require('path');
const assert=require('assert/strict');
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
    const data={
      AdvancedSettings:'{"cycleLength":2,"singleWeek":false,"showLunchWeek":true,"showBreaksWeek":true,"widgetAutoDensity":false}',
      UiSettings:'{"language":"fr","theme":"blue","appFontScale":1,"widgetFontScale":1}'
    };
    window.__uiData759=data;window.confirm=()=>true;window.alert=()=>{};
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
  await page.waitForFunction(()=>window.__edtHeavyPanels648&&document.getElementById('week658LunchSettings'));
  await page.locator('#settingsBtn').tap();
  await page.waitForTimeout(80);

  const layout=await page.evaluate(()=>{
    const center=id=>{const r=document.getElementById(id)?.getBoundingClientRect();return r?(r.left+r.right)/2:null};
    const centerOf=el=>{const r=el?.getBoundingClientRect();return r?(r.left+r.right)/2:null};
    const summary=document.querySelector('#weekTypeSettings86>summary');
    const week=document.getElementById('weekTypeSettings86'),colors=document.getElementById('colorSettings86');
    week.open=true;colors.open=true;
    const reset=document.getElementById('resetText87'),textBody=document.querySelector('#textSettings86>.settingsSectionBody86');
    const school=document.getElementById('schoolCalendarSetting'),calendar=document.getElementById('advCalendarTitle')?.closest('.settingBox');
    const holiday=document.getElementById('advHoliday')?.closest('.advRow');
    return {
      weekSummary:summary?.textContent.trim(),
      weekTypeParent:document.getElementById('settingsWeekCycle678')?.closest('.settingsSection86')?.id,
      slotsParent:document.getElementById('slotSettingsGroup759')?.closest('.settingsSection86')?.id,
      slotsExists:!!document.getElementById('slotSettings'),
      slotGroupExists:!!document.getElementById('slotSettingsGroup759'),
      slotParent:document.getElementById('slotSettings')?.parentElement?.id,
      slotAncestors:(()=>{const out=[];let n=document.getElementById('slotSettings');while(n){out.push(n.id||n.className||n.tagName);n=n.parentElement}return out})(),
      lunchParent:document.getElementById('week658LunchSettings')?.closest('.settingsSection86')?.id,
      lunchPrevious:document.getElementById('week658LunchSettings')?.previousElementSibling?.id,
      breakChildren:[...document.querySelectorAll('#breakSettings86>.settingsSectionBody86>*')].map(x=>({id:x.id,cls:x.className})),
      adaptiveParent:document.getElementById('advDensityAutoRow665')?.closest('.settingsSection86')?.id,
      adaptiveText:document.querySelector('#advDensityAutoRow665 span')?.textContent.trim(),
      typeDelta:Math.abs(centerOf(document.querySelector('#settingsWeekCycle678>.settingTitle'))-center('settingsWeekCycle678')),
      themeDelta:Math.abs(center('themeTitle')-centerOf(document.getElementById('themeTitle')?.parentElement)),
      paletteDelta:Math.abs(center('appPaletteTitle')-center('paletteSettingRoot')),
      resetGap:textBody&&reset?textBody.getBoundingClientRect().bottom-reset.getBoundingClientRect().bottom:null,
      resetInside:reset?.parentElement===textBody,
      calendarTitle:document.getElementById('advCalendarTitle')?.textContent.trim(),
      schoolInside:school?.parentElement===calendar,
      schoolAfterTitle:school?.previousElementSibling?.id==='advCalendarTitle',
      schoolBeforeHoliday:!!(school&&holiday&&(school.compareDocumentPosition(holiday)&Node.DOCUMENT_POSITION_FOLLOWING))
    };
  });

  assert.equal(layout.weekSummary,'Semaines et horaires');
  assert.equal(layout.weekTypeParent,'weekTypeSettings86');
  assert.equal(layout.slotsParent,'weekTypeSettings86');
  assert.equal(layout.lunchParent,'breakSettings86');
  assert.equal(layout.lunchPrevious,'breakNamesSettings763');
  assert.deepEqual(layout.breakChildren.map(x=>x.id),['breakDisplaySetting','breakNamesSettings763','week658LunchSettings']);
  assert.equal(layout.adaptiveParent,'textSettings86');
  assert.equal(layout.adaptiveText,'Adapter la taille du texte pour tout afficher dans le widget');
  assert.ok(layout.typeDelta<=1,JSON.stringify(layout));
  assert.ok(layout.themeDelta<=1,JSON.stringify(layout));
  assert.ok(layout.paletteDelta<=1,JSON.stringify(layout));
  assert.equal(layout.resetInside,true);
  assert.ok(layout.resetGap>=8,JSON.stringify(layout));
  assert.equal(layout.calendarTitle,'Vacances et jours fériés');
  assert.equal(layout.schoolInside,true);
  assert.equal(layout.schoolAfterTitle,true);
  assert.equal(layout.schoolBeforeHoliday,true);

  await page.evaluate(()=>document.getElementById('textSettings86').open=true);
  await page.locator('#advDensityAuto665').check();
  assert.equal(JSON.parse(await page.evaluate(()=>window.__uiData759.AdvancedSettings)).widgetAutoDensity,true);

  const breakDetails=page.locator('#breakSettings86');
  for(let i=0;i<6;i++){
    await breakDetails.locator(':scope>summary').tap();
    await page.waitForTimeout(20);
    if(!(await breakDetails.evaluate(el=>el.open)))await breakDetails.locator(':scope>summary').tap();
    const state=await breakDetails.evaluate(el=>({
      open:el.open,
      height:el.getBoundingClientRect().height,
      matrix:getComputedStyle(document.getElementById('breakDisplaySetting')).display,
      lunchRows:document.querySelectorAll('#week658LunchSettings .w658Day').length
    }));
    assert.equal(state.open,true);
    assert.ok(state.height>250,JSON.stringify(state));
    assert.notEqual(state.matrix,'none');
    assert.equal(state.lunchRows,5);
    await breakDetails.locator(':scope>summary').tap();
  }

  const slotReport=await page.evaluate(async()=>{
    const settle=()=>new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve)));
    document.getElementById('weekTypeSettings86').open=true;
    const before=slots.length;
    const addButton=document.querySelector('#slotSettings .slotAdd');
    addButton.click();
    await settle();
    const added=slots.length,savedRaw=__uiData759.Schedule||null,savedAdded=savedRaw?JSON.parse(savedRaw):{};
    const addedTime=slots[slots.length-1].start;
    activeWeek='A';mode='week';state=weeks.A;renderWeek();
    const rowAdded=[...document.querySelectorAll('#weekGrid>.wh.timecol')].some(x=>x.textContent.includes(addedTime));
    document.querySelector('#slotSettings .slotRow:last-of-type .slotRemove').click();
    await settle();
    const afterUnused=slots.length;
    const courseBefore=Number(weeks.A[2].courses.find(c=>c.start==='08:00'&&c.end==='09:00')?.slot);
    document.querySelector('#slotSettings .slotRow .slotRemove').click();
    await settle();
    const courseAfter=weeks.A[2].courses.find(c=>c.start==='08:00'&&c.end==='09:00');
    activeWeek='A';mode='week';state=weeks.A;renderWeek();
    const preservedRow=[...document.querySelectorAll('#weekGrid>.wh.timecol')].some(x=>x.textContent.includes('08:00')&&x.textContent.includes('09:00'));
    return {
      before,added,afterUnused,afterCourse:slots.length,rowAdded,preservedRow,
      addButton:!!addButton,addHidden:addButton.hidden,addDisabled:addButton.disabled,addOnclick:typeof addButton.onclick,addHandler:String(addButton.onclick).slice(0,120),savedRaw:!!savedRaw,slotConfig:savedAdded._slotConfigV2,storedAdded:savedAdded._slots?.length,
      courseBefore,courseAfter:courseAfter?.slot,
      savedCourse:__uiData759.Schedule?JSON.parse(__uiData759.Schedule)._weeks.A['2'].courses.find(c=>c.start==='08:00'&&c.end==='09:00')?.slot:null
    };
  });
  assert.deepEqual(slotReport,{before:9,added:10,afterUnused:9,afterCourse:8,rowAdded:true,preservedRow:true,addButton:true,addHidden:false,addDisabled:false,addOnclick:'function',addHandler:slotReport.addHandler,savedRaw:true,slotConfig:true,storedAdded:10,courseBefore:1,courseAfter:0,savedCourse:0});

  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({layout,slotReport,errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
