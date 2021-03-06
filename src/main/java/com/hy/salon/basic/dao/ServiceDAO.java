package com.hy.salon.basic.dao;

import com.hy.salon.basic.entity.Service;
import com.hy.salon.basic.vo.ServiceVo;
import com.hy.salon.basic.vo.StockTransferApplicationVo;
import com.zhxh.core.data.BaseDAOWithEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("serviceDao")
public class ServiceDAO extends BaseDAOWithEntity<Service> {

    public int insertService(Service condition){
        return insert(condition);
    }

    public List<Service> queryServiceForId(Long Id){
        String where="store_id=#{Id}";
        Map parameters = new HashMap();
        parameters.put("Id", Id);
        return this.getByWhere(where,parameters);
    }

    public Service queryOneService(Long recordId){
        String where="record_id=#{recordId}";
        Map parameters = new HashMap();
        parameters.put("recordId", recordId);
        return this.getOne(where,parameters);

    }

    public List<Service> queryServiceForServiceId(Long serviceId,Long storeId){
        String where="service_series_id=#{serviceId} and store_id = #{storeId}";
        Map parameters = new HashMap();
        parameters.put("storeId", storeId);
        parameters.put("serviceId", serviceId);
        return this.getByWhere(where,parameters);
    }




    public List<ServiceVo> getServiceForSuit(Long id) {
        Map parameters = new HashMap();
        parameters.put("id", id);
        return this.getSqlHelper().getSqlSession().selectList(SQL_GET_SERVICE_FOR_SUIT, parameters);
    }

    public List<Map<String,Object>> queryServiceForSeries(Long parentId) {
        Map parameters = new HashMap();
        parameters.put("parentId", parentId);
        return this.getSqlHelper().getSqlSession().selectList(SQL_QUERY_SERVICE_FOR_SERIES, parameters);
    }

    public List<Map<String,Object>> queryBinServiceForSeries(Long suiteId) {
        Map parameters = new HashMap();
        parameters.put("suiteId", suiteId);
        return this.getSqlHelper().getSqlSession().selectList(SQL_QUERY_BIN_SERVICE_FOR_SERIES, parameters);
    }

    protected final static String SQL_GET_SERVICE_FOR_SUIT = "com.hy.salon.basic.dao.GET_SERVICE_FOR_SUIT";

    protected final static String SQL_QUERY_SERVICE_FOR_SERIES = "com.hy.salon.basic.dao.QUERY_SERVICE_FOR_SERIES";

    protected final static String SQL_QUERY_BIN_SERVICE_FOR_SERIES = "com.hy.salon.basic.dao.QUERY_BIN_SERVICE_FOR_SERIES";









}
