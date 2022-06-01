package com.example.boogi_trainer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.boogi_trainer.databinding.ActivityRunningBinding
import com.example.boogi_trainer.repository.APIManager
import com.example.boogi_trainer.repository.CardioExerciseType
import com.example.boogi_trainer.tflite.Classifier
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class RunningActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var naverMap: NaverMap

    private lateinit var binding: ActivityRunningBinding

    // 뒤로가기 버튼을 누른 시각을 저장하는 속성
    var initTime = 0L
    // 멈춘 시간을 저장하는 속성
    var pauseTime = 0L

    var saveTime = 0L

    private var runningName : String = "runningMachine"

    var speed = 1F
    var speedNum = 6
    var distance = 0F
    var kcal = 0F

    private lateinit var timer : Timer

    var preLocationLet : Double = 0.0
    var preLocationLong : Double = 0.0
    var coords : MutableList<LatLng> = ArrayList()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        const val MY_PERMISSION_ACCESS_ALL = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRunningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("uay6b7lbet")

        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        binding.speed.text = speedNum.toString()
        binding.runningName.text = "런닝머신"
        runningName = "runningMachine"

        if(binding.runningName.text == "조깅") {
            binding.mapLayout.isVisible = true
            binding.runningMachineLayout.isVisible = false
            val fm = supportFragmentManager
            val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
                ?: MapFragment.newInstance().also {
                    fm.beginTransaction().add(R.id.map_fragment, it).commit()
                }

            mapFragment.getMapAsync(this)

            binding.marking.isVisible = false

        }
        else{
            binding.mapLayout.isVisible = false
            binding.runningMachineLayout.isVisible = true
            binding.marking.isVisible = true
        }
        binding.runningName.setOnClickListener {
            val items = arrayOf("런닝머신", "조깅")
            val itemsEn = arrayOf("runningMachine", "running")
            var selectedItem: String? = null
            val builder = AlertDialog.Builder(this)
                .setTitle("운동 선택")
                .setSingleChoiceItems(items, -1) { dialog, which ->
                    selectedItem = items[which]
                    runningName = itemsEn[which]
                }
                .setPositiveButton("OK") { dialog, which ->
                    binding.runningName.text = selectedItem.toString()

                    if(selectedItem.toString() == "조깅") {
                        binding.marking.isVisible = false
                        binding.mapLayout.isVisible = true
                        binding.runningMachineLayout.isVisible = false
                        val fm = supportFragmentManager
                        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
                            ?: MapFragment.newInstance().also {
                                fm.beginTransaction().add(R.id.map_fragment, it).commit()
                            }

                        mapFragment.getMapAsync(this)
                    }
                    else{
                        binding.marking.isVisible = true
                        binding.mapLayout.isVisible = false
                        binding.runningMachineLayout.isVisible = true
                    }

                }
                .show()

        }

        binding.speedUp.setOnClickListener {
            if(speedNum < 15) {
                speed += 0.2F
                speedNum += 1
                binding.speed.text = (speedNum).toString()
            }
        }
        binding.speedDown.setOnClickListener {
            if(speedNum > 0) {
                speed -= 0.2F
                speedNum -= 1
                binding.speed.text = (speedNum).toString()
            }
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
            binding.marking.isVisible = false

            if(binding.runningName.text == "런닝머신") {
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        kcal += 0.8.toFloat() * speed
                        runOnUiThread {
                            binding.kcalNum.text = String.format("%.1f", kcal)
                        }
                    }

                }, 1000, 5000)
            }
            else{
                requestLocationUpdate()
            }
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

            if(binding.runningName.text == "런닝머신") {
                timer.cancel()
            }else{
                removeLocationUpdate()
            }
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
            if(runningName == "runningMachine"){
                APIManager.postCardioExercise(CardioExerciseType.RUNNING_MACHINE, saveTime.toInt())
            }
            else{
                APIManager.postCardioExercise(CardioExerciseType.JOGGING, saveTime.toInt())
            }


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
        if (requestCode === MY_PERMISSION_ACCESS_ALL) {
            if (grantResults.isNotEmpty()) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) return
                }

                //requestLocationUpdate()
            }
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

    override fun finish() {
        super.finish()
        //removeLocationUpdate()
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate(){
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 180000

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper());
    }

    private fun removeLocationUpdate(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.equals(null)) {
                return
            }

            for (location in locationResult.locations) {
                if (location != null) {
                    //val latitude = location.latitude
                    //val longitude = location.longitude

                    val dist = getDistance(location.latitude, location.longitude, preLocationLet, preLocationLong)

                    if(dist > 20 && coords.size != 0){
                        coords.add(LatLng(location.latitude, location.longitude))
                        val polyline = PolylineOverlay()
                        polyline.coords = coords
                        polyline.map = naverMap

                        distance += dist
                        preLocationLet = location.latitude
                        preLocationLong = location.longitude
                    }
                    else if(coords.size == 0){
                        coords.add(LatLng(location.latitude, location.longitude))
                        preLocationLet = location.latitude
                        preLocationLong = location.longitude
                    }

                    runOnUiThread {
                        binding.meter.text = String.format("%.1f", distance)
                    }
                    Log.d("Test", "dis = ${distance}, GPS Location changed, Latitude: $location.latitude, Longitude: $location.longitude")
                }
            }
        }
    }

    // 좌표로 거리구하기
    fun getDistance( lat1: Double, lng1:Double, lat2:Double, lng2:Double) : Float{

        val myLoc = Location(LocationManager.NETWORK_PROVIDER)
        val targetLoc = Location(LocationManager.NETWORK_PROVIDER)
        myLoc.latitude= lat1
        myLoc.longitude = lng1

        targetLoc.latitude= lat2
        targetLoc.longitude = lng2

        return myLoc.distanceTo(targetLoc)
    }

}