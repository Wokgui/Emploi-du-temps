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
    const data={};window.__testAndroidData=data;
    window.AndroidSchedule=new Proxy({}, {get(target,key){
      if(typeof key!=='string')return;
      return (...args)=>{
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
    const before={reloads:__edtRenderBurst650.stats.reloads,suppressed:__edtRenderBurst650.stats.reloadSuppressed};
    reloadSchedule();
    return {
      reloads:__edtRenderBurst650.stats.reloads-before.reloads,
      suppressed:__edtRenderBurst650.stats.reloadSuppressed-before.suppressed
    };
  });
  assert.equal(reloadReport.reloads,1);
  assert.ok(reloadReport.suppressed>=1,'reload must suppress its duplicate full render');

  await page.evaluate(()=>{
    window.parseOcrSchedule=()=>({count:1,days:[2],parsed:{2:[{start:'08:05',end:'09:00',label:'OCR 650',room:'Test',slot:1}]}});
    applyOcrSchedule({});
  });
  await page.waitForSelector('#ocrPreviewCorrect86');
  const beforeOcr=await page.evaluate(()=>({corrections:__edtRenderBurst650.stats.ocrCorrections,suppressed:__edtRenderBurst650.stats.ocrSuppressed}));
  await page.locator('#ocrPreviewCorrect86').tap();
  await page.waitForFunction(()=>!document.getElementById('ocrPreview86').classList.contains('show'));
  await page.waitForTimeout(100);
  const ocrReport=await page.evaluate(before=>({
    corrections:__edtRenderBurst650.stats.ocrCorrections-before.corrections,
    suppressed:__edtRenderBurst650.stats.ocrSuppressed-before.suppressed,
    imported:(weeks[activeWeek][2].courses||[]).some(c=>c.label==='OCR 650'),
    slotRows:document.querySelectorAll('#slotSettings .slotRow').length
  }),beforeOcr);
  assert.equal(ocrReport.corrections,1);
  assert.ok(ocrReport.suppressed>=2,'OCR correction must suppress two redundant full renders');
  assert.equal(ocrReport.imported,true);
  assert.equal(ocrReport.slotRows,9);
  assert.deepEqual(errors,[]);
  console.log('browser_render_bursts_650=passed',JSON.stringify({reloadReport,ocrReport}));
  await context.close();await browser.close();
})().catch(e=>{console.error(e);process.exitCode=1});
