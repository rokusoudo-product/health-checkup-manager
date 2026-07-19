package com.rokusoudo.healthcheckup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.rokusoudo.healthcheckup.databinding.FragmentCameraBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    // 撮影した画像プロキシのリスト（複数枚対応）
    private val capturedImages: MutableList<ImageProxy> = mutableListOf()

    // OCR処理用コルーチンスコープ
    private val ocrScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ML Kit Japanese Text Recognizer
    private val textRecognizer by lazy {
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }

    // カメラパーミッションリクエスト
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "カメラのパーミッションが必要です", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        applyBottomControlsInsets()

        // パーミッション確認
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        binding.btnStartOcr.setOnClickListener {
            if (capturedImages.isNotEmpty()) {
                startOcrProcessing()
            }
        }

        updateCapturedCountUI()
    }

    /**
     * targetSdk 35 の edge-to-edge 強制によりシャッター・OCR開始ボタンがナビゲーションバーに
     * 被るため、ナビゲーションバー分を bottom padding に加算する。
     */
    private fun applyBottomControlsInsets() {
        val initialBottomPadding = binding.bottomControls.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomControls) { view, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = initialBottomPadding + navBarInsets.bottom)
            insets
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "カメラの起動に失敗しました", e)
                Toast.makeText(requireContext(), "カメラの起動に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    capturedImages.add(image)
                    updateCapturedCountUI()
                    Log.d(TAG, "撮影成功: ${capturedImages.size}枚目")
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "撮影失敗", exception)
                    Toast.makeText(requireContext(), "撮影に失敗しました", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun updateCapturedCountUI() {
        val count = capturedImages.size
        binding.tvCapturedCount.text = getString(R.string.label_captured_count, count)
        binding.btnStartOcr.visibility = if (count > 0) View.VISIBLE else View.GONE
        binding.btnStartOcr.text = getString(R.string.btn_start_ocr, count)
    }

    /**
     * 撮影リスト内の全画像をML KitでOCR処理し、テキストを結合してOCR結果画面に遷移する。
     * 処理はIOディスパッチャで非同期実行。
     */
    private fun startOcrProcessing() {
        binding.btnStartOcr.isEnabled = false
        binding.btnCapture.isEnabled = false

        ocrScope.launch {
            val combinedText = StringBuilder()
            var totalBlocks = 0
            var totalLines = 0

            for (imageProxy in capturedImages) {
                try {
                    val inputImage = InputImage.fromMediaImage(
                        imageProxy.image!!,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    val result = textRecognizer.process(inputImage).await()

                    totalBlocks += result.textBlocks.size
                    result.textBlocks.forEach { block ->
                        totalLines += block.lines.size
                    }
                    combinedText.append(result.text).append("\n")
                } catch (e: Exception) {
                    Log.e(TAG, "OCR処理エラー", e)
                } finally {
                    imageProxy.close()
                }
            }
            capturedImages.clear()

            val ocrText = combinedText.toString().trim()

            // エラー評価
            val confidence = OcrAnalyzer.estimateConfidence(ocrText, totalBlocks, totalLines)
            val ocrError = OcrAnalyzer.evaluate(ocrText, totalBlocks, confidence)

            // UIスレッドで遷移
            launch(Dispatchers.Main) {
                navigateToOcrResult(ocrText, ocrError)
            }
        }
    }

    private fun navigateToOcrResult(ocrText: String, ocrError: OcrAnalyzer.OcrError) {
        // OcrResultFragment へ結果を渡す
        // エラー情報はOCRテキストに特殊プレフィクスを付与して伝達する
        // （Safe Args 生成クラスは NavGraph の argument 定義に基づく）
        val errorPrefix = when (ocrError) {
            OcrAnalyzer.OcrError.INSUFFICIENT_TEXT -> ERROR_PREFIX_INSUFFICIENT
            OcrAnalyzer.OcrError.LOW_CONFIDENCE -> ERROR_PREFIX_LOW_CONFIDENCE
            OcrAnalyzer.OcrError.NONE -> ""
        }
        val payload = errorPrefix + ocrText

        val action = CameraFragmentDirections.actionCameraToOcrResult(payload)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        capturedImages.forEach { it.close() }
        capturedImages.clear()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        ocrScope.cancel()
        textRecognizer.close()
    }

    companion object {
        private const val TAG = "CameraFragment"
        const val ERROR_PREFIX_INSUFFICIENT = "##ERROR_INSUFFICIENT##"
        const val ERROR_PREFIX_LOW_CONFIDENCE = "##ERROR_LOW_CONFIDENCE##"
    }
}
