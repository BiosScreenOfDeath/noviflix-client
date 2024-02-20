package com.example.noviflixclient

import android.os.Bundle
import android.os.NetworkOnMainThreadException
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.noviflixclient.ui.theme.NoviflixClientTheme
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

@Composable
fun AlertDialogWrapper(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoviflixClientTheme {
                NoviflixClient(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun NoviflixClient(modifier: Modifier){
    Surface(modifier = Modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        MovieLoop()
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun MovieLoop(
    modifier: Modifier = Modifier
) {

    val movies = try {
        runBlocking {
            withContext(Dispatchers.IO){
                get("http://10.0.2.2:8085/api/v1/movies", null)
            }
        }
    } catch (e: NetworkOnMainThreadException) {
        listOf<Movie>()//e.toString()
    }

    val showCreatedMovie = remember{ mutableStateOf(false) }
    val showNextMovie = remember { mutableStateOf(false) }
    val showSearchedMovie = remember { mutableStateOf(false) }
    val searchText = remember { mutableStateOf("") }

    val nextMovie = remember{ mutableStateOf<Movie>(Movie(-1,"","","")) }
    val newMovie = remember{ mutableStateOf<Movie>(Movie(-1,"","","")) }
    val updatedMovie = remember{ mutableStateOf<Movie>(Movie(-1,"","","")) }
    val searchMovie = remember{ mutableStateOf<Movie>(Movie(-1,"","","")) }

    Column (
        modifier = Modifier
    ) {

        Row (
            modifier = Modifier
        ){
            ElevatedButton(onClick = {
                newMovie.value = try {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            println("Running post!")
                            post("http://10.0.2.2:8085/api/v1/movies/")
                        }
                    }
                } catch (e: Exception) {
                    println(e.printStackTrace())
                    Movie(-1, "", "", "")
                }
                showCreatedMovie.value = !showCreatedMovie.value
                println("New movie! ${newMovie.value.title}")
            }) {
                Text("Direct a movie!")
            }

            ElevatedButton(
                onClick = {
                    nextMovie.value = try{
                        runBlocking {
                            withContext(Dispatchers.IO){
                                whatsnext("http://10.0.2.2:8085/api/v1/movies/whatsnext")
                            }
                        }
                    }catch (e: Exception){
                        println(e.printStackTrace())
                        Movie(-1, "", "", "")
                    }
                    showNextMovie.value = !showNextMovie.value
                    println("Next movie! ${nextMovie.value.title}")
                },
                modifier = modifier.fillMaxWidth()
                ) {
                Text("What's next?")
            }

            if(showNextMovie.value){
                AlertDialogWrapper(
                    onDismissRequest = { showNextMovie.value = false },
                    onConfirmation = { showNextMovie.value = false },
                    dialogTitle = "Coming up",
                    dialogText = "Stay tuned for '${nextMovie.value.title}', where '${nextMovie.value.plot}'!",
                    icon = Icons.Default.Info
                )
            }

            if(showCreatedMovie.value){
                println("New movie! ${newMovie.value.title}")
                AlertDialogWrapper(
                    onDismissRequest = { showCreatedMovie.value = false },
                    onConfirmation = { showCreatedMovie.value = false },
                    dialogTitle = "Movie created",
                    dialogText = "Directed the movie: ${newMovie.value.title}!",
                    icon = Icons.Default.Info
                )
            }
        }

        Row (
            modifier = modifier.align(Alignment.CenterHorizontally)
        ) {
            OutlinedTextField(
                value = searchText.value,
                onValueChange = { newValue -> searchText.value = newValue },
                label = { Text("ID Search") }
            )
            ElevatedButton(
                onClick = {
                    searchMovie.value = try{
                        runBlocking {
                            withContext(Dispatchers.IO){
                                get("http://10.0.2.2:8085/api/v1/movies", searchText.value.toInt())[0]
                            }
                        }
                    } catch (e: Exception){
                        println(e.printStackTrace())
                        searchMovie.value
                    }
                    showSearchedMovie.value = !showSearchedMovie.value
                },
                modifier = modifier.align(Alignment.CenterVertically),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Green
                )
            ) {
                Text("Submit ID")
            }

            if(showSearchedMovie.value){
                AlertDialogWrapper(
                    onDismissRequest = { showSearchedMovie.value = false },
                    onConfirmation = { showSearchedMovie.value = false },
                    dialogTitle = "Coming up",
                    dialogText = "Movie: \"${searchMovie.value.title}\"" + "\n" +
                        "Director: \"${searchMovie.value.director}\"" + "\n" +
                        "Plot: \"${searchMovie.value.plot}\"",
                    icon = Icons.Default.Info
                )
            }
        }

        LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
            items(
                items = movies,
                ) {  movie ->

                val expandButton = remember { mutableStateOf(false) }
                val extraPadding = if(expandButton.value) 48.dp else 0.dp
                val deleteButton = remember { mutableStateOf(false) }
                val deleteAlertDialog = remember { mutableStateOf(false) }
                val updateAlertDialog = remember { mutableStateOf(false) }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    if(!deleteButton.value){
                        Column (
                            modifier = Modifier
                                .padding(12.dp)
                        ) {
                            Row {
                                Column (modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = extraPadding)) {
                                    Text(
                                        text = "Movie: ${movie.title}\n" +
                                                //"Director: ${movie.director}\n" +
                                                "Plot: ${movie.plot}",
                                    )
                                }
                                ElevatedButton(onClick = {
                                    expandButton.value = !expandButton.value
                                }) {
                                    Text(
                                        if (expandButton.value){
                                            "Less"
                                        } else {
                                            "More"
                                        }
                                    )
                                }
                            }
                            if(expandButton.value){
                                ElevatedButton(onClick = {
                                    deleteButton.value = !deleteButton.value
                                    try {
                                        runBlocking {
                                            withContext(Dispatchers.IO) {
                                                delete("http://10.0.2.2:8085/api/v1/movies/${movie.id}")
                                            }
                                        }
                                    } catch (e: Exception){ println(e.printStackTrace()) }
                                    deleteAlertDialog.value = !deleteAlertDialog.value
                                }) {
                                    Text("Delete movie")
                                }
                                ElevatedButton(onClick = {
                                    updatedMovie.value = try {
                                        runBlocking {
                                            withContext(Dispatchers.IO) {
                                                put("http://10.0.2.2:8085/api/v1/movies/${movie.id}", movie.id)
                                            }
                                        }
                                    } catch (e: Exception){
                                        println(e.printStackTrace())
                                        Movie(-1,"","","")
                                    }
                                    updateAlertDialog.value = !updateAlertDialog.value
                                }) {
                                    Text("Update movie")
                                }
                            }
                        }
                    }
                    if(deleteAlertDialog.value){
                        AlertDialogWrapper(
                            onDismissRequest = { deleteAlertDialog.value = false },
                            onConfirmation = { deleteAlertDialog.value = false },
                            dialogTitle = "Movie deleted",
                            dialogText = "Movie ${movie.title} was deleted!",
                            icon = Icons.Default.Info
                        )
                    }
                    if(updateAlertDialog.value){
                        AlertDialogWrapper(
                            onDismissRequest = { updateAlertDialog.value = false },
                            onConfirmation = { updateAlertDialog.value = false },
                            dialogTitle = "Movie updated",
                            dialogText = "This movie was lame, we're remaking it as '${updatedMovie.value.title}'!",
                            icon = Icons.Default.Info
                        )
                    }
                }
            }
        }
    }
}