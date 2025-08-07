package kioskware.vision.landmarks.faces.ui.theme

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kioskware.vision.Rotation
import kioskware.vision.camera.BackendCamera
import kioskware.vision.camera.BackendCameras
import kioskware.vision.camera.CameraSnapshot
import kioskware.vision.camera.CameraSnapshotConfig
import kioskware.vision.camera.CameraState
import kioskware.vision.landmarks.faces.FacesImageProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class MainScreenViewModel(
    private val backendCameras: BackendCameras
) : ViewModel() {

    // Initialize PoseImageProcessor
    private val faceImageProcessor = FacesImageProcessor()

    val availableCameras = backendCameras.availableCameras

    // Remove duplicate StateFlow - use camera's bitmap directly
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState = _cameraState.asStateFlow()

    // Store camera reference to access its cameraBitmap
    private val _currentCamera = MutableStateFlow<BackendCamera?>(null)
    val currentCamera = _currentCamera.asStateFlow()

    private val _cameraSnapshot = MutableStateFlow<CameraSnapshot?>(null)
    val cameraSnapshot: StateFlow<CameraSnapshot?> = _cameraSnapshot.asStateFlow()

    init {

        viewModelScope.launch {
            cameraSnapshot.collectLatest {

            }
        }

    }


    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            // Create camera config with PoseImageProcessor
            backendCameras.getCamera("2")?.also { camera ->
                _currentCamera.value = camera
                camera.cameraSnapshotConfig.value = CameraSnapshotConfig(
                    cameraViewEnabled = true,
                    visualisationEnabled = true
                )
                // Now collect from camera's bitmap flow directly
                viewModelScope.launch {
                    camera.cameraSnapshot.collectLatest { snap ->
                        _cameraSnapshot.value = snap
                    }
                }
                camera.startCamera(
                    targetRotation = Rotation.Degrees0,
                    imageProcessors = listOf(
                        faceImageProcessor
                    ),
                    lifecycleOwner = lifecycleOwner,
                )
            }
        }
    }

    fun selectCamera(cameraId: String) {

    }

    override fun onCleared() {
        super.onCleared()
    }
}
