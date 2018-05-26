package me.jludden.landmarkremark

import android.app.DialogFragment
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.content.Context
import android.location.Location
import android.widget.EditText
import android.widget.TextView


class CreateLandmarkDialog : DialogFragment() {

    // Use this instance of the interface to deliver action events
    private lateinit var listener: DialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement DialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        //get passed in args
        val location = arguments.getParcelable<Location>(LOCATION_KEY)


        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)


        val inflater = activity.layoutInflater.inflate(R.layout.dialog_create_landmark, null)
                .apply {
                    findViewById<TextView>(R.id.dialog_latlng).text = location.toDisplayString()
                }

        builder.setView(inflater)
                .setNegativeButton(R.string.cancel, { dialog, id -> })
                .setPositiveButton(R.string.create, { dialog, id ->
                    val message = inflater.findViewById<EditText>(R.id.dialog_message).text.toString()
                    listener.onDialogAccept(message, location)
                })
        // Create the AlertDialog object and return it
        return builder.create()
    }

    interface DialogListener {
        fun onDialogAccept(message: String, location: Location)
    }

    companion object {
        const val LOCATION_KEY = "LatLong"

        fun newInstance(loc: Location) : CreateLandmarkDialog {
            val dialog = CreateLandmarkDialog()
            val args = Bundle()
            args.putParcelable(LOCATION_KEY, loc)
            dialog.arguments = args

            return dialog
        }
    }
}





