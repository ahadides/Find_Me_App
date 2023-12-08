package com.example.findmeapp


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdate

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var client: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (hasLocationPermission()) {
            trackLocation()
        }
    }

    private fun trackLocation() {

        // Create location request
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .build()

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    updateMap(location)
                }
            }
        }

        client = LocationServices.getFusedLocationProviderClient(this)
    }



        private fun updateMap(location: Location) {

            // Get current location
            val currentLatLng = LatLng(location.latitude,
                location.longitude)

            // Remove previous marker
            googleMap.clear()

            // Place a marker at the current location
            val markerOptions = MarkerOptions()
                .title("Here you are!")
                .position(currentLatLng)
            googleMap.addMarker(markerOptions)

            // Move and zoom to current location at the street level
            val update: CameraUpdate =
                CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
            googleMap.animateCamera(update)
            // Zoom to previously saved level
            val update2: CameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel)

            googleMap.animateCamera(update2)

        }

    private var zoomLevel = 15f

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Save zoom level
        googleMap.setOnCameraMoveListener {
            zoomLevel = googleMap.cameraPosition.zoom
        }

        // Handle marker click
        googleMap.setOnMarkerClickListener { marker: Marker ->
            val lineBreak = System.getProperty("line.separator")
            Toast.makeText(this,
                "Lat: ${marker.position.latitude} $lineBreak Long: ${marker.position.longitude}",
                Toast.LENGTH_LONG).show()

            return@setOnMarkerClickListener false
        }
    }


    override fun onPause() {
        super.onPause()
        client?.removeLocationUpdates(locationCallback!!)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        if (hasLocationPermission()) {
            client?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, Looper.getMainLooper())
        }
    }

    private fun hasLocationPermission(): Boolean {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            trackLocation()
        }
    }
}