package com.example.boogi_trainer

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.boogi_trainer.camera.CameraSource
import com.example.boogi_trainer.data.Device
import com.example.boogi_trainer.data.Person
import com.example.boogi_trainer.ml.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log


class PoseActivity : AppCompatActivity() {
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
    private var exerciseName : String = ""
    private var checkNumberTmp = 0

    /** Default device is CPU */
    private var device = Device.CPU


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

        tvFPS = findViewById(R.id.tvFps)
        tvClassificationValue1 = findViewById(R.id.tvClassificationValue1)
        tvClassificationValue2 = findViewById(R.id.tvClassificationValue2)
        tvClassificationValue3 = findViewById(R.id.tvClassificationValue3)

        pushUp = findViewById(R.id.pushUP)
        kneeUp = findViewById(R.id.kneeUP)
        squat = findViewById(R.id.squat)

        pushUp.setOnClickListener { changePose(0) }
        kneeUp.setOnClickListener { changePose(1) }
        squat.setOnClickListener { changePose(2) }

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
        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource =
                    CameraSource(surfaceView, object : CameraSource.CameraSourceListener {
                        override fun onFPSListener(fps: Int) {
                           tvFPS.text = getString(R.string.tfe_pe_tv_fps, fps)
                        }

                        override fun onDetectedInfo(
                            personScore: Float?,
                            poseLabels: List<Pair<String, Float>>?,
                            person: Person?
                        ) {
                            poseLabels?.sortedByDescending { it.second }?.let {
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

    // 자세 분류 2차 확인 함수
    private fun checkPose(person: Person?, pair: List<Pair<String, Float>>?) {
        val inputVector_x = FloatArray(17)
        val inputVector_y = FloatArray(17)

        // 사람 좌표 값 벡터 저장
        person?.keyPoints?.forEachIndexed { index, keyPoint ->
            inputVector_x[index] = keyPoint.coordinate.y
            Log.d("key_point x = $index -", (keyPoint.coordinate.x).toString())
            inputVector_y[index] = keyPoint.coordinate.x
            Log.d("key_point y = $index -", (keyPoint.coordinate.y).toString())
        }

        //자세 확인
        when (model) {
            "pushup_classifier.tflite" -> {
                exerciseName = "pushup"

            } // 푸쉬업 끝

            "kneeup_classifier.tflite" -> {
                exerciseName = "kneeup"

            }   // 니업 끝

            "squat_classifier.tflite" -> {
                exerciseName = "squat"
                val squatCheckList = listOf(0,5, 5,7, 6,8, 5,11, 6,10, 6,12, 11,16, 12,15)
                var boolean = true
                for(i in squatCheckList.indices step 2){
                    if(inputVector_y[i] >= inputVector_y[i+1]){
                        boolean = false
                    }
                }
                if (boolean){
                    pair?.sortedByDescending { it.second }?.let {
                        Log.d("it[0].first", it[0].first)
                        when(it[0].first){
                            "down_1" ->  {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            "down_2" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            "narrow" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            "stand" -> {
                                when (checkNumberTmp) {
                                    0 -> {
                                        checkNumberTmp = 1
                                    }
                                    2 -> {
                                        checkNumberTmp = 0
                                        exerciseNum++
                                    }
                                    else -> {
                                    }
                                }
                            }
                            "wide" -> {
                                when (checkNumberTmp) {
                                    1 -> {checkNumberTmp = 2}
                                    else -> {}
                                }
                            }
                            else -> {}
                        }

                    }
                }else{}

            }   // 스쿼트 끝

            else -> {

            }
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
    private fun changePose(poseNum : Int){
        when(poseNum){
            0 -> { model = "pushup_classifier.tflite"
                txt = "pushup_labels.txt" }

            1 -> { model = "kneeup_classifier.tflite"
                txt = "kneeup_labels.txt" }

            2 -> { model = "squat_classifier.tflite"
                txt = "squat_labels.txt" }

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
}