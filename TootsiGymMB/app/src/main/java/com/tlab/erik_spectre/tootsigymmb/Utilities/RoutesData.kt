package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.app.Application
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tlab.erik_spectre.tootsigymmb.Model.Route

object RoutesData {
    var data: List<Route>? = null

    fun init(fileName: String, app: Application) {
        val jsonString = app.assets.open(fileName).bufferedReader().use {  it->
            it.readText()
        }

        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        val listType = Types.newParameterizedType(List::class.java, Route::class.java)
        val adapter: JsonAdapter<List<Route>> = moshi.adapter(listType)
        data = adapter.fromJson(jsonString)
    }
}