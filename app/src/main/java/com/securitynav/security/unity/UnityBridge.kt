package com.securitynav.security.unity

import android.content.Context
import android.util.Log

class UnityBridge(private val context: Context) {

    companion object {
        private const val TAG = "UnityBridge"
        
        // Método que invoca Unity mediante JNI
        @JvmStatic
        fun onUnitySceneLoaded() {
            Log.d(TAG, "Escena de Unity 3D cargada e inicializada.")
        }

        // Enviar eventos de seguridad a la interfaz 3D
        fun sendThreatToUnity3D(gameObjectName: String, methodName: String, jsonPayload: String) {
            try {
                val unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer")
                val sendMessageMethod = unityPlayerClass.getMethod(
                    "UnitySendMessage",
                    String::class.java,
                    String::class.java,
                    String::class.java
                )
                sendMessageMethod.invoke(null, gameObjectName, methodName, jsonPayload)
            } catch (e: Exception) {
                Log.e(TAG, "UnityPlayer no está presente en el runtime actual: ${e.message}")
            }
        }
    }
}
