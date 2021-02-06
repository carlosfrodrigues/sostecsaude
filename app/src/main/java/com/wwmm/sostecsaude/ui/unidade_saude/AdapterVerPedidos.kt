package com.wwmm.sostecsaude.ui.unidade_saude

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_unidade_saude_ver_pedidos.*
import kotlinx.android.synthetic.main.recyclerview_unidade_saude_ver_pedidos.view.*
import kotlinx.android.synthetic.main.recyclerview_unidade_saude_ver_pedidos.view.editText_defeito
import kotlinx.android.synthetic.main.recyclerview_unidade_saude_ver_pedidos.view.editText_fabricante
import kotlinx.android.synthetic.main.recyclerview_unidade_saude_ver_pedidos.view.editText_modelo
import kotlinx.android.synthetic.main.recyclerview_unidade_saude_ver_pedidos.view.editText_nome
import kotlinx.android.synthetic.main.recyclerview_unidade_saude_ver_pedidos.view.editText_numero_serie
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class AdapterVerPedidos(private val frag: VerPedidos, private val lines: JSONArray) :
    RecyclerView.Adapter<AdapterVerPedidos.ViewHolder>(), Filterable {
    private var mFilterArray = lines
    private var mMyPrefs = PreferenceManager.getDefaultSharedPreferences(frag.requireContext())
    private var mToken = mMyPrefs.getString("Token", "")!!
    private var mQueue = Volley.newRequestQueue(frag.requireContext())

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_unidade_saude_ver_pedidos, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = mFilterArray[position] as JSONObject

        val id = line.getString("ID")
        var nome = line.getString("Nome")
        var fabricante = line.getString("Fabricante")
        var modelo = line.getString("Modelo")
        var numeroSerie = line.getString("NumeroSerie")
        var defeito = line.getString("Defeito")

        holder.view.textView_title_equipamento.text = "Equipamento EQ$id"

        holder.view.editText_nome.setText(nome)
        holder.view.editText_fabricante.setText(fabricante)
        holder.view.editText_modelo.setText(modelo)
        holder.view.editText_numero_serie.setText(numeroSerie)
        holder.view.editText_defeito.setText(defeito)

        holder.view.button_remove.setOnClickListener(null)
        holder.view.button_update.setOnClickListener(null)

        holder.view.button_remove.setOnClickListener {
            frag.progressBar.visibility = View.VISIBLE

            val request = object : StringRequest(
                Method.POST, "$myServerURL/unidade_saude_remover_equipamento",
                Response.Listener { response ->
                    val msg = response.toString()

                    if (msg == "invalid_token") {
                        frag.mActivityController.navigate(R.id.action_global_fazerLogin)
                    } else {
                        mFilterArray.remove(position)

                        for (n in 0 until lines.length()) {
                            if (lines[n] == line) {
                                lines.remove(n)

                                break
                            }
                        }

                        notifyItemRemoved(position)

                        frag.progressBar.visibility = View.GONE
                    }
                },
                Response.ErrorListener {
                    Log.d(LOGTAG, "failed request: $it")

                    connectionErrorMessage(frag.main_layout_ver_pedidos, it)
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()

                    parameters["token"] = mToken
                    parameters["id"] = id

                    return parameters
                }
            }

            mQueue.add(request)
        }

        holder.view.button_update.setOnClickListener {
            nome = holder.view.editText_nome.text.toString()
            fabricante = holder.view.editText_fabricante.text.toString()
            modelo = holder.view.editText_modelo.text.toString()
            numeroSerie = holder.view.editText_numero_serie.text.toString()
            defeito = holder.view.editText_defeito.text.toString()

            frag.progressBar.visibility = View.VISIBLE

            val request = object : StringRequest(
                Method.POST, "$myServerURL/unidade_saude_atualizar_equipamento",
                Response.Listener { response ->
                    val msg = response.toString()

                    if (msg == "invalid_token") {
                        val controller = frag.findNavController()

                        controller.navigate(R.id.action_global_fazerLogin)
                    } else {
                        for (j in 0 until lines.length()) {
                            val obj = lines[j] as JSONObject

                            if (obj.getString("ID") == id) {
                                obj.put("Nome", nome)
                                obj.put("Fabricante", fabricante)
                                obj.put("Modelo", modelo)
                                obj.put("NumeroSerie", numeroSerie)
                                obj.put("Defeito", defeito)
                                obj.put("Quantidade", "1") // essa variável será retirada um dia...

                                lines.put(j, obj)

                                break
                            }
                        }

                        frag.progressBar.visibility = View.GONE

                        Snackbar.make(
                            frag.main_layout_ver_pedidos, msg,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                },
                Response.ErrorListener {
                    Log.d(LOGTAG, "failed request: $it")

                    connectionErrorMessage(frag.main_layout_ver_pedidos, it)
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()

                    parameters["token"] = mToken
                    parameters["id"] = id
                    parameters["nome"] = nome
                    parameters["fabricante"] = fabricante
                    parameters["modelo"] = modelo
                    parameters["numero_serie"] = numeroSerie
                    parameters["quantidade"] = "1" // um dia essa varável será removida
                    parameters["defeito"] = defeito

                    return parameters
                }
            }

            mQueue.add(request)
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
                        if (lines[n].toString().contains(constraint, true)) {
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
        const val LOGTAG = "adapter ver pedidos"
    }
}