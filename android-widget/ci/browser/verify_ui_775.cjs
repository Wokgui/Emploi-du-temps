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
    const data={UiSettings:JSON.stringify({language:'fr',theme:'blue'}),AdvancedSettings:JSON.stringify({widgetTopBarMode:'progress',widgetBottomBarMode:'progress'}),Schedule:'{}'};
    window.AndroidSchedule=new Proxy({},{get(target,key){if(typeof key!=='string')return;return(...args)=>{
      if(key==='loadUiSettings')return data.UiSettings;if(key==='saveUiSettings'){data.UiSettings=args[0];return true}
      if(key==='loadAdvancedSettings')return data.AdvancedSettings;if(key==='saveAdvancedSettings'){data.AdvancedSettings=args[0];return true}
      if(key==='loadSchedule')return data.Schedule;if(key==='saveSchedule'){data.Schedule=args[0];return true}
      if(key==='listProfiles')return JSON.stringify({current:'main',profiles:[{id:'main',name:'Principal'}]});
      if(key==='loadWidgetPalette')return 'vivid';if(key==='loadSpecialColors')return '{}';
      if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks')return '[]';return '{}';
    }}});
    window.alert=()=>{};window.confirm=()=>true;
  });
  await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
  for(const name of fs.readdirSync(chunks).sort()){
    await page.evaluate(fs.readFileSync(path.join(chunks,name),'utf8')+'\n//# sourceURL='+name);
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__feedback775&&window.__feedback774&&window.__feedback769);
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));
  await page.evaluate(()=>{document.getElementById('widgetSettings86').open=true;window.refreshSettingsLayout();window.refreshFeedback775()});
  await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve))));
  const ui=await page.evaluate(()=>{
    const title=document.getElementById('settingsTitle').getBoundingClientRect(),gear=document.getElementById('settingsGear774').getBoundingClientRect(),sheet=document.getElementById('settingsSheet').getBoundingClientRect();
    const labels=[...document.querySelectorAll('#widgetEdgeBars672 .bar672Label')];
    return {
      version:document.getElementById('appVersionInfo').textContent.trim(),
      titleDelta:Math.abs((title.left+title.width/2)-(sheet.left+sheet.width/2)),
      gearGap:title.left-gear.right,
      gearLeftOfTitle:gear.right<=title.left,
      labels:labels.map(label=>({text:label.textContent.trim(),lines:[...label.querySelectorAll('.bar775Line')].map(line=>({text:line.textContent,top:line.getBoundingClientRect().top}))}))
    };
  });
  assert.equal(ui.version,'Version 7.75');assert.ok(ui.titleDelta<=1,JSON.stringify(ui));assert.equal(ui.gearLeftOfTitle,true);assert.ok(ui.gearGap>=4&&ui.gearGap<=6,JSON.stringify(ui));
  assert.deepEqual(ui.labels.map(x=>x.text),['Affichage de la barre du haut','Affichage de la barre du bas']);
  for(const label of ui.labels){assert.equal(label.lines.length,2,JSON.stringify(label));assert.equal(label.lines[0].text,'Affichage de la barre');assert.ok(label.lines[1].text==='du haut'||label.lines[1].text==='du bas');assert.ok(label.lines[1].top>label.lines[0].top,JSON.stringify(label))}

  const xml=fs.readFileSync(path.join(root,'app/src/main/res/layout/widget_course_row.xml'),'utf8');
  const mini=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java'),'utf8');
  const condensed=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java'),'utf8');
  const gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');
  assert.equal((xml.match(/android:id="@\+id\/miniCell\d+"/g)||[]).length,11);
  assert.equal((xml.match(/android:id="@\+id\/miniCellText\d+"[\s\S]{0,180}android:gravity="center"/g)||[]).length,11);
  assert.equal((xml.match(/android:id="@\+id\/miniCellAccent\d+"[\s\S]{0,140}android:layout_height="match_parent"/g)||[]).length,11);
  assert.equal((xml.match(/android:id="@\+id\/miniCellAccent\d+"[\s\S]{0,180}android:visibility="gone"/g)||[]).length,11);
  assert.match(mini,/private static final int\[\] TEXT_IDS/);assert.match(mini,/private static final int\[\] ACCENT_IDS/);
  assert.match(mini,/setTextViewText\(TEXT_IDS\[i\], text\)/);assert.match(mini,/setViewVisibility\(ACCENT_IDS\[i\], stripe \? View\.VISIBLE : View\.GONE\)/);
  assert.doesNotMatch(mini,/QuoteSpan|stripeText\(/);
  const nonCourse=condensed.match(/if \(item\.type != Item\.COURSE\) \{[\s\S]*?\} else \{/);assert.ok(nonCourse);assert.match(nonCourse[0],/setViewVisibility\(R\.id\.rowCondensedAccent, View\.GONE\)/);
  assert.match(gradle,/versionCode 775001/);assert.match(gradle,/versionName '7[.]75'/);
  assert.deepEqual(errors,[]);
  await page.screenshot({path:path.join('smoke-browser','ui-775-settings.png'),fullPage:true});
  console.log(JSON.stringify({ui,miniCells:11,condensedBreakAccent:'gone',errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
