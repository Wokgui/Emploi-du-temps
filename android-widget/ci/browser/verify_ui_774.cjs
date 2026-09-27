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
    const courses=[
      {start:'08:00',end:'09:00',label:'4G1 ALL · 4G2 ALL',room:'217',slot:1,color:'blue'},
      {start:'09:00',end:'10:00',label:'4G3 ALL · 4G4 ALL',room:'217',slot:2,color:'green'},
      {start:'10:00',end:'11:00',label:'6G3BIL · 6G4BIL',room:'216',slot:3,color:'violet'}
    ];
    const schedule={_currentWeek:'A',_weeks:{A:{'2':{enabled:true,courses},'3':{enabled:true,courses}}},_breaks:{gapLabel:'Trou',lunchLabel:'Midi'},_slots:[['08:00','09:00'],['09:00','10:00'],['10:00','11:00'],['11:00','12:00'],['12:00','13:00'],['13:00','14:00']].map((v,i)=>({n:i+1,start:v[0],end:v[1]}))};
    const data={UiSettings:JSON.stringify({language:'fr',theme:'blue'}),AdvancedSettings:JSON.stringify({cycleLength:2,appClassColorMode:'stripe',appColorByClass:true,widgetClassColorMode:'stripe',colorByClass:true,showBreaksToday:true,showBreaksWeek:true,showLunchToday:true,showLunchWeek:true}),Schedule:JSON.stringify(schedule)};
    window.__data774=data;window.alert=()=>{};window.confirm=()=>true;
    window.AndroidSchedule=new Proxy({},{get(target,key){if(typeof key!=='string')return;return(...args)=>{
      if(key==='saveAdvancedSettings'){data.AdvancedSettings=args[0];return true}if(key==='loadAdvancedSettings')return data.AdvancedSettings;
      if(key==='saveUiSettings'){data.UiSettings=args[0];return true}if(key==='loadUiSettings')return data.UiSettings;
      if(key==='saveSchedule'){data.Schedule=args[0];return true}if(key==='loadSchedule')return data.Schedule;
      if(key==='loadEffectiveCourses')return JSON.stringify({courses});
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
  await page.waitForFunction(()=>window.__feedback774&&window.__feedback769&&window.__weekAppearance658);
  const settle=()=>page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve))));
  const mode=async value=>{await page.evaluate(value=>{const a=JSON.parse(window.__data774.AdvancedSettings);a.appClassColorMode=value;a.appColorByClass=value!=='none';AndroidSchedule.saveAdvancedSettings(JSON.stringify(a));renderWeek();window.refreshFeedback769Views();window.refreshFeedback774()},value);await settle()};

  await page.evaluate(()=>setMode('week'));await mode('stripe');
  const stripe=await page.evaluate(()=>{
    const cells=[...document.querySelectorAll('#weekGrid .wc.has.classTint')].filter(x=>x.getBoundingClientRect().width>0).sort((a,b)=>a.getBoundingClientRect().top-b.getBoundingClientRect().top);
    return cells.map(cell=>{const r=cell.getBoundingClientRect(),p=getComputedStyle(cell,'::before');const top=r.top+parseFloat(p.top),bottom=r.bottom-parseFloat(p.bottom);return {top,bottom,cellTop:r.top,cellBottom:r.bottom,height:parseFloat(p.height),width:parseFloat(p.width),overflow:getComputedStyle(cell).overflow,color:p.backgroundColor}});
  });
  assert.ok(stripe.length>=3,JSON.stringify(stripe));for(const item of stripe){assert.equal(item.width,4);assert.equal(item.overflow,'visible');assert.ok(item.top<=item.cellTop-0.9,JSON.stringify(item));assert.ok(item.bottom>=item.cellBottom+0.9,JSON.stringify(item))}

  await mode('fill');
  const fill=await page.evaluate(()=>[...document.querySelectorAll('#weekGrid .wc.has.classFill')].map(cell=>{const s=getComputedStyle(cell);return {background:s.backgroundColor,top:s.borderTopColor,bottom:s.borderBottomColor,left:s.borderLeftColor,right:s.borderRightColor,shadow:s.boxShadow}}));
  assert.ok(fill.length>=3,JSON.stringify(fill));for(const item of fill){assert.equal(item.top,item.background,JSON.stringify(item));assert.equal(item.bottom,item.background,JSON.stringify(item));assert.equal(item.left,item.background,JSON.stringify(item));assert.equal(item.right,item.background,JSON.stringify(item));assert.equal(item.shadow,'none',JSON.stringify(item))}

  const lunch=await page.evaluate(()=>[...document.querySelectorAll('#weekGrid>.week658LunchRail')].map(node=>({kind:node.className,height:node.getBoundingClientRect().height,background:getComputedStyle(node).backgroundColor})));
  assert.ok(lunch.length>=2,JSON.stringify(lunch));assert.ok(lunch.some(x=>x.kind.includes('Top')));assert.ok(lunch.some(x=>x.kind.includes('Bottom')));for(const rail of lunch){assert.equal(rail.height,2,JSON.stringify(rail));assert.notEqual(rail.background,'rgba(0, 0, 0, 0)')}

  await page.locator('#settingsBtn').tap();await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));
  await page.evaluate(()=>{document.getElementById('colorSettings86').open=true;document.getElementById('widgetSettings86').open=true;document.getElementById('advancedSettings85').open=true;window.refreshSettingsLayout();window.refreshFeedback774()});await settle();
  const settings=await page.evaluate(()=>{
    const sheet=document.getElementById('settingsSheet').getBoundingClientRect(),title=document.getElementById('settingsTitle').getBoundingClientRect(),label=document.getElementById('advRangeLabel');label.focus();const s=getComputedStyle(label);
    return {version:document.getElementById('appVersionInfo').textContent.trim(),gear:!!document.querySelector('#settingsGear774 svg path'),titleDelta:Math.abs((title.left+title.width/2)-(sheet.left+sheet.width/2)),rows:[...document.querySelectorAll('#displayGrid767 [data-row]')].map(x=>x.textContent.trim()),theme:[...document.querySelectorAll('#themeGrid .themeButton')].map(button=>{const name=button.querySelector('.themeName');return {text:name.textContent.trim(),buttonHeight:button.getBoundingClientRect().height,nameHeight:name.getBoundingClientRect().height,overflow:name.scrollHeight-name.clientHeight}}),range:{top:s.borderTopWidth,right:s.borderRightWidth,bottom:s.borderBottomWidth,left:s.borderLeftWidth,outline:s.outlineStyle,shadow:s.boxShadow}};
  });
  assert.equal(settings.version,'Version 7.79');assert.equal(settings.gear,true);assert.ok(settings.titleDelta<=1,JSON.stringify(settings));assert.deepEqual(settings.rows,['Salle','Horaires','Temps restant','Trait de couleur par classe','Case de couleur par classe']);
  assert.ok(settings.theme.length>=10);for(const theme of settings.theme){assert.ok(theme.text.length>0);assert.ok(theme.buttonHeight>=theme.nameHeight);assert.ok(theme.overflow<=1,JSON.stringify(theme))}
  assert.deepEqual({top:settings.range.top,right:settings.range.right,bottom:settings.range.bottom,left:settings.range.left},{top:'1px',right:'1px',bottom:'1px',left:'1px'});assert.equal(settings.range.outline,'none');assert.notEqual(settings.range.shadow,'none');

  const rowXml=fs.readFileSync(path.join(root,'app/src/main/res/layout/widget_course_row.xml'),'utf8'),condensed=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java'),'utf8'),gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');
  assert.match(rowXml,/rowCondensedAccent[\s\S]{0,180}layout_height="match_parent"/);assert.match(condensed,/setViewLayoutHeight\(R[.]id[.]rowCondensedAccent, fittedHeight,/);assert.doesNotMatch(condensed,/rowCondensedAccent, Math[.]max\(1, fittedHeight - 3\)/);assert.match(gradle,/versionCode 779001/);assert.match(gradle,/versionName '7[.]79'/);
  await page.screenshot({path:path.join('smoke-browser','ui-774-settings.png'),fullPage:true});
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({stripe,fill,lunch,settings,errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
