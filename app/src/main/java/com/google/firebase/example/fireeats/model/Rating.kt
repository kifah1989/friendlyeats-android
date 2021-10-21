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

import com.google.firebase.auth.FirebaseUser
import android.text.TextUtils

/**
 * Model POJO for a rating.
 */
class Rating(user: FirebaseUser, var rating: Double, text: String) {
    private var userId: String = ""
    var userName: String = ""
    var text: String = ""


    init {
        userId = user.uid
        userName = user.displayName.toString()
        if (TextUtils.isEmpty(userName)) {
            userName = user.email.toString()
        }
    }
}