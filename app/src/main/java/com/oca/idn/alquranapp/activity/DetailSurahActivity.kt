package com.oca.idn.alquranapp.activity

import AyatAdapter
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.icu.text.DateFormat.MEDIUM
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.renderscript.RenderScript
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.example.aplikasialquran.model.ModelAyat
import com.example.aplikasialquran.model.ModelSurah
import com.example.aplikasialquran.networking.Api.URL_LIST_AYAT
import com.google.android.gms.common.api.Api
import com.oca.idn.alquranapp.BuildConfig
import com.oca.idn.alquranapp.R
import kotlinx.android.synthetic.main.activity_detail_surah.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.text.DateFormat.MEDIUM

class DetailSurahActivity : AppCompatActivity() {

    private var nomor: String? = null
    private var nama: String? = null
    private var arti: String? = null
    private var type: String? = null
    private var ayat: String? = null
    private var keterangan: String? = null
    private var audio: String? = null
    private var modelSurah: ModelSurah? = null
    private var ayatAdapter: AyatAdapter? = null
    var progressDialog: ProgressDialog? = null
    var modelAyat: MutableList<ModelAyat> = ArrayList()
    private var mHandler: Handler? = null

    @SuppressLint("RestrictedApi", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_surah)

        //set toolbar
        toolbar_detail.title = null
        setSupportActionBar(toolbar_detail)
        if (BuildConfig.DEBUG && supportActionBar == null) {
            error("Assertion failed")
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mHandler = Handler()

        //get data dari ListSurah
        modelSurah = intent.getSerializableExtra("detailSurah") as ModelSurah
        if (modelSurah != null) {
            nomor = modelSurah!!.nomor
            nama = modelSurah!!.nama
            arti = modelSurah!!.arti
            type = modelSurah!!.type
            ayat = modelSurah!!.ayat
            audio = modelSurah!!.audio
            keterangan = modelSurah!!.keterangan

            fabStop.visibility = View.GONE
            fabPlay.visibility = View.VISIBLE

            //Set text
            tvHeader.text = nama
            tvTitle.text = nama
            tvSubTitle.text = arti
            tvInfo.text = "$type - $ayat Ayat "

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) tvKet.text = Html.fromHtml(
                keterangan,
                Html.FROM_HTML_MODE_COMPACT
            )
            else {
                tvKet.text = Html.fromHtml(keterangan)
            }

            //get & play Audio
            val mediaPlayer = MediaPlayer()
            fabPlay.setOnClickListener {
                try {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer.setDataSource(audio)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                fabPlay.visibility = View.GONE
                fabStop.visibility = View.VISIBLE
            }

            fabStop.setOnClickListener {
                mediaPlayer.stop()
                mediaPlayer.reset()
                fabPlay.visibility = View.VISIBLE
                fabStop.visibility = View.GONE
            }
        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data...")

        rvAyat.layoutManager = LinearLayoutManager(this)
        rvAyat.setHasFixedSize(true)

        //Methods get data
        listAyat()
    }

    private fun listAyat () {
        progressDialog!!.show()
        AndroidNetworking.get(URL_LIST_AYAT)
            .addPathParameter("nomor", nomor)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    for (i in 0 until response.length()) {
                        try {
                            progressDialog!!.dismiss()
                            val dataApi = ModelAyat()
                            val jsonObject = response.getJSONObject(i)
                            dataApi.nomor = jsonObject.getString("nomor")
                            dataApi.arab = jsonObject.getString("ar")
                            dataApi.indo = jsonObject.getString("id")
                            dataApi.terjemahan = jsonObject.getString("tr")
                            modelAyat.add(dataApi)
                            showListAyat()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@DetailSurahActivity, "Gagal menampilkan data!",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onError(anError: ANError) {
                    progressDialog!!.dismiss()
                    Toast.makeText(this@DetailSurahActivity, "Tidak ada jaringan internet!",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showListAyat() {
        ayatAdapter = AyatAdapter(modelAyat)
        rvAyat!!.adapter = ayatAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
