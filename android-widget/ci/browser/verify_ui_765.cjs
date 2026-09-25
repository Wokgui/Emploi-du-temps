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
    const data={AdvancedSettings:'{"cycleLength":2,"singleWeek":false,"showLunchWeek":true,"showBreaksWeek":true,"dayViewDensity765":100}',UiSettings:'{"language":"fr","theme":"blue","appFontScale":1}'};
    window.__data765=data;window.alert=()=>{};window.confirm=()=>true;
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
  await page.waitForFunction(()=>window.__feedback765&&window.__edtHeavyPanels648);
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));

  const report=await page.evaluate(()=>{
    ['textSettings86','weekTypeSettings86','colorSettings86','breakSettings86','widgetSettings86','advancedSettings85'].forEach(id=>{const node=document.getElementById(id);if(node)node.open=true});
    window.refreshFeedback765();
    const style=selector=>getComputedStyle(document.querySelector(selector));
    const ref=style('#advRangeTitle');
    const subtitleSelectors=['#appFontTitle','#widgetFontTitle','#dayDensityTitle765','#widgetDensity664 .feedback664DensityTitle','#settingsWeekCycle678>.settingTitle','.weekCurrentLabel678','#slotSettingsGroup759>.settingTitle','#themeTitle','#paletteSettingRoot>.settingTitle','#week658Settings .w658Title','#breakNamesTitle763','#week658LunchSettings .w658Title'];
    const contentSelectors=['#appFontValue','#widgetFontValue','#advDensityValue665','.weekCycleChoice678','.weekCurrentChoice678','#slotSettings input[type=time]','.themeName','.coursePaletteName','#week658Settings .w658Colors span','#breakNamesSettings763 .breakName','#week658LunchSettings .w658DayHead'];
    const separatorSelectors=['#widgetFontTitle','#dayDensity765','#widgetDensity664','.weekCurrentSettings678','#slotSettingsGroup759','#paletteSettingRoot','#week658Settings','#breakNamesSettings763','#week658LunchSettings'];
    const format=document.getElementById('advFormat'),formatStyle=getComputedStyle(format),option=format.options[format.selectedIndex].text;
    const canvas=document.createElement('canvas'),ctx=canvas.getContext('2d');ctx.font=formatStyle.font;const needed=ctx.measureText(option).width+parseFloat(formatStyle.paddingLeft)+parseFloat(formatStyle.paddingRight)+8;
    const school=style('#schoolCalendarSetting'),exception=style('#advExceptionList'),day=document.getElementById('dayDensity765'),widgetTitle=document.getElementById('widgetFontTitle'),widgetRow=widgetTitle.nextElementSibling;
    return {
      reference:{size:ref.fontSize,color:ref.color,weight:ref.fontWeight},
      subtitles:subtitleSelectors.map(selector=>({selector,size:style(selector).fontSize,color:style(selector).color,weight:style(selector).fontWeight,align:style(selector).textAlign})),
      contents:contentSelectors.map(selector=>({selector,size:style(selector).fontSize})),
      separators:separatorSelectors.map(selector=>({selector,width:style(selector).borderTopWidth,kind:style(selector).borderTopStyle})),
      format:{clientWidth:format.clientWidth,needed,text:option,rowWidth:format.closest('.advRow').getBoundingClientRect().width,selectWidth:format.getBoundingClientRect().width},
      exceptionBorder:exception.borderTopWidth,
      schoolBorders:[school.borderTopWidth,school.borderRightWidth,school.borderLeftWidth],
      dayParent:day.parentElement.id,dayAfterWidgetRow:day.previousElementSibling===widgetRow,
      densityTitle:document.getElementById('dayDensityTitle765').textContent,
      densityValue:document.getElementById('dayDensityValue765').textContent
    };
  });
  report.subtitles.forEach(item=>{assert.equal(item.size,report.reference.size,JSON.stringify(item));assert.equal(item.color,report.reference.color,JSON.stringify(item));assert.equal(item.weight,report.reference.weight,JSON.stringify(item));assert.equal(item.align,'center',JSON.stringify(item))});
  report.contents.forEach(item=>assert.equal(item.size,report.reference.size,JSON.stringify(item)));
  report.separators.forEach(item=>{assert.equal(item.width,'1px',JSON.stringify(item));assert.notEqual(item.kind,'none',JSON.stringify(item))});
  assert.ok(report.format.selectWidth>=190&&report.format.clientWidth>=report.format.needed,JSON.stringify(report.format));
  assert.equal(report.exceptionBorder,'0px');
  assert.deepEqual(report.schoolBorders,['0px','0px','0px']);
  assert.equal(report.dayParent,'fontCombined79');assert.equal(report.dayAfterWidgetRow,true);
  assert.equal(report.densityTitle,'Densité de la vue jour');assert.equal(report.densityValue,'0 %');

  await page.locator('#settingsDone').tap();
  await page.locator('.nav[data-mode="today"]').tap();
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  const full=await page.evaluate(()=>parseFloat(document.getElementById('todayList').style.minHeight));
  await page.locator('#settingsBtn').tap();await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));
  await page.evaluate(()=>{document.getElementById('textSettings86').open=true});
  await page.locator('#dayDensitySlider765').evaluate((node)=>{node.value='100';node.dispatchEvent(new Event('input',{bubbles:true}))});
  await page.locator('#settingsDone').tap();await page.locator('.nav[data-mode="today"]').tap();
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  const compact=await page.evaluate(()=>({height:parseFloat(document.getElementById('todayList').style.minHeight),saved:JSON.parse(window.__data765.AdvancedSettings).dayViewDensity765}));
  assert.equal(compact.saved,100);assert.ok(compact.height<full*.7,JSON.stringify({full,compact}));

  const gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');
  assert.match(gradle,/versionCode 766001/);assert.match(gradle,/versionName '7[.]66'/);
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({report,dayDensity:{full,compact},errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
