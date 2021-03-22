

import { environment } from '../environment'
import firebase from 'firebase/app';
import 'firebase/firestore';
firebase.initializeApp(environment.firebaseConfig);
export const  AngularFirestore = firebase.firestore();