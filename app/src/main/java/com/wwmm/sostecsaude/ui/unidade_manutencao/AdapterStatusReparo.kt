package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.format.DateFormat
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.getEstadoString
import com.wwmm.sostecsaude.myServerURL
import com.wwmm.sostecsaude.ui.unidade_saude.VerOfertas
import kotlinx.android.synthetic.main.recyclerview_unidade_manutencao_status_reparo.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class AdapterStatusReparo(
    private val lines: JSONArray,
    context: Context,
    statusReparo: StatusReparoInterface
) :
    RecyclerView.Adapter<AdapterStatusReparo.ViewHolder>(), Filterable {
    private var mFilterArray = lines
    private val mContext = context
    private val mStatusReparo = statusReparo

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private fun getEstadoColor(estado: Int): Int {
        return when (estado) {
            0 -> ContextCompat.getColor(mContext, R.color.colorEstado0)
            1 -> ContextCompat.getColor(mContext, R.color.colorEstado1)
            2 -> ContextCompat.getColor(mContext, R.color.colorEstado1)
            3 -> ContextCompat.getColor(mContext, R.color.colorEstado1)
            9 -> ContextCompat.getColor(mContext, R.color.colorEstado1)
            else -> ContextCompat.getColor(mContext, R.color.colorEstado2)
        }
    }

    private fun getDateStr(timestamp: Int): String {
        if (timestamp == 0) return ""

        return DateFormat.format("dd/MM/yyyy", timestamp.toLong() * 1000).toString()
    }

    data class ButtonState(
        val cancelVisible: Boolean,
        val cancelText: String,
        val okVisible: Boolean,
        val okText: String
    )

    private fun getButtonStates(state: Int): ButtonState {
        return when (state) {
            0 -> ButtonState(
                false,
                "",
                false,
                ""
            )
            1 -> ButtonState(
                false,
                "",
                false,
                ""
            )
            2 -> ButtonState(
                false,
                "",
                false,
                ""
            )
            3 -> ButtonState(
                false,
                "",
                true,
                "Recebido"
            )
            4 -> ButtonState(
                true,
                "Não recebido",
                true,
                "Em triagem"
            )
            5 -> ButtonState(
                true,
                "Não em triagem",
                true,
                "Em manutenção"
            )
            6 -> ButtonState(
                true,
                "Triagem",
                true,
                "Em higienização"
            )
            7 -> ButtonState(
                true,
                "Manutenção",
                true,
                "Sair para entrega"
            )
            8 -> ButtonState(
                true,
                "Não saiu para entrega",
                false,
                ""
            )
            else -> ButtonState(false, "", false, "")
        }
    }

    // Extension function to show toast message
    private fun toast(message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun mudaEstado(ofertaId: Int, estadoTo: Int) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
        val token = prefs.getString("Token", "")!!
        val jsonArray = JSONArray()

        jsonArray.put(0, token)
        jsonArray.put(1, ofertaId.toString())
        jsonArray.put(2, estadoTo.toString())

        val queue = Volley.newRequestQueue(mContext)

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/v2/altera_estado_oferta",
            jsonArray,
            Response.Listener { response ->
                if (!response.getBoolean(0)) {
                    toast("Erro: ${response.getString(1)}")
                } else {
                    mStatusReparo.changeListItemState(ofertaId, estadoTo)
                }
            },
            Response.ErrorListener {
                Log.d(VerOfertas.LOGTAG, "failed request: $it")
                toast("Erro: $it")
            }
        )

        queue.add(request)
    }

    private fun confirmChangeState(
        id: Int,
        stateTo: Int,
        title: String,
        message: String,
        btnPositive: String = "Sim",
        btnNegative: String? = null,
        btnNeutral: String? = null
    ) {
        val builder = AlertDialog.Builder(mContext)

        builder.setTitle(title)
        builder.setMessage(message)

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> mudaEstado(id, stateTo)
            }
        }

        builder.setPositiveButton(btnPositive, dialogClickListener)

        if (btnNegative != null) builder.setNegativeButton(btnNegative, dialogClickListener)

        if (btnNeutral != null) builder.setNeutralButton(btnNeutral, dialogClickListener)

        builder.create().show()
    }

    private fun onOKPress(view: View, id: Int, state: Int) {
        when (state) {
            4 -> {
                mudaEstado(id, state)
            }
            5 -> {
                mudaEstado(id, state)
            }
            6 -> {
                mudaEstado(id, state)
            }
            7 -> {
                mudaEstado(id, state)
            }
            8 -> {
                confirmChangeState(
                    id,
                    state,
                    "Equipamento saiu para entrega?",
                    "A unidade de saúde será notificada.",
                    "Sim",
                    "Não"
                )
            }
            else -> return
        }
    }

    private fun onCancelPress(view: View, id: Int, state: Int) {
        when (state) {
            3 -> {
                confirmChangeState(
                    id,
                    state,
                    "Equipamento não recebido?",
                    "Isso indicará que o equipamento ainda não foi recebido.",
                    "Não recebido",
                    null,
                    "Voltar"
                )
            }
            4 -> {
                confirmChangeState(
                    id,
                    state,
                    "Equipamento não está em triagem?",
                    "O reparo voltara para o estado \"Recebido\".",
                    "Não está em triagem",
                    null,
                    "Voltar"
                )
            }
            5 -> {
                mudaEstado(id, state)
            }
            6 -> {
                mudaEstado(id, state)
            }
            7 -> {
                confirmChangeState(
                    id,
                    state,
                    "Equipamento não saiu para entrega?",
                    "O reparo voltara para o estado \"Higienização\".",
                    "Não saiu para entrega",
                    null,
                    "Voltar"
                )
            }
            else -> return
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_unidade_manutencao_status_reparo, parent, false)

        view.unidadeManutencaoOfertasDetail.visibility = View.GONE

        view.textView_equipamento.setOnClickListener {
            TransitionManager.beginDelayedTransition(view as ViewGroup?)

            if (view.unidadeManutencaoOfertasDetail.visibility == View.VISIBLE) {
                view.unidadeManutencaoOfertasDetail.visibility = View.GONE

                val icon = ContextCompat.getDrawable(
                    mContext,
                    R.drawable.ic_arrow_drop_down_black_24dp
                )

                view.textView_equipamento.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    icon,
                    null
                )
            } else {
                view.unidadeManutencaoOfertasDetail.visibility = View.VISIBLE

                val icon = ContextCompat.getDrawable(
                    mContext,
                    R.drawable.ic_arrow_drop_up_black_24dp
                )

                view.textView_equipamento.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    icon,
                    null
                )
            }
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val json = mFilterArray.getJSONObject(position)

        val nome = json.getString("nome")
        val estado = json.getInt("estado")
        val (cancelVisible, cancelText, okVisible, okText) = getButtonStates(estado)

        holder.view.unidadeManutencaoOfertasBtnOK.visibility =
            if (okVisible) View.VISIBLE else View.GONE
        holder.view.unidadeManutencaoOfertasBtnOK.text = okText
        holder.view.unidadeManutencaoOfertasBtnCancel.visibility =
            if (cancelVisible) View.VISIBLE else View.GONE
        holder.view.unidadeManutencaoOfertasBtnCancel.text = cancelText

        holder.view.textView_equipamento.text = nome + " EQ${json.getString("id")}"
        holder.view.unidadeManutencaoOfertasEstado.text = getEstadoString(estado)
        holder.view.unidadeManutencaoOfertasEstado.background.setTint(getEstadoColor(estado))
        holder.view.unidadeManutencaoOfertasUpdatedAt.text = getDateStr(json.getInt("updatedAt"))
        holder.view.editText_nome.text = nome
        holder.view.editText_fabricante.text = json.getString("fabricante")
        holder.view.editText_modelo.text = json.getString("modelo")
        holder.view.editText_numero_serie.text = json.getString("numeroSerie")
        holder.view.editText_defeito.text = json.getString("defeito")
        holder.view.unidadeManutencaoSaudeNome.text = json.getString("unidade")
        holder.view.unidadeManutencaoSaudeEndereco.text = json.getString("local")
        holder.view.unidadeManutencaoSaudeEmail.text = json.getString("email")

        holder.view.unidadeManutencaoOfertasBtnOK.setOnClickListener(null)
        holder.view.unidadeManutencaoOfertasBtnCancel.setOnClickListener(null)

        holder.view.unidadeManutencaoOfertasBtnOK.setOnClickListener {
            onOKPress(holder.view, json.getInt("idOferta"), estado + 1)
        }

        holder.view.unidadeManutencaoOfertasBtnCancel.setOnClickListener {
            onCancelPress(holder.view, json.getInt("idOferta"), estado - 1)
        }
    }

    override fun getItemCount() = mFilterArray.length()

    override fun getFilter(): Filter {
        return object : Filter() {
            private val results = FilterResults()

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                if (constraint.isNullOrBlank()) {
                    results.count = lines.length()
                    results.values = lines
                } else {
                    val filteredArray = JSONArray()

                    for (n in 0 until lines.length()) {
                        val line = lines[n] as JSONObject

                        val id = line.getString("id")
                        val idSistema = "EQ$id"

                        if (lines[n].toString().contains(constraint,true) ||
                            constraint == idSistema) {
                            filteredArray.put(lines[n])
                        }
                    }

                    results.count = filteredArray.length()
                    results.values = filteredArray
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mFilterArray = results?.values as JSONArray

                notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val LOGTAG = "AdapterStatusReparo"
    }
}