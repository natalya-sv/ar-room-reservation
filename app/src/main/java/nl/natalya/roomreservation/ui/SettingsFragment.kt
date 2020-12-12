package nl.natalya.roomreservation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.data.CurrentUser
import nl.natalya.roomreservation.databinding.FragmentSettingsBinding
import nl.natalya.roomreservation.factory.AspAPI

class SettingsFragment : Fragment() {

    private lateinit var settingsBinding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        settingsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)

        settingsBinding.changePassword.setOnClickListener { view ->

            val email = settingsBinding.emailField.text.toString()
            val oldPassword = settingsBinding.password.text.toString()
            val newPassword = settingsBinding.newPassword.text.toString()

            if (email.isNotBlank() && oldPassword.isNotBlank() && newPassword.isNotBlank() && email == CurrentUser.getUser()?.userEmail) {
                AspAPI().changeUserPassword(email, oldPassword, newPassword).thenApply { result ->
                    if (result) {
                        showToastMessage("Password has been changed!")
                        activity?.runOnUiThread {
                            settingsBinding.emailField.text.clear()
                            settingsBinding.newPassword.text.clear()
                            settingsBinding.password.text.clear()
                            view.findNavController().navigate(R.id.profileFragment)
                        }

                    }
                    else {
                        showToastMessage("Password has NOT been changed!")
                    }



                }.exceptionally {
                    showToastMessage(it.cause?.message.toString())
                }
            }
            else {
                showToastMessage("Please fill in all the fields!")
            }
        }
        return settingsBinding.root
    }

    private fun showToastMessage(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}