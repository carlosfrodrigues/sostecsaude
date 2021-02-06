package com.wwmm.sostecsaude.ui.unidade_saude

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.getEstadoString
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_unidade_saude_relatorio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream

class Relatorio : Fragment() {
    private lateinit var mController: NavController
    private lateinit var mQueue: RequestQueue
    private lateinit var mPrefs: SharedPreferences
    private var mProcessing = false
    private var mHavePermissions = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var mListaEquipamentos = JSONArray()
    private var mListaStatus = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unidade_saude_relatorio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mController = findNavController()

        mPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        mQueue = Volley.newRequestQueue(requireContext())

        criarRelatorio()

        button_salvar_relatorio.setOnClickListener {
            if (mHavePermissions) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)

                    type = "*/*"

                    putExtra(Intent.EXTRA_TITLE, "relatorio.pdf")
                }

                startActivityForResult(intent, SAVE_PDF)
            } else {
                Snackbar.make(
                    layout_unidade_saude_relatorio,
                    "Sem permissão para salvar arquivos!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun criarRelatorio() {
        if (!mProcessing) {
            table_layout_status_global.removeAllViews()

            getEquipamentos()
        }
    }

    private fun getEquipamentos() {
        progressBar.visibility = View.VISIBLE
        mProcessing = true

        val token = mPrefs.getString("Token", "")!!

        val jsonToken = JSONArray()

        jsonToken.put(0, token)

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/v2/unidade_saude_pegar_equipamentos",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0) {
                        if (response[0] == "invalid_token" || response[0] == "perfil_invalido") {
                            mController.navigate(R.id.action_global_fazerLogin)
                        } else {
                            mListaEquipamentos = response

                            getEstadoManutencao()
                        }
                    }

                    progressBar.visibility = View.GONE
                    mProcessing = false
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")

                if (isAdded) {
                    progressBar.visibility = View.GONE
                }

                mProcessing = false

                connectionErrorMessage(layout_unidade_saude_relatorio, it)
            }
        )

        mQueue.add(request)
    }

    private fun getEstadoManutencao() {
        progressBar.visibility = View.VISIBLE
        mProcessing = true

        val token = mPrefs.getString("Token", "")!!

        val jsonToken = JSONArray()

        jsonToken.put(0, token)

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/get_estado_equipamentos",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0) {
                        if (response[0] == "invalid_token" || response[0] == "perfil_invalido") {
                            mController.navigate(R.id.action_global_fazerLogin)
                        } else {
                            mListaStatus = response
                        }
                    }

                    createGlobalBarChart()
                    createGlobalTable()

                    progressBar.visibility = View.GONE
                    mProcessing = false
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")

                if (isAdded) {
                    progressBar.visibility = View.GONE
                }

                mProcessing = false

                connectionErrorMessage(layout_unidade_saude_relatorio, it)
            }
        )

        mQueue.add(request)
    }

    private fun createGlobalBarChart() {
        val countState = arrayOf(
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
        )

        if (mListaStatus.length() == 0) { // não há nenhuma oferta de manutenção
            countState[0] = mListaEquipamentos.length().toFloat()
        } else {
            for (n in 0 until mListaEquipamentos.length()) {
                val json = mListaEquipamentos[n] as JSONObject
                val idEquipamento = json.getString("id")
                var status = -1

                for (j in 0 until mListaStatus.length()) {
                    val jsonStatus = mListaStatus[j] as JSONObject
                    val id = jsonStatus.getInt("id")

                    if (id == idEquipamento.toInt()) {
                        status = jsonStatus.getInt("estado")

                        break
                    }
                }

                if (status == -1) {
                    countState[0]++
                } else {
                    countState[status]++
                }
            }
        }

        val labels = arrayOf(
            "sem oferta", "aguardando aceite", "aceito", "pronto para retirada",
            "retirado", "recebido", "triagem", "manutenção", "higienização", "saiu para entrega",
            "entregue"
        )

        val values = ArrayList<BarEntry>()

        values.add(BarEntry(0.0f, countState[0]))
        values.add(BarEntry(1.0f, countState[1]))
        values.add(BarEntry(2.0f, countState[2]))
        values.add(BarEntry(3.0f, countState[3]))
        values.add(BarEntry(4.0f, countState[4]))
        values.add(BarEntry(5.0f, countState[5]))
        values.add(BarEntry(6.0f, countState[6]))
        values.add(BarEntry(7.0f, countState[7]))
        values.add(BarEntry(8.0f, countState[8]))
        values.add(BarEntry(9.0f, countState[9]))
        values.add(BarEntry(10.0f, countState[10]))

        val dataSet = BarDataSet(values, "")

        val myColors = mutableListOf(
            Color.rgb(207, 248, 246), Color.rgb(148, 212, 212),
            Color.rgb(136, 180, 187), Color.rgb(118, 174, 175),
            Color.rgb(42, 109, 130)
        )

        dataSet.colors = myColors
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)

        bar_chart_global.data = data
        bar_chart_global.description = null
        bar_chart_global.legend.isEnabled = false

        bar_chart_global.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        bar_chart_global.xAxis.position = XAxis.XAxisPosition.BOTTOM
        bar_chart_global.xAxis.labelCount = labels.size
        bar_chart_global.xAxis.setDrawGridLines(false)
        bar_chart_global.xAxis.setDrawAxisLine(false)

        bar_chart_global.axisRight.setDrawLabels(false)
        bar_chart_global.axisRight.setDrawGridLines(false)
        bar_chart_global.axisRight.setDrawAxisLine(false)

        bar_chart_global.axisLeft.setDrawLabels(false)
        bar_chart_global.axisLeft.setDrawGridLines(false)
        bar_chart_global.axisLeft.setDrawAxisLine(false)

        bar_chart_global.invalidate()
    }

    private fun createGlobalTable() {
        val layoutParams = TableLayout.LayoutParams()

        layoutParams.setMargins(0, 5, 0, 5)

        // column headers

        var row = TableRow(requireContext())

        row.layoutParams = layoutParams

        row.addView(buildField("unidade", Color.WHITE, 14, Gravity.CENTER, false))
        row.addView(buildField("equipamento", Color.LTGRAY, 14, Gravity.CENTER, false))
        row.addView(buildField("nome", Color.GRAY, 14, Gravity.CENTER, false))
        row.addView(buildField("modelo", Color.LTGRAY, 14, Gravity.CENTER, false))
        row.addView(buildField("fabricante", Color.GRAY, 14, Gravity.CENTER, false))
        row.addView(buildField("número de série", Color.LTGRAY, 14, Gravity.CENTER, false))
        row.addView(buildField("status de manutenção", Color.LTGRAY, 14, Gravity.CENTER, false))

        row.gravity = Gravity.START

        table_layout_status_global.addView(row)

        // linhas

        for (n in 0 until mListaEquipamentos.length()) {
            val json = mListaEquipamentos[n] as JSONObject

            val unidade = json.getString("unidade")
            val equipamento = "EQ${json.getString("id")}"
            val nome = json.getString("nome")
            val fabricante = json.getString("fabricante")
            val modelo = json.getString("modelo")
            val numeroSerie = json.getString("numeroSerie")

            row = TableRow(requireContext())

            row.layoutParams = layoutParams
            row.gravity = Gravity.START

            row.addView(buildField(unidade, Color.WHITE, 14, Gravity.CENTER, false))
            row.addView(buildField(equipamento, Color.WHITE, 14, Gravity.CENTER, false))
            row.addView(buildField(nome, Color.WHITE, 14, Gravity.CENTER, false))
            row.addView(buildField(modelo, Color.WHITE, 14, Gravity.CENTER, false))
            row.addView(buildField(fabricante, Color.WHITE, 14, Gravity.CENTER, false))
            row.addView(buildField(numeroSerie, Color.WHITE, 14, Gravity.CENTER, false))

            val idEquipamento = json.getString("id")
            var status = -1

            for (j in 0 until mListaStatus.length()) {
                val jsonStatus = mListaStatus[j] as JSONObject
                val id = jsonStatus.getInt("id")

                if (id == idEquipamento.toInt()) {
                    status = jsonStatus.getInt("estado")

                    break
                }
            }

            if (status == -1) {
                row.addView(
                    buildField(
                        "Sem ofertas de reparo", Color.WHITE, 14,
                        Gravity.CENTER, false
                    )
                )
            } else {
                row.addView(
                    buildField(
                        getEstadoString(status), Color.WHITE, 14,
                        Gravity.CENTER, false
                    )
                )
            }

            table_layout_status_global.addView(row)
        }
    }

    private fun buildField(
        text: String,
        color: Int,
        size: Int,
        gravity: Int,
        caps: Boolean
    ): TextView {
        val tv = TextView(requireContext())

        tv.gravity = gravity

        tv.textSize = size.toFloat()

        tv.setPadding(20, 5, 20, 5)

        tv.setBackgroundColor(color)

        tv.isAllCaps = caps
        tv.text = text

        return tv
    }

    private fun exportPdf(outputStream: OutputStream) {
        val document = PdfDocument()

        // primeira página

        var pageInfo = PdfDocument.PageInfo.Builder(
            linear_layout_bar_chart_global.width,
            linear_layout_bar_chart_global.height,
            1
        ).create()

        var page = document.startPage(pageInfo)

        linear_layout_bar_chart_global.draw(page.canvas)

        document.finishPage(page)

        // segunda página

        pageInfo = PdfDocument.PageInfo.Builder(
            linear_layout_table_global.width,
            linear_layout_table_global.height,
            1
        ).create()

        page = document.startPage(pageInfo)

        linear_layout_table_global.draw(page.canvas)

        document.finishPage(page)

        // finalizar documento

        document.writeTo(outputStream)

        document.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty()) {
            mHavePermissions = if (requestCode == REQUEST_PERMISSION_CODE) {
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            } else {
                Log.e(LOGTAG, "no permission to write to the sdcard!")

                false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == SAVE_PDF && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                GlobalScope.launch(Dispatchers.IO) {
                    val os = requireContext().contentResolver.openOutputStream(uri)

                    if (os != null) {
                        exportPdf(os)
                    }
                }

            }
        }
    }

    companion object {
        const val LOGTAG = "Relatorio"
        const val REQUEST_PERMISSION_CODE = 1
        const val SAVE_PDF = 2
    }
}
