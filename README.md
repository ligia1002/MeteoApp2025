# 🌤️ MeteoApp
> Aplicație pentru monitorizarea condițiilor meteo, cu Firebase și notificări în timp real

MeteoApp este o aplicație mobilă Android care permite utilizatorilor să primească alerte meteo inteligente pe baza pragurilor personalizate. Sistemul este complet integrat cu Firebase (Auth, Firestore, Realtime Database, FCM) pentru procesarea automată a datelor primite de la senzorii unui modul autonom de transmitere, cu posibilitate de încărcare.

---
<br>

## 📡 COMPONENTA HARDWARE
MeteoApp funcționează cu un dispozitiv fizic bazat pe **ESP32 Nano**, echipat cu o serie de senzori de mediu care colectează date în timp real și le trimit către Firebase.

### 🧱 Componente folosite

| Componentă           | Funcție                                      |
|----------------------|----------------------------------------------|
| **ESP32 Nano**  | Microcontroller Wi-Fi, trimite date în cloud |
| **DHT22**            | Temperatură și umiditate                     |
| **MQ135**            | Detectarea nivelului de poluare (AQI estimat)|
| **UV Sensor (VEML6070 / GUVA-S12SD)** | Măsurare indice UV          |
| **Pluviometru** | Detectare precipitații      |
| **BMP180** | Presiune atmosferică și altitudine           |

### 🔄 Funcționare

1. ESP-ul colectează date de la senzori la fiecare 60 secunde
2. Formatează datele într-un JSON:
   ```json
   {
     "temperature": 28.4,
     "humidity": 40,
     "pollutionLevel": 95,
     "rainDetected": false,
     "uvIndex": 6.2,
     "timestamp": "2025-06-22T10:03:00Z"
   }
3. Trimite datele către Firebase Realtime Database la nodul `/SensorData`
4. Backend-ul Node.js preia intrările și decide dacă trebuie declanșate alerte

### 🔐 Securitate
- ESP-ul trebuie să se autentifice folosind un token Firebase (sau acces cu permisii restricționate)
- baza de date poate fi securizată cu Firebase Rules:
```
{
  "rules": {
    "SensorData": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}

  ```
---
## 📊 Arhitectura generală a aplicației. Schema bloc
<img src="https://github.com/user-attachments/assets/9f5e2146-2596-4768-a6bc-cb66ef42ed32" width="400"/>

---
## 👨‍💻 COMPONENTA SOFTWARE
### 🔧 Funcționalități

- ✅ Autentificare cu email și parolă (Firebase Auth)
- ✅ Praguri personalizate pentru senzori: temperatură, umiditate, ploaie, UV, poluare
- ✅ Preferințe individuale de notificare
- ✅ Stocare în Firebase Firestore
- ✅ Notificări push automate (Firebase Cloud Messaging)
- ✅ Backend Node.js care monitorizează senzorii și trimite alerte în timp real

### 📁 Structura proiectului

```
app/
│
├── src/main/java/com/example/meteoapp/
│   ├── BaseActivity.java
│   ├── MeteoApp.java
│   ├── model/              # Clase model pentru DevicePreferences, Threshold etc.
│   ├── repository/         # Acces Firebase
│   ├── service/            # Servicii (ex: notificări)
│   └── ui/
│       ├── auth/           # LoginActivity, RegisterActivity
│       ├── main/           # MainActivity, FactsActivity
│       ├── alarms/         # Alarme personalizate
│       └── settings/       # Praguri și preferințe
│
├── functions/              # Backend Node.js pentru alerte automate
├── build.gradle            # Configurare proiect Android
├── google-services.json    # Firebase config
├── firebase.json, .firebaserc
```

### 🚀 Instalare și rulare

#### 📱 Android App

1. Deschide folderul `app/` în Android Studio
2. Asigură-te că:
   - ai conectat Firebase (Auth, Firestore, Messaging)
   - ai adăugat `google-services.json` în `app/`
3. Rulează aplicația pe emulator sau dispozitiv real


#### 🖥️ Backend (Node.js)

1. Navighează în terminal la:
```
app/functions/
```
2. Instalează dependențele:
```
npm install
```
3. Rulează scriptul principal:
```
node index.js
```
---
### 🧠 Clase principale

#### `MainActivity.java`
- Componenta principală a aplicației după autentificare
- Afișează datele meteo actuale și animații în funcție de vreme (soare, ploaie, noapte etc.)
- Încarcă preferințele de notificare ale utilizatorului din Firestore (`devicePreferences/default`)
- Afișează imaginea de profil și nickname-ul utilizatorului
- Controlează switch-urile individuale de notificare și le salvează în Firebase
- Include grafice UV generate cu MPAndroidChart


#### `RegisterActivity.java`
- Înregistrează utilizatorul folosind Firebase Auth (email + parolă)
- Creează praguri implicite în subcolecția `users/<uid>/thresholds/` pentru:
  - Temperatură
  - Umiditate
  - Ploaie
  - UV
  - Poluare
- Creează documentul `devicePreferences/default` și setează toate opțiunile
- Preia și salvează token-ul FCM în `users/<uid>/fcmToken`
- După succes, redirecționează către `MainActivity`


#### `LoginActivity.java`
- Autentifică utilizatorul folosind Firebase Auth
- Redirecționează către `MainActivity`


#### `ThresholdsActivity.java`
- Permite utilizatorului să vadă și să editeze pragurile de notificare pentru fiecare senzor
- Pragurile sunt salvate în `users/<uid>/thresholds/`
- Fiecare prag conține:
  - Tipul senzorului (`sensorType`)
  - Valoarea de declanșare (`thresholdValue`)
  - Condiția logică (`<`, `>`, etc.)
  - Mesajul notificării (`messageTemplate`)
  - Activ/inactiv (`active`)
  - Timpul de creare/modificare (`createdAt`, `updatedAt`)


#### `DevicePreferences.java`
- Model pentru preferințele de notificare ale unui utilizator
- Câmpuri:
  - `notifyTemperature`, `notifyRain`, `notifyHumidity`, `notifyUv`, `notifyPollution`
  - `createdAt`, `updatedAt`
- Salvat în `users/<uid>/devicePreferences/default`


#### `Threshold.java`
- Model pentru un prag asociat unui senzor meteo
- Câmpuri:
  - `sensorType`, `thresholdValue`, `condition`, `messageTemplate`
  - `active`, `alarmType`, `createdAt`, `updatedAt`


#### `functions/index.js` (Node.js backend)
- Monitorizează datele din Realtime Database (`/SensorData`)
- Compară valorile curente cu cele anterioare și cu pragurile definite
- Dacă un prag este depășit și notificarea este activă:
  - Trimite notificare FCM folosind tokenul din `users/<uid>/fcmToken`
- Rulează în loop la fiecare 60 de secunde


#### `MeteoApp.java`
- Clasa de aplicație care extinde `Application`
- Inițializează Firebase și contextul global al aplicației
- Este declarată în `AndroidManifest.xml`



#### `MyFirebaseMessagingService.java`
- Primește notificările push
- Creează notificări Android locale cu ID-uri unice pentru a evita suprascrierea
- Permite afișarea mai multor notificări în paralel

---

### 🔔 Notificări

Sistemul de notificări din MeteoApp este proiectat să alerteze utilizatorii în timp real atunci când valorile senzorilor depășesc pragurile configurate.

#### 🔗 Cum funcționează

1. **Backend-ul Node.js (`functions/index.js`)** rulează periodic (la fiecare 60 secunde).
2. Citește cele mai recente 2 valori din `Realtime Database` la nodul `/SensorData`.
3. Pentru fiecare utilizator care are `devicePreferences/default`, verifică:
   - Dacă notificarea este activată (`notifyX == true`)
   - Dacă există un prag activ pentru senzorul respectiv (`threshold.active == true`)
   - Dacă valoarea actuală îndeplinește condiția setată (`<`, `>`, etc.)
4. Dacă toate condițiile sunt îndeplinite:
   - Se trimite o notificare push prin Firebase Cloud Messaging (FCM)
   - Notificările sunt etichetate cu un `tag` specific (`"temp-alert"`, `"uv-alert"`, etc.) pentru a evita suprascrierea


#### 📤 Exemple de notificări

- **Temperatură:** „Temperatura a depășit pragul! 32 °C”
- **Ploaie:** „Senzorul a detectat precipitații.”
- **Poluare:** „Nivel ridicat de poluare: AQI 140”
- **Umiditate:** „Umiditatea a scăzut sub prag: 25%”
- **UV:** „Indice UV ridicat: 7.5”


#### 🔐 Condiții necesare pentru trimitere

|  Condiție                              | Detalii                                                             |
|---------------------------------------|----------------------------------------------------------------------|
|  `notifyX == true`                  | Utilizatorul vrea notificări pentru acel tip de senzor                 |
|  `threshold.active == true`         | Pragul pentru senzor este activat                                      |
|  `threshold.condition` îndeplinit   | De exemplu: temperatura > 30°C                                         |
|  `FCM token` prezent                | `users/<uid>/fcmToken` trebuie să fie valid                            |
|  `timestamp recent`                 | Ultima măsurătoare are sub 1 minut                                     |


#### 📱 Afișare pe Android

Aplicația Android folosește `FirebaseMessagingService` pentru a prelua notificările și a le afișa local folosind `NotificationManager`.

- Fiecare notificare primește un `notificationId` unic (bazat pe timestamp) pentru a evita suprascrierea.
- Notificările se afișează simultan în zona de notificări.

#### 🔒 Evitarea dublurilor / spamului

- Notificările sunt trimise doar la tranziții (ex: `rainDetected: false → true`)
- Backend-ul verifică dacă datele sunt proaspete înainte să trimită
- Pragurile inactive sau cu `notifyX = false` sunt ignorate

#### ✅ Exemplu de structură Firestore
```
users/
  <uid>/
    fcmToken: "..."
    devicePreferences/
      default/
        notifyRain: true
        notifyTemperature: true
        ...
      thresholds/
        temperature/
        sensorType: "temperature"
        thresholdValue: 30
        condition: ">"
        active: true
```
---
### 📦 Tehnologii folosite

#### 📱 Android App
- **Java** — limbajul principal pentru activități și logică
- **Firebase SDK** — autentificare, baze de date și push notifications
- **Firestore** — pentru salvarea pragurilor și preferințelor utilizatorului
- **Realtime Database** — sursă live pentru datele senzorilor
- **Firebase Cloud Messaging (FCM)** — trimiterea notificărilor push
- **MPAndroidChart** — afișarea graficului UV

#### ⚙️ Backend
- **Node.js** — monitorizează senzorii și trimite alerte
- **Firebase Admin SDK** — acces complet la Firestore, RTDB și FCM
- **Cron logic intern** — verifică datele la fiecare 60 secunde

---

### 🛠️ Funcționalități suplimentare
- Suport complet pentru teme de zi/noapte/meteo
- Fundaluri dinamice în funcție de vreme (cer senin, ploaie, noapte)
- Personalizare notificări cu mesaj custom (`messageTemplate`)
- Reset automat la toate notificările la autentificare
- Salvare locală a stării switch-urilor (opțional)

---

### 🔄 Extensii posibile

- Adăugarea de notificări programate (alarme)
- Istoric complet al datelor meteo pentru fiecare utilizator
- Suport multi-device (cu sincronizare în cloud)
- Widget meteo pentru homescreen
- Control vocal pentru comenzi meteo

---
@ligia1002, Timișoara, RO, 2025
