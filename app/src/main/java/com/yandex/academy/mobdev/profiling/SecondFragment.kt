package com.yandex.academy.mobdev.profiling

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import kotlin.concurrent.thread
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), LocationListener {

    private val background = ColorDrawable()

    private val animation = ValueAnimator.ofArgb(Color.WHITE, Color.DKGRAY).apply {
        duration = 2000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animator ->
            background.color = animator.animatedValue as Int
        }
    }

    init {
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        view.findViewById<Button>(R.id.factorial).setOnClickListener { button ->
            thread(name = "factorial ${Random.nextInt()}") {
                // context leak
                Timber.tag("factorial").i(button.context.getString(R.string.result, factorial()))
            }
        }

        view.findViewById<Button>(R.id.network).setOnClickListener {
            repeat(10) {
                thread(name = "network ${Random.nextInt()}") {
                    network(
                        onSuccess = { Timber.tag("network").i("code = %d", it.code) },
                        onFailure = { Timber.tag("network").e(it) }
                    )
                }
            }
        }

        view.findViewById<Button>(R.id.location).setOnClickListener {
            ContextCompat.getSystemService(view.context, LocationManager::class.java)?.let { manager ->
                val result = view.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                if (result == PackageManager.PERMISSION_GRANTED) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
                }
            }
        }

        view.findViewById<Button>(R.id.animation).setOnClickListener {
            if (animation.isStarted) {
                animation.end()
                 view.background = null
            } else {
                view.background = background
                animation.start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (animation.isPaused) {
            animation.resume()
        }
    }

    override fun onPause() {
        if (animation.isRunning) {
            animation.pause()
        }
        super.onPause()
    }

    private fun factorial(): Long {
        return (3 until 7000000000 step 2).fold(1L) { acc, el -> acc * el }
    }

    private fun network(onSuccess: (Response) -> Unit, onFailure: (Throwable) -> Unit) {
        val client = OkHttpClient.Builder().addInterceptor(ChuckInterceptor(context)).build()
        val request = Request.Builder().url("https://www.google.com/").build()
        return try {
            client.newCall(request).execute().use(onSuccess)
//            client.newCall(request).execute().let(onSuccess) // resource not closed
        } catch (t: Throwable) {
            onFailure(t)
        }
    }

    override fun onLocationChanged(location: Location?) {
        Timber.tag("location").i("onLocationChanged")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.tag("location").i("onStatusChanged")
    }

    override fun onProviderEnabled(provider: String?) {
        Timber.tag("location").i("onProviderEnabled")
    }

    override fun onProviderDisabled(provider: String?) {
        Timber.tag("location").i("onProviderDisabled")
    }
}
