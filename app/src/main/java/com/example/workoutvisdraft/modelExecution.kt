package com.example.workoutvisdraft
import android.annotation.SuppressLint
import android.content.Context;
import android.graphics.*
import android.media.Image
import android.view.SurfaceHolder
import androidx.camera.core.VideoCapture
import androidx.camera.core.impl.VideoCaptureConfig

class ModelExecution{
    private lateinit var posenet: Posenet;
    private var paint = Paint()
    var videoCapture: VideoCapture? = null
    private val minConfidence = 0.5

    private val circleRadius = 8.0f
    private val bodyJoints = listOf(
            Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
            Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
            Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
            Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
            Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
            Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
            Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
            Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
            Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
            Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )
    fun init(con:Context){

        posenet = Posenet(con);
    }
    @SuppressLint("RestrictedApi")
    public fun getVideoCaptureX():VideoCapture{
        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
           // setTargetAspectRatio(1)
            //setTargetRotation(viewFinder.display.rotation)

        }.build()

        videoCapture=VideoCaptureConfig.Builder().build()
        return videoCapture as VideoCapture
        /*val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)

        }.build()

        videoCapture = VideoCapture(videoCaptureConfig)*/


    }

    fun call(scaledBitmap: Bitmap):Person{

        var person=posenet.estimateSinglePose(scaledBitmap)

        return person
    }
    private fun setPaint() {
        paint.color = Color.RED
        paint.textSize = 80.0f
        paint.strokeWidth = 8.0f
    }
    fun draw(canvas: Canvas, person: Person, bitmap: Bitmap, surfaceHolder: SurfaceHolder) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        // Draw `bitmap` and `person` in square canvas.
        val screenWidth: Int
        val screenHeight: Int
        val left: Int
        val right: Int
        val top: Int
        val bottom: Int
        if (canvas.height > canvas.width) {
            screenWidth = canvas.width
            screenHeight = canvas.width
            left = 0
            top = (canvas.height - canvas.width) / 2
        } else {
            screenWidth = canvas.height
            screenHeight = canvas.height
            left = (canvas.width - canvas.height) / 2
            top = 0
        }
        right = left + screenWidth
        bottom = top + screenHeight

        setPaint()
        canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(left, top, right, bottom),
                paint
        )

        val widthRatio = screenWidth.toFloat() / MODEL_WIDTH
        val heightRatio = screenHeight.toFloat() / MODEL_HEIGHT

        // Draw key points over the image.
        for (keyPoint in person.keyPoints) {
            if (keyPoint.score > minConfidence) {
                val position = keyPoint.position
                val adjustedX: Float = position.x.toFloat() * widthRatio + left
                val adjustedY: Float = position.y.toFloat() * heightRatio + top
                canvas.drawCircle(adjustedX, adjustedY, circleRadius, paint)
            }
        }

        for (line in bodyJoints) {
            if (
                    (person.keyPoints[line.first.ordinal].score > minConfidence) and
                    (person.keyPoints[line.second.ordinal].score > minConfidence)
            ) {
                canvas.drawLine(
                        person.keyPoints[line.first.ordinal].position.x.toFloat() * widthRatio + left,
                        person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio + top,
                        person.keyPoints[line.second.ordinal].position.x.toFloat() * widthRatio + left,
                        person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio + top,
                        paint
                )
            }
        }

        canvas.drawText(
                "Score: %.2f".format(person.score),
                (15.0f * widthRatio),
                (30.0f * heightRatio + bottom),
                paint
        )
        canvas.drawText(
                "Device: %s".format(posenet.device),
                (15.0f * widthRatio),
                (50.0f * heightRatio + bottom),
                paint
        )
        canvas.drawText(
                "Time: %.2f ms".format(posenet.lastInferenceTimeNanos * 1.0f / 1_000_000),
                (15.0f * widthRatio),
                (70.0f * heightRatio + bottom),
                paint
        )

        // Draw!
        surfaceHolder!!.unlockCanvasAndPost(canvas)
    }




}