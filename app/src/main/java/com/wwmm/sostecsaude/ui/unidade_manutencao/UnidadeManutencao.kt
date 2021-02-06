package com.wwmm.sostecsaude.ui.unidade_manutencao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.messaging.FirebaseMessaging
import com.wwmm.sostecsaude.R
import kotlinx.android.synthetic.main.fragment_unidade_manutencao.*

class UnidadeManutencao : Fragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var mActivityController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().subscribeToTopic("pedido_reparo")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unidade_manutencao, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivityController = Navigation.findNavController(requireActivity(), R.id.nav_host_main)

        toolbar.title = getString(R.string.title_equipamentos)
        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_search).isVisible = false
        toolbar.setOnMenuItemClickListener(this)

        viewpager.isUserInputEnabled = false
        viewpager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tab_layout, viewpager) { tab, tabIdx ->
            when (tabIdx) {
                0 -> {
                    tab.text = getString(R.string.title_com_defeito)
                    tab.setIcon(R.drawable.ic_broken_image)
                }

                1 -> {
                    tab.text = getString(R.string.title_estou_consertando)
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

                mActivityController.navigate(R.id.action_global_fazerLogin)

                return true
            }

            R.id.menu_atualizar_perfil -> {
                mActivityController.navigate(R.id.action_global_unidadeManutencao)

                return true
            }

            R.id.menu_alterar_senha -> {
                mActivityController.navigate(R.id.action_global_alterarSenha)

                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        const val LOGTAG = "UnidadeManutencao"
    }
}
