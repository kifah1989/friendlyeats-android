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
package com.google.firebase.example.fireeats.adapter

import com.google.firebase.example.fireeats.Filters.Companion.default
import com.google.firebase.example.fireeats.adapter.FirestoreAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import com.google.firebase.example.fireeats.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter.OnRestaurantSelectedListener
import com.google.firebase.example.fireeats.model.Restaurant
import com.bumptech.glide.Glide
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.auth.FirebaseUser
import android.text.TextUtils
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.auth.AuthUI
import com.google.firebase.example.fireeats.util.FirebaseUtil
import androidx.lifecycle.ViewModel
import com.google.firebase.example.fireeats.Filters
import com.google.firebase.firestore.*
import java.util.ArrayList

/**
 * RecyclerView adapter for displaying the results of a Firestore [Query].
 *
 * Note that this class forgoes some efficiency to gain simplicity. For example, the result of
 * [DocumentSnapshot.toObject] is not cached so the same object may be deserialized
 * many times as the user scrolls.
 *
 * See the adapter classes in FirebaseUI (https://github.com/firebase/FirebaseUI-Android/tree/master/firestore) for a
 * more efficient implementation of a Firestore RecyclerView Adapter.
 */
abstract class FirestoreAdapter<VH : RecyclerView.ViewHolder?>(private var mQuery: Query?) :
    RecyclerView.Adapter<VH>(), EventListener<QuerySnapshot> {
    override fun onEvent(
        documentSnapshots: QuerySnapshot?,
        e: FirebaseFirestoreException?
    ) {

        // Handle errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e)
            return
        }

        // Dispatch the event
        if (documentSnapshots != null) {
            for (change in documentSnapshots.documentChanges) {
                // Snapshot of the changed document
                val snapshot: DocumentSnapshot = change.document
                when (change.type) {
                    DocumentChange.Type.ADDED -> onDocumentAdded(change)
                    DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                    DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
                }
            }
        }
        onDataChanged()
    }

    private var mRegistration: ListenerRegistration? = null
    private val mSnapshots = ArrayList<DocumentSnapshot>()
    fun startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery!!.addSnapshotListener(this)
        }
    }

    fun stopListening() {
        if (mRegistration != null) {
            mRegistration!!.remove()
            mRegistration = null
        }
        mSnapshots.clear()
        notifyDataSetChanged()
    }

    fun setQuery(query: Query?) {
        // Stop listening
        stopListening()

        // Clear existing data
        mSnapshots.clear()
        notifyDataSetChanged()

        // Listen to new query
        mQuery = query
        startListening()
    }

    override fun getItemCount(): Int {
        return mSnapshots.size
    }

    protected fun getSnapshot(index: Int): DocumentSnapshot {
        return mSnapshots[index]
    }

    protected open fun onError(e: FirebaseFirestoreException?) {}
    protected open fun onDataChanged() {}
    protected fun onDocumentAdded(change: DocumentChange) {
        mSnapshots.add(change.newIndex, change.document)
        notifyItemInserted(change.newIndex)
    }

    protected fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            mSnapshots[change.oldIndex] = change.document
            notifyItemChanged(change.oldIndex)
        } else {
            // Item changed and changed position
            mSnapshots.removeAt(change.oldIndex)
            mSnapshots.add(change.newIndex, change.document)
            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    protected fun onDocumentRemoved(change: DocumentChange) {
        mSnapshots.removeAt(change.oldIndex)
        notifyItemRemoved(change.oldIndex)
    }

    companion object {
        private const val TAG = "Firestore Adapter"
    }
}