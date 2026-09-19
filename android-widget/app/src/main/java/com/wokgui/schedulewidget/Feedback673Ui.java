package com.wokgui.schedulewidget;

/** 6.73 pass: keep the previous edit week visible until the replacement is fully painted. */
final class Feedback673Ui {
    private Feedback673Ui() {}

    static String script() {
        return """
            (function(){
              try{
                if(window.__feedback673)return;
                window.__feedback673=true;
                let coverToken=0;
                const removeCovers=()=>document.querySelectorAll('.editSwapCover673').forEach(node=>node.remove());
                const copyFormState=(source,target)=>{
                  const from=source.querySelectorAll('input,select,textarea'),to=target.querySelectorAll('input,select,textarea');
                  from.forEach((field,index)=>{const clone=to[index];if(!clone)return;try{clone.value=field.value;clone.checked=field.checked;if(field.tagName==='SELECT')clone.selectedIndex=field.selectedIndex}catch(e){}});
                };
                const beginSwap=()=>{
                  const live=document.getElementById('viewEdit');
                  if(!live||!live.parentNode||!live.classList.contains('active'))return null;
                  const title=live.querySelector('#editDayTitle'),list=live.querySelector('#editList');
                  if(!title||!title.textContent.trim()||!list||!list.childElementCount)return null;
                  const parent=live.parentNode,rect=live.getBoundingClientRect(),parentRect=parent.getBoundingClientRect();
                  if(rect.width<2||rect.height<2)return null;
                  removeCovers();
                  const cover=live.cloneNode(true),token=++coverToken;
                  copyFormState(live,cover);
                  cover.classList.add('editSwapCover673');cover.setAttribute('aria-hidden','true');
                  cover.dataset.editSwapToken673=String(token);
                  cover.style.position='absolute';cover.style.left=(rect.left-parentRect.left+parent.scrollLeft)+'px';
                  cover.style.top=(rect.top-parentRect.top+parent.scrollTop)+'px';cover.style.width=Math.max(1,Math.round(rect.width))+'px';
                  cover.style.height=Math.max(1,Math.round(rect.height))+'px';cover.style.margin='0';cover.style.pointerEvents='none';
                  cover.style.visibility='visible';cover.style.display='block';cover.style.opacity='1';cover.style.zIndex='11';
                  cover.style.overflow='hidden';cover.style.background=getComputedStyle(live).backgroundColor||'#f7f9fc';
                  if(getComputedStyle(parent).position==='static')parent.style.position='relative';
                  parent.appendChild(cover);
                  return cover;
                };
                const finishSwap=cover=>{
                  if(!cover||!cover.isConnected)return;
                  const token=Number(cover.dataset.editSwapToken673||0);
                  setTimeout(()=>requestAnimationFrame(()=>requestAnimationFrame(()=>{
                    if(token===coverToken&&cover.isConnected)cover.remove();
                  })),0);
                };
                window.beginEditSwap673=beginSwap;
                window.finishEditSwap673=finishSwap;
              }catch(e){console.error('Feedback673Ui',e)}
            })();
            """;
    }
}