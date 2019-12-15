package com.project.himanshu.sltmuve.ui.activitys.main

import android.Manifest.permission.*
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.project.himanshu.sltmuve.BuildConfig
import com.project.himanshu.sltmuve.R
import com.project.himanshu.sltmuve.data.dataModel.UserLocation
import com.project.himanshu.sltmuve.data.dataModel.VehicleType
import com.project.himanshu.sltmuve.databinding.ActivityMainBinding
import com.project.himanshu.sltmuve.services.background.LocationUpdatesService
import com.project.himanshu.sltmuve.ui.adaptars.VehicleTypeAdaptar
import com.project.himanshu.sltmuve.viewmodels.LocationViewmodel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var myReceiver: MyReceiver

    private lateinit var mService: LocationUpdatesService
    private var mBound = false
    private lateinit var model: LocationViewmodel
    lateinit var binding: ActivityMainBinding

     var setFlagToCurrentLocation= false

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_CHECK_SETTINGS = 35
    }

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationUpdatesService.LocalBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trasperat()
        myReceiver = MyReceiver()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        model = ViewModelProvider(this).get(LocationViewmodel::class.java)
        binding.viemodel = model

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        if (!checkPermissions()) {
            requestPermissions()
        }


        val adapter = VehicleTypeAdaptar()
        binding.root.recyclerview_vehicletype.adapter = adapter
        subscribeHeadlineVehicleTypeToUi(adapter)

        model.liveLocation.observe(this) { news ->
            Log.i(TAG, "liveLocation observe$news")
        }
    }

    override fun onStart() {
        super.onStart()

        relativelayout_timeslote!!.setOnClickListener { mService.removeLocationUpdates() }
        bindService(
            Intent(this, LocationUpdatesService::class.java),
            mServiceConnection,
            Context.BIND_AUTO_CREATE
        )

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            checkCurrentLocationSetting()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            myReceiver, IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver)
        super.onPause()
    }

    override fun onStop() {
        if (mBound) {
            unbindService(mServiceConnection)
            mBound = false
        }
        super.onStop()
    }

    private fun subscribeHeadlineVehicleTypeToUi(adapter: VehicleTypeAdaptar) {
        model.vehicleTypeList.observe(this) { typs ->
            adapter.submitList(typs)
            adapter.setOnItemClickListener(object : VehicleTypeAdaptar.ClickListener {
                override fun onClick(vehicletype: VehicleType, aView: View) {


                }
            })
        }


    }


    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_BACKGROUND_LOCATION
                ) == PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE
                )
            }

        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.permission_denied_explanation,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PERMISSION_GRANTED -> {
                    mService.requestLocationUpdates()
                }
                else -> {
                    Snackbar.make(
                        findViewById(R.id.main_activity),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.settings) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }.show()
                }
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            when {
                grantResults.isEmpty() -> {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.permission_denied_explanation,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PERMISSION_GRANTED -> {
                    mService.requestLocationUpdates()
                }
                else -> {
                    Snackbar.make(
                        findViewById(R.id.main_activity),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.settings) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }.show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.customize_map))
        updateLocationUI()

    }


    fun updateLocationUI() {
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (e:SecurityException ) {

        }

    }

    private fun updateLocationUI(location: Location) {
        val currentLocation = LatLng(location.latitude, location.longitude)
        if(!setFlagToCurrentLocation){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            mMap.isMyLocationEnabled = false
            mMap.uiSettings.isMyLocationButtonEnabled = false
            mMap.addMarker(MarkerOptions().position(currentLocation).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_24)))
            setFlagToCurrentLocation = true
        }else{


        }



    }


    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            updateLocationUI(location)
            if (location != null) {
                model.setLocationUpdate(UserLocation(location.latitude, location.longitude))
            }
        }
    }

    fun checkCurrentLocationSetting() {
        val builder = LocationSettingsRequest.Builder()
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            mService.requestLocationUpdates()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: SendIntentException) {

                }
            }
        }

    }


    private fun trasperat() {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }
    }


    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
}
