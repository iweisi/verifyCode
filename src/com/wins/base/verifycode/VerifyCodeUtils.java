package com.wins.base.verifycode;

import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

public class VerifyCodeUtils{

    public static final String VERIFY_CODE_SESSION_KEY = "VERIFY_CODE_SESSION_KEY";

    private static Random random = new Random();

    public static VerifyEnum verifyByCode(HttpServletRequest request, String code){
        HttpSession session = request.getSession();
        if(session == null){
            return VerifyEnum.TIMED_OUT;
        }
        String realVerifyCode = (String)session.getAttribute(VerifyCodeUtils.VERIFY_CODE_SESSION_KEY);
        if(StringUtils.isEmpty(realVerifyCode)){
            return VerifyEnum.NULL;
        }
        if(!realVerifyCode.equalsIgnoreCase(code)){
            return VerifyEnum.ERROR;
        }
        if(realVerifyCode.equalsIgnoreCase(code)){
            session.removeAttribute(VerifyCodeUtils.VERIFY_CODE_SESSION_KEY);
            return VerifyEnum.SUCCES;
        }
        return VerifyEnum.ERROR;
    }

    /**
     * 使用系统默认字符源生成验证码 不含中文
     * @param verifySize	验证码长度
     * @return
     */
    public static String generateVerifyCode(int verifySize){
        return generateVerifyCode(verifySize, VERIFY_CODES);
    }

    /**
     * 使用指定源生成验证码 包括随机中文
     * @param verifySize	验证码长度
     * @return
     */

    public static String generateVerifyCodeHasChinese(int verifySize){
        //中文出现的位置
        int codesLen = VERIFY_CODES.length();
        int cn_codesLen = CHINESE_CODES.length();
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder verifyCode = new StringBuilder(verifySize);
        for(int i = 0; i < verifySize; i++){
            int hasChinese = (int)(Math.random()*4);
            if(hasChinese == 0){
                verifyCode.append(CHINESE_CODES.charAt(rand.nextInt(cn_codesLen-1)));
            }else {
                verifyCode.append(VERIFY_CODES.charAt(rand.nextInt(codesLen-1)));
            }
        }
        return verifyCode.toString();
    }

    /**
     * 使用指定源生成验证码
     * @param verifySize	验证码长度
     * @param sources	验证码字符源
     * @return
     */
    public static String generateVerifyCode(int verifySize, String sources){
        if(sources == null || sources.length() == 0){
            sources = VERIFY_CODES;
        }

        int codesLen = sources.length();
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder verifyCode = new StringBuilder(verifySize);
        for(int i = 0; i < verifySize; i++){
            verifyCode.append(sources.charAt(rand.nextInt(codesLen-1)));
        }
        return verifyCode.toString();
    }

    /**
     * 生成随机验证码文件,并返回验证码值
     * @param w
     * @param h
     * @param outputFile
     * @param verifySize
     * @return
     * @throws IOException
     */
    public static String outputVerifyImage(int w, int h, File outputFile, int verifySize) throws IOException{
        String verifyCode = generateVerifyCode(verifySize);
        outputImage(w, h, outputFile, verifyCode);
        return verifyCode;
    }

    /**
     * 输出随机验证码图片流,并返回验证码值
     * @param w
     * @param h
     * @param os
     * @param verifySize
     * @return
     * @throws IOException
     */
    public static String outputVerifyImage(int w, int h, OutputStream os, int verifySize) throws IOException{
        String verifyCode = generateVerifyCode(verifySize);
        outputImage(w, h, os, verifyCode);
        return verifyCode;
    }

    /**
     * 生成指定验证码图像文件
     * @param w
     * @param h
     * @param outputFile
     * @param code
     * @throws IOException
     */
    public static void outputImage(int w, int h, File outputFile, String code) throws IOException{
        if(outputFile == null){
            return;
        }
        File dir = outputFile.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        try{
            outputFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(outputFile);
            outputImage(w, h, fos, code);
            fos.close();
        } catch(IOException e){
            throw e;
        }
    }

    /**
     * 输出指定验证码图片流
     * @param w
     * @param h
     * @param os
     * @param code
     * @throws IOException
     */
    public static void outputImage(int w, int h, OutputStream os, String code) throws IOException{
        int verifySize = code.length();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Random rand = new Random();
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color[] colors = new Color[5];
        Color[] colorSpaces = new Color[] { Color.WHITE, Color.CYAN,
                Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE,
                Color.PINK, Color.YELLOW };
        float[] fractions = new float[colors.length];
        for(int i = 0; i < colors.length; i++){
            colors[i] = colorSpaces[rand.nextInt(colorSpaces.length)];
            fractions[i] = rand.nextFloat();
        }
        Arrays.sort(fractions);

        g2.setColor(Color.GRAY);// 设置边框色
        g2.fillRect(0, 0, w, h);

        Color c = getRandColor(200, 250);
        g2.setColor(c);// 设置背景色
        g2.fillRect(0, 2, w, h-4);

        //绘制干扰线
        Random random = new Random();
        g2.setColor(getRandColor(160, 200));// 设置线条的颜色
        for (int i = 0; i < 80; i++) {
            int x = random.nextInt(w - 1);
            int y = random.nextInt(h - 1);
            int xl = random.nextInt(6) + 1;
            int yl = random.nextInt(12) + 1;
            g2.drawLine(x, y, x + xl + 40, y + yl + 20);
        }

        // 添加噪点
        float yawpRate = 0.07f;// 噪声率
        int area = (int) (yawpRate * w * h);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int rgb = getRandomIntColor();
            image.setRGB(x, y, rgb);
        }

        shear(g2, w, h, c);// 使图片扭曲

        g2.setColor(getRandColor(100, 160));
        int fontSize = h-6;
        Font font = new Font(Font.MONOSPACED, Font.CENTER_BASELINE, fontSize);
        g2.setFont(font);
        char[] chars = code.toCharArray();
        for(int i = 0; i < verifySize; i++){
            AffineTransform affine = new AffineTransform();
            affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1), (w / verifySize) * i + fontSize/2, h/2);
            g2.setTransform(affine);
            g2.drawChars(chars, i, 1, ((w-10) / verifySize) * i + 5, h/2 + fontSize/2 - 10);
        }

        g2.dispose();
        ImageIO.write(image, "jpg", os);
    }

    private static Color getRandColor(int fc, int bc) {
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static int getRandomIntColor() {
        int[] rgb = getRandomRgb();
        int color = 0;
        for (int c : rgb) {
            color = color << 8;
            color = color | c;
        }
        return color;
    }

    private static int[] getRandomRgb() {
        int[] rgb = new int[3];
        for (int i = 0; i < 3; i++) {
            rgb[i] = random.nextInt(255);
        }
        return rgb;
    }

    private static void shear(Graphics g, int w1, int h1, Color color) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color);
    }

    private static void shearX(Graphics g, int w1, int h1, Color color) {

        int period = random.nextInt(2);

        boolean borderGap = true;
        int frames = 1;
        int phase = random.nextInt(2);

        for (int i = 0; i < h1; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period
                    + (6.2831853071795862D * (double) phase)
                    / (double) frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
            if (borderGap) {
                g.setColor(color);
                g.drawLine((int) d, i, 0, i);
                g.drawLine((int) d + w1, i, w1, i);
            }
        }

    }

    private static void shearY(Graphics g, int w1, int h1, Color color) {

        int period = random.nextInt(40) + 10; // 50;

        boolean borderGap = true;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < w1; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period
                    + (6.2831853071795862D * (double) phase)
                    / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(i, (int) d, i, 0);
                g.drawLine(i, (int) d + h1, i, h1);
            }

        }

    }
//    public static void main(String[] args) throws IOException{
//        File dir = new File("F:/verifies");
//        int w = 200, h = 80;
//        for(int i = 0; i < 50; i++){
//            String verifyCode = generateVerifyCode(4);
//            File file = new File(dir, verifyCode + ".jpg");
//            outputImage(w, h, file, verifyCode);
//        }
//    }

    //使用到字体，系统里没有的话需要安装字体，字体只显示大写，去掉了1,0,i,o几个容易混淆的字符
    public static final String VERIFY_CODES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    public static final String CHINESE_CODES =
            "的一了是我不在人们有来他这" +
            "上着个地到大里说就去子得也和" +
            "那要下看天时过出小么起你都把" +
            "好还多没为又可家学只以主会样" +
            "年想生同老中十从自面前头道它" +
            "后然走很像见两用她国动进成回" +
            "什边作对开而己些现山民候经发" +
            "工向事命给长水几义三声于高手" +
            "知理眼志点心战二问但身方实吃" +
            "做叫当住听革打呢真全才四已所" +
            "敌之最光产情路分总条白话东席" +
            "次亲如被花口放儿常气五第使写" +
            "军吧文运再果怎定许快明行因别" +
            "飞外树物活部门无往船望新带队" +
            "先力完却站代员机更九您每风级" +
            "跟笑啊孩万少直意夜比阶连车重" +
            "便斗马哪化太指变社似士者干石" +
            "满日决百原拿群究各六本思解立" +
            "河村八难早论吗根共让相研今其" +
            "书坐接应关信觉步反处记将千找" +
            "争领或师结块跑谁草越字加脚紧" +
            "爱等习阵怕月青半火法题建赶位" +
            "唱海七女任件感准张团屋离色脸" +
            "片科倒睛利世刚且由送切星导晚" +
            "表够整认响雪流未场该并底深刻" +
            "平伟忙提确近亮轻讲农古黑告界" +
            "拉名呀土清阳照办史改历转画造" +
            "嘴此治北必服雨穿内识验传业菜" +
            "爬睡兴形量咱观苦体众通冲合破" +
            "友度术饭公旁房极南枪读沙岁线" +
            "野坚空收算至政城劳落钱特围弟" +
            "胜教热展包歌类渐强数乡呼性音" +
            "答哥际旧神座章帮啦受系令跳非" +
            "何牛取入岸敢掉忽种装顶急林停" +
            "息句区衣般报叶压慢叔背细";
}
