package com.hy.salon.basic.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.hy.salon.basic.common.StatusUtil;
import com.hy.salon.basic.dao.*;
import com.hy.salon.basic.entity.*;
import com.hy.salon.basic.vo.Result;
import com.hy.salon.stock.entity.ProductStock;
import com.zhxh.admin.entity.SystemUser;
import com.zhxh.admin.service.AuthenticateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Api(value = "WarehouseController|仓库控制器")
@RequestMapping("/hy/basic/warehouse")
@Controller
public class WarehouseController {

    @Resource(name="productStockDAO")
    private ProductStockDAO productStockDAO;

    @Resource(name="productStockMovementDao")
    private ProductStockMovementDao productStockMovementDao;

    @Resource(name="productDao")
    private ProductDao productDao;

    @Resource(name = "productPropertyDAO")
    private ProductPropertyDAO productPropertyDAO;

    @Resource(name = "stockTransferApplicationDao")
    private StockTransferApplicationDao stockTransferApplicationDao;

    @Resource(name = "picturesDao")
    private PicturesDAO picturesDao;

    @Resource(name = "productSeriesDao")
    private ProductSeriesDAO productSeriesDao;

    @Resource(name = "stuffDao")
    private StuffDao stuffDao;
    @Resource(name = "authenticateService")
    private AuthenticateService authenticateService;

    @Resource(name = "salonDao")
    private SalonDao salonDao;


    /**
     * 新增入库
     */
    @ResponseBody
    @RequestMapping("warehousing")
    @ApiOperation(value = "新增入库", notes = "新增入库")
    public Result warehousing(Long salonId, Long productId, Integer movementQty, Double purchaseCost, String remark,Byte recordCreateType,Date dateOfManufacture,String referenceRecordNo){
        Result result=new Result();
        try {
            //查看该产品之前是否入过库
            ProductStock proCondition=productStockDAO.getOneProdectStockForId(productId);
            if(null !=proCondition){
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                proCondition.setStockQty(proCondition.getStockQty()+movementQty);
                BigDecimal b3=b1.multiply(b2);
                BigDecimal cost=new BigDecimal(Double.toString(proCondition.getCost()));
                proCondition.setCost(cost.add(b3).doubleValue());
                productStockDAO.update(proCondition);
            }else{
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                proCondition=new ProductStock();
                proCondition.setWarehouseId(salonId);
                proCondition.setStockQty(movementQty);
                proCondition.setProductId(productId);
                proCondition.setCost(b1.multiply(b2).doubleValue());
                productStockDAO.insert(proCondition);
            }
            ProductStockMovement movement=new ProductStockMovement();
            movement.setRecordCreateType(recordCreateType);
            movement.setMovementType(new Byte("0"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String movementNo="HY"+formatter.format(new Date());
            movement.setMovementNo(movementNo);
            movement.setProductId(productId);
            movement.setMovementQty(movementQty);
            movement.setDateOfManufacture(dateOfManufacture);
            movement.setPurchaseCost(purchaseCost);
            movement.setWarehouseId(salonId);
            movement.setReferenceRecordNo(referenceRecordNo);
            movement.setRemark(remark);
            productStockMovementDao.insert(movement);

            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("入库成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("入库失败");
        }
        return result;
    }

    /**
     * 入库记录
     */
    @ResponseBody
    @RequestMapping("queryWarehousingList")
    @ApiOperation(value = "入库记录", notes = "入库记录")
    public Result queryWarehousingList(Long salonId,String movementType){
        Result result=new Result();
        try {
            if(salonId==null || salonId==0){
                SystemUser user = authenticateService.getCurrentLogin();
                Stuff stuff=stuffDao.getStuffForUser(user.getRecordId());

                salonId=stuff.getStoreId();
            }
            List<ProductStockMovement> ProductStockMovementList =productStockMovementDao.getProductForSalonId(salonId,movementType);
            result.setData(ProductStockMovementList);
            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("获取成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("获取失败");
        }
        return result;
    }

    /**
     * 入库详情
     */
    @ResponseBody
    @RequestMapping("queryWarehousingData")
    @ApiOperation(value = "入库记录", notes = "入库记录")
    public Result queryWarehousingData(Long recordId){
        Result result=new Result();
        try {
            JSONObject jsonObj=new JSONObject();
            ProductStockMovement ProductStockMovement =productStockMovementDao.getOneProductForSalonId(recordId);
            jsonObj.put("ProductStockMovement",ProductStockMovement);
            Product pro=productDao.getOneProdectForId(ProductStockMovement.getProductId());
            jsonObj.put("product",pro);
            List<Map<String,Object>> Property1= productPropertyDAO.getPropertyName(ProductStockMovement.getProductId(),new Byte("0"));
            List<Map<String,Object>> Property2= productPropertyDAO.getPropertyName(ProductStockMovement.getProductId(),new Byte("1"));
            if(null!=Property1 && Property1.size()!=0){
                jsonObj.put("specifications",Property1.get(0));
            }else{
                jsonObj.put("specifications","");
            }
            if(null!=Property2 && Property2.size()!=0){
                jsonObj.put("company",Property2.get(0));
            }else{
                jsonObj.put("company","");
            }
            List<Pictures> pic=picturesDao.getPicturesForCondition(pro.getRecordId(),new Byte("5"),new Byte("0"));
            if(null!=pic && pic.size()!=0){
                pro.setPicUrl(pic.get(0).getPicUrl());
            }

            result.setData(jsonObj);
            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("获取成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("获取失败");
        }
        return result;
    }


    /**
     * 新增出库
     */
    @ResponseBody
    @RequestMapping("outOfWarehouse")
    @ApiOperation(value = "新增出库", notes = "新增出库")
    public Result outOfWarehouse(Long salonId, Long productId, Integer movementQty, Double purchaseCost, String remark,Byte recordCreateType,Date dateOfManufacture,String referenceRecordNo){
        Result result=new Result();
        try {
            //查看该产品之前是否入过库
            ProductStock proCondition=productStockDAO.getOneProdectStockForId(productId);
            if(null !=proCondition){
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                proCondition.setStockQty(new BigDecimal(Integer.toString(proCondition.getStockQty())).subtract(new BigDecimal(Integer.toString(movementQty))).intValue());
                BigDecimal b3=b1.multiply(b2);
                BigDecimal cost=new BigDecimal(Double.toString(proCondition.getCost()));
                proCondition.setCost(cost.subtract(b3).doubleValue());
                productStockDAO.update(proCondition);
            }else{
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                BigDecimal  b3=new BigDecimal("0");


                proCondition=new ProductStock();
                proCondition.setWarehouseId(salonId);
                proCondition.setStockQty(b3.subtract(b1).intValue());
                proCondition.setProductId(productId);
                proCondition.setCost(b3.subtract(b1.multiply(b2)).doubleValue());
                productStockDAO.insert(proCondition);
            }
            ProductStockMovement movement=new ProductStockMovement();
            movement.setRecordCreateType(recordCreateType);
            movement.setMovementType(new Byte("64"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String movementNo="HY"+formatter.format(new Date());
            movement.setMovementNo(movementNo);
            movement.setProductId(productId);
            movement.setMovementQty(movementQty);
            movement.setDateOfManufacture(dateOfManufacture);
            movement.setPurchaseCost(purchaseCost);
            movement.setWarehouseId(salonId);
            movement.setReferenceRecordNo(referenceRecordNo);
            movement.setRemark(remark);
            productStockMovementDao.insert(movement);

            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("出库成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("出库失败");
        }
        return result;
    }


    /**
     * 新增调库
     */
    @ResponseBody
    @RequestMapping("banishWarehouse")
    @ApiOperation(value = "新增调库", notes = "新增调库")
    public Result banishWarehouse(Long salonId, Long productId, Integer movementQty, Double purchaseCost, String remark,Byte recordCreateType,Date dateOfManufacture,String referenceRecordNo,Long banishSalonId,String remarkApplication){
        Result result=new Result();
        try {
            //获取发起门店该产品
            Product product=productDao.getOneProdectForId(productId);
            //查看接收门店是否有该产品
            Product pro=productDao.checkName(banishSalonId,product.getProductName());
            if(null == pro){
                product.setRecordId(null);
                product.setStoreId(banishSalonId);
                pro=product;
                productDao.insert(pro);
            }

            //查看该产品之前是否入过库
            ProductStock proCondition=productStockDAO.getOneProdectStockForId(productId);
            //发起门店出库
            if(null !=proCondition){
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                proCondition.setStockQty(new BigDecimal(Integer.toString(proCondition.getStockQty())).subtract(new BigDecimal(Integer.toString(movementQty))).intValue());
                BigDecimal b3=b1.multiply(b2);
                BigDecimal cost=new BigDecimal(Double.toString(proCondition.getCost()));
                proCondition.setCost(cost.subtract(b3).doubleValue());
                productStockDAO.update(proCondition);
            }else{
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                BigDecimal  b3=new BigDecimal("0");
                proCondition=new ProductStock();
                proCondition.setWarehouseId(salonId);
                proCondition.setStockQty(b3.subtract(b1).intValue());
                proCondition.setProductId(productId);
                proCondition.setCost(b3.subtract(b1.multiply(b2)).doubleValue());
                productStockDAO.insert(proCondition);
            }
//            //查看该产品之前是否入过库
//            ProductStock proCondition2=productStockDAO.getOneProdectStockForId(pro.getRecordId());
//            //接收门店入库
//            if(null != proCondition2){
//                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
//                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
//                proCondition.setStockQty(new BigDecimal(Integer.toString(proCondition.getStockQty())).add(new BigDecimal(Integer.toString(movementQty))).intValue());
//                BigDecimal b3=b1.multiply(b2);
//                BigDecimal cost=new BigDecimal(Double.toString(proCondition.getCost()));
//                proCondition.setCost(cost.add(b3).doubleValue());
//                productStockDAO.update(proCondition);
//            }else{
//                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
//                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
//                proCondition=new ProductStock();
//                proCondition.setWarehouseId(salonId);
//                proCondition.setStockQty(b1.intValue());
//                proCondition.setProductId(productId);
//                proCondition.setCost(b1.multiply(b2).doubleValue());
//                productStockDAO.insert(proCondition);
//            }

            //发起门店产生一笔出库记录
            ProductStockMovement movement=new ProductStockMovement();
            movement.setRecordCreateType(recordCreateType);
            movement.setMovementType(new Byte("71"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String movementNo="HY"+formatter.format(new Date());
            movement.setMovementNo(movementNo);
            movement.setProductId(productId);
            movement.setMovementQty(movementQty);
            movement.setDateOfManufacture(dateOfManufacture);
            movement.setPurchaseCost(purchaseCost);
            movement.setWarehouseId(salonId);
            movement.setReferenceRecordNo(referenceRecordNo);
            movement.setRemark(remark);
            productStockMovementDao.insert(movement);


            //插入一笔记录
            StockTransferApplication stockTransferApplication=new StockTransferApplication();
            stockTransferApplication.setApplicationNo("HY"+formatter.format(new Date()));
            stockTransferApplication.setInWarehouseId(pro.getRecordId());
            stockTransferApplication.setProductStockMovementId(movement.getRecordId());
            stockTransferApplication.setOutWarehouseId(salonId);
            stockTransferApplication.setInWarehouseId(banishSalonId);
            stockTransferApplication.setRemarkApplication(remarkApplication);
            stockTransferApplication.setRecordStatus(new Byte("0"));
            stockTransferApplicationDao.insert(stockTransferApplication);



            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("调库成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("调库失败");
        }
        return result;
    }


    /**
     * 调拨审核
     */
    @ResponseBody
    @RequestMapping("banishWarehouseExamine")
    @ApiOperation(value = "调拨审核", notes = "调拨审核")
    public Result banishWarehouseExamine(Long StockTransferId){
        Result result=new Result();
        try {

            StockTransferApplication stockTransferApplication=stockTransferApplicationDao.queryOneStock(StockTransferId);

            ProductStockMovement productStockMovement= productStockMovementDao.getOneProductForSalonId(stockTransferApplication.getProductStockMovementId());
            //查看该产品之前是否入过库
            ProductStock proCondition=productStockDAO.getOneProdectStockForId(stockTransferApplication.getInWarehouseProductId());
            //发起门店出库
            if(null !=proCondition){
                BigDecimal  b1=new BigDecimal(Integer.toString(productStockMovement.getMovementQty()));
                BigDecimal  b2=new BigDecimal(Double.toString(productStockMovement.getPurchaseCost()));
                proCondition.setStockQty(new BigDecimal(Integer.toString(proCondition.getStockQty())).add(new BigDecimal(Integer.toString(productStockMovement.getMovementQty()))).intValue());
                BigDecimal b3=b1.multiply(b2);
                BigDecimal cost=new BigDecimal(Double.toString(proCondition.getCost()));
                proCondition.setCost(cost.add(b3).doubleValue());
                productStockDAO.update(proCondition);
            }else{
                BigDecimal  b1=new BigDecimal(Integer.toString(productStockMovement.getMovementQty()));
                BigDecimal  b2=new BigDecimal(Double.toString(productStockMovement.getPurchaseCost()));
                proCondition=new ProductStock();
                proCondition.setWarehouseId(stockTransferApplication.getInWarehouseId());
                proCondition.setStockQty(b1.intValue());
                proCondition.setProductId(stockTransferApplication.getInWarehouseProductId());
                proCondition.setCost(b1.multiply(b2).doubleValue());
                productStockDAO.insert(proCondition);
            }


            //接收门店产生一笔入库记录
            ProductStockMovement movement=new ProductStockMovement();
            movement.setRecordCreateType(new Byte("0"));
            movement.setMovementType(new Byte("1"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String movementNo="HY"+formatter.format(new Date());
            movement.setMovementNo(movementNo);
            movement.setProductId(stockTransferApplication.getInWarehouseProductId());
            movement.setMovementQty(productStockMovement.getMovementQty());
            movement.setDateOfManufacture(productStockMovement.getDateOfManufacture());
            movement.setPurchaseCost(productStockMovement.getPurchaseCost());
            movement.setWarehouseId(stockTransferApplication.getInWarehouseId());
            productStockMovementDao.insert(movement);

            stockTransferApplication.setRecordStatus(new Byte("4"));
            stockTransferApplicationDao.update(stockTransferApplication);

            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("调库成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("调库失败");
        }
        return result;
    }


    /**
     * 调库记录
     */
    @ResponseBody
    @RequestMapping("queryStockTransferList")
    @ApiOperation(value = "调库记录", notes = "调库记录")
    public Result queryStockTransferList(Long salonId,Byte  recordStatus){
        Result result=new Result();
        try {

            if(salonId==null || salonId==0){
                SystemUser user = authenticateService.getCurrentLogin();
                Stuff stuff=stuffDao.getStuffForUser(user.getRecordId());
                salonId=stuff.getStoreId();
            }
            List<StockTransferApplication> stockTransferApplication=stockTransferApplicationDao.getStockTransferApplication(recordStatus,salonId);
            result.setData(stockTransferApplication);
            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("获取成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("获取失败");
        }
        return result;
    }


    /**
     * 调库记录详情
     */
    @ResponseBody
    @RequestMapping("queryStockTransferData")
    @ApiOperation(value = "调库记录详情", notes = "调库记录详情")
    public Result queryStockTransferData(Long recordId){
        Result result=new Result();
        try {
            JSONObject jsonObj=new JSONObject();
            StockTransferApplication stockTransferApplication=stockTransferApplicationDao.queryOneStock(recordId);
            jsonObj.put("stockTransferApplication",stockTransferApplication);
            Salon inStore=salonDao.getSalonForStoreId(stockTransferApplication.getInWarehouseId());
            Salon outStore=salonDao.getSalonForStoreId(stockTransferApplication.getOutWarehouseId());
            jsonObj.put("inStore",inStore);
            jsonObj.put("outStore",outStore);
            ProductStockMovement ProductStockMovement =productStockMovementDao.getOneProductForSalonId(stockTransferApplication.getProductStockMovementId());
            jsonObj.put("ProductStockMovement",ProductStockMovement);
            Product pro=productDao.getOneProdectForId(stockTransferApplication.getInWarehouseProductId());
            jsonObj.put("product",pro);
            List<Map<String,Object>> Property1= productPropertyDAO.getPropertyName(stockTransferApplication.getInWarehouseProductId(),new Byte("0"));
            List<Map<String,Object>> Property2= productPropertyDAO.getPropertyName(stockTransferApplication.getInWarehouseProductId(),new Byte("1"));
            if(null!=Property1 && Property1.size()!=0){
                jsonObj.put("specifications",Property1.get(0));
            }else{
                jsonObj.put("specifications","");
            }
            if(null!=Property2 && Property2.size()!=0){
                jsonObj.put("company",Property2.get(0));
            }else{
                jsonObj.put("company","");
            }
            List<Pictures> pic=picturesDao.getPicturesForCondition(pro.getRecordId(),new Byte("5"),new Byte("0"));
            if(null!=pic && pic.size()!=0){
                pro.setPicUrl(pic.get(0).getPicUrl());
            }

            result.setData(jsonObj);
            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("获取成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("获取失败");
        }
        return result;
    }



    /**
     *产品库存记录
     */
    @ResponseBody
    @RequestMapping("queryProductStockList")
    @ApiOperation(value = "产品库存记录", notes = "产品库存记录")
    public Result queryProductStockList(Long salonId,String productClass,Long productSeriesId,int page,int limit){
        Result result=new Result();
        try {

            if(salonId==null || salonId==0){
                SystemUser user = authenticateService.getCurrentLogin();
                Stuff stuff=stuffDao.getStuffForUser(user.getRecordId());
                salonId=stuff.getStoreId();
            }

            result.setTotal(productDao.getProdectForCondition(salonId,productClass,productSeriesId).size());
            PageHelper.startPage(page, limit);
            List<Product> list=productDao.getProdectForCondition(salonId,productClass,productSeriesId);
            if(list.size()!=0){
                for(Product p:list){
                    List<Map<String,Object>> Property1= productPropertyDAO.getPropertyName(p.getRecordId(),new Byte("0"));
                    List<Map<String,Object>> Property2= productPropertyDAO.getPropertyName(p.getRecordId(),new Byte("1"));
                    if(null!=Property1 && Property1.size()!=0){
                        p.setSpecificationsName(Property1.get(0).get("propertyName").toString());
                    }
                    if(null!=Property2 && Property2.size()!=0){
                        p.setCompanyName(Property2.get(0).get("propertyName").toString());
                    }
                    List<Pictures> pic=picturesDao.getPicturesForCondition(p.getRecordId(),new Byte("5"),new Byte("0"));
                    if(null!=pic && pic.size()!=0){
                        p.setPicUrl(pic.get(0).getPicUrl());
                    }
                    //查看该产品之前是否入过库
                    ProductStock proStock=productStockDAO.getOneProdectStockForId(p.getRecordId());
                    if(proStock!=null){
                        p.setStockQty(proStock.getStockQty().toString());
                    }else{
                        p.setStockQty("0");
                    }

                }
            }

            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("查询成功");
            result.setData(list);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(true);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("查询失败");
        }
        return result;
    }

    /**
     * 获取产品详情
     */
    @ResponseBody
    @RequestMapping("queryProductData")
    @ApiOperation(value = "获取产品详情", notes = "通过id获取产品详情")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "recordId", value = "产品Id", required = true, dataType = "Long")
    })
    public Result queryProductData(Long recordId){
        Result result=new Result();
        try {

            Product p=productDao.getOneProdectForId(recordId);
            JSONObject jsonObj=new JSONObject();
            List<Pictures> picList=picturesDao.getPicturesForCondition(recordId,new Byte("5"),new Byte("0"));
            List<Map<String,Object>> Property1= productPropertyDAO.getPropertyName(recordId,new Byte("0"));
            List<Map<String,Object>> Property2= productPropertyDAO.getPropertyName(recordId,new Byte("1"));
            List<Map<String,Object>> Property3= productPropertyDAO.getPropertyName(recordId,new Byte("2"));
            List<Map<String,Object>> Property4= productPropertyDAO.getPropertyName(recordId,new Byte("3"));

            ProductSeries proSeries= productSeriesDao.getSeriesForId(p.getProductSeriesId());

            ProductSeries parentSeries= productSeriesDao.getSeriesForId(proSeries.getParentId());

            if(null!=Property1 && Property1.size()!=0){
                jsonObj.put("specifications",Property1.get(0));
            }else{
                jsonObj.put("specifications","");
            }
            if(null!=Property2 && Property2.size()!=0){
                jsonObj.put("sompany",Property2.get(0));
            }else{
                jsonObj.put("sompany","");
            }

            if(null!=Property3 && Property3.size()!=0){
                jsonObj.put("applicableParts",Property3.get(0));
            }else{
                jsonObj.put("applicableParts","");
            }

            if(null!=Property4 && Property4.size()!=0){
                jsonObj.put("effect",Property4.get(0));
            }else{
                jsonObj.put("effect","");
            }


            jsonObj.put("proSeries",proSeries);
            jsonObj.put("parentSeries",parentSeries);
            jsonObj.put("product",p);
            jsonObj.put("picList",picList);
            result.setData(jsonObj);
            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("获取成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("获取失败");
        }
        return result;
    }


/**
 * 盘点库存
 */
    @ResponseBody
    @RequestMapping("inventory")
    @ApiOperation(value = "盘点库存", notes = "盘点库存")
    public Result inventory(Long salonId, Long productId, Integer movementQty, Double purchaseCost, String remark,Byte recordCreateType,Date dateOfManufacture,String referenceRecordNo){
        Result result=new Result();
        try {
            //查看该产品之前是否入过库
            ProductStock proCondition=productStockDAO.getOneProdectStockForId(productId);
            if(null !=proCondition){
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                //获取相差数量


                proCondition.setStockQty(proCondition.getStockQty()+movementQty);
                BigDecimal b3=b1.multiply(b2);
                BigDecimal cost=new BigDecimal(Double.toString(proCondition.getCost()));
                proCondition.setCost(cost.add(b3).doubleValue());
                productStockDAO.update(proCondition);
            }else{
                BigDecimal  b1=new BigDecimal(Integer.toString(movementQty));
                BigDecimal  b2=new BigDecimal(Double.toString(purchaseCost));
                proCondition=new ProductStock();
                proCondition.setWarehouseId(salonId);
                proCondition.setStockQty(movementQty);
                proCondition.setProductId(productId);
                proCondition.setCost(b1.multiply(b2).doubleValue());
                productStockDAO.insert(proCondition);
            }
            ProductStockMovement movement=new ProductStockMovement();
            movement.setRecordCreateType(recordCreateType);
            movement.setMovementType(new Byte("0"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String movementNo="HY"+formatter.format(new Date());
            movement.setMovementNo(movementNo);
            movement.setProductId(productId);
            movement.setMovementQty(movementQty);
            movement.setDateOfManufacture(dateOfManufacture);

            movement.setPurchaseCost(purchaseCost);
            movement.setWarehouseId(salonId);
            movement.setReferenceRecordNo(referenceRecordNo);
            movement.setRemark(remark);
            productStockMovementDao.insert(movement);

            result.setSuccess(true);
            result.setMsgcode(StatusUtil.OK);
            result.setMsg("盘点成功");
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsgcode(StatusUtil.ERROR);
            result.setMsg("盘点失败");
        }
        return result;
    }



}