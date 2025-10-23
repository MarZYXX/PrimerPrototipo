package com.example.primerprototipo.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.primerprototipo.R

class AdminActivity : AppCompatActivity() {

    private lateinit var btnManageAccounts: Button
    private lateinit var txtBusView: TextView
    private lateinit var txtBusCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

         btnManageAccounts = findViewById(R.id.manageAcc)
        txtBusView = findViewById(R.id.busView)
        txtBusCount = findViewById(R.id.textView2)

         txtBusCount.text = "5"

         btnManageAccounts.setOnClickListener {
             Toast.makeText(applicationContext, "En construccion", Toast.LENGTH_SHORT).show()

//             val intent = Intent(this, ManageAccountsActivity::class.java)
//            startActivity(intent)
        }
    }
}
