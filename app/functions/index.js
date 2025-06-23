// index.js
const admin = require("firebase-admin");
const path  = require("path");

// Init Admin SDK
const serviceAccount = require(path.join(__dirname, "serviceAccountKey.json"));

admin.initializeApp({
  credential : admin.credential.cert(serviceAccount),
  databaseURL: "https://meteo-app-2025-default-rtdb.europe-west1.firebasedatabase.app"
});

const rtdb      = admin.database();
const firestore = admin.firestore();

// ─────────────────────────────────────────────────────────────────────────
// Trimite notificare
function sendNotification(token, title, body, tag) {
  return admin.messaging().send({
    token,
    android: {
      priority: "high",
      notification: {
        title,
        body,
        tag: tag || `${Date.now()}`
      }
    }
  });
}


//Ultimele două înregistrări
async function getLastTwoSensorReadings() {
  const snap = await rtdb
    .ref("/SensorData")
    .orderByKey()
    .limitToLast(2)
    .once("value");

  const vals = Object.values(snap.val() || {});
  if (vals.length < 2) return null;              // nu avem istoric
  return { prev: vals[0], curr: vals[1] };       // sortate vechi → nou
}

// Funcție generală de evaluare condiție
function checkCondition(value, threshold, operator) {
  switch (operator) {
    case "<": return value < threshold;
    case "<=": return value <= threshold;
    case ">": return value > threshold;
    case ">=": return value >= threshold;
    case "==": return value == threshold;
    case "!=": return value != threshold;
    default: return false;
  }
}

// Obține prag pentru senzor (per user)
async function getThreshold(userId, sensorType) {
  const docRef = firestore
    .collection("users")
    .doc(userId)
    .collection("thresholds")
    .doc(sensorType);
  const snap = await docRef.get();
  return snap.exists ? snap.data() : null;
}

// Citește ultimul pachet de date
async function getLatestSensorData() {
  const snap = await rtdb
    .ref("/SensorData")
    .orderByKey()
    .limitToLast(1)
    .once("value");

  const raw = snap.val();
  return raw ? Object.values(raw)[0] : null;
}

// Funcția principala
async function checkRealtimeDataAndNotify() {
  try {
    // 1. date meteo
//    const sensorData = await getLatestSensorData();
//    if (!sensorData) { console.log(" Nicio înregistrare."); return; }
//    console.log(" Ultimele date meteo:", sensorData);
    const data = await getLastTwoSensorReadings();
    if (!data) { console.log(" Nu avem suficiente date."); return; }
    const { prev, curr } = data;
    console.log(" Ultimele date:", curr);

      const now = Date.now();
      // ————————— detectam daca senzorul e offline
      const sampleTs = Date.parse(curr.timestamp);
      const isStale  = now - sampleTs <= 60000; //1 min


    const allPrefsSnap = await firestore.collectionGroup("devicePreferences").get();
    if (allPrefsSnap.empty) { console.log(" Nicio preferință găsită."); return; }

    await Promise.all(
      allPrefsSnap.docs.map(async (prefDoc) => {
        if (prefDoc.id !== "default") return;

        const prefs  = prefDoc.data();                // { notifyRain, …, fcmToken }
        const userId = prefDoc.ref.parent.parent.id;  // users/<uid>/devicePreferences

        // Obține fcmToken separat din documentul de top-level
        const userDoc = await firestore.collection("users").doc(userId).get();
        const fcmToken = userDoc.exists ? userDoc.data().fcmToken : null;
        //const fcmToken = "cy_LoBTHQwiLf6qTh0gKfJ:APA91bFPWVxuv8mOBJmOCim6F4NRD4iIhvD8pddN1vzunz1btxPQG__Ghp6F296sddSPAdfJ8t3VSyR3JdMovciQSHH4YPI4woKqdUaofrJye0yncnHdzU4";
        if (!fcmToken) {
          console.log(`${userId} fără token FCM.`);
          return;
        }

        const tasks = [];
//  DETECȚIE TRANZIȚII


if (isStale === true && prefs.notifyTemperature) {
  const threshold = await getThreshold(userId, "temperature");
  if (
    threshold?.active &&
    checkCondition(curr.temperature, threshold.thresholdValue, threshold.condition)
  ) {
    tasks.push(sendNotification(
      fcmToken,
      "Alertă temperatură",
      threshold.messageTemplate || `Temperatura: ${curr.temperature} °C`
    , "temp-alert").catch(e => console.error("Eroare notificare 1:", e)));
  }
}

if (isStale === true && prefs.notifyPollution) {
  const threshold = await getThreshold(userId, "pollution");
  if (
    threshold?.active &&
    checkCondition(curr.pollutionLevel, threshold.thresholdValue, threshold.condition)
  ) {
    tasks.push(sendNotification(
      fcmToken,
      "Alertă poluare",
      threshold.messageTemplate || `Nivel AQI: ${curr.pollutionLevel}`
    , "pol-alert").catch(e => console.error("Eroare notificare 2:", e)));
  }
}


      if (isStale===true && prefs.notifyHumidity) {
        const threshold = await getThreshold(userId, "humidity");
        if (
          threshold?.active &&
          checkCondition(curr.humidity, threshold.thresholdValue, threshold.condition)
        ) {
          tasks.push(sendNotification(
            fcmToken,
            "Alertă umiditate",
            threshold.messageTemplate || `Umiditate: ${curr.humidity}%`
          , "hum-alert").catch(e => console.error("Eroare notificare 3:", e)));
        }
      }
              if (prefs.notifyUv && isStale===true) {
                const threshold = await getThreshold(userId, "uv");
                if (
                  threshold?.active &&
                  checkCondition(curr.uvIndex, threshold.thresholdValue, threshold.condition)
                ) {
                  tasks.push(sendNotification(
                    fcmToken,
                    "Alertă UV",
                    threshold.messageTemplate || `Indice UV: ${curr.uvIndex}`
                  , "uv-alert").catch(e => console.error("Eroare notificare 4:", e)));
                }
              }

                      if (
                        isStale === true && prefs.notifyRain &&
                        prev.rainDetected === false &&
                        curr.rainDetected === true
                      ) {
                        tasks.push(sendNotification(
                          fcmToken,
                          "Ploaie detectată!",
                          "Senzorul a detectat precipitații."
                        , "rain-alert").catch(e => console.error("Eroare notificare 5:", e)));
                      }

        if (tasks.length) {
          await Promise.all(tasks);
          console.log(` Notificări trimise către ${userId}`);
        } else {
          console.log(` Nicio tranziție relevantă pentru ${userId}`);
        }
      })
    );
  } catch (err) {
    console.error("Eroare generală:", err);
  }
}
// ruleaza o data
checkRealtimeDataAndNotify();



loop();


async function loop() {
  try {
    await checkRealtimeDataAndNotify(); // functia ta principala
  } catch (e) {
    console.error("Loop error:", e);
  } finally {
    setTimeout(loop, 60_000); // ← 60 000 ms = 60 s; modifica după nevoie
  }
}
