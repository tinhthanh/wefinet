
chrome.runtime.onInstalled.addListener(() => {
  // do something;
  chrome.storage.sync.clear(() => {});
});

chrome.tabs.onCreated.addListener((tab) => {
  // alert(JSON.stringify(tab))
});
chrome.cookies.onChanged.addListener((changeInfo) => {
  // alert(JSON.stringify(changeInfo.cookie))
});
chrome.runtime.onMessage.addListener((request, sender, respond) => {
  const handler = new Promise((resolve, reject) => {
   if (request.action == 'WEFINEX') {
      chrome.tabs.query({}, tabs => {
        resolve({ data: tabs });
       });
    } else if(request.action == 'NOTIFICATION') {
      createNotify(request.msg);
    } else {
      reject('//request is empty.');
    }
  });
  handler.then(message => respond(message)).catch(error => respond(error));
  return true;
});
const createNotify = (message: string) => {
  chrome.notifications.create(
    "wefinex-bot",
    {
      type: "basic",
      iconUrl: "icon16.png",
      title: "Server Wefinex",
      message: message,
    },
     () =>  {}
  );
}