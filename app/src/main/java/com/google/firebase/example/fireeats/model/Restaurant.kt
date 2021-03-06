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
package com.google.firebase.example.fireeats.model

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Restaurant POJO.
 */
@IgnoreExtraProperties
data class Restaurant(
    var name: String = "",
    var city: String = "",
    var category: String = "",
    var photo: String = "",
    var price: Int = 0,
    var numRatings: Int = 0,
    var avgRating: Double = 0.0
) {

    companion object {
        const val FIELD_CITY = "city"
        const val FIELD_CATEGORY = "category"
        const val FIELD_PRICE = "price"
        const val FIELD_POPULARITY = "numRatings"
        const val FIELD_AVG_RATING = "avgRating"
    }
}