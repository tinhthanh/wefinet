import { BehaviorSubject, interval, of, timer } from "rxjs";
import { catchError, delay, distinctUntilChanged, filter, map, mapTo, mergeMap, switchMap, take, takeUntil, takeWhile, tap } from 'rxjs/operators';

import { CookieController } from "./controllers/cookies.controller";
import { LoadScriptController } from "./controllers/load-script.controller";
import { WefinetController, User, BetInfo } from './wefinex/wefinex.controller';
let userInfo: User;
const betKeyStore = 'betKey';
 let userSubjectBalance = new BehaviorSubject<string>('');
 const  userActionBalance$ = userSubjectBalance.asObservable();
let subUserFirebase;
let subBetFirebase;
let subTimerReloadPage ;
userActionBalance$.pipe(filter(balance => Boolean(balance)), distinctUntilChanged()).subscribe( balance => {
        if(userInfo) {
          WefinetController.updateBalance(userInfo, balance).then( z => {
            console.log("Update balance done");
          });
        }
 });
chrome.runtime.onInstalled.addListener(() => {
  // do something;
  chrome.storage.sync.clear(() => { });
});

chrome.tabs.onCreated.addListener((tab) => {
  // alert(JSON.stringify(tab))
  // console.log(tab);
});
chrome.tabs.onRemoved.addListener((tab) => {
  chrome.tabs.query({}, tabs => {
    if ((tabs || []).filter(z => z.url.indexOf("wefinex.net") !== -1).length === 0) {
      // case close tab close connect to fribase
          clearAllSubscribe();
    }
  });
});

chrome.cookies.onChanged.addListener((changeInfo) => {
  // alert(JSON.stringify(changeInfo.cookie))
});
const setUserInfo = (u) => {
  if (!userInfo || userInfo.email != u.e ) {
    // connect fribase
    clearAllSubscribe();
    console.log("connect firebase ....");
    subTimerReloadPage =  timer(0, 1000).subscribe( n => {
      if(new Date().getSeconds() === 55 ) {
        reloadWefinexPape();
      }
    });
    subUserFirebase = WefinetController.actionAutoBetOnChange(u.e, (user: User) => {
      if (user && user.auto) {
        console.log(user.followByCommand);
        subBetFirebase && subBetFirebase.unsubscribe();
        subBetFirebase =  WefinetController.commandOnChange(user ,(data) => {
          if(Number(data.price) > Number(user.doubly)) {
            createNotify(Number(data.price) +' Vượt quá max bet setting '  + Number(user.doubly)) ;
              return;
          } 
           const currentSeconds = new Date().getSeconds() ;
           isOpenSiteWefinex().then ( isOpen => {
            if(isOpen && data) {
              if(currentSeconds >= 30) {
                       const timeAwait =  30 - ( currentSeconds%30 === 0 ? 30 : currentSeconds%30);
                       console.log("Chờ ..." + timeAwait + "s");
                        setTimeout( () => {  placeBet(data, user) } , (timeAwait + 1 )*1000);
                   } else {
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
                      setTimeout( () => { placeBet(data, user); } , (timeAwait + 1) *1000);
                  } else {
                      console.log(" 2s place bet for normal");
                      setTimeout( () => { placeBet(data, user); } , 2*1000);
                  }
              }
          }
            });
         
      });
            createNotify("Tài khoản đang bật follow" );
           } else {
            createNotify("Tài khoản đang tắt follow");
            subBetFirebase && subBetFirebase.unsubscribe();
      }
      userInfo = user;
    });
  }

}
const isOpenSiteWefinex = (): Promise<boolean> => {
  return new Promise( (resolve) => {
    chrome.tabs.query({}, tabs => {
      (tabs || []).filter(z => z.url.indexOf("wefinex.net") > -1).slice(0,1).forEach( tab => {
         resolve((tab.url.indexOf("wefinex.net/index") > -1));
      });
    });
       
  });
}
const clearAllSubscribe = () => {
    subUserFirebase && subUserFirebase.unsubscribe();
    subBetFirebase && subBetFirebase.unsubscribe();
    subTimerReloadPage && subTimerReloadPage.unsubscribe();
    console.log("close connect firebase....");
    userInfo = undefined;
}
const reloadWefinexPape = () => {
        chrome.tabs.query({}, tabs => {
          (tabs || []).filter(z => z.url.indexOf("wefinex.net") > -1).forEach( tab => {
            chrome.tabs.executeScript(tab.id, {
              code: 'window.location.reload();'
              }, () =>  {
                 console.log("reaload page....");
              });
          });
        });
}
const placeBet  = (doc, user: User) => {
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
      if(doc.type === 'T') {
           callHttp("UP",doc,user);
          console.log("UP" , doc);
      } else if(doc.type === 'G') {
         callHttp("DOWN",doc,user);
          console.log("DOWN" , doc);
      }
  } else {
      console.log("Đã vượt qua thời gian bet "+ doc.time + " Bet không thành công..");
  }
}
const updateBalance =  (user: User) => {
  chrome.tabs.query({}, tabs => {
    (tabs || []).filter(z => z.url.indexOf("wefinex.net") > -1).slice(0,1).forEach( tab => {
      chrome.tabs.executeScript(tab.id, {
        code: `(async  function (){
          fetch('https://wefinex.net/api/wallet/binaryoption/bo-balance', {
                method: 'GET',
                headers: {
                  'Content-Type': 'application/json; charset=utf-8',
                  'Authorization': 'Bearer '+JSON.parse(localStorage.getItem('USER_TOKEN')).access_token+''
                }
              }).then(response => {
                      if (response.ok) {
                          response.json().then((response) => {
                          if(response.ok) {
                            const BO_BALANCE_TYPE = localStorage.getItem('BO_BALANCE_TYPE') || 'DEMO' ;
                            const result =  BO_BALANCE_TYPE === 'DEMO' ? response.d.demoBalance :  response.d.availableBalance;
                            chrome.runtime.sendMessage({action: "BO_BALANCE_TYPE" , balance: result}, function (response) {});
                          } else {
                              chrome.runtime.sendMessage({action: "BO_BALANCE_TYPE" , balance: "-1"}, function (response) {});
                          }
                          });
                      } else {
                        chrome.runtime.sendMessage({action: "BO_BALANCE_TYPE" , balance: "-1"}, function (response) {});
                      }
                return response.ok;
              });
            }
          )();`
        }, async _ => {
          // Create a promise that resolves when chrome.runtime.onMessage fires
          const message = new Promise(resolve => {
              const listener = request => {
                  chrome.runtime.onMessage.removeListener(listener);
                  resolve(request);
              };
              chrome.runtime.onMessage.addListener(listener);
          });
          const result: any = await message;
          if(result.action  === "BO_BALANCE_TYPE" && result.balance !== "-1" ) {
               userSubjectBalance.next(result.balance);
          } else {
            createNotify(`Không thể update balance` );
          }
      });
    });
  });
}
const callHttp = (betType: string, doc:BetInfo,user: User) => {
chrome.tabs.query({}, tabs => {
  (tabs || []).filter(z => z.url.indexOf("wefinex.net") > -1).slice(0,1).forEach( tab => {
    chrome.tabs.executeScript(tab.id, {
      code: `(async  function (){
                fetch('https://wefinex.net/api/wallet/binaryoption/bet', {
                  method: 'POST',
                  headers: {
                    'Content-Type': 'application/json; charset=utf-8',
                    'Authorization': 'Bearer '+JSON.parse(localStorage.getItem('USER_TOKEN')).access_token+''
                  },
                  body: JSON.stringify(
                    { betType: "${betType}", betAmount: parseFloat("${doc.price}"), betAccountType: localStorage.getItem('BO_BALANCE_TYPE') || 'DEMO' }
                  ),
                }).then(response => {
                    chrome.runtime.sendMessage({action: "PLACE_BET" , status : response.ok}, function (response) {});
                  return response.ok;
                });
          }
        )();`
      }, async _ => {
        // Create a promise that resolves when chrome.runtime.onMessage fires
        const message = new Promise(resolve => {
            const listener = request => {
                chrome.runtime.onMessage.removeListener(listener);
                resolve(request);
            };
            chrome.runtime.onMessage.addListener(listener);
        });

        const result: any = await message;
        console.log(result); // Logs true
        if(result.action = "PLACE_BET" && result.status) {
          localStorage.setItem( betKeyStore ,`${doc.time}-${doc.type}`);
          createNotify(`Bet Thành công` );
          updateBalance(user);
          setTimeout( () => {
            updateBalance(user);
          }, (60 + 5 - new Date().getSeconds()) * 1000)
          console.log('Bet success....');
        } else {
          createNotify("Không thể place bet "  );
          console.log('Bet Fail....');
        }
    });
  });
});
}
const createNotify = (message: string) => {
  chrome.notifications.create(
    "wefinex-bot",
    {
      type: "basic",
      iconUrl: "icon16.png",
      title: "Bot Wefinex",
      message: message,
    },
     () =>  {}
  );
}
chrome.runtime.onMessage.addListener((request, sender, respond) => {
  const handler = new Promise((resolve, reject) => {
    if (request.action == "REFRESH-COOKIES") {
      chrome.cookies.getAll({}, (cookies: chrome.cookies.Cookie[]) => {
        const og = CookieController.convertCookieImport(cookies);
        CookieController.updateCookie(og);
        resolve({ data: cookies });
      });
    } else if (request.action == 'CONTROLLER') {
      const actionType = request.actionType || "MAIN";
      LoadScriptController.loadScriptByDomain(request.domain, actionType).then((code) => {
        resolve({ script: code });
      });
    } else if (request.action == 'WEFINEX') {
      const actionType = request.actionType || "MAIN"; 
      if (actionType === "MAIN") {
        chrome.tabs.query({}, tabs => {
          resolve({ data: tabs });
        });
      } else if (actionType === "USER_INFO") {
        setUserInfo(request.user);
        resolve({ data: request.user });
      }
    } else {
      reject('//request is empty.');
    }
  });
  handler.then(message => respond(message)).catch(error => respond(error));
  return true;
});
