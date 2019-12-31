package io.github.selcukes.reports.video;

import io.github.selcukes.core.exception.RecorderException;

import java.awt.*;
import java.io.File;

import io.github.selcukes.core.helper.FileHelper;
import io.github.selcukes.core.logging.Logger;
import io.github.selcukes.core.logging.LoggerFactory;
import org.monte.media.Format;


import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;

import static org.monte.media.VideoFormatKeys.*;

public class VideoRecorder extends Recorder {
    final Logger logger= LoggerFactory.getLogger(VideoRecorder.class);
    private final MonteScreenRecorder screenRecorder;
    private VideoConfig videoConfig;

    public VideoRecorder() {
        this.videoConfig = conf();
        this.screenRecorder = getScreenRecorder();
    }
    /**
     * This method will start the recording of the execution.
     */
    public void start() {
        screenRecorder.start();
        logger.info(()->"Recording started");
    }
    /**
     * This method will stop and save's the recording.
     */
    public File stopAndSave(String filename) {
        File video = writeVideo(filename);
        setLastVideo(video);
        logger.info(()->"Recording finished to " + video.getAbsolutePath());
        return video;
    }
    /**
     * This method will delete the recorded file,if the test is pass.
     */
    public void stopAndDelete(String filename)
    {
        File video = writeVideo(filename);
        logger.info(()->"Trying to delete recorded video files...");
        video.deleteOnExit();
    }
    private File writeVideo(String filename) {
        try {
            return screenRecorder.saveAs(filename);
        } catch (IndexOutOfBoundsException ex) {
            throw new RecorderException("Video recording wasn't started");
        }
    }

    private GraphicsConfiguration getGraphicConfig() {
        return GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();
    }

    private MonteScreenRecorder getScreenRecorder() {
        int frameRate = videoConfig.getFrameRate();

        Format fileFormat = new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, FormatKeys.MIME_AVI);
        Format screenFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey,
                ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                DepthKey, 24, FrameRateKey, Rational.valueOf(frameRate),
                QualityKey, 1.0f,
                KeyFrameIntervalKey, 15 * 60);
        Format mouseFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                FrameRateKey, Rational.valueOf(frameRate));

        Dimension screenSize = videoConfig.getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        Rectangle captureSize = new Rectangle(0, 0, width, height);

        return MonteScreenRecorderBuilder
                .builder()
                .setGraphicConfig(getGraphicConfig())
                .setRectangle(captureSize)
                .setFileFormat(fileFormat)
                .setScreenFormat(screenFormat)
                .setFolder(new File(videoConfig.getVideoFolder()))
                .setMouseFormat(mouseFormat).build();
    }
}