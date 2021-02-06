package com.wwmm.sostecsaude.ui.unidade_saude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayoutMediator
import com.wwmm.sostecsaude.R
import kotlinx.android.synthetic.main.fragment_unidade_saude.*

class UnidadeSaude : Fragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var mController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unidade_saude, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mController = findNavController()

        toolbar.title = getString(R.string.title_equipamentos)
        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_search).isVisible = false
        toolbar.setOnMenuItemClickListener(this)

        viewpager.isUserInputEnabled = false
        viewpager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tab_layout, viewpager) { tab, tabIdx ->
            when (tabIdx) {
                0 -> {
                    tab.text = getString(R.string.title_registro)
                    tab.setIcon(R.drawable.ic_view_list)
                }

                1 -> {
                    tab.text = getString(R.string.title_ofertas)
                    tab.setIcon(R.drawable.ic_build)
                }

                2 -> {
                    tab.text = getString(R.string.title_relatorio)
                    tab.setIcon(R.drawable.ic_pdf)
                }
            }
        }.attach()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

                val editor = prefs.edit()

                editor.putString("Token", "")
                editor.putString("Perfil", "")
                editor.putString("Email", "")

                editor.apply()

                mController.navigate(R.id.action_global_fazerLogin)

                return true
            }

            R.id.menu_atualizar_perfil -> {
                mController.navigate(R.id.action_global_unidadeSaude)

                return true
            }

            R.id.menu_alterar_senha -> {
                mController.navigate(R.id.action_global_alterarSenha)

                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        const val LOGTAG = "UnidadeSaude"
    }
}
