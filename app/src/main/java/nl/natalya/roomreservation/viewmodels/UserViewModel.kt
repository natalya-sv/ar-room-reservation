package nl.natalya.roomreservation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.natalya.roomreservation.data.CurrentUser
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.repositories.UserRepository

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<CurrentUser>()
    val currentUser: LiveData<CurrentUser>
        get() = _currentUser

    fun signIn(email: String, password: String, api: APIFactory) {
        userRepository.signIn(email, password, _currentUser, api)
    }

    fun signUserOut() {
        userRepository.signOut(_currentUser)
    }

    override fun onCleared() {
        super.onCleared()
        _currentUser.value = null
    }
}
