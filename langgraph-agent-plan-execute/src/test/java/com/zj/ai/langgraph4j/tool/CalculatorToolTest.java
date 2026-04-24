package com.zj.ai.langgraph4j.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 计算器工具测试
 *
 * @author zj
 * @date 2026/04/24
 */
class CalculatorToolTest {

    private CalculatorTool calculatorTool;

    @BeforeEach
    void setUp() {
        calculatorTool = new CalculatorTool();
    }

    @Test
    void testCalculate() {
        String result = calculatorTool.calculate("25 * 4 + 10");
        assertNotNull(result);
        assertTrue(result.contains("110"));
    }

    @Test
    void testCalculateWithParentheses() {
        String result = calculatorTool.calculate("(10 + 5) * 2");
        assertNotNull(result);
        assertTrue(result.contains("30"));
    }

    @Test
    void testCalculateDivision() {
        String result = calculatorTool.calculate("100 / 4");
        assertNotNull(result);
        assertTrue(result.contains("25"));
    }

    @Test
    void testCalculateSubtraction() {
        String result = calculatorTool.calculate("100 - 50");
        assertNotNull(result);
        assertTrue(result.contains("50"));
    }

    @Test
    void testAdd() {
        String result = calculatorTool.add(10.0, 20.0);
        assertNotNull(result);
        assertTrue(result.contains("30"));
    }

    @Test
    void testMultiply() {
        String result = calculatorTool.multiply(5.0, 6.0);
        assertNotNull(result);
        assertTrue(result.contains("30"));
    }

    @Test
    void testCalculateInvalidExpression() {
        String result = calculatorTool.calculate("abc");
        assertTrue(result.contains("错误"));
    }
}
