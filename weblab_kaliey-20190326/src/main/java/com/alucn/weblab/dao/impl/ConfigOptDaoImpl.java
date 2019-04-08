package com.alucn.weblab.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Repository;

import com.alucn.casemanager.server.common.util.JdbcUtil;
import com.alucn.weblab.dao.DataAccessInterface;
import com.alucn.weblab.dao.KalieyDataAccessInterface;
import com.alucn.weblab.utils.KalieyMysqlUtil;

/**
 * @author haiqiw
 * 2017年6月23日 下午2:04:04
 * desc:ConfigOptDaoImpl
 */
@Repository("configOptDaoImpl")
public class ConfigOptDaoImpl implements KalieyDataAccessInterface{

	@Override
	public void insert(KalieyMysqlUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		jdbc.executeSql(sql);
	}

	@Override
	public void update(KalieyMysqlUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		jdbc.executeSql(sql);
	}

	@Override
	public void delete(KalieyMysqlUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		jdbc.executeSql(sql);
	}

	@Override
	public ArrayList<HashMap<String, Object>> query(KalieyMysqlUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		return jdbc.query(sql);
	}

	/*@Override
	public void insert(JdbcUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(JdbcUtil jdbc, String sql) throws Exception {
		jdbc.executeSql(sql);
	}

	@Override
	public void delete(JdbcUtil jdbc, String sql) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<HashMap<String, Object>> query(JdbcUtil jdbc, String sql) throws Exception {
		return jdbc.query(sql);
	}*/

}
