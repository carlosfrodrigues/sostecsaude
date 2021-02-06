package com.wwmm.sostecsaude.ui.login

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.wwmm.sostecsaude.R
import com.wwmm.sostecsaude.connectionErrorMessage
import com.wwmm.sostecsaude.myServerURL
import com.wwmm.sostecsaude.sendFirebaseToken
import kotlinx.android.synthetic.main.fragment_check_credentials.*

class CheckCredentials : Fragment() {
    private lateinit var mMyPrefs: SharedPreferences
    private lateinit var mQueue: RequestQueue
    private lateinit var mController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_check_credentials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mController = findNavController()

        toolbar.setupWithNavController(mController)

        mMyPrefs = getDefaultSharedPreferences(requireContext())

        mQueue = Volley.newRequestQueue(requireContext())

        checkCredentials()

        button_retry.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            checkCredentials()
        }
    }

    private fun checkCredentials() {
        val request = object : StringRequest(
            Method.POST, "$myServerURL/check_credentials",
            Response.Listener { response ->
                val msg = response.toString()

                if (msg == "invalid_token") {
                    mController.navigate(R.id.action_checkCredentials_to_login)
                } else {
                    val arr = msg.split("<&>")

                    if (arr.size == 3) {
                        val token = arr[0]
                        val perfil = arr[1]
                        val email = arr[2]

                        val editor = mMyPrefs.edit()

                        editor.putString("Token", token)
                        editor.putString("Perfil", perfil)
                        editor.putString("Email", email)

                        editor.apply()

                        val fbToken = mMyPrefs.getString("FBtoken", "")!!

                        when (perfil) {
                            "unidade_saude" -> {
                                sendFirebaseToken(mQueue, token, fbToken)

                                mController.navigate(R.id.action_checkCredentials_to_unidadeSaude)
                            }

                            "unidade_manutencao" -> {
                                sendFirebaseToken(mQueue, token, fbToken)

                                mController.navigate(R.id.action_checkCredentials_to_unidadeManutencao)
                            }

                            "administrador" -> {
                                mController.navigate(R.id.action_checkCredentials_to_homeAdministration)
                            }
                        }
                    }
                }
            },
            Response.ErrorListener {
                Log.d(LOGTAG, "request failed: $it")

                progressBar.visibility = View.GONE

                connectionErrorMessage(layout_check_credentials, it)
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()

                params["token"] = mMyPrefs.getString("Token", "nao existe token")!!

                return params
            }
        }

        mQueue.add(request)
    }

    companion object {
        const val LOGTAG = "check credentials"
    }
}
