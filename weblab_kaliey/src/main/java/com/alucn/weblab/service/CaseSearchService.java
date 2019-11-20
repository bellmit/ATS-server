package com.alucn.weblab.service;

import java.awt.ItemSelectable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.ScatteringByteChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;
import javax.swing.text.DefaultEditorKit.InsertBreakAction;

import org.apache.catalina.tribes.transport.nio.ParallelNioSender;
import org.junit.Test;
import org.openqa.jetty.html.Select;
import org.openqa.selenium.logging.NeedsLocalLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoader;

import com.alucn.casemanager.server.common.CaseConfigurationCache;
import com.alucn.casemanager.server.common.constant.Constant;
import com.alucn.casemanager.server.common.util.Fiforeader;
import com.alucn.casemanager.server.common.util.JdbcUtil;
import com.alucn.casemanager.server.common.util.ParamUtil;
import com.alucn.casemanager.server.listener.MainListener;
import com.alucn.weblab.controller.CaseSearchController;
import com.alucn.weblab.dao.impl.CaseSearchDaoImpl;
import com.alucn.weblab.socket.TcpClient;
import com.alucn.weblab.utils.JDBCHelper;
import com.alucn.weblab.utils.LabStatusUtil;
import com.alucn.weblab.utils.StringUtil;
import org.apache.log4j.Logger;
import org.apache.naming.java.javaURLContextFactory;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aspectj.apache.bcel.generic.ReturnaddressType;

import com.microsoft.schemas.office.visio.x2012.main.VisioDocumentDocument1;
import com.mysql.fabric.xmlrpc.base.Array;
import com.mysql.fabric.xmlrpc.base.Data;
import com.mysql.jdbc.log.Log;

import mx4j.tools.config.DefaultConfigurationBuilder.New;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * @author haiqiw
 * 2017年8月7日 上午10:58:41
 * desc:CaseSearchService
 */
@Service("caseSearchService")
public class CaseSearchService {
    private static Logger logger = Logger.getLogger(CaseSearchService.class);
	@Autowired(required=true)
	private CaseSearchDaoImpl caseSearchDaoImpl;
	private Map<String, Object> caseSearchItemMap = new HashMap<String, Object>();
	public static volatile String sqlAdmin = "";
	//private Lock lock = new ReentrantLock(true);
	@Autowired
	private ServletContext servletContext;
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public Map<String, List<String>> getCaseSearch() throws NumberFormatException, InterruptedException, IOException{
	    Map<String, List<String>> caseSearchMap = new HashMap<String, List<String>>();
		//ServletContext servletContext = ContextLoader.getCurrentWebApplicationContext().getServletContext();
		String configPath = System.getenv("WEBLAB_CONF");
		String [] args1 = {servletContext.getRealPath("conf")};       //localhost server
		String [] args2 = { configPath };
		//String tagConfig = MainListener.configFilesPath+File.separator+"TagConfig.json";
		String tagConfig = (args2[0]!=null?args2[0]:args1[0])+File.separator+"TagConfig.json";
		//D:\eclipse-jee-oxygen-3a-win32-x86_64\workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp1\wtpwebapps\weblab_kaliey\conf\TagConfig.json
		//System.err.println("getCaseSearch >> tagConfig >> "+tagConfig);
		
		System.err.println(tagConfig);
		
		JSONObject caseSearchItems = JSONObject.fromObject(Fiforeader.readCaseInfoFromChannel(tagConfig));
		JSONArray single = caseSearchItems.getJSONArray("single");
		for(int i=0; i<single.size(); i++){
			JSONObject caseSearchItem = single.getJSONObject(i);
			caseSearchMap.put(caseSearchItem.getString("name"), JSONArray.toList(caseSearchItem.getJSONArray("value")));
		}
		caseSearchItems = getJsonFileFromWeb(false);
		single = caseSearchItems.getJSONArray("single");
		for(int i=0; i<single.size(); i++){
			JSONObject caseSearchItem = single.getJSONObject(i);
			caseSearchMap.put(caseSearchItem.getString("name"), JSONArray.toList(caseSearchItem.getJSONArray("value")));
		}
		
		
		JSONArray multiple = caseSearchItems.getJSONArray("multiple");
		for(int i=0; i<multiple.size(); i++){
			JSONObject caseSearchItem = multiple.getJSONObject(i);
			caseSearchMap.put(caseSearchItem.getString("name"), JSONArray.toList(caseSearchItem.getJSONArray("value")));
		}
		
		return caseSearchMap;
	}
	
	
	public JSONObject get_tag_editable(boolean isAR)
	{
	    JSONArray tag_items = new JSONArray();
	    JSONArray do_checklist = new JSONArray();
	    JSONArray do_select = new JSONArray();
	    String key = "dft";
	    String value = "";
	    if(isAR)
	    {
	        key = "ar";
	    }
	    JSONArray dyamic_datas = new JSONArray();
	    JSONObject caseSearchItems = getJsonFileFromWeb(isAR);
	    
        JSONArray single = caseSearchItems.getJSONArray("single");
        for(int i=0; i<single.size(); i++){
            JSONObject caseSearchItem = single.getJSONObject(i);
            String key_name = caseSearchItem.getString("name");
            
            if("base_release".equals(key_name))
            {             
                JSONObject dyamic_data = new JSONObject();
                String name = "workable_release";
                dyamic_data.put("name", name);
                String element_id = key + "-" + name;
                JSONObject obj = new JSONObject();
                obj.put("id", element_id);
                JSONArray source = new JSONArray();
                source.add("");          
                JSONArray value_list = caseSearchItem.getJSONArray("value");
                source.addAll(value_list);
                obj.put("source", source);
                obj.put("value", new JSONArray());
                
                do_select.add(obj);
                value = "<a href=\"javascript:void(0)\" id=\"" + element_id + "\" data-name=\"" 
                      + element_id + "\" data-type=\"select\" data-pk=\"undefined\"  data-title=\"" 
                      + name + "\" data-value=\"\" class=\"editable editable-click editable-empty\">Empty</a>";
                
                dyamic_data.put("value", value);
                dyamic_datas.add(dyamic_data);
                break;
            }
        }
        
        for(int i=0; i<single.size(); i++){
            JSONObject caseSearchItem = single.getJSONObject(i);
            String name = caseSearchItem.getString("name");
            JSONObject dyamic_data = new JSONObject();
            dyamic_data.put("name", name);
            String element_id = key + "-" + name;
            JSONObject obj = new JSONObject();
            obj.put("id", element_id);
            obj.put("source", caseSearchItem.getJSONArray("value"));
            obj.put("value", new JSONArray());
            
            do_checklist.add(obj);
            value = "<a href=\"javascript:void(0)\" id=\"" + element_id + "\" data-name=\"" 
                  + element_id + "\" data-type=\"checklist\" data-pk=\"undefined\"  data-title=\"" 
                  + name + "\" data-value=\"\" class=\"editable editable-click editable-empty\">Empty</a>";
            
            dyamic_data.put("value", value);
            dyamic_datas.add(dyamic_data);
            
        }
        
        
        JSONArray multiple = caseSearchItems.getJSONArray("multiple");
        for(int i=0; i<multiple.size(); i++){
            JSONObject caseSearchItem = multiple.getJSONObject(i);
            String name = caseSearchItem.getString("name");
            JSONObject dyamic_data = new JSONObject();
            dyamic_data.put("name", name);
            String element_id = key + "-" + name;
            JSONObject obj = new JSONObject();
            obj.put("id", element_id);
            obj.put("source", caseSearchItem.getJSONArray("value"));
            obj.put("value", new JSONArray());
            
            do_checklist.add(obj);
            value = "<a href=\"javascript:void(0)\" id=\"" + element_id + "\" data-name=\"" 
                  + element_id + "\" data-type=\"checklist\" data-pk=\"undefined\"  data-title=\"" 
                  + name + "\" data-value=\"\" class=\"editable editable-click editable-empty\">Empty</a>";
            
            dyamic_data.put("value", value);
            dyamic_datas.add(dyamic_data);
        }
        
        JSONObject result = new JSONObject();
        result.put("do_select", do_select);
        result.put("do_checklist", do_checklist);
        result.put("datas", dyamic_datas);
        return result;
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
    public Map<String, Object> getCaseSearchPara() throws NumberFormatException, InterruptedException, IOException{
        
        String configPath = System.getenv("WEBLAB_CONF");
        String [] args1 = {servletContext.getRealPath("conf")};       //localhost server
        String [] args2 = { configPath };

        String tagConfig = (args2[0]!=null?args2[0]:args1[0])+File.separator+"TagConfig.json";

  
        JSONObject do_edit = new JSONObject();
        JSONArray do_checklist = new JSONArray();
        JSONArray do_select = new JSONArray();
        JSONArray do_text = new JSONArray();
        JSONArray do_date = new JSONArray();
        
        JSONArray dynamic_columns = new JSONArray();
        JSONObject dynamic_item = new JSONObject();
        dynamic_item.put("title", "name");
        dynamic_item.put("field", "name");
        dynamic_columns.add(dynamic_item);
        
        dynamic_item = new JSONObject();
        dynamic_item.put("title", "value");
        dynamic_item.put("field", "value");
        dynamic_columns.add(dynamic_item);
        
        JSONObject caseSearchItems = JSONObject.fromObject(Fiforeader.readCaseInfoFromChannel(tagConfig));
        
        for (Object str:caseSearchItems.keySet()) {
            String key = (String)str;
            JSONArray mypara_list = caseSearchItems.getJSONArray(key);
            JSONObject dyamic_obj = new JSONObject();
            dyamic_obj.put("columns", dynamic_columns);
            JSONArray dyamic_datas = new JSONArray();
            for(int i = 0; i < mypara_list.size(); i++)
            {
                JSONObject para = mypara_list.getJSONObject(i);
                String type = para.getString("type");
                String name = para.getString("name");
                String value = "";
                String element_id = key + "-" + name;
                JSONObject dyamic_data = new JSONObject();
                dyamic_data.put("name", name);
                String classes = "editable editable-click editable-empty";
                String display_value = "Empty";
                String data_value = "";
                if("checklist".equals(type))
                {
                    JSONObject obj = new JSONObject();
                    obj.put("id", element_id);
                    obj.put("source", para.getJSONArray("source"));
                    if(para.has("value"))
                    {
                        obj.put("value", para.getJSONArray("value"));
                        display_value = "";
                        for(int j = 0; j < para.getJSONArray("value").size(); j ++)
                        {
                            for(int k = 0; k < para.getJSONArray("source").size(); k ++)
                            {
                                if(para.getJSONArray("value").getString(j).equals(para.getJSONArray("source").getJSONObject(k).getString("value")))
                                {
                                    display_value += "<br>" + para.getJSONArray("source").getJSONObject(k).getString("text");
                                    data_value += "," + para.getJSONArray("source").getJSONObject(k).getString("value");
                                }
                            }
                            
                        }
                        if(!"".equals(display_value))
                        {
                            display_value = display_value.substring(4);
                            data_value = data_value.substring(1);
                        }
                        else
                        {
                            display_value = "Empty";
                        }
                        
                        classes = "editable editable-click";
                    }
                    else
                    {
                        obj.put("value", new JSONArray());
                    }
                    do_checklist.add(obj);
                    
                }
                else if("text".equals(type))
                {
                    JSONObject obj = new JSONObject();
                    obj.put("id", element_id);                   
                    do_text.add(obj);
                  
                }
                else if("date".equals(type))
                {
                    JSONObject obj = new JSONObject();
                    obj.put("id", element_id);
                    do_date.add(obj);
                }
                else if("select".equals(type))
                {
                    JSONObject obj = new JSONObject();
                    obj.put("id", element_id);
                    obj.put("source", para.getJSONArray("source"));
                    if(para.has("value"))
                    {
                        obj.put("value", para.getString("value"));
                        
                        for(int k = 0; k < para.getJSONArray("source").size(); k ++)
                        {
                            if(para.getString("value").equals(para.getJSONArray("source").getJSONObject(k).getString("value")))
                            {
                                display_value = para.getJSONArray("source").getJSONObject(k).getString("text");
                                break;
                            }
                        }
                                          
                        data_value = para.getString("value");
                        classes = "editable editable-click";
                    }
                    else
                    {
                        obj.put("value", "");
                    }
                    do_select.add(obj);
                    
                }
                
                value = "<a href=\"javascript:void(0)\" id=\"" + element_id + "\" data-name=\"" 
                        + element_id + "\" data-type=\"" + type + "\" data-pk=\"undefined\"  data-title=\"" 
                        + name + "\" data-value=\""+ data_value +"\" class=\"editable editable-click editable-empty\">" + display_value + "</a>";
                dyamic_data.put("value", value);
                dyamic_datas.add(dyamic_data);
            }
            dyamic_obj.put("datas", dyamic_datas);
            logger.info("key: ------------------------------" + key);
            logger.info("dyamic_obj: " + dyamic_obj.toString());

            caseSearchItemMap.put(key, dyamic_obj);
            
        }
        
        //for sanity release 
        JSONObject tag_items = getJsonFileFromWeb(false);
        
        JSONArray single = tag_items.getJSONArray("single");
        for(int i=0; i<single.size(); i++){
            JSONObject singleItem = single.getJSONObject(i);
            String key_name = singleItem.getString("name");
            
            if("base_release".equals(key_name))
            {             
                JSONObject dyamic_data = new JSONObject();
                String name = "target_release";
                dyamic_data.put("name", name);
                String element_id =  "sanity-" + name;
                JSONObject obj = new JSONObject();
                obj.put("id", element_id);
                JSONArray source = new JSONArray();
                source.add("");          
                JSONArray value_list = singleItem.getJSONArray("value");
                source.addAll(value_list);
                obj.put("source", source);
                obj.put("value", new JSONArray());
                
                do_select.add(obj);
                String value = "<a href=\"javascript:void(0)\" id=\"" + element_id + "\" data-name=\"" 
                      + element_id + "\" data-type=\"select\" data-pk=\"undefined\"  data-title=\"" 
                      + name + "\" data-value=\"\" class=\"editable editable-click editable-empty\">Empty</a>";
                
                dyamic_data.put("value", value);
                JSONObject sanity_obj = (JSONObject)caseSearchItemMap.get("sanity");
                JSONArray datas = sanity_obj.getJSONArray("datas");
                datas.add(1,dyamic_data);
                sanity_obj.put("datas", datas);
                caseSearchItemMap.put("sanity", sanity_obj);
                break;
            }
        }
        //dft
        JSONObject dft_tags = get_tag_editable(false);
        logger.info("dft_tags: " + dft_tags.toString());
        do_checklist.addAll(dft_tags.getJSONArray("do_checklist"));
        do_select.addAll(dft_tags.getJSONArray("do_select"));
        caseSearchItemMap.put("dft", dft_tags.getJSONArray("datas"));
      
        //ar
        JSONObject ar_tags = get_tag_editable(true);
        logger.info("ar_tags: " + ar_tags.toString());
        do_checklist.addAll(ar_tags.getJSONArray("do_checklist"));
        do_select.addAll(ar_tags.getJSONArray("do_select"));
        caseSearchItemMap.put("ar", ar_tags.getJSONArray("datas"));
        
        do_edit.put("select", do_select);
        do_edit.put("text", do_text);
        do_edit.put("date", do_date);
        do_edit.put("checklist", do_checklist);
        logger.info("do_edit: " + do_edit.toString());
        caseSearchItemMap.put("editable_obj", do_edit);
        
        return caseSearchItemMap;
    }
	
	@SuppressWarnings({ "deprecation", "unchecked" })
    public  Map<String, List<String>> getSearchParasAR() throws NumberFormatException, InterruptedException, IOException{
	    Map<String, List<String>> SearchItemMap = new HashMap<String, List<String>>();
        
        JSONObject caseSearchItems = getJsonFileFromWeb(true);
        JSONArray single = caseSearchItems.getJSONArray("single");
        for(int i=0; i<single.size(); i++){
            JSONObject caseSearchItem = single.getJSONObject(i);
            SearchItemMap.put(caseSearchItem.getString("name"), JSONArray.toList(caseSearchItem.getJSONArray("value")));
        }
        
        
        JSONArray multiple = caseSearchItems.getJSONArray("multiple");
        for(int i=0; i<multiple.size(); i++){
            JSONObject caseSearchItem = multiple.getJSONObject(i);
            SearchItemMap.put(caseSearchItem.getString("name"), JSONArray.toList(caseSearchItem.getJSONArray("value")));
        }
        
        return SearchItemMap;
    }
	
	public String cancelBatch(String batch_id) throws Exception {
	    String update_sql = "update cases_info_db.temp_run_case set run_result = 'C', complete_date = 'Cancel' where batch_id = '" + batch_id + "' and run_result = ''";
	    logger.error("update_sql:==="+update_sql);
        JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
        int count = caseSearchDaoImpl.update(jdbc, update_sql);
        if (count > 0)
        {
            return "SUCCESS";
        }
        
        return "FAIL";
    }
	
	
	
	public JSONArray getCaseServer(String deptid,boolean hasRole){
		// Map<String, List<String>> caseSearchItemMap = new HashMap<String, List<String>>();
		//JSONArray Servers = CaseConfigurationCache.readOrWriteSingletonCaseProperties(CaseConfigurationCache.lock, true,null);
		JSONArray Servers = null;
		try {
			Servers = LabStatusUtil.getLabStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// List<String> serversName = new ArrayList<String>();
		JSONArray result = new JSONArray();
		for (int i = 0; i < Servers.size(); i++) {
			JSONObject ServerMem = Servers.getJSONObject(i).getJSONObject("body").getJSONObject(Constant.LAB);
			// String serverName = ServerMem.getString(Constant.SERVERNAME);
			String sdeptid = ServerMem.getString("deptid");
			// getCaseServer: {"deptid":"1","serverName":"BJRMS21E","serverIp":"135.242.17.206","serverRelease":"SP17.9","serverProtocol":"ITU","serverType":"Group","serverMate":"Primary","mateServer":"BJRMS21F","setName":"set1","serverSPA":["AethosTest","CDRPP311","CDRPPGW311","DIAMCL179","DROUTER179","ECTRL179","ENWTPPS179","EPAY179","EPPSA179","EPPSM179","GATEWAY179","NWTCOM111","NWTGSM066"],"serverRTDB":["SCRRTDBV7","AECIDB179","SGLDB28H","TIDDB28C","GPRSSIM08","AIRTDB179","CTRTDB179","HTIDDB179","PMOUDB179","PROMDB179","SIMDB179","SYDB179","GCIPL312","VTXDB179","SHRTDB28F","CDBRTDB","RCNRDB173","HMRTDB173","SESSDB311","ACMDB104","SIMIDXDB","FSNDB173","UARTDB287","RERTDB279","SFFDB28C","GCURDB","SLTBLRTDB","ID2MDN01","GTMDB28A"],"laststatus":"Dead","lasttime":1552614155131}
			// if(sdeptid.equals(deptid)||hasRole) {
			if(sdeptid.equals(deptid)) {
				// serversName.add(serverName);
				result.add(ServerMem);
			}
		}
		// caseSearchItemMap.put("servers",serversName);
		return result;
	}
	
	public String convert_condition_to_html(JSONObject condition)
	{
	    String result= "";
	    for (Object str:condition.keySet()) {
            String key = (String)str;
            result += "<strong>" + key + ": </strong>" + condition.get(key).toString() + "<br>";
	    }
	    return result;
	}
	
	public String convert_condtion_to_sql(String condition)
	{
	    String sql = "";
	    String database = "";
	    String [] conds = condition.split("&");
	    for (int i = 0; i < conds.length; i++)
	    {
	        String [] paras = conds[i].split("=");
	        if ("data_source".equals(paras[0]))
	        {
	            if("DailyCase".equals(paras[1]))
	            {
	                database = "cases_info_db.daily_case";
	            }
	            else{
	                database = "cases_info_db.case_tag";
	            }
	        }
	        else if ("protocol".equals(paras[0])) 
	        {   if(paras.length == 2)
                {
    	            if("ANSI".equals(paras[1])) {
                        sql += " and base_data like 'VzW%'";
                    }else if ("ITU".equals(paras[1])) {
                        sql += " and base_data not like 'VzW%'";
                    }
                }
            }
	        else if ("scenario".equals(paras[0])) 
            {   if(paras.length == 2)
                {
                        sql += " and type = '" + paras[1] + "'"; 
                }
            }
	        else if ("workable_release".equals(paras[0])) {
	            if(paras.length == 2)
                {
                    try {
                        JSONObject tags = getJsonFileFromWeb(false);
                        JSONArray single_list = tags.getJSONArray("single");
                        List<String> release_list = new ArrayList<String>();
                        for(int j = 0; j < single_list.size(); j++)
                        {
                            if("base_release".equals(single_list.getJSONObject(j).getString("name")))
                            {
                                for(int k = 0; k < single_list.getJSONObject(j).getJSONArray("value").size(); k++)
                                {
                                    release_list.add(single_list.getJSONObject(j).getJSONArray("value").getString(k));
                                }
                                break;
                            }
                        }
                        
                        logger.info("release_list: " + release_list.toString());

                        int indexOf = -1;
                        if(release_list.contains(paras[1])) {
                            indexOf = release_list.indexOf(paras[1]);
                        }
                        List<String> subList = new ArrayList<>();
                        if(indexOf != -1) {
                            subList = release_list.subList(0, indexOf+1);
                        }
                        if(subList.size()>0) {
                            sql += " and (";
                            //System.err.println(subList); // [SP29.12, SP29.13, SP29.14, SP29.15]
                            for (String str : subList) {
                                sql += "porting_release like '%" + str + "+%' or ";
                            }
                            sql = sql.substring(0, sql.length() -3) + " or base_release = '" + paras[1] + "' "
                                       +" or porting_release like '%" + paras[1] + "%')";
                        }
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
	        else 
	        {
	            if(paras.length == 2)
	            {
	                sql += " and " + paras[0] + " in (";
	                String [] para_conds = paras[1].split(",");	           
	                for(int j = 0; j < para_conds.length; j++)
	                {
	                    sql += "'" + para_conds[j] + "'";
	                    if(j != para_conds.length-1){           
	                        sql += ",";
	                    }
	                }
	                sql += ")";

	            }
            }
	    }
	    sql = " from " + database + " where 1=1 " + sql;
	    return sql;
	}
	
	public Object searchCaseInfo(Map<String, Object> param ,String cond, String auth,String retrunType) throws Exception{
		
	    
		String scope = "*";
		if(retrunType=="total") {
			scope = "count(1) rcount";
		}
		String sql;
		String tmp_sql = convert_condtion_to_sql(cond);
		System.err.println("tmp_sql:==="+tmp_sql);
        sql = "select "+scope+ tmp_sql;
		
		if(""!=param.get("caseName")&&param.get("caseName")!=null) {
			sql += "and case_name='"+param.get("caseName")+"' ";
		}

        if(retrunType=="rows") {
            if(""!=param.get("offset")&& ""!=param.get("limit")){
                sql +=" limit "+param.get("offset")+","+param.get("limit");
            }
        }
		
		//query_sql:===select * from DailyCase where 1=1 and feature_number='731590' and case_name='null'  limit null,null
		logger.error("query_sql:==="+sql);
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
				//new ArrayList<HashMap<String, Object>>();
		
		/*if(!"".equals(conds[11]) && !"0".equals(conds[11])){
			query = caseSearchDaoImpl.query(jdbc, sql);
			if(retrunType=="insert") {
				if(query!=null && query.size()!=0){
					jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
					String query_sql="select max(group_id) gid from toDistributeCases";
					String gid = caseSearchDaoImpl.query(jdbc, query_sql).get(0).get("gid").toString();
					JSONArray Servers = CaseConfigurationCache.readOrWriteSingletonCaseProperties(CaseConfigurationCache.lock,true, null);
					JSONArray spaList=new JSONArray();
					JSONArray rtdbList=new JSONArray();
					for (int i = 0; i < Servers.size(); i++) {
						JSONObject serverMem = Servers.getJSONObject(i).getJSONObject(Constant.LAB);
						String serverName = serverMem.getString(Constant.SERVERNAME);
						if(serverName.equals(conds[11])){
							spaList=serverMem.getJSONArray(Constant.SERVERSPA);
							rtdbList=serverMem.getJSONArray(Constant.SERVERRTDB);
						}
					}
					for(int i=0; i<query.size(); i++){
						String disSql="INSERT INTO toDistributeCases (case_name, lab_number, mate, special_data, base_data, second_data, release, porting_release, SPA, RTDB, server, customer, group_id) VALUES('"
								+query.get(i).get("case_name")+"', '"
								+query.get(i).get("lab_number")+"', '"
								+query.get(i).get("mate")+"', '"
								+query.get(i).get("special_data")+"', '"
								+query.get(i).get("base_data")+"', '"
								+query.get(i).get("second_data")+"', '"
								+query.get(i).get("release")+"', '"
								+query.get(i).get("porting_release")+"', '"
								+spaList.toString()+"', '"
								+rtdbList.toString()+"', '"
								+query.get(i).get("server")+"', '"
								+query.get(i).get("customer")+"', "
								+gid+");";
						caseSearchDaoImpl.insert(jdbc, disSql);
					}
				}
			}
		}else if(auth.equals("all")){
			sqlAdmin=sql;
			sqlAdmin+=" order by lab_number asc,special_data asc;";
			query = caseSearchDaoImpl.query(jdbc, sql);
		}*/
		if(retrunType=="rows" || retrunType=="condition") {
			return query;
		}
		if(retrunType=="total") {
			return query.get(0).get("rcount");
		}
		return "";//"Total record:"+query.size();
	}
	
	public static JSONObject getJsonFileFromWeb(boolean isAR) {
		URL url;
		JSONObject tagInfos = new JSONObject();
		try {
		    String url_addr = "http://135.251.249.250/hg/SurepayDraft/rawfile/tip/.info/TagConfig.json";
		    if(isAR)
		    {
		        url_addr = "http://135.251.249.250/hg/SurepayAR/rawfile/tip/.info/TagConfig.json";
		    }
			url = new URL(url_addr);
			InputStream inputStream = null;
			InputStreamReader inputStreamReader = null;
			BufferedReader reader = null;
			String tempLine, response = "";
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = connection.getInputStream();
				inputStreamReader = new InputStreamReader(inputStream);
				reader = new BufferedReader(inputStreamReader);

				while ((tempLine = reader.readLine()) != null) {
					response += tempLine;

				}

				tagInfos = JSONObject.fromObject(response);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tagInfos;
	}
	/**
	 * <pre>
	 * Example: String insertToDistributeCasesTbl = caseSearchService.insertToDistributeCasesTbl(paramMap);
	 * Description: 虽然不知道什么原理，但是只要把case信息房到这张表里面就可以指定server运行
	 * 					1、将操作记录到一张日志表内方便后续跟踪
	 *                  2、将数据放到目标表内
	 * Arguments: 参数map
	 * Return: String
	 * Variable：null
	 * </pre>
	 * @throws Exception 
	 */
	public Map<String,Object> insertToDistributeCasesTbl(Map<String,Object> paramMap) throws Exception {
		
		Map<String,Object> returnMap = new HashMap<String,Object>();
		
		String ids = (String)paramMap.get("ids").toString();
		String condition = (String)paramMap.get("condition");
		String login = (String)paramMap.get("login");
		String formtitle = (String)paramMap.get("formtitle");
		// String title ="["+login+"]"+ids.split(",")[0]+"...";
		
		
		if(!"".equals(ids)&&!"".equals(condition)&&!"".equals(login)) {
			//JSONArray Servers = CaseConfigurationCache.getSingletonCaseProperties(CaseConfigurationCache.lock);
			JSONArray Servers = LabStatusUtil.getLabStatus();
			StringBuffer midString =new StringBuffer();
			String substring ="";
			if(!ids.contains(",")) {
				substring = midString.append("'"+ids+"'").toString();
			}else {
				String[] split = ids.split(",");
				for (String string : split) {
					midString.append("'"+string+"',");
				}
				int lastIndexOf = midString.lastIndexOf(",");
				substring = midString.substring(0, lastIndexOf);
			}
			
			String [] conds = condition.split(";");
			
			if("".equals(conds[0])){
				returnMap.put("msg", "Please select a data source !");
				returnMap.put("result", false);
				return returnMap;
			}
			
			/*
			 * 不做任何校验
			 * String csql = "select * from kaliey.n_rerunning_case_tbl where stateflag='0' and case_info ='"+ids+"' order by create_time desc";
			jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
			ArrayList<HashMap<String,Object>> cquery = caseSearchDaoImpl.query(jdbc, csql);
			if(cquery.size()>0) {
				SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
				String date = (String) cquery.get(0).get("create_time");
				Date ndate = new Date();
				long from = simpleFormat.parse(date).getTime();
				long to = ndate.getTime();
				int minutes = (int) ((to - from)/(1000 * 60));
				Object object = cquery.get(0).get("server_info");
				JSONObject server_info = JSONObject.fromObject(object);
				String serverName = (String) server_info.get("serverName");
				//如果数据库的server_info字段为空，那么下面的代码会报空指针异常java.lang.NullPointerException
				if(minutes<=10 && serverName.equals(conds[11])) {
					returnMap.put("msg", "you checked case will be running in 10 minutes !");
					returnMap.put("result", false);
					return returnMap;
				}
			}*/
			
			
			/*String tsql ="select distinct case_name from toDistributeCases";
			ArrayList<HashMap<String,Object>> tquery = caseSearchDaoImpl.query(jdbc, tsql);
			String str = "";
			for (int i=0;i<tquery.size();i++) {
				if(i!=tquery.size()&&i!=0) {
					str=str+",";
				}
				String case_name = "'"+(String)tquery.get(i).get("case_name")+"'";
				str=str+case_name;
			}
			System.err.println("str:======="+str);*/
			/*JdbcUtil jdbc = null;
			if(conds[0].equals(Constant.DAILYCASE)){
				jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
			}else if(conds[0].equals(Constant.DFTTAG)){
				jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("DftCaseDB"));
			}*/
			String dataBase = "";
			if("DailyCase".equals(conds[0])) {
				dataBase = "cases_info_db.daily_case";
			} else if("DftTag".equals(conds[0])) {
				dataBase = "cases_info_db.case_tag";
			}
			String sql = "select * from "+dataBase+" where 1=1 and case_name in ("+substring+")";// and case_name not in ("+str+")";
			JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
			ArrayList<HashMap<String,Object>> query = caseSearchDaoImpl.query(jdbc, sql);
			if(query.size()==0) {
				returnMap.put("result", false);
				returnMap.put("msg", "you checked case will be running !");
				return returnMap;
			}
			
			/*String nsql = "select case_name from "+conds[0]+" where 1=1 and case_name in ("+substring+")";// and case_name in ("+str+")";
			ArrayList<HashMap<String,Object>> nquery = caseSearchDaoImpl.query(jdbc, nsql);*/
			
			
			//jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
			String query_sql="select ifnull(max(group_id),0)+1 gid from toDistributeCases;";
			String gid = caseSearchDaoImpl.query(jdbc, query_sql).get(0).get("gid").toString();
			
			String server = "";
			List serverList = new ArrayList<>();
			if(conds[13].contains(",")) {
				String[] servers = conds[13].split(",");
				server = server+"[";
				for (int i=0; i<servers.length;i++) {
					serverList.add(servers[i]);
					if(i==servers.length-1) {
						server = server+"\""+servers[i]+"\"";
					}else {
						server = server+"\""+servers[i]+"\",";
					}
				}
				server = server+"]";
			}else {
				serverList.add(conds[13]);
				server = server+"[\""+conds[13]+"\"]";
			}
			//此处代码只允许一个server的spa和rtdb
			//明显没有考虑多个server的情况
			//当前问题是如果取每个server的spa和rtdb的交集是否会有影响
			/*JSONObject serverMemDb = new JSONObject();
			JSONArray spaList=new JSONArray();
			JSONArray rtdbList=new JSONArray();
			for (int i = 0; i < Servers.size(); i++) {
				JSONObject serverMem = Servers.getJSONObject(i).getJSONObject(Constant.LAB);
				String serverName = serverMem.getString(Constant.SERVERNAME);
				if(serverName.equals(conds[11])){
					spaList=serverMem.getJSONArray(Constant.SERVERSPA);
					rtdbList=serverMem.getJSONArray(Constant.SERVERRTDB);
					serverMemDb=serverMem;
					System.err.println("serverMemDb:===="+serverMemDb);
				}
			}*/
			
			//[变更]将符合条件的case指定服务器运行 4需求修改<1>
			//4. check case & lab match status for case list before running.
		    //   for matched partial list, continue to execute;
		    //   for unmatched partial list, mark separate list
			
			Map<String,Object> serverMap = new HashMap<String,Object>();
			
			for (int z = 0; z < Servers.size(); z++) {
				Map<String,Object> serverAttr = new HashMap<String,Object>();
				JSONObject serverMem = Servers.getJSONObject(z).getJSONObject(Constant.BODY).getJSONObject(Constant.LAB);
				String serverName = serverMem.getString(Constant.SERVERNAME);
				if(serverList.contains(serverName)){
					serverAttr.put("spaList", serverMem.getJSONArray(Constant.SERVERSPA));
					serverAttr.put("rtdbList", serverMem.getJSONArray(Constant.SERVERRTDB));
					serverMap.put(serverName, serverAttr);
				}
			}
			
			
			
			Map<String,Object> csMap = new HashMap<String,Object>();
			Map<String,Object> cfMap = new HashMap<String,Object>();
			for(int i=0; i<query.size(); i++){
				
				String dpendSql = "select * from cases_info_db.case_depends where case_name='"+query.get(i).get("case_name")+"'";
				ArrayList<HashMap<String, Object>> dpendList = caseSearchDaoImpl.query(jdbc, dpendSql);
				String spa = "";
				String db = "";
				String t_spa ="[";
				String t_db="[";
				List l_spa = new ArrayList<>();
				List l_db = new ArrayList<>();
				
				//DIAMCL,ENWTPPS,NWTCOM,EPAY
				//["DIAMCL","ENWTPPS","NWTCOM","EPAY"]
				if(dpendList.size()>0) {
					spa = (String) dpendList.get(0).get("spa");
					db = (String) dpendList.get(0).get("db");
				}
				if(spa!=null&&!"".equals(spa)) {
					if(spa.contains(",")) {
						String[] split = spa.split(",");
						int j = 0;
						for (String str : split) {
							l_spa.add(str);
							if(j==split.length-1) {
								t_spa=t_spa + "\""+str+"\"]";
							}else {
								t_spa=t_spa + "\""+str+"\",";
							}
							j++;
						}
					}else {
						l_spa.add(spa);
						t_spa=t_spa + "\""+spa+"\"]";
					}
				}else {
					t_spa="";
				}
				if(db!=null&&!"".equals(db)) {
					if(db.contains(",")) {
						String[] split = db.split(",");
						int j = 0;
						for (String str : split) {
							l_db.add(str);
							if(j==split.length-1) {
								t_db=t_db + "\""+str+"\"]";
							}else {
								t_db=t_db + "\""+str+"\",";
							}
							j++;
						}
					}else {
						l_db.add(db);
						t_db=t_db + "\""+db+"\"]";
					}
				}else {
					t_db="";
				}
				
				//System.err.println("t_spa:===="+t_spa);
				//System.err.println("t_db:===="+t_db);
				
				//[变更]将符合条件的case指定服务器运行 4需求修改<2>
				//4. check case & lab match status for case list before running.
			    //   for matched partial list, continue to execute;
			    //   for unmatched partial list, mark separate list
				
				
				List csList = new ArrayList<>();
				List cfList = new ArrayList<>();
				for(String serverName :serverMap.keySet()) {
					Map<String,Object> serverAttr = (Map<String, Object>) serverMap.get(serverName);
					JSONArray spaArray = (JSONArray) serverAttr.get("spaList");
					JSONArray rtdbArray = (JSONArray) serverAttr.get("rtdbList");
					List spaList = JSONArray.toList(spaArray);
					List spaList2 = new ArrayList<>(); 
					for (Object object : spaList) {
						spaList2.add((""+object).trim().replaceAll("\\d+\\w?$", "").replace(".*", "").replace("RTDB ", "").toUpperCase());
					}
					List rtdbList = JSONArray.toList(rtdbArray);
					List rtdbList2 = new ArrayList<>(); 
					for (Object object : rtdbList) {
						rtdbList2.add((""+object).trim().replaceAll("\\d+\\w?$", "").replace(".*", "").replace("RTDB ", "").toUpperCase());
					}
					//满足条件的case
					System.err.println(spaList2.containsAll(l_spa));
					System.err.println(rtdbList2.containsAll(l_db));
					System.err.println(spaList2+"\n"+l_spa);
					System.err.println(rtdbList2+"\n"+l_db);
					/*if(spaList2.containsAll(l_spa) && rtdbList2.containsAll(l_db)) {
						csList.add("\""+serverName+"\"");
					}else {
						cfList.add(serverName+":"+spaList2.containsAll(l_spa)+":"+rtdbList2.containsAll(l_db));
					}*/
					//暂不check运行条件
					csList.add("\""+serverName+"\"");
				}
				if(csList.size()>0) {
					csMap.put(query.get(i).get("case_name").toString(), csList);
					String disSql="replace into toDistributeCases (case_name, lab_number, mate, special_data, base_data, second_data, `release`, porting_release, SPA, RTDB, server, customer, group_id) VALUES('"
							+query.get(i).get("case_name")+"', '"
							+query.get(i).get("lab_number")+"', '"
							+query.get(i).get("mate")+"', '"
							+query.get(i).get("special_data")+"', '"
							+query.get(i).get("base_data")+"', '"
							+query.get(i).get("second_data")+"', '"
							+query.get(i).get("release")+"', '"
							+query.get(i).get("porting_release")+"', '"
							/*+spaList.toString()+"', '"
							+rtdbList.toString()+"', '"*/
							+t_spa+"', '"
							+t_db+"', '"
							+csList.toString().trim()+"', '"
							+query.get(i).get("customer")+"', "
							+gid+");";
					caseSearchDaoImpl.insert(jdbc, disSql);
				}else {
					cfMap.put(query.get(i).get("case_name").toString(), cfList);
				}
				//csMap:=-=-=-=--={731590/fr0589.json=[BJRMS21B, BJRMS21A, BJRMS21F, BJRMS21E, BJRMS21D, BJRMS21C]}
				System.err.println("csMap:=-=-=-=--="+csMap);
			}
			
			
			
			
			returnMap.put("s_case", csMap.size());
			returnMap.put("f_case", cfMap.size());
			int i=0;
			
			
			PreparedStatement ps = null;
			PreparedStatement ups = null;
			PreparedStatement sps = null;
			Connection conn = jdbc.getConnection();
			conn.setAutoCommit(false);
			
			while(i<10) {
				//lock.lock();
				String idsql="select ifnull(max(int_id),0)+1 max_id from kaliey.n_rerunning_case_tbl";
				ArrayList<HashMap<String, Object>> idList = caseSearchDaoImpl.query(jdbc, idsql);
				String s_max_id = (String) idList.get(0).get("max_id");
				int max_id = 1;
				if(s_max_id!=null) {
					max_id = Integer.parseInt(s_max_id);
				}
				try {
					String isql ="insert into kaliey.n_rerunning_case_tbl(int_id,title,server_info,query_condition,author,create_time) values("
							//+max_id+",'"+title +"','"+server+"','"+condition+"','"+login+"',datetime('now', 'localtime'))";
					+max_id+",'"+formtitle +"','"+server+"','"+condition+"','"+login+"',now())";
					ps = conn.prepareStatement(isql);
					ps.execute();
					ps.close();
					
					
					
					String baksql="insert into kaliey.n_case_bak_tbl(case_name,case_status,case_server,spa,db,rerunning_id) values(?,?,?,?,?,?)";
					ups = conn.prepareStatement(baksql);
					for (HashMap<String, Object> bMap : query) {
						String dpendSql = "select * from case_depends where case_name='"+(String)bMap.get("case_name")+"'";
						ArrayList<HashMap<String, Object>> dpendList = caseSearchDaoImpl.query(jdbc, dpendSql);
						String spa = "";
						String db = "";
						if(dpendList.size()>0) {
							spa = (String) dpendList.get(0).get("spa");
							db = (String) dpendList.get(0).get("db");
						}
						//判断是否是通过check的case
						String case_server="";
						for(String case_name :csMap.keySet()) {
							if(case_name==(String)bMap.get("case_name")&& case_name.equals((String)bMap.get("case_name"))) {
								case_server=csMap.get(case_name).toString();
							}
						}
						for(String case_name :cfMap.keySet()) {
							if(case_name==(String)bMap.get("case_name")&& case_name.equals((String)bMap.get("case_name"))) {
								case_server=cfMap.get(case_name).toString();
							}
						}
						
						
						ups.setString(1, (String)bMap.get("case_name"));
						ups.setString(2, (String)bMap.get("case_status"));
						ups.setString(3, case_server);
						ups.setString(4, spa);
						ups.setString(5, db);
						ups.setString(6, max_id+"");
						ups.addBatch();
					}
					ups.executeBatch();
					ups.close();
					
					
					
					String serverSql ="insert into kaliey.n_case_server_tbl(rerunning_id,serverName,serverIp,serverRelease,serverProtocol,serverType,serverMate,mateServer,setName,serverSPA,serverRTDB) "
							+ "values(?,?,?,?,?,?,?,?,?,?,?) ";
					sps = conn.prepareStatement(serverSql);
					
					
					for (int j = 0; j < Servers.size(); j++) {
						JSONObject serverMem = Servers.getJSONObject(j).getJSONObject(Constant.BODY).getJSONObject(Constant.LAB);
						String serverName = serverMem.getString(Constant.SERVERNAME);
						boolean flag =false;
						if(conds[11].contains(",")) {
							String[] servers = conds[11].split(",");
							List<String> list=Arrays.asList(servers);
							if(list.contains(serverName)) {
								flag=true;
							}
						}else {
							if(serverName.equals(conds[11])) {
								flag=true;
							}
						}
						
						if(flag){
							sps.setString(1, max_id+"");
							sps.setString(2, (String)serverMem.get("serverName"));
							sps.setString(3, (String)serverMem.get("serverIp"));
							sps.setString(4, (String)serverMem.get("serverRelease"));
							sps.setString(5, (String)serverMem.get("serverProtocol"));
							sps.setString(6, (String)serverMem.get("serverType"));
							sps.setString(7, (String)serverMem.get("serverMate"));
							sps.setString(8, (String)serverMem.get("mateServer"));
							sps.setString(9, (String)serverMem.get("setName"));
							sps.setString(10, serverMem.get("serverSPA").toString());
							sps.setString(11, serverMem.get("serverRTDB").toString());
							sps.addBatch();
						}
					}
					sps.executeBatch();
					sps.close();
					
					
					
					
					
					
					conn.commit();
					returnMap.put("result", true);
					break;
				} catch (Exception e) {
					e.printStackTrace();
					conn.rollback();
					i++;
					Thread.sleep(1000);
				}finally {
					conn.close();
					//lock.unlock();
				}
			}
			
		}
		return returnMap;
	}
	public Object searchCaseRunLogInfo(Map<String, Object> param, String auth, String retrunType, List<String> deptids,Boolean adminFlag) throws Exception {
		
		/*String sql ="select int_id,title,server_info,condition,author,create_time from kaliey.n_rerunning_case_tbl where stateflag='0' order by create_time desc";*/
		/*String dept_ids = "";
		if(adminFlag) {
			String dsql = "select id deptid from kaliey.n_dept where stateflag=0";
			ArrayList<HashMap<String, Object>> queryDept = caseSearchDaoImpl.query(jdbc, dsql);
			List<String> allDeptids= new ArrayList<String>();
			if(queryDept.size()>0) {
				for (HashMap<String, Object> hashMap : queryDept) {
					String deptid = ""+hashMap.get("deptid");
					if(deptid!=null && !"".equals(deptid)) {
						allDeptids.add(deptid);
					}
				}
			}
			dept_ids = StringUtil.formatSplitList(allDeptids);
		}else {
			dept_ids = StringUtil.formatSplitList(deptids);
		}*/
		String sql ="select distinct a.int_id,a.run_mode, a.title,a.server_info,a.query_condition,a.author,a.create_time,ifnull(d.result_count,0) running_count "+
				"from kaliey.n_rerunning_case_tbl a " + 
				"left join kaliey.n_user b on a.author=b.username and b.stateflag='0' " + 
				"left join kaliey.n_user_dept c on b.id=c.user_id  and c.stateflag='0' " + 
				"left join (select batch_id,count(1) result_count from cases_info_db.temp_run_case where run_result='' group by  batch_id) d on a.int_id=d.batch_id "+
				"where a.stateflag='0' "; 
		if(!adminFlag) {
			String dept_ids = StringUtil.formatSplitList(deptids);
			sql =sql +"and c.dept_id in("+dept_ids+") ";
		}
			sql =sql +"order by a.create_time desc ";
		/*if(auth=="all") {
			
		}*/
		if(retrunType=="rows") {
			if(""!=param.get("offset")&& ""!=param.get("limit")){
				sql +=" limit "+param.get("offset")+","+param.get("limit");
			}
		}
		//JdbcUtil jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		if(retrunType=="rows") {
			return query;
		}
		if(retrunType=="total") {
			return query.size()+"";
		}
		return "";//"Total record:"+query.size();
	}
	public ArrayList<HashMap<String, Object>> searchCaseRunLogInfoById(Map<String, Object> param) throws Exception {
		String sql ="select *  from kaliey.n_rerunning_case_tbl where stateflag='0' and int_id= "+param.get("int_id");
		//JdbcUtil jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		return query;
	}
	public ArrayList<HashMap<String, Object>> searchCaseRunLogCaseInfoById(Map<String, Object> param) throws Exception {
		
		Boolean case_status = (Boolean)param.get("case_status");
		String sql = "";
		if(case_status) {
			sql ="select * from kaliey.n_case_bak_tbl where stateflag='0' and rerunning_id= "+param.get("int_id") +" and case_server not like '%:%'";
		}else {
			sql ="select * from kaliey.n_case_bak_tbl where stateflag='0' and rerunning_id= "+param.get("int_id") +" and case_server like '%:%'";
		}
		System.err.println(sql);
		//JdbcUtil jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
		String case_str = "";
		if(query.size()>0) {
			for (HashMap<String, Object> hashMap : query) {
				String case_name = (String) hashMap.get("case_name");
				case_str=case_str+"'"+case_name+"',";
			}
			int lastIndexOf = case_str.lastIndexOf(",");
			String substring = case_str.substring(0, lastIndexOf);
			System.err.println(substring);
			//jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("DftCaseDB"));
			sql ="select * from cases_info_db.case_tag a left join cases_info_db.case_depends b on a.case_name= b.case_name where a.case_name in ("+substring+")";
			arrayList = caseSearchDaoImpl.query(jdbc, sql);
			
		}
		for (HashMap<String, Object> hashMap : arrayList) {
			for (HashMap<String, Object> qMap : query) {
				if(qMap.get("case_name").equals(hashMap.get("case_name"))) {
					hashMap.put("per_case_status", qMap.get("case_status"));
					hashMap.put("spa", qMap.get("spa"));
					hashMap.put("db", qMap.get("db"));
					String case_server = (String)qMap.get("case_server");
					hashMap.put("case_server",case_server);
				}
			}
		}
		return arrayList;
	}
	
	public String searchSanityCases() throws Exception{
	    String sql ="select * from cases_info_db.certify_server_config";
	    JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
	    ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
	    for(int i = 0; i < query.size(); i++)
	    {
	        if(query.get(i).get("con_key").toString().equals("sanity_case_list"))
	        {
	            return query.get(i).get("con_value").toString();
	        }
	    }
	    return "";
	    
	}
	public ArrayList<HashMap<String, Object>> searchCaseRunLogCaseServerById(Map<String, Object> param) throws Exception {
		String sql ="select * from kaliey.n_case_server_tbl where stateflag='0' and rerunning_id= "+param.get("int_id");
		//JdbcUtil jdbc = new JdbcUtil(Constant.DATASOURCE,ParamUtil.getUnableDynamicRefreshedConfigVal("CaseInfoDB"));
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		return query;
	}
	
	public JSONObject condition_to_jsonobject(String condition)
	{
	    JSONObject con = new JSONObject();
	    String [] conds = condition.split("&");
        for (int i = 0; i < conds.length; i++)
        {
            String [] paras = conds[i].split("=");
            if(paras.length == 2)
            {
                con.put(paras[0], paras[1]);
            }
            
        }
	    return con;
	}
	
	@Transactional
	public Map<String,Object> onlyrun(String set, String servers,String title,String login,String only, String select_all_flag, String condition, String run_mode) throws Exception {
		Map<String,Object> returnMap = new HashMap<String,Object>();
		String server = StringUtil.formatJsonString(servers);
		// 第零步，判断是否是search传进来的case,如果是有一种特殊情况，全选flag选中后，直接使用关联insert
		
		String complete_date = "";
        String target_release = "";
        String work_type = "dft";
        String schedule_date = "";
        String hotslide = "";    
        JSONObject con = condition_to_jsonobject(condition);
        if(con.has("schedule_date"))
        {
            schedule_date = con.getString("schedule_date");
        }
        if(con.has("hotslide"))
        {
            hotslide = con.getString("hotslide");
        }
        if(!"".equals(schedule_date))
        {
            complete_date = "WAIT";
            if(con.has("workable_release"))
            {
                target_release = con.getString("workable_release");
            }
            else
            {
                returnMap.put("msg", "When schedule_date set, workable_release must set!");
                return returnMap;
            }
        }
        if(con.has("scenario"))
        {
            work_type = con.getString("scenario");
        }
        
		if(select_all_flag!="" && "true".equals(select_all_flag)) {
		    System.out.println("select all");
			int caseLogId = insertCaseLog(title,server,login,condition,run_mode);
			if(caseLogId!=-1) {
				// 保存server和case信息
				String[] split2 = servers.split(",");
				JSONArray Servers = LabStatusUtil.getLabStatus();
				List<Object []> serverParams = new ArrayList<Object []>();
				for (String str : split2) {
					for (int i = 0; i < Servers.size(); i++) {
						JSONObject serverMem = Servers.getJSONObject(i).getJSONObject(Constant.BODY).getJSONObject(Constant.LAB);
						String serverName = serverMem.getString(Constant.SERVERNAME);
						String serverIp = serverMem.getString(Constant.IP);
						String serverRelease = serverMem.getString(Constant.SERVERRELEASE);
						String serverProtocol = serverMem.getString(Constant.SERVERPROTOCOL);
						String serverType = serverMem.getString(Constant.SERVERTYPE);
						String serverMate = serverMem.getString(Constant.SERVERMATE);
						String mateServer = serverMem.getString(Constant.MATESERVER);
						String setName = serverMem.getString(Constant.SETNAME);
						String serverSPA = serverMem.getString(Constant.SERVERSPA);
						String serverRTDB = serverMem.getString(Constant.SERVERRTDB);
						if(str.contains(serverName)){
							//rerunning_id,serverName,serverIp,serverRelease,serverProtocol,serverType,serverMate,mateServer,setName,serverSPA,serverRTDB
							
							Object [] paramsArray = new Object[11];
		                    
		                    paramsArray[0] = caseLogId;
		                    paramsArray[1] = serverName;
		                    paramsArray[2] = serverIp;
		                    paramsArray[3] = serverRelease;
		                    paramsArray[4] = serverProtocol;
		                    paramsArray[5] = serverType;
		                    paramsArray[6] = serverMate;
		                    paramsArray[7] = mateServer;
		                    paramsArray[8] = setName;
		                    paramsArray[9] = serverSPA;
		                    paramsArray[10] = serverRTDB;
							serverParams.add(paramsArray);
						}
					}	
				}
				insertCaseServer(serverParams);
				
				String sql = "insert into cases_info_db.temp_run_case(case_name,only_run_flag,target_labs,submit_owner,submit_date,batch_id,hotslide, complete_date, target_release, schedule_date) "
						+ splitCondition(condition, "", "case_name,'Y','"+server+"','"+login+"',now(),"+caseLogId+",'"+hotslide+"'"+",'"+complete_date+"'"+",'"+target_release+"'"+",'"+schedule_date+"'");
				System.out.println(sql);
				JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
				caseSearchDaoImpl.insert(jdbc, sql);
			}
		}
		// 第一步，校验数据是否在数据库中，确认不是胡乱传的
		if (!"".equals(set)) {
			String[] split = set.split(",");
			String casesql = "";
			for (String string : split) {
				casesql = casesql +"'"+string.trim() +"',";
			}
			int len = split.length;
			String cases = casesql.substring(0, casesql.length()-1);
			String type = work_type;
			if("sanity".equals(run_mode))
			{
			    type = "sanity";
			}
			String checkCaseInfo = checkCaseInfo(cases,split, type);
			if("".equals(checkCaseInfo)) {
				int caseLogId = insertCaseLog(title,server,login,condition, run_mode);
				if("sanity".equals(run_mode))
	            {
	                complete_date = "WAIT_SANITY";
	            }
				if (caseLogId!=-1) {
					// 保存server和case信息
					String[] split2 = servers.split(",");
					JSONArray Servers = LabStatusUtil.getLabStatus();
					List<Object []> serverParams = new ArrayList<Object []>();
					for (String str : split2) {
						for (int i = 0; i < Servers.size(); i++) {
							JSONObject serverMem = Servers.getJSONObject(i).getJSONObject(Constant.BODY).getJSONObject(Constant.LAB);
							String serverName = serverMem.getString(Constant.SERVERNAME);
							String serverIp = serverMem.getString(Constant.IP);
							String serverRelease = serverMem.getString(Constant.SERVERRELEASE);
							String serverProtocol = serverMem.getString(Constant.SERVERPROTOCOL);
							String serverType = serverMem.getString(Constant.SERVERTYPE);
							String serverMate = serverMem.getString(Constant.SERVERMATE);
							String mateServer = serverMem.getString(Constant.MATESERVER);
							String setName = serverMem.getString(Constant.SETNAME);
							String serverSPA = serverMem.getString(Constant.SERVERSPA);
							String serverRTDB = serverMem.getString(Constant.SERVERRTDB);
							if(str.contains(serverName)){
								//rerunning_id,serverName,serverIp,serverRelease,serverProtocol,serverType,serverMate,mateServer,setName,serverSPA,serverRTDB
							    Object [] paramsArray = new Object[11];
	                            
	                            paramsArray[0] = caseLogId;
	                            paramsArray[1] = serverName;
	                            paramsArray[2] = serverIp;
	                            paramsArray[3] = serverRelease;
	                            paramsArray[4] = serverProtocol;
	                            paramsArray[5] = serverType;
	                            paramsArray[6] = serverMate;
	                            paramsArray[7] = mateServer;
	                            paramsArray[8] = setName;
	                            paramsArray[9] = serverSPA;
	                            paramsArray[10] = serverRTDB;
	                            serverParams.add(paramsArray);
							}
						}	
					}
					insertCaseServer(serverParams);
					
					List<Object []> params = new ArrayList<Object []>();
					for (String casename : split) {
					    Object [] paramsArray = new Object[10];
						paramsArray[0] = casename;
						paramsArray[1] = only;
						paramsArray[2] = server;
						paramsArray[3] = login;
						paramsArray[4] = new Date().getTime();
						paramsArray[5] = caseLogId;
						paramsArray[6] = hotslide;
						paramsArray[7] = complete_date;
						paramsArray[8] = target_release;
						paramsArray[9] = schedule_date;
						params.add(paramsArray);
					}
					// 第三步，将数据传入正式运行表中
					InsertTempRunCase(params);
				}
			}else {
				returnMap.put("msg", checkCaseInfo);
			}
		}
		return returnMap;
	}
	public String checkCaseInfo(String set, String [] cases, String work_type) throws Exception {
		
		String sql ="select case_name from cases_info_db.case_tag where type = '" + work_type +"' and case_name in ("+set+")";
		logger.debug("checkCaseInfo sql: " + sql);
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		if(query.size()==cases.length) {
			return "";
		}
		else{
		    logger.debug(cases.length);
		    logger.debug(query.size());
		    
		    logger.debug(cases.toString());
		    logger.debug(query.toString());
		    List input_cases =new ArrayList();
		    for(int i =0; i < cases.length; i++)
		    {
		        input_cases.add(cases[i]);
		    }
		    
		    List get_cases =new ArrayList();
		    for(int i = 0; i < query.size(); i++)
		    {
		        get_cases.add(query.get(i).get("case_name").toString());
		    }
		    
		    input_cases.removeAll(get_cases);
		    return "Below cases not exist: " + input_cases.toString();
		}
		
	}
	public int insertCaseLog(String title,String server,String login,String condition, String run_mode) throws Exception {
		String isql ="insert into kaliey.n_rerunning_case_tbl(title,server_info,query_condition,author,create_time, run_mode) values('"+title +"','"+server+"','"+condition+"','"+login+"',now(),'" + run_mode + "')";
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		int id = jdbc.executeSqlReturnId(isql);
		return id;
	}
	public void insertCaseServer(List<Object []> params) throws SQLException {
		String serverSql ="insert into kaliey.n_case_server_tbl(rerunning_id,serverName,serverIp,serverRelease,serverProtocol,serverType,serverMate,mateServer,setName,serverSPA,serverRTDB) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?) ";
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		jdbc.executeBatch(serverSql,params);
	}
	public void insertCaseBak(List<Object []> params) throws SQLException {
		String baksql="insert into kaliey.n_case_bak_tbl(case_name,case_status,case_server,spa,db,rerunning_id) values(?,?,?,?,?,?)";
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		jdbc.executeBatch(baksql,params);
	}
	public void InsertTempRunCase(List<Object []> params) throws Exception {
		String isql ="insert into cases_info_db.temp_run_case(case_name,only_run_flag,target_labs,submit_owner,submit_date,batch_id,hotslide, complete_date, target_release, schedule_date) "
				+ "values(?,?,?,?,?,?,?,?,?,?)";
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		jdbc.executeBatch(isql,params);
	}
	public ArrayList<HashMap<String,Object>> getTempRunCaseResultByBatchId(String batchid) throws Exception {
		String sql ="select * from cases_info_db.temp_run_case where batch_id="+batchid + " order by html_path DESC";
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String,Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		return query;
	}
	
	
	public XSSFWorkbook getTempRunCaseResultByBatchIds(String condition) throws Exception {
	    String tmp_sql = convert_condtion_to_sql(condition);
	    System.out.println("tmp_sql: " + tmp_sql);
	    String sql ="select case_name, count(case_name) as case_num from cases_info_db.temp_run_case " + 
	            tmp_sql.substring(5) + " and run_result = 'F' group by case_name order by case_num DESC";
        //String sql ="select * from cases_info_db.temp_run_case where batch_id="+batchid + " order by html_path DESC";
        JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
        ArrayList<HashMap<String,Object>> query = caseSearchDaoImpl.query(jdbc, sql);
        sql = "select * from cases_info_db.temp_run_case " + tmp_sql.substring(5) + " and run_result = 'F'";
        ArrayList<HashMap<String,Object>> cases = caseSearchDaoImpl.query(jdbc, sql);
        
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sh = wb.createSheet();
        Row title = sh.createRow(0);
        int k = 1;
        for(int i = 0; i < query.size(); i ++)
        {
            String case_name = query.get(i).get("case_name").toString();
            for(int j = 0; j <cases.size(); j ++ )
            {
                if( i == 0 && j == 0)
                {
                    int m = 0;
                    HashMap<String,Object> hashMap = cases.get(0);
                    for(String key :hashMap.keySet()) {
                        Cell titleCell = title.createCell(m);
                        titleCell.setCellValue(""+key);
                        m++;
                    }
                    Cell titleCell = title.createCell(m);
                    titleCell.setCellValue("case_num");
                }
                if(case_name.equals(cases.get(j).get("case_name").toString()))
                {
                    int m = 0;
                    Row row = sh.createRow(k);
                    k++;
                    HashMap<String,Object> hashMap = cases.get(j);
                    for(String key :hashMap.keySet()) {
                        Cell cell = row.createCell(m);
                        String value = hashMap.get(key)+"";
                        if("html_path".equalsIgnoreCase(key) && !"".equals(String.valueOf(hashMap.get(key))))
                        {
                            CreationHelper createHelper = wb.getCreationHelper();
                            XSSFHyperlink  link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
                            link.setAddress(value);
                            cell.setHyperlink(link);
                            cell.setCellValue(value);
                        }
                        else
                        {
                            cell.setCellValue(value);
                        }
                        m++;
                    }
                    Cell cell = row.createCell(m);
                    cell.setCellValue(query.get(i).get("case_num").toString());
                }
            }
        }
  
        return wb;
    }
	@Test
	public void test() throws Exception {
		CaseSearchService cs = new CaseSearchService();
		cs.onlyrun("72624/fn3916.json,72624/fn3917.json,72624/fn3918.json,72624/fn3919.json","testserver","testtitle","root","Y","","","");
	}
	public String splitCondition(String cond,String retrunType,String fields) throws NumberFormatException, InterruptedException, IOException {
	    
	    
		String sql = "";
		String dataBase = "";
		
		String scope = "*";
        if(retrunType=="total") {
            scope = "count(1) rcount";
        }
        if(!"".equals(fields)) {
            scope = fields;
        }
        
        String tmp_sql = convert_condtion_to_sql(cond);
        System.err.println("tmp_sql:==="+tmp_sql);
        sql = "select "+scope+ tmp_sql;
        
		return sql;
	}

	public String getBattchStatus(String id) throws Exception {
		String sql = "select count(1) result_count from cases_info_db.temp_run_case where batch_id="+id
				+" union all "+
				"select count(1) result_count from cases_info_db.temp_run_case where run_result='' and batch_id="+id;
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		if (query.size()>0) {
			int all_count = Integer.parseInt(String.valueOf(query.get(0).get("result_count")));
			if(all_count==0) {
				return "Error";
			}
			int runing_count = Integer.parseInt(String.valueOf(query.get(1).get("result_count")));
			if(runing_count>0) {
				return "Running";
			}else {
				return "Complete";
			}
		}
		return null;
		
	}

	public Map<String, Object> getBattchStatusCount(String int_id) throws Exception {
		Map<String, Object> result = new HashMap<>();
		int runing_count = 0;
		int success_count = 0;
		int fail_count = 0;
		int cancel_count = 0;
		String sql ="select count(1) result_count from cases_info_db.temp_run_case where run_result='' and batch_id=" +int_id+ 
				" union all " + 
				"select count(1) result_count from cases_info_db.temp_run_case where run_result='S' and batch_id=" +int_id+ 
				" union all " + 
				"select count(1) result_count from cases_info_db.temp_run_case where run_result='F' and batch_id="+int_id +
				" union all " + 
                "select count(1) result_count from cases_info_db.temp_run_case where run_result='C' and batch_id="+int_id;
		JDBCHelper jdbc = JDBCHelper.getInstance("mysql-1");
		ArrayList<HashMap<String, Object>> query = caseSearchDaoImpl.query(jdbc, sql);
		if (query.size()>0) {
			runing_count = Integer.parseInt(String.valueOf(query.get(0).get("result_count")));
			success_count = Integer.parseInt(String.valueOf(query.get(1).get("result_count")));
			fail_count = Integer.parseInt(String.valueOf(query.get(2).get("result_count")));
			cancel_count = Integer.parseInt(String.valueOf(query.get(3).get("result_count")));
		}
		result.put("runing_count", runing_count);
		result.put("success_count", success_count);
		result.put("fail_count", fail_count);
		result.put("cancel_count", cancel_count);
		return result;
	}

	public String getBattchStatusByIds(ArrayList<String> mid) {
		String ids = StringUtil.formatSplitList(mid);
		return null;
		
	}
}
