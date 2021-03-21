
const run  =  () => { 
    document.querySelector('.balance a span:first-child').textContent = "Tài khoản Thực";
    document.querySelector('.balance a span:first-child').setAttribute('style', 'display: block');

    const temp =  document.querySelector('#rightNav  ul  li.balance div.boxItemRadio:nth-child(2) div.flexRight').innerHTML ;
        document.querySelector('#rightNav  ul  li.balance div.boxItemRadio:nth-child(2) div.flexRight').innerHTML =  document.querySelector('.flexRight a:last-child').innerHTML ;
        document.querySelector('.flexRight a:last-child').innerHTML  = temp  ;

    document.querySelector('#rightNav > ul > li.balance > div > div > div:nth-child(2) > div > div.flexLeft > div > div > div > span').textContent = 'Tài khoản Thực';
    document.querySelector('#rightNav > ul > li.balance > div > div > div:nth-child(1) > div > div.flexLeft > div > div > div > span').textContent = 'Tài khoản Demo';
    document.querySelector('#rightNav li.balance div.boxItemRadio div.flexLeft span.price  > span:nth-child(2)').textContent = "1,000.00";
    document.querySelector('#rightNav  ul  li.balance div.boxItemRadio:nth-child(2) div.flexRight').addEventListener('click' , () =>  {
        (document.querySelector('#rightNav li.balance  div.flexRight a.buttonDeposit ') as any).click();
        setTimeout( () => {
            document.querySelector('div.headerMaster div.leftHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent = "220.69";
            document.querySelector('div.headerMaster div.rightHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent = document.querySelector('#rightNav > ul > li.balance > div > a > div > div > span').textContent.replace('$','');
            document.querySelector("div.changeAmount.pointer").addEventListener('click' , () => { 
                const temp =  document.querySelector('div.headerMaster div.leftHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent  ;
                document.querySelector('div.headerMaster div.leftHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent =  document.querySelector('div.headerMaster div.rightHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent ;
                document.querySelector('div.headerMaster div.rightHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent  = temp;
             });
             document.querySelector('button.btn-border-radius.btn-large.wbtn.w-75.font-weight-bold').setAttribute('style', 'pointer-events: none; user-select: none;');
             document.querySelector('.inputGroup input[type="number"]').setAttribute('style', 'user-select: none;');

             document.querySelector('.modal-body-popup .content').addEventListener("dblclick", () => {
                console.log('xxx'); 
               const div =  document.getElementById("notifyCation") || document.createElement('div');
               div.setAttribute("id",'notifyCation');
               div.setAttribute("style", "transition: transform 0.3s ease-in-out;");
               div.innerHTML = `<div style="transition: transform 0.3s ease-in-out;" >
               <div  class="wrapMainNotify">
                   <div  id="9345351789">
                       <div  class="wrapNotify success">
                           <div  class="boxNotify">
                               <div  class="d-flex align-items-center">
                                   <div >
                                       <span  class="icon">
                                           <!---->
                                           <svg 
                                               xmlns="http://www.w3.org/2000/svg" aria-hidden="true" data-prefix="fas" data-icon="times" role="img" viewBox="0 0 352 512" class="svg-inline--fa fa-times fa-w-11">
                                               <path  fill="currentColor" d="M242.72 256l100.07-100.07c12.28-12.28 12.28-32.19 0-44.48l-22.24-22.24c-12.28-12.28-32.19-12.28-44.48 0L176 189.28 75.93 89.21c-12.28-12.28-32.19-12.28-44.48 0L9.21 111.45c-12.28 12.28-12.28 32.19 0 44.48L109.28 256 9.21 356.07c-12.28 12.28-12.28 32.19 0 44.48l22.24 22.24c12.28 12.28 32.2 12.28 44.48 0L176 322.72l100.07 100.07c12.28 12.28 32.2 12.28 44.48 0l22.24-22.24c12.28-12.28 12.28-32.19 0-44.48L242.72 256z"></path>
                                           </svg>
                                           <!---->
                                       </span>
                                   </div>
                                   <div  class="ml-2">
                                       <div  class="boxContent">
                                           <span  class="message">Bạn đã chuyển thành công ${(document.querySelector('.inputGroup input[type="number"]') as any ).value} WIN đến WIN Wallet</span>
                                       </div>
                                   </div>
                               </div>
                               <span >
                                   <svg 
                                       xmlns="http://www.w3.org/2000/svg" width="34" height="34" viewBox="0 0 34 34" class="close-notify">
                                       <g  id="Group_11134" data-name="Group 11134" transform="translate(-1268 -28)">
                                           <circle  id="Ellipse_201" data-name="Ellipse 201" cx="17" cy="17" r="17" transform="translate(1268 28)" opacity="0.081"></circle>
                                           <g  id="e-remove" transform="translate(1278.49 38.49)">
                                               <path  id="Path_13784" data-name="Path 13784" d="M13.742,1.279a.9.9,0,0,0-1.3,0L7.51,6.208,2.581,1.279a.9.9,0,0,0-1.3,0,.9.9,0,0,0,0,1.3L6.208,7.51,1.279,12.44a.9.9,0,0,0,0,1.3.844.844,0,0,0,.651.279.844.844,0,0,0,.651-.279L7.51,8.813l4.929,4.929a.9.9,0,0,0,1.3,0,.9.9,0,0,0,0-1.3L8.813,7.51l4.929-4.929A.9.9,0,0,0,13.742,1.279Z" transform="translate(-1 -1)" fill="#fff"></path>
                                           </g>
                                       </g>
                                   </svg>
                               </span>
                           </div>
                       </div>
                   </div>
               </div>
           </div>`
           document.querySelector('div.white-theme').appendChild(div);

           document.querySelector('div.headerMaster div.leftHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent = (
            Number(document.querySelector('div.headerMaster div.leftHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent) -  Number((document.querySelector('.inputGroup input[type="number"]') as any ).value)) +"";
           document.querySelector('div.headerMaster div.rightHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700').textContent  = Number(document.querySelector('div.headerMaster div.rightHeader.d-flex.flex-column.align-items-center > span.font-26.font-weight-700')) + Number((document.querySelector('.inputGroup input[type="number"]') as any ).value) +"";

           setTimeout(() => {  div.innerHTML= "";}, 1500)
             });
        }, 100);
      }); 
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
