package com.example.lookey.ui.cart

interface CartPort {
    fun isInCart(name: String): Boolean
    fun remove(name: String)
    fun namesSnapshot(): List<String>
}
