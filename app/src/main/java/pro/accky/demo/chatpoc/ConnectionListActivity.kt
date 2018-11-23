package pro.accky.demo.chatpoc

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_connection_list.*
import kotlinx.android.synthetic.main.item_connection.view.*
import pro.accky.demo.chatpoc.model.Message

val Int.stringUserId get() = "user_$this"
val Int.stringConnectionId get() = "connection_$this"

inline fun <reified T> DataSnapshot.extractValue() = getValue(T::class.java)

data class ConnectionWithLastMessage(
    val name: String,
    val msg: Message
)

fun DatabaseReference.loadSingle(
    errorHandler: ((DatabaseError) -> Unit)? = null,
    resultHandler: (DataSnapshot) -> Unit
) = addListenerForSingleValueEvent(object : ValueEventListener {
    override fun onCancelled(error: DatabaseError) = errorHandler?.invoke(error) ?: Unit
    override fun onDataChange(snapshot: DataSnapshot) = resultHandler.invoke(snapshot)
})

fun logd(msg: Any?) = Log.d("AAAAAA", msg.toString())

class ConnectionListActivity : AppCompatActivity() {

    companion object {
        val key_user by key()
    }

    private val userId get() = intent.getIntExtra(key_user, 2)
    private val adapter = ConnectionsAdapter(this::onItemClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_list)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val reference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId.stringUserId)

        reference.loadSingle { ds ->
            // todo not tracking changes at the moment
            val connectionsRoot = ds.child("connections")
            connectionsRoot.children
                .map { connection ->
                    val connectionName = connection.key ?: return@map null
                    val msg = connection.child("last_message").extractValue<Message>() ?: return@map null
                    ConnectionWithLastMessage(connectionName, msg)
                }
                .filterNotNull()
                .forEach {
                    logd(it)
                    adapter.addConnection(it)
                }
        }
    }

    private fun onItemClick(connectionId: String) = ChatActivity.startChat(this, connectionId)

    fun authenticate() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val token = "token" // todo request token from backend
            auth.signInWithCustomToken(token).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    // todo - can use chat
                }
            }
        }
    }

    fun signOutFromFirebase() = FirebaseAuth.getInstance().signOut()
}

class ConnectionsAdapter(private val onclick: (String) -> Unit) : RecyclerView.Adapter<ConnectionsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(connection: ConnectionWithLastMessage, onclick: (String) -> Unit) {
            itemView.title.text = """
                ${connection.name}
                ${connection.msg}
            """.trimIndent()
            itemView.setOnClickListener {
                onclick(connection.name)
            }
        }
    }

    private val connectionList = mutableListOf<ConnectionWithLastMessage>()

    fun addConnection(connection: ConnectionWithLastMessage) {
        connectionList.add(connection)
        notifyItemInserted(connectionList.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = connectionList.size
    override fun onBindViewHolder(viewHolder: ConnectionsAdapter.ViewHolder, pos: Int) =
        viewHolder.bind(connectionList[pos], onclick)
}