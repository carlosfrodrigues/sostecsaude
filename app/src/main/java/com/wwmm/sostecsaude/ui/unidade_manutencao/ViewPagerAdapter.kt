package com.wwmm.sostecsaude.ui.unidade_manutencao

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                VerPedidos()
            }

            1 -> {
                StatusReparo()
            }

            2 -> {
                Relatorio()
            }

            else -> {
                VerPedidos()
            }
        }
    }
}