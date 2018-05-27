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
import kotlinx.android.synthetic.main.activity_search.*
import me.jludden.landmarkremark.LandmarksPresenter.Companion.LANDMARKS_CHILD

/**
 * Activity that provides the ability to search for a note based on contained text or username
 * there isn't much business logic to implement here so I did not implement MVC architecture
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

                //Update the Adapter
                landmarksAdapter.landmarksList = allLandmarks.filter{
                    it.user.toLowerCase().contains(query) ||
                    it.remark.toLowerCase().contains(query)
                }

                //Show message if no results are found
                if(!landmarksAdapter.landmarksList.isEmpty()) {
                    no_results.visibility = GONE
                }
                else {
                    no_results.apply {
                        text = getString(R.string.no_search_results, query)
                        visibility = VISIBLE
                    }
                }
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