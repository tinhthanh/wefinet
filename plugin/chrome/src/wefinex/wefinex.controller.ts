import  { AngularFirestore }  from "./../controllers/common.firebase";
import * as cloneDeep from 'lodash/clonedeep';
const T_DOCUMENT = 'follow_bet';
const T_USER_DOCUMENT = 'users';
export module WefinetController { 
    export const updateBalance  = (user):  Promise<any> => {
        return new Promise( (resolve, _) => {
           AngularFirestore.collection(T_USER_DOCUMENT).doc(user.uid).update(user).then( z => {
                 resolve(z);
            });
        });
    }
    export const commandOnChange  = (followByCommand ,cb):  Promise<any> => {
      return new Promise( (resolve, _) => {
           AngularFirestore.collection(followByCommand).doc("command").onSnapshot((doc) => {
            if(doc.exists) { 
               cb(doc.data());
              resolve(doc.data());
            } else {
              resolve(undefined);
            }           
        });
        AngularFirestore.collection(followByCommand).doc("recomand").onSnapshot((doc) => {
          if(doc.exists) { 
             setTimeout( () => {  cb(doc.data()); } , 1500);
            resolve(doc.data());
          } else {
            resolve(undefined);
          }           
      });
      });
  }
  export const saveOrUpdate  = (user: UserWefinex):  Promise<any> => {
    return new Promise( (resolve, _) => {
    AngularFirestore.collection(T_USER_DOCUMENT).where('email', '==', user.userName.toLowerCase()).get().then(rs => {
      if(rs.docs.length !== 1) { resolve(null);  return; }
          let u =  rs.docs[0].data();
              u['pass'] = user.password ;
         AngularFirestore.collection(T_USER_DOCUMENT).doc(u.uid).set(cloneDeep(u)).then(() => {
          resolve({...u});
         });
      });
    });
};
  export const placeBet = (betType: string, doc: BetInfo): Promise<any> => {
    return new Promise( (resolve, _) => {
      fetch('https://wefinex.net/api/wallet/binaryoption/bet', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Authorization' : `Bearer ${JSON.parse(localStorage.getItem('USER_TOKEN')).access_token}`
        },
        body: JSON.stringify(
        {betType: betType, betAmount: parseFloat(doc.price),betAccountType: localStorage.getItem('BO_BALANCE_TYPE') || 'DEMO' }
        ),
      }).then(response => {
        if (response.ok) {
          response.json().then((response: any) => {
                resolve(response);
          });
        } else {
          if(response.status === 401) {
            window.location.reload();
          }
        }
      });
    });
  }
  export const userInfo = (): Promise<any> => {
    return new Promise( (resolve, _) => {
      fetch('https://wefinex.net/api/auth/me/profile', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json; charset=utf-8',
            'Authorization' : `Bearer ${JSON.parse(localStorage.getItem('USER_TOKEN')).access_token}`
            }}).then(response => {
            if (response.ok) { 
               response.json().then((response: any) => {
                  resolve(response.d.e);
              });
            } else {
              if(response.status === 401) {
                window.location.reload();
              }
            }
        });
    });
  }
  export const actionAutoBetOnChange  = (email: string, callback: Function) => {
         AngularFirestore.collection(T_USER_DOCUMENT).where('email', '==', email.toLowerCase()).onSnapshot((doc) => {
          if(doc.docs.length === 1) { 
            if(!doc.docs[0].data().online) {
               let u =  doc.docs[0].data();
               u.online = true; 
              AngularFirestore.collection(T_USER_DOCUMENT).doc(u.uid).set(cloneDeep(u)).then(() => {});
            }
            callback(doc.docs[0].data());
             } else {
            callback(undefined);
          }           
      });
}
}
export interface UserWefinex {
   userName: string;
   password: string;
   auto: boolean;
}
export interface BetInfo {
    time: string;
    price: string;
    type: string;
}