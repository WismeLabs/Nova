package com.wisme.nova.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.text.isEmpty

class AuthViewModel: ViewModel () {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState= MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init{   //run at the start of the app to check wether user is logged in or not
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value=AuthState.Unauthenticated
        }else{
            _authState.value=AuthState.Authenticated
        }
    }

    fun login(email:String, password:String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value=AuthState.Error("Email or Password cant be empty")
            return
        }
        _authState.value=AuthState.Loading  //Set it to loading initially to give firebase some time to process
        auth.signInWithEmailAndPassword(email,password) //the sign in function
            .addOnCompleteListener {task->  //this returns a task that helps to figure out success or failure
                if(task.isSuccessful){  //upon success, authenticate the user
                    _authState.value=AuthState.Authenticated
                }
                else{                   //upon failure, display error message
                    _authState.value=AuthState.Error(task.exception?.message ?: "Unknown Error") //because the task.exception can be null so give it a default value
                }
            }
    }

    fun signup(email:String, password:String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value=AuthState.Error("Email or Password cant be empty")
            return
        }
        _authState.value=AuthState.Loading  //Set it to loading initially to give firebase some time to process
        auth.createUserWithEmailAndPassword(email,password) //the sign up function
            .addOnCompleteListener {task->  //this returns a task that helps to figure out success or failure
                if(task.isSuccessful){  //upon success, authenticate the user
                    _authState.value=AuthState.Authenticated
                }
                else{                   //upon failure, display error message
                    _authState.value=AuthState.Error(task.exception?.message ?: "Unknown Error") //because the task.exception can be null so give it a default value
                }
            }
    }

    fun signout(){
        auth.signOut()
        _authState.value=AuthState.Unauthenticated
    }

}

sealed class AuthState{
    object Authenticated: AuthState()
    object Unauthenticated: AuthState()
    object Loading: AuthState()
    data class Error(val message:String): AuthState()
}