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
import android.content.Context
import com.google.firebase.example.fireeats.RestaurantDetailActivity
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import android.widget.Toast
import android.text.TextUtils
import android.widget.Spinner
import com.google.firebase.firestore.*
import java.lang.StringBuilder

/**
 * Object for passing filters around.
 */
class Filters {
    var category: String? = null
    var city: String? = null
    var price = -1
    var sortBy: String = ""
    lateinit var sortDirection: Query.Direction
    fun hasCategory(): Boolean {
        return !TextUtils.isEmpty(category)
    }

    fun hasCity(): Boolean {
        return !TextUtils.isEmpty(city)
    }

    fun hasPrice(): Boolean {
        return price > 0
    }

    fun hasSortBy(): Boolean {
        return !TextUtils.isEmpty(sortBy)
    }

    fun getSearchDescription(context: Context): String {
        val desc = StringBuilder()
        if (category == null && city == null) {
            desc.append("<b>")
            desc.append(context.getString(R.string.all_restaurants))
            desc.append("</b>")
        }
        if (category != null) {
            desc.append("<b>")
            desc.append(category)
            desc.append("</b>")
        }
        if (category != null && city != null) {
            desc.append(" in ")
        }
        if (city != null) {
            desc.append("<b>")
            desc.append(city)
            desc.append("</b>")
        }
        if (price > 0) {
            desc.append(" for ")
            desc.append("<b>")
            desc.append(RestaurantUtil.getPriceString(price))
            desc.append("</b>")
        }
        return desc.toString()
    }

    fun getOrderDescription(context: Context): String {
        return if (Restaurant.FIELD_PRICE == sortBy) {
            context.getString(R.string.sorted_by_price)
        } else if (Restaurant.FIELD_POPULARITY == sortBy) {
            context.getString(R.string.sorted_by_popularity)
        } else {
            context.getString(R.string.sorted_by_rating)
        }
    }

    companion object {
        @JvmStatic
        val default: Filters
            get() {
                val filters = Filters()
                filters.sortBy = Restaurant.FIELD_AVG_RATING
                filters.sortDirection = Query.Direction.DESCENDING
                return filters
            }
    }
}