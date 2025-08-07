package kioskware.vision.landmarks.faces

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kioskware.vision.landmarks.`object`.Object
import kioskware.vision.landmarks.scene.Scene

object DefaultFacesVisualisations {

    fun facesProcessor(
        boundingBoxPaint: Paint = Paint().apply {
            color = Color.GREEN
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        },
        landmarkPaint: Paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
            isAntiAlias = true
        },
        landmarkStrokePaint: Paint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        },
        textPaint: Paint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            isAntiAlias = true
        }
    ) : suspend (scene: Scene, canvas: Canvas) -> Unit = { scene, canvas ->

        fun drawBoundingBox(
            canvas: Canvas,
            face: Object
        ) {
            val rect = face.bounding
            canvas.drawRect(rect, boundingBoxPaint)
        }

        fun drawLandmarks(
            canvas: Canvas,
            face: Object
        ) {
            face.landmarks.forEach { landmark ->
                val radius = 12f

                // Draw landmark point
                canvas.drawCircle(
                    landmark.location.x,
                    landmark.location.y,
                    radius,
                    landmarkPaint
                )

                // Draw landmark border
                canvas.drawCircle(
                    landmark.location.x,
                    landmark.location.y,
                    radius,
                    landmarkStrokePaint
                )
            }
        }

        @SuppressLint("DefaultLocale")
        fun drawFaceInfo(
            canvas: Canvas,
            face: Object
        ) {
            val x = face.bounding.left
            val y = face.bounding.top - 10f

            val smilingProbability = face.params.find { it.typeId == FacesImageProcessor.TypeIdFaceSmiling }?.score
            val leftEyeOpenProbability = face.params.find { it.typeId == FacesImageProcessor.TypeIdFaceLeftEyeOpen }?.score
            val rightEyeOpenProbability = face.params.find { it.typeId == FacesImageProcessor.TypeIdFaceRightEyeOpen }?.score

            val info = buildString {
                append("ID: ${face.trackingId} ")
                smilingProbability?.let { append("Smile: ${String.format("%.1f", it * 100)}% ") }
                leftEyeOpenProbability?.let {
                    append(
                        "L.Eye: ${
                            String.format(
                                "%.1f",
                                it * 100
                            )
                        }% "
                    )
                }
                rightEyeOpenProbability?.let {
                    append(
                        "R.Eye: ${
                            String.format(
                                "%.1f",
                                it * 100
                            )
                        }% "
                    )
                }
            }

            if (info.isNotEmpty()) {
                canvas.drawText(info, x, y, textPaint)
            }
        }

        scene.objects.filter {
            it.typeId == FacesImageProcessor.TypeIdFace
        }.forEach {
            // Draw bounding box
            drawBoundingBox(canvas, it)
            // Draw landmarks
            drawLandmarks(canvas, it)
            // Draw face information
            drawFaceInfo(canvas, it)
        }
    }

}