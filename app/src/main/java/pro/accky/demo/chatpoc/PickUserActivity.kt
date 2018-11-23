package pro.accky.demo.chatpoc

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_pick_user.*

class PickUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_user)

        val usersArray = arrayOf(1, 2)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, usersArray).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.setSelection(0)
        ok_button.setOnClickListener {
            val i = Intent(this, ConnectionListActivity::class.java)
            i.putExtra(ConnectionListActivity.key_user, usersArray[spinner.selectedItemPosition])
            startActivity(i)
        }
    }
}