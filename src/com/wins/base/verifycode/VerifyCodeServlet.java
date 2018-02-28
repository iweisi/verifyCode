package com.wins.base.verifycode;

import dckj.core.base.ConfigService;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class VerifyCodeServlet extends HttpServlet {

    //步骤
    //1.定义BufferedImage对象
    //2.获得Graphics
    //3.通过Random长生随机验证码信息
    //4.使用Graphics绘制图片
    //5.记录验证码信息到session中
    //6.使用ImageIO输出图片
    public static final int sessionTimeOut = Integer.parseInt((String) ConfigService.getContextProperty("verificationCode.maxtime")) ; //单位：秒

    public static final String openchinese = ((String) ConfigService.getContextProperty("verificationCode.chinese")) ; //验证码长度 4-6

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        String verifyCode = null;
        //生成随机长度
        int length = (int)(Math.random()*3+4);
        //生成随机字串
        if(!StringUtils.isEmpty(openchinese) && openchinese.equals("1")){
            verifyCode = VerifyCodeUtils.generateVerifyCodeHasChinese(length);
        }else{
            verifyCode = VerifyCodeUtils.generateVerifyCode(length);
        }

        //存入会话session
        HttpSession session = request.getSession();
        session.setAttribute(VerifyCodeUtils.VERIFY_CODE_SESSION_KEY, verifyCode.toLowerCase());
        session.setMaxInactiveInterval(sessionTimeOut);
        //生成图片
        int w = 200, h = 80;
        if(length == 5){
            w += 80;
        }
        if(length == 6){
            w += 150;
        }

        VerifyCodeUtils.outputImage(w, h, response.getOutputStream(), verifyCode);

    }

}