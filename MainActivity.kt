package com.example.freecinema

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Movie(
    val title:String, val year:String, val description:String,
    val sourceUrl:String, val downloadUrl:String? = null
)

class MainActivity : AppCompatActivity() {
    private val allMovies = listOf(
        Movie("Night of the Living Dead", "1968",
            "Classic horror film commonly available as a public-domain title.",
            "https://archive.org/details/night_of_the_living_dead",
            null),
        Movie("His Girl Friday", "1940",
            "Classic comedy-drama. Check the license applicable in your country.",
            "https://archive.org/details/his_girl_friday",
            null),
        Movie("The General", "1926",
            "Buster Keaton silent-film classic. Availability varies by jurisdiction.",
            "https://archive.org/details/the_general",
            null),
        Movie("Charade", "1963",
            "Classic mystery/romance. Check the license applicable in your country.",
            "https://archive.org/details/Charade_1963",
            null)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = findViewById<RecyclerView>(R.id.movies)
        val search = findViewById<EditText>(R.id.search)
        list.layoutManager = LinearLayoutManager(this)

        fun show(items: List<Movie>) {
            list.adapter = MovieAdapter(items, { movie ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(movie.sourceUrl)))
            }, { movie ->
                if (movie.downloadUrl == null) {
                    Toast.makeText(this,
                        "This source does not expose a direct download link in this starter app. Use the source page's permitted download option.",
                        Toast.LENGTH_LONG).show()
                    return@MovieAdapter
                }
                val req = DownloadManager.Request(Uri.parse(movie.downloadUrl))
                    .setTitle(movie.title)
                    .setDescription("Downloading legally permitted movie")
                    .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, movie.title + ".mp4")
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(false)
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
            })
        }

        show(allMovies)
        search.addTextChangedListener(object: android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st:Int, c:Int, a:Int) {}
            override fun onTextChanged(s: CharSequence?, st:Int, b:Int, c:Int) {
                val q = s.toString().trim().lowercase()
                show(if (q.isEmpty()) allMovies else allMovies.filter {
                    it.title.lowercase().contains(q) || it.year.contains(q)
                })
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
}

class MovieAdapter(
    private val items: List<Movie>,
    private val watch:(Movie)->Unit,
    private val download:(Movie)->Unit
) : RecyclerView.Adapter<MovieAdapter.VH>() {
    class VH(v:View):RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(p:ViewGroup, t:Int):VH {
        val v = LinearLayout(p.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20,20,20,20)
            setBackgroundColor(android.graphics.Color.rgb(22,22,22))
            val lp = RecyclerView.LayoutParams(-1, -2)
            lp.setMargins(12, 8, 12, 8)
            layoutParams = lp
        }
        return VH(v)
    }

    override fun onBindViewHolder(h:VH, pos:Int) {
        val box = h.itemView as LinearLayout
        box.removeAllViews()
        val m = items[pos]
        val title = TextView(box.context).apply {
            text = "${m.title} (${m.year})"
            textSize = 19f
            setTextColor(android.graphics.Color.WHITE)
        }
        val desc = TextView(box.context).apply {
            text = m.description
            textSize = 14f
            setTextColor(android.graphics.Color.LTGRAY)
            setPadding(0,8,0,8)
        }
        val row = LinearLayout(box.context).apply { orientation = LinearLayout.HORIZONTAL }
        val watchBtn = Button(box.context).apply {
            text = "WATCH"
            setOnClickListener { watch(m) }
        }
        val downloadBtn = Button(box.context).apply {
            text = "DOWNLOAD"
            setOnClickListener { download(m) }
        }
        row.addView(watchBtn, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(downloadBtn, LinearLayout.LayoutParams(0, -2, 1f))
        box.addView(title); box.addView(desc); box.addView(row)
    }
    override fun getItemCount() = items.size
}
