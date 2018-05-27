package me.jludden.landmarkremark

import android.location.Location

interface LandmarksContract {

    interface View : BaseView<Presenter> {

        fun showLandmarks(landmarks: List<Landmark>)

        fun showCreateLandmarkDialog()

        fun showChangeUsernameDialog(oldName: String)

        fun showSearchWidget()
    }


    interface Presenter : BasePresenter {

        fun createNewLandmark(message: String, location: Location)

        fun onMyLocationClick()

        fun onSearchClick()

        fun onUsernameChangeClick()

        fun updateUsername(username: String)

    }
}

interface BaseView<T> {

    var presenter: T
}

interface BasePresenter {

    fun start()

}