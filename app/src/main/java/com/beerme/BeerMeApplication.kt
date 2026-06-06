package com.beerme

import android.app.Application
import com.beerme.data.AppContainer
import org.osmdroid.config.Configuration

class BeerMeApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // osmdroid configuration (tile cache, user agent for OSM tile policy).
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName
        container = AppContainer(this)
    }
}
