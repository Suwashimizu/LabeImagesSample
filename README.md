# LabeImagesSample
MLKitのLabelImagesとTranslateTextで画像に対して日本語でタグ付けするよ

## Memo
以下のAPIを使って実装  
ドキュメントは英語に切り替えること、日本語版は情報が古い

https://firebase.google.com/docs/ml-kit/android/label-images
https://firebase.google.com/docs/ml-kit/android/translate-text#top_of_page


## 画像からタグを取得する

bitmapを生成してFirebaseVisionImageに投げるだけ！
結果はコールバックで返ってくる

```
val bitmap = BitmapFactory.decodeResource(resources, imageId)
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


```

## テキスト翻訳

Modelのダウンロードが必要なため事前に行っておく

```
       val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(FirebaseTranslateLanguage.EN)
            .setTargetLanguage(FirebaseTranslateLanguage.JA)
            .build()

        val englishGermanTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

        englishGermanTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translatorCompleted = true
//                Toast.makeText(this, "download success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
            
```

ダウンロード後FirebaseNaturalLanguageに投げると翻訳結果が返ってくる

```

val text = "hello world"

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
```

