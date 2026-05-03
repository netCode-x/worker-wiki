package com.codebit.api.repository;

import com.codebit.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description:
 * @VERSON: 17
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户，返回 Optional 可以优雅处理空值情况
     * 方法名符合 JPA 规范，自动生成 select ... where username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * 判断用户名是否存在，用于注册时校验
     * 返回 boolean，自动生成 exists 查询，效率高
     */
    boolean existsByUsername(String username);


    /**
     * 查询邮件
     * @param email
     * @return
     */
    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

}
