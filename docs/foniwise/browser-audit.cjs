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
 }catch(e){failures.push([lang,width,id,e.message]);}
 }
 if(errors.length)failures.push([lang,width,'pageerror',errors]);
 }catch(e){failures.push([lang,width,e.message]);}finally{await page.close();}
 }
 }finally{await browser.close();fs.writeFileSync('browser-results/report.json',JSON.stringify({failures},null,2));}
 if(failures.length){console.error(failures);process.exitCode=1;}
})().catch(e=>{console.error(e);process.exitCode=1;});
