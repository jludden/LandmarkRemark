package me.jludden.landmarkremark

import android.app.AlertDialog
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
import android.widget.EditText
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
    private var username = "Anonymous"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmarks)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //set up firebase database
        firebaseDB = FirebaseDatabase.getInstance().reference

        //set up the location permissions. If they don't accept, they will still be able to see other user's landmarks
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(!checkLocationPermission()) {    //Request current location permission
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

        setupLandmarksListener() //add landmarks to map and continue to listen for updates

        //special on-click for my location pin
        map.setOnMyLocationClickListener { createNewLandmarkDialog() }

        //show a snackbar message when the find my location button is clicked
        map.setOnMyLocationButtonClickListener {
            Snackbar.make(findViewById<View>(R.id.map), getString(R.string.create_new_landmark_instruct), Snackbar.LENGTH_LONG)
                    .also { it.setAction("CREATE", {
                        createNewLandmarkDialog()
                    }) }
                    .show()
            false
        }

        //initially center camera on last known location
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if(location != null)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location.latlng(), 9f))
                }
    }

    //Create a dialog for the user to add a new landmark remark
    private fun createNewLandmarkDialog() {
        if (!checkLocationPermission()) return

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    Log.d(LandmarksActivity.TAG, "lastLocation found createNewLandmarkDialog")
                    if(location == null) Snackbar.make(findViewById<View>(R.id.map), getString(R.string.no_landmark_location), Snackbar.LENGTH_LONG).show()
                    else CreateLandmarkDialog.newInstance(location).show(fragmentManager, "CreateLandmark")
                }
    }

    //Called when the landmark creation dialog is accepted
    override fun onDialogAccept(message: String, location: Location) {
        createLandmark(message, location, username)
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

    //Add landmark markers to the map, and continually listen to updates from Firebase
    private fun setupLandmarksListener() {
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
            map.addMarker(MarkerOptions()
                    .position(it.location.toLatLng())
                    .title("${it.user}: ${it.location}")
                    .snippet(it.remark)
            ).apply { this.tag = it.id }
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
                showChangeUsernameDialog()
            }
        }
        return true
    }

    fun showChangeUsernameDialog() {
        val newUsernameField = EditText(this)
                .apply {
                    setText(username)
                    setSingleLine(true) }
        AlertDialog.Builder(this@LandmarksActivity)
                .setView(newUsernameField)
                .setMessage(R.string.change_username)
                .setTitle(R.string.app_name)
                .setPositiveButton(R.string.ok, { _, _ -> //user accepted
                    username = newUsernameField.text.toString() })
                .setNegativeButton(R.string.cancel) { _, _ ->  } //user cancelled
                .create()
                .show()
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

