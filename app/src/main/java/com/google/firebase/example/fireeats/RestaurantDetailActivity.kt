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

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.example.fireeats.RatingDialogFragment.RatingListener
import com.google.firebase.example.fireeats.adapter.RatingAdapter
import com.google.firebase.example.fireeats.model.Rating
import com.google.firebase.example.fireeats.model.Restaurant
import com.google.firebase.example.fireeats.util.FirebaseUtil
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.firestore.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar

class RestaurantDetailActivity : AppCompatActivity(), View.OnClickListener,
    EventListener<DocumentSnapshot>, RatingListener {
    private lateinit var mImageView: ImageView
    private lateinit var mNameView: TextView
    private lateinit var mRatingIndicator: MaterialRatingBar
    private lateinit var mNumRatingsView: TextView
    private lateinit var mCityView: TextView
    private lateinit var mCategoryView: TextView
    private lateinit var mPriceView: TextView
    private lateinit var mEmptyView: ViewGroup
    private lateinit var mRatingsRecycler: RecyclerView
    private lateinit var mRatingDialog: RatingDialogFragment
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mRestaurantRef: DocumentReference
    private lateinit var mRestaurantRegistration: ListenerRegistration
    private lateinit var mRatingAdapter: RatingAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)
        mImageView = findViewById(R.id.restaurant_image)
        mNameView = findViewById(R.id.restaurant_name)
        mRatingIndicator = findViewById(R.id.restaurant_rating)
        mNumRatingsView = findViewById(R.id.restaurant_num_ratings)
        mCityView = findViewById(R.id.restaurant_city)
        mCategoryView = findViewById(R.id.restaurant_category)
        mPriceView = findViewById(R.id.restaurant_price)
        mEmptyView = findViewById(R.id.view_empty_ratings)
        mRatingsRecycler = findViewById(R.id.recycler_ratings)
        findViewById<View>(R.id.restaurant_button_back).setOnClickListener(this)
        findViewById<View>(R.id.fab_show_rating_dialog).setOnClickListener(this)

        // Get restaurant ID from extras
        val restaurantId = intent.extras!!.getString(KEY_RESTAURANT_ID)
            ?: throw IllegalArgumentException("Must pass extra $KEY_RESTAURANT_ID")

        // Initialize Firestore
        mFirestore = FirebaseUtil.firestore!!

        // Get reference to the restaurant
        mRestaurantRef = mFirestore.collection("restaurants").document(restaurantId)

        // Get ratings
        val ratingsQuery = mRestaurantRef
            .collection("ratings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)

        // RecyclerView
        mRatingAdapter = object : RatingAdapter(ratingsQuery) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    mRatingsRecycler.visibility = View.GONE
                    mEmptyView.visibility = View.VISIBLE
                } else {
                    mRatingsRecycler.visibility = View.VISIBLE
                    mEmptyView.visibility = View.GONE
                }
            }
        }
        mRatingsRecycler.layoutManager = LinearLayoutManager(this)
        mRatingsRecycler.adapter = mRatingAdapter
        mRatingDialog = RatingDialogFragment()
    }

    public override fun onStart() {
        super.onStart()
        mRatingAdapter.startListening()
        mRestaurantRegistration = mRestaurantRef.addSnapshotListener(this)
    }

    public override fun onStop() {
        super.onStop()
        mRatingAdapter.stopListening()
        mRestaurantRegistration.remove()

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.restaurant_button_back -> onBackArrowClicked()
            R.id.fab_show_rating_dialog -> onAddRatingClicked()
        }
    }

    private fun addRating(
        restaurantRef: DocumentReference?,
        rating: Rating
    ): Task<Void> {
        // Create reference for new rating, for use inside the transaction
        val ratingRef = restaurantRef!!.collection("ratings")
            .document()

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction { transaction: Transaction ->
            val restaurant = transaction[restaurantRef]
                .toObject(Restaurant::class.java)

            // Compute new number of ratings
            val newNumRatings = restaurant!!.numRatings + 1

            // Compute new average rating
            val oldRatingTotal = restaurant.avgRating *
                    restaurant.numRatings
            val newAvgRating = (oldRatingTotal + rating.rating) /
                    newNumRatings

            // Set new restaurant info
            restaurant.numRatings = newNumRatings
            restaurant.avgRating = newAvgRating

            // Commit to Firestore
            transaction[restaurantRef] = restaurant
            transaction[ratingRef] = rating
            null
        }
    }

    /**
     * Listener for the Restaurant document ([.mRestaurantRef]).
     */
    override fun onEvent(value: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e)
            return
        }
        if (value != null) {
            onRestaurantLoaded(value.toObject(Restaurant::class.java))
        }
    }

    private fun onRestaurantLoaded(restaurant: Restaurant?) {
        mNameView.text = restaurant!!.name
        mRatingIndicator.rating = restaurant.avgRating.toFloat()
        mNumRatingsView.text = getString(R.string.fmt_num_ratings, restaurant.numRatings)
        mCityView.text = restaurant.city
        mCategoryView.text = restaurant.category
        mPriceView.text = RestaurantUtil.getPriceString(restaurant)

        // Background image
        Glide.with(mImageView.context)
            .load(restaurant.photo)
            .into(mImageView)
    }

    private fun onBackArrowClicked() {
        onBackPressed()
    }

    private fun onAddRatingClicked() {
        mRatingDialog.show(supportFragmentManager, RatingDialogFragment.TAG)
    }

    override fun onRating(rating: Rating?) {
        // In a transaction, add the new rating and update the aggregate totals
        if (rating != null) {
            addRating(mRestaurantRef, rating)
                .addOnSuccessListener(this) {
                    Log.d(TAG, "Rating added")

                    // Hide keyboard and scroll to top
                    hideKeyboard()
                    mRatingsRecycler.smoothScrollToPosition(0)
                }
                .addOnFailureListener(this) { e ->
                    Log.w(TAG, "Add rating failed", e)

                    // Show failure message and hide keyboard
                    hideKeyboard()
                    Snackbar.make(
                        findViewById(android.R.id.content), "Failed to add rating",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {
        private const val TAG = "RestaurantDetail"
        const val KEY_RESTAURANT_ID = "key_restaurant_id"
    }
}