package com.Nightjar.caliq5

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast

class PermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Show custom dialog instead of Toast
                AlertDialog.Builder(this)
                    .setTitle("تفعيل إشعارات حفظ PDF")
                    .setMessage("يرجى الموافقة على صلاحية الإشعارات لتتمكن من رؤية إشعار عند اكتمال حفظ ملف الـ PDF. إذا رفضت الصلاحية مسبقاً، سيتم توجيهك للإعدادات.")
                    .setCancelable(false)
                    .setPositiveButton("تفعيل الآن") { _, _ ->
                        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            // If previously denied but not permanently, request again. 
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
                        } else {
                            // If it's false, they either never asked or permanently denied. Let's just ask.
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
                        }
                    }
                    .setNegativeButton("تخطي") { _, _ ->
                        finish()
                    }
                    .show()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Denied. Open Settings.
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try { startActivity(fallbackIntent) } catch (e: Exception) {}
                }
                finish()
            } else {
                // Granted!
                Toast.makeText(this, "تم تفعيل الإشعارات بنجاح", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
