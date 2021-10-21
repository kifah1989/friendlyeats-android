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
import android.os.Bundle
import com.google.firebase.example.fireeats.R
import com.google.firebase.example.fireeats.util.FirebaseUtil
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.example.fireeats.FilterDialogFragment
import com.google.firebase.example.fireeats.adapter.RestaurantAdapter
import com.google.firebase.example.fireeats.viewmodel.MainActivityViewModel
import com.google.firebase.example.fireeats.MainActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.example.fireeats.model.Restaurant
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.example.fireeats.Filters
import android.text.Html
import android.content.Intent
import android.app.Activity
import com.google.firebase.example.fireeats.RestaurantDetailActivity
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import android.widget.Toast
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.*

class MainActivity: AppCompatActivity(), View.OnClickListener, FilterDialogFragment.FilterListener,
    RestaurantAdapter.OnRestaurantSelectedListener {
    private var mToolbar: Toolbar? = null
    private var mCurrentSearchView: TextView? = null
    private var mCurrentSortByView: TextView? = null
    private var mRestaurantsRecycler: RecyclerView? = null
    private var mEmptyView: ViewGroup? = null
    private var mFirestore: FirebaseFirestore? = null
    private var mQuery: Query? = null
    private var mFilterDialog: FilterDialogFragment? = null
    private var mAdapter: RestaurantAdapter? = null
    private var mViewModel: MainActivityViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFirestore = FirebaseUtil.firestore

        // Get the 50 highest rated restaurants
        mQuery = mFirestore?.collection("restaurants")
            ?.orderBy("avgRating", Query.Direction.DESCENDING)
            ?.limit(LIMIT.toLong())
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        mCurrentSearchView = findViewById(R.id.text_current_search)
        mCurrentSortByView = findViewById(R.id.text_current_sort_by)
        mRestaurantsRecycler = findViewById(R.id.recycler_restaurants)
        mEmptyView = findViewById(R.id.view_empty)
        findViewById<View>(R.id.filter_bar).setOnClickListener(this)
        findViewById<View>(R.id.button_clear_filter).setOnClickListener(this)

        // View model
        mViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Initialize Firestore and the main RecyclerView
        mFirestore = FirebaseUtil.firestore
        initRecyclerView()

        // Filter Dialog
        mFilterDialog = FilterDialogFragment()
    }

    private fun initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView")
        }
        mAdapter = object : RestaurantAdapter(mQuery, this@MainActivity) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (itemCount == 0) {
                    mRestaurantsRecycler!!.visibility = View.GONE
                    mEmptyView!!.visibility = View.VISIBLE
                } else {
                    mRestaurantsRecycler!!.visibility = View.VISIBLE
                    mEmptyView!!.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException?) {
                // Show a snackbar on errors
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error: check logs for info.", Snackbar.LENGTH_LONG
                ).show()
            }
        }
        mRestaurantsRecycler!!.layoutManager = LinearLayoutManager(this)
        mRestaurantsRecycler!!.adapter = mAdapter
    }

    public override fun onStart() {
        super.onStart()

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn()
            return
        }

        // Apply filters
        onFilter(mViewModel!!.filters)

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter!!.startListening()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (mAdapter != null) {
            mAdapter!!.stopListening()
        }
    }

    private fun onAddItemsClicked() {
        // Get a reference to the restaurants collection
        val restaurants = mFirestore!!.collection("restaurants")
        for (i in 0..9) {
            // Get a random Restaurant POJO
            val restaurant = RestaurantUtil.getRandom(this)

            // Add a new document to the restaurants collection
            restaurants.add(restaurant)
        }
        showTodoToast()
    }

    override fun onFilter(filters: Filters) {
        // Construct query basic query
        var query: Query = mFirestore!!.collection("restaurants")

        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo("category", filters.category)
        }

        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo("city", filters.city)
        }

        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo("price", filters.price)
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.sortBy, filters.sortDirection)
        }

        // Limit items
        query = query.limit(LIMIT.toLong())

        // Update the query
        mQuery = query
        mAdapter!!.setQuery(query)

        // Set header
        mCurrentSearchView!!.text = Html.fromHtml(filters.getSearchDescription(this))
        mCurrentSortByView!!.text = filters.getOrderDescription(this)

        // Save filters
        mViewModel!!.filters = filters
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_items -> onAddItemsClicked()
            R.id.menu_sign_out -> {
                FirebaseUtil.authUI?.signOut(this)
                startSignIn()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            mViewModel!!.isSigningIn = false
            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.filter_bar -> onFilterClicked()
            R.id.button_clear_filter -> onClearFilterClicked()
        }
    }

    fun onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog!!.show(supportFragmentManager, FilterDialogFragment.Companion.TAG)
    }

    fun onClearFilterClicked() {
        mFilterDialog!!.resetFilters()
        onFilter(Filters.default)
    }

    override fun onRestaurantSelected(restaurant: DocumentSnapshot?) {
        // Go to the details page for the selected restaurant
        val intent = Intent(this, RestaurantDetailActivity::class.java)
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant?.id)
        startActivity(intent)
    }

    private fun shouldStartSignIn(): Boolean {
        return !mViewModel!!.isSigningIn && FirebaseUtil.auth?.currentUser == null
    }

    private fun startSignIn() {
        // Sign in with FirebaseUI
        val intent = FirebaseUtil.authUI
            ?.createSignInIntentBuilder()
            ?.setAvailableProviders(
                listOf(
                    EmailBuilder().build()
                )
            )
            ?.setIsSmartLockEnabled(false)
            ?.build()
        startActivityForResult(intent, RC_SIGN_IN)
        mViewModel!!.isSigningIn = true
    }

    private fun showTodoToast() {
        Toast.makeText(this, "TODO: Implement", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
        private const val LIMIT = 50
    }
}