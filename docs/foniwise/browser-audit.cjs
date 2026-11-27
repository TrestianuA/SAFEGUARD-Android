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

 for(const id of ['15','16','17','27']){
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
 return [...doc.querySelectorAll('g[id^="fraud-v12-"],g[id^="fraud-v13-"],g[id^="fraud-v14-"],g[id^="connect-v15-"],g[id^="connect-v16-"],g[id^="connect-v17-"],g[id^="connect-v27-"]')].filter(g=>/^(?:fraud-v(?:12|13|14)|connect-v(?:15|16|17|27))-(?:initial|offline|zoom|\d+)$/.test(g.id)).map(g=>{
 const clone=g.cloneNode(true);clone.removeAttribute('transform');
 return {id:g.id,height:Number(g.querySelector('rect').getAttribute('height')),markup:new XMLSerializer().serializeToString(clone)};
 });
 },source);
 assert.equal(frames.filter(f=>f.id.startsWith('fraud-')).length,16,'fraud SVG frame count');
 assert.equal(frames.filter(f=>f.id.startsWith('connect-')).length,17,'connectivity SVG frame count');
 assert.equal(new Set(frames.map(f=>f.id)).size,33,'unique frame ids');
 for(const f of frames){
 try{
 assert.ok(Number.isFinite(f.height)&&f.height>0);
 await page.setContent('<!doctype html><meta charset="utf-8"><style>body{margin:0}svg{display:block}</style><svg xmlns="http://www.w3.org/2000/svg" width="320" height="'+f.height+'" viewBox="0 0 320 '+f.height+'">'+f.markup+'</svg>');
 const frame=page.locator('body > svg');
 assert.equal(await frame.locator('g[id="'+f.id+'"]').count(),1);
 if(!f.id.endsWith('-zoom')){
 const outside=await frame.locator('text').evaluateAll(xs=>xs.filter(x=>{const b=x.getBoundingClientRect();return b.left < -1||b.right > 321||b.top < -1||b.bottom > x.ownerSVGElement.closest('body > svg').getBoundingClientRect().bottom+1;}).map(x=>x.textContent));
 assert.deepEqual(outside,[],'SVG text outside frame');
 }
 await frame.screenshot({path:'browser-results/svg-'+lang+'-'+f.id+'.png'});
 console.log('SVG PASS',lang,f.id);
 }catch(e){failures.push([lang,'svg',f.id,e.stack||e.message]);}
 }
 }catch(e){failures.push([lang,'svg',e.stack||e.message]);}finally{await page.close();}
 }
 }finally{await browser.close();fs.writeFileSync('browser-results/report.json',JSON.stringify({failures},null,2));}
 if(failures.length){console.error(failures);process.exitCode=1;}
})().catch(e=>{console.error(e);process.exitCode=1;});
