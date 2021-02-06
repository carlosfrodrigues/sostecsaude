package com.wwmm.sostecsaude

import android.util.Log
import android.view.View
import com.android.volley.NetworkError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.google.android.material.snackbar.Snackbar

const val myServerURL = "http://119.8.81.103:8081"
//const val myServerURL = "http://albali.eic.cefet-rj.br:8081"
//const val myServerURL = "http://192.168.1.106:8081"

fun connectionErrorMessage(view: View, volleyError: VolleyError) {
    if (volleyError is NetworkError) {
        Snackbar.make(
            view,
            "Sem conexão com o servidor!",
            Snackbar.LENGTH_LONG
        ).show()
    }
}

fun sendFirebaseToken(queue: RequestQueue, token: String, fbToken: String) {
    val request = object : StringRequest(
        Method.POST, "$myServerURL/update_fb_token",
        null,
        Response.ErrorListener {
            Log.d("sendFirebaseToken", "failed request: $it")
        }
    ) {
        override fun getParams(): MutableMap<String, String> {
            val params = HashMap<String, String>()

            params["token"] = token
            params["fb_token"] = fbToken

            return params
        }
    }

    queue.add(request)
}

fun getEstadoString(estado: Int): String {
    return when (estado) {
        0 -> "Aguardando aceite"
        1 -> "Aceito"
        2 -> "Pronto para retirada"
        3 -> "Retirado"
        4 -> "Recebido"
        5 -> "Triagem"
        6 -> "Manutenção"
        7 -> "Higienização"
        8 -> "Saiu para entrega"
        9 -> "Recebido"
        else -> estado.toString()
    }
}
