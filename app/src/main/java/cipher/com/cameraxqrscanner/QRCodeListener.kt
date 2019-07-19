package cipher.com.cameraxqrscanner

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import java.lang.Exception

interface QRCodeListener {
    fun onQRReadSuccess(results: List<FirebaseVisionBarcode>)
    fun onQRReadFailure(exception: Exception)
}