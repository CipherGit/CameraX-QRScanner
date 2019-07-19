package cipher.com.cameraxqrscanner

import android.util.SparseIntArray
import android.view.Surface
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class QRCodeAnalyzer(private val qrListener: QRCodeListener) : ImageAnalysis.Analyzer {

    private val ORIENTATIONS = SparseIntArray()
    private val firebaseQRDetector : FirebaseVisionBarcodeDetector

    init {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()
        firebaseQRDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        ORIENTATIONS.append(90, Surface.ROTATION_0)
        ORIENTATIONS.append(0, Surface.ROTATION_90)
        ORIENTATIONS.append(270, Surface.ROTATION_180)
        ORIENTATIONS.append(180, Surface.ROTATION_270)
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        if(image != null){
            image.image?.let {
                val fireBaseImage = FirebaseVisionImage.fromMediaImage(it, ORIENTATIONS[rotationDegrees])
                firebaseQRDetector.detectInImage(fireBaseImage)
                    .addOnSuccessListener { results ->
                        qrListener.onQRReadSuccess(results)
                    }.addOnFailureListener { exception ->
                        qrListener.onQRReadFailure(exception)
                    }
            }
        }
    }
}