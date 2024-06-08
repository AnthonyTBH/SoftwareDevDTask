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

class LoginFragment : Fragment() {
    lateinit var btnLogin: Button
    lateinit var btnNoAccount: Button
    lateinit var etLoginEmail: EditText
    lateinit var etLoginPassword: EditText
    private var db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        initUI(view)
        handleEvents()
        arguments?.let{
            val signUpName = it.getString("signUpName")
            val signUpPassword = it.getString("signUpPassword")

            etLoginEmail.setText(signUpName)
            etLoginPassword.setText(signUpPassword)
        }
        return view
    }
    private fun handleEvents() {
        btnLogin.setOnClickListener{
            handleLogin()
        }
        btnNoAccount.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private fun handleLogin() {
        val email = etLoginEmail.text.toString()
        val password = etLoginPassword.text.toString()

        if (email == "admin@gmail.com" && password == "anthony") {
            // Navigate to the admin dashboard if the admin credentials are correct
            findNavController().navigate(R.id.action_loginFragment_to_adminDashboardFragment)
        } else {
            // Query Firestore for user data
            db.collection("user")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // User with the provided email exists
                        for (document in documents) {
                            val storedPassword = document.getString("password")
                            if (storedPassword == password) {
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                findNavController().navigate(R.id.action_loginFragment_to_menuFragment)
                            } else {
                                // Password does not match
                                Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // No user with the provided email found
                        Toast.makeText(requireContext(), "User does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Error querying Firestore
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun initUI(view:View) {
        btnLogin = view.findViewById(R.id.btnLogin)
        btnNoAccount = view.findViewById(R.id.btnNoAccount)
        etLoginEmail = view.findViewById(R.id.etLoginName)
        etLoginPassword = view.findViewById(R.id.etLoginPassword)
    }
}