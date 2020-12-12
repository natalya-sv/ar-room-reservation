package nl.natalya.roomreservation.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import nl.natalya.roomreservation.MainActivity
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.data.CurrentUser
import nl.natalya.roomreservation.databinding.FragmentProfileBinding
import nl.natalya.roomreservation.viewmodels.UserViewModel

class ProfileFragment : Fragment() {

    lateinit var profileBinding: FragmentProfileBinding
    private val sharedUserViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        profileBinding.settings.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileBinding.profileModel = sharedUserViewModel
        profileBinding.signOut.visibility = View.VISIBLE

        profileBinding.signOut.setOnClickListener {
            sharedUserViewModel.signUserOut()
            signOut()
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun signOut() {
        activity?.runOnUiThread {
            val intent = Intent(activity, MainActivity::class.java)
            this.startActivity(intent)
            (activity as MainActivity).finishAffinity()
        }
        showToastMessage("You have signed out...")
    }

    companion object {
        fun newInstance(): ProfileFragment{
            return ProfileFragment()
        }
    }
}