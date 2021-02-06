package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wwmm.sostecsaude.R
import kotlinx.android.synthetic.main.fragment_tab_unidade_saude.*


class TabUnidadeSaude : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_unidade_saude, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments.apply {
            textView_unidade_saude.text = this!!.getString("Nome").toString()
            textView_local.text = this.getString("Local").toString()
        }
    }
}
