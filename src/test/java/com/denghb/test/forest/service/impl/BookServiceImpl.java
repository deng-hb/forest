package com.denghb.test.forest.service.impl;

import com.denghb.eorm.Eorm;
import com.denghb.forest.annotation.Autowired;
import com.denghb.forest.annotation.Round;
import com.denghb.forest.annotation.Service;
import com.denghb.forest.annotation.Value;
import com.denghb.test.forest.Book;
import com.denghb.test.forest.service.BookService;
import com.denghb.test.forest.service.UserService;

@Service
public class BookServiceImpl implements BookService {

    @Value(name = "${a}")
    private String xx;

    @Autowired
    private Eorm eorm;

    @Autowired
    private UserService userService;

    @Round
    public void update() {

        Book book = new Book();
        eorm.update(book);
    }
}
