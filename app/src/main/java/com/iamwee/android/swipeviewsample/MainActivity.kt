package com.iamwee.android.swipeviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        unLockerSwipeVerticalView.setOnProgressChangeListener {
            textView.alpha = (it.toFloat() / 100)
        }

        unLockerSwipeVerticalView.setOnSwipeActionListener(object : UnLockerSwipeVerticalView.OnSwipeActionListener {
            override fun onSwipeSucceeded() {
                Toast.makeText(this@MainActivity, "Swipe succeeded", Toast.LENGTH_SHORT).show()
            }

            override fun onSwipeFailed() {
                Toast.makeText(this@MainActivity, "Swipe failed", Toast.LENGTH_SHORT).show()
            }

        })
    }
}