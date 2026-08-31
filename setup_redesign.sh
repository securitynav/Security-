#!/bin/bash

# 1. Update colors
cat << 'XML' > app/src/main/res/values/colors.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="bg_dark">#0F0F1A</color>
    <color name="surface_dark">#1A1A24</color>
    <color name="neon_cyan">#00FFC2</color>
    <color name="neon_purple">#9D00FF</color>
    <color name="neon_green">#39FF14</color>
    <color name="neon_red">#FF0055</color>
    <color name="neon_yellow">#FFF000</color>
    <color name="text_primary">#FFFFFF</color>
    <color name="text_secondary">#B3B3B3</color>
    <color name="black">#000000</color>
    <color name="on_primary">#000000</color>
    <color name="on_secondary">#000000</color>
    <color name="error">#FF0055</color>
</resources>
XML

# 2. Add splash screen layout
cat << 'XML' > app/src/main/res/layout/activity_splash.xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.motion.widget.MotionLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/bg_dark"
    app:layoutDescription="@xml/motion_scene_splash">

    <ImageView
        android:id="@+id/ivRadar"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@android:drawable/ic_menu_compass"
        app:tint="@color/neon_cyan"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/tvLogo"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="SecurityNav"
        android:textColor="@color/neon_purple"
        android:textSize="32sp"
        android:textStyle="bold"
        android:alpha="0"
        app:layout_constraintTop_toBottomOf="@id/ivRadar"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="16dp" />

</androidx.constraintlayout.motion.widget.MotionLayout>
XML

# 3. Add motion scene
mkdir -p app/src/main/res/xml
cat << 'XML' > app/src/main/res/xml/motion_scene_splash.xml
<?xml version="1.0" encoding="utf-8"?>
<MotionScene xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <Transition
        app:constraintSetStart="@id/start"
        app:constraintSetEnd="@id/end"
        app:duration="2000"
        app:autoTransition="animateToEnd">
    </Transition>

    <ConstraintSet android:id="@+id/start">
        <Constraint
            android:id="@+id/ivRadar"
            android:layout_width="120dp"
            android:layout_height="120dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" >
            <Transform android:rotation="0" />
        </Constraint>
        <Constraint
            android:id="@+id/tvLogo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintTop_toBottomOf="@id/ivRadar"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            android:layout_marginTop="16dp">
            <PropertySet android:alpha="0" />
        </Constraint>
    </ConstraintSet>

    <ConstraintSet android:id="@+id/end">
        <Constraint
            android:id="@+id/ivRadar"
            android:layout_width="80dp"
            android:layout_height="80dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" >
            <Transform android:rotation="720" />
        </Constraint>
        <Constraint
            android:id="@+id/tvLogo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintTop_toBottomOf="@id/ivRadar"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            android:layout_marginTop="16dp">
            <PropertySet android:alpha="1" />
        </Constraint>
    </ConstraintSet>
</MotionScene>
XML

# 4. AuthManager Stub
cat << 'KT' > app/src/main/java/com/securitynav/security/data/AuthManager.kt
package com.securitynav.security.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }
}
KT

# 5. SplashActivity
cat << 'KT' > app/src/main/java/com/securitynav/security/ui/SplashActivity.kt
package com.securitynav.security.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.securitynav.security.R
import com.securitynav.security.data.AuthManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val authManager = AuthManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (authManager.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
            finish()
        }, 2200)
    }
}
KT

