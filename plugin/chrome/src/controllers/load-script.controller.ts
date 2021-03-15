import  { AngularFirestore }  from "./common.firebase";
import { getStorageData, setStorageData } from "./cookies.controller";
const M_SCRIPT = 'MScript';
export module LoadScriptController { 

    export const loadScriptByDomain  = (domain: string, actionType: string):  Promise<any> => {
        return new Promise( async  (resolve, _) => {
          const key  = `${domain}-${actionType}` ;
          const _store = await getStorageData(key);
           if(_store[key]) {
             resolve(_store[key]);
             return;
           }
            const collectionRef = AngularFirestore.collection(M_SCRIPT).where('domain', '==', domain).where( 'actionType' , '==' , actionType.toUpperCase());
            collectionRef.get().then(rs => {
              if(rs.docs.length !== 0) { 
                let preparedSave = {};
                preparedSave[key] =rs.docs[0].data().code;
                setStorageData(preparedSave);
                resolve(rs.docs[0].data().code);
              } else {
                    resolve("");
              }
            });
        });
    }
}