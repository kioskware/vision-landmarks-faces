package kioskware.vision.landmarks.faces.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import kioskware.vision.impl.defaultBackendCameras
import kioskware.vision.landmarks.faces.ui.theme.componets.CameraPreview

@Composable
internal fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: MainScreenViewModel = remember {
        MainScreenViewModel(defaultBackendCameras(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    // Inicjalizuj kamerę po utworzeniu komponentu
    LaunchedEffect(viewModel) {
        viewModel.initializeCamera(lifecycleOwner)
    }
    Box(modifier = modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            snapshotProvider = viewModel.cameraSnapshot,
            cameraState = viewModel.cameraState
        )
    }
}
