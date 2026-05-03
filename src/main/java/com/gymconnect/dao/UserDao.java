package com.gymconnect.dao;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    private SessionFactory sessionFactory;

    public List<String> findAllUsernames() {
        List<String> usernames = sessionFactory.getCurrentSession()
                .createQuery("SELECT u.username FROM User u", String.class)
                .list();
        logger.debug("Found {} existing usernames", usernames.size());
        return usernames;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
