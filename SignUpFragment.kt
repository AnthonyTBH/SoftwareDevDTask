package com.example.chucksgourmet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class SignUpFragment : Fragment() {
    lateinit var etSignUpEmail: EditText
    lateinit var etSignUpPassword: EditText
    lateinit var etSignUpConfirmPassword: EditText
    lateinit var btnSignUp: Button
    lateinit var btnExistingAccount:Button

    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        initUI(view)
        handleEvents()
        return view
    }
    private fun handleEvents() {
        btnSignUp.setOnClickListener {
            if (validateSignUp()) {
                val email = etSignUpEmail.text.toString()
                val password = etSignUpPassword.text.toString()

                // Check if email already exists in Firestore
                db.collection("user").whereEqualTo("email", email).get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // Email does not exist, proceed with sign-up
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Sign-up process successful
                                        val user = FirebaseAuth.getInstance().currentUser
                                        val userId = user?.uid ?: ""
                                        val userMap = hashMapOf(
                                            "email" to email,
                                            "password" to password
                                        )

                                        // Save user information to Firestore
                                        db.collection("user").document(userId).set(userMap)
                                            .addOnSuccessListener {
                                                val bundle = Bundle().apply {
                                                    putString("signUpName", etSignUpEmail.text.toString())
                                                    putString("signUpPassword", etSignUpPassword.text.toString())
                                                }
                                                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment, bundle)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(requireContext(), "Failed to add User", Toast.LENGTH_SHORT).show()
                                            }

                                        // Navigate to next screen or perform any other actions
                                    } else {
                                        // Sign-up process failed
                                        Toast.makeText(requireContext(), "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // Email already exists
                            Toast.makeText(requireContext(), "Email already in use", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Error querying Firestore
                        Toast.makeText(requireContext(), "Error checking email: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        btnExistingAccount.setOnClickListener {
            it.findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun initUI(view:View) {
        etSignUpEmail=view.findViewById(R.id.etSignUpEmail)
        etSignUpPassword=view.findViewById(R.id.etSignUpPassword)
        etSignUpConfirmPassword=view.findViewById(R.id.etSignUpConfirmPassword)
        btnSignUp=view.findViewById(R.id.btnSignUp)
        btnExistingAccount = view.findViewById(R.id.btnExistingAccount)
    }

    private fun validateSignUp():Boolean{
        val email = etSignUpEmail.text.toString()
        val password = etSignUpPassword.text.toString()
        val confirmPassword = etSignUpConfirmPassword.text.toString()

        if (email.isEmpty()) {
            // Email field is empty
            etSignUpEmail.error = "Email cannot be empty"
            return false
        }

        if (password.isEmpty()) {
            // Password field is empty
            etSignUpPassword.error = "Password cannot be empty"
            return false
        }

        if (confirmPassword.isEmpty()) {
            // Confirm password field is empty
            etSignUpConfirmPassword.error = "Confirm password cannot be empty"
            return false
        }

        if (password != confirmPassword) {
            // Password and confirm password do not match
            etSignUpConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }
}