package com.lemonview.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.lemonview.ai.model.SkinResult
import com.lemonview.ai.utils.FaceLandmarkOverlay
import com.lemonview.ai.utils.FaceLandmarkProcessor
import com.lemonview.ai.utils.RoutinePlanGenerator
import com.lemonview.ai.utils.SkinAnalysisProcessor
import com.lemonview.ai.utils.SkinDataStore
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.media.Image as AndroidImage

@OptIn(ExperimentalGetImage::class)
class SkinAnalysisActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnAnalyze: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var faceLandmarkOverlay: FaceLandmarkOverlay
    private lateinit var faceStatusText: TextView
    
    // Metrics UI elements
    private lateinit var qualityScore: TextView
    private lateinit var landmarkCount: TextView
    private lateinit var angleX: TextView
    private lateinit var angleY: TextView
    private lateinit var eyesStatus: TextView

    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    
    private lateinit var skinProcessor: SkinAnalysisProcessor
    private lateinit var dataStore: SkinDataStore
    private lateinit var routineGenerator: RoutinePlanGenerator
    private lateinit var landmarkProcessor: FaceLandmarkProcessor
    
    private var analysisMode = "skin_analysis"  // or "makeup_advisor"
    private var faceDetected = false
    private var currentFaceMetrics: FaceLandmarkProcessor.FaceMetrics? = null

    private val CAMERA_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skin_analysis)

        analysisMode = intent.getStringExtra("mode") ?: "skin_analysis"

        previewView = findViewById(R.id.previewView)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        faceLandmarkOverlay = findViewById(R.id.faceLandmarkOverlay)
        faceStatusText = findViewById(R.id.faceStatusText)
        
        // Initialize metrics UI elements
        qualityScore = findViewById(R.id.qualityScore)
        landmarkCount = findViewById(R.id.landmarkCount)
        angleX = findViewById(R.id.angleX)
        angleY = findViewById(R.id.angleY)
        eyesStatus = findViewById(R.id.eyesStatus)

        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Initialize local ML processor and data store
        skinProcessor = SkinAnalysisProcessor(this)
        dataStore = SkinDataStore(this)
        routineGenerator = RoutinePlanGenerator()
        landmarkProcessor = FaceLandmarkProcessor()

        btnBack.setOnClickListener { finish() }

        btnAnalyze.setOnClickListener {
            capturePhoto()
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )
        }
    }

    // =====================================================
    // CAMERA PREVIEW WITH FACE DETECTION
    // =====================================================
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

            // Setup face detection analyzer
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        detectFaceLandmarks(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    // =====================================================
    // FACE LANDMARK DETECTION
    // =====================================================
    private fun detectFaceLandmarks(imageProxy: ImageProxy) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)

        try {
            val image = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        faceDetected = true
                        val face = faces[0]
                        
                        // Extract landmark metrics
                        currentFaceMetrics = landmarkProcessor.extractLandmarks(face)
                        
                        // Get positioning feedback
                        val feedback = landmarkProcessor.getPositioningFeedback(currentFaceMetrics!!)
                        val qualityScoreValue = landmarkProcessor.calculateQualityScore(currentFaceMetrics!!)
                        
                        // Update status text with feedback and quality score
                        faceStatusText.text = feedback
                        faceStatusText.setTextColor(
                            when {
                                qualityScoreValue >= 80f -> android.graphics.Color.parseColor("#00FF00")
                                qualityScoreValue >= 60f -> android.graphics.Color.parseColor("#FFD700")
                                else -> android.graphics.Color.parseColor("#FF6600")
                            }
                        )
                        
                        // Enable/disable capture button based on readiness
                        btnAnalyze.isEnabled = feedback == "Perfect! Ready to capture"
                        btnAnalyze.alpha = if (btnAnalyze.isEnabled) 1.0f else 0.5f
                        
                        // Update metrics display on UI thread
                        runOnUiThread {
                            qualityScore.text = String.format("%.0f%%", qualityScoreValue)
                            qualityScore.setTextColor(
                                when {
                                    qualityScoreValue >= 80f -> android.graphics.Color.parseColor("#00FF00")
                                    qualityScoreValue >= 60f -> android.graphics.Color.parseColor("#FFD700")
                                    else -> android.graphics.Color.parseColor("#FF6600")
                                }
                            )
                            
                            landmarkCount.text = "${currentFaceMetrics!!.totalLandmarks}"
                            angleX.text = String.format("%.1f°", currentFaceMetrics!!.headRotationX)
                            angleY.text = String.format("%.1f°", currentFaceMetrics!!.headRotationY)
                            
                            val avgEyeOpen = (currentFaceMetrics!!.leftEyeOpenProbability + currentFaceMetrics!!.rightEyeOpenProbability) / 2f
                            eyesStatus.text = String.format("%.0f%%", avgEyeOpen * 100f)
                            eyesStatus.setTextColor(
                                if (avgEyeOpen > 0.5f) android.graphics.Color.parseColor("#00FF00")
                                else android.graphics.Color.parseColor("#FF6600")
                            )
                        }
                        
                        // Calculate scale and offset for preview display
                        val previewWidth = previewView.width.toFloat()
                        val previewHeight = previewView.height.toFloat()
                        val scaleX = previewWidth / image.width.toFloat()
                        val scaleY = previewHeight / image.height.toFloat()
                        
                        // Update overlay with face and landmarks
                        faceLandmarkOverlay.setFaces(
                            faces,
                            scaleX = scaleX,
                            scaleY = scaleY
                        )
                    } else {
                        faceDetected = false
                        currentFaceMetrics = null
                        faceStatusText.text = "얼굴을 감지하지 못했습니다 - 얼굴을 정렬하세요"
                        faceStatusText.setTextColor(android.graphics.Color.parseColor("#FFD700"))
                        faceLandmarkOverlay.setFaces(emptyList())
                        
                        // Disable capture button when no face detected
                        btnAnalyze.isEnabled = false
                        btnAnalyze.alpha = 0.5f
                        
                        // Clear metrics display
                        runOnUiThread {
                            qualityScore.text = "--"
                            landmarkCount.text = "--"
                            angleX.text = "--"
                            angleY.text = "--"
                            eyesStatus.text = "--"
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    // Error message hidden - keep previous status text
                    // faceStatusText.text = "감지 오류 - 다시 시도하세요"
                    // faceStatusText.setTextColor(android.graphics.Color.RED)
                }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    // =====================================================
    // CAPTURE IMAGE
    // =====================================================
    private fun capturePhoto() {
        android.util.Log.d("SkinAnalysis", "=== capturePhoto() CALLED ===")

        val capture = imageCapture ?: return

        android.util.Log.d("SkinAnalysis", "✓ imageCapture is available")
        progressBar.visibility = View.VISIBLE
        btnAnalyze.isEnabled = false

        val photoFile = File(
            externalCacheDir,
            "skin_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                override fun onError(exception: ImageCaptureException) {
                    progressBar.visibility = View.GONE
                    btnAnalyze.isEnabled = true
                    Toast.makeText(
                        this@SkinAnalysisActivity,
                        "사진 촬영 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // ✅ ANALYZE IMAGE LOCALLY USING ML MODEL
                    analyzeImageLocally(photoFile)
                }
            }
        )
    }

    // =====================================================
    // LOCAL SKIN ANALYSIS WITH ML MODEL
    // =====================================================
    private fun analyzeImageLocally(imageFile: File) {
        android.util.Log.d("SkinAnalysis", "=== LOCAL ML ANALYSIS STARTED ===")
        
        try {
            progressBar.visibility = View.VISIBLE
            btnAnalyze.isEnabled = false
            
            // Load the captured image bitmap
            android.util.Log.d("SkinAnalysis", "Step 1: Loading image from file: ${imageFile.absolutePath}")
            val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            
            if (imageBitmap == null) {
                android.util.Log.e("SkinAnalysis", "✗ Failed to decode bitmap from file")
                throw Exception("Unable to load image")
            }
            
            android.util.Log.d("SkinAnalysis", "✓ Image loaded: ${imageBitmap.width}x${imageBitmap.height}")
            
            // Step 2: Use LOCAL ML models for analysis (TensorFlow + ONNX)
            android.util.Log.d("SkinAnalysis", "Step 2: Running local ML analysis (TensorFlow + ONNX)...")
            
            val skinResult = skinProcessor.analyzeSkin(imageBitmap)
            
            android.util.Log.d("SkinAnalysis", "✓ ML Analysis completed successfully")
            android.util.Log.d("SkinAnalysis", "✓ Health: ${skinResult.skinHealthPercentage}% | Tone: ${skinResult.skinTone}")
            android.util.Log.d("SkinAnalysis", "✓ Diseases detected: ${skinResult.diseasesLevel.count { it.value > 0 }} conditions")
            
            // Step 3: Save the result for the routine planner
            val dataStore = SkinDataStore(this@SkinAnalysisActivity)
            dataStore.saveSkinResult(skinResult)
            android.util.Log.d("SkinAnalysis", "✓ Result saved to DataStore")
            
            // Step 4: Generate routine plan based on ML analysis
            val routineGen = RoutinePlanGenerator()
            val routinePlan = routineGen.generateRoutinePlan(skinResult)
            dataStore.saveRoutinePlan(routinePlan)
            android.util.Log.d("SkinAnalysis", "✓ Routine plan generated and saved")
            
            // Step 5: Navigate to results
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true
                
                if (analysisMode == "makeup_advisor") {
                    android.util.Log.d("SkinAnalysis", "✓ Navigating to MakeupAdvisorActivity...")
                    val makeupIntent = Intent(this@SkinAnalysisActivity, MakeupAdvisorActivity::class.java)
                    makeupIntent.putExtra(MakeupAdvisorActivity.EXTRA_IMAGE_PATH, imageFile.absolutePath)
                    startActivity(makeupIntent)
                } else {
                    android.util.Log.d("SkinAnalysis", "✓ Navigating to SkinResultActivity...")
                    val skinIntent = Intent(this@SkinAnalysisActivity, SkinResultActivity::class.java)
                    skinIntent.putExtra("image_path", imageFile.absolutePath)
                    skinIntent.putExtra("skin_result", skinResult as java.io.Serializable)
                    startActivity(skinIntent)
                }
                
                finish()
                android.util.Log.d("SkinAnalysis", "=== LOCAL ML ANALYSIS COMPLETED SUCCESSFULLY ===")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SkinAnalysis", "✗ ERROR during analysis: ${e.message}", e)
            e.printStackTrace()
            
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnAnalyze.isEnabled = true
                Toast.makeText(
                    this@SkinAnalysisActivity,
                    "분석 오류: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        skinProcessor.release()
    }
}

