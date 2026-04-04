package com.rokusodo.healthcheckup.ui.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.rokusodo.healthcheckup.HealthCheckupApp
import com.rokusodo.healthcheckup.R
import com.rokusodo.healthcheckup.data.db.dao.ItemTrend
import androidx.core.content.ContextCompat
import com.rokusodo.healthcheckup.databinding.FragmentTrendGraphBinding
import kotlinx.coroutines.launch

/**
 * 経年グラフ画面。
 * 指定した検査項目の経年変化を折れ線グラフで表示する。
 * 薬事法対応: グラフタイトルは項目名と単位のみ。「正常」「異常」などの判定テキストなし。
 */
class TrendGraphFragment : Fragment() {

    private var _binding: FragmentTrendGraphBinding? = null
    private val binding get() = _binding!!

    private val args: TrendGraphFragmentArgs by navArgs()

    private val viewModel: TrendGraphViewModel by viewModels {
        val app = requireActivity().application as HealthCheckupApp
        TrendGraphViewModel.Factory(app.repository, args.itemName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrendGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeTrends()
    }

    private fun observeTrends() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trends.collect { trends ->
                    renderGraph(trends)
                }
            }
        }
    }

    private fun renderGraph(trends: List<ItemTrend>) {
        // 数値変換できるデータのみ使用
        val validTrends = trends.mapNotNull { trend ->
            val numericValue = trend.value.toDoubleOrNull() ?: return@mapNotNull null
            Pair(trend, numericValue)
        }

        if (validTrends.isEmpty()) {
            binding.lineChart.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }

        binding.lineChart.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE

        // グラフタイトル（薬事法対応: 項目名と単位のみ）
        val unit = validTrends.first().first.unit
        val titleText = if (unit.isNotBlank()) {
            "${viewModel.itemName} ($unit)"
        } else {
            viewModel.itemName
        }
        binding.tvGraphTitle.text = titleText

        // Entryリストを作成
        val entries = validTrends.mapIndexed { index, (_, value) ->
            Entry(index.toFloat(), value.toFloat())
        }
        val dates = validTrends.map { it.first.recordDate }

        val dataSet = LineDataSet(entries, viewModel.itemName).apply {
            color = requireContext().getColor(android.R.color.holo_blue_dark)
            setCircleColor(requireContext().getColor(android.R.color.holo_blue_dark))
            valueTextSize = 10f
            lineWidth = 2f
            circleRadius = 4f
        }

        // X軸ラベルを受診日にする
        binding.lineChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in dates.indices) dates[idx] else ""
                }
            }
            granularity = 1f
            labelRotationAngle = -45f
            labelCount = dates.size
        }

        // 基準値ラインを追加（薬事法対応: ラベルなし）
        binding.lineChart.axisLeft.removeAllLimitLines()
        val refMin = validTrends.first().first.referenceMin
        val refMax = validTrends.first().first.referenceMax

        if (refMin != null) {
            val limitLine = LimitLine(refMin.toFloat()).apply {
                lineColor = ContextCompat.getColor(requireContext(), R.color.abnormal_color)
                lineWidth = 1.5f
                enableDashedLine(10f, 5f, 0f)
            }
            binding.lineChart.axisLeft.addLimitLine(limitLine)
        }
        if (refMax != null) {
            val limitLine = LimitLine(refMax.toFloat()).apply {
                lineColor = ContextCompat.getColor(requireContext(), R.color.abnormal_color)
                lineWidth = 1.5f
                enableDashedLine(10f, 5f, 0f)
            }
            binding.lineChart.axisLeft.addLimitLine(limitLine)
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
