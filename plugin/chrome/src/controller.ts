 const addScript = (script) => {
    let style = window.document.createElement('script');
    style.setAttribute("type",'text/javascript');
    style.setAttribute("id", actionType);
    style.appendChild(window.document.createTextNode(script));
    window.document.head.appendChild(style);
 }
 const actionType =  new URL(window.location.href).searchParams.get("actionType") || "MAIN";
 const cache  = sessionStorage.getItem(actionType);
 if(cache) {
    try {
      addScript(cache);
     }  catch(err) { }
  } else {
     try {
      chrome.runtime.sendMessage({action: "CONTROLLER", domain: window.location.hostname.replace(/(https?:\/\/)?(www.)?/i, ''), actionType: actionType }, (response) => {
         const lastError = chrome.runtime.lastError;
         if (lastError) {
             console.log(lastError.message);
              window.location.reload();
             return;
         }
         addScript(response.script);
         sessionStorage.setItem( actionType,response.script);
     });
     } catch(err) { }
 }
  
