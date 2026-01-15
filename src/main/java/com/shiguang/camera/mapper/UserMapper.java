package com.shiguang.camera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiguang.camera.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据身份证号查询用户
     * @param idCard 身份证号
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE id_card = #{idCard}")
    User selectByIdCard(@Param("idCard") String idCard);

    /**
     * 更新用户实名认证状态
     * @param userId 用户ID
     * @param isVerified 是否认证：0-否，1-是
     * @return 更新条数
     */
    @Update("UPDATE user SET is_verified = #{isVerified}, verified_time = NOW() WHERE id = #{userId}")
    int updateVerificationStatus(@Param("userId") Integer userId, @Param("isVerified") Integer isVerified);

    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param password 新密码
     * @return 更新条数
     */
    @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{userId}")
    int updatePassword(@Param("userId") Integer userId, @Param("password") String password);

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 更新条数
     */
    @Update("UPDATE user SET role_id = #{roleId}, update_time = NOW() WHERE id = #{userId}")
    int updateRole(@Param("userId") Integer userId, @Param("roleId") Integer roleId);
}