package com.yomahub.liteflow.test.aop;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.slot.DefaultContext;
import com.yomahub.liteflow.spring.ComponentScanner;
import com.yomahub.liteflow.test.BaseTest;
import com.yomahub.liteflow.test.aop.aspect.CmpAspect;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 切面场景单元测试
 * @author Bryan.Zhang
 */
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/aop/application.properties")
@SpringBootTest(classes = GlobalAOPSpringbootTest.class)
@EnableAutoConfiguration
@Import(CmpAspect.class)
@ComponentScan({"com.yomahub.liteflow.test.aop.cmp1","com.yomahub.liteflow.test.aop.cmp2"})
public class GlobalAOPSpringbootTest extends BaseTest {

    @Resource
    private FlowExecutor flowExecutor;

    //测试全局AOP，串行场景
    @Test
    public void testGlobalAopS() {
        LiteflowResponse<DefaultContext> response= flowExecutor.execute2Resp("chain1", "it's a request");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals("before_after", response.getContextBean().getData("a"));
        Assert.assertEquals("before_after", response.getContextBean().getData("b"));
        Assert.assertEquals("before_after", response.getContextBean().getData("c"));
        Assert.assertEquals("before_after", response.getContextBean().getData("d"));
        Assert.assertEquals("before_after", response.getContextBean().getData("e"));
    }

    //测试全局AOP，并行场景
    @Test
    public void testGlobalAopP() {
        LiteflowResponse<DefaultContext> response= flowExecutor.execute2Resp("chain2", "it's a request");
        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals("before_after", response.getContextBean().getData("a"));
        Assert.assertEquals("before_after", response.getContextBean().getData("b"));
        Assert.assertEquals("before_after", response.getContextBean().getData("c"));
        Assert.assertEquals("before_after", response.getContextBean().getData("d"));
        Assert.assertEquals("before_after", response.getContextBean().getData("e"));
    }

    @Test
    public void testGlobalAopException() {
        LiteflowResponse<DefaultContext> response= flowExecutor.execute2Resp("chain3", "it's a request");
        Assert.assertFalse(response.isSuccess());
        Assert.assertEquals("before_after", response.getContextBean().getData("a"));
        Assert.assertEquals("before_after", response.getContextBean().getData("b"));
        Assert.assertEquals("before_after", response.getContextBean().getData("c"));
        Assert.assertEquals("before_after", response.getContextBean().getData("f"));
    }

    @AfterClass
    public static void cleanScanCache(){
        BaseTest.cleanScanCache();
        ComponentScanner.cmpAroundAspect = null;
    }
}
