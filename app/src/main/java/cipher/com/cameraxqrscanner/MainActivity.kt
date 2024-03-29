package cipher.com.cameraxqrscanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cipher.com.utils.AutoFitPreviewBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

private const val TAG = "CameraX QR Scanner"
private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity(), QRCodeListener{

    private var preview: Preview? = null
    private var qrCodeAnalyzer: ImageAnalysis? = null
    private var lensFacing = CameraX.LensFacing.BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        // Request camera permissions
        if (allPermissionsGranted()) {
            camera_viewfinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        Toast.makeText(this, "Please point at a QR Code!", Toast.LENGTH_LONG).show()
    }

    private fun startCamera(){
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { camera_viewfinder.display.getRealMetrics(it) }
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        // Create configuration object for the preview
        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(camera_viewfinder.display.rotation)
        }.build()

        // Setup image analysis pipeline that looks for QRCodes
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // Use a worker thread for image analysis to prevent glitches
            val analyzerThread = HandlerThread(
                "FirebaseQRScanner").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        // Build the preview viewfinder use case
        preview = AutoFitPreviewBuilder.build(previewConfig, camera_viewfinder)

        // Build the image analyser and instantiate our analyzer
        qrCodeAnalyzer = ImageAnalysis(analyzerConfig).also {
            it.analyzer = QRCodeAnalyzer(this)
        }

        CameraX.bindToLifecycle(this, preview, qrCodeAnalyzer)
    }

    override fun onQRReadSuccess(results: List<FirebaseVisionBarcode>) {
        for(result in results){
            val valueType = result.valueType
            val rawValue = result.rawValue

            Log.d(TAG, "Value Type: $valueType")
            Log.d(TAG, "Raw Value: $rawValue")
            Toast.makeText(this, rawValue, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onQRReadFailure(exception: Exception) {
        exception.printStackTrace()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                camera_viewfinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}
