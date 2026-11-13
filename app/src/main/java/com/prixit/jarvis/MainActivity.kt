package com.prixit.jarvis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var arcReactor: ArcReactorView
    private lateinit var statusText: TextView
    private lateinit var consoleLog: TextView
    private lateinit var consoleScroll: ScrollView
    private lateinit var sysStatsText: TextView
    private var isTorchOn = false

    private val jokesList = listOf(
        "Teacher ne pucha: Batao Akbar mahan kyun tha? Student bola: Kyunki uske paas itni badi daadhi thi!",
        "Ek aadmi doctor ke paas gaya aur bola: Doctor sahab, jab main chai peeta hoon toh meri aankh mein dard hota hai. Doctor bola: Chamach nikal kar piya karo!",
        "Pappu ne Jarvis se pucha: Bhai pass hone ka koi shortcut bata. Jarvis bola: Padhai kar le mere bhai, main AI hoon, jadugar nahi!",
        "Why did the computer go to the doctor? Because it caught a virus!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arcReactor = findViewById(R.id.arcReactorView)
        statusText = findViewById(R.id.statusText)
        consoleLog = findViewById(R.id.consoleLog)
        consoleScroll = findViewById(R.id.consoleScroll)
        sysStatsText = findViewById(R.id.sysStatsText)

        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()
        updateSystemStats()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA), 101)
        }

        findViewById<Button>(R.id.micButton).setOnClickListener { startListening() }
        arcReactor.setOnClickListener { startListening() }

        // Quick Action Chips
        findViewById<TextView>(R.id.chipYt).setOnClickListener { processCommand("open youtube") }
        findViewById<TextView>(R.id.chipStore).setOnClickListener { processCommand("open play store and search free fire") }
        findViewById<TextView>(R.id.chipBt).setOnClickListener { processCommand("open bluetooth settings") }
        findViewById<TextView>(R.id.chipCoin).setOnClickListener { processCommand("flip a coin") }
        findViewById<TextView>(R.id.chipJoke).setOnClickListener { processCommand("tell me a joke") }
    }

    private fun updateSystemStats() {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        sysStatsText.text = "POWER: $batLevel%  |  SPEED: ULTRA-FAST  |  CORE: ONLINE"
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val res = tts.setLanguage(Locale("hi", "IN"))
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.US
            }
            tts.setSpeechRate(1.25f)
            tts.setPitch(1.02f)
        }
    }

    private fun speakFast(message: String) {
        appendLog("JARVIS: $message")
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_ID")
    }

    private fun appendLog(line: String) {
        consoleLog.append("\n> $line")
        consoleScroll.post { consoleScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                arcReactor.isListeningMode = true
                statusText.text = "⚡ LISTENING... SPEAK NOW"
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                arcReactor.isListeningMode = false
                statusText.text = "PROCESSING..."
            }

            override fun onError(error: Int) {
                arcReactor.isListeningMode = false
                statusText.text = "TAP REACTOR TO SPEAK"
            }

            override fun onResults(results: Bundle?) {
                arcReactor.isListeningMode = false
                statusText.text = "READY"
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val command = matches[0]
                    appendLog("MALIK: $command")
                    processCommand(command.lowercase(Locale.getDefault()))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)
    }

    private fun processCommand(cmd: String) {
        updateSystemStats()

        if (cmd == "hey jarvis" || cmd == "jarvis" || cmd == "hello jarvis") {
            speakFast("Ji Malik, main aapki kya madad kar sakta hoon?")
            return
        }

        if (cmd.contains("youtube") || cmd.contains("yt")) {
            val query = cmd.replace(Regex("open|youtube|yt|and|search|karo|dikhao|par|play"), "").trim()
            if (query.isNotEmpty()) {
                speakFast("YouTube par $query search kar raha hoon Malik.")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}"))
                startActivity(intent)
            } else {
                speakFast("YouTube khol raha hoon Malik.")
                openAppByPackageOrUrl("com.google.android.youtube", "https://www.youtube.com")
            }
            return
        }

        if (cmd.contains("play store") || cmd.contains("playstore")) {
            var query = cmd.replace(Regex("open|play store|playstore|and|search|download|karo"), "").trim()
            if (query == "ff" || query == "free fire") query = "Free Fire MAX"
            if (query.isNotEmpty()) {
                speakFast("Play Store par $query khoj raha hoon Malik.")
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(query)}")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(query)}&c=apps")))
                }
            } else {
                speakFast("Play Store khol raha hoon Malik.")
                openAppByPackageOrUrl("com.android.vending", "https://play.google.com/store")
            }
            return
        }

        if (cmd.contains("free fire") || cmd.contains("ff")) {
            speakFast("Free Fire MAX launch kar raha hoon Malik.")
            openAppByPackageOrUrl("com.dts.freefiremax", "https://play.google.com/store/apps/details?id=com.dts.freefiremax")
            return
        }
        if (cmd.contains("whatsapp")) {
            speakFast("WhatsApp khol raha hoon.")
            openAppByPackageOrUrl("com.whatsapp", "https://web.whatsapp.com")
            return
        }
        if (cmd.contains("instagram")) {
            speakFast("Instagram khol raha hoon.")
            openAppByPackageOrUrl("com.instagram.android", "https://www.instagram.com")
            return
        }
        if (cmd.contains("chrome")) {
            speakFast("Chrome khol raha hoon.")
            openAppByPackageOrUrl("com.android.chrome", "https://www.google.com")
            return
        }
        if (cmd.contains("camera")) {
            speakFast("Camera khol raha hoon.")
            startActivity(Intent("android.media.action.IMAGE_CAPTURE"))
            return
        }

        if (cmd.contains("bluetooth")) {
            speakFast("Malik, Bluetooth settings khol di hain, yahan se on-off kar lein.")
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            return
        }
        if (cmd.contains("wifi") || cmd.contains("wi-fi")) {
            speakFast("Wi-Fi settings khol raha hoon Malik.")
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            return
        }
        if (cmd.contains("mobile data") || cmd.contains("internet") || cmd.contains("data")) {
            speakFast("Mobile network settings khol raha hoon Malik.")
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            return
        }
        if (cmd.contains("setting")) {
            speakFast("Device settings khol raha hoon Malik.")
            startActivity(Intent(Settings.ACTION_SETTINGS))
            return
        }

        if (cmd.contains("volume up") || cmd.contains("volume badhao") || cmd.contains("awaz badhao")) {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audio.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            speakFast("Volume badha diya hai Malik.")
            return
        }
        if (cmd.contains("volume down") || cmd.contains("volume kam") || cmd.contains("awaz kam")) {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audio.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            speakFast("Volume kam kar diya hai Malik.")
            return
        }
        if (cmd.contains("torch") || cmd.contains("flashlight") || cmd.contains("flash")) {
            toggleFlashlight()
            return
        }

        if (cmd.contains("coin") || cmd.contains("toss") || cmd.contains("sikka")) {
            val result = if (Random.nextBoolean()) "Heads aaya hai Malik!" else "Tails aaya hai Malik!"
            speakFast(result)
            return
        }
        if (cmd.contains("dice") || cmd.contains("roll") || cmd.contains("pasa")) {
            val num = Random.nextInt(1, 7)
            speakFast("Dice roll karne par $num aaya hai Malik!")
            return
        }
        if (cmd.contains("joke") || cmd.contains("chutkula") || cmd.contains("hasao")) {
            speakFast(jokesList.random())
            return
        }
        if (cmd.contains("time") || cmd.contains("samay") || cmd.contains("baje")) {
            val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            speakFast("Malik, abhi $time ho rahe hain.")
            return
        }
        if (cmd.contains("date") || cmd.contains("tarikh")) {
            val date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            speakFast("Aaj ki tarikh $date hai Malik.")
            return
        }
        if (cmd.contains("battery")) {
            val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
            val bat = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            speakFast("Malik, phone ki battery $bat percent charge hai.")
            return
        }
        if (cmd.contains("who made you") || cmd.contains("kisne banaya")) {
            speakFast("Mujhe P R I X I T ne banaya hai, aur main apne Malik ki seva ke liye hoon.")
            return
        }

        val mathAnswer = MathEvaluator.trySolve(cmd)
        if (mathAnswer != null) {
            speakFast("Malik, iska answer $mathAnswer hai.")
            return
        }

        val searchQuery = cmd.replace(Regex("google|search|karo|batao"), "").trim()
        speakFast("Google par $searchQuery search kar raha hoon Malik.")
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(searchQuery)}")))
    }

    private fun openAppByPackageOrUrl(packageName: String, fallbackUrl: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
        }
    }

    private fun toggleFlashlight() {
        try {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            speakFast(if (isTorchOn) "Flashlight on kar di hai Malik." else "Flashlight band kar di hai Malik.")
        } catch (e: Exception) {
            speakFast("Malik, flashlight control karne mein dikkat aa rahi hai.")
        }
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
```[cite: 12]
