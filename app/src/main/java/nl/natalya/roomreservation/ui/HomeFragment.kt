package nl.natalya.roomreservation.ui

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import nl.natalya.roomreservation.MainActivity
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.data.CurrentUser
import nl.natalya.roomreservation.databinding.FragmentHomeBinding
import nl.natalya.roomreservation.viewmodels.UserViewModel

class HomeFragment : Fragment() {
    private lateinit var homeBinding: FragmentHomeBinding
    private val sharedUserViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeBinding.currentUserModel = sharedUserViewModel

        sharedUserViewModel.currentUser.observe(viewLifecycleOwner, Observer<CurrentUser> { user ->
            if (user != null) {
                (activity as MainActivity).showUI()
                homeBinding.welcomeUserText.visibility = View.VISIBLE
                homeBinding.username.visibility = View.VISIBLE
                homeBinding.explanation.visibility = View.VISIBLE
                homeBinding.username.text = getString(R.string.welcome_string).plus(user.userName)
            }
        })
    }

    companion object {

        fun newInstance(): HomeFragment{
            return HomeFragment()
        }
    }
}


