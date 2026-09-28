package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.entity.UserAddress;
import com.abcmall.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 收货地址管理 */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/list")
    public Result<List<UserAddress>> list() {
        return Result.success(addressService.listByUser(UserContext.requireUserId()));
    }

    @GetMapping("/default")
    public Result<UserAddress> defaultAddress() {
        return Result.success(addressService.getDefault(UserContext.requireUserId()));
    }

    @GetMapping("/{id}")
    public Result<UserAddress> detail(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    /** 新增地址 */
    @PostMapping
    public Result<UserAddress> save(@RequestBody UserAddress address) {
        address.setId(null);
        return Result.success("地址已保存", addressService.saveAddress(UserContext.requireUserId(), address));
    }

    /** 修改地址 */
    @PutMapping("/{id}")
    public Result<UserAddress> update(@PathVariable Long id, @RequestBody UserAddress address) {
        address.setId(id);
        return Result.success("地址已更新", addressService.saveAddress(UserContext.requireUserId(), address));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        addressService.removeAddress(UserContext.requireUserId(), id);
        return Result.success("地址已删除", null);
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(UserContext.requireUserId(), id);
        return Result.success("已设为默认地址", null);
    }
}
