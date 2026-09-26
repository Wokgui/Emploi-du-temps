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
      Schedule:JSON.stringify({_weeks:{A:{'2':{enabled:true,courses:[{start:'08:00',end:'09:00',label:'6G3',room:'12',slot:1,color:'violet'}]}}},_breaks:{gapLabel:'Pause',lunchLabel:'Repas'},_slots:[['08:00','09:00'],['09:00','10:00'],['10:00','11:00'],['11:00','12:00'],['13:00','14:00'],['14:00','15:00'],['16:00','17:00'],['17:00','18:00'],['18:00','19:00']].map((v,i)=>({n:i+1,start:v[0],end:v[1]}))}),
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
    await page.waitForFunction(()=>window.__feedback769&&window.__feedback768&&window.__feedback767&&window.__feedback766&&window.__edtHeavyPanels648);
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
      barsHidden:style('widgetEdgeBars672').display!=='none'&&getComputedStyle(bars.querySelector('.bar672Title')).display==='none',barLabels:[...bars.querySelectorAll('.bar672Label')].map(x=>x.textContent.trim()),barsStyle:{border:style('widgetEdgeBars672').borderTopWidth,background:style('widgetEdgeBars672').backgroundColor},barGeometry:[...bars.querySelectorAll('.bar672Label')].map(label=>{const select=document.getElementById(label.htmlFor),a=label.getBoundingClientRect(),b=select.getBoundingClientRect();return {labelCenter:Math.round(a.top+a.height/2),selectCenter:Math.round(b.top+b.height/2),selectWidth:Math.round(b.width)}}),
      removed:[...['advShowPercent','advShowProgress','advShowWeekInfo']].every(id=>!document.getElementById(id)),
      sectionResets:[...document.querySelectorAll('#resetText87,#resetColors87,#resetWidget87')].length,
      buttonText:{add:document.getElementById('addCourse').textContent.trim(),bulk:document.getElementById('addBulkCourses').textContent.trim()},
      fontSizes:{photo:style('importPhoto').fontSize,add:style('addCourse').fontSize,bulk:style('addBulkCourses').fontSize,undo:style('undoEdit86').fontSize},buttonHeights:{photo:document.getElementById('importPhoto').getBoundingClientRect().height,add:document.getElementById('addCourse').getBoundingClientRect().height,bulk:document.getElementById('addBulkCourses').getBoundingClientRect().height},
      historyHeight:document.getElementById('undoEdit86').getBoundingClientRect().height,
      columns:getComputedStyle(document.getElementById('displayGrid767')).gridTemplateColumns.split(' ').length,
      formatRemoved:!document.getElementById('advFormat'),
      followingRemoved:!document.getElementById('advFollowing'),
      headerColors:[...document.querySelectorAll('#displayGrid767 .displayHead767')].slice(1).map(x=>getComputedStyle(x).color),
      roomColor:getComputedStyle(document.querySelector('#displayGrid767 [data-row="0"]')).color,
      profileFont:parseFloat(style('advProfilesTitle').fontSize),
      representativeFonts:['#languageSelect','#appFontTitle','#widgetFontTitle','#settingsWeekCycle678>.settingTitle','#slotSettingsGroup759>.settingTitle','#themeTitle','#paletteSettingRoot>.settingTitle','.themeName','.coursePaletteName','#breakNamesTitle763','#week658LunchSettings .w658Title','#advRangeTitle','#advReminderTitle','#advExceptionsTitle','#advProfilesTitle','#advBackupTitle','#slotSettings .slotNum','#slotSettings input'].map(selector=>document.querySelector(selector)).filter(Boolean).map(node=>({text:(node.textContent||node.value||node.id).trim(),font:parseFloat(getComputedStyle(node).fontSize)})),
      themeNameStyle:(()=>{const x=document.querySelector('.themeName'),s=getComputedStyle(x);return {whiteSpace:s.whiteSpace,overflow:s.overflow,textOverflow:s.textOverflow}})(),
      barRows:[...document.querySelectorAll('#widgetEdgeBars672 .bar771Row')].map(x=>[...x.children].map(y=>y.id||y.htmlFor)),
      access:[...document.querySelectorAll('#advAppAccess767,#advAccess')].map(x=>({width:x.getBoundingClientRect().width,font:getComputedStyle(x).fontSize,text:x.options[x.selectedIndex].text}))
    };
  });
  assert.equal(german.title,'Einstellungen');assert.equal(german.display,'Anzeige');
  for(const expected of ['Sprache','Textgröße','Wochen und Zeiten','Farben','Pausenanzeige','Anzeige','Erweiterte Einstellungen'])assert.ok(german.summaries.includes(expected),JSON.stringify(german));
  assert.deepEqual(german.gridTitles,['','App','Widget']);assert.equal(german.general,'Allgemeine Anzeige');assert.equal(german.widget,'Widget');
  assert.ok(german.rows.includes('Freistunden'));assert.ok(german.rows.includes('Farbstreifen je Klasse'));assert.ok(german.rows.includes('Farbiges Feld je Klasse'));assert.equal(german.barsHidden,true);assert.deepEqual(german.barLabels,['Anzeige der oberen Leiste','Anzeige der unteren Leiste']);
  assert.ok(Math.abs(german.barGeometry[0].labelCenter-german.barGeometry[0].selectCenter)<=1);assert.ok(Math.abs(german.barGeometry[1].labelCenter-german.barGeometry[1].selectCenter)<=1);assert.ok(german.barGeometry[1].labelCenter>german.barGeometry[0].labelCenter);assert.equal(german.barGeometry[0].selectWidth,german.barGeometry[1].selectWidth);
  assert.deepEqual(german.barsStyle,{border:'0px',background:'rgba(0, 0, 0, 0)'});assert.equal(german.removed,true);assert.equal(german.sectionResets,0);assert.equal(german.columns,3);
  assert.equal(german.buttonText.bulk,'＋ Mehrere Stunden zu einer Klasse hinzufügen');assert.equal(german.fontSizes.photo,german.fontSizes.add);assert.equal(german.fontSizes.photo,german.fontSizes.bulk);assert.ok(parseFloat(german.fontSizes.photo)<14);assert.ok(german.buttonHeights.photo<=40);assert.ok(german.buttonHeights.add<=40);assert.ok(german.buttonHeights.bulk<=40);assert.ok(german.historyHeight>=48);assert.ok(parseFloat(german.fontSizes.undo)>=13);
  assert.equal(german.formatRemoved,true);assert.equal(german.followingRemoved,true);assert.deepEqual(german.headerColors,[german.roomColor,german.roomColor]);assert.deepEqual(german.barRows,[['widgetTopBarMode672','widgetTopBarMode672','widgetTopBarColor672'],['widgetBottomBarMode672','widgetBottomBarMode672','widgetBottomBarColor672']]);assert.deepEqual(german.themeNameStyle,{whiteSpace:'normal',overflow:'visible',textOverflow:'clip'});for(const item of german.representativeFonts)assert.ok(Math.abs(item.font-german.profileFont)<.15,JSON.stringify({reference:german.profileFont,item}));for(const select of german.access){assert.ok(select.width>=104);assert.ok(Math.abs(parseFloat(select.font)-german.profileFont)<.15)}

  await page.locator('#advAppShowRoom767').uncheck();
  await page.evaluate(()=>{const probe=document.createElement('div');probe.id='classColorProbe770';probe.className='editCourse';probe.innerHTML='<span class="label">6G3</span>';document.body.appendChild(probe)});
  await page.locator('#advAppClassStripe770').check();
  await page.locator('#advWidgetClassFill770').check();
  assert.equal(await page.locator('#classColorProbe770').evaluate(x=>x.classList.contains('classTint')),true);
  await page.evaluate(()=>{if(typeof renderWeek==='function')renderWeek();if(window.refreshCoursePaletteV4)window.refreshCoursePaletteV4()});
  const continuousStripe=await page.evaluate(()=>{const cell=document.querySelector('#weekGrid .wc.has');if(!cell)return null;const box=cell.getBoundingClientRect(),pseudo=getComputedStyle(cell,'::before');return {border:getComputedStyle(cell).borderLeftWidth,content:pseudo.content,height:parseFloat(pseudo.height),cellHeight:box.height,width:parseFloat(pseudo.width),color:pseudo.backgroundColor}});
  assert.ok(continuousStripe,JSON.stringify(continuousStripe));assert.equal(continuousStripe.border,'0px');assert.notEqual(continuousStripe.content,'none');assert.ok(Math.abs(continuousStripe.height-continuousStripe.cellHeight)<=1,JSON.stringify(continuousStripe));assert.equal(continuousStripe.width,4);assert.notEqual(continuousStripe.color,'rgba(0, 0, 0, 0)');
  await page.locator('#advAppClassFill770').check();
  assert.equal(await page.locator('#advAppClassStripe770').isChecked(),false);
  assert.equal(await page.locator('#classColorProbe770').evaluate(x=>x.classList.contains('classFill')),true);
  const fillContrast=await page.locator('#classColorProbe770').evaluate(node=>{const parse=value=>(String(value).match(/[\d.]+/g)||[]).slice(0,3).map(Number),channel=v=>{v/=255;return v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4)},lum=value=>{const [r,g,b]=parse(value);return .2126*channel(r)+.7152*channel(g)+.0722*channel(b)},bg=getComputedStyle(node).backgroundColor,ink=getComputedStyle(node.querySelector('.label')).color,a=lum(bg),b=lum(ink);return {bg,ink,ratio:(Math.max(a,b)+.05)/(Math.min(a,b)+.05),cssInk:node.style.getPropertyValue('--class-ink')}});
  assert.ok(fillContrast.ratio>=4,JSON.stringify(fillContrast));assert.ok(fillContrast.cssInk,JSON.stringify(fillContrast));
  await page.locator('#advAppAccess767').selectOption('high_contrast');
  await page.locator('#advAccess').selectOption('colorblind');
  const behavior=await page.evaluate(()=>({
    advanced:JSON.parse(window.__data767.AdvancedSettings),classes:document.documentElement.className,
    accessFit:[...document.querySelectorAll('#advAppAccess767,#advAccess')].map(select=>{const style=getComputedStyle(select),canvas=document.createElement('canvas'),ctx=canvas.getContext('2d');ctx.font=style.font;const measured=ctx.measureText(select.options[select.selectedIndex].text).width;const available=select.clientWidth-parseFloat(style.paddingLeft)-parseFloat(style.paddingRight);return {text:select.options[select.selectedIndex].text,measured,available}})
  }));
  assert.equal(behavior.advanced.appShowRoom,false);assert.equal(behavior.advanced.appClassColorMode,'fill');assert.equal(behavior.advanced.widgetClassColorMode,'fill');assert.equal(behavior.advanced.colorByClass,true);assert.equal(behavior.advanced.appAccessibility,'high_contrast');assert.equal(behavior.advanced.accessibility,'colorblind');
  for(const fit of behavior.accessFit)assert.ok(fit.measured<=fit.available,JSON.stringify(fit));
  assert.match(behavior.classes,/appHideRoom767/);assert.match(behavior.classes,/accessHigh/);
  await page.screenshot({path:path.join('smoke-browser','ui-771-display.png'),fullPage:true});

  const largeFontSlots=await page.evaluate(()=>{document.documentElement.style.fontSize='22.4px';const section=document.getElementById('weekTypeSettings86');if(section)section.open=true;if(typeof renderSlots==='function')renderSlots();window.refreshFeedback769();return [...document.querySelectorAll('#slotSettings .slotRemove:not([hidden])')].map(button=>{const b=button.getBoundingClientRect(),lead=button.closest('.slotLead').getBoundingClientRect();return {display:getComputedStyle(button).display,width:b.width,height:b.height,left:b.left,right:b.right,leadLeft:lead.left,leadRight:lead.right}})});
  assert.ok(largeFontSlots.length>=1,JSON.stringify(largeFontSlots));for(const cross of largeFontSlots){assert.notEqual(cross.display,'none');assert.ok(cross.width>=23&&cross.height>=23,JSON.stringify(cross));assert.ok(cross.left>=cross.leadLeft&&cross.right<=cross.leadRight+.5,JSON.stringify(cross))}
  await page.evaluate(()=>{document.documentElement.style.removeProperty('font-size');window.refreshFeedback769()});

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
  assert.match(widgetPalette,/widgetClassColorMode/);assert.match(widgetPalette,/assignedCourseColor/);assert.match(widgetPalette,/courseBackground/);assert.match(widgetPalette,/weekFreeColor/);assert.match(advancedStore,/static String widgetClassColorMode/);assert.match(advancedStore,/static String appClassColorMode/);assert.match(advancedStore,/static String weekFreeColor/);assert.match(main,/resetAllSettings/);
  const condensedProvider=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetCondensedProvider.java'),'utf8');
  const provider=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetProvider.java'),'utf8');
  const upcoming=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java'),'utf8');
  const condensed=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java'),'utf8');
  const previewMini=fs.readFileSync(path.join(root,'app/src/main/res/layout/widget_preview_mini.xml'),'utf8');const previewCondensed=fs.readFileSync(path.join(root,'app/src/main/res/layout/widget_preview_condensed.xml'),'utf8');
  const infoMini=fs.readFileSync(path.join(root,'app/src/main/res/xml/widget_info_mini.xml'),'utf8');const infoCondensed=fs.readFileSync(path.join(root,'app/src/main/res/xml/widget_info_condensed.xml'),'utf8');const rowXml=fs.readFileSync(path.join(root,'app/src/main/res/layout/widget_course_row.xml'),'utf8');const core=fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/TimetableCoreUi.java'),'utf8');
  assert.match(condensedProvider,/WidgetLayoutStore[.]set\(context, id, WidgetLayoutStore[.]FORMAT_CONDENSED\)/);assert.match(provider,/enforceProviderFormat/);assert.match(provider,/courses\/"\+format/);assert.match(upcoming,/courseLimit = AdvancedSettingsStore[.]upcomingCount/);assert.match(upcoming,/new QuoteSpan\(accent, 3, 3\)/);assert.doesNotMatch(upcoming,/out[.]append\('▌'\)/);assert.match(upcoming,/rowCourseAccent/);assert.match(condensed,/courseLimit = AdvancedSettingsStore[.]upcomingCount/);assert.match(condensed,/items[.]subList\(capacity/);assert.match(previewMini,/layout_height="60dp"/);assert.match(previewCondensed,/layout_height="72dp"/);assert.match(infoMini,/minHeight="40dp"/);assert.match(infoMini,/targetCellHeight="1"/);assert.match(infoCondensed,/minHeight="72dp"/);assert.match(infoCondensed,/targetCellHeight="1"/);assert.ok((rowXml.match(/includeFontPadding="false"/g)||[]).length>=13);assert.match(core,/[.]wc[.]classTint:before/);assert.match(core,/--class-ink/);
  const gradle=fs.readFileSync(path.join(root,'app/build.gradle'),'utf8');assert.match(gradle,/versionCode 771001/);assert.match(gradle,/versionName '7[.]71'/);
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({german,behavior,french,reset:{calls:reset.calls,slots:reset.schedule._slots.length,course:reset.schedule._weeks.A['2'].courses[0].label},errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
