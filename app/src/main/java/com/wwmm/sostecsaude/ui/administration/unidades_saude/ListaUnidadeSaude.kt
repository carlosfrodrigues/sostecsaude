package com.wwmm.sostecsaude.ui.administration.unidades_saude

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_admin_lista_unidades.*
import org.json.JSONArray

class ListaUnidadeSaude : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var mActivityController: NavController
    private var mAdapterLista: AdapterLista? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_lista_unidades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

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

        val prefs = getDefaultSharedPreferences(requireContext())

        val token = prefs.getString("Token", "")!!

        val jsonToken = JSONArray()

        jsonToken.put(0, token)

        val queue = Volley.newRequestQueue(requireContext())

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/lista_unidade_saude",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0) {
                        if (response[0] == "invalid_token") {
                            mActivityController.navigate(R.id.action_global_fazerLogin)
                        } else if (response.length() == 2) {
                            val unidades = response[0] as? JSONArray
                            val whitelist = response[1] as JSONArray

                            if (unidades != null) {
                                mAdapterLista = AdapterLista(
                                    this@ListaUnidadeSaude, unidades,
                                    whitelist
                                )

                                recyclerview.apply {
                                    adapter = mAdapterLista
                                }
                            }

                            progressBar.visibility = View.GONE
                        }
                    }
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")

                if (isAdded) {
                    progressBar.visibility = View.GONE
                }

                connectionErrorMessage(layout_admin_unidades, it)
            }
        )

        queue.add(request)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mAdapterLista?.filter?.filter(newText)

        return true
    }

    companion object {
        const val LOGTAG = "ListaUnidades"
    }
}
