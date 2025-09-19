package com.example.lookey.ui.cart

import com.example.lookey.ui.viewmodel.CartLine

interface CartPort {
    fun isInCart(name: String): Boolean
    fun remove(name: CartLine)
    fun namesSnapshot(): List<String>
}
