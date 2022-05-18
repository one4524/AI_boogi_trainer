package com.example.boogi_trainer
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.boogi_trainer.databinding.ActivityRunningBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource

class RunningActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private lateinit var binding: ActivityRunningBinding

    // 뒤로가기 버튼을 누른 시각을 저장하는 속성
    var initTime = 0L
    // 멈춘 시간을 저장하는 속성
    var pauseTime = 0L

    var saveTime = 0L

    private var runningName : String = "walk"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRunningBinding.inflate(layoutInflater)
        setContentView(binding.root)


        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("uay6b7lbet")

        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)

        binding.runningName.text = "걷기"
        binding.runningName.setOnClickListener {
            val items = arrayOf("걷기", "빠르게 걷기", "뛰기", "계단")
            val itemsEn = arrayOf("walk", "fast_walk", "running", "stairs")
            var selectedItem: String? = null
            val builder = AlertDialog.Builder(this)
                .setTitle("운동 선택")
                .setSingleChoiceItems(items, -1) { dialog, which ->
                    selectedItem = items[which]
                    runningName = itemsEn[which]
                }
                .setPositiveButton("OK") { dialog, which ->
                    binding.runningName.text = selectedItem.toString()

                }
                .show()

        }

        binding.stop.isEnabled = false
        binding.start.isVisible = true
        binding.stop.isVisible = true
        binding.reset.isVisible = false
        binding.save.isVisible = false


        binding.start.setOnClickListener {
            binding.chronometer.base = SystemClock.elapsedRealtime() + pauseTime    // 이전 시간부터 스타트
            binding.chronometer.start()
            binding.start.isEnabled = false
            binding.stop.isEnabled = true
            binding.reset.isEnabled = true
            binding.save.isEnabled = false

            binding.reset.isVisible = false
            binding.start.isVisible = false
            binding.stop.isVisible = true
            binding.save.isVisible = false
        }

        binding.stop.setOnClickListener {
            pauseTime = binding.chronometer.base - SystemClock.elapsedRealtime()
            binding.chronometer.stop()
            binding.start.isEnabled = true
            binding.stop.isEnabled = false
            binding.reset.isEnabled = true
            binding.save.isEnabled = true

            binding.start.isVisible = true
            binding.stop.isVisible = false
            binding.reset.isVisible = true
            binding.save.isVisible = true
        }

        binding.reset.setOnClickListener {
            pauseTime = 0L
            binding.chronometer.base = SystemClock.elapsedRealtime()    // ??
            binding.chronometer.stop()

            binding.start.isEnabled = true
            binding.stop.isEnabled = false
            binding.reset.isEnabled = false
            binding.save.isEnabled = false

            binding.start.isVisible = true
            binding.stop.isVisible = true
            binding.reset.isVisible = false
            binding.save.isVisible = false
        }

        binding.save.setOnClickListener {

            saveTime = -pauseTime
            saveTime /= 1000
            Log.d("savetime", saveTime.toString())
            finish()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        val uiSettings = naverMap.uiSettings

        uiSettings.isLocationButtonEnabled = true
        val locationOverlay = naverMap.locationOverlay
        locationOverlay.isVisible = true

        naverMap.addOnLocationChangeListener { location ->
            locationOverlay.position = LatLng(location.latitude, location.longitude)
        }

    }


}