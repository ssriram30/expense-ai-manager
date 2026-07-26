package com.expenseai.manager.presentation.ocr

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.common.InputImage
import com.expenseai.manager.presentation.components.LoadingIndicator
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OCRScanScreen(
    onBack: () -> Unit,
    onResultReady: () -> Unit,
    viewModel: OCRViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    var captureCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val image = InputImage.fromFilePath(context, uri)
            viewModel.processImage(image, uri)
        }
    }

    LaunchedEffect(state.result) {
        if (state.result != null) {
            // Pass OCR result to AddEditExpenseScreen via shared state or navigation
            // For now, navigate to add expense screen
            onResultReady()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            cameraPermission.status.isGranted -> {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onImageCaptured = { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            viewModel.processImage(image)
                        }
                        imageProxy.close()
                    },
                    onCaptureReady = { captureCallback = it }
                )

                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape)) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Text("Scan Receipt", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape)) {
                        Icon(Icons.Default.Photo, "Gallery", tint = Color.White)
                    }
                }

                // Scan frame overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp, 200.dp)
                            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                    )
                    Text(
                        "Position receipt within frame",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Bottom controls
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).navigationBarsPadding().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(color = Color.White)
                        Text("Extracting receipt details...", color = Color.White)
                    } else {
                        FloatingActionButton(
                            onClick = { captureCallback?.invoke() },
                            modifier = Modifier.size(72.dp),
                            containerColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Camera, "Capture", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                        Text("Tap to capture", color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }

                state.error?.let { error ->
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        action = { TextButton(onClick = viewModel::clearResult) { Text("Dismiss") } }
                    ) { Text(error) }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("Camera Permission Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "We need camera access to scan receipts and automatically extract expense details.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { cameraPermission.launchPermissionRequest() }) { Text("Grant Permission") }
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) { Text("Use Gallery Instead") }
                    TextButton(onClick = onBack) { Text("Go Back") }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (ImageProxy) -> Unit,
    onCaptureReady: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    LaunchedEffect(Unit) {
        onCaptureReady {
            imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) { onImageCaptured(image) }
                override fun onError(exception: ImageCaptureException) {}
            })
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}
