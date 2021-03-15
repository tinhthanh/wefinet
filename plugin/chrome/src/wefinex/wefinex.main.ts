
import { WefinetController } from '../wefinex/wefinex.controller';
let reload ;
const betKeyStore = 'betKey';
const placeBet  = (doc) => {
    const inputPrice =  (document.querySelector('#InputNumber') as HTMLInputElement);
                        inputPrice.value = doc.price  ;
                        const d = new Date();
                        const hours = String(d.getHours()).padStart(2, '0') ;
                        const minute = String(d.getMinutes()).padStart(2, '0')   ;
                        
                        const day =  String(d.getDate()).padStart(2, '0') ;
                        const month =   String(d.getMonth() + 1).padStart(2, '0') ;
                        const year = d.getFullYear();
                    
                        const currentTime =  `${day}:${month}:${year} ${hours}:${minute}` ;
                        console.log(doc.time) ;
                        console.log(currentTime);
                        if(  localStorage.getItem( betKeyStore)) {
                            if(localStorage.getItem( betKeyStore)  === `${doc.time}-${doc.type}`) {
                               console.log('Đã bet lệch này ' + doc.time + doc.type );
                                return;
                            }
                        }
                        if(currentTime === doc.time) {
                            localStorage.setItem( betKeyStore ,`${doc.time}-${doc.type}`);
                            if(doc.type === 'T') {
                                const btnSuccess = (document.querySelector('.btnSuccess') as HTMLInputElement) ;
                                setTimeout(() => { btnSuccess.click()} , 500);
                                console.log("BET Tăng " + doc.price);
                            } else if(doc.type === 'G') {
                                const btnDown = (document.querySelector('.btnDown') as HTMLInputElement ) ;
                                setTimeout(() => { btnDown.click()} , 500);
                                console.log("BET Giam " + doc.price);
                            }
                        } else {
                            console.log("Đã vượt qua thời gian bet "+ doc.time + " Bet không thành công..");
                        }
}
try {
      const actionType =  new URL(window.location.href).searchParams.get("actionType") || "MAIN";
      chrome.runtime.sendMessage({action: "WEFINEX", domain: window.location.hostname.replace(/(https?:\/\/)?(www.)?/i, ''), actionType: actionType }, (response) => {
           if((response.data || []).filter(z => z.url.indexOf(window.location.hostname) !== -1).length > 1 )  {
               alert('Please open only one window for https://wefinex.net');
               window.location.href = 'http://google.com/';
           } else {
               if(window.location.href.indexOf('wefinex.net/index') != -1 ) {
                WefinetController.commandOnChange((data) => {
                    if(reload) {   clearTimeout(reload); }

                     reload = setTimeout(() => { window.location.reload(); }, 60*3*1000); 

                      if(data && window.location.href.indexOf('wefinex.net/index') != -1 ) {
                        if(document.querySelector('.btnSuccess').getAttribute('disabled') === 'disabled') {
                            const timeWaitEl = document.querySelector('.btnTransparent').textContent.match(/\d+/) ;
                            if(timeWaitEl) {
                                 const timeAwait =  Number(timeWaitEl[0]) ;
                                 console.log("Chờ ..." + timeAwait + "s");
                                  setTimeout( () => {  placeBet(data) } , (timeAwait + 1 )*1000);
                               }
                             } else {
                            console.log("place bet ...");
                            placeBet(data);
                        }
                    }
                }).then( doc => {
                    console.log(doc);
                });
                }
           }
       });
 } catch(err) { console.log(err); }
 
  
