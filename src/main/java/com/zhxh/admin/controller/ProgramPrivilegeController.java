package com.zhxh.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zhxh.admin.entity.SystemProgram;
import com.zhxh.admin.service.ProgramPrivilegeService;
import com.zhxh.admin.service.SystemProgramService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.zhxh.admin.entity.ProgramPrivilege;
import com.zhxh.core.web.ExtJsResult;
import com.zhxh.core.web.ListRequest;
import com.zhxh.core.web.ListRequestBaseHandler;
import com.zhxh.core.web.ListRequestProcessHandler;

@Controller
@RequestMapping("/admin/programPrivilege")
public class ProgramPrivilegeController {
    @Resource(name = "programPrivilegeService")
    private ProgramPrivilegeService programPrivilegeService;
    @Resource(name = "systemProgramService")
    private SystemProgramService systemProgramService;

    private final ListRequestProcessHandler listRequestProcessHandler = new ListRequestProcessHandler();


    //@RequestMapping("/updateListprogramPrivilege")
    public void getList(HttpServletRequest req){
       // SystemProgramService systemProgramService= new SystemProgramService();
        List<SystemProgram> list =  systemProgramService.getAll();
        for(SystemProgram sp :list){
           // ProgramPrivilegeService programPrivilegeService = new ProgramPrivilegeService();
            Map map = new HashMap<>();
            String where = "program_id=#{programId} ";
            Map parameters = new HashMap();
            parameters.put("programId", sp.getRecordId());
            List listprogramPrivilege =  programPrivilegeService.getPageListByProgramId(map,parameters);
            if(listprogramPrivilege.size()>0){

            }else{
                ProgramPrivilege pp = new ProgramPrivilege();
                pp.setProgramId(sp.getRecordId());
                pp.setPrivilegeCode("RUN");
                pp.setPrivilegeName("系统运行");

                programPrivilegeService.insert(pp);
            }
        }

    }
    @RequestMapping("getProgramPrivilegeByProgramId.handler")
    @ResponseBody
    public ExtJsResult getProgramPrivilegeByProgramId(HttpServletRequest request, HttpServletResponse response) {
	   Map parameters = new HashMap();
   	    parameters.put("programId",request.getParameter("programId"));
        return listRequestProcessHandler.getListFromHttpRequest(request, new ListRequestBaseHandler() {
            @Override
            public List getByRequest(ListRequest listRequest) {
                return programPrivilegeService.getPageListByProgramId(listRequest.toMap(), parameters);
            }

            @Override
            public int getRequestListCount(ListRequest listRequest) {
                return programPrivilegeService.getPageListCountByProgramId(listRequest.toMap(), parameters);
            }
        });
    }
    
    @RequestMapping("update.handler")
    @ResponseBody
    public ProgramPrivilege update(ProgramPrivilege item) {
    	programPrivilegeService.update(item);
        return item;
    }

    @RequestMapping("delete.handler")
    @ResponseBody
    public int delete(@RequestBody String[] id)  {
        return programPrivilegeService.delete(id);
    }
}
