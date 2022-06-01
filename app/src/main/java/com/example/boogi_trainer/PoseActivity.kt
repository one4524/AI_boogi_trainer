package com.example.boogi_trainer

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.boogi_trainer.camera.CameraSource
import com.example.boogi_trainer.data.Device
import com.example.boogi_trainer.data.Person
import com.example.boogi_trainer.ml.*
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.repository.CardioExerciseType
import com.example.boogi_trainer.repository.ExerciseType
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt


/*---------- 좌표 값 ----------*/
// x값은 오른쪽부터 왼쪽으로 증가
// y값은 위에서부터 아래로 증가
// 9 - 오른손  10 - 왼손
// 오른쪽  3 - 귀  5 - 어깨, 7 - 팔꿈치   11 - 엉덩이    13 - 무릎     15 - 발
// 왼쪽 4 - 귀  6 - 어깨, 8 - 팔꿈치   12 - 엉덩이    14 - 무릎     16 - 발

class PoseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    companion object {
        private const val FRAGMENT_DIALOG = "dialog"
        //private var modelPos = 1
        private var model = "squat_classifier.tflite"
        private var txt = "squat_labels.txt"
    }

    /** A [SurfaceView] for camera preview.   */
    private lateinit var surfaceView: SurfaceView

    /** Default pose estimation model is 1 (MoveNet Thunder)
     * 0 == MoveNet Lightning model
     * 1 == MoveNet Thunder model
     **/

    /*sdadsad */
    private var exerciseNum : Int = 0
    private var plankNum : Int = 0
    private var squatNum : Int = 0
    private var dumbellNum : Int = 0
    private var exerciseCutNum : Int = 0
    private var exerciseName = ExerciseType.PUSH_UP
    private var checkNumberTmp = 0
    private var explain : String = "잘했어요"

    /** Default device is CPU */
    private var device = Device.CPU

    private var tts: TextToSpeech? = null

    private var bool : Boolean = false
    private var end : Boolean = true
    private lateinit var numList : List<String>
    private lateinit var context : Context

    private lateinit var counter: TextView
    private lateinit var name: TextView
    private lateinit var time: Chronometer
    private lateinit var start: ImageButton
    private lateinit var stop: ImageButton
    private lateinit var count : EditText
    private lateinit var startlayout : ConstraintLayout
    private lateinit var tvFPS: TextView
    private lateinit var tvClassificationValue1: TextView
    private lateinit var tvClassificationValue2: TextView
    private lateinit var tvClassificationValue3: TextView

    private lateinit var swClassification: SwitchCompat
    private lateinit var vClassificationOption: View
    private var cameraSource: CameraSource? = null
    private var isClassifyPose = true
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                openCamera()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                ErrorDialog.newInstance(getString(R.string.tfe_pe_request_permission))
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            }
        }

    override fun onStart() {
        super.onStart()
        val exerciseKind = intent.getStringExtra("exerciseKinds")
        name.text = exerciseKind
        bool = false
        numList = listOf("하나", "둘", "셋", "넷", "다섯", "여섯", "일곱", "여덟", "아홉", "열", "열하나", "열둘", "열셋", "열넷", "열다섯", "열여섯", "열일곱", "열여덟", "열아홉", "스물")

        context = this
        changePose(exerciseKind)
        openCamera()
    }

    override fun onResume() {
        cameraSource?.resume()
        super.onResume()
    }

    override fun onPause() {
        cameraSource?.close()
        cameraSource = null
        super.onPause()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose)

        // keep screen on while app is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        surfaceView = findViewById(R.id.surfaceView)


        //tvFPS = findViewById(R.id.tvFps)
        tvClassificationValue1 = findViewById(R.id.tvClassificationValue1)
        //tvClassificationValue2 = findViewById(R.id.tvClassificationValue2)
        //tvClassificationValue3 = findViewById(R.id.tvClassificationValue3)

        //pushUp = findViewById(R.id.pushUP)
        //kneeUp = findViewById(R.id.kneeUP)
        //squat = findViewById(R.id.squat)
        start = findViewById(R.id.start)
        stop = findViewById(R.id.stop)
        count = findViewById(R.id.count)
        startlayout = findViewById(R.id.start_layout)

        start.setOnClickListener {
            hideKeyboard()
            startTime()
            exerciseCutNum = count.text.toString().toInt()
            thread(start = true) {
                val list = listOf<String>("오초 후에 시작합니다", "", "", "", "", "", "시작")


                for (c in list){

                    runOnUiThread {
                        speakOut2(c)
                    }

                    Thread.sleep(1000)
                    if (c == "시작"){
                        bool = true}
                }
            }
            stop.isVisible = true
            startlayout.isVisible = false

        }

        stop.setOnClickListener {
            runBlocking {
                GlobalScope.launch {
                    APIManager.postExercise(exerciseName, exerciseNum)
                }
            }
            // 백스택 액티비티들 종료하고 메인 액티비티 실행
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            finish()
        }

        counter = findViewById(R.id.counter)
        name = findViewById(R.id.exercise_name)
        time = findViewById(R.id.exercise_time)

        //pushUp.setOnClickListener { changePose(0) }
        //kneeUp.setOnClickListener { changePose(1) }
        //squat.setOnClickListener { changePose(2) }

        tts = TextToSpeech(this, this)

        if (!isCameraPermissionGranted()) {
            requestPermission()
        }
    }

    // check if permission is granted or not.
    private fun isCameraPermissionGranted(): Boolean {
        return checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }

    // open camera
    @OptIn(DelicateCoroutinesApi::class)
    private fun openCamera() {
        val dm = applicationContext.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        Log.d("w,h", "$width   $height")


        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource =
                    CameraSource(surfaceView, width, height, object : CameraSource.CameraSourceListener {
                        override fun onFPSListener(fps: Int) {
                            //tvFPS.text = getString(R.string.tfe_pe_tv_fps, fps)
                        }

                        override fun onDetectedInfo(
                            personScore: Float?,
                            poseLabels: List<Pair<String, Float>>?,
                            person: Person?
                        ) {
                            if(bool) {
                                poseLabels?.sortedByDescending { it.second }?.let {
                                    """
                                    runOnUiThread {
                                        tvClassificationValue1.text = getString(
                                            R.string.tfe_pe_tv_classification_value,
                                            convertPoseLabels(if (it.isNotEmpty()) it[0] else null)
                                        )
                                    }"""

                                    """
                                tvClassificationValue2.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.size >= 2) it[1] else null)
                                )
                                tvClassificationValue3.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.size >= 3) it[2] else null)
                                )
                                """
                                }
                                if (exerciseNum >= exerciseCutNum && end) {
                                    runBlocking {
                                        GlobalScope.launch {

                                            end = false
                                            APIManager.postExercise(exerciseName, exerciseNum)
                                        }
                                    }
                                    // 백스택 액티비티들 종료하고 메인 액티비티 실행
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)


                                    finish()
                                }

                                // 체크 포인트 확인
                                checkPose(person, poseLabels)
                            }
                        }

                    }).apply {
                        prepareCamera()
                    }
                isPoseClassifier()  // 초기 분류기 생성
                lifecycleScope.launch(Dispatchers.Main) {
                    cameraSource?.initCamera()  // 카메라 초기화
                }
            }

            // 포즈넷 모델 생성
            val poseDetector = MoveNet.create(this, device, ModelType.Thunder)
            poseDetector.let { detector ->
                cameraSource?.setDetector(detector)
            }
        }
    }

    private fun startTime(){
        time.base = SystemClock.elapsedRealtime()
        time.start()
    }

    private fun endTime(){
        time.stop()
        time.endBatchEdit()
    }

    // 자세 분류 2차 확인 함수
    private fun checkPose(person: Person?, pair: List<Pair<String, Float>>?) {
        val inputVector_x = FloatArray(17)
        val inputVector_y = FloatArray(17)

        // 사람 좌표 값 벡터 저장
        person?.keyPoints?.forEachIndexed { index, keyPoint ->
            inputVector_x[index] = keyPoint.coordinate.y
            Log.d("key_point x = $index -", (keyPoint.coordinate.y).toString())
            inputVector_y[index] = keyPoint.coordinate.x
            Log.d("key_point y = $index -", (keyPoint.coordinate.x).toString())
        }

        var boolean = true
        //자세 확인
        when (model) {
            //푸쉬업 자세
            "pushup_classifier.tflite" -> {
                exerciseName = ExerciseType.PUSH_UP
                val pushupList1 = listOf(5, 7, 6, 8, 11, 12, 5, 6)
                val pushupList2 = listOf(9, 9, 10, 10, 15, 16, 15, 16)

                for(i in 0 until pushupList1.size){
                    if(inputVector_y[pushupList1[i]] >= inputVector_y[pushupList2[i]]){
                        boolean = false
                    }
                }

                if(abs(inputVector_x[5] - inputVector_x[15])<abs(inputVector_y[6] - inputVector_y[16]) || abs(inputVector_x[5] - inputVector_x[15]) < 160){
                    boolean = false
                }

                if (boolean){
                    pair?.sortedByDescending { it.second }?.let {
                        Log.d("it[0].first", it[0].first)

                        // 어깨와 팔꿈치
                        val d1L = sqrt( (inputVector_x[6]-inputVector_x[8]).pow(2) + (inputVector_y[6]-inputVector_y[8]).pow(2) )

                        //팔꿈치와 손목
                        val d2L = sqrt( (inputVector_x[8]-inputVector_x[10]).pow(2) + (inputVector_y[8]-inputVector_y[10]).pow(2) )

                        //어깨와 손목
                        val d3L = sqrt( (inputVector_x[6]-inputVector_x[10]).pow(2) + (inputVector_y[6]-inputVector_y[10]).pow(2) )

                        val a_left =
                            Math.toDegrees(acos((d1L.pow(2) + d2L.pow(2) - d3L.pow(2)) / (2 * d1L * d2L)).toDouble())


                        Log.d("pushup-left", a_left.toString())

                        // 어깨와 팔꿈치
                        val d1 = sqrt( (inputVector_x[5]-inputVector_x[7]).pow(2) + (inputVector_y[5]-inputVector_y[7]).pow(2) )

                        //팔꿈치와 손목
                        val d2 = sqrt( (inputVector_x[7]-inputVector_x[9]).pow(2) + (inputVector_y[7]-inputVector_y[9]).pow(2) )

                        //어깨와 손목
                        val d3 = sqrt( (inputVector_x[5]-inputVector_x[9]).pow(2) + (inputVector_y[5]-inputVector_y[9]).pow(2) )

                        val a_right =
                            Math.toDegrees(acos((d1.pow(2) + d2.pow(2) - d3.pow(2)) / (2 * d1 * d2)).toDouble())


                        //Log.d("pushup-right", a1.toString())

                        when(it[0].first){
                            "bodyup_left" ->  {
                                if(a_left<=120){
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2
                                            explain = "몸의 수평을 맞추세요"}
                                        else -> {}
                                    }
                                }
                                else{ if(checkNumberTmp==1){
                                    explain = ""
                                    checkNumberTmp = 1}}
                            }
                            "bodyup_right" -> {
                                if(a_right<=120){
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2
                                            explain = "몸의 수평을 맞추세요"}
                                        else -> {}
                                    }
                                }
                                else{if(checkNumberTmp==1){
                                    explain = ""
                                    checkNumberTmp = 1}}
                            }
                            "handup_left" -> {
                                if(a_left<=120 && inputVector_x[6] > inputVector_x[10]){
                                    explain = "손을 가슴 높이에 맞추세요"
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2}
                                        else -> {}
                                    }
                                }
                                else{if(checkNumberTmp==1){
                                    explain = ""
                                    checkNumberTmp = 1}}
                            }
                            "handup_right" -> {
                                if(a_right<=120 && inputVector_x[5] < inputVector_x[9]){
                                    explain = "손을 가슴 높이에 맞추세요"
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2}
                                        else -> {}
                                    }
                                }
                                else{if(checkNumberTmp==1){
                                    explain = ""
                                    checkNumberTmp = 1}}
                            }
                            "hipup_left" -> {
                                if(a_left<=120){
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2
                                            explain = "엉덩이를 낮추세요"}
                                        else -> {}
                                    }
                                }
                                else{explain = "너무 높아요"}
                            }
                            "hipup_right" -> {
                                if(a_right<=120){
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2
                                            explain = "엉덩이를 낮추세요"}
                                        else -> {}
                                    }
                                }
                                else{if(checkNumberTmp==1){
                                    explain = ""
                                    checkNumberTmp = 1}}

                            }
                            "set_left" -> {
                                if(a_left>150) {
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                        }
                                        2 -> {
                                            checkNumberTmp = 0
                                            exerciseNum++
                                            speakOut()
                                        }
                                        3 -> {
                                            checkNumberTmp = 0
                                            exerciseNum++
                                            speakOut()
                                        }
                                        else -> {
                                        }
                                    }
                                }else{}
                            }
                            "set_right" -> {
                                if(a_right>150) {
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                        }
                                        2 -> {
                                            checkNumberTmp = 0
                                            exerciseNum++
                                            speakOut()
                                        }
                                        3 -> {
                                            checkNumberTmp = 0
                                            exerciseNum++
                                            speakOut()
                                        }
                                        else -> {
                                        }
                                    }
                                }else{}
                            }
                            "success_left" -> {
                                if(a_left<=120 || a_right<=120){
                                    when (checkNumberTmp) {
                                        1, 2 -> {
                                            checkNumberTmp = 3
                                            explain = numList[exerciseNum]
                                        }
                                        else -> {}
                                    }
                                }
                                else{if(checkNumberTmp==1){
                                    explain = "너무 높아요"
                                    checkNumberTmp = 2}}

                            }
                            "success_right" -> {
                                if(a_right<=120 || a_left<=120) {
                                    when (checkNumberTmp) {
                                        1, 2 -> {
                                            checkNumberTmp = 3
                                            explain = numList[exerciseNum]
                                        }
                                        else -> {
                                        }
                                    }
                                }
                                else{if(checkNumberTmp==1){
                                    explain = "너무 높아요"
                                    checkNumberTmp = 2}}
                            }
                            else -> {}
                        }

                    }
                }else{}


            } // 푸쉬업 끝

            "pullup_classifier.tflite" -> {
                exerciseName = ExerciseType.PULL_UP
                val pullupList1 = listOf(0, 0, 6, 5, 11, 12, 10, 9)
                val pullupList2 = listOf(5, 6, 12, 11, 15, 16, 8, 7)

                for(i in 0 until pullupList1.size){
                    if(inputVector_y[pullupList1[i]] >= inputVector_y[pullupList2[i]]){
                        boolean = false
                    }
                }
                if(abs(inputVector_x[6] - inputVector_x[16])>abs(inputVector_y[16] - inputVector_y[6]) || abs(inputVector_y[15] - inputVector_y[5]) < 160){
                    boolean = false
                }

                if (boolean) {
                    pair?.sortedByDescending { it.second }?.let {
                        when (it[0].first) {
                            "narrow" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                        explain = "손을 넓게 잡으세요"
                                    }
                                    else -> {}
                                }
                            }

                            "set" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2, 3 -> {
                                        checkNumberTmp = 0
                                        exerciseNum++
                                        speakOut()
                                    }
                                    else -> {
                                    }
                                }
                            }

                            "success" -> {
                                when (checkNumberTmp) {
                                    1, 2 -> {checkNumberTmp = 3
                                        explain = if(abs(inputVector_x[10]-inputVector_x[9]) > abs(inputVector_x[6]-inputVector_x[5])*2.7){
                                            "손을 조금 좁게 잡으세요"
                                        } else if (inputVector_x[12] > inputVector_x[6] || inputVector_x[5] > inputVector_x[11]) {
                                            "몸을 기울이지 마세요"
                                        } else{
                                            numList[exerciseNum]
                                        }
                                    }
                                    else -> {}
                                }
                            }

                            else -> {

                            }

                        }
                    }
                }else{}


            }   // 풀업 끝

            "situp_classifier.tflite" -> {
                exerciseName = ExerciseType.SIT_UP

                Log.d("situp", abs(inputVector_x[5] - inputVector_x[15]).toString() + "  -  "+abs(inputVector_y[6] - inputVector_y[16]).toString() )

                if (abs(inputVector_x[5] - inputVector_x[15]) < 160 || (inputVector_x[6] - inputVector_x[16]) <160){
                    boolean = false
                }

                if (boolean) {
                    pair?.sortedByDescending { it.second }?.let {

                        // 어깨와 팔꿈치
                        val d1L = sqrt( (inputVector_x[6]-inputVector_x[12]).pow(2) + (inputVector_y[6]-inputVector_y[12]).pow(2) )

                        //팔꿈치와 손목
                        val d2L = sqrt( (inputVector_x[12]-inputVector_x[14]).pow(2) + (inputVector_y[12]-inputVector_y[14]).pow(2) )

                        //어깨와 손목
                        val d3L = sqrt( (inputVector_x[6]-inputVector_x[14]).pow(2) + (inputVector_y[6]-inputVector_y[14]).pow(2) )

                        val a_left =
                            Math.toDegrees(acos((d1L.pow(2) + d2L.pow(2) - d3L.pow(2)) / (2 * d1L * d2L)).toDouble())


                        Log.d("pushup-left", a_left.toString())

                        // 어깨와 팔꿈치
                        val d1 = sqrt( (inputVector_x[5]-inputVector_x[11]).pow(2) + (inputVector_y[5]-inputVector_y[11]).pow(2) )

                        //팔꿈치와 손목
                        val d2 = sqrt( (inputVector_x[11]-inputVector_x[13]).pow(2) + (inputVector_y[11]-inputVector_y[13]).pow(2) )

                        //어깨와 손목
                        val d3 = sqrt( (inputVector_x[5]-inputVector_x[13]).pow(2) + (inputVector_y[5]-inputVector_y[13]).pow(2) )

                        val a_right =
                            Math.toDegrees(acos((d1.pow(2) + d2.pow(2) - d3.pow(2)) / (2 * d1 * d2)).toDouble())


                        when (it[0].first) {
                            "success_left" -> {
                                when (checkNumberTmp) {
                                    1 -> {
                                        if(a_left < 130 || a_right < 130) {
                                            explain = numList[exerciseNum]
                                            checkNumberTmp = 2
                                        }else{
                                            explain = "상체를 더 올리세요"
                                            checkNumberTmp = 3
                                        }
                                    }
                                    else -> {}
                                }
                            }
                            "success_right" -> {
                                when (checkNumberTmp) {
                                    1 -> {
                                        if(a_right < 130 || a_left < 130) {
                                            explain = numList[exerciseNum]
                                            checkNumberTmp = 2
                                        }else{
                                            explain = "상체를 더 올리세요"
                                            checkNumberTmp = 3
                                        }
                                    }
                                    else -> {}
                                }
                            }
                            "set_left" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2 -> {
                                        checkNumberTmp = 0
                                        exerciseNum++
                                        speakOut()
                                    }
                                    3 -> {
                                        checkNumberTmp = 0
                                        speakOut()
                                    }
                                    else -> {
                                    }
                                }
                            }
                            "set_right" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2 -> {
                                        checkNumberTmp = 0
                                        exerciseNum++
                                        speakOut()
                                    }
                                    3 -> {
                                        checkNumberTmp = 0
                                        speakOut()
                                    }
                                    else -> {
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }else{}

            }   // 싯업 끝

            "babelrow_classifier.tflite" -> {
                exerciseName = ExerciseType.BARBELL_ROW
                val babelrowList1 = listOf(5, 7, 6, 8, 5, 6, 11, 12)
                val babelrowList2 = listOf(9, 9, 10, 10, 11, 12, 15, 16)

                for(i in 0 until babelrowList1.size){
                    if(inputVector_y[babelrowList1[i]] >= inputVector_y[babelrowList2[i]]){
                        boolean = false
                    }
                }
                if(abs(inputVector_x[6] - inputVector_x[16])>abs(inputVector_y[16] - inputVector_y[6]) || abs(inputVector_y[15] - inputVector_y[5]) < 140){
                    boolean = false
                }

                if (boolean) {
                    pair?.sortedByDescending { it.second }?.let {
                        when (it[0].first) {
                            "success_left" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                        explain = numList[exerciseNum]
                                    }
                                    else -> {}
                                }
                            }
                            "success_right" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                        explain = numList[exerciseNum]
                                    }
                                    else -> {}
                                }
                            }
                            "set_left" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2 -> {
                                        checkNumberTmp = 0
                                        exerciseNum++
                                        speakOut()
                                    }
                                    else -> {
                                    }
                                }
                            }
                            "set_right" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2 -> {
                                        checkNumberTmp = 0
                                        exerciseNum++
                                        speakOut()
                                    }
                                    else -> {
                                    }
                                }
                            }
                            else -> {}

                        }
                    }
                }else{}

            }   // 바벨로우 끝

            "deadlift_classifier.tflite" -> {
                exerciseName = ExerciseType.DEAD_LIFT
                val deadliftList1 = listOf(5, 7, 6, 8, 5, 6, 11, 12)
                val deadliftList2 = listOf(9, 9, 10, 10, 11, 12, 15, 16)

                for(i in 0 until deadliftList1.size){
                    if(inputVector_y[deadliftList1[i]] >= inputVector_y[deadliftList2[i]]){
                        boolean = false
                    }
                }
                if(abs(inputVector_x[6] - inputVector_x[16])>abs(inputVector_y[16] - inputVector_y[6]) || abs(inputVector_y[15] - inputVector_y[5]) < 110){
                    boolean = false
                }

                if (boolean) {
                    pair?.sortedByDescending { it.second }?.let {
                        when (it[0].first) {
                            "stand_left" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 0
                                        exerciseNum++
                                        speakOut()
                                    }
                                    else -> {}
                                }
                            }
                            "stand_right" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 0
                                        exerciseNum++
                                        speakOut()
                                    }
                                    else -> {}
                                }
                            }
                            "success_left" -> {
                                if(inputVector_y[10] > inputVector_y[14]){
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                            explain = numList[exerciseNum]
                                        }
                                        else -> {
                                        }
                                    }}
                            }
                            "success_right" -> {
                                if(inputVector_y[10] > inputVector_y[14]){
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                            explain = numList[exerciseNum]
                                        }
                                        else -> {
                                        }
                                    }}
                            }
                            else -> {
                                if (inputVector_y[10] > inputVector_y[14]) {
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                            explain = "무릎을 내밀지 마세요"
                                        }

                                        else -> {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else{}

            }   // 데드리프트 끝

            "dumbelcurl_classifier.tflite" -> {
                exerciseName = ExerciseType.DUMBBELL_CURL
                val dumbbelcurlList1 = listOf(0, 5, 6, 5, 6, 11, 12)
                val dumbbelcurlList2 = listOf(5, 7, 8, 11, 12, 15, 16)

                Log.d("dumbel  ", inputVector_x[9].toString() +" - "+ inputVector_x[10].toString())

                for(i in 0 until dumbbelcurlList1.size){
                    if(inputVector_y[dumbbelcurlList1[i]] >= inputVector_y[dumbbelcurlList2[i]]){
                        boolean = false
                    }
                }
                if(abs(inputVector_x[6] - inputVector_x[16])>abs(inputVector_y[16] - inputVector_y[6]) || abs(inputVector_y[15] - inputVector_y[5]) < 160){
                    boolean = false
                }

                if(inputVector_y[9] > inputVector_y[7] && inputVector_y[10] > inputVector_y[8]){}

                if (boolean) {
                    pair?.sortedByDescending { it.second }?.let {
                        when (it[0].first) {
                            "left" -> {
                                if (inputVector_y[9] < inputVector_y[7] || inputVector_y[10] < inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2
                                            explain = numList[exerciseNum]
                                        }
                                        3 -> {
                                            checkNumberTmp = 4
                                            explain = numList[exerciseNum]
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                            "left_wide" -> {
                                if (inputVector_y[9] < inputVector_y[7] || inputVector_y[10] < inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2
                                            explain = "오른쪽 팔꿈치를 붙이세요"
                                        }
                                        3 -> {
                                            checkNumberTmp = 4
                                            explain = "오른쪽 팔꿈치를 붙이세요"
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                            "right" -> {
                                if (inputVector_y[9] < inputVector_y[7] || inputVector_y[10] < inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2
                                            explain = numList[exerciseNum]
                                        }
                                        3 -> {
                                            checkNumberTmp = 4
                                            explain = numList[exerciseNum]
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                            "right_wide" -> {
                                if (inputVector_y[9] < inputVector_y[7] || inputVector_y[10] < inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2
                                            explain = "왼쪽 팔꿈치를 붙이세요"
                                        }
                                        3 -> {
                                            checkNumberTmp = 4
                                            explain = "왼쪽 팔꿈치를 붙이세요"
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                            "set" -> {
                                dumbellNum++
                                if(dumbellNum > 3){
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                        }
                                        2 -> {
                                            checkNumberTmp = 3
                                        }
                                        4 -> {
                                            checkNumberTmp = 0
                                            exerciseNum++
                                            speakOut()
                                        }
                                        else -> {
                                        }
                                    }
                                }

                            }
                            else -> {}
                        }
                    }
                }else{}

            } // 덤벨컬 끝

            "babelcurl_classifier.tflite" -> {
                exerciseName = ExerciseType.BARBELL_CURL


                val babelcurlList1 = listOf(0, 5, 6, 5, 6, 11, 12)
                val babelcurlList2 = listOf(5, 7, 8, 11, 12, 15, 16)


                for(i in 0 until babelcurlList1.size){
                    if(inputVector_y[babelcurlList1[i]] >= inputVector_y[babelcurlList2[i]]){
                        boolean = false
                    }
                }

                if(abs(inputVector_x[6] - inputVector_x[16])>abs(inputVector_y[16] - inputVector_y[6]) || abs(inputVector_y[15] - inputVector_y[5]) < 160){
                    boolean = false
                }

                if (boolean) {
                    pair?.sortedByDescending { it.second }?.let {
                        when (it[0].first) {

                            "set" -> {
                                if(inputVector_y[9] > inputVector_y[7] && inputVector_y[10] > inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                        }
                                        2, 3 -> {
                                            checkNumberTmp = 0
                                            exerciseNum++
                                            speakOut()
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                            "shoulderup" -> {
                                if(inputVector_y[9] < inputVector_y[7] && inputVector_y[10] < inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2
                                            explain = "어깨를 내리고 고정하세요"
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                            "success" -> {
                                if(inputVector_y[9] < inputVector_y[7] && inputVector_y[10] < inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        1, 2 -> {checkNumberTmp = 3
                                            explain = numList[exerciseNum]
                                        }
                                        else -> {}
                                    }
                                }

                            }
                            "wide" -> {
                                if(inputVector_y[9] < inputVector_y[7] && inputVector_y[10] < inputVector_y[8]) {
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2

                                            explain = if(inputVector_x[6] + 10 < inputVector_x[8] || inputVector_x[5] - 10 > inputVector_x[7]){
                                                "팔꿈치를 몸에 붙이세요"
                                            } else{
                                                numList[exerciseNum]
                                            }
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }

                            else -> {}

                        }
                    }
                }else{}

            } // 바벨컬 끝

            "plank_classifier.tflite" -> {
                exerciseName = ExerciseType.PLANK

                if(abs(inputVector_x[6] - inputVector_x[16])<abs(inputVector_y[6] - inputVector_y[16]) || abs(inputVector_x[5] - inputVector_x[15]) < 160){
                    boolean = false
                }
                if (boolean) {
                    pair?.sortedByDescending { it.second }?.let {
                        when (it[0].first) {

                            "hipdown_left" -> {
                                plankNum++
                                if(plankNum > 8){
                                    speakOut2("몸을 수평으로 맞추세요")
                                    plankNum = 0
                                }
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                    }
                                    else -> {}
                                }
                            }
                            "hipdown_right" -> {
                                plankNum++
                                if(plankNum > 8){
                                    speakOut2("몸을 수평으로 맞추세요")
                                    plankNum = 0
                                }
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                    }
                                    else -> {}
                                }
                            }
                            "hipup_left" -> {
                                plankNum++
                                if(plankNum > 8){
                                    speakOut2("엉덩이를 내리세요")
                                    plankNum = 0
                                }

                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                    }
                                    else -> {}
                                }}
                            "hipup_right" -> {
                                plankNum++
                                if(plankNum > 8){
                                    speakOut2("엉덩이를 내리세요")
                                    plankNum = 0
                                }
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                    }
                                    else -> {}
                                }
                            }
                            "success-left" -> {
                                plankNum = 0
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2
                                    }
                                    else -> {}
                                }
                            }
                            "success-right" -> {
                                plankNum = 0
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2

                                    }
                                    else -> {}
                                }
                            }
                            "set_left" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2 -> {
                                        plankNum++
                                        if(plankNum > 16){
                                            val pauseTime = time.base - SystemClock.elapsedRealtime()
                                            var saveTime = -pauseTime
                                            saveTime /= 1000
                                            if(saveTime.toInt() < 5)
                                                saveTime = 0
                                            APIManager.postCardioExercise(CardioExerciseType.STAIR_CLIMBING, saveTime.toInt() - 4)
                                            finish()
                                        }
                                    }
                                    else -> {
                                    }
                                }
                            }
                            "set_right" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2 -> {
                                        plankNum++
                                        if(plankNum > 16){
                                            val pauseTime = time.base - SystemClock.elapsedRealtime()
                                            var saveTime = -pauseTime
                                            saveTime /= 1000
                                            if(saveTime.toInt() < 5)
                                                saveTime = 0
                                            APIManager.postCardioExercise(CardioExerciseType.STAIR_CLIMBING, saveTime.toInt() - 4)
                                            finish()
                                        }
                                    }
                                    else -> {
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }else{}

            } // 플랭크 끝

            "squat_classifier.tflite" -> {
                exerciseName = ExerciseType.SQUAT

                val squatCheckList1 = listOf(0, 5, 6, 5, 6, 11, 12)
                val squatCheckList2 = listOf(5, 7, 8, 11, 12, 15, 16)

                for(i in 0 until squatCheckList1.size){
                    if(inputVector_y[squatCheckList1[i]] >= inputVector_y[squatCheckList2[i]]){
                        boolean = false
                    }
                }
                if(inputVector_y[squatCheckList1[0]] >= inputVector_y[10]){boolean = false}
                else if(inputVector_y[squatCheckList1[0]] >= inputVector_y[9]){boolean = false}

                if(abs(inputVector_x[6] - inputVector_x[16])>abs(inputVector_y[16] - inputVector_y[6]) || abs(inputVector_y[15] - inputVector_y[5]) < 120){
                    boolean = false
                }

                if (boolean){
                    pair?.sortedByDescending { it.second }?.let {
                        Log.d("it[0].first", it[0].first)

                        // 엉덩이와 무릅
                        val d1L = sqrt( (inputVector_x[12]-inputVector_x[14]).pow(2) + (inputVector_y[12]-inputVector_y[14]).pow(2) )

                        //무릅과 발목
                        val d2L = sqrt( (inputVector_x[14]-inputVector_x[16]).pow(2) + (inputVector_y[14]-inputVector_y[16]).pow(2) )

                        //엉덩이와 발목
                        val d3L = sqrt( (inputVector_x[12]-inputVector_x[16]).pow(2) + (inputVector_y[12]-inputVector_y[16]).pow(2) )

                        val a_left =
                            Math.toDegrees(acos((d1L.pow(2) + d2L.pow(2) - d3L.pow(2)) / (2 * d1L * d2L)).toDouble())


                        // 엉덩이와 무릅
                        val d1 = sqrt( (inputVector_x[11]-inputVector_x[13]).pow(2) + (inputVector_y[11]-inputVector_y[13]).pow(2) )

                        //무릅과 발목
                        val d2 = sqrt( (inputVector_x[13]-inputVector_x[15]).pow(2) + (inputVector_y[13]-inputVector_y[15]).pow(2) )

                        //엉덩이와 발목
                        val d3 = sqrt( (inputVector_x[11]-inputVector_x[15]).pow(2) + (inputVector_y[11]-inputVector_y[15]).pow(2) )

                        val a_right =
                            Math.toDegrees(acos((d1.pow(2) + d2.pow(2) - d3.pow(2)) / (2 * d1 * d2)).toDouble())

                        Log.d("squat_angle", "각도기 = $a_left - $a_right")


                        when(it[0].first){
                            "set" -> {
                                squatNum++
                                if(a_left>170 || a_right>170 && squatNum > 2) {
                                    squatNum = 0
                                    when (checkNumberTmp) {
                                        0 -> {
                                            checkNumberTmp = 1
                                        }
                                        2, 3 -> {
                                            checkNumberTmp = 0
                                            exerciseNum++
                                            speakOut()
                                        }
                                        4 -> {
                                            checkNumberTmp = 0
                                            //speakOut()
                                        }
                                        else -> {
                                        }
                                    }
                                }   // if
                            }
                            "narrow" -> {
                                when (checkNumberTmp) {
                                    1, 2 -> {
                                        checkNumberTmp = 2
                                        explain = "무릎을 넓히세요"
                                    }
                                    else -> {
                                    }
                                } // when end

                            }
                            "wide" -> {
                                when (checkNumberTmp) {
                                    1, 2 -> {
                                        checkNumberTmp = 2
                                        explain = "무릎을 좁히세요"
                                    }
                                    else -> {
                                    }
                                } // when end
                            }
                            "success" -> {
                                when (checkNumberTmp) {
                                    1 -> {
                                        checkNumberTmp = 3
                                        explain = numList[exerciseNum]
                                    }
                                    else -> {
                                    }
                                }

                            }
                            else -> {
                            }
                        }   // when end

                    }
                }else{}

            }   // 스쿼트 끝

            else -> {

            }
        }

        runOnUiThread {
            counter.text = exerciseNum.toString()
            //name.text = exerciseName
        }
    }

    private fun convertPoseLabels(pair: Pair<String, Float>?): String {
        if (pair == null) return "empty"
        return "${pair.first} (${String.format("%.2f", pair.second)})"
    }

    // 분류기 객체 생성
    private fun isPoseClassifier() {
        cameraSource?.setClassifier(if (isClassifyPose) PoseClassifier.create(this, model, txt) else null)
    }

    // 분류기 변경
    private fun changePose(poseNum: String?){
        when(poseNum){
            "푸쉬업" -> { model = "pushup_classifier.tflite"
                txt = "pushup_labels.txt" }

            "스쿼트" -> { model = "squat_classifier.tflite"
                txt = "squat_labels.txt" }

            "풀업" -> { model = "pullup_classifier.tflite"
                txt = "pullup_labels.txt" }

            "싯업" -> { model = "situp_classifier.tflite"
                txt = "situp_labels.txt" }

            "바벨로우" -> { model = "babelrow_classifier.tflite"
                txt = "babelrow_labels.txt" }

            "데드리프트" -> { model = "deadlift_classifier.tflite"
                txt = "deadlift_labels.txt" }

            "덤벨컬" -> { model = "dumbelcurl_classifier.tflite"
                txt = "dumbelcurl_labels.txt" }

            "바벨컬" -> { model = "babelcurl_classifier.tflite"
                txt = "babelcurl_labels.txt" }

            "플랭크" -> { model = "plank_classifier.tflite"
                txt = "plank_labels.txt" }


            else -> { finish()}
        }
        cameraSource?.setClassifier(if (isClassifyPose) PoseClassifier.create(this, model, txt) else null)

    }



    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                // You can use the API that requires the permission.
                openCamera()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // do nothing
                }
                .create()

        companion object {

            @JvmStatic
            private val ARG_MESSAGE = "message"

            @JvmStatic
            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }

    override fun onDestroy() {
        if (tts != null) { // 사용한 TTS객체 제거
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onInit(status: Int) { // OnInitListener를 통해서 TTS 초기화
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.KOREA) // TTS언어 한국어로 설정
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                Log.e("TTS", "This Language is not supported")
            } else {
                //speakOut() // onInit에 음성출력할 텍스트를 넣어줌
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun speakOut() {
        val text: CharSequence = explain
        tts?.setPitch(1.toFloat()) // 음성 톤 높이 지정
        tts?.setSpeechRate(1.toFloat()) // 음성 속도 지정

        // 첫 번째 매개변수: 음성 출력을 할 텍스트
        // 두 번째 매개변수: 1. TextToSpeech.QUEUE_FLUSH - 진행중인 음성 출력을 끊고 이번 TTS의 음성 출력
        //                 2. TextToSpeech.QUEUE_ADD - 진행중인 음성 출력이 끝난 후에 이번 TTS의 음성 출력
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun speakOut2(str : String) {
        val text: CharSequence = str
        tts?.setPitch(1.toFloat()) // 음성 톤 높이 지정
        tts?.setSpeechRate(1.toFloat()) // 음성 속도 지정

        // 첫 번째 매개변수: 음성 출력을 할 텍스트
        // 두 번째 매개변수: 1. TextToSpeech.QUEUE_FLUSH - 진행중인 음성 출력을 끊고 이번 TTS의 음성 출력
        //                 2. TextToSpeech.QUEUE_ADD - 진행중인 음성 출력이 끝난 후에 이번 TTS의 음성 출력
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "id1")
    }

    /**
     * Hiding keyboard after every button press
     */
    private fun hideKeyboard() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}