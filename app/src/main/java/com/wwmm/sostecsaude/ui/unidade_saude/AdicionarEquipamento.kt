package com.wwmm.sostecsaude.ui.unidade_saude

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_unidade_saude_adicionar_equipamento.*

class AdicionarEquipamento : Fragment() {
    private lateinit var mActivityController: NavController
    private lateinit var mQueue: RequestQueue
    private lateinit var mPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_unidade_saude_adicionar_equipamento,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

        mPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        mQueue = Volley.newRequestQueue(requireContext())

        progressBar.visibility = View.GONE

        initFields()

        button_add.setOnClickListener {
            val nome = editText_nome.text.toString()
            val fabricante = editText_fabricante.text.toString()
            val modelo = editText_modelo.text.toString()
            val numeroSerie = editText_numero_serie.text.toString()
            val defeito = editText_defeito.text.toString()
            val unidade = editText_unidade_saude.text.toString()
            val local = editText_local.text.toString()

            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                        InputMethodManager?

            imm?.hideSoftInputFromWindow(
                requireActivity().currentFocus?.windowToken,
                0
            )

            if (nome.isNotBlank() && fabricante.isNotBlank() && modelo.isNotBlank()
                && numeroSerie.isNotBlank() && defeito.isNotBlank() &&
                unidade.isNotBlank() && local.isNotBlank()
            ) {
                progressBar.visibility = View.VISIBLE

                val token = mPrefs.getString("Token", "")!!

                val request = object : StringRequest(
                    Method.POST, "$myServerURL/unidade_saude_adicionar_equipamento",
                    Response.Listener { response ->
                        if (isAdded) {
                            when (val msg = response.toString()) {
                                "invalid_token" -> {
                                    mActivityController.navigate(R.id.action_global_fazerLogin)
                                }

                                else -> {
                                    progressBar.visibility = View.GONE

                                    Snackbar.make(
                                        main_layout_registrar, msg,
                                        Snackbar.LENGTH_SHORT
                                    ).show()

                                    mActivityController.navigate(R.id.action_adicionarEquipamento_to_unidadeSaude)
                                }
                            }
                        }
                    },
                    Response.ErrorListener {
                        Log.d(LOGTAG, "failed request: $it")

                        connectionErrorMessage(main_layout_registrar, it)
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        val parameters = HashMap<String, String>()

                        parameters["token"] = token
                        parameters["nome"] = nome
                        parameters["fabricante"] = fabricante
                        parameters["modelo"] = modelo
                        parameters["numero_serie"] = numeroSerie
                        parameters["quantidade"] = "1" // um dia essa varável será removida
                        parameters["defeito"] = defeito
                        parameters["unidade"] = unidade
                        parameters["local"] = local

                        return parameters
                    }
                }

                mQueue.add(request)
            } else {
                Snackbar.make(
                    main_layout_registrar, "Preencha todos os campos!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initFields() {
        var unidade = mPrefs.getString("UnidadeSaude_nome", "")!!
        var local = mPrefs.getString("UnidadeSaude_local", "")!!

        if (unidade.isBlank() || local.isBlank()) {
            val token = mPrefs.getString("Token", "")!!

            val requestGet = object : StringRequest(
                Method.POST, "$myServerURL/get_unidade",
                Response.Listener { response ->
                    val msg = response.toString()

                    if (msg == "invalid_token") {
                        mActivityController.navigate(R.id.action_global_fazerLogin)
                    } else {
                        val arr = msg.split("<&>")

                        if (arr.size == 2) {
                            unidade = arr[0]
                            local = arr[1]

                            editText_unidade_saude.setText(unidade)
                            editText_local.setText(local)

                            val editor = mPrefs.edit()

                            editor.putString("UnidadeSaude_nome", unidade)
                            editor.putString("UnidadeSaude_local", local)

                            editor.apply()
                        }
                    }
                },
                Response.ErrorListener {
                    Log.d(LOGTAG, "failed request: $it")

                    connectionErrorMessage(main_layout_registrar, it)
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val parameters = HashMap<String, String>()

                    parameters["token"] = token

                    return parameters
                }
            }

            mQueue.add(requestGet)
        } else {
            editText_unidade_saude.setText(unidade)
            editText_local.setText(local)
        }
    }

    companion object {
        const val LOGTAG = "AdicionarEquipamento"
    }
}
