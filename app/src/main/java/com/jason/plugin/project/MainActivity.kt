package com.jason.plugin.project

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.didi.virtualapk.PluginManager
import com.jason.plugin.commom.BundleUrl
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.go_plugin_one).setOnClickListener {
            val pkg="com.jason.plugin.one"
            if (PluginManager.getInstance(this).getLoadedPlugin(pkg) == null) {
                Toast.makeText(this, "plugin $pkg not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent()
            intent.setClassName(this, BundleUrl.PLUGIN_ONE_MAIN_URL)
            startActivity(intent)

        }

        findViewById<Button>(R.id.go_plugin_two).setOnClickListener {
            val pkg="com.jason.plugin.two"
            if (PluginManager.getInstance(this).getLoadedPlugin(pkg) == null) {
                Toast.makeText(this, "plugin $pkg not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent()
            intent.setClassName(this, BundleUrl.PLUGIN_TWO_MAIN_URL)
            startActivity(intent)
        }

        loadPlugin()
    }


    private fun loadPlugin(){
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            Toast.makeText(this, "sdcard was NOT MOUNTED!", Toast.LENGTH_SHORT).show()
        }
        val pluginManager = PluginManager.getInstance(this)
        val pluginOne = File(Environment.getExternalStorageDirectory(), "plugin_one.apk")
        val pluginTwo = File(Environment.getExternalStorageDirectory(), "plugin_two.apk")

        try {
            if (pluginOne.exists()){
                pluginManager.loadPlugin(pluginOne)
                Log.e("MainActivity--->","load succss $pluginOne")
            }

            if (pluginTwo.exists()){
                pluginManager.loadPlugin(pluginTwo)
                Log.e("MainActivity--->","load succss $pluginTwo")
            }
        }catch (e:Exception){
            Log.e("MainActivity--->",e.toString())
        }
    }
}
