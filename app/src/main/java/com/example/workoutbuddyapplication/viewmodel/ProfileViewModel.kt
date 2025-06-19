package com.example.workoutbuddyapplication.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutbuddyapplication.models.UserProfile
import com.example.workoutbuddyapplication.ui.theme.UserPreferencesManager
import com.example.workoutbuddyapplication.data.SupabaseClient
import com.example.workoutbuddyapplication.data.fetchUserProfile
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val context: Context) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _nameInput = MutableStateFlow("")
    val nameInput: StateFlow<String> = _nameInput

    private val _weightInput = MutableStateFlow("70")
    val weightInput: StateFlow<String> = _weightInput

    private val _weight = MutableStateFlow(70.0)
    val weight: StateFlow<Double> = _weight

    private val _profileSaveMessage = MutableStateFlow<String?>(null)
    val profileSaveMessage: StateFlow<String?> = _profileSaveMessage

    private val _weightSaveMessage = MutableStateFlow<String?>(null)
    val weightSaveMessage: StateFlow<String?> = _weightSaveMessage

    private val preferencesManager = UserPreferencesManager(context)

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    private val _imageVersion = MutableStateFlow(0)
    val imageVersion: StateFlow<Int> = _imageVersion

    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword

    private val _confirmNewPassword = MutableStateFlow("")
    val confirmNewPassword: StateFlow<String> = _confirmNewPassword

    private val _passwordSaveMessage = MutableStateFlow<String?>(null)
    val passwordSaveMessage: StateFlow<String?> = _passwordSaveMessage

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    private val _deletePassword = MutableStateFlow("")
    val deletePassword: StateFlow<String> = _deletePassword

    private val _deleteAccountMessage = MutableStateFlow<String?>(null)
    val deleteAccountMessage: StateFlow<String?> = _deleteAccountMessage

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    private val _showDeleteSuccessScreen = MutableStateFlow(false)
    val showDeleteSuccessScreen: StateFlow<Boolean> = _showDeleteSuccessScreen

    fun setShowDeleteDialog(show: Boolean) {
        _showDeleteDialog.value = show
    }

    fun setDeletePassword(value: String) {
        _deletePassword.value = value
    }

    fun setCurrentPassword(value: String) {
        _currentPassword.value = value
    }

    fun setNewPassword(value: String) {
        _newPassword.value = value
    }

    fun setConfirmNewPassword(value: String) {
        _confirmNewPassword.value = value
    }

    fun setNameInput(value: String) {
        _nameInput.value = value
    }

    fun setWeightInput(value: String) {
        _weightInput.value = value
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    fun setShowDeleteSuccessScreen(show: Boolean) {
        _showDeleteSuccessScreen.value = show
    }

    fun getProfileImageUrl(url: String?, version: Int): String? {
        return url?.let { "$it?v=$version" }
    }

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _weight.value = preferencesManager.getUserWeight()
            _weightInput.value = _weight.value.toString()
            val profile = fetchUserProfile(userId)
            _profile.value = profile
            _nameInput.value = profile?.name ?: ""
            _isLoading.value = false
        }
    }

    fun saveProfile(userId: String) {
        viewModelScope.launch {
            val success = updateUserProfile(userId, _nameInput.value)
            if (success) {
                val updatedProfile = fetchUserProfile(userId)
                _profile.value = updatedProfile
                _nameInput.value = updatedProfile?.name ?: ""
                _profileSaveMessage.value = "Profiel bijgewerkt"
            } else {
                _profileSaveMessage.value = "Bijwerken mislukt"
            }
        }
    }

    fun saveWeight() {
        viewModelScope.launch {
            val w = _weightInput.value.toDoubleOrNull() ?: 70.0
            _weight.value = w
            try {
                preferencesManager.saveUserWeight(w)
                _weightSaveMessage.value = "Gewicht opgeslagen"
            } catch (e: Exception) {
                _weightSaveMessage.value = "Opslaan mislukt"
            }
        }
    }

    fun uploadProfileImage(userId: String, uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    val fileName = "$userId.jpg"
                    SupabaseClient.client.storage.from("profile-pictures")
                        .upload(fileName, bytes, upsert = true)
                    val publicUrl =
                        SupabaseClient.client.storage.from("profile-pictures").publicUrl(fileName)
                    updateUserProfile(userId, _nameInput.value, publicUrl)
                    _profile.value = fetchUserProfile(userId)
                    _imageVersion.value = _imageVersion.value + 1
                    _profileSaveMessage.value = "Profielfoto bijgewerkt"
                }
            } catch (e: Exception) {
                _profileSaveMessage.value = "Uploaden mislukt"
            }
            _imageUri.value = null
        }
    }

    fun changePassword(email: String) {
        viewModelScope.launch {
            _passwordSaveMessage.value = null
            if (_newPassword.value != _confirmNewPassword.value) {
                _passwordSaveMessage.value = "Wachtwoorden komen niet overeen"
                return@launch
            }
            try {
                SupabaseClient.client.auth.signInWith(
                    io.github.jan.supabase.gotrue.providers.builtin.Email
                ) {
                    this.email = email
                    password = _currentPassword.value
                }
                SupabaseClient.client.auth.modifyUser {
                    password = _newPassword.value
                }
                _passwordSaveMessage.value = "Wachtwoord succesvol gewijzigd"
                _currentPassword.value = ""
                _newPassword.value = ""
                _confirmNewPassword.value = ""
            } catch (e: Exception) {
                _passwordSaveMessage.value = "Wachtwoord wijzigen mislukt"
            }
        }
    }

    fun deleteAccount(email: String) {
        viewModelScope.launch {
            _isDeleting.value = true
            _deleteAccountMessage.value = null
            try {
                SupabaseClient.client.auth.signInWith(
                    io.github.jan.supabase.gotrue.providers.builtin.Email
                ) {
                    this.email = email
                    password = _deletePassword.value
                }
                _showDeleteDialog.value = false
                _deletePassword.value = ""
                _showDeleteSuccessScreen.value = true
            } catch (e: Exception) {
                _deleteAccountMessage.value = "Onjuist wachtwoord."
            }
            _isDeleting.value = false
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        name: String,
        pictureUrl: String? = null
    ): Boolean {
        val updateMap = mutableMapOf<String, String>("name" to name)
        if (pictureUrl != null) updateMap["picture"] = pictureUrl
        return try {
            SupabaseClient.client
                .from("profiles")
                .update(updateMap) {
                    filter { eq("id", userId) }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}