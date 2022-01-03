package ru.kot1.demo.activity.loading

import android.os.Bundle
import android.transition.Explode
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import dagger.hilt.android.AndroidEntryPoint
import ru.kot1.demo.R
import ru.kot1.demo.activity.loading.LoadingFragment


@AndroidEntryPoint
class LoadingActivity : AppCompatActivity(R.layout.activity_app) {

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            exitTransition = Explode()
        }
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<LoadingFragment>(R.id.fragment_container_view, "loading")
            }
    }

}

