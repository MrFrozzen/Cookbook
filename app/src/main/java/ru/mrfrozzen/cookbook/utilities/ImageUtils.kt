package ru.mrfrozzen.cookbook.utilities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.ImageView
import java.io.IOException

// TODO: Exifinterface : androidx?
object ImageUtils {
    private val TAG = ImageUtils::class.java.name

    /**
     * Sets the image at given path to the ImageView. Gets the dimensions of the ImageView, and
     * scales the image accordingly. Also rotates the image according to it's Exif data.
     *
     * @param view      The ImageView in which the image will be placed.
     * @param imagePath String path to the image.
     */
    fun setImage(view: ImageView, imagePath: String) {
        // Get the dimensions of the View
        val targetW = view.width
        val targetH = view.height
        //Log.e(TAG, "View target width: $targetW")
        //Log.e(TAG, "View target height: $targetH")
        //Log.e(TAG, "View visibility: " + view.visibility)

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        // Determine how much to scale down the image
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true

        var bitmap = BitmapFactory.decodeFile(imagePath, bmOptions)

        try {
            val ei = ExifInterface(imagePath)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> bitmap = rotateImage(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> bitmap = rotateImage(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> bitmap = rotateImage(bitmap, 270)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        view.setImageBitmap(bitmap)
    }

    /**
     * Helper function to rotate a Bitmap image.
     *
     * @param img    The image to rotate.
     * @param degree The amount to rotate (clockwise) the image in degrees.
     * @return Returns the rotated Bitmap image.
     */
    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}
