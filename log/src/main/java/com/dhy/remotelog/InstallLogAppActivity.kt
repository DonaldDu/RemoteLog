package com.dhy.remotelog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class InstallLogAppActivity : AppCompatActivity() {
    private val context = this
    private val installAppDialog by lazy {
        AlertDialog.Builder(context)
            .setTitle("Tip")
            .setMessage("You need install log ContentProvider app")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Install") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(context.getString(R.string.LOG_APP_URL))
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "please install browser", Toast.LENGTH_SHORT).show()
                }
            }.setOnDismissListener {
                finish()
            }.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installAppDialog.show()
    }
}