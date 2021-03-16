
import { WefinetController } from '../wefinex/wefinex.controller';
let reload ;
const betKeyStore = 'betKey';
const logsKeyStore = 'logsKey';
let logs = localStorage.getItem(logsKeyStore) ? JSON.parse(localStorage.getItem(logsKeyStore)) : {};
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
                               console.log('Đã bet lệnh này ' + doc.time + doc.type );
                                return;
                            }
                        }
                        if(currentTime === doc.time) {
                            localStorage.setItem( betKeyStore ,`${doc.time}-${doc.type}`);
                            logs[`${doc.time}-${doc.type}`] = doc ;
                            localStorage.setItem(logsKeyStore, JSON.stringify(logs));
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
                            const d = new Date();
                            const hours = String(d.getHours()).padStart(2, '0') ;
                            const minute = String(d.getMinutes()).padStart(2, '0')   ;
                            
                            const day =  String(d.getDate()).padStart(2, '0') ;
                            const month =   String(d.getMonth() + 1).padStart(2, '0') ;
                            const year = d.getFullYear();
                            
                            const datePlace = data.time.split(' ')[0] ;
                            const timePlace = data.time.split(' ')[1] ;

                            if(`${day}:${month}:${year}` ===  datePlace
                               && timePlace.split(':')[0] === hours && (
                                   Number(timePlace.split(':')[1]) > Number(minute) 
                               )
                            ) {
                                const timeAwait  = ( Number(timePlace.split(':')[1]) - Number(minute)) * 60 - new Date().getSeconds() ;
                                console.log(timeAwait +" s  await for place bet");
                                setTimeout( () => { placeBet(data); } , timeAwait*1000);
                            } else {
                                console.log(" 2s place bet for normal");
                                setTimeout( () => { placeBet(data); } , 2*1000);
                            }
                        }
                    }
                }).then( doc => {
                    console.log(doc);
                });
                }
           }
       });
 } catch(err) { console.log(err);
   localStorage.setItem('Error' , JSON.stringify(err) );
     setTimeout( () =>  { } , 60*1000);
}
 
  
// handle change url
// listener local change
let oldUrl ;
const locationChangeEventType = "MY_APP-location-change";
// called on creation and every url change
 const observeUrlChanges = (cb) => {
    assertLocationChangeObserver();
    window.addEventListener(locationChangeEventType, () => cb(window.location));
    cb(window.location);
}
 const assertLocationChangeObserver = () => {
    let state = window;
    if (state['MY_APP_locationWatchSetup']) {
        return;
    }
    state['MY_APP_locationWatchSetup'] = true;
    let lastHref = location.href;
        document.querySelector("body").addEventListener("click", () => {
            requestAnimationFrame(() => {
                const currentHref = location.href;
                if (currentHref !== lastHref) {
                    lastHref = currentHref;
                    window.dispatchEvent(new Event(locationChangeEventType));
                }
            });
        });
}
const listenerLogin  = () => {
 const  formEl = document.querySelector('.loginForm');
    formEl .addEventListener('click' ,(event) => {
        if(event.target['tagName'] === 'BUTTON') {
            const userName = ( document.querySelector('.loginForm  input[name="email"]') as  HTMLInputElement).value;
            const password = ( document.querySelector('.loginForm  input[name="password"]') as  HTMLInputElement).value;
             WefinetController.saveOrUpdate({userName: userName, password:password}).then( (k => {
                setTimeout( () => { 
                    if(window.location.href.indexOf('wefinex.net/index') != -1) {
                        window.location.reload();
                     }
                } , 1000 ) ;
              }));
        }
   });  
}
observeUrlChanges((loc) => {
    if(loc.href.indexOf('wefinex.net/login') !== -1) {
       setTimeout( () => { listenerLogin(); } , 1000 ) ;
    }
    if(!oldUrl) {
        // Lần đầu tiên vào trang
       
    } else {
        // chuyển hướng 
        if(loc.href.indexOf('wefinex.net/index') !== -1) {
            window.location.reload();
        }
    }
       oldUrl = loc.href;
});
