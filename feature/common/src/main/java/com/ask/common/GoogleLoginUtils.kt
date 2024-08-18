package com.ask.common

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

fun getCredentialRequest(context: Context): GetCredentialRequest {
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it ->
        str + "%02x".format(it)
    }
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.client_id))
        .setAutoSelectEnabled(true)
        .setNonce(hashedNonce)
        .build()
    return GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
}

suspend fun googleLogin(context: Context): GoogleIdTokenCredential? {
    val manager = CredentialManager.create(context)
    val response = manager.getCredential(context, getCredentialRequest(context))
    return when (val credential = response.credential) {
        is CustomCredential -> if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (e: GoogleIdTokenParsingException) {
                e.printStackTrace()
                null
            }
        } else null

        else -> null
    }
}

