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

class MovieAdapter(private var movieList: List<Movie>) :
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
        movieList = try {
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
        holder.textView.text = "Movie: " + movieList[position].title
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

        holder.updateButton.setOnClickListener{
            try {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        put("http://10.0.2.2:8085/api/v1/movies/${movieList[position].id}",
                            movieList[position].id)
                    }
                }
                loadMovies()
            }catch (e: Exception){
                println("Error on updateButton's click event:"+e.printStackTrace())
            }
        }

        holder.deleteButton.setOnClickListener{
            try {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        delete("http://10.0.2.2:8085/api/v1/movies/${movieList[position].id}")
                    }
                }
                holder.movieEntryView.removeView(holder.cardView)
                loadMovies()
            }catch (e: Exception){
                println("Error on deleteButton's click event: "+e.printStackTrace())
            }
        }
    }

    override fun getItemCount(): Int {
        return movieList.size
    }
}