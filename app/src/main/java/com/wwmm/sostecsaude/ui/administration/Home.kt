package com.wwmm.sostecsaude.ui.administration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.messaging.FirebaseMessaging
import com.wwmm.sostecsaude.R
import kotlinx.android.synthetic.main.fragment_admin_home.*

class Home : Fragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var mController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().subscribeToTopic("administration")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mController = findNavController()

        toolbar.setupWithNavController(mController)

        toolbar.inflateMenu(R.menu.menu_toolbar)
        toolbar.menu.findItem(R.id.menu_search).isVisible = false
        toolbar.menu.findItem(R.id.menu_atualizar_perfil).isVisible = false
        toolbar.setOnMenuItemClickListener(this)

        viewpager.adapter = Adapter(this)

        TabLayoutMediator(tab_layout, viewpager) { tab, tabIdx ->
            when (tabIdx) {
                0 -> {
                    tab.text = getString(R.string.title_hospitais)
                    tab.setIcon(R.drawable.ic_home_black_24dp)
                }

                1 -> {
                    tab.text = getString(R.string.title_oficinas)
                    tab.setIcon(R.drawable.ic_build)
                }

                2 -> {
                    tab.text = getString(R.string.title_equipamentos)
                    tab.setIcon(R.drawable.ic_scanner)
                }

                3 -> {
                    tab.text = getString(R.string.title_alterar_senha)
                    tab.setIcon(R.drawable.ic_lock_open_black)
                }
            }
        }.attach()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_login -> {
                val prefs =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())

                val editor = prefs.edit()

                editor.putString("Token", "")
                editor.putString("Perfil", "")
                editor.putString("Email", "")

                editor.apply()

                mController.navigate(R.id.action_global_fazerLogin)

                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
