package com.mycode.myscreenshot;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class App {

    static int length = 0;

    public static void main(String[] args) throws Exception {
        System.out.print("スクリーンショットくん起動中...");
        Main main = new Main();
        main.addRouteBuilder(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("timer:foo?repeatCount=1").process(new Processor() {

                    @Override
                    public void process(Exchange exchng) throws Exception {
                        System.out.println("起動が完了しました。\n\n　PrintScreenキー ・・・ スクリーンショットを撮影・保存\n　Ctrl+C または ウィンドウの閉じるボタン ・・・ プログラムを終了\n");
                    }
                });
                from("timer:foo?period=1s").choice().when(new Predicate() {

                    @Override
                    public boolean matches(Exchange exchange) {
                        try {
                            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                            BufferedImage bi = (BufferedImage) (Image) clip.getData(DataFlavor.imageFlavor);
                            byte[] imageInByte;
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                ImageIO.write(bi, "png", baos);
                                baos.flush();
                                imageInByte = baos.toByteArray();
                            }

                            if (length == 0) {
                                length = imageInByte.length;
                                return false;
                            } else if (length != imageInByte.length) {
                                length = imageInByte.length;
                                String format = new SimpleDateFormat("MMdd_HHmmss").format(new Date()) + ".png";
                                exchange.getIn().setHeader(Exchange.FILE_NAME, format);
                                exchange.getIn().setBody(imageInByte);
                                System.out.println("保存しました。 " + format);
                                return true;
                            }
                        } catch (UnsupportedFlavorException | IOException | NullPointerException e) {

                        }
                        return false;
                    }
                }).to("file:images");
            }
        });
        main.run();
    }
}
