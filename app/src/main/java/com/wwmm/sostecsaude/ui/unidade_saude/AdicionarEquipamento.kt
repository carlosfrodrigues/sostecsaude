package com.wwmm.sostecsaude.ui.unidade_saude

import android.content.Context
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
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_unidade_saude_adicionar_equipamento.*

class AdicionarEquipamento : Fragment(){
    private lateinit var mActivityController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unidade_saude_adicionar_equipamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

        progressBar.visibility = View.GONE

        button_add.setOnClickListener {
            val nome = textView_nome.text.toString()
            val fabricante = textView_fabricante.text.toString()
            val modelo = textView_modelo.text.toString()
            val numeroSerie = textView_numero_serie.text.toString()
            val defeito = textView_defeito.text.toString()

            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                        InputMethodManager?

            imm?.hideSoftInputFromWindow(
                requireActivity().currentFocus?.windowToken,
                0
            )

            if (nome.isNotBlank() && fabricante.isNotBlank() && modelo.isNotBlank()
                && numeroSerie.isNotBlank() && defeito.isNotBlank() &&
                textView_quantidade.text.isNotBlank()
            ) {
                val quantidade = textView_quantidade.text.toString()

                progressBar.visibility = View.VISIBLE

                val prefs =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())

                val token = prefs.getString("Token", "")!!

                val queue = Volley.newRequestQueue(requireContext())

                val request = object : StringRequest(
                    Method.POST, "$myServerURL/unidade_saude_adicionar_equipamento",
                    Response.Listener { response ->
                        if (isAdded) {
                            when (val msg = response.toString()) {
                                "invalid_token" -> {
                                    mActivityController.navigate(R.id.action_global_fazerLogin)
                                }

                                else -> {
                                    textView_nome.text.clear()
                                    textView_fabricante.text.clear()
                                    textView_modelo.text.clear()
                                    textView_numero_serie.text.clear()
                                    textView_quantidade.text.clear()
                                    textView_defeito.text.clear()

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
                        parameters["quantidade"] = quantidade
                        parameters["defeito"] = defeito

                        return parameters
                    }
                }

                queue.add(request)
            } else {
                Snackbar.make(
                    main_layout_registrar, "Preencha todos os campos!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val LOGTAG = "relatar defeito"
    }
}