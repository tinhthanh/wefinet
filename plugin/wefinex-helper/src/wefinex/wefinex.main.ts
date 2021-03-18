
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