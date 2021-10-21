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
import android.text.TextUtils
import com.google.firebase.example.fireeats.model.Restaurant
import com.google.firebase.example.fireeats.util.RestaurantUtil
import com.google.firebase.firestore.Query

/**
 * Object for passing filters around.
 */
class Filters {
    var category: String = ""
    var city: String = ""
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

    fun getSearchDescription(): String {
        val desc = StringBuilder()
        desc.append("<b>")
        desc.append(category)
        desc.append("</b>")
        desc.append(" in ")
        desc.append("<b>")
        desc.append(city)
        desc.append("</b>")
        if (price > 0) {
            desc.append(" for ")
            desc.append("<b>")
            desc.append(RestaurantUtil.getPriceString(price))
            desc.append("</b>")
        }
        return desc.toString()
    }

    fun getOrderDescription(context: Context): String {
        return when (sortBy) {
            Restaurant.FIELD_PRICE -> {
                context.getString(R.string.sorted_by_price)
            }
            Restaurant.FIELD_POPULARITY -> {
                context.getString(R.string.sorted_by_popularity)
            }
            else -> {
                context.getString(R.string.sorted_by_rating)
            }
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