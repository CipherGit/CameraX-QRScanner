package cipher.com.cameraxqrscanner

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import java.lang.Exception

interface QRCodeListener {
    fun onSuccess(results: List<FirebaseVisionBarcode>)
    fun onFailure(exception: Exception)
}