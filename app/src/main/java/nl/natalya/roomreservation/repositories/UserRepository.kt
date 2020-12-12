package nl.natalya.roomreservation.repositories

import androidx.lifecycle.MutableLiveData
import nl.natalya.roomreservation.data.CurrentUser
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.factory.CalendarAPIFactory

class UserRepository {

    fun signIn(email: String, password: String, currentUser: MutableLiveData<CurrentUser>, api: APIFactory) {
        CalendarAPIFactory().getCalendarAPI(api).getUserByUserEmail(email, password).thenApply { user ->
            currentUser.postValue(user)
        }?.exceptionally {
            currentUser.postValue(null)
        }
    }

    fun signOut(currentUser: MutableLiveData<CurrentUser>) {
        currentUser.postValue(null)
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            synchronized(this) {
                if (instance == null) {
                    instance = UserRepository()
                }
                return instance as UserRepository
            }
        }
    }
}