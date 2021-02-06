package com.wwmm.sostecsaude.ui.unidade_saude

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
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.recyclerview_unidade_saude_ver_ofertas.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class AdapterVerOfertas(
    private val lines: JSONArray,
    context: Context,
    verOfertas: VerOfertasInterface
) :
    RecyclerView.Adapter<AdapterVerOfertas.ViewHolder>(), Filterable {
    private var mFilterArray = lines
    private val mContext = context
    private val mVerOfertas = verOfertas

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private fun getEstadoString(estado: Int): String {
        return when (estado) {
            0 -> "Aguardando aceite"
            1 -> "Aceito"
            2 -> "Pronto para retirada"
            3 -> "Retirado"
            4 -> "Recebido"
            5 -> "Triagem"
            6 -> "Manutenção"
            7 -> "Higienização"
            8 -> "Saiu para entrega"
            9 -> "Recebido"
            else -> estado.toString()
        }
    }

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
                true,
                "Aceitar"
            )
            1 -> ButtonState(
                true,
                "Cancelar",
                true,
                "Pronto para retirar"
            )
            2 -> ButtonState(
                true,
                "Não pronto",
                true,
                "Confirmar retirada"
            )
            3 -> ButtonState(
                true,
                "Não retirado",
                false,
                ""
            )
            8 -> ButtonState(
                false,
                "",
                true,
                "Acusar recebimento"
            )
            9 -> ButtonState(
                true,
                "Não recebido",
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
                    mVerOfertas.changeListItemState(ofertaId, estadoTo)
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
            1 -> {
                mudaEstado(id, state)
            }
            2 -> {
                confirmChangeState(
                    id,
                    state,
                    "Pronto para retirar?",
                    "A unidade de manutenção será notificada.",
                    "Sim",
                    "Não"
                )
            }
            3 -> {
                confirmChangeState(
                    id,
                    state,
                    "Equipamento retirado?",
                    "Isso indicará que a unidade de manutenção retirou o equipamento.",
                    "Sim",
                    "Não"
                )
            }
            9 -> {
                mudaEstado(id, state)
            }
            else -> return
        }
    }

    private fun onCancelPress(view: View, id: Int, state: Int) {
        when (state) {
            0 -> {
                confirmChangeState(
                    id,
                    state,
                    "Cancelar aceite?",
                    "O pedido voltara para o estado \"Aberto\".\nLembre de avisar à unidade de manutenção.",
                    "Cancelar aceite",
                    null,
                    "Voltar"
                )
            }
            1 -> {
                confirmChangeState(
                    id,
                    state,
                    "Não está pronto para retirar?",
                    "O estado voltará para apenas \"Aceito\".\nLembre de avisar à unidade de manutenção.",
                    "Não está pronto", null,
                    "Voltar"
                )
            }
            2 -> {
                confirmChangeState(
                    id,
                    state,
                    "Equipamento não foi retirado?",
                    "O estado voltará para \"Pronto para retirada\".",
                    "Não foi retirado", null,
                    "Voltar"
                )
            }
            8 -> {
                confirmChangeState(
                    id,
                    state,
                    "Equipamento não foi recebido?",
                    "O estado voltará para \"Saiu para entrega\".",
                    "Não recebido", null,
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
            .inflate(R.layout.recyclerview_unidade_saude_ver_ofertas, parent, false)

        view.unidadeSaudeVerOfertasHeader.setOnClickListener {
            TransitionManager.beginDelayedTransition(view as ViewGroup?)
            if (view.unidadeSaudeVerOfertasDetail.visibility == View.VISIBLE) {
                view.unidadeSaudeVerOfertasDetail.visibility = View.GONE
                view.unidadeSaudeVerOfertasChevron.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
            } else {
                view.unidadeSaudeVerOfertasDetail.visibility = View.VISIBLE
                view.unidadeSaudeVerOfertasChevron.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
            }
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val json = mFilterArray[position] as JSONObject

        val empresa = json.getJSONObject("empresa")
        val estado = json.getInt("estado")
        val (cancelVisible, cancelText, okVisible, okText) = getButtonStates(estado)

        holder.view.unidadeSaudeVerOfertasBtnOK.visibility =
            if (okVisible) View.VISIBLE else View.GONE
        holder.view.unidadeSaudeVerOfertasBtnOK.text = okText
        holder.view.unidadeSaudeVerOfertasBtnCancel.visibility =
            if (cancelVisible) View.VISIBLE else View.GONE
        holder.view.unidadeSaudeVerOfertasBtnCancel.text = cancelText

        holder.view.textView_empresa.text = empresa.getString("nome")
        holder.view.textView_setor.text = empresa.getString("setor")
        holder.view.textView_cnpj.text = empresa.getString("cnpj")
        holder.view.textView_local.text = empresa.getString("local")
        holder.view.textView_telefone.text = empresa.getString("telefone")
        holder.view.textView_email.text = empresa.getString("email")
        holder.view.unidadeSaudeVerOfertasEstado.text = getEstadoString(estado)
        holder.view.unidadeSaudeVerOfertasEstado.background.setTint(getEstadoColor(estado))
        holder.view.unidadeSaudeVerOfertasUpdatedAt.text =
            getDateStr(json.getInt("updatedAt"))

        holder.view.unidadeSaudeVerOfertasBtnOK.setOnClickListener {
            onOKPress(holder.view, json.getInt("id"), estado + 1)
        }
        holder.view.unidadeSaudeVerOfertasBtnCancel.setOnClickListener {
            onCancelPress(holder.view, json.getInt("id"), estado - 1)
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
                        if (lines[n].toString().contains(constraint,true)) {
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
}