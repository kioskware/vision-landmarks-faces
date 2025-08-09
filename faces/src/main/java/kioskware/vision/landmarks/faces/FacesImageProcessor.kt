package kioskware.vision.landmarks.faces

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kioskware.vision.landmarks.Landmark
import kioskware.vision.landmarks.common.Point3D
import kioskware.vision.landmarks.`object`.Object
import kioskware.vision.landmarks.`object`.ObjectParam
import kioskware.vision.landmarks.scene.Scene
import kioskware.vision.landmarks.scene.SceneImageProcessor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * ImageProcessor implementation for face detection and visualization.
 * Detects faces in images and provides visualization of face landmarks and bounding boxes.
 */
class FacesImageProcessor(
    val accurate: Boolean = true,
    val detectLandmarks: Boolean = true,
    val detectClassification: Boolean = true,
    val trackingEnabled: Boolean = true,
    val minFaceSize: Float = 0.15f,
    private val visualization: suspend (scene: Scene, overlayCanvas: Canvas) -> Unit = DefaultFacesVisualisations.facesProcessor()
) : SceneImageProcessor() {

    companion object {

        val TypeIdFace = "face"
        val TypeIdFaceAngleX = "face_angle_x"
        val TypeIdFaceAngleY = "face_angle_y"
        val TypeIdFaceAngleZ = "face_angle_z"
        val TypeIdFaceSmiling = "face_smiling"
        val TypeIdFaceLeftEyeOpen = "face_left_eye_open"
        val TypeIdFaceRightEyeOpen = "face_right_eye_open"
        val TypeIdFaceLandmarkPrefix = "face_landmark_"

    }

    // ML Kit Face Detector
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(
                if (accurate) FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
                else FaceDetectorOptions.PERFORMANCE_MODE_FAST
            )
            .setLandmarkMode(
                if (detectLandmarks) FaceDetectorOptions.LANDMARK_MODE_ALL
                else FaceDetectorOptions.LANDMARK_MODE_NONE
            )
            .setClassificationMode(
                if (detectClassification) FaceDetectorOptions.CLASSIFICATION_MODE_ALL
                else FaceDetectorOptions.CLASSIFICATION_MODE_NONE
            )
            .setMinFaceSize(minFaceSize)
            .let {
                if (trackingEnabled) it.enableTracking() else it
            }
            .build()
        FaceDetection.getClient(options)
    }

    override suspend fun onProcess(
        image: Bitmap,
    ): Scene? {
        val detectedFaces = detectFaces(image, 0) ?: return null
        return createResultScene(
            image = image,
            objects = detectedFaces.map { face ->
                face.toSceneObject()
            }
        )
    }

    override suspend fun onRenderVisualization(scene: Scene, image: Bitmap, overlayCanvas: Canvas) {
        visualization(scene, overlayCanvas)
    }

    private fun Face.toSceneObject(): Object {
        return Object(
            typeId = TypeIdFace,
            trackingId = trackingId.toString(),
            bounding = boundingBox.toRectF(),
            landmarks = getFaceLandmarks(),
            params = getFaceParams()
        )
    }

    private fun Face.getFaceLandmarks(): List<Landmark> {
        return allLandmarks.map { landmark ->
            Landmark(
                typeId = TypeIdFaceLandmarkPrefix + landmark.landmarkType,
                location = Point3D(
                    x = landmark.position.x,
                    y = landmark.position.y,
                    z = Float.NaN // ML Kit does not provide z-coordinate for face landmarks
                ),
                score = 1f
            )
        }
    }

    private fun Face.getFaceParams() : List<ObjectParam<*>> {

        fun Float?.toBoolean(): Boolean {
            return this != null && this > 0.5f
        }

        return listOfNotNull(
            ObjectParam(TypeIdFaceAngleX, headEulerAngleX),
            ObjectParam(TypeIdFaceAngleY, headEulerAngleY),
            ObjectParam(TypeIdFaceAngleZ, headEulerAngleZ),

            smilingProbability?.let {
                ObjectParam(TypeIdFaceSmiling, it.toBoolean(), it)
            },
            leftEyeOpenProbability?.let {
                ObjectParam(TypeIdFaceLeftEyeOpen, it.toBoolean(), it)
            },
            rightEyeOpenProbability?.let {
                ObjectParam(TypeIdFaceRightEyeOpen, it.toBoolean(), it)
            }
        )
    }

    private suspend fun detectFaces(
        image: Bitmap,
        rotationDegrees: Int
    ): List<Face>? = suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromBitmap(image, rotationDegrees)
        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                continuation.resume(faces)
            }
            .addOnFailureListener { exception ->
                continuation.resume(null)
            }
    }

    override suspend fun release() {
        faceDetector.close()
        super.release()
    }

}