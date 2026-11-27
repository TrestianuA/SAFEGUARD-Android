const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const {pathToFileURL} = require('node:url');
const path = require('node:path');
(async()=>{
 const browser=await chromium.launch();
 const failures=[];
 try {
 for(const lang of ['ro','en']) for(const width of [320,360,412]){
 const page=await browser.newPage({viewport:{width,height:800},deviceScaleFactor:1});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 try{
 await page.goto(pathToFileURL(path.resolve('docs/foniwise/visual-archive'+(lang==='en'?'-en':'')+'.html')).href);
 for(const id of ['38','04','05','06','39','40']){
 try{
 const reset=page.locator('#v'+id+'reset');
 const section=reset.locator('xpath=ancestor::section[1]');
 await reset.click();
 const snapshot=()=>section.evaluate(s=>JSON.stringify({svg:[...s.querySelectorAll('svg')].map(x=>x.outerHTML),controls:[...s.querySelectorAll('input,select,button')].map(x=>[x.value,x.checked,x.getAttribute('aria-pressed')]),outputs:[...s.querySelectorAll('output')].map(x=>x.textContent)}));
 const initial=await snapshot();
 const diagrams=section.locator('.security-diagram');
 for(let i=0;i<await diagrams.count();i++){
 const diagram=diagrams.nth(i);
 if(id==='05') await section.locator('[data-v05-select="v05mode"][data-index="'+i+'"]').click();
 assert.equal(await diagram.isVisible(),true,'diagram visible before keyboard test');
 const toggle=diagram.locator('xpath=preceding-sibling::button[1]');
 assert.equal(await diagram.evaluate(d=>d.scrollWidth<=d.clientWidth+1),true,'full diagram initially visible');
 await toggle.focus();await toggle.press('Enter');
 assert.equal(await toggle.getAttribute('aria-pressed'),'true');
 assert.equal(await diagram.evaluate(d=>d.scrollWidth>d.clientWidth),true,'enlargement scrollable');
 await diagram.focus();
 await diagram.evaluate(d=>d.scrollLeft=d.scrollWidth);
 assert.equal(await diagram.evaluate(d=>d.scrollLeft>0),true);
 await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-zoom-'+i+'.png'});
 await toggle.focus();await toggle.press('Space');
 assert.equal(await toggle.getAttribute('aria-pressed'),'false');
 assert.equal(await diagram.evaluate(d=>d.scrollWidth<=d.clientWidth+1),true);
 }
 if(id==='06'){
 for(const n of [4,6,8]){
 await section.locator('[data-control="v06length"][data-index="'+([4,6,8].indexOf(n))+'"]').click();
 assert.equal(Number((await page.locator('#v06count').textContent()).replace(/[^0-9]/g,'')),10**n);
 }
 }
 await reset.click();
 assert.equal(await snapshot(),initial);
 const buttons=section.locator('button[data-control]');
 for(let i=0;i<await buttons.count();i++){
 const b=buttons.nth(i);await b.click();assert.equal(await b.getAttribute('aria-pressed'),'true');
 }
 if(id==='04'){await page.locator('#v04toggle').click();assert.equal(await page.locator('#v04paper').getAttribute('fill'),'#064D2B');}
 if(id==='05'){
 for(let mode=0;mode<2;mode++) for(let part=0;part<3;part++){
 await section.locator('[data-v05-select="v05mode"][data-index="'+mode+'"]').click();
 await section.locator('[data-v05-select="v05part"][data-index="'+part+'"]').click();
 assert.equal(await page.locator('#v05e2ee').isVisible(),mode===1);
 assert.equal(await page.locator('#v05tls').isVisible(),mode===0);
 assert.equal(await section.locator('[data-v05-select="v05part"][data-index="'+part+'"]').getAttribute('aria-pressed'),'true');
 await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V05-'+mode+'-'+part+'.png'});
 }
 }
 if(id==='39'){await page.locator('#v39a').check();await page.locator('#v39b').check();}
 if(id==='40'){await page.locator('#v40threshold').fill('79');assert.match(await page.locator('#v40status').textContent(),/respins|rejected/i);}
 await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-changed.png'});
 await reset.click();assert.equal(await snapshot(),initial,'reset differs');
 await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-initial.png'});
 const overflow=await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1);
 assert.equal(overflow,false,'section overflow');
 console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of ['12','13','14']){
 try{
 const reset=page.locator('#v'+id+'reset'), section=reset.locator('xpath=ancestor::section[1]');
 await reset.click();
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 await capture('initial');
 if(id==='12'){
 const toggle=section.locator('#v12zoom'), diagram=section.locator('.security-diagram');
 assert.equal(await diagram.evaluate(d=>d.scrollWidth<=d.clientWidth+1),true,'V12 full diagram');
 await toggle.focus();await toggle.press('Enter');
 assert.equal(await toggle.getAttribute('aria-pressed'),'true');
 assert.equal(await diagram.evaluate(d=>d.scrollWidth>d.clientWidth),true);
 await diagram.evaluate(d=>d.scrollLeft=d.scrollWidth);
 assert.equal(await diagram.evaluate(d=>d.scrollLeft>0),true);
 await capture('zoom');
 await reset.click();
 assert.equal(await toggle.getAttribute('aria-pressed'),'false');
 assert.equal(await diagram.evaluate(d=>d.scrollLeft===0&&d.scrollWidth<=d.clientWidth+1),true);
 }
 if(id==='13'){
 assert.deepEqual(await section.locator('[data-compare="1"] > span:last-child').evaluateAll(xs=>xs.map(x=>x.textContent)),lang==='ro'?['serviciu.example','imitatie.example']:['service.example','imitation.example']);
 }
 if(id!=='14'){
 const buttons=section.locator('[data-choice]'), detail=section.locator('#v'+id+'detail'), initial=await detail.textContent(), seen=new Set();
 assert.equal(await buttons.count(),id==='12'?4:3);
 for(let i=0;i<await buttons.count();i++){
 const b=buttons.nth(i);await b.focus();await b.press('Enter');
 assert.equal(await b.getAttribute('aria-pressed'),'true');
 assert.equal(await section.locator('[aria-pressed="true"]').count(),1);
 const result=await detail.textContent();assert.notEqual(result,initial);assert.equal(seen.has(result),false);seen.add(result);
 if(id==='12')assert.equal(await section.locator('svg rect').nth(i+1).getAttribute('stroke-width'),'3');
 else assert.equal(await section.locator('[data-compare="'+i+'"]').evaluateAll(xs=>xs.length===2&&xs.every(x=>x.style.outlineStyle==='solid')),true);
 await capture('choice-'+i);
 }
 await reset.click();assert.equal(await detail.textContent(),initial);assert.equal(await section.locator('[aria-pressed="true"]').count(),0);
 if(id==='12')assert.equal(await section.locator('svg rect[stroke-width="3"]').count(),0);
 else assert.equal(await section.locator('[data-compare]').evaluateAll(xs=>xs.every(x=>!x.style.outline)),true);
 }else{
 const fp=section.locator('#fp'),tp=section.locator('#tp');
 for(const slider of [fp,tp])assert.equal(await slider.evaluate(x=>x.getBoundingClientRect().height>=44),true,'range height >=44');
 const check=async(f,t)=>{
 assert.equal(await fp.inputValue(),String(f));assert.equal(await tp.inputValue(),String(t));
 for(const [grid,n,color] of [['fpgrid',f,'rgb(201, 104, 70)'],['tpgrid',t,'rgb(8, 124, 38)']]){
 const cells=section.locator('#'+grid+' span');assert.equal(await cells.count(),100);
 assert.equal(await cells.evaluateAll((xs,c)=>xs.filter(x=>x.style.background===c).length,color),n);
 }
 assert.deepEqual((await section.locator('#deepresult').textContent()).match(/\d+/g).map(Number),[f,t,100-t]);
 };
 await check(5,90);
 for(const [f,t] of [[0,90],[100,90],[100,0],[100,100],[0,100]]){await fp.fill(String(f));await tp.fill(String(t));await check(f,t);await capture(f+'-'+t);}
 await reset.click();await check(5,90);
 }
 await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of ['15','16','17','27','41','42','43']){
 try{
 const section=page.locator('#connect-v'+id), reset=section.locator('#v'+id+'reset');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 await reset.click();await capture('initial');
 if(id==='15'){
 const wan=section.locator('#wan'),status=section.locator('#wanstatus'),link=section.locator('#uplink'),broken=section.locator('#broken');
 const initial=await status.textContent();
 assert.equal(await wan.isChecked(),true);assert.equal(await link.getAttribute('stroke'),'#087C26');assert.equal(await broken.getAttribute('visibility'),'hidden');
 await wan.focus();await wan.press('Space');assert.equal(await wan.isChecked(),false);assert.equal(await link.getAttribute('stroke'),'#C96846');assert.equal(await broken.getAttribute('visibility'),'visible');assert.notEqual(await status.textContent(),initial);
 assert.match(await status.textContent(),lang==='ro'?/Wi-Fi activ; internet indisponibil/:/Wi-Fi active; internet unavailable/);
 await capture('offline');await reset.click();assert.equal(await wan.isChecked(),true);assert.equal(await status.textContent(),initial);assert.equal(await broken.getAttribute('visibility'),'hidden');
 }else if(id==='17'){
 const rate=section.locator('#rate'),duration=section.locator('#duration');
 const check=async(r,t)=>{
 assert.equal(await rate.inputValue(),String(r));assert.equal(await duration.inputValue(),String(t));
 assert.equal(await section.locator('#ratevalue').textContent(),String(r));assert.equal(await section.locator('#durationvalue').textContent(),String(t));
 assert.equal(await section.locator('#durationunit').textContent(),lang==='ro'?(t===1?'minut':t>=20?'de minute':'minute'):(t===1?'minute':'minutes'));
 const v=r*t*60/8;
 assert.equal(await section.locator('#volume').textContent(),v.toLocaleString(lang==='ro'?'ro-RO':'en-US'));
 assert.ok(Math.abs(await section.locator('#volbar').evaluate(x=>parseFloat(x.style.width))-v/90)<0.001);
 };
 for(const slider of [rate,duration])assert.ok(await slider.evaluate(x=>x.getBoundingClientRect().height>=44));
 await check(5,10);
 for(const [r,t] of [[1,1],[20,1],[1,60],[20,60],[5,11]]){await rate.fill(String(r));await duration.fill(String(t));await check(r,t);await capture(r+'-'+t);}
 await reset.click();await check(5,10);
 }else{
 const buttons=section.locator('[data-choice]'),detail=section.locator('#v'+id+'detail'),initial=await detail.textContent(),seen=new Set();
 const count=id==='16'?4:3;
 assert.equal(await buttons.count(),count);
 if(['41','42','43'].includes(id)){
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 assert.ok(await buttons.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 }
 for(let i=0;i<count;i++){
 const b=buttons.nth(i);await b.focus();await b.press('Enter');assert.equal(await b.getAttribute('aria-pressed'),'true');assert.equal(await section.locator('[aria-pressed="true"]').count(),1);
 for(let j=0;j<count;j++)assert.equal(await section.locator('#v'+id+'highlight'+j).getAttribute('visibility'),i===j?'visible':'hidden');
 const value=await detail.textContent();assert.notEqual(value,initial);assert.equal(seen.has(value),false);seen.add(value);
 await capture('choice-'+i);
 }
 await reset.click();assert.equal(await detail.textContent(),initial);assert.equal(await section.locator('[aria-pressed="true"]').count(),0);
 for(let j=0;j<count;j++)assert.equal(await section.locator('#v'+id+'highlight'+j).getAttribute('visibility'),'hidden');
 }
 await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of ['18','19']){
 try{
 const section=page.locator('#notify-v'+id), reset=section.locator('#v'+id+'reset'),detail=section.locator('#v'+id+'detail');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 await reset.click();const initial=await detail.textContent();await capture('initial');
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 const check=async(panel,count,prefix,name)=>{
 const buttons=panel.locator('[data-choice]');assert.equal(await buttons.count(),count);assert.ok(await buttons.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 const seen=new Set();for(let i=0;i<count;i++){const b=buttons.nth(i);await b.focus();await b.press('Enter');assert.equal(await b.getAttribute('aria-pressed'),'true');assert.equal(await panel.locator('[data-choice][aria-pressed="true"]').count(),1);for(let j=0;j<count;j++)assert.equal(await panel.locator('#'+prefix+j).getAttribute('visibility'),i===j?'visible':'hidden');const value=await detail.textContent();assert.notEqual(value,initial);assert.equal(seen.has(value),false);seen.add(value);await capture(name+'-'+i);}
 };
 if(id==='18'){
 const simple=section.locator('#notif-simple'),advanced=section.locator('#notif-advanced'),m=section.locator('[data-mode]');
 assert.equal(await simple.isVisible(),true);assert.equal(await advanced.isVisible(),false);assert.equal(await m.nth(0).getAttribute('aria-pressed'),'true');
 await check(simple,3,'v18highlight','simple');await m.nth(1).focus();await m.nth(1).press('Enter');
 assert.equal(await advanced.isVisible(),true);assert.equal(await simple.isVisible(),false);assert.equal(await m.nth(1).getAttribute('aria-pressed'),'true');assert.equal(await detail.textContent(),initial);
 await capture('advanced');await check(advanced,2,'v18adv','advanced');await reset.click();assert.equal(await simple.isVisible(),true);assert.equal(await advanced.isVisible(),false);assert.equal(await m.nth(0).getAttribute('aria-pressed'),'true');
 }else{await check(section,2,'v19highlight','choice');await reset.click();}
 assert.equal(await detail.textContent(),initial);assert.equal(await section.locator('[data-choice][aria-pressed="true"]').count(),0);
 assert.equal(await section.locator('g[id^="v'+id+'"][visibility="visible"]').count(),0);
 await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 try{
 const id='44',section=page.locator('#notify-v44'),reset=section.locator('#v44reset'),buttons=section.locator('[data-choice]'),detail=section.locator('#v44detail');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V44-'+state+'.png'});};
 await reset.click();const initial=await detail.textContent(),event=await section.locator('#v44event').textContent();const seen=new Set();
 assert.equal(await buttons.count(),3);assert.ok(await buttons.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 const check=async i=>{
 assert.equal(await buttons.nth(i).getAttribute('aria-pressed'),'true');
 assert.equal(await section.locator('[data-choice][aria-pressed="true"]').count(),1);
 for(const [name,on] of [['notice',i!==2],['empty',i===2],['bell',i===0],['mute',i===1]])assert.equal(await section.locator('#v44'+name).getAttribute('visibility'),on?'visible':'hidden');
 assert.equal(await section.locator('#v44event').textContent(),event);assert.equal(await section.locator('#v44event').isVisible(),true);
 };
 await check(0);await capture('initial');
 for(let i=0;i<3;i++){await buttons.nth(i).focus();await buttons.nth(i).press('Enter');await check(i);const t=await detail.textContent();assert.equal(seen.has(t),false);seen.add(t);await capture('choice-'+i);}
 await reset.click();await check(0);assert.equal(await detail.textContent(),initial);await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,'44',e.stack||e.message]);}

 for(const id of ['20','21']){
 try{
 const section=page.locator('#system-v'+id),reset=section.locator('#v'+id+'reset'),detail=section.locator('#v'+id+'detail');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 await reset.click();const initial=await detail.textContent();await capture('initial');
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 const check=async(panel,count,prefix,name)=>{
 const buttons=panel.locator('[data-choice]'),start=await detail.textContent(),seen=new Set();
 assert.equal(await buttons.count(),count);assert.ok(await buttons.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 for(let i=0;i<count;i++){const b=buttons.nth(i);await b.focus();await b.press('Enter');assert.equal(await b.getAttribute('aria-pressed'),'true');assert.equal(await panel.locator('[data-choice][aria-pressed="true"]').count(),1);
 for(let j=0;j<count;j++)assert.equal(await panel.locator('#'+prefix+j).getAttribute('visibility'),i===j?'visible':'hidden');
 const value=await detail.textContent();assert.notEqual(value,start);assert.equal(seen.has(value),false);seen.add(value);await capture(name+'-'+i);}
 };
 if(id==='20'){
 const simple=section.locator('#v20panel0'),advanced=section.locator('#v20panel1'),m=section.locator('[data-mode]');
 assert.equal(await simple.isVisible(),true);assert.equal(await advanced.isVisible(),false);assert.equal(await m.nth(0).getAttribute('aria-pressed'),'true');
 assert.ok(await m.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 await check(simple,2,'v20simple','simple');await m.nth(1).focus();await m.nth(1).press('Enter');
 assert.equal(await simple.isVisible(),false);assert.equal(await advanced.isVisible(),true);assert.equal(await m.nth(1).getAttribute('aria-pressed'),'true');
 assert.equal(await advanced.locator('svg text').filter({hasText:'Android 14'}).count(),2);
 const dates=lang==='ro'?['01.09.2025','01.10.2025']:['Sep 1, 2025','Oct 1, 2025'];for(const date of dates)assert.equal(await advanced.locator('svg text').filter({hasText:date}).count(),1);
 await capture('advanced');await check(advanced,2,'v20advanced','advanced');await reset.click();
 assert.equal(await simple.isVisible(),true);assert.equal(await advanced.isVisible(),false);assert.equal(await m.nth(0).getAttribute('aria-pressed'),'true');
 }else{await check(section,5,'v21highlight','choice');await reset.click();}
 assert.equal(await detail.textContent(),initial);assert.equal(await section.locator('[data-choice][aria-pressed="true"]').count(),0);
 assert.equal(await section.locator('g[id^="v'+id+'"][visibility="visible"]').count(),0);
 await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of ['45','46']){
 try{
 const section=page.locator('#system-v'+id),reset=section.locator('#v'+id+'reset'),detail=section.locator('#v'+id+'detail'),buttons=section.locator('[data-choice]');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 const count=id==='45'?3:4,prefix=id==='45'?'v45state':'v46highlight',initialIndex=id==='45'?0:-1;
 assert.equal(await buttons.count(),count);assert.ok(await buttons.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 const check=async i=>{
 assert.equal(await section.locator('[data-choice][aria-pressed="true"]').count(),i<0?0:1);
 for(let j=0;j<count;j++){assert.equal(await buttons.nth(j).getAttribute('aria-pressed'),String(i===j));assert.equal(await section.locator('#'+prefix+j).getAttribute('visibility'),i===j?'visible':'hidden');}
 if(id==='46'){for(const name of ['v46modemoutline','v46netoutline']){const effective=await section.locator('#'+name+' rect').evaluateAll(xs=>xs.map(x=>getComputedStyle(x).visibility));assert.deepEqual(effective,[i===3?'visible':'hidden',i===3?'visible':'hidden'],'V46 effective nested SVG visibility');}}
 if(id==='45'){const visible=section.locator('#v45state'+i);assert.match(await visible.textContent(),new RegExp((lang==='ro'?'Sistemul rulează din ':'System runs from ')+(i===2?'B':'A')));if(i>0)assert.match(await visible.textContent(),lang==='ro'?/Versiune nouă/:/New version/);}
 };
 await reset.click();const initial=await detail.textContent();await check(initialIndex);await capture('initial');
 const seen=new Set();for(let i=0;i<count;i++){await buttons.nth(i).focus();await buttons.nth(i).press('Enter');await check(i);const t=await detail.textContent();assert.equal(seen.has(t),false);seen.add(t);if(i!==initialIndex)assert.notEqual(t,initial);await capture('choice-'+i);}
 await reset.click();await check(initialIndex);assert.equal(await detail.textContent(),initial);await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of ['22','23']){
 try{
 const section=page.locator('#access-v'+id),range=section.locator(id==='22'?'#fontdemo':'#accesszoom'),reset=section.locator(id==='22'?'#fontreset':'#zoomreset');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 assert.ok(await range.evaluate(x=>x.getBoundingClientRect().height>=44));
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 const fmt=n=>n.toFixed(1).replace(/\.0$/,'').replace('.',lang==='ro'?',':'.');
 const values=id==='22'?[100,125,150,175,200]:[1,1.5,2,2.5,3];
 const original=await section.locator(id==='22'?'#fontsample':'#accessZoomScene').textContent();
 const check=async v=>{
 assert.equal(Number(await range.inputValue()),v);
 if(id==='22'){
 assert.equal(await section.locator('#fontpercent').textContent(),v+'%');
 assert.equal(await section.locator('#fontsample').evaluate(x=>parseFloat(getComputedStyle(x).fontSize)),16*v/100);
 assert.equal(await section.locator('#fontsample').textContent(),original);
 assert.ok(await section.locator('#fontsample').evaluate(x=>x.scrollWidth<=x.clientWidth+1));
 }else{
 assert.equal(await section.locator('#zoomfactor').textContent(),fmt(v)+'×');
 assert.equal(await section.locator('#zoomarea').textContent(),fmt(100/(v*v))+'%');
 assert.equal(await section.locator('#accessZoomScene').getAttribute('transform'),'translate(160 105) scale('+v+') translate(-160 -105)');
 assert.equal(await section.locator('#accessZoomScene').textContent(),original);
 assert.equal(await section.locator('g[clip-path]').getAttribute('clip-path'),'url(#accessZoomClip)');
 }
 };
 await reset.click();await check(values[0]);await capture('initial');
 for(let i=1;i<values.length;i++){await range.fill(String(values[i]));await check(values[i]);await capture('choice-'+i);}
 await reset.click();await check(values[0]);await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of ['47','48']){
 try{
 const section=page.locator('#access-v'+id),buttons=section.locator('[data-choice]'),reset=section.locator('#v'+id+'reset'),detail=section.locator('#v'+id+'detail');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 assert.equal(await buttons.count(),3);assert.ok(await buttons.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 const prefix=id==='47'?'v47highlight':'v48caption',initialIndex=id==='47'?-1:0;
 const stable=await section.locator(id==='47'?'svg':'#v48speech').textContent();
 const check=async i=>{
 assert.equal(await section.locator('[data-choice][aria-pressed="true"]').count(),i<0?0:1);
 for(let j=0;j<3;j++){assert.equal(await buttons.nth(j).getAttribute('aria-pressed'),String(i===j));assert.equal(await section.locator('#'+prefix+j).getAttribute('visibility'),i===j?'visible':'hidden');}
 assert.equal(await section.locator(id==='47'?'svg':'#v48speech').textContent(),stable);
 if(id==='47'){const ann=lang==='ro'?['Ne întâlnim în parc. Text.','Trimite, buton.','Înapoi, buton.']:["Let's meet in the park. Text.",'Send, button.','Back, button.'];assert.equal(await section.locator('#v47announce').textContent(),i<0?(lang==='ro'?'Niciun element focalizat':'No element focused'):ann[i]);}
 else{const correct=lang==='ro'?'Trenul pleacă la ora opt.':'The train leaves at eight.';const wrong=lang==='ro'?'Trenul pleacă la ora șapte.':'The train leaves at seven.';assert.equal(await section.locator('#v48speech').textContent(),correct);assert.equal(await section.locator('#v48caption1 text').textContent(),correct);assert.equal(await section.locator('#v48caption2 text').textContent(),wrong);}
 };
 await reset.click();const initial=await detail.textContent();await check(initialIndex);await capture('initial');
 const seen=new Set();for(let i=0;i<3;i++){await buttons.nth(i).focus();await buttons.nth(i).press('Enter');await check(i);const t=await detail.textContent();assert.equal(seen.has(t),false);seen.add(t);if(i!==initialIndex)assert.notEqual(t,initial);await capture('choice-'+i);}
 await reset.click();await check(initialIndex);assert.equal(await detail.textContent(),initial);await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of ['24','25','26']){
 try{
 const section=page.locator('#advanced-v'+id),buttons=section.locator('[data-choice]'),reset=section.locator('#v'+id+'reset'),detail=section.locator('#v'+id+'detail');
 const count=id==='25'?4:3,initialIndex=id==='24'?1:-1;
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+id+'-'+state+'.png'});};
 assert.equal(await buttons.count(),count);assert.ok(await buttons.evaluateAll(xs=>xs.every(x=>x.getBoundingClientRect().height>=44)));
 assert.equal(await section.locator('small').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 const check=async i=>{
 assert.equal(await section.locator('[data-choice][aria-pressed="true"]').count(),i<0?0:1);
 for(let j=0;j<count;j++){assert.equal(await buttons.nth(j).getAttribute('aria-pressed'),String(i===j));if(id!=='24')assert.equal(await section.locator('#v'+id+'highlight'+j).getAttribute('visibility'),i===j?'visible':'hidden');}
 if(id==='24'){const ms=[200,400,800][i];assert.equal(await section.locator('#animduration').textContent(),ms+' ms');assert.equal(await section.locator('#animbar').evaluate(x=>x.style.width),ms/8+'%');assert.equal(await section.locator('#v24loadbar').evaluate(x=>x.style.width),'75%');assert.match(await section.locator('#v24load').textContent(),/600 ms/);}
 };
 await reset.click();const initial=await detail.textContent();await check(initialIndex);await capture('initial');
 const seen=new Set();for(let i=0;i<count;i++){await buttons.nth(i).focus();await buttons.nth(i).press('Enter');await check(i);const t=await detail.textContent();assert.equal(seen.has(t),false);seen.add(t);if(i!==initialIndex)assert.notEqual(t,initial);await capture('choice-'+i);}
 await reset.click();await check(initialIndex);assert.equal(await detail.textContent(),initial);await capture('reset');console.log('PASS',lang,width,id);
 }catch(e){failures.push([lang,width,id,e.stack||e.message]);}
 }

 for(const id of [1,2,3,7,8,9,10,11,28,29,30,31,32,33,34,35,36,37]){
 try{
 const section=page.locator('#legacy-v'+id),loc=x=>section.locator(x),get=x=>loc(x).textContent(),attr=(x,k)=>loc(x).getAttribute(k);
 const reset=loc('#v'+String(id).padStart(2,'0')+'reset');
 const capture=async state=>{assert.equal(await section.evaluate(s=>s.scrollWidth>s.clientWidth+1||s.getBoundingClientRect().right>innerWidth+1),false,'legacy section overflow');await section.screenshot({path:'browser-results/'+lang+'-'+width+'-V'+String(id).padStart(2,'0')+'-legacy-'+state+'.png'});};
 const number=s=>Number(s.trim().replace(/\s/g,'').replace(lang==='ro'?/\./g:/,/g,'').replace(',','.'));
 const fmt=n=>n.toLocaleString(lang==='ro'?'ro-RO':'en-US',{maximumFractionDigits:3});
 assert.equal(await loc('.legacy-legend').textContent(),lang==='ro'?'Exemplu informativ':'Illustrative example');
 assert.equal(await loc('button,input[type=range],select').evaluateAll(xs=>xs.filter(x=>x.getBoundingClientRect().height>0).every(x=>x.getBoundingClientRect().height>=44)),true,'legacy control size');
 const snap=()=>section.evaluate(s=>JSON.stringify({html:[...s.querySelectorAll('svg')].map(x=>x.outerHTML),inputs:[...s.querySelectorAll('input,select')].map(x=>[x.value,x.checked]),buttons:[...s.querySelectorAll('button')].map(x=>[x.textContent,x.getAttribute('aria-pressed')]),outputs:[...s.querySelectorAll('p,output')].map(x=>x.textContent),fills:[...s.querySelectorAll('[id]')].map(x=>[x.id,x.hasAttribute('style')?x.style.cssText:null])}));
 if(await reset.count())await reset.click();
 const initial=await snap();await capture('initial');
 if(id===1){for(const n of [40,60,80,100]){await loc('#health').fill(String(n));assert.equal(await get('#pct'),n+'%');assert.equal(number(await get('#remain')),50*n);assert.equal(number(await get('#lost')),5000-50*n);assert.equal(await loc('#fill').evaluate(x=>parseFloat(x.style.height)),92*n/100);await capture('health-'+n);}}
 if(id===2){for(const n of [1,2,4,8]){await loc('#v02cores').selectOption(String(n));assert.equal(number(await get('#v02total')),4+6/n);assert.equal(number(await get('#v02saved')),(6-6/n)*10);assert.equal(await loc('.v02core[fill="#087C26"]').count(),n);await capture('cores-'+n);}}
 if(id===3){await loc('#v03go').click();assert.equal(await attr('#v03removed','opacity'),'0.12');assert.match(await get('#v03out'),/200 MB.*300 MB/);await capture('removed');}
 if(id===7){for(const a of [60,120,240])for(const b of [60,90,120]){await loc('#v07t').selectOption(String(a));await loc('#v07d').selectOption(String(b));assert.equal(await loc('#v07bars > div').nth(0).locator('span').count(),a/10);assert.equal(await loc('#v07bars > div').nth(1).locator('span').count(),b/10);assert.ok((await get('#v07result')).includes((1000/a).toFixed(1)+' ms'));await capture('hz-'+a+'-'+b);}}
 if(id===8){for(const n of [100,250,400]){await loc('#v08light').fill(String(n));assert.ok(Math.abs(Number(await attr('#v08shade','opacity'))-(.05+.45*(400-n)/300))<1e-6);assert.ok((await get('#v08result')).endsWith(Math.sqrt(n).toFixed(1)));await capture('photons-'+n);}}
 if(id===9){for(const a of [16000,44100,48000])for(const b of [1,5,10]){await loc('#v09rate').selectOption(String(a));await loc('#v09minutes').fill(String(b));assert.equal(await get('#v09result'),b+' min · '+fmt(a*2*b*60/1e6)+' MB');await capture('pcm-'+a+'-'+b);}}
 if(id===11){for(const p of ['mic','cam','loc'])for(const a of [false,true]){await loc('#v11select').selectOption(p);await loc('#v11allow').setChecked(a);for(const q of [false,true]){if(q)await loc('#v11request').click();assert.equal(await attr('#v11'+p,'display'),'inline');assert.equal(await attr('#v11link','stroke'),a&&q?'#087C26':'#A4B5A0');assert.equal(await attr('#v11shield','fill'),a?'#087C26':'#A66A35');await capture('permission-'+p+'-'+Number(a)+'-'+Number(q));}await reset.click();}}
 if(id===29){for(let i=0;i<3;i++){await loc('[data-choice]').nth(i).press('Enter');assert.equal(await loc('[data-choice][aria-pressed="true"]').count(),1);assert.equal(await loc('svg rect').nth(i+1).getAttribute('fill'),'#B5F52C');assert.notEqual(await get('#v29detail'),'');await capture('usb-'+i);}}
 if(id===33){for(const n of [0,4,12,24]){await loc('#v33').fill(String(n));assert.equal(await get('#v33center'),(24-n)+' GB');assert.equal(await get('#v33legendv'),n+' GB');assert.equal(await get('#v33legendf'),(24-n)+' GB');assert.equal(await get('#v33f'),String(24-n));await capture('storage-'+n);}}
 if(id===34){for(const n of [0,50,80,100]){await loc('#v34').fill(String(n));const avg=n/100*10+(1-n/100)*200;assert.equal(await loc('.v34cell[fill="#087C26"]').count(),n);assert.ok(Math.abs(number(await get('#v34time'))-avg)<1e-6);assert.ok(Math.abs(number(await get('#v34saved'))-(200-avg))<1e-6);await capture('cache-'+n);}}
 if(id===35){await loc('#v35open').click();assert.equal(await attr('#v35contents','visibility'),'visible');assert.ok(await section.evaluate(s=>{const c=s.querySelector('#v35contents').getBoundingClientRect(),f=s.querySelector('#v35flaps').getBoundingClientRect();return f.top-c.bottom>=2;}),'V35 components must not overlap open flaps');for(let i=0;i<4;i++){await loc('#v35select').selectOption({index:i});assert.equal(await loc('#v35contents rect').nth(i).getAttribute('fill'),'#B5F52C');assert.notEqual(await get('#v35detail'),'');await capture('apk-'+i);}}
 if(id===36){for(const n of [0,6,12]){await loc('#v36count').fill(String(n));assert.equal(await loc('#v36ticks rect').count(),n);assert.ok((await get('#v36out')).includes(String(n*5)));await capture('sync-'+n);}}
 if(id===37){const before=await get('#v37info');await loc('#v37choice').selectOption('d');assert.notEqual(await get('#v37info'),before);await capture('data');}
 if(await reset.count()){await reset.click();assert.equal(await snap(),initial,'legacy reset differs');await capture('reset');}
 console.log('PASS',lang,width,'legacy-v'+id);
 }catch(e){failures.push([lang,width,'legacy-v'+id,e.stack||e.message]);}
 }
 if(errors.length)failures.push([lang,width,'pageerror',errors]);
 }catch(e){failures.push([lang,width,e.message]);}finally{await page.close();}
 }

 // Render the actual archived SVG frames, not a reconstruction of their source.
 for(const lang of ['ro','en']){
 const page=await browser.newPage({viewport:{width:320,height:800},deviceScaleFactor:1});
 try{
 const source=fs.readFileSync('docs/foniwise/visual-schemes'+(lang==='en'?'-en':'')+'.svg','utf8');
 const frames=await page.evaluate(source=>{
 const doc=new DOMParser().parseFromString(source,'image/svg+xml');
 if(doc.querySelector('parsererror'))throw Error(doc.querySelector('parsererror').textContent);
 return [...doc.querySelectorAll('g[id^="fraud-v12-"],g[id^="fraud-v13-"],g[id^="fraud-v14-"],g[id^="connect-v15-"],g[id^="connect-v16-"],g[id^="connect-v17-"],g[id^="connect-v27-"],g[id^="connect-v41-"],g[id^="connect-v42-"],g[id^="connect-v43-"],g[id^="notify-v18-"],g[id^="notify-v19-"],g[id^="notify-v44-"],g[id^="system-v20-"],g[id^="system-v21-"],g[id^="system-v45-"],g[id^="system-v46-"],g[id^="access-v22-"],g[id^="access-v23-"],g[id^="access-v47-"],g[id^="access-v48-"],g[id^="advanced-v24-"],g[id^="advanced-v25-"],g[id^="advanced-v26-"],g[id^="legacy-v"]')].filter(g=>/^(?:fraud-v(?:12|13|14)|connect-v(?:15|16|17|27|41|42|43)|notify-v(?:18|19|44)|system-v(?:20|21|45|46)|access-v(?:22|23|47|48)|advanced-v(?:24|25|26)|legacy-v(?:1|2|3|7|8|9|10|11|28|29|30|31|32|33|34|35|36|37))-(?:adv-)?(?:initial|offline|zoom|\d+)$/.test(g.id)).map(g=>{
 const clone=g.cloneNode(true);clone.removeAttribute('transform');
 return {id:g.id,height:Number(g.querySelector('rect').getAttribute('height')),markup:new XMLSerializer().serializeToString(clone)};
 });
 },source);
 assert.equal(frames.filter(f=>f.id.startsWith('fraud-')).length,16,'fraud SVG frame count');
 assert.equal(frames.filter(f=>f.id.startsWith('connect-')).length,29,'connectivity SVG frame count');
 assert.equal(frames.filter(f=>f.id.startsWith('notify-')).length,13,'notification SVG frame count');
 assert.equal(frames.filter(f=>f.id.startsWith('system-')).length,20,'system SVG frame count');
 assert.equal(frames.filter(f=>f.id.startsWith('access-')).length,17,'accessibility SVG frame count');
 assert.equal(frames.filter(f=>f.id.startsWith('advanced-')).length,12,'advanced SVG frame count');
 assert.equal(frames.filter(f=>f.id.startsWith('legacy-')).length,70,'legacy SVG frame count');
 assert.equal(new Set(frames.map(f=>f.id)).size,177,'unique frame ids');
 for(const f of frames){
 try{
 assert.ok(Number.isFinite(f.height)&&f.height>0);
 await page.setContent('<!doctype html><meta charset="utf-8"><style>body{margin:0}svg{display:block}</style><svg xmlns="http://www.w3.org/2000/svg" width="320" height="'+f.height+'" viewBox="0 0 320 '+f.height+'">'+f.markup+'</svg>');
 const frame=page.locator('body > svg');
 assert.equal(await frame.locator('g[id="'+f.id+'"]').count(),1);
 if(!f.id.endsWith('-zoom')){
 const outside=await frame.locator('text').evaluateAll(xs=>xs.filter(x=>{if(x.closest('g[clip-path]'))return false;const b=x.getBoundingClientRect();return b.left < -1||b.right > 321||b.top < -1||b.bottom > x.ownerSVGElement.closest('body > svg').getBoundingClientRect().bottom+1;}).map(x=>x.textContent));
 assert.deepEqual(outside,[],'SVG text outside frame');
 }
 if(f.id.startsWith('system-v46-')){const expected=f.id.endsWith('-3')?'visible':'hidden';for(const name of ['v46modemoutline','v46netoutline']){const effective=await frame.locator('[id="'+f.id+'-'+name+'"] rect').evaluateAll(xs=>xs.map(x=>getComputedStyle(x).visibility));assert.deepEqual(effective,[expected,expected],'V46 archived nested SVG visibility');}}
 if(f.id.startsWith('access-v23-')){const i=f.id.endsWith('-initial')?0:Number(f.id.split('-').pop());const z=1+i*0.5;assert.equal(await frame.locator('[id="'+f.id+'-accessZoomScene"]').getAttribute('transform'),'translate(160 105) scale('+z+') translate(-160 -105)');assert.equal(await frame.locator('g[clip-path]').getAttribute('clip-path'),'url(#'+f.id+'-accessZoomClip)');}
 if(f.id.startsWith('access-v47-')||f.id.startsWith('access-v48-')){const is47=f.id.startsWith('access-v47-'),i=f.id.endsWith('-initial')?(is47?-1:0):Number(f.id.split('-').pop());for(let j=0;j<3;j++)assert.equal(await frame.locator('[id="'+f.id+'-'+(is47?'v47highlight':'v48caption')+j+'"]').getAttribute('visibility'),i===j?'visible':'hidden');}
 if(f.id.startsWith('advanced-v24-')){const i=f.id.endsWith('-initial')?1:Number(f.id.split('-').pop());assert.equal(Number(await frame.locator('[id="'+f.id+'-animbar"]').getAttribute('width')),300*[200,400,800][i]/800);assert.equal(await frame.locator('[id="'+f.id+'-loadbar"]').getAttribute('width'),'225');}
 if(f.id.startsWith('advanced-v25-')||f.id.startsWith('advanced-v26-')){const is25=f.id.startsWith('advanced-v25-'),i=f.id.endsWith('-initial')?-1:Number(f.id.split('-').pop());for(let j=0;j<(is25?4:3);j++)assert.equal(await frame.locator('[id="'+f.id+'-v'+(is25?'25':'26')+'highlight'+j+'"]').getAttribute('visibility'),i===j?'visible':'hidden');}
 if(f.id.startsWith('legacy-v35-')){const i=f.id.endsWith('-initial')?-1:Number(f.id.split('-').pop())-1;const g=frame.locator('[id="'+f.id+'-v35contents"]');assert.equal(await g.getAttribute('visibility'),i<0?'hidden':'visible');assert.deepEqual(await g.locator('rect').evaluateAll(xs=>xs.map(x=>x.getAttribute('fill'))),Array.from({length:4},(_,j)=>j===Math.max(i,0)?'#B5F52C':'#E7F8DB'));if(i>=0)assert.ok(await g.evaluate(c=>{const f=c.ownerSVGElement.querySelector('[id$="-v35flaps"]');return f.getBoundingClientRect().top-c.getBoundingClientRect().bottom>=2;}),'V35 archived components must not overlap open flaps');}
 await frame.screenshot({path:'browser-results/svg-'+lang+'-'+f.id+'.png'});
 console.log('SVG PASS',lang,f.id);
 }catch(e){failures.push([lang,'svg',f.id,e.stack||e.message]);}
 }
 }catch(e){failures.push([lang,'svg',e.stack||e.message]);}finally{await page.close();}
 }
 }finally{await browser.close();fs.writeFileSync('browser-results/report.json',JSON.stringify({failures},null,2));}
 if(failures.length){console.error(failures);process.exitCode=1;}
})().catch(e=>{console.error(e);process.exitCode=1;});
