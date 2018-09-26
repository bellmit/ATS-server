package com.alucn.weblab.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.stereotype.Repository;

import com.alucn.casemanager.server.common.util.JdbcUtil;
import com.alucn.weblab.dao.DataAccessInterface;

@Repository("userDaoImpl")
public class UserDaoImpl implements DataAccessInterface{

	@Override
	public void insert(JdbcUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		jdbc.executeSql(sql);
	}

	@Override
	public void update(JdbcUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		jdbc.executeSql(sql);
	}

	@Override
	public void delete(JdbcUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		jdbc.executeSql(sql);
	}

	@Override
	public ArrayList<HashMap<String, Object>> query(JdbcUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		return jdbc.query(sql);
	}
	
}
