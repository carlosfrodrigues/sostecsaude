package com.wwmm.sostecsaude.ui.administration.senhas

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_admin_alterar_senha_usuarios.*
import org.json.JSONArray

class AlterarSenhaUsuarios : Fragment() {
    private lateinit var mController: NavController
    private lateinit var mPrefs: SharedPreferences
    private lateinit var mQueue: RequestQueue
    private val userList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_alterar_senha_usuarios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mController = findNavController()

        mPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        mQueue = Volley.newRequestQueue(requireContext())

        getUsersList()

        button_alterar_senha.setOnClickListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager?

            imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

            val novaSenha = editText_nova_senha.text.toString()

            when {
                novaSenha.length < 6 -> {
                    Snackbar.make(
                        layout_alterar_senha_usuarios, "A senha deve ter pelo menos 6 caracteres!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    progressBar.visibility = View.VISIBLE

                    val params = HashMap<String, String>()

                    params["token"] = mPrefs.getString("Token", "")!!
                    params["email_usuario"] = spinner_usuarios.selectedItem.toString()
                    params["nova_senha"] = novaSenha

                    val request = object : StringRequest(
                        Request.Method.POST, "$myServerURL/alterar_senha",
                        Response.Listener { response ->
                            if (isAdded) {
                                when (response) {
                                    "invalid_token", "perfil_invalido" -> {
                                        mController.navigate(R.id.action_global_fazerLogin)
                                    }

                                    else -> {
                                        editText_nova_senha.text.clear()

                                        Snackbar.make(
                                            layout_alterar_senha_usuarios, "Senha alterada!",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
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

                            connectionErrorMessage(layout_alterar_senha_usuarios, it)
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

    private fun getUsersList() {
        val token = mPrefs.getString("Token", "")!!

        val jsonToken = JSONArray()

        jsonToken.put(0, token)

        val request = JsonArrayRequest(
            Request.Method.POST,
            "$myServerURL/lista_usuarios",
            jsonToken,
            Response.Listener { response ->
                if (isAdded) {
                    if (response.length() > 0) {
                        if (response[0] == "invalid_token") {
                            mController.navigate(R.id.action_global_fazerLogin)
                        } else {
                            for (n in 0 until response.length()) {
                                val email = response[n] as String

                                if(email.isNotBlank()) {
                                    userList.add(email)
                                }
                            }

                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_dropdown_item, userList
                            )

                            spinner_usuarios.adapter = adapter

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

                connectionErrorMessage(layout_alterar_senha_usuarios, it)
            }
        )

        mQueue.add(request)
    }

    companion object {
        const val LOGTAG = "AlterarSenhaUsuarios"
    }
}
