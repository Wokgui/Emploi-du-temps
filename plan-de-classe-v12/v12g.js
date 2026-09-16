(function(){
  // Restore ordinary click handlers. Android WebView handles these more reliably
  // than the pointerup interception introduced in v12f.
  const mixBtn=document.getElementById('mix');
  if(mixBtn)mixBtn.onclick=mix;

  const undoBtn=document.getElementById('undo');
  if(undoBtn)undoBtn.onclick=()=>{if(!H.length)return;F.push(JSON.stringify(S));restore(H.pop())};

  const redoBtn=document.getElementById('redo');
  if(redoBtn)redoBtn.onclick=()=>{if(!F.length)return;H.push(JSON.stringify(S));restore(F.pop())};

  document.querySelectorAll('.premium-foot .nav').forEach(b=>{b.onclick=()=>showView(b.dataset.v)});

  // Re-render so all current desks receive the v5 seatClick/seatDown handlers.
  renderPlan();

  // Lightweight runtime diagnostics used only for verification.
  window.__planV12gDiagnostics={
    settingsDelegated:typeof document.getElementById('rowCfg')?.onclick==='function',
    mixBound:typeof document.getElementById('mix')?.onclick==='function',
    seatCount:document.querySelectorAll('#room .seat').length
  };
})();
