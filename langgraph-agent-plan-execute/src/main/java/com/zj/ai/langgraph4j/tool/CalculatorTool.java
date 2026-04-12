package com.zj.ai.langgraph4j.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 计算器工具
 * 支持基本的数学运算
 *
 * @author zj
 * @date 2026/04/12
 */
@Component
public class CalculatorTool {

    /**
     * 执行数学计算
     * 支持加减乘除和简单表达式
     *
     * @param expression 数学表达式，如 "25 * 4 + 10"
     * @return 计算结果
     */
    @Tool("执行数学计算，支持加减乘除运算。输入数学表达式，返回计算结果。")
    public String calculate(@P("数学表达式，如 '25 * 4 + 10'") String expression) {
        try {
            // 简单的表达式计算
            expression = expression.trim().replaceAll("\\s+", "");

            // 处理加减乘除
            double result = evaluateExpression(expression);
            return String.format("计算结果: %s = %.2f", expression, result);

        } catch (Exception e) {
            return "计算错误: " + e.getMessage();
        }
    }

    /**
     * 加法运算
     */
    @Tool("执行加法运算")
    public String add(@P("第一个数") double a, @P("第二个数") double b) {
        return String.format("%.2f + %.2f = %.2f", a, b, a + b);
    }

    /**
     * 乘法运算
     */
    @Tool("执行乘法运算")
    public String multiply(@P("第一个数") double a, @P("第二个数") double b) {
        return String.format("%.2f × %.2f = %.2f", a, b, a * b);
    }

    /**
     * 简单表达式求值
     * 支持 + - * / ()
     */
    private double evaluateExpression(String expr) {
        // 简单实现：使用递归下降解析
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                return x;
            }
        }.parse();
    }
}
