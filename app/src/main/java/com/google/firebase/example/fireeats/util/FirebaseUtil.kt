/**
 * Copyright 2021 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.example.fireeats.util

import com.google.firebase.example.fireeats.Filters.Companion.default
import com.google.firebase.example.fireeats.adapter.FirestoreAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import com.google.firebase.example.fireeats.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter.OnRestaurantSelectedListener
import com.google.firebase.example.fireeats.model.Restaurant
import com.bumptech.glide.Glide
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.auth.FirebaseUser
import android.text.TextUtils
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.auth.AuthUI
import com.google.firebase.example.fireeats.util.FirebaseUtil
import androidx.lifecycle.ViewModel
import com.google.firebase.example.fireeats.BuildConfig
import com.google.firebase.example.fireeats.Filters

/**
 * Utility class for initializing Firebase services and connecting them to the Firebase Emulator
 * Suite if necessary.
 */
object FirebaseUtil {
    /** Use emulators only in debug builds  */
    private val sUseEmulators = BuildConfig.DEBUG
    private var FIRESTORE: FirebaseFirestore? = null
    private var AUTH: FirebaseAuth? = null
    private var AUTH_UI: AuthUI? = null

    // Connect to the Cloud Firestore emulator when appropriate. The host '10.0.2.2' is a
    // special IP address to let the Android emulator connect to 'localhost'.
    val firestore: FirebaseFirestore?
        get() {
            if (FIRESTORE == null) {
                FIRESTORE = FirebaseFirestore.getInstance()

                // Connect to the Cloud Firestore emulator when appropriate. The host '10.0.2.2' is a
                // special IP address to let the Android emulator connect to 'localhost'.
                if (sUseEmulators) {
                    FIRESTORE!!.useEmulator("10.0.2.2", 8080)
                }
            }
            return FIRESTORE
        }

    // Connect to the Firebase Auth emulator when appropriate. The host '10.0.2.2' is a
    // special IP address to let the Android emulator connect to 'localhost'.
    val auth: FirebaseAuth?
        get() {
            if (AUTH == null) {
                AUTH = FirebaseAuth.getInstance()

                // Connect to the Firebase Auth emulator when appropriate. The host '10.0.2.2' is a
                // special IP address to let the Android emulator connect to 'localhost'.
                if (sUseEmulators) {
                    AUTH!!.useEmulator("10.0.2.2", 9099)
                }
            }
            return AUTH
        }

    // Connect to the Firebase Auth emulator when appropriate. The host '10.0.2.2' is a
    // special IP address to let the Android emulator connect to 'localhost'.
    val authUI: AuthUI?
        get() {
            if (AUTH_UI == null) {
                AUTH_UI = AuthUI.getInstance()

                // Connect to the Firebase Auth emulator when appropriate. The host '10.0.2.2' is a
                // special IP address to let the Android emulator connect to 'localhost'.
                if (sUseEmulators) {
                    AUTH_UI!!.useEmulator("10.0.2.2", 9099)
                }
            }
            return AUTH_UI
        }
}