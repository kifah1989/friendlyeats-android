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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.google.firebase.example.fireeats.model.Restaurant
import com.google.firebase.firestore.Query

/**
 * Dialog Fragment containing filter form.
 */
class FilterDialogFragment : DialogFragment(), View.OnClickListener {
    internal interface FilterListener {
        fun onFilter(filters: Filters)
    }

    private lateinit var mRootView: View
    private lateinit var mCategorySpinner: Spinner
    private lateinit var mCitySpinner: Spinner
    private lateinit var mSortSpinner: Spinner
    private lateinit var mPriceSpinner: Spinner
    private lateinit var mFilterListener: FilterListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false)
        mCategorySpinner = mRootView.findViewById(R.id.spinner_category)
        mCitySpinner = mRootView.findViewById(R.id.spinner_city)
        mSortSpinner = mRootView.findViewById(R.id.spinner_sort)
        mPriceSpinner = mRootView.findViewById(R.id.spinner_price)
        mRootView.findViewById<View>(R.id.button_search).setOnClickListener(this)
        mRootView.findViewById<View>(R.id.button_cancel).setOnClickListener(this)
        return mRootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FilterListener) {
            mFilterListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_search -> onSearchClicked()
            R.id.button_cancel -> onCancelClicked()
        }
    }

    private fun onSearchClicked() {
        mFilterListener.onFilter(filters)
        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    private val selectedCategory: String?
        get() {
            val selected = mCategorySpinner.selectedItem as String
            return if (getString(R.string.value_any_category) == selected) {
                null
            } else {
                selected
            }
        }
    private val selectedCity: String?
        get() {
            val selected = mCitySpinner.selectedItem as String
            return if (getString(R.string.value_any_city) == selected) {
                null
            } else {
                selected
            }
        }
    private val selectedPrice: Int
        get() {
            return when (mPriceSpinner.selectedItem as String) {
                getString(R.string.price_1) -> {
                    1
                }
                getString(R.string.price_2) -> {
                    2
                }
                getString(R.string.price_3) -> {
                    3
                }
                else -> {
                    -1
                }
            }
        }
    private val selectedSortBy: String?
        get() {
            val selected = mSortSpinner.selectedItem as String
            if (getString(R.string.sort_by_rating) == selected) {
                return Restaurant.FIELD_AVG_RATING
            }
            if (getString(R.string.sort_by_price) == selected) {
                return Restaurant.FIELD_PRICE
            }
            return if (getString(R.string.sort_by_popularity) == selected) {
                Restaurant.FIELD_POPULARITY
            } else null
        }
    private val sortDirection: Query.Direction?
        get() {
            val selected = mSortSpinner.selectedItem as String
            if (getString(R.string.sort_by_rating) == selected) {
                return Query.Direction.DESCENDING
            }
            if (getString(R.string.sort_by_price) == selected) {
                return Query.Direction.ASCENDING
            }
            return if (getString(R.string.sort_by_popularity) == selected) {
                Query.Direction.DESCENDING
            } else null
        }

    fun resetFilters() {
        mCategorySpinner.setSelection(0)
        mCitySpinner.setSelection(0)
        mPriceSpinner.setSelection(0)
        mSortSpinner.setSelection(0)
    }

    private val filters: Filters
        get() {
            val filters = Filters()
            filters.category = selectedCategory
            filters.city = selectedCity
            filters.price = selectedPrice
            filters.sortBy = selectedSortBy.toString()
            filters.sortDirection = sortDirection!!
            return filters
        }

    companion object {
        const val TAG = "FilterDialog"
    }
}