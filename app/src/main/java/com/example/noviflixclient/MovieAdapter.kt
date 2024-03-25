package com.example.noviflixclient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class MovieAdapter(var activity: MainActivity, private var movieList: List<Movie>) :
    RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textView: TextView
        val moreButton: Button
        val updateButton: Button
        val deleteButton: Button
        val movieEntryView: LinearLayout
        val cardView: CardView

        init {
            textView = view.findViewById(R.id.textView)
            moreButton = view.findViewById(R.id.moreButton)
            updateButton = view.findViewById(R.id.updateButton)
            deleteButton = view.findViewById(R.id.deleteButton)
            cardView = view.findViewById(R.id.cardView)
            movieEntryView = view.findViewById(R.id.movieEntryView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    fun loadMovies(){
        this.movieList = try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    get("http://10.0.2.2:8085/api/v1/movies", null)
                }
            }
        }catch (e: Exception){
            println("Error on loadMovies():"+e.printStackTrace())
            listOf<Movie>()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text =
            "Movie: " + movieList[position].title + "(#${movieList[position].id})"
        holder.moreButton.text = "More"
        holder.updateButton.text = "Update movie"
        holder.deleteButton.text = "Delete movie"

        holder.moreButton.setOnClickListener{
            if(holder.updateButton.visibility == View.VISIBLE
                && holder.deleteButton.visibility == View.VISIBLE){
                holder.updateButton.visibility = View.INVISIBLE
                holder.deleteButton.visibility = View.INVISIBLE

            } else{
                holder.updateButton.visibility = View.VISIBLE
                holder.deleteButton.visibility = View.VISIBLE
            }
        }

        holder.updateButton
            .setOnClickListener( object : View.OnClickListener{
                override fun onClick(view: View?) {
                    if (view != null) {
                        val updatedMovie: Movie = try {
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    put("http://10.0.2.2:8085/api/v1/movies/${movieList[holder.absoluteAdapterPosition].id}",
                                        movieList[holder.absoluteAdapterPosition].id)
                                }
                            }
                        }catch (e: Exception){
                            println("Error on updateButton's click event:"+e.printStackTrace())
                            Movie(-1,"","","")
                        }
                        if(updatedMovie.id > -1){
                            println("updated movie! id: "+movieList[holder.absoluteAdapterPosition].id)
                            showUpdateAlert(updatedMovie, activity.supportFragmentManager)
                            loadMovies()
                            notifyDataSetChanged()
                        }
                    }
                }
            })

        holder.deleteButton
            .setOnClickListener( object : View.OnClickListener{
                override fun onClick(view: View?) {
                    if (view != null) {
                        try {
                            val movieToDelete = movieList[holder.absoluteAdapterPosition].id
                            runBlocking {
                                withContext(Dispatchers.IO) {
                                    delete("http://10.0.2.2:8085/api/v1/movies/${movieToDelete}")
                                }
                            }
                            println("removed movie!")
                            // query the adapter to remove the view of the respective movie id
                        }catch (e: Exception){
                            println("Error on deleteButton's click event: "+e.printStackTrace())
                        }
                        showDeleteAlert(activity.supportFragmentManager)
                        loadMovies()
                        notifyDataSetChanged()
                    }
                }
            })
    }

    override fun getItemCount(): Int {
        return movieList.size
    }
}