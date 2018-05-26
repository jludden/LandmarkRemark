package me.jludden.landmarkremark

import com.google.android.gms.maps.model.LatLng

data class Landmark(
        val remark: String = "",
        val location: PublicLatLng = PublicLatLng(),
        val user: String = "",
        var id: String = ""
)

//my custom implementation to provide a public empty constructor (a firebase requirement)
data class PublicLatLng(
        val lat: Double = 0.0,
        val lng: Double = 0.0
){
   fun toLatLng() = LatLng(lat, lng)
}

//extension function to transform google maps LatLng to my implementation
fun LatLng.toPublicLatLng(): PublicLatLng = PublicLatLng(this.latitude, this.longitude)