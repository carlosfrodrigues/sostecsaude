package com.wwmm.sostecsaude.ui.unidade_saude

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_unidade_saude_ver_ofertas.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

interface VerOfertasInterface {
    fun replaceListItem(id: Int, item: JSONObject)
    fun changeListItemState(id: Int, estado: Int)
}

class VerOfertas : Fragment(), Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener,
    VerOfertasInterface {
    private lateinit var mActivityController: NavController
    private lateinit var mController: NavController
    private var mIdList = ArrayList<String>()
    private var mAdapterVerOfertas: AdapterVerOfertas? = null
    private lateinit var mOfertas: JSONArray

    private fun getOfertaIdx(id: Int): Int {
        for (i in 0 until mOfertas.length()) {
            val item = mOfertas.getJSONObject(i)
            if (id == item.getInt("id")) {
                return i
            }
        }
        return -1
    }

    private fun getOferta(id: Int): JSONObject? {
        val idx = getOfertaIdx(id)
        if (idx < 0) return null
        return mOfertas.getJSONObject(idx)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unidade_saude_ver_ofertas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

        mController = findNavController()

        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_atualizar_perfil).isVisible = false
        toolbar.menu.findItem(R.id.menu_login).isVisible = false
        toolbar.setOnMenuItemClickListener(this)

        val actionView = toolbar.menu.findItem(R.id.menu_search).actionView as
                SearchView

        actionView.setOnQueryTextListener(this)

        spinner_equipamento.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                updateRecycler(mIdList[i])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

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
            "$myServerURL/v2/unidade_saude_pegar_equipamentos",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0) {
                        if (response[0] == "invalid_token") {
                            mActivityController.navigate(R.id.action_global_fazerLogin)
                        } else {

                            val list = ArrayList<String>()
                            mIdList.clear()

                            for (n in 0 until response.length()) {
                                val obj = response[n] as JSONObject

                                val id = obj.getString("id")

                                list.add(obj.getString("nome") + " EQ$id")
                                mIdList.add(id)
                            }

                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_dropdown_item, list
                            )

                            spinner_equipamento.adapter = adapter
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

                connectionErrorMessage(layout_unidade_saude_ver_ofertas, it)
            }
        )

        queue.add(request)
    }

    override fun replaceListItem(id: Int, item: JSONObject) {
        val idx = getOfertaIdx(id)
        if (id == -1) return
        mOfertas.put(idx, item)
        recyclerview.adapter?.notifyDataSetChanged()
    }

    override fun changeListItemState(id: Int, estado: Int) {
        val oferta = getOferta(id) ?: return
        oferta.put("estado", estado)
        oferta.put("updatedAt", floor((Calendar.getInstance().timeInMillis / 1000).toDouble()))
        replaceListItem(id, oferta)
    }

    private fun updateRecycler(idEquipamento: String) {
        progressBar.visibility = View.VISIBLE

        recyclerview.apply {
            adapter = null
        }

        val prefs = getDefaultSharedPreferences(requireContext())

        val token = prefs.getString("Token", "")!!

        val jsonToken = JSONArray()

        jsonToken.put(0, token)
        jsonToken.put(1, idEquipamento)

        val queue = Volley.newRequestQueue(requireContext())

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/v2/lista_interessados_manutencao",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0 && response[0] == "invalid_token") {
                        mActivityController.navigate(R.id.action_global_fazerLogin)
                    } else {
                        mOfertas = response
                        mAdapterVerOfertas =
                            AdapterVerOfertas(mOfertas, requireContext(), this)

                        recyclerview.apply {
                            adapter = mAdapterVerOfertas
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

                connectionErrorMessage(layout_unidade_saude_ver_ofertas, it)
            }
        )

        queue.add(request)
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
        mAdapterVerOfertas?.filter?.filter(newText)

        return true
    }

    companion object {
        const val LOGTAG = "relatar ver ofertas"
    }
}
