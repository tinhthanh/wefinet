
import { WefinetController } from '../wefinex/wefinex.controller';
      chrome.runtime.sendMessage({action: "WEFINEX", actionType: "MAIN" }, (response) => {
        const lastError = chrome.runtime.lastError;
                if (lastError) {
                    console.log(lastError.message);
                     window.location.reload();
                    return;
                }
           if((response.data || []).filter(z => z.url.indexOf(window.location.hostname) !== -1).length > 1 )  {
               alert('Please open only one window for https://wefinex.net');
               window.location.href = 'http://google.com/';
           } else {
               if(window.location.href.indexOf('wefinex.net/index') != -1 ) {
                WefinetController.userInfo().then( (user) => { 
                    chrome.runtime.sendMessage({action: "WEFINEX", actionType: "USER_INFO", user: user }, (response) => {
                        const lastError = chrome.runtime.lastError;
                                if (lastError) {
                                    console.log(lastError.message);
                                     window.location.reload();
                                    return;
                                }
                                console.log("SERVER SEND ", response);   
                                console.log("SEND USER INFO.....")
                            });
                });
               
                }
           }
       }); 
let oldUrl ;
const locationChangeEventType = "MY_APP-location-change";
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
observeUrlChanges((loc) => {
    if(oldUrl && loc.href.indexOf('wefinex.net/index') !== -1) {
            window.location.reload();
    }
       oldUrl = loc.href;
});
