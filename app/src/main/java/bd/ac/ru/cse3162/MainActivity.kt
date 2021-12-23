package bd.ac.ru.cse3162

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var fusedlocation: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedlocation = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()


    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (locationEnable()) {
                fusedlocation.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location != null) {
                        val lat = location.latitude.toString()
                        var long = location.longitude.toString()

                        if (lat != null && long != null) {
                            getJsonData(lat, long)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on your GPS location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun locationEnable(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        try {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1001
            )
        } catch (e: Exception) {

        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                requestPermission()
            }
        }
    }

    public fun getJsonData(lat: String, long: String) {
        val API_KEY = "a4a8993271ad47afb3ac8003707e4261" // use your own api_key
        val queue = Volley.newRequestQueue(this)
        val cityName = getCityName(lat.toDouble(), long.toDouble())

        val url =
           // "https://api.weatherbit.io/v2.0/current?city=${cityName}&key=${API_KEY}"
            "https://api.weatherbit.io/v2.0/current?lat=35.7796&lon=-78.6382&key=a4a8993271ad47afb3ac8003707e4261&include=minutely"


        try {
            val jsonRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    setValues(response)
                },
                Response.ErrorListener {
                    Toast.makeText(
                        this,
                        "Please turn on internet connection",
                        Toast.LENGTH_LONG
                    ).show()
                })
            queue.add(jsonRequest)
            Log.i("Loc", jsonRequest.toString() )

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "ERROR" + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun setValues(response: JSONObject) {

        city.text = "City Name: "
        var tempr = response.getJSONObject("main").getString("temp")
        temp_c.text = "Temparature: ${(tempr.toFloat() - 273).toInt()}°C"
    }


    private fun getCityName(lat: Double, long: Double): String {
        var geoCoder = Geocoder(this, Locale.getDefault())
        var adress = geoCoder.getFromLocation(lat, long, 3)

        return adress.get(0).locality
    }
}