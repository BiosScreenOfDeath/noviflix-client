package com.example.noviflixclient

import android.os.Bundle
import android.os.NetworkOnMainThreadException
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

// add the HTTP calls to some "noviflix" class in the final cleanup

fun randomMovie(): Movie {
    val movieSources = listOf<Movie>(
        Movie(0, "gun","john", "it's like wow"),
        Movie(1, "sky","cena", "don't ask"),
        Movie(2, "night","no", "yes it is"),
        Movie(3, "monster","not you", "just"),
        Movie(4, "4","alan-e", "amazing"),
        Movie(5, "duck","you-da", "literally me"),
        Movie(6, "laser","des-k", "just... no"),
        Movie(7, "the man","co-fi", "hm"),
        Movie(8, "go","c-up", "it's cool"),
    )
    return Movie(
        movieSources.random().id,
        movieSources.random().title+" "+movieSources.random().title,
        movieSources.random().director,
        movieSources.random().plot)
}

suspend fun get(url: String, id: Int?): List<Movie> {

    val fullURL = if(id != null){
        "$url/$id"
    } else {
        url
    }

    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url(fullURL)
            .header("Accept", "application/json")
            .build()

    var getResponse: List<Movie> = listOf<Movie>(
        Movie(-1,"","","")
    )

    withContext(Dispatchers.IO){
        try {
            val response = client.newCall(request).execute()
            val gson = Gson()
            val jsonData = response.body?.string()
            val responseCode = response.code
            if(responseCode == 200){
                println("GET successful: $responseCode")
                getResponse = if( jsonData?.get(0).toString() == "{"){
                    //println(jsonData)
                    listOf(gson.fromJson<Movie?>(jsonData, Movie::class.java))
                }else{
                    gson.fromJson<Array<Movie>?>(jsonData, Array<Movie>::class.java)
                        .toList<Movie>()
                }
            } else {
                println("GET unsuccessful: $responseCode")
            }
        } catch (e: Exception) {
            println("Σ' ΕΠΙΑΣΑ: $e")
        }
    }

    println("Response data: $getResponse")
    return getResponse
}

suspend fun delete(url: String) {
    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url(url)
            .delete()
            .build()

    withContext(Dispatchers.IO){
        try {
            val response: Response = client.newCall(request).execute()
            val responseCode = response.code
            if(responseCode === 204){
                println("Deletion was successful: $responseCode")
            } else {
                println("Houston, deletion was unsuccessful: $responseCode")
            }
        } catch (e: Exception) {
            println("Σ' ΕΠΙΑΣΑ: $e")
        }
    }
}

suspend fun post(url: String): Movie{
    val client = OkHttpClient()

    val gson = Gson()
    val directedMovie = randomMovie()
    val body = gson.toJson(directedMovie, Movie::class.java)
        .toRequestBody("application/json".toMediaTypeOrNull())

    val request =
        Request.Builder()
            .url(url)
            .addHeader("Content-Encoding", "application/json")
            .post(body)
            .build()

    var responseCode = -1
    var postMovie = Movie(-1,"","","")

    withContext(Dispatchers.IO){
        try {
            val response: Response = client.newCall(request).execute()
            responseCode = response.code
            if(responseCode === 201){
                println("Addition was successful: $responseCode")
                val jsonData = response.body?.string()
                postMovie = gson.fromJson<Movie?>(jsonData, Movie::class.java)
            } else {
                println("Houston, addition was unsuccessful: $responseCode")
            }
        } catch (e: Exception) {
            println("Σ' ΕΠΙΑΣΑ: $e")
        }
    }

    return postMovie
}

suspend fun put(url: String, id: Int): Movie{
    val client = OkHttpClient()

    val gson = Gson()
    val directedMovie = randomMovie()
    val body = gson.toJson(directedMovie, Movie::class.java)
        .toRequestBody("application/json".toMediaTypeOrNull())

    val request =
        Request.Builder()
            .url(url)
            .addHeader("Content-Encoding", "application/json")
            .put(body)
            .build()

    var putMovie = Movie(id,"[REDACTED]","[REDACTED]","[REDACTED]")

    withContext(Dispatchers.IO){
        try {
            val response: Response = client.newCall(request).execute()
            val responseCode = response.code
            if(responseCode === 200){
                println("Update was successful: $responseCode")
                val jsonData = response.body?.string()
                putMovie = gson.fromJson<Movie?>(jsonData, Movie::class.java)
            } else {
                println("Houston, update was unsuccessful: $responseCode")
            }
        } catch (e: Exception) {
            println("Σ' ΕΠΙΑΣΑ: $e")
        }
    }

    return putMovie
}

suspend fun whatsnext(url: String): Movie{
    var nextMovie = Movie(-1,"","","")
    val client = OkHttpClient()
    val request =
        Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()

    withContext(Dispatchers.IO){
        try {
            val response = client.newCall(request).execute()
            val responseCode = response.code
            val gson = Gson()
            if(responseCode == 200){
                println("Received upcoming movie!")
                val jsonData = response.body?.string()
                nextMovie = gson.fromJson<Movie?>(jsonData, Movie::class.java)
            } else {
                println("Failed to receive upcoming movie.")
            }
        } catch (e: Exception) {
            println("Σ' ΕΠΙΑΣΑ: $e")
        }
    }
    return nextMovie
}

class MainActivity : AppCompatActivity() {

    private fun MovieLoop() {

        val movies = try {
            runBlocking {
                withContext(Dispatchers.IO){
                    get("http://10.0.2.2:8085/api/v1/movies", null)
                }
            }
        } catch (e: NetworkOnMainThreadException) {
            listOf<Movie>()//e.toString()
        }

        val movieAdapter = MovieAdapter(movies)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.adapter = movieAdapter

        mainActivityButtons(movieAdapter)
    }

    private fun mainActivityButtons(movieAdapter: MovieAdapter){
        val directMovieButton = findViewById<Button>(R.id.directMovieButton)
        val whatsNextButton = findViewById<Button>(R.id.whatsNextButton)

        directMovieButton.text = "Direct a movie"
        whatsNextButton.text = "What's next?"

        directMovieButton.setOnClickListener{
            try {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        post("http://10.0.2.2:8085/api/v1/movies/")
                    }
                }
                movieAdapter.loadMovies()
            }catch (e: Exception){
                println("Error on directMovieButton's click event:"+e.printStackTrace())
            }
        }

        whatsNextButton.setOnClickListener{
            try {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        whatsnext("http://10.0.2.2:8085/api/v1/movies/whatsnext")
                    }
                }
                movieAdapter.loadMovies()
            }catch (e: Exception){
                println("Error on whatsNextButton's click event:"+e.printStackTrace())
            }
        }
    }

    fun noviflixClient(){
        MovieLoop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noviflixClient()
    }
}