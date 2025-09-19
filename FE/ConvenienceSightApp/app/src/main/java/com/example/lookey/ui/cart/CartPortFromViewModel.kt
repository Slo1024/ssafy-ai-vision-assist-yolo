package com.example.lookey.ui.cart

import com.example.lookey.ui.viewmodel.CartLine
import com.example.lookey.ui.viewmodel.CartViewModel

class CartPortFromViewModel(
    private val vm: CartViewModel
) : CartPort {

    override fun isInCart(name: String): Boolean =
        vm.cart.value.any { it.name == name }

    override fun remove(cartLine: CartLine) {
       // vm.removeFromCart(cartLine.cartId!!)
    }

    override fun namesSnapshot(): List<String> =
        vm.cart.value.mapNotNull { it.name }
}
