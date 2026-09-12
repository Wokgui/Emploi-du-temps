const {chromium}=require('playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const {pathToFileURL}=require('node:url');

const chunks=path.resolve(process.env.EDT_UI_CHUNKS||'smoke-browser/chunks');
const asset=path.resolve(__dirname,'../../app/src/main/assets/index.html');

(async()=>{
  const browser=await chromium.launch({headless:true});
  const context=await browser.newContext({viewport:{width:412,height:915},isMobile:true,hasTouch:true});
  const page=await context.newPage();
  const errors=[];
  page.on('pageerror',e=>errors.push(String(e)));
  page.on('console',m=>{if(m.type()==='error')errors.push(m.text())});
  await page.addInitScript(()=>{
    const data={},calls={};window.__testAndroidData=data;window.__testAndroidCalls=calls;
    window.AndroidSchedule=new Proxy({}, {get(target,key){
      if(typeof key!=='string')return;
      return (...args)=>{
        calls[key]=(calls[key]||0)+1;
        if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
        if(key==='loadSchedule')return data.Schedule||'';
        if(key==='loadUiSettings')return data.UiSettings||'{"language":"fr","theme":"light"}';
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
  await page.waitForFunction(()=>window.__edtRenderBurst650&&window.__edtRenderPipeline650&&window.__edtLazyImportReady);
  await page.evaluate(()=>window.setModeFromAndroid('edit'));

  const reloadReport=await page.evaluate(()=>{
    const before={
      reloads:__edtRenderBurst650.stats.reloads,
      renders:__edtRenderBurst650.stats.reloadRenders,
      suppressed:__edtRenderBurst650.stats.reloadSuppressed
    };
    reloadSchedule();
    return {
      reloads:__edtRenderBurst650.stats.reloads-before.reloads,
      renders:__edtRenderBurst650.stats.reloadRenders-before.renders,
      suppressed:__edtRenderBurst650.stats.reloadSuppressed-before.suppressed
    };
  });
  assert.equal(reloadReport.reloads,1);
  assert.ok(reloadReport.renders>=2,'legacy reload path must expose the duplicate render being optimized');
  assert.ok(reloadReport.suppressed>=1,'reload must suppress its duplicate full render');

  // The current photo-import path is ImportReviewUi, not the obsolete OcrPreviewUi.
  // It already needs just one save; protect that invariant so future changes do not
  // reintroduce the old save -> setMode -> render -> day-click chain.
  await page.evaluate(()=>{
    window.parseOcrSchedule=()=>({
      count:1,days:[2],quality:'high',warningCodes:[],
      parsed:{2:[{start:'08:05',end:'09:00',label:'OCR 650',room:'Test',slot:1,confidence:1}]}
    });
    applyOcrSchedule({});
  });
  await page.waitForSelector('#edtImportReview.show .irApply');
  assert.equal(await page.locator('#ocrPreview86.show').count(),0,'obsolete OCR preview must not be the active import path');
  const beforeImport=await page.evaluate(()=>({
    native:__testAndroidCalls.saveSchedule||0,
    saves:__edtRenderPipeline650.stats.saveExecutions
  }));
  await page.locator('#edtImportReview .irApply').tap();
  await page.waitForFunction(()=>!document.getElementById('edtImportReview').classList.contains('show'));
  await page.waitForTimeout(80);
  const importReport=await page.evaluate(before=>({
    nativeWrites:(__testAndroidCalls.saveSchedule||0)-before.native,
    saveExecutions:__edtRenderPipeline650.stats.saveExecutions-before.saves,
    imported:(weeks[activeWeek][2].courses||[]).some(c=>c.label==='OCR 650'),
    slotRows:document.querySelectorAll('#slotSettings .slotRow').length
  }),beforeImport);
  assert.equal(importReport.nativeWrites,1,'current import review must persist exactly once');
  assert.equal(importReport.saveExecutions,1,'current import review must execute one save');
  assert.equal(importReport.imported,true);
  assert.equal(importReport.slotRows,9);
  assert.deepEqual(errors,[]);
  console.log('browser_render_bursts_650=passed',JSON.stringify({reloadReport,importReport}));
  await context.close();await browser.close();
})().catch(e=>{console.error(e);process.exitCode=1});
