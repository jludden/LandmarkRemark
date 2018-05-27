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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions

class LandmarksActivity: AppCompatActivity(),
        OnMapReadyCallback,
        CreateLandmarkDialog.DialogListener,
        LandmarksContract.View {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override lateinit var presenter: LandmarksContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmarks)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //set up the location permissions. If they don't accept, they will still be able to see other user's landmarks
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(!checkLocationPermission()) {    //Request current location permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }

        //set up the presenter to control the logic
        presenter = LandmarksPresenter(this)
    }

    //todo del
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

        presenter.start() //start the presenter - it will populate the map with landmarks

        //special on-click for my location pin
        map.setOnMyLocationClickListener { presenter.onMyLocationClick() }

        //show a snackbar message when the find my location button is clicked
        map.setOnMyLocationButtonClickListener {
            Snackbar.make(findViewById<View>(R.id.map), getString(R.string.create_new_landmark_instruct), Snackbar.LENGTH_LONG)
                    .setAction("CREATE", { presenter.onMyLocationClick() })
                    .show()
            false
        }

        //initially center camera on last known location todo del?
/*        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if(location != null)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location.toLatLng(), 9f))
                }*/
    }

    //Create a dialog for the user to add a new landmark remark
    override fun showCreateLandmarkDialog() {
        if (!checkLocationPermission()) return

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if(location == null) Snackbar.make(findViewById<View>(R.id.map), getString(R.string.no_landmark_location), Snackbar.LENGTH_LONG).show()
                    else CreateLandmarkDialog.newInstance(location).show(fragmentManager, "CreateLandmark")
                }
    }

    //Called when the landmark creation dialog is accepted
    override fun onCreateLandmarkDialogAccepted(message: String, location: Location)
            = presenter.createNewLandmark(message, location)

    //Add landmarks to the map
    override fun showLandmarks(landmarks: List<Landmark>) {
        map.clear()

        landmarks.forEach {
            map.addMarker(MarkerOptions()
                    .position(it.location.toLatLng())
                    .title("${it.user}: ${it.location}")
                    .snippet(it.remark)
            ).apply { this.tag = it.id }
        }
    }

    //Create the toolbar menu (search, change username, etc.)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean =
        menuInflater.inflate(R.menu.main_toolbar, menu).run { true }

    //Called when a toolbar menu item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_search -> {
                presenter.onSearchClick()
            }
            R.id.action_change_username -> {
                presenter.onUsernameChangeClick()
            }
        }
        return true
    }

    //Show the Search Widget
    override fun showSearchWidget() {
        onSearchRequested()
    }

    //Show the Change Username Dialog
    override fun showChangeUsernameDialog(oldName: String) {
        val newUsernameField = EditText(this)
                .apply {
                    setText(oldName)
                    setSingleLine(true) }
        AlertDialog.Builder(this@LandmarksActivity)
                .setView(newUsernameField)
                .setMessage(R.string.change_username)
                .setTitle(R.string.app_name)
                .setPositiveButton(R.string.ok, { _, _ -> // user accepted
                    presenter.updateUsername(newUsernameField.text.toString())})
                .setNegativeButton(R.string.cancel) { _, _ ->  } // user cancelled
                .create()
                .show()
    }

    //Check the ACCESS_FINE_LOCATION permission
    private fun checkLocationPermission() =
            ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val TAG = "LandmarksActivity"
    }
}

