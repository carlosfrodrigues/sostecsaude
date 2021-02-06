package com.wwmm.sostecsaude.ui.login

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
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_alterar_senha.*

class AlterarSenha : Fragment() {
    private lateinit var mController: NavController
    private lateinit var mMyPrefs: SharedPreferences
    private lateinit var mQueue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alterar_senha, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar.visibility = View.GONE

        mController = findNavController()

        mMyPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        mQueue = Volley.newRequestQueue(requireContext())

        button_enviar.setOnClickListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager?

            imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

            val senhaAtual = editText_senha_atual.text.toString()
            val novaSenha = editText_nova_senha.text.toString()
            val confirmacao = editText_confirmar_senha.text.toString()

            when {
                senhaAtual.isBlank() || novaSenha.isBlank() || confirmacao.isBlank() -> {
                    Snackbar.make(
                        layout_alterar_senha, "Preencha todos os campos!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                novaSenha != confirmacao -> {
                    Snackbar.make(
                        layout_alterar_senha, "Erro ao confirmar a senha!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                novaSenha.length < 6 -> {
                    Snackbar.make(
                        layout_alterar_senha, "A senha deve ter pelo menos 6 caracteres!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    progressBar.visibility = View.VISIBLE

                    val params = HashMap<String, String>()

                    params["token"] = mMyPrefs.getString("Token", "")!!
                    params["senha_atual"] = senhaAtual
                    params["nova_senha"] = novaSenha

                    val request = object : StringRequest(
                        Request.Method.POST, "$myServerURL/alterar_senha",
                        Response.Listener { response ->
                            if (isAdded) {
                                when (response) {
                                    "invalid_token", "perfil_invalido" -> {
                                        mController.navigate(R.id.action_global_fazerLogin)
                                    }

                                    "senha_atual_invalida" -> {
                                        Snackbar.make(
                                            layout_alterar_senha, "Senha atual inválida!",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }

                                    else -> {
                                        Snackbar.make(
                                            layout_alterar_senha, "Senha alterada!",
                                            Snackbar.LENGTH_SHORT
                                        ).show()

                                        mController.navigate(R.id.action_alterarSenha_to_checkCredentials)
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

                            connectionErrorMessage(layout_alterar_senha, it)
                        }) {
                        override fun getParams(): MutableMap<String, String> {
                            return params
                        }
                    }

                    mQueue.add(request)
                }
            }
        }
    }

    companion object {
        const val LOGTAG = "AlterarSenha"
    }
}
