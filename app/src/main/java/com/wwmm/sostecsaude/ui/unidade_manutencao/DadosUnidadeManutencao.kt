package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.content.Context
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
import kotlinx.android.synthetic.main.fragment_dados_unidade_manutencao.*

class DadosUnidadeManutencao : Fragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var mActivityController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dados_unidade_manutencao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

        val controller = findNavController()

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
                if (isAdded) {
                    val msg = response.toString()

                    if (msg == "invalid_token") {
                        controller.navigate(R.id.action_global_fazerLogin)
                    } else {
                        val arr = msg.split("<&>")

                        if (arr.size == 5) {
                            val nome = arr[0]
                            val setor = arr[1]
                            val local = arr[2]
                            val cnpj = arr[3]
                            val telefone = arr[4]

                            editText_nome.setText(nome)
                            editText_setor.setText(setor)
                            editText_local.setText(local)
                            editText_cnpj.setText(cnpj)
                            editText_telefone.setText(telefone)
                        }
                    }
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "failed request: $it")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val parameters = HashMap<String, String>()

                parameters["token"] = token

                return parameters
            }
        }

        queue.add(requestGet)

        button_empresa_contato.setOnClickListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager?

            imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

            if (editText_nome.text.isNotBlank() && editText_setor.text.isNotBlank() &&
                editText_local.text.isNotBlank() && editText_cnpj.text.isNotBlank() &&
                editText_telefone.text.isNotBlank()
            ) {
                val r = object : StringRequest(
                    Method.POST, "$myServerURL/update_unidade",
                    Response.Listener { response ->
                        val msg = response.toString()

                        if (msg == "invalid_token") {
                            controller.navigate(R.id.action_global_fazerLogin)
                        } else {
                            Snackbar.make(
                                main_layout_contato_manutencao, msg,
                                Snackbar.LENGTH_SHORT
                            ).show()

                            if (criandoConta) {
                                controller.navigate(R.id.action_dadosUnidadeManutencao_to_welcome)
                            } else {
                                controller.navigate(R.id.action_dadosUnidadeManutencao_to_unidadeManutencao)
                            }
                        }
                    },
                    Response.ErrorListener {
                        Log.d(LOGTAG, "failed request: atualizar contato de unidade de saúde")

                        connectionErrorMessage(main_layout_contato_manutencao, it)
                    }) {
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()

                        parameters["token"] = token
                        parameters["nome"] = editText_nome.text.toString()
                        parameters["setor"] = editText_setor.text.toString()
                        parameters["local"] = editText_local.text.toString()
                        parameters["cnpj"] = editText_cnpj.text.toString()
                        parameters["telefone"] = editText_telefone.text.toString()

                        return parameters
                    }
                }

                queue.add(r)
            } else {
                Snackbar.make(
                    main_layout_contato_manutencao, "Preencha todos os campos!",
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
        const val LOGTAG = "contato unidade manu"
    }
}
