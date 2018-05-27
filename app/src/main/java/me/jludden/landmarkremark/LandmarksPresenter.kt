package me.jludden.landmarkremark

import android.location.Location
import android.util.Log
import com.google.firebase.database.*

class LandmarksPresenter(val view: LandmarksContract.View) : LandmarksContract.Presenter {

    private lateinit var firebaseDB: DatabaseReference
    private val landmarks = mutableListOf<Landmark>()
    private var username = "Anonymous"

    override fun start() {
        //set up firebase database
        firebaseDB = FirebaseDatabase.getInstance().reference
        setupLandmarksListener()
    }

    //Add landmark markers to the view, and continually listen to updates from Firebase
    private fun setupLandmarksListener() {
        firebaseDB.child(LANDMARKS_CHILD)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        landmarks.clear()
                        snapshot.children.mapNotNullTo(landmarks) {
                            it.getValue<Landmark>(Landmark::class.java)
                        }
                        view.showLandmarks(landmarks)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(LandmarksActivity.TAG, "Firebase ValueEventListener.onCancelled() ${error.toException()}")
                    }
                })
    }

    //Create a new landmark and add it to firebase
    override fun createNewLandmark(message: String, location: Location) {
        Landmark(message, location.toPublicLatLng(), username)
                .apply {
                    val key = firebaseDB.child(LANDMARKS_CHILD).push().key
                    this.id = key
                    firebaseDB.child(LANDMARKS_CHILD).child(key).setValue(this)
                }
    }

    //Launch the Create Landmark Dialog
    override fun onMyLocationClick() = view.showCreateLandmarkDialog()

    //Activate the Search Widget
    override fun onSearchClick() = view.showSearchWidget()

    //Launch the Change Username Dialog
    override fun onUsernameChangeClick() = view.showChangeUsernameDialog(username)

    //Update the Username
    override fun updateUsername(username: String) {
        if(username.isNotBlank()) this.username = username
    }

    companion object {
        const val LANDMARKS_CHILD = "landmarks" //all our landmarks are stored under this firebase key
    }
}