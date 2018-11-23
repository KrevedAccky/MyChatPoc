package pro.accky.demo.chatpoc.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    var body: String? = null,
    var sender_id: String? = null,
    var sent_at: Any? = null,
    var type: String? = null
)