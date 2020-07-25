package com.hypercode.android.excart.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sku(
    @PrimaryKey
    var code: String,
    var quantity: Int
)