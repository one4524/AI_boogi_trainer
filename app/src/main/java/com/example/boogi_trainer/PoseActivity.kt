package com.example.boogi_trainer

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
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
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.boogi_trainer.camera.CameraSource
import com.example.boogi_trainer.data.Device
import com.example.boogi_trainer.data.Person
import com.example.boogi_trainer.ml.*
import com.example.boogi_trainer.repository.APIManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

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
    private var exerciseName : String = "운동"
    private var checkNumberTmp = 0
    private var explain : String = "잘했어요"

    /** Default device is CPU */
    private var device = Device.CPU

    private var tts: TextToSpeech? = null

    private lateinit var counter: TextView
    private lateinit var name: TextView
    private lateinit var time: Chronometer
    private lateinit var pushUp: Button
    private lateinit var kneeUp: Button
    private lateinit var squat: Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pose)

        // keep screen on while app is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        surfaceView = findViewById(R.id.surfaceView)

        //tvFPS = findViewById(R.id.tvFps)
        //tvClassificationValue1 = findViewById(R.id.tvClassificationValue1)
        //tvClassificationValue2 = findViewById(R.id.tvClassificationValue2)
        //tvClassificationValue3 = findViewById(R.id.tvClassificationValue3)

        //pushUp = findViewById(R.id.pushUP)
        //kneeUp = findViewById(R.id.kneeUP)
        //squat = findViewById(R.id.squat)
        counter = findViewById(R.id.counter)
        name = findViewById(R.id.exercise_name)
        time = findViewById(R.id.exercise_time)

        //pushUp.setOnClickListener { changePose(0) }
        //kneeUp.setOnClickListener { changePose(1) }
        //squat.setOnClickListener { changePose(2) }

        tts = TextToSpeech(this, this)

        SystemClock.elapsedRealtime()

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
    private fun openCamera() {
        val dm = applicationContext.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        Log.d("w,h", "$width   $height")

        startTime()

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
                            poseLabels?.sortedByDescending { it.second }?.let {
                                """
                                tvClassificationValue1.text = getString(
                                    R.string.tfe_pe_tv_classification_value,
                                    convertPoseLabels(if (it.isNotEmpty()) it[0] else null)
                                )
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
                            if(exerciseNum >= 20){
                                runBlocking{
                                    GlobalScope.launch {


                                        APIManager.postExercise(exerciseName, exerciseNum)
                                    }
                                }

                                finish()
                            }
                            // 체크 포인트 확인
                            checkPose(person, poseLabels)
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
            poseDetector?.let { detector ->
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
                exerciseName = "푸쉬업"
                val pushupList1 = listOf(5, 7, 6, 8, 11, 12, 5, 6)
                val pushupList2 = listOf(9, 9, 10, 10, 15, 16, 15, 16)

                for(i in 0 until pushupList1.size){
                    if(inputVector_y[pushupList1[i]] >= inputVector_y[pushupList2[i]]){
                        boolean = false
                    }
                }

                if (boolean){
                    pair?.sortedByDescending { it.second }?.let {
                        Log.d("it[0].first", it[0].first)

                        // 어깨와 팔꿈치
                        var d1L = sqrt( (inputVector_x[6]-inputVector_x[8]).pow(2) + (inputVector_y[6]-inputVector_y[8]).pow(2) )

                        //팔꿈치와 손목
                        var d2L = sqrt( (inputVector_x[8]-inputVector_x[10]).pow(2) + (inputVector_y[8]-inputVector_y[10]).pow(2) )

                        //어깨와 손목
                        var d3L = sqrt( (inputVector_x[6]-inputVector_x[10]).pow(2) + (inputVector_y[6]-inputVector_y[10]).pow(2) )

                        var a_left =
                            Math.toDegrees(acos((d1L.pow(2) + d2L.pow(2) - d3L.pow(2)) / (2 * d1L * d2L)).toDouble())


                        Log.d("pushup-left", a_left.toString())

                        // 어깨와 팔꿈치
                        var d1 = sqrt( (inputVector_x[5]-inputVector_x[7]).pow(2) + (inputVector_y[5]-inputVector_y[7]).pow(2) )

                        //팔꿈치와 손목
                        var d2 = sqrt( (inputVector_x[7]-inputVector_x[9]).pow(2) + (inputVector_y[7]-inputVector_y[9]).pow(2) )

                        //어깨와 손목
                        var d3 = sqrt( (inputVector_x[5]-inputVector_x[9]).pow(2) + (inputVector_y[5]-inputVector_y[9]).pow(2) )

                        var a_right =
                            Math.toDegrees(acos((d1.pow(2) + d2.pow(2) - d3.pow(2)) / (2 * d1 * d2)).toDouble())


                        //Log.d("pushup-right", a1.toString())


                        when(it[0].first){
                            "fail_1_left" ->  {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            "fail_1_right" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            "fail_2_left" -> {
                                if(a_left<=100){
                                    explain = "완벽해요"
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2}
                                        else -> {}
                                    }
                                }
                                else{explain = "너무 높아요"}
                            }
                            "fail_2_right" -> {
                                if(a_right<=100){
                                    explain = "완벽해요"
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2}
                                        else -> {}
                                    }
                                }
                                else{explain = "너무 높아요"}
                            }
                            "fail_3_left" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            "fail_3_right" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            "set_left" -> {
                                if(a_left>140) {
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
                                }else{explain = "잘못됐어요"}
                            }
                            "set_right" -> {
                                if(a_right>140) {
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
                                }else{explain = "잘못됐어요"}
                            }
                            "success_left" -> {
                                if(a_left<=100){
                                    explain = "완벽해요"
                                    when (checkNumberTmp) {
                                        1 -> {checkNumberTmp = 2
                                        }
                                        else -> {}
                                    }
                                }
                                else{explain = "너무 높아요"}

                            }
                            "success_right" -> {
                                if(a_right<=100) {
                                    explain = "완벽해요"
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2
                                        }
                                        else -> {
                                        }
                                    }
                                }
                                else{explain = "너무 높아요"}
                            }
                            else -> {}
                        }

                    }
                }else{}


            } // 푸쉬업 끝

            "kneeup_classifier.tflite" -> {
                exerciseName = "kneeup"

            }   // 니업 끝

            "squat_classifier.tflite" -> {
                exerciseName = "스쿼트"

                val squatCheckList1 = listOf(0, 5, 6, 5, 6, 11, 12)
                val squatCheckList2 = listOf(5, 7, 8, 11, 12, 15, 16)

                for(i in 0 until squatCheckList1.size){
                    if(inputVector_y[squatCheckList1[i]] >= inputVector_y[squatCheckList2[i]]){
                        boolean = false
                    }
                }
                if(inputVector_y[squatCheckList1[0]] >= inputVector_y[10]){boolean = false}
                else if(inputVector_y[squatCheckList1[0]] >= inputVector_y[9]){boolean = false}


                if (boolean){
                    pair?.sortedByDescending { it.second }?.let {
                        Log.d("it[0].first", it[0].first)

                        Log.d("abab11", inputVector_y[0].toString()+ "  "+ inputVector_y[5].toString()+ "  " + inputVector_y[11].toString())

                        // 엉덩이와 무릅
                        var d1L = sqrt( (inputVector_x[12]-inputVector_x[14]).pow(2) + (inputVector_y[12]-inputVector_y[14]).pow(2) )

                        //무릅과 발목
                        var d2L = sqrt( (inputVector_x[14]-inputVector_x[16]).pow(2) + (inputVector_y[14]-inputVector_y[16]).pow(2) )

                        //엉덩이와 발목
                        var d3L = sqrt( (inputVector_x[12]-inputVector_x[16]).pow(2) + (inputVector_y[12]-inputVector_y[16]).pow(2) )

                        var a_left =
                            Math.toDegrees(acos((d1L.pow(2) + d2L.pow(2) - d3L.pow(2)) / (2 * d1L * d2L)).toDouble())


                        // 엉덩이와 무릅
                        var d1 = sqrt( (inputVector_x[11]-inputVector_x[13]).pow(2) + (inputVector_y[11]-inputVector_y[13]).pow(2) )

                        //무릅과 발목
                        var d2 = sqrt( (inputVector_x[13]-inputVector_x[15]).pow(2) + (inputVector_y[13]-inputVector_y[15]).pow(2) )

                        //엉덩이와 발목
                        var d3 = sqrt( (inputVector_x[11]-inputVector_x[15]).pow(2) + (inputVector_y[11]-inputVector_y[15]).pow(2) )

                        var a_right =
                            Math.toDegrees(acos((d1.pow(2) + d2.pow(2) - d3.pow(2)) / (2 * d1 * d2)).toDouble())


                        when(it[0].first){
                            "stand" -> {
                                if(a_left>140 && a_right>140) {
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
                            }
                            else -> {
                                if (a_left < 140 && a_right < 140) {
                                    if (a_left < 100 && a_right < 100) {
                                        explain = "완벽해요"

                                    } else{
                                        if (checkNumberTmp != 2) {
                                            explain = "너무 높아요"
                                        }
                                    }
                                    when (checkNumberTmp) {
                                        1 -> {
                                            checkNumberTmp = 2
                                        }
                                        else -> {
                                        }
                                    } // when end


                                val len = inputVector_x[12] - inputVector_x[11]
                                if (inputVector_x[14] < inputVector_x[6] || inputVector_x[13] > inputVector_x[5]) {
                                    explain = "다리 사이가 좁아요"
                                } else if (inputVector_x[14] > inputVector_x[12] + len && inputVector_x[11] - len > inputVector_x[13]) {
                                    explain = "다리 사이가 넓어요"
                                } else {
                                }

                                } // 140 end


                            }
                        }

                    }
                }else{}

            }   // 스쿼트 끝

            else -> {

            }
        }

        runOnUiThread {
            counter.text = exerciseNum.toString()
            //name.text = exerciseName
            name.text = explain
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

            "니업" -> { model = "kneeup_classifier.tflite"
                txt = "kneeup_labels.txt" }


            else -> {}
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
}