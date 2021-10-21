/**
 * Copyright 2017 Google Inc. All Rights Reserved.
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
package com.google.firebase.example.fireeats.viewmodel

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
import com.google.firebase.example.fireeats.Filters

/**
 * ViewModel for [com.google.firebase.example.fireeats.MainActivity].
 */
class MainActivityViewModel : ViewModel() {
    var isSigningIn = false
    var filters: Filters

    init {
        filters = default
    }
}