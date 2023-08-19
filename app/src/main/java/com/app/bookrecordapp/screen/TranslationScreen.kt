package com.app.bookrecordapp.screen

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import coil.compose.rememberImagePainter
import com.app.bookrecordapp.data.AppDatabase
import com.app.bookrecordapp.data.User
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun TranslationScreen(navController: NavController) {
    val activity = LocalContext.current as Activity
//    val sharedPref = remember { activity?.getPreferences(Context.MODE_PRIVATE) }
//
//    val textValue = sharedPref?.getString("textValue", "") ?: ""
//
//    var savedText by remember { mutableStateOf(textValue) }


    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "contacts.db"
        )
            .addMigrations()
            .build()
    }
    val scope = rememberCoroutineScope()


    val koEnTranslator = remember {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        Translation.getClient(options)
    }
    val koJaTranslator = remember {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.JAPANESE)
            .build()
        Translation.getClient(options)

    }

    val koChTranslator = remember {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.CHINESE)
            .build()
        Translation.getClient(options)
    }

    var enabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        koEnTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                enabled = true

            }
        koJaTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                enabled = true

            }
        koChTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                enabled = true

            }


            .addOnFailureListener { exception ->

            }

    }




    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        var selectUri by remember {
            mutableStateOf<Uri?>(null)
        }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                selectUri = uri

            }
        )


        Button(
            onClick = {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,

                ),
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
        ) {
            Text(
                "문장 등록",
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )

        }
        //영어 텍스트
//        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        //한글 텍스트
        val koRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        val context = LocalContext.current
        var trText by remember { mutableStateOf("") }
        var enText by remember { mutableStateOf("") }
        var jaText by remember { mutableStateOf("") }
        var chText by remember { mutableStateOf("") }

        selectUri?.let {
            try {
                val image = InputImage.fromFilePath(context, it)
                koRecognizer.process(image)
                    .addOnSuccessListener { result ->
                        trText = result.text
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item() {
                Image(
                    painter = rememberImagePainter(data = selectUri),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )



                Text(
                    text = trText,
                    fontSize = 24.sp
                )

                Button(
                    onClick = {
                        koEnTranslator.translate(trText)
                            .addOnSuccessListener { translatedText ->
                                enText = translatedText


                            }
                        koJaTranslator.translate(trText)
                            .addOnSuccessListener { translatedText ->
                                jaText = translatedText


                            }
                        koChTranslator.translate(trText)
                            .addOnSuccessListener { translatedText ->
                                chText = translatedText


                            }

                        val newUser = User(
                            textId="",
                            textPw = "",
                            text = trText,
                            title="",
                            description = "",
                            selectedImageUri = null

                        )

                        scope.launch(Dispatchers.IO) {
                            db.userDao().insertAll(newUser)
                        }



                    }, enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,

                        ),
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp)


                ) {

                    Text(
                        text = "번역",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif
                    )


                }

                Text(text = enText)

                Text(text = jaText)

                Text(text = chText)

                Button(
                    onClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Translated Text", enText) // Change enText to jaText or chText as needed
                        clipboardManager.setPrimaryClip(clip)


                        Toast.makeText(context, "Translated text copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp)
                ) {
                    Text(
                        text = "Copy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }


                Column {

                    val languageIdentifier = LanguageIdentification.getClient()

                    Button(onClick = {
                        languageIdentifier.identifyLanguage(trText)
                            .addOnSuccessListener { languageCode ->
                                if (languageCode == "und") {
                                    Log.i(ContentValues.TAG, "Can't identify language.")
                                } else {
                                    Log.i(ContentValues.TAG, "Language: $languageCode")
                                }
                            }
                            .addOnFailureListener {

                            }

                        languageIdentifier.identifyLanguage(enText)
                            .addOnSuccessListener { languageCode ->
                                if (languageCode == "und") {
                                    Log.i(ContentValues.TAG, "Can't identify language.")
                                } else {
                                    Log.i(ContentValues.TAG, "Language: $languageCode")
                                }
                            }
                            .addOnFailureListener {

                            }

                        languageIdentifier.identifyLanguage(jaText)
                            .addOnSuccessListener { languageCode ->
                                if (languageCode == "und") {
                                    Log.i(ContentValues.TAG, "Can't identify language.")
                                } else {
                                    Log.i(ContentValues.TAG, "Language: $languageCode")
                                }
                            }
                            .addOnFailureListener {

                            }

                        languageIdentifier.identifyLanguage(chText)
                            .addOnSuccessListener { languageCode ->
                                if (languageCode == "und") {
                                    Log.i(ContentValues.TAG, "Can't identify language.")
                                } else {
                                    Log.i(ContentValues.TAG, "Language: $languageCode")
                                }
                            }
                            .addOnFailureListener {

                            }
                    }
                    ) {
                        Text(text = "언어 식별")

                    }
                }

            }


        }
    }


}