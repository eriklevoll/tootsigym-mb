package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tlab.erik_spectre.tootsigymmb.Controller.MainActivity
import com.tlab.erik_spectre.tootsigymmb.Model.Route
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.schedulers.IoScheduler
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import se.ansman.kotshi.KotshiJsonAdapterFactory
import java.io.*


object RoutesData {
    var data: Map<String, List<Route>?>? = null
    var data2: List<Route>? = null

    fun init(fileName: String, app: Application) {
        val jsonString = app.assets.open(fileName).bufferedReader().use { it.readText() }

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        val listType = Types.newParameterizedType(List::class.java, Route::class.java)
        val adapter: JsonAdapter<List<Route>> = moshi.adapter(listType)

        val ungroupedData = adapter.fromJson(jsonString)
        data = ungroupedData?.groupBy { it.Data.Grade }
    }

    fun initString(strData: String) {
        val jsonString = strData

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        //val moshi = Moshi.Builder().add(KotshiJsonAdapterFactory()).build()

        val listType = Types.newParameterizedType(List::class.java, Route::class.java)
        val adapter: JsonAdapter<List<Route>> = moshi.adapter(listType)

        //val j = adapter.fromJson(strData)

        val ungroupedData = adapter.fromJson(jsonString)
        data = ungroupedData?.groupBy { it.Data.Grade }
        //data = j?.groupBy { it.Data.Grade }
    }

    fun readRoutesFromInternal(activity: MainActivity) {
        activity.setToast("Reading routes")
        launch {
            withContext(DefaultDispatcher) { readFile(activity) }
            activity.runOnUiThread {
                activity.setToast("Done reading ${data2?.count()} routes")
            }
        }
    }

    fun readFile(activity: MainActivity){
        val list = activity.fileList()
        val validFile = (ROUTES_FILE_NAME in list)

        if (!validFile) return

        val fi = FileInputStream(File(activity.filesDir,"data_routes.txt"))
        val oi = ObjectInputStream(fi)

        RoutesData.data2 = oi.readObject() as List<Route>

        oi.close()
        fi.close()
    }

    interface Api {

        @GET(ROUTES_FILE_FILEURL)
        fun getRoutesList(): Observable<List<Route>>

    }

    fun downloadRoutes(activity: MainActivity) {
        activity.setToast("Started download")
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(ROUTES_FILE_BASEURL).build()

        val postsApi = retrofit.create(Api::class.java)

        var response = postsApi.getRoutesList()
//
        val subscribe = response.observeOn(AndroidSchedulers.mainThread()).subscribeOn(IoScheduler()).subscribe (
                { result -> saveDataToInternal(activity, result) },
                { error -> println("no, $error") }
        )
    }

    fun saveDataToInternal(activity: MainActivity, list: List<Route>) {
        activity.setToast("Download complete. Saving data")
        RoutesData.data2 = list

        launch {
            withContext(DefaultDispatcher) { saveFile(activity) }
            activity.runOnUiThread {
                activity.setToast("Done saving ${RoutesData.data2?.count()} routes")
            }
        }
    }

    fun saveFile(activity: MainActivity) {
        val file = File(activity.filesDir, "data_routes.txt")
        val f = FileOutputStream(file)
        val o = ObjectOutputStream(f)

        o.writeObject(RoutesData.data2)

        o.close()
        f.close()
    }
}

