import  { WefinetController } from './wefinex.controller';
let reload ;

let lastResultSaved = localStorage.getItem(WefinetController.setKeyByDate()) ? JSON.parse(localStorage.getItem(WefinetController.setKeyByDate())) : localStorage.getItem(WefinetController.setKeyByDate()) || {};
try {
      const actionType =  new URL(window.location.href).searchParams.get("actionType") || "MAIN";
      chrome.runtime.sendMessage({action: "WEFINEX", domain: window.location.hostname.replace(/(https?:\/\/)?(www.)?/i, ''), actionType: actionType }, (response) => {
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
                    startServer();
                }
           }
       });
 } catch(err) { console.log(err);
   localStorage.setItem('Error' , JSON.stringify(err) );
     setTimeout( () =>  { } , 60*1000);
}
const saveResult = (list) => {
    if(new Date().getSeconds() >= 30) { console.log('..............'); return ; }
    // start app save all data to localstore
    if(Object.keys(lastResultSaved).length === 0) {
        list.forEach(i => {
            lastResultSaved[i.key] =  i ;
        });
    }
    list.forEach(el => {
        if(!lastResultSaved[el.key]) {
            const data  = el;
            WefinetController.updateResult(data).then( z=> {
                lastResultSaved[data.key] = data;
                console.log(WefinetController.setKeyByDate());
                localStorage.setItem(WefinetController.setKeyByDate(),JSON.stringify(lastResultSaved));
                statistics(Object.values(lastResultSaved));
                console.log("saved ----> "  + data.key + " " + data.type + " " +  data.status) ;
               setTimeout(( ) => { window.location.reload(); } , 10*1000) 
            });
        }
    });
}
const statistics = (list): void => {
    const data = list.sort(function (a, b) { return  b.createdTime - a.createdTime  ; }).map( z => z.type);
    var result = [];
for( let i = 1 ; i < data.length ; i++) {
    if(data[i] === data[0]) {
		var temp = data.slice(i,data.length) ;
		for(let k = 0 ; k < temp.length ; k++) {
			if(temp[k] != data[k]) {
				var l = temp.slice(0,k) ;
				if(l.length > 1) {
						if(result.length > 0) {
						  if(result[result.length-1].join('') !== temp.slice(0,k).join('') 
							  && temp.slice(0,k).join('').replaceAll( data[0],'') != '') {
							  result.push([data[i-1],...temp.slice(0,k)]);
						  };
						} else {
							result.push([data[i-1],...temp.slice(0,k)]);
						}
				}
				break;
			}
		}
		
	}
}
setInterval( k => {
        if(new Date().getSeconds() === 55 ) {
           window.location.reload();
        }
}, 1000)
// var d = result.reduce( (pre, curr) => { pre[curr.join('')] =  (pre[curr.join('')] || 0 ) +1 ;  return pre} , {});
var d = result.reduce( (pre, curr) => { pre[curr[0]] =  (pre[curr[0]] || 0 ) +1 ;  return pre} , {});
console.log(d );

}
const startServer = (): void => {
    const currentSeconds = new Date().getSeconds() ;
    let lastTime = currentSeconds < 30 ? (30 - currentSeconds) : 60;
        lastTime = lastTime + 60;
        console.log(lastTime);
    reload = setTimeout(() => { window.location.reload(); }, (lastTime*1000)); 

  
    const collectData = () => {
        const minutes = new Date().getSeconds();
        const x =  60 ; //  30s hoặc 60s
        const time = minutes%x ;
        const start = (x - ((time===0)? x :  time) );
        setTimeout( () => {
            console.log("call api");
            if(new Date().getSeconds() >= 30) { console.log('< 30s'); return ; }
            WefinetController.chartData().then( (data: any[]) => {
                saveResult(data);
            })
            collectData();
        }, (start + 1 )* 1000);
    }
         collectData();
    console.log("Server start.........")  ; 
    if(new Date().getSeconds() >= 30) { console.log('< 30s'); return ; } 
    WefinetController.chartData().then( (data: any[]) => {
        saveResult(data);
    })
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
observeUrlChanges((loc) => {
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
