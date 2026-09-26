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
    const data={AdvancedSettings:'{"cycleLength":2,"singleWeek":false,"density":"compact","widgetDensityPercent":20,"dayViewDensity765":100}',UiSettings:'{"language":"fr","theme":"blue","appFontScale":1}'};
    window.__data766=data;window.alert=()=>{};window.confirm=()=>true;
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
  await page.waitForFunction(()=>window.__feedback766&&window.__feedback765&&window.__edtHeavyPanels648);
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));

  const report=await page.evaluate(()=>{
    ['textSettings86','colorSettings86','widgetSettings86','advancedSettings85'].forEach(id=>{const node=document.getElementById(id);if(node)node.open=true});
    window.refreshFeedback766();
    const values=JSON.parse(window.__data766.AdvancedSettings),style=selector=>getComputedStyle(document.querySelector(selector));
    const pick=(selector,keys)=>{const current=style(selector);return Object.fromEntries(keys.map(key=>[key,current[key]]))};
    const keys=['display','borderRadius','backgroundColor','color','fontSize','fontWeight','lineHeight','width','paddingTop','paddingRight','paddingBottom','paddingLeft'];
    const modal=document.getElementById('settingsModal'),sheet=document.getElementById('settingsSheet'),modalRect=modal.getBoundingClientRect(),sheetRect=sheet.getBoundingClientRect();
    const paletteTitle=pick('#paletteSettingRoot>.settingTitle',['fontSize','color','fontWeight','lineHeight','textAlign']);
    const barsTitle=pick('#widgetEdgeBars672 .bar672Title',['fontSize','color','fontWeight','lineHeight','textAlign']);
    const autoTile=document.getElementById('advDensityAutoRow665'),paletteTile=document.querySelector('#paletteSettingRoot .paletteSyncRow');
    const profile=document.getElementById('advProfileSelect'),profileTitle=document.getElementById('advProfilesTitle'),profileText=profile.options[profile.selectedIndex].text;
    const canvas=document.createElement('canvas'),ctx=canvas.getContext('2d');ctx.font=getComputedStyle(profile).font;
    return {
      migrated:values,
      widgetTitle:document.querySelector('#widgetDensity664 .feedback664DensityTitle').textContent,
      dayTitle:document.getElementById('dayDensityTitle765').textContent,
      widgetTicks:[...document.querySelectorAll('#widgetDensity664 .feedback665DensityScale span')].map(node=>node.textContent),
      sliderValues:{widget:document.getElementById('advDensitySlider665').value,day:document.getElementById('dayDensitySlider765').value},
      titles:{paletteTitle,barsTitle},
      tiles:{auto:pick('#advDensityAutoRow665',keys),palette:pick('#paletteSettingRoot .paletteSyncRow',keys),inputFirst:paletteTile.firstElementChild.id,tileWidth:paletteTile.getBoundingClientRect().width,autoWidth:autoTile.getBoundingClientRect().width},
      profile:{titleSize:getComputedStyle(profileTitle).fontSize,selectSize:getComputedStyle(profile).fontSize,width:profile.getBoundingClientRect().width,needed:ctx.measureText(profileText).width+45,text:profileText},
      page:{classed:modal.classList.contains('settingsPage766'),role:modal.getAttribute('role'),ariaModal:modal.getAttribute('aria-modal'),background:style('#settingsModal').backgroundColor,radius:style('#settingsSheet').borderRadius,modalRect:{x:modalRect.x,y:modalRect.y,width:modalRect.width,height:modalRect.height},sheetRect:{x:sheetRect.x,y:sheetRect.y,width:sheetRect.width,height:sheetRect.height}}
    };
  });

  assert.equal(report.migrated.densityDirection766,true);
  assert.equal(report.migrated.widgetDensityPercent,80);
  assert.equal(report.migrated.dayViewDensity765,0);
  assert.equal(report.migrated.density,'compact');
  assert.equal(report.widgetTitle,'Densité du widget');
  assert.equal(report.dayTitle,'Densité de la vue jour');
  assert.deepEqual(report.widgetTicks,['Peu dense','Normale','Très dense']);
  assert.deepEqual(report.sliderValues,{widget:'80',day:'0'});
  assert.deepEqual(report.titles.barsTitle,report.titles.paletteTitle);
  assert.equal(report.tiles.inputFirst,'paletteSyncToggle');
  for(const key of Object.keys(report.tiles.auto))assert.equal(report.tiles.palette[key],report.tiles.auto[key],key+' '+JSON.stringify(report.tiles));
  assert.equal(report.tiles.tileWidth,report.tiles.autoWidth);
  assert.equal(report.profile.text,'Principal');
  assert.equal(report.profile.selectSize,report.profile.titleSize);
  assert.ok(report.profile.width>=report.profile.needed&&report.profile.width<=150,JSON.stringify(report.profile));
  assert.equal(report.page.classed,true);assert.equal(report.page.role,'region');assert.equal(report.page.ariaModal,null);
  assert.equal(report.page.background,'rgb(255, 255, 255)');assert.equal(report.page.radius,'0px');
  assert.deepEqual(report.page.modalRect,{x:0,y:0,width:412,height:915});
  assert.deepEqual(report.page.sheetRect,{x:0,y:0,width:412,height:915});

  await page.locator('#advDensitySlider665').evaluate(node=>{node.value='0';node.dispatchEvent(new Event('change',{bubbles:true}))});
  let density=await page.evaluate(()=>JSON.parse(window.__data766.AdvancedSettings));
  assert.equal(density.widgetDensityPercent,0);assert.equal(density.density,'comfortable');
  await page.locator('#advDensitySlider665').evaluate(node=>{node.value='100';node.dispatchEvent(new Event('change',{bubbles:true}))});
  density=await page.evaluate(()=>JSON.parse(window.__data766.AdvancedSettings));
  assert.equal(density.widgetDensityPercent,100);assert.equal(density.density,'compact');

  await page.locator('#settingsDone').tap();
  await page.locator('.nav[data-mode="today"]').tap();
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  const leastDense=await page.evaluate(()=>parseFloat(document.getElementById('todayList').style.minHeight));
  await page.locator('#settingsBtn').tap();await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));
  await page.evaluate(()=>{document.getElementById('textSettings86').open=true});
  await page.locator('#dayDensitySlider765').evaluate(node=>{node.value='100';node.dispatchEvent(new Event('input',{bubbles:true}))});
  await page.locator('#settingsDone').tap();await page.locator('.nav[data-mode="today"]').tap();
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  const mostDense=await page.evaluate(()=>parseFloat(document.getElementById('todayList').style.minHeight));
  assert.ok(mostDense<leastDense*.7,JSON.stringify({leastDense,mostDense}));
  density=await page.evaluate(()=>JSON.parse(window.__data766.AdvancedSettings));assert.equal(density.dayViewDensity765,100);

  await page.locator('.nav[data-mode="edit"]').tap();
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  const editHeadings=await page.evaluate(()=>[...document.querySelectorAll('#viewEdit .sectionHead')].filter(head=>/horaires des cours|class times|period times|unterrichtszeiten/i.test(head.textContent||'')).map(head=>({hidden:head.hidden,display:getComputedStyle(head).display,text:head.textContent.trim()})));
  editHeadings.forEach(item=>assert.ok(item.hidden||item.display==='none',JSON.stringify(item)));

  const gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');
  assert.match(gradle,/versionCode 772001/);assert.match(gradle,/versionName '7[.]72'/);
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({report,densityDirection:{leastDense,mostDense},editHeadings,errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
