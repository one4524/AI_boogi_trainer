package com.example.boogi_trainer.ui.food

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.iterator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.boogi_trainer.MainActivity
import com.example.boogi_trainer.databinding.ActivityFoodDetailBinding
import com.example.boogi_trainer.env.ImageUtils
import com.example.boogi_trainer.env.Logger
import com.example.boogi_trainer.env.Utils
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.repository.MealType
import com.example.boogi_trainer.tflite.Classifier
import com.example.boogi_trainer.tflite.YoloV5Classifier
import com.example.boogi_trainer.tracking.MultiBoxTracker
import kotlinx.android.synthetic.main.recyclerview_food_list_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.*

class FoodDetailActivity : AppCompatActivity(){

    val MINIMUM_CONFIDENCE_TF_OD_API = 0.6f

    private val LOGGER: Logger = Logger()

    val TF_OD_API_INPUT_SIZE = 640

    private val TF_OD_API_IS_QUANTIZED = false

    private val TF_OD_API_MODEL_FILE = "best-fp16.tflite"

    private val TF_OD_API_LABELS_FILE = "file:///android_asset/50labels.txt"

    // Minimum detection confidence to track a detection.
    private val MAINTAIN_ASPECT = true
    private val sensorOrientation = 90

    private var detector: Classifier? = null

    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    private var tracker: MultiBoxTracker? = null

    private var previewWidth = 0
    private var previewHeight = 0

    private var sourceBitmap: Bitmap? = null
    private var cropBitmap: Bitmap? = null


    private lateinit var binding: ActivityFoodDetailBinding

    // 리사이클러뷰가 불러올 목록
    private var data: MutableList<FoodDetailData> = mutableListOf()


    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // FoodCameraActivity에서 넘어온 Intent 받음
        val intent = getIntent()
        if (intent.getStringExtra("from") == "camera") {
            val uri = intent.getParcelableExtra<Uri>("imageUri")
            binding.imageView.setImageURI(uri) // imageView에 가져온 이미지 삽입
        }
        else {
            val food = intent.getStringExtra("foodName")
            data.add(FoodDetailData(food!!, "100"))
        }
        val mealTime = intent.getStringExtra("mealTime")
        binding.textMealTime.text = mealTime // 아침 점심 저녁 표시
        refreshRecyclerView()



        // 음식 검출 Thread 시작 //
        val drawable = binding.imageView.drawable
        sourceBitmap = drawable.toBitmap()
        cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE)

        initBox()
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo

        val handler = Handler(Looper.getMainLooper())

        Thread {
            val results: List<Classifier.Recognition> =
                detector!!.recognizeImage(cropBitmap)
            handler.post { handleResult(cropBitmap!!, results) }
            runOnUiThread{
                refreshRecyclerView()
            }
        }.start()
        //////////////////////////

        setTotalKcal()

        // 음식 추가 버튼
        binding.buttonAddFood.setOnClickListener {
            data = updateData()
            data.add(FoodDetailData("쌀밥", "100"))
            setTotalKcal()
            refreshRecyclerView()
        }

        // 리사이클러뷰에서 데이터 가져와 서버로 보내야됨
        binding.buttonComplete.setOnClickListener {
            data = updateData()
            var kind = MealType.BREAKFAST
            when (mealTime) {
                "아침" -> kind = MealType.BREAKFAST
                "점심" -> kind = MealType.LUNCH
                "저녁" -> kind = MealType.DINNER
            }
            runBlocking {
                GlobalScope.launch {
                    for (food in data) {
                        APIManager.postMeal(food.name, food.gram.toInt(), kind)
                    }
                }
            }
//            // 백스택 액티비티들 종료하고 메인 액티비티 실행
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(intent)
            finish()
        }
        // 리사이클러뷰에서 데이터 가져와 서버로 보내야됨
    }

    private fun setTotalKcal() {
        var kcal = 0
        for (item in data) {
            kcal = (kcal + APIManager.getFoodKcal(item.name, item.gram.toInt())).toInt()
        }
        binding.totalKcalMeal.text = kcal.toString()
    }

    private fun updateData(): MutableList<FoodDetailData> {
        val rvFoodList = binding.rvFoodList
        val mutableList = mutableListOf<FoodDetailData>()

        for (item in rvFoodList) {
            val name = item.listItemFoodName.text.toString()
            val gram = item.editTextGram.text.toString()
            with(mutableList) {
                add(FoodDetailData(name, gram))
            }
        }

        return mutableList
    }

    private fun refreshRecyclerView() {
        val adapter = FoodDetailAdapter()
        adapter.foodData = data
        binding.rvFoodList.adapter = adapter
        binding.rvFoodList.layoutManager = LinearLayoutManager(this)
    }

    private fun initBox() {
        previewHeight = TF_OD_API_INPUT_SIZE
        previewWidth = TF_OD_API_INPUT_SIZE
        frameToCropTransform = ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
            sensorOrientation, MAINTAIN_ASPECT
        )
        cropToFrameTransform = Matrix()
        frameToCropTransform!!.invert(cropToFrameTransform)
        tracker = MultiBoxTracker(this)
        binding.trackingOverlay.addCallback { canvas -> tracker!!.draw(canvas) }
        tracker!!.setFrameConfiguration(
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_INPUT_SIZE,
            sensorOrientation
        )
        try {
            detector = YoloV5Classifier.create(
                assets,
                TF_OD_API_MODEL_FILE,
                TF_OD_API_LABELS_FILE,
                TF_OD_API_IS_QUANTIZED,
                TF_OD_API_INPUT_SIZE
            )
        } catch (e: IOException) {
            e.printStackTrace()
            LOGGER.e(e, "Exception initializing classifier!")
            val toast = Toast.makeText(
                applicationContext, "Classifier could not be initialized", Toast.LENGTH_SHORT
            )
            toast.show()
            finish()
        }
    }

    private fun handleResult(bitmap: Bitmap, results: List<Classifier.Recognition>) {
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.0f
        val mappedRecognitions: MutableList<Classifier.Recognition> = LinkedList<Classifier.Recognition>()
        for (result in results) {
            val location: RectF = result.location
            if (location != null && result.confidence >= MINIMUM_CONFIDENCE_TF_OD_API) {
                canvas.drawRect(location, paint)
                cropToFrameTransform!!.mapRect(location)
                result.setLocation(location)
                mappedRecognitions.add(result)
                LOGGER.e(result.title.toString() + "_title_")
                // result.getTitle() = 출력값
                with(data) {
                    add(FoodDetailData(result.title.toString(), "100"))
                }
            }
        }
        tracker!!.trackResults(mappedRecognitions, Random().nextInt().toLong())
        binding.trackingOverlay.postInvalidate()
        binding.imageView.setImageBitmap(bitmap)
    }

}