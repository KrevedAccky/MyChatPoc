package pro.accky.demo.chatpoc.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(
    var body: String? = null,
    var s_id: String? = null,
    var sent: Any? = null,
    var type: String? = null
)