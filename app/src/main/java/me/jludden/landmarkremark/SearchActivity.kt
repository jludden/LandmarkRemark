package me.jludden.landmarkremark

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.search_item.view.*
import android.app.SearchManager
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_search.*
import me.jludden.landmarkremark.LandmarksActivity.Companion.LANDMARKS_CHILD

/**
 * ability to search for a note based on contained text or username
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var firebaseDB: DatabaseReference
    private val landmarksAdapter = SearchResultsAdapter(ArrayList(0))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        firebaseDB = FirebaseDatabase.getInstance().reference

        findViewById<RecyclerView>(R.id.landmarks_container)
                .apply {
                    layoutManager = android.support.v7.widget.LinearLayoutManager(context)
                    adapter = landmarksAdapter
                }

        if (Intent.ACTION_SEARCH == intent.action) { //get the passed in search query
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.e(TAG, "do my search: $query")
            doSearch(query.toLowerCase())
        }
    }

    //get data from firebase, filter it using the query string, then update the adapter
    private fun doSearch(query: String) {
        firebaseDB.child(LANDMARKS_CHILD).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allLandmarks = mutableListOf<Landmark>()
                snapshot.children.mapNotNullTo(allLandmarks) {
                    it.getValue<Landmark>(Landmark::class.java)
                }

                landmarksAdapter.landmarksList = allLandmarks.filter{
                    it.user.toLowerCase().contains(query) ||
                    it.remark.toLowerCase().contains(query)
                }.apply {
                    no_results.let { //show message if list is empty
                        it.text = getString(R.string.no_search_results, query)
                        it.visibility = if(this.isEmpty()) VISIBLE else GONE
                    } }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase ValueEventListener.onCancelled() ${error.toException()}")
            }
        })
    }

    //adapter to hold the search results
    class SearchResultsAdapter(landmarks: List<Landmark>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var landmarksList: List<Landmark> = landmarks
            set(landmarks) {
                field = landmarks
                notifyDataSetChanged()
            }

        fun ViewGroup.inflate(layoutRes: Int) : View = LayoutInflater.from(context).inflate(layoutRes, this, false)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
                = SearchResultViewHolder(parent.inflate(R.layout.search_item))

        override fun getItemCount() = landmarksList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
                (holder as SearchResultViewHolder).bind(landmarksList[position])
    }

    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(landmark: Landmark) = with(itemView) {
            landmark_message.text = landmark.remark
            landmark_user.text = landmark.user
            landmark_location.text = landmark.location.toString()
        }
    }

    companion object {
        const val TAG = "SearchActivity"
    }

}