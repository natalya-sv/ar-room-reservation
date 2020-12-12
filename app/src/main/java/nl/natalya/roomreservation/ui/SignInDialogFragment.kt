package nl.natalya.roomreservation.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import nl.natalya.roomreservation.MainActivity
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.data.CurrentUser
import nl.natalya.roomreservation.databinding.FragmentSignInBinding
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.viewmodels.UserViewModel

class SignInDialogFragment : DialogFragment() {

    private lateinit var fragmentSignInBinding: FragmentSignInBinding
    private val sharedUserViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentSignInBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_in, container, false)

        //prevents going back to home page if not logged in
        fragmentSignInBinding.root.isFocusableInTouchMode = true
        fragmentSignInBinding.root.requestFocus()
        fragmentSignInBinding.root.setOnKeyListener { v, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK
        }

        return fragmentSignInBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentSignInBinding.signIn.setOnClickListener {
            val email = fragmentSignInBinding.email.text.toString()
            val password = fragmentSignInBinding.password.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                sharedUserViewModel.signIn(email, password, APIFactory.ASP)
            }
            else {
                showToastMessage("Please fill in all the fields!")
            }
        }
        fragmentSignInBinding.signUp.setOnClickListener {
            val signIn = SignUpFragment()
            val transaction = childFragmentManager.beginTransaction()
            transaction.addToBackStack(null)
            signIn.show(transaction, "")
        }

        sharedUserViewModel.currentUser.observe(viewLifecycleOwner, Observer<CurrentUser> { user ->
            if (user != null) {
                dialog?.dismiss()
                (activity as MainActivity).startApplication()
            }
            else {
                showToastMessage("Log in failed!")
            }
        })
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    fun newInstance(): SignInDialogFragment {
        return SignInDialogFragment()
    }
}