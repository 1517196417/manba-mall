package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.dto.CartAddDTO;
import com.abcmall.entity.CartItem;
import com.abcmall.entity.Product;
import com.abcmall.entity.ProductSku;
import com.abcmall.mapper.CartItemMapper;
import com.abcmall.mapper.ProductMapper;
import com.abcmall.mapper.ProductSkuMapper;
import com.abcmall.vo.CartItemVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 购物车服务 */
@Service
@RequiredArgsConstructor
public class CartService extends ServiceImpl<CartItemMapper, CartItem> {

    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductService productService;

    /** 购物车列表（含商品/SKU 信息与有效性校验） */
    public List<CartItemVO> listVO(Long userId) {
        List<CartItem> items = list(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreateTime)
                .orderByDesc(CartItem::getId));
        List<CartItemVO> result = new ArrayList<>();
        for (CartItem item : items) {
            result.add(toVO(item));
        }
        return result;
    }

    public CartItemVO toVO(CartItem item) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setProductId(item.getProductId());
        vo.setSkuId(item.getSkuId());
        vo.setQuantity(item.getQuantity());
        vo.setSelected(item.getSelected());

        Product product = productMapper.selectById(item.getProductId());
        if (product == null || (product.getStatus() != null && product.getStatus() == 0)) {
            vo.setValid(false);
            vo.setInvalidReason("商品已下架");
            vo.setProductName(product == null ? "已删除商品" : product.getName());
            vo.setImage(product == null ? null : product.getMainImage());
            vo.setPrice(BigDecimal.ZERO);
            vo.setStock(0);
            return vo;
        }

        vo.setProductName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setImage(product.getMainImage());

        ProductSku sku = item.getSkuId() == null ? null : skuMapper.selectById(item.getSkuId());
        if (sku == null) {
            sku = productService.defaultSku(item.getProductId());
        }
        if (sku != null) {
            vo.setSkuDesc(sku.getSpecDesc());
            vo.setSpecJson(sku.getSpecJson());
            vo.setPrice(sku.getPrice());
            vo.setMarketPrice(sku.getMarketPrice());
            vo.setStock(sku.getStock());
            if (sku.getImage() != null) {
                vo.setImage(sku.getImage());
            }
            if (sku.getStatus() != null && sku.getStatus() == 0) {
                vo.setValid(false);
                vo.setInvalidReason("该规格已下架");
            } else if (sku.getStock() != null && sku.getStock() <= 0) {
                vo.setValid(false);
                vo.setInvalidReason("该规格暂时缺货");
            }
        } else {
            vo.setPrice(product.getPrice());
            vo.setMarketPrice(product.getMarketPrice());
            vo.setStock(product.getStock());
            vo.setValid(false);
            vo.setInvalidReason("规格信息缺失");
        }

        if (Boolean.TRUE.equals(vo.getValid()) && item.getQuantity() != null
                && vo.getStock() != null && item.getQuantity() > vo.getStock()) {
            vo.setValid(false);
            vo.setInvalidReason("库存不足，仅剩 " + vo.getStock() + " 件");
        }

        vo.setSpecs(productService.buildSpecs(item.getProductId()));
        return vo;
    }

    /** 加入购物车 */
    @Transactional(rollbackFor = Exception.class)
    public CartItem add(Long userId, CartAddDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        BizException.notNull(product, "商品不存在");
        BizException.check(product.getStatus() != null && product.getStatus() == 0, "商品已下架");

        Long skuId = dto.getSkuId();
        if (skuId == null) {
            ProductSku sku = productService.defaultSku(dto.getProductId());
            BizException.notNull(sku, "该商品暂无可售规格");
            skuId = sku.getId();
        }
        ProductSku sku = productService.requireSku(dto.getProductId(), skuId);
        BizException.check(sku.getStock() == null || sku.getStock() <= 0, "该规格暂时缺货");

        int quantity = dto.getQuantity() == null || dto.getQuantity() < 1 ? 1 : dto.getQuantity();

        CartItem exist = getOne(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, dto.getProductId())
                .eq(CartItem::getSkuId, skuId)
                .last("limit 1"), false);

        if (exist != null) {
            int newQty = exist.getQuantity() + quantity;
            BizException.check(newQty > sku.getStock(), "库存不足，最多可购买 " + sku.getStock() + " 件");
            exist.setQuantity(newQty);
            exist.setSelected(1);
            exist.setUpdateTime(LocalDateTime.now());
            updateById(exist);
            return exist;
        }

        BizException.check(quantity > sku.getStock(), "库存不足，最多可购买 " + sku.getStock() + " 件");
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(dto.getProductId());
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        item.setSelected(1);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        save(item);
        return item;
    }

    /** 修改数量 / 勾选状态 */
    public void updateItem(Long userId, Long id, Integer quantity, Integer selected) {
        CartItem item = getById(id);
        BizException.notNull(item, "购物车条目不存在");
        BizException.check(!item.getUserId().equals(userId), "无权操作他人购物车");

        if (quantity != null) {
            BizException.check(quantity < 1, "数量至少为 1");
            if (item.getSkuId() != null) {
                ProductSku sku = skuMapper.selectById(item.getSkuId());
                if (sku != null && sku.getStock() != null) {
                    BizException.check(quantity > sku.getStock(), "库存不足，最多可购买 " + sku.getStock() + " 件");
                }
            }
            item.setQuantity(quantity);
        }
        if (selected != null) {
            item.setSelected(selected);
        }
        item.setUpdateTime(LocalDateTime.now());
        updateById(item);
    }

    /** 切换规格 */
    public void changeSku(Long userId, Long id, Long skuId) {
        CartItem item = getById(id);
        BizException.notNull(item, "购物车条目不存在");
        BizException.check(!item.getUserId().equals(userId), "无权操作他人购物车");
        ProductSku sku = productService.requireSku(item.getProductId(), skuId);
        BizException.check(sku.getStock() == null || sku.getStock() <= 0, "所选规格暂时缺货");

        item.setSkuId(skuId);
        if (item.getQuantity() > sku.getStock()) {
            item.setQuantity(sku.getStock());
        }
        item.setUpdateTime(LocalDateTime.now());
        updateById(item);
    }

    public void selectAll(Long userId, Integer selected) {
        update(Wrappers.<CartItem>lambdaUpdate()
                .eq(CartItem::getUserId, userId)
                .set(CartItem::getSelected, selected == null ? 1 : selected)
                .set(CartItem::getUpdateTime, LocalDateTime.now()));
    }

    public void removeItem(Long userId, Long id) {
        CartItem item = getById(id);
        BizException.notNull(item, "购物车条目不存在");
        BizException.check(!item.getUserId().equals(userId), "无权操作他人购物车");
        removeById(id);
    }

    public void clear(Long userId) {
        remove(Wrappers.<CartItem>lambdaQuery().eq(CartItem::getUserId, userId));
    }

    /** 清理失效商品 */
    public int cleanInvalid(Long userId) {
        List<CartItemVO> items = listVO(userId);
        int count = 0;
        for (CartItemVO vo : items) {
            if (!Boolean.TRUE.equals(vo.getValid())) {
                removeById(vo.getId());
                count++;
            }
        }
        return count;
    }

    /** 已勾选且有效的条目 */
    public List<CartItem> selectedItems(Long userId) {
        return list(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSelected, 1));
    }

    public int countByUser(Long userId) {
        return Math.toIntExact(count(Wrappers.<CartItem>lambdaQuery().eq(CartItem::getUserId, userId)));
    }

    /** 批量删除（下单成功后清空已购条目） */
    public void removeByIdsForUser(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        remove(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, ids));
    }

    /** 内联状态常量，便于阅读 */
    public static final int SELECTED_YES = 1;
    public static final int SELECTED_NO = 0;
}
