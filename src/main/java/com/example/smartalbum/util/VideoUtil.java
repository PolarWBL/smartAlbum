package com.example.smartalbum.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
@Component
public class VideoUtil {

    @Resource
    private FFmpegCommandUtil fFmpegCommandUtil;

    /**
     * 1、把图片转化为一个有淡入淡出效果的ts视频(可改成后缀为mp4等任意视频，后缀ts是为了方便区分^_^)
     *
     * @param inputPath  图片的文件夹路径
     * @param outputPath ts文件输出的文件夹路径
     * @throws IOException          执行命令行命令时或者关闭流时发生的异常
     * @throws InterruptedException 等待子线程完成任务的过程中，子线程被中断时发生异常
     */
    public void createMp4(String inputPath, String outputPath) throws IOException, InterruptedException {
        System.out.println("======进入了createMp4方法======");
        File[] files = new File(inputPath).listFiles();
        if (files != null) {
            //图片转化为视频
            List<Process> processes = new ArrayList<>();
            String command;
            String img;
            String outputVideo;
            for (int i = 0; i < files.length; i++) {
                img = files[i].getAbsolutePath();
                outputVideo = outputPath + i + ".ts";
                command = fFmpegCommandUtil.createFadeVideo(img, outputVideo);
//                processes.add(Runtime.getRuntime().exec(command));
                processes.add(Runtime.getRuntime().exec(new String[]{"bash", "-c",command}));
            }

            //等待所有子线程执行完毕
            for (Process process : processes) {
                //不知道啥原因，不执行这些语句的话，主线程会一直被阻塞
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
                //等待
                process.waitFor();
            }

        }
        System.out.println("======退出了createMp4方法======");
    }

    /**
     * 2、生成待合并视频的文件列表merge.txt，合并视频时以此文件为根据进行合并
     *
     * @param outputPath merge.txt存放的文件夹路径
     * @throws IOException 执行命令行命令时或者关闭流时发生的异常
     */
    public void createMargeTxt(String outputPath) throws IOException {
        System.out.println("======进入了createMargeTxt方法======");
        //生成的待合成视频数组
        File[] videos = new File(outputPath).listFiles();
        if (videos != null) {
            String margePath = outputPath + "marge.txt";
            File file = new File(margePath);
            if (file.exists()) {
                file.delete();
            }

            //把所有生成的ts文件的完整路径写入marge.txt
            StringBuilder margeTxt = new StringBuilder();
            for (File video : videos) {
                if (video.length() >= 0) {
                    if (video.getName().contains(".ts")) {
                        //构建内容
                        margeTxt.append("file '").append(video.getAbsolutePath()).append("'\n");
                    }
                }
            }

            //写入文件
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
                writer.write(String.valueOf(margeTxt));
            }
        }
        System.out.println("======退出了createMargeTxt方法======");
    }

    /**
     * 3、根据marge.txt 合成所有视频片段，生成最终的视频mp4文件
     *
     * @param outputPath 输出的路径，不需要带文件名
     * @throws IOException          执行命令行命令时或者关闭流时发生的异常
     * @throws InterruptedException 等待子线程完成任务的过程中，子线程被中断时发生异常
     */
    public void mergeVideo(String outputPath) throws InterruptedException, IOException {
        System.out.println("======进入了mergeVideo方法======");
        String margeTxt = outputPath + "marge.txt";
        String outputVideo = outputPath + "out.mp4";
        String command = fFmpegCommandUtil.mergeVideo(margeTxt, outputVideo);

//        Process exec = Runtime.getRuntime().exec(command);
        Process exec = Runtime.getRuntime().exec(new String[]{"bash", "-c",command});
        printProcessMsg(exec);
        exec.getOutputStream().close();
        exec.waitFor();
        System.out.println("======退出了mergeVideo方法======");
    }


    /**
     * 处理process输出流和错误流，防止进程阻塞，在process.waitFor();前调用
     * @param exec
     * @throws IOException
     */
    private void printProcessMsg(Process exec) throws IOException {
        //防止ffmpeg进程塞满缓存造成死锁
        InputStream error = exec.getErrorStream();
        InputStream is = exec.getInputStream();

        StringBuffer result = new StringBuffer();
        String line = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(error,"GBK"));
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is,"GBK"));

            while((line = br.readLine()) != null){
                result.append(line).append("\n");
            }
            log.info("FFMPEG视频转换进程错误信息："+result.toString());

            result = new StringBuffer();
            line = null;

            while((line = br2.readLine()) != null){
                result.append(line).append("\n");
            }
            log.info("FFMPEG视频转换进程输出内容为："+result.toString());
        }catch (IOException e2){
            e2.printStackTrace();
        }finally {
            error.close();
            is.close();
        }

    }
}
