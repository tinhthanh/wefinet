import  { AngularFirestore }  from "./../controllers/common.firebase";
const T_DOCUMENT = 'follow_bet';
export module WefinetController { 
    export const command  = ():  Promise<any> => {
        return new Promise( (resolve, _) => {
            const collectionRef = AngularFirestore.collection(T_DOCUMENT).doc("command");
            collectionRef.get().then(rs => {
              if(rs.exists) { 
                    resolve(rs.data());
              } else {
                    resolve("");
              }
            });
        });
    }
    export const commandOnChange  = (cb):  Promise<any> => {
      return new Promise( (resolve, _) => {
           AngularFirestore.collection(T_DOCUMENT).doc("command").onSnapshot((doc) => {
            if(doc.exists) { 
               cb(doc.data());
              resolve(doc.data());
            } else {
              resolve(undefined);
            }           
        })
      });
  }
}