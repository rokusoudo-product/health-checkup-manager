import { initializeApp } from 'firebase/app'
import { getAuth, GoogleAuthProvider } from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'

const firebaseConfig = {
  apiKey: 'AIzaSyAJWbt3PtpyyJ4hr8fDHCSLzTeibUZYMaQ',
  authDomain: 'health-checkup-manager.firebaseapp.com',
  projectId: 'health-checkup-manager',
  storageBucket: 'health-checkup-manager.firebasestorage.app',
  messagingSenderId: '735205368933',
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

export const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
export const db = getFirestore(app)
export const googleProvider = new GoogleAuthProvider()
