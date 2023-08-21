package com.example.serkom

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.serkom.camera.CameraActivity
import com.example.serkom.data.ItemData
import com.example.serkom.databinding.ActivityFormEntryBinding
import com.example.serkom.utils.rotateBitmap
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FormEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormEntryBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    var selectedDate: String = ""
    var sImage: String? = ""
    private var getFile: File? = null
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        requestCamPermission()

        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayofMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayofMonth)
            updateLabel(myCalendar)
        }

        db = FirebaseDatabase.getInstance().reference

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.buttonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        binding.btnSave.setOnClickListener {
            val nik = binding.nikEditText.text.toString().trim()
            val name = binding.nameEditText.toString().trim()
            val phone = binding.phoneEditText.toString().trim()

            if (nik.isEmpty() && nik.length < 16) {
                binding.nikEditText.error = "NIK harus diisi dan sesuai"
                binding.nikEditText.requestFocus()
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                binding.nameEditText.error = "Nama harus diisi"
                binding.nameEditText.requestFocus()
                return@setOnClickListener
            }
            if (phone.isEmpty() || phone.length < 12) {
                binding.phoneEditText.error = "Nomor telepon harus diisi"
                binding.phoneEditText.requestFocus()
                return@setOnClickListener
            }
            insertData()
        }
        binding.btnCamera.setOnClickListener {
            startCamerax()
        }
        binding.btnChooseImage.setOnClickListener {
            insertImg()
        }
        binding.tvPilihAlamat.setOnClickListener {
            getLastLocation()
        }
        binding.btnCalendar.setOnClickListener {
            DatePickerDialog(
                this,
                datePicker,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun startCamerax() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    //Launcher for CameraX
    private val launcherIntentCameraX =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CAMERA_X_RESULT) {
                val myFile: File = it.data?.getSerializableExtra(EXTRA_PHOTO) as File
                val backCamera = it.data?.getBooleanExtra(BACK_CAMERA, true) as Boolean
                getFile = myFile

                val result = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    BitmapFactory.decodeFile(myFile.path)
                } else {
                    rotateBitmap(BitmapFactory.decodeFile(myFile.path), backCamera)
                }

                binding.imageDisplay.setImageBitmap(result)
            }
        }

    private fun updateLabel(myCalendar: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        selectedDate = sdf.format(myCalendar.time) // store the selected date in a variable
        binding.tvTanggal.text = selectedDate
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener() { task ->
                    var location = task.result
                    if (location == null) {
                        getNewLocation()
                    } else {
                        binding.tvIsiAlamat.text =
                            "Lat: " + location.latitude + "; Long: " + location.longitude + "\n Kota: " + getCityName(
                                location.latitude, location.longitude
                            ) + ", Negara: " + getCountryName(location.latitude, location.longitude)
                    }
                }
            } else {
                Toast.makeText(this, "Tolong nyalakakan servis lokasi anda", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestLocPermission()
        }
    }

    private fun getNewLocation() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2

        // Check if the required permission is granted
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission has been granted
            Looper.myLooper()?.let {
                fusedLocationProviderClient!!.requestLocationUpdates(
                    locationRequest, locationCallback, it
                )
            }
        } else {
            // Permission has not been granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_ID
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            binding.tvIsiAlamat.text =
                "Lat: " + lastLocation.latitude + "; Long: " + lastLocation.longitude + "\n Kota: " + getCityName(
                    lastLocation.latitude, lastLocation.longitude
                ) + ", Negara: " + getCountryName(lastLocation.latitude, lastLocation.longitude)
        }
    }

    // Cek Permission Lokasi
    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Req Cam Permission
    private fun requestCamPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.CAMERA
            ), REQUEST_CODE_PERMISSIONS
        )
    }

    // Req Location Permission
    private fun requestLocPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            ), PERMISSION_ID
        )
    }

    // Cek GPS
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getCityName(lat: Double, long: Double): String {
        var cityName = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        val address = geocoder.getFromLocation(lat, long, 1)

        if (address != null) {
            cityName = address.get(0).locality
        }
        return cityName
    }

    private fun getCountryName(lat: Double, long: Double): String {
        var countryName = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        val address = geocoder.getFromLocation(lat, long, 1)

        if (address != null) {
            countryName = address.get(0).countryName
        }
        return countryName
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug", "Allowed")
            }
        }
    }


    private fun insertData() {
        db = FirebaseDatabase.getInstance().getReference("users")
        val nik = binding.nikEditText.text.toString()
        val name = binding.nameEditText.text.toString()
        val phone = binding.phoneEditText.text.toString()
        val gender =
            if (binding.rbLaki.isChecked) "Laki-Laki" else "Perempuan" // set the gender field based on the selected radio button

        val user =
            ItemData(
                nik,
                name,
                phone,
                gender,
                selectedDate,
                sImage
            ) // pass the gender field to the user object
        val databaseReference = FirebaseDatabase.getInstance().reference
        val id = databaseReference.push().key
        binding.progressBar.visibility = View.VISIBLE

        db.child(id.toString()).setValue(user).addOnSuccessListener {
            binding.nikEditText.text?.clear()
            binding.nameEditText.text?.clear()
            binding.phoneEditText.text?.clear()
            binding.rbLaki.isChecked = false
            binding.rbPerempuan.isChecked = false // clear the selected radio button
            binding.tvIsiAlamat.text = "Belum ada lokasi" // clear the location
            binding.tvTanggal.text = "Tanggal" // clear the selected date
            binding.imageDisplay.setImageResource(R.drawable.ic_place_holder)
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Data tidak berhasil disimpan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertImg() {
        val myFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        myFileIntent.type = "image/*"
        ActivityResultLauncher.launch(myFileIntent)
    }


    private val ActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data!!.data
            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val bytes = stream.toByteArray()
                sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                binding.imageDisplay.setImageBitmap(myBitmap)
                inputStream!!.close()
                Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        const val EXTRA_PHOTO = "extra_photo"
        const val BACK_CAMERA = "extra_BackCamera"

        private const val PERMISSION_ID = 1000
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}