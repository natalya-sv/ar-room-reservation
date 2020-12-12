package nl.natalya.roomreservation.repositories

import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Session
import nl.natalya.roomreservation.data.ImageData

class AugmentedImagesDatabase {
    lateinit var imagesDatabase: AugmentedImageDatabase

    /** Adds images to the image database*/
    fun addImageToImageDatabase(session: Session, list: MutableList<ImageData>): Boolean {

        val listSize = list.size
        return if (listSize > 0) {
            imagesDatabase = AugmentedImageDatabase(session)
            for (image in list) {
                val imageWidth = image.width / 100.0
                imagesDatabase.addImage(image.name, image.bitmap, imageWidth.toFloat())
            }
            true
        }
        else {
            false
        }
    }
}