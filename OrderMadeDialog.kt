import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.example.chucksgourmet.R

class NotificationDialog(context: Context,message:String) : Dialog(context) {
    private var messageTextView: TextView? = null
    private lateinit var dismissButton: Button
    private var myMessage = message
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.notification_dialog)

        // Initialize views after setContentView
        messageTextView = findViewById(R.id.messageTextView)
        messageTextView!!.text = myMessage

        dismissButton = findViewById(R.id.dismissButton)

        // Set up button click listener to dismiss the dialog
        dismissButton.setOnClickListener {
            dismiss()
        }
    }
}
