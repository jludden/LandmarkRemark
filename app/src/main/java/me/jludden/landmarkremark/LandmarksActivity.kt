package me.jludden.landmarkremark

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class LandmarksActivity : AppCompatActivity(), OnMapReadyCallback, CreateLandmarkDialog.DialogListener {


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
//        testAdd(firebaseDB) //todo del

        //set up the location permissions
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(!checkLocationPermission()) {    //Request current location permission
            Log.d(TAG, "Requesting Map Location Permissions")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }
    }

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

        setupLandmarks()

        //special on-click for my location pin
        map.setOnMyLocationClickListener {
            //presenter.showAddNewLandmark(it)
            //todo
            Log.d(LandmarksActivity.TAG, "onmap location clicked")
            createNewLandmarkDialog()
        }

        //show remark when the find my location button is clicked
        map.setOnMyLocationButtonClickListener {
            Log.d(LandmarksActivity.TAG, "onmap location BUTTON clicked")
            Snackbar.make(findViewById<View>(R.id.map), "Click the blue location dot to save a remark", Snackbar.LENGTH_LONG)
                    .also { it.setAction("CREATE", {
                        createNewLandmarkDialog()
                    }) }
                    .show()
            false
        }

        fusedLocationClient.lastLocation //initially center camera on last known location
                .addOnSuccessListener { location ->
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(location.latlng(), 9f))
                }
    }

    private fun createNewLandmarkDialog() {
        Log.d(LandmarksActivity.TAG, "createNewLandmarkDialog createNewLandmarkDialog createNewLandmarkDialog")


        checkLocationPermission() //todo handle the no

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    Log.d(LandmarksActivity.TAG, "lastLocation found createNewLandmarkDialog")
                    val dialog = CreateLandmarkDialog.newInstance(location)
                    dialog.show(fragmentManager, "CreateLandmark")
                }


//        val dialog = CreateLandmarkDialog.newInstance(map)
//        dialog.show(fragmentManager, "CreateLandmark") //todo not support fm?



//        CreateLandmarkDialog().show(fragmentManager, "CreateLandmark")
    }

    override fun onDialogAccept(message: String, location: Location) {
        val user = "123" //todo get user
        createLandmark(message, location, user)
    }

    private fun createLandmark(message: String, location: Location, user: String) {
        val loc  = location.latlng().toPublicLatLng()
        Landmark(message, loc, user)
                .apply {
                    val key = firebaseDB.child(LANDMARKS_CHILD).push().key
                    this.id = key
                    firebaseDB.child(LANDMARKS_CHILD).child(key).setValue(this)
                }
    }

    //todo probably want to move to ChildEventListener 
    fun setupLandmarks() {
        firebaseDB.child(LANDMARKS_CHILD).addValueEventListener(object : ValueEventListener {
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
        })
    }

    fun addLandmarksToMap() {
        map.clear()

        landmarks.forEach {
            map.addMarker(MarkerOptions().position(it.location.toLatLng()).title("${it.user}: ${it.remark}")).apply { this.tag = it.id }
        }
    }

    fun Location.latlng(): LatLng = LatLng(this.latitude, this.longitude)

    private fun checkLocationPermission() =
            ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onCreateOptionsMenu(menu: Menu?): Boolean =
        menuInflater.inflate(R.menu.main_toolbar, menu).run { true }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_search -> {
                Log.e(TAG, "Starting search activity")
                onSearchRequested()
            }
            R.id.settings -> {
                Log.d(TAG, "Other settings selected")
            }
        }
        return true
    }


    //todo del - add some fake data
    fun testAdd(firebaseDatabase: DatabaseReference) {

        val sydney = LatLng(-34.0, 151.0).toPublicLatLng()
        val nyc = LatLng(40.75, -73.98).toPublicLatLng()
        val shanghai = LatLng(31.23, 121.47).toPublicLatLng()

        val user1 = "testUser1"
        val user2 = "testUser2"

        val landmarks: List<Landmark> = mutableListOf(
                Landmark("opera house", sydney, user1),
                Landmark("times square", nyc, user1),
                Landmark("oriental pearl", shanghai, user2)
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

//Util functions todo
fun Location.toDisplayString(): String? {
    return "(${"%.2f".format(latitude)}, ${"%.2f".format(longitude)})"
}
