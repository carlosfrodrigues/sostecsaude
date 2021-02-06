package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_unidade_manutencao_ver_pedidos.*
import org.json.JSONArray

class VerPedidos : Fragment(), SearchView.OnQueryTextListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var mAdapterVerPedidos: AdapterVerPedidos? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unidade_manutencao_ver_pedidos, container, false)
    }

    private fun refreshList() {
        val controller = findNavController()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val token = prefs.getString("Token", "")!!
        val jsonToken = JSONArray()
        jsonToken.put(0, token)
        val queue = Volley.newRequestQueue(requireContext())

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/lista_todos_equipamentos",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response[0] == "invalid_token") {
                        controller.navigate(R.id.action_global_fazerLogin)
                    } else {
                        if (response.length() == 2) {
                            val equipamentos = response[0] as? JSONArray
                            val idNumbers = response[1] as JSONArray
                            if (equipamentos != null) {
                                mAdapterVerPedidos = AdapterVerPedidos(
                                    this@VerPedidos, equipamentos,
                                    idNumbers
                                )
                                recyclerview.apply {
                                    adapter = mAdapterVerPedidos
                                }
                            }
                        }

                        progressBar.visibility = View.GONE
                        refreshView.isRefreshing = false
                    }
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")
                connectionErrorMessage(layout_manutencao_ver_pedidos, it)
                refreshView.isRefreshing = false
            }
        )
        queue.add(request)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_atualizar_perfil).isVisible = false
        toolbar.menu.findItem(R.id.menu_login).isVisible = false

        val actionView = toolbar.menu.findItem(R.id.menu_search).actionView as
                SearchView

        actionView.setOnQueryTextListener(this)

        recyclerview.apply {
            setHasFixedSize(true)

            layoutManager = LinearLayoutManager(requireContext())
        }

        refreshView.setOnRefreshListener(this)

        refreshList()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mAdapterVerPedidos?.filter?.filter(newText)

        return true
    }

    companion object {
        const val LOGTAG = "VerPedidos"
    }

    override fun onRefresh() {
        refreshList()
    }
}
