// Configuración de Firebase para el panel admin
// Copiado de app/google-services.json

const firebaseConfig = {
    apiKey: "AIzaSyBj7SMSi92N9THWlcBcnHgdydySoBMh5W4",
    authDomain: "ngdtechsupport.firebaseapp.com",
    projectId: "ngdtechsupport",
    storageBucket: "ngdtechsupport.firebasestorage.app",
    messagingSenderId: "542370713474",
    appId: "1:542370713474:android:9b1de130ee195a97b1899b"
};

// Inicializar Firebase
firebase.initializeApp(firebaseConfig);

// Inicializar Auth y Firestore
const auth = firebase.auth();
const db = firebase.firestore();

// Habilitar persistencia offline (opcional para admin)
db.enablePersistence({ synchronizeTabs: true })
    .catch((err) => {
        if (err.code === 'failed-precondition') {
            console.warn('Persistence failed: multiple tabs open');
        } else if (err.code === 'unimplemented') {
            console.warn('Persistence not available in this browser');
        }
    });

// Configurar idioma de Firestore
db.useEmulator("localhost", 8080); // Descomenta para usar emulador
