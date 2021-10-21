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
package com.google.firebase.example.fireeats

import me.zhanghai.android.materialratingbar.MaterialRatingBar
import android.widget.EditText
import com.google.firebase.example.fireeats.RatingDialogFragment.RatingListener
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.util.FirebaseUtil
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter.OnRestaurantSelectedListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.example.fireeats.FilterDialogFragment
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter
import com.google.firebase.example.fireeats.viewmodel.MainActivityViewModel
import com.google.firebase.example.fireeats.MainActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.example.fireeats.model.Restaurant
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.example.fireeats.Filters
import android.text.Html
import android.content.Intent
import android.app.Activity
import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.example.fireeats.RestaurantDetailActivity
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import android.widget.Toast
import android.text.TextUtils
import android.view.View
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.google.firebase.example.fireeats.model.Rating

/**
 * Dialog Fragment containing rating form.
 */
class RatingDialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var mRatingBar: MaterialRatingBar
    private lateinit var mRatingText: EditText

    internal interface RatingListener {
        fun onRating(rating: Rating?)
    }

    private var mRatingListener: RatingListener? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.dialog_rating, container, false)
        mRatingBar = v.findViewById(R.id.restaurant_form_rating)
        mRatingText = v.findViewById(R.id.restaurant_form_text)
        v.findViewById<View>(R.id.restaurant_form_button).setOnClickListener(this)
        v.findViewById<View>(R.id.restaurant_form_cancel).setOnClickListener(this)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RatingListener) {
            mRatingListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.restaurant_form_button -> onSubmitClicked(v)
            R.id.restaurant_form_cancel -> onCancelClicked(v)
        }
    }

    fun onSubmitClicked(view: View?) {
        val rating = FirebaseUtil.auth?.currentUser?.let {
            Rating(
                it,
                mRatingBar.rating.toDouble(),
                mRatingText.text.toString()
            )
        }
        if (mRatingListener != null) {
            mRatingListener!!.onRating(rating)
        }
        dismiss()
    }

    fun onCancelClicked(view: View?) {
        dismiss()
    }

    companion object {
        const val TAG = "RatingDialog"
    }
}