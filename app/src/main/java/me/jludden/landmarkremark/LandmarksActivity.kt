package me.jludden.landmarkremark

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class LandmarksActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    private lateinit var firebaseDB: DatabaseReference
    private val landmarks = mutableListOf<Landmark>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmarks)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //set up firebase database
        firebaseDB = FirebaseDatabase.getInstance().reference
//        testAdd(firebaseDB todo del


        //set up the location permissions
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(!checkLocationPermission()) {    //Request current location permission
            Log.d(TAG, "Requesting Map Location Permissions")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }

    }

    /*//
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CHECK_SETTINGS){
            if(resultCode == Activity.RESULT_OK){
                Log.d(TAG, "onActivityResult Map Location Permissions granted")
            }
        }
        Log.e(TAG, "onActivityResult Map Location Permissions not granted")
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            200 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult granted")
                }
            }
        }
        Log.e(TAG, "onRequestPermissionsResult not granted")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = false

        if(checkLocationPermission()) map.isMyLocationEnabled = true

        map = googleMap
        setupLandmarks()

        //special on-click for my location pin
        if(map.isMyLocationEnabled) {
            map.setOnMyLocationClickListener {
                //presenter.showAddNewLandmark(it)
                //todo
                Log.d(LandmarksActivity.TAG, "onmap location clicked")
            }
        }


        fusedLocationClient.lastLocation //initially center camera on last known location
                .addOnSuccessListener { location ->
                    Log.d(LandmarksActivity.TAG, "lastLocation found!!!!!!!!")
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(location.latlng(), 9f))
                }
    }


    private fun checkLocationPermission() =
            ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    //todo probably want to move to ChildEventListener 
    fun setupLandmarks() {
        val landmarkListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                landmarks.clear()
                snapshot.children.mapNotNullTo(landmarks) {
                    it.getValue<Landmark>(Landmark::class.java)
                }

                addLandmarksToMap()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase ValueEventListener.onCancelled() ${error.toException()}")
            }
        }

        firebaseDB.child(LANDMARKS_CHILD).addValueEventListener(landmarkListener)



        //todo del above
/*

        firebaseDB.child(LANDMARKS_CHILD).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase ChildEventListener.onCancelled() ${error.toException()}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e(TAG, "Firebase ChildEventListener.onChildMoved() $previousChildName")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e(TAG, "Firebase ChildEventListener.onChildChanged() $previousChildName")
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "Firebase ChildEventListener.onChildAdded() $previousChildName")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.e(TAG, "Firebase ChildEventListener.onChildRemoved() ")
            }

        })
*/

    }


    fun addLandmarksToMap() {
        map.clear()

        landmarks.forEach {
            map.addMarker(MarkerOptions().position(it.location.toLatLng()).title("${it.user}: ${it.remark}")).apply { this.tag = it.id }
        }
    }

    fun Location.latlng(): LatLng = LatLng(this.latitude, this.longitude)

    fun testAdd(firebaseDatabase: DatabaseReference) {

        val sydney = LatLng(-34.0, 151.0).toPublicLatLng()
        val nyc = LatLng(40.75, -73.98).toPublicLatLng()
        val shanghai = LatLng(31.23, 121.47).toPublicLatLng()

        val user1 = "testUser1"
        val user2 = "testUser2"

        val landmarks: List<Landmark> = mutableListOf(
                Landmark(sydney, "opera house", user1),
                Landmark(nyc, "times square", user1),
                Landmark(shanghai, "oriental pearl", user2)
        )

        landmarks.forEach {
            val key = firebaseDatabase.child(LANDMARKS_CHILD).push().key
            it.id = key
            firebaseDatabase.child(LANDMARKS_CHILD).child(key).setValue(it)
        }
    }

    companion object {
        const val TAG = "LandmarksActivity"
        const val LANDMARKS_CHILD = "landmarks"
    }
}
