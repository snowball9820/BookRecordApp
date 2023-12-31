@file:OptIn(ExperimentalMaterial3Api::class)

package com.app.bookrecordapp


import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.room.TypeConverter
import coil.compose.rememberImagePainter
import com.app.bookrecordapp.data.AppDatabase
import com.app.bookrecordapp.data.User
import com.app.bookrecordapp.screen.MembershipScreen
import com.app.bookrecordapp.screen.MenuScreen
import com.app.bookrecordapp.screen.RegistrationScreen
import com.app.bookrecordapp.screen.StopWatchScreen
import com.app.bookrecordapp.screen.TranslationRecordScreen
import com.app.bookrecordapp.screen.TranslationScreen
import com.app.bookrecordapp.screen.TtsScreen
import com.app.bookrecordapp.screen.myBookRecordScreen
import com.app.bookrecordapp.ui.theme.BookRecordAppTheme
import com.jaikeerthick.composable_graphs.color.LinearGraphColors
import com.jaikeerthick.composable_graphs.composables.LineGraph
import com.jaikeerthick.composable_graphs.data.GraphData
import com.jaikeerthick.composable_graphs.style.LineGraphStyle
import com.jaikeerthick.composable_graphs.style.LinearGraphVisibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.*
import java.time.format.DateTimeFormatter
import java.util.UUID


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navi()

        }
    }
}


@Composable
fun Navi() {

    val context = LocalContext.current
    val userDao = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "AppDataBase"
        ).build().userDao()

    }

    BookRecordAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var isFavorite by rememberSaveable { mutableStateOf(false) }

            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "membership") {
                composable("membership") {
                    MembershipScreen(navController)
                }
                composable("registration") {
                    RegistrationScreen(navController)
                }
                composable("menu") {
                    MenuScreen(navController)
                }
                composable("translation") {
                    TranslationScreen(navController)
                }
                composable("record") {
                    ReadingRecordScreen(navController)
                }
                composable("tts") {
                    TtsScreen(navController)
                }
                composable("stopwatch") {
                    StopWatchScreen(navController)
                }
                composable("translationRecord") {
                    TranslationRecordScreen(navController, userDao, isFavorite) { favorite ->
                        isFavorite = favorite
                    }
                }
                composable("myBook") {
                    myBookRecordScreen(navController, userDao, isFavorite) { favorite ->
                        isFavorite = favorite
                    }
                }


            }

        }
    }


}


@Composable
fun MyProfile(noteCount: Int) {

    val imageResourceId = remember(noteCount) {
        when {
            noteCount >= 20 -> R.drawable.shark_cat
            noteCount >= 15 -> R.drawable.strb_cat
            noteCount >= 10 -> R.drawable.apple_cat
            noteCount >= 5 -> R.drawable.rabbit_cat
            else -> R.drawable.notebook_cat
        }
    }

    Row {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = imageResourceId),
                contentDescription = "",
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .size(150.dp)
            )

            Text(text = "나의 상태")

            Button(
                onClick = {
                },
                modifier = Modifier
                    .width(180.dp)
                    .height(50.dp)
            ) {
                Text(text = "독서 그래프")
            }
        }
    }
}


@Composable
fun recordGraph(notes: List<Note>, navController: NavController) {


    Column() {
        val style = LineGraphStyle(
            visibility = LinearGraphVisibility(
                isHeaderVisible = true,
                isYAxisLabelVisible = true,
                isCrossHairVisible = true,
            ),
            colors = LinearGraphColors(
                lineColor = Color(0xFFDDA0DD),
                pointColor = androidx.compose.ui.graphics.Color.LightGray,
                clickHighlightColor = Color(0xFFDDA0DD),
                fillGradient = Brush.verticalGradient(
                    0.2f to Color(0xFF9966CC), 0.5f to Color(0xFF5F4B8B)
                )


            ),
            height = 100.dp


        )

        LineGraph(
            xAxisData = listOf(
                LocalDate.now().dayOfMonth,
                LocalDate.now().plusDays(1).dayOfMonth,
                LocalDate.now().plusDays(2).dayOfMonth,
                LocalDate.now().plusDays(3).dayOfMonth,
                LocalDate.now().plusDays(4).dayOfMonth,
                LocalDate.now().plusDays(5).dayOfMonth,
                LocalDate.now().plusDays(6).dayOfMonth,
                LocalDate.now().plusDays(7).dayOfMonth
            ).map {
                GraphData.String(it.toString())
            },
            yAxisData = listOf(notes.size, 0, 0, 0, 0, 0, 0, 0),
            style = style
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun NoteInputText(
    modifier: Modifier = Modifier,
    text: String,
    label: String,
    maxLine: Int = 1,
    onTextChange: (String) -> Unit
) {

    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        colors = TextFieldDefaults.textFieldColors(MaterialTheme.colorScheme.tertiary),
        maxLines = maxLine,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )
        },

        modifier = modifier
    )

}

@Composable
fun NoteButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Text(text = text)
    }
}


data class Note(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val entryDate: LocalDateTime = LocalDateTime.now(),
    var selectedImageUri: Uri? = null
) {
    companion object {
        fun create(title: String, description: String): Note {
            return Note(title = title, description = description)
        }
    }
}


@Composable
fun NoteRow(
    modifier: Modifier = Modifier,
    note: Note,
    onNoteClicked: (Note) -> Unit
) {
    Surface(
        modifier
            .clickable { onNoteClicked(note) }
            .padding(4.dp)
            .clip(RoundedCornerShape(topEnd = 33.dp, bottomStart = 33.dp))
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 14.dp,
                    vertical = 6.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = note.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = note.entryDate.format(DateTimeFormatter.ofPattern("EEE, d MMM")),
                style = MaterialTheme.typography.bodyLarge
            )
            if (note.selectedImageUri != null) {
                Image(
                    painter = rememberImagePainter(note.selectedImageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

class UriTypeConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReadingRecordScreen(navController: NavController) {


    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri

        }
    )

    val notes = remember { mutableStateListOf<Note>() }


//    val inkBuilder = Ink.builder()
//    var strokeBuilder: Ink.Stroke.Builder? = null
//
//    val drawingPoints = remember { mutableListOf<Offset>() }

    fun removeNoteById(noteId: UUID) {
        notes.removeIf { it.id == noteId }
    }

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


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxWidth()
//            .pointerInput(Unit) {
//                detectTransformGestures { _, _, _, _ ->
//
//
//                }
//            }
    ) {

        MyProfile(noteCount = notes.size)

        Row {


        }
//        fun addNewTouchEvent(event: MotionEvent, drawingPoints: MutableList<Offset>) {
//            val action = event.actionMasked
//            val x = event.x
//            val y = event.y
//            val t = System.currentTimeMillis()
//
//            when (action) {
//                MotionEvent.ACTION_DOWN -> {
//                    strokeBuilder = Ink.Stroke.builder()
//                    strokeBuilder!!.addPoint(Ink.Point.create(x, y, t))
//                }
//
//                MotionEvent.ACTION_MOVE -> {
//                    strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
//                }
//
//                MotionEvent.ACTION_UP -> {
//                    strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
//                    strokeBuilder?.let {
//                        inkBuilder.addStroke(it.build())
//                    }
//                    strokeBuilder = null
//                }
//
//                else -> {
//
//                }
//            }
//        }
//
//        Canvas(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(180.dp)
//                .pointerInput(Unit) {
//                    detectTransformGestures { _, pan, _, _ ->
//                        val offsetX = pan.x
//                        val offsetY = pan.y
//                        drawingPoints.add(Offset(offsetX, offsetY))
//
//                        val event = MotionEvent.obtain(
//                            SystemClock.uptimeMillis(),
//                            SystemClock.uptimeMillis(),
//                            MotionEvent.ACTION_MOVE,
//                            offsetX,
//                            offsetY,
//                            0
//                        )
//                        addNewTouchEvent(event, drawingPoints)
//                    }
//                }
//        ) {
//            for (i in 0 until drawingPoints.size - 1) {
//                drawLine(
//                    color = androidx.compose.ui.graphics.Color.DarkGray,
//                    start = drawingPoints[i],
//                    end = drawingPoints[i + 1],
//                    strokeWidth = 5f
//                )
//            }
//        }
//
//        fun createInkFromDrawingPoints(points: List<Offset>): Ink {
//            val inkBuilder = Ink.builder()
//            val strokeBuilder = Ink.Stroke.builder()
//
//            for (point in points) {
//                strokeBuilder.addPoint(Ink.Point.create(point.x, point.y))
//            }
//
//            inkBuilder.addStroke(strokeBuilder.build())
//            return inkBuilder.build()
//        }


        Spacer(modifier = Modifier.padding(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row {
                    Card(
                        modifier = Modifier
                            .padding(start = 8.dp)
                    ) {
                        Image(
                            painter = rememberImagePainter(data = selectedImageUri),
                            contentDescription = "",
                            modifier = Modifier
                                .width(130.dp)
                                .height(220.dp)
                                .padding(top = 8.dp, bottom = 4.dp)
                        )


                    }
                    Spacer(
                        modifier = Modifier
                            .padding(6.dp)
                    )
                    Column {
                        NoteInputText(
                            Modifier
                                .padding(top = 9.dp, bottom = 8.dp, end = 8.dp)
                                .fillMaxWidth(),
                            text = title,
                            label = "제목",
                            onTextChange = {
                                if (it.all { char ->
                                        char.isLetter() || char.isWhitespace()
                                    }) title = it
                            }
                        )
                        NoteInputText(
                            Modifier
                                .padding(top = 9.dp, bottom = 8.dp, end = 8.dp)
                                .fillMaxWidth()
                                .height(120.dp),
                            text = description,
                            label = "기록 추가",
                            onTextChange = {
                                if (it.all { char ->
                                        char.isLetter() || char.isWhitespace()
                                    }) description = it
                            }
                        )

                    }


                }

                Row {

                    Button(
                        onClick = {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                            val ink = createInkFromDrawingPoints(drawingPoints)
//                            recognizeInk(ink)
//
//                            drawingPoints.clear()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,

                            ),
                        modifier = Modifier
                            .width(100.dp)
                            .height(30.dp)
                    ) {
                        Text(
                            "책 등록",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                        )

                    }
                    Spacer(
                        modifier =
                        Modifier.padding(4.dp)
                    )
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && description.isNotEmpty()) {
                                val newNote = Note.create(title, description).apply {
                                    this.selectedImageUri = selectedImageUri
                                }
                                notes.add(newNote)
                                title = ""
                                description = ""
                                selectedImageUri = null

//                                val ink = inkBuilder.build()
//                                recognizeInk(ink)
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,

                            ),
                        modifier = Modifier
                            .width(100.dp)
                            .height(30.dp)
                    ) {
                        Text(
                            "초기화",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )

                    }

                }

                NoteButton(
                    text = "저장",
                    onClick = {

                        val newUser = User(
                            textId = "",
                            textPw = "",
                            text = "",
                            title = title,
                            description = description,
                            selectedImageUri = selectedImageUri

                        )

                        scope.launch(Dispatchers.IO) {
                            db.userDao().insertAll(newUser)
                        }
                    }
                )

                Divider(modifier = Modifier.padding(10.dp))
            }
            items(notes) { note ->
                NoteRow(note = note, onNoteClicked = {
                    removeNoteById(note.id)
                })
            }

            item {
                Divider(modifier = Modifier.padding(10.dp))

                recordGraph(notes, navController)

            }

        }


    }


}

//
//private fun recognizeInk(ink: Ink) {
//    val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")
//        ?: return
//    val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
//
//    val recognizer: DigitalInkRecognizer = DigitalInkRecognition.getClient(
//        DigitalInkRecognizerOptions.builder(model).build()
//    )
//
//    recognizer.recognize(ink)
//        .addOnSuccessListener { result: RecognitionResult ->
//            if (result.candidates.isNotEmpty()) {
//                val recognizedText = result.candidates[0].text
//
//                Log.d("RecognizedText", "Recognized Text: $recognizedText")
//            } else {
//                Log.d("RecognizedText", "No candidates found in recognition result.")
//            }
//        }
//        .addOnFailureListener { e: Exception ->
//            Log.e("RecognizedText", "Error during recognition: $e")
//        }
//}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BookRecordAppTheme {

    }
}