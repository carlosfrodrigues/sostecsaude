package com.wwmm.sostecsaude.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
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
import com.wwmm.sostecsaude.sendFirebaseToken
import kotlinx.android.synthetic.main.fragment_criar_conta.*

class CriarConta : Fragment() {
    private lateinit var mController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_criar_conta, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mController = findNavController()

        toolbar.setupWithNavController(mController)

        button_criar_conta.setOnClickListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager?

            imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

            var perfil = ""

            if (radioButton_unidade_saude.isChecked) {
                perfil = "unidade_saude"
            } else if (radioButton_unidade_manutencao.isChecked) {
                perfil = "unidade_manutencao"
            }

            when {
                editText_email.text.isBlank() -> {
                    Snackbar.make(
                        layout_criar_conta, "Digite um e-mail!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                editText_senha.text.isBlank() || editText_senha_confirmar.text.isBlank() -> {
                    Snackbar.make(
                        layout_criar_conta, "Digite uma senha!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                editText_senha.text.length < 6 || editText_senha_confirmar.text.length < 6 -> {
                    Snackbar.make(
                        layout_criar_conta, "A senha deve ter pelo menos 6 caracteres!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                editText_senha.text.toString() != editText_senha_confirmar.text.toString() -> {
                    Snackbar.make(
                        layout_criar_conta, "Erro ao confirmar a senha!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                editText_senha.text.toString() == editText_senha_confirmar.text.toString() &&
                        editText_email.text.isNotBlank() -> {
                    val params = HashMap<String, String>()

                    params["perfil"] = perfil
                    params["email"] = editText_email.text.toString()
                    params["senha"] = editText_senha.text.toString()

                    val queue = Volley.newRequestQueue(requireContext())

                    val request = object : StringRequest(
                        Method.POST, "$myServerURL/cadastrar",
                        Response.Listener { response ->
                            val msg = response.toString()

                            if (msg == "invalid_email") {
                                Snackbar.make(
                                    layout_criar_conta, "Escolha outro e-mail!",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                val prefs =
                                    PreferenceManager.getDefaultSharedPreferences(requireContext())

                                val editor = prefs.edit()

                                editor.putString("Token", msg)
                                editor.putString("Perfil", perfil)
                                editor.putString("Email", editText_email.text.toString())
                                editor.putBoolean("CriandoConta", true)

                                editor.apply()

                                val fbToken = prefs.getString("FBtoken", "")!!

                                when (perfil) {
                                    "unidade_saude" -> {
                                        sendFirebaseToken(queue, msg, fbToken)

                                        mController.navigate(R.id.action_criarConta_to_dadosUnidadeSaude)
                                    }

                                    "unidade_manutencao" -> {
                                        sendFirebaseToken(queue, msg, fbToken)

                                        mController.navigate(R.id.action_criarConta_to_dadosUnidadeManutencao)
                                    }
                                }
                            }
                        },
                        Response.ErrorListener {
                            Log.d(LOGTAG, "failed request: $it")

                            connectionErrorMessage(layout_criar_conta, it)
                        }) {
                        override fun getParams(): MutableMap<String, String> {
                            return params
                        }
                    }

                    queue.add(request)
                }
            }
        }
    }

    companion object {
        const val LOGTAG = "criar conta"
    }
}
