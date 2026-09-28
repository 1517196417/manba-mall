package com.abcmall.mapper;

import com.abcmall.common.PageResult;
import com.abcmall.entity.ShopOrder;
import com.abcmall.vo.OrderVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShopOrderMapper extends BaseMapper<ShopOrder> {

    // 分页查询订单数据
    IPage<ShopOrder> pageOrders(Page<ShopOrder> page, @Param("userId") Long userId, @Param("status") List<Integer> status, @Param("keyword") String keyword);

}
