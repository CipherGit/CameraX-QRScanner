package cipher.com.cameraxqrscanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class QRCodeAnalyzer(private val qrListener: QRCodeListener) : ImageAnalysis.Analyzer {

    private val firebaseQRDetector : FirebaseVisionBarcodeDetector

    init {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()
        firebaseQRDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        if(image != null){
            image.image?.let {
                val fireBaseImage = FirebaseVisionImage.fromMediaImage(it, rotationDegrees)
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