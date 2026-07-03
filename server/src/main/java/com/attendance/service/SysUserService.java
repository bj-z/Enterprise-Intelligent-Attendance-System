package com.attendance.service;

import com.attendance.common.PageResult;
import com.attendance.domain.SysUser;

public interface SysUserService {

    PageResult<SysUser> listByPage(int page, int pageSize, String keyword);

    SysUser create(SysUser user);

    void changeStatus(Long id, String status);

    void delete(Long id);
}
