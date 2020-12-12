package nl.natalya.roomreservation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.data.Job
import nl.natalya.roomreservation.data.User
import nl.natalya.roomreservation.databinding.FragmentSignUpBinding
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.factory.CalendarAPIFactory
import nl.natalya.roomreservation.newObjectsToSend.NewUser
import nl.natalya.roomreservation.viewmodels.UserViewModel


class SignUpFragment : DialogFragment() {

    private lateinit var signUpBinding: FragmentSignUpBinding
    private val sharedUserViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        signUpBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)

        return signUpBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signUpBinding.createAccount.setOnClickListener {
            val name = signUpBinding.nameInput.text.toString()
            val surname = signUpBinding.surnameInput.text.toString()
            val email = signUpBinding.emailField.text.toString()
            val password = signUpBinding.password.text.toString()
            val newUser = NewUser(name, surname, email, password)

            if (name.isNotBlank() && surname.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                CalendarAPIFactory().getCalendarAPI(APIFactory.ASP).createNewAccount(newUser)
                    .thenApply { currentUser ->
                        showToastMessage("Account was created!")
                        sharedUserViewModel.signIn(currentUser.userEmail, password, APIFactory.ASP)
                    }.exceptionally {
                        showToastMessage(it.cause?.message.toString())
                    }
            }
            else {
                showToastMessage("Please fill in all the fields!")
            }
        }
    }

    private fun showToastMessage(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    companion object {
        fun newInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }
}