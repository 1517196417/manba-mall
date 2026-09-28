package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.entity.UserAddress;
import com.abcmall.mapper.UserAddressMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 收货地址服务 */
@Service
public class AddressService extends ServiceImpl<UserAddressMapper, UserAddress> {

    public List<UserAddress> listByUser(Long userId) {
        return list(Wrappers.<UserAddress>lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));
    }

    public UserAddress getDefault(Long userId) {
        UserAddress address = getOne(Wrappers.<UserAddress>lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
                .last("limit 1"), false);
        if (address == null) {
            List<UserAddress> list = listByUser(userId);
            address = list.isEmpty() ? null : list.get(0);
        }
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserAddress saveAddress(Long userId, UserAddress address) {
        BizException.check(address.getReceiver() == null || address.getReceiver().isBlank(), "请填写收货人");
        BizException.check(address.getPhone() == null || address.getPhone().isBlank(), "请填写联系电话");
        address.setUserId(userId);
        long count = count(Wrappers.<UserAddress>lambdaQuery().eq(UserAddress::getUserId, userId));

        boolean first = count == 0;
        if (address.getId() == null) {
            // 新增
            address.setIsDefault(first || Integer.valueOf(1).equals(address.getIsDefault()) ? 1 : 0);
            address.setCreateTime(LocalDateTime.now());
            address.setUpdateTime(LocalDateTime.now());
            save(address);
        } else {
            UserAddress exist = getById(address.getId());
            BizException.notNull(exist, "地址不存在");
            BizException.check(!exist.getUserId().equals(userId), "无权操作他人地址");
            address.setUpdateTime(LocalDateTime.now());
            updateById(address);
        }
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            setDefault(userId, address.getId());
        }
        return getById(address.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        BizException.notNull(address, "地址不存在");
        BizException.check(!address.getUserId().equals(userId), "无权操作他人地址");

        update(Wrappers.<UserAddress>lambdaUpdate()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));

        update(Wrappers.<UserAddress>lambdaUpdate()
                .eq(UserAddress::getId, addressId)
                .set(UserAddress::getIsDefault, 1));
    }

    public void removeAddress(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        BizException.notNull(address, "地址不存在");
        BizException.check(!address.getUserId().equals(userId), "无权操作他人地址");
        removeById(addressId);
    }
}
