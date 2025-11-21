package com.example.lab_week_11_b

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.Executors
import android.Manifest

class MainActivity : AppCompatActivity() {
    companion object{
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    // Helper class to manage files in MediaStore
    private lateinit var providerFileManager: ProviderFileManager
    // Data model for the file
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null
    // Flag to indicate whether the user is capturing a photo or video
    private var isCapturingVideo = false
    // Activity result launcher to capture images and videos
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        providerFileManager =
            ProviderFileManager(
                applicationContext,
                FileHelper(applicationContext),
                contentResolver,
                Executors.newSingleThreadExecutor(),
                MediaContentHelper()
            )

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess){
                    providerFileManager.insertImageToStore(photoInfo)
                } else {

                }

            }
        takeVideoLauncher =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) { isSuccess ->
                if (isSuccess){
                    providerFileManager.insertVideoToStore(videoInfo)
                } else{

                }

            }

        findViewById<Button>(R.id.photo_button).setOnClickListener {
            isCapturingVideo = false
            checkStoragePermission {
                openImageCapture()
            }
        }

        findViewById<Button>(R.id.video_button).setOnClickListener {
            isCapturingVideo = true
            checkStoragePermission {
                openVideoCapture()
            }
        }
    }

    private fun openImageCapture(){
        photoInfo =
            providerFileManager.generatePhotoUri(System.currentTimeMillis())
        photoInfo?.uri?.let { uri ->
            takePictureLauncher.launch(uri)
        }

    }

    private fun openVideoCapture() {
        videoInfo =
            providerFileManager.generateVideoUri(System.currentTimeMillis())
        videoInfo?.uri?.let { uri ->
            takeVideoLauncher.launch(uri)
        }
    }

    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (android.os.Build.VERSION.SDK_INT <
            android.os.Build.VERSION_CODES.Q
        ) {
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
// If the permission is granted
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
// if the permission is not granted, request the permission
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        }
        else {
            onPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED)) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
                return
            }
// for other request code, do nothing
            else -> {
            }

                }
        }
    }
