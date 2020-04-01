package com.wwmm.sostecsaude.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.myServerURL
import kotlinx.android.synthetic.main.fragment_login.*

class Login : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val controller = findNavController()

        val bottomNav = requireActivity().findViewById(R.id.bottom_nav) as BottomNavigationView

        bottomNav.visibility = View.GONE

        val prefs = requireActivity().getSharedPreferences(
            "UserInfo",
            0
        )

        button_login.setOnClickListener {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager?

            imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

            when {
                editText_email.text.isBlank() -> {
                    Snackbar.make(
                        layout_login, "Digite um e-mail!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                editText_senha.text.isBlank() -> {
                    Snackbar.make(
                        layout_login, "Digite uma senha!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                editText_email.text.isNotBlank() && editText_senha.text.isNotBlank() -> {
                    val queue = Volley.newRequestQueue(requireContext())

                    val request = object : StringRequest(
                        Method.POST, "$myServerURL/login",
                        Response.Listener { response ->
                            val msg = response.toString()

                            val arr = msg.split("<&>")

                            if (arr.size == 3) {
                                val token = arr[0]
                                val perfil = arr[1]
                                val email = arr[2]

                                val editor = prefs.edit()

                                editor.putString("Token", token)
                                editor.putString("Perfil", perfil)
                                editor.putString("Email", email)

                                editor.apply()

                                controller.navigate(R.id.action_login_to_carregarPerfil)
                            }else{
                                Snackbar.make(
                                    layout_login, msg,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        },
                        Response.ErrorListener { Log.d(LOGTAG, "failed request: criar conta") }) {
                        override fun getParams(): MutableMap<String, String> {
                            val params = HashMap<String, String>()

                            params["email"] = editText_email.text.toString()
                            params["senha"] = editText_senha.text.toString()

                            return params
                        }
                    }

                    queue.add(request)
                }
            }
        }

        button_cadastro.setOnClickListener {
            controller.navigate(R.id.action_login_to_criarConta)
        }
    }

    companion object {
        const val LOGTAG = "login"
    }
}
