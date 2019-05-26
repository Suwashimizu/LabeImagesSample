package org.suwashizmu.imagelabel

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var translatorCompleted = false
    //初期は日本語へ翻訳
    private var currentTranslateLanguage = FirebaseTranslateLanguage.JA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setup()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable._004)
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val labeler = FirebaseVision.getInstance().onDeviceImageLabeler

        labeler.processImage(image)
            .addOnSuccessListener { labels ->

                val inflater = LayoutInflater.from(this)

                for (label in labels) {
                    val text = label.text
                    val entityId = label.entityId
                    val confidence = label.confidence

                    Log.d("Label", "text=:$text,entityId=$entityId,confidence=$confidence")

                    //visionの結果をTextViewに表示(英語)
                    val textView = (inflater.inflate(R.layout.label, labelBox, false) as TextView).apply {
                        this.text = text
                        //オリジナルの結果を保存
                        this.tag = text
                    }

                    labelBox.addView(textView)
                }

            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

        imageView.setImageBitmap(bitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.translate) {

            for (i in 0 until labelBox.childCount) {
                val textView = labelBox.getChildAt(i) as? TextView
                textView?.let { label ->


                    when (currentTranslateLanguage) {
                        FirebaseTranslateLanguage.JA -> {
                            translateToJA(label.text.toString()) {
                                label.text = it
                            }
                        }
                        else -> {
                            label.text = label.tag as String
                        }
                    }
                }
            }

            //翻訳先を切り替え
            val nextTranslate =
                if (currentTranslateLanguage == FirebaseTranslateLanguage.JA) FirebaseTranslateLanguage.EN else FirebaseTranslateLanguage.JA
            currentTranslateLanguage = nextTranslate
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setup() {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(FirebaseTranslateLanguage.JA)
            .build()

        val englishGermanTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        englishGermanTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translatorCompleted = true
                Toast.makeText(this, "download success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
    }

    private fun translateToJA(text: String, onSuccess: (String) -> Unit) {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(FirebaseTranslateLanguage.JA)
            .build()

        val englishGermanTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        englishGermanTranslator.translate(text)
            .addOnSuccessListener { jaText ->
                onSuccess(jaText)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
    }
}
