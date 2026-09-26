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
    const initialUi=localStorage.getItem('test-ui-767')||JSON.stringify({language:'de',theme:'blue',appFontScale:1.1,widgetFontScale:1.2});
    const data={
      UiSettings:initialUi,
      AdvancedSettings:JSON.stringify({cycleLength:2,showRoom:true,showTimes:true,showRemaining:true,showBreaks:true,showLunch:true,colorByClass:false,accessibility:'normal',appShowRoom:true,appShowTimes:true,appShowRemaining:true,appColorByClass:false,appAccessibility:'normal'}),
      Schedule:JSON.stringify({_weeks:{A:{'2':{enabled:true,courses:[{start:'08:00',end:'09:00',label:'6G3',room:'12',slot:1}]}}},_breaks:{gapLabel:'Pause',lunchLabel:'Repas'},_slots:[{n:1,start:'07:30',end:'08:15'}]}),
      resetCalls:0,reloadCalls:0
    };
    window.__data767=data;window.alert=()=>{};window.confirm=()=>true;
    window.AndroidSchedule=new Proxy({},{get(target,key){if(typeof key!=='string')return;return(...args)=>{
      if(key==='saveUiSettings'){data.UiSettings=args[0];localStorage.setItem('test-ui-767',args[0]);return true}
      if(key==='saveAdvancedSettings'){data.AdvancedSettings=args[0];return true}
      if(key==='saveSchedule'){data.Schedule=args[0];return true}
      if(key==='saveWidgetPalette'){data.WidgetPalette=args[0];return true}
      if(key==='saveSpecialColors'){data.SpecialColors=args[0];return true}
      if(key==='resetAllSettings'){data.resetCalls++;return true}
      if(key==='reloadForLanguage'){data.reloadCalls++;return true}
      if(key==='loadUiSettings')return data.UiSettings;
      if(key==='loadAdvancedSettings')return data.AdvancedSettings;
      if(key==='loadSchedule')return data.Schedule;
      if(key==='loadEffectiveCourses')return JSON.stringify({courses:[]});
      if(key==='listProfiles')return JSON.stringify({current:'main',profiles:[{id:'main',name:'Principal'}]});
      if(key==='loadWidgetPalette')return data.WidgetPalette||'vivid';
      if(key==='loadSpecialColors')return data.SpecialColors||'{}';
      if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks')return '[]';
      return data[key.replace(/^load/,'')]||'{}';
    }}});
  });
  const inject=async()=>{
    for(const name of fs.readdirSync(chunks).sort()){
      await page.evaluate(fs.readFileSync(path.join(chunks,name),'utf8')+'\n//# sourceURL='+name);
      await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
    }
    await page.waitForFunction(()=>window.__feedback768&&window.__feedback767&&window.__feedback766&&window.__edtHeavyPanels648);
  };
  const openSettings=async()=>{
    await page.locator('#settingsBtn').tap();
    await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));
    await page.evaluate(()=>{const d=document.getElementById('widgetSettings86');if(d)d.open=true;window.refreshFeedback767()});
  };

  await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
  await inject();await openSettings();
  const german=await page.evaluate(()=>{
    const style=id=>getComputedStyle(document.getElementById(id));
    const bars=document.getElementById('widgetEdgeBars672');
    return {
      summaries:[...document.querySelectorAll('#settingsSheet>details>summary')].map(x=>x.textContent.trim()),
      title:document.getElementById('settingsTitle').textContent.trim(),display:document.querySelector('#widgetSettings86>summary').textContent.trim(),
      gridTitles:[...document.querySelectorAll('#displayGrid767 .displayHead767')].map(x=>x.textContent.trim()),
      rows:[...document.querySelectorAll('#displayGrid767 [data-row]')].map(x=>x.textContent.trim()),
      general:document.getElementById('displayGeneralTitle767').textContent.trim(),widget:document.getElementById('displayWidgetTitle767').textContent.trim(),
      barsHidden:style('widgetEdgeBars672').display!=='none'&&getComputedStyle(bars.querySelector('.bar672Title')).display==='none',barLabels:[...bars.querySelectorAll('.bar672Label')].map(x=>x.textContent.trim()),barsStyle:{border:style('widgetEdgeBars672').borderTopWidth,background:style('widgetEdgeBars672').backgroundColor},
      removed:[...['advShowPercent','advShowProgress','advShowWeekInfo']].every(id=>!document.getElementById(id)),
      sectionResets:[...document.querySelectorAll('#resetText87,#resetColors87,#resetWidget87')].length,
      buttonText:{add:document.getElementById('addCourse').textContent.trim(),bulk:document.getElementById('addBulkCourses').textContent.trim()},
      fontSizes:{photo:style('importPhoto').fontSize,add:style('addCourse').fontSize,bulk:style('addBulkCourses').fontSize,undo:style('undoEdit86').fontSize},buttonHeights:{photo:document.getElementById('importPhoto').getBoundingClientRect().height,add:document.getElementById('addCourse').getBoundingClientRect().height,bulk:document.getElementById('addBulkCourses').getBoundingClientRect().height},
      historyHeight:document.getElementById('undoEdit86').getBoundingClientRect().height,
      columns:getComputedStyle(document.getElementById('displayGrid767')).gridTemplateColumns.split(' ').length,
      formatInside:document.getElementById('advFormat').closest('#displayWidget767')?.id,
      followingInside:document.getElementById('advFollowing').closest('#displayWidget767')?.id
    };
  });
  assert.equal(german.title,'Einstellungen');assert.equal(german.display,'Anzeige');
  for(const expected of ['Sprache','Textgröße','Wochen und Zeiten','Farben','Pausenanzeige','Anzeige','Erweiterte Einstellungen'])assert.ok(german.summaries.includes(expected),JSON.stringify(german));
  assert.deepEqual(german.gridTitles,['','App','Widget']);assert.equal(german.general,'Allgemeine Anzeige');assert.equal(german.widget,'Widget');
  assert.ok(german.rows.includes('Freistunden'));assert.ok(german.rows.includes('Farbe je Klasse'));assert.equal(german.barsHidden,true);assert.deepEqual(german.barLabels,['Anzeige der oberen Leiste','Anzeige der unteren Leiste']);
  assert.deepEqual(german.barsStyle,{border:'0px',background:'rgba(0, 0, 0, 0)'});assert.equal(german.removed,true);assert.equal(german.sectionResets,0);assert.equal(german.columns,3);
  assert.equal(german.buttonText.bulk,'＋ Mehrere Stunden zu einer Klasse hinzufügen');assert.equal(german.fontSizes.photo,german.fontSizes.add);assert.equal(german.fontSizes.photo,german.fontSizes.bulk);assert.ok(parseFloat(german.fontSizes.photo)<14);assert.ok(german.buttonHeights.photo<=40);assert.ok(german.buttonHeights.add<=40);assert.ok(german.buttonHeights.bulk<=40);assert.ok(german.historyHeight>=48);assert.ok(parseFloat(german.fontSizes.undo)>=13);
  assert.equal(german.formatInside,'displayWidget767');assert.equal(german.followingInside,'displayWidget767');

  await page.locator('#advAppShowRoom767').uncheck();
  await page.locator('#advClassColors').check();
  await page.locator('#advAppAccess767').selectOption('high_contrast');
  await page.locator('#advAccess').selectOption('colorblind');
  const behavior=await page.evaluate(()=>({advanced:JSON.parse(window.__data767.AdvancedSettings),classes:document.documentElement.className}));
  assert.equal(behavior.advanced.appShowRoom,false);assert.equal(behavior.advanced.colorByClass,true);assert.equal(behavior.advanced.appAccessibility,'high_contrast');assert.equal(behavior.advanced.accessibility,'colorblind');
  assert.match(behavior.classes,/appHideRoom767/);assert.match(behavior.classes,/accessHigh/);

  await page.locator('#languageSelect').evaluate(select=>{select.value='fr';select.dispatchEvent(new Event('change',{bubbles:true}))});
  assert.equal(await page.evaluate(()=>JSON.parse(window.__data767.UiSettings).language),'fr');
  assert.equal(await page.evaluate(()=>window.__data767.reloadCalls),1);
  await page.reload();await inject();await openSettings();
  const french=await page.evaluate(()=>({title:document.getElementById('settingsTitle').textContent.trim(),display:document.querySelector('#widgetSettings86>summary').textContent.trim(),week:document.querySelector('#weekTypeSettings86>summary').textContent.trim(),advanced:document.querySelector('#advancedSettings85>summary').textContent.trim(),bulk:document.getElementById('addBulkCourses').textContent.trim(),barTitleDisplay:getComputedStyle(document.querySelector('#widgetEdgeBars672 .bar672Title')).display,barLabels:[...document.querySelectorAll('#widgetEdgeBars672 .bar672Label')].map(x=>x.textContent.trim()),colorTitle:document.querySelector('#week658Settings .w658Title')?.textContent.trim(),colorLabels:[...document.querySelectorAll('#week658Settings .w658Colors span')].map(x=>x.textContent.trim())}));
  assert.deepEqual(french,{title:'Réglages',display:'Affichage',week:'Semaines et horaires',advanced:'Réglages avancés',bulk:'＋ Ajouter plusieurs cours à une classe',barTitleDisplay:'none',barLabels:['Affichage de la barre du haut','Affichage de la barre du bas'],colorTitle:'Couleurs des interruptions',colorLabels:['Trous','Cours','Midi']});

  await page.locator('#settingsReset').tap();
  const reset=await page.evaluate(()=>({ui:JSON.parse(window.__data767.UiSettings),advanced:JSON.parse(window.__data767.AdvancedSettings),schedule:JSON.parse(window.__data767.Schedule),calls:window.__data767.resetCalls}));
  assert.equal(reset.calls,1,JSON.stringify(reset));assert.deepEqual(reset.ui,{appFontScale:1,widgetFontScale:1,language:'fr',theme:'blue'});assert.equal(reset.advanced.appShowRoom,true);assert.equal(reset.advanced.colorByClass,false);assert.equal(reset.schedule._slots.length,9);assert.equal(reset.schedule._breaks.gapLabel,'Trou');assert.equal(reset.schedule._weeks.A['2'].courses[0].label,'6G3');

  const widgetPalette=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/WidgetPaletteStore.java'),'utf8');
  const advancedStore=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/AdvancedSettingsStore.java'),'utf8');
  const main=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/MainActivity.java'),'utf8');
  assert.match(widgetPalette,/AdvancedSettingsStore[.]colorByClass/);assert.match(widgetPalette,/weekFreeColor/);assert.match(advancedStore,/static String weekFreeColor/);assert.match(main,/resetAllSettings/);
  const gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');assert.match(gradle,/versionCode 768001/);assert.match(gradle,/versionName '7[.]68'/);
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({german,behavior,french,reset:{calls:reset.calls,slots:reset.schedule._slots.length,course:reset.schedule._weeks.A['2'].courses[0].label},errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
