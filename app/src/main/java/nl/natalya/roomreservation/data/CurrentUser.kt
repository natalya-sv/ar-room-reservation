package nl.natalya.roomreservation.data

class CurrentUser (var userId: Int, var userName: String, var userEmail: String, var surname: String, var job: Job) {

    companion object {
        @Volatile
        private var currentUser: CurrentUser? = null

        fun getUser(): CurrentUser? {
            synchronized(this) {

                return currentUser
            }
        }

        fun setUser(user: CurrentUser) {
            this.currentUser = user
        }

        fun logOutUser() {
            this.currentUser = null
        }
    }
}



