package com.wokgui.schedulewidget;

/**
 * Production diagnostics and deterministic soak runner for the two expensive sheets.
 *
 * <p>The prelude is deliberately installed before every legacy UI layer so a benchmark can
 * prove that opening a sheet does not register more listeners or observers over time. The
 * runtime probe is installed last and measures from pointer-down until a painted, mutation-
 * quiet frame. It does not delay or otherwise alter the application code being measured.</p>
 */
final class HeavyPanelPerformanceUi648 {
    private HeavyPanelPerformanceUi648() {}

    static String prelude() {
        return """
            (function(){
              try{
                if(window.__edtHeavyPerfPrelude648)return;
                const counters={listenerAdds:0,mutationObservers:0,resizeObservers:0,errors:0,
                  bridgeCalls:0,bridgeReads:0,bridgeWrites:0,storageReads:0,storageWrites:0,bridgeWrapped:false,bridgeByName:Object.create(null)};
                const nativeAdd=EventTarget.prototype.addEventListener;
                const nativeConsoleError=console.error.bind(console);
                console.error=function(){counters.errors++;return nativeConsoleError.apply(console,arguments)};
                const input={pointerdown:null};
                nativeAdd.call(document,'pointerdown',function(event){
                  const callback=input.pointerdown;if(callback)callback(event,performance.now());
                },{capture:true,passive:true});
                EventTarget.prototype.addEventListener=function(){
                  counters.listenerAdds++;
                  return nativeAdd.apply(this,arguments);
                };
                if(typeof window.MutationObserver==='function'){
                  const NativeMutationObserver=window.MutationObserver;
                  window.MutationObserver=new Proxy(NativeMutationObserver,{
                    construct(Target,args){counters.mutationObservers++;return Reflect.construct(Target,args)}
                  });
                }
                if(typeof window.ResizeObserver==='function'){
                  const NativeResizeObserver=window.ResizeObserver;
                  window.ResizeObserver=new Proxy(NativeResizeObserver,{
                    construct(Target,args){counters.resizeObservers++;return Reflect.construct(Target,args)}
                  });
                }
                if(typeof window.Storage==='function'){
                  const nativeGet=Storage.prototype.getItem,nativeSet=Storage.prototype.setItem,nativeRemove=Storage.prototype.removeItem,nativeClear=Storage.prototype.clear;
                  Storage.prototype.getItem=function(){counters.storageReads++;return nativeGet.apply(this,arguments)};
                  Storage.prototype.setItem=function(){counters.storageWrites++;return nativeSet.apply(this,arguments)};
                  Storage.prototype.removeItem=function(){counters.storageWrites++;return nativeRemove.apply(this,arguments)};
                  Storage.prototype.clear=function(){counters.storageWrites++;return nativeClear.apply(this,arguments)};
                }
                const nativeBridge=window.AndroidSchedule;
                if(nativeBridge&&typeof window.Proxy==='function'){
                  const wrappers=new Map();
                  const proxy=new Proxy(nativeBridge,{get:function(target,property){
                    const value=target[property];if(typeof value!=='function')return value;
                    if(!wrappers.has(property))wrappers.set(property,function(){
                      const name=String(property);counters.bridgeCalls++;counters.bridgeByName[name]=(counters.bridgeByName[name]||0)+1;
                      if(/^(load|list|supported|download)/.test(name))counters.bridgeReads++;else counters.bridgeWrites++;
                      return value.apply(target,arguments);
                    });
                    return wrappers.get(property);
                  }});
                  try{window.AndroidSchedule=proxy;counters.bridgeWrapped=window.AndroidSchedule===proxy}catch(e){}
                }
                window.__edtHeavyPerfPrelude648={counters:counters,nativeAdd:nativeAdd,nativeConsoleError:nativeConsoleError,input:input};
              }catch(e){console.log('HeavyPanelPerformanceUi648 prelude',e)}
            })();
            """;
    }

    static String script() {
        return """
            (function(){
              try{
                if(window.__edtHeavyPanelMetrics648)return;
                const prelude=window.__edtHeavyPerfPrelude648||{counters:{listenerAdds:0,mutationObservers:0,resizeObservers:0,errors:0}};
                const counters=prelude.counters;
                const checkpoints=new Set([1,20,100,300]);
                const panelElements={settings:document.getElementById('settingsModal'),course:document.getElementById('modal')};
                const panelRoots={settings:document.getElementById('settingsSheet'),course:document.getElementById('courseForm')};
                const activity={settings:null,course:null};
                const mutations={settings:0,course:0,main:0};
                const mutationAt={settings:performance.now(),course:performance.now(),main:performance.now()};
                const added={settings:0,course:0,main:0};
                const removed={settings:0,course:0,main:0};
                const renderCounts={all:0,today:0,week:0,edit:0};
                const samples={settings:{open:[],close:[]},course:{open:[],close:[]}};
                const counts={settings:{open:0,close:0},course:{open:0,close:0}};
                const waiters=new Map();
                let scenario='physical';

                function observeMutations(name,root){
                  if(!root)return;
                  new MutationObserver(function(records){
                    mutations[name]+=records.length;mutationAt[name]=performance.now();
                    records.forEach(function(record){
                      added[name]+=record.addedNodes?record.addedNodes.length:0;
                      removed[name]+=record.removedNodes?record.removedNodes.length:0;
                    });
                  }).observe(root,{childList:true,subtree:true,characterData:true,attributes:true});
                }
                observeMutations('settings',panelRoots.settings);
                observeMutations('course',panelRoots.course);
                observeMutations('main',document.querySelector('main.wrap'));

                function wrapRender(name,key){
                  const old=window[name];if(typeof old!=='function'||old.__edtHeavyMetrics648)return;
                  const wrapped=function(){renderCounts[key]++;return old.apply(this,arguments)};
                  wrapped.__edtHeavyMetrics648=true;wrapped.__edtHeavyMetricsOriginal=old;
                  window[name]=wrapped;
                  try{eval(name+'=wrapped')}catch(e){}
                }
                wrapRender('render','all');wrapRender('renderToday','today');wrapRender('renderWeek','week');wrapRender('renderEdit','edit');

                function totalRenders(){return renderCounts.all+renderCounts.today+renderCounts.week+renderCounts.edit}
                function panelOpen(name){
                  const el=panelElements[name];if(!el)return false;
                  if(el.hasAttribute('data-edt-open'))return el.getAttribute('data-edt-open')==='true';
                  return el.classList.contains('show');
                }
                function panelReady(name,action){
                  const modal=panelElements[name],root=panelRoots[name];if(!modal||!root)return false;
                  if(action==='close')return !panelOpen(name);
                  if(!panelOpen(name))return false;
                  const rect=root.getBoundingClientRect();
                  if(rect.width<1||rect.height<1)return false;
                  if(name==='settings'){
                    const close=document.getElementById('settingsX'),language=document.getElementById('languageSelect');
                    return !!(close&&language&&!close.disabled&&!language.disabled);
                  }
                  const cancel=document.getElementById('cancelEdit'),label=document.getElementById('fLabel'),slot=document.getElementById('fSlot');
                  return !!(cancel&&label&&slot&&!cancel.disabled&&!label.disabled&&!slot.disabled&&slot.options.length>0);
                }
                function classify(target){
                  const el=target&&target.closest?target.closest('button,.editCourse,.todayCourse,.wc,#settingsModal,#modal'):null;
                  if(!el)return null;
                  if(el.id==='settingsBtn')return ['settings','open'];
                  if(el.id==='settingsX'||el.id==='settingsDone'||el.id==='settingsModal')return ['settings','close'];
                  if(el.id==='cancelEdit'||el.id==='modal')return ['course','close'];
                  if(el.id==='addCourse'||el.classList.contains('editCourse')||el.classList.contains('todayCourse')||el.classList.contains('wc'))return ['course','open'];
                  return null;
                }
                function begin(panel,action,started){
                  if(!panelElements[panel])return null;
                  const active=activity[panel];
                  if(active&&active.action===action){
                    if(started&&started<active.started)active.started=started;
                    return active;
                  }
                  const n=++counts[panel][action];
                  const pending={panel:panel,action:action,n:n,started:started||performance.now(),
                    mutationStart:mutations[panel],mainStart:mutations.main,addedStart:added[panel],removedStart:removed[panel],
                    renderStart:totalRenders(),listenerStart:counters.listenerAdds,observerStart:counters.mutationObservers,
                    resizeStart:counters.resizeObservers,bridgeStart:counters.bridgeCalls,bridgeReadStart:counters.bridgeReads,
                    bridgeWriteStart:counters.bridgeWrites,storageReadStart:counters.storageReads,storageWriteStart:counters.storageWrites,firstMs:null};
                  activity[panel]=pending;
                  if(checkpoints.has(n))console.log('EDT_HEAVY_INPUT|scenario='+scenario+'|panel='+panel+'|action='+action+'|n='+n);
                  return pending;
                }
                function percentile(values,p){
                  if(!values.length)return 0;const sorted=values.slice().sort(function(a,b){return a-b});
                  return sorted[Math.min(sorted.length-1,Math.max(0,Math.ceil(sorted.length*p)-1))];
                }
                function rounded(value){return Math.round(value*10)/10}
                function finish(pending){
                  if(activity[pending.panel]!==pending)return;
                  const now=performance.now();
                  const result={panel:pending.panel,action:pending.action,n:pending.n,
                    firstMs:rounded(pending.firstMs==null?now-pending.started:pending.firstMs),readyMs:rounded(now-pending.started),
                    mutations:mutations[pending.panel]-pending.mutationStart,mainMutations:mutations.main-pending.mainStart,
                    added:added[pending.panel]-pending.addedStart,removed:removed[pending.panel]-pending.removedStart,
                    renders:totalRenders()-pending.renderStart,listeners:counters.listenerAdds-pending.listenerStart,
                    observers:counters.mutationObservers-pending.observerStart,resizeObservers:counters.resizeObservers-pending.resizeStart,
                    bridgeCalls:counters.bridgeCalls-pending.bridgeStart,bridgeReads:counters.bridgeReads-pending.bridgeReadStart,
                    bridgeWrites:counters.bridgeWrites-pending.bridgeWriteStart,storageReads:counters.storageReads-pending.storageReadStart,
                    storageWrites:counters.storageWrites-pending.storageWriteStart};
                  samples[pending.panel][pending.action].push(result);activity[pending.panel]=null;
                  if(checkpoints.has(result.n)){
                    console.log('EDT_HEAVY_CHECKPOINT|scenario='+scenario+'|panel='+result.panel+'|action='+result.action+'|n='+result.n+
                      '|firstMs='+result.firstMs+'|readyMs='+result.readyMs+'|mutations='+result.mutations+'|mainMutations='+result.mainMutations+
                      '|added='+result.added+'|removed='+result.removed+'|renders='+result.renders+'|listeners='+result.listeners+
                      '|observers='+result.observers+'|resizeObservers='+result.resizeObservers+'|bridgeCalls='+result.bridgeCalls+
                      '|bridgeReads='+result.bridgeReads+'|bridgeWrites='+result.bridgeWrites+'|storageReads='+result.storageReads+'|storageWrites='+result.storageWrites);
                  }
                  const key=result.panel+'|'+result.action+'|'+result.n,resolve=waiters.get(key);
                  if(resolve){waiters.delete(key);resolve(result)}
                  window.dispatchEvent(new CustomEvent('edt-heavy-sample',{detail:result}));
                }
                function settle(pending){
                  let stableFrames=0,lastMutation=mutations[pending.panel],frames=0;
                  function frame(){
                    if(activity[pending.panel]!==pending)return;
                    frames++;
                    const now=performance.now(),ready=panelReady(pending.panel,pending.action);
                    if(pending.firstMs==null&&ready)pending.firstMs=now-pending.started;
                    if(ready&&mutations[pending.panel]===lastMutation&&now-mutationAt[pending.panel]>=12)stableFrames++;
                    else stableFrames=0;
                    lastMutation=mutations[pending.panel];
                    if(stableFrames>=2||frames>=180){finish(pending);return}
                    requestAnimationFrame(frame);
                  }
                  requestAnimationFrame(frame);
                }
                function transition(panel,open){
                  const action=open?'open':'close';let pending=activity[panel];
                  if(!pending||pending.action!==action)pending=begin(panel,action,performance.now());
                  settle(pending);
                }

                Object.keys(panelElements).forEach(function(name){
                  const modal=panelElements[name];if(!modal)return;
                  let open=panelOpen(name);
                  new MutationObserver(function(){const next=panelOpen(name);if(next===open)return;open=next;transition(name,next)}).observe(modal,{attributes:true,attributeFilter:['class','data-edt-open']});
                });

                function captureInput(event,started){
                  const hit=classify(event.target);if(hit)begin(hit[0],hit[1],started||performance.now());
                }
                if(prelude.input)prelude.input.pointerdown=captureInput;
                else document.addEventListener('pointerdown',function(event){captureInput(event,performance.now())},{capture:true,passive:true});
                window.addEventListener('error',function(){counters.errors++});
                window.addEventListener('unhandledrejection',function(){counters.errors++});

                function waitFor(panel,action,n){
                  return new Promise(function(resolve,reject){
                    const key=panel+'|'+action+'|'+n;waiters.set(key,resolve);
                    let frames=0;function watchdog(){if(!waiters.has(key))return;if(++frames>360){waiters.delete(key);reject(new Error('sample timeout '+key));return}requestAnimationFrame(watchdog)}requestAnimationFrame(watchdog);
                  });
                }
                function press(el,panel,action){
                  if(!el)return Promise.reject(new Error('missing '+panel+' '+action+' control'));
                  const n=counts[panel][action]+1,promise=waitFor(panel,action,n);
                  const init={bubbles:true,cancelable:true,pointerId:1,pointerType:'touch',isPrimary:true,button:0,buttons:1};
                  el.dispatchEvent(new PointerEvent('pointerdown',init));
                  el.dispatchEvent(new PointerEvent('pointerup',Object.assign({},init,{buttons:0})));
                  el.click();
                  return promise;
                }
                function frame(){return new Promise(function(resolve){requestAnimationFrame(resolve)})}
                async function idle(){
                  for(let i=0;i<480&&window.__edtLazyImportScheduled&&!window.__edtLazyImportReady;i++)await frame();
                  await frame();await frame();await frame();
                }
                function startScenario(name){
                  scenario=name;
                  ['settings','course'].forEach(function(panel){['open','close'].forEach(function(action){samples[panel][action].length=0;counts[panel][action]=0});activity[panel]=null});
                  return {listeners:counters.listenerAdds,observers:counters.mutationObservers,resizeObservers:counters.resizeObservers,
                    nodes:document.getElementsByTagName('*').length,errors:counters.errors,renders:totalRenders(),mainMutations:mutations.main};
                }
                function summarize(panel,before){
                  const opens=samples[panel].open,closes=samples[panel].close;
                  const openReady=opens.map(function(x){return x.readyMs}),openFirst=opens.map(function(x){return x.firstMs}),closeReady=closes.map(function(x){return x.readyMs});
                  const headReady=openReady.slice(0,Math.min(20,openReady.length)),tailReady=openReady.slice(Math.max(0,openReady.length-20));
                  const mutationTotal=opens.concat(closes).reduce(function(n,x){return n+x.mutations},0);
                  const mainMutationTotal=opens.concat(closes).reduce(function(n,x){return n+x.mainMutations},0);
                  const renderTotal=opens.concat(closes).reduce(function(n,x){return n+x.renders},0);
                  const bridgeTotal=opens.concat(closes).reduce(function(n,x){return n+x.bridgeCalls},0);
                  const bridgeReadTotal=opens.concat(closes).reduce(function(n,x){return n+x.bridgeReads},0);
                  const bridgeWriteTotal=opens.concat(closes).reduce(function(n,x){return n+x.bridgeWrites},0);
                  const storageReadTotal=opens.concat(closes).reduce(function(n,x){return n+x.storageReads},0);
                  const storageWriteTotal=opens.concat(closes).reduce(function(n,x){return n+x.storageWrites},0);
                  const out={panel:panel,openN:opens.length,closeN:closes.length,openFirstP50:rounded(percentile(openFirst,.5)),
                    openFirstP95:rounded(percentile(openFirst,.95)),openFirstMax:rounded(percentile(openFirst,1)),
                    openReadyP50:rounded(percentile(openReady,.5)),openReadyP95:rounded(percentile(openReady,.95)),openReadyMax:rounded(percentile(openReady,1)),
                    openHeadP50:rounded(percentile(headReady,.5)),openTailP50:rounded(percentile(tailReady,.5)),
                    closeReadyP50:rounded(percentile(closeReady,.5)),closeReadyP95:rounded(percentile(closeReady,.95)),closeReadyMax:rounded(percentile(closeReady,1)),
                    mutationTotal:mutationTotal,mainMutationTotal:mainMutationTotal,renderTotal:renderTotal,
                    bridgeTotal:bridgeTotal,bridgeReadTotal:bridgeReadTotal,bridgeWriteTotal:bridgeWriteTotal,
                    storageReadTotal:storageReadTotal,storageWriteTotal:storageWriteTotal,
                    listenerDelta:counters.listenerAdds-before.listeners,observerDelta:counters.mutationObservers-before.observers,
                    resizeObserverDelta:counters.resizeObservers-before.resizeObservers,nodeDelta:document.getElementsByTagName('*').length-before.nodes,
                    errorDelta:counters.errors-before.errors};
                  console.log('EDT_HEAVY_SUMMARY|scenario='+scenario+'|panel='+panel+'|openN='+out.openN+'|closeN='+out.closeN+
                    '|openFirstP50='+out.openFirstP50+'|openFirstP95='+out.openFirstP95+'|openFirstMax='+out.openFirstMax+
                    '|openReadyP50='+out.openReadyP50+'|openReadyP95='+out.openReadyP95+'|openReadyMax='+out.openReadyMax+
                    '|openHeadP50='+out.openHeadP50+'|openTailP50='+out.openTailP50+
                    '|closeReadyP50='+out.closeReadyP50+'|closeReadyP95='+out.closeReadyP95+'|closeReadyMax='+out.closeReadyMax+
                    '|mutations='+out.mutationTotal+'|mainMutations='+out.mainMutationTotal+'|renders='+out.renderTotal+
                    '|bridgeCalls='+out.bridgeTotal+'|bridgeReads='+out.bridgeReadTotal+'|bridgeWrites='+out.bridgeWriteTotal+
                    '|storageReads='+out.storageReadTotal+'|storageWrites='+out.storageWriteTotal+
                    '|listenerDelta='+out.listenerDelta+'|observerDelta='+out.observerDelta+'|resizeObserverDelta='+out.resizeObserverDelta+
                    '|nodeDelta='+out.nodeDelta+'|errorDelta='+out.errorDelta);
                  return out;
                }
                async function settingsCycle(){
                  await press(document.getElementById('settingsBtn'),'settings','open');
                  await press(document.getElementById('settingsX'),'settings','close');
                }
                async function courseCycle(){
                  const target=document.querySelector('#editList .editCourse')||document.getElementById('addCourse');
                  await press(target,'course','open');
                  await press(document.getElementById('cancelEdit'),'course','close');
                }
                async function runBenchmark(name,cycles){
                  cycles=Math.max(1,Math.min(300,Number(cycles)||300));await idle();
                  const before=startScenario(name);console.log('EDT_HEAVY_BENCHMARK|scenario='+name+'|status=ready|cycles='+cycles);
                  for(let i=0;i<60;i++)await frame();
                  if(name==='settings')for(let i=0;i<cycles;i++)await settingsCycle();
                  else if(name==='course')for(let i=0;i<cycles;i++)await courseCycle();
                  else if(name==='mixed')for(let i=0;i<cycles;i++){if(i%2===0)await settingsCycle();else await courseCycle()}
                  else throw new Error('unknown benchmark '+name);
                  const reports=[];if(name!=='course')reports.push(summarize('settings',before));if(name!=='settings')reports.push(summarize('course',before));
                  console.log('EDT_HEAVY_BENCHMARK|scenario='+name+'|status=complete|cycles='+cycles+'|errors='+(counters.errors-before.errors));
                  return reports;
                }

                window.__edtHeavyPanelMetrics648={counters:counters,samples:samples,counts:counts,begin:begin,transition:transition,panelOpen:panelOpen,run:runBenchmark};
                window.runHeavyPanelBenchmark648=function(name,cycles){
                  if(window.__edtHeavyBenchmarkRunning648)return false;window.__edtHeavyBenchmarkRunning648=true;
                  runBenchmark(name,cycles).catch(function(error){console.error('EDT_HEAVY_BENCHMARK|scenario='+name+'|status=error|message='+error);});return true;
                };
                console.log('EDT_HEAVY_METRICS|ready|listeners='+counters.listenerAdds+'|observers='+counters.mutationObservers+'|resizeObservers='+counters.resizeObservers+'|bridgeWrapped='+(counters.bridgeWrapped?1:0));
              }catch(e){console.error('HeavyPanelPerformanceUi648',e)}
            })();
            """;
    }
}
