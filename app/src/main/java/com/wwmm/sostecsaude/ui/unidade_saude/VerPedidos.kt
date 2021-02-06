package com.wwmm.sostecsaude.ui.unidade_saude

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
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
import kotlinx.android.synthetic.main.fragment_unidade_saude_ver_pedidos.*
import org.json.JSONArray

class VerPedidos : Fragment(), Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener {
    lateinit var mActivityController: NavController
    private var mAdapterVerPedidos: AdapterVerPedidos? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unidade_saude_ver_pedidos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_atualizar_perfil).isVisible = false
        toolbar.menu.findItem(R.id.menu_login).isVisible = false
        toolbar.setOnMenuItemClickListener(this)

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
            "$myServerURL/unidade_saude_pegar_equipamentos",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0) {
                        if (response[0] == "invalid_token") {
                            mActivityController.navigate(R.id.action_global_fazerLogin)
                        } else {
                            mAdapterVerPedidos = AdapterVerPedidos(this@VerPedidos, response)

                            recyclerview.apply {
                                adapter = mAdapterVerPedidos
                            }
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

                connectionErrorMessage(main_layout_ver_pedidos, it)
            }
        )

        queue.add(request)

        floatingActionButton.setOnClickListener {
            mActivityController.navigate(R.id.action_global_adicionarEquipamento)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login -> {
                val prefs = getDefaultSharedPreferences(requireContext())

                val editor = prefs.edit()

                editor.putString("Token", "")
                editor.putString("Perfil", "")
                editor.putString("Email", "")

                editor.apply()

                mActivityController.navigate(R.id.action_global_fazerLogin)

                return true
            }

            R.id.menu_atualizar_perfil -> {
                mActivityController.navigate(R.id.action_global_unidadeSaude)

                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mAdapterVerPedidos?.filter?.filter(newText)

        return true
    }

    companion object {
        const val LOGTAG = "relatar ver pedidos"
    }
}
