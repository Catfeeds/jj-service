package com.cdkj.service.api.impl;

import com.cdkj.service.ao.IQualifyAO;
import com.cdkj.service.api.AProcessor;
import com.cdkj.service.dto.req.XN612017Req;
import com.cdkj.service.exception.BizException;
import com.cdkj.service.exception.ParaException;
import com.cdkj.service.spring.SpringContextHolder;

/**
 * 详情查询资质
 * @author: asus 
 * @since: 2017年6月4日 下午4:16:52 
 * @history:
 */
public class XN612017 extends AProcessor {
    private IQualifyAO qualifyAO = SpringContextHolder
        .getBean(IQualifyAO.class);

    private XN612017Req req = null;

    @Override
    public Object doBusiness() throws BizException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void doCheck(String inputparams) throws ParaException {
        // TODO Auto-generated method stub

    }

}
