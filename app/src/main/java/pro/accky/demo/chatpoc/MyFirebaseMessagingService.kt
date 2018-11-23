package pro.accky.demo.chatpoc

import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String?) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // todo
    }
}
