/**
 * <p>Title: liteflow</p>
 * <p>Description: 轻量级的组件式流程框架</p>
 *
 * @author Bryan.Zhang
 * @email weenyc31@163.com
 * @Date 2020/4/1
 */
package com.yomahub.liteflow.entity.flow;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;

import com.yomahub.liteflow.entity.data.DataBus;
import com.yomahub.liteflow.entity.data.Slot;
import com.yomahub.liteflow.enums.ExecuteTypeEnum;
import com.yomahub.liteflow.exception.FlowSystemException;
import com.yomahub.liteflow.property.LiteflowConfig;
import com.yomahub.liteflow.util.SpringAware;

/**
 * chain对象，实现可执行器
 * @author Bryan.Zhang
 */
public class Chain implements Executable {
    
    private String chainName;

    private List<Condition> conditionList;

    private static int whenMaxWaitSeconds;

    static {
        LiteflowConfig liteflowConfig = SpringAware.getBean(LiteflowConfig.class);
        if (ObjectUtil.isNotNull(liteflowConfig)) {
            whenMaxWaitSeconds = liteflowConfig.getWhenMaxWaitSeconds();
        } else {
            whenMaxWaitSeconds = 15;
        }
    }

    public Chain(String chainName, List<Condition> conditionList) {
        this.chainName = chainName;
        this.conditionList = conditionList;
    }

    public List<Condition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<Condition> conditionList) {
        this.conditionList = conditionList;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    //执行chain的主方法
    @Override
    public void execute(Integer slotIndex) throws Exception {
        if (CollUtil.isEmpty(conditionList)) {
            throw new FlowSystemException("no conditionList in this chain[" + chainName + "]");
        }

        Slot slot = DataBus.getSlot(slotIndex);

        //循环chain里包含的condition，每一个condition有可能是then，也有可能是when
        //when的话为异步，用闭锁进行等待，所有when结束后才能进入下一个condition
        for (Condition condition : conditionList) {
            if (condition instanceof ThenCondition) {
                for (Executable executableItem : condition.getNodeList()) {
                    try {
                        executableItem.execute(slotIndex);
                    } catch (Exception e) {
                        throw e;
                    }
                }
            } else if (condition instanceof WhenCondition) {
                final CountDownLatch latch = new CountDownLatch(condition.getNodeList().size());
                for (Executable executableItem : condition.getNodeList()) {
                    new WhenConditionThread(executableItem, slotIndex, slot.getRequestId(), latch).start();
                }
                latch.await(whenMaxWaitSeconds, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public ExecuteTypeEnum getExecuteType() {
        return ExecuteTypeEnum.CHAIN;
    }

    @Override
    public String getExecuteName() {
        return chainName;
    }
}
