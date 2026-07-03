package com.attendance.service.impl;

import com.attendance.common.BusinessException;
import com.attendance.common.PageResult;
import com.attendance.domain.SysUser;
import com.attendance.repository.SysUserRepository;
import com.attendance.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUser> listByPage(int page, int pageSize, String keyword) {
        Page<SysUser> result;
        if (keyword != null && !keyword.isEmpty()) {
            result = sysUserRepository.searchByKeyword(keyword, PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id")));
        } else {
            result = sysUserRepository.findAll(PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id")));
        }
        return new PageResult<>(result.getContent(), result.getTotalElements(), page, pageSize);
    }

    @Override
    @Transactional
    public SysUser create(SysUser user) {
        if (sysUserRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("该账号已存在");
        }
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setStatus("正常");
        user.setLastLogin(LocalDateTime.now());
        return sysUserRepository.save(user);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, String status) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("系统管理员".equals(user.getRole())) {
            throw new BusinessException("系统管理员状态不可修改");
        }
        user.setStatus(status);
        sysUserRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("系统管理员".equals(user.getRole())) {
            throw new BusinessException("系统管理员不可删除");
        }
        sysUserRepository.delete(user);
    }
}
