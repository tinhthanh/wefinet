import  { WefinetController } from './wefinex.controller';
let reload;
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
    
 lastResultSaved = localStorage.getItem(WefinetController.setKeyByDate()) ? JSON.parse(localStorage.getItem(WefinetController.setKeyByDate())) : localStorage.getItem(WefinetController.setKeyByDate()) || {};

    if(Object.keys(lastResultSaved).length === 0) {
        list.forEach(i => {
            lastResultSaved[i.key] =  i ;
        });
    }
    let couter = 0 ;
    for(let i = 1 ; i < list.length ; i++ ) {
        if(list[0].type === list[i].type ) {
            couter++;
        } else {
            break;
        }
    }
    if(couter >= 4) {
        chrome.runtime.sendMessage({action: "NOTIFICATION", msg:  "Chuỗi " + couter}, (response) => {});
    }
    const d =  list.slice(0, 27);
    console.log(d.filter(z => z.type === 'G').length);
    console.log(d.filter(z => z.type === 'T').length);
     list.slice(0,25).forEach(el => {
        if(!lastResultSaved[el.key]) {
            const data  = el;
            WefinetController.updateResult(data).then( z=> {
                lastResultSaved[data.key] = data;
                console.log(WefinetController.setKeyByDate());
                Object.keys(lastResultSaved).forEach( key => {
                    if(key.indexOf(WefinetController.setKeyByDate()) == -1 ) {
                        delete lastResultSaved[key];
                    }
                });
                localStorage.setItem(WefinetController.setKeyByDate(),JSON.stringify(lastResultSaved));
                statistics(Object.values(lastResultSaved));

                const time  = WefinetController.setKeyByDate();			  
                const f1 = Object.values(JSON.parse(localStorage.getItem(time))).filter((z: any) => z.key.startsWith(time)).sort( (a: any, b: any) => { return  b.createdTime - a.createdTime  ; });
 
                const f2 = f1.map((item: any) => { return { x : new Date(item.createdTime), y: [item.openPrice ,item.highPrice , item.lowPrice , item.closePrice]}}).slice(0,50).reverse();
                WefinetController.saveStatisticalStock({data: JSON.stringify(f2)}).then( l => { console.log("save -> stock -> done")})
                console.log(f2);
                analysis();
                console.log("saved ----> "  + data.key + " " + data.type + " " +  data.status) ;
               setTimeout(( ) => { window.location.reload(); } , 10*1000) 
            });
        }
    });
    localStorage.setItem(WefinetController.setKeyByDate(),JSON.stringify(lastResultSaved));
}
const analysis = (): void => {
const time  = WefinetController.setKeyByDate();			  
const data = Object.values(JSON.parse(localStorage.getItem(time))).filter((z: any) => z.key.startsWith(time)).sort(function (a: any, b: any) { return  b.createdTime - a.createdTime  ; });
  let result = [];
      let same: any = data[0];
      let obj = {};
      for( let i = 0 ; i < data.length; i++) {
        const d: any = data[i] ;
          if(d.type !== same.type) { 
          if(result.length > 2){
            const listKey = result.map(k => k.type).join('') ;
            const key  = listKey.length + listKey[0] ;
            obj[key]  = [...(obj[key] || [] ), result[0].key.split(' ')[1]]  ;
          }
          result = [];
        }
        result.push(d)
        same = d;

      }
      if(result.length > 2){
        const listKey = result.map(k => k.type).join('') ;
        const key  = listKey.length + listKey[0] ;
        obj[key]  =  [...(obj[key] || [] ), result[0].key.split(' ')[1]];
      }
      const temp1 = obj;
      const list = Object.values(Object.keys(temp1).reduce( (pre , curr) => {  pre[curr.replace("T", "").replace("G", "")]  = { N:curr.match(/\d+/g)[0] , T : (temp1[curr.match(/\d+/g)[0] + "T"] ? temp1[curr.match(/\d+/g) + "T"].length : 0)  , G : (temp1[curr.match(/\d+/g)[0] + "G"] ? temp1[curr.match(/\d+/g) + "G"].length : 0) } ; return pre; } , {})).
      sort((b1: any, b2: any) => Number(b1.name) - Number(b2.name) ) ;
	  const chart = localStorage.getItem(`CHART-${WefinetController.setKeyByDate()}`) || '';
      if(chart === JSON.stringify(list)) {
          console.log('no change');
      } else {
        WefinetController.saveStatistical({data: JSON.stringify(list)}).then( z => {
            console.log("saveStatistical -> done ");
            console.log(list);
        });
      }
      localStorage.setItem(`CHART-${WefinetController.setKeyByDate()}`,JSON.stringify(list));
				  
}
const statistics = (list): void => {

    const data = list.sort(function (a, b) { return  b.createdTime - a.createdTime  ; }).map( z => z.type);
    const result = [];
for( let i = 1 ; i < data.length ; i++) {
    if(data[i] === data[0]) {
		const temp = data.slice(i,data.length) ;
		for(let k = 0 ; k < temp.length ; k++) {
			if(temp[k] != data[k]) {
				const l = temp.slice(0,k) ;
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
// var d = result.reduce( (pre, curr) => { pre[curr.join('')] =  (pre[curr.join('')] || 0 ) +1 ;  return pre} , {});
const d = result.reduce( (pre, curr) => { pre[curr[0]] =  (pre[curr[0]] || 0 ) +1 ;  return pre} , {});
console.log(d );



}
setInterval( k => {
    if(new Date().getSeconds() === 55 ) {
       window.location.reload();
    }
}, 1000)
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
