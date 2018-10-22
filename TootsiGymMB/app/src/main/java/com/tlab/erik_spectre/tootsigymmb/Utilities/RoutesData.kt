package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.widget.Toast
import com.google.gson.GsonBuilder
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
import java.io.*


object RoutesData {
    var data: Map<String, List<Route>?>? = null
    var data2: List<Route>? = null
    var RouteFilters: HashMap<String, Any> = hashMapOf(
            "Grade" to hashSetOf("6B", "6B+"),
            "Rating" to 0,
            "UserRating" to 0,
            "Repeats" to 0,
            "IsBenchmark" to true
    )

    fun readRoutesFromInternal(activity: MainActivity) {
        activity.setToast("Reading routes")
        launch {
            withContext(DefaultDispatcher) { readFile(activity) }
            if (data2 == null || data2?.count() == 0) {
                alertRoutesUpdate(activity)
            } else {
                activity.runOnUiThread {
                    activity.setToast("Done reading ${data2?.count()} routes")
                }
            }
        }
    }

    private fun readFile(activity: MainActivity){
        val list = activity.fileList()
        val validFile = (ROUTES_FILE_NAME in list)

        if (!validFile) return

        val fi = FileInputStream(File(activity.filesDir, ROUTES_FILE_NAME))
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
                { error -> downloadFailed(activity) }
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

    private fun downloadFailed (activity: MainActivity) {
        activity.setToast("Download failed")
    }

    private fun saveFile(activity: MainActivity) {
        val file = File(activity.filesDir, ROUTES_FILE_NAME)
        val f = FileOutputStream(file)
        val o = ObjectOutputStream(f)

        o.writeObject(RoutesData.data2)

        o.close()
        f.close()
    }

    fun getRoutesCount(activity: MainActivity) {
        if (data2 == null || data2?.count() == 0) {
            alertRoutesUpdate(activity)
        } else {
            activity.setToast("${data2?.count()} routes")
        }
    }

    fun alertRoutesUpdate(activity: MainActivity) {
        activity.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Ok") { _, _ ->
                    downloadRoutes(activity)
                }
                setNegativeButton("Cancel") { _, _ ->
                    println("Clicked cancel")
                }
            }
            builder.setMessage("You have no routes saved. Update now?")

            launch {
                activity.runOnUiThread {
                    builder.create()
                    builder.show()
                }
            }
        }
    }

    fun getFilteredList() : List<Route>? {
        val newList = RoutesData.data2?.filter { RoutesData.applyRoutesFilter(it) }

        return if (newList?.count() == 0) null
        else newList
    }

    fun applyRoutesFilter(route: Route) : Boolean {
        val gradeList = RouteFilters["Grade"] as HashSet<String>
        when {
            !gradeList.contains(route.Data.Grade) -> return false
            route.Data.IsBenchmark != RouteFilters["IsBenchmark"] as Boolean -> return false
            route.Data.UserRating < RouteFilters["UserRating"] as Int -> return false
            route.Data.Rating < RouteFilters["Rating"] as Int -> return false
            route.Data.Repeats < RouteFilters["Repeats"] as Int -> return false
        }
        return true
    }
}

