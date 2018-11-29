package pro.accky.demo.chatpoc

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_connection_list.*
import kotlinx.android.synthetic.main.item_connection.view.*
import pro.accky.demo.chatpoc.model.Message
import pro.accky.demo.chatpoc.network.AuthService
import pro.accky.demo.chatpoc.network.FirebaseToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

inline fun <reified T> DataSnapshot.extractValue() = getValue(T::class.java)
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

data class ConnectionWithLastMessage(
    val name: String,
    val msg: Message
)

open class ChildEventListenerAdapter : ChildEventListener {
    override fun onCancelled(snapshot: DatabaseError) = Unit
    override fun onChildMoved(snapshot: DataSnapshot, prev: String?) = Unit
    override fun onChildChanged(snapshot: DataSnapshot, prev: String?) = Unit
    override fun onChildAdded(snapshot: DataSnapshot, prev: String?) = Unit
    override fun onChildRemoved(snapshot: DataSnapshot) = Unit
}

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
    private val userStringId get() = "u_$userId"
    private val adapter = ConnectionsAdapter(this::onItemClick)

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_list)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uid = userId.toString()
        when {
            user == null -> authenticateAndConnect(uid)
            user.uid != uid -> authenticateAndConnect(uid)
            else -> connectToFirebaseDatabase()
        }
    }

    private fun authenticateAndConnect(uid: String) {
        AuthService.getAuthToken(uid).enqueue(object : Callback<FirebaseToken?> {
            override fun onFailure(call: Call<FirebaseToken?>, t: Throwable) =
                toast("Can't receive Firebase auth token")

            override fun onResponse(call: Call<FirebaseToken?>, response: Response<FirebaseToken?>) {
                if (response.isSuccessful) {
                    response.body()?.token?.let { token ->
                        auth.signInWithCustomToken(token).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                connectToFirebaseDatabase()
                            }
                        }
                    }
                } else {
                    toast("Can't receive Firebase auth token: ${response.code()}")
                }
            }
        })
    }

    private fun connectToFirebaseDatabase() {
        val connectionsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userStringId)
            .child("connections")

        connectionsRef.addChildEventListener(object : ChildEventListenerAdapter() {
            override fun onChildChanged(snapshot: DataSnapshot, prev: String?) {
                snapshot.child("last_message").extractValue<Message>()?.let { msg ->
                    snapshot.key?.let { connectionId ->
                        adapter.updateConnection(connectionId, msg)
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, prev: String?) {
                val connectionId = snapshot.key
                snapshot.child("last_message").extractValue<Message>()?.let { msg ->
                    adapter.addConnection(ConnectionWithLastMessage(connectionId.orEmpty(), msg))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                snapshot.key?.let { connectionId ->
                    adapter.removeConnection(connectionId)
                }
            }
        })
    }

    private fun onItemClick(connectionId: String) = ChatActivity.startChat(this, connectionId)

    fun signOutFromFirebase() = auth.signOut()
}

class ConnectionsAdapter(private val onclick: (String) -> Unit) :
    RecyclerView.Adapter<ConnectionsAdapter.ViewHolder>() {
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

    fun removeConnection(connectionId: String) {
        connectionList.firstOrNull { it.name == connectionId }?.let {
            val index = connectionList.indexOf(it)
            connectionList.remove(it)
            notifyItemRemoved(index)
        }
    }

    fun updateConnection(connectionId: String, msg: Message) {
        val idx = connectionList.indexOfFirst { it.name == connectionId }
        if (idx != -1) {
            connectionList[idx] = ConnectionWithLastMessage(connectionId, msg)
            notifyItemChanged(idx)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = connectionList.size
    override fun onBindViewHolder(viewHolder: ConnectionsAdapter.ViewHolder, pos: Int) =
        viewHolder.bind(connectionList[pos], onclick)
}