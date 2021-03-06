package com.cdkj.service.ao.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cdkj.service.ao.ITrainAO;
import com.cdkj.service.bo.ICompanyBO;
import com.cdkj.service.bo.IGsQualifyBO;
import com.cdkj.service.bo.IQualifyBO;
import com.cdkj.service.bo.ISmsOutBO;
import com.cdkj.service.bo.ITrainBO;
import com.cdkj.service.bo.IUserBO;
import com.cdkj.service.bo.base.Paginable;
import com.cdkj.service.core.EGeneratePrefix;
import com.cdkj.service.core.OrderNoGenerater;
import com.cdkj.service.core.StringValidater;
import com.cdkj.service.domain.Company;
import com.cdkj.service.domain.GsQualify;
import com.cdkj.service.domain.Qualify;
import com.cdkj.service.domain.Train;
import com.cdkj.service.domain.User;
import com.cdkj.service.dto.req.XN612090Req;
import com.cdkj.service.dto.req.XN612092Req;
import com.cdkj.service.enums.EBoolean;
import com.cdkj.service.enums.ECompanyStatus;
import com.cdkj.service.exception.BizException;

@Service
public class TrainAOImpl implements ITrainAO {

    @Autowired
    private ITrainBO trainBO;

    @Autowired
    private ISmsOutBO smsOutBO;

    @Autowired
    private ICompanyBO companyBO;

    @Autowired
    private IUserBO userBO;

    @Autowired
    private IQualifyBO qualifyBO;

    @Autowired
    private IGsQualifyBO gsQualifyBO;

    @Override
    public String addTrain(XN612090Req req) {
        String code = null;
        GsQualify gsQualify = gsQualifyBO.queryGsQualifyList(req
            .getCompanyCode());
        if (null != gsQualify
                && gsQualify.getStatus().equals(
                    ECompanyStatus.PASS_YES.getCode())) {
            Train data = new Train();
            code = OrderNoGenerater.generateM(EGeneratePrefix.TRAIN.getCode());
            data.setCode(code);
            data.setName(req.getName());
            data.setPic(req.getPic());
            data.setAdvPic(req.getAdvPic());
            data.setCompanyCode(req.getCompanyCode());

            data.setQuoteMin(StringValidater.toLong(req.getQuoteMin()));
            data.setQuoteMax(StringValidater.toLong(req.getQuoteMax()));
            data.setLectorNum(StringValidater.toInteger(req.getLectorNum()));
            data.setMtrainTimes(StringValidater.toInteger(req.getMtrainTimes()));

            data.setMtrainNum(StringValidater.toInteger(req.getMtrainNum()));
            data.setResume1(req.getResume1());
            data.setResume2(req.getResume2());
            data.setResume3(req.getResume3());
            data.setCourse(req.getCourse());

            data.setLocation(EBoolean.NO.getCode());
            data.setOrderNo(EBoolean.NO.getCode());
            data.setDescription(req.getDescription());
            data.setStatus(EBoolean.YES.getCode());
            data.setPublisher(req.getPublisher());

            data.setPublishDatetime(new Date());
            data.setQualifyCode(gsQualify.getQualifyCode());
            trainBO.saveTrain(data);
        } else {
            throw new BizException("xn0000", "公司还未获得该资质");
        }
        return code;
    }

    @Override
    public void editTrain(XN612092Req req) {
        Train data = trainBO.getTrain(req.getCode());
        data.setName(req.getName());
        data.setPic(req.getPic());
        data.setAdvPic(req.getAdvPic());
        data.setQuoteMin(StringValidater.toLong(req.getQuoteMin()));
        data.setQuoteMax(StringValidater.toLong(req.getQuoteMax()));

        data.setLectorNum(StringValidater.toInteger(req.getLectorNum()));
        data.setMtrainTimes(StringValidater.toInteger(req.getMtrainTimes()));
        data.setMtrainNum(StringValidater.toInteger(req.getMtrainNum()));
        data.setResume1(req.getResume1());
        data.setResume2(req.getResume2());

        data.setResume3(req.getResume3());
        data.setCourse(req.getCourse());
        data.setDescription(req.getDescription());
        data.setPublisher(req.getPublisher());
        data.setPublishDatetime(new Date());
        trainBO.refreshTrain(data);
    }

    @Override
    public void dropTrain(String code) {
        if (!trainBO.isTrainExist(code)) {
            throw new BizException("xn0000", "记录编号不存在");
        }
        trainBO.removeTrain(code);
    }

    @Override
    public Paginable<Train> queryTrainPage(int start, int limit, Train condition) {
        Paginable<Train> page = trainBO.getPaginable(start, limit, condition);
        List<Train> list = page.getList();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Train train : list) {
                Company company = companyBO.getCompany(train.getCompanyCode());
                User user = userBO.getRemoteUser(company.getUserId());
                company.setRealName(user.getNickname());
                Qualify qualify = qualifyBO.getQualify(train.getQualifyCode());
                train.setQualityName(qualify.getName());
                train.setCompany(company);
            }
        }
        return page;
    }

    @Override
    public List<Train> queryTrainList(Train condition) {
        return trainBO.queryTrainList(condition);
    }

    @Override
    public Train getTrain(String code) {
        return trainBO.getTrain(code);
    }

    @Override
    public void wgclTrain(String code, String dealer, String dealNote) {
        Train train = trainBO.getTrain(code);
        if (EBoolean.NO.getCode().equals(train.getStatus())) {
            throw new BizException("xn0000", "该服务已做违规处理");
        }
        String publisher = train.getPublisher();
        smsOutBO.sentContent(publisher, "尊敬的企业，您所发布的培训服务[" + train.getName()
                + "]已做违规处理，违规原因[" + dealNote + "]。");
        trainBO.wgclTrain(train, dealer, dealNote);
    }

    @Override
    public void editLocation(String code, String location, String orderNo,
            String dealer) {
        Train train = trainBO.getTrain(code);
        if (EBoolean.NO.getCode().equals(train.getStatus())) {
            throw new BizException("xn0000", "服务已违规,不可设置为热门");
        }
        if (!EBoolean.NO.getCode().equals(orderNo)) {
            List<Train> trainList = trainBO.queryTrainList(
                EBoolean.YES.getCode(), location, orderNo);
            if (CollectionUtils.isNotEmpty(trainList)) {
                throw new BizException("xn0000", "顺序重复");
            }
        }
        trainBO.refreshLocation(train, location, orderNo, dealer);
    }
}
