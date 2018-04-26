package com.denghb.test.forest.service.impl;

import com.denghb.eorm.Eorm;
import com.denghb.forest.annotation.Autowired;
import com.denghb.forest.annotation.Service;
import com.denghb.forest.annotation.Transaction;
import com.denghb.test.forest.User;
import com.denghb.test.forest.service.UserService;

@Service
public class UserMockServiceImpl implements UserService {


    @Autowired
    private Eorm eorm;


    @Transaction
    public void create() {

        User user = new User();
        user.setName("fixedRate");
        user.setMobile("1000L");
        eorm.insert(user);

        user.setName("xxxx");
        eorm.update(user);

//        throw new RuntimeException("rollback");

    }
}
