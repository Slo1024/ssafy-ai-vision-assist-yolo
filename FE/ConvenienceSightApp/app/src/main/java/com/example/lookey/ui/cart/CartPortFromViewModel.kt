package com.example.lookey.ui.cart

import com.example.lookey.ui.viewmodel.CartViewModel

class CartPortFromViewModel(
    private val vm: CartViewModel
) : CartPort {

    override fun isInCart(name: String): Boolean =
        vm.cart.value.any { it.name == name }

    override fun remove(name: String) {
        vm.removeFromCart(name)
    }

    override fun namesSnapshot(): List<String> =
        vm.cart.value.map { it.name }
}
