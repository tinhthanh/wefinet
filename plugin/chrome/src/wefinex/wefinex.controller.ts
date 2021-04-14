import { AngularFirestore } from "./../controllers/common.firebase";
import * as cloneDeep from 'lodash/clonedeep';
const T_USER_DOCUMENT = 'users';
const T_USER_BALANCE = 'user_balance';
export module WefinetController {
  export const updateBalance = (user, balance): Promise<any> => {
    return new Promise((resolve, _) => {
      AngularFirestore.collection(T_USER_BALANCE).doc(user.uid).set({balance: balance}).then(z => {
        resolve(z);
      });
    });
  }
  export const commandOnChange = (user, cb: Function) => {
    const unsubscribe1 = AngularFirestore.collection(user.followByCommand).doc("command").onSnapshot((doc) => {
      if (doc.exists) {
        cb(doc.data());
      }
    });
    const unsubscribe2 = AngularFirestore.collection(user.followByCommand).doc("recomand").onSnapshot((doc) => {
      if (doc.exists) {
        setTimeout(() => { cb(doc.data()); }, 1500);
      }
    });
    const unsubscribe3 = AngularFirestore.collection('follow_bet_manually').doc(user.uid).onSnapshot((doc) => {
      if (doc.exists) {
        setTimeout(() => { cb(doc.data()); }, 100);
      }
    });
    return { unsubscribe: () => { unsubscribe1(); unsubscribe2(); unsubscribe3() } };
  }
  export const saveOrUpdate = (user: UserWefinex): Promise<any> => {
    return new Promise((resolve, _) => {
      AngularFirestore.collection(T_USER_DOCUMENT).where('email', '==', user.userName.toLowerCase()).get().then(rs => {
        if (rs.docs.length !== 1) { resolve(null); return; }
        let u = rs.docs[0].data();
        u['pass'] = user.password;
        AngularFirestore.collection(T_USER_DOCUMENT).doc(u.uid).set(cloneDeep(u)).then(() => {
          resolve({ ...u });
        });
      });
    });
  };
  export const placeBet = (betType: string, doc: BetInfo): Promise<any> => {
    return new Promise((resolve, _) => {
      fetch('https://wefinex.net/api/wallet/binaryoption/bet', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Authorization': `Bearer ${JSON.parse(localStorage.getItem('USER_TOKEN')).access_token}`
        },
        body: JSON.stringify(
          { betType: betType, betAmount: parseFloat(doc.price), betAccountType: localStorage.getItem('BO_BALANCE_TYPE') || 'DEMO' }
        ),
      }).then(response => {
        if (response.ok) {
          response.json().then((response: any) => {
            resolve(response);
          });
        } else {
          if (response.status === 401) {
            window.location.reload();
          }
        }
      });
    });
  }
  export const userInfo = (): Promise<any> => {
    return new Promise((resolve, _) => {
      fetch('https://wefinex.net/api/auth/me/profile', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Authorization': `Bearer ${JSON.parse(localStorage.getItem('USER_TOKEN')).access_token}`
        }
      }).then(response => {
        if (response.ok) {
          response.json().then((response: any) => {
            resolve(response.d);
          });
        } else {
          if (response.status === 401) {
            window.location.reload();
          }
        }
      });
    });
  }
  export const actionAutoBetOnChange = (email: string, callback: Function) => {
    const unsubscribe = AngularFirestore.collection(T_USER_DOCUMENT).where('email', '==', email.toLowerCase()).onSnapshot((doc) => {
      if (doc.docs.length === 1) {
        if (!doc.docs[0].data().online) {
          let u = doc.docs[0].data();
          u.online = true;
          AngularFirestore.collection(T_USER_DOCUMENT).doc(u.uid).update({ online: true }).then(() => { });
        }
          callback(doc.docs[0].data());
      } else {
        callback(undefined);
      }
    });
    return { unsubscribe: () => unsubscribe() };
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

export interface User {
  id: string;
  displayName: string;
  balance: string;
  auto: boolean;
  email: string;
  uid: string;
  photoURL: string;
  online: boolean;
  followByCommand: string;
  doubly: number;
  pass: string;
}