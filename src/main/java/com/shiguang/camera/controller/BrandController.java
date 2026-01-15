package com.shiguang.camera.controller;

import com.shiguang.camera.common.Result;
import com.shiguang.camera.common.ResultCode;
import com.shiguang.camera.entity.Brand;
import com.shiguang.camera.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * 获取所有品牌列表（分页）
     * GET /api/brands?page=1&size=10&status=1
     */
    @GetMapping
    public Result<?> getBrandList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        try {
            List<Brand> brands;

            if (status != null) {
                brands = brandService.lambdaQuery()
                        .eq(Brand::getStatus, status)
                        .orderByAsc(Brand::getSortOrder)
                        .orderByDesc(Brand::getCreateTime)
                        .list();
            } else {
                brands = brandService.list();
            }

            return Result.success(brands);

        } catch (Exception e) {
            log.error("获取品牌列表失败", e);
            return Result.error("获取品牌列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的品牌（用于下拉选择）
     * GET /api/brands/enabled
     */
    @GetMapping("/enabled")
    public Result<?> getEnabledBrands() {
        try {
            List<Brand> brands = brandService.getAllEnabledBrands();
            return Result.success(brands);
        } catch (Exception e) {
            log.error("获取启用品牌失败", e);
            return Result.error("获取启用品牌失败: " + e.getMessage());
        }
    }

    /**
     * 获取品牌详情
     * GET /api/brands/{id}
     */
    @GetMapping("/{id}")
    public Result<?> getBrandDetail(@PathVariable Integer id) {
        try {
            Brand brand = brandService.getById(id);
            if (brand == null) {
                return Result.error(ResultCode.NOT_FOUND.getCode(), "品牌不存在");
            }
            return Result.success(brand);
        } catch (Exception e) {
            log.error("获取品牌详情失败: id={}", id, e);
            return Result.error("获取品牌详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加品牌
     * POST /api/brands
     */
    @PostMapping
    public Result<?> addBrand(@RequestBody Brand brand) {
        try {
            // 验证必填字段
            if (brand.getName() == null || brand.getName().trim().isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "品牌名称不能为空");
            }

            boolean success = brandService.addBrand(brand);
            if (success) {
                return Result.success("添加品牌成功", brand);
            } else {
                return Result.error("添加品牌失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("添加品牌失败", e);
            return Result.error("添加品牌失败: " + e.getMessage());
        }
    }

    /**
     * 更新品牌
     * PUT /api/brands/{id}
     */
    @PutMapping("/{id}")
    public Result<?> updateBrand(@PathVariable Integer id, @RequestBody Brand brand) {
        try {
            // 确保ID一致
            brand.setId(id);

            // 验证必填字段
            if (brand.getName() == null || brand.getName().trim().isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "品牌名称不能为空");
            }

            boolean success = brandService.updateBrand(brand);
            if (success) {
                return Result.success("更新品牌成功");
            } else {
                return Result.error("更新品牌失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新品牌失败: id={}", id, e);
            return Result.error("更新品牌失败: " + e.getMessage());
        }
    }

    /**
     * 删除品牌（物理删除）
     * DELETE /api/brands/{id}
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteBrand(@PathVariable Integer id) {
        try {
            boolean success = brandService.deleteBrand(id);
            if (success) {
                return Result.success("删除品牌成功");
            } else {
                return Result.error("删除品牌失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除品牌失败: id={}", id, e);
            return Result.error("删除品牌失败: " + e.getMessage());
        }
    }

    /**
     * 上传品牌Logo
     * POST /api/brands/{id}/logo
     */
    @PostMapping("/{id}/logo")
    public Result<?> uploadBrandLogo(@PathVariable Integer id,
                                     @RequestParam String logoUrl) {
        try {
            boolean success = brandService.uploadLogo(id, logoUrl);
            if (success) {
                return Result.success("上传品牌Logo成功");
            } else {
                return Result.error("上传品牌Logo失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("上传品牌Logo失败: id={}", id, e);
            return Result.error("上传品牌Logo失败: " + e.getMessage());
        }
    }

    /**
     * 获取品牌统计信息
     * GET /api/brands/stats
     */
    @GetMapping("/stats")
    public Result<?> getBrandStats() {
        try {
            var stats = brandService.getBrandStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取品牌统计信息失败", e);
            return Result.error("获取品牌统计信息失败: " + e.getMessage());
        }
    }
}