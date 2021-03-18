
const run  =  () => { 
    document.querySelector('.balance a span:first-child').textContent = "Tài khoản Thực";
    document.querySelector('.balance a span:first-child').setAttribute('style', 'display: block');

    const temp =  document.querySelector('#rightNav  ul  li.balance div.boxItemRadio:nth-child(2) div.flexRight').innerHTML ;
        document.querySelector('#rightNav  ul  li.balance div.boxItemRadio:nth-child(2) div.flexRight').innerHTML =  document.querySelector('.flexRight a:last-child').innerHTML ;
        document.querySelector('.flexRight a:last-child').innerHTML  = temp  ;

    document.querySelector('#rightNav > ul > li.balance > div > div > div:nth-child(2) > div > div.flexLeft > div > div > div > span').textContent = 'Tài khoản Thực';
    document.querySelector('#rightNav > ul > li.balance > div > div > div:nth-child(1) > div > div.flexLeft > div > div > div > span').textContent = 'Tài khoản Demo';
}
setTimeout( ()=> { 
    if( document.querySelector('.balance a span:first-child')) {
        run(); 
    } else {
        setTimeout( ()=> { if( document.querySelector('.balance a span:first-child')) { run(); }  }, 500);
    }
}, 100);
    

setInterval( () => {
    const numberOfSuggestion =  localStorage.getItem('numberOfSuggestion') ;
   if(numberOfSuggestion && numberOfSuggestion === "3" ) {
    localStorage.setItem('numberOfSuggestion',"1");
        setTimeout(() => { window.location.reload() }, 1500)  ;
   };
}, 1000);


  
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
            setTimeout( () => { 
                if(window.location.href.indexOf('wefinex.net/index') != -1) {
                    window.location.reload();
                 }
            } , 1000 ) ; 
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
             console.log(".......")
            setTimeout( ()=> { if( document.querySelector('.balance a span:first-child')) { run(); }  }, 500);
         }
        }
    oldUrl = loc.href;
});
