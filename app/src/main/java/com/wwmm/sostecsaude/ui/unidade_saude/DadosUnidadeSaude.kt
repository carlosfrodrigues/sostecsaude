package com.wwmm.sostecsaude.ui.unidade_saude

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_dados_unidade_saude.*


class DadosUnidadeSaude : Fragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var mActivityController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dados_unidade_saude, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val controller = findNavController()

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

        toolbar.setupWithNavController(findNavController())
        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_search).isVisible = false
        toolbar.menu.findItem(R.id.menu_atualizar_perfil).isVisible = false
        toolbar.setOnMenuItemClickListener(this)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val token = prefs.getString("Token", "")!!
        val criandoConta = prefs.getBoolean("CriandoConta", false)

        val queue = Volley.newRequestQueue(requireContext())

        val requestGet = object : StringRequest(
            Method.POST, "$myServerURL/get_unidade",
            Response.Listener { response ->
                val msg = response.toString()

                if (msg == "invalid_token") {
                    controller.navigate(R.id.action_global_fazerLogin)
                } else {
                    val arr = msg.split("<&>")

                    if (arr.size == 2) {
                        val nome = arr[0]
                        val local = arr[1]

                        editText_unidade_saude.setText(nome)
                        editText_local.setText(local)
                    }
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "\"failed request: pegar contato de unidade de saúde\"")

                connectionErrorMessage(main_layout_unidade_saude, it)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val parameters = HashMap<String, String>()

                parameters["token"] = token

                return parameters
            }
        }

        queue.add(requestGet)

        button_save.setOnClickListener {
            val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as
                    InputMethodManager?

            imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

            if (editText_local.text.isNotBlank() && editText_unidade_saude.text.isNotBlank()) {
                val r = object : StringRequest(
                    Method.POST, "$myServerURL/update_unidade",
                    Response.Listener { response ->
                        if (isAdded) {
                            val msg = response.toString()

                            if (msg == "invalid_token") {
                                controller.navigate(R.id.action_global_fazerLogin)
                            } else {
                                val editor = prefs.edit()

                                editor.putString("UnidadeSaude_nome", editText_unidade_saude.text.toString())
                                editor.putString("UnidadeSaude_local", editText_local.text.toString())

                                editor.apply()

                                Snackbar.make(
                                    main_layout_unidade_saude, msg,
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                if (criandoConta) {
                                    controller.navigate(R.id.action_dadosUnidadeSaude_to_welcome)
                                } else {
                                    controller.navigate(R.id.action_dadosUnidadeSaude_to_unidadeSaude)
                                }
                            }
                        }
                    },
                    Response.ErrorListener {
                        Log.d(LOGTAG, "failed request: $it")
                    }) {
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()

                        parameters["token"] = token
                        parameters["nome"] = editText_unidade_saude.text.toString()
                        parameters["local"] = editText_local.text.toString()

                        return parameters
                    }
                }

                queue.add(r)
            } else {
                Snackbar.make(
                    main_layout_unidade_saude, "Preencha todos os campos!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login -> {
                val prefs =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())

                val editor = prefs.edit()

                editor.putString("Token", "")
                editor.putString("Perfil", "")
                editor.putString("Email", "")

                editor.apply()

                mActivityController.navigate(R.id.action_global_fazerLogin)

                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        const val LOGTAG = "contato unidade saude"
    }
}
