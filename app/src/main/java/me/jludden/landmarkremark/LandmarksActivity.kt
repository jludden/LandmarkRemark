package me.jludden.landmarkremark

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*

class LandmarksActivity : MapLocationActivity() {

    private lateinit var map: GoogleMap
    private lateinit var firebaseDB: DatabaseReference

    private val landmarks = mutableListOf<Landmark>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_landmarks)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//                .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)


        //set up firebase database
        firebaseDB = FirebaseDatabase.getInstance().reference
        testAdd(firebaseDB)





    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        map = googleMap
//        map.uiSettings.isMapToolbarEnabled = false
        setupLandmarks()


        //set up marker onclick listeners - return false to allow default marker info appear
        map.setOnMarkerClickListener { marker ->
            if(marker.tag == -1){
                Log.d(TAG, "current position marker clicked")
                false
            } else false
        }

    }

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

    companion object {
        const val TAG = "LandmarksActivity"
        const val LANDMARKS_CHILD = "landmarks"
    }
}
