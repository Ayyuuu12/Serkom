package com.example.serkom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class
MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnForm: Button
    private lateinit var btnData: Button
    private lateinit var btnInformation: Button
    private lateinit var btnExit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        btnForm = findViewById(R.id.btn_form_entry)
        btnForm.setOnClickListener(this)

        btnData = findViewById(R.id.btn_data)
        btnData.setOnClickListener(this)

        btnInformation = findViewById(R.id.btn_information)
        btnInformation.setOnClickListener(this)

        btnExit = findViewById(R.id.btn_exit)
        btnExit.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_form_entry -> {
                val intentForm = Intent(this@MainActivity, FormEntryActivity::class.java)
                startActivity(intentForm)
            }
            R.id.btn_data -> {
                val intentForm = Intent(this@MainActivity, DataActivity::class.java)
                startActivity(intentForm)
            }
            R.id.btn_information -> {
                val intentForm = Intent(this@MainActivity, InformationActivity::class.java)
                startActivity(intentForm)
            }
            R.id.btn_exit -> {
                finishAffinity()
            }
        }
    }
}