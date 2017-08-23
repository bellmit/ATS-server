package com.alucn.weblab.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.stereotype.Repository;

import com.alucn.casemanager.server.common.util.JdbcUtil;
import com.alucn.weblab.dao.DataAccessInterface;

/**
 * @author haiqiw
 * 2017年8月7日 下午2:58:48
 * desc:CaseSearchDaoImpl
 */

@Repository("caseSearchDaoImpl")
public class CaseSearchDaoImpl implements DataAccessInterface{

	@Override
	public void insert(JdbcUtil jdbc, String sql) throws Exception {
		jdbc.executeSql(sql);
	}

	@Override
	public void update(JdbcUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(JdbcUtil jdbc, String sql) throws Exception {
	}

	@Override
	public ArrayList<HashMap<String, Object>> query(JdbcUtil jdbc, String sql) throws Exception {
		return jdbc.query(sql);
	}
	
}
