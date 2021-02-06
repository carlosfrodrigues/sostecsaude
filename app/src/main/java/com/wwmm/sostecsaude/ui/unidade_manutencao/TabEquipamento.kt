package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wwmm.sostecsaude.R
import kotlinx.android.synthetic.main.fragment_tab_equipamento.*

class TabEquipamento : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_equipamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments.apply {
            val id = this?.getString("ID").toString()

            textView_equipamento.text = this!!.getString("Nome").toString()
            editText_fabricante.text = this.getString("Fabricante").toString()
            editText_modelo.text = this.getString("Modelo").toString()
            editText_numero_serie.text = this.getString("NumeroSerie").toString()
            textView_id_sistema.text = "EQ$id"
        }
    }
}
