package com.elektro.monitoring.data.repo


import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.helper.utils.Resource
import com.elektro.monitoring.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(private val sharedPreferences: SharedPrefData) {

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val fireDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val loggedOutLiveData: MutableLiveData<Boolean> = MutableLiveData()

    init {
        if (firebaseAuth.currentUser != null) {
            loggedOutLiveData.postValue(false)
        }
    }

    fun login(email: String, password: String): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit((result.user?.let {
                sharedPreferences.saveData("email", email)
                sharedPreferences.saveData("password", password)
                Resource.Success(data = it)
            }!!))
            loggedOutLiveData.postValue(false)
        } catch (e: HttpException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Unknown Error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Check Your Internet Connection"))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Email atau Password salah"))
        }
    }

    fun logOut() {
        firebaseAuth.signOut()
        sharedPreferences.clearAuth()
        loggedOutLiveData.postValue(true)
    }

    fun getLoggedUser(): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        if (firebaseAuth.currentUser != null) {
            loggedOutLiveData.postValue(false)
            emit(Resource.Success(data = firebaseAuth.currentUser!!))
        } else {
            emit(Resource.Error("Not Logged"))
        }
    }

    fun getUserData(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        if (firebaseAuth.currentUser != null) {
            try {
                val snapshot = fireDatabase.getReference("users")
                    .child(firebaseAuth.currentUser!!.uid).get().await()
                if (snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)
                    emit(Resource.Success(data = user!!))
                }
            } catch (e: HttpException) {
                emit(Resource.Error(message = e.localizedMessage ?: "Unknown Error"))
            } catch (e: IOException) {
                emit(Resource.Error(message = e.localizedMessage ?: "Check Your Internet Connection"))
            } catch (e: Exception) {
                emit(Resource.Error(message = e.localizedMessage ?: ""))
            }
        }
    }

    fun changePassword(email: String, currentPassword: String, newPassword: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            val reAuth = firebaseAuth.currentUser!!.reauthenticate(credential).await()
            reAuth.let {
                firebaseAuth.currentUser!!.updatePassword(newPassword).await()
                sharedPreferences.saveData("password", newPassword)
                emit(Resource.Success(data = true))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Unknown Error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Check Your Internet Connection"))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: ""))
        }
    }

    fun register(email: String, password: String, uri: Uri, name: String, nomor: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuth.signOut()
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.let { val upload = firebaseStorage.reference.child("users")
                .child(result.user!!.uid).child("profile.jpg").putFile(uri).await()
                upload.let {
                    val download = firebaseStorage.reference.child("users")
                        .child(result.user!!.uid).child("profile.jpg").downloadUrl.await()
                    download.let {
                        val data = User(email, download.toString(), name, nomor)
                        val addDatabase = fireDatabase.getReference("users")
                            .child(result.user!!.uid).setValue(data).await()
                        addDatabase.let {
                            val emailAdmin = sharedPreferences.loadData("email")
                            val passwordAdmin = sharedPreferences.loadData("password")
                            firebaseAuth.signOut()
                            firebaseAuth.signInWithEmailAndPassword(emailAdmin, passwordAdmin)
                            emit(Resource.Success(data= true))
                        }
                    }
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Unknown Error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Check Your Internet Connection"))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: ""))
        }
    }

    fun changeData(email: String, uri: Uri, name: String, nomor: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUser = firebaseAuth.currentUser!!.uid
            val upload = firebaseStorage.reference.child("users")
                .child(currentUser).child("profile.jpg").putFile(uri).await()
                upload.let {
                    val download = firebaseStorage.reference.child("users")
                        .child(currentUser).child("profile.jpg").downloadUrl.await()
                    download.let {
                        val data = User(email, download.toString(), name, nomor)
                        fireDatabase.getReference("users")
                            .child(currentUser).setValue(data).await()
                        emit(Resource.Success(data= true))
                    }
                }
        } catch (e: HttpException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Unknown Error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Check Your Internet Connection"))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: ""))
        }
    }

    fun changeDataNoUri(email: String, image: String, name: String, nomor: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val currentUser = firebaseAuth.currentUser!!.uid
            val data = User(email, image, name, nomor)
            fireDatabase.getReference("users")
                .child(currentUser).setValue(data).await()
            emit(Resource.Success(data= true))

        } catch (e: HttpException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Unknown Error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Check Your Internet Connection"))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: ""))
        }
    }
}