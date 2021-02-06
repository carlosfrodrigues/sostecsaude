package com.wwmm.sostecsaude.ui.administration.unidades_saude

import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_admin_lista_unidades.*
import kotlinx.android.synthetic.main.recyclerview_admin_unidade_saude.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class AdapterLista(
    private val fragment: Fragment, private val unidades: JSONArray,
    private val whitelist: JSONArray
) :
    RecyclerView.Adapter<AdapterLista.ViewHolder>(), Filterable {

    private var mFilterArray = unidades
    private var mMyPrefs = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
    private var mToken = mMyPrefs.getString("Token", "")!!
    private var mQueue = Volley.newRequestQueue(fragment.requireContext())

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_admin_unidade_saude, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = mFilterArray[position] as JSONObject

        val nome = line.getString("Nome")
        val local = line.getString("Local")
        val email = line.getString("Email")

        holder.view.editText_nome.text = nome
        holder.view.textView_local.text = local
        holder.view.textView_email.text = email

        holder.view.switch_whitelist.setOnCheckedChangeListener(null)

        var enableSwitch = false

        for (n in 0 until whitelist.length()) {
            if (whitelist[n] == email) {
                enableSwitch = true

                break
            }
        }

        holder.view.switch_whitelist.isChecked = enableSwitch

        holder.view.switch_whitelist.setOnCheckedChangeListener { _, state ->
            fragment.progressBar.visibility = View.VISIBLE

            val request = object : StringRequest(
                Method.POST, "$myServerURL/update_whitelist",
                Response.Listener { response ->
                    when (val msg = response.toString()) {
                        "invalid_token" -> {
                            val controller = fragment.findNavController()

                            controller.navigate(R.id.action_global_fazerLogin)
                        }

                        else -> {
                            // Temos que atualizar a whitelist para que o filtro atualize
                            // corretamente o status dos switches

                            var naLista = false

                            for (idx in 0 until whitelist.length()) {
                                if (whitelist[idx] == email) {
                                    naLista = true

                                    if (!state) {
                                        whitelist.remove(idx)
                                    }

                                    break
                                }
                            }

                            if (!naLista && state) {
                                whitelist.put(email)
                            }

                            fragment.progressBar.visibility = View.GONE

                            Snackbar.make(
                                fragment.layout_admin_unidades, msg,
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                Response.ErrorListener {
                    Log.d(LOGTAG, "failed request: $it")

                    if (fragment.isAdded) {
                        fragment.progressBar.visibility = View.GONE
                    }

                    connectionErrorMessage(fragment.layout_admin_unidades, it)
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()

                    parameters["token"] = mToken
                    parameters["email"] = email
                    parameters["state"] = state.toString()

                    return parameters
                }
            }

            mQueue.add(request)
        }

        holder.view.button_remove.setOnClickListener(null)

        holder.view.button_remove.setOnClickListener {
            val alertDialog: AlertDialog? = fragment.requireActivity().let {
                val builder = AlertDialog.Builder(it)

                builder.apply {
                    setPositiveButton("Sim", DialogInterface.OnClickListener { _, _ ->
                        removeUser(email, position, line)
                    })

                    setNegativeButton("Não", DialogInterface.OnClickListener { _, _ ->
                    })
                }

                builder.setMessage("Remover esta unidade permanentemente do banco de dados?")
                    .setTitle("Remover Unidade")

                builder.create()
            }

            alertDialog?.show()
        }
    }

    override fun getItemCount() = mFilterArray.length()

    override fun getFilter(): Filter {
        return object : Filter() {
            private val results = FilterResults()

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                if (constraint.isNullOrBlank()) {
                    results.count = unidades.length()
                    results.values = unidades
                } else {
                    val filteredArray = JSONArray()

                    for (n in 0 until unidades.length()) {
                        if (unidades[n].toString().contains(constraint,true)
                        ) {
                            filteredArray.put(unidades[n])
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

    private fun removeUser(email: String, position: Int, line: JSONObject) {
        val request = object : StringRequest(
            Method.POST, "$myServerURL/remover_usuario",
            Response.Listener { response ->
                when (val msg = response.toString()) {
                    "invalid_token" -> {
                        val controller = fragment.findNavController()

                        controller.navigate(R.id.action_global_fazerLogin)
                    }

                    else -> {
                        mFilterArray.remove(position)

                        for (n in 0 until unidades.length()) {
                            if (unidades[n] == line) {
                                unidades.remove(n)

                                break
                            }
                        }

                        notifyItemRemoved(position)

                        fragment.progressBar.visibility = View.GONE

                        Snackbar.make(
                            fragment.layout_admin_unidades, msg,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")

                if (fragment.isAdded) {
                    fragment.progressBar.visibility = View.GONE
                }
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val parameters = HashMap<String, String>()

                parameters["token"] = mToken
                parameters["email"] = email

                return parameters
            }
        }

        mQueue.add(request)
    }

    companion object {
        const val LOGTAG = "AdapterUnidade"
    }
}