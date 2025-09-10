package com.erokin.mokaaicodemother.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.erokin.mokaaicodemother.entity.User;
import com.erokin.mokaaicodemother.mapper.UserMapper;
import com.erokin.mokaaicodemother.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/EROQIN">Erokin</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
