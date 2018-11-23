package pro.accky.demo.chatpoc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.item_connection.view.*
import pro.accky.demo.chatpoc.model.Message

class ChatActivity : AppCompatActivity() {

    companion object {
        private val key_connection by key()
        fun startChat(activity: Activity, connectionId: String) {
            val intent = Intent(activity, ChatActivity::class.java).apply {
                putExtra(key_connection, connectionId)
            }
            activity.startActivity(intent)
        }
    }

    private val connectionId: String get() = intent.getStringExtra(key_connection)
    private val adapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        val reference = FirebaseDatabase.getInstance()
            .getReference("messages")
            .child(connectionId)

        // todo not tracking changes at the moment
        reference.loadSingle { ds ->
            ds.children
                .mapNotNull { msg ->
                    msg.extractValue<Message>()
                }
                .forEach {
                    logd(it)
                    adapter.addMessage(it)
                }
        }

        sendButton.setOnClickListener {
            val text = textField.text.toString()
            if(text.isNotBlank()) {
                textField.setText("")
                // todo add text to the firebase
            }
        }
    }
}

class MessagesAdapter : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(message: Message) {
            itemView.title.text = message.toString()
        }
    }

    private val messages = mutableListOf<Message>()

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_connection, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size
    override fun onBindViewHolder(viewHolder: MessagesAdapter.ViewHolder, pos: Int) =
        viewHolder.bind(messages[pos])
}