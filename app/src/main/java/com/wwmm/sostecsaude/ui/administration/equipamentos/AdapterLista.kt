package com.wwmm.sostecsaude.ui.administration.equipamentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.wwmm.sostecsaude.R
import kotlinx.android.synthetic.main.recyclerview_admin_equipamentos.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class AdapterLista(private val lines: JSONArray) :
    RecyclerView.Adapter<AdapterLista.ViewHolder>(), Filterable {
    private var mFilterArray = lines

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_admin_equipamentos, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val line = mFilterArray[position] as JSONObject

        val nome = line.getString("Nome")
        val fabricante = line.getString("Fabricante")
        val modelo = line.getString("Modelo")
        val numeroSerie = line.getString("NumeroSerie")
        val quantidade = line.getString("Quantidade")
        val defeito = line.getString("Defeito")

        holder.view.editText_nome.text = nome
        holder.view.editText_fabricante.text = fabricante
        holder.view.editText_modelo.text = modelo
        holder.view.editText_numero_serie.text = numeroSerie
        holder.view.editText_defeito.text = defeito
        holder.view.editText_quantidade.text = quantidade
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

    companion object {
        const val LOGTAG = "adapter ver pedidos"
    }
}