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
import com.google.mlkit.vision.text.Text
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
     * 撮影リスト内の全画像をML KitでOCR処理し、座標付きセル（[OcrCell]）に変換して
     * OCR結果画面に遷移する。処理はIOディスパッチャで非同期実行。
     *
     * Issue #12: 行の文字列を空白区切りで解釈する方式では、健診結果表のように
     * 1行に複数の数値が並ぶ表形式を正しく解析できないため、ML Kit の
     * `boundingBox`（座標）を保持した [OcrCell] のリストを OcrResultFragment に渡し、
     * 座標ベースのレイアウト解析（[OcrParser]）に委ねる。
     * 複数枚撮影時は画像ごとに座標系が独立するため、`page` にキャプチャ順のインデックスを持たせる。
     *
     * Issue #16: 紙面自体が回転して撮影された場合、行・列クラスタリングが成立しないため、
     * 1回目の認識結果の傾きから回転を推定し（[OcrRotationCorrector]）、必要な場合のみ
     * 画像を回転させて再認識する（[recognizeWithRotationCorrection]）。
     */
    private fun startOcrProcessing() {
        binding.btnStartOcr.isEnabled = false
        binding.btnCapture.isEnabled = false

        ocrScope.launch {
            val combinedText = StringBuilder()
            val allCells = mutableListOf<OcrCell>()
            var totalBlocks = 0
            var totalLines = 0

            capturedImages.forEachIndexed { pageIndex, imageProxy ->
                try {
                    val result = recognizeWithRotationCorrection(imageProxy)

                    totalBlocks += result.textBlocks.size
                    result.textBlocks.forEach { block ->
                        totalLines += block.lines.size
                        block.lines.forEach { line ->
                            line.elements.forEach { element ->
                                val box = element.boundingBox
                                if (box != null && element.text.isNotBlank()) {
                                    allCells.add(
                                        OcrCell(
                                            text = element.text,
                                            left = box.left,
                                            top = box.top,
                                            right = box.right,
                                            bottom = box.bottom,
                                            page = pageIndex
                                        )
                                    )
                                }
                            }
                        }
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

            // エラー評価（従来どおりテキスト量・信頼度ヒューリスティックで判定）
            val confidence = OcrAnalyzer.estimateConfidence(ocrText, totalBlocks, totalLines)
            val ocrError = OcrAnalyzer.evaluate(ocrText, totalBlocks, confidence)

            // UIスレッドで遷移
            launch(Dispatchers.Main) {
                navigateToOcrResult(allCells, ocrError)
            }
        }
    }

    /**
     * 1枚の画像に対し、必要な場合のみ紙面の回転補正を行ったうえでML Kit認識結果を返す。
     *
     * 1. カメラの向きのみ補正した画像で1回目の認識を行う（従来どおり `InputImage.fromMediaImage`。
     *    Bitmap変換のオーバーヘッドをかけない）
     * 2. 認識できた要素の `cornerPoints` から紙面の回転を推定する（[OcrRotationCorrector]）
     * 3. 回転が疑われる場合のみ、画像をBitmapに変換して回転させ、再認識する（最大1回）
     * 4. 回転前後の認識結果を文字数・行数で比較し、良い方を採用する
     *
     * 回転していない画像（1回目の認識結果が十分な場合）では2回目の認識は行われないため、
     * 処理時間は従来と変わらない。
     */
    private suspend fun recognizeWithRotationCorrection(imageProxy: ImageProxy): Text {
        val originalResult = textRecognizer.process(
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        ).await()

        val angles = originalResult.textBlocks
            .flatMap { it.lines }
            .flatMap { it.elements }
            .mapNotNull { element ->
                val corners = element.cornerPoints?.map {
                    OcrRotationCorrector.CornerPoint(it.x.toFloat(), it.y.toFloat())
                }
                corners?.let { OcrRotationCorrector.elementAngleDegrees(it) }
            }

        val elementCount = angles.size
        val charCount = originalResult.text.trim().length
        val medianAngle = OcrRotationCorrector.medianAngleDegrees(angles)
        val rotationCandidate = OcrRotationCorrector.quantizeTo90(medianAngle)

        val shouldAttempt = OcrRotationCorrector.shouldAttemptCorrection(
            rotationCandidate = rotationCandidate,
            elementCount = elementCount,
            charCount = charCount
        )
        if (!shouldAttempt) {
            return originalResult
        }

        val correctionDegrees = OcrRotationCorrector.resolveCandidateDegrees(rotationCandidate)
        val rotatedResult = try {
            val rawBitmap = ImageRotationUtils.toBitmap(imageProxy)
            // カメラの向き補正 + 紙面の回転補正候補、両方をまとめて1枚のBitmapに適用する
            val totalDegrees = imageProxy.imageInfo.rotationDegrees + correctionDegrees
            val correctedBitmap = ImageRotationUtils.rotate(rawBitmap, totalDegrees)
            textRecognizer.process(InputImage.fromBitmap(correctedBitmap, 0)).await()
        } catch (e: Exception) {
            Log.e(TAG, "回転補正のための再認識に失敗しました", e)
            null
        } ?: return originalResult

        val rotatedLineCount = rotatedResult.textBlocks.sumOf { it.lines.size }
        val originalLineCount = originalResult.textBlocks.sumOf { it.lines.size }
        val rotatedCharCount = rotatedResult.text.trim().length

        val adoptRotated = OcrRotationCorrector.shouldAdoptRotated(
            originalCharCount = charCount,
            originalLineCount = originalLineCount,
            rotatedCharCount = rotatedCharCount,
            rotatedLineCount = rotatedLineCount
        )

        return if (adoptRotated) rotatedResult else originalResult
    }

    private fun navigateToOcrResult(cells: List<OcrCell>, ocrError: OcrAnalyzer.OcrError) {
        val action = CameraFragmentDirections.actionCameraToOcrResult(
            cells.toTypedArray(),
            ocrError.name
        )
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
    }
}
