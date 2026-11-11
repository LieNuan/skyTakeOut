package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    //添加购物车
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    //减少购物车中的商品数量
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> seeShoppingCart();

    void deleteShoppingCart();
}
