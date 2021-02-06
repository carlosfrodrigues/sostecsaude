package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_unidade_manutencao_status_reparo.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

interface StatusReparoInterface {
    fun replaceListItem(id: Int, item: JSONObject)
    fun changeListItemState(id: Int, estado: Int)
}

class StatusReparo : Fragment(), SearchView.OnQueryTextListener,
    SwipeRefreshLayout.OnRefreshListener, StatusReparoInterface {
    private lateinit var mController: NavController
    private lateinit var mQueue: RequestQueue
    private val listNomeUnidade = ArrayList<String>()
    private val listEmailCliente = ArrayList<String>()
    private var mAdapterStatusReparo: AdapterStatusReparo? = null
    private lateinit var mSelectedEmail: String
    private lateinit var mReparos: JSONArray

    private fun getReparoIdx(id: Int): Int {
        for (i in 0 until mReparos.length()) {
            val item = mReparos.getJSONObject(i)
            if (id == item.getInt("idOferta")) {
                return i
            }
        }
        return -1
    }

    private fun getReparo(id: Int): JSONObject? {
        val idx = getReparoIdx(id)
        if (idx < 0) return null
        return mReparos.getJSONObject(idx)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_unidade_manutencao_status_reparo,
            container,
            false
        )
    }

    private fun refreshListaClientes() {
        val prefs = getDefaultSharedPreferences(requireContext())
        val token = prefs.getString("Token", "")!!
        val jsonToken = JSONArray()
        jsonToken.put(0, token)
        mQueue = Volley.newRequestQueue(requireContext())

        listNomeUnidade.clear()
        listEmailCliente.clear()

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/unidade_manutencao_lista_clientes",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0) {
                        if (response[0] == "invalid_token" || response[0] == "perfil_invalido") {
                            mController.navigate(R.id.action_global_fazerLogin)
                        } else {
                            for (n in 0 until response.length()) {
                                val unidade = response[n] as JSONObject
                                val nome = unidade["nome"] as String
                                val emailCliente = unidade["email"] as String

                                listNomeUnidade.add(nome)
                                listEmailCliente.add(emailCliente)
                            }
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_dropdown_item, listNomeUnidade
                            )
                            spinner_unidade_saude.adapter = adapter
                        }
                    }
                    progressBar.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")
                if (isAdded) {
                    progressBar.visibility = View.GONE
                }
                connectionErrorMessage(layout_manutencao_status_reparo, it)
            }
        )

        mQueue.add(request)
    }

    private fun refreshListaConsertos(emailCliente: String) {
        val prefs = getDefaultSharedPreferences(requireContext())
        val token = prefs.getString("Token", "")!!
        val jsonToken = JSONArray()

        jsonToken.put(0, token)
        jsonToken.put(1, emailCliente)
        val queue = Volley.newRequestQueue(requireContext())

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/lista_equipamentos_cliente",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0 && response[0] == "invalid_token") {
                        mController.navigate(R.id.action_global_fazerLogin)
                    } else {
                        mReparos = response
                        mAdapterStatusReparo = AdapterStatusReparo(response, requireContext(), this)

                        refreshView.isRefreshing = false
                        recyclerview.apply {
                            adapter = mAdapterStatusReparo
                        }

                        progressBar.visibility = View.GONE
                    }
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")

                if (isAdded) {
                    progressBar.visibility = View.GONE
                }
                refreshView.isRefreshing = false

                connectionErrorMessage(layout_manutencao_status_reparo, it)
            }
        )
        queue.add(request)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mController = findNavController()

        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_atualizar_perfil).isVisible = false
        toolbar.menu.findItem(R.id.menu_login).isVisible = false

        val actionView = toolbar.menu.findItem(R.id.menu_search).actionView as
                SearchView

        actionView.setOnQueryTextListener(this)

        spinner_unidade_saude.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                mSelectedEmail = listEmailCliente[i]
                updateRecycler(mSelectedEmail)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        recyclerview.apply {
            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(requireContext())
        }

        refreshView.setOnRefreshListener(this)
        refreshListaClientes()
    }

    private fun updateRecycler(emailCliente: String) {
        progressBar.visibility = View.VISIBLE

        recyclerview.apply {
            adapter = null
        }

        refreshListaConsertos(emailCliente)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mAdapterStatusReparo?.filter?.filter(newText)

        return true
    }

    override fun onRefresh() {
        refreshListaConsertos(mSelectedEmail)
    }

    override fun replaceListItem(id: Int, item: JSONObject) {
        val idx = getReparoIdx(id)
        if (id == -1) return
        mReparos.put(idx, item)
        recyclerview.adapter?.notifyDataSetChanged()
    }

    override fun changeListItemState(id: Int, estado: Int) {
        val oferta = getReparo(id) ?: return
        oferta.put("estado", estado)
        oferta.put("updatedAt", floor((Calendar.getInstance().timeInMillis / 1000).toDouble()))
        replaceListItem(id, oferta)
    }

    companion object {
        const val LOGTAG = "StatusReparo"
    }
}
