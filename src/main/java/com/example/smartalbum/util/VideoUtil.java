package com.example.smartalbum.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<Integer, String> info = new HashMap<>();
        log.info("======开始将图片转换为视频======");
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
                info.put(i, command);
//                processes.add(Runtime.getRuntime().exec(command));
                processes.add(Runtime.getRuntime().exec(new String[]{"bash", "-c", command}));
            }

            //等待所有子线程执行完毕
            for (Process process : processes) {
                //不知道啥原因，不执行这些语句的话，主线程会一直被阻塞
                printProcessMsg(process);
                process.getOutputStream().close();
                //等待
                process.waitFor();
            }

            for (int i = 0; i < 10; i++) {
                if (checkTsFile(outputPath, info)){
                    break;
                }

            }
            log.info("======ts文件检查完毕======");
        } else {
            log.error("======图片转视频失败, 找不到图片!======");
        }
        log.info("======图片转视频完毕======");
    }

    private boolean checkTsFile(String outputPath, Map<Integer,String> info) throws IOException, InterruptedException {
        log.info("======检查ts文件是否全部生成======");
        boolean result = true;
        File[] videos = new File(outputPath).listFiles();
        if (videos != null) {
            List<Process> processes = new ArrayList<>();
            for (File video : videos) {
                if (video.length() <= 0) {
                    result = false;
                    String videoName = video.getName();
                    log.info("======发现空的ts文件:{}, 正在重新生成======", videoName);
                    String name = videoName.substring(0, videoName.lastIndexOf("."));
                    Integer id = Integer.valueOf(name);
                    String command = info.get(id);
                    processes.add(Runtime.getRuntime().exec(new String[]{"bash", "-c", command}));
                }
            }

            //等待所有子线程执行完毕
            for (Process process : processes) {
                //不知道啥原因，不执行这些语句的话，主线程会一直被阻塞
                printProcessMsg(process);
                process.getOutputStream().close();
                //等待
                process.waitFor();
            }

        }
        return result;
    }

    /**
     * 2、生成待合并视频的文件列表merge.txt，合并视频时以此文件为根据进行合并
     *
     * @param outputPath merge.txt存放的文件夹路径
     * @throws IOException 执行命令行命令时或者关闭流时发生的异常
     */
    public void createMargeTxt(String outputPath) throws IOException {
        log.info("======开始生成合并视频列表文件: merge.txt======");
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
                if (video.length() > 0) {
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
        log.info("======合并视频列表文件: merge.txt生成完毕======");
    }

    /**
     * 3、根据marge.txt 合成所有视频片段，生成最终的视频mp4文件
     *
     * @param outputPath 输出的路径，不需要带文件名
     * @throws IOException          执行命令行命令时或者关闭流时发生的异常
     * @throws InterruptedException 等待子线程完成任务的过程中，子线程被中断时发生异常
     */
    public void mergeVideo(String outputPath) throws InterruptedException, IOException {
        log.info("======开始合并所有视频======");
        String margeTxt = outputPath + "marge.txt";
        String outputVideo = outputPath + "out.mp4";
        String command = fFmpegCommandUtil.mergeVideo(margeTxt, outputVideo);

//        Process exec = Runtime.getRuntime().exec(command);
        Process exec = Runtime.getRuntime().exec(new String[]{"bash", "-c",command});
        printProcessMsg(exec);
        exec.getOutputStream().close();
        exec.waitFor();
        log.info("======视频合并完成======");
    }


    /**
     * 处理process输出流和错误流，防止进程阻塞，在process.waitFor();前调用
     */
    private void printProcessMsg(Process exec) {
        //防止ffmpeg进程塞满缓存造成死锁
        StringBuilder result = new StringBuilder();
        String line;
        try (InputStream error = exec.getErrorStream(); InputStream is = exec.getInputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(error, "GBK"));
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is, "GBK"));

            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
//            log.info("FFMPEG视频转换进程错误信息：" + result);

            result = new StringBuilder();

            while ((line = br2.readLine()) != null) {
                result.append(line).append("\n");
            }
//            log.info("FFMPEG视频转换进程输出内容为：" + result);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

    }
}
