sed -i 's/<activity\s*android:name=".ui.PinActivity"\s*android:exported="true">/<activity android:name=".ui.PinActivity" android:exported="false">/g' app/src/main/AndroidManifest.xml

# Remove existing intent-filter from PinActivity
sed -i '/<intent-filter>/,/<\/intent-filter>/d' app/src/main/AndroidManifest.xml

# Add SplashActivity with intent filter
sed -i '/<activity android:name=".ui.MainActivity"/i \
        <activity android:name=".ui.SplashActivity" android:exported="true">\
            <intent-filter>\
                <action android:name="android.intent.action.MAIN" />\
                <category android:name="android.intent.category.LAUNCHER" />\
            </intent-filter>\
        </activity>' app/src/main/AndroidManifest.xml

