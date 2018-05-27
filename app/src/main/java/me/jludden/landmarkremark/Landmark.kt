package me.jludden.landmarkremark

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Define the Landmark model that we will use with Firebase
 *  as well as some helper stuff regarding location & latitude/longitudes
 */
data class Landmark(
        val remark: String = "",
        val location: PublicLatLng = PublicLatLng(),
        val user: String = "",
        var id: String = ""
)

/** A custom implementation of LatLng
 *  that provides a public empty constructor (a firebase requirement)
 */
data class PublicLatLng(
        val lat: Double = 0.0,
        val lng: Double = 0.0 ){

    fun toLatLng() = LatLng(lat, lng)

    override fun toString(): String {
        return formatLatLng(lat, lng)
    }
}

/**
 * Global utility and extension functions
 */
//extension function to transform google maps LatLng to my implementation
fun LatLng.toPublicLatLng(): PublicLatLng = PublicLatLng(this.latitude, this.longitude)

//get a nicely formatted location to string
fun Location.toDisplayString(): String? = formatLatLng(latitude, longitude)

//format a latitude / longitude pair for display
fun formatLatLng(latitude: Double, longitude: Double): String {
    return "(${"%.2f".format(latitude)}, ${"%.2f".format(longitude)})"
}