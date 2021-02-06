package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wwmm.sostecsaude.R
import kotlinx.android.synthetic.main.fragment_tab_defeito.*

class TabDefeito : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_defeito, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments.apply {
            editText_defeito.text = this!!.getString("Defeito").toString()
            editText_quantidade.text = this.getString("Quantidade").toString()
        }
    }
}
