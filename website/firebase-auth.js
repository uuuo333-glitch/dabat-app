import { initializeApp } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js";
import {
  createUserWithEmailAndPassword,
  getAuth,
  onAuthStateChanged,
  sendPasswordResetEmail,
  signInWithEmailAndPassword,
  signOut
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";

const response = await fetch(new URL("/__/firebase/init.json", window.location.origin), {
  cache: "no-store"
});

if (!response.ok) {
  throw new Error(`تعذر تحميل إعداد Firebase (${response.status}).`);
}

const firebaseConfig = await response.json();
if (!firebaseConfig.apiKey || !firebaseConfig.projectId) {
  throw new Error("إعداد Firebase غير مكتمل.");
}

const auth = getAuth(initializeApp(firebaseConfig));

export const createAccount = (email, password) =>
  createUserWithEmailAndPassword(auth, email, password);
export const signIn = (email, password) =>
  signInWithEmailAndPassword(auth, email, password);
export const resetPassword = (email) => sendPasswordResetEmail(auth, email);
export const signOutAccount = () => signOut(auth);
export const onUserChanged = (callback) => onAuthStateChanged(auth, callback);
