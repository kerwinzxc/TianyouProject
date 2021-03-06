package com.k3k.unplat.controller.slg;

import com.k3k.unplat.entity.slg.SlgAccountStatis;
import com.k3k.unplat.service.slg.SlgAccountService;
import com.k3k.unplat.utils.Page;
import com.k3k.unplat.utils.ServletUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jef on 2016/12/7.
 */
@Controller
@RequestMapping(value = "slgaccount")
public class SlgAccountController {
    private static final Log LOGGER = LogFactory.getLog(SlgAccountController.class);
    private static final SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");


    @Autowired
    private SlgAccountService slgAccountService;

    @RequestMapping(value = "slgaccount", method = RequestMethod.GET)
    public ModelAndView slgAccount() {
        /**
         * 查看SLG账号统计，可以根据相应条件筛选
         */
        ModelAndView mav = new ModelAndView();
        LOGGER.info("SJL Account Start");
        mav.setViewName("admin/SLG/accountstatis");
        return mav;
    }

    @RequestMapping(value = "query", method = RequestMethod.POST)
    public void query(Page<SlgAccountStatis> page, SlgAccountStatis filter, HttpServletRequest request, HttpServletResponse response) {
        int startIndex = Integer.parseInt(request.getParameter("iDisplayStart"));
        int pageSize = Integer.parseInt(request.getParameter("iDisplayLength"));
        String DateRange = filter.getDateRange();
        LOGGER.info("dataRange : " + filter.getDateRange());
        try {
            if (null != DateRange && !DateRange.equals("")) {
                String[] dateS = DateRange.split("&");
                String sdateStr = dateS[0].trim();
                String edateStr = dateS[1].trim();
                filter.setSdate(df.parse(sdateStr).getTime());
                filter.setEdate(df.parse(edateStr).getTime()+86400000L);
            }
            Calendar instance = Calendar.getInstance();
            if (filter.getEdate() == 0L) {
                instance.add(Calendar.DATE, 1);
                String edate = df.format(instance.getTime());
                long e = df.parse(edate).getTime();
                filter.setEdate(e);
                LOGGER.info("Edate: " + edate +"; " + e);
            }
            if (filter.getSdate() == 0L) {
                instance.add(Calendar.DATE, -3);
                String sdate = df.format(instance.getTime());
                long s = df.parse(sdate).getTime();
                filter.setSdate(s);
                LOGGER.info("Sdate: " + sdate+"; "+ s);
            }
        }catch (Exception e){
            LOGGER.error(e);
        }
        page.setPage((startIndex + pageSize) / pageSize);
        page.setPageSize(Integer.parseInt(request.getParameter("iDisplayLength")));
        page = slgAccountService.findPage(page, filter);
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("sEcho", request.getParameter("sEcho"));
        ret.put("aaData", page.getResults());
        ret.put("iTotalRecords", page.getTotalRows());
        ret.put("iTotalDisplayRecords", page.getTotalRows());
        ServletUtils.responseJson(response, ret);
    }

}
