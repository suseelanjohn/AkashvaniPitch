package com.kamaraj.akashvanipitch

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

data class Delivery(
    val over: String,
    val score: String,
    val need: String,
    val event: String,
    val statusSpeech: String,
    val actionSpeech: String,
    val pitchInsight: String,
    val playerInsight: String
)

data class MatchStation(
    val title: String,
    val category: String,
    val mode: String,
    val deliveries: List<Delivery>
)

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var soundPool: SoundPool
    private var batKnockSoundId: Int = 0

    private var selectedVoiceGender = "MALE"
    private var maleVoice: Voice? = null
    private var femaleVoice: Voice? = null

    private var currentStationIdx = 0
    private var currentBallIdx = 0
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    private val stations = listOf(
        MatchStation(
            title = "IND v PAK (2007 T20 Final)",
            category = "HISTORIC FULL REPLAY",
            mode = "CLASSIC",
            deliveries = listOf(
                Delivery(
                    "16.1", "PAK: 104/7", "Need 54 from 24 balls", "DOT",
                    "Current Match Status: Pakistan are 104 for 7 at the start of the seventeenth over. They need 54 runs from 24 balls.",
                    "Irfan Pathan steams in to Misbah-ul-Haq, crisp length on off stump, Misbah defends it solidly back down the pitch. No run.",
                    "Looking at this Wanderers pitch in Johannesburg, the afternoon sun has baked the surface hard, offering extra bounce for seamers.",
                    "Misbah-ul-Haq has been Pakistan's calmest finisher all tournament, refusing to panic under scoreboard pressure."
                ),
                Delivery(
                    "19.3", "PAK: 152/10", "INDIA WIN BY 5 RUNS!", "WICKET",
                    "Ball 19.3 Status: Pakistan are 152 for 9, needing 6 runs from 4 balls. Misbah-ul-Haq on strike to Joginder Sharma.",
                    "Misbah walks across his stumps and scoops it high in the air over short fine leg! Sreesanth settles under it... and takes the catch! India win the inaugural T20 World Cup!",
                    "Joginder rolled his fingers over the seam to take pace off, forcing Misbah to generate all the elevation himself.",
                    "Young captain MS Dhoni lifts history in Johannesburg! Every player in this young squad writes their name into folklore!"
                )
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(audioAttributes).build()
        setupUIControls()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.ENGLISH
            val voices = tts.voices
            if (voices != null) {
                maleVoice = voices.firstOrNull { it.name.contains("male", true) || it.name.contains("end", true) } ?: voices.firstOrNull()
                femaleVoice = voices.firstOrNull { it.name.contains("female", true) || it.name.contains("ena", true) } ?: voices.firstOrNull()
            }
        }
    }

    private fun setupUIControls() {
        val btnPlay = findViewById<Button>(R.id.btnPlayPause)
        val btnNext = findViewById<Button>(R.id.btnNextBall)
        val btnMale = findViewById<Button>(R.id.btnMaleVoice)
        val btnFemale = findViewById<Button>(R.id.btnFemaleVoice)
        val btnDuo = findViewById<Button>(R.id.btnDuoVoice)

        btnPlay.setOnClickListener {
            isPlaying = !isPlaying
            if (isPlaying) {
                btnPlay.text = "PAUSE RADIO"
                playCurrentDeliverySequence()
            } else {
                btnPlay.text = "START RADIO"
                tts.stop()
            }
        }

        btnNext.setOnClickListener {
            val station = stations[currentStationIdx]
            currentBallIdx = (currentBallIdx + 1) % station.deliveries.size
            if (isPlaying) playCurrentDeliverySequence()
            else updateLCD()
        }

        btnMale.setOnClickListener { selectedVoiceGender = "MALE"; Toast.makeText(this, "Booth: Male Lead", Toast.LENGTH_SHORT).show() }
        btnFemale.setOnClickListener { selectedVoiceGender = "FEMALE"; Toast.makeText(this, "Booth: Female Lead", Toast.LENGTH_SHORT).show() }
        btnDuo.setOnClickListener { selectedVoiceGender = "DUO"; Toast.makeText(this, "Booth: Co-Host Duo", Toast.LENGTH_SHORT).show() }

        updateLCD()
    }

    private fun playCurrentDeliverySequence() {
        if (!isPlaying) return
        val delivery = stations[currentStationIdx].deliveries[currentBallIdx]
        updateLCD()

        val leadGender = if (selectedVoiceGender == "FEMALE") "FEMALE" else "MALE"
        val analystGender = if (selectedVoiceGender == "DUO") "FEMALE" else leadGender

        applyVoiceProfile(leadGender, 0.95f, 0.98f)
        tts.speak(delivery.statusSpeech, TextToSpeech.QUEUE_FLUSH, null, "STATUS_ID")

        handler.postDelayed({
            if (!isPlaying) return@postDelayed
            applyVoiceProfile(leadGender, 1.05f, 1.02f)
            tts.speak(delivery.actionSpeech, TextToSpeech.QUEUE_ADD, null, "ACTION_ID")

            handler.postDelayed({
                if (!isPlaying) return@postDelayed
                applyVoiceProfile(analystGender, 0.94f, 0.96f)
                tts.speak("Pitch Report: ${delivery.pitchInsight}", TextToSpeech.QUEUE_ADD, null, "LORE_ID")

                handler.postDelayed({
                    if (!isPlaying) return@postDelayed
                    currentBallIdx = (currentBallIdx + 1) % stations[currentStationIdx].deliveries.size
                    playCurrentDeliverySequence()
                }, 4000)
            }, 3500)
        }, 3500)
    }

    private fun applyVoiceProfile(gender: String, pitch: Float, rate: Float) {
        if (gender == "MALE") {
            maleVoice?.let { tts.voice = it }
            tts.setPitch(pitch * 0.88f)
        } else {
            femaleVoice?.let { tts.voice = it }
            tts.setPitch(pitch * 1.06f)
        }
        tts.setSpeechRate(rate)
    }

    private fun updateLCD() {
        val station = stations[currentStationIdx]
        val delivery = station.deliveries[currentBallIdx]
        findViewById<TextView>(R.id.tvLcdTitle).text = station.title
        findViewById<TextView>(R.id.tvLcdScore).text = "${delivery.score} (${delivery.over} Ov)"
        findViewById<TextView>(R.id.tvLcdTicker).text = delivery.statusSpeech
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        soundPool.release()
        super.onDestroy()
    }
}