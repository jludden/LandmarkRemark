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

    private lateinit var listener: DialogListener

    //define interface for receiving events from this dialog
    interface DialogListener {
        fun onCreateLandmarkDialogAccepted(message: String, location: Location)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement DialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val location = arguments.getParcelable<Location>(LOCATION_KEY) //get passed in args

        val inflater = activity.layoutInflater.inflate(R.layout.dialog_create_landmark, null)
                .apply {
                    findViewById<TextView>(R.id.dialog_latlng).text = location.toDisplayString()
                }

        return AlertDialog.Builder(activity)
                .setView(inflater)
                .setNegativeButton(R.string.cancel, { dialog, id -> })
                .setPositiveButton(R.string.create, { dialog, id ->
                    val message = inflater.findViewById<EditText>(R.id.dialog_message).text.toString()
                    listener.onCreateLandmarkDialogAccepted(message, location)
                })
                .create()
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





