package kioskware.vision.landmarks.faces.ui.theme.componets

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kioskware.vision.camera.CameraSnapshot
import kioskware.vision.camera.CameraState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun CameraPreview(
    modifier: Modifier = Modifier,
    snapshotProvider: Flow<CameraSnapshot?>,
    cameraState: StateFlow<CameraState>
) {
    val currentSnap by snapshotProvider.collectAsState(null)

    @Composable
    fun ImageFromBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Camera preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Inside
            )
        }
    }

    currentSnap?.let { snap ->
        Box(modifier = modifier.fillMaxSize()) {
            ImageFromBitmap(snap.contentBitmap)
            ImageFromBitmap(snap.overlayBitmap)
        }
    }
}